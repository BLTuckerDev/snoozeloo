package dev.bltucker.snoozeloo.common

import android.app.AlarmManager
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.os.Vibrator
import android.os.VibratorManager
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

    @Provides
    fun providePowerService(@ApplicationContext context: Context): PowerManager{
        return context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    @Provides
    fun provideVibrator(@ApplicationContext context: Context): Vibrator{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    @Provides
    fun provideAudioManager(@ApplicationContext context: Context): AudioManager{
        return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
}