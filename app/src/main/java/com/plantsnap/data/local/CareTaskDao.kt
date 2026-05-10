package com.plantsnap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.plantsnap.data.local.model.CareTaskEntity
import com.plantsnap.data.local.model.CareTaskWithPlant
import kotlinx.coroutines.flow.Flow

@Dao
interface CareTaskDao {

    @Query(
        """
        SELECT * FROM care_tasks
        WHERE savedPlantId = :savedPlantId
        ORDER BY taskType ASC
        """
    )
    fun observeForPlant(savedPlantId: String): Flow<List<CareTaskEntity>>

    @Query(
        """
        SELECT ct.*,
               sp.nickname AS plant_nickname,
               sp.imageUrl AS plant_image_url,
               pd.scientificName AS plant_scientific_name
        FROM care_tasks ct
        INNER JOIN saved_plants sp ON sp.id = ct.savedPlantId
        INNER JOIN plant_details pd ON pd.plantGbifId = sp.plantGbifId
        WHERE ct.enabled = 1
          AND sp.isArchived = 0
          AND ct.nextDueAt <= :dayEndMillis
        ORDER BY ct.nextDueAt ASC
        """
    )
    fun observeDueBy(dayEndMillis: Long): Flow<List<CareTaskWithPlant>>

    @Query(
        """
        SELECT ct.*,
               sp.nickname AS plant_nickname,
               sp.imageUrl AS plant_image_url,
               pd.scientificName AS plant_scientific_name
        FROM care_tasks ct
        INNER JOIN saved_plants sp ON sp.id = ct.savedPlantId
        INNER JOIN plant_details pd ON pd.plantGbifId = sp.plantGbifId
        WHERE ct.enabled = 1
          AND sp.isArchived = 0
        ORDER BY ct.nextDueAt ASC
        LIMIT :limit
        """
    )
    fun observeNextDue(limit: Int): Flow<List<CareTaskWithPlant>>

    @Query("SELECT * FROM care_tasks WHERE id = :id LIMIT 1")
    suspend fun get(id: String): CareTaskEntity?

    @Query("SELECT * FROM care_tasks WHERE savedPlantId = :savedPlantId")
    suspend fun getAllForPlant(savedPlantId: String): List<CareTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CareTaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<CareTaskEntity>)

    @Query("SELECT * FROM care_tasks WHERE synced = 0")
    suspend fun getUnsynced(): List<CareTaskEntity>

    @Query("UPDATE care_tasks SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("SELECT id FROM care_tasks")
    suspend fun getAllIds(): List<String>

    @Query(
        """
        UPDATE care_tasks
        SET lastCompletedAt = :completedAt,
            nextDueAt = :nextDueAt,
            updatedAt = :updatedAt,
            synced = 0
        WHERE id = :id
        """
    )
    suspend fun markCompleted(id: String, completedAt: Long, nextDueAt: Long, updatedAt: Long)

    @Query(
        """
        UPDATE care_tasks
        SET cadenceDays = :cadenceDays,
            nextDueAt = :nextDueAt,
            userOverride = 1,
            updatedAt = :updatedAt,
            synced = 0
        WHERE id = :id
        """
    )
    suspend fun setCadence(id: String, cadenceDays: Int, nextDueAt: Long, updatedAt: Long)

    @Query(
        """
        UPDATE care_tasks
        SET enabled = :enabled,
            cadenceDays = :cadenceDays,
            nextDueAt = :nextDueAt,
            updatedAt = :updatedAt,
            synced = 0
        WHERE id = :id
        """
    )
    suspend fun setEnabled(id: String, enabled: Boolean, cadenceDays: Int, nextDueAt: Long, updatedAt: Long)

    @Query("DELETE FROM care_tasks")
    suspend fun deleteAll()
}
