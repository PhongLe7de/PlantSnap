package com.plantsnap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.plantsnap.data.local.model.CandidateEntity
import com.plantsnap.data.local.model.PlantDetailsEntity
import com.plantsnap.data.local.model.PlantOfTheDayEntity
import com.plantsnap.data.local.model.SavedPlantEntity
import com.plantsnap.data.local.model.ScanEntity


@Database(
    entities = [
        ScanEntity::class,
        CandidateEntity::class,
        SavedPlantEntity::class,
        PlantDetailsEntity::class,
        PlantOfTheDayEntity::class,
    ],
    version = 12,
)
abstract class PlantSnapDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
    abstract fun savedPlantDao(): SavedPlantDao
    abstract fun plantDetailsDao(): PlantDetailsDao
    abstract fun plantOfTheDayDao(): PlantOfTheDayDao

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

        /**
         * Reshapes `saved_plants` to mirror the Supabase columns (user_id, plant_gbif_id,
         * nickname, is_archived, is_favourite, last_watered_at, image_url, synced),
         * adds `gbifId` to `candidates` so each saved candidate can be pushed by its
         * GBIF id, and adds `imageUrl` to `scans` to hold the Storage bucket path of
         * the user's captured image.
         */
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE candidates ADD COLUMN gbifId INTEGER")
                db.execSQL("ALTER TABLE scans ADD COLUMN imageUrl TEXT")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `saved_plants_new` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT,
                        `plantGbifId` INTEGER,
                        `originalScanId` TEXT,
                        `scientificName` TEXT NOT NULL,
                        `nickname` TEXT NOT NULL,
                        `imageUrl` TEXT,
                        `isArchived` INTEGER NOT NULL,
                        `isFavourite` INTEGER NOT NULL,
                        `lastWateredAt` INTEGER,
                        `createdAt` INTEGER NOT NULL,
                        `synced` INTEGER NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`originalScanId`) REFERENCES `scans`(`id`)
                            ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO saved_plants_new
                        (id, userId, plantGbifId, originalScanId, scientificName, nickname,
                         imageUrl, isArchived, isFavourite, lastWateredAt, createdAt, synced)
                    SELECT id, NULL, NULL, sourceScanId, scientificName, scientificName,
                           NULL, 0, 0, NULL, savedAt, 0
                    FROM saved_plants
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE saved_plants")
                db.execSQL("ALTER TABLE saved_plants_new RENAME TO saved_plants")

                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_saved_plants_originalScanId` " +
                        "ON `saved_plants` (`originalScanId`)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS " +
                        "`index_saved_plants_originalScanId_scientificName` " +
                        "ON `saved_plants` (`originalScanId`, `scientificName`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_saved_plants_synced` " +
                        "ON `saved_plants` (`synced`)"
                )
            }
        }

        /**
         * Normalizes `scientificName` out of `saved_plants` into a new `plant_details`
         * table keyed by `plantGbifId`, mirroring the Supabase shape. Also makes
         * `saved_plants.plantGbifId` non-null and adds an FK to `plant_details`. Rows
         * with an unresolvable null `plantGbifId` (couldn't be derived from the matching
         * candidate either) are dropped — they could never be synced anyway.
         */
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `plant_details` (
                        `plantGbifId` INTEGER NOT NULL,
                        `scientificName` TEXT NOT NULL,
                        PRIMARY KEY(`plantGbifId`)
                    )
                    """.trimIndent()
                )

                // Backfill plant_details from rows that already have a gbifId.
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO plant_details (plantGbifId, scientificName)
                    SELECT plantGbifId, scientificName FROM saved_plants WHERE plantGbifId IS NOT NULL
                    """.trimIndent()
                )

                // Try to resolve null gbifIds from the matching candidate row.
                db.execSQL(
                    """
                    UPDATE saved_plants
                    SET plantGbifId = (
                        SELECT gbifId FROM candidates
                        WHERE candidates.scanId = saved_plants.originalScanId
                          AND candidates.scientificName = saved_plants.scientificName
                          AND candidates.gbifId IS NOT NULL
                        LIMIT 1
                    )
                    WHERE plantGbifId IS NULL
                    """.trimIndent()
                )

                // Re-run backfill for rows that just got resolved.
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO plant_details (plantGbifId, scientificName)
                    SELECT plantGbifId, scientificName FROM saved_plants WHERE plantGbifId IS NOT NULL
                    """.trimIndent()
                )

                // Drop rows we still couldn't resolve — they were unsyncable.
                db.execSQL("DELETE FROM saved_plants WHERE plantGbifId IS NULL")

                // Recreate saved_plants without scientificName, with non-null plantGbifId
                // and an FK to plant_details.
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `saved_plants_new` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT,
                        `plantGbifId` INTEGER NOT NULL,
                        `originalScanId` TEXT,
                        `nickname` TEXT NOT NULL,
                        `imageUrl` TEXT,
                        `isArchived` INTEGER NOT NULL,
                        `isFavourite` INTEGER NOT NULL,
                        `lastWateredAt` INTEGER,
                        `createdAt` INTEGER NOT NULL,
                        `synced` INTEGER NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`originalScanId`) REFERENCES `scans`(`id`)
                            ON UPDATE NO ACTION ON DELETE SET NULL,
                        FOREIGN KEY(`plantGbifId`) REFERENCES `plant_details`(`plantGbifId`)
                            ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO saved_plants_new
                        (id, userId, plantGbifId, originalScanId, nickname, imageUrl,
                         isArchived, isFavourite, lastWateredAt, createdAt, synced)
                    SELECT id, userId, plantGbifId, originalScanId, nickname, imageUrl,
                           isArchived, isFavourite, lastWateredAt, createdAt, synced
                    FROM saved_plants
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE saved_plants")
                db.execSQL("ALTER TABLE saved_plants_new RENAME TO saved_plants")

                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_saved_plants_originalScanId` " +
                        "ON `saved_plants` (`originalScanId`)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS " +
                        "`index_saved_plants_originalScanId_plantGbifId` " +
                        "ON `saved_plants` (`originalScanId`, `plantGbifId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_saved_plants_plantGbifId` " +
                        "ON `saved_plants` (`plantGbifId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_saved_plants_synced` " +
                        "ON `saved_plants` (`synced`)"
                )
            }
        }

        /** Adds the `plant_of_the_day` single-row cache table. */
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `plant_of_the_day` (
                        `id` INTEGER NOT NULL,
                        `cachedDate` TEXT NOT NULL,
                        `plantJson` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        /**
         * Recreates `scans` to the canonical shape: drops the lingering `DEFAULT 0`
         * on `isFavorite` (baked in by MIGRATION_5_6's `ADD COLUMN ... DEFAULT 0`)
         * and renames the legacy `thumbnailUrl` column to `imageUrl`
         */
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val hasImageUrl = db.query(
                    "SELECT 1 FROM pragma_table_info('scans') WHERE name = 'imageUrl'"
                ).use { it.moveToFirst() }
                val hasThumbnailUrl = db.query(
                    "SELECT 1 FROM pragma_table_info('scans') WHERE name = 'thumbnailUrl'"
                ).use { it.moveToFirst() }
                val sourceImageColumn = when {
                    hasImageUrl -> "imageUrl"
                    hasThumbnailUrl -> "thumbnailUrl"
                    else -> "NULL"
                }

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `scans_new` (
                        `id` TEXT NOT NULL,
                        `imagePath` TEXT NOT NULL,
                        `organ` TEXT NOT NULL,
                        `bestMatch` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `synced` INTEGER NOT NULL,
                        `rawResponseJson` TEXT,
                        `plantGbifId` TEXT,
                        `identificationScore` REAL,
                        `isFavorite` INTEGER NOT NULL,
                        `latitude` REAL,
                        `longitude` REAL,
                        `imageUrl` TEXT,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO scans_new
                        (id, imagePath, organ, bestMatch, timestamp, synced, rawResponseJson,
                         plantGbifId, identificationScore, isFavorite, latitude, longitude, imageUrl)
                    SELECT id, imagePath, organ, bestMatch, timestamp, synced, rawResponseJson,
                           plantGbifId, identificationScore, isFavorite, latitude, longitude, $sourceImageColumn
                    FROM scans
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE scans")
                db.execSQL("ALTER TABLE scans_new RENAME TO scans")
            }
        }
    }
}
