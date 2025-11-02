package com.imhungry.sillok.presentation.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.imhungry.sillok.data.local.TokenExpirationManager
import com.imhungry.sillok.presentation.screen.home.HomeScreen
import com.imhungry.sillok.presentation.screen.login.LoginScreen
import com.imhungry.sillok.presentation.screen.meetingdetail.MeetingDetailScreen
import com.imhungry.sillok.presentation.screen.splash.SplashScreen
import com.imhungry.sillok.presentation.screen.voice.CreateMeetingCompleteScreen
import com.imhungry.sillok.presentation.screen.voice.VoiceRecognitionCompleteScreen
import com.imhungry.sillok.presentation.screen.voice.VoiceRecognitionIntroScreen
import com.imhungry.sillok.presentation.screen.voice.VoiceRecognitionScreen
import com.imhungry.sillok.presentation.screen.waitingroom.WaitingRoomScreen
import com.imhungry.sillok.presentation.screen.meeting.MeetingInProgressScreen
import com.imhungry.sillok.presentation.screen.meetingform.MeetingFormScreen
import com.imhungry.sillok.presentation.screen.meetingminutes.MeetingMinutesScreen
import com.imhungry.sillok.ui.components.BackPressHandler

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login?token={token}") {
        fun createRoute(token: String? = null) = if (token != null) "login?token=$token" else "login"
    }
    object VoiceRecognitionIntro : Screen("voice_recognition_intro")
    object VoiceRecognition : Screen("voice_recognition")
    object VoiceRecognitionComplete : Screen("voice_recognition_complete")
    object Home : Screen("home")
    object MeetingForm : Screen("meeting_form")
    object EditMeetingForm : Screen("edit_meeting_form/{meetingId}") {
        fun createRoute(meetingId: Long) = "edit_meeting_form/$meetingId"
    }
    object CreateMeetingComplete : Screen("create_meeting_complete")
    object MeetingDetail : Screen("meeting_detail/{meetingId}") {
        fun createRoute(meetingId: Long) = "meeting_detail/$meetingId"
    }
    object WaitingRoom : Screen("waiting_room/{meetingId}") {
        fun createRoute(meetingId: Long) = "waiting_room/$meetingId"
    }
    object MeetingInProgress : Screen("meeting_in_progress/{meetingId}") {
        fun createRoute(meetingId: Long) = "meeting_in_progress/$meetingId"
    }
    object MeetingMinutesGeneration : Screen("meeting_minutes_generation/{meetingId}") {
        fun createRoute(meetingId: Long) = "meeting_minutes_generation/$meetingId"
    }
    object MeetingMinutes : Screen("meeting_minutes/{meetingId}") {
        fun createRoute(meetingId: Long) = "meeting_minutes/$meetingId"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SillokNavigation(
    navController: NavHostController = rememberNavController(),
    loginToken: String? = null,
    tokenExpirationManager: TokenExpirationManager? = null
) {
    // 딥링크로 받은 토큰이 있으면 로그인 화면으로 이동
    LaunchedEffect(loginToken) {
        if (loginToken != null) {
            navController.navigate(Screen.Login.createRoute(loginToken)) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    // 토큰 만료 처리
    LaunchedEffect(Unit) {
        tokenExpirationManager?.shouldNavigateToLogin?.collect { shouldNavigate ->
            if (shouldNavigate) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
                tokenExpirationManager.clearNavigationEvent()
            }
        }
    }
    
    BackPressHandler(navController = navController) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
        // 스플래시 화면
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToVoiceRecognitionIntro = {
                    navController.navigate(Screen.VoiceRecognitionIntro.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 로그인 화면
        composable(
            route = Screen.Login.route,
            arguments = listOf(navArgument("token") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token")
            LoginScreen(
                token = token,
                onNavigateToVoiceRecognitionIntro = {
                    navController.navigate(Screen.VoiceRecognitionIntro.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 음성 인식 시작 안내 화면
        composable(Screen.VoiceRecognitionIntro.route) {
            VoiceRecognitionIntroScreen(
                onStartRecognition = {
                    navController.navigate(Screen.VoiceRecognition.route)
                }
            )
        }
        
        // 음성 인식 화면
        composable(Screen.VoiceRecognition.route) {
            VoiceRecognitionScreen(
                onStopRecognition = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.VoiceRecognitionIntro.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 음성 인식 완료 안내 화면
        composable(Screen.VoiceRecognitionComplete.route) {
            VoiceRecognitionCompleteScreen(
                onProcessingComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.VoiceRecognitionIntro.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 홈 화면
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCreateMeeting = {
                    navController.navigate(Screen.MeetingForm.route)
                },
                onNavigateToMeetingDetail = { meetingId ->
                    navController.navigate(Screen.MeetingDetail.createRoute(meetingId))
                },
                onNavigagteToMeetingInProgress = { meetingId ->
                    navController.navigate(Screen.MeetingInProgress.createRoute(meetingId)) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToMeetingMinutes = { meetingId ->
                    navController.navigate(Screen.MeetingMinutes.createRoute(meetingId))
                }
            )
        }
        
        // 회의 생성 화면
        composable(Screen.MeetingForm.route) {
            MeetingFormScreen(
                onCreateMeeting = {
                    navController.navigate(Screen.CreateMeetingComplete.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // 회의 생성 완료 안내 화면
        composable(Screen.CreateMeetingComplete.route) {
            CreateMeetingCompleteScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.CreateMeetingComplete.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 회의 정보 조회 화면
        composable(
            route = Screen.MeetingDetail.route,
            arguments = listOf(navArgument("meetingId") { type = NavType.LongType })
        ) { backStackEntry ->
            val meetingId = backStackEntry.arguments?.getLong("meetingId") ?: 1L
            MeetingDetailScreen(
                meetingId = meetingId,
                onEditMeeting = {
                    navController.navigate(Screen.EditMeetingForm.createRoute(meetingId))
                },
                onStartMeeting = {
                    navController.navigate(Screen.WaitingRoom.createRoute(meetingId))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // 회의 정보 수정 화면
        composable(
            route = Screen.EditMeetingForm.route,
            arguments = listOf(navArgument("meetingId") { type = NavType.LongType })
        ) { backStackEntry ->
            val meetingId = backStackEntry.arguments?.getLong("meetingId") ?: 1L
            MeetingFormScreen(
                onCreateMeeting = {
                    navController.navigate(Screen.MeetingDetail.createRoute(meetingId)) {
                        popUpTo(Screen.MeetingDetail.route) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                },
                isEditMode = true,
                meetingId = meetingId
            )
        }
        
        // 대기실 화면
        composable(
            route = Screen.WaitingRoom.route,
            arguments = listOf(navArgument("meetingId") { type = NavType.LongType })
        ) { backStackEntry ->
            val meetingId = backStackEntry.arguments?.getLong("meetingId") ?: 1L
            WaitingRoomScreen(
                meetingId = meetingId,
                onStartMeeting = {
                    navController.navigate(Screen.MeetingInProgress.createRoute(meetingId)) {
                        popUpTo(Screen.WaitingRoom.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 회의 중 화면
        composable(
            route = Screen.MeetingInProgress.route,
            arguments = listOf(navArgument("meetingId") { type = NavType.LongType })
        ) { backStackEntry ->
            val meetingId = backStackEntry.arguments?.getLong("meetingId") ?: 1L
            MeetingInProgressScreen(
                meetingId = meetingId,
                onStopMeeting = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onCompleteMeeting = {
                    navController.navigate(Screen.MeetingMinutesGeneration.createRoute(meetingId)) {
                        popUpTo(Screen.MeetingInProgress.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 회의록 화면
        composable(
            route = Screen.MeetingMinutes.route,
            arguments = listOf(navArgument("meetingId") { type = NavType.LongType })
        ) { backStackEntry ->
            val meetingId = backStackEntry.arguments?.getLong("meetingId") ?: 1L
            MeetingMinutesScreen(
                meetingId = meetingId,
                onBackClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
    }
} 