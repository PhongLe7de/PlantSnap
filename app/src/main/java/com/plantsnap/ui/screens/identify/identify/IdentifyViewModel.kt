package com.plantsnap.ui.screens.identify.identify

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.ScanResult
import com.plantsnap.domain.repository.SavedPlantRepository
import com.plantsnap.domain.services.PlantService
import com.plantsnap.ui.screens.identify.camera.CapturedPhotosHolder
import com.plantsnap.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class IdentifyViewModel @Inject constructor(
    private val plantService: PlantService,
    private val photosHolder: CapturedPhotosHolder,
    private val savedPlantRepo: SavedPlantRepository,
) : ViewModel() {

    private companion object {
        const val TAG = "IdentifyViewModel"
    }

    val photos: StateFlow<List<Uri>> = photosHolder.photos
    val organByPhoto: StateFlow<Map<Uri, String>> = photosHolder.organByPhoto

    private val _uiState = MutableStateFlow<UiState<ScanResult>>(UiState.Idle)
    val uiState: StateFlow<UiState<ScanResult>> = _uiState.asStateFlow()

    /** Set of scientific names currently saved for the active scan. Reactive. */
    val savedNames: StateFlow<Set<String>> = _uiState
        .flatMapLatest { state ->
            val scanId = (state as? UiState.Success)?.data?.id
            if (scanId == null) flowOf(emptySet())
            else savedPlantRepo.observeAll().map { plants ->
                plants.filter { it.sourceScanId == scanId }
                    .map { it.plant.scientificName }
                    .toSet()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggleSaved(candidate: Candidate) {
        val scanId = (_uiState.value as? UiState.Success)?.data?.id ?: return
        viewModelScope.launch {
            val existing = savedPlantRepo.findExisting(scanId, candidate.scientificName)
            if (existing != null) {
                savedPlantRepo.unsave(existing.id)
            } else {
                savedPlantRepo.save(candidate, scanId)
            }
        }
    }

    fun startIdentification() {
        val uris = photosHolder.photos.value
        val organMap = photosHolder.organByPhoto.value

        if (uris.isEmpty()) {
            Log.w(TAG, "No images provided, aborting identification")
            _uiState.value = UiState.Error("No images provided")
            return
        }

        val pairs = uris.mapNotNull { uri ->
            uri.path?.let { path -> File(path) to (organMap[uri] ?: "auto") }
        }

        if (pairs.isEmpty()) {
            Log.w(TAG, "No valid file paths from URIs, aborting")
            _uiState.value = UiState.Error("No valid images found")
            return
        }

        val files = pairs.map { it.first }
        val organs = pairs.map { it.second }

        Log.d(TAG, "Calling identifyPlant with ${files.size} files, organs=$organs")
        identifyPlant(files, organs)
    }

    private fun identifyPlant(imagePaths: List<File>, organs: List<String>) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            Log.d(TAG, "identifyPlant: sending ${imagePaths.size} images to PlantNet API...")
            try {
                val results = plantService.identifyPlantAndSaveToLocal(imagePaths, organs)
                Log.d(TAG, "identifyPlant: SUCCESS — bestMatch=${results.bestMatch}, candidates=${results.candidates.size}")
                results.candidates.forEachIndexed { i, c ->
                    Log.d(TAG, "  candidate[$i]: ${c.scientificName} (${c.score * 100}%) family=${c.family}")
                }
                _uiState.value = UiState.Success(results)
                photosHolder.clear()
            } catch (e: HttpException) {
                Log.e(TAG, "identifyPlant: HTTP ${e.code()}", e)
                val message = when (e.code()) {
                    404 -> "No plant species found. Try a clearer photo of a leaf or flower."
                    401 -> "Invalid API key. Please check your PlantNet configuration."
                    429 -> "Too many requests. Please try again later."
                    else -> "Server error (${e.code()}). Please try again."
                }
                _uiState.value = UiState.Error(message, e)
            } catch (e: Exception) {
                Log.e(TAG, "identifyPlant: FAILED", e)
                _uiState.value = UiState.Error("Failed to identify plant. Check your connection.", e)
            }
        }
    }

}