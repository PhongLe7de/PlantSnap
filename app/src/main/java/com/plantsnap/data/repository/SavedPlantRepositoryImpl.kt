package com.plantsnap.data.repository

import android.util.Log
import com.plantsnap.data.local.PlantDetailsDao
import com.plantsnap.data.local.SavedPlantDao
import com.plantsnap.data.local.ScanDao
import com.plantsnap.data.local.model.PlantDetailsEntity
import com.plantsnap.data.local.model.SavedPlantEntity
import com.plantsnap.data.local.model.toDomain
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.SavedPlant
import com.plantsnap.domain.repository.SavedPlantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedPlantRepositoryImpl @Inject constructor(
    private val dao: SavedPlantDao,
    private val plantDetailsDao: PlantDetailsDao,
    private val scanDao: ScanDao,
) : SavedPlantRepository {

    private companion object {
        const val TAG = "SavedPlantRepository"
    }

    override fun observeAll(): Flow<List<SavedPlant>> =
        dao.observeAllWithDetails().map { rows ->
            rows.map { it.saved.toDomain(scientificName = it.scientificName) }
        }

    override fun observeById(savedPlantId: String): Flow<SavedPlant?> =
        dao.observeWithDetailsById(savedPlantId).map { row ->
            row?.saved?.toDomain(scientificName = row.scientificName)
        }

    override fun observeIsSaved(scanId: String, plantGbifId: Long): Flow<Boolean> =
        dao.observeIsSaved(scanId, plantGbifId)

    override suspend fun findExisting(scanId: String, plantGbifId: Long): SavedPlant? {
        val entity = dao.findExisting(scanId, plantGbifId) ?: return null
        val name = plantDetailsDao.get(plantGbifId)?.scientificName ?: entity.nickname
        return entity.toDomain(scientificName = name)
    }

    override suspend fun save(candidate: Candidate, scanId: String?): SavedPlant {
        val gbifId = candidate.gbifId
            ?: scanId?.let { scanDao.getCandidateGbifId(it, candidate.scientificName) }
            ?: throw IllegalArgumentException(
                "Cannot save plant without a gbifId (scanId=$scanId name=${candidate.scientificName})"
            )

        plantDetailsDao.upsert(PlantDetailsEntity(plantGbifId = gbifId, scientificName = candidate.scientificName))

        val nickname = candidate.commonNames.firstOrNull() ?: candidate.scientificName
        // Prefer the user's actual capture (thumbnail uploaded to Storage) over the
        // PlantNet reference image when available.
        val imageUrl = scanId?.let { scanDao.getImageUrl(it) } ?: candidate.imageUrl
        val existing = scanId?.let { dao.findExisting(it, gbifId) }
        val entity = if (existing != null) {
            // Re-saving (e.g., after a soft delete) flips archived back off and re-queues sync.
            existing.copy(
                isArchived = false,
                nickname = nickname,
                imageUrl = imageUrl,
                synced = false,
            )
        } else {
            SavedPlantEntity(
                id = UUID.randomUUID().toString(),
                userId = null,
                plantGbifId = gbifId,
                originalScanId = scanId,
                nickname = nickname,
                imageUrl = imageUrl,
                isArchived = false,
                isFavourite = false,
                lastWateredAt = null,
                createdAt = System.currentTimeMillis(),
                synced = false,
            )
        }
        dao.upsert(entity)
        Log.d(TAG, "save: id=${entity.id} scanId=$scanId name=${candidate.scientificName} gbif=$gbifId synced=${entity.synced}")
        return entity.toDomain(scientificName = candidate.scientificName)
    }

    override suspend fun unsave(savedPlantId: String) {
        dao.setArchived(savedPlantId, true)
        Log.d(TAG, "unsave: archived id=$savedPlantId, marked synced=0")
    }

    override suspend fun updateNickname(savedPlantId: String, nickname: String) {
        dao.updateNickname(savedPlantId, nickname)
        Log.d(TAG, "updateNickname: id=$savedPlantId nickname=$nickname")
    }

    override suspend fun updateFavourite(savedPlantId: String, isFavourite: Boolean) {
        dao.updateFavourite(savedPlantId, isFavourite)
        Log.d(TAG, "updateFavourite: id=$savedPlantId isFavourite=$isFavourite")
    }

    override suspend fun updateLastWatered(savedPlantId: String, timestamp: Long) {
        dao.updateLastWatered(savedPlantId, timestamp)
        Log.d(TAG, "updateLastWatered: id=$savedPlantId timestamp=$timestamp")
    }
}
