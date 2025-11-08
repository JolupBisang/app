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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.imhungry.sillok.R
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.components.SillokDialog
import com.imhungry.sillok.ui.theme.brown100
import com.imhungry.sillok.ui.theme.brown400
import com.imhungry.sillok.ui.theme.primarySurface

@Composable
fun VoiceRecognitionCompleteScreen(
    onProcessingComplete: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    // 뒤로 가기 처리
    BackHandler(enabled = true) {
        showDialog = true
    }

    BasicBox(
        statusBarColor = brown400,
        navigationBarColor = brown400,
        backgroundColor = brown400
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
                    text = "목소리 학습 완료",
                    color = brown100,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "회의실록이 당신의 목소리를 기억합니다.",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Normal,
                    color = primarySurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "이제 서비스를 사용할 준비가 끝났어요!",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Normal,
                    color = primarySurface,
                    textAlign = TextAlign.Center
                )
            }
            SillokButton(
                text = "완료",
                onClick = onProcessingComplete,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        SillokDialog(
            visible = showDialog,
            message = "목소리 학습을 완료하지 않고 나가시겠습니까?",
            confirmText = "예",
            cancelText = "취소",
            onConfirm = {
                showDialog = false
                // 확인 시 아무 동작도 하지 않음 (화면에 머무름)
            },
            onDismiss = {
                showDialog = false
            }
        )
    }
} 