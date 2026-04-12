package com.plantsnap.data.plantnet

import com.plantsnap.BuildConfig
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * PlantNet API
 *
 * Base URL: https://my-api.plantnet.org/
 * Auth:     api-key query parameter on every request
 * Quota:    remainingIdentificationRequests in each response body
 *
 * Docs: https://my.plantnet.org/doc/api
 */
interface PlantNetApi {

    /**
     * POST /v2/identify/{project}
     *
     * Identifies plant species from one or more images.
     * NOTE:
     * One request identifies one plant. When multiple images are submitted at once,
     * they must represent the same individual.
     * Returns a ranked list of probable species sorted by confidence score (0–1) descending.
     *
     * @param project  Flora/referential to query. Use "all" when the geographic area is unknown.
     * @param apiKey   Your PlantNet API key (required).
     * @param images   1–5 JPG/PNG images of the same plant individual. Total size ≤ 50 MB.
     * @param organs   Organ depicted in each image:
     * auto, leaf, flower, fruit, bark, habit, scan, branch, sheet, other, drawing, seed, bud, anatomy, aerial
     *                 Must have the same number of entries as [images]. Defaults to "auto".
     * @param noReject Set to true to prevent rejection when the image is not recognised as a plant.
     * @param nbResults Limit the number of species returned. Fewer results improve response time.
     * @param lang      Available lang codes: https://my.plantnet.org/doc/api/openapi
     */
    @Multipart
    @POST("v2/identify/{project}")
    suspend fun identify(
        @Path("project") project: String = "all",
        @Query("api-key") apiKey: String = BuildConfig.PLANTNET_API_KEY,
        @Part images: List<MultipartBody.Part>,
        @Part organs: List<MultipartBody.Part>,
        @Query("include-related-images") includeRelatedImages: Boolean = false,
        @Query("no-reject") noReject: Boolean = false,
        @Query("nb-results") nbResults: Int = 5,
        @Query("lang") lang: String = "en"
    ): IdentifyPlantResponse

    /**
     * POST /v2/diseases/identify
     *
     * Identifies plant diseases from one or more images.
     * Returns a ranked list of probable diseases sorted by confidence score (0–1) descending.
     * Diseases are identified by EPPO codes.
     *
     * NOTE: One request identifies one plant. Multiple images must represent the same individual.
     *
     * @param apiKey   Your PlantNet API key (required).
     * @param images   1–5 JPG/PNG images of the same plant individual. Total size ≤ 50 MB.
     * @param organs   Organ depicted in each image: leaf | flower | fruit | bark | auto.
     *                 Must have the same number of entries as [images]. Defaults to "auto".
     * @param noReject Set to true to prevent rejection when the image is not recognised as a plant.
     * @param nbResults Limit the number of diseases returned. Fewer results improve response time.
     * @param lang     Language code for localised disease descriptions (e.g. "en", "fr").
     *                 Available codes: https://my.plantnet.org/doc/api/openapi
     */
    @Multipart
    @POST("v2/diseases/identify")
    suspend fun identifyDisease(
        @Query("api-key") apiKey: String = BuildConfig.PLANTNET_API_KEY,
        @Part images: List<MultipartBody.Part>,
        @Part organs: List<MultipartBody.Part>,
        @Query("no-reject") noReject: Boolean = false,
        @Query("nb-results") nbResults: Int = 5,
        @Query("lang") lang: String = "en"
    ): DiseaseResponse
}
