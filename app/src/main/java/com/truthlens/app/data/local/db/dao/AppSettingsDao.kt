package com.truthlens.app.data.local.db.dao

import androidx.room.*
import com.truthlens.app.data.local.db.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {

    @Query("SELECT * FROM app_settings ORDER BY isPreset DESC, appName ASC")
    fun getAll(): Flow<List<AppSettingsEntity>>

    @Query("SELECT packageName FROM app_settings WHERE isEnabled = 1")
    fun getEnabledPackages(): Flow<List<String>>

    @Query("SELECT * FROM app_settings WHERE packageName = :packageName LIMIT 1")
    suspend fun getByPackage(packageName: String): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(entity: AppSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AppSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<AppSettingsEntity>)

    @Query("UPDATE app_settings SET isEnabled = :enabled WHERE packageName = :packageName")
    suspend fun setEnabled(packageName: String, enabled: Boolean)

    @Query("UPDATE app_settings SET lastScanTime = :time WHERE packageName = :packageName")
    suspend fun recordScan(packageName: String, time: Long)

    @Query("UPDATE app_settings SET alertCount = alertCount + 1 WHERE packageName = :packageName")
    suspend fun incrementAlertCount(packageName: String)
}
