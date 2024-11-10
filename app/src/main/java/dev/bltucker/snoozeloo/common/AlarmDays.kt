package dev.bltucker.snoozeloo.common

import java.time.DayOfWeek

object AlarmDays {

    private const val MONDAY    = 1L shl 0
    private const val TUESDAY   = 1L shl 1
    private const val WEDNESDAY = 1L shl 2
    private const val THURSDAY  = 1L shl 3
    private const val FRIDAY    = 1L shl 4
    private const val SATURDAY  = 1L shl 5
    private const val SUNDAY    = 1L shl 6

    fun fromDaysList(days: List<DayOfWeek>): Long {
        var bitmask = 0L
        days.forEach { day ->
            bitmask = bitmask or when (day) {
                DayOfWeek.MONDAY -> MONDAY
                DayOfWeek.TUESDAY -> TUESDAY
                DayOfWeek.WEDNESDAY -> WEDNESDAY
                DayOfWeek.THURSDAY -> THURSDAY
                DayOfWeek.FRIDAY -> FRIDAY
                DayOfWeek.SATURDAY -> SATURDAY
                DayOfWeek.SUNDAY -> SUNDAY
            }
        }
        return bitmask
    }

    fun toDaysList(bitmask: Long): List<DayOfWeek> {
        val days = mutableListOf<DayOfWeek>()
        if (bitmask and MONDAY != 0L) days.add(DayOfWeek.MONDAY)
        if (bitmask and TUESDAY != 0L) days.add(DayOfWeek.TUESDAY)
        if (bitmask and WEDNESDAY != 0L) days.add(DayOfWeek.WEDNESDAY)
        if (bitmask and THURSDAY != 0L) days.add(DayOfWeek.THURSDAY)
        if (bitmask and FRIDAY != 0L) days.add(DayOfWeek.FRIDAY)
        if (bitmask and SATURDAY != 0L) days.add(DayOfWeek.SATURDAY)
        if (bitmask and SUNDAY != 0L) days.add(DayOfWeek.SUNDAY)
        return days
    }

    fun isEnabled(bitmask: Long, day: DayOfWeek): Boolean {
        val bitFlag = when (day) {
            DayOfWeek.MONDAY -> MONDAY
            DayOfWeek.TUESDAY -> TUESDAY
            DayOfWeek.WEDNESDAY -> WEDNESDAY
            DayOfWeek.THURSDAY -> THURSDAY
            DayOfWeek.FRIDAY -> FRIDAY
            DayOfWeek.SATURDAY -> SATURDAY
            DayOfWeek.SUNDAY -> SUNDAY
        }
        return bitmask and bitFlag != 0L
    }
}

