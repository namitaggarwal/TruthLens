package com.truthlens.app

import com.google.common.truth.Truth.assertThat
import com.truthlens.app.domain.model.FactCheckResult
import com.truthlens.app.domain.model.RiskLevel
import com.truthlens.app.domain.provider.FactCheckProvider
import com.truthlens.app.domain.repository.AppSettingsRepository
import com.truthlens.app.domain.repository.ScanHistoryRepository
import com.truthlens.app.domain.usecase.AnalyzeContentUseCase
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.UUID

class AnalyzeContentUseCaseTest {

    private val factCheckProvider = mockk<FactCheckProvider>()
    private val scanHistoryRepository = mockk<ScanHistoryRepository>()
    private val appSettingsRepository = mockk<AppSettingsRepository>()
    private lateinit var useCase: AnalyzeContentUseCase

    @Before
    fun setUp() {
        useCase = AnalyzeContentUseCase(factCheckProvider, scanHistoryRepository, appSettingsRepository)
        coEvery { scanHistoryRepository.insert(any()) } returns 1L
        coEvery { appSettingsRepository.recordScan(any()) } just Runs
        coEvery { appSettingsRepository.incrementAlertCount(any()) } just Runs
    }

    @Test
    fun `blank text returns UNABLE_TO_VERIFY`() = runTest {
        val result = useCase("", "com.instagram.android", "Instagram")
        assertThat(result.riskLevel).isEqualTo(RiskLevel.UNKNOWN)
        coVerify(exactly = 0) { factCheckProvider.analyzeClaim(any(), any()) }
    }

    @Test
    fun `very short text returns UNABLE_TO_VERIFY`() = runTest {
        val result = useCase("Short", "com.instagram.android", "Instagram")
        assertThat(result.riskLevel).isEqualTo(RiskLevel.UNKNOWN)
    }

    @Test
    fun `valid text triggers analysis and saves history`() = runTest {
        val fakeResult = FactCheckResult(
            id = UUID.randomUUID().toString(),
            riskLevel = RiskLevel.MEDIUM,
            confidenceScore = 60,
            summary = "Needs verification",
            reasons = listOf("Unattributed claim"),
            sourceLinks = emptyList(),
            factCheckLinks = emptyList(),
            timestamp = System.currentTimeMillis(),
            detectedText = "Scientists say this changes everything we know",
            sourceApp = "com.instagram.android"
        )
        coEvery { factCheckProvider.analyzeClaim(any(), any()) } returns fakeResult

        val longText = "Scientists say this changes everything we know about modern medicine and health."
        val result = useCase(longText, "com.instagram.android", "Instagram")

        assertThat(result.riskLevel).isEqualTo(RiskLevel.MEDIUM)
        coVerify { scanHistoryRepository.insert(any()) }
        coVerify { appSettingsRepository.incrementAlertCount("com.instagram.android") }
    }

    @Test
    fun `provider exception returns UNABLE_TO_VERIFY`() = runTest {
        coEvery { factCheckProvider.analyzeClaim(any(), any()) } throws RuntimeException("Network error")

        val text = "Scientists say this will change the world forever and ever and ever more text here."
        val result = useCase(text, "com.twitter.android", "X")

        assertThat(result.riskLevel).isEqualTo(RiskLevel.UNKNOWN)
    }

    @Test
    fun `low risk result does not increment alert count`() = runTest {
        val lowResult = FactCheckResult(
            id = "low-1",
            riskLevel = RiskLevel.LOW,
            confidenceScore = 80,
            summary = "No concerns",
            reasons = emptyList(),
            sourceLinks = emptyList(),
            factCheckLinks = emptyList(),
            timestamp = System.currentTimeMillis(),
            detectedText = "The local sports team won the championship game yesterday.",
            sourceApp = "com.android.chrome"
        )
        coEvery { factCheckProvider.analyzeClaim(any(), any()) } returns lowResult

        val text = "The local sports team won the championship game yesterday in a thrilling final match."
        useCase(text, "com.android.chrome", "Chrome")

        coVerify(exactly = 0) { appSettingsRepository.incrementAlertCount(any()) }
    }
}
