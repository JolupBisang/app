package com.imhungry.jjongseol.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.navigation.compose.rememberNavController
import com.imhungry.jjongseol.viewmodel.LoginViewModel

@Composable
fun SilRokApp(
    startDestinationState: State<SilRokNavigation>,
    loginViewModel: LoginViewModel
) {
    val navController = rememberNavController()

    SilRokNavGraph(
        startDestination = startDestinationState.value,
        navController = navController,
        loginViewModel = loginViewModel
    )
}
