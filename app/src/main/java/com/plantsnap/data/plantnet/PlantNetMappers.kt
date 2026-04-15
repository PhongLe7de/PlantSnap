package com.plantsnap.data.plantnet

import com.plantsnap.domain.models.Candidate

fun IdentifyPlantResponse.toCandidates(): List<Candidate> = results.map { it.toCandidate() }

fun Result.toCandidate(): Candidate = Candidate(
    scientificName = species.scientificName,
    commonNames = species.commonNames,
    family = species.family.name,
    score = score.toFloat(),
    iucnCategory = iucn?.category,
    imageUrl = images?.firstOrNull()?.url?.m,
)
