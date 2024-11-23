package dev.bltucker.snoozeloo.alarmtrigger

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import dev.bltucker.snoozeloo.R
import dev.bltucker.snoozeloo.common.AlarmReceiverIntentFactory
import dev.bltucker.snoozeloo.common.theme.SnoozelooBlue
import dev.bltucker.snoozeloo.common.theme.SnoozelooBluePale
import dev.bltucker.snoozeloo.common.theme.SnoozelooTheme
import dev.bltucker.snoozeloo.common.theme.SnoozelooWhite

const val ALARM_TRIGGER_ROUTE = "alarm-trigger"
const val ALARM_ID_ARG = "alarmId"


fun NavGraphBuilder.alarmTriggerScreen(
    onDismiss: () -> Unit,
) {
    composable(
        route = "$ALARM_TRIGGER_ROUTE/{$ALARM_ID_ARG}",
        arguments = listOf(
            navArgument(ALARM_ID_ARG) {
                type = NavType.LongType
            }
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "snoozeloo://alarm-trigger/{$ALARM_ID_ARG}"
                action = AlarmReceiverIntentFactory.ALARM_TRIGGER_ACTION
            }
        )
    ) {
        val viewModel = hiltViewModel<AlarmTriggerViewModel>()
        val model = viewModel.observableModel.collectAsStateWithLifecycle()

        LifecycleStartEffect(Unit) {
            viewModel.onStart()

            onStopOrDispose{
                //nothing to do
            }
        }

        AlarmTriggerScreen(
            modifier = Modifier.fillMaxSize(),
            model = model.value,
            onTurnOff = {
                viewModel.onTurnOff()
                onDismiss()
            },
            onSnooze = {
                viewModel.onSnooze()
                onDismiss()
            }
        )
    }
}

@Composable
fun AlarmTriggerScreen(
    modifier: Modifier = Modifier,
    model: AlarmTriggerScreenModel,
    onTurnOff: () -> Unit,
    onSnooze: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            painter = painterResource(R.drawable.solid_alarm),
            contentDescription = "Alarm Icon",
            tint = SnoozelooBlue,
            modifier = Modifier.size(62.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = model.formattedTime,
            fontSize = 82.sp,
            fontWeight = FontWeight.Bold,
            color = SnoozelooBlue,
            textAlign = TextAlign.Center,
            lineHeight = 76.sp
        )

        if (!model.alarmName.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = model.alarmName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onTurnOff,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SnoozelooBlue,
                contentColor = SnoozelooWhite
            ),
            shape = RoundedCornerShape(30.dp)
        ) {
            Text(
                text = "Turn Off",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onSnooze,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = SnoozelooBlue,
                containerColor = SnoozelooBluePale
            ),
            border = BorderStroke(1.dp, SnoozelooBlue),
            shape = RoundedCornerShape(30.dp)
        ) {
            Text(
                text = "Snooze for 5 min",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun AlarmTriggerScreenPreview() {
    val previewModel = AlarmTriggerScreenModel(
        alarmId = 1L,
        alarmName = "Work",
        formattedTime = "10:00"
    )

    SnoozelooTheme {
        AlarmTriggerScreen(
            model = previewModel,
            onTurnOff = {},
            onSnooze = {}
        )
    }
}