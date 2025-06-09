package com.imhungry.jjongseol.ui.completedmeeting.component

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.theme.Pretend
import com.imhungry.jjongseol.ui.theme.tertiary
import com.imhungry.jjongseol.ui.theme.whiteColor

@Composable
fun AudioPlayerBar(
    audioUrl: String,
    modifier: Modifier
) {
    val context = LocalContext.current
    // ExoPlayer 생성 및 관리
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(audioUrl)))
            prepare()
        }
    }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    var showSpeedSheet by remember { mutableStateOf(false) }

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

    DisposableEffect(Unit) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
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
            },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            Modifier
                .fillMaxWidth()
                .background(whiteColor)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                formatTime(playbackPosition),
                fontFamily = Pretend,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Text(
                formatTime(duration),
                fontFamily = Pretend,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
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
                        fontFamily = Pretend,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = tertiary,
                        modifier = Modifier.padding(start = 20.dp)
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
                            .size(24.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                val seek = (exoPlayer.currentPosition - 5000).coerceAtLeast(0)
                                exoPlayer.seekTo(seek)
                                playbackPosition = seek
                            }
                    )
                    Spacer(Modifier.width(48.dp))
                    Image(
                        painter = painterResource(
                            id = if (isPlaying) R.drawable.playfalse else R.drawable.playtrue
                        ),
                        contentDescription = if (isPlaying) "pause" else "play",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            }
                    )
                    Spacer(Modifier.width(48.dp))
                    Image(
                        painter = painterResource(R.drawable.forward),
                        contentDescription = "5초 앞으로",
                        modifier = Modifier
                            .size(28.dp)
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
