package com.plantsnap.ui.screens.home

import androidx.lifecycle.ViewModel
import com.plantsnap.domain.models.PlantOfTheDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PlantOfTheDayDetailViewModel @Inject constructor(
    holder: PlantOfTheDayHolder,
) : ViewModel() {
    val plant: StateFlow<PlantOfTheDay?> = holder.current
}
