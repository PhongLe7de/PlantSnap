package com.plantsnap

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.plantsnap.data.local.PlantOfTheDayDao
import com.plantsnap.data.local.SavedPlantDao
import com.plantsnap.data.local.ScanDao
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.plantsnap.data.plantnet.IdentifyPlantResponse
import com.plantsnap.data.plantnet.Iucn
import com.plantsnap.data.plantnet.PredictedOrgan
import com.plantsnap.data.plantnet.Query
import com.plantsnap.data.plantnet.Result
import com.plantsnap.data.plantnet.Species
import com.plantsnap.data.plantnet.Taxon
import com.plantsnap.data.storage.PlantImageUploader
import com.plantsnap.data.sync.ScanSyncManager
import com.plantsnap.domain.repository.CareTaskRepository
import com.plantsnap.domain.repository.GeminiRepository
import com.plantsnap.domain.repository.PlantDetailsRepository
import com.plantsnap.domain.repository.PlantNetRepository
import com.plantsnap.domain.repository.ScanRepository
import com.plantsnap.domain.services.PlantService
import kotlinx.serialization.json.Json
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.File

class PlantServiceTest {

    private lateinit var context: Context
    private lateinit var plantNetRepo: PlantNetRepository
    private lateinit var scanRepo: ScanRepository
    private lateinit var geminiRepo: GeminiRepository
    private lateinit var scanSyncManager: ScanSyncManager
    private lateinit var plantDetailsRepository: PlantDetailsRepository
    private lateinit var plantImageUploader: PlantImageUploader
    private lateinit var plantOfTheDayDao: PlantOfTheDayDao
    private lateinit var savedPlantDao: SavedPlantDao
    private lateinit var scanDao: ScanDao
    private lateinit var careTaskRepository: CareTaskRepository
    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var service: PlantService

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>(), any()) } returns 0

        mockkStatic(LocationServices::class)
        every { LocationServices.getFusedLocationProviderClient(any<Context>()) } returns mockk(relaxed = true)

        mockkStatic(ContextCompat::class)
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_DENIED

        context = mockk(relaxed = true)
        plantNetRepo = mockk()
        scanRepo = mockk()
        geminiRepo = mockk()
        plantOfTheDayDao = mockk()
        scanSyncManager = mockk(relaxed = true)
        plantDetailsRepository = mockk(relaxed = true)
        plantImageUploader = mockk(relaxed = true)
        savedPlantDao = mockk(relaxed = true)
        scanDao = mockk(relaxed = true)
        careTaskRepository = mockk(relaxed = true)
        service = PlantService(
            context,
            plantNetRepo,
            geminiRepo,
            scanRepo,
            scanSyncManager,
            plantDetailsRepository,
            plantImageUploader,
            plantOfTheDayDao,
            savedPlantDao,
            scanDao,
            careTaskRepository,
            json,
        )
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
        unmockkStatic(LocationServices::class)
        unmockkStatic(ContextCompat::class)
    }

    private fun mockFile(path: String = "/test/photo.jpg", exists: Boolean = true) = mockk<File> {
        every { this@mockk.exists() } returns exists
        every { absolutePath } returns path
        every { name } returns path.substringAfterLast("/")
    }

    private fun fakeResponse(
        bestMatch: String = "Monstera deliciosa",
        organ: String = "leaf",
        iucn: Iucn? = null
    ) = IdentifyPlantResponse(
        query = Query(
            project = "all",
            images = listOf("photo.jpg"),
            organs = listOf(organ),
            includeRelatedImages = false,
            noReject = false
        ),
        language = "en",
        preferedReferential = "the-plant-list",
        bestMatch = bestMatch,
        results = listOf(
            Result(
                score = 0.95,
                species = Species(
                    scientificName = "Monstera deliciosa",
                    scientificNameAuthorship = "Liebm.",
                    scientificNameFull = "Monstera deliciosa Liebm.",
                    genus = Taxon("Monstera", "", "Monstera Adans."),
                    family = Taxon("Araceae", "", "Araceae Juss."),
                    commonNames = listOf("Swiss Cheese Plant")
                ),
                iucn = iucn
            )
        ),
        remainingIdentificationRequests = 100,
        version = "2.0",
        predictedOrgans = listOf(
            PredictedOrgan(image = "photo.jpg", filename = "photo.jpg", organ = organ, score = 0.98)
        )
    )

    // region Validation

    @Test
    fun `empty imageFiles list throws`() = runTest {
        try {
            service.identifyPlantAndSaveToLocal(emptyList(), emptyList())
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
        }
    }

    @Test
    fun `mismatched imageFiles and organs sizes throws`() = runTest {
        try {
            service.identifyPlantAndSaveToLocal(
                listOf(mockFile()),
                listOf("leaf", "flower")
            )
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
        }
    }

    @Test
    fun `non-existent file throws`() = runTest {
        try {
            service.identifyPlantAndSaveToLocal(
                listOf(mockFile(exists = false)),
                listOf("leaf")
            )
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
        }
    }

    // endregion

    // region Happy path

    @Test
    fun `maps bestMatch from response`() = runTest {
        coEvery { plantNetRepo.identifyPlantFromMultipleImages(any(), any()) } returns fakeResponse(
            bestMatch = "Rosa canina"
        )
        coEvery { scanRepo.save(any()) } just Runs

        val result = service.identifyPlantAndSaveToLocal(listOf(mockFile()), listOf("leaf"))

        assertEquals("Rosa canina", result.bestMatch)
    }

    @Test
    fun `maps organ from predictedOrgans`() = runTest {
        coEvery { plantNetRepo.identifyPlantFromMultipleImages(any(), any()) } returns fakeResponse(
            organ = "flower"
        )
        coEvery { scanRepo.save(any()) } just Runs

        val result = service.identifyPlantAndSaveToLocal(listOf(mockFile()), listOf("flower"))

        assertEquals("flower", result.organ)
    }

    @Test
    fun `falls back to auto when predictedOrgans is empty`() = runTest {
        val response = fakeResponse().copy(predictedOrgans = emptyList())
        coEvery { plantNetRepo.identifyPlantFromMultipleImages(any(), any()) } returns response
        coEvery { scanRepo.save(any()) } just Runs

        val result = service.identifyPlantAndSaveToLocal(listOf(mockFile()), listOf("leaf"))

        assertEquals("auto", result.organ)
    }

    @Test
    fun `maps candidate score from Double to Float`() = runTest {
        coEvery {
            plantNetRepo.identifyPlantFromMultipleImages(
                any(),
                any()
            )
        } returns fakeResponse()
        coEvery { scanRepo.save(any()) } just Runs

        val result = service.identifyPlantAndSaveToLocal(listOf(mockFile()), listOf("leaf"))

        assertEquals(0.95f, result.candidates.first().score)
    }

    @Test
    fun `maps iucnCategory when present`() = runTest {
        coEvery { plantNetRepo.identifyPlantFromMultipleImages(any(), any()) } returns
                fakeResponse(iucn = Iucn(id = "12345", category = "EN"))
        coEvery { scanRepo.save(any()) } just Runs

        val result = service.identifyPlantAndSaveToLocal(listOf(mockFile()), listOf("leaf"))

        assertEquals("EN", result.candidates.first().iucnCategory)
    }

    @Test
    fun `iucnCategory is null when iucn field absent`() = runTest {
        coEvery { plantNetRepo.identifyPlantFromMultipleImages(any(), any()) } returns fakeResponse(
            iucn = null
        )
        coEvery { scanRepo.save(any()) } just Runs

        val result = service.identifyPlantAndSaveToLocal(listOf(mockFile()), listOf("leaf"))

        assertNull(result.candidates.first().iucnCategory)
    }

    @Test
    fun `saves scan to local repository`() = runTest {
        coEvery {
            plantNetRepo.identifyPlantFromMultipleImages(
                any(),
                any()
            )
        } returns fakeResponse()
        coEvery { scanRepo.save(any()) } just Runs

        service.identifyPlantAndSaveToLocal(listOf(mockFile()), listOf("leaf"))

        coVerify(exactly = 1) { scanRepo.save(any()) }
    }

    @Test
    fun `returns the scan result`() = runTest {
        coEvery {
            plantNetRepo.identifyPlantFromMultipleImages(
                any(),
                any()
            )
        } returns fakeResponse()
        coEvery { scanRepo.save(any()) } just Runs

        val result = service.identifyPlantAndSaveToLocal(listOf(mockFile()), listOf("leaf"))

        assertEquals("Monstera deliciosa", result.bestMatch)
        assertEquals(1, result.candidates.size)
    }

    // endregion
}
