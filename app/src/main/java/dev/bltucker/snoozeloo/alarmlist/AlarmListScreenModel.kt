package dev.bltucker.snoozeloo.alarmlist

import dev.bltucker.snoozeloo.common.room.AlarmEntity


data class AlarmListScreenModel(
    val alarms: List<AlarmEntity> = emptyList(),
    val isLoading: Boolean
)