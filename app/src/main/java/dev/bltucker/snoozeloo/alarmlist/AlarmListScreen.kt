package dev.bltucker.snoozeloo.alarmlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.bltucker.snoozeloo.R
import dev.bltucker.snoozeloo.common.AlarmDays
import dev.bltucker.snoozeloo.common.composables.LoadingSpinner
import dev.bltucker.snoozeloo.common.room.AlarmEntity
import dev.bltucker.snoozeloo.common.theme.SnoozeLooGreyBackground
import dev.bltucker.snoozeloo.common.theme.SnoozelooBlue
import dev.bltucker.snoozeloo.common.theme.SnoozelooTheme
import dev.bltucker.snoozeloo.common.theme.SnoozelooWhite
import java.time.DayOfWeek


const val ALARM_LIST_ROUTE = "alarm-list"

fun NavGraphBuilder.alarmListScreen(
    onNavigateToCreateAlarm: () -> Unit,
    onNavigateToEditAlarm: (alarmId: Long) -> Unit,
) {
    composable(ALARM_LIST_ROUTE) {
        val viewModel = hiltViewModel<AlarmListScreenViewModel>()
        val model by viewModel.observableModel.collectAsStateWithLifecycle()


        LifecycleStartEffect(Unit) {
            viewModel.onStart()
            onStopOrDispose {  }
        }

        AlarmListScreen(
            modifier = Modifier.fillMaxSize(),
            model = model,
            onCreateAlarm = { onNavigateToCreateAlarm()},
            onNavigateToEditAlarm = onNavigateToEditAlarm,
            onToggleAlarm = viewModel::onToggleAlarm,
            onDayToggled = viewModel::onDayToggled
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    modifier: Modifier = Modifier,
    model: AlarmListScreenModel,
    onCreateAlarm: () -> Unit,
    onToggleAlarm: (Long, Boolean) -> Unit,
    onNavigateToEditAlarm: (alarmId: Long) -> Unit,
    onDayToggled: (Long, Long) -> Unit,) {

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Your Alarms") }
            )
        },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.Center,
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.size(56.dp),
                onClick = onCreateAlarm,
                containerColor = SnoozelooBlue,
                contentColor = SnoozelooWhite,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create Alarm",
                    modifier = Modifier.size(24.dp)
                )
            }
        },

    ) { paddingValues ->
        if(model.isLoading){
            LoadingSpinner(modifier = Modifier.fillMaxSize().padding(paddingValues))
        }else if (model.alarms.isEmpty()) {
            EmptyAlarmList(Modifier
                .fillMaxSize()
                .padding(paddingValues))
        } else {
           AlarmList(
               modifier = Modifier
                   .fillMaxSize()
                   .padding(paddingValues),
               onNavigateToEditAlarm = onNavigateToEditAlarm,
               onToggleAlarm = onToggleAlarm,
               alarms = model.alarms,
               onDayToggled = onDayToggled)
        }
    }
}

@Composable
private fun EmptyAlarmList(modifier: Modifier = Modifier) {

        Column(
            modifier = modifier.background(SnoozeLooGreyBackground),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(62.dp),
                painter = painterResource(R.drawable.solid_alarm),
                contentDescription = "Alarm Icon",
                tint = SnoozelooBlue,
            )
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = "It's empty! Add the first alarm so you\ndon't miss an important moment!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
}

@Composable
private fun AlarmList(
    modifier: Modifier = Modifier,
    onToggleAlarm: (Long, Boolean) -> Unit,
    alarms: List<AlarmEntity>,
    onNavigateToEditAlarm: (alarmId: Long) -> Unit,
    onDayToggled: (Long, Long) -> Unit){
    LazyColumn(
        modifier = modifier.background(SnoozeLooGreyBackground),
    ) {
        items(alarms, key = { it.id }){ alarm ->
            AlarmListItem(alarm = alarm,
                onNavigateToEditAlarm = onNavigateToEditAlarm,
                onToggleAlarm = onToggleAlarm,
                onDayToggled = onDayToggled)
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun AlarmListScreenPreview() {
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
            repeatDays = AlarmDays.fromDaysList(listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            )),
            ringtone = "default",
            volume = 50,
            vibrate = true,
            nextScheduledTime = thirtyMinutesFromNow
        ),
        AlarmEntity(
            id = 2,
            name = "Weekend",
            hour = 9,
            minute = 0,
            isEnabled = false,
            repeatDays = AlarmDays.fromDaysList(listOf(
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY
            )),
            ringtone = "default",
            volume = 75,
            vibrate = false,
            nextScheduledTime = twoDaysFromNow
        ),
        AlarmEntity(
            id = 3,
            name = null,
            hour = 7,
            minute = 15,
            isEnabled = true,
            repeatDays = 0L,
            ringtone = "custom",
            volume = 100,
            vibrate = true,
            nextScheduledTime = oneDayFromNow
        )
    )

    val previewModel = AlarmListScreenModel(
        alarms = previewAlarms,
        isLoading = false
    )

    SnoozelooTheme {
        AlarmListScreen(
            model = previewModel,
            onCreateAlarm = {},
            onToggleAlarm = { _, _ -> },
            onNavigateToEditAlarm = { },
            onDayToggled = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyAlarmListScreenPreview() {
    val emptyModel = AlarmListScreenModel(
        alarms = emptyList(),
        isLoading = false
    )

    SnoozelooTheme {
        AlarmListScreen(
            model = emptyModel,
            onCreateAlarm = {},
            onToggleAlarm = { _, _ -> },
            onNavigateToEditAlarm = {},
            onDayToggled = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingAlarmListScreenPreview() {
    val emptyModel = AlarmListScreenModel(
        alarms = emptyList(),
        isLoading = true
    )

    SnoozelooTheme {
        AlarmListScreen(
            model = emptyModel,
            onCreateAlarm = {},
            onToggleAlarm = { _, _ -> },
            onNavigateToEditAlarm = {},
            onDayToggled = { _, _ -> }
        )
    }
}