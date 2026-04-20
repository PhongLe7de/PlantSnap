package com.plantsnap.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsnap.domain.models.PlantOfTheDay
import com.plantsnap.domain.models.ScanResult
import com.plantsnap.domain.services.PlantService
import com.plantsnap.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val MAX_RECENT_SCANS = 2

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val plantService: PlantService
) : ViewModel() {

    private val _scansState = MutableStateFlow<UiState<List<ScanResult>>>(UiState.Idle)
    val scansState: StateFlow<UiState<List<ScanResult>>> = _scansState.asStateFlow()

    private val _plantOfTheDayState = MutableStateFlow<UiState<PlantOfTheDay>>(UiState.Idle)
    val plantOfTheDayState: StateFlow<UiState<PlantOfTheDay>> = _plantOfTheDayState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _scansState.value = UiState.Loading
            plantService.getPlantsFromLocal().collect { scans ->
                _scansState.value= UiState.Success(
                    scans
                        .sortedByDescending { it.timestamp }
                        .take(MAX_RECENT_SCANS)
                )
            }
        }
        loadPlantOfTheDay()
    }

    private fun loadPlantOfTheDay() {
        if(_plantOfTheDayState.value is UiState.Success) return
        viewModelScope.launch {
            _plantOfTheDayState.value = UiState.Loading
            try {
                val plantOfTheDay = plantService.getPlantOfTheDay()
                _plantOfTheDayState.value = UiState.Success(plantOfTheDay)
            } catch (e: Exception) {
                _plantOfTheDayState.value = UiState.Error(e.message ?: "Unknown error")
            }

        }
    }
}
