package com.plantsnap.domain.services

import android.util.Log
import com.plantsnap.data.plantnet.IdentifyPlantResponse
import com.plantsnap.data.plantnet.toCandidates
import com.plantsnap.data.sync.ScanSyncManager
import com.plantsnap.domain.models.PlantAiInfo
import com.plantsnap.domain.models.PlantOfTheDay
import com.plantsnap.domain.models.ScanResult
import com.plantsnap.domain.repository.GeminiRepository
import com.plantsnap.domain.repository.PlantNetRepository
import com.plantsnap.domain.repository.ScanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject


class PlantService @Inject constructor(
    private val plantNetRepo: PlantNetRepository,
    private val geminiRepo: GeminiRepository,
    private val scanRepo: ScanRepository,
    private val scanSyncManager: ScanSyncManager,
    private val json: Json,
) {

    private companion object {
        const val TAG = "PlantService"
    }

    suspend fun identifyPlantAndSaveToLocal(imageFiles: List<File>, organs: List<String>): ScanResult {
        require(imageFiles.isNotEmpty()) { "imageFiles must not be empty" }
        require(imageFiles.size == organs.size) { "imageFiles and organs must have the same size" }

        imageFiles.forEach { file ->
            require(file.exists()) { "Image file does not exist: ${file.absolutePath}" }
        }

        Log.d(TAG, "Identifying plant from ${imageFiles.size} images...")
        val plants = plantNetRepo.identifyPlantFromMultipleImages(imageFiles, organs)

        val topResult = plants.results.firstOrNull()
        val scanResult = ScanResult(
            imagePath = imageFiles.first().absolutePath,
            organ = plants.predictedOrgans.firstOrNull()?.organ ?: "auto",
            bestMatch = plants.bestMatch,
            candidates = plants.toCandidates(),
            rawResponseJson = json.encodeToString(IdentifyPlantResponse.serializer(), plants),
            plantGbifId = topResult?.gbif?.id?.toString(),
            identificationScore = topResult?.score,
        )

        scanRepo.save(scanResult)
        Log.d(TAG, "Saved scan ${scanResult.id} to local DB")

        try {
            scanSyncManager.syncPending()
        } catch (e: Exception) {
            Log.w(TAG, "Post-save sync failed (will retry later)", e)
        }

        return scanResult
    }

    suspend fun requestAdditionalInfo(scanId: String, scientificName: String): PlantAiInfo {
        Log.d(TAG, "requestAdditionalInfo: scanId=$scanId name=$scientificName")
        val info = geminiRepo.getPlantInfo(scientificName)
        val infoJson = json.encodeToString(PlantAiInfo.serializer(), info)
        scanRepo.updateCandidateAiInfo(scanId, scientificName, infoJson)
        return info
    }

    //TODO: add caching and handle image search
    suspend fun getPlantOfTheDay(): PlantOfTheDay {
        return geminiRepo.getPlantOfTheDay()
    }
    fun getPlantsFromLocal(): Flow<List<ScanResult>> = scanRepo.getAll()
}
