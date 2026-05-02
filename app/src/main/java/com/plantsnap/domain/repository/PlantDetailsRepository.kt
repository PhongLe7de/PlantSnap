package com.plantsnap.domain.repository

import com.plantsnap.domain.models.PlantAiInfo

interface PlantDetailsRepository {
    /**
     * Resolves the gbif id from the local candidate cache, then inserts the full
     * Gemini payload. Insert-only with `ON CONFLICT DO NOTHING` so existing rows
     * are never overwritten, `plant_details` is generated once per species.
     * Called from `PlantService.requestAdditionalInfo` after the Gemini response
     * is cached locally. No-op when the candidate has no gbif id.
     */
    suspend fun upsertIfHasGbif(scanId: String, scientificName: String, info: PlantAiInfo)

    /** True if a `plant_details` row already exists for this gbif. */
    suspend fun exists(plantGbifId: Long): Boolean

    /** Looks up scientific names for the given gbif ids. Used by the pull path. */
    suspend fun getScientificNames(plantGbifIds: Collection<Long>): Map<Long, String>
}
