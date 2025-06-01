package com.imhungry.jjongseol.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.imhungry.jjongseol.viewmodel.AgendaViewModel
import com.imhungry.jjongseol.viewmodel.LoginViewModel
import com.imhungry.jjongseol.viewmodel.MeetingViewModel

@Composable
fun SilRokApp(
    startDestinationState: State<SilRokNavigation>,
    loginViewModel: LoginViewModel
) {
    val navController = rememberNavController()
    val agendaViewModel: AgendaViewModel = hiltViewModel()
    val meetingViewModel: MeetingViewModel = hiltViewModel()

    SilRokNavGraph(
        startDestination = startDestinationState.value,
        navController = navController,
        loginViewModel = loginViewModel,
        agendaViewModel = agendaViewModel,
        meetingViewModel = meetingViewModel,
    )
}
