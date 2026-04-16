package com.plantsnap.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class PlantAiInfo(
    val care: CareInfo,
    val toxicity: String,
    val habitat: List<HabitatInfo>,
    val description: String,
)

@Serializable
data class CareInfo(
    val light: String,
    val water: String,
    val temperature: String,
    val humidity: String,
    val soil: String,
)

@Serializable
data class HabitatInfo(
    val title: String,
    val body: String,
)
