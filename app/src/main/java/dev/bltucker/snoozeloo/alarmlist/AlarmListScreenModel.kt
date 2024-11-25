package dev.bltucker.snoozeloo.alarmlist

import dev.bltucker.snoozeloo.common.room.AlarmEntity


data class AlarmListScreenModel(
    val alarms: List<AlarmEntity> = emptyList(),
    val isLoading: Boolean = true,
    val hasNotificationPermission: Boolean = false
){

    fun getAlarmById(alarmId: Long): AlarmEntity? {
        return alarms.find { it.id == alarmId }
    }
}