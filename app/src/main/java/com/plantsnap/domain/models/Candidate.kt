package com.plantsnap.domain.models

data class Candidate(
    /** Clean scientific name without authorship (species.scientificNameWithoutAuthor). */
    val scientificName: String,
    /** All common names in the requested language (species.commonNames). */
    val commonNames: List<String>,
    /** Plant family (species.family.scientificNameWithoutAuthor). */
    val family: String,
    /** Confidence score 0–1 (result.score). */
    val score: Float,
    /** IUCN Red List category e.g. "LC", "EN", "CR". Null if not on Red List. */
    val iucnCategory: String?,
    val imageUrl: String? = null,
    /** Cached PlantAiInfo serialized as JSON. Null until fetched from Gemini. */
    val aiInfo: String? = null,
)
