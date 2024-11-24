package dev.bltucker.snoozeloo.alarmdetail


data class AlarmDetailScreenModel(
    val alarmId: Long? = null,
    val hour: String = "",
    val minute: String = "",
    val name: String? = null,
    val repeatDays: Long = 0L,
    val ringtone: String = "default",
    val volume: Int = 50,
    val vibrate: Boolean = false,
    val nextAlarmText: String = "",
    val showNameDialog: Boolean = false,
    val isSaveEnabled: Boolean = false
)