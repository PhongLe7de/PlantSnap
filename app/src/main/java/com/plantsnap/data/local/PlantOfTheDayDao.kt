package com.plantsnap.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.plantsnap.data.local.model.PlantOfTheDayEntity

@Dao
interface PlantOfTheDayDao {

    @Query("SELECT * FROM plant_of_the_day WHERE id = 1 AND cachedDate = :date LIMIT 1")
    suspend fun getForDate(date: String): PlantOfTheDayEntity?

    @Upsert
    suspend fun upsert(entity: PlantOfTheDayEntity)
}