package com.truthlens.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.truthlens.app.domain.model.AppMonitorConfig
import com.truthlens.app.domain.usecase.AnalyzeContentUseCase
import com.truthlens.app.domain.repository.AppSettingsRepository
import com.truthlens.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@AndroidEntryPoint
class TruthLensAccessibilityService : AccessibilityService() {

    @Inject lateinit var analyzeContentUseCase: AnalyzeContentUseCase
    @Inject lateinit var appSettingsRepository: AppSettingsRepository
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Track last scanned text per package to avoid re-scanning identical content
    private val lastScannedText = ConcurrentHashMap<String, String>()
    // Cooldown: timestamp of last alert per package
    private val lastAlertTime = ConcurrentHashMap<String, Long>()
    // Debounce job per package
    private val debounceJobs = ConcurrentHashMap<String, Job>()

    private var currentPackage: String = ""
    private var enabledPackages: Set<String> = emptySet()
    private var masterEnabled: Boolean = false
    private var scanDelayMs: Long = 2000L

    // Nodes that should never be scanned (privacy protection)
    private val sensitiveHints = setOf(
        "password", "passcode", "pin", "cvv", "card number",
        "credit card", "debit card", "bank", "ssn", "social security"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        Timber.d("TruthLens AccessibilityService connected")
        applyDynamicPackageFilter()
        observeSettings()
    }

    private fun observeSettings() {
        serviceScope.launch {
            userPreferencesRepository.get().collect { prefs ->
                masterEnabled = prefs.masterMonitoringEnabled
                scanDelayMs = (prefs.scanDelaySeconds * 1000L).coerceAtLeast(500L)
            }
        }
        serviceScope.launch {
            appSettingsRepository.getEnabledPackages().collect { packages ->
                enabledPackages = packages
                applyDynamicPackageFilter()
            }
        }
    }

    private fun applyDynamicPackageFilter() {
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                         AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                         AccessibilityEvent.TYPE_VIEW_SCROLLED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 500
            packageNames = if (enabledPackages.isEmpty()) null
                           else enabledPackages.toTypedArray()
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        if (!masterEnabled) return

        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return // ignore our own app
        if (pkg in AppMonitorConfig.BLOCKED_PACKAGES) return
        if (pkg !in enabledPackages) return

        currentPackage = pkg

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> scheduleContentScan(pkg)
        }
    }

    private fun scheduleContentScan(packageName: String) {
        // Cancel existing debounce job for this package
        debounceJobs[packageName]?.cancel()
        debounceJobs[packageName] = serviceScope.launch {
            delay(scanDelayMs)
            if (!isActive) return@launch
            scanContent(packageName)
        }
    }

    private suspend fun scanContent(packageName: String) {
        try {
            val rootNode = rootInActiveWindow ?: return
            if (isSensitiveWindow(rootNode)) {
                rootNode.recycle()
                return
            }

            val text = extractTextFromNode(rootNode).trim()
            rootNode.recycle()

            if (text.length < MIN_TEXT_LENGTH) return
            if (text == lastScannedText[packageName]) return // same content, skip

            // Cooldown check — don't show alert for the same package more than once per COOLDOWN_MS
            val now = System.currentTimeMillis()
            val lastAlert = lastAlertTime[packageName] ?: 0L
            if (now - lastAlert < ALERT_COOLDOWN_MS) return

            lastScannedText[packageName] = text

            val appName = appSettingsRepository.getByPackage(packageName)?.appName ?: packageName

            Timber.d("Scanning content in $appName (${text.length} chars)")
            val result = analyzeContentUseCase(text, packageName, appName)

            lastAlertTime[packageName] = System.currentTimeMillis()

            // Broadcast result to OverlayService
            val intent = Intent(OverlayService.ACTION_SHOW_OVERLAY).apply {
                putExtra(OverlayService.EXTRA_RISK_LEVEL, result.riskLevel.name)
                putExtra(OverlayService.EXTRA_SUMMARY, result.summary)
                putExtra(OverlayService.EXTRA_RESULT_ID, result.id)
                putExtra(OverlayService.EXTRA_CONFIDENCE, result.confidenceScore)
                setPackage(this@TruthLensAccessibilityService.packageName)
            }
            sendBroadcast(intent)
        } catch (e: Exception) {
            Timber.e(e, "Error scanning content for $packageName")
        }
    }

    private fun isSensitiveWindow(root: AccessibilityNodeInfo): Boolean {
        return try {
            traverseNode(root) { node ->
                val hint = node.hintText?.toString()?.lowercase() ?: ""
                val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
                val viewId = node.viewIdResourceName?.lowercase() ?: ""
                sensitiveHints.any { sensitive ->
                    hint.contains(sensitive) || contentDesc.contains(sensitive) || viewId.contains(sensitive)
                } || node.isPassword
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun extractTextFromNode(root: AccessibilityNodeInfo): String {
        val sb = StringBuilder()
        traverseAndCollect(root, sb, depth = 0)
        return sb.toString()
    }

    private fun traverseAndCollect(node: AccessibilityNodeInfo?, sb: StringBuilder, depth: Int) {
        if (node == null || depth > MAX_TREE_DEPTH) return
        if (node.isPassword) return  // never read password fields

        val text = node.text?.toString()
        if (!text.isNullOrBlank()) {
            sb.append(text).append(" ")
        }
        for (i in 0 until node.childCount) {
            traverseAndCollect(node.getChild(i), sb, depth + 1)
        }
    }

    private fun traverseNode(root: AccessibilityNodeInfo, predicate: (AccessibilityNodeInfo) -> Boolean): Boolean {
        if (predicate(root)) return true
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            if (traverseNode(child, predicate)) {
                child.recycle()
                return true
            }
            child.recycle()
        }
        return false
    }

    override fun onInterrupt() {
        Timber.d("TruthLens AccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        debounceJobs.values.forEach { it.cancel() }
    }

    companion object {
        private const val MIN_TEXT_LENGTH = 30
        private const val MAX_TREE_DEPTH = 8
        private const val ALERT_COOLDOWN_MS = 30_000L // 30 seconds between alerts for the same app
    }
}
