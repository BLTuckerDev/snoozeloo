package dev.bltucker.snoozeloo

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import dev.bltucker.snoozeloo.alarmlist.ALARM_LIST_ROUTE
import dev.bltucker.snoozeloo.alarmlist.alarmListScreen
import dev.bltucker.snoozeloo.alarmtrigger.alarmTriggerScreen


@Composable
fun SnoozelooNavigationGraph(navController: NavHostController){
    NavHost(navController = navController,
        startDestination = ALARM_LIST_ROUTE){

        alarmListScreen(
            onNavigateToCreateAlarm = {
                //TODO navigate
            }
        )

        alarmTriggerScreen(
            onDismiss = {
                navController.popBackStack()
            }
        )
    }
}