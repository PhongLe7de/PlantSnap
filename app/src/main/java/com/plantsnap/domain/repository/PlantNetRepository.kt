package com.plantsnap.domain.repository

import com.plantsnap.data.plantnet.IdentifyPlantResponse
import java.io.File


interface PlantNetRepository {
    suspend fun identifyPlant(imageFile: File, organ: String?): IdentifyPlantResponse
    suspend fun identifyPlantFromMultipleImages(
        imageFiles: List<File>,
        organs: List<String>?
    ): IdentifyPlantResponse
}