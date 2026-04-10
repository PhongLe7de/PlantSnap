package com.plantsnap.domain.services

import android.util.Log
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.ScanResult
import com.plantsnap.domain.repository.PlantNetRepository
import com.plantsnap.domain.repository.ScanRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject


class PlantService @Inject constructor(
    private val plantNetRepo: PlantNetRepository,
//    private val llmRepo: LlmRepository,
    private val scanRepo: ScanRepository
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

        val scanResult = ScanResult(
            imagePath = imageFiles.first().absolutePath,
            organ = plants.predictedOrgans.firstOrNull()?.organ ?: "auto",
            bestMatch = plants.bestMatch,
            candidates = plants.results.map { result ->
                Candidate(
                    scientificName = result.species.scientificName,
                    commonNames = result.species.commonNames,
                    family = result.species.family.name,
                    score = result.score.toFloat(),
                    iucnCategory = result.iucn?.category
                )
            },
            aiInfo = null
        )

        try {
            scanRepo.save(scanResult)
            Log.d(TAG, "Saved scan ${scanResult.id} to local DB")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save scan to local DB", e)
        }

        return scanResult
    }

    suspend fun requestAdditionalInfo(scanId: String) {
//        val scan = scanRepo.getById(scanId) ?: return
//        val aiInfo = llmRepo.getPlantInfo(scan.bestMatch)
//        scanRepo.updateAiInfo(scanId, aiInfo)
    }

    fun getPlantsFromLocal(): Flow<List<ScanResult>> = scanRepo.getAll()
}