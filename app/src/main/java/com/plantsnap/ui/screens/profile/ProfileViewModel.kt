package com.plantsnap.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsnap.domain.repository.ScanRepository
import com.plantsnap.ui.screens.profile.model.PlantRank
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _statsState = MutableStateFlow(ProfileStatsState())
    val statsState: StateFlow<ProfileStatsState> = _statsState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                scanRepository.observeTotalScanCount(),
                scanRepository.observeDistinctSpeciesCount(),
                scanRepository.observeFirstScanTimestamp(),
            ) { total, speciesCount, firstTimestamp ->
                ProfileStatsState(
                    totalScans = total,
                    plantsFound = speciesCount,
                    firstScanTimestamp = firstTimestamp,
                    rank = PlantRank.fromScanCount(total),
                    rankProgress = PlantRank.progressToNext(total),
                    scansToNextRank = PlantRank.scansToNextRank(total),
                    isLoading = false,
                )
            }.collect { _statsState.value = it }
        }
    }
}
