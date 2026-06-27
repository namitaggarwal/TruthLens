package com.truthlens.app.domain.provider

import android.graphics.Bitmap

interface OcrProvider {
    suspend fun extractText(bitmap: Bitmap): String
    fun isAvailable(): Boolean
}
