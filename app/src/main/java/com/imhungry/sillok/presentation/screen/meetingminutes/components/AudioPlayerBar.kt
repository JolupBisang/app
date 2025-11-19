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
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
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
    val coroutineScope = rememberCoroutineScope()

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
    var isSeeking by remember { mutableStateOf(false) } // 시크 중인지 추적
    // ExoPlayer 상태 업데이트용
    LaunchedEffect(Unit) {
        // duration 세팅
        while (duration == 0L) {
            duration = exoPlayer.duration.coerceAtLeast(0L)
            delay(100L)
        }
        // 재생 위치 계속 갱신 및 콜백 호출
        while (true) {
            // 시크 중이 아닐 때만 업데이트 (시크 완료 후 안정화 시간)
            if (!isSeeking) {
                playbackPosition = exoPlayer.currentPosition
                onPositionChange(playbackPosition)
            }
            delay(500L)
        }
    }
    // currentPosition 변경 감지 (세그먼트 클릭 등 외부 시크 요청만 처리)
    // onPositionChange로 인한 자동 업데이트는 무시하기 위해 lastSeekedPosition 추적
    var lastSeekedPosition by remember { mutableStateOf(0L) }
    LaunchedEffect(currentPosition) {
        // onPositionChange로 인한 자동 업데이트는 무시
        // currentPosition이 실제로 외부에서 변경되었고, ExoPlayer 위치와 차이가 클 때만 시크
        val positionDiff = abs(exoPlayer.currentPosition - currentPosition)
        if (positionDiff > 500 && currentPosition != lastSeekedPosition) {
            // 시크 실행
            isSeeking = true
            exoPlayer.seekTo(currentPosition)
            playbackPosition = currentPosition
            onPositionChange(currentPosition)
            lastSeekedPosition = currentPosition
            // 시크 완료 후 안정화 시간 (300ms)
            coroutineScope.launch {
                //delay(300L)
                isSeeking = false
            }
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
            onValueChange = { newPosition ->
                // 드래그 중에는 UI만 업데이트 (시크는 드래그 종료 시에만)
                playbackPosition = newPosition.toLong()
            },
            onValueChangeFinished = { finalPosition ->
                // 드래그 종료 시에만 시크 실행
                isSeeking = true
                val seekPosition = finalPosition.toLong().coerceIn(0L, duration)
                exoPlayer.seekTo(seekPosition)
                playbackPosition = seekPosition
                onPositionChange(seekPosition)
                // 시크 완료 후 안정화 시간 (300ms)
                coroutineScope.launch {
                    //delay(300L)
                    isSeeking = false
                }
            }
        )
        Row(
            Modifier
                .fillMaxWidth()
                .background(whiteBackground)
                .padding(start = 12.dp, end = 12.dp, top = 2.dp),
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
                                isSeeking = true
                                val seek = (exoPlayer.currentPosition - 5000).coerceAtLeast(0)
                                exoPlayer.seekTo(seek)
                                playbackPosition = seek
                                onPositionChange(seek)
                                // 시크 완료 후 안정화 시간 (300ms)
                                coroutineScope.launch {
                                   // delay(300L)
                                    isSeeking = false
                                }
                            }
                    )
                    Spacer(Modifier.width(34.dp))
                    Image(
                        painter = painterResource(
                            id = if (isPlaying) R.drawable.playstop else R.drawable.play
                        ),
                        contentDescription = if (isPlaying) "pause" else "play",
                        modifier = Modifier
                            .size(37.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                when (exoPlayer.playbackState) {
                                    ExoPlayer.STATE_ENDED -> {
                                        // 재생이 끝난 경우 처음부터 다시 재생
                                        exoPlayer.seekTo(0)
                                        exoPlayer.play()
                                        // 즉시 UI 업데이트
                                        isPlaying = true
                                        onPlayingChanged(true)
                                    }
                                    ExoPlayer.STATE_IDLE -> {
                                        // 준비되지 않은 경우 재생 시도 (자동으로 prepare 후 재생)
                                        if (!exoPlayer.isPlaying) {
                                            exoPlayer.play()
                                            // 즉시 UI 업데이트
                                            isPlaying = true
                                            onPlayingChanged(true)
                                        }
                                    }
                                    ExoPlayer.STATE_BUFFERING, ExoPlayer.STATE_READY -> {
                                        // 준비 중이거나 준비 완료된 경우 재생/일시정지 토글
                                        if (exoPlayer.isPlaying) {
                                            exoPlayer.pause()
                                            // 즉시 UI 업데이트
                                            isPlaying = false
                                            onPlayingChanged(false)
                                        } else {
                                            exoPlayer.play()
                                            // 즉시 UI 업데이트
                                            isPlaying = true
                                            onPlayingChanged(true)
                                        }
                                    }
                                }
                            }
                    )
                    Spacer(Modifier.width(34.dp))
                    Image(
                        painter = painterResource(R.drawable.forward),
                        contentDescription = "5초 앞으로",
                        modifier = Modifier
                            .size(34.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                isSeeking = true
                                val seek = (exoPlayer.currentPosition + 5000).coerceAtMost(duration)
                                exoPlayer.seekTo(seek)
                                playbackPosition = seek
                                onPositionChange(seek)
                                // 시크 완료 후 안정화 시간 (300ms)
                                coroutineScope.launch {
                                    //delay(300L)
                                    isSeeking = false
                                }
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
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
