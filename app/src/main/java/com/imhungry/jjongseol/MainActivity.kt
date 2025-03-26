package com.imhungry.jjongseol

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.imhungry.jjongseol.ui.LoginScreen
import com.imhungry.jjongseol.helper.rememberGoogleSignInLauncher
import com.imhungry.jjongseol.ui.theme.AppTheme
import com.imhungry.jjongseol.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var googleSignInClient: GoogleSignInClient
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val launcher = rememberGoogleSignInLauncher(loginViewModel)

            AppTheme {
                LoginScreen(
                    onGoogleClick = {
                        val intent = googleSignInClient.signInIntent
                        launcher.launch(intent)
                    }
                )
            }
        }
    }
}