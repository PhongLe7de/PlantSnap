package com.plantsnap

import android.util.Log
import com.plantsnap.domain.models.CareInfo
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.HabitatInfo
import com.plantsnap.domain.models.PlantAiInfo
import com.plantsnap.domain.models.ScanResult
import com.plantsnap.domain.repository.ProfileRepository
import com.plantsnap.domain.repository.SavedPlantRepository
import com.plantsnap.domain.repository.ScanRepository
import com.plantsnap.domain.services.PlantService
import com.plantsnap.ui.screens.identify.detail.PlantDetailViewModel
import com.plantsnap.ui.state.UiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlantDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val scanRepository: ScanRepository = mockk()
    private val plantService: PlantService = mockk()
    private val profileRepository: ProfileRepository = mockk()
    private val savedPlantRepo: SavedPlantRepository = mockk(relaxed = true) {
        every { observeIsSaved(any(), any()) } returns flowOf(false)
    }

    private val json = Json { ignoreUnknownKeys = true }

    private lateinit var viewModel: PlantDetailViewModel

    private val plantAiInfo = PlantAiInfo(
        care = CareInfo(
            light = "Bright indirect light",
            water = "Every 1-2 weeks",
            temperature = "65-85°F (18-30°C)",
            humidity = "60%+",
            soil = "Well-draining mix",
        ),
        toxicity = "Toxic to cats and dogs.",
        habitat = listOf(
            HabitatInfo("Tropical Jungles", "Flourishes in high humidity"),
            HabitatInfo("Central America", "Originates from southern Mexico."),
        ),
        description = "A popular houseplant with large, perforated leaves."
    )

    private val serializedAiInfo = json.encodeToString(PlantAiInfo.serializer(), plantAiInfo)

    private fun makeCandidate(
        scientificName: String = "Monstera deliciosa",
        aiInfo: String? = null,
    ) = Candidate(
        scientificName = scientificName,
        commonNames = listOf("Swiss Cheese Plant"),
        family = "Araceae",
        score = 0.97f,
        iucnCategory = "LC",
        aiInfo = aiInfo
    )

    private fun makeScanResult(candidates: List<Candidate> = listOf(makeCandidate())) =
        ScanResult(
            id = "scan-1",
            imagePath = "/images/scan-1.jpg",
            organ = "leaf",
            bestMatch = candidates.firstOrNull()?.scientificName ?: "Unknown",
            candidates = candidates,
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>(), any()) } returns 0
        coEvery { profileRepository.getProfile() } returns null
        viewModel = PlantDetailViewModel(scanRepository, plantService, profileRepository, savedPlantRepo, json)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    @Test
    fun `initial state is Idle for both flows`() {
        assertTrue(viewModel.candidateState.value is UiState.Idle)
        assertTrue(viewModel.aiInfoState.value is UiState.Idle)
        assertTrue(viewModel.canRetry.value)
    }

    @Test
    fun `loadPlantDetail emits Success for candidate and fetches AI info`() = runTest {
        coEvery { scanRepository.observeById("scan-1") } returns flowOf(makeScanResult())
        coEvery { plantService.requestAdditionalInfo(any(), any()) } returns plantAiInfo

        viewModel.loadPlantDetail("scan-1", 0)
        advanceUntilIdle()

        val state = viewModel.candidateState.value as UiState.Success
        assertEquals("Monstera deliciosa", state.data.scientificName)
        assertTrue(viewModel.aiInfoState.value is UiState.Success)

    }

    @Test
    fun `loadPlantDetail sets candidateState to Loading synchronously`() {
        coEvery { scanRepository.observeById("scan-1") } returns flowOf(makeScanResult())
        coEvery { plantService.requestAdditionalInfo(any(), any()) } returns plantAiInfo

        viewModel.loadPlantDetail("scan-1", 0)

        assertTrue(viewModel.candidateState.value is UiState.Loading)
    }

    @Test
    fun `loadPlantDetail calls plantService with correct scanId and scientificName`() = runTest {
        val candidate = makeCandidate("Rosa canina")

        coEvery { scanRepository.observeById("scan-99") } returns flowOf(
            makeScanResult(
                listOf(
                    candidate
                )
            )
        )
        coEvery { plantService.requestAdditionalInfo("scan-99", "Rosa canina") } returns plantAiInfo

        viewModel.loadPlantDetail("scan-99", 0)
        advanceUntilIdle()

        coVerify(exactly = 1) { plantService.requestAdditionalInfo("scan-99", "Rosa canina") }
    }

    @Test
    fun `loadPlantDetail selects correct candidate by index`() = runTest {
        val candidates = listOf(
            makeCandidate("Monstera deliciosa"),
            makeCandidate("Ficus lyrata"),
            makeCandidate("Pothos aureus")
        )

        coEvery { scanRepository.observeById("scan-1") } returns flowOf(makeScanResult(candidates))
        coEvery { plantService.requestAdditionalInfo(any(), "Ficus lyrata") } returns plantAiInfo

        viewModel.loadPlantDetail("scan-1", 1)
        advanceUntilIdle()

        val state = viewModel.candidateState.value as UiState.Success
        assertEquals("Ficus lyrata", state.data.scientificName)
        coVerify(exactly = 1) { plantService.requestAdditionalInfo(any(), "Ficus lyrata") }
    }

    @Test
    fun `loadPlantDetail uses cached aiInfo and skips network call`() = runTest {
        val candidate = makeCandidate(aiInfo = serializedAiInfo)
        coEvery { scanRepository.observeById("scan-1") } returns flowOf(
            makeScanResult(
                listOf(
                    candidate
                )
            )
        )

        viewModel.loadPlantDetail("scan-1", 0)
        advanceUntilIdle()

        coVerify(exactly = 0) { plantService.requestAdditionalInfo(any(), any()) }
        val aiState = viewModel.aiInfoState.value as UiState.Success
        assertEquals(plantAiInfo.description, aiState.data.description)
        assertEquals(plantAiInfo.care?.light, aiState.data.care?.light)
    }

    @Test
    fun `loadPlantDetail fetches from network when cached aiInfo JSON is malformed`() = runTest {
        val candidate = makeCandidate(aiInfo = "{ not valid json }")
        coEvery { scanRepository.observeById("scan-1") } returns flowOf(
            makeScanResult(
                listOf(
                    candidate
                )
            )
        )
        coEvery { plantService.requestAdditionalInfo(any(), any()) } returns plantAiInfo

        viewModel.loadPlantDetail("scan-1", 0)
        advanceUntilIdle()

        coVerify(exactly = 1) { plantService.requestAdditionalInfo(any(), any()) }
        assertTrue(viewModel.aiInfoState.value is UiState.Success)
    }

    @Test
    fun `loadPlantDetail emits Error when scan is not found`() = runTest {
        coEvery { scanRepository.observeById("missing-id") } returns flowOf(null)

        viewModel.loadPlantDetail("missing-id", 0)
        advanceUntilIdle()

        val state = viewModel.candidateState.value as UiState.Error
        assertEquals("Plant details not found", state.message)
        assertTrue(viewModel.aiInfoState.value is UiState.Idle)
    }

    @Test
    fun `loadPlantDetail emits Error when candidateIndex is out of bounds`() = runTest {
        coEvery { scanRepository.observeById("scan-1") } returns flowOf(makeScanResult())

        viewModel.loadPlantDetail("scan-1", 5)
        advanceUntilIdle()

        val state = viewModel.candidateState.value as UiState.Error
        assertEquals("Candidate not found", state.message)
        assertTrue(viewModel.aiInfoState.value is UiState.Idle)
    }

    @Test
    fun `loadPlantDetail reacts to new scan emissions from repository`() = runTest {
        val flow = MutableStateFlow(makeScanResult())
        coEvery { scanRepository.observeById("scan-1") } returns flow
        coEvery { plantService.requestAdditionalInfo(any(), any()) } returns plantAiInfo

        viewModel.loadPlantDetail("scan-1", 0)
        advanceUntilIdle()

        val first = viewModel.candidateState.value as UiState.Success
        assertEquals("Monstera deliciosa", first.data.scientificName)
    }

    @Test
    fun `loadPlantDetail resets retry count and canRetry on each fresh call`() = runTest {
        coEvery { scanRepository.observeById("scan-1") } returns flowOf(makeScanResult())
        coEvery { plantService.requestAdditionalInfo(any(), any()) } throws RuntimeException("fail")

        viewModel.loadPlantDetail("scan-1", 0)
        advanceUntilIdle()
        viewModel.retryAiInfo()
        advanceUntilIdle()

        coEvery { plantService.requestAdditionalInfo(any(), any()) } returns plantAiInfo
        viewModel.loadPlantDetail("scan-1", 0)
        advanceUntilIdle()

        assertTrue(viewModel.canRetry.value)
        assertTrue(viewModel.aiInfoState.value is UiState.Success)
    }

    @Test
    fun `first AI failure emits error with 2 retries left`() = runTest {
        coEvery { scanRepository.observeById("scan-1") } returns flowOf(makeScanResult())
        coEvery { plantService.requestAdditionalInfo(any(), any()) } throws RuntimeException("fail")

        viewModel.loadPlantDetail("scan-1", 0)
        advanceUntilIdle()

        val state = viewModel.aiInfoState.value as UiState.Error
        assertEquals("Couldn't load plant info (2 retries left)", state.message)
        assertTrue(viewModel.canRetry.value)
    }

    @Test
    fun `second AI failure emits error with 1 retry left`() = runTest {
        coEvery { scanRepository.observeById("scan-1") } returns flowOf(makeScanResult())
        coEvery { plantService.requestAdditionalInfo(any(), any()) } throws RuntimeException("fail")

        viewModel.loadPlantDetail("scan-1", 0)
        advanceUntilIdle()
        viewModel.retryAiInfo()
        advanceUntilIdle()

        val state = viewModel.aiInfoState.value as UiState.Error
        assertEquals("Couldn't load plant info (1 retry left)", state.message)
        assertTrue(viewModel.canRetry.value)
    }

    @Test
    fun `third AI failure emits final error message and disables retry`() = runTest {
        coEvery { scanRepository.observeById("scan-1") } returns flowOf(makeScanResult())
        coEvery { plantService.requestAdditionalInfo(any(), any()) } throws RuntimeException("fail")

        viewModel.loadPlantDetail("scan-1", 0)
        advanceUntilIdle()
        viewModel.retryAiInfo()
        advanceUntilIdle()
        viewModel.retryAiInfo()
        advanceUntilIdle()

        val state = viewModel.aiInfoState.value as UiState.Error
        assertEquals("Couldn't load plant info. Please try again later.", state.message)
        assertFalse(viewModel.canRetry.value)
    }

    @Test
    fun `retryAiInfo is ignored when canRetry is false`() = runTest {
        coEvery { scanRepository.observeById("scan-1") } returns flowOf(makeScanResult())
        coEvery { plantService.requestAdditionalInfo(any(), any()) } throws RuntimeException("fail")

        viewModel.loadPlantDetail("scan-1", 0)
        advanceUntilIdle()
        viewModel.retryAiInfo()
        advanceUntilIdle()
        viewModel.retryAiInfo()
        advanceUntilIdle()

        assertFalse(viewModel.canRetry.value)
        val messageBefore = (viewModel.aiInfoState.value as UiState.Error).message

        viewModel.retryAiInfo()
        advanceUntilIdle()

        coVerify(exactly = 3) { plantService.requestAdditionalInfo(any(), any()) }
        assertEquals(messageBefore, (viewModel.aiInfoState.value as UiState.Error).message)
    }

    @Test
    fun `retryAiInfo is ignored before loadPlantDetail is called`() = runTest {
        viewModel.retryAiInfo()
        advanceUntilIdle()

        coVerify(exactly = 0) { plantService.requestAdditionalInfo(any(), any()) }
        assertTrue(viewModel.aiInfoState.value is UiState.Idle)
    }

    @Test
    fun `successful retry resets canRetry to true and emits Success`() = runTest {
        coEvery { scanRepository.observeById("scan-1") } returns flowOf(makeScanResult())
        coEvery { plantService.requestAdditionalInfo(any(), any()) } throws RuntimeException("fail")

        viewModel.loadPlantDetail("scan-1", 0)
        advanceUntilIdle()

        coEvery { plantService.requestAdditionalInfo(any(), any()) } returns plantAiInfo
        viewModel.retryAiInfo()
        advanceUntilIdle()

        assertTrue(viewModel.aiInfoState.value is UiState.Success)
        assertTrue(viewModel.canRetry.value)
    }
}