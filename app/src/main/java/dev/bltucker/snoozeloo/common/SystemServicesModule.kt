package dev.bltucker.snoozeloo.common

import android.app.AlarmManager
import android.content.Context
import android.media.RingtoneManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object SystemServicesModule {

    @Provides
    fun provdesAlarmManager(@ApplicationContext context: Context): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    @Provides
    fun provideRingtonManager(@ApplicationContext context: Context): RingtoneManager {
        val ringtoneManager = RingtoneManager(context)
        ringtoneManager.setType(RingtoneManager.TYPE_ALARM)
        return ringtoneManager
    }
}