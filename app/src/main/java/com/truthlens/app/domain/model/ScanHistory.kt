package com.truthlens.app.domain.model

data class ScanHistory(
    val id: Long = 0,
    val timestamp: Long,
    val appPackage: String,
    val appName: String,
    val riskLevel: RiskLevel,
    val claimPreview: String,
    val resultId: String?,
    val result: FactCheckResult? = null
)
