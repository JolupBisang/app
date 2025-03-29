package com.imhungry.jjongseol.ui

import androidx.compose.runtime.Composable
import com.imhungry.jjongseol.ui.theme.AppTheme

@Composable
fun SilRokApp(
    startDestination: SilRokNavigation
) {
    AppTheme {
        SilRokNavGraph(startDestination = startDestination)
    }
}
