package com.imhungry.jjongseol.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.viewmodel.SplashViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinish: (SilRokNavigation) -> Unit
) {
    val viewModel: SplashViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        delay(1500)
        val destination = viewModel.getNavigationDestination()
        onFinish(destination)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF6E8)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo2),
            contentDescription = "splash image",
            modifier = Modifier.size(210.dp)
        )
    }
}
