package dev.bltucker.snoozeloo.ringtonesetting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.bltucker.snoozeloo.common.RingtoneProvider
import dev.bltucker.snoozeloo.common.repositories.AlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RingtoneSettingViewModel @Inject constructor(
    private val ringtoneProvider: RingtoneProvider,
    private val alarmRepository: AlarmRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sourceAlarmId: Long = checkNotNull(savedStateHandle[RINGTONE_SOURCE_ALARM_ID])

    private val mutableModel = MutableStateFlow(RingtoneSettingScreenModel(
        selectedRingtoneInfo = ringtoneProvider.getDefaultRingtoneInfo()
    ))
    val observableModel: StateFlow<RingtoneSettingScreenModel> = mutableModel

    private var hasStarted = false

    fun onStart() {
        if(hasStarted){
            return
        }

        hasStarted = true

        viewModelScope.launch {
            val ringtones = ringtoneProvider.getAvailableRingtones()
            val alarm = alarmRepository.getAlarmById(sourceAlarmId)
            val alarmRingtone = ringtones.find { it.title == alarm?.ringtone } ?: ringtoneProvider.getDefaultRingtoneInfo()

            mutableModel.update {
                it.copy(ringtones = ringtones, selectedRingtoneInfo = alarmRingtone, isLoading = false)
            }
        }
    }
}