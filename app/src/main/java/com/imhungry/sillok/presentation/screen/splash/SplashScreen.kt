package com.imhungry.sillok.presentation.screen.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.viewmodel.splash.SplashViewModel
import com.imhungry.sillok.ui.components.SystemBars
import com.imhungry.sillok.ui.theme.beige

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToVoiceRecognitionIntro: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            if (!state.isLoggedIn) {
                onNavigateToLogin()
            } else if (!state.isVoiceRecognitionCompleted) {
                onNavigateToVoiceRecognitionIntro()
            } else {
                onNavigateToHome()
            }
        }
    }

    SystemBars(
        statusBarColor = beige,
        navigationBarColor = beige,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(beige)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.splash),
                contentDescription = "splash",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}