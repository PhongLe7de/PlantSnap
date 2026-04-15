package com.plantsnap.ui.screens.identify.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.PlantAiInfo
import com.plantsnap.domain.repository.ScanRepository
import com.plantsnap.domain.services.PlantService
import com.plantsnap.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class PlantDetailViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val plantService: PlantService,
    private val json: Json,
) : ViewModel() {

    private companion object {
        const val TAG = "PlantDetailViewModel"
    }

    private val _candidateState = MutableStateFlow<UiState<Candidate>>(UiState.Idle)
    val candidateState: StateFlow<UiState<Candidate>> = _candidateState.asStateFlow()

    private val _aiInfoState = MutableStateFlow<UiState<PlantAiInfo>>(UiState.Idle)
    val aiInfoState: StateFlow<UiState<PlantAiInfo>> = _aiInfoState.asStateFlow()

    private var lastScanId: String? = null
    private var lastScientificName: String? = null

    fun loadPlantDetail(plantId: String, candidateIndex: Int) {
        _candidateState.value = UiState.Loading
        _aiInfoState.value = UiState.Idle
        Log.d(TAG, "loadPlantDetail: scanId=$plantId, candidateIndex=$candidateIndex")
        viewModelScope.launch {
            val scanResult = scanRepository.observeById(plantId).firstOrNull()
            if (scanResult == null) {
                Log.w(TAG, "loadPlantDetail: scan not found for id=$plantId")
                _candidateState.value = UiState.Error("Plant details not found")
                return@launch
            }
            val candidate = scanResult.candidates.getOrNull(candidateIndex)
            if (candidate == null) {
                Log.w(
                    TAG,
                    "loadPlantDetail: candidate index $candidateIndex out of bounds (${scanResult.candidates.size} candidates)"
                )
                _candidateState.value = UiState.Error("Candidate not found")
                return@launch
            }

            Log.d(TAG, "loadPlantDetail: ${candidate.scientificName} (${candidate.score * 100}%)")
            _candidateState.value = UiState.Success(candidate)
            lastScanId = plantId
            lastScientificName = candidate.scientificName

            val cached = candidate.aiInfo?.let { runCatching { json.decodeFromString(PlantAiInfo.serializer(), it) }.getOrNull() }
            if (cached != null) {
                Log.d(TAG, "aiInfo cache hit for ${candidate.scientificName}")
                _aiInfoState.value = UiState.Success(cached)
            } else {
                fetchAiInfo(plantId, candidate.scientificName)
            }
        }
    }

    fun retryAiInfo() {
        val scanId = lastScanId ?: return
        val name = lastScientificName ?: return
        fetchAiInfo(scanId, name)
    }

    private fun fetchAiInfo(scanId: String, scientificName: String) {
        _aiInfoState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val info = plantService.requestAdditionalInfo(scanId, scientificName)
                _aiInfoState.value = UiState.Success(info)
            } catch (e: Exception) {
                Log.w(TAG, "gemini fetch failed for $scientificName", e)
                _aiInfoState.value = UiState.Error("Couldn't load care info")
            }
        }
    }
}
