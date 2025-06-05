package com.imhungry.jjongseol.data.network.config

import android.content.Context

class AppPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun isVoiceTutorialCompleted(): Boolean = prefs.getBoolean("voice_tutorial_completed", false)

    fun setVoiceTutorialCompleted() {
        prefs.edit().putBoolean("voice_tutorial_completed", true).apply()
    }

    fun isMeetingForegroundServiceRunning(): Boolean =
        prefs.getBoolean("meeting_fg_service_running", false)

    fun setMeetingForegroundServiceRunning(running: Boolean) {
        prefs.edit().putBoolean("meeting_fg_service_running", running).apply()
    }

    fun getRunningMeetingId(): Long =
        prefs.getLong("running_meeting_id", -1L)

    fun setRunningMeetingId(meetingId: Long) {
        prefs.edit().putLong("running_meeting_id", meetingId).apply()
    }

    fun clearRunningMeetingId() {
        prefs.edit().remove("running_meeting_id").apply()
    }
}
