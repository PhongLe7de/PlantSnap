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
                - "care": object with keys: "light" (short phrase), "water" (short phrase), "temperature" (include both °F and °C), "humidity" (short phrase), "soil" (short phrase).
                - "toxicity": 1 sentence on toxicity to pets and humans.
                - "habitat": array of 2 objects, each with "title" (short label e.g. "Tropical Jungles", "Central America") and "body" (1 sentence).
                - "description": 1-2 sentences about the plant's characteristics, history, and notable features.
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
