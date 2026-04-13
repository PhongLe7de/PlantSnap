package com.plantsnap.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {

    data class State(
        val isOnboardingComplete: Boolean = false, // TODO: redundant when user data persistence is implemented
        val selectedPets: PetType? = null,
        val selectedInterests: Set<PlantInterest> = emptySet(),
        val selectedExperience: ExperienceLevel? = null
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun selectPets(option: PetType) {
        _state.update { it.copy(selectedPets = if (it.selectedPets == option) null else option) }
    }

    fun toggleInterest(interest: PlantInterest) {
        _state.update {
            val updated = if (interest in it.selectedInterests) {
                it.selectedInterests - interest
            } else {
                it.selectedInterests + interest
            }
            it.copy(selectedInterests = updated)
        }
    }

    fun selectExperience(level: ExperienceLevel) {
        _state.update { it.copy(selectedExperience = if (it.selectedExperience == level) null else level) }
    }

    fun completeOnboarding() {
        // TODO: supabase repo will be called here once user data persistence is implemented
        _state.update { it.copy(isOnboardingComplete = true) }
    }
}
