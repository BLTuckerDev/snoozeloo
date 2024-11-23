package dev.bltucker.snoozeloo.alarmlist

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AlarmListScreenViewModel @Inject constructor(): ViewModel(){

    private val mutableModel: MutableStateFlow<AlarmListScreenModel> = MutableStateFlow(AlarmListScreenModel())

    val observableModel: StateFlow<AlarmListScreenModel> = mutableModel

    private var hasStarted= false

    fun onStart(){
        if(hasStarted){
            return
        }

        hasStarted = true

        //load data
    }


    fun onToggleAlarm(alarmId: Long, isEnabled: Boolean){
        //update alarm
    }
}