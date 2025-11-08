package com.imhungry.sillok.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

@Composable
fun SystemBars(
    statusBarColor: Color = Color.Transparent,
    navigationBarColor: Color = Color.Transparent,
) {
    val context = LocalContext.current
    DisposableEffect(statusBarColor, navigationBarColor) {
        val window = (context as? android.app.Activity)?.window
        if (window != null) {
            // 상태바 색상 설정
            window.statusBarColor = statusBarColor.toArgb()

            // 하단바 색상 설정
            window.navigationBarColor = navigationBarColor.toArgb()
        }

        onDispose {
            // 정리 작업이 필요한 경우 여기에 추가
        }
    }
}

@Composable
fun navigationBarColor(
    navigationBarColor: Color
) {
    val context = LocalContext.current
    DisposableEffect(navigationBarColor) {
        val window = (context as? android.app.Activity)?.window
        if (window != null) {
            // 하단바 색상 설정
            window.navigationBarColor = navigationBarColor.toArgb()
        }

        onDispose {
            // 정리 작업이 필요한 경우 여기에 추가
        }
    }
}