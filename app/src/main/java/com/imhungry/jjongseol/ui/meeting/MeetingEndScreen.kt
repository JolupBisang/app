package com.imhungry.jjongseol.ui.meeting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.data.model.meeting.MeetingStatus
import com.imhungry.jjongseol.data.network.config.AppPrefs
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.ui.component.dialog.ErrorDialogHandler
import com.imhungry.jjongseol.ui.theme.Pretend
import com.imhungry.jjongseol.ui.theme.SetNavigationBarColor
import com.imhungry.jjongseol.ui.theme.inverseText
import com.imhungry.jjongseol.ui.theme.primaryBackground
import com.imhungry.jjongseol.ui.theme.primaryButton
import com.imhungry.jjongseol.ui.theme.primaryTextColor
import com.imhungry.jjongseol.ui.theme.whiteColor
import com.imhungry.jjongseol.viewmodel.AgendaViewModel
import com.imhungry.jjongseol.viewmodel.LoginViewModel
import com.imhungry.jjongseol.viewmodel.MeetingViewModel
import kotlinx.coroutines.delay

@Composable
fun MeetingEndScreen(
    loginViewModel: LoginViewModel,
    onFinish: (SilRokNavigation) -> Unit,
    meetingViewModel: MeetingViewModel,
    navController: NavController,
    meetingId: Long
) {
    val context = LocalContext.current
    val appPrefs = AppPrefs(context)
    val meetingStatus by meetingViewModel.meetingStatus.collectAsState()
    var isCompleted by remember { mutableStateOf(false) }
    val meetingError by meetingViewModel.errorMessage.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        meetingViewModel.updateMeetingStatus(meetingId, MeetingStatus.COMPLETED)
    }

    LaunchedEffect(meetingError) {
        appPrefs.setMeetingForegroundServiceRunning(true)
        appPrefs.setRunningMeetingId(meetingId)
        showDialog = true
    }

    LaunchedEffect(meetingStatus) {
        if (meetingStatus == MeetingStatus.COMPLETED) {
            appPrefs.setMeetingForegroundServiceRunning(false)
            appPrefs.clearRunningMeetingId()
            isCompleted = true
        }
    }

    SetNavigationBarColor(primaryBackground)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryBackground)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
    ) {
        ErrorDialogHandler(
            errorMessage = meetingError,
            showDialog = showDialog,
            onFinish = onFinish,
            clearError = { meetingViewModel.clearErrorMessage() },
            loginViewModel = loginViewModel
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 96.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.gif)
                        .decoderFactory(GifDecoder.Factory())
                        .build()
                ),
                contentDescription = "회의록 생성 중 gif",
                modifier = Modifier.size(140.dp)
            )
             Text(
                text = if (isCompleted) "회의록 생성 완료" else "회의록 생성 중",
                fontFamily = Pretend,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 28.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isCompleted) "회의 내용을 성공적으로 저장하였습니다." else "회의 내용을 생성하는 중입니다.",
                style = MaterialTheme.typography.labelMedium,
                color = primaryTextColor,
                textAlign = TextAlign.Center
            )
        }

        if (isCompleted) {
            Button(
                onClick = {
                    navController.navigate("meetingRoute/completed/$meetingId") {
                        popUpTo(0)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryButton,
                    contentColor = inverseText
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = null
            ) {
                Text(
                    text = "완료",
                    fontFamily = Pretend,
                    color = inverseText,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
