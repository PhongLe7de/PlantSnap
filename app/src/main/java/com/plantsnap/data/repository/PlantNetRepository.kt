package com.plantsnap.data.repository

import com.plantsnap.data.plantnet.IdentifyPlantResponse
import com.plantsnap.data.plantnet.PlantNetApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PlantNetRepository @Inject constructor(
    private val api: PlantNetApi
) {
    // 1 image file
    suspend fun identifyPlant(imageFile: File): IdentifyPlantResponse {
        val body = imageFile.asRequestBody("image/jpeg".toMediaType())
        val part = MultipartBody.Part.createFormData("images", imageFile.name, body)
        val organ = MultipartBody.Part.createFormData("organs", "auto")
        return api.identify(images = listOf(part), organs = listOf(organ))
    }

    // Multiple files
    suspend fun identifyPlantFromMultipleImages(imageFiles: List<File>): IdentifyPlantResponse {
        val imageParts = imageFiles.map { file ->
            val body = file.asRequestBody("image/jpeg".toMediaType())
            MultipartBody.Part.createFormData("images", file.name, body)
        }
        val organParts = imageFiles.map {
            MultipartBody.Part.createFormData("organs", "auto")
        }
        return api.identify(images = imageParts, organs = organParts)
    }
}
