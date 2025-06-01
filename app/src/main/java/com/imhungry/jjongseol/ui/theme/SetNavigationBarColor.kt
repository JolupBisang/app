package com.imhungry.jjongseol.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

@Composable
fun SetNavigationBarColor(color: Color) {
    val view = LocalView.current
    LaunchedEffect(color) {
        val activity = view.context as? Activity ?: return@LaunchedEffect
        activity.window.navigationBarColor = color.toArgb()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.window.isNavigationBarContrastEnforced = false
        }
    }
}
