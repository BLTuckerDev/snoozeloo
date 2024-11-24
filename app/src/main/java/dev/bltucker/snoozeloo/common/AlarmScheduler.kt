package dev.bltucker.snoozeloo.common

import android.app.AlarmManager
import android.util.Log
import dev.bltucker.snoozeloo.common.room.AlarmEntity
import javax.inject.Inject
import javax.inject.Singleton

class ExactAlarmPermissionException : Exception("Cannot schedule exact alarms. SCHEDULE_EXACT_ALARM permission is required on Android 12+")

@Singleton
class AlarmScheduler @Inject constructor(
    private val alarmManager: AlarmManager,
    private val alarmSdkChecker: AlarmSdkChecker,
    private val alarmReceiverIntentFactory: AlarmReceiverIntentFactory,
    private val alarmInfoIntentFactory: AlarmInfoIntentFactory,
) {
    fun scheduleAlarm(alarm: AlarmEntity) {
        val alarmIntent = alarmReceiverIntentFactory.createAlarmReceiverPendingIntent(alarm)
        val infoIntent = alarmInfoIntentFactory.createInfoPendingIntent(alarm)

        val alarmClockInfo = AlarmManager.AlarmClockInfo(
            alarm.nextScheduledTime,
            infoIntent
        )

        if (alarmSdkChecker.isAtLeastS()) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(
                    alarmClockInfo,
                    alarmIntent
                )
                Log.d("ALARM_DEBUG", "Scheduled alarm ${alarm.id} for ${alarm.nextScheduledTime}")
            } else {
                throw ExactAlarmPermissionException()
            }
        } else {
            alarmManager.setAlarmClock(
                alarmClockInfo,
                alarmIntent
            )
            Log.d("ALARM_DEBUG", "Scheduled alarm ${alarm.id} for ${alarm.nextScheduledTime}")
        }
    }

    fun cancelAlarm(alarm: AlarmEntity) {
        val intent = alarmReceiverIntentFactory.createAlarmReceiverPendingIntent(alarm)
        alarmManager.cancel(intent)
    }
}