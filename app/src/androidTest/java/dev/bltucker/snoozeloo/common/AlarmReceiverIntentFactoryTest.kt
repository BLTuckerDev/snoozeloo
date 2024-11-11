package dev.bltucker.snoozeloo.common

import android.app.PendingIntent
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.bltucker.snoozeloo.common.AlarmReceiverIntentFactory.Companion.ALARM_TRIGGER_ACTION
import dev.bltucker.snoozeloo.common.AlarmReceiverIntentFactory.Companion.EXTRA_ALARM_ID
import dev.bltucker.snoozeloo.common.AlarmReceiverIntentFactory.Companion.EXTRA_ALARM_NAME
import dev.bltucker.snoozeloo.common.AlarmReceiverIntentFactory.Companion.EXTRA_ALARM_RINGTONE
import dev.bltucker.snoozeloo.common.AlarmReceiverIntentFactory.Companion.EXTRA_ALARM_VIBRATE
import dev.bltucker.snoozeloo.common.AlarmReceiverIntentFactory.Companion.EXTRA_ALARM_VOLUME
import dev.bltucker.snoozeloo.common.room.AlarmEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlarmReceiverIntentFactoryTest {

    private lateinit var context: Context
    private lateinit var factory: AlarmReceiverIntentFactory
    private lateinit var testAlarm: AlarmEntity

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        factory = AlarmReceiverIntentFactory(context)
        testAlarm = AlarmEntity(
            id = 123L,
            name = "Test Alarm",
            hour = 8,
            minute = 30,
            isEnabled = true,
            ringtone = "test_ringtone",
            volume = 75,
            vibrate = true,
            nextScheduledTime = System.currentTimeMillis() + 3600000
        )
    }

    @Test
    fun createAlarmIntent_setsCorrectAction() {
        val intent = factory.createAlarmIntent(testAlarm)
        assertEquals(ALARM_TRIGGER_ACTION, intent.action)
    }

    @Test
    fun createAlarmIntent_setsCorrectExtras() {
        val intent = factory.createAlarmIntent(testAlarm)

        assertEquals(testAlarm.id, intent.getLongExtra(EXTRA_ALARM_ID, -1))
        assertEquals(testAlarm.name, intent.getStringExtra(EXTRA_ALARM_NAME))
        assertEquals(testAlarm.volume, intent.getIntExtra(EXTRA_ALARM_VOLUME, -1))
        assertEquals(testAlarm.vibrate, intent.getBooleanExtra(EXTRA_ALARM_VIBRATE, false))
        assertEquals(testAlarm.ringtone, intent.getStringExtra(EXTRA_ALARM_RINGTONE))
    }

    @Test
    fun createAlarmPendingIntent_returnsValidPendingIntent() {
        val pendingIntent = factory.createAlarmPendingIntent(testAlarm)

        assertNotNull(pendingIntent)
        assertTrue(pendingIntent.isImmutable)
    }

    @Test
    fun createAlarmPendingIntent_uniqueForDifferentAlarms() {
        val pendingIntent1 = factory.createAlarmPendingIntent(testAlarm)

        val differentAlarm = testAlarm.copy(id = 456L)
        val pendingIntent2 = factory.createAlarmPendingIntent(differentAlarm)

        assert(!pendingIntent1.equals(pendingIntent2))
    }
}