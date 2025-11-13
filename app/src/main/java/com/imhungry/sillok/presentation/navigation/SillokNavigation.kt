package com.imhungry.sillok.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.imhungry.sillok.data.local.TokenExpirationManager
import com.imhungry.sillok.presentation.screen.home.HomeScreen
import com.imhungry.sillok.presentation.screen.login.LoginScreen
import com.imhungry.sillok.presentation.screen.meeting.MeetingInProgressScreen
import com.imhungry.sillok.presentation.screen.meetingdetail.MeetingDetailScreen
import com.imhungry.sillok.presentation.screen.meetingform.MeetingFormScreen
import com.imhungry.sillok.presentation.screen.meetingminutes.MeetingMinutesScreen
import com.imhungry.sillok.presentation.screen.folder.FolderDetailScreen
import com.imhungry.sillok.presentation.screen.folder.FolderFormScreen
import com.imhungry.sillok.presentation.screen.folder.FolderListScreen
import com.imhungry.sillok.presentation.screen.folder.FolderMeetingAddScreen
import com.imhungry.sillok.presentation.screen.notification.NotificationHistoryScreen
import com.imhungry.sillok.presentation.screen.splash.SplashScreen
import com.imhungry.sillok.presentation.screen.team.TeamDetailScreen
import com.imhungry.sillok.presentation.screen.team.TeamFormScreen
import com.imhungry.sillok.presentation.screen.team.TeamListScreen
import com.imhungry.sillok.presentation.screen.voice.CreateMeetingCompleteScreen
import com.imhungry.sillok.presentation.screen.voice.VoiceRecognitionCompleteScreen
import com.imhungry.sillok.presentation.screen.voice.VoiceRecognitionIntroScreen
import com.imhungry.sillok.presentation.screen.voice.VoiceRecognitionScreen
import com.imhungry.sillok.presentation.screen.waitingroom.WaitingRoomScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login?token={token}") {
        fun createRoute(token: String? = null) =
            if (token != null) "login?token=$token" else "login"
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

    object MeetingMinutes : Screen("meeting_minutes/{meetingId}") {
        fun createRoute(meetingId: Long) = "meeting_minutes/$meetingId"
    }

    object NotificationHistory : Screen("notification_history")
    object TeamList : Screen("team_list")
    object TeamForm : Screen("team_form")
    object TeamDetail : Screen("team_detail/{teamId}") {
        fun createRoute(teamId: Long) = "team_detail/$teamId"
    }
    object FolderList : Screen("folder_list")
    object FolderForm : Screen("folder_form")
    object FolderDetail : Screen("folder_detail/{folderId}?folderName={folderName}") {
        fun createRoute(folderId: Long, folderName: String = "") = 
            if (folderName.isNotEmpty()) {
                "folder_detail/$folderId?folderName=${java.net.URLEncoder.encode(folderName, "UTF-8")}"
            } else {
                "folder_detail/$folderId"
            }
    }
    object FolderMeetingAdd : Screen("folder_meeting_add/{folderId}") {
        fun createRoute(folderId: Long) = "folder_meeting_add/$folderId"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SillokNavigation(
    navController: NavHostController = rememberNavController(),
    loginToken: String? = null,
    tokenExpirationManager: TokenExpirationManager? = null,
    notificationMeetingId: Long? = null,
    onNotificationHandled: () -> Unit = {}
) {
    // 딥링크로 받은 토큰이 있으면 로그인 화면으로 이동
    LaunchedEffect(loginToken) {
        if (loginToken != null) {
            navController.navigate(Screen.Login.createRoute(loginToken)) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // 알림 클릭 시 회의 상세 화면으로 이동
    LaunchedEffect(notificationMeetingId) {
        notificationMeetingId?.let { meetingId ->
            navController.navigate(Screen.MeetingDetail.createRoute(meetingId)) {
                popUpTo(Screen.Home.route) { inclusive = false }
            }
            onNotificationHandled()
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

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
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
                },
                onComplete = {
                    navController.navigate(Screen.VoiceRecognitionComplete.route) {
                        popUpTo(Screen.VoiceRecognition.route) { inclusive = true }
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
                },
                onNavigateToNotificationHistory = {
                    navController.navigate(Screen.NotificationHistory.route)
                },
                onNavigateToTeamList = {
                    navController.navigate(Screen.TeamList.route)
                },
                onNavigateToMeetingMinutesFolder = {
                    navController.navigate(Screen.FolderList.route)
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
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.MeetingDetail.route) { inclusive = true }
                    }
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
                    navController.navigate(Screen.Home.route) {
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
                    navController.popBackStack()
                }
            )
        }

        // 알림 기록 화면
        composable(Screen.NotificationHistory.route) {
            NotificationHistoryScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // 팀 목록 화면
        composable(Screen.TeamList.route) {
            TeamListScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToCreateTeam = {
                    navController.navigate(Screen.TeamForm.route)
                },
                onNavigateToTeamDetail = { teamId ->
                    navController.navigate(Screen.TeamDetail.createRoute(teamId))
                },
                onNavigateToNotificationHistory = {
                    navController.navigate(Screen.NotificationHistory.route)
                }
            )
        }

        // 팀 생성 화면
        composable(Screen.TeamForm.route) {
            TeamFormScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onComplete = { teamName, memberEmails ->
                    // 팀 생성 완료 후 팀 목록으로 이동
                    navController.navigate(Screen.TeamList.route) {
                        popUpTo(Screen.TeamList.route) { inclusive = true }
                    }
                }
            )
        }

        // 팀 상세 화면
        composable(
            route = Screen.TeamDetail.route,
            arguments = listOf(navArgument("teamId") { type = NavType.LongType })
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getLong("teamId") ?: 1L
            TeamDetailScreen(
                teamId = teamId,
                onBackClick = {
                    navController.popBackStack()
                },
                onNotificationClick = {
                    navController.navigate(Screen.NotificationHistory.route)
                }
            )
        }

        // 회의록 폴더 화면
        composable(Screen.FolderList.route) {
            FolderListScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToCreateFolder = {
                    navController.navigate(Screen.FolderForm.route)
                },
                onNavigateToFolderDetail = { folderId, folderName ->
                    navController.navigate(Screen.FolderDetail.createRoute(folderId, folderName))
                },
                onNavigateToNotificationHistory = {
                    navController.navigate(Screen.NotificationHistory.route)
                }
            )
        }

        // 회의록 폴더 생성 화면
        composable(Screen.FolderForm.route) {
            FolderFormScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onComplete = {
                    // 폴더 생성 완료 후 폴더 목록으로 이동
                    navController.navigate(Screen.FolderList.route) {
                        popUpTo(Screen.FolderList.route) { inclusive = true }
                    }
                }
            )
        }

        // 회의록 폴더 상세 화면
        composable(
            route = Screen.FolderDetail.route,
            arguments = listOf(
                navArgument("folderId") { type = NavType.LongType },
                navArgument("folderName") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getLong("folderId") ?: 1L
            val folderName = backStackEntry.arguments?.getString("folderName") ?: ""
            FolderDetailScreen(
                folderId = folderId,
                folderName = folderName,
                onBackClick = {
                    navController.popBackStack()
                },
                onMeetingToggle = { meetingId, isSelected ->
                    // TODO: 회의 선택/해제 처리
                },
                onAddClick = {
                    navController.navigate(Screen.FolderMeetingAdd.createRoute(folderId))
                }
            )
        }

        // 회의록 폴더 회의 추가 화면
        composable(
            route = Screen.FolderMeetingAdd.route,
            arguments = listOf(navArgument("folderId") { type = NavType.LongType })
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getLong("folderId") ?: 1L
            FolderMeetingAddScreen(
                folderId = folderId,
                onBackClick = {
                    navController.popBackStack()
                },
                onComplete = {
                    navController.navigate(Screen.FolderDetail.createRoute(folderId)) {
                        popUpTo(Screen.FolderDetail.route) { inclusive = true }
                    }
                }
            )
        }
    }
}