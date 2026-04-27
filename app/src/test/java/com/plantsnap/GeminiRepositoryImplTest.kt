package com.plantsnap

import android.util.Log
import com.plantsnap.data.repository.GeminiRepositoryImpl
import com.plantsnap.data.wikipedia.WikipediaApi
import com.plantsnap.domain.models.SupabaseProfile
import com.plantsnap.domain.models.TemperatureUnit
import com.plantsnap.domain.models.UserSettings
import com.plantsnap.domain.repository.ProfileRepository
import com.plantsnap.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GeminiRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()
    private val json = Json { ignoreUnknownKeys }

    private lateinit var profileRepository: ProfileRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var repository: TestableGeminiRepository
    private lateinit var wikipediaApi: WikipediaApi

    private val validAiInfoJson = """
        {
          "care": {
            "light": "Bright indirect light",
            "water": "Every 1-2 weeks",
            "temperature": "65-85°F (18-30°C)",
            "humidity": "60%+",
            "soil": "Well-draining mix"
          },
          "toxicity": "Toxic to cats and dogs.",
          "habitat": [
            {"title": "Tropical Jungles", "body": "Flourishes in high humidity."},
            {"title": "Central America", "body": "Originates from southern Mexico."}
          ],
          "description": "A popular houseplant with large, perforated leaves."
        }
    """.trimIndent()

    private val validPlantOfDayJson = """
        {
          "scientificName": "Ficus lyrata",
          "commonName": "Fiddle Leaf Fig",
          "care": {
            "light": "Bright indirect light",
            "water": "When top inch is dry",
            "temperature": "60-75°F (15-24°C)",
            "humidity": "Moderate",
            "soil": "Well-draining potting mix"
          },
          "toxicity": "Mildly toxic to pets.",
          "habitat": [
            {"title": "West Africa", "body": "Native to lowland tropical rainforest."},
            {"title": "Rainforest", "body": "Grows in warm, humid conditions."}
          ],
          "description": "A striking plant with large, violin-shaped leaves."
        }        
    """.trimIndent()

    private fun makeProfile(
        petType: String? = null,
        experienceLevel: String? = null,
        plantInterests: List<String> = emptyList(),
    ): SupabaseProfile = SupabaseProfile(
        userId = "test-user",
        createdAt = "2024-01-01",
        petType = petType,
        experienceLevel = experienceLevel,
        plantInterests = plantInterests,
    )

    class TestableGeminiRepository(
        profileRepository: ProfileRepository,
        settingsRepository: SettingsRepository,
        wikipediaApi: WikipediaApi,
        json: Json,
    ) : GeminiRepositoryImpl(mockk(relaxed = true), profileRepository, settingsRepository, wikipediaApi, json) {

        var nextResponse: String = ""
        var lastPrompt: String = ""

        override suspend fun callGemini(prompt: String): String {
            lastPrompt = prompt
            return nextResponse
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>(), any()) } returns 0
        profileRepository = mockk()
        settingsRepository = mockk(relaxed = true)
        wikipediaApi = mockk(relaxed = true)
        coEvery { profileRepository.getProfile() } returns makeProfile()
        coEvery { wikipediaApi.summary(any()) } returns mockk(relaxed = true)
        repository = TestableGeminiRepository(profileRepository,settingsRepository,wikipediaApi, json)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getPlantInfo returns parsed PlantAiInfo on valid response`() = runTest {
        repository.nextResponse = validAiInfoJson

        val result = repository.getPlantInfo("Monstera deliciosa")
        advanceUntilIdle()

        assertEquals("Bright indirect light", result.care?.light)
        assertEquals("Toxic to cats and dogs.", result.toxicity)
        assertEquals(2, result.habitat?.size)
        assertEquals("A popular houseplant with large, perforated leaves.", result.description)
    }

    @Test
    fun `getPlantInfo strips markdown json fences from response`() = runTest {
        repository.nextResponse = "```json\n$validAiInfoJson\n```"

        val result = repository.getPlantInfo("Monstera deliciosa")
        advanceUntilIdle()

        assertNotNull(result)
        assertEquals("Bright indirect light", result.care?.light)
    }

    @Test
    fun `getPlantInfo strips plain code fences from response`() = runTest {
        repository.nextResponse = "```\n$validAiInfoJson\n```"

        val result = repository.getPlantInfo("Monstera deliciosa")
        advanceUntilIdle()

        assertNotNull(result)
        assertEquals("Bright indirect light", result.care?.light)
    }

    @Test(expected = Exception::class)
    fun `getPlantInfo throws on invalid JSON response`() = runTest {
        repository.nextResponse = "not valid json"

        repository.getPlantInfo("Monstera deliciosa")
        advanceUntilIdle()
    }

    @Test
    fun `getPlantInfo proceeds with defaults when getProfile throws`() = runTest {
        coEvery { profileRepository.getProfile() } throws RuntimeException("DB error")
        repository.nextResponse = validAiInfoJson

        val result = repository.getPlantInfo("Monstera deliciosa")
        advanceUntilIdle()

        assertNotNull(result)
    }

    @Test
    fun `getPlantInfo prompt mentions dogs for DOG petType`() = runTest {
        coEvery { profileRepository.getProfile() } returns makeProfile(petType = "DOG")
        repository.nextResponse = validAiInfoJson

        repository.getPlantInfo("Monstera deliciosa")
        advanceUntilIdle()

        assertTrue("Prompt should mention dogs", repository.lastPrompt.contains("dogs"))
    }

    @Test
    fun `getPlantInfo prompt mentions cats for CAT petType`() = runTest {
        coEvery { profileRepository.getProfile() } returns makeProfile(petType = "CAT")
        repository.nextResponse = validAiInfoJson

        repository.getPlantInfo("Monstera deliciosa")
        advanceUntilIdle()

        assertTrue("Prompt should mention cats", repository.lastPrompt.contains("cats"))
    }

    @Test
    fun `getPlantInfo prompt mentions dogs and cats for BOTH petType`() = runTest {
        coEvery { profileRepository.getProfile() } returns makeProfile(petType = "BOTH")
        repository.nextResponse = validAiInfoJson

        repository.getPlantInfo("Monstera deliciosa")
        advanceUntilIdle()

        assertTrue(
            "Prompt should mention both pets",
            repository.lastPrompt.contains("dogs and cats")
        )
    }

    @Test
    fun `getPlantInfo prompt uses generic pets for null petType`() = runTest {
        coEvery { profileRepository.getProfile() } returns makeProfile(petType = null)
        repository.nextResponse = validAiInfoJson

        repository.getPlantInfo("Monstera deliciosa")
        advanceUntilIdle()

        assertTrue("Prompt should use generic 'pets'", repository.lastPrompt.contains("pets"))
    }

    @Test
    fun `getPlantInfo prompt includes beginner-friendly hint for BEGINNER level`() = runTest {
        coEvery { profileRepository.getProfile() } returns makeProfile(experienceLevel = "BEGINNER")
        repository.nextResponse = validAiInfoJson

        repository.getPlantInfo("Monstera deliciosa")
        advanceUntilIdle()

        assertTrue(
            "Expected beginner-friendly hint",
            repository.lastPrompt.contains("beginner-friendly")
        )
    }

    @Test
    fun `getPlantInfo prompt includes expert hint for EXPERT level`() = runTest {
        coEvery { profileRepository.getProfile() } returns makeProfile(experienceLevel = "EXPERT")
        repository.nextResponse = validAiInfoJson

        repository.getPlantInfo("Monstera deliciosa")
        advanceUntilIdle()

        assertTrue("Expected expert hint", repository.lastPrompt.contains("pH ranges"))
    }

    @Test
    fun `getPlantInfo prompt includes indoor context for INDOOR interest`() = runTest {
        coEvery { profileRepository.getProfile() } returns makeProfile(plantInterests = listOf("INDOOR"))
        repository.nextResponse = validAiInfoJson

        repository.getPlantInfo("Monstera deliciosa")
        advanceUntilIdle()

        assertTrue("Expected indoor hint", repository.lastPrompt.contains("indoor"))
    }

    @Test
    fun `getPlantInfo prompt includes succulent context for SUCCULENTS interest`() = runTest {
        coEvery { profileRepository.getProfile() } returns makeProfile(plantInterests = listOf("SUCCULENTS"))
        repository.nextResponse = validAiInfoJson

        repository.getPlantInfo("Monstera deliciosa")
        advanceUntilIdle()

        assertTrue("Expected succulent hint", repository.lastPrompt.contains("succulent"))
    }

    @Test
    fun `getPlantInfo prompt includes multiple interest contexts`() = runTest {
        coEvery { profileRepository.getProfile() } returns makeProfile(
            plantInterests = listOf(
                "INDOOR",
                "EDIBLE"
            )
        )
        repository.nextResponse = validAiInfoJson

        repository.getPlantInfo("Monstera deliciosa")
        advanceUntilIdle()

        assertTrue("Expected indoor hint", repository.lastPrompt.contains("indoor"))
        assertTrue("Expected food-safety hint", repository.lastPrompt.contains("food-safety"))

    }

    @Test
    fun `getPlantOfTheDay returns parsed PlantOfTheDay on valid response`() = runTest {
        repository.nextResponse = validPlantOfDayJson

        val result = repository.getPlantOfTheDay()
        advanceUntilIdle()

        assertEquals("Ficus lyrata", result.scientificName)
        assertEquals("Fiddle Leaf Fig", result.commonName)
        assertEquals("Bright indirect light", result.care?.light)
    }

    @Test
    fun `getPlantOfTheDay strips markdown fences`() = runTest {
        repository.nextResponse = "```json\n$validPlantOfDayJson\n```"

        val result = repository.getPlantOfTheDay()
        advanceUntilIdle()

        assertEquals("Ficus lyrata", result.scientificName)

    }

    @Test(expected = Exception::class)
    fun `getPlantOfTheDay throws on invalid JSON`() = runTest {
        repository.nextResponse = "not valid json"

        repository.getPlantOfTheDay()
        advanceUntilIdle()
    }

    @Test
    fun `getPlantOfTheDay proceeds with defaults when getProfile throws`() = runTest {
        coEvery { profileRepository.getProfile() } throws RuntimeException("DB error")
        repository.nextResponse = validPlantOfDayJson

        val result = repository.getPlantOfTheDay()
        advanceUntilIdle()

        assertNotNull(result)
    }

    @Test
    fun `getPlantOfTheDay prompt includes interest hint when interests are set`() = runTest {
        coEvery { profileRepository.getProfile() } returns makeProfile(
            plantInterests = listOf("INDOOR", "SUCCULENTS")
        )
        repository.nextResponse = validPlantOfDayJson

        repository.getPlantOfTheDay()
        advanceUntilIdle()

        assertTrue(
            "Expected interest hint in prompt",
            repository.lastPrompt.contains("indoor") || repository.lastPrompt.contains("succulents")
        )
    }

    @Test
    fun `getPlantOfTheDay prompt uses generic hint when no interests set`() = runTest {
        coEvery { profileRepository.getProfile() } returns makeProfile(plantInterests = emptyList())
        repository.nextResponse = validPlantOfDayJson

        repository.getPlantOfTheDay()
        advanceUntilIdle()

        assertTrue(
            "Expected generic interest hint",
            repository.lastPrompt.contains("interesting, beautiful, or unique")
        )
    }

    @Test
    fun `getPlantOfTheDay prompt includes encouraging tone for BEGINNER`() = runTest {
        coEvery { profileRepository.getProfile() } returns makeProfile(experienceLevel = "BEGINNER")
        repository.nextResponse = validPlantOfDayJson

        repository.getPlantOfTheDay()
        advanceUntilIdle()

        assertTrue("Expected beginner tone hint", repository.lastPrompt.contains("encouraging"))
    }

    @Test
    fun `getPlantOfTheDay prompt includes professional tone for EXPERT`() = runTest {
        coEvery { profileRepository.getProfile() } returns makeProfile(experienceLevel = "EXPERT")
        repository.nextResponse = validPlantOfDayJson

        repository.getPlantOfTheDay()
        advanceUntilIdle()

        assertTrue("Expected expert tone hint", repository.lastPrompt.contains("horticultural"))
    }

    @Test
    fun `getPlantOfTheDay prompt has no tone instruction for INTERMEDIATE`() = runTest {
        coEvery { profileRepository.getProfile() } returns makeProfile(experienceLevel = "INTERMEDIATE")
        repository.nextResponse = validPlantOfDayJson

        repository.getPlantOfTheDay()
        advanceUntilIdle()

        assertTrue(
            "INTERMEDIATE should have no tone instruction",
            !repository.lastPrompt.contains("encouraging") && !repository.lastPrompt.contains("horticultural")
        )
    }

    @Test
    fun `getPlantOfTheDay uses Wikipedia image when available`() = runTest {
        coEvery { wikipediaApi.summary("Ficus lyrata") } returns mockk {
            every { thumbnail } returns mockk { every { source } returns "https://wiki.img/ficus.jpg" }
            every { originalimage } returns null
        }
        repository.nextResponse = validPlantOfDayJson

        val result = repository.getPlantOfTheDay()
        advanceUntilIdle()

        assertEquals("https://wiki.img/ficus.jpg", result.imageUrl)
    }

    @Test
    fun `getPlantOfTheDay falls back to Gemini imageUrl when Wikipedia returns null`() = runTest {
        coEvery { wikipediaApi.summary(any()) } returns mockk {
            every { thumbnail } returns null
            every { originalimage } returns null
        }
        val jsonWithImage = """
            {
              "scientificName": "Ficus lyrata",
              "commonName": "Fiddle Leaf Fig",
              "care": {
                "light": "Bright indirect light",
                "water": "When top inch is dry",
                "temperature": "60-75°F (15-24°C)",
                "humidity": "Moderate",
                "soil": "Well-draining potting mix"
              },
              "toxicity": "Mildly toxic to pets.",
              "habitat": [
                {"title": "West Africa", "body": "Native to lowland tropical rainforest."},
                {"title": "Rainforest", "body": "Grows in warm, humid conditions."}
              ],
              "description": "A striking plant with large, violin-shaped leaves.",
              "imageUrl": "https://gemini.img/plant.jpg"
            }
        """.trimIndent()
        repository.nextResponse = jsonWithImage

        val result = repository.getPlantOfTheDay()
        advanceUntilIdle()

        assertEquals("https://gemini.img/plant.jpg", result.imageUrl)
    }

    @Test
    fun `getPlantOfTheDay falls back to common name lookup when scientific name returns no image`() = runTest {
        coEvery { wikipediaApi.summary("Ficus lyrata") } returns mockk {
            every { thumbnail } returns null
            every { originalimage } returns null
        }
        coEvery { wikipediaApi.summary("Fiddle Leaf Fig") } returns mockk {
            every { thumbnail } returns mockk { every { source } returns "https://wiki.img/fiddle.jpg" }
            every { originalimage } returns null
        }
        repository.nextResponse = validPlantOfDayJson

        val result = repository.getPlantOfTheDay()
        advanceUntilIdle()

        assertEquals("https://wiki.img/fiddle.jpg", result.imageUrl)
    }

    @Test
    fun `getPlantInfo prompt requests Celsius only when unit is CELSIUS`() = runTest {
        coEvery { settingsRepository.getSettings() } returns UserSettings(temperatureUnit = TemperatureUnit.CELSIUS)
        repository.nextResponse = validAiInfoJson

        repository.getPlantInfo("Monstera deliciosa")
        advanceUntilIdle()

        assertTrue("Prompt should request °C", repository.lastPrompt.contains("°C"))
        assertTrue("Prompt should not request °F", !repository.lastPrompt.contains("°F"))
    }

    @Test
    fun `getPlantInfo prompt requests Fahrenheit only when unit is FAHRENHEIT`() = runTest {
        coEvery { settingsRepository.getSettings() } returns UserSettings(temperatureUnit = TemperatureUnit.FAHRENHEIT)
        repository.nextResponse = validAiInfoJson

        repository.getPlantInfo("Monstera deliciosa")
        advanceUntilIdle()

        assertTrue("Prompt should request °F", repository.lastPrompt.contains("°F"))
        assertTrue("Prompt should not request °C", !repository.lastPrompt.contains("°C"))
    }

    @Test
    fun `getPlantOfTheDay prompt requests Fahrenheit when unit is FAHRENHEIT`() = runTest {
        coEvery { settingsRepository.getSettings() } returns UserSettings(temperatureUnit = TemperatureUnit.FAHRENHEIT)
        repository.nextResponse = validPlantOfDayJson

        repository.getPlantOfTheDay()
        advanceUntilIdle()

        assertTrue("Prompt should request °F", repository.lastPrompt.contains("°F"))
        assertTrue("Prompt should not request °C", !repository.lastPrompt.contains("°C"))
    }
}