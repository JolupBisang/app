package com.imhungry.sillok.presentation.screen.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.state.login.LoginState
import com.imhungry.sillok.presentation.viewmodel.login.LoginViewModel
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.components.ExitDialog
import com.imhungry.sillok.ui.theme.SillokTheme
import com.imhungry.sillok.ui.theme.secondaryButton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
        if (token != null) {
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
        statusBarColor = Color(0xFFFEFCFE),
        navigationBarColor = Color(0xFFFEFCFE),
        backgroundColor = Color(0xFFFEFCFE),
        isLoading = state.isLoading
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.splash)
                        .decoderFactory(GifDecoder.Factory())
                        .build()
                ),
                contentDescription = "splash gif",
                modifier = Modifier.size(230.dp)
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

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    SillokTheme {
        BasicBox(
            statusBarColor = secondaryButton,
            navigationBarColor = secondaryButton,
            backgroundColor = secondaryButton,
            isLoading = false
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(100.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.sealog),
                        contentDescription = "Sealog",
                        modifier = Modifier.size(132.dp)
                            .padding(top = 100.dp)
                    )
                }
                Spacer(modifier = Modifier.height(60.dp))

                Text(
                    text = "로그인하고 바로 시작하세요",
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                GoogleLoginButton(
                    onClick = {  },
                    isLoading = false
                )
            }
        }
    }
}