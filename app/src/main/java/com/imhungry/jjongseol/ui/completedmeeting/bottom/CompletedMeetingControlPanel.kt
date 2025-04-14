package com.imhungry.jjongseol.ui.completedmeeting.bottom

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.component.PlaybackSpeedBottomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CompletedMeetingControlPanel(
    currentPosition: Float,
    duration: Float,
    onSeek: (Float) -> Unit,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    playbackSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isBottomSheetVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (isBottomSheetVisible) {
        PlaybackSpeedBottomSheet(
            speed = playbackSpeed,
            onSpeedChange = onSpeedChange,
            onDismiss = { isBottomSheetVisible = false }
        )
    }

    var internalPosition by remember { mutableStateOf(currentPosition) }

    LaunchedEffect(currentPosition) {
        internalPosition = currentPosition
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "X ${playbackSpeed}",
                color = Color(0xFF1E93EF),
                fontSize = 20.sp,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isBottomSheetVisible = true
                    }
            )

            Row(
                modifier = Modifier.weight(2f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.playprev),
                    contentDescription = "5초 전",
                    modifier = Modifier
                        .size(32.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    var isLongPressed = false
                                    val job = coroutineScope.launch {
                                        delay(500)
                                        isLongPressed = true
                                        while (true) {
                                            internalPosition = (internalPosition - 5f).coerceAtLeast(0f)
                                            onSeek(internalPosition)
                                            delay(500)
                                        }
                                    }

                                    val success = tryAwaitRelease()
                                    job.cancel()

                                    if (!isLongPressed) {
                                        internalPosition = (internalPosition - 5f).coerceAtLeast(0f)
                                        onSeek(internalPosition)
                                    }
                                }
                            )
                        }
                )

                Spacer(modifier = Modifier.width(14.dp))

                Image(
                    painter = painterResource(
                        id = if (isPlaying) R.drawable.pause else R.drawable.play
                    ),
                    contentDescription = if (isPlaying) "pause" else "play",
                    modifier = Modifier
                        .size(42.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onTogglePlay()
                        }
                )

                Spacer(modifier = Modifier.width(14.dp))

                Image(
                    painter = painterResource(id = R.drawable.playnext),
                    contentDescription = "5초 후",
                    modifier = Modifier
                        .size(32.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    var isLongPressed = false
                                    val job = coroutineScope.launch {
                                        delay(500)
                                        isLongPressed = true
                                        while (true) {
                                            internalPosition = (internalPosition + 5f).coerceAtMost(duration)
                                            onSeek(internalPosition)
                                            delay(500)
                                        }
                                    }

                                    val success = tryAwaitRelease()
                                    job.cancel()

                                    if (!isLongPressed) {
                                        internalPosition = (internalPosition + 5f).coerceAtMost(duration)
                                        onSeek(internalPosition)
                                    }
                                }
                            )
                        }
                )
            }

            Image(
                painter = painterResource(id = R.drawable.chat_bubble),
                contentDescription = "익명 채팅",
                modifier = Modifier
                    .size(26.dp)
                    .weight(1f),
                alignment = Alignment.CenterEnd
            )
        }
    }
}

