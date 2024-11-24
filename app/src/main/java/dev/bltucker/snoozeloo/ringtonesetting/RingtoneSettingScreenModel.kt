package dev.bltucker.snoozeloo.ringtonesetting

import dev.bltucker.snoozeloo.common.RingtoneInfo

data class RingtoneSettingScreenModel(
    val ringtones: List<RingtoneInfo> = emptyList(),
    val selectedRingtoneInfo: RingtoneInfo,
    val isLoading: Boolean = true
)