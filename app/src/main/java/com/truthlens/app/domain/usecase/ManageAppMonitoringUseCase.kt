package com.truthlens.app.domain.usecase

import com.truthlens.app.domain.model.AppMonitorConfig
import com.truthlens.app.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageAppMonitoringUseCase @Inject constructor(
    private val repository: AppSettingsRepository
) {
    fun getAll(): Flow<List<AppMonitorConfig>> = repository.getAll()
    fun getEnabledPackages(): Flow<Set<String>> = repository.getEnabledPackages()

    suspend fun toggleApp(packageName: String, enabled: Boolean) =
        repository.setEnabled(packageName, enabled)

    suspend fun ensurePresetAppsExist() {
        val existing = mutableSetOf<String>()
        repository.getAll().collect { configs ->
            configs.forEach { existing.add(it.packageName) }
        }
        val missing = AppMonitorConfig.PRESET_APPS.filter { it.packageName !in existing }
        if (missing.isNotEmpty()) repository.upsertAll(missing)
    }

    suspend fun addCustomApp(packageName: String, appName: String) {
        if (packageName in AppMonitorConfig.BLOCKED_PACKAGES) return
        repository.upsert(
            AppMonitorConfig(
                packageName = packageName,
                appName = appName,
                isEnabled = false,
                isPreset = false
            )
        )
    }
}
