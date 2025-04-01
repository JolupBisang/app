package com.imhungry.jjongseol.ui

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

sealed class SilRokNavigation(val route: String) {
    object Splash : SilRokNavigation("splash")
    object Login : SilRokNavigation("login")
    object Home : SilRokNavigation("home")
    object Meeting : SilRokNavigation("meeting")
}

class SilRokNavigationActions(private val navController: NavHostController) {
    fun navigateTo(destination: SilRokNavigation) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}
