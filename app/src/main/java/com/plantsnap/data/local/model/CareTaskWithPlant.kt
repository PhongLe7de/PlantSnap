package com.plantsnap.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class CareTaskWithPlant(
    @Embedded val task: CareTaskEntity,
    @ColumnInfo(name = "plant_nickname") val plantNickname: String,
    @ColumnInfo(name = "plant_image_url") val plantImageUrl: String?,
    @ColumnInfo(name = "plant_scientific_name") val plantScientificName: String,
)
