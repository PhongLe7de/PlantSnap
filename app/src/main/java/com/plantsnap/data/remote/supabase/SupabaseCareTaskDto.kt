package com.plantsnap.data.remote.supabase

import com.plantsnap.data.local.model.CareTaskEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class SupabaseCareTaskDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("saved_plant_id") val savedPlantId: String,
    @SerialName("task_type") val taskType: String,
    @SerialName("cadence_days") val cadenceDays: Int,
    @SerialName("next_due_at") val nextDueAt: String,
    @SerialName("last_completed_at") val lastCompletedAt: String? = null,
    val enabled: Boolean,
    @SerialName("user_override") val userOverride: Boolean,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

fun CareTaskEntity.toSupabaseDto(userId: String): SupabaseCareTaskDto = SupabaseCareTaskDto(
    id = id,
    userId = userId,
    savedPlantId = savedPlantId,
    taskType = taskType,
    cadenceDays = cadenceDays,
    nextDueAt = Instant.ofEpochMilli(nextDueAt).toString(),
    lastCompletedAt = lastCompletedAt?.let { Instant.ofEpochMilli(it).toString() },
    enabled = enabled,
    userOverride = userOverride,
    createdAt = Instant.ofEpochMilli(createdAt).toString(),
    updatedAt = Instant.ofEpochMilli(updatedAt).toString(),
)

fun SupabaseCareTaskDto.toEntity(): CareTaskEntity = CareTaskEntity(
    id = id,
    savedPlantId = savedPlantId,
    taskType = taskType,
    cadenceDays = cadenceDays,
    nextDueAt = Instant.parse(nextDueAt).toEpochMilli(),
    lastCompletedAt = lastCompletedAt?.let { Instant.parse(it).toEpochMilli() },
    enabled = enabled,
    userOverride = userOverride,
    createdAt = Instant.parse(createdAt).toEpochMilli(),
    updatedAt = Instant.parse(updatedAt).toEpochMilli(),
    synced = true,
)
