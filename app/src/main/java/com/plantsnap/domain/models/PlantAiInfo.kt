package com.plantsnap.domain.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.contentOrNull

/**
 * Accepts either a JSON string or when Gemini drifts and wraps the field in an
 * object/array, flattens any primitive values it finds into a single space-joined
 * string, so one malformed field never fails the whole response.
 */
internal object LenientStringSerializer : JsonTransformingSerializer<String>(String.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement = when (element) {
        is JsonPrimitive -> element
        is JsonObject -> JsonPrimitive(
            element.values.flatten().joinToString(" ").ifBlank { "" }
        )
        is JsonArray -> JsonPrimitive(
            element.flatten().joinToString(" ").ifBlank { "" }
        )
    }

    private fun Iterable<JsonElement>.flatten(): List<String> = flatMap { value ->
        when (value) {
            is JsonPrimitive -> listOfNotNull(value.contentOrNull)
            is JsonObject -> value.values.flatten()
            is JsonArray -> value.flatten()
        }
    }
}

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
    @Serializable(with = LenientStringSerializer::class)
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
    val waterEveryDays: Int? = null,
    val fertilizeEveryDays: Int? = null,
    val mistEveryDays: Int? = null,
    val rotateEveryDays: Int? = null,
    val repotEveryDays: Int? = null,
)

@Serializable
data class HabitatInfo(
    val title: String? = null,
    val body: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

@Serializable
data class PlantOfTheDay(
    val scientificName: String,
    val commonName: String,
    val care: CareInfo? = null,
    @Serializable(with = LenientStringSerializer::class)
    val toxicity: String? = null,
    val habitat: List<HabitatInfo>? = null,
    val description: String? = null,
    val imageUrl: String? = null,
)
