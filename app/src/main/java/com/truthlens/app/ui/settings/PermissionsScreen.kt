package com.truthlens.app.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    data class PermInfo(
        val icon: ImageVector, val name: String,
        val why: String, val isRequired: Boolean,
        val action: (() -> Unit)?
    )

    val permissions = listOf(
        PermInfo(Icons.Outlined.Accessibility, "Accessibility Service",
            "Required to read visible text from monitored apps. Never reads passwords, payment screens, or apps not in your enabled list.",
            true) {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        },
        PermInfo(Icons.Outlined.Layers, "Display Over Other Apps",
            "Required to show fact-check alert banners on top of monitored apps.",
            true) {
            context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
        },
        PermInfo(Icons.Outlined.Notifications, "Post Notifications",
            "Required on Android 13+ to show monitoring status and alert notifications.",
            true, null),
        PermInfo(Icons.Outlined.CameraAlt, "Screen Capture",
            "Optional. Used only when you enable OCR screen analysis. Screenshots are processed locally and never stored or uploaded.",
            false, null),
        PermInfo(Icons.Outlined.NotificationsActive, "Notification Listener",
            "Optional. Allows TruthLens to analyse notification content previews. Disabled by default.",
            false) {
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Permissions") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "TruthLens requires the following permissions. Tap a permission to open the relevant settings page.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            permissions.forEach { perm ->
                Card(
                    onClick = { perm.action?.invoke() },
                    enabled = perm.action != null,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(perm.icon, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp).padding(top = 2.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(perm.name, style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    color = if (perm.isRequired) MaterialTheme.colorScheme.errorContainer
                                            else MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        if (perm.isRequired) "Required" else "Optional",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (perm.isRequired) MaterialTheme.colorScheme.onErrorContainer
                                                else MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(perm.why, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (perm.action != null) {
                                Spacer(Modifier.height(6.dp))
                                Text("Tap to open settings →",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
