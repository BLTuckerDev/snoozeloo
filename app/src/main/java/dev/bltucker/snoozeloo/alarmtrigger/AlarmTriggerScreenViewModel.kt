package dev.bltucker.snoozeloo.alarmtrigger

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.bltucker.snoozeloo.common.repositories.AlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AlarmTriggerViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val alarmId: Long = checkNotNull(savedStateHandle[ALARM_ID_ARG])

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

        //TODO need to play ringtone and vibrate it required by the alarm.
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
            }
        }
    }

    fun onTurnOff() {
        viewModelScope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId)
            if (alarm != null && !alarm.isRepeating()) {
                alarmRepository.toggleAlarm(alarmId, false)
            }
        }
    }

    fun onSnooze() {
        viewModelScope.launch {
            alarmRepository.snoozeAlarm(alarmId)
        }
    }
}