package com.plantsnap.ui.screens.identify.detail

import androidx.lifecycle.ViewModel
import com.plantsnap.domain.models.ScanResult
import com.plantsnap.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PlantDetailViewModel @Inject constructor(

) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<ScanResult>>(UiState.Idle)
    val uiState: StateFlow<UiState<ScanResult>> = _uiState.asStateFlow()

    fun loadPlantDetail(plantId: String) {
        //TODO: Implement logic to load plant details based on plantId, e.g. from a repository or service
    }
}
