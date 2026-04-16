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
)
