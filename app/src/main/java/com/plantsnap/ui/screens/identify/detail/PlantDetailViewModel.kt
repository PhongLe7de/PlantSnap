package com.plantsnap.ui.screens.identify.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.repository.ScanRepository
import com.plantsnap.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlantDetailViewModel @Inject constructor(
    private val scanRepository: ScanRepository
) : ViewModel() {

    private companion object {
        const val TAG = "PlantDetailViewModel"
    }

    private val _uiState = MutableStateFlow<UiState<Candidate>>(UiState.Idle)
    val uiState: StateFlow<UiState<Candidate>> = _uiState.asStateFlow()

    fun loadPlantDetail(plantId: String, candidateIndex: Int) {
        _uiState.value = UiState.Loading
        Log.d(TAG, "loadPlantDetail: scanId=$plantId, candidateIndex=$candidateIndex")
        viewModelScope.launch {
            scanRepository.observeById(plantId).collect { scanResult ->
                if (scanResult != null) {
                    val candidate = scanResult.candidates.getOrNull(candidateIndex)
                    if (candidate != null) {
                        Log.d(TAG, "loadPlantDetail: ${candidate.scientificName} (${candidate.score * 100}%) commonNames=${candidate.commonNames} family=${candidate.family} iucn=${candidate.iucnCategory}")
                        _uiState.value = UiState.Success(candidate)
                    } else {
                        Log.w(TAG, "loadPlantDetail: candidate index $candidateIndex out of bounds (${scanResult.candidates.size} candidates)")
                        _uiState.value = UiState.Error("Candidate not found")
                    }
                } else {
                    Log.w(TAG, "loadPlantDetail: scan not found for id=$plantId")
                    _uiState.value = UiState.Error("Plant details not found")
                }
            }
        }
    }
}
