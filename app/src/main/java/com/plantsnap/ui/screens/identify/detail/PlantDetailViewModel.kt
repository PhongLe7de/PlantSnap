package com.plantsnap.ui.screens.identify.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.PlantAiInfo
import com.plantsnap.domain.models.SupabaseProfile
import com.plantsnap.domain.repository.ProfileRepository
import com.plantsnap.domain.repository.SavedPlantRepository
import com.plantsnap.domain.repository.ScanRepository
import com.plantsnap.domain.safety.SafetyAdvisor
import com.plantsnap.domain.safety.SafetyAlert
import com.plantsnap.domain.services.PlantService
import com.plantsnap.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class PlantDetailViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val plantService: PlantService,
    private val profileRepository: ProfileRepository,
    private val savedPlantRepo: SavedPlantRepository,
    private val json: Json,
) : ViewModel() {

    private companion object {
        const val TAG = "PlantDetailViewModel"
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

    private val _canRetry = MutableStateFlow(true)
    val canRetry: StateFlow<Boolean> = _canRetry.asStateFlow()

    private var aiRetryCount = 0
    private var lastScanId: String? = null
    private var lastScientificName: String? = null
    private var currentScanId: String? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val isSaved: StateFlow<Boolean> = candidateState
        .flatMapLatest { state ->
            val c = (state as? UiState.Success)?.data
            val scanId = currentScanId
            if (c == null || scanId == null) flowOf(false)
            else savedPlantRepo.observeIsSaved(scanId, c.scientificName)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun loadPlantDetail(plantId: String, candidateIndex: Int) {
        _candidateState.value = UiState.Loading
        _aiInfoState.value = UiState.Idle
        aiRetryCount = 0
        _canRetry.value = true
        currentScanId = plantId
        Log.d(TAG, "loadPlantDetail: scanId=$plantId, candidateIndex=$candidateIndex")
        viewModelScope.launch {
            val scanResult = scanRepository.observeById(plantId).firstOrNull()
            if (scanResult == null) {
                Log.w(TAG, "loadPlantDetail: scan not found for id=$plantId")
                _candidateState.value = UiState.Error("Plant details not found")
                return@launch
            }
            val candidate = scanResult.candidates.getOrNull(candidateIndex)
            if (candidate == null) {
                Log.w(
                    TAG,
                    "loadPlantDetail: candidate index $candidateIndex out of bounds (${scanResult.candidates.size} candidates)"
                )
                _candidateState.value = UiState.Error("Candidate not found")
                return@launch
            }

            Log.d(TAG, "loadPlantDetail: ${candidate.scientificName} (${candidate.score * 100}%)")
            _candidateState.value = UiState.Success(candidate)
            lastScanId = plantId
            lastScientificName = candidate.scientificName

            _profile.value = runCatching { profileRepository.getProfile() }.getOrNull()

            val cached = candidate.aiInfo?.let { runCatching { json.decodeFromString(PlantAiInfo.serializer(), it) }.getOrNull() }
            if (cached != null) {
                Log.d(TAG, "aiInfo cache hit for ${candidate.scientificName}")
                _aiInfoState.value = UiState.Success(cached)
            } else {
                fetchAiInfo(plantId, candidate.scientificName)
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
                Log.w(TAG, "gemini fetch failed for $scientificName (attempt $aiRetryCount/$MAX_AI_RETRIES)", e)
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

    fun toggleSaved() {
        val c = (candidateState.value as? UiState.Success)?.data ?: return
        val scanId = currentScanId ?: return
        viewModelScope.launch {
            if (isSaved.value) {
                val existing = savedPlantRepo.findExisting(scanId, c.scientificName) ?: return@launch
                savedPlantRepo.unsave(existing.id)
            } else {
                savedPlantRepo.save(c, scanId)
            }
        }
    }
}
