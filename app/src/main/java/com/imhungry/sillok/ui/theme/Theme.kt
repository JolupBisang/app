package com.imhungry.sillok.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val DarkColorScheme = darkColorScheme(
    background = primaryBackground,
    surface = primaryBackground,
    onBackground = primaryTextColor,
    onSurface = primaryTextColor
)

private val LightColorScheme = lightColorScheme(
    background = primaryBackground,
    surface = primaryBackground,
    onBackground = primaryTextColor,
    onSurface = primaryTextColor
)

@Composable
fun SillokTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography
    ) {
        CompositionLocalProvider(
            LocalDensity provides Density(
                density = LocalDensity.current.density,
                fontScale = 1f
            )
        ) {
            content()
        }
    }
}