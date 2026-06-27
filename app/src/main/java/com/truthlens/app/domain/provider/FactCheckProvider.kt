package com.truthlens.app.domain.provider

import com.truthlens.app.domain.model.FactCheckResult

interface FactCheckProvider {
    suspend fun analyzeClaim(text: String, sourceApp: String): FactCheckResult
    fun isAvailable(): Boolean
}
