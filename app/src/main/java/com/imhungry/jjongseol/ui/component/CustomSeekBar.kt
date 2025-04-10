package com.imhungry.jjongseol.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomSeekBar(
    currentPosition: Float,
    duration: Float,
    onValueChange: (Float) -> Unit
) {
    val thumbRadius = 6.dp
    val trackHeight = 2.dp

    val density = LocalDensity.current
    val thumbRadiusPx = with(density) { thumbRadius.toPx() }
    val trackHeightPx = with(density) { trackHeight.toPx() }

    var barWidth by remember { mutableStateOf(1f) }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .pointerInput(duration) {
                    detectTapGestures { offset ->
                        val newValue = (offset.x / barWidth) * duration
                        onValueChange(newValue.coerceIn(0f, duration))
                    }
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .padding(horizontal = 6.dp)
                    .pointerInput(duration) {
                        detectDragGestures { change, _ ->
                            val newValue = (change.position.x / barWidth) * duration
                            onValueChange(newValue.coerceIn(0f, duration))
                        }
                    }
            ) {
                barWidth = size.width

                drawRoundRect(
                    color = Color.LightGray,
                    topLeft = Offset(0f, size.height / 2 - trackHeightPx / 2),
                    size = Size(size.width, trackHeightPx),
                    cornerRadius = CornerRadius(trackHeightPx / 2, trackHeightPx / 2)
                )

                val progressWidth = (currentPosition / duration) * size.width
                drawRoundRect(
                    color = Color.Gray,
                    topLeft = Offset(0f, size.height / 2 - trackHeightPx / 2),
                    size = Size(progressWidth, trackHeightPx),
                    cornerRadius = CornerRadius(trackHeightPx / 2, trackHeightPx / 2)
                )

                drawCircle(
                    color = Color.Gray,
                    radius = thumbRadiusPx,
                    center = Offset(progressWidth, size.height / 2)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition.toLong()),
                color = Color(0xFFB0B0B0),
                fontSize = 14.sp
            )
            Text(
                text = formatTime(duration.toLong()),
                color = Color(0xFFB0B0B0),
                fontSize = 14.sp
            )
        }
    }
}

fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}

