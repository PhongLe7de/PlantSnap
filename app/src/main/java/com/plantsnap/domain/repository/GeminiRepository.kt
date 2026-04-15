package com.plantsnap.domain.repository

import com.plantsnap.domain.models.PlantAiInfo

interface GeminiRepository {
    suspend fun getPlantInfo(plantName: String): PlantAiInfo
}
