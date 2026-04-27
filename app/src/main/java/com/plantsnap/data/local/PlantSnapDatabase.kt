package com.plantsnap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.plantsnap.data.local.model.CandidateEntity
import com.plantsnap.data.local.model.SavedPlantEntity
import com.plantsnap.data.local.model.ScanEntity


@Database(
    entities = [ScanEntity::class, CandidateEntity::class, SavedPlantEntity::class],
    version = 8,
)
abstract class PlantSnapDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
    abstract fun savedPlantDao(): SavedPlantDao

    companion object {
        /** Adds the `isFavorite` column on `scans` shipped by PR #61. */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scans ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** Adds the `saved_plants` table powering the My Garden screen. */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                createSavedPlantsTable(db)
            }
        }

        private fun createSavedPlantsTable(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `saved_plants` (
                    `id` TEXT NOT NULL,
                    `candidateJson` TEXT NOT NULL,
                    `scientificName` TEXT NOT NULL,
                    `sourceScanId` TEXT,
                    `savedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`sourceScanId`) REFERENCES `scans`(`id`)
                        ON UPDATE NO ACTION ON DELETE SET NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_saved_plants_sourceScanId` " +
                    "ON `saved_plants` (`sourceScanId`)"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS " +
                    "`index_saved_plants_sourceScanId_scientificName` " +
                    "ON `saved_plants` (`sourceScanId`, `scientificName`)"
            )
        }

        /** Adds latitude/longitude columns to scans for GPS location capture. */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scans ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE scans ADD COLUMN longitude REAL")
            }
        }
    }
}