package com.plantsnap.data.repository

import android.util.Log
import com.plantsnap.data.local.PlantDetailsDao
import com.plantsnap.data.local.SavedPlantDao
import com.plantsnap.data.local.ScanDao
import com.plantsnap.data.local.model.PlantDetailsEntity
import com.plantsnap.data.local.model.SavedPlantEntity
import com.plantsnap.data.local.model.toDomain
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.PlantAiInfo
import com.plantsnap.domain.models.SavedPlant
import com.plantsnap.domain.repository.CareTaskRepository
import com.plantsnap.domain.repository.SavedPlantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedPlantRepositoryImpl @Inject constructor(
    private val dao: SavedPlantDao,
    private val plantDetailsDao: PlantDetailsDao,
    private val scanDao: ScanDao,
    private val careTaskRepository: CareTaskRepository,
    private val json: Json,
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

    override fun observeSavedFor(scanId: String, plantGbifId: Long): Flow<SavedPlant?> =
        dao.observeSavedFor(scanId, plantGbifId).map { row ->
            row?.saved?.toDomain(scientificName = row.scientificName)
        }

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
        val imageUrl = scanId?.let { scanDao.getImageUrl(it) } ?: candidate.imageUrl
        val existing = scanId?.let { dao.findExisting(it, gbifId) }
        val entity = if (existing != null) {
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

        // Generate care tasks if Gemini's AI info is already cached on the candidate.
        // The in-memory `candidate.aiInfo` is unreliable (the candidate is created
        // before requestAdditionalInfo writes back), so read from disk. If still null
        // here, PlantService.requestAdditionalInfo's hook 2 will catch it later.
        val aiInfoJson = scanId?.let { scanDao.getCandidateAiInfo(it, candidate.scientificName) }
        if (aiInfoJson != null) {
            val careInfo = runCatching {
                json.decodeFromString(PlantAiInfo.serializer(), aiInfoJson).care
            }.onFailure {
                Log.w(TAG, "save: failed to decode aiInfo for ${entity.id}", it)
            }.getOrNull()
            try {
                careTaskRepository.generateForSavedPlant(entity.id, careInfo)
            } catch (e: Exception) {
                Log.w(TAG, "save: care task generation failed for ${entity.id}", e)
            }
        } else {
            Log.d(TAG, "save: no cached aiInfo for ${entity.id}; care tasks deferred")
        }

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

    override suspend fun updateLastWatered(savedPlantId: String, timestamp: Long?) {
        dao.updateLastWatered(savedPlantId, timestamp)
        Log.d(TAG, "updateLastWatered: id=$savedPlantId timestamp=$timestamp")
    }

    override suspend fun updateLastWateredBulk(savedPlantIds: List<String>, timestamp: Long?) {
        if (savedPlantIds.isEmpty()) return
        dao.updateLastWateredBulk(savedPlantIds, timestamp)
        Log.d(TAG, "updateLastWateredBulk: count=${savedPlantIds.size} timestamp=$timestamp")
    }
}
