package com.plantsnap.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local mirror of the Supabase `plant_details` table — a per-species cache keyed by
 * GBIF id. Saved plants reference this row so the species name lives once per species
 * instead of duplicated on every saved-plant row.
 */
@Entity(tableName = "plant_details")
data class PlantDetailsEntity(
    @PrimaryKey val plantGbifId: Long,
    val scientificName: String,
)
