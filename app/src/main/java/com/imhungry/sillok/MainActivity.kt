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
import com.imhungry.sillok.data.local.TokenExpirationManager
import com.imhungry.sillok.presentation.navigation.SillokNavigation
import com.imhungry.sillok.ui.theme.SillokTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var loginToken by mutableStateOf<String?>(null)
    private var notificationMeetingId by mutableStateOf<Long?>(null)
    
    @Inject
    lateinit var tokenExpirationManager: TokenExpirationManager
    
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