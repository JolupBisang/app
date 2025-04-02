package com.imhungry.jjongseol.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.imhungry.jjongseol.ui.home.HomeScreen
import com.imhungry.jjongseol.ui.login.LoginScreen
import com.imhungry.jjongseol.ui.meeting.MeetingScreen
import com.imhungry.jjongseol.ui.meeting.MeetingWaitingScreen
import com.imhungry.jjongseol.ui.splash.SplashScreen
import com.imhungry.jjongseol.viewmodel.LoginViewModel
import com.imhungry.jjongseol.viewmodel.MeetingViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SilRokNavGraph(
    startDestination: SilRokNavigation = SilRokNavigation.Splash,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val navActions = remember(navController) { SilRokNavigationActions(navController) }

    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier
    ) {
        composable(SilRokNavigation.Splash.route) {
            SplashScreen(
                onFinish = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(SilRokNavigation.Login.route) {
            val context = LocalContext.current
            val loginViewModel: LoginViewModel = hiltViewModel()

            LoginScreen(
                loginViewModel = loginViewModel,
                onGoogleClick = { loginViewModel.launchGoogleLogin(context) },
                onLoginSuccess = { navActions.navigateTo(SilRokNavigation.Home) }
            )
        }

        composable(SilRokNavigation.Home.route) {
            HomeScreen(navController)
        }

        composable(SilRokNavigation.CreateNewMeeting.route) {
            CreateNewMeetingScreen(navController)
        }

        composable(SilRokNavigation.Meeting.route) {
            val meetingViewModel: MeetingViewModel = hiltViewModel()

            MeetingScreen(meetingViewModel)
        }

        composable(SilRokNavigation.MeetingWaiting.route) {
            MeetingWaitingScreen(navController)
        }
    }
}