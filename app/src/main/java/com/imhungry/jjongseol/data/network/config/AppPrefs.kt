package com.imhungry.jjongseol.data.network.config

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.imhungry.jjongseol.data.model.meeting.MeetingState

class AppPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

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

    fun saveMeetingStates(states: Map<Long, MeetingState>) {
        prefs.edit().putString("meeting_states", gson.toJson(states)).apply()
    }

    fun loadMeetingStates(): Map<Long, MeetingState> {
        val json = prefs.getString("meeting_states", null) ?: return emptyMap()
        val type = object : TypeToken<Map<Long, MeetingState>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }

    fun addOrUpdateMeetingState(
        context: Context,
        meetingId: Long,
        micEnabled: Boolean,
        startTime: Long,
        endTime: Long
    ) {
        val appPrefs = AppPrefs(context)
        val states = appPrefs.loadMeetingStates().toMutableMap()
        states[meetingId] = MeetingState(meetingId, micEnabled, startTime, endTime)
        appPrefs.saveMeetingStates(states)
    }

    fun updateMicStatus(context: Context, meetingId: Long, micEnabled: Boolean) {
        val appPrefs = AppPrefs(context)
        val states = appPrefs.loadMeetingStates().toMutableMap()
        val state = states[meetingId]
        if (state != null) {
            states[meetingId] = state.copy(micEnabled = micEnabled)
            appPrefs.saveMeetingStates(states)
        }
    }

    fun removeMeetingState(context: Context, meetingId: Long) {
        val appPrefs = AppPrefs(context)
        val states = appPrefs.loadMeetingStates().toMutableMap()
        states.remove(meetingId)
        appPrefs.saveMeetingStates(states)
    }

    fun scheduleMeetingAutoEnd(context: Context, meetingId: Long, endTime: Long) {
        val now = System.currentTimeMillis()
        val delay = endTime - now
        if (delay > 0) {
            Handler(Looper.getMainLooper()).postDelayed({

            }, delay)
        } else {

        }
    }
}
