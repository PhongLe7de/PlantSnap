package com.plantsnap.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsnap.domain.models.ScanResult
import com.plantsnap.domain.services.PlantService
import com.plantsnap.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val plantService: PlantService
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<ScanResult>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<ScanResult>>> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            try {
                plantService.getPlantsFromLocal().collect { scans ->
                    _uiState.value = UiState.Success(scans)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load history", e)
            }
        }
    }

}