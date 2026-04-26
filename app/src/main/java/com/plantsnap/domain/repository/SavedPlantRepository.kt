package com.plantsnap.domain.repository

import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.SavedPlant
import kotlinx.coroutines.flow.Flow

interface SavedPlantRepository {
    fun observeAll(): Flow<List<SavedPlant>>
    fun observeIsSaved(scanId: String, scientificName: String): Flow<Boolean>
    suspend fun findExisting(scanId: String, scientificName: String): SavedPlant?
    suspend fun save(candidate: Candidate, scanId: String?): SavedPlant
    suspend fun unsave(savedPlantId: String)
}
