package com.plantsnap.data

import android.content.Context
import androidx.room.Room
import com.plantsnap.data.local.PlantSnapDatabase
import com.plantsnap.data.local.ScanDao
import com.plantsnap.data.repository.ScanRepositoryImpl
import com.plantsnap.domain.repository.ScanRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    @Binds
    @Singleton
    abstract fun bindScanRepository(impl: ScanRepositoryImpl): ScanRepository

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): PlantSnapDatabase =
            Room.databaseBuilder(
                context,
                PlantSnapDatabase::class.java,
                "plantsnap.db"
            )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()

        @Provides
        @Singleton
        fun provideScanDao(db: PlantSnapDatabase): ScanDao = db.scanDao()
    }
}
