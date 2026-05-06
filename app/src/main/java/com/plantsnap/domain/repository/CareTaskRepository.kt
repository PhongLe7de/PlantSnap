package com.plantsnap.domain.repository

import com.plantsnap.data.local.model.CareTaskWithPlant
import com.plantsnap.domain.models.CareInfo
import com.plantsnap.domain.models.CareTask
import kotlinx.coroutines.flow.Flow

interface CareTaskRepository {
    fun observeForPlant(savedPlantId: String): Flow<List<CareTask>>

    /** Tasks due on or before [dayEndMillis], joined with plant display info. Used by
     *  the Garden "Today's Tasks" section. */
    fun observeDueBy(dayEndMillis: Long): Flow<List<CareTaskWithPlant>>

    /** Top [limit] enabled tasks across the garden, ordered by nextDueAt ascending.
     *  Used by the Home "Daily Care" widget. */
    fun observeNextDue(limit: Int): Flow<List<CareTaskWithPlant>>

    /** Idempotently upserts the canonical 5-row set of care tasks for a saved plant
     *  derived from the latest Gemini [careInfo]. Preserves user overrides and
     *  lastCompletedAt on existing rows. */
    suspend fun generateForSavedPlant(savedPlantId: String, careInfo: CareInfo?)

    /** Marks the task done at the current wall-clock time, advances nextDueAt by the
     *  task's cadence, and (for WATER) mirrors the completion timestamp into
     *  saved_plants.lastWateredAt for the existing "watered N days ago" UI. */
    suspend fun markCompleted(taskId: String)

    /** Persists a user-supplied cadence in days, sets userOverride=true so the next
     *  Gemini regeneration won't clobber it, and recomputes nextDueAt from the
     *  task's last-completed timestamp (falling back to created-at). */
    suspend fun setCadence(taskId: String, cadenceDays: Int)

    /** Toggles whether the task is part of the active schedule. Re-enabling a task
     *  with a zero cadence falls back to [com.plantsnap.domain.models.CareTaskType.defaultCadenceDays]. */
    suspend fun setEnabled(taskId: String, enabled: Boolean)
}
