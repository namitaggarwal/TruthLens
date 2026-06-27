package com.truthlens.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FactCheckRequestDto(
    @SerializedName("text")      val text: String,
    @SerializedName("source")    val source: String,
    @SerializedName("language")  val language: String = "en"
)

data class FactCheckResponseDto(
    @SerializedName("id")               val id: String,
    @SerializedName("risk_level")       val riskLevel: String,
    @SerializedName("confidence")       val confidence: Int,
    @SerializedName("summary")         val summary: String,
    @SerializedName("reasons")         val reasons: List<String>,
    @SerializedName("source_links")    val sourceLinks: List<String>,
    @SerializedName("fact_check_links") val factCheckLinks: List<String>,
    @SerializedName("timestamp")       val timestamp: Long
)
