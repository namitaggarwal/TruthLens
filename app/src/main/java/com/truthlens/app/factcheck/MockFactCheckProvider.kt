package com.truthlens.app.factcheck

import com.truthlens.app.domain.model.FactCheckResult
import com.truthlens.app.domain.model.RiskLevel
import com.truthlens.app.domain.provider.FactCheckProvider
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject

/**
 * Mock provider for development and testing.
 * Simulates network latency and returns plausible responses based on local scoring.
 */
class MockFactCheckProvider @Inject constructor(
    private val localScorer: LocalRiskScorer
) : FactCheckProvider {

    override suspend fun analyzeClaim(text: String, sourceApp: String): FactCheckResult {
        delay(600) // simulate network

        val scored = localScorer.score(text)
        val riskLevel = RiskLevel.fromScore(scored.score)

        val summary = when (riskLevel) {
            RiskLevel.LOW    -> "No major misinformation signals were detected in this content."
            RiskLevel.MEDIUM -> "This content contains claims that may benefit from independent verification."
            RiskLevel.HIGH   -> "This content contains patterns commonly associated with misleading information."
            RiskLevel.UNKNOWN -> FactCheckResult.UNABLE_TO_VERIFY.summary
        }

        val factCheckLinks = if (riskLevel != RiskLevel.LOW) listOf(
            "https://www.snopes.com",
            "https://www.factcheck.org",
            "https://www.politifact.com"
        ) else emptyList()

        return FactCheckResult(
            id = UUID.randomUUID().toString(),
            riskLevel = riskLevel,
            confidenceScore = when (riskLevel) {
                RiskLevel.LOW    -> (60..85).random()
                RiskLevel.MEDIUM -> (45..70).random()
                RiskLevel.HIGH   -> (65..90).random()
                RiskLevel.UNKNOWN -> 0
            },
            summary = summary,
            reasons = scored.reasons.ifEmpty {
                listOf("No specific misinformation patterns detected.")
            },
            sourceLinks = emptyList(),
            factCheckLinks = factCheckLinks,
            timestamp = System.currentTimeMillis(),
            detectedText = text,
            sourceApp = sourceApp
        )
    }

    override fun isAvailable(): Boolean = true
}
