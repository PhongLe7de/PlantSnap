package com.plantsnap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.plantsnap.data.local.model.SavedPlantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPlantDao {
    @Query("SELECT * FROM saved_plants ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<SavedPlantEntity>>

    @Query("""
        SELECT EXISTS(
          SELECT 1 FROM saved_plants
          WHERE sourceScanId = :scanId AND scientificName = :name
        )
    """)
    fun observeIsSaved(scanId: String, name: String): Flow<Boolean>

    @Query("SELECT * FROM saved_plants WHERE sourceScanId = :scanId AND scientificName = :name LIMIT 1")
    suspend fun findExisting(scanId: String, name: String): SavedPlantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SavedPlantEntity)

    @Query("DELETE FROM saved_plants WHERE id = :id")
    suspend fun deleteById(id: String)
}
