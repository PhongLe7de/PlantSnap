package com.plantsnap.ui.screens.profile

import com.plantsnap.ui.screens.profile.model.PlantRank

data class ProfileStatsState(
    val totalScans: Int = 0,
    val plantsFound: Int = 0,
    val firstScanTimestamp: Long? = null,
    val rank: PlantRank = PlantRank.SEEDLING,
    val rankProgress: Float = 0f,
    val scansToNextRank: Int = 0,
    val isLoading: Boolean = true
)
