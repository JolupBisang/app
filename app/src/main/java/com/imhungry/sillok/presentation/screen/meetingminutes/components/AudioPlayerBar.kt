package com.imhungry.sillok.presentation.screen.meetingminutes.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.viewmodel.meetingminutes.MeetingMinutesViewModel
import com.imhungry.sillok.ui.theme.primaryButton
import com.imhungry.sillok.ui.theme.whiteBackground
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun AudioPlayerBar(
    meetingMinutesViewModel: MeetingMinutesViewModel,
    currentPosition: Long,
    onPositionChange: (Long) -> Unit,
    modifier: Modifier,
    onPlayingChanged: (Boolean) -> Unit,
    onExternalSeek: (Long) -> Unit
) {
    val context = LocalContext.current
    val state by meetingMinutesViewModel.state.collectAsState()
    val audio = state.audio

    // ExoPlayer 생성 및 관리
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }
    
    // 오디오 URL이 변경될 때 ExoPlayer 업데이트
    LaunchedEffect(audio.presignedUrl) {
        if (audio.presignedUrl.isNotEmpty()) {
            try {
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
                exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(audio.presignedUrl)))
                exoPlayer.prepare()
            } catch (e: Exception) {
                android.util.Log.e("AudioPlayerBar", "오디오 로드 실패: ${e.message}", e)
            }
        }
    }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    var showSpeedSheet by remember { mutableStateOf(false) }
    var isPlayingState by remember { mutableStateOf(false) }
    // ExoPlayer 상태 업데이트용
    LaunchedEffect(Unit) {
        // duration 세팅
        while (duration == 0L) {
            duration = exoPlayer.duration.coerceAtLeast(0L)
            delay(100L)
        }
        // 재생 위치 계속 갱신
        while (true) {
            playbackPosition = exoPlayer.currentPosition
            delay(200L)
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            playbackPosition = exoPlayer.currentPosition
            onPositionChange(playbackPosition)
            delay(500L)
        }
    }
    LaunchedEffect(currentPosition) {
        if (abs(playbackPosition - currentPosition) > 300) {
            exoPlayer.seekTo(currentPosition)
            playbackPosition = currentPosition
        }
    }

    DisposableEffect(Unit) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlayingState = isPlayingNow
                isPlaying = isPlayingNow
                onPlayingChanged(isPlayingNow)  // 상태 전달
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == ExoPlayer.STATE_ENDED) {
                    isPlaying = false // 재생이 끝나면 false로 설정
                    onPlayingChanged(false)
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.release()
        }
    }

    Column(
        modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        CustomSeekBar(
            currentPosition = playbackPosition.toFloat(),
            duration = duration.toFloat().coerceAtLeast(1f),
            onValueChange = {
                playbackPosition = it.toLong()
                exoPlayer.seekTo(it.toLong())
            }
        )
        Row(
            Modifier
                .fillMaxWidth()
                .background(whiteBackground)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                formatTime(playbackPosition),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp
            )
            Text(
                formatTime(duration),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                TextButton(
                    onClick = { showSpeedSheet = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "X ${playbackSpeed}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryButton,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 24.dp)
                    )
                }
            }

            Box(
                Modifier
                    .weight(2f),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.backward),
                        contentDescription = "5초 뒤로",
                        modifier = Modifier
                            .size(34.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                val seek = (exoPlayer.currentPosition - 5000).coerceAtLeast(0)
                                exoPlayer.seekTo(seek)
                                playbackPosition = seek
                            }
                    )
                    Spacer(Modifier.width(40.dp))
                    Image(
                        painter = painterResource(
                            id = if (isPlaying) R.drawable.playstop else R.drawable.play
                        ),
                        contentDescription = if (isPlaying) "pause" else "play",
                        modifier = Modifier
                            .size(34.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (exoPlayer.playbackState == ExoPlayer.STATE_ENDED) {
                                    exoPlayer.seekTo(0)
                                    exoPlayer.play()
                                } else if (exoPlayer.isPlaying) {
                                    exoPlayer.pause()
                                } else {
                                    exoPlayer.play()
                                }
                            }
                    )
                    Spacer(Modifier.width(37.dp))
                    Image(
                        painter = painterResource(R.drawable.forward),
                        contentDescription = "5초 앞으로",
                        modifier = Modifier
                            .size(37.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                val seek = (exoPlayer.currentPosition + 5000).coerceAtMost(duration)
                                exoPlayer.seekTo(seek)
                                playbackPosition = seek
                            }
                    )
                }
            }

            Box(
                Modifier
                    .weight(1f)
            ) { }

            if (showSpeedSheet) {
                PlaybackSpeedBottomSheet(
                    speed = playbackSpeed,
                    onSpeedChange = {
                        playbackSpeed = it
                        exoPlayer.setPlaybackSpeed(it)
                    },
                    onDismiss = { showSpeedSheet = false }
                )
            }
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0)
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    else
        String.format("%02d:%02d", minutes, seconds)
}
