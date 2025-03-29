package com.imhungry.jjongseol.ui

import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.livedata.observeAsState
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

        setContent {
            AppTheme {
                val isLoggedIn = loginViewModel.isLoggedIn.observeAsState(false).value
                val startDestination = if (isLoggedIn) {
                    SilRokNavigation.Home
                } else {
                    SilRokNavigation.Login
                }
                SilRokApp(startDestination)
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
}
