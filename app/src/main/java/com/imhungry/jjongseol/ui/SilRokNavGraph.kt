package com.imhungry.jjongseol.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.imhungry.jjongseol.ui.completedmeeting.CompletedMeetingScreen
import com.imhungry.jjongseol.ui.completedmeeting.pager.CompletedMeetingSummaryScreen
import com.imhungry.jjongseol.ui.home.HomeScreen
import com.imhungry.jjongseol.ui.login.LoginScreen
import com.imhungry.jjongseol.ui.meeting.MeetingEndScreen
import com.imhungry.jjongseol.ui.meeting.MeetingScreen
import com.imhungry.jjongseol.ui.meeting.MeetingWaitingScreen
import com.imhungry.jjongseol.ui.newmeeting.CompletedNewMeeting
import com.imhungry.jjongseol.ui.newmeeting.CreateNewMeetingScreen
import com.imhungry.jjongseol.ui.profilecard.CompletedProfile
import com.imhungry.jjongseol.ui.profilecard.MakeProfile
import com.imhungry.jjongseol.ui.splash.SplashScreen
import com.imhungry.jjongseol.viewmodel.LoginViewModel

@Composable
fun SilRokNavGraph(
    startDestination: SilRokNavigation,
    navController: NavHostController,
    loginViewModel: LoginViewModel,
) {
    val navActions = remember(navController) { SilRokNavigationActions(navController) }

    NavHost(
        navController = navController,
        startDestination = startDestination.route,
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
            LoginScreen(
                loginViewModel = loginViewModel,
                onGoogleClick = { loginViewModel.launchGoogleLogin(context) },
                onLoginSuccess = { navActions.navigateTo(SilRokNavigation.Home, SilRokNavigation.Splash) }
            )
        }

        composable(SilRokNavigation.Home.route) {
            HomeScreen(navController)
        }

       composable(SilRokNavigation.CreateNewMeeting.route) {
            CreateNewMeetingScreen(navController)
        }

        composable(SilRokNavigation.Meeting.route) {
            MeetingScreen(
                onFinish = { destination ->
                    navActions.navigateTo(destination, SilRokNavigation.Meeting)
                },
                meetingId = 1L
            )
        }

        composable(SilRokNavigation.MeetingWaiting.route) {
            MeetingWaitingScreen(
                onFinish = { destination ->
                    navActions.navigateTo(destination, SilRokNavigation.MeetingWaiting)
                }
            )
        }

        composable(SilRokNavigation.MeetingEnd.route) {
            MeetingEndScreen(
                onFinish = { destination ->
                navActions.navigateTo(destination, SilRokNavigation.MeetingEnd)
            })
        }

        composable(SilRokNavigation.MakeProfile.route) {
            MakeProfile(navController)
        }

        composable(SilRokNavigation.CompleteProfile.route) {
            CompletedProfile(navController)
        }

        composable(SilRokNavigation.CompleteNewMeeting.route) {
            CompletedNewMeeting(navController)
        }

        composable(SilRokNavigation.CompletedMeeting.route) {
            CompletedMeetingScreen(
                navController,
                onFinish = { destination ->
                    navActions.navigateTo(destination, SilRokNavigation.CompletedMeeting)
                }
            )
        }

        composable(SilRokNavigation.CompletedMeetingSummary.route) {
            CompletedMeetingSummaryScreen()
        }
    }
}