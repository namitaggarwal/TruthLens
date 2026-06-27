package com.truthlens.app.data.repository

import com.truthlens.app.data.local.db.dao.ScanHistoryDao
import com.truthlens.app.data.local.db.entity.toDomain
import com.truthlens.app.data.local.db.entity.toEntity
import com.truthlens.app.domain.model.ScanHistory
import com.truthlens.app.domain.repository.ScanHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ScanHistoryRepositoryImpl @Inject constructor(
    private val dao: ScanHistoryDao
) : ScanHistoryRepository {

    override fun getAll(): Flow<List<ScanHistory>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun insert(scan: ScanHistory): Long =
        dao.insert(scan.toEntity())

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    override suspend fun deleteAll() = dao.deleteAll()

    override suspend fun getById(id: Long): ScanHistory? =
        dao.getById(id)?.toDomain()
}
