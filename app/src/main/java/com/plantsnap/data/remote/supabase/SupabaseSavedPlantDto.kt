package com.plantsnap.data.remote.supabase

import com.plantsnap.data.local.model.SavedPlantEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class SupabaseSavedPlantDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("plant_gbif_id") val plantGbifId: Long,
    @SerialName("original_scan_id") val originalScanId: String,
    val nickname: String,
    @SerialName("is_archived") val isArchived: Boolean,
    @SerialName("is_favourite") val isFavourite: Boolean,
    @SerialName("last_watered_at") val lastWateredAt: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("image_url") val imageUrl: String? = null,
)

fun SavedPlantEntity.toSupabaseDto(userId: String): SupabaseSavedPlantDto? {
    val scanId = originalScanId ?: return null
    return SupabaseSavedPlantDto(
        id = id,
        userId = userId,
        plantGbifId = plantGbifId,
        originalScanId = scanId,
        nickname = nickname,
        isArchived = isArchived,
        isFavourite = isFavourite,
        lastWateredAt = lastWateredAt?.let { Instant.ofEpochMilli(it).toString() },
        createdAt = Instant.ofEpochMilli(createdAt).toString(),
        imageUrl = imageUrl,
    )
}

fun SupabaseSavedPlantDto.toEntity(): SavedPlantEntity = SavedPlantEntity(
    id = id,
    userId = userId,
    plantGbifId = plantGbifId,
    originalScanId = originalScanId,
    nickname = nickname,
    imageUrl = imageUrl,
    isArchived = isArchived,
    isFavourite = isFavourite,
    lastWateredAt = lastWateredAt?.let { Instant.parse(it).toEpochMilli() },
    createdAt = Instant.parse(createdAt).toEpochMilli(),
    synced = true,
)
