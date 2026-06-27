package com.truthlens.app.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truthlens.app.domain.model.FactCheckResult
import com.truthlens.app.domain.model.RiskLevel
import com.truthlens.app.domain.repository.ScanHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val result: FactCheckResult? = null,
    val isLoading: Boolean = true,
    val feedbackGiven: Boolean = false
)

@HiltViewModel
class FactCheckDetailViewModel @Inject constructor(
    private val scanHistoryRepository: ScanHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadResult(resultId: String) {
        if (resultId == "demo") {
            _uiState.value = DetailUiState(
                result = FactCheckResult(
                    id = "demo",
                    riskLevel = RiskLevel.MEDIUM,
                    confidenceScore = 65,
                    summary = "This content contains claims that may benefit from independent verification.",
                    reasons = listOf(
                        "Contains unattributed claim: \"scientists say\"",
                        "Missing named sources",
                        "Sensational phrasing detected"
                    ),
                    sourceLinks = emptyList(),
                    factCheckLinks = listOf("https://www.snopes.com", "https://www.factcheck.org"),
                    timestamp = System.currentTimeMillis(),
                    detectedText = "Scientists say this new discovery will change everything we know about health.",
                    sourceApp = "com.instagram.android"
                ),
                isLoading = false
            )
            return
        }
        _uiState.value = DetailUiState(isLoading = false,
            result = FactCheckResult.UNABLE_TO_VERIFY.copy(id = resultId))
    }

    fun submitFeedback(helpful: Boolean) {
        _uiState.value = _uiState.value.copy(feedbackGiven = true)
    }
}
