package dev.bltucker.snoozeloo.alarmtrigger

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.bltucker.snoozeloo.common.AlarmNotificationService
import dev.bltucker.snoozeloo.common.repositories.AlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import android.os.Build
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AlarmTriggerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmRepository: AlarmRepository,
    private val vibrator: Vibrator,
    private val audioManager: AudioManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val alarmId: Long = checkNotNull(savedStateHandle[ALARM_ID_ARG])

    private var mediaPlayer: MediaPlayer? = null

    private val mutableModel = MutableStateFlow(
        AlarmTriggerScreenModel(
            alarmId = alarmId,
            alarmName = null,
            formattedTime = ""
        )
    )
    val observableModel: StateFlow<AlarmTriggerScreenModel> = mutableModel

    private var hasStarted = false

    fun onStart() {
        if(hasStarted){
            return
        }

        hasStarted = true

        viewModelScope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId)
            alarm?.let {
                val time = LocalTime.of(it.hour, it.minute)
                val formatter = DateTimeFormatter.ofPattern("HH:mm")

                mutableModel.value = AlarmTriggerScreenModel(
                    alarmId = it.id,
                    alarmName = it.name,
                    formattedTime = time.format(formatter)
                )

                if (!shouldUseService()) {
                    startAlarm(alarm.ringtone, alarm.volume, alarm.vibrate)
                }
            }
        }
    }

    private fun startAlarm(ringtonePath: String, volume: Int, shouldVibrate: Boolean) {
        if (ringtonePath != "silent") {
            playRingtone(ringtonePath, volume)
        }
        if (shouldVibrate) {
            startVibration()
        }
    }

    private fun playRingtone(ringtonePath: String, volume: Int) {
        try {
            val ringtoneUri = when (ringtonePath) {
                "default" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                else -> Uri.parse(ringtonePath)
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, ringtoneUri)
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
        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 1000), 0))
    }

    private fun stopLocalAlarm() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        vibrator.cancel()
    }

    fun onTurnOff() {
        viewModelScope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId)
            if (alarm != null && !alarm.isRepeating()) {
                alarmRepository.toggleAlarm(alarmId, false)
            }

            if (shouldUseService()) {
                AlarmNotificationService.stopService(context)
            } else {
                stopLocalAlarm()
            }
        }
    }

    fun onSnooze() {
        viewModelScope.launch {
            alarmRepository.snoozeAlarm(alarmId)

            if (shouldUseService()) {
                AlarmNotificationService.stopService(context)
            } else {
                stopLocalAlarm()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (shouldUseService()) {
            AlarmNotificationService.stopService(context)
        } else {
            stopLocalAlarm()
        }
    }

    private fun shouldUseService(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}