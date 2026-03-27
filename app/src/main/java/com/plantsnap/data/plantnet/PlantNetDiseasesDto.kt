package com.plantsnap.data.plantnet

import kotlinx.serialization.Serializable

// Reuses SpeciesImage, ImageDate, ImageUrl from PlantNetIdentifyDto.kt

// ─── Root response ────────────────────────────────────────────────────────────

@Serializable
data class DiseaseResponse(
    val query: DiseaseQuery,
    val language: String,
    /** Probable diseases sorted by confidence score descending (0–1). */
    val results: List<DiseaseResult>,
    val version: String,
    /** Remaining API calls for the current day. */
    val remainingIdentificationRequests: Int
)

// ─── Query summary ────────────────────────────────────────────────────────────

@Serializable
data class DiseaseQuery(
    val images: List<String>,
    /** One organ value per image: leaf | flower | fruit | bark | auto. */
    val organs: List<String>,
    val includeRelatedImages: Boolean,
    val noReject: Boolean
)

// ─── Disease result ───────────────────────────────────────────────────────────

@Serializable
data class DiseaseResult(
    /** EPPO code identifying the disease (e.g. "PHYTIN" for Phytophthora infestans). */
    val name: String,
    /** Human-readable disease name in the requested language. */
    val description: String,
    /** Confidence score between 0 and 1. */
    val score: Double,
    /** Populated only when include-related-images=true. */
    val images: List<SpeciesImage>? = null
)
