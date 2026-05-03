package com.plantsnap.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.plantsnap.data.local.model.PlantDetailsEntity

@Dao
interface PlantDetailsDao {

    @Upsert
    suspend fun upsert(entity: PlantDetailsEntity)

    @Upsert
    suspend fun upsertAll(entities: List<PlantDetailsEntity>)

    @Query("SELECT * FROM plant_details WHERE plantGbifId = :plantGbifId LIMIT 1")
    suspend fun get(plantGbifId: Long): PlantDetailsEntity?

    @Query("SELECT * FROM plant_details WHERE plantGbifId IN (:plantGbifIds)")
    suspend fun getAll(plantGbifIds: Collection<Long>): List<PlantDetailsEntity>
}
