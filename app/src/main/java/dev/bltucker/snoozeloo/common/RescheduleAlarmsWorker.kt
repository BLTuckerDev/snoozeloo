package dev.bltucker.snoozeloo.common

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.bltucker.snoozeloo.common.repositories.AlarmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class RescheduleAlarmsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "RescheduleAlarmsWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting alarm rescheduling after boot")

            val enabledAlarms = alarmRepository.getEnabledAlarms()

            enabledAlarms.forEach { alarm ->
                try {
                    val shouldBeActive = when {

                        // One-time alarms in the past should be disabled
                        alarm.repeatDays == 0L && alarm.nextScheduledTime < System.currentTimeMillis() -> {
                            Log.d(TAG, "Disabling passed one-time alarm ${alarm.id}")
                            alarmRepository.toggleAlarm(alarm.id, false)
                            false
                        }

                        // Handle snoozed alarms
                        alarm.snoozedUntil != null && alarm.snoozedUntil < System.currentTimeMillis() -> {
                            Log.d(
                                TAG,
                                "Snooze time passed for alarm ${alarm.id}, reverting to regular schedule"
                            )
                            // Clear snooze and revert to regular schedule
                            alarmRepository.clearSnooze(alarm.id)
                            true
                        }

                        else -> true
                    }

                    if (shouldBeActive) {
                        val updatedAlarm = alarmRepository.updateNextScheduledTime(alarm.id)
                        Log.d(
                            TAG,
                            "Rescheduling alarm ${alarm.id} for ${updatedAlarm.nextScheduledTime}"
                        )
                        alarmScheduler.scheduleAlarm(updatedAlarm)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error rescheduling individual alarm ${alarm.id}", e)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during alarm rescheduling", e)
            Result.retry()
        }
    }
}