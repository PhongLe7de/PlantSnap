package com.plantsnap.domain.services

import com.plantsnap.data.plantnet.IdentifyPlantResponse
import com.plantsnap.data.repository.PlantNetRepository
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.ScanResult
import java.io.File
import javax.inject.Inject


class PlantService @Inject constructor(
    private val plantNetRepo: PlantNetRepository,
//    private val llmRepo: LlmRepository,
//    private val scanRepo: ScanRepository
) {

    // Request plant from image(s), map relevant info for the UI from the res, save to supabase and local DB
    suspend fun identifyPlantAndSaveToLocal(
        imageFiles: List<File>,
        organs: List<String>
    ): ScanResult {
        val plants: IdentifyPlantResponse =
            plantNetRepo.identifyPlantFromMultipleImages(imageFiles, organs)
        val scanResult =
            ScanResult( // TODO: no business logic in service, move this somewhere 🧑‍🦽‍➡️
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

        //llmRepo.getPlantInfo(Candidate) TODO: Additional gemini info on every scan or by separate request by user?
        //scanRepo.save(scanResult)  // TODO
        return scanResult
    }

    fun getPlantsFromLocal(): List<Candidate> {
        // TODO: Implement local storage retrieval and proper error handling
        throw UnsupportedOperationException("getPlantsFromLocal is not yet implemented")
    }
}