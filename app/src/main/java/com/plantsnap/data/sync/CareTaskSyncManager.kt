package com.plantsnap.data.sync

import android.util.Log
import com.plantsnap.data.local.CareTaskDao
import com.plantsnap.data.local.SavedPlantDao
import com.plantsnap.data.remote.supabase.SupabaseCareTaskDto
import com.plantsnap.data.remote.supabase.toEntity
import com.plantsnap.data.remote.supabase.toSupabaseDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CareTaskSyncManager @Inject constructor(
    private val supabase: SupabaseClient,
    private val careTaskDao: CareTaskDao,
    private val savedPlantDao: SavedPlantDao,
) {
    private companion object {
        const val TAG = "CareTaskSyncManager"
        const val TABLE = "care_tasks"
    }

    private val mutex = Mutex()

    /** Pull remote → local and push local → remote in parallel. */
    suspend fun sync() = mutex.withLock {
        val userId = supabase.auth.currentUserOrNull()?.id
        if (userId == null) {
            Log.d(TAG, "sync: not authenticated, skipping")
            return@withLock
        }
        coroutineScope {
            launch { pullFromRemoteInternal(userId) }
            launch { pushPendingInternal(userId) }
        }
    }

    /** Push-only path used right after a local mutation, before the auth-state pull fires. */
    suspend fun syncPending() = mutex.withLock {
        val userId = supabase.auth.currentUserOrNull()?.id
        if (userId == null) {
            Log.w(TAG, "syncPending: not authenticated, skipping (sign in to sync)")
            return@withLock
        }
        Log.d(TAG, "syncPending: starting for user=$userId")
        pushPendingInternal(userId)
        Log.d(TAG, "syncPending: done for user=$userId")
    }

    private suspend fun pullFromRemoteInternal(userId: String) {
        val remote: List<SupabaseCareTaskDto> = try {
            supabase.postgrest.from(TABLE)
                .select { filter { eq("user_id", userId) } }
                .decodeList()
        } catch (e: Exception) {
            Log.w(TAG, "pull failed", e)
            return
        }

        if (remote.isEmpty()) {
            Log.d(TAG, "pull: no remote care tasks for user")
            return
        }

        // FK on care_tasks.savedPlantId requires the parent saved_plants row to exist
        // locally. SavedPlantSyncManager always pulls before us, but if the remote
        // claims a parent that hasn't synced down yet we skip rather than violate the FK.
        val knownPlantIds = savedPlantDao.getAllIds().toHashSet()

        var inserted = 0
        var skippedNoParent = 0
        var skippedOlder = 0
        for (dto in remote) {
            if (dto.savedPlantId !in knownPlantIds) {
                skippedNoParent++
                continue
            }
            val incoming = try {
                dto.toEntity()
            } catch (e: Exception) {
                Log.w(TAG, "pull: failed to decode ${dto.id}", e)
                continue
            }
            // LWW reconcile: keep local if its updatedAt is at-or-after remote's.
            val existing = careTaskDao.get(incoming.id)
            if (existing != null && existing.updatedAt >= incoming.updatedAt) {
                skippedOlder++
                continue
            }
            try {
                careTaskDao.upsert(incoming)
                inserted++
            } catch (e: Exception) {
                Log.w(TAG, "pull: failed to upsert ${incoming.id}", e)
            }
        }
        Log.d(
            TAG,
            "pull: applied=$inserted, skippedNoParent=$skippedNoParent, skippedOlder=$skippedOlder, total=${remote.size}",
        )
    }

    private suspend fun pushPendingInternal(userId: String) {
        val unsynced = careTaskDao.getUnsynced()
        if (unsynced.isEmpty()) {
            Log.d(TAG, "push: no unsynced rows")
            return
        }

        Log.d(TAG, "push: draining ${unsynced.size} care task(s) to Supabase")
        for (task in unsynced) {
            try {
                val dto = task.toSupabaseDto(userId)
                supabase.postgrest.from(TABLE).upsert(dto)
                careTaskDao.markSynced(task.id)
                Log.d(TAG, "push: synced ${task.id} type=${task.taskType}")
            } catch (e: Exception) {
                Log.w(
                    TAG,
                    "push: failed for ${task.id} with ${e::class.simpleName}: ${e.message}; will retry on next trigger",
                    e,
                )
            }
        }
    }
}
