package com.plantsnap.domain.repository

import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.SavedPlant
import kotlinx.coroutines.flow.Flow

interface SavedPlantRepository {
    fun observeAll(): Flow<List<SavedPlant>>
    fun observeById(savedPlantId: String): Flow<SavedPlant?>
    fun observeIsSaved(scanId: String, plantGbifId: Long): Flow<Boolean>
    suspend fun findExisting(scanId: String, plantGbifId: Long): SavedPlant?
    suspend fun save(candidate: Candidate, scanId: String?): SavedPlant
    suspend fun unsave(savedPlantId: String)
    suspend fun updateNickname(savedPlantId: String, nickname: String)
    suspend fun updateFavourite(savedPlantId: String, isFavourite: Boolean)
    suspend fun updateLastWatered(savedPlantId: String, timestamp: Long?)
    suspend fun updateLastWateredBulk(savedPlantIds: List<String>, timestamp: Long?)
}
