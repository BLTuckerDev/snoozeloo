package dev.bltucker.snoozeloo.ringtonesetting

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.bltucker.snoozeloo.common.RingtoneInfo
import dev.bltucker.snoozeloo.common.RingtoneProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RingtoneSettingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ringtoneProvider: RingtoneProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sourceRingtoneName: String = checkNotNull(savedStateHandle[RINGTONE_NAME])

    private val mutableModel = MutableStateFlow(RingtoneSettingScreenModel(
        selectedRingtoneInfo = ringtoneProvider.getDefaultRingtoneInfo()
    ))
    val observableModel: StateFlow<RingtoneSettingScreenModel> = mutableModel

    private var ringtone: Ringtone? = null

    private var hasStarted = false

    fun onStart() {
        if(hasStarted){
            return
        }

        hasStarted = true

        viewModelScope.launch {
            val ringtones = ringtoneProvider.getAvailableRingtones()
            val alarmRingtone = ringtones.find { it.title == sourceRingtoneName } ?: ringtoneProvider.getDefaultRingtoneInfo()

            mutableModel.update {
                it.copy(ringtones = ringtones, selectedRingtoneInfo = alarmRingtone, isLoading = false)
            }
        }
    }

    fun onRingtoneSelected(ringtoneInfo: RingtoneInfo) {
        mutableModel.update {
            it.copy(selectedRingtoneInfo = ringtoneInfo)
        }

        ringtone?.stop()
        viewModelScope.launch {
            ringtone = RingtoneManager.getRingtone(context, Uri.parse(ringtoneInfo.uri))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone?.volume = 0.5f
            }
            ringtone?.play()
            delay(4_000)
            ringtone?.stop()
        }
    }

    override fun onCleared() {
        super.onCleared()
        ringtone?.stop()
    }
}