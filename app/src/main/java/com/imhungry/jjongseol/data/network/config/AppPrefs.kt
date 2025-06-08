package com.imhungry.jjongseol.data.network.config

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.imhungry.jjongseol.data.model.meeting.MeetingState
import com.imhungry.jjongseol.data.model.user.response.UserInfoResponse

class AppPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveMyProfile(userInfo: UserInfoResponse) {
        val json = gson.toJson(userInfo)
        prefs.edit().putString("my_profile", json).apply()
    }

    fun loadMyProfile(): UserInfoResponse? {
        val json = prefs.getString("my_profile", null) ?: return null
        return try {
            gson.fromJson(json, UserInfoResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

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

    fun setMicEnabled(meetingId: Long, enabled: Boolean) {
        prefs.edit().putBoolean("mic_enabled_$meetingId", enabled).apply()
    }

    fun getMicEnabled(meetingId: Long): Boolean {
        return prefs.getBoolean("mic_enabled_$meetingId", true)
    }

    fun setMeetingStartTime(meetingId: Long, startTimeMillis: Long) {
        prefs.edit().putLong("meeting_start_time_$meetingId", startTimeMillis).apply()
    }

    fun getMeetingStartTime(meetingId: Long): Long? {
        val millis = prefs.getLong("meeting_start_time_$meetingId", -1L)
        return if (millis == -1L) null else millis
    }
}
