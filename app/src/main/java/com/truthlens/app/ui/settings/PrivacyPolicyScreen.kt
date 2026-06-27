package com.truthlens.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Last updated: June 2025", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            PolicySection("What TruthLens Collects",
                "TruthLens processes visible text from apps you choose to monitor. This text is analysed locally on your device and, if you have not disabled online verification, may be sent to our fact-checking API. No screenshots, message histories, or personally identifiable information are collected without your explicit consent.")

            PolicySection("Accessibility Service",
                "TruthLens uses Android's Accessibility Service to read visible text content in apps you have enabled for monitoring. This permission is never used to log keystrokes, capture passwords, or monitor apps you have not opted in to.")

            PolicySection("Screen Capture / OCR",
                "Screen capture for OCR analysis is disabled by default. If you enable it, screenshots are processed locally using ML Kit and are never stored or transmitted.")

            PolicySection("Local Storage",
                "Scan history and app settings are stored locally on your device using Room database. You can delete all stored data at any time from Settings → Data & Privacy.")

            PolicySection("Third-Party Fact-Checking",
                "When online verification is enabled, detected text claims may be sent to our fact-checking API over an encrypted HTTPS connection. No personal data is included in these requests.")

            PolicySection("Your Rights",
                "You can disable monitoring, revoke permissions, and delete all local data at any time. TruthLens does not sell, share, or monetise your data.")

            PolicySection("Contact",
                "For privacy enquiries, please contact: privacy@truthlens.app")
        }
    }
}

@Composable
fun PolicySection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(body, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
