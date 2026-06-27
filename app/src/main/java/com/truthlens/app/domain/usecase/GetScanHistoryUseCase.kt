package com.truthlens.app.domain.usecase

import com.truthlens.app.domain.model.ScanHistory
import com.truthlens.app.domain.repository.ScanHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetScanHistoryUseCase @Inject constructor(
    private val repository: ScanHistoryRepository
) {
    operator fun invoke(): Flow<List<ScanHistory>> = repository.getAll()
    suspend fun deleteById(id: Long) = repository.deleteById(id)
    suspend fun deleteAll() = repository.deleteAll()
}
