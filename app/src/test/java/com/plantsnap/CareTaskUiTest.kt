package com.plantsnap

import com.plantsnap.ui.screens.garden.DueLabel
import com.plantsnap.ui.screens.garden.dueLabelFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class CareTaskUiTest {

    private val zone: ZoneId = ZoneId.of("Europe/Helsinki")
    private val day = 86_400_000L

    private fun midday(date: LocalDate = LocalDate.of(2026, 5, 2)): Long =
        date.atTime(LocalTime.NOON).atZone(zone).toInstant().toEpochMilli()

    @Test
    fun `due exactly at noon today is DueToday`() {
        val now = midday()
        val result = dueLabelFor(nextDueAt = now, now = now, zoneId = zone)
        assertEquals(DueLabel.DueToday, result)
    }

    @Test
    fun `due at start of today is DueToday`() {
        val today = LocalDate.of(2026, 5, 2)
        val now = midday(today)
        val startOfToday = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val result = dueLabelFor(nextDueAt = startOfToday, now = now, zoneId = zone)
        assertEquals(DueLabel.DueToday, result)
    }

    @Test
    fun `due at end of today is DueToday`() {
        val today = LocalDate.of(2026, 5, 2)
        val now = midday(today)
        val endOfToday = today.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
        val result = dueLabelFor(nextDueAt = endOfToday, now = now, zoneId = zone)
        assertEquals(DueLabel.DueToday, result)
    }

    @Test
    fun `due 1 second before midnight yesterday is Overdue 1`() {
        val today = LocalDate.of(2026, 5, 2)
        val now = midday(today)
        val justBeforeMidnight = today.atStartOfDay(zone).toInstant().toEpochMilli() - 1_000
        val result = dueLabelFor(nextDueAt = justBeforeMidnight, now = now, zoneId = zone)
        assertTrue("expected Overdue, got $result", result is DueLabel.Overdue)
        assertEquals(1, (result as DueLabel.Overdue).daysLate)
    }

    @Test
    fun `due 5 days ago is Overdue 5`() {
        val today = LocalDate.of(2026, 5, 2)
        val now = midday(today)
        val fiveDaysAgo = midday(today.minusDays(5))
        val result = dueLabelFor(nextDueAt = fiveDaysAgo, now = now, zoneId = zone)
        assertTrue(result is DueLabel.Overdue)
        assertEquals(5, (result as DueLabel.Overdue).daysLate)
    }

    @Test
    fun `due tomorrow morning is Upcoming 1`() {
        val today = LocalDate.of(2026, 5, 2)
        val now = midday(today)
        val tomorrowMorning = today.plusDays(1).atTime(8, 0).atZone(zone).toInstant().toEpochMilli()
        val result = dueLabelFor(nextDueAt = tomorrowMorning, now = now, zoneId = zone)
        assertTrue("expected Upcoming, got $result", result is DueLabel.Upcoming)
        assertEquals(1, (result as DueLabel.Upcoming).daysUntil)
    }

    @Test
    fun `due 7 days out is Upcoming 7`() {
        val today = LocalDate.of(2026, 5, 2)
        val now = midday(today)
        val inSeven = midday(today.plusDays(7))
        val result = dueLabelFor(nextDueAt = inSeven, now = now, zoneId = zone)
        assertTrue(result is DueLabel.Upcoming)
        assertEquals(7, (result as DueLabel.Upcoming).daysUntil)
    }
}
