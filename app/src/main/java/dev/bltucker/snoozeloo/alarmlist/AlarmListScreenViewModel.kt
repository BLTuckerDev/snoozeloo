package dev.bltucker.snoozeloo.alarmlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.bltucker.snoozeloo.common.repositories.AlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmListScreenViewModel @Inject constructor(private val alarmRepository: AlarmRepository): ViewModel(){

    private val mutableModel: MutableStateFlow<AlarmListScreenModel> = MutableStateFlow(AlarmListScreenModel())

    val observableModel: StateFlow<AlarmListScreenModel> = mutableModel

    private var hasStarted= false

    fun onStart(){
        if(hasStarted){
            return
        }

        hasStarted = true

        viewModelScope.launch {
            alarmRepository.observeAllAlarms().collect{ alarms ->
                mutableModel.update { it.copy(alarms = alarms, isLoading = false) }
            }
        }
    }


    fun onToggleAlarm(alarmId: Long, isEnabled: Boolean){
        viewModelScope.launch {
            alarmRepository.toggleAlarm(alarmId, isEnabled)
        }
    }

    fun onDayToggled(alarmId: Long, dayFlag: Long) {
        viewModelScope.launch {
            val alarm = mutableModel.value.getAlarmById(alarmId) ?: return@launch
            val newRepeatDays = alarm.repeatDays xor dayFlag
            alarmRepository.updateAlarmRepeatDays(alarmId, newRepeatDays)
        }
    }
}