package com.plantsnap.domain.services

import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.ScanResult
import com.plantsnap.domain.repository.PlantNetRepository
import com.plantsnap.domain.repository.ScanRepository
import java.io.File
import javax.inject.Inject


class PlantService @Inject constructor(
    private val plantNetRepo: PlantNetRepository,
//    private val llmRepo: LlmRepository,
    private val scanRepo: ScanRepository
) {

    // Request plant from image(s), map relevant info for the UI from the res, save to supabase and local DB
    suspend fun identifyPlantAndSaveToLocal(imageFiles: List<File>, organs: List<String>): ScanResult {
        val plants = plantNetRepo.identifyPlantFromMultipleImages(imageFiles, organs)
        val scanResult = ScanResult( // TODO: no business logic in service, move this somewhere 🧑‍🦽‍➡️
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

        scanRepo.save(scanResult)
        return scanResult
    }

    suspend fun requestAdditionalInfo(scanId: String) {
//        val scan = scanRepo.getById(scanId) ?: return
//        val aiInfo = llmRepo.getPlantInfo(scan.bestMatch)
//        scanRepo.updateAiInfo(scanId, aiInfo)
    }

}