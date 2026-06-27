package com.truthlens.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.truthlens.app.domain.model.AppMonitorConfig

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isEnabled: Boolean,
    val lastScanTime: Long?,
    val alertCount: Int,
    val isPreset: Boolean
)

fun AppSettingsEntity.toDomain() = AppMonitorConfig(
    packageName = packageName,
    appName = appName,
    isEnabled = isEnabled,
    lastScanTime = lastScanTime,
    alertCount = alertCount,
    isPreset = isPreset
)

fun AppMonitorConfig.toEntity() = AppSettingsEntity(
    packageName = packageName,
    appName = appName,
    isEnabled = isEnabled,
    lastScanTime = lastScanTime,
    alertCount = alertCount,
    isPreset = isPreset
)
