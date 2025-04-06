package com.imhungry.jjongseol.ui

import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.imhungry.jjongseol.ui.theme.AppTheme
import com.imhungry.jjongseol.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        val startDestination = getStartDestination()

        setContent {
            AppTheme {
                SilRokApp(startDestination = startDestination)
            }
        }
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.getQueryParameter("token")?.let { token ->
            loginViewModel.saveToken(token)
        }
    }

    private fun getStartDestination(): SilRokNavigation {
        val prefs = getSharedPreferences("meeting_prefs", MODE_PRIVATE)
        return if (prefs.getBoolean("isMeetingOngoing", false)) {
            SilRokNavigation.Meeting
        } else {
            SilRokNavigation.Splash
        }
    }
}
