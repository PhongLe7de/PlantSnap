package com.plantsnap.domain.repository

import com.plantsnap.data.plantnet.IdentifyPlantResponse
import com.plantsnap.domain.models.DiseaseScanResult
import java.io.File


interface PlantNetRepository {
    suspend fun identifyPlant(imageFile: File, organ: String?): IdentifyPlantResponse
    suspend fun identifyPlantFromMultipleImages(
        imageFiles: List<File>,
        organs: List<String>?
    ): IdentifyPlantResponse
    suspend fun identifyDisease(imageFiles: List<File>, organs: List<String>?, imagePath: String): DiseaseScanResult
}