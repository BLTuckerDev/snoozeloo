package dev.bltucker.snoozeloo.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun needsNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
    }

    fun needsToShowAlarmPermissionSettings(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    fun shouldUseForegroundService(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
}