package com.plantsnap

import android.util.Log
import com.plantsnap.data.storage.PlantImageUrlResolver
import com.plantsnap.data.sync.SavedPlantSyncManager
import com.plantsnap.domain.repository.SavedPlantRepository
import com.plantsnap.ui.screens.garden.MyGardenViewModel
import com.plantsnap.ui.screens.identify.camera.CapturedPhotosHolder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyGardenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val repo: SavedPlantRepository = mockk(relaxed = true) {
        every { observeAll() } returns flowOf(emptyList())
    }
    private val imageUrlResolver: PlantImageUrlResolver = mockk {
        coEvery { resolveAll(any()) } returns emptyMap()
    }
    private val photosHolder: CapturedPhotosHolder = mockk(relaxed = true)
    private val syncManager: SavedPlantSyncManager = mockk(relaxed = true)

    private lateinit var viewModel: MyGardenViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>(), any()) } returns 0
        viewModel = MyGardenViewModel(repo, imageUrlResolver, photosHolder, syncManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    @Test
    fun `setWateredToday with watered=true calls repo with non-null timestamp`() = runTest {
        viewModel.setWateredToday("plant-1", watered = true)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repo.updateLastWatered("plant-1", match { it != null && it > 0L })
        }
        coVerify(exactly = 1) { syncManager.syncPending() }
    }

    @Test
    fun `setWateredToday with watered=false calls repo with null timestamp`() = runTest {
        viewModel.setWateredToday("plant-1", watered = false)
        advanceUntilIdle()

        coVerify(exactly = 1) { repo.updateLastWatered("plant-1", null) }
    }

    @Test
    fun `setAllWateredToday with empty list does not call repo or sync`() = runTest {
        viewModel.setAllWateredToday(emptyList(), watered = true)
        advanceUntilIdle()

        coVerify(exactly = 0) { repo.updateLastWateredBulk(any(), any()) }
        coVerify(exactly = 0) { syncManager.syncPending() }
    }

    @Test
    fun `setAllWateredToday calls bulk DAO once with all ids and timestamp`() = runTest {
        val ids = listOf("a", "b", "c")

        viewModel.setAllWateredToday(ids, watered = true)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repo.updateLastWateredBulk(ids, match { it != null && it > 0L })
        }
        coVerify(exactly = 1) { syncManager.syncPending() }
    }

    @Test
    fun `setAllWateredToday with watered=false passes null timestamp`() = runTest {
        val ids = listOf("a", "b")

        viewModel.setAllWateredToday(ids, watered = false)
        advanceUntilIdle()

        coVerify(exactly = 1) { repo.updateLastWateredBulk(ids, null) }
    }

    @Test
    fun `setAllWateredToday concurrent invocation is ignored while in progress`() = runTest {
        val ids = listOf("a", "b")
        coEvery { repo.updateLastWateredBulk(ids, any()) } coAnswers {
            kotlinx.coroutines.delay(10)
        }

        viewModel.setAllWateredToday(ids, watered = true)
        viewModel.setAllWateredToday(ids, watered = false)
        advanceUntilIdle()

        coVerify(exactly = 1) { repo.updateLastWateredBulk(ids, any()) }
    }

    @Test
    fun `setAllWateredToday clears inProgress flag in finally on exception`() = runTest {
        val ids = listOf("a")
        coEvery { repo.updateLastWateredBulk(ids, any()) } throws RuntimeException("boom")

        viewModel.setAllWateredToday(ids, watered = true)
        advanceUntilIdle()

        assertFalse(viewModel.isWateringAll.value)
    }
}
