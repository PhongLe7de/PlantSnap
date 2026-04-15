package com.plantsnap

import com.plantsnap.data.plantnet.IdentifyPlantResponse
import com.plantsnap.data.plantnet.PlantNetApi
import com.plantsnap.data.plantnet.PredictedOrgan
import com.plantsnap.data.plantnet.Query
import com.plantsnap.data.plantnet.Result
import com.plantsnap.data.plantnet.Species
import com.plantsnap.data.plantnet.Taxon
import com.plantsnap.data.repository.PlantNetRepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.IOException

class PlantNetRepositoryImplTest {

    private lateinit var api: PlantNetApi
    private lateinit var repository: PlantNetRepositoryImpl

    private val tempFiles = mutableListOf<File>()

    @Before
    fun setUp() {
        api = mockk()
        repository = PlantNetRepositoryImpl(api)
    }

    @After
    fun tearDown() {
        tempFiles.forEach { it.delete() }
    }

    private fun tempFile(): File =
        File.createTempFile("plantsnap_test", ".jpg").also { tempFiles += it }

    private fun fakeResponse() = IdentifyPlantResponse(
        query = Query(
            project = "all",
            images = listOf("test.jpg"),
            organs = listOf("leaf"),
            includeRelatedImages = false,
            noReject = false
        ),
        language = "en",
        preferedReferential = "the-plant-list",
        bestMatch = "Monstera deliciosa",
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
                )
            )
        ),
        remainingIdentificationRequests = 100,
        version = "2.0",
        predictedOrgans = listOf(
            PredictedOrgan(image = "test.jpg", filename = "test.jpg", organ = "leaf", score = 0.98)
        )
    )

    // region identifyPlant

    @Test
    fun `identifyPlant calls api once`() = runTest {
        val file = tempFile()
        coEvery {
            api.identify(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns fakeResponse()

        repository.identifyPlant(file, "leaf")

        coVerify(exactly = 1) {
            api.identify(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `identifyPlant returns api response`() = runTest {
        val file = tempFile()
        val expected = fakeResponse()
        coEvery {
            api.identify(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns expected

        val result = repository.identifyPlant(file, "leaf")

        assertEquals(expected, result)
    }

    // endregion

    // region identifyPlantFromMultipleImages

    @Test
    fun `identifyPlantFromMultipleImages passes one part per image`() = runTest {
        val files = listOf(tempFile(), tempFile())
        val imageSlot = slot<List<MultipartBody.Part>>()
        coEvery {
            api.identify(
                any(),
                any(),
                capture(imageSlot),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns fakeResponse()

        repository.identifyPlantFromMultipleImages(files, listOf("leaf", "flower"))

        assertEquals(2, imageSlot.captured.size)
    }

    @Test
    fun `identifyPlantFromMultipleImages passes one organ part per image`() = runTest {
        val files = listOf(tempFile(), tempFile())
        val organSlot = slot<List<MultipartBody.Part>>()
        coEvery {
            api.identify(
                any(),
                any(),
                any(),
                capture(organSlot),
                any(),
                any(),
                any(),
                any()
            )
        } returns fakeResponse()

        repository.identifyPlantFromMultipleImages(files, listOf("leaf", "flower"))

        assertEquals(2, organSlot.captured.size)
    }

    @Test
    fun `identifyPlantFromMultipleImages defaults null organs to same count as images`() = runTest {
        val files = listOf(tempFile(), tempFile())
        val organSlot = slot<List<MultipartBody.Part>>()
        coEvery {
            api.identify(
                any(),
                any(),
                any(),
                capture(organSlot),
                any(),
                any(),
                any(),
                any()
            )
        } returns fakeResponse()

        repository.identifyPlantFromMultipleImages(files, null)

        assertEquals(2, organSlot.captured.size)
    }

    @Test
    fun `identifyPlantFromMultipleImages sets includeRelatedImages to true`() = runTest {
        val file = tempFile()
        coEvery {
            api.identify(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns fakeResponse()

        repository.identifyPlantFromMultipleImages(listOf(file), listOf("leaf"))

        coVerify {
            api.identify(
                any(),
                any(),
                any(),
                any(),
                includeRelatedImages = true,
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `identifyPlantFromMultipleImages returns api response`() = runTest {
        val file = tempFile()
        val expected = fakeResponse()
        coEvery {
            api.identify(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns expected

        val result = repository.identifyPlantFromMultipleImages(listOf(file), listOf("leaf"))

        assertEquals(expected, result)
    }

    // endregion

    // region Error propagation

    @Test
    fun `HttpException from api propagates to caller`() = runTest {
        val file = tempFile()
        coEvery { api.identify(any(), any(), any(), any(), any(), any(), any(), any()) } throws
                HttpException(Response.error<Any>(404, "".toResponseBody()))

        try {
            repository.identifyPlant(file, null)
            fail("Expected HttpException")
        } catch (e: HttpException) {
            assertEquals(404, e.code())
        }
    }

    @Test
    fun `IOException from api propagates to caller`() = runTest {
        val file = tempFile()
        coEvery { api.identify(any(), any(), any(), any(), any(), any(), any(), any()) } throws
                IOException("Network unreachable")

        try {
            repository.identifyPlant(file, null)
            fail("Expected IOException")
        } catch (_: IOException) {
        }
    }

    // endregion
}
