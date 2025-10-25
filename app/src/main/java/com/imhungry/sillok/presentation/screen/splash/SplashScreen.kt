package com.imhungry.sillok.presentation.screen.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.viewmodel.splash.SplashViewModel
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.theme.beige
import android.util.Log

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
        }
    }
}