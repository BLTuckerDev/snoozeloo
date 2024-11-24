package dev.bltucker.snoozeloo.common

import android.app.AlarmManager
import dev.bltucker.snoozeloo.common.room.AlarmEntity
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AlarmScheduler @Inject constructor(
    private val alarmManager: AlarmManager,
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

        alarmManager.setAlarmClock(
            alarmClockInfo,
            alarmIntent
        )
    }

    fun cancelAlarm(alarm: AlarmEntity) {
        val intent = alarmReceiverIntentFactory.createAlarmReceiverPendingIntent(alarm)
        alarmManager.cancel(intent)
    }
}