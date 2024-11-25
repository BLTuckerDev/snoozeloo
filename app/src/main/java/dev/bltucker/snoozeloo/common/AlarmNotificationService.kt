package dev.bltucker.snoozeloo.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.bltucker.snoozeloo.R
import dev.bltucker.snoozeloo.common.repositories.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmNotificationService : Service() {

    @Inject
    lateinit var powerManager: PowerManager

    @Inject
    lateinit var alarmInfoIntentFactory: AlarmInfoIntentFactory

    @Inject
    lateinit var alarmRepository: AlarmRepository

    @Inject
    lateinit var audioManager: AudioManager

    @Inject
    lateinit var vibrator: Vibrator

    @Inject
    lateinit var ringtoneProvider: RingtoneProvider

    private var wakeLock: PowerManager.WakeLock? = null
    private var mediaPlayer: MediaPlayer? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getLongExtra(EXTRA_ALARM_ID, -1L) ?: -1L
        if (alarmId == -1L) {
            stopSelf()
            return START_NOT_STICKY
        }

        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Snoozeloo::AlarmWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L) // 10 minutes
        }

        serviceScope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId)
            if (alarm == null) {
                stopSelf()
                return@launch
            }

            if (alarm.ringtone != "silent") {
                playRingtone(alarm.ringtone, alarm.volume)
            }

            if (alarm.vibrate) {
                startVibration()
            }

            // Show notification
            val fullScreenIntent = getFullScreenIntent(alarmId)
            val fullScreenPendingIntent = PendingIntent.getActivity(
                this@AlarmNotificationService,
                alarmId.toInt(),
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this@AlarmNotificationService, CHANNEL_ID)
                .setSmallIcon(R.drawable.solid_alarm)
                .setContentTitle(alarm.name ?: "Alarm")
                .setContentText("Tap to view alarm")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setOngoing(true)
                .setAutoCancel(false)
                .build()

            startForeground(NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    private fun getFullScreenIntent(alarmId: Long): Intent {
        return Intent(
            Intent.ACTION_VIEW,
            Uri.parse("snoozeloo://alarm-trigger/$alarmId")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
    }

    private fun playRingtone(ringtoneTitle: String, volume: Int) {
        try {
            val ringtoneUri = when (ringtoneTitle) {
                "default" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                else -> {
                    ringtoneProvider.getAvailableRingtones().find { it.title == ringtoneTitle }?.let {
                        Uri.parse(it.uri)
                    } ?: run {
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    }
                }
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmNotificationService, ringtoneUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true

                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                val scaledVolume = (volume / 100f) * maxVolume
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, scaledVolume.toInt(), 0)

                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startVibration() {
        vibrator.vibrate(
            VibrationEffect.createWaveform(
                longArrayOf(0, 500, 1000),
                0
            )
        )
    }

    private fun stopEverything() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null

        vibrator.cancel()

        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopEverything()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableLights(true)
            enableVibration(true)
            setBypassDnd(true)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "alarm_service_channel"
        private const val NOTIFICATION_ID = 1
        const val EXTRA_ALARM_ID = "alarm_id"

        fun stopService(context: Context) {
            val intent = Intent(context, AlarmNotificationService::class.java)
            context.stopService(intent)
        }
    }
}