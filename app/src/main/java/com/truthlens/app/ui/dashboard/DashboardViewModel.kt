package com.truthlens.app.ui.dashboard

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truthlens.app.domain.model.AppMonitorConfig
import com.truthlens.app.domain.model.UserPreferences
import com.truthlens.app.domain.repository.UserPreferencesRepository
import com.truthlens.app.domain.usecase.ManageAppMonitoringUseCase
import com.truthlens.app.service.MonitoringForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val apps: List<AppMonitorConfig> = emptyList(),
    val preferences: UserPreferences = UserPreferences(),
    val isAccessibilityEnabled: Boolean = false,
    val isOverlayEnabled: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val manageAppMonitoringUseCase: ManageAppMonitoringUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { manageAppMonitoringUseCase.ensurePresetAppsExist() }
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                manageAppMonitoringUseCase.getAll(),
                userPreferencesRepository.get()
            ) { apps, prefs -> Pair(apps, prefs) }
                .collect { (apps, prefs) ->
                    _uiState.update {
                        it.copy(
                            apps = apps,
                            preferences = prefs,
                            isAccessibilityEnabled = isAccessibilityServiceEnabled(),
                            isOverlayEnabled = Settings.canDrawOverlays(context),
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun toggleApp(packageName: String, enabled: Boolean) {
        viewModelScope.launch {
            manageAppMonitoringUseCase.toggleApp(packageName, enabled)
        }
    }

    fun toggleMasterMonitoring(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setMasterMonitoring(enabled)
            if (enabled) {
                ContextCompat.startForegroundService(context, MonitoringForegroundService.startIntent(context))
            } else {
                context.startService(MonitoringForegroundService.stopIntent(context))
            }
        }
    }

    fun refreshPermissionStatus() {
        _uiState.update {
            it.copy(
                isAccessibilityEnabled = isAccessibilityServiceEnabled(),
                isOverlayEnabled = Settings.canDrawOverlays(context)
            )
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val serviceName = "${context.packageName}/.service.TruthLensAccessibilityService"
        val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return enabled?.contains(serviceName) == true
    }
}
