package com.plantsnap.data.plantnet

import com.google.gson.annotations.SerializedName

// ─── Root response ────────────────────────────────────────────────────────────

data class IdentifyPlantResponse(
    val query: Query,
    val language: String,
    val preferedReferential: String,
    /** Set when the API auto-switched to a more appropriate project than requested. */
    val switchToProject: String?,
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
    val otherResults: OtherResults?
)

// ─── Query summary ────────────────────────────────────────────────────────────

data class Query(
    val project: String,
    val images: List<String>,
    /** One organ value per image:
     * auto, leaf, flower, fruit, bark, habit, scan, branch, sheet, other, drawing, seed, bud, anatomy, aerial
     */
    val organs: List<String>,
    val includeRelatedImages: Boolean,
    val noReject: Boolean,
    val type: String?
)

// ─── Species results ──────────────────────────────────────────────────────────

data class Result(
    /** Confidence score between 0 and 1. */
    val score: Double,
    val species: Species,
    /** Populated only when include-related-images=true. */
    val images: List<SpeciesImage>?,
    val gbif: Gbif?,
    val powo: Powo?,
    val iucn: Iucn?
)

data class Species(
    @SerializedName("scientificNameWithoutAuthor") val scientificName: String,
    val scientificNameAuthorship: String,
    /** Full name including authorship. */
    @SerializedName("scientificName") val scientificNameFull: String,
    val genus: Taxon,
    val family: Taxon,
    val commonNames: List<String>
)

data class Taxon(
    @SerializedName("scientificNameWithoutAuthor") val name: String,
    val scientificNameAuthorship: String,
    @SerializedName("scientificName") val nameFull: String
)

// ─── Images ───────────────────────────────────────────────────────────────────

data class SpeciesImage(
    val organ: String,
    val author: String,
    val license: String,
    val date: ImageDate,
    val citation: String,
    /** Three resolutions: o=original, m=medium, s=small. */
    val url: ImageUrl
)

data class ImageDate(
    val timestamp: Long,
    val string: String
)

data class ImageUrl(
    /** Original resolution. */
    val o: String,
    /** Medium resolution. */
    val m: String,
    /** Small/thumbnail resolution. */
    val s: String
)

// ─── External IDs ─────────────────────────────────────────────────────────────

data class Gbif(val id: Int)
data class Powo(val id: String)
data class Iucn(
    val id: String,
    /** IUCN Red List category, e.g. "LC", "EN", "CR". */
    val category: String
)

// ─── Predicted organs ─────────────────────────────────────────────────────────

data class PredictedOrgan(
    val image: String,
    val filename: String,
    val organ: String,
    val score: Double
)

// ─── Other results (detailed=true) ───────────────────────────────────────────

data class OtherResults(
    val genus: List<GenusResult>?,
    val family: List<FamilyResult>?
)

data class GenusResult(
    val score: Double,
    val genus: GenusInfo,
    val gbif: Gbif?,
    val images: List<SpeciesImage>?
)

data class GenusInfo(
    val scientificName: String,
    val family: Taxon,
    val commonNames: List<String>
)

data class FamilyResult(
    val score: Double,
    val family: FamilyInfo,
    val gbif: Gbif?,
    val images: List<SpeciesImage>?
)

data class FamilyInfo(
    val scientificName: String,
    val commonNames: List<String>
)
