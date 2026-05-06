package com.plantsnap.data.plantnet

import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.DiseaseCandidate
import com.plantsnap.domain.models.DiseaseScanResult

fun IdentifyPlantResponse.toCandidates(): List<Candidate> = results.map { it.toCandidate() }

fun Result.toCandidate(): Candidate = Candidate(
    scientificName = species.scientificName,
    commonNames = species.commonNames,
    family = species.family.name,
    score = score.toFloat(),
    iucnCategory = iucn?.category,
    imageUrl = images?.firstOrNull()?.url?.m,
    gbifId = gbif?.id?.toLong(),
)

fun DiseaseResponse.toDomain(imagePath: String): DiseaseScanResult = DiseaseScanResult(
    imagePath = imagePath,
    candidates = results.map { r ->
        DiseaseCandidate(
            eppoCode = r.name,
            commonName = r.description,
            score = r.score.toFloat(),
            imageUrl = r.images?.firstOrNull()?.url?.m
        )
    }
)
