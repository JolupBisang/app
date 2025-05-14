package com.imhungry.jjongseol.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import com.imhungry.jjongseol.ui.theme.AppTheme
import com.imhungry.jjongseol.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    private val startDestinationState = mutableStateOf<SilRokNavigation>(SilRokNavigation.Splash)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        setContent {
            AppTheme {
                SilRokApp(startDestinationState)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)

        if (intent.getBooleanExtra("resumeMeeting", false)) {
            val prefs = getSharedPreferences("meeting_prefs", MODE_PRIVATE)
            prefs.edit().putBoolean("navigateToMeeting", true).apply()
        }
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.getQueryParameter("token")?.let { token ->
            loginViewModel.saveToken(token)
        }

        val prefs = getSharedPreferences("meeting_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("isMeetingOngoing", false)) {
            startDestinationState.value = SilRokNavigation.Meeting
        } else {
            startDestinationState.value = SilRokNavigation.Splash
        }
    }
}
