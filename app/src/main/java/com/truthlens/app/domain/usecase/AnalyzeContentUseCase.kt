package com.truthlens.app.domain.usecase

import com.truthlens.app.domain.model.FactCheckResult
import com.truthlens.app.domain.model.RiskLevel
import com.truthlens.app.domain.model.ScanHistory
import com.truthlens.app.domain.provider.FactCheckProvider
import com.truthlens.app.domain.repository.AppSettingsRepository
import com.truthlens.app.domain.repository.ScanHistoryRepository
import timber.log.Timber
import javax.inject.Inject

class AnalyzeContentUseCase @Inject constructor(
    private val factCheckProvider: FactCheckProvider,
    private val scanHistoryRepository: ScanHistoryRepository,
    private val appSettingsRepository: AppSettingsRepository
) {
    suspend operator fun invoke(text: String, packageName: String, appName: String): FactCheckResult {
        if (text.isBlank() || text.length < MIN_TEXT_LENGTH) {
            return FactCheckResult.UNABLE_TO_VERIFY.copy(detectedText = text, sourceApp = packageName)
        }

        return try {
            val result = factCheckProvider.analyzeClaim(text, packageName)

            val scan = ScanHistory(
                timestamp = System.currentTimeMillis(),
                appPackage = packageName,
                appName = appName,
                riskLevel = result.riskLevel,
                claimPreview = text.take(CLAIM_PREVIEW_LENGTH),
                resultId = result.id
            )
            scanHistoryRepository.insert(scan)

            if (result.riskLevel != RiskLevel.LOW) {
                appSettingsRepository.incrementAlertCount(packageName)
            }
            appSettingsRepository.recordScan(packageName)

            result
        } catch (e: Exception) {
            Timber.e(e, "Failed to analyze content")
            FactCheckResult.UNABLE_TO_VERIFY.copy(
                detectedText = text,
                sourceApp = packageName,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    companion object {
        private const val MIN_TEXT_LENGTH = 20
        private const val CLAIM_PREVIEW_LENGTH = 120
    }
}
