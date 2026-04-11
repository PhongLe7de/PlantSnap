package com.plantsnap.domain.repository

interface GeminiRepository {
    suspend fun getPlantInfo(plantName: String): String
}
