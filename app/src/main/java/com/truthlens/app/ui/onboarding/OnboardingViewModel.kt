package com.truthlens.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truthlens.app.domain.repository.UserPreferencesRepository
import com.truthlens.app.domain.usecase.ManageAppMonitoringUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val manageAppMonitoringUseCase: ManageAppMonitoringUseCase
) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch {
            manageAppMonitoringUseCase.ensurePresetAppsExist()
            userPreferencesRepository.setOnboardingCompleted(true)
        }
    }
}
