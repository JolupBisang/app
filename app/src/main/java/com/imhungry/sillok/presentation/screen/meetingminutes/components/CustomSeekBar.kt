package com.imhungry.sillok.presentation.screen.meetingminutes.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.imhungry.sillok.ui.theme.green300
import com.imhungry.sillok.ui.theme.pagerIndicatorBackground
import com.imhungry.sillok.ui.theme.shadow

@Composable
fun CustomSeekBar(
    currentPosition: Float,
    duration: Float,
    onValueChange: (Float) -> Unit,
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
                .wrapContentHeight()
                .pointerInput(duration) {
                    detectTapGestures { offset ->
                        val newValue = (offset.x / barWidth) * duration
                        onValueChange(newValue.coerceIn(0f, duration))
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                shadow
                            )
                        )
                    )
            )
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .pointerInput(duration) {
                        detectDragGestures { change, _ ->
                            val newValue = (change.position.x / barWidth) * duration
                            onValueChange(newValue.coerceIn(0f, duration))
                        }
                    }
            ) {
                barWidth = size.width

                drawRoundRect(
                    color = pagerIndicatorBackground,
                    topLeft = Offset(0f, size.height / 2 - trackHeightPx / 2),
                    size = Size(size.width, trackHeightPx),
                    cornerRadius = CornerRadius(trackHeightPx / 2, trackHeightPx / 2)
                )

                val progressWidth = (currentPosition / duration) * size.width
                drawRoundRect(
                    color = green300,
                    topLeft = Offset(0f, size.height / 2 - trackHeightPx / 2),
                    size = Size(progressWidth, trackHeightPx),
                    cornerRadius = CornerRadius(trackHeightPx / 2, trackHeightPx / 2)
                )

                drawCircle(
                    color = green300,
                    radius = thumbRadiusPx,
                    center = Offset(progressWidth, size.height / 2)
                )
            }
        }
    }
}

