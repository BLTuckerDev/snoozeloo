package dev.bltucker.snoozeloo.common.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String?,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean,

    // Repeat settings - using a BitSet encoded as Long for efficient storage
    // Each bit represents a day of the week (Monday = 1, Sunday = 7)
    // 0 means no repeat, otherwise each bit indicates if that day is selected
    val repeatDays: Long = 0,

    val ringtone: String = "default",
    val volume: Int = 50,
    val vibrate: Boolean = false,

    val snoozedUntil: Long? = null, // Epoch milliseconds of next snooze alarm

    val nextScheduledTime: Long // Epoch milliseconds of next occurrence
){
    fun isRepeating(): Boolean = repeatDays != 0L


    fun repeatsOn(dayOfWeek: DayOfWeek): Boolean {
        val bitPosition = when (dayOfWeek) {
            DayOfWeek.MONDAY -> 0
            DayOfWeek.TUESDAY -> 1
            DayOfWeek.WEDNESDAY -> 2
            DayOfWeek.THURSDAY -> 3
            DayOfWeek.FRIDAY -> 4
            DayOfWeek.SATURDAY -> 5
            DayOfWeek.SUNDAY -> 6
        }
        return (repeatDays and (1L shl bitPosition)) != 0L
    }
}



