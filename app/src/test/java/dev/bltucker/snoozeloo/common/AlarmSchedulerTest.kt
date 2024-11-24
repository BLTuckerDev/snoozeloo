package dev.bltucker.snoozeloo.common
import android.app.AlarmManager
import android.app.PendingIntent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import dev.bltucker.snoozeloo.common.room.AlarmEntity
import org.junit.Before
import org.junit.Test

class AlarmSchedulerTest {

    private lateinit var alarmManager: AlarmManager
    private lateinit var alarmSdkChecker: AlarmSdkChecker
    private lateinit var alarmReceiverIntentFactory: AlarmReceiverIntentFactory
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var mockAlarmInfoIntentFactory: AlarmInfoIntentFactory

    private val testAlarm = AlarmEntity(
        id = 123L,
        name = "Test Alarm",
        hour = 8,
        minute = 30,
        isEnabled = true,
        ringtone = "test_ringtone",
        volume = 75,
        vibrate = true,
        nextScheduledTime = 1234567890L
    )

    private val testPendingIntent = mockk<PendingIntent>()
    private val testAlarmInfoIntent = mockk<PendingIntent>()

    @Before
    fun setup() {
        alarmManager = mockk(relaxed = true)
        alarmSdkChecker = mockk()
        alarmReceiverIntentFactory = mockk()
        mockAlarmInfoIntentFactory = mockk()

        every { alarmReceiverIntentFactory.createAlarmReceiverPendingIntent(any()) } returns testPendingIntent
        every { mockAlarmInfoIntentFactory.createInfoPendingIntent(any()) } returns testAlarmInfoIntent

        alarmScheduler = AlarmScheduler(
            alarmManager = alarmManager,
            alarmSdkChecker = alarmSdkChecker,
            alarmReceiverIntentFactory = alarmReceiverIntentFactory,
            alarmInfoIntentFactory = mockAlarmInfoIntentFactory,
        )
    }

    @Test
    fun scheduleAlarm_beforeAndroidS_schedulesExactAlarm() {
        every { alarmSdkChecker.isAtLeastS() } returns false

        alarmScheduler.scheduleAlarm(testAlarm)

        verify {
            alarmReceiverIntentFactory.createAlarmReceiverPendingIntent(testAlarm)
            alarmSdkChecker.isAtLeastS()
            alarmManager.setAlarmClock(any(), any())
        }
    }

    @Test
    fun scheduleAlarm_afterAndroidS_withPermission_schedulesExactAlarm() {
        every { alarmSdkChecker.isAtLeastS() } returns true
        every { alarmManager.canScheduleExactAlarms() } returns true

        alarmScheduler.scheduleAlarm(testAlarm)

        verify {
            alarmReceiverIntentFactory.createAlarmReceiverPendingIntent(testAlarm)
            alarmSdkChecker.isAtLeastS()
            alarmManager.canScheduleExactAlarms()
            alarmManager.setAlarmClock(any(), any())
        }
    }

    @Test(expected = ExactAlarmPermissionException::class)
    fun scheduleAlarm_afterAndroidS_withoutPermission_throwsException() {
        every { alarmSdkChecker.isAtLeastS() } returns true
        every { alarmManager.canScheduleExactAlarms() } returns false

        alarmScheduler.scheduleAlarm(testAlarm)
    }

    @Test
    fun cancelAlarm_cancelsWithCorrectPendingIntent() {
        alarmScheduler.cancelAlarm(testAlarm)

        verify {
            alarmReceiverIntentFactory.createAlarmReceiverPendingIntent(testAlarm)
            alarmManager.cancel(testPendingIntent)
        }
    }
}