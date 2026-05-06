package com.plantsnap

import android.util.Log
import com.plantsnap.data.local.PlantDetailsDao
import com.plantsnap.data.local.SavedPlantDao
import com.plantsnap.data.local.ScanDao
import com.plantsnap.data.local.model.SavedPlantEntity
import com.plantsnap.data.local.model.SavedPlantWithDetails
import com.plantsnap.data.repository.SavedPlantRepositoryImpl
import com.plantsnap.domain.repository.CareTaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SavedPlantRepositoryImplTest {

    private val dao: SavedPlantDao = mockk(relaxed = true)
    private val plantDetailsDao: PlantDetailsDao = mockk(relaxed = true)
    private val scanDao: ScanDao = mockk(relaxed = true)
    private val careTaskRepository: CareTaskRepository = mockk(relaxed = true)
    private val json: Json = Json { ignoreUnknownKeys = true }

    private lateinit var repo: SavedPlantRepositoryImpl

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>(), any()) } returns 0
        repo = SavedPlantRepositoryImpl(dao, plantDetailsDao, scanDao, careTaskRepository, json)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    private fun makeEntity(
        id: String = "saved-1",
        plantGbifId: Long = 42L,
        nickname: String = "Monty",
        isFavourite: Boolean = false,
        lastWateredAt: Long? = null,
    ) = SavedPlantEntity(
        id = id,
        userId = null,
        plantGbifId = plantGbifId,
        originalScanId = "scan-1",
        nickname = nickname,
        imageUrl = null,
        isArchived = false,
        isFavourite = isFavourite,
        lastWateredAt = lastWateredAt,
        createdAt = 1_000L,
        synced = true,
    )

    @Test
    fun `observeById maps entity row to domain model`() = runTest {
        val entity = makeEntity(nickname = "Figgy", isFavourite = true, lastWateredAt = 999L)
        val row = SavedPlantWithDetails(saved = entity, scientificName = "Ficus lyrata")
        coEvery { dao.observeWithDetailsById("saved-1") } returns flowOf(row)

        val result = repo.observeById("saved-1").first()

        assertEquals("saved-1", result?.id)
        assertEquals("Figgy", result?.nickname)
        assertEquals(true, result?.isFavourite)
        assertEquals(999L, result?.lastWateredAt)
        assertEquals("Ficus lyrata", result?.plant?.scientificName)
    }

    @Test
    fun `observeById emits null when row not found`() = runTest {
        coEvery { dao.observeWithDetailsById("missing") } returns flowOf(null)

        val result = repo.observeById("missing").first()

        assertNull(result)
    }

    @Test
    fun `updateNickname delegates to dao`() = runTest {
        repo.updateNickname("saved-1", "  Monty  ")
        coVerify(exactly = 1) { dao.updateNickname("saved-1", "  Monty  ") }
    }

    @Test
    fun `updateFavourite delegates to dao`() = runTest {
        repo.updateFavourite("saved-1", true)
        coVerify(exactly = 1) { dao.updateFavourite("saved-1", true) }
    }

    @Test
    fun `updateLastWatered delegates to dao with timestamp`() = runTest {
        repo.updateLastWatered("saved-1", 1_700_000_000L)
        coVerify(exactly = 1) { dao.updateLastWatered("saved-1", 1_700_000_000L) }
    }

    @Test
    fun `updateLastWatered delegates to dao with null`() = runTest {
        repo.updateLastWatered("saved-1", null)
        coVerify(exactly = 1) { dao.updateLastWatered("saved-1", null) }
    }

    @Test
    fun `updateLastWateredBulk early-returns and skips dao on empty list`() = runTest {
        repo.updateLastWateredBulk(emptyList(), 1L)
        coVerify(exactly = 0) { dao.updateLastWateredBulk(any(), any()) }
    }

    @Test
    fun `updateLastWateredBulk delegates to dao when ids non-empty`() = runTest {
        val ids = listOf("a", "b", "c")
        repo.updateLastWateredBulk(ids, 1_700_000_000L)
        coVerify(exactly = 1) { dao.updateLastWateredBulk(ids, 1_700_000_000L) }
    }

    @Test
    fun `updateLastWateredBulk passes null timestamp through`() = runTest {
        val ids = listOf("a")
        repo.updateLastWateredBulk(ids, null)
        coVerify(exactly = 1) { dao.updateLastWateredBulk(ids, null) }
    }
}
