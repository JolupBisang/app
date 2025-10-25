package com.imhungry.sillok.presentation.screen.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.sillok.ui.components.SillokTextButton
import com.imhungry.sillok.ui.theme.gray200
import com.imhungry.sillok.ui.theme.green500
import com.imhungry.sillok.ui.theme.primarySurface
import com.imhungry.sillok.ui.theme.tertiary
import kotlin.math.abs

@Composable
fun OngoingMeetingNotification(
    meetingTitle: String,
    meetingTime: String,
    onJoinMeeting: () -> Unit,
    onDeclineMeeting: () -> Unit,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    var isVisible by remember { mutableStateOf(true) }
    
    val animatedOffsetX by animateFloatAsState(
        targetValue = if (isVisible) offsetX else 1000f,
        animationSpec = tween(durationMillis = 20),
        label = "offsetX"
    )
    
    if (isVisible) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer(
                    translationX = animatedOffsetX
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            if (abs(offsetX) > 500f) {
                                isVisible = false
                                onDismiss()
                            } else {
                                offsetX = 0f
                            }
                        }
                    ) { _, dragAmount ->
                        offsetX += dragAmount.x
                    }
                }
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = green500
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "📍 진행 중인 회의가 있습니다",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 15.sp,
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 회의 제목
                Text(
                    text = meetingTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = gray200,
                )

                Spacer(modifier = Modifier.height(6.dp))
                
                // 회의 시간
                Text(
                    text = meetingTime,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = gray200,
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SillokTextButton(
                        text = "참여하지 않기",
                        onClick = {
                            isVisible = false
                            onDeclineMeeting()
                        },
                        textColor = tertiary,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    SillokTextButton(
                        text = "바로 참여하기",
                        onClick = onJoinMeeting,
                        textColor = primarySurface,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}