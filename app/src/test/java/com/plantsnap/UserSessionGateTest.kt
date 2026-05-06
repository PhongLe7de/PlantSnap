package com.plantsnap

import android.util.Log
import com.plantsnap.data.local.CareTaskDao
import com.plantsnap.data.local.SavedPlantDao
import com.plantsnap.data.local.ScanDao
import com.plantsnap.data.sync.LastUserIdStore
import com.plantsnap.data.sync.UserSessionGate
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserSessionGateTest {

    private class FakeLastUserIdStore(initial: String? = null) : LastUserIdStore {
        var value: String? = initial
        override suspend fun get(): String? = value
        override suspend fun set(id: String) { value = id }
    }

    private lateinit var scanDao: ScanDao
    private lateinit var savedPlantDao: SavedPlantDao
    private lateinit var careTaskDao: CareTaskDao

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.i(any(), any<String>()) } returns 0
        scanDao = mockk(relaxed = true)
        savedPlantDao = mockk(relaxed = true)
        careTaskDao = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    private fun gate(store: LastUserIdStore) =
        UserSessionGate(store, scanDao, savedPlantDao, careTaskDao)

    @Test
    fun `first reconcile persists userId without wiping`() = runTest {
        val store = FakeLastUserIdStore()

        gate(store).reconcile("jaakko")

        assertEquals("jaakko", store.value)
        coVerify(exactly = 0) { scanDao.deleteAll() }
        coVerify(exactly = 0) { savedPlantDao.deleteAll() }
        coVerify(exactly = 0) { careTaskDao.deleteAll() }
    }

    @Test
    fun `same user does not wipe`() = runTest {
        val store = FakeLastUserIdStore(initial = "jaakko")

        gate(store).reconcile("jaakko")

        assertEquals("jaakko", store.value)
        coVerify(exactly = 0) { scanDao.deleteAll() }
        coVerify(exactly = 0) { savedPlantDao.deleteAll() }
        coVerify(exactly = 0) { careTaskDao.deleteAll() }
    }

    @Test
    fun `different user wipes user-scoped tables and persists new id`() = runTest {
        val store = FakeLastUserIdStore(initial = "jaakko")

        gate(store).reconcile("pertti")

        assertEquals("pertti", store.value)
        // FK chain: care_tasks → saved_plants → scans must be deleted in this order.
        coVerifyOrder {
            careTaskDao.deleteAll()
            savedPlantDao.deleteAll()
            scanDao.deleteAll()
        }
    }

    @Test
    fun `concurrent reconcile calls wipe at most once`() = runTest {
        val store = FakeLastUserIdStore(initial = "jaakko")
        val g = gate(store)

        listOf(
            async { g.reconcile("pertti") },
            async { g.reconcile("pertti") },
            async { g.reconcile("pertti") },
        ).awaitAll()

        assertEquals("pertti", store.value)
        coVerify(exactly = 1) { careTaskDao.deleteAll() }
        coVerify(exactly = 1) { savedPlantDao.deleteAll() }
        coVerify(exactly = 1) { scanDao.deleteAll() }
    }
}
