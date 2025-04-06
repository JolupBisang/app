package com.imhungry.jjongseol.service

import android.app.Notification
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
import com.imhungry.jjongseol.data.audio.RealTimeAudioStreamer
import com.imhungry.jjongseol.data.network.WebSocketManager
import com.imhungry.jjongseol.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

class AudioStreamingService : Service() {
    private val CHANNEL_ID = "audio_streaming_channel"
    private val NOTIFICATION_ID = 1

    private lateinit var scope: CoroutineScope
    private var streamer: RealTimeAudioStreamer? = null

    companion object {
        private var instance: AudioStreamingService? = null

        fun pauseEncoding() = instance?.streamer?.pauseEncoding()
        fun resumeEncoding() = instance?.streamer?.resumeEncoding()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        scope = CoroutineScope(Dispatchers.IO)
        startForegroundWithNotification()

        val ws = WebSocketManager().apply {
            connect(
                url = "ws://your.server/ws/audio",
                onMessage = { Log.d("WebSocket", "서버 응답: $it") },
                onFailure = { Log.e("WebSocket", "연결 실패", it) }
            )
        }

        streamer = RealTimeAudioStreamer(
            userId = 1L,
            meetingId = 1L,
            webSocketManager = ws,
            cacheDir = cacheDir
        ).also {
            it.start(scope)
        }
    }

    override fun onDestroy() {
        streamer?.stop()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundWithNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Streaming Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "회의 음성을 전송하기 위한 Foreground Service"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("녹음 중..")
            .setSmallIcon(R.drawable.mic)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }
}
