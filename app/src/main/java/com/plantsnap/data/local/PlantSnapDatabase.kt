package com.plantsnap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.plantsnap.data.local.model.CandidateEntity
import com.plantsnap.data.local.model.ScanEntity


@Database(
    entities = [ScanEntity::class, CandidateEntity::class],
    version = 6,
)
abstract class PlantSnapDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao

    companion object {
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scans ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}