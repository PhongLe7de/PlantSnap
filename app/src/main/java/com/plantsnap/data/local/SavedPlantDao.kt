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

    @Query(
        """
        SELECT sp.*, pd.scientificName AS details_scientificName
        FROM saved_plants sp
        INNER JOIN plant_details pd ON pd.plantGbifId = sp.plantGbifId
        WHERE sp.id = :id
        LIMIT 1
        """
    )
    fun observeWithDetailsById(id: String): Flow<SavedPlantWithDetails?>

    @Query("""
        SELECT EXISTS(
          SELECT 1 FROM saved_plants
          WHERE originalScanId = :scanId AND plantGbifId = :plantGbifId AND isArchived = 0
        )
    """)
    fun observeIsSaved(scanId: String, plantGbifId: Long): Flow<Boolean>

    @Query("SELECT * FROM saved_plants WHERE originalScanId = :scanId AND plantGbifId = :plantGbifId LIMIT 1")
    suspend fun findExisting(scanId: String, plantGbifId: Long): SavedPlantEntity?

    @Query("""
        SELECT sp.*, pd.scientificName AS details_scientificName
        FROM saved_plants sp
        INNER JOIN plant_details pd ON pd.plantGbifId = sp.plantGbifId
        WHERE sp.originalScanId = :scanId
          AND sp.plantGbifId = :plantGbifId
          AND sp.isArchived = 0
        LIMIT 1
    """)
    fun observeSavedFor(scanId: String, plantGbifId: Long): Flow<SavedPlantWithDetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SavedPlantEntity)

    @Query("SELECT * FROM saved_plants WHERE synced = 0")
    suspend fun getUnsynced(): List<SavedPlantEntity>

    @Query("UPDATE saved_plants SET synced = 1, userId = :userId WHERE id = :id")
    suspend fun markSynced(id: String, userId: String)

    @Query("SELECT id FROM saved_plants")
    suspend fun getAllIds(): List<String>

    @Query("UPDATE saved_plants SET isArchived = :v, synced = 0 WHERE id = :id")
    suspend fun setArchived(id: String, v: Boolean)

    @Query("UPDATE saved_plants SET nickname = :nickname, synced = 0 WHERE id = :id")
    suspend fun updateNickname(id: String, nickname: String)

    @Query("UPDATE saved_plants SET isFavourite = :isFavourite, synced = 0 WHERE id = :id")
    suspend fun updateFavourite(id: String, isFavourite: Boolean)

    @Query("UPDATE saved_plants SET lastWateredAt = :timestamp, synced = 0 WHERE id = :id")
    suspend fun updateLastWatered(id: String, timestamp: Long?)

    @Query("UPDATE saved_plants SET lastWateredAt = :timestamp, synced = 0 WHERE id IN (:ids)")
    suspend fun updateLastWateredBulk(ids: List<String>, timestamp: Long?)

    @Query("DELETE FROM saved_plants")
    suspend fun deleteAll()
}
