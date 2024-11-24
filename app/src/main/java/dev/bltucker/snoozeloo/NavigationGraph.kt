package dev.bltucker.snoozeloo

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import dev.bltucker.snoozeloo.alarmdetail.alarmDetailScreen
import dev.bltucker.snoozeloo.alarmlist.ALARM_LIST_ROUTE
import dev.bltucker.snoozeloo.alarmlist.alarmListScreen
import dev.bltucker.snoozeloo.alarmtrigger.alarmTriggerScreen
import dev.bltucker.snoozeloo.ringtonesetting.RINGTONE_SETTING_ROUTE
import dev.bltucker.snoozeloo.ringtonesetting.RINGTONE_SOURCE_ALARM_ID
import dev.bltucker.snoozeloo.ringtonesetting.ringtoneSettingScreen


@Composable
fun SnoozelooNavigationGraph(navController: NavHostController){
    NavHost(navController = navController,
        startDestination = ALARM_LIST_ROUTE){

        alarmListScreen(
            onNavigateToCreateAlarm = {
                //TODO navigate
            }
        )

        alarmDetailScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToRingtoneSetting = { alarmId ->
                navController.navigate("$RINGTONE_SETTING_ROUTE?$RINGTONE_SOURCE_ALARM_ID=$alarmId")
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
            }
        )
    }
}