package com.plantsnap.domain.repository

import com.plantsnap.domain.models.ScanResult
import kotlinx.coroutines.flow.Flow

interface ScanRepository {
    suspend fun save(scanResult: ScanResult)
    fun getAll(): Flow<List<ScanResult>>
    fun observeById(id: String): Flow<ScanResult?>
    suspend fun delete(id: String)
    suspend fun getUnsynced(): List<ScanResult>
    suspend fun markSynced(id: String)
    suspend fun updateAiInfo(id: String, aiInfo: String)
}
