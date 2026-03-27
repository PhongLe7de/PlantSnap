package com.plantsnap.domain.models

import java.util.UUID

/** Mapped from IdentifyPlantResponse keeping only UI-relevant fields. */
data class ScanResult(
    val id: String = UUID.randomUUID().toString(),
    val imagePath: String,
    val organ: String,
    /** Top-1 scientific name shortcut (IdentifyPlantResponse.bestMatch). */
    val bestMatch: String,
    /** All ranked candidates from PlantNet, sorted by confidence descending. */
    val candidates: List<Candidate>,
    /** AI-generated care/toxicity/habitat info. Null until user requests detail. */
    val aiInfo: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
