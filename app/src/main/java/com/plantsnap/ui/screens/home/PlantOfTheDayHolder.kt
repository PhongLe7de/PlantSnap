package com.plantsnap.ui.screens.home

import com.plantsnap.domain.models.PlantOfTheDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlantOfTheDayHolder @Inject constructor() {
    private val _current = MutableStateFlow<PlantOfTheDay?>(null)
    val current: StateFlow<PlantOfTheDay?> = _current.asStateFlow()

    fun set(plant: PlantOfTheDay) {
        _current.value = plant
    }
}
