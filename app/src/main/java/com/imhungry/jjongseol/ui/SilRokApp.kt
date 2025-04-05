package com.imhungry.jjongseol.ui

import androidx.compose.runtime.Composable

@Composable
fun SilRokApp(
    startDestination: SilRokNavigation
) {
    SilRokNavGraph(startDestination = startDestination)
}
