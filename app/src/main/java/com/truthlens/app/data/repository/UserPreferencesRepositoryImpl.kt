package com.truthlens.app.data.repository

import com.truthlens.app.data.local.datastore.UserPreferencesDataStore
import com.truthlens.app.domain.model.UserPreferences
import com.truthlens.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: UserPreferencesDataStore
) : UserPreferencesRepository {

    override fun get(): Flow<UserPreferences> = dataStore.preferences

    override suspend fun update(transform: (UserPreferences) -> UserPreferences) =
        dataStore.update(transform)

    override suspend fun setMasterMonitoring(enabled: Boolean) =
        dataStore.setMasterMonitoring(enabled)

    override suspend fun setOnboardingCompleted(completed: Boolean) =
        dataStore.setOnboardingCompleted(completed)

    override suspend fun setOcrEnabled(enabled: Boolean) =
        dataStore.setOcrEnabled(enabled)

    override suspend fun setOverlayEnabled(enabled: Boolean) =
        dataStore.setOverlayEnabled(enabled)
}
