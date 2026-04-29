package com.plantsnap.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Embedded

/** Projection that joins `saved_plants` with its `plant_details` row to expose
 *  the species name in a single query. */
data class SavedPlantWithDetails(
    @Embedded val saved: SavedPlantEntity,
    @ColumnInfo(name = "details_scientificName") val scientificName: String,
)
