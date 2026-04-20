package com.plantsnap.data.repository

import com.google.genai.Client
import com.plantsnap.domain.models.PlantAiInfo
import com.plantsnap.domain.models.PlantOfTheDay
import com.plantsnap.domain.repository.GeminiRepository
import com.plantsnap.domain.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRepositoryImpl @Inject constructor(
    private val client: Client,
    private val profileRepository: ProfileRepository,
    private val json: Json,
) : GeminiRepository {

    private companion object {
        const val MODEL = "gemini-3.1-flash-lite-preview"
    }

    override suspend fun getPlantInfo(
        plantName: String,
    ): PlantAiInfo = withContext(Dispatchers.IO) {
        val profile = try { profileRepository.getProfile() } catch (e: Exception) { null }
        val petType = profile?.petType ?: "NONE"
        val plantInterests = profile?.plantInterests?.toSet() ?: emptySet()
        val experienceLevel = profile?.experienceLevel ?: "NOT SPECIFIED"

        val toxicity = buildToxicityInstruction(petType)
        val care = buildCareInstruction(experienceLevel, plantInterests)
        val tone = buildToneInstruction(experienceLevel)

        val prompt = """
            Return a JSON object describing the plant "$plantName" with exactly these fields:
            - "care": object with keys: "light" (short phrase), "water" (short phrase), "temperature" (include both °F and °C), "humidity" (short phrase), "soil" (short phrase).$care
            - "toxicity": Possible toxicity to humans and $toxicity
            - "habitat": array of 2 objects, each with "title" (short label e.g. "Tropical Jungles", "Central America"), "body" (1 sentence), and "imageUrl" (direct URL to a representative Wikimedia Commons photo, omit if unsure).
            - "description": 1-2 sentences about the plant's characteristics, history, and notable features.
            ${if (tone.isNotEmpty()) "$tone\n" else ""}Respond with ONLY the JSON object. No markdown fences, no commentary.
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

    override suspend fun getPlantOfTheDay(): PlantOfTheDay = withContext(Dispatchers.IO) {
        val profile = try { profileRepository.getProfile() } catch (_: Exception) { null }
        val petType = profile?.petType
        val plantInterests = profile?.plantInterests?.toSet() ?: emptySet()
        val experienceLevel = profile?.experienceLevel

        val toxicity = buildToxicityInstruction(petType)
        val care = buildCareInstruction(experienceLevel, plantInterests)
        val tone = buildToneInstruction(experienceLevel)

        val interestHint = if (plantInterests.isNotEmpty()) {
            "Pick a plant that fits these interests: ${plantInterests.joinToString(", ").lowercase().replace("_", " ")}."
        } else {
            "Pick any interesting, beautiful, or unique plant."
        }

        val prompt = """
            Recommend a random plant for "Plant of the Day". $interestHint
            Return a JSON object with exactly these fields:
            - "scientificName": the plant's scientific name
            - "commonName": the most popular common name
            - "care": object with keys: "light" (short phrase), "water" (short phrase), "temperature" (include both °F and °C), "humidity" (short phrase), "soil" (short phrase).$care
            - "toxicity": Possible toxicity to humans and $toxicity
            - "habitat": array of 2 objects, each with "title" (short label), "body" (1 sentence), and "imageUrl" (direct URL to a representative Wikimedia Commons photo, omit if unsure).
            - "description": 1-2 sentences about the plant's characteristics and history.
            - "imageUrl": a direct URL to a Wikimedia Commons photo of this plant (e.g., https://upload.wikimedia.org/...). If unsure, omit this field.
            ${if (tone.isNotEmpty()) "$tone\n" else ""}Respond with ONLY the JSON object. No markdown fences, no commentary.
        """.trimIndent()

        val response = client.models.generateContent(MODEL, prompt, null)
        val raw = response.text() ?: error("Empty Gemini response")
        val cleaned = raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        json.decodeFromString(PlantOfTheDay.serializer(), cleaned)
    }

    private fun buildToxicityInstruction(petType: String?): String = when (petType) {
        "DOG" -> "dogs. 1-2 sentences, mention symptoms if toxic."
        "CAT" -> "cats. 1-2 sentences, mention symptoms if toxic."
        "BOTH" -> "dogs and cats. 1-2 sentences, mention symptoms for each if toxic."
        else -> "pets. 1 sentence."
    }

    private fun buildCareInstruction(experienceLevel: String?, plantInterests: Set<String>): String {
        val levelHint = when (experienceLevel) {
            "BEGINNER" -> " Use simple, beginner-friendly language."
            "INTERMEDIATE" -> " Include practical tips and common mistakes to avoid."
            "EXPERT" -> " Include technical details like pH ranges, lux requirements, and advanced techniques."
            else -> ""
        }

        val contextHints = buildList {
            if ("INDOOR" in plantInterests) add("indoor growing conditions")
            if ("OUTDOOR" in plantInterests) add("outdoor planting considerations")
            if ("SUCCULENTS" in plantInterests) add("succulent-specific drainage needs")
            if ("EDIBLE" in plantInterests) add("food-safety considerations")
        }

        val contextStr = if (contextHints.isNotEmpty()) {
            " Consider: ${contextHints.joinToString(", ")}."
        } else ""

        return levelHint + contextStr
    }

    private fun buildToneInstruction(experienceLevel: String?): String = when (experienceLevel) {
        "BEGINNER" -> "Use encouraging, simple language. Avoid jargon."
        "EXPERT" -> "Use professional horticultural terminology."
        else -> ""
    }
}
