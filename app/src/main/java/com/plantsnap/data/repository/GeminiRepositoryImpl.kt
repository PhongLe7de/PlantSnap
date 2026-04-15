package com.plantsnap.data.repository

import com.google.genai.Client
import com.plantsnap.domain.models.PlantAiInfo
import com.plantsnap.domain.repository.GeminiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRepositoryImpl @Inject constructor(
    private val client: Client,
    private val json: Json,
) : GeminiRepository {

    private companion object {
        const val MODEL = "gemini-3.1-flash-lite-preview"
    }

    override suspend fun getPlantInfo(plantName: String): PlantAiInfo =
        withContext(Dispatchers.IO) {
            val prompt = """
                Return a JSON object describing the plant "$plantName" with exactly these fields:
                - "care": 1-2 sentences on light, water, and soil needs.
                - "toxicity": 1 sentence on toxicity to pets and humans.
                - "habitat": 1 sentence on native habitat or climate.
                Respond with ONLY the JSON object. No markdown fences, no commentary.
            """.trimIndent()

            val response = client.models.generateContent(MODEL, prompt, null)
            val raw = response.text() ?: error("Empty Gemini response")
            val cleaned = raw.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            json.decodeFromString(PlantAiInfo.serializer(), cleaned)
        }
}
