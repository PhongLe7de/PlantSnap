package com.plantsnap.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class SavedPlantWithDetails(
    @Embedded val saved: SavedPlantEntity,
    @ColumnInfo(name = "details_scientificName") val scientificName: String,
)
