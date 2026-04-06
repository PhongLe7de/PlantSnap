package com.plantsnap.data.repository

import com.plantsnap.data.local.ScanDao
import com.plantsnap.data.local.model.toDomain
import com.plantsnap.data.local.model.toEntity
import com.plantsnap.domain.models.ScanResult
import com.plantsnap.domain.repository.ScanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanRepositoryImpl @Inject constructor(
    private val dao: ScanDao
) : ScanRepository {

    override suspend fun save(scanResult: ScanResult) {
        dao.insertScan(scanResult.toEntity())
        dao.insertCandidates(scanResult.candidates.map { it.toEntity(scanResult.id) })
    }

    override fun getAll(): Flow<List<ScanResult>> =
        dao.getScansWithCandidates().map { list -> list.map { it.toDomain() } }

    // Flow instead of one shot get to update additional LLM requested info
    override fun observeById(id: String): Flow<ScanResult?> =
        dao.observeScanById(id).map { it?.toDomain() }

    override suspend fun delete(id: String) =
        dao.deleteScan(id)

    override suspend fun getUnsynced(): List<ScanResult> =
        dao.getUnsyncedScans().map { it.toDomain() }

    override suspend fun markSynced(id: String) =
        dao.markSynced(id)

    override suspend fun updateAiInfo(id: String, aiInfo: String) =
        dao.updateAiInfo(id, aiInfo)

    override fun observeTotalScanCount(): Flow<Int> = dao.observeTotalScanCount()
    override fun observeFirstScanTimestamp(): Flow<Long?> = dao.observeFirstScanTimestamp()
}
