package com.plantsnap.data.sync

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanSyncObserver @Inject constructor(
    private val supabase: SupabaseClient,
    private val scanSyncManager: ScanSyncManager,
) {
    private companion object {
        const val TAG = "ScanSyncObserver"
    }

    fun start(scope: CoroutineScope) {
        scope.launch {
            supabase.auth.sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) {
                    Log.d(TAG, "session authenticated — syncing (pull + push)")
                    scanSyncManager.sync()
                }
            }
        }
    }
}
