package com.plantsnap.data.sync

import android.util.Log
import com.plantsnap.data.device.DeviceIdProvider
import com.plantsnap.data.local.model.toEntity
import com.plantsnap.data.supabase.SupabaseScanDto
import com.plantsnap.data.supabase.toScanResult
import com.plantsnap.data.supabase.toSupabaseDto
import com.plantsnap.domain.repository.ScanRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanSyncManager @Inject constructor(
    private val supabase: SupabaseClient,
    private val scanRepo: ScanRepository,
    private val deviceIdProvider: DeviceIdProvider,
    private val json: Json,
) {
    private companion object {
        const val TAG = "ScanSyncManager"
        const val TABLE = "scans"
    }

    private val mutex = Mutex()

    /** Pull remote → local, then push local → remote. Use for any full-refresh trigger. */
    suspend fun sync() = mutex.withLock {
        val userId = supabase.auth.currentUserOrNull()?.id
        if (userId == null) {
            Log.d(TAG, "sync: not authenticated, skipping")
            return@withLock
        }
        pullFromRemoteInternal(userId)
        pushPendingInternal(userId)
    }

    /** Push-only path used right after a local save, before the pull trigger fires. */
    suspend fun syncPending() = mutex.withLock {
        val userId = supabase.auth.currentUserOrNull()?.id
        if (userId == null) {
            Log.d(TAG, "syncPending: not authenticated, skipping")
            return@withLock
        }
        pushPendingInternal(userId)
    }

    private suspend fun pullFromRemoteInternal(userId: String) {
        val remote: List<SupabaseScanDto> = try {
            supabase.postgrest.from(TABLE)
                .select { filter { eq("user_id", userId) } }
                .decodeList()
        } catch (e: Exception) {
            Log.w(TAG, "pull failed", e)
            return
        }

        if (remote.isEmpty()) {
            Log.d(TAG, "pull: no remote scans for user")
            return
        }

        val localIds = scanRepo.getAllScanIds().toHashSet()
        var inserted = 0
        for (dto in remote) {
            if (dto.id in localIds) continue
            try {
                scanRepo.save(dto.toScanResult(json))
                inserted++
            } catch (e: Exception) {
                Log.w(TAG, "pull: failed to hydrate ${dto.id}", e)
            }
        }
        Log.d(TAG, "pull: inserted $inserted new scan(s) from Supabase (${remote.size} total remote)")
    }

    private suspend fun pushPendingInternal(userId: String) {
        val unsynced = scanRepo.getUnsynced()
        if (unsynced.isEmpty()) return

        Log.d(TAG, "push: draining ${unsynced.size} scan(s) to Supabase")
        for (scan in unsynced) {
            try {
                val dto = scan.toEntity().toSupabaseDto(userId, deviceIdProvider.deviceId, json)
                supabase.postgrest.from(TABLE).insert(dto)
                scanRepo.markSynced(scan.id)
                Log.d(TAG, "synced ${scan.id}")
            } catch (e: Exception) {
                Log.w(TAG, "push failed for ${scan.id}; will retry on next trigger", e)
            }
        }
    }
}
