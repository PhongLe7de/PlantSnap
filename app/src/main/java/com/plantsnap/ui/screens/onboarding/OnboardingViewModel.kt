package com.plantsnap.ui.screens.onboarding

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsnap.domain.repository.ProfileRepository
import com.plantsnap.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    data class State(
        val isOnboardingComplete: Boolean = false, // TODO: redundant when user data persistence is implemented
        val selectedPets: PetType? = null,
        val selectedInterests: Set<PlantInterest> = emptySet(),
        val selectedExperience: ExperienceLevel? = null
    )

    private val _uiState = MutableStateFlow<UiState<List<Uri>>>(UiState.Idle)
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
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                profileRepository.updateOnboardingData(
                    petType = state.value.selectedPets,
                    plantInterests = state.value.selectedInterests,
                    experienceLevel = state.value.selectedExperience
                )
                _state.update { it.copy(isOnboardingComplete = true) }
                _uiState.value = UiState.Idle
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to save", e)
            }
        }
    }
}