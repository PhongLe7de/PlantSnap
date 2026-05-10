package com.plantsnap.ui.screens.garden

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.ui.graphics.vector.ImageVector
import com.plantsnap.R
import com.plantsnap.data.local.model.CareTaskWithPlant
import com.plantsnap.domain.models.CareTask
import com.plantsnap.domain.models.CareTaskType
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.max

/** UI projection for the cross-plant Garden / Home rows. The per-plant Plant Detail
 *  section reuses [DueLabel] / [dueLabelFor] but operates on raw [CareTask] domain
 *  models since the parent screen already provides the plant context. */
data class CareTaskUi(
    val id: String,
    val savedPlantId: String,
    val taskType: CareTaskType,
    val plantNickname: String,
    val plantSpecies: String,
    val plantImageUrl: String?,
    val cadenceDays: Int,
    val enabled: Boolean,
    val nextDueAt: Long,
    val lastCompletedAt: Long?,
)

sealed interface DueLabel {
    data class Overdue(val daysLate: Int) : DueLabel
    data object DueToday : DueLabel
    data class Upcoming(val daysUntil: Int) : DueLabel
}

/** Buckets [nextDueAt] against the device-local day window anchored at [now]. Pure. */
fun dueLabelFor(
    nextDueAt: Long,
    now: Long,
    zoneId: ZoneId = ZoneId.systemDefault(),
): DueLabel {
    val today = java.time.Instant.ofEpochMilli(now).atZone(zoneId).toLocalDate()
    val startOfToday = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val endOfToday = today.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
    val day = 86_400_000L
    return when {
        nextDueAt < startOfToday -> {
            val daysLate = max(1, ((startOfToday - nextDueAt + day - 1) / day).toInt())
            DueLabel.Overdue(daysLate)
        }
        nextDueAt <= endOfToday -> DueLabel.DueToday
        else -> {
            val daysUntil = max(1, ((nextDueAt - endOfToday + day - 1) / day).toInt())
            DueLabel.Upcoming(daysUntil)
        }
    }
}

fun CareTaskWithPlant.toUi(resolvedImageUrl: String?): CareTaskUi = CareTaskUi(
    id = task.id,
    savedPlantId = task.savedPlantId,
    taskType = CareTaskType.fromName(task.taskType) ?: CareTaskType.WATER,
    plantNickname = plantNickname,
    plantSpecies = plantScientificName,
    plantImageUrl = resolvedImageUrl,
    cadenceDays = task.cadenceDays,
    enabled = task.enabled,
    nextDueAt = task.nextDueAt,
    lastCompletedAt = task.lastCompletedAt,
)

/** Static lookups for the five MVP task types. */
data class CareTaskVisuals(
    val icon: ImageVector,
    val shortLabelRes: Int,
)

fun CareTaskType.visuals(): CareTaskVisuals = when (this) {
    CareTaskType.WATER -> CareTaskVisuals(Icons.Filled.WaterDrop, R.string.care_task_water_short)
    CareTaskType.FERTILIZE -> CareTaskVisuals(Icons.Outlined.Eco, R.string.care_task_fertilize_short)
    CareTaskType.MIST -> CareTaskVisuals(Icons.Filled.Cloud, R.string.care_task_mist_short)
    CareTaskType.ROTATE -> CareTaskVisuals(Icons.Filled.Refresh, R.string.care_task_rotate_short)
    CareTaskType.REPOT -> CareTaskVisuals(Icons.Outlined.LocalFlorist, R.string.care_task_repot_short)
}
