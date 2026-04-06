package com.plantsnap.ui.screens.identify.camera

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.plantsnap.ui.state.UiState
import com.plantsnap.utils.MAX_PHOTOS
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject

data class CameraScreenUiState(
    val flashEnabled: Boolean = false
)

@HiltViewModel
class CameraViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    val photosHolder: CapturedPhotosHolder
) : ViewModel() {
    private val _screenState = MutableStateFlow(CameraScreenUiState())
    val screenState: StateFlow<CameraScreenUiState> = _screenState.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<List<Uri>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<Uri>>> = _uiState.asStateFlow()


    fun toggleFlash(controller: LifecycleCameraController) {
        val newFlash = !_screenState.value.flashEnabled

        Log.i("CameraViewModel", "toggleFlash: $newFlash")
        _screenState.value = _screenState.value.copy(flashEnabled = newFlash)
        // Apply to camera
        controller.imageCaptureFlashMode =
            if (newFlash) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
    }

    fun capturePhoto(controller: LifecycleCameraController) {
        _uiState.value = UiState.Loading

        val photoFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), // App private dir, no storage permissions required
            "plant_${System.currentTimeMillis()}.jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        controller.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val uri = output.savedUri ?: Uri.fromFile(photoFile)
                    photosHolder.addPhoto(uri)
                    _uiState.value = UiState.Idle
                }

                override fun onError(exception: ImageCaptureException) {
                    _uiState.value = UiState.Error(exception.toUserMessage())
                }
            }
        )
        Log.d("CameraViewModel", "capturePhoto: ${photosHolder.photos.value}")
    }

    fun addPhotosFromGallery(uris: List<Uri>) {
        val remaining = MAX_PHOTOS - photosHolder.count
        if (uris.isEmpty() || remaining <= 0) return
        _uiState.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                uris.take(remaining).forEachIndexed { index, uri ->
                    val destFile = File(
                        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "gallery_${System.currentTimeMillis()}_$index.jpg"
                    )
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Unable to open selected gallery photo")
                    inputStream.use { input ->
                        destFile.outputStream().use { output -> input.copyTo(output) }
                    }
                    photosHolder.addPhoto(Uri.fromFile(destFile))
                }
                _uiState.value = UiState.Idle
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to import gallery photo")
            }
        }
    }

    // List of up to MAX_PHOTOS photos sent to the API - taken photos wiped from local storage after successful identification
    fun submitForIdentification() {
        val photos = photosHolder.photos.value
        if (photos.isEmpty()) return
        _uiState.value = UiState.Success(photos)
    }

    private fun ImageCaptureException.toUserMessage(): String {
        // Camera2 errors
        (cause as? CameraAccessException)?.let {
            return when (it.reason) {
                CameraAccessException.CAMERA_IN_USE -> "Camera is in use by another app"
                CameraAccessException.CAMERA_DISABLED -> "Camera has been disabled"
                CameraAccessException.CAMERA_DISCONNECTED -> "Camera disconnected"
                else -> "Camera hardware error"
            }
        }
        // CameraX errors
        return when (imageCaptureError) {
            ImageCapture.ERROR_CAMERA_CLOSED -> "Camera closed unexpectedly"
            ImageCapture.ERROR_FILE_IO -> "Failed to save — check storage"
            ImageCapture.ERROR_CAPTURE_FAILED -> "Capture failed, try again"
            else -> message ?: "Capture failed"
        }
    }

    // For closing the error dialog
    fun clearError() {
        _uiState.value = UiState.Idle
    }
}
