package com.plantsnap.ui.screens.garden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsnap.data.storage.PlantImageUrlResolver
import com.plantsnap.domain.models.SavedPlant
import com.plantsnap.domain.repository.SavedPlantRepository
import com.plantsnap.ui.screens.identify.camera.CapturedPhotosHolder
import com.plantsnap.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class SavedPlantUi(
    val plant: SavedPlant,
    val displayImageUrl: String?,
)

@HiltViewModel
class MyGardenViewModel @Inject constructor(
    repo: SavedPlantRepository,
    private val imageUrlResolver: PlantImageUrlResolver,
    private val photosHolder: CapturedPhotosHolder,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val plants: StateFlow<UiState<List<SavedPlantUi>>> =
        repo.observeAll()
            .flatMapLatest { saved ->
                flow {
                    val resolved = imageUrlResolver.resolveAll(saved.map { it.plant.imageUrl })
                    emit(UiState.Success(saved.map { sp ->
                        SavedPlantUi(
                            plant = sp,
                            displayImageUrl = sp.plant.imageUrl?.let { resolved[it] },
                        )
                    }))
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun resetIdentifyFlow() {
        photosHolder.clear()
    }
}
