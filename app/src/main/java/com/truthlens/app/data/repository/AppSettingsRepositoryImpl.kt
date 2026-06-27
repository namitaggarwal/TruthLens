package com.truthlens.app.data.repository

import com.truthlens.app.data.local.db.dao.AppSettingsDao
import com.truthlens.app.data.local.db.entity.toDomain
import com.truthlens.app.data.local.db.entity.toEntity
import com.truthlens.app.domain.model.AppMonitorConfig
import com.truthlens.app.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppSettingsRepositoryImpl @Inject constructor(
    private val dao: AppSettingsDao
) : AppSettingsRepository {

    override fun getAll(): Flow<List<AppMonitorConfig>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun upsert(config: AppMonitorConfig) =
        dao.upsert(config.toEntity())

    override suspend fun upsertAll(configs: List<AppMonitorConfig>) =
        dao.upsertAll(configs.map { it.toEntity() })

    override suspend fun setEnabled(packageName: String, enabled: Boolean) =
        dao.setEnabled(packageName, enabled)

    override suspend fun recordScan(packageName: String) =
        dao.recordScan(packageName, System.currentTimeMillis())

    override suspend fun incrementAlertCount(packageName: String) =
        dao.incrementAlertCount(packageName)

    override suspend fun getByPackage(packageName: String): AppMonitorConfig? =
        dao.getByPackage(packageName)?.toDomain()

    override fun getEnabledPackages(): Flow<Set<String>> =
        dao.getEnabledPackages().map { it.toSet() }
}
