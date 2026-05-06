package com.plantsnap.domain.models

data class DiseaseCandidate(
    val eppoCode: String,
    val commonName: String,
    val score: Float,
    val imageUrl: String? = null
)
