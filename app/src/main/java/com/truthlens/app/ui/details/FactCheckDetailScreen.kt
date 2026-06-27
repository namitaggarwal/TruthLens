package com.truthlens.app.ui.details

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.truthlens.app.domain.model.FactCheckResult
import com.truthlens.app.domain.model.RiskLevel
import com.truthlens.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FactCheckDetailScreen(
    resultId: String,
    onBack: () -> Unit,
    viewModel: FactCheckDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(resultId) { viewModel.loadResult(resultId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fact-Check Result") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val result = uiState.result ?: return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Risk level header card
            RiskHeaderCard(result)

            // Detected claim
            if (result.detectedText.isNotBlank()) {
                SectionCard(title = "Detected Claim") {
                    Text(
                        "\"${result.detectedText.take(200)}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(result.detectedText))
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Copy claim text", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Why flagged
            if (result.reasons.isNotEmpty()) {
                SectionCard(title = "Why It Was Flagged") {
                    result.reasons.forEach { reason ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Filled.Circle, null,
                                modifier = Modifier.size(6.dp).padding(top = 6.dp),
                                tint = riskColor(result.riskLevel))
                            Spacer(Modifier.width(8.dp))
                            Text(reason, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Fact-check sources
            if (result.factCheckLinks.isNotEmpty()) {
                SectionCard(title = "Fact-Check Sources") {
                    result.factCheckLinks.forEach { link ->
                        TextButton(
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                            },
                            contentPadding = PaddingValues(horizontal = 0.dp)
                        ) {
                            Icon(Icons.Outlined.OpenInBrowser, null,
                                modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(link, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Feedback
            if (!uiState.feedbackGiven) {
                SectionCard(title = "Was this helpful?") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { viewModel.submitFeedback(true) }) {
                            Icon(Icons.Outlined.ThumbUp, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Helpful")
                        }
                        OutlinedButton(onClick = { viewModel.submitFeedback(false) }) {
                            Icon(Icons.Outlined.ThumbDown, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Not helpful")
                        }
                    }
                }
            } else {
                Card(colors = CardDefaults.cardColors(containerColor = RiskLowBg)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Check, null, tint = RiskLow)
                        Spacer(Modifier.width(8.dp))
                        Text("Thanks for your feedback", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Disclaimer
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Info, null,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        FactCheckResult.DEFAULT_DISCLAIMER,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun RiskHeaderCard(result: FactCheckResult) {
    val bgColor = riskBgColor(result.riskLevel)
    val fgColor = riskColor(result.riskLevel)

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(result.riskLevel.emoji, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(result.riskLevel.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = fgColor)
                    Text("Confidence: ${result.confidenceScore}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = fgColor.copy(alpha = 0.8f))
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(result.summary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

fun riskColor(risk: RiskLevel) = when (risk) {
    RiskLevel.LOW     -> RiskLow
    RiskLevel.MEDIUM  -> RiskMedium
    RiskLevel.HIGH    -> RiskHigh
    RiskLevel.UNKNOWN -> RiskUnknown
}

fun riskBgColor(risk: RiskLevel) = when (risk) {
    RiskLevel.LOW     -> RiskLowBg
    RiskLevel.MEDIUM  -> RiskMediumBg
    RiskLevel.HIGH    -> RiskHighBg
    RiskLevel.UNKNOWN -> RiskUnknownBg
}
