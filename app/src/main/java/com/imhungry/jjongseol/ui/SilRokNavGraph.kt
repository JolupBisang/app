package com.imhungry.jjongseol.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.imhungry.jjongseol.ui.completedmeeting.CompletedMeetingScreen
import com.imhungry.jjongseol.ui.completedmeeting.pager.CompletedMeetingSummaryScreen
import com.imhungry.jjongseol.ui.home.HomeScreen
import com.imhungry.jjongseol.ui.learningvoice.LearningVoiceFirstScreen
import com.imhungry.jjongseol.ui.learningvoice.LearningVoiceLastScreen
import com.imhungry.jjongseol.ui.learningvoice.RecordingVoiceScreen
import com.imhungry.jjongseol.ui.login.LoginScreen
import com.imhungry.jjongseol.ui.meeting.MeetingEndScreen
import com.imhungry.jjongseol.ui.meeting.MeetingScreen
import com.imhungry.jjongseol.ui.meeting.MeetingWaitingScreen
import com.imhungry.jjongseol.ui.meetingdetail.MeetingDetailEditScreen
import com.imhungry.jjongseol.ui.newmeeting.CompletedNewMeeting
import com.imhungry.jjongseol.ui.newmeeting.CreateNewMeetingScreen
import com.imhungry.jjongseol.ui.profilecard.CompletedProfile
import com.imhungry.jjongseol.ui.profilecard.MakeProfile
import com.imhungry.jjongseol.ui.splash.SplashScreen
import com.imhungry.jjongseol.viewmodel.AgendaViewModel
import com.imhungry.jjongseol.viewmodel.LoginViewModel
import com.imhungry.jjongseol.viewmodel.MeetingViewModel

@Composable
fun SilRokNavGraph(
    startDestination: SilRokNavigation,
    navController: NavHostController,
    loginViewModel: LoginViewModel,
    agendaViewModel: AgendaViewModel,
    meetingViewModel: MeetingViewModel
) {
    val navActions = remember(navController) { SilRokNavigationActions(navController) }

    NavHost(
        navController = navController,
        startDestination = startDestination.route,
    ) {
        composable(SilRokNavigation.Splash.route) {
            SplashScreen(
                navController = navController
            )
        }

        composable(SilRokNavigation.Login.route) {
            val context = LocalContext.current
            LoginScreen(
                loginViewModel = loginViewModel,
                onGoogleClick = { loginViewModel.launchGoogleLogin(context) },
                navController = navController
            )
        }

        composable(SilRokNavigation.Home.route) {
            HomeScreen(navController)
        }

       composable(SilRokNavigation.CreateNewMeeting.route) {
            CreateNewMeetingScreen(navController)
        }

        /*composable(SilRokNavigation.Meeting.route) {
            MeetingScreen(
                agendaViewModel = agendaViewModel,
                meetingViewModel = meetingViewModel,
                onFinish = { destination ->
                    navActions.navigateTo(destination, SilRokNavigation.Meeting)
                },
                meetingId = 1L
            )
        }*/

        /*composable(SilRokNavigation.MeetingWaiting.route) {
            MeetingWaitingScreen(
                loginViewModel = loginViewModel,
                agendaViewModel = agendaViewModel,
                meetingViewModel = meetingViewModel,
                onFinish = { destination ->
                    navActions.navigateTo(destination, SilRokNavigation.MeetingWaiting)
                },
                meetingId = 1L
            )
        }*/

       /*composable(SilRokNavigation.MeetingEnd.route) {
            MeetingEndScreen(
                meetingViewModel = meetingViewModel,
                onFinish = { destination ->
                navActions.navigateTo(destination, SilRokNavigation.MeetingEnd)
            })
        }*/

        composable(SilRokNavigation.MakeProfile.route) {
            MakeProfile(navController)
        }

        composable(SilRokNavigation.CompleteProfile.route) {
            CompletedProfile(navController)
        }

        composable(SilRokNavigation.CompleteNewMeeting.route) {
            CompletedNewMeeting(navController)
        }

        /*composable(SilRokNavigation.CompletedMeeting.route) {
            CompletedMeetingScreen(
                navController,
                onFinish = { destination ->
                    navActions.navigateTo(destination, SilRokNavigation.CompletedMeeting)
                }
            )
        }*/

        composable(SilRokNavigation.CompletedMeetingSummary.route) {
            CompletedMeetingSummaryScreen()
        }

        composable("meetingDetail/{id}") { backStackEntry ->
            val meetingId = backStackEntry.arguments?.getString("id")?.toLong() ?: return@composable
            MeetingDetailEditScreen(meetingId = meetingId, navController = navController)
        }

        composable("meetingRoute/waiting/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: return@composable
            MeetingWaitingScreen(
                loginViewModel = loginViewModel,
                onFinish = { destination ->
                    navActions.navigateTo(destination, SilRokNavigation.MeetingWaiting)
                },
                meetingViewModel = meetingViewModel,
                agendaViewModel = agendaViewModel,
                navController = navController,
                meetingId = id
            )
        }

        composable("meetingRoute/inprogress/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: return@composable
            MeetingScreen(
                loginViewModel = loginViewModel,
                meetingViewModel = meetingViewModel,
                agendaViewModel = agendaViewModel,
                onFinish = { destination ->
                    navActions.navigateTo(destination, SilRokNavigation.Meeting)
                },
                navController = navController,
                meetingId = id
            )
        }

        composable("meetingRoute/end/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: return@composable
            MeetingEndScreen(
                meetingViewModel = meetingViewModel,
                navController = navController,
                meetingId = id
            )
        }

        composable("meetingRoute/completed/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: return@composable
            CompletedMeetingScreen(
                navController = navController,
                meetingId = id
            )
        }

        composable(SilRokNavigation.LearningVoiceFirst.route) {
            LearningVoiceFirstScreen(navController)
        }

        composable(SilRokNavigation.RecordingVoice.route) {
            RecordingVoiceScreen(navController)
        }

        composable(SilRokNavigation.LearningVoiceLast.route) {
            LearningVoiceLastScreen(navController)
        }
    }
}