package com.plantsnap.domain.services

import com.plantsnap.data.local.model.CareTaskEntity
import com.plantsnap.domain.models.CareInfo
import com.plantsnap.domain.models.CareTaskType
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pure (no I/O) projection of Gemini-supplied cadences onto a saved plant's care
 * task rows. The repository reads the existing rows and passes them in so this
 * stays unit-testable — see CareTaskGeneratorTest.
 */
@Singleton
class CareTaskGenerator @Inject constructor() {

    /**
     * Returns the canonical set of five care task rows for the plant. For each
     * [CareTaskType]:
     *   - if an existing row has [CareTaskEntity.userOverride] = true, it's preserved verbatim.
     *   - else if a row exists with the same cadence/enabled state, it's preserved
     *     verbatim — important so a later regeneration (Hook 2) doesn't reset the
     *     "first task" taste below.
     *   - else if a row exists with a changed cadence, nextDueAt is recomputed from
     *     the row's lastCompletedAt (or createdAt as a floor).
     *   - else a fresh row is created. On the very first generation
     *     ([existing] is empty), the first enabled task in canonical order
     *     (WATER → FERTILIZE → MIST → ROTATE → REPOT) gets `nextDueAt = now` so
     *     the user has an immediate task to try on day one. All other fresh rows
     *     get `nextDueAt = now + cadenceDays`.
     *
     * A null/zero/out-of-range Gemini cadence is treated as "task not applicable":
     * the row is stored with cadenceDays=0 and enabled=false. The user can flip it
     * on later via the UI (see [CareTaskType.defaultCadenceDays] for the fallback).
     *
     * Caller must upsert all returned entities; rows whose data hasn't changed are
     * still returned so the repository can do a single bulk upsert.
     */
    fun generate(
        savedPlantId: String,
        careInfo: CareInfo?,
        existing: List<CareTaskEntity>,
        now: Long,
    ): List<CareTaskEntity> {
        val byType = existing.associateBy { it.taskType }
        val isFirstGeneration = existing.isEmpty()
        val firstEnabledType: CareTaskType? = if (isFirstGeneration) {
            CareTaskType.entries.firstOrNull { sanitize(careInfo?.cadenceFor(it)) != null }
        } else null

        return CareTaskType.entries.map { type ->
            val current = byType[type.name]
            if (current?.userOverride == true) {
                return@map current
            }
            val rawCadence = careInfo?.cadenceFor(type)
            val sanitized = sanitize(rawCadence)
            val cadenceDays = sanitized ?: 0
            val enabled = sanitized != null
            if (current != null) {
                val cadenceChanged = current.cadenceDays != cadenceDays
                val enabledChanged = current.enabled != enabled
                if (!cadenceChanged && !enabledChanged) {
                    // Preserve nextDueAt as-is so a Hook 2 regen doesn't move it
                    // (e.g., overwriting the first-task taste, or a manually set value).
                    current
                } else {
                    val nextDueAt = if (enabled) {
                        (current.lastCompletedAt ?: current.createdAt) + cadenceDays.daysToMillis()
                    } else {
                        current.nextDueAt
                    }
                    current.copy(
                        cadenceDays = cadenceDays,
                        enabled = enabled,
                        nextDueAt = nextDueAt,
                        updatedAt = now,
                        synced = false,
                    )
                }
            } else {
                val nextDueAt = if (enabled) {
                    if (type == firstEnabledType) now
                    else now + cadenceDays.daysToMillis()
                } else now
                CareTaskEntity(
                    id = UUID.randomUUID().toString(),
                    savedPlantId = savedPlantId,
                    taskType = type.name,
                    cadenceDays = cadenceDays,
                    nextDueAt = nextDueAt,
                    lastCompletedAt = null,
                    enabled = enabled,
                    userOverride = false,
                    createdAt = now,
                    updatedAt = now,
                    synced = false,
                )
            }
        }
    }

    private fun CareInfo.cadenceFor(type: CareTaskType): Int? = when (type) {
        CareTaskType.WATER -> waterEveryDays
        CareTaskType.FERTILIZE -> fertilizeEveryDays
        CareTaskType.MIST -> mistEveryDays
        CareTaskType.ROTATE -> rotateEveryDays
        CareTaskType.REPOT -> repotEveryDays
    }

    private fun sanitize(value: Int?): Int? {
        if (value == null) return null
        if (value !in MIN_CADENCE_DAYS..MAX_CADENCE_DAYS) return null
        return value
    }

    private fun Int.daysToMillis(): Long = this * MILLIS_PER_DAY

    companion object {
        const val MIN_CADENCE_DAYS = 1
        const val MAX_CADENCE_DAYS = 3650
        const val MILLIS_PER_DAY = 86_400_000L
    }
}
