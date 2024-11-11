package dev.bltucker.snoozeloo.common

import android.app.AlarmManager
import android.app.PendingIntent
import dev.bltucker.snoozeloo.common.room.AlarmEntity
import javax.inject.Inject
import javax.inject.Singleton

class ExactAlarmPermissionException : Exception("Cannot schedule exact alarms. SCHEDULE_EXACT_ALARM permission is required on Android 12+")

@Singleton
class AlarmScheduler @Inject constructor(
    private val alarmManager: AlarmManager,
    private val alarmSdkChecker: AlarmSdkChecker,
    private val alarmReceiverIntentFactory: AlarmReceiverIntentFactory,
) {
    fun scheduleAlarm(alarm: AlarmEntity) {
        val intent = alarmReceiverIntentFactory.createAlarmPendingIntent(alarm)
        scheduleExactAlarm(intent, alarm.nextScheduledTime)
    }

    private fun scheduleExactAlarm(intent: PendingIntent, triggerTime: Long) {
        if (alarmSdkChecker.isAtLeastS()) {
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
        val intent = alarmReceiverIntentFactory.createAlarmPendingIntent(alarm)
        alarmManager.cancel(intent)
    }
}