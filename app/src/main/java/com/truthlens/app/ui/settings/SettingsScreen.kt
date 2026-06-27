package com.truthlens.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.truthlens.app.domain.model.UserPreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onHowItWorks: () -> Unit,
    onManagePermissions: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Privacy") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        val prefs = uiState.preferences
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SettingsSectionHeader("Monitoring")

            SettingsToggleRow(
                icon = Icons.Outlined.Layers,
                title = "Show Overlay Alerts",
                subtitle = "Display fact-check banners over monitored apps",
                checked = prefs.overlayEnabled,
                onCheckedChange = { viewModel.setOverlayEnabled(it) }
            )

            SettingsSectionItem(title = "Scan Delay: ${prefs.scanDelaySeconds}s") {
                Column {
                    Slider(
                        value = prefs.scanDelaySeconds.toFloat(),
                        onValueChange = { v ->
                            viewModel.updatePreferences { it.copy(scanDelaySeconds = v.toInt()) }
                        },
                        valueRange = 1f..5f,
                        steps = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("1s", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("5s", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            SettingsToggleRow(
                icon = Icons.Outlined.BatteryStd,
                title = "Battery Saver Mode",
                subtitle = "Reduces scan frequency to preserve battery life",
                checked = prefs.batterySaverMode,
                onCheckedChange = { viewModel.updatePreferences { p -> p.copy(batterySaverMode = it) } }
            )

            SettingsToggleRow(
                icon = Icons.Outlined.Wifi,
                title = "Wi-Fi Only Verification",
                subtitle = "Use online fact-checking only when on Wi-Fi",
                checked = prefs.wifiOnlyBackend,
                onCheckedChange = { viewModel.updatePreferences { p -> p.copy(wifiOnlyBackend = it) } }
            )

            SettingsToggleRow(
                icon = Icons.Outlined.CameraAlt,
                title = "OCR Screen Analysis",
                subtitle = "Use screen capture for OCR when text is inaccessible (optional)",
                checked = prefs.ocrEnabled,
                onCheckedChange = { viewModel.setOcrEnabled(it) }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsSectionHeader("Data & Privacy")

            SettingsActionRow(
                icon = Icons.Outlined.DeleteSweep,
                title = "Clear Scan History",
                subtitle = "Delete all locally stored scan records",
                onClick = { showClearHistoryDialog = true }
            )

            SettingsActionRow(
                icon = Icons.Outlined.FileDownload,
                title = "Export My Data",
                subtitle = "Export your scan history as a JSON file",
                onClick = {
                    scope.launch {
                        snackbarHost.showSnackbar("Export feature coming soon")
                    }
                }
            )

            SettingsActionRow(
                icon = Icons.Outlined.DeleteForever,
                title = "Delete All My Data",
                subtitle = "Permanently delete all TruthLens data from this device",
                tintError = true,
                onClick = { showDeleteAllDialog = true }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsSectionHeader("About")

            SettingsActionRow(Icons.Outlined.Policy, "Privacy Policy",
                "How TruthLens handles your data", onClick = onPrivacyPolicy)
            SettingsActionRow(Icons.Outlined.HelpOutline, "How TruthLens Works",
                "Learn about the fact-checking process", onClick = onHowItWorks)
            SettingsActionRow(Icons.Outlined.AdminPanelSettings, "Manage Permissions",
                "Review and modify app permissions", onClick = onManagePermissions)

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear scan history?") },
            text = { Text("This will permanently delete all scan records stored on this device. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearHistoryDialog = false
                    scope.launch { snackbarHost.showSnackbar("Scan history cleared") }
                }) { Text("Clear", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showClearHistoryDialog = false }) { Text("Cancel") } }
        )
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Delete all data?") },
            text = { Text("This will permanently delete all TruthLens data including scan history and settings. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showDeleteAllDialog = false
                    scope.launch { snackbarHost.showSnackbar("All data deleted") }
                }) { Text("Delete all", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteAllDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tintError: Boolean = false,
    onClick: () -> Unit
) {
    val tint = if (tintError) MaterialTheme.colorScheme.error
               else MaterialTheme.colorScheme.primary
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall,
                    color = if (tintError) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun SettingsSectionItem(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}
