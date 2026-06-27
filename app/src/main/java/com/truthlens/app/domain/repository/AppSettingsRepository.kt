package com.truthlens.app.domain.repository

import com.truthlens.app.domain.model.AppMonitorConfig
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    fun getAll(): Flow<List<AppMonitorConfig>>
    suspend fun upsert(config: AppMonitorConfig)
    suspend fun upsertAll(configs: List<AppMonitorConfig>)
    suspend fun setEnabled(packageName: String, enabled: Boolean)
    suspend fun recordScan(packageName: String)
    suspend fun incrementAlertCount(packageName: String)
    suspend fun getByPackage(packageName: String): AppMonitorConfig?
    fun getEnabledPackages(): Flow<Set<String>>
}
