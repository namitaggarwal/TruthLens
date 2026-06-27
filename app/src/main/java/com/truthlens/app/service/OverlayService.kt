package com.truthlens.app.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.truthlens.app.domain.model.RiskLevel
import com.truthlens.app.ui.overlay.OverlayBanner
import com.truthlens.app.ui.theme.TruthLensTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class OverlayService : Service(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    // LifecycleOwner boilerplate for ComposeView in a Service
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    private val viewModelStore = ViewModelStore()
    override fun getViewModelStore() = viewModelStore

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null

    private val overlayState = mutableStateOf<OverlayData?>(null)

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_SHOW_OVERLAY -> handleShowOverlay(intent)
                ACTION_DISMISS_OVERLAY -> dismissOverlay()
            }
        }
    }

    override fun onCreate() {
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        registerReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        return START_STICKY
    }

    private fun registerReceiver() {
        val filter = IntentFilter().apply {
            addAction(ACTION_SHOW_OVERLAY)
            addAction(ACTION_DISMISS_OVERLAY)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(broadcastReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(broadcastReceiver, filter)
        }
    }

    private fun handleShowOverlay(intent: Intent) {
        val riskName = intent.getStringExtra(EXTRA_RISK_LEVEL) ?: return
        val risk = runCatching { RiskLevel.valueOf(riskName) }.getOrDefault(RiskLevel.UNKNOWN)

        // Don't show overlay for LOW risk unless it's informational
        if (risk == RiskLevel.LOW) return

        val summary = intent.getStringExtra(EXTRA_SUMMARY) ?: ""
        val resultId = intent.getStringExtra(EXTRA_RESULT_ID) ?: ""
        val confidence = intent.getIntExtra(EXTRA_CONFIDENCE, 0)

        overlayState.value = OverlayData(riskLevel = risk, summary = summary, resultId = resultId, confidence = confidence)
        showOverlayView()
    }

    private fun showOverlayView() {
        if (composeView != null) return  // already shown

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)

            setContent {
                TruthLensTheme {
                    val data by overlayState
                    data?.let { overlay ->
                        OverlayBanner(
                            riskLevel = overlay.riskLevel,
                            summary = overlay.summary,
                            onDismiss = { dismissOverlay() },
                            onViewDetails = { openDetails(overlay.resultId) }
                        )
                    }
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = 0
            y = 0
        }

        try {
            windowManager.addView(composeView, params)
        } catch (e: Exception) {
            Timber.e(e, "Failed to add overlay view")
            composeView = null
        }
    }

    private fun dismissOverlay() {
        overlayState.value = null
        composeView?.let {
            try { windowManager.removeView(it) } catch (e: Exception) { /* already removed */ }
            composeView = null
        }
    }

    private fun openDetails(resultId: String) {
        dismissOverlay()
        val intent = Intent(this, com.truthlens.app.MainActivity::class.java).apply {
            action = ACTION_OPEN_DETAILS
            putExtra(EXTRA_RESULT_ID, resultId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        dismissOverlay()
        unregisterReceiver(broadcastReceiver)
        viewModelStore.clear()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    data class OverlayData(
        val riskLevel: RiskLevel,
        val summary: String,
        val resultId: String,
        val confidence: Int
    )

    companion object {
        const val ACTION_SHOW_OVERLAY     = "com.truthlens.SHOW_OVERLAY"
        const val ACTION_DISMISS_OVERLAY  = "com.truthlens.DISMISS_OVERLAY"
        const val ACTION_OPEN_DETAILS     = "com.truthlens.OPEN_DETAILS"
        const val EXTRA_RISK_LEVEL        = "extra_risk_level"
        const val EXTRA_SUMMARY           = "extra_summary"
        const val EXTRA_RESULT_ID         = "extra_result_id"
        const val EXTRA_CONFIDENCE        = "extra_confidence"
    }
}
