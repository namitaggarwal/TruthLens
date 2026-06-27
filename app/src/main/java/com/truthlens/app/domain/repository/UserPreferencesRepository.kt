package com.truthlens.app.domain.repository

import com.truthlens.app.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun get(): Flow<UserPreferences>
    suspend fun update(transform: (UserPreferences) -> UserPreferences)
    suspend fun setMasterMonitoring(enabled: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setOcrEnabled(enabled: Boolean)
    suspend fun setOverlayEnabled(enabled: Boolean)
}
