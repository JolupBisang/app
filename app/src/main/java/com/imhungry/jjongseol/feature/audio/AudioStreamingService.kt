package com.imhungry.jjongseol.feature.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class AudioStreamingService : Service() {
    private val CHANNEL_ID = "audio_streaming_channel"
    private val NOTIFICATION_ID = 1

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var streamer: RealTimeAudioStreamer

    companion object {
        @Volatile private var instance: AudioStreamingService? = null

        fun pauseEncoding() = instance?.streamer?.pauseEncoding()
        fun resumeEncoding() = instance?.streamer?.resumeEncoding()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d("Audio", "서비스 onCreate 호출됨")
        startForegroundWithNotification()

        streamer = RealTimeAudioStreamer(
            context = applicationContext,
            userId = 1L,
            meetingId = 1L,
            webSocketManager = null,
            cacheDir = cacheDir
        )

        streamer.start(serviceScope)
        Log.d("Audio", "RealTimeAudioStreamer 시작됨")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
    }

    override fun onDestroy() {
        streamer.stop()
        serviceScope.cancel()
        instance = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundWithNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Streaming Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "오디오 스트리밍을 위한 포그라운드 서비스"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                putExtra("resumeMeeting", true)
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("회의 녹음 중")
            .setSmallIcon(R.drawable.mic)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }
}

