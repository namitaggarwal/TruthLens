package com.truthlens.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowItWorksScreen(onBack: () -> Unit) {
    val steps = listOf(
        "1" to Pair("You browse a monitored app", "TruthLens detects which app is active and waits for content to stabilise (configurable 1–5 second delay)."),
        "2" to Pair("Text is extracted", "Using Android's Accessibility API, TruthLens reads the visible text from the screen. No passwords or sensitive fields are ever read."),
        "3" to Pair("Local analysis runs first", "A lightweight on-device scorer checks for sensational language, unattributed claims, and known misinformation patterns."),
        "4" to Pair("Online verification (optional)", "If enabled and not on battery-saver mode, the claim is checked against trusted fact-checking databases over an encrypted connection."),
        "5" to Pair("You see a small alert", "A colour-coded banner appears at the top of the screen. Green = no concerns, Amber = may need checking, Red = potentially misleading."),
        "6" to Pair("You decide", "TruthLens never tells you what to believe. It provides signals to help you make informed decisions before sharing content.")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("How TruthLens Works") },
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "TruthLens is a passive monitoring tool that helps you spot potentially misleading content as you browse social media and news apps.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            steps.forEach { (step, pair) ->
                Row(verticalAlignment = Alignment.Top) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(step, style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(pair.first, style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold)
                        Text(pair.second, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Divider()

            Text("What TruthLens Does NOT Do",
                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            listOf(
                "Collect your messages or browsing history",
                "Monitor apps you have not enabled",
                "Access password fields or payment screens",
                "Declare content definitively fake",
                "Upload screenshots or personal data"
            ).forEach { item ->
                Row(verticalAlignment = Alignment.Top) {
                    Text("✗", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text(item, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
