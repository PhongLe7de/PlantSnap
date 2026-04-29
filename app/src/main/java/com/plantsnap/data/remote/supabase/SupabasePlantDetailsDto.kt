package com.plantsnap.data.remote.supabase

import com.plantsnap.domain.models.PlantAiInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabasePlantDetailsDto(
    @SerialName("plant_gbif_id") val plantGbifId: Long,
    @SerialName("scientific_name") val scientificName: String,
    val description: String? = null,
    val habitat: String? = null,
    @SerialName("toxicity_human") val toxicityHuman: String? = null,
    @SerialName("toxicity_cat") val toxicityCat: String? = null,
    @SerialName("toxicity_dog") val toxicityDog: String? = null,
    @SerialName("care_temperature") val careTemperature: String? = null,
    @SerialName("care_light") val careLight: String? = null,
    @SerialName("care_water") val careWater: String? = null,
    @SerialName("care_humidity") val careHumidity: String? = null,
    @SerialName("care_soil") val careSoil: String? = null,
    @SerialName("foraging_notes") val foragingNotes: String? = null,
)

@Serializable
data class PlantDetailsNameRow(
    @SerialName("plant_gbif_id") val plantGbifId: Long,
    @SerialName("scientific_name") val scientificName: String,
)

fun PlantAiInfo.toPlantDetailsDto(plantGbifId: Long, scientificName: String): SupabasePlantDetailsDto =
    SupabasePlantDetailsDto(
        plantGbifId = plantGbifId,
        scientificName = scientificName,
        description = description,
        habitat = habitat?.takeIf { it.isNotEmpty() }?.joinToString("\n\n") { entry ->
            val title = entry.title.orEmpty()
            val body = entry.body.orEmpty()
            if (title.isNotBlank()) "$title: $body" else body
        },
        toxicityHuman = safety?.human?.symptoms ?: toxicity,
        toxicityCat = safety?.cat?.symptoms,
        toxicityDog = safety?.dog?.symptoms,
        careTemperature = care?.temperature,
        careLight = care?.light,
        careWater = care?.water,
        careHumidity = care?.humidity,
        careSoil = care?.soil,
        foragingNotes = safety?.foragingNotes,
    )
