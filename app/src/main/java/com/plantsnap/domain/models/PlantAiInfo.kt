package com.plantsnap.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class PlantAiInfo(
    val care: CareInfo? = null,
    val toxicity: String? = null,
    val habitat: List<HabitatInfo>? = null,
    val description: String? = null,
)

@Serializable
data class CareInfo(
    val light: String? = null,
    val water: String? = null,
    val temperature: String? = null,
    val humidity: String? = null,
    val soil: String? = null,
)

@Serializable
data class HabitatInfo(
    val title: String? = null,
    val body: String? = null,
    val imageUrl: String? = null,
)

@Serializable
data class PlantOfTheDay(
    val scientificName: String,
    val commonName: String,
    val care: CareInfo? = null,
    val toxicity: String? = null,
    val habitat: List<HabitatInfo>? = null,
    val description: String? = null,
    val imageUrl: String? = null,
)
