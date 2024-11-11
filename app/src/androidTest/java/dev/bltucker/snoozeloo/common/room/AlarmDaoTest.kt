package dev.bltucker.snoozeloo.common.room

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class AlarmDaoTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var alarmDao: AlarmDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        alarmDao = database.alarmDao()
    }

    @After
    fun cleanup() {
        database.close()
    }

    @Test
    fun insertAndRetrieveAlarm() = runTest {
        val alarm = AlarmEntity(
            name = "Test Alarm",
            hour = 8,
            minute = 30,
            isEnabled = true,
            repeatDays = 0L,
            ringtone = "default",
            volume = 50,
            vibrate = false,
            nextScheduledTime = System.currentTimeMillis() + 3600000
        )

        val id = alarmDao.insert(alarm)
        val retrievedAlarm = alarmDao.getAlarmById(id)!!

        assertNotNull(retrievedAlarm)
        assertEquals("Test Alarm", retrievedAlarm.name)
        assertEquals(8, retrievedAlarm.hour)
        assertEquals(30, retrievedAlarm.minute)
        assertTrue(retrievedAlarm.isEnabled)
    }

    @Test
    fun observeAllAlarms_returnsAlarmsInOrder() = runTest {
        val now = System.currentTimeMillis()
        val alarm1 = AlarmEntity(
            name = "First",
            hour = 9,
            minute = 0,
            isEnabled = true,
            nextScheduledTime = now + 7200000
        )
        val alarm2 = AlarmEntity(
            name = "Second",
            hour = 8,
            minute = 0,
            isEnabled = true,
            nextScheduledTime = now + 3600000
        )

        alarmDao.insert(alarm1)
        alarmDao.insert(alarm2)
        val alarms = alarmDao.observeAllAlarms().first()

        assertEquals(2, alarms.size)
        assertEquals("Second", alarms[0].name)
        assertEquals("First", alarms[1].name)
    }

    @Test
    fun updateAlarmEnabled() = runTest {

        val alarm = AlarmEntity(
            name = "Test Alarm",
            hour = 8,
            minute = 30,
            isEnabled = true,
            nextScheduledTime = System.currentTimeMillis() + 3600000
        )
        val id = alarmDao.insert(alarm)


        alarmDao.updateAlarmEnabled(id, false)
        val updatedAlarm = alarmDao.getAlarmById(id)!!

        assertNotNull(updatedAlarm)
        assertEquals(false, updatedAlarm.isEnabled)
    }

    @Test
    fun updateAlarmSnooze() = runTest {
        val now = System.currentTimeMillis()
        val alarm = AlarmEntity(
            name = "Test Alarm",
            hour = 8,
            minute = 30,
            isEnabled = true,
            nextScheduledTime = now + 3600000
        )
        val id = alarmDao.insert(alarm)

        val snoozedUntil = now + 300000
        val nextScheduledTime = now + 7200000
        alarmDao.updateAlarmSnooze(id, snoozedUntil, nextScheduledTime)
        val updatedAlarm = alarmDao.getAlarmById(id)!!

        assertNotNull(updatedAlarm)
        assertEquals(snoozedUntil, updatedAlarm.snoozedUntil)
        assertEquals(nextScheduledTime, updatedAlarm.nextScheduledTime)
    }

    @Test
    fun deleteAlarm() = runTest {
        val alarm = AlarmEntity(
            name = "Test Alarm",
            hour = 8,
            minute = 30,
            isEnabled = true,
            nextScheduledTime = System.currentTimeMillis() + 3600000
        )
        val id = alarmDao.insert(alarm)

        val retrievedAlarm = alarmDao.getAlarmById(id)!!
        assertNotNull(retrievedAlarm)
        alarmDao.delete(retrievedAlarm)

        val deletedAlarm = alarmDao.getAlarmById(id)
        assertNull(deletedAlarm)
    }

    @Test
    fun getEnabledAlarms() = runTest {
        val now = System.currentTimeMillis()
        val enabledAlarm = AlarmEntity(
            name = "Enabled",
            hour = 8,
            minute = 30,
            isEnabled = true,
            nextScheduledTime = now + 3600000
        )
        val disabledAlarm = AlarmEntity(
            name = "Disabled",
            hour = 9,
            minute = 0,
            isEnabled = false,
            nextScheduledTime = now + 7200000
        )

        alarmDao.insert(enabledAlarm)
        alarmDao.insert(disabledAlarm)
        val enabledAlarms = alarmDao.getEnabledAlarms()

        assertEquals(1, enabledAlarms.size)
        assertEquals("Enabled", enabledAlarms[0].name)
    }
}