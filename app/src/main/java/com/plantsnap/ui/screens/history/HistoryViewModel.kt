package com.plantsnap.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    fun getHistory() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // TODO: Implement actual history retrieval logic here, e.g. plantService.getHistory()
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load history", e)
            }
        }
    }
}