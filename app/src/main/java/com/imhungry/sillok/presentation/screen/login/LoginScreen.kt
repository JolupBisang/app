package com.imhungry.sillok.presentation.screen.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.sillok.R
import com.imhungry.sillok.data.local.VoiceRecognitionStore
import com.imhungry.sillok.presentation.viewmodel.login.LoginViewModel
import com.imhungry.sillok.presentation.viewmodel.shared.SharedViewModel
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.components.ExitAppBackHandler
import com.imhungry.sillok.ui.theme.beige
import android.util.Log
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel? = null,
    voiceRecognitionStore: VoiceRecognitionStore? = null,
    onNavigateToVoiceRecognitionIntro: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    val context = LocalContext.current
    val loginState by loginViewModel.state.collectAsState()
    
    ExitAppBackHandler()

    LaunchedEffect(sharedViewModel) {
        sharedViewModel?.loginToken?.collect { token ->
            if (token != null && !loginState.isLoading) {
                loginViewModel.handleLogin(token)
            }
        }
    }

    LaunchedEffect(loginState.isLoginSuccess) {
        if (loginState.isLoginSuccess && voiceRecognitionStore != null) {
            val voiceState = voiceRecognitionStore.voiceRecognitionState.first()
            if (voiceState.isCompleted) {
                onNavigateToHome()
            } else {
                onNavigateToVoiceRecognitionIntro()
            }
            loginViewModel.resetLoginSuccess()
        }
    }

    BasicBox(
        statusBarColor = beige,
        navigationBarColor = beige,
        backgroundColor = beige
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(220.dp))
            
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(120.dp))

            Text(
                text = "로그인하고 바로 시작하세요",
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            GoogleLoginButton(
                onClick = { /*loginViewModel.launchGoogleOAuth()*/ onNavigateToHome() }, // onNavigateHome() 또는 onNavigateToVoiceRecognitionIntro()
                isLoading = loginState.isLoading
            )
        }
    }
}