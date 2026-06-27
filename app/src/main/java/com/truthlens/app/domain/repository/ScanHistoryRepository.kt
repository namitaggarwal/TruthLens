package com.truthlens.app.domain.repository

import com.truthlens.app.domain.model.ScanHistory
import kotlinx.coroutines.flow.Flow

interface ScanHistoryRepository {
    fun getAll(): Flow<List<ScanHistory>>
    suspend fun insert(scan: ScanHistory): Long
    suspend fun deleteById(id: Long)
    suspend fun deleteAll()
    suspend fun getById(id: Long): ScanHistory?
}
