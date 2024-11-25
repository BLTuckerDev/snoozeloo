package dev.bltucker.snoozeloo.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import dev.bltucker.snoozeloo.common.AlarmReceiverIntentFactory.Companion.ALARM_TRIGGER_ACTION
import dev.bltucker.snoozeloo.common.AlarmReceiverIntentFactory.Companion.EXTRA_ALARM_ID
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var permissionChecker: PermissionChecker

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ALARM_DEBUG", "onReceive")
        if (intent.action != ALARM_TRIGGER_ACTION) {
            return
        }

        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        Log.d("ALARM_DEBUG", "alarmId: $alarmId")
        if (alarmId == -1L) return

        if (permissionChecker.shouldUseForegroundService()) {
            // For Android 12+ use foreground service approach
            val serviceIntent = Intent(context, AlarmNotificationService::class.java).apply {
                putExtra(AlarmNotificationService.EXTRA_ALARM_ID, alarmId)
            }
            context.startForegroundService(serviceIntent)
        } else {
            // For older Android versions, directly launch the activity
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
}