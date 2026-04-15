package com.plantsnap

import android.app.Application
import com.plantsnap.data.sync.ScanSyncObserver
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

@HiltAndroidApp
class PlantSnapApp : Application() {

    @Inject lateinit var scanSyncObserver: ScanSyncObserver

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        scanSyncObserver.start(appScope)
    }
}
