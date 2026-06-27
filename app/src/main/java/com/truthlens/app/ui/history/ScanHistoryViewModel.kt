package com.truthlens.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truthlens.app.domain.model.ScanHistory
import com.truthlens.app.domain.usecase.GetScanHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val scans: List<ScanHistory> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ScanHistoryViewModel @Inject constructor(
    private val getScanHistoryUseCase: GetScanHistoryUseCase
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = getScanHistoryUseCase()
        .map { HistoryUiState(scans = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

    fun deleteItem(id: Long) {
        viewModelScope.launch { getScanHistoryUseCase.deleteById(id) }
    }

    fun clearAll() {
        viewModelScope.launch { getScanHistoryUseCase.deleteAll() }
    }
}
