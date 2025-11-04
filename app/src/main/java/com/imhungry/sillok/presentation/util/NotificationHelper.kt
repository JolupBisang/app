package com.imhungry.sillok.presentation.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.imhungry.sillok.MainActivity
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.navigation.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationHelper {
    const val CHANNEL_ID = "meeting_notification_channel"
    private const val CHANNEL_NAME = "회의 알림"
    private const val NOTIFICATION_ID_NEW_MEETING = 1001
    private var historyStore: com.imhungry.sillok.data.local.NotificationHistoryStore? = null

    /**
     * 알림 기록 저장소를 설정합니다 (DI로 주입)
     */
    fun setHistoryStore(store: com.imhungry.sillok.data.local.NotificationHistoryStore) {
        historyStore = store
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "새로운 회의 초대 알림을 받습니다"
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNewMeetingNotification(
        context: Context,
        meetingId: Long,
        meetingTitle: String
    ) {
        // 알림 채널 생성 (최초 1회만 실행)
        createNotificationChannel(context)

        // 알림 기록 저장
        historyStore?.let { store ->
            CoroutineScope(Dispatchers.IO).launch {
                store.addNotification(meetingId, meetingTitle)
            }
        }

        // 알림 클릭 시 회의 상세 화면으로 이동하는 Intent 생성
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("meetingId", meetingId)
            putExtra("navigation", Screen.MeetingDetail.createRoute(meetingId))
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            meetingId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 알림 빌더 생성
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // 앱 아이콘 사용
            .setContentTitle("새로운 회의 초대")
            .setContentText("${meetingTitle}에 초대되었습니다")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${meetingTitle}에 초대되었습니다.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // 알림 표시
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            meetingId.toInt(), // 각 회의마다 고유한 알림 ID 사용
            notification
        )
    }

    fun cancelNotification(context: Context, meetingId: Long) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(meetingId.toInt())
    }
    
    fun cancelAllNotifications(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}

