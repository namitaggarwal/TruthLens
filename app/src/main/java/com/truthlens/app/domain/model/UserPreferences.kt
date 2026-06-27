package com.truthlens.app.domain.model

data class UserPreferences(
    val masterMonitoringEnabled: Boolean = false,
    val overlayEnabled: Boolean = true,
    val scanDelaySeconds: Int = 2,
    val batterySaverMode: Boolean = false,
    val wifiOnlyBackend: Boolean = false,
    val ocrEnabled: Boolean = false,
    val notificationAccessEnabled: Boolean = false,
    val onboardingCompleted: Boolean = false
)
