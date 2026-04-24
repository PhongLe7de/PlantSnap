package com.plantsnap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.plantsnap.data.local.model.CandidateEntity
import com.plantsnap.data.local.model.SavedPlantEntity
import com.plantsnap.data.local.model.ScanEntity


@Database(
    entities = [ScanEntity::class, CandidateEntity::class, SavedPlantEntity::class],
    version = 6,
)
abstract class PlantSnapDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
    abstract fun savedPlantDao(): SavedPlantDao
}
