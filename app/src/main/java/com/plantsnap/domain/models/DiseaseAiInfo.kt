package com.plantsnap.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class DiseaseAiInfo(
    val description: String? = null,
    val symptoms: String? = null,
    val causes: String? = null,
    val treatment: String? = null,
    val prevention: String? = null,
)
