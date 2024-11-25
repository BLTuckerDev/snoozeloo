package dev.bltucker.snoozeloo.alarmdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.bltucker.snoozeloo.common.AlarmDays
import dev.bltucker.snoozeloo.common.repositories.AlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AlarmDetailViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val alarmId: Long? = savedStateHandle.get<Long>(ALARM_ID_ARG)

    private val mutableModel = MutableStateFlow(AlarmDetailScreenModel())
    val observableModel: StateFlow<AlarmDetailScreenModel> = mutableModel

    private var hasStarted = false

    fun onStart() {
        if (hasStarted) {
            return
        }

        hasStarted = true

        viewModelScope.launch {
            if (alarmId != null) {
                val alarm = alarmRepository.getAlarmById(alarmId)
                alarm?.let {
                    mutableModel.update { model ->
                        model.copy(
                            alarmId = alarm.id,
                            hour = alarm.hour.toString().padStart(2, '0'),
                            minute = alarm.minute.toString().padStart(2, '0'),
                            name = alarm.name,
                            repeatDays = alarm.repeatDays,
                            ringtone = alarm.ringtone,
                            volume = alarm.volume,
                            vibrate = alarm.vibrate,
                            nextAlarmText = calculateNextAlarmText(alarm.nextScheduledTime),
                            isSaveEnabled = true
                        )
                    }
                }
            }
        }
    }

    fun onHourChanged(newHour: String) {
        if (newHour.length > 2) return
        if (newHour.isNotEmpty() && !newHour.all { it.isDigit() }) return

        mutableModel.update { model ->
            val hourValue = newHour.toIntOrNull() ?: 0
            model.copy(
                hour = newHour,
                isSaveEnabled = isTimeValid(hourValue, model.minute.toIntOrNull() ?: 0)
            )
        }
    }

    fun onMinuteChanged(newMinute: String) {
        if (newMinute.length > 2) return
        if (newMinute.isNotEmpty() && !newMinute.all { it.isDigit() }) return

        mutableModel.update { model ->
            val minuteValue = newMinute.toIntOrNull() ?: 0
            model.copy(
                minute = newMinute,
                isSaveEnabled = isTimeValid(model.hour.toIntOrNull() ?: 0, minuteValue)
            )
        }
    }

    fun onNameDialogShow(){
        mutableModel.update { it.copy(showNameDialog = true) }
    }

    fun onNameDialogDismiss() {
        mutableModel.update { it.copy(showNameDialog = false) }
    }

    fun onNameSaved(name: String) {
        mutableModel.update { model ->
            model.copy(
                name = name.takeIf { it.isNotBlank() },
                showNameDialog = false
            )
        }
    }

    fun onNameChanged(updatedName: String){
        mutableModel.update { it.copy(name = updatedName) }
    }

    fun onRepeatDayToggled(dayFlag: Long) {
        mutableModel.update { model ->
            model.copy(repeatDays = model.repeatDays xor dayFlag)
        }
    }

    fun onVolumeChanged(volume: Int) {
        mutableModel.update { it.copy(volume = volume) }
    }

    fun onVibrateToggled() {
        mutableModel.update { it.copy(vibrate = !it.vibrate) }
    }

    fun onUpdateRingtone(ringtone: String) {
        mutableModel.update { it.copy(ringtone = ringtone) }
    }

    fun onSave() {
        val model = mutableModel.value
        val hour = model.hour.toIntOrNull() ?: return
        val minute = model.minute.toIntOrNull() ?: return

        viewModelScope.launch {
            if (model.alarmId == null) {
                alarmRepository.createAlarm(
                    hour = hour,
                    minute = minute,
                    name = model.name,
                    repeatDays = AlarmDays.toDaysList(model.repeatDays).toSet(),
                    ringtone = model.ringtone,
                    volume = model.volume,
                    vibrate = model.vibrate
                )
            } else {
                alarmRepository.updateAlarm(
                    alarmId = model.alarmId,
                    hour = hour,
                    minute = minute,
                    name = model.name,
                    repeatDays = AlarmDays.toDaysList(model.repeatDays).toSet(),
                    ringtone = model.ringtone,
                    volume = model.volume,
                    vibrate = model.vibrate
                )
            }
        }
    }

    private fun isTimeValid(hour: Int, minute: Int): Boolean {
        return hour in 0..23 && minute in 0..59
    }

    private fun calculateNextAlarmText(nextScheduledTime: Long): String {
        val now = System.currentTimeMillis()
        val diff = nextScheduledTime - now

        val days = diff / (24 * 60 * 60 * 1000)
        val hours = (diff % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
        val minutes = (diff % (60 * 60 * 1000)) / (60 * 1000)

        return buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0) append("${hours}h ")
            if (minutes > 0) append("${minutes}min")
        }.trim()
    }

    companion object {
        const val ALARM_ID_ARG = "alarmId"
    }
}