package com.plantsnap

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.plantsnap.data.sync.ScanSyncObserver
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class PlantSnapApp : Application(), SingletonImageLoader.Factory {

    @Inject lateinit var scanSyncObserver: ScanSyncObserver

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        scanSyncObserver.start(appScope)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = {
                            OkHttpClient.Builder()
                                .addInterceptor { chain ->
                                    val request = chain.request().newBuilder()
                                        .header(
                                            "User-Agent",
                                            "PlantSnap-Android/1.0 (https://github.com/PhongLe7de/PlantSnap)",
                                        )
                                        .build()
                                    chain.proceed(request)
                                }
                                .build()
                        },
                    ),
                )
            }
            .build()
}
