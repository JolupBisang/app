package com.imhungry.jjongseol.feature.audio

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import javax.inject.Inject

class StreamingController @Inject constructor(
    private val application: Application
) {
    private val context get() = application.applicationContext

    fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startStreamingService(meetingId: Long, jwtToken: String): Boolean {
        if (!hasRecordAudioPermission()) return false

        val intent = Intent(context, AudioStreamingService::class.java).apply {
            putExtra("meetingId", meetingId)
            putExtra("jwtToken", jwtToken)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(intent)
        else
            context.startService(intent)

        return true
    }

    fun stopStreamingService(deleteLocalPackets: Boolean = false) {
        AudioStreamingService.getStreamer()?.stop(deleteLocalPackets)
        context.stopService(Intent(context, AudioStreamingService::class.java))
    }

    fun pauseEncoding() {
        AudioStreamingService.pauseEncoding()
    }

    fun resumeEncoding() {
        AudioStreamingService.resumeEncoding()
    }
}
