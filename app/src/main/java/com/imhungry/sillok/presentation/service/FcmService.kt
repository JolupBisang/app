package com.imhungry.sillok.presentation.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.imhungry.sillok.MainActivity
import com.imhungry.sillok.R
import com.imhungry.sillok.data.local.TokenStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "FcmService"
        private const val CHANNEL_ID = "sillok_notifications"
    }

    @Inject
    lateinit var tokenStore: TokenStore

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "새로운 FCM 토큰: $token")
        saveTokenToDataStore(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "알림 수신: ${remoteMessage.notification?.title}")

        // 알림 표시
        remoteMessage.notification?.let { notification ->
            showNotification(
                title = notification.title ?: "",
                body = notification.body ?: "",
                data = remoteMessage.data
            )
        }

        // 데이터 처리
        remoteMessage.data.let { data ->
            handleNotificationData(data)
        }
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Notification Channel 생성 (Android 8.0 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sillok 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "회의 및 팀 초대 알림"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 클릭 시 이동할 Intent
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // 데이터 전달
            data.forEach { (key, value) ->
                when (key) {
                    "meetingId" -> putExtra("meetingId", value.toLongOrNull() ?: -1L)
                    "teamId" -> putExtra("teamId", value.toLongOrNull() ?: -1L)
                    else -> putExtra(key, value)
                }
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun handleNotificationData(data: Map<String, String>) {
        val type = data["type"]
        
        when (type) {
            "NEW_MEETING" -> {
                val meetingId = data["meetingId"]
                Log.d(TAG, "새로운 회의 초대: meetingId=$meetingId")
                // 필요시 특정 화면으로 이동하거나 상태 업데이트
            }
            "MEETING_STARTED" -> {
                val meetingId = data["meetingId"]
                Log.d(TAG, "회의 시작: meetingId=$meetingId")
                // 회의 진행 화면으로 이동
            }
            "NEW_TEAM" -> {
                val teamId = data["teamId"]
                Log.d(TAG, "팀 초대: teamId=$teamId")
                // 팀 화면으로 이동
            }
        }
    }

    private fun saveTokenToDataStore(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                tokenStore.saveFcmToken(token)
                Log.d(TAG, "FCM 토큰 DataStore 저장 완료")
            } catch (e: Exception) {
                Log.e(TAG, "FCM 토큰 DataStore 저장 실패", e)
            }
        }
    }
}

