package com.plantsnap.domain.models

data class DiseaseScanResult(
    val imagePath: String,
    val candidates: List<DiseaseCandidate>
)
