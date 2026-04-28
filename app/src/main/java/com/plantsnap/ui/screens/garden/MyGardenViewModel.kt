package com.plantsnap.ui.screens.garden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsnap.domain.models.SavedPlant
import com.plantsnap.domain.repository.SavedPlantRepository
import com.plantsnap.ui.screens.identify.camera.CapturedPhotosHolder
import com.plantsnap.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MyGardenViewModel @Inject constructor(
    repo: SavedPlantRepository,
    private val photosHolder: CapturedPhotosHolder,
) : ViewModel() {
    val plants: StateFlow<UiState<List<SavedPlant>>> =
        repo.observeAll()
            .map<List<SavedPlant>, UiState<List<SavedPlant>>> { UiState.Success(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun resetIdentifyFlow() {
        photosHolder.clear()
    }
}
