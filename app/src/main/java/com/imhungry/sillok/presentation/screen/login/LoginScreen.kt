package com.imhungry.sillok.presentation.screen.login

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.viewmodel.login.LoginViewModel
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.components.ExitDialog
import com.imhungry.sillok.ui.theme.Monda
import com.imhungry.sillok.ui.theme.beige

@Composable
fun LoginScreen(
    token: String? = null,
    loginViewModel: LoginViewModel = hiltViewModel(),
    onNavigateToVoiceRecognitionIntro: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    val context = LocalContext.current
    val state by loginViewModel.state.collectAsState()

    LaunchedEffect(token) {
        if (token != null && !state.isLoading) {
            loginViewModel.handleLogin(token)
        }
    }

    LaunchedEffect(state.isLoginSuccess) {
        if (state.isLoginSuccess) {
            if (state.isVoiceRecognitionCompleted) {
                onNavigateToHome()
            } else {
                onNavigateToVoiceRecognitionIntro()
            }
            loginViewModel.resetLoginSuccess()
        }
    }

    BackHandler(enabled = !state.showExitDialog) {
        loginViewModel.showExitDialog()
    }

    ExitDialog(
        visible = state.showExitDialog,
        onConfirm = {
            loginViewModel.dismissExitDialog()
        },
        onDismiss = { loginViewModel.dismissExitDialog() }
    )

    BasicBox(
        statusBarColor = beige,
        navigationBarColor = beige,
        backgroundColor = beige,
        isLoading = state.isLoading
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(100.dp)
            )
            Text(
                text = "sealog",
                fontFamily = Monda,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "로그인하고 바로 시작하세요",
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            GoogleLoginButton(
                onClick = { loginViewModel.launchGoogleOAuth(context) },
                isLoading = state.isLoading
            )
        }
    }
}