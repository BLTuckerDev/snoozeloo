package dev.bltucker.snoozeloo.ringtonesetting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.bltucker.snoozeloo.common.RingtoneInfo
import dev.bltucker.snoozeloo.common.theme.SnoozeLooGreyBackground
import dev.bltucker.snoozeloo.common.theme.SnoozelooBlue
import dev.bltucker.snoozeloo.common.theme.SnoozelooTheme
import dev.bltucker.snoozeloo.common.theme.SnoozelooWhite

const val RINGTONE_SETTING_ROUTE = "ringtone-setting"
const val RINGTONE_SOURCE_ALARM_ID = "sourceAlarmId"

fun NavGraphBuilder.ringtoneSettingScreen(
    onNavigateBack: () -> Unit
) {
    composable(
        route = "$RINGTONE_SETTING_ROUTE/{$RINGTONE_SOURCE_ALARM_ID}",
        arguments = listOf(
            navArgument(RINGTONE_SOURCE_ALARM_ID) {
                type = NavType.LongType
            }
        )
    ) {
        val viewModel = hiltViewModel<RingtoneSettingViewModel>()
        val model by viewModel.observableModel.collectAsStateWithLifecycle()

        LifecycleStartEffect(Unit) {
            viewModel.onStart()

            onStopOrDispose {  }
        }

        RingtoneSettingScreen(
            model = model,
            onBackClick = onNavigateBack,
            onRingtoneSelected = viewModel::onRingtoneSelected
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RingtoneSettingScreen(
    modifier: Modifier = Modifier,
    model: RingtoneSettingScreenModel,
    onBackClick: () -> Unit,
    onRingtoneSelected: (RingtoneInfo) -> Unit
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SnoozeLooGreyBackground)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item{
                Spacer(modifier = Modifier.height(24.dp))
            }

            items(model.ringtones) { ringtone ->
                RingtoneItem(
                    ringtoneInfo = ringtone,
                    isSelected = ringtone == model.selectedRingtoneInfo,
                    onRingtoneSelected = onRingtoneSelected
                )
            }
        }
    }
}

@Composable
private fun RingtoneItem(
    ringtoneInfo: RingtoneInfo,
    isSelected: Boolean,
    onRingtoneSelected: (RingtoneInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onRingtoneSelected(ringtoneInfo) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SnoozelooWhite
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(SnoozeLooGreyBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (ringtoneInfo.uri == "silent") {
                        Icons.Outlined.NotificationsOff
                    } else {
                        Icons.Outlined.Notifications
                    },
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = ringtoneInfo.title,
                modifier = Modifier.weight(1f)
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(SnoozelooBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = SnoozelooWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RingtoneSettingScreenPreview() {
    val sampleRingtones = listOf(
        RingtoneInfo(
            uri = "silent",
            title = "Silent"
        ),
        RingtoneInfo(
            uri = "content://settings/system/alarm_alert",
            title = "Default (Bright Morning)"
        ),
        RingtoneInfo(
            uri = "content://media/internal/audio/media/123",
            title = "Bright Morning"
        ),
        RingtoneInfo(
            uri = "content://media/internal/audio/media/124",
            title = "Cuckoo Clock"
        ),
        RingtoneInfo(
            uri = "content://media/internal/audio/media/125",
            title = "Early Twilight"
        )
    )

    val previewModel = RingtoneSettingScreenModel(
        ringtones = sampleRingtones,
        selectedRingtoneInfo = sampleRingtones[1],
        isLoading = false
    )

    SnoozelooTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            RingtoneSettingScreen(
                model = previewModel,
                onBackClick = {},
                onRingtoneSelected = {}
            )
        }
    }
}
