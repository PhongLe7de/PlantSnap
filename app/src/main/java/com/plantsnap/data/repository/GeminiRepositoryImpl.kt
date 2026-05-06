package com.plantsnap.data.repository

import android.util.Log
import com.google.genai.Client
import com.plantsnap.data.wikipedia.WikipediaApi
import com.plantsnap.domain.models.PlantAiInfo
import com.plantsnap.domain.models.PlantOfTheDay
import com.plantsnap.domain.models.SupabaseProfile
import com.plantsnap.domain.models.TemperatureUnit
import com.plantsnap.domain.repository.GeminiRepository
import com.plantsnap.domain.repository.ProfileRepository
import com.plantsnap.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class GeminiRepositoryImpl @Inject constructor(
    private val client: Client,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val wikipediaApi: WikipediaApi,
    private val json: Json,
) : GeminiRepository {

    private companion object {
        const val TAG = "GeminiRepository"
        const val MODEL = "gemini-3.1-flash-lite-preview"
        const val PROFILE_CACHE_TTL_MS = 5 * 60 * 1000L
    }

    @Volatile private var profileCache: SupabaseProfile? = null
    @Volatile private var profileCacheExpiry = 0L

    /**
     * Extracted SDK call — open so tests can override without needing to mock
     * the concrete [Client] / [Models] classes.
     */
    protected open suspend fun callGemini(prompt: String): String =
        withContext(Dispatchers.IO) {
            val response = client.models.generateContent(MODEL, prompt, null)
            response.text() ?: error("Empty Gemini response")
        }

    private suspend fun getCachedProfile(): SupabaseProfile? {
        val now = System.currentTimeMillis()
        if (now < profileCacheExpiry) return profileCache
        val fresh = try { profileRepository.getProfile() } catch (_: Exception) { null }
        profileCache = fresh
        profileCacheExpiry = now + PROFILE_CACHE_TTL_MS
        return fresh
    }

    override suspend fun getPlantInfo(
        plantName: String,
    ): PlantAiInfo = withContext(Dispatchers.IO) {
        val profile = getCachedProfile()
        val settings = settingsRepository.getSettings()
        val petType = profile?.petType ?: "NONE"
        val plantInterests = profile?.plantInterests?.toSet() ?: emptySet()
        val experienceLevel = profile?.experienceLevel ?: "NOT SPECIFIED"

        val toxicity = buildToxicityInstruction(petType)
        val care = buildCareInstruction(experienceLevel, plantInterests)
        val tone = buildToneInstruction(experienceLevel)
        val tempUnit = buildTemperatureInstruction(settings.temperatureUnit.name)

        val prompt = """
            Return a JSON object describing the plant "$plantName" with exactly these fields:
            - "care": object with keys: "light" (short phrase), "water" (short phrase), "temperature" ($tempUnit), "humidity" (a numeric percentage range like "40-60%", not a descriptive phrase), "soil" (short phrase), "waterEveryDays" (typical interval in days between waterings as a positive integer between 1 and 60, or null if the plant doesn't need recurring watering), "fertilizeEveryDays" (positive integer between 7 and 365, or null if not applicable), "mistEveryDays" (positive integer between 1 and 14, or null if humidity needs are met without misting), "rotateEveryDays" (positive integer between 7 and 60, or null if rotation is not important for this plant), "repotEveryDays" (positive integer between 180 and 1825, or null if repotting is uncommon).$care
            - "toxicity": a single plain JSON string (NOT a nested object or array) of 1-2 sentences summarizing toxicity risks to humans, dogs, and cats. Every sentence MUST begin with the exposure route that triggers the risk (e.g. "If ingested,", "On skin contact,", "If inhaled,", "If the sap contacts skin,"). $toxicity
            - "safety": object with keys:
                "dog": { "level": "NONE|MILD|MODERATE|SEVERE|UNKNOWN", "symptoms": "1 sentence that MUST start with the exposure route (e.g. \"If ingested,\", \"On contact,\", \"If chewed,\") followed by the symptoms observed in dogs, or null if level is NONE/UNKNOWN" },
                "cat": { "level": "NONE|MILD|MODERATE|SEVERE|UNKNOWN", "symptoms": "1 sentence that MUST start with the exposure route followed by symptoms observed in cats, or null if level is NONE/UNKNOWN" },
                "human": { "level": "NONE|MILD|MODERATE|SEVERE|UNKNOWN", "symptoms": "1 sentence that MUST start with the exposure route followed by symptoms observed in humans, or null if level is NONE/UNKNOWN" },
                "edibility": "EDIBLE|INEDIBLE|TOXIC|UNKNOWN",
                "foragingNotes": "1 sentence on foraging caution (e.g. toxic lookalikes, preparation requirements, exposure routes), or null if not applicable".
            - "habitat": array of 2 objects, each with "title" (short label e.g. "Tropical Jungles", "Central America"), "body" (1 sentence), "latitude" (decimal degrees of a representative location in this habitat, e.g. 1.35), and "longitude" (decimal degrees, e.g. 103.87).
            - "description": 1-2 sentences about the plant's characteristics, history, and notable features.
            ${if (tone.isNotEmpty()) "$tone\n" else ""}Respond with ONLY the JSON object. No markdown fences, no commentary.
        """.trimIndent()

        val raw = callGemini(prompt)
        val cleaned = raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        json.decodeFromString(PlantAiInfo.serializer(), cleaned)
    }

    override suspend fun getPlantOfTheDay(): PlantOfTheDay = withContext(Dispatchers.IO) {
        val profile = getCachedProfile()
        val settings = settingsRepository.getSettings()
        val petType = profile?.petType
        val plantInterests = profile?.plantInterests?.toSet() ?: emptySet()
        val experienceLevel = profile?.experienceLevel

        val toxicity = buildToxicityInstruction(petType)
        val care = buildCareInstruction(experienceLevel, plantInterests)
        val tone = buildToneInstruction(experienceLevel)
        val tempUnit = buildTemperatureInstruction(settings.temperatureUnit.name)


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
            - "care": object with keys: "light" (short phrase), "water" (short phrase), "temperature" ($tempUnit), "humidity" (a numeric percentage range like "40-60%", not a descriptive phrase), "soil" (short phrase).$care
            - "toxicity": a single plain JSON string (NOT a nested object or array). Possible toxicity to humans and $toxicity
            - "habitat": array of 2 objects, each with "title" (short label), "body" (1 sentence), "latitude" (decimal degrees of a representative location), and "longitude" (decimal degrees).
            - "description": 1-2 sentences about the plant's characteristics and history.
            ${if (tone.isNotEmpty()) "$tone\n" else ""}Respond with ONLY the JSON object. No markdown fences, no commentary.
        """.trimIndent()

        val raw = callGemini(prompt)
        val cleaned = raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val potd = json.decodeFromString(PlantOfTheDay.serializer(), cleaned)

        val wikiImageUrl = coroutineScope {
            val byScientific = async { fetchWikipediaImage(potd.scientificName) }
            val byCommon = async { fetchWikipediaImage(potd.commonName) }
            byScientific.await() ?: byCommon.await()
        }
        val finalImageUrl = wikiImageUrl ?: potd.imageUrl
        Log.d(
            TAG,
            "getPlantOfTheDay final imageUrl=$finalImageUrl (wiki=$wikiImageUrl, gemini=${potd.imageUrl}) for ${potd.scientificName} / ${potd.commonName}",
        )
        potd.copy(imageUrl = finalImageUrl)
    }

    private suspend fun fetchWikipediaImage(title: String): String? {
        val trimmed = title.trim()
        if (trimmed.isBlank()) return null
        return runCatching {
            val summary = wikipediaApi.summary(trimmed)
            summary.thumbnail?.source ?: summary.originalimage?.source
        }.onFailure { error ->
            Log.w(TAG, "Wikipedia lookup failed for '$trimmed'", error)
        }.getOrNull()
    }

    private fun buildTemperatureInstruction(temperatureUnit: String): String = when (temperatureUnit) {
        "FAHRENHEIT" -> "temperature in °F only (e.g. \"65-85°F\")"
        else -> "temperature in °C only (e.g. \"18-30°C\")"
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