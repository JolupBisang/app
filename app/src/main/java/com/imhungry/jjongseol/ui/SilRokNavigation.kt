package com.imhungry.jjongseol.ui

import androidx.navigation.NavHostController

sealed class SilRokNavigation(val route: String) {
    object Splash : SilRokNavigation("splash")
    object Login : SilRokNavigation("login")
    object Home : SilRokNavigation("home")
    object Meeting : SilRokNavigation("meeting")
    object CreateNewMeeting : SilRokNavigation("createNewMeeting")
    object MeetingWaiting : SilRokNavigation("meetingWaiting")
    object MeetingEnd: SilRokNavigation("meetingEnd")
    object MakeProfile : SilRokNavigation("makeProfile")
    object CompleteProfile : SilRokNavigation("completeProfile")
    object CompleteNewMeeting : SilRokNavigation("CompleteNewMeeting")
}

class SilRokNavigationActions(private val navController: NavHostController) {
    fun navigateTo(destination: SilRokNavigation, popUpTo: SilRokNavigation? = null) {
        navController.navigate(destination.route) {
            popUpTo?.let {
                popUpTo(it.route) {
                    inclusive = true
                }
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}
