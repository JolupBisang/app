package com.imhungry.jjongseol.controller

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import com.imhungry.jjongseol.service.AudioStreamingService
import javax.inject.Inject

class StreamingController @Inject constructor(
    private val application: Application
) {

    fun startStreamingService() {
        val prefs = application.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        if (!prefs.contains("meetingStartedAt")) {
            prefs.edit().putBoolean("isMeetingOngoing", true)
                .putLong("meetingStartedAt", System.currentTimeMillis()).apply()
        }

        val intent = Intent(application, AudioStreamingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            application.startForegroundService(intent)
        else
            application.startService(intent)
    }

    fun stopStreamingService() {
        application.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE).edit()
            .putBoolean("isMeetingOngoing", false)
            .remove("meetingStartedAt").apply()
        application.stopService(Intent(application, AudioStreamingService::class.java))
    }

    fun pauseEncoding() = AudioStreamingService.pauseEncoding()
    fun resumeEncoding() = AudioStreamingService.resumeEncoding()
}
