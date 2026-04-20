package com.plantsnap.ui.screens.home

import android.util.Log
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
    private val plantService: PlantService,
    private val plantOfTheDayHolder: PlantOfTheDayHolder,
) : ViewModel() {

    private companion object {
        const val TAG = "HomeViewModel"
    }

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
        if (_plantOfTheDayState.value is UiState.Success) return
        viewModelScope.launch {
            _plantOfTheDayState.value = UiState.Loading
            try {
                val plantOfTheDay = plantService.getPlantOfTheDay()
                plantOfTheDayHolder.set(plantOfTheDay)
                _plantOfTheDayState.value = UiState.Success(plantOfTheDay)
            } catch (e: Exception) {
                val code = extractHttpCode(e.message)
                Log.e(TAG, "getPlantOfTheDay: HTTP ${code ?: "?"}", e)
                val message = when (code) {
                    401, 403 -> "Plant of the day is unavailable right now. Please try again later."
                    429 -> "Too many requests. Please try again in a moment."
                    in 500..599 -> "The service is temporarily down. Please try again later."
                    else -> "We couldn't fetch today's plant. Please check your connection and try again."
                }
                _plantOfTheDayState.value = UiState.Error(message)
            }
        }
    }

    /** Pulls a leading 3-digit HTTP status code out of error messages like "403 . Method doesn't allow…". */
    private fun extractHttpCode(message: String?): Int? =
        message?.trim()?.take(3)?.toIntOrNull()?.takeIf { it in 100..599 }
}
