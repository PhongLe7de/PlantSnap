package com.plantsnap.domain.models

data class SavedPlant(
    val id: String,
    val plant: Candidate,
    val sourceScanId: String?,
    val savedAt: Long,
)
