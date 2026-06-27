package com.truthlens.app.domain.model

data class AppMonitorConfig(
    val packageName: String,
    val appName: String,
    val isEnabled: Boolean = false,
    val lastScanTime: Long? = null,
    val alertCount: Int = 0,
    val isPreset: Boolean = false           // true for the built-in supported apps
) {
    companion object {
        val PRESET_APPS = listOf(
            AppMonitorConfig("com.android.chrome",         "Chrome",    isPreset = true),
            AppMonitorConfig("com.instagram.android",      "Instagram", isPreset = true),
            AppMonitorConfig("com.twitter.android",        "X (Twitter)", isPreset = true),
            AppMonitorConfig("com.facebook.katana",        "Facebook",  isPreset = true),
            AppMonitorConfig("com.whatsapp",               "WhatsApp",  isPreset = true),
            AppMonitorConfig("com.google.android.youtube", "YouTube",   isPreset = true),
        )

        // Apps that must never be monitored for privacy/security reasons
        val BLOCKED_PACKAGES = setOf(
            "com.google.android.inputmethod.latin",
            "com.android.systemui",
            "com.android.settings",
            "com.android.keyguard",
            "com.google.android.gms",
        )
    }
}
