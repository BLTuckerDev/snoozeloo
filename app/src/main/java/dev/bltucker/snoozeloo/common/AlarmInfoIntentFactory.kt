package dev.bltucker.snoozeloo.common

import android.app.PendingIntent
import dagger.Reusable
import dev.bltucker.snoozeloo.common.room.AlarmEntity
import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.bltucker.snoozeloo.MainActivity
import javax.inject.Inject

@Reusable
class AlarmInfoIntentFactory @Inject constructor(@ApplicationContext private val context: Context) {
    private fun createInfoIntent(alarm: AlarmEntity): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP


            data = Uri.parse("snoozeloo://alarm-trigger/${alarm.id}")
            action = ALARM_INFO_ACTION
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_ALARM_NAME, alarm.name)
        }
    }

    fun createInfoPendingIntent(alarm: AlarmEntity): PendingIntent {
        return PendingIntent.getActivity(
            context,
            getInfoRequestCode(alarm.id),
            createInfoIntent(alarm),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getInfoRequestCode(alarmId: Long): Int {
        return (alarmId + INFO_REQUEST_CODE_OFFSET).toInt()
    }

    companion object {
        const val ALARM_INFO_ACTION = "dev.bltucker.snoozeloo.ALARM_INFO"
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_ALARM_NAME = "alarm_name"

        private const val INFO_REQUEST_CODE_OFFSET = 1_000_000L
    }
}