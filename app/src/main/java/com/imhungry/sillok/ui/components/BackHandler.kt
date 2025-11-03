package com.imhungry.sillok.ui.components

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController

@Composable
fun BackPressHandler(
    navController: NavHostController,
    message: String = "앱을 종료하시겠습니까?",
    confirmText: String = "예",
    cancelText: String = "취소",
    screenContent: @Composable () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    BackHandler {
        if (!navController.popBackStack()) {
            showDialog = true
        }
    }

    SillokDialogHost(
        showDialog = showDialog,
        onDismiss = {
            showDialog = false
        },
        onConfirm = {
            showDialog = false
            activity?.finish()
        },
        message = message,
        confirmText = confirmText,
        cancelText = cancelText,
        screenContent = screenContent
    )
}