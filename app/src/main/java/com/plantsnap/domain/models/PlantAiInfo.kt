package com.plantsnap.domain.models

import kotlinx.serialization.Serializable

@Serializable
enum class ToxicityLevel { NONE, MILD, MODERATE, SEVERE, UNKNOWN }

@Serializable
enum class Edibility { EDIBLE, INEDIBLE, TOXIC, UNKNOWN }

@Serializable
data class PetToxicity(
    val level: ToxicityLevel? = ToxicityLevel.UNKNOWN,
    val symptoms: String? = null,
)

@Serializable
data class SafetyInfo(
    val dog: PetToxicity? = null,
    val cat: PetToxicity? = null,
    val human: PetToxicity? = null,
    val edibility: Edibility? = Edibility.UNKNOWN,
    val foragingNotes: String? = null,
)

@Serializable
data class PlantAiInfo(
    val care: CareInfo? = null,
    val toxicity: String? = null,
    val safety: SafetyInfo? = null,
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
