package dev.bltucker.snoozeloo.common

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.bltucker.snoozeloo.common.room.AlarmEntity
import javax.inject.Inject
import javax.inject.Singleton

class ExactAlarmPermissionException : Exception("Cannot schedule exact alarms. SCHEDULE_EXACT_ALARM permission is required on Android 12+")

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager
) {
    fun scheduleAlarm(alarm: AlarmEntity) {
        val intent = createAlarmIntent(alarm)
        scheduleExactAlarm(intent, alarm.nextScheduledTime)
    }

    private fun createAlarmIntent(alarm: AlarmEntity): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "dev.bltucker.snoozeloo.ALARM_TRIGGER"
            putExtra("alarm_id", alarm.id)
            putExtra("alarm_name", alarm.name)
            putExtra("alarm_volume", alarm.volume)
            putExtra("alarm_vibrate", alarm.vibrate)
            putExtra("alarm_ringtone", alarm.ringtone)
        }

        return PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun scheduleExactAlarm(intent: PendingIntent, triggerTime: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    intent
                )
            } else {
                throw ExactAlarmPermissionException()
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                intent
            )
        }
    }

    fun cancelAlarm(alarm: AlarmEntity) {
        val intent = createAlarmIntent(alarm)
        alarmManager.cancel(intent)
    }
}