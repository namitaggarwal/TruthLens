package com.truthlens.app.factcheck

import com.truthlens.app.data.remote.api.FactCheckApi
import com.truthlens.app.data.remote.dto.FactCheckRequestDto
import com.truthlens.app.domain.model.FactCheckResult
import com.truthlens.app.domain.model.RiskLevel
import com.truthlens.app.domain.provider.FactCheckProvider
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class RemoteFactCheckProvider @Inject constructor(
    private val api: FactCheckApi,
    private val localScorer: LocalRiskScorer
) : FactCheckProvider {

    override suspend fun analyzeClaim(text: String, sourceApp: String): FactCheckResult {
        return try {
            val response = api.analyze(FactCheckRequestDto(text = text, source = sourceApp))
            FactCheckResult(
                id = response.id,
                riskLevel = runCatching { RiskLevel.valueOf(response.riskLevel.uppercase()) }
                    .getOrDefault(RiskLevel.UNKNOWN),
                confidenceScore = response.confidence,
                summary = response.summary,
                reasons = response.reasons,
                sourceLinks = response.sourceLinks,
                factCheckLinks = response.factCheckLinks,
                timestamp = response.timestamp,
                detectedText = text,
                sourceApp = sourceApp
            )
        } catch (e: Exception) {
            Timber.w(e, "Remote fact-check failed, falling back to local scoring")
            val scored = localScorer.score(text)
            FactCheckResult(
                id = UUID.randomUUID().toString(),
                riskLevel = RiskLevel.fromScore(scored.score),
                confidenceScore = 30,
                summary = "Online verification unavailable. Local analysis only.",
                reasons = scored.reasons,
                sourceLinks = emptyList(),
                factCheckLinks = emptyList(),
                timestamp = System.currentTimeMillis(),
                detectedText = text,
                sourceApp = sourceApp
            )
        }
    }

    override fun isAvailable(): Boolean = true
}
