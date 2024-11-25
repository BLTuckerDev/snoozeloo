package dev.bltucker.snoozeloo.alarmlist

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.bltucker.snoozeloo.common.repositories.AlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmListScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmRepository: AlarmRepository): ViewModel(){

    private val mutableModel: MutableStateFlow<AlarmListScreenModel> = MutableStateFlow(AlarmListScreenModel())

    val observableModel: StateFlow<AlarmListScreenModel> = mutableModel

    private var hasStarted= false

    fun onStart(){
        if(hasStarted){
            return
        }

        hasStarted = true

        checkNotificationPermission()

        viewModelScope.launch {
            alarmRepository.observeAllAlarms().collect{ alarms ->
                mutableModel.update { it.copy(alarms = alarms, isLoading = false) }
            }
        }
    }


    fun checkNotificationPermission() {
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        mutableModel.update { it.copy(hasNotificationPermission = hasPermission) }
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