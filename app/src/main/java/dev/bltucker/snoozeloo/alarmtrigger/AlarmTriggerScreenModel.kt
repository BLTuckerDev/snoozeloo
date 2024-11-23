package dev.bltucker.snoozeloo.alarmtrigger

data class AlarmTriggerScreenModel(
    val alarmId: Long,
    val alarmName: String?,
    val formattedTime: String
)