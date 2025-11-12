package com.imhungry.sillok

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.imhungry.sillok.data.local.DismissedMeetingStore
import com.imhungry.sillok.data.local.TokenExpirationManager
import com.imhungry.sillok.data.local.UserStore
import com.imhungry.sillok.presentation.navigation.SillokNavigation
import com.imhungry.sillok.ui.theme.SillokTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var loginToken by mutableStateOf<String?>(null)
    private var notificationMeetingId by mutableStateOf<Long?>(null)

    @Inject
    lateinit var tokenExpirationManager: TokenExpirationManager

    @Inject
    lateinit var userStore: UserStore

    @Inject
    lateinit var dismissedMeetingStore: DismissedMeetingStore

    companion object {
        private const val TAG = "MainActivity"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SillokTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SillokNavigation(
                        loginToken = loginToken,
                        tokenExpirationManager = tokenExpirationManager,
                        notificationMeetingId = notificationMeetingId,
                        onNotificationHandled = { notificationMeetingId = null }
                    )
                }
            }
        }

        // FCM 토큰 가져오기 (자동으로 FcmService.onNewToken 호출됨)
//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val token = task.result
//                Log.d(TAG, "FCM 토큰: $token")
//            } else {
//                Log.e(TAG, "FCM 토큰 가져오기 실패", task.exception)
//            }
//        }

        // User 데이터 로그 출력
        lifecycleScope.launch {
            userStore.user.collect { user ->
                if (user != null) {
                    Log.d(
                        TAG,
                        "User 데이터: id=${user.id}, email=${user.email}, nickname=${user.nickname}, profileImage=${user.pictureURL}"
                    )
                } else {
                    Log.d(TAG, "User 데이터: null")
                }
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        // 알림 클릭 시 회의 상세 화면으로 이동
        intent?.getLongExtra("meetingId", -1L)?.takeIf { it != -1L }?.let { meetingId ->
            notificationMeetingId = meetingId
        }

        // 기존 토큰 처리
        intent?.data?.getQueryParameter("token")?.let { token ->
            loginToken = token
        }
    }
}