package com.imhungry.jjongseol.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.navigation.compose.rememberNavController

@Composable
fun SilRokApp(startDestinationState: State<SilRokNavigation>) {
    val navController = rememberNavController()

    SilRokNavGraph(
        startDestination = startDestinationState.value,
        navController = navController
    )
}