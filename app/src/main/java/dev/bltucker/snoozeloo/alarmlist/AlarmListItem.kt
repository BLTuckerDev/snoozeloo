package dev.bltucker.snoozeloo.alarmlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bltucker.snoozeloo.common.AlarmDays
import dev.bltucker.snoozeloo.common.room.AlarmEntity
import dev.bltucker.snoozeloo.common.theme.SnoozeLooGreyBackground
import dev.bltucker.snoozeloo.common.theme.SnoozelooBlack
import dev.bltucker.snoozeloo.common.theme.SnoozelooBlue
import dev.bltucker.snoozeloo.common.theme.SnoozelooBluePale
import dev.bltucker.snoozeloo.common.theme.SnoozelooTheme
import dev.bltucker.snoozeloo.common.theme.SnoozelooWhite
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

//TODO functionality for changing day by tapping chip
@Composable
fun AlarmListItem(
    modifier: Modifier = Modifier,
    alarm: AlarmEntity,
    onToggleAlarm: (Long, Boolean) -> Unit,
    onNavigateToEditAlarm: (alarmId: Long) -> Unit,
    onDayToggled: (Long, Long) -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onNavigateToEditAlarm(alarm.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SnoozelooWhite,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    if (!alarm.name.isNullOrBlank()) {
                        Text(
                            text = alarm.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = SnoozelooBlack
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = formatTimeWithoutAmPm(alarm.hour, alarm.minute),
                            style = MaterialTheme.typography.headlineMedium,
                            color = SnoozelooBlack
                        )
                        Text(
                            text = getAmPmIndicator(alarm.hour),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SnoozelooBlack,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                    }
                }

                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { isEnabled -> onToggleAlarm(alarm.id, isEnabled) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SnoozelooWhite,
                        checkedTrackColor = SnoozelooBlue,
                        checkedBorderColor = SnoozelooBlue,
                        uncheckedThumbColor = SnoozelooWhite,
                        uncheckedTrackColor = SnoozelooBluePale,
                        uncheckedBorderColor = SnoozelooBluePale
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Alarm in ${formatNextAlarmTime(alarm.nextScheduledTime)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            AlarmDayChips(
                modifier = Modifier,
                repeatDays = alarm.repeatDays,
                alarmId = alarm.id,
                onDayToggled = onDayToggled,
                enabled = alarm.isEnabled,
                )

            if (shouldShowSleepSuggestion(alarm.hour, alarm.minute)) {
                Spacer(modifier = Modifier.height(8.dp))
                val bedtime = calculateBedtime(alarm.hour, alarm.minute)
                Text(
                    text = "Go to bed at ${formatTime(bedtime.hour, bedtime.minute)} to get 8h of sleep",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AlarmDayChips(
    repeatDays: Long,
    alarmId: Long,
    onDayToggled: (Long, Long) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DayOfWeek.entries.forEach { day ->
            val dayFlag = 1L shl day.ordinal
            val isSelected = AlarmDays.isEnabled(repeatDays, day)
            val dayText = day.name.take(2).let { it[0] + it[1].lowercase() }

            Box(
                modifier = Modifier
                    .width(38.dp)
                    .height(26.dp)
                    .clip(RoundedCornerShape(38.dp))
                    .background(
                        when {
                            isSelected -> SnoozelooBlue
                            enabled -> SnoozelooBluePale
                            else -> SnoozelooBluePale.copy(alpha = 0.5f)
                        }
                    )
                    .clickable(
                        enabled = enabled,
                        onClick = { onDayToggled(alarmId, dayFlag) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayText,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight(500),
                    color = when {
                        isSelected -> SnoozelooWhite
                        enabled -> SnoozelooBlack
                        else -> SnoozelooBlack.copy(alpha = 0.5f)
                    },
                    fontFamily = FontFamily.Default
                )
            }
        }
    }
}

private fun formatTimeWithoutAmPm(hour: Int, minute: Int): String {
    val time = LocalTime.of(hour, minute)
    return time.format(DateTimeFormatter.ofPattern("hh:mm"))
}

private fun getAmPmIndicator(hour: Int): String {
    return if (hour < 12) "AM" else "PM"
}

private fun formatTime(hour: Int, minute: Int): String {
    val time = LocalTime.of(hour, minute)
    return time.format(DateTimeFormatter.ofPattern("hh:mm a"))
}

private fun formatNextAlarmTime(nextScheduledTime: Long): String {
    val now = System.currentTimeMillis()
    val diff = nextScheduledTime - now

    val days = diff / (24 * 60 * 60 * 1000)
    val hours = (diff % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
    val minutes = (diff % (60 * 60 * 1000)) / (60 * 1000)

    return buildString {
        if (days > 0) append("${days}d ")
        if (hours > 0) append("${hours}h ")
        if (minutes > 0) append("${minutes}min")
    }.trim()
}

private fun shouldShowSleepSuggestion(hour: Int, minute: Int): Boolean {
    if (hour !in 4..10) {
        return false
    }

    val now = LocalTime.now()
    val alarmTime = LocalTime.of(hour, minute)
    val bedtime = alarmTime.minusHours(8)

    if (now.isAfter(bedtime) && now.isBefore(alarmTime)) {
        return false
    }

    return true
}

private fun calculateBedtime(wakeHour: Int, wakeMinute: Int): LocalTime {
    val wakeTime = LocalTime.of(wakeHour, wakeMinute)
    return wakeTime.minusHours(8)
}

@Preview(showBackground = true)
@Composable
private fun AlarmListItemPreviewCollection() {
    val thirtyMinutesFromNow = System.currentTimeMillis() + (30 * 60 * 1000)
    val oneDayFromNow = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
    val twoDaysFromNow = System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000)

    val previewAlarms = listOf(
        AlarmEntity(
            id = 1,
            name = "Wake Up",
            hour = 6,
            minute = 30,
            isEnabled = true,
            repeatDays = AlarmDays.fromDaysList(DayOfWeek.entries),
            ringtone = "default",
            volume = 50,
            vibrate = true,
            snoozedUntil = null,
            nextScheduledTime = thirtyMinutesFromNow
        ),
        AlarmEntity(
            id = 2,
            name = null,
            hour = 7,
            minute = 0,
            isEnabled = true,
            repeatDays = AlarmDays.fromDaysList(listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            )),
            ringtone = "default",
            volume = 50,
            vibrate = false,
            snoozedUntil = null,
            nextScheduledTime = oneDayFromNow
        ),
        AlarmEntity(
            id = 3,
            name = "Weekend",
            hour = 9,
            minute = 30,
            isEnabled = false,
            repeatDays = AlarmDays.fromDaysList(listOf(
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY
            )),
            ringtone = "custom",
            volume = 75,
            vibrate = true,
            snoozedUntil = null,
            nextScheduledTime = twoDaysFromNow
        ),
        AlarmEntity(
            id = 4,
            name = "Early Meeting",
            hour = 5,
            minute = 0,
            isEnabled = true,
            repeatDays = 0,
            ringtone = "default",
            volume = 100,
            vibrate = true,
            snoozedUntil = null,
            nextScheduledTime = oneDayFromNow
        ),
        AlarmEntity(
            id = 5,
            name = "Night Shift",
            hour = 22,
            minute = 0,
            isEnabled = true,
            repeatDays = AlarmDays.fromDaysList(listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.FRIDAY
            )),
            ringtone = "default",
            volume = 50,
            vibrate = false,
            snoozedUntil = null,
            nextScheduledTime = thirtyMinutesFromNow
        ),
        AlarmEntity(
            id = 6,
            name = "Snoozed",
            hour = 8,
            minute = 0,
            isEnabled = true,
            repeatDays = 0,
            ringtone = "default",
            volume = 50,
            vibrate = true,
            snoozedUntil = System.currentTimeMillis() + (5 * 60 * 1000), // Snoozed for 5 minutes
            nextScheduledTime = System.currentTimeMillis() + (5 * 60 * 1000)
        ),
        AlarmEntity(
            id = 7,
            name = "Daily Reminder",
            hour = 12,
            minute = 0,
            isEnabled = true,
            repeatDays = AlarmDays.fromDaysList(DayOfWeek.entries),
            ringtone = "default",
            volume = 50,
            vibrate = true,
            snoozedUntil = null,
            nextScheduledTime = oneDayFromNow
        )
    )

    SnoozelooTheme {
        Column(
            modifier = Modifier
                .background(color = SnoozeLooGreyBackground)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            previewAlarms.forEach { alarm ->
                AlarmListItem(
                    alarm = alarm,
                    onToggleAlarm = { _, _ -> },
                    onNavigateToEditAlarm = { },
                    onDayToggled = { _, _ -> }
                )
            }
        }
    }
}