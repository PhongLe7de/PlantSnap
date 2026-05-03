package com.plantsnap.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.SavedPlant

@Entity(
    tableName = "saved_plants",
    foreignKeys = [
        ForeignKey(
            entity = ScanEntity::class,
            parentColumns = ["id"],
            childColumns = ["originalScanId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = PlantDetailsEntity::class,
            parentColumns = ["plantGbifId"],
            childColumns = ["plantGbifId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("originalScanId"),
        Index(value = ["originalScanId", "plantGbifId"], unique = true),
        Index("plantGbifId"),
        Index("synced"),
    ],
)
data class SavedPlantEntity(
    @PrimaryKey val id: String,
    val userId: String?,
    val plantGbifId: Long,
    val originalScanId: String?,
    val nickname: String,
    val imageUrl: String?,
    val isArchived: Boolean,
    val isFavourite: Boolean,
    val lastWateredAt: Long?,
    val createdAt: Long,
    val synced: Boolean,
)

fun SavedPlantEntity.toDomain(scientificName: String): SavedPlant = SavedPlant(
    id = id,
    plant = Candidate(
        scientificName = scientificName,
        commonNames = if (nickname == scientificName) emptyList() else listOf(nickname),
        family = "",
        score = 0f,
        iucnCategory = null,
        imageUrl = imageUrl,
        aiInfo = null,
        gbifId = plantGbifId,
    ),
    originalScanId = originalScanId,
    createdAt = createdAt,
    nickname = nickname,
    isFavourite = isFavourite,
    lastWateredAt = lastWateredAt,
)
