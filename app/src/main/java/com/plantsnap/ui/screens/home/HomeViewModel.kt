package com.plantsnap.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.services.PlantService
import com.plantsnap.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val plantService: PlantService
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Candidate>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<Candidate>>> = _uiState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val plants = plantService.getPlantsFromLocal()
            _uiState.value = UiState.Success(plants)
        }
    }
}