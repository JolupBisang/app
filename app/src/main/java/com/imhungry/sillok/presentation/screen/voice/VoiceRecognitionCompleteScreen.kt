package com.imhungry.sillok.presentation.screen.voice

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.screen.login.GoogleLoginButton
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.components.SillokDialog
import com.imhungry.sillok.ui.theme.SillokTheme
import com.imhungry.sillok.ui.theme.blackBackGround
import com.imhungry.sillok.ui.theme.brown100
import com.imhungry.sillok.ui.theme.brown400
import com.imhungry.sillok.ui.theme.inverse
import com.imhungry.sillok.ui.theme.primarySurface
import com.imhungry.sillok.ui.theme.secondaryButton

@Composable
fun VoiceRecognitionCompleteScreen(
    onProcessingComplete: () -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    // 뒤로 가기 처리
    BackHandler(enabled = true) {
        showDialog = true
    }

    BasicBox(
        statusBarColor = inverse,
        navigationBarColor = inverse,
        backgroundColor = inverse
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(100.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.sealog),
                        contentDescription = "Sealog",
                        modifier = Modifier
                            .size(132.dp)
                            .padding(top = 100.dp)
                    )
                }
                Spacer(modifier = Modifier.height(110.dp))

            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "회의실록이 당신의 목소리를 기억합니다.",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = primarySurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "이제 서비스를 사용할 준비가 끝났어요!",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = primarySurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                SillokButton(
                    text = "완료",
                    onClick = onProcessingComplete,
                )
            }
        }

        SillokDialog(
            visible = showDialog,
            message = "목소리 학습을 완료하지 않고 나가시겠습니까?",
            confirmText = "예",
            cancelText = "취소",
            onConfirm = {
                showDialog = false
                // 확인 시 앱 종료
                (context as? Activity)?.finishAffinity()
            },
            onDismiss = {
                showDialog = false
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VoiceRecognitionCompleteScreenScreenPreview() {
    SillokTheme {
        BasicBox(
            statusBarColor = inverse,
            navigationBarColor = inverse,
            backgroundColor = inverse
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(100.dp)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.sealog),
                            contentDescription = "Sealog",
                            modifier = Modifier
                                .size(132.dp)
                                .padding(top = 100.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(110.dp))

                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "회의실록이 당신의 목소리를 기억합니다.",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = primarySurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "이제 서비스를 사용할 준비가 끝났어요!",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = primarySurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SillokButton(
                        text = "완료",
                        onClick = {  },
                    )
                }
            }
        }
    }
}