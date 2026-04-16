package com.plantsnap

import android.net.Uri
import android.util.Log
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.ScanResult
import com.plantsnap.domain.services.PlantService
import com.plantsnap.ui.screens.identify.camera.CapturedPhotosHolder
import com.plantsnap.ui.screens.identify.identify.IdentifyViewModel
import com.plantsnap.ui.state.UiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** Creates a mock Uri with the given path. */
private fun mockUri(path: String): Uri = mockk<Uri> {
    every { this@mockk.path } returns path
}

@OptIn(ExperimentalCoroutinesApi::class)
class IdentifyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var plantService: PlantService
    private lateinit var photosHolder: CapturedPhotosHolder
    private lateinit var viewModel: IdentifyViewModel

    private val testCandidate = Candidate(
        scientificName = "Monstera deliciosa",
        commonNames = listOf("Swiss Cheese Plant"),
        family = "Araceae",
        score = 0.95f,
        iucnCategory = null
    )

    private val testScanResult = ScanResult(
        imagePath = "/test/photo.jpg",
        organ = "leaf",
        bestMatch = "Monstera deliciosa",
        candidates = listOf(testCandidate),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        plantService = mockk()
        photosHolder = CapturedPhotosHolder()
        viewModel = IdentifyViewModel(plantService, photosHolder)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    // region Initial State

    @Test
    fun `initial state is Idle`() {
        assertEquals(UiState.Idle, viewModel.uiState.value)
    }

    // endregion

    // region Validation

    @Test
    fun `empty photos emits error`() {
        viewModel.startIdentification()
        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertEquals("No images provided", (state as UiState.Error).message)
    }

    @Test
    fun `URIs with null paths emits error`() {
        val uri = mockk<Uri>()
        every { uri.path } returns null
        photosHolder.addPhoto(uri)

        viewModel.startIdentification()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertEquals("No valid images found", (state as UiState.Error).message)
    }

    // endregion

    // region Success

    @Test
    fun `valid photos triggers identification and emits Success`() = runTest {
        photosHolder.addPhoto(mockUri("/test/photo.jpg"))
        coEvery { plantService.identifyPlantAndSaveToLocal(any(), any()) } returns testScanResult

        viewModel.startIdentification()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(testScanResult, (state as UiState.Success).data)
    }

    @Test
    fun `organs mapped correctly from photosHolder`() = runTest {
        photosHolder.addPhoto(mockUri("/test/photo.jpg"), "leaf")
        coEvery { plantService.identifyPlantAndSaveToLocal(any(), any()) } returns testScanResult

        viewModel.startIdentification()
        advanceUntilIdle()

        coVerify {
            plantService.identifyPlantAndSaveToLocal(any(), match { it == listOf("leaf") })
        }
    }

    @Test
    fun `missing organ defaults to auto`() = runTest {
        photosHolder.addPhoto(mockUri("/test/photo.jpg"))
        coEvery { plantService.identifyPlantAndSaveToLocal(any(), any()) } returns testScanResult

        viewModel.startIdentification()
        advanceUntilIdle()

        coVerify {
            plantService.identifyPlantAndSaveToLocal(any(), match { it == listOf("auto") })
        }
    }

    // endregion

    // region HTTP Errors

    @Test
    fun `HttpException 404 emits species not found error`() = runTest {
        photosHolder.addPhoto(mockUri("/test/photo.jpg"))
        coEvery { plantService.identifyPlantAndSaveToLocal(any(), any()) } throws
                HttpException(Response.error<Any>(404, "".toResponseBody()))

        viewModel.startIdentification()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertEquals(
            "No plant species found. Try a clearer photo of a leaf or flower.",
            (state as UiState.Error).message
        )
    }

    @Test
    fun `HttpException 401 emits invalid API key error`() = runTest {
        photosHolder.addPhoto(mockUri("/test/photo.jpg"))
        coEvery { plantService.identifyPlantAndSaveToLocal(any(), any()) } throws
                HttpException(Response.error<Any>(401, "".toResponseBody()))

        viewModel.startIdentification()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertEquals(
            "Invalid API key. Please check your PlantNet configuration.",
            (state as UiState.Error).message
        )
    }

    @Test
    fun `HttpException 429 emits rate limit error`() = runTest {
        photosHolder.addPhoto(mockUri("/test/photo.jpg"))
        coEvery { plantService.identifyPlantAndSaveToLocal(any(), any()) } throws
                HttpException(Response.error<Any>(429, "".toResponseBody()))

        viewModel.startIdentification()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertEquals(
            "Too many requests. Please try again later.",
            (state as UiState.Error).message
        )
    }

    // endregion

    // region Generic Error

    @Test
    fun `generic exception emits connection error`() = runTest {
        photosHolder.addPhoto(mockUri("/test/photo.jpg"))
        coEvery { plantService.identifyPlantAndSaveToLocal(any(), any()) } throws
                IOException("Network unreachable")

        viewModel.startIdentification()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertEquals(
            "Failed to identify plant. Check your connection.",
            (state as UiState.Error).message
        )
    }

    // endregion
}
