package com.plantsnap.data.repository

import com.plantsnap.data.local.SavedPlantDao
import com.plantsnap.data.local.model.SavedPlantEntity
import com.plantsnap.data.local.model.toDomain
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.SavedPlant
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
    private val json: Json,
) : SavedPlantRepository {

    override fun observeAll(): Flow<List<SavedPlant>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain(json) } }

    override fun observeIsSaved(scanId: String, scientificName: String): Flow<Boolean> =
        dao.observeIsSaved(scanId, scientificName)

    override suspend fun findExisting(scanId: String, scientificName: String): SavedPlant? =
        dao.findExisting(scanId, scientificName)?.toDomain(json)

    override suspend fun save(candidate: Candidate, scanId: String?): SavedPlant {
        val existing = scanId?.let { dao.findExisting(it, candidate.scientificName) }
        val entity = existing?.copy(
            candidateJson = json.encodeToString(Candidate.serializer(), candidate),
        ) ?: SavedPlantEntity(
            id = UUID.randomUUID().toString(),
            candidateJson = json.encodeToString(Candidate.serializer(), candidate),
            scientificName = candidate.scientificName,
            sourceScanId = scanId,
            savedAt = System.currentTimeMillis(),
        )
        dao.upsert(entity)
        return entity.toDomain(json)
    }

    override suspend fun unsave(savedPlantId: String) {
        dao.deleteById(savedPlantId)
    }
}
