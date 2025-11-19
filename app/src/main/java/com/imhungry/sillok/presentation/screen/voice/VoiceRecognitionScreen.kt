package com.imhungry.sillok.presentation.screen.voice

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.permission.PermissionHandler
import com.imhungry.sillok.presentation.screen.login.GoogleLoginButton
import com.imhungry.sillok.presentation.state.voice.RecordState
import com.imhungry.sillok.presentation.state.voice.VoiceRecognitionConstants
import com.imhungry.sillok.presentation.viewmodel.voice.VoiceRecognitionViewModel
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.theme.SillokTheme
import com.imhungry.sillok.ui.theme.Sunbatang
import com.imhungry.sillok.ui.theme.blackBackGround
import com.imhungry.sillok.ui.theme.brown100
import com.imhungry.sillok.ui.theme.brown400
import com.imhungry.sillok.ui.theme.danger
import com.imhungry.sillok.ui.theme.green300
import com.imhungry.sillok.ui.theme.green600
import com.imhungry.sillok.ui.theme.inverse
import com.imhungry.sillok.ui.theme.primarySurface
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.secondaryButton

@Composable
fun VoiceRecognitionScreen(
    onStopRecognition: () -> Unit,
    onComplete: () -> Unit,
    viewModel: VoiceRecognitionViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showPermissionRequest by remember { mutableStateOf(true) }

    if (showPermissionRequest) {
        PermissionHandler(
            onPermissionsGranted = {
                showPermissionRequest = false
            },
            onPermissionsDenied = { deniedPermissions ->
                Log.w("Permission", "거부된 권한: $deniedPermissions")
                showPermissionRequest = false
            }
        )
    }

    LaunchedEffect(state.currentStep, state.isProcessing) {
        if (state.currentStep > VoiceRecognitionConstants.TOTAL_STEPS && !state.isProcessing) {
            onComplete()
        }
    }

    BasicBox(
        statusBarColor = Color(0xFFFAFAF9),
        navigationBarColor = Color(0xFFFAFAF9),
        backgroundColor = Color(0xFFFAFAF9),
        isLoading = state.isProcessing
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "${state.currentStep}/${VoiceRecognitionConstants.TOTAL_STEPS}",
                    color = brown100,
                    fontFamily = Sunbatang,
                    fontWeight = FontWeight.Light,
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(modifier = Modifier.height(64.dp))

                when (state.recordState) {
                    is RecordState.Recording -> {
                        // 녹음 중일 때만 gif 표시
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(R.drawable.voicing)
                                    .decoderFactory(GifDecoder.Factory())
                                    .build()
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(140.dp)
                        )
                    }
                    is RecordState.TooShort -> {
                        // 녹음 시간이 너무 짧을 때 warning 이미지
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(R.drawable.warning)
                                    .decoderFactory(GifDecoder.Factory())
                                    .build()
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(140.dp)
                        )
                    }
                    else -> {
                        // 녹음 중이 아닐 때 정적 이미지 표시
                        Image(
                            painter = painterResource(id = R.drawable.voicing_img),
                            contentDescription = null,
                            modifier = Modifier.size(140.dp)
                        )
                    }
                }

                Text(
                    text = "휴대폰 마이크에 대고 다음과 같이 말씀해주세요.",
                    color = primarySurface,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = when (state.currentStep) {
                        1 -> "많고 많은 사람 중에"
                        2 -> "너무 맑고 초롱한"
                        3 -> "그대만큼 사랑스러운"
                        else -> ""
                    },
                    color = brown100,
                    fontFamily = Sunbatang,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when (state.currentStep) {
                        1 -> "그대 한 사람"
                        2 -> "그 중 하나 별이여"
                        3 -> "사람을 본 일 없다"
                        else -> ""
                    },
                    color = brown100,
                    fontFamily = Sunbatang,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (state.recordState) {
                    is RecordState.TooShort -> {
                        Text(
                            text = "녹음 시간이 너무 짧습니다.\n다시 녹음해주시길 바랍니다.",
                            color = danger,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SillokButton(
                            text = "다시 녹음하기",
                            onClick = { viewModel.retryRecording() },
                            modifier = Modifier,
                            backgroundColor = blackBackGround
                        )
                    }

                    is RecordState.Idle -> {
                        SillokButton(
                            text = "녹음 시작하기",
                            onClick = { viewModel.startRecording(context) },
                            modifier = Modifier,
                        )
                    }

                    is RecordState.Recording -> {
                        SillokButton(
                            text = "완료하기",
                            onClick = { viewModel.stopRecording() },
                            modifier = Modifier,
                            backgroundColor = blackBackGround
                        )
                    }

                    is RecordState.Recorded -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            SillokButton(
                                text = "다시 녹음하기",
                                onClick = { viewModel.retryRecording() },
                                modifier = Modifier.weight(1f),
                                backgroundColor = blackBackGround
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            SillokButton(
                                text = "다음",
                                onClick = { viewModel.nextStep() },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}
