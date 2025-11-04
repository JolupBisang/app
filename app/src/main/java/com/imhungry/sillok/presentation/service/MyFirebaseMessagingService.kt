package com.imhungry.sillok.presentation.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.imhungry.sillok.MainActivity
import com.imhungry.sillok.R
import com.imhungry.sillok.data.local.NotificationHistoryStore
import com.imhungry.sillok.data.local.UserStore
import com.imhungry.sillok.presentation.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    @Inject
    lateinit var notificationHistoryStore: NotificationHistoryStore
    
    @Inject
    lateinit var userStore: UserStore
    
    private val TAG = "MyFirebaseMessagingService"
    
    override fun onCreate() {
        super.onCreate()
        // 알림 채널 생성
        NotificationHelper.createNotificationChannel(this)
        NotificationHelper.setHistoryStore(notificationHistoryStore)
    }
    
    /**
     * FCM 토큰이 생성되거나 갱신될 때 호출됩니다.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "새로운 FCM 토큰: $token")
        // Firestore에 토큰 저장
        saveTokenToFirestore(token)
    }
    
    /**
     * FCM 메시지를 수신했을 때 호출됩니다.
     * 
     * 참고: 이 메서드는 앱이 포그라운드에 있을 때만 호출됩니다.
     * 백그라운드/종료 상태에서 notification 페이로드가 있으면 시스템이 자동으로 알림을 표시합니다.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM 메시지 수신: ${remoteMessage.from}")
        
        // data 페이로드에서 meetingId와 meetingTitle 가져오기
        val meetingId = remoteMessage.data["meetingId"]?.toLongOrNull()
        val meetingTitle = remoteMessage.data["meetingTitle"] ?: "새로운 회의"
        
        if (meetingId != null) {
            // 알림 표시 (앱이 포그라운드에 있을 때만 이 코드가 실행됨)
            NotificationHelper.showNewMeetingNotification(
                context = this,
                meetingId = meetingId,
                meetingTitle = meetingTitle
            )
        } else {
            // notification 페이로드가 있고 data에 meetingId가 없는 경우
            remoteMessage.notification?.let { notification ->
                val title = "${notification.title}에 초대되었습니다" ?: "새로운 회의 초대"
                val body = "${notification.title}에 초대되었습니다." ?: "회의에 초대되었습니다"
                showNotification(title, body)
            }
        }
    }
    
    /**
     * Firestore에 FCM 토큰을 저장합니다.
     */
    private fun saveTokenToFirestore(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = userStore.user.first()
                val userId = user?.id
                
                if (userId != null && userId > 0) {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(userId.toString())
                        .update("fcmToken", token)
                        .addOnSuccessListener {
                            Log.d(TAG, "FCM 토큰 저장 성공: $token")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "FCM 토큰 저장 실패", e)
                        }
                } else {
                    Log.w(TAG, "사용자 ID를 찾을 수 없어 토큰을 저장하지 않습니다.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "FCM 토큰 저장 중 오류 발생", e)
            }
        }
    }
    
    /**
     * 기본 알림을 표시합니다.
     */
    private fun showNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
