package com.truthlens.app.domain.model

enum class RiskLevel(val displayName: String, val emoji: String) {
    LOW("No major concerns", "🟢"),
    MEDIUM("May need verification", "🟡"),
    HIGH("Potentially misleading", "🔴"),
    UNKNOWN("Unable to verify", "🔵");

    companion object {
        fun fromScore(score: Int): RiskLevel = when {
            score >= 70 -> HIGH
            score >= 40 -> MEDIUM
            score >= 0  -> LOW
            else        -> UNKNOWN
        }
    }
}
