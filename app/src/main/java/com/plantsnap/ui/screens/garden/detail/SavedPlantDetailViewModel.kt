package com.plantsnap.ui.screens.garden.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsnap.data.storage.PlantImageUrlResolver
import com.plantsnap.data.sync.SavedPlantSyncManager
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.PlantAiInfo
import com.plantsnap.domain.models.SupabaseProfile
import com.plantsnap.domain.repository.ProfileRepository
import com.plantsnap.domain.repository.SavedPlantRepository
import com.plantsnap.domain.safety.SafetyAdvisor
import com.plantsnap.domain.safety.SafetyAlert
import com.plantsnap.domain.services.PlantService
import com.plantsnap.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedPlantDetailViewModel @Inject constructor(
    private val savedPlantRepo: SavedPlantRepository,
    private val plantService: PlantService,
    private val profileRepository: ProfileRepository,
    private val savedPlantSyncManager: SavedPlantSyncManager,
    private val imageUrlResolver: PlantImageUrlResolver,
) : ViewModel() {

    private companion object {
        const val TAG = "SavedPlantDetailVM"
        const val MAX_AI_RETRIES = 3
    }

    private val _candidateState = MutableStateFlow<UiState<Candidate>>(UiState.Idle)
    val candidateState: StateFlow<UiState<Candidate>> = _candidateState.asStateFlow()

    private val _aiInfoState = MutableStateFlow<UiState<PlantAiInfo>>(UiState.Idle)
    val aiInfoState: StateFlow<UiState<PlantAiInfo>> = _aiInfoState.asStateFlow()

    private val _profile = MutableStateFlow<SupabaseProfile?>(null)

    val safetyAlerts: StateFlow<List<SafetyAlert>> = combine(_aiInfoState, _profile) { ai, profile ->
        if (ai is UiState.Success) SafetyAdvisor.evaluate(ai.data, profile) else emptyList()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val _isFavourite = MutableStateFlow(false)
    val isFavourite: StateFlow<Boolean> = _isFavourite.asStateFlow()

    private val _lastWateredAt = MutableStateFlow<Long?>(null)
    val lastWateredAt: StateFlow<Long?> = _lastWateredAt.asStateFlow()

    private val _canRetry = MutableStateFlow(true)
    val canRetry: StateFlow<Boolean> = _canRetry.asStateFlow()

    private var aiRetryCount = 0
    private var savedPlantId: String? = null
    private var lastScanId: String? = null
    private var lastScientificName: String? = null
    private var observeJob: Job? = null
    private var aiInfoLoadedFor: String? = null

    fun loadSavedPlant(savedPlantId: String) {
        if (this.savedPlantId == savedPlantId) return
        this.savedPlantId = savedPlantId
        _candidateState.value = UiState.Loading
        _aiInfoState.value = UiState.Idle
        aiRetryCount = 0
        _canRetry.value = true
        aiInfoLoadedFor = null

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _profile.value = runCatching { profileRepository.getProfile() }.getOrNull()
            savedPlantRepo.observeById(savedPlantId).collect { saved ->
                if (saved == null) {
                    Log.w(TAG, "loadSavedPlant: not found id=$savedPlantId")
                    _candidateState.value = UiState.Error("Saved plant not found")
                    return@collect
                }
                val candidate = saved.plant.copy(
                    imageUrl = imageUrlResolver.resolve(saved.plant.imageUrl),
                )
                _candidateState.value = UiState.Success(candidate)
                _displayName.value = saved.nickname
                _isFavourite.value = saved.isFavourite
                _lastWateredAt.value = saved.lastWateredAt
                lastScanId = saved.originalScanId
                lastScientificName = candidate.scientificName

                if (aiInfoLoadedFor != candidate.scientificName) {
                    aiInfoLoadedFor = candidate.scientificName
                    val scanId = saved.originalScanId
                    if (scanId != null) {
                        fetchAiInfo(scanId, candidate.scientificName)
                    } else {
                        _aiInfoState.value = UiState.Error("AI info unavailable for this plant")
                        _canRetry.value = false
                    }
                }
            }
        }
    }

    fun retryAiInfo() {
        if (!_canRetry.value) return
        val scanId = lastScanId ?: return
        val name = lastScientificName ?: return
        fetchAiInfo(scanId, name)
    }

    private fun fetchAiInfo(scanId: String, scientificName: String) {
        _aiInfoState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val info = plantService.requestAdditionalInfo(scanId, scientificName)
                aiRetryCount = 0
                _canRetry.value = true
                _aiInfoState.value = UiState.Success(info)
            } catch (e: Exception) {
                aiRetryCount++
                _canRetry.value = aiRetryCount < MAX_AI_RETRIES
                Log.w(TAG, "ai fetch failed for $scientificName ($aiRetryCount/$MAX_AI_RETRIES)", e)
                val remaining = MAX_AI_RETRIES - aiRetryCount
                val message = when {
                    remaining > 1 -> "Couldn't load plant info ($remaining retries left)"
                    remaining == 1 -> "Couldn't load plant info (1 retry left)"
                    else -> "Couldn't load plant info. Please try again later."
                }
                _aiInfoState.value = UiState.Error(message)
            }
        }
    }

    fun updateNickname(newName: String) {
        val id = savedPlantId ?: return
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            try {
                savedPlantRepo.updateNickname(id, trimmed)
                triggerSync()
            } catch (e: Exception) {
                Log.w(TAG, "updateNickname failed", e)
            }
        }
    }

    fun toggleFavourite() {
        val id = savedPlantId ?: return
        val newValue = !_isFavourite.value
        viewModelScope.launch {
            try {
                savedPlantRepo.updateFavourite(id, newValue)
                triggerSync()
            } catch (e: Exception) {
                Log.w(TAG, "toggleFavourite failed", e)
            }
        }
    }

    fun markWatered() {
        val id = savedPlantId ?: return
        viewModelScope.launch {
            try {
                savedPlantRepo.updateLastWatered(id, System.currentTimeMillis())
                triggerSync()
            } catch (e: Exception) {
                Log.w(TAG, "markWatered failed", e)
            }
        }
    }

    fun archive() {
        val id = savedPlantId ?: return
        viewModelScope.launch {
            try {
                savedPlantRepo.unsave(id)
                triggerSync()
            } catch (e: Exception) {
                Log.w(TAG, "archive failed", e)
            }
        }
    }

    private suspend fun triggerSync() {
        try {
            savedPlantSyncManager.syncPending()
        } catch (e: Exception) {
            Log.w(TAG, "syncPending threw ${e::class.simpleName}: ${e.message}", e)
        }
    }
}
