package dev.bltucker.snoozeloo.common

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.bltucker.snoozeloo.common.room.AlarmEntity
import javax.inject.Inject

@Reusable
class AlarmReceiverIntentFactory @Inject constructor(@ApplicationContext private val context: Context){

    fun createAlarmReceiverIntent(alarm: AlarmEntity): Intent {
        return Intent(context, AlarmReceiver::class.java).apply {
            action = ALARM_TRIGGER_ACTION
            putExtra(EXTRA_ALARM_ID, alarm.id)
        }
    }

    fun createAlarmReceiverPendingIntent(alarm: AlarmEntity): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            createAlarmReceiverIntent(alarm),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ALARM_TRIGGER_ACTION = "dev.bltucker.snoozeloo.ALARM_TRIGGER"
        const val EXTRA_ALARM_ID = "alarm_id"
    }

}