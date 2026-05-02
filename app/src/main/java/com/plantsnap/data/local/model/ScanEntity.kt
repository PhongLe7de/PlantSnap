package com.plantsnap.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.plantsnap.domain.models.ScanResult

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey val id: String,
    val imagePath: String,
    val organ: String,
    val bestMatch: String,
    val timestamp: Long,
    val synced: Boolean,
    val rawResponseJson: String?,
    val plantGbifId: String?,
    val identificationScore: Double?,
    val isFavorite: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    /** Storage bucket path of the captured image, `{userId}/{id}.jpg`. Resolved to a
     *  signed URL at display time by `PlantImageUrlResolver`. */
    val imageUrl: String? = null,
)

fun ScanResult.toEntity() = ScanEntity(
    id = id,
    imagePath = imagePath,
    organ = organ,
    bestMatch = bestMatch,
    timestamp = timestamp,
    synced = synced,
    rawResponseJson = rawResponseJson,
    plantGbifId = plantGbifId,
    identificationScore = identificationScore,
    isFavorite = isFavorite,
    latitude = latitude,
    longitude = longitude,
    imageUrl = imageUrl,
)
