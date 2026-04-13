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
        val isOnboardingComplete: Boolean = false, // TODO: reduntant when user data persistence is implemented
        val selectedPets: String? = null,
        val selectedInterests: Set<String> = emptySet(),
        val selectedExperience: String? = null
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun selectPets(option: String) {
        _state.update { it.copy(selectedPets = if (it.selectedPets == option) null else option) }
    }

    fun toggleInterest(interest: String) {
        _state.update {
            val updated = if (interest in it.selectedInterests) {
                it.selectedInterests - interest
            } else {
                it.selectedInterests + interest
            }
            it.copy(selectedInterests = updated)
        }
    }

    fun selectExperience(level: String) {
        _state.update { it.copy(selectedExperience = if (it.selectedExperience == level) null else level) }
    }

    fun completeOnboarding() {
        // supabase repo will be called here
        _state.update { it.copy(isOnboardingComplete = true) }
    }
}
