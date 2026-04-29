package com.plantsnap.data.sync

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedPlantSyncObserver @Inject constructor(
    private val supabase: SupabaseClient,
    private val savedPlantSyncManager: SavedPlantSyncManager,
) {
    private companion object {
        const val TAG = "SavedPlantSyncObserver"
    }

    fun start(scope: CoroutineScope) {
        Log.d(TAG, "start: collecting sessionStatus")
        scope.launch {
            supabase.auth.sessionStatus
                .distinctUntilChangedBy { it::class }
                .collect { status ->
                Log.d(TAG, "sessionStatus = ${status::class.simpleName}")
                if (status is SessionStatus.Authenticated) {
                    Log.d(TAG, "session authenticated — syncing saved plants (pull + push)")
                    try {
                        savedPlantSyncManager.sync()
                        Log.d(TAG, "sync returned")
                    } catch (e: Exception) {
                        Log.w(TAG, "sync threw ${e::class.simpleName}: ${e.message}", e)
                    }
                }
            }
        }
    }
}
