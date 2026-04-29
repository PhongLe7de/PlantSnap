package com.plantsnap.data.sync

import android.util.Log
import com.plantsnap.data.local.PlantDetailsDao
import com.plantsnap.data.local.SavedPlantDao
import com.plantsnap.data.local.model.PlantDetailsEntity
import com.plantsnap.data.local.model.SavedPlantEntity
import com.plantsnap.data.remote.supabase.SupabaseSavedPlantDto
import com.plantsnap.data.remote.supabase.toEntity
import com.plantsnap.data.remote.supabase.toSupabaseDto
import com.plantsnap.domain.repository.PlantDetailsRepository
import com.plantsnap.domain.services.PlantService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedPlantSyncManager @Inject constructor(
    private val supabase: SupabaseClient,
    private val savedPlantDao: SavedPlantDao,
    private val plantDetailsDao: PlantDetailsDao,
    private val plantDetailsRepository: PlantDetailsRepository,
    private val plantService: PlantService,
) {
    private companion object {
        const val TAG = "SavedPlantSyncManager"
        const val TABLE = "saved_plants"
    }

    private val mutex = Mutex()

    /** Pull remote → local and push local → remote in parallel. They touch disjoint
     *  rows (pull writes synced=true; push reads synced=0), so concurrency is safe. */
    suspend fun sync() = mutex.withLock {
        val userId = supabase.auth.currentUserOrNull()?.id
        if (userId == null) {
            Log.d(TAG, "sync: not authenticated, skipping")
            return@withLock
        }
        coroutineScope {
            launch { pullFromRemoteInternal(userId) }
            launch { pushPendingInternal(userId) }
        }
    }

    /** Push-only path used right after a local save, before the pull trigger fires. */
    suspend fun syncPending() = mutex.withLock {
        val userId = supabase.auth.currentUserOrNull()?.id
        if (userId == null) {
            Log.w(TAG, "syncPending: not authenticated, skipping (sign in to sync)")
            return@withLock
        }
        Log.d(TAG, "syncPending: starting for user=$userId")
        pushPendingInternal(userId)
        Log.d(TAG, "syncPending: done for user=$userId")
    }

    private suspend fun pullFromRemoteInternal(userId: String) {
        val remote: List<SupabaseSavedPlantDto> = try {
            supabase.postgrest.from(TABLE)
                .select { filter { eq("user_id", userId) } }
                .decodeList()
        } catch (e: Exception) {
            Log.w(TAG, "pull failed", e)
            return
        }

        if (remote.isEmpty()) {
            Log.d(TAG, "pull: no remote saved plants for user")
            return
        }

        val localIds = savedPlantDao.getAllIds().toHashSet()
        val newRows = remote.filter { it.id !in localIds }
        if (newRows.isEmpty()) {
            Log.d(TAG, "pull: ${remote.size} remote rows; all already local")
            return
        }

        val gbifs = newRows.map { it.plantGbifId }.toSet()
        val nameMap = plantDetailsRepository.getScientificNames(gbifs)
        if (nameMap.size < gbifs.size) {
            Log.w(TAG, "pull: ${gbifs.size - nameMap.size} gbif(s) missing scientific_name; falling back to nickname")
        }

        // Upsert local plant_details first so the FK on saved_plants.plantGbifId resolves.
        val detailsRows = newRows
            .associateBy { it.plantGbifId }
            .map { (gbif, dto) ->
                PlantDetailsEntity(
                    plantGbifId = gbif,
                    scientificName = nameMap[gbif] ?: dto.nickname,
                )
            }
        plantDetailsDao.upsertAll(detailsRows)

        var inserted = 0
        for (dto in newRows) {
            try {
                savedPlantDao.upsert(dto.toEntity())
                inserted++
            } catch (e: Exception) {
                Log.w(TAG, "pull: failed to hydrate ${dto.id}", e)
            }
        }
        Log.d(TAG, "pull: inserted $inserted new saved plant(s) (${remote.size} total remote)")
    }

    private suspend fun pushPendingInternal(userId: String) {
        val unsynced = savedPlantDao.getUnsynced()
        if (unsynced.isEmpty()) {
            Log.d(TAG, "push: no unsynced rows")
            return
        }

        Log.d(TAG, "push: draining ${unsynced.size} saved plant(s) to Supabase")
        for (saved in unsynced) {
            Log.d(TAG, "push: row id=${saved.id} scanId=${saved.originalScanId} gbif=${saved.plantGbifId}")
            try {
                val dto = saved.toSupabaseDto(userId) ?: run {
                    Log.w(TAG, "push: skipping ${saved.id}, missing originalScanId")
                    continue
                }
                ensurePlantDetailsExists(dto.plantGbifId, saved)
                Log.d(TAG, "push: upserting saved_plants row id=${saved.id} gbif=${dto.plantGbifId}")
                supabase.postgrest.from(TABLE).upsert(dto)
                savedPlantDao.markSynced(saved.id)
                Log.d(TAG, "push: synced ${saved.id}")
            } catch (e: Exception) {
                Log.w(TAG, "push: failed for ${saved.id} with ${e::class.simpleName}: ${e.message}; will retry on next trigger", e)
            }
        }
    }

    private suspend fun ensurePlantDetailsExists(gbif: Long, saved: SavedPlantEntity) {
        val exists = plantDetailsRepository.exists(gbif)
        Log.d(TAG, "push: plant_details exists check for gbif=$gbif → $exists")
        if (exists) return
        val scanId = saved.originalScanId ?: run {
            Log.w(TAG, "push: cannot fetch Gemini for gbif=$gbif, no originalScanId on ${saved.id}")
            return
        }
        val name = plantDetailsDao.get(gbif)?.scientificName ?: run {
            Log.w(TAG, "push: cannot fetch Gemini for gbif=$gbif, no local plant_details row to source the species name from")
            return
        }
        Log.d(TAG, "push: requesting Gemini fetch for scanId=$scanId name=$name")
        plantService.requestAdditionalInfo(scanId, name)
        Log.d(TAG, "push: Gemini fetch returned for gbif=$gbif")
    }
}
