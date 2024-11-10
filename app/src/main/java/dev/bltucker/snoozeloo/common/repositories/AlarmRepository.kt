package dev.bltucker.snoozeloo.common.repositories

import dev.bltucker.snoozeloo.common.AlarmDays
import dev.bltucker.snoozeloo.common.AlarmScheduler
import dev.bltucker.snoozeloo.common.room.AlarmDao
import dev.bltucker.snoozeloo.common.room.AlarmEntity
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao,
    private val alarmScheduler: AlarmScheduler
) {
    fun observeAllAlarms(): Flow<List<AlarmEntity>> = alarmDao.observeAllAlarms()

    suspend fun getEnabledAlarms() = alarmDao.getEnabledAlarms()

    suspend fun getAlarmById(alarmId: Long) = alarmDao.getAlarmById(alarmId)

    suspend fun deleteAlarm(alarmId: Long) {
        val alarm = alarmDao.getAlarmById(alarmId)
        alarm?.let {
            alarmDao.delete(it)
            alarmScheduler.cancelAlarm(it)
        }
    }

    suspend fun createAlarm(
        hour: Int,
        minute: Int,
        name: String? = null,
        repeatDays: Set<DayOfWeek> = emptySet(),
        ringtone: String = "default",
        volume: Int = 50,
        vibrate: Boolean = false
    ): Long {
        require(hour in 0..23) { "Hour must be between 0 and 23" }
        require(minute in 0..59) { "Minute must be between 0 and 59" }
        require(volume in 0..100) { "Volume must be between 0 and 100" }

        val repeatDaysBitmask = if (repeatDays.isEmpty()) {
            0L
        } else {
            AlarmDays.fromDaysList(repeatDays.toList())
        }

        val nextScheduledTime = calculateNextScheduledTime(
            hour = hour,
            minute = minute,
            repeatDays = repeatDaysBitmask
        )

        val alarm = AlarmEntity(
            hour = hour,
            minute = minute,
            name = name,
            repeatDays = repeatDaysBitmask,
            ringtone = ringtone,
            volume = volume,
            vibrate = vibrate,
            isEnabled = true,
            nextScheduledTime = nextScheduledTime
        )

        val alarmId = alarmDao.insert(alarm)
        alarmScheduler.scheduleAlarm(alarm.copy(id = alarmId))

        return alarmId
    }

    suspend fun toggleAlarm(alarmId: Long, isEnabled: Boolean) {
        alarmDao.updateAlarmEnabled(alarmId, isEnabled)

        val alarm = alarmDao.getAlarmById(alarmId)
        alarm?.let {
            if (isEnabled) {
                val nextScheduledTime = calculateNextScheduledTime(
                    hour = it.hour,
                    minute = it.minute,
                    repeatDays = it.repeatDays
                )
                alarmDao.update(it.copy(nextScheduledTime = nextScheduledTime))
                alarmScheduler.scheduleAlarm(it.copy(nextScheduledTime = nextScheduledTime))
            } else {
                alarmScheduler.cancelAlarm(it)
            }
        }
    }

    suspend fun snoozeAlarm(alarmId: Long) {
        val alarm = alarmDao.getAlarmById(alarmId) ?: return

        val now = System.currentTimeMillis()
        val snoozedUntil = now + SNOOZE_DURATION_MS

        val nextRegularTime = calculateNextScheduledTime(
            hour = alarm.hour,
            minute = alarm.minute,
            repeatDays = alarm.repeatDays
        )

        val nextScheduledTime = minOf(snoozedUntil, nextRegularTime)

        alarmDao.updateAlarmSnooze(
            alarmId = alarmId,
            snoozedUntil = snoozedUntil,
            nextScheduledTime = nextScheduledTime
        )

        alarmScheduler.scheduleAlarm(
            alarm.copy(
                snoozedUntil = snoozedUntil,
                nextScheduledTime = nextScheduledTime
            )
        )
    }

    suspend fun clearSnooze(alarmId: Long) {
        val alarm = alarmDao.getAlarmById(alarmId) ?: return

        val nextScheduledTime = calculateNextScheduledTime(
            hour = alarm.hour,
            minute = alarm.minute,
            repeatDays = alarm.repeatDays
        )

        alarmDao.updateAlarmSnooze(
            alarmId = alarmId,
            snoozedUntil = null,
            nextScheduledTime = nextScheduledTime
        )

        alarmScheduler.scheduleAlarm(alarm.copy(
            snoozedUntil = null,
            nextScheduledTime = nextScheduledTime
        ))
    }

    suspend fun updateNextScheduledTime(alarmId: Long): AlarmEntity {
        val alarm = alarmDao.getAlarmById(alarmId) ?: throw IllegalStateException("Alarm not found")

        val nextScheduledTime = calculateNextScheduledTime(
            hour = alarm.hour,
            minute = alarm.minute,
            repeatDays = alarm.repeatDays
        )

        val updatedAlarm = alarm.copy(nextScheduledTime = nextScheduledTime)
        alarmDao.update(updatedAlarm)
        return updatedAlarm
    }

    private fun calculateNextScheduledTime(
        hour: Int,
        minute: Int,
        repeatDays: Long
    ): Long {
        val now = System.currentTimeMillis()
        val todayTime = LocalTime.of(hour, minute)

        if (repeatDays == 0L) {
            var scheduledTime = todayTime.toEpochMillis()

            if (scheduledTime <= now) {
                scheduledTime += DAY_IN_MILLIS
            }

            return scheduledTime
        }

        var nextTime = todayTime.toEpochMillis()
        var daysToAdd = 0

        while (nextTime <= now || !AlarmDays.isEnabled(repeatDays, getDayOfWeek(nextTime))) {
            daysToAdd++
            nextTime = todayTime.toEpochMillis() + (daysToAdd * DAY_IN_MILLIS)
        }

        return nextTime
    }

    private fun getDayOfWeek(timeInMillis: Long): DayOfWeek {
        val calendar = Calendar.getInstance().apply {
            setTimeInMillis(timeInMillis)
        }

        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> DayOfWeek.MONDAY
            Calendar.TUESDAY -> DayOfWeek.TUESDAY
            Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
            Calendar.THURSDAY -> DayOfWeek.THURSDAY
            Calendar.FRIDAY -> DayOfWeek.FRIDAY
            Calendar.SATURDAY -> DayOfWeek.SATURDAY
            Calendar.SUNDAY -> DayOfWeek.SUNDAY
            else -> throw IllegalStateException("Invalid day of week")
        }
    }

    companion object {
        const val SNOOZE_DURATION_MS = 5 * 60 * 1000L // 5 minutes
        const val DAY_IN_MILLIS = 24 * 60 * 60 * 1000L
    }
}


private fun LocalTime.toEpochMillis(): Long {
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

