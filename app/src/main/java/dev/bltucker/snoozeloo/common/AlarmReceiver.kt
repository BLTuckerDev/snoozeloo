package dev.bltucker.snoozeloo.common

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.bltucker.snoozeloo.R
import dev.bltucker.snoozeloo.common.AlarmReceiverIntentFactory.Companion.ALARM_TRIGGER_ACTION
import dev.bltucker.snoozeloo.common.AlarmReceiverIntentFactory.Companion.EXTRA_ALARM_ID
import dev.bltucker.snoozeloo.common.repositories.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmRepository: AlarmRepository


    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ALARM_DEBUG", "onReceive")
        if (intent.action != ALARM_TRIGGER_ACTION) {
            return
        }

        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        Log.d("ALARM_DEBUG", "alarmId: $alarmId")
        if (alarmId == -1L) return

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        scope.launch {
            try {
                val alarm = withContext(Dispatchers.IO) {
                    alarmRepository.getAlarmById(alarmId)
                }

                if (alarm == null) {
                    pendingResult.finish()
                    return@launch
                }

                val fullScreenIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("snoozeloo://alarm-trigger/${alarm.id}")
                ).apply {
                    action = Intent.ACTION_VIEW
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(EXTRA_ALARM_ID, alarm.id)
                }

                createNotificationChannel(context)

                val notification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
                    .setSmallIcon(R.drawable.baseline_alarm_24)
                    .setContentTitle(alarm.name)
                    .setContentText("Tap to view alarm")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setFullScreenIntent(
                        PendingIntent.getActivity(
                            context,
                            alarmId.toInt(),
                            fullScreenIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        ),
                        true
                    )
                    .setAutoCancel(true)
                    .build()

                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.notify(alarmId.toInt(), notification)

            } catch (e: Exception) {
                Log.e("ALARM_DEBUG", "Error in AlarmReceiver", e)
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            ALARM_CHANNEL_ID,
            "Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarm notifications"
            enableLights(true)
            enableVibration(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val ALARM_CHANNEL_ID = "alarm_channel"
    }
}