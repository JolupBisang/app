package com.imhungry.jjongseol.ui.meeting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.data.model.agenda.AgendaDto
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.ui.component.CheckItem
import com.imhungry.jjongseol.ui.component.CustomDialog
import com.imhungry.jjongseol.ui.component.TopSheet
import com.imhungry.jjongseol.ui.meeting.bottom.MeetingControlPanel
import com.imhungry.jjongseol.viewmodel.MeetingViewModel

@OptIn(ExperimentalPagerApi::class)
@Composable
fun MeetingWaitingScreen(
    viewModel: MeetingViewModel = hiltViewModel(),
    onFinish: (SilRokNavigation) -> Unit,
    meetingId: Long = 1L
) {
    val agendas by viewModel.agendaItems.collectAsState()
    val checkedStates by viewModel.checkedStates.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAgendas(meetingId)
    }

    if (errorMessage != null) {
        showDialog.value = true
    }

    if (showDialog.value && errorMessage != null) {
        CustomDialog(
            description = if (errorMessage == "TOKEN_EXPIRED") "로그인 정보가 만료되었어요. 다시 로그인해주세요." else errorMessage,
            confirmText = if (errorMessage == "TOKEN_EXPIRED") "로그인 하기" else "홈으로",
            showDismissButton = false,
            onDismissRequest = {},
            onConfirmExit = {
                showDialog.value = false
                viewModel.clearErrorMessage()
                if (errorMessage == "TOKEN_EXPIRED") {
                    onFinish(SilRokNavigation.Login)
                } else {
                    onFinish(SilRokNavigation.Home)
                }
            }
        )
    }

    val lastCheckedIndex = remember { mutableStateOf(0) }
    val firstUncheckedIndex = checkedStates.indexOfFirst { !it }
    val peekIndex = if (firstUncheckedIndex == -1) lastCheckedIndex.value else firstUncheckedIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(modifier = Modifier.weight(0.70f)) {
            MeetingContent(
                onFinish = onFinish,
                agendas = agendas,
                checkedStates = checkedStates,
                peekIndex = peekIndex,
                firstUncheckedIndex = firstUncheckedIndex,
                viewModel = viewModel
            )
        }

        HorizontalPagerIndicator(
            pagerState = rememberPagerState(initialPage = 1),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 4.dp)
                .alpha(0f),
            activeColor = Color(0xFF1E93EF),
            inactiveColor = Color.LightGray,
            indicatorWidth = 6.dp,
            spacing = 4.dp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Divider(
                color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        MeetingControlPanel(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.20f),
            timeText = "00:00:00",
            micEnabled = false,
            micIcon = R.drawable.inactive_mic,
            logoutIcon = R.drawable.inactive_logout,
            powerIcon = R.drawable.inactive_power,
            onFinish = onFinish,
            onExitConfirmed = {},
            viewModel = viewModel
        )
    }
}

@Composable
private fun MeetingContent(
    onFinish: (SilRokNavigation) -> Unit,
    agendas: List<AgendaDto>,
    checkedStates: List<Boolean>,
    peekIndex: Int,
    firstUncheckedIndex: Int,
    viewModel: MeetingViewModel
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        val isLoading = agendas.isEmpty() || checkedStates.isEmpty() || peekIndex !in agendas.indices

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF86CC3B))
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(0.2f))
                Box(modifier = Modifier.weight(0.8f)) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "회의가 시작되길\n기다리는 중",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            color = Color.LightGray
                        )
                        StartButtonText { onFinish(SilRokNavigation.Meeting) }
                    }
                }
            }

            TopSheet(
                collapsedHeight = 60.dp,
                peekContent = {
                    CheckItem(
                        text = agendas[peekIndex].content,
                        checked = checkedStates[peekIndex],
                        isFocused = !checkedStates[peekIndex],
                        onToggle = { viewModel.toggleAgendaChecked(peekIndex) }
                    )
                },
                content = {
                    Column {
                        agendas.forEachIndexed { i, item ->
                            CheckItem(
                                text = item.content,
                                checked = checkedStates[i],
                                isFocused = !checkedStates[i] && firstUncheckedIndex == i,
                                onToggle = { viewModel.toggleAgendaChecked(i) }
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun StartButtonText(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val baseColor = Color(0xFF1E93EF)
    val pressedColor = baseColor.copy(alpha = 0.6f)

    Text(
        text = "시작하기",
        style = MaterialTheme.typography.titleLarge,
        color = if (isPressed) pressedColor else baseColor,
        modifier = Modifier
            .padding(top = 40.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    )
}
