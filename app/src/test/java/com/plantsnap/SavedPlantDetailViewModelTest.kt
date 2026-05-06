package com.plantsnap

import android.util.Log
import com.plantsnap.data.storage.PlantImageUrlResolver
import com.plantsnap.data.sync.SavedPlantSyncManager
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.PlantAiInfo
import com.plantsnap.domain.models.SavedPlant
import com.plantsnap.domain.repository.CareTaskRepository
import com.plantsnap.domain.repository.ProfileRepository
import com.plantsnap.domain.repository.SavedPlantRepository
import com.plantsnap.domain.services.PlantService
import com.plantsnap.ui.screens.garden.detail.SavedPlantDetailViewModel
import com.plantsnap.ui.state.UiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SavedPlantDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val savedPlantRepo: SavedPlantRepository = mockk(relaxed = true)
    private val plantService: PlantService = mockk()
    private val profileRepository: ProfileRepository = mockk()
    private val syncManager: SavedPlantSyncManager = mockk(relaxed = true)
    private val imageUrlResolver: PlantImageUrlResolver = mockk {
        coEvery { resolve(any()) } returns null
    }
    private val careTaskRepository: CareTaskRepository = mockk(relaxed = true) {
        every { observeForPlant(any()) } returns flowOf(emptyList())
    }

    private lateinit var viewModel: SavedPlantDetailViewModel

    private val aiInfo = PlantAiInfo(description = "A nice plant.")

    private fun makeCandidate() = Candidate(
        scientificName = "Monstera deliciosa",
        commonNames = listOf("Swiss Cheese Plant"),
        family = "Araceae",
        score = 0.97f,
        iucnCategory = null,
        gbifId = 42L,
    )

    private fun makeSavedPlant(
        id: String = "saved-1",
        scanId: String? = "scan-1",
        nickname: String = "Monty",
        isFavourite: Boolean = false,
        lastWateredAt: Long? = null,
    ) = SavedPlant(
        id = id,
        plant = makeCandidate(),
        originalScanId = scanId,
        createdAt = 1_000L,
        nickname = nickname,
        isFavourite = isFavourite,
        lastWateredAt = lastWateredAt,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>(), any()) } returns 0
        coEvery { profileRepository.getProfile() } returns null
        viewModel = SavedPlantDetailViewModel(
            savedPlantRepo = savedPlantRepo,
            plantService = plantService,
            profileRepository = profileRepository,
            savedPlantSyncManager = syncManager,
            imageUrlResolver = imageUrlResolver,
            careTaskRepository = careTaskRepository,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    @Test
    fun `initial state is Idle`() {
        assertTrue(viewModel.candidateState.value is UiState.Idle)
        assertTrue(viewModel.aiInfoState.value is UiState.Idle)
        assertTrue(viewModel.canRetry.value)
    }

    @Test
    fun `loadSavedPlant emits Success when repo returns plant`() = runTest {
        coEvery { savedPlantRepo.observeById("saved-1") } returns flowOf(makeSavedPlant())
        coEvery { plantService.requestAdditionalInfo(any(), any()) } returns aiInfo

        viewModel.loadSavedPlant("saved-1")
        advanceUntilIdle()

        val state = viewModel.candidateState.value as UiState.Success
        assertEquals("Monstera deliciosa", state.data.scientificName)
        assertEquals("Monty", viewModel.displayName.value)
        assertTrue(viewModel.aiInfoState.value is UiState.Success)
    }

    @Test
    fun `loadSavedPlant emits Error when repo returns null`() = runTest {
        coEvery { savedPlantRepo.observeById("missing") } returns flowOf(null)

        viewModel.loadSavedPlant("missing")
        advanceUntilIdle()

        assertTrue(viewModel.candidateState.value is UiState.Error)
    }

    @Test
    fun `loadSavedPlant disables retry when scanId is null`() = runTest {
        coEvery { savedPlantRepo.observeById("saved-1") } returns
            flowOf(makeSavedPlant(scanId = null))

        viewModel.loadSavedPlant("saved-1")
        advanceUntilIdle()

        assertTrue(viewModel.aiInfoState.value is UiState.Error)
        assertFalse(viewModel.canRetry.value)
    }

    @Test
    fun `updateNickname trims input and skips empty`() = runTest {
        coEvery { savedPlantRepo.observeById("saved-1") } returns flowOf(makeSavedPlant())
        coEvery { plantService.requestAdditionalInfo(any(), any()) } returns aiInfo
        viewModel.loadSavedPlant("saved-1")
        advanceUntilIdle()

        viewModel.updateNickname("   ")
        advanceUntilIdle()

        coVerify(exactly = 0) { savedPlantRepo.updateNickname(any(), any()) }
    }

    @Test
    fun `updateNickname calls repo with trimmed value and triggers sync`() = runTest {
        coEvery { savedPlantRepo.observeById("saved-1") } returns flowOf(makeSavedPlant())
        coEvery { plantService.requestAdditionalInfo(any(), any()) } returns aiInfo
        viewModel.loadSavedPlant("saved-1")
        advanceUntilIdle()

        viewModel.updateNickname("  Figgy  ")
        advanceUntilIdle()

        coVerify(exactly = 1) { savedPlantRepo.updateNickname("saved-1", "Figgy") }
        coVerify(atLeast = 1) { syncManager.syncPending() }
    }

    @Test
    fun `toggleFavourite flips state via repo`() = runTest {
        coEvery { savedPlantRepo.observeById("saved-1") } returns
            flowOf(makeSavedPlant(isFavourite = false))
        coEvery { plantService.requestAdditionalInfo(any(), any()) } returns aiInfo
        viewModel.loadSavedPlant("saved-1")
        advanceUntilIdle()

        viewModel.toggleFavourite()
        advanceUntilIdle()

        coVerify(exactly = 1) { savedPlantRepo.updateFavourite("saved-1", true) }
    }

    @Test
    fun `markWatered calls repo with non-null timestamp`() = runTest {
        coEvery { savedPlantRepo.observeById("saved-1") } returns flowOf(makeSavedPlant())
        coEvery { plantService.requestAdditionalInfo(any(), any()) } returns aiInfo
        viewModel.loadSavedPlant("saved-1")
        advanceUntilIdle()

        viewModel.markWatered()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            savedPlantRepo.updateLastWatered("saved-1", match { it > 0L })
        }
    }

    @Test
    fun `archive calls unsave and triggers sync`() = runTest {
        coEvery { savedPlantRepo.observeById("saved-1") } returns flowOf(makeSavedPlant())
        coEvery { plantService.requestAdditionalInfo(any(), any()) } returns aiInfo
        viewModel.loadSavedPlant("saved-1")
        advanceUntilIdle()

        viewModel.archive()
        advanceUntilIdle()

        coVerify(exactly = 1) { savedPlantRepo.unsave("saved-1") }
        coVerify(atLeast = 1) { syncManager.syncPending() }
    }
}
