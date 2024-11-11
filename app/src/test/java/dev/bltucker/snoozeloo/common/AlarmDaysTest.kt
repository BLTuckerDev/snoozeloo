package dev.bltucker.snoozeloo.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

class AlarmDaysTest {

    @Test
    fun `fromDaysList with empty list returns zero`() {
        val result = AlarmDays.fromDaysList(emptyList())
        assertEquals(0L, result)
    }

    @Test
    fun `fromDaysList with single day returns correct bitmask`() {
        val mondayOnly = AlarmDays.fromDaysList(listOf(DayOfWeek.MONDAY))
        assertEquals(1L, mondayOnly) // 0b001

        val wednesdayOnly = AlarmDays.fromDaysList(listOf(DayOfWeek.WEDNESDAY))
        assertEquals(4L, wednesdayOnly) // 0b100
    }

    @Test
    fun `fromDaysList with multiple days returns correct bitmask`() {
        val days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        val result = AlarmDays.fromDaysList(days)
        assertEquals(21L, result) // 0b010101
    }

    @Test
    fun `fromDaysList with all days returns correct bitmask`() {
        val allDays = DayOfWeek.entries
        val result = AlarmDays.fromDaysList(allDays)
        assertEquals(127L, result) // 0b1111111
    }

    @Test
    fun `toDaysList with zero returns empty list`() {
        val result = AlarmDays.toDaysList(0L)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `toDaysList with single bit returns correct day`() {
        val mondayResult = AlarmDays.toDaysList(1L)
        assertEquals(listOf(DayOfWeek.MONDAY), mondayResult)

        val wednesdayResult = AlarmDays.toDaysList(4L)
        assertEquals(listOf(DayOfWeek.WEDNESDAY), wednesdayResult)
    }

    @Test
    fun `toDaysList with multiple bits returns correct days in order`() {
        // Monday (1) + Wednesday (4) + Friday (16) = 21
        val result = AlarmDays.toDaysList(21L)
        assertEquals(
            listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            result
        )
    }

    @Test
    fun `toDaysList with all bits set returns all days in order`() {
        val result = AlarmDays.toDaysList(127L) // 0b1111111
        assertEquals(DayOfWeek.values().toList(), result)
    }

    @Test
    fun `isEnabled returns true for enabled days`() {
        // Set Monday (1) and Friday (16)
        val bitmask = 17L // 0b10001

        assertTrue(AlarmDays.isEnabled(bitmask, DayOfWeek.MONDAY))
        assertTrue(AlarmDays.isEnabled(bitmask, DayOfWeek.FRIDAY))
    }

    @Test
    fun `isEnabled returns false for disabled days`() {
        // Set Monday (1) and Friday (16)
        val bitmask = 17L // 0b10001

        assertFalse(AlarmDays.isEnabled(bitmask, DayOfWeek.TUESDAY))
        assertFalse(AlarmDays.isEnabled(bitmask, DayOfWeek.WEDNESDAY))
        assertFalse(AlarmDays.isEnabled(bitmask, DayOfWeek.THURSDAY))
        assertFalse(AlarmDays.isEnabled(bitmask, DayOfWeek.SATURDAY))
        assertFalse(AlarmDays.isEnabled(bitmask, DayOfWeek.SUNDAY))
    }

    @Test
    fun `roundtrip conversion preserves days`() {
        val originalDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        val bitmask = AlarmDays.fromDaysList(originalDays)
        val resultDays = AlarmDays.toDaysList(bitmask)
        assertEquals(originalDays, resultDays)
    }

    @Test
    fun `consecutive days are handled correctly`() {
        val consecutiveDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY)
        val bitmask = AlarmDays.fromDaysList(consecutiveDays)
        val resultDays = AlarmDays.toDaysList(bitmask)
        assertEquals(consecutiveDays, resultDays)
    }
}