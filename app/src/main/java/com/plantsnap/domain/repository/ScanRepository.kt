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
    suspend fun updateCandidateAiInfo(scanId: String, scientificName: String, aiInfoJson: String)
    suspend fun getAllScanIds(): List<String>

    // Aggregate stats
    fun observeTotalScanCount(): Flow<Int>
    fun observeFirstScanTimestamp(): Flow<Long?>

    suspend fun setFavorite(id: String, isFavorite: Boolean)

    suspend fun setImageUrl(id: String, url: String?)
    suspend fun getImageUrl(id: String): String?
}
