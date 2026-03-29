package com.plantsnap.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsnap.domain.models.ScanResult
import com.plantsnap.domain.services.PlantService
import com.plantsnap.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val plantService: PlantService
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<ScanResult>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<ScanResult>>> = _uiState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            plantService.getPlantsFromLocal().collect { scans ->
                _uiState.value = UiState.Success(scans)
            }
        }
    }
}
