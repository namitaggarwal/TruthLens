package com.truthlens.app.ui.overlay

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.truthlens.app.domain.model.RiskLevel
import com.truthlens.app.ui.details.riskBgColor
import com.truthlens.app.ui.details.riskColor
import com.truthlens.app.ui.theme.*

@Composable
fun OverlayBanner(
    riskLevel: RiskLevel,
    summary: String,
    onDismiss: () -> Unit,
    onViewDetails: () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = riskBgColor(riskLevel)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(riskLevel.emoji, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            overlayTitle(riskLevel),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = riskColor(riskLevel)
                        )
                        Text(
                            summary.take(80),
                            style = MaterialTheme.typography.bodySmall,
                            color = riskColor(riskLevel).copy(alpha = 0.85f),
                            maxLines = 2
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Close, "Dismiss",
                            modifier = Modifier.size(16.dp),
                            tint = riskColor(riskLevel))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Dismiss",
                            style = MaterialTheme.typography.labelMedium,
                            color = riskColor(riskLevel))
                    }
                    TextButton(
                        onClick = onViewDetails,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("View Details",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = riskColor(riskLevel))
                    }
                }
            }
        }
    }
}

private fun overlayTitle(risk: RiskLevel) = when (risk) {
    RiskLevel.LOW     -> "No major misinformation signals found"
    RiskLevel.MEDIUM  -> "This claim may need verification"
    RiskLevel.HIGH    -> "Potentially misleading information detected"
    RiskLevel.UNKNOWN -> "Fact-check available — tap to review"
}
