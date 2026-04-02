package com.plantsnap.ui.screens.identify.camera

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.plantsnap.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject

data class CameraScreenUiState(
    val flashEnabled: Boolean = false,
    val picturesTaken: Int = 0 // Max 5
)

@HiltViewModel
class CameraViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _screenState = MutableStateFlow(CameraScreenUiState())
    val screenState: StateFlow<CameraScreenUiState> = _screenState.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<Uri>>(UiState.Idle)
    val uiState: StateFlow<UiState<Uri>> = _uiState.asStateFlow()

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
                    _uiState.value = UiState.Success(uri)
                }
                override fun onError(exception: ImageCaptureException) {
                    _uiState.value = UiState.Error(exception.message ?: "Capture failed")
                }
            }
        )
    }

    fun toggleFlash(controller: LifecycleCameraController) {
        val newFlash = !_screenState.value.flashEnabled

        Log.i("CameraViewModel", "toggleFlash: $newFlash")
        _screenState.value = _screenState.value.copy(flashEnabled = newFlash)
        // Apply to camera
        controller.imageCaptureFlashMode =
            if (newFlash) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
    }


    fun setImageUri(uri: Uri) {
        _uiState.value = UiState.Success(uri)
    }
}