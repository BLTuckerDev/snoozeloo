package dev.bltucker.snoozeloo

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import dev.bltucker.snoozeloo.alarmdetail.alarmDetailScreen
import dev.bltucker.snoozeloo.alarmdetail.navigateToAlarmDetail
import dev.bltucker.snoozeloo.alarmlist.ALARM_LIST_ROUTE
import dev.bltucker.snoozeloo.alarmlist.alarmListScreen
import dev.bltucker.snoozeloo.alarmtrigger.alarmTriggerScreen
import dev.bltucker.snoozeloo.ringtonesetting.RINGTONE_REQUEST_KEY
import dev.bltucker.snoozeloo.ringtonesetting.navigateToRingtoneSettings
import dev.bltucker.snoozeloo.ringtonesetting.ringtoneSettingScreen


@Composable
fun SnoozelooNavigationGraph(navController: NavHostController){
    NavHost(navController = navController,
        startDestination = ALARM_LIST_ROUTE){

        alarmListScreen(
            onNavigateToCreateAlarm = {
                navController.navigateToAlarmDetail(null)
            },
            onNavigateToEditAlarm = { alarmId ->
                navController.navigateToAlarmDetail(alarmId)
            }
        )

        alarmDetailScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToRingtoneSetting = { alarmId ->
                navController.navigateToRingtoneSettings(alarmId)
            }
        )

        alarmTriggerScreen(
            onDismiss = {
                navController.popBackStack()
            }
        )

        ringtoneSettingScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onRingtoneSelected = { ringtoneInfo ->
                navController.previousBackStackEntry?.savedStateHandle?.set(RINGTONE_REQUEST_KEY, ringtoneInfo.title)
                navController.popBackStack()
            }
        )
    }
}