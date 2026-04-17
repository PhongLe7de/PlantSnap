package com.plantsnap.data.remote.supabase

import com.plantsnap.data.local.model.ScanEntity
import com.plantsnap.data.plantnet.IdentifyPlantResponse
import com.plantsnap.data.plantnet.toCandidates
import com.plantsnap.domain.models.ScanResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.time.Instant

@Serializable
data class SupabaseScanDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("device_id") val deviceId: String?,
    @SerialName("plant_gbif_id") val plantGbifId: String?,
    @SerialName("identification_score") val identificationScore: Double?,
    @SerialName("detected_organ") val detectedOrgan: String?,
    @SerialName("all_results") val allResults: JsonElement?,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("scanned_at") val scannedAt: String,
)

fun ScanEntity.toSupabaseDto(userId: String, deviceId: String, json: Json): SupabaseScanDto =
    SupabaseScanDto(
        id = id,
        userId = userId,
        deviceId = deviceId,
        plantGbifId = plantGbifId,
        identificationScore = identificationScore,
        detectedOrgan = organ,
        allResults = rawResponseJson?.let { json.parseToJsonElement(it) },
        scannedAt = Instant.ofEpochMilli(timestamp).toString(),
    )

/**
 * Reconstructs a ScanResult from a Supabase row. `imagePath` is left blank: the
 * original photo lives on the device that captured it and isn't in Supabase,
 * so remote-only scans fall back to the PlantNet reference image in the UI.
 */
fun SupabaseScanDto.toScanResult(json: Json): ScanResult {
    val parsed = allResults?.let { json.decodeFromJsonElement(IdentifyPlantResponse.serializer(), it) }
    return ScanResult(
        id = id,
        imagePath = "",
        organ = detectedOrgan ?: "auto",
        bestMatch = parsed?.bestMatch ?: "",
        candidates = parsed?.toCandidates().orEmpty(),
        timestamp = Instant.parse(scannedAt).toEpochMilli(),
        synced = true,
        rawResponseJson = allResults?.toString(),
        plantGbifId = plantGbifId,
        identificationScore = identificationScore,
    )
}
