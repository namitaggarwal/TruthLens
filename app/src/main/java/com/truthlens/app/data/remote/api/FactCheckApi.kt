package com.truthlens.app.data.remote.api

import com.truthlens.app.data.remote.dto.FactCheckRequestDto
import com.truthlens.app.data.remote.dto.FactCheckResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface FactCheckApi {
    @POST("v1/analyze")
    suspend fun analyze(@Body request: FactCheckRequestDto): FactCheckResponseDto
}
