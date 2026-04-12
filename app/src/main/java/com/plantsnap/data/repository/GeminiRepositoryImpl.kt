package com.plantsnap.data.repository

import com.google.genai.Client
import com.plantsnap.domain.repository.GeminiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRepositoryImpl @Inject constructor(
    private val client: Client
) : GeminiRepository {

    override suspend fun getPlantInfo(plantName: String): String {
        return withContext(Dispatchers.IO) {
            val response = client.models.generateContent(
                "gemini-3.1-flash-lite-preview",
                "Give a brief summary of the plant \"$plantName\". " +
                    "Include common names, care tips, and whether it is toxic to pets. " +
                    "Keep the response concise (under 150 words).",
                null
            )
            response.text() ?: "No information available."
        }
    }
}
