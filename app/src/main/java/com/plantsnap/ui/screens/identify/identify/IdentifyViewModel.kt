package com.plantsnap.ui.screens.identify.identify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsnap.domain.models.ScanResult
import com.plantsnap.domain.services.PlantService
import com.plantsnap.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

@HiltViewModel
class IdentifyViewModel @Inject constructor(
    private val plantService: PlantService
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ScanResult>>(UiState.Idle)
    val uiState: StateFlow<UiState<ScanResult>> = _uiState.asStateFlow()

    fun identifyPlant(imagePaths: List<File>, organs: List<String>) {
        if (imagePaths.isEmpty()) {
            _uiState.value = UiState.Error("No images provided")
            return
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val results = plantService.identifyPlantAndSaveToLocal(imagePaths, organs)
                _uiState.value = UiState.Success(results)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to identify plant", e)
            }
        }
    }

}