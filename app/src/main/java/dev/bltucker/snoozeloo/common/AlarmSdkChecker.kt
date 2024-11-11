package dev.bltucker.snoozeloo.common

import android.os.Build
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmSdkChecker @Inject constructor(){

    fun isAtLeastS(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

}