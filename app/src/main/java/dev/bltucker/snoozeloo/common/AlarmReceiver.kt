package dev.bltucker.snoozeloo.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import dev.bltucker.snoozeloo.common.AlarmReceiverIntentFactory.Companion.ALARM_TRIGGER_ACTION
import dev.bltucker.snoozeloo.common.AlarmReceiverIntentFactory.Companion.EXTRA_ALARM_ID

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ALARM_DEBUG", "onReceive")
        if (intent.action != ALARM_TRIGGER_ACTION) {
            return
        }

        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        Log.d("ALARM_DEBUG", "alarmId: $alarmId")
        if (alarmId == -1L) return

        val fullScreenIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("snoozeloo://alarm-trigger/$alarmId")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_ALARM_ID, alarmId)
        }

        context.startActivity(fullScreenIntent)
    }
}