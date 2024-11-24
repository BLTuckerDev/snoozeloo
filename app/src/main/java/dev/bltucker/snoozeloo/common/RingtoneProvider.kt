package dev.bltucker.snoozeloo.common

import android.content.Context
import android.media.RingtoneManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class RingtoneInfo(
    val uri: String,
    val title: String
)

@Singleton
class RingtoneProvider @Inject constructor(private val ringtoneManager: RingtoneManager,
                                           @ApplicationContext private val context: Context) {
    fun getAvailableRingtones(): List<RingtoneInfo> {

        val ringtones = mutableListOf(
            RingtoneInfo(
                uri = "silent",
                title = "Silent"
            )
        )

        try {
            val cursor = ringtoneManager.cursor

            while (cursor.moveToNext()) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uri = ringtoneManager.getRingtoneUri(cursor.position).toString()

                ringtones.add(RingtoneInfo(uri, title))
            }

            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
            if (ringtones.size == 1) {
                val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ringtones.add(RingtoneInfo(
                    uri = defaultUri.toString(),
                    title = "Default"
                ))
            }
        }

        return ringtones
    }

    fun getDefaultRingtoneInfo(): RingtoneInfo{
        val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        return RingtoneInfo(
            uri = defaultUri.toString(),
            title = "Default"
        )
    }

    fun getCurrentRingtoneName(uri: String): String {
        if (uri == "silent") return "Silent"
        if (uri == "default") {
            val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            return getRingtoneName(defaultUri.toString())
        }
        return getRingtoneName(uri)
    }

    private fun getRingtoneName(uri: String): String {
        return try {
            val ringtone = RingtoneManager.getRingtone(context, android.net.Uri.parse(uri))
            ringtone.getTitle(context) ?: "Unknown"
        } catch (e: Exception) {
            e.printStackTrace()
            "Default"
        }
    }
}