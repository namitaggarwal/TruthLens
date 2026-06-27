package com.truthlens.app.domain.model

data class FactCheckResult(
    val id: String,
    val riskLevel: RiskLevel,
    val confidenceScore: Int,           // 0–100
    val summary: String,
    val reasons: List<String>,
    val sourceLinks: List<String>,
    val factCheckLinks: List<String>,
    val timestamp: Long,
    val disclaimer: String = DEFAULT_DISCLAIMER,
    val detectedText: String,
    val sourceApp: String,
    val isVerified: Boolean = false
) {
    companion object {
        const val DEFAULT_DISCLAIMER =
            "TruthLens provides informational signals and does not determine absolute truth. " +
            "Always consult multiple reliable sources before forming conclusions or sharing information."

        val UNABLE_TO_VERIFY = FactCheckResult(
            id = "unverified",
            riskLevel = RiskLevel.UNKNOWN,
            confidenceScore = 0,
            summary = "Unable to verify this claim. Consider checking trusted sources before sharing.",
            reasons = emptyList(),
            sourceLinks = emptyList(),
            factCheckLinks = emptyList(),
            timestamp = System.currentTimeMillis(),
            detectedText = "",
            sourceApp = ""
        )
    }
}
