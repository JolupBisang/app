package com.imhungry.sillok.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.imhungry.sillok.ui.theme.gradientBrush2

@Composable
fun BasicBox(
    statusBarColor: Color,
    navigationBarColor: Color,
    backgroundColor: Color,
    content: @Composable () -> Unit
) {
    SystemBars(
        statusBarColor = statusBarColor,
        navigationBarColor = navigationBarColor,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(20.dp)
    ) {
        content()
    }
}

@Composable
fun MeetingBasicBox(
    navigationBarColor: Color,
    backgroundColor: Color,
    content: @Composable () -> Unit
) {
    navigationBarColor(
        navigationBarColor = navigationBarColor
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        content()
    }
}

@Composable
fun HomeBasicBox(
    statusBarColor: Color,
    navigationBarColor: Color,
    backgroundColor: Color,
    gradientBrush: Brush = gradientBrush2,
    content: @Composable () -> Unit
){
    SystemBars(
        statusBarColor = statusBarColor,
        navigationBarColor = navigationBarColor,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .background(gradientBrush)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 12.dp)
    ) {
        content()
    }
}