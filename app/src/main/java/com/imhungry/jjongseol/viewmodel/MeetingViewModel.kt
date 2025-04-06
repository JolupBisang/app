package com.imhungry.jjongseol.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import com.imhungry.jjongseol.service.AudioStreamingService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MeetingViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    private val context by lazy { application.applicationContext }

    fun pauseEncoding() = AudioStreamingService.pauseEncoding()
    fun resumeEncoding() = AudioStreamingService.resumeEncoding()

    fun startStreamingService() {
        val prefs = context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        val alreadyStarted = prefs.contains("meetingStartedAt")
        if (!alreadyStarted) {
            prefs.edit()
                .putBoolean("isMeetingOngoing", true)
                .putLong("meetingStartedAt", System.currentTimeMillis())
                .apply()
        }

        val intent = Intent(context, AudioStreamingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(intent)
        else
            context.startService(intent)
    }

    fun stopStreamingService() {
        val prefs = context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("isMeetingOngoing", false)
            .remove("meetingStartedAt")
            .apply()

        context.stopService(Intent(context, AudioStreamingService::class.java))
    }
}
