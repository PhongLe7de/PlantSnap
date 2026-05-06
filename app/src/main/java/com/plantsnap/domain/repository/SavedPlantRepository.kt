package com.plantsnap.domain.repository

import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.SavedPlant
import kotlinx.coroutines.flow.Flow

interface SavedPlantRepository {
    fun observeAll(): Flow<List<SavedPlant>>
    fun observeIsSaved(scanId: String, plantGbifId: Long): Flow<Boolean>
    fun observeSavedFor(scanId: String, plantGbifId: Long): Flow<SavedPlant?>
    suspend fun findExisting(scanId: String, plantGbifId: Long): SavedPlant?
    suspend fun save(candidate: Candidate, scanId: String?): SavedPlant
    suspend fun unsave(savedPlantId: String)
}
