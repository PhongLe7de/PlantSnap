package com.plantsnap.domain.models

data class CareTask(
    val id: String,
    val savedPlantId: String,
    val taskType: CareTaskType,
    val cadenceDays: Int,
    val nextDueAt: Long,
    val lastCompletedAt: Long?,
    val enabled: Boolean,
    val userOverride: Boolean,
)
