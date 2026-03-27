package com.plantsnap.data.plantnet

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Root response ────────────────────────────────────────────────────────────

@Serializable
data class IdentifyPlantResponse(
    val query: Query,
    val language: String,
    val preferedReferential: String,
    /** Set when the API auto-switched to a more appropriate project than requested. */
    val switchToProject: String? = null,
    /** Shortcut to the top-1 species full scientific name. */
    val bestMatch: String,
    /** Probable species sorted by confidence score descending (0–1). */
    val results: List<Result>,
    /** Remaining API calls for the current day. */
    val remainingIdentificationRequests: Int,
    val version: String,
    /** Top-1 predicted organ for each submitted image. */
    val predictedOrgans: List<PredictedOrgan>,
    /** Present only when request was made with detailed=true. */
    val otherResults: OtherResults? = null
)

// ─── Query summary ────────────────────────────────────────────────────────────

@Serializable
data class Query(
    val project: String,
    val images: List<String>,
    /** One organ value per image:
     * auto, leaf, flower, fruit, bark, habit, scan, branch, sheet, other, drawing, seed, bud, anatomy, aerial
     */
    val organs: List<String>,
    val includeRelatedImages: Boolean,
    val noReject: Boolean,
    val type: String? = null
)

// ─── Species results ──────────────────────────────────────────────────────────

@Serializable
data class Result(
    /** Confidence score between 0 and 1. */
    val score: Double,
    val species: Species,
    /** Populated only when include-related-images=true. */
    val images: List<SpeciesImage>? = null,
    val gbif: Gbif? = null,
    val powo: Powo? = null,
    val iucn: Iucn? = null
)

@Serializable
data class Species(
    @SerialName("scientificNameWithoutAuthor") val scientificName: String,
    val scientificNameAuthorship: String,
    /** Full name including authorship. */
    @SerialName("scientificName") val scientificNameFull: String,
    val genus: Taxon,
    val family: Taxon,
    val commonNames: List<String>
)

@Serializable
data class Taxon(
    @SerialName("scientificNameWithoutAuthor") val name: String,
    val scientificNameAuthorship: String,
    @SerialName("scientificName") val nameFull: String
)

// ─── Images ───────────────────────────────────────────────────────────────────

@Serializable
data class SpeciesImage(
    val organ: String,
    val author: String,
    val license: String,
    val date: ImageDate,
    val citation: String,
    /** Three resolutions: o=original, m=medium, s=small. */
    val url: ImageUrl
)

@Serializable
data class ImageDate(
    val timestamp: Long,
    val string: String
)

@Serializable
data class ImageUrl(
    /** Original resolution. */
    val o: String,
    /** Medium resolution. */
    val m: String,
    /** Small/thumbnail resolution. */
    val s: String
)

// ─── External IDs ─────────────────────────────────────────────────────────────

@Serializable
data class Gbif(val id: Int)

@Serializable
data class Powo(val id: String)

@Serializable
data class Iucn(
    val id: String,
    /** IUCN Red List category e.g. "LC" (least concern), "EN" (endangered), "CR" (critically endangered). */
    val category: String
)

// ─── Predicted organs ─────────────────────────────────────────────────────────

@Serializable
data class PredictedOrgan(
    val image: String,
    val filename: String,
    val organ: String,
    val score: Double
)

// ─── Other results (detailed=true) ───────────────────────────────────────────

@Serializable
data class OtherResults(
    val genus: List<GenusResult>? = null,
    val family: List<FamilyResult>? = null
)

@Serializable
data class GenusResult(
    val score: Double,
    val genus: GenusInfo,
    val gbif: Gbif? = null,
    val images: List<SpeciesImage>? = null
)

@Serializable
data class GenusInfo(
    val scientificName: String,
    val family: Taxon,
    val commonNames: List<String>
)

@Serializable
data class FamilyResult(
    val score: Double,
    val family: FamilyInfo,
    val gbif: Gbif? = null,
    val images: List<SpeciesImage>? = null
)

@Serializable
data class FamilyInfo(
    val scientificName: String,
    val commonNames: List<String>
)
