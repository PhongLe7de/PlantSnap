package com.plantsnap.domain.repository

import com.plantsnap.domain.models.PlantAiInfo
import com.plantsnap.domain.models.PlantOfTheDay

interface GeminiRepository {
    suspend fun getPlantInfo(plantName: String): PlantAiInfo
    suspend fun getPlantOfTheDay(): PlantOfTheDay
}
