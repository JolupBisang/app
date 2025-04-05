package com.imhungry.jjongseol.ui.meeting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.ui.component.TopSheet
import com.imhungry.jjongseol.ui.meeting.bottom.MeetingControlPanel
import com.imhungry.jjongseol.ui.meeting.pager.CheckItem

@OptIn(ExperimentalPagerApi::class)
@Composable
fun MeetingWaitingScreen(
    onFinish: (SilRokNavigation) -> Unit
) {
    val topicItems = listOf(
        "저메추", "지구는 평평한가?", "35세는 어린이인가?", "가르마 왼쪽 vs 오른쪽", "왼손잡이는 똑똑할까?"
    )

    val checkedStates = remember { mutableStateListOf(false, false, false, false, false) }
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
                items = topicItems,
                checkedStates = checkedStates,
                lastCheckedIndex = lastCheckedIndex,
                peekIndex = peekIndex,
                firstUncheckedIndex = firstUncheckedIndex
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
            onExitConfirmed = {}
        )
    }
}

@Composable
private fun MeetingContent(
    onFinish: (SilRokNavigation) -> Unit,
    items: List<String>,
    checkedStates: MutableList<Boolean>,
    lastCheckedIndex: MutableState<Int>,
    peekIndex: Int,
    firstUncheckedIndex: Int
) {
    Box(modifier = Modifier.fillMaxWidth()) {
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
                    text = items[peekIndex],
                    checked = checkedStates[peekIndex],
                    isFocused = !checkedStates[peekIndex],
                    onToggle = {
                        checkedStates[peekIndex] = !checkedStates[peekIndex]
                        if (checkedStates[peekIndex]) lastCheckedIndex.value = peekIndex
                    }
                )
            },
            content = {
                Column {
                    items.forEachIndexed { i, item ->
                        CheckItem(
                            text = item,
                            checked = checkedStates[i],
                            isFocused = !checkedStates[i] && firstUncheckedIndex == i,
                            onToggle = {
                                checkedStates[i] = !checkedStates[i]
                                if (checkedStates[i]) lastCheckedIndex.value = i
                            }
                        )
                    }
                }
            }
        )
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
