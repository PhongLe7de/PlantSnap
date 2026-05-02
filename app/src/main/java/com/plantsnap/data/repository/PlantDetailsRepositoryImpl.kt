package com.plantsnap.data.repository

import android.util.Log
import com.plantsnap.data.local.ScanDao
import com.plantsnap.data.remote.supabase.PlantDetailsNameRow
import com.plantsnap.data.remote.supabase.toPlantDetailsDto
import com.plantsnap.domain.models.PlantAiInfo
import com.plantsnap.domain.repository.PlantDetailsRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlantDetailsRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val scanDao: ScanDao,
) : PlantDetailsRepository {

    private companion object {
        const val TAG = "PlantDetailsRepository"
        const val TABLE = "plant_details"
    }

    override suspend fun upsertIfHasGbif(scanId: String, scientificName: String, info: PlantAiInfo) {
        if (supabase.auth.currentUserOrNull() == null) {
            Log.w(TAG, "upsertIfHasGbif: not authenticated, skipping for $scientificName")
            return
        }
        val gbif = scanDao.getCandidateGbifId(scanId, scientificName)
        if (gbif == null) {
            Log.w(TAG, "upsertIfHasGbif: no gbif on local candidate (scanId=$scanId name=$scientificName); skipping")
            return
        }
        Log.d(TAG, "upsertIfHasGbif: inserting plant_details gbif=$gbif name=$scientificName (ignoreDuplicates)")
        try {
            supabase.postgrest.from(TABLE).upsert(info.toPlantDetailsDto(gbif, scientificName)) {
                onConflict = "plant_gbif_id"
                ignoreDuplicates = true
            }
            Log.d(TAG, "upsertIfHasGbif: upsert returned for gbif=$gbif")
        } catch (e: Exception) {
            Log.w(TAG, "upsertIfHasGbif: failed for gbif=$gbif with ${e::class.simpleName}: ${e.message}", e)
        }
    }

    override suspend fun exists(plantGbifId: Long): Boolean {
        if (supabase.auth.currentUserOrNull() == null) {
            Log.w(TAG, "exists: not authenticated, returning false for gbif=$plantGbifId")
            return false
        }
        return try {
            val rows = supabase.postgrest.from(TABLE)
                .select(columns = Columns.list("plant_gbif_id", "scientific_name")) {
                    filter { eq("plant_gbif_id", plantGbifId) }
                    limit(1)
                }
                .decodeList<PlantDetailsNameRow>()
            Log.d(TAG, "exists: gbif=$plantGbifId rows=${rows.size}")
            rows.isNotEmpty()
        } catch (e: Exception) {
            Log.w(TAG, "exists: failed for $plantGbifId with ${e::class.simpleName}: ${e.message}", e)
            false
        }
    }

    override suspend fun getScientificNames(plantGbifIds: Collection<Long>): Map<Long, String> {
        if (plantGbifIds.isEmpty()) return emptyMap()
        if (supabase.auth.currentUserOrNull() == null) return emptyMap()
        return try {
            supabase.postgrest.from(TABLE)
                .select(columns = Columns.list("plant_gbif_id", "scientific_name")) {
                    filter { isIn("plant_gbif_id", plantGbifIds.toList()) }
                }
                .decodeList<PlantDetailsNameRow>()
                .associate { it.plantGbifId to it.scientificName }
        } catch (e: Exception) {
            Log.w(TAG, "getScientificNames failed for ${plantGbifIds.size} ids", e)
            emptyMap()
        }
    }
}
