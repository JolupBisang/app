package com.imhungry.jjongseol.ui.splash

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.viewmodel.LoginViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinish: (SilRokNavigation) -> Unit,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isLoggedIn by loginViewModel.isLoggedIn.observeAsState()

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        val shouldNavigateToMeeting = prefs.getBoolean("navigateToMeeting", false)

        prefs.edit().remove("navigateToMeeting").apply()

        delay(1500)

        when {
            shouldNavigateToMeeting -> onFinish(SilRokNavigation.Meeting)
            isLoggedIn == true -> onFinish(SilRokNavigation.MeetingWaiting)
            else -> onFinish(SilRokNavigation.Login)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1EBE0)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "splash image",
            modifier = Modifier.size(210.dp)
        )
    }
}
