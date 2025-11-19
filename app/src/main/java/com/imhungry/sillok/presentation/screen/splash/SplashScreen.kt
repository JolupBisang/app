package com.imhungry.sillok.presentation.screen.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.screen.login.GoogleLoginButton
import com.imhungry.sillok.presentation.viewmodel.splash.SplashViewModel
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.components.SystemBars
import com.imhungry.sillok.ui.theme.SillokTheme
import com.imhungry.sillok.ui.theme.beige
import com.imhungry.sillok.ui.theme.secondaryButton

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
        statusBarColor = Color(0xFFFEFCFE),
        navigationBarColor = Color(0xFFFEFCFE),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFEFCFE))
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.splash)
                        .decoderFactory(GifDecoder.Factory())
                        .build()
                ),
                contentDescription = "splash gif",
                modifier = Modifier.size(200.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    SillokTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFEFCFE))
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(R.drawable.splash)
                            .decoderFactory(GifDecoder.Factory())
                            .build()
                    ),
                    contentDescription = "splash gif",
                    modifier = Modifier.size(132.dp)
                )
            }
        }
    }
}