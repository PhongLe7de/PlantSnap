package com.plantsnap.domain.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.plantsnap.data.local.PlantOfTheDayDao
import com.plantsnap.data.local.SavedPlantDao
import com.plantsnap.data.local.ScanDao
import com.plantsnap.data.local.model.PlantOfTheDayEntity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.plantsnap.data.plantnet.IdentifyPlantResponse
import com.plantsnap.data.plantnet.toCandidates
import com.plantsnap.data.storage.PlantImageUploader
import com.plantsnap.data.sync.ScanSyncManager
import com.plantsnap.domain.models.PlantAiInfo
import com.plantsnap.domain.models.PlantOfTheDay
import com.plantsnap.domain.models.ScanResult
import com.plantsnap.domain.repository.CareTaskRepository
import com.plantsnap.domain.repository.GeminiRepository
import com.plantsnap.domain.repository.PlantDetailsRepository
import com.plantsnap.domain.repository.PlantNetRepository
import com.plantsnap.domain.repository.ScanRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject


class PlantService @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val plantNetRepo: PlantNetRepository,
    private val geminiRepo: GeminiRepository,
    private val scanRepo: ScanRepository,
    private val scanSyncManager: ScanSyncManager,
    private val plantDetailsRepository: PlantDetailsRepository,
    private val plantImageUploader: PlantImageUploader,
    private val plantOfTheDayDao: PlantOfTheDayDao,
    private val savedPlantDao: SavedPlantDao,
    private val scanDao: ScanDao,
    private val careTaskRepository: CareTaskRepository,
    private val json: Json,
) {
    private val locationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(
            appContext
        )

    private companion object {
        const val TAG = "PlantService"
    }

    suspend fun identifyPlantAndSaveToLocal(
        imageFiles: List<File>,
        organs: List<String>
    ): ScanResult {
        require(imageFiles.isNotEmpty()) { "imageFiles must not be empty" }
        require(imageFiles.size == organs.size) { "imageFiles and organs must have the same size" }

        imageFiles.forEach { file ->
            require(file.exists()) { "Image file does not exist: ${file.absolutePath}" }
        }

        Log.d(TAG, "Identifying plant from ${imageFiles.size} images...")
        val plants = plantNetRepo.identifyPlantFromMultipleImages(imageFiles, organs)

        val topResult = plants.results.firstOrNull()

        val location = getCurrentLocation()
        Log.d(TAG, "Captured location: lat=${location?.first}, lng=${location?.second}")

        val scanResult = ScanResult(
            imagePath = imageFiles.first().absolutePath,
            organ = plants.predictedOrgans.firstOrNull()?.organ ?: "auto",
            bestMatch = plants.bestMatch,
            candidates = plants.toCandidates(),
            rawResponseJson = json.encodeToString(IdentifyPlantResponse.serializer(), plants),
            plantGbifId = topResult?.gbif?.id?.toString(),
            identificationScore = topResult?.score,
            latitude = location?.first,
            longitude = location?.second,
        )

        scanRepo.save(scanResult)
        Log.d(TAG, "Saved scan ${scanResult.id} to local DB")

        try {
            scanSyncManager.syncPending()
        } catch (e: Exception) {
            Log.w(TAG, "Post-save sync failed (will retry later)", e)
        }

        try {
            val storagePath = plantImageUploader.uploadScanImage(scanResult.imagePath, scanResult.id)
            if (storagePath != null) {
                scanRepo.setImageUrl(scanResult.id, storagePath)
                Log.d(TAG, "Uploaded scan image to $storagePath")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Scan image upload failed (will retry on next save)", e)
        }

        return scanResult
    }

    suspend fun requestAdditionalInfo(scanId: String, scientificName: String): PlantAiInfo {
        Log.d(TAG, "requestAdditionalInfo: scanId=$scanId name=$scientificName")
        val info = geminiRepo.getPlantInfo(scientificName)
        val infoJson = json.encodeToString(PlantAiInfo.serializer(), info)
        scanRepo.updateCandidateAiInfo(scanId, scientificName, infoJson)
        plantDetailsRepository.upsertIfHasGbif(scanId, scientificName, info)

        // Hook 2: catch-up generation for the rare flow where a plant was saved
        // before its AI info was fetched. Hook 1 (SavedPlantRepositoryImpl.save)
        // covers the typical flow where AI info is already cached.
        val gbifId = scanDao.getCandidateGbifId(scanId, scientificName)
        if (gbifId != null) {
            val savedPlant = savedPlantDao.findExisting(scanId, gbifId)
            if (savedPlant != null) {
                try {
                    careTaskRepository.generateForSavedPlant(savedPlant.id, info.care)
                } catch (e: Exception) {
                    Log.w(TAG, "requestAdditionalInfo: care task generation failed for ${savedPlant.id}", e)
                }
            }
        }

        return info
    }

    suspend fun getPlantOfTheDay(): PlantOfTheDay {
        val today = java.time.LocalDate.now().toString()

        val cached = plantOfTheDayDao.getForDate(today)

        if (cached != null) {
            Log.d(TAG, "getPlantOfTheDay: cache hit for $today")
            return runCatching {
                json.decodeFromString(PlantOfTheDay.serializer(), cached.plantJson)
            }.getOrElse {
                Log.w(TAG, "getPlantOfTheDay: cache parse failed, fetching fresh", it)
                null
            } ?: fetchAndCache(today)
        }

        Log.d(TAG, "getPlantOfTheDay: cache miss for $today, fetching from Gemini")
        return fetchAndCache(today)
    }

    private suspend fun fetchAndCache(date: String): PlantOfTheDay {
        val plant = geminiRepo.getPlantOfTheDay()
        try {
            val plantJson = json.encodeToString(PlantOfTheDay.serializer(), plant)
            plantOfTheDayDao.upsert(PlantOfTheDayEntity(cachedDate = date, plantJson = plantJson))
            Log.d(TAG, "getPlantOfTheDay: cached ${plant.scientificName} for $date")
        } catch (e: Exception) {
            Log.w(TAG, "getPlantOfTheDay: failed to write cache", e)
        }
        return plant
    }


    fun getPlantsFromLocal(): Flow<List<ScanResult>> = scanRepo.getAll()

    private suspend fun getCurrentLocation(): Pair<Double, Double>? {
        val hasFine = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) {
            Log.d(TAG, "Location permission not granted")
            return null
        }

        return try {
            suspendCancellableCoroutine { cont ->
                locationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    CancellationTokenSource().token,
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        cont.resume(location.latitude to location.longitude)
                    } else {
                        cont.resume(null)
                    }
                }.addOnFailureListener { e ->
                    Log.w(TAG, "Failed to get location", e)
                    cont.resume(null)
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException getting location", e)
            null
        }
    }
}
