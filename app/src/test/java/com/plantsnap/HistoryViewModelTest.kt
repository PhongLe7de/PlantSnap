package com.plantsnap

import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.ScanResult
import com.plantsnap.domain.services.PlantService
import com.plantsnap.ui.screens.history.HistoryViewModel
import com.plantsnap.ui.state.UiState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var plantService: PlantService
    private lateinit var viewModel: HistoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        plantService = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() {
        every { plantService.getPlantsFromLocal() } returns flowOf(emptyList())
        viewModel = HistoryViewModel(plantService)
        assertTrue(viewModel.uiState.value is UiState.Loading)
    }

    @Test
    fun `empty scan list emits Success with empty list`() = runTest {
        every { plantService.getPlantsFromLocal() } returns flowOf(emptyList())
        viewModel = HistoryViewModel(plantService)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertTrue((state as UiState.Success).data.isEmpty())
    }

    @Test
    fun `scan list emits Success with correct scans`() = runTest {
        val scans = listOf(fakeScan("id-1", "Monstera deliciosa"), fakeScan("id-2","Rosa canina"))
        every { plantService.getPlantsFromLocal() } returns flowOf(scans)
        viewModel = HistoryViewModel(plantService)

        advanceUntilIdle()

        val state = viewModel.uiState.value as UiState.Success
        assertEquals(2, state.data.size)
        assertEquals("Monstera deliciosa", state.data[0].bestMatch)
        assertEquals("Rosa canina", state.data[1].bestMatch)
    }

    @Test
    fun `new scan added to flow updates state automatically`() = runTest {
        val flow = MutableStateFlow<List<ScanResult>>(emptyList())
        every { plantService.getPlantsFromLocal() } returns flow
        viewModel = HistoryViewModel(plantService)

        advanceUntilIdle()
        assertEquals(0, (viewModel.uiState.value as UiState.Success).data.size)

        flow.value = listOf(fakeScan("id-1", "Monstera deliciosa"))
        advanceUntilIdle()

        assertEquals(1, (viewModel.uiState.value as UiState.Success).data.size)
        assertEquals("Monstera deliciosa", (viewModel.uiState.value as UiState.Success).data[0].bestMatch)
    }

    @Test
    fun `exception from service emits Error state`() = runTest {
        every { plantService.getPlantsFromLocal() } throws RuntimeException("DB error")
        viewModel = HistoryViewModel(plantService)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertEquals("Failed to load history", (state as UiState.Error).message)
    }

    private fun fakeScan(id: String, bestMatch: String) = ScanResult(
        id = id,
        imagePath = "",
        organ = "leaf",
        bestMatch = bestMatch,
        candidates = listOf(
            Candidate(
                scientificName = bestMatch,
                commonNames = listOf("Common Name"),
                family = "Araceae",
                score = 0.9f,
                iucnCategory = null,
            )
        ),
        timestamp = System.currentTimeMillis(),
    )
}