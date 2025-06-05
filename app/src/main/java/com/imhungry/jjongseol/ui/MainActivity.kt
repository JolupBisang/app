package com.imhungry.jjongseol.ui

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import com.imhungry.jjongseol.data.network.config.AppPrefs
import com.imhungry.jjongseol.service.MeetingSseService
import com.imhungry.jjongseol.ui.theme.AppTheme
import com.imhungry.jjongseol.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    private val startDestinationState = mutableStateOf(SilRokNavigation.Splash)
    private lateinit var appPrefs: AppPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        appPrefs = AppPrefs(applicationContext)

        if (appPrefs.isMeetingForegroundServiceRunning()) {
            val isRunning = isServiceRunning(this, MeetingSseService::class.java)
            if (!isRunning) {
                appPrefs.setMeetingForegroundServiceRunning(false)
                appPrefs.clearRunningMeetingId()
            }
        }

        handleIntent(intent)

        setContent {
            AppTheme {
                SilRokApp(startDestinationState, loginViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
        startDestinationState.value = SilRokNavigation.Splash
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.getQueryParameter("token")?.let { token ->
            loginViewModel.onLoginSuccess(token)
        }
    }

    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
