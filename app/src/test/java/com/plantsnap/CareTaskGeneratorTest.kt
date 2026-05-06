package com.plantsnap

import com.plantsnap.data.local.model.CareTaskEntity
import com.plantsnap.domain.models.CareInfo
import com.plantsnap.domain.models.CareTaskType
import com.plantsnap.domain.services.CareTaskGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CareTaskGeneratorTest {

    private lateinit var generator: CareTaskGenerator
    private val plantId = "plant-1"
    private val now = 1_700_000_000_000L
    private val day = CareTaskGenerator.MILLIS_PER_DAY

    @Before
    fun setUp() {
        generator = CareTaskGenerator()
    }

    private fun fullCareInfo() = CareInfo(
        waterEveryDays = 7,
        fertilizeEveryDays = 30,
        mistEveryDays = 3,
        rotateEveryDays = 14,
        repotEveryDays = 365,
    )

    @Test
    fun `generates one row per task type when there are no existing rows`() {
        val rows = generator.generate(plantId, fullCareInfo(), existing = emptyList(), now = now)
        assertEquals(5, rows.size)
        assertEquals(setOf("WATER", "FERTILIZE", "MIST", "ROTATE", "REPOT"), rows.map { it.taskType }.toSet())
        rows.forEach {
            assertEquals(plantId, it.savedPlantId)
            assertTrue("expected enabled", it.enabled)
            assertFalse("expected userOverride=false", it.userOverride)
            assertEquals(now, it.createdAt)
            assertEquals(now, it.updatedAt)
            assertFalse("expected synced=false", it.synced)
        }
    }

    @Test
    fun `first generation gives the first enabled task an immediate due date`() {
        val rows = generator.generate(plantId, fullCareInfo(), existing = emptyList(), now = now)
        // WATER is first in canonical order, so it gets the "taste" treatment.
        val water = rows.first { it.taskType == "WATER" }
        assertEquals(now, water.nextDueAt)
        // Other tasks keep the standard "now + cadence" schedule.
        val mist = rows.first { it.taskType == "MIST" }
        assertEquals(now + 3 * day, mist.nextDueAt)
        val fertilize = rows.first { it.taskType == "FERTILIZE" }
        assertEquals(now + 30 * day, fertilize.nextDueAt)
    }

    @Test
    fun `first task taste falls back when WATER cadence is null`() {
        val info = CareInfo(fertilizeEveryDays = 30, mistEveryDays = 3)
        val rows = generator.generate(plantId, info, existing = emptyList(), now = now)
        val water = rows.first { it.taskType == "WATER" }
        assertFalse("WATER should be disabled when cadence is null", water.enabled)
        // FERTILIZE is first enabled in canonical order, so it gets the taste.
        val fertilize = rows.first { it.taskType == "FERTILIZE" }
        assertEquals(now, fertilize.nextDueAt)
        val mist = rows.first { it.taskType == "MIST" }
        assertEquals(now + 3 * day, mist.nextDueAt)
    }

    @Test
    fun `existing row with unchanged cadence preserves nextDueAt across regeneration`() {
        // Simulates Hook 2 firing after Hook 1 has already produced the taste row.
        val tasted = CareTaskEntity(
            id = "task-1",
            savedPlantId = plantId,
            taskType = "WATER",
            cadenceDays = 7,
            nextDueAt = now,
            lastCompletedAt = null,
            enabled = true,
            userOverride = false,
            createdAt = now,
            updatedAt = now,
            synced = false,
        )
        val rows = generator.generate(
            savedPlantId = plantId,
            careInfo = fullCareInfo(),
            existing = listOf(tasted),
            now = now + 1_000,
        )
        val water = rows.first { it.taskType == "WATER" }
        assertEquals("Taste must survive regen with same cadence", tasted, water)
    }

    @Test
    fun `null cadence produces a disabled row with cadenceDays zero`() {
        val info = CareInfo(waterEveryDays = 7) // others null
        val rows = generator.generate(plantId, info, existing = emptyList(), now = now)
        val mist = rows.first { it.taskType == "MIST" }
        assertFalse("mist should be disabled when Gemini returned null", mist.enabled)
        assertEquals(0, mist.cadenceDays)
    }

    @Test
    fun `out-of-range cadence is treated as null and disabled`() {
        // Negative water days, 9999 fertilize days — both outside the sanitize window.
        val info = CareInfo(waterEveryDays = -3, fertilizeEveryDays = 9999, mistEveryDays = 3)
        val rows = generator.generate(plantId, info, existing = emptyList(), now = now)
        val water = rows.first { it.taskType == "WATER" }
        assertFalse(water.enabled)
        assertEquals(0, water.cadenceDays)
        val fertilize = rows.first { it.taskType == "FERTILIZE" }
        assertFalse(fertilize.enabled)
        assertEquals(0, fertilize.cadenceDays)
        val mist = rows.first { it.taskType == "MIST" }
        assertTrue(mist.enabled)
        assertEquals(3, mist.cadenceDays)
    }

    @Test
    fun `userOverride row is preserved verbatim`() {
        val original = CareTaskEntity(
            id = "task-1",
            savedPlantId = plantId,
            taskType = "WATER",
            cadenceDays = 5,
            nextDueAt = now - 10 * day,
            lastCompletedAt = now - 3 * day,
            enabled = true,
            userOverride = true,
            createdAt = now - 100 * day,
            updatedAt = now - 50 * day,
            synced = true,
        )
        // Gemini suggests a different cadence — should be ignored because of override.
        val info = CareInfo(waterEveryDays = 14)
        val rows = generator.generate(plantId, info, existing = listOf(original), now = now)
        val water = rows.first { it.taskType == "WATER" }
        assertEquals(original, water)
    }

    @Test
    fun `existing row without override updates cadence and recomputes nextDueAt from lastCompletedAt`() {
        val lastDone = now - 2 * day
        val original = CareTaskEntity(
            id = "task-1",
            savedPlantId = plantId,
            taskType = "WATER",
            cadenceDays = 7,
            nextDueAt = lastDone + 7 * day,
            lastCompletedAt = lastDone,
            enabled = true,
            userOverride = false,
            createdAt = now - 100 * day,
            updatedAt = now - 50 * day,
            synced = true,
        )
        // Gemini now suggests 5 days.
        val info = CareInfo(waterEveryDays = 5)
        val rows = generator.generate(plantId, info, existing = listOf(original), now = now)
        val water = rows.first { it.taskType == "WATER" }
        assertEquals(5, water.cadenceDays)
        assertEquals(lastDone + 5 * day, water.nextDueAt)
        assertEquals(lastDone, water.lastCompletedAt)
        assertFalse("synced should flip to false on update", water.synced)
        assertEquals(now, water.updatedAt)
        assertEquals("task-1", water.id)
    }

    @Test
    fun `existing row with no lastCompletedAt uses createdAt as floor for nextDueAt`() {
        val created = now - 100 * day
        val original = CareTaskEntity(
            id = "task-1",
            savedPlantId = plantId,
            taskType = "FERTILIZE",
            cadenceDays = 30,
            nextDueAt = created + 30 * day,
            lastCompletedAt = null,
            enabled = true,
            userOverride = false,
            createdAt = created,
            updatedAt = created,
            synced = true,
        )
        val info = CareInfo(fertilizeEveryDays = 60)
        val rows = generator.generate(plantId, info, existing = listOf(original), now = now)
        val fertilize = rows.first { it.taskType == "FERTILIZE" }
        assertEquals(60, fertilize.cadenceDays)
        assertEquals(created + 60 * day, fertilize.nextDueAt)
    }

    @Test
    fun `existing row identical to incoming is returned unchanged`() {
        val info = fullCareInfo()
        val first = generator.generate(plantId, info, existing = emptyList(), now = now)
        // Re-running with the same input must be idempotent.
        val again = generator.generate(plantId, info, existing = first, now = now + 1)
        assertEquals(first, again)
    }

    @Test
    fun `null careInfo yields disabled rows`() {
        val rows = generator.generate(plantId, careInfo = null, existing = emptyList(), now = now)
        assertEquals(5, rows.size)
        rows.forEach { assertFalse("expected ${it.taskType} disabled", it.enabled) }
    }

    @Test
    fun `each task type maps to the correct cadence field`() {
        val info = CareInfo(
            waterEveryDays = 1,
            fertilizeEveryDays = 2,
            mistEveryDays = 3,
            rotateEveryDays = 4,
            repotEveryDays = 5,
        )
        val rows = generator.generate(plantId, info, existing = emptyList(), now = now).associateBy { it.taskType }
        assertEquals(1, rows.getValue("WATER").cadenceDays)
        assertEquals(2, rows.getValue("FERTILIZE").cadenceDays)
        assertEquals(3, rows.getValue("MIST").cadenceDays)
        assertEquals(4, rows.getValue("ROTATE").cadenceDays)
        assertEquals(5, rows.getValue("REPOT").cadenceDays)
    }

    @Test
    fun `defaultCadenceDays returns sensible values for each type`() {
        assertNotNull(CareTaskType.defaultCadenceDays(CareTaskType.WATER))
        assertEquals(7, CareTaskType.defaultCadenceDays(CareTaskType.WATER))
        assertEquals(30, CareTaskType.defaultCadenceDays(CareTaskType.FERTILIZE))
        assertEquals(3, CareTaskType.defaultCadenceDays(CareTaskType.MIST))
        assertEquals(14, CareTaskType.defaultCadenceDays(CareTaskType.ROTATE))
        assertEquals(365, CareTaskType.defaultCadenceDays(CareTaskType.REPOT))
    }
}
