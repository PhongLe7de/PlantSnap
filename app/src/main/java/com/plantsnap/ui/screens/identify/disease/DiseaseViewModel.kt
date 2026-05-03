package com.plantsnap.ui.screens.identify.disease

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsnap.data.DiseaseScanResultHolder
import com.plantsnap.domain.models.DiseaseScanResult
import com.plantsnap.domain.repository.PlantNetRepository
import com.plantsnap.ui.screens.identify.camera.CapturedPhotosHolder
import com.plantsnap.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DiseaseViewModel @Inject constructor(
    private val plantNetRepo: PlantNetRepository,
    private val photosHolder: CapturedPhotosHolder,
    private val scanResultHolder: DiseaseScanResultHolder,
) : ViewModel() {

    private companion object {
        const val TAG = "DiseaseViewModel"
    }

    val photos: StateFlow<List<Uri>> = photosHolder.photos

    private val _uiState = MutableStateFlow<UiState<DiseaseScanResult>>(UiState.Idle)
    val uiState: StateFlow<UiState<DiseaseScanResult>> = _uiState.asStateFlow()

    fun startDiseaseIdentification() {
        val uris = photosHolder.photos.value
        val organMap = photosHolder.organByPhoto.value

        if (uris.isEmpty()) {
            _uiState.value = UiState.Error("No images provided")
            return
        }

        val pairs = uris.mapNotNull { uri ->
            uri.path?.let { path -> File(path) to (organMap[uri] ?: "auto") }
        }

        if (pairs.isEmpty()) {
            _uiState.value = UiState.Error("No valid images found")
            return
        }

        val imagePath = pairs.first().first.absolutePath

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            Log.d(TAG, "startDiseaseIdentification: sending ${pairs.size} images to disease API…")
            try {
                val result = plantNetRepo.identifyDisease(
                    imageFiles = pairs.map { it.first },
                    organs = pairs.map { it.second },
                    imagePath = imagePath
                )
                Log.d(
                    TAG,
                    "startDiseaseIdentification: SUCCESS — ${result.candidates.size} diseases"
                )
                scanResultHolder.result = result
                _uiState.value = UiState.Success(result)
                photosHolder.clear()
            } catch (e: HttpException) {
                Log.e(TAG, "startDiseaseIdentification: HTTP ${e.code()}", e)
                val message = when (e.code()) {
                    404 -> "No diseases found. Try a clearer photo of affected leaves or stems."
                    401 -> "Invalid API key. Please check your PlantNet configuration."
                    429 -> "Too many requests. Please try again later."
                    else -> "Server error (${e.code()}). Please try again."
                }
                _uiState.value = UiState.Error(message, e)
            } catch (e: Exception) {
                Log.e(TAG, "startDiseaseIdentification: FAILED", e)
                _uiState.value =
                    UiState.Error("Failed to identify disease. Check your connection.", e)
            }
        }
    }
}
