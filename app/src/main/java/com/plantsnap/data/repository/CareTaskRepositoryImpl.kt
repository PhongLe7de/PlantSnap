package com.plantsnap.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.plantsnap.data.local.CareTaskDao
import com.plantsnap.data.local.PlantSnapDatabase
import com.plantsnap.data.local.SavedPlantDao
import com.plantsnap.data.local.model.CareTaskWithPlant
import com.plantsnap.data.local.model.toDomain
import com.plantsnap.data.sync.CareTaskSyncManager
import com.plantsnap.data.sync.SavedPlantSyncManager
import com.plantsnap.domain.models.CareInfo
import com.plantsnap.domain.models.CareTask
import com.plantsnap.domain.models.CareTaskType
import com.plantsnap.domain.repository.CareTaskRepository
import com.plantsnap.domain.services.CareTaskGenerator
import dagger.Lazy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CareTaskRepositoryImpl @Inject constructor(
    private val db: PlantSnapDatabase,
    private val careTaskDao: CareTaskDao,
    private val savedPlantDao: SavedPlantDao,
    private val generator: CareTaskGenerator,
    private val careTaskSyncManager: CareTaskSyncManager,
    // Lazy to break the Hilt construction cycle:
    // CareTaskRepository → SavedPlantSyncManager → PlantService → CareTaskRepository (Hook 2).
    private val savedPlantSyncManager: Lazy<SavedPlantSyncManager>,
) : CareTaskRepository {

    private companion object {
        const val TAG = "CareTaskRepository"
        const val MILLIS_PER_DAY = 86_400_000L
    }

    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun observeForPlant(savedPlantId: String): Flow<List<CareTask>> =
        careTaskDao.observeForPlant(savedPlantId).map { rows -> rows.map { it.toDomain() } }

    override fun observeDueBy(dayEndMillis: Long): Flow<List<CareTaskWithPlant>> =
        careTaskDao.observeDueBy(dayEndMillis)

    override fun observeNextDue(limit: Int): Flow<List<CareTaskWithPlant>> =
        careTaskDao.observeNextDue(limit)

    override suspend fun generateForSavedPlant(savedPlantId: String, careInfo: CareInfo?) {
        val now = System.currentTimeMillis()
        val existing = careTaskDao.getAllForPlant(savedPlantId)
        val rows = generator.generate(savedPlantId, careInfo, existing, now)
        // Bulk upsert; rows that didn't change still hit REPLACE but the DAO's
        // unique (savedPlantId, taskType) index keeps it idempotent.
        careTaskDao.upsertAll(rows)
        Log.d(TAG, "generate: plant=$savedPlantId rows=${rows.size} careInfoNonNull=${careInfo != null}")
        triggerCareTaskSync()
    }

    override suspend fun markCompleted(taskId: String) {
        val task = careTaskDao.get(taskId)
        if (task == null) {
            Log.w(TAG, "markCompleted: no task with id=$taskId")
            return
        }
        val now = System.currentTimeMillis()
        val cadenceDays = if (task.cadenceDays > 0) task.cadenceDays else {
            CareTaskType.defaultCadenceDays(CareTaskType.fromName(task.taskType) ?: CareTaskType.WATER)
        }
        val nextDueAt = now + cadenceDays * MILLIS_PER_DAY
        val mirrorWater = CareTaskType.fromName(task.taskType) == CareTaskType.WATER

        db.withTransaction {
            careTaskDao.markCompleted(taskId, completedAt = now, nextDueAt = nextDueAt, updatedAt = now)
            if (mirrorWater) {
                savedPlantDao.setLastWatered(task.savedPlantId, now)
            }
        }
        Log.d(TAG, "markCompleted: id=$taskId type=${task.taskType} nextDueAt=$nextDueAt")
        triggerCareTaskSync()
        if (mirrorWater) triggerSavedPlantSync()
    }

    override suspend fun setCadence(taskId: String, cadenceDays: Int) {
        val task = careTaskDao.get(taskId)
        if (task == null) {
            Log.w(TAG, "setCadence: no task with id=$taskId")
            return
        }
        val sanitized = cadenceDays.coerceIn(1, 3650)
        val now = System.currentTimeMillis()
        val baseTimestamp = task.lastCompletedAt ?: task.createdAt
        val nextDueAt = baseTimestamp + sanitized * MILLIS_PER_DAY
        careTaskDao.setCadence(taskId, cadenceDays = sanitized, nextDueAt = nextDueAt, updatedAt = now)
        Log.d(TAG, "setCadence: id=$taskId cadenceDays=$sanitized")
        triggerCareTaskSync()
    }

    override suspend fun setEnabled(taskId: String, enabled: Boolean) {
        val task = careTaskDao.get(taskId)
        if (task == null) {
            Log.w(TAG, "setEnabled: no task with id=$taskId")
            return
        }
        val now = System.currentTimeMillis()
        val cadenceDays = if (enabled && task.cadenceDays <= 0) {
            CareTaskType.defaultCadenceDays(CareTaskType.fromName(task.taskType) ?: CareTaskType.WATER)
        } else {
            task.cadenceDays
        }
        val nextDueAt = if (enabled) {
            val baseTimestamp = task.lastCompletedAt ?: task.createdAt
            baseTimestamp + cadenceDays * MILLIS_PER_DAY
        } else {
            task.nextDueAt
        }
        careTaskDao.setEnabled(
            id = taskId,
            enabled = enabled,
            cadenceDays = cadenceDays,
            nextDueAt = nextDueAt,
            updatedAt = now,
        )
        Log.d(TAG, "setEnabled: id=$taskId enabled=$enabled cadenceDays=$cadenceDays")
        triggerCareTaskSync()
    }

    private fun triggerCareTaskSync() {
        syncScope.launch {
            try {
                careTaskSyncManager.syncPending()
            } catch (e: Exception) {
                Log.w(TAG, "syncPending failed (will retry on next trigger)", e)
            }
        }
    }

    private fun triggerSavedPlantSync() {
        syncScope.launch {
            try {
                savedPlantSyncManager.get().syncPending()
            } catch (e: Exception) {
                Log.w(TAG, "savedPlant syncPending failed", e)
            }
        }
    }
}
