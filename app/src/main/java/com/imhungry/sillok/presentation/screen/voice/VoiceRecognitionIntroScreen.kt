package com.imhungry.sillok.presentation.screen.voice

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.imhungry.sillok.R
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.components.ExitDialog
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.theme.blackBackGround
import com.imhungry.sillok.ui.theme.brown100
import com.imhungry.sillok.ui.theme.brown400
import com.imhungry.sillok.ui.theme.green400
import com.imhungry.sillok.ui.theme.green600
import com.imhungry.sillok.ui.theme.primarySurface

@Composable
fun VoiceRecognitionIntroScreen(
    onStartRecognition: () -> Unit
) {
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = !showExitDialog) {
        showExitDialog = true
    }

    ExitDialog(
        visible = showExitDialog,
        onConfirm = {
            showExitDialog = false
        },
        onDismiss = { showExitDialog = false }
    )

    BasicBox(
        statusBarColor = Color(0xFFFAFAF9),
        navigationBarColor = Color(0xFFFAFAF9),
        backgroundColor = Color(0xFFFAFAF9)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(110.dp))
                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(R.drawable.bubble)
                            .decoderFactory(GifDecoder.Factory())
                            .build()
                    ),
                    contentDescription = "말풍선 gif",
                    modifier = Modifier.size(140.dp)
                )
                Text(
                    buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = green400
                            ),
                        ) {
                            append("당신의 목소리")
                        }
                        append("를\n학습시키세요")
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = brown100,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "이 설정을 통해 회의 중 발화자를 구분할 수 있습니다.",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Normal,
                    color = primarySurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "조용한 환경에서 평소처럼 말해주시길 바랍니다.",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Normal,
                    color = primarySurface,
                    textAlign = TextAlign.Center
                )
            }
            SillokButton(
                text = "학습 시작하기",
                onClick = onStartRecognition,
                modifier = Modifier.align(Alignment.BottomCenter),
                backgroundColor = blackBackGround
            )
        }
    }
} 