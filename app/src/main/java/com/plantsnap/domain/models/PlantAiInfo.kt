package com.plantsnap.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class PlantAiInfo(
    val care: String,
    val toxicity: String,
    val habitat: String,
)
