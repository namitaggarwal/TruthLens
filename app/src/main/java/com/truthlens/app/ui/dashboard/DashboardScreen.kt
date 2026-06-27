package com.truthlens.app.ui.dashboard

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.truthlens.app.domain.model.AppMonitorConfig
import com.truthlens.app.domain.model.RiskLevel
import com.truthlens.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.refreshPermissionStatus() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Verified, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("TruthLens", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Outlined.History, "Scan history")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Outlined.Settings, "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 80.dp,
                start = 16.dp, end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Permission warnings
            if (!uiState.isAccessibilityEnabled) {
                item {
                    PermissionWarningBanner(
                        message = "Accessibility Service not enabled — tap to enable in Settings",
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }
                    )
                }
            }
            if (!uiState.isOverlayEnabled) {
                item {
                    PermissionWarningBanner(
                        message = "Display over apps not enabled — tap to enable overlay alerts",
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                            )
                        }
                    )
                }
            }

            // Master toggle
            item {
                MasterToggleCard(
                    enabled = uiState.preferences.masterMonitoringEnabled,
                    onToggle = viewModel::toggleMasterMonitoring
                )
            }

            item {
                Text(
                    "Monitored Apps",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            items(uiState.apps, key = { it.packageName }) { app ->
                AppMonitorCard(
                    app = app,
                    masterEnabled = uiState.preferences.masterMonitoringEnabled,
                    onToggle = { enabled -> viewModel.toggleApp(app.packageName, enabled) }
                )
            }
        }
    }
}

@Composable
fun PermissionWarningBanner(message: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = RiskMediumBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Warning, null, tint = RiskMedium, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(message, style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF856404), modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ChevronRight, null, tint = RiskMedium, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun MasterToggleCard(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (enabled) Icons.Filled.Shield else Icons.Outlined.ShieldMoon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("TruthLens Monitoring",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Text(
                    if (enabled) "Active — analysing supported apps"
                    else "Inactive — tap to enable monitoring",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
fun AppMonitorCard(
    app: AppMonitorConfig,
    masterEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val sdf = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App initial avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(appAvatarColor(app.packageName)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.appName.first().uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(app.appName, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    if (app.alertCount > 0) {
                        Spacer(Modifier.width(6.dp))
                        Badge { Text("${app.alertCount}") }
                    }
                }
                Text(
                    text = app.lastScanTime?.let { "Last: ${sdf.format(Date(it))}" } ?: "Never scanned",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = app.isEnabled,
                onCheckedChange = onToggle,
                enabled = masterEnabled || !app.isEnabled
            )
        }
    }
}

private fun appAvatarColor(packageName: String): Color {
    val colors = listOf(
        Color(0xFF1A73E8), Color(0xFF34A853), Color(0xFFFBBC04),
        Color(0xFFEA4335), Color(0xFF9334E6), Color(0xFF00BCD4)
    )
    return colors[packageName.hashCode().and(0x7FFFFFFF) % colors.size]
}
