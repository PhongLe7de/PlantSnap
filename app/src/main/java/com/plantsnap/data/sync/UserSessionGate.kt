package com.plantsnap.data.sync

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.plantsnap.data.local.CareTaskDao
import com.plantsnap.data.local.SavedPlantDao
import com.plantsnap.data.local.ScanDao
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userSessionGateStore: DataStore<Preferences> by preferencesDataStore(name = "user_session_gate")

interface LastUserIdStore {
    suspend fun get(): String?
    suspend fun set(id: String)
}

@Singleton
class DataStoreLastUserIdStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : LastUserIdStore {
    private val key = stringPreferencesKey("last_signed_in_user_id")
    override suspend fun get(): String? = context.userSessionGateStore.data.first()[key]
    override suspend fun set(id: String) {
        context.userSessionGateStore.edit { it[key] = id }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class UserSessionGateModule {
    @Binds
    @Singleton
    abstract fun bindLastUserIdStore(impl: DataStoreLastUserIdStore): LastUserIdStore
}

/**
 * Wipes user-scoped Room tables when the authenticated user changes between sessions.
 *
 * Each SyncObserver calls [reconcile] before its sync; the mutex serializes the
 * concurrent calls so the wipe runs at most once per user-change.
 */
@Singleton
class UserSessionGate @Inject constructor(
    private val lastUserIdStore: LastUserIdStore,
    private val scanDao: ScanDao,
    private val savedPlantDao: SavedPlantDao,
    private val careTaskDao: CareTaskDao,
) {
    private val mutex = Mutex()

    suspend fun reconcile(userId: String) = mutex.withLock {
        val previous = lastUserIdStore.get()
        if (previous == userId) return@withLock
        if (previous != null) {
            Log.i("UserSessionGate", "user changed (${previous.take(8)} → ${userId.take(8)}); wiping user-scoped local tables")
            careTaskDao.deleteAll()
            savedPlantDao.deleteAll()
            scanDao.deleteAll()
        }
        lastUserIdStore.set(userId)
    }
}
