package com.plantsnap.data.repository

import com.plantsnap.data.plantnet.IdentifyPlantResponse
import com.plantsnap.data.plantnet.PlantNetApi
import com.plantsnap.domain.repository.PlantNetRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PlantNetRepositoryImpl @Inject constructor(
    private val api: PlantNetApi
) : PlantNetRepository {
    // 1 image file. This might be redundant
    override suspend fun identifyPlant(imageFile: File, organ: String?): IdentifyPlantResponse {
        val body = imageFile.asRequestBody("image/jpeg".toMediaType())
        val imagePart = MultipartBody.Part.createFormData("images", imageFile.name, body)
        val organPart = MultipartBody.Part.createFormData("organs", organ ?: "auto")
        return api.identify(images = listOf(imagePart), organs = listOf(organPart))
    }

    // Multiple files
    override suspend fun identifyPlantFromMultipleImages(
        imageFiles: List<File>,
        organs: List<String>?
    ): IdentifyPlantResponse {
        val imageParts = imageFiles.map { file ->
            val body = file.asRequestBody("image/jpeg".toMediaType())
            MultipartBody.Part.createFormData("images", file.name, body)
        }
        val organParts = imageFiles.mapIndexed { index, _ ->
            MultipartBody.Part.createFormData("organs", organs?.getOrNull(index) ?: "auto")
        }
        return api.identify(images = imageParts, organs = organParts)
    }
}
