package com.imhungry.sillok.presentation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.imhungry.sillok.R
import dagger.hilt.android.AndroidEntryPoint
import kotlin.jvm.JvmSuppressWildcards
import kotlin.jvm.functions.Function1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import com.imhungry.sillok.data.remote.realtime.RealtimeClient

@AndroidEntryPoint
class RealtimeService : Service() {

    @Inject lateinit var clientCreator: Function1<@JvmSuppressWildcards CoroutineScope, RealtimeClient>

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var client: RealtimeClient? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val meetingId = intent?.getLongExtra(EXTRA_MEETING_ID, 0L) ?: 0L
        if (meetingId == 0L) return START_NOT_STICKY

        startForeground(NOTI_ID, buildNotification())
        if (client == null) {
            client = clientCreator.invoke(serviceScope)
        }
        client?.start(meetingId)
        return START_STICKY
    }

    override fun onDestroy() {
        client?.stop()
        client = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_logo)
            .setContentTitle("회의 진행 중..")
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "회의 중 알림",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "회의 진행 중 SSE/웹소켓 연결을 유지합니다"
                setShowBadge(false)
            }
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "realtime"
        const val NOTI_ID = 1011
        const val EXTRA_MEETING_ID = "meeting_id"
    }
}


