package com.plantsnap.ui.screens.identify.disease

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsnap.data.DiseaseScanResultHolder
import com.plantsnap.domain.models.DiseaseAiInfo
import com.plantsnap.domain.models.DiseaseCandidate
import com.plantsnap.domain.repository.GeminiRepository
import com.plantsnap.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiseaseDetailViewModel @Inject constructor(
    private val scanResultHolder: DiseaseScanResultHolder,
    private val geminiRepository: GeminiRepository,
) : ViewModel() {

    private companion object {
        const val TAG = "DiseaseDetailViewModel"
        const val MAX_AI_RETRIES = 3
    }

    private val _candidateState = MutableStateFlow<UiState<DiseaseCandidate>>(UiState.Idle)
    val candidateState: StateFlow<UiState<DiseaseCandidate>> = _candidateState.asStateFlow()

    private val _aiInfoState = MutableStateFlow<UiState<DiseaseAiInfo>>(UiState.Idle)
    val aiInfoState: StateFlow<UiState<DiseaseAiInfo>> = _aiInfoState.asStateFlow()

    private val _canRetry = MutableStateFlow(true)
    val canRetry: StateFlow<Boolean> = _canRetry.asStateFlow()

    private var aiRetryCount = 0
    private var lastCandidate: DiseaseCandidate? = null

    fun loadDetail(candidateIndex: Int) {
        val result = scanResultHolder.result
        if (result == null) {
            _candidateState.value = UiState.Error("Disease scan result not available")
            return
        }
        val candidate = result.candidates.getOrNull(candidateIndex)
        if (candidate == null) {
            _candidateState.value = UiState.Error("Candidate not found")
            return
        }
        _candidateState.value = UiState.Success(candidate)
        lastCandidate = candidate
        aiRetryCount = 0
        _canRetry.value = true
        fetchAiInfo(candidate)
    }

    fun retryAiInfo() {
        if (!_canRetry.value) return
        val candidate = lastCandidate ?: return
        fetchAiInfo(candidate)
    }

    private fun fetchAiInfo(candidate: DiseaseCandidate) {
        _aiInfoState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val info = geminiRepository.getDiseaseInfo(candidate.commonName, candidate.eppoCode)
                aiRetryCount = 0
                _canRetry.value = true
                _aiInfoState.value = UiState.Success(info)
            } catch (e: Exception) {
                aiRetryCount++
                _canRetry.value = aiRetryCount < MAX_AI_RETRIES
                Log.w(TAG, "Gemini fetch failed for ${candidate.commonName} (attempt $aiRetryCount/$MAX_AI_RETRIES)", e)
                val remaining = MAX_AI_RETRIES - aiRetryCount
                val message = when {
                    remaining > 1 -> "Couldn't load disease info ($remaining retries left)"
                    remaining == 1 -> "Couldn't load disease info (1 retry left)"
                    else -> "Couldn't load disease info. Please try again later."
                }
                _aiInfoState.value = UiState.Error(message)
            }
        }
    }
}
