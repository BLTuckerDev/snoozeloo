package dev.bltucker.snoozeloo.alarmdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.bltucker.snoozeloo.common.theme.SnoozeLooGreyBackground
import dev.bltucker.snoozeloo.common.theme.SnoozelooBlack
import dev.bltucker.snoozeloo.common.theme.SnoozelooBlue
import dev.bltucker.snoozeloo.common.theme.SnoozelooBluePale
import dev.bltucker.snoozeloo.common.theme.SnoozelooWhite
import dev.bltucker.snoozeloo.ringtonesetting.RINGTONE_REQUEST_KEY
import java.time.DayOfWeek

const val ALARM_DETAIL_ROUTE = "alarm-detail"

fun NavController.navigateToAlarmDetail(alarmId: Long? = null) {
    val route = buildString {
        append(ALARM_DETAIL_ROUTE)
        if (alarmId != null) {
            append("?alarmId=$alarmId")
        }
    }
    navigate(route)
}

fun NavGraphBuilder.alarmDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRingtoneSetting: (String) -> Unit
) {
    composable(
        route = "$ALARM_DETAIL_ROUTE?alarmId={alarmId}",
        arguments = listOf(
            navArgument("alarmId") {
                type = NavType.LongType
                defaultValue = -1L
            }
        )
    ) { backStackEntry ->
        val viewModel = hiltViewModel<AlarmDetailViewModel>()
        val model by viewModel.observableModel.collectAsStateWithLifecycle()

        val ringtoneTitle by backStackEntry.savedStateHandle.getStateFlow<String?>(RINGTONE_REQUEST_KEY, null).collectAsStateWithLifecycle()

        LaunchedEffect(ringtoneTitle) {
            ringtoneTitle?.let { ringtone ->
                viewModel.onUpdateRingtone(ringtone)
                backStackEntry.savedStateHandle.remove<String>(RINGTONE_REQUEST_KEY)
            }
        }

        LifecycleStartEffect(Unit) {
            viewModel.onStart()
            onStopOrDispose { }
        }

        AlarmDetailScreen(
            model = model,
            onBackClick = onNavigateBack,
            onHourChanged = viewModel::onHourChanged,
            onMinuteChanged = viewModel::onMinuteChanged,
            onNameDialogShow = viewModel::onNameDialogShow,
            onNameDialogDismiss = viewModel::onNameDialogDismiss,
            onNameSaved = viewModel::onNameSaved,
            onNameChanged = viewModel::onNameChanged,
            onRepeatDayToggled = viewModel::onRepeatDayToggled,
            onRingtoneClick = {
                onNavigateToRingtoneSetting(model.ringtone)
            },
            onVolumeChanged = viewModel::onVolumeChanged,
            onVibrateToggled = viewModel::onVibrateToggled,
            onSave = {
                viewModel.onSave()
                onNavigateBack()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDetailScreen(
    modifier: Modifier = Modifier,
    model: AlarmDetailScreenModel,
    onBackClick: () -> Unit,
    onHourChanged: (String) -> Unit,
    onMinuteChanged: (String) -> Unit,
    onNameDialogShow: () -> Unit,
    onNameDialogDismiss: () -> Unit,
    onNameChanged: (String) -> Unit,
    onNameSaved: (String) -> Unit,
    onRepeatDayToggled: (Long) -> Unit,
    onRingtoneClick: () -> Unit,
    onVolumeChanged: (Int) -> Unit,
    onVibrateToggled: () -> Unit,
    onSave: () -> Unit,
) {

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SnoozelooBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SnoozelooWhite
                            )
                        }
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSave,
                        enabled = model.isSaveEnabled
                    ) {
                        Text(
                            text = "Save",
                            color = if (model.isSaveEnabled) SnoozelooBlue else SnoozelooBlue.copy(
                                alpha = 0.5f
                            )
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SnoozeLooGreyBackground)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TimeInputRow(
                model = model,
                onHourChanged = onHourChanged,
                onMinuteChanged = onMinuteChanged
            )

            if (model.nextAlarmText.isNotEmpty()) {
                Text(
                    text = "Alarm in ${model.nextAlarmText}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AlarmNameCard(onNameDialogShow = onNameDialogShow, model = model)

            RepeatDaysCard(model = model, onRepeatDayToggled = onRepeatDayToggled)

            RingtoneCard(onRingtoneClick = onRingtoneClick, model = model)

            VolumeCard(model = model, onVolumeChanged = onVolumeChanged)

            VibrateCard(model = model, onVibrateToggled = onVibrateToggled)
        }

        if (model.showNameDialog) {
            AlarmNameDialog(
                name = model.name ?: "",
                onNameChange = {
                    onNameChanged(it)
                },
                onDismiss = {
                    onNameDialogDismiss()
                },
                onSave = {
                    onNameSaved(it)
                }
            )
        }
    }
}

@Composable
private fun VibrateCard(
    model: AlarmDetailScreenModel,
    onVibrateToggled: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Vibrate",
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = model.vibrate,
                onCheckedChange = { onVibrateToggled() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SnoozelooWhite,
                    checkedTrackColor = SnoozelooBlue,
                    checkedBorderColor = SnoozelooBlue,
                    uncheckedThumbColor = SnoozelooWhite,
                    uncheckedTrackColor = SnoozelooBluePale,
                    uncheckedBorderColor = SnoozelooBluePale
                )
            )
        }
    }
}

@Composable
private fun VolumeCard(
    modifier: Modifier = Modifier,
    model: AlarmDetailScreenModel,
    onVolumeChanged: (Int) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Alarm volume",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = model.volume.toFloat(),
                onValueChange = { onVolumeChanged(it.toInt()) },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = SnoozelooBlue,
                    activeTrackColor = SnoozelooBlue,
                    inactiveTrackColor = SnoozelooBluePale
                )
            )
        }
    }
}

@Composable
private fun RingtoneCard(
    modifier: Modifier = Modifier,
    onRingtoneClick: () -> Unit,
    model: AlarmDetailScreenModel
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onRingtoneClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Alarm ringtone",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = model.ringtone,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Select ringtone"
            )
        }
    }
}

@Composable
private fun AlarmNameCard(
    modifier: Modifier = Modifier,
    onNameDialogShow: () -> Unit,
    model: AlarmDetailScreenModel
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onNameDialogShow() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Alarm Name",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = model.name ?: "Optional",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (model.name == null) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit name"
            )
        }
    }
}

@Composable
private fun RepeatDaysCard(
    modifier: Modifier = Modifier,
    model: AlarmDetailScreenModel,
    onRepeatDayToggled: (Long) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Repeat",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DayOfWeek.entries.forEach { day ->
                    val dayFlag = 1L shl day.ordinal
                    val isSelected = (model.repeatDays and dayFlag) != 0L
                    DayChip(
                        day = day,
                        isSelected = isSelected,
                        onClick = { onRepeatDayToggled(dayFlag) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeInputRow(
    modifier: Modifier = Modifier,
    model: AlarmDetailScreenModel,
    onHourChanged: (String) -> Unit,
    onMinuteChanged: (String) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeInput(
            value = model.hour,
            onValueChange = onHourChanged,
            placeholder = "00"
        )
        Text(
            text = ":",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        TimeInput(
            value = model.minute,
            onValueChange = onMinuteChanged,
            placeholder = "00"
        )
    }
}

@Composable
private fun TimeInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.displayLarge
            )
        },
        textStyle = MaterialTheme.typography.displayLarge.copy(textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier.width(120.dp)
    )
}

@Composable
private fun DayChip(
    modifier: Modifier = Modifier,
    day: DayOfWeek,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(RoundedCornerShape(38.dp))
            .background(if (isSelected) SnoozelooBlue else SnoozelooBluePale)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val dayText = day.name.take(2).let { it[0] + it[1].lowercase() }
        Text(
            text = dayText,
            color = if (isSelected) SnoozelooWhite else SnoozelooBlack,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

