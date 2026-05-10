package com.plantsnap.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.plantsnap.domain.models.CareTask
import com.plantsnap.domain.models.CareTaskType

@Entity(
    tableName = "care_tasks",
    foreignKeys = [
        ForeignKey(
            entity = SavedPlantEntity::class,
            parentColumns = ["id"],
            childColumns = ["savedPlantId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["savedPlantId", "taskType"], unique = true),
        Index("savedPlantId"),
        Index("nextDueAt"),
        Index("synced"),
    ],
)
data class CareTaskEntity(
    @PrimaryKey val id: String,
    val savedPlantId: String,
    val taskType: String,
    val cadenceDays: Int,
    val nextDueAt: Long,
    val lastCompletedAt: Long?,
    val enabled: Boolean,
    val userOverride: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val synced: Boolean,
)

fun CareTaskEntity.toDomain(): CareTask = CareTask(
    id = id,
    savedPlantId = savedPlantId,
    taskType = CareTaskType.fromName(taskType) ?: CareTaskType.WATER,
    cadenceDays = cadenceDays,
    nextDueAt = nextDueAt,
    lastCompletedAt = lastCompletedAt,
    enabled = enabled,
    userOverride = userOverride,
)
