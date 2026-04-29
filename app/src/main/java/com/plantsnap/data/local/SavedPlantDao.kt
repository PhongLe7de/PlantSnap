package com.plantsnap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.plantsnap.data.local.model.SavedPlantEntity
import com.plantsnap.data.local.model.SavedPlantWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPlantDao {
    @Query(
        """
        SELECT sp.*, pd.scientificName AS details_scientificName
        FROM saved_plants sp
        INNER JOIN plant_details pd ON pd.plantGbifId = sp.plantGbifId
        WHERE sp.isArchived = 0
        ORDER BY sp.createdAt DESC
        """
    )
    fun observeAllWithDetails(): Flow<List<SavedPlantWithDetails>>

    @Query("""
        SELECT EXISTS(
          SELECT 1 FROM saved_plants
          WHERE originalScanId = :scanId AND plantGbifId = :plantGbifId AND isArchived = 0
        )
    """)
    fun observeIsSaved(scanId: String, plantGbifId: Long): Flow<Boolean>

    @Query("SELECT * FROM saved_plants WHERE originalScanId = :scanId AND plantGbifId = :plantGbifId LIMIT 1")
    suspend fun findExisting(scanId: String, plantGbifId: Long): SavedPlantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SavedPlantEntity)

    @Query("SELECT * FROM saved_plants WHERE synced = 0")
    suspend fun getUnsynced(): List<SavedPlantEntity>

    @Query("UPDATE saved_plants SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("SELECT id FROM saved_plants")
    suspend fun getAllIds(): List<String>

    @Query("UPDATE saved_plants SET isArchived = :v, synced = 0 WHERE id = :id")
    suspend fun setArchived(id: String, v: Boolean)
}
