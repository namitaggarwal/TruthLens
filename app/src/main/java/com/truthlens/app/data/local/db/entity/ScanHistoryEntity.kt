package com.truthlens.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.truthlens.app.domain.model.RiskLevel
import com.truthlens.app.domain.model.ScanHistory

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val appPackage: String,
    val appName: String,
    val riskLevel: String,          // RiskLevel.name
    val claimPreview: String,
    val resultId: String?
)

fun ScanHistoryEntity.toDomain() = ScanHistory(
    id = id,
    timestamp = timestamp,
    appPackage = appPackage,
    appName = appName,
    riskLevel = runCatching { RiskLevel.valueOf(riskLevel) }.getOrDefault(RiskLevel.UNKNOWN),
    claimPreview = claimPreview,
    resultId = resultId
)

fun ScanHistory.toEntity() = ScanHistoryEntity(
    id = id,
    timestamp = timestamp,
    appPackage = appPackage,
    appName = appName,
    riskLevel = riskLevel.name,
    claimPreview = claimPreview,
    resultId = resultId
)
