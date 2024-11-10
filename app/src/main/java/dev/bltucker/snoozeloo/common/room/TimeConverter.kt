package dev.bltucker.snoozeloo.common.room

import androidx.room.TypeConverter
import java.time.LocalTime

/**
 * Type converters for Room
 */
class TimeConverter {
    @TypeConverter
    fun fromLocalTime(time: LocalTime): String = time.toString()

    @TypeConverter
    fun toLocalTime(timeStr: String): LocalTime = LocalTime.parse(timeStr)
}