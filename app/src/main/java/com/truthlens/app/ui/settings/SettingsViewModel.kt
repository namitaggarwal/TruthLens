package com.truthlens.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truthlens.app.domain.model.UserPreferences
import com.truthlens.app.domain.repository.ScanHistoryRepository
import com.truthlens.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = true,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val scanHistoryRepository: ScanHistoryRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = userPreferencesRepository.get()
        .map { SettingsUiState(preferences = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        viewModelScope.launch { userPreferencesRepository.update(transform) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            scanHistoryRepository.deleteAll()
        }
    }

    fun setOverlayEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setOverlayEnabled(enabled) }
    }

    fun setOcrEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setOcrEnabled(enabled) }
    }
}
