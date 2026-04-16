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
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false,
    /** Raw IdentifyPlantResponse JSON, forwarded to Supabase `all_results` on sync. */
    val rawResponseJson: String? = null,
    /** Top candidate's GBIF id, as text. */
    val plantGbifId: String? = null,
    /** Top candidate's confidence score (0..1). */
    val identificationScore: Double? = null,
)
