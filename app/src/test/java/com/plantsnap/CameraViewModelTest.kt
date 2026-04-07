package com.plantsnap

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.view.LifecycleCameraController
import com.plantsnap.ui.screens.identify.camera.CapturedPhotosHolder
import com.plantsnap.ui.screens.identify.camera.CameraViewModel
import com.plantsnap.ui.state.UiState
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CameraViewModelTest {

    private lateinit var context: Context
    private lateinit var photosHolder: CapturedPhotosHolder
    private lateinit var viewModel: CameraViewModel

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        photosHolder = CapturedPhotosHolder()
        viewModel = CameraViewModel(context, photosHolder)
    }

    @Test
    fun `initial flash state is off`() {
        assertFalse(viewModel.screenState.value.flashEnabled)
    }

    @Test
    fun `toggleFlash enables flash`() {
        val controller = mockk<LifecycleCameraController>(relaxed = true)
        viewModel.toggleFlash(controller)
        assertTrue(viewModel.screenState.value.flashEnabled)
    }

    @Test
    fun `toggleFlash twice restores flash to off`() {
        val controller = mockk<LifecycleCameraController>(relaxed = true)
        viewModel.toggleFlash(controller)
        viewModel.toggleFlash(controller)
        assertFalse(viewModel.screenState.value.flashEnabled)
    }

    @Test
    fun `toggleFlash on sets controller flash mode to on`() {
        val controller = mockk<LifecycleCameraController>(relaxed = true)
        viewModel.toggleFlash(controller)
        verify { controller.imageCaptureFlashMode = ImageCapture.FLASH_MODE_ON }
    }

    @Test
    fun `toggleFlash off sets controller flash mode to off`() {
        val controller = mockk<LifecycleCameraController>(relaxed = true)
        viewModel.toggleFlash(controller)
        viewModel.toggleFlash(controller)
        verify { controller.imageCaptureFlashMode = ImageCapture.FLASH_MODE_OFF }
    }

    @Test
    fun `submitForIdentification does nothing when holder is empty`() {
        viewModel.submitForIdentification()
        assertEquals(UiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `submitForIdentification emits success with photos from holder`() {
        val uri = mockk<Uri>()
        photosHolder.addPhoto(uri)

        viewModel.submitForIdentification()

        assertEquals(UiState.Success(listOf(uri)), viewModel.uiState.value)
    }

    @Test
    fun `submitForIdentification includes all photos in holder`() {
        val uris = List(3) { mockk<Uri>() }
        uris.forEach { photosHolder.addPhoto(it) }

        viewModel.submitForIdentification()

        assertEquals(UiState.Success(uris), viewModel.uiState.value)
    }

    @Test
    fun `clearError resets uiState to Idle`() {
        photosHolder.addPhoto(mockk())
        viewModel.submitForIdentification()
        check(viewModel.uiState.value is UiState.Success)

        viewModel.clearError()

        assertEquals(UiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `clearError is idempotent when already idle`() {
        viewModel.clearError()
        assertEquals(UiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `addPhotosFromGallery with empty list leaves state idle`() {
        viewModel.addPhotosFromGallery(emptyList())
        assertEquals(UiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `addPhotosFromGallery when holder is full leaves state idle`() {
        repeat(5) { photosHolder.addPhoto(mockk()) }
        viewModel.addPhotosFromGallery(listOf(mockk()))
        assertEquals(UiState.Idle, viewModel.uiState.value)
    }
}
