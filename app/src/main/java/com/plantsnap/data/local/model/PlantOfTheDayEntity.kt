package com.plantsnap.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey


// Single row cache
// When yyyy-MM-dd string matches today, we skip Gemini call

@Entity(tableName = "plant_of_the_day")
data class PlantOfTheDayEntity(
    @PrimaryKey val id: Int = 1,
    val cachedDate: String,
    val plantJson: String,
)