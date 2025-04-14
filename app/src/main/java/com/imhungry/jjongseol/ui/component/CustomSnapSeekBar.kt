package com.imhungry.jjongseol.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

@Composable
fun CustomSnapSeekBar(
    value: Float,
    onValueChange: (Float) -> Unit
) {
    val snapPoints = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
    val density = LocalDensity.current
    val thumbRadius = 6.dp
    val trackHeight = 2.dp
    val thumbRadiusPx = with(density) { thumbRadius.toPx() }
    val trackHeightPx = with(density) { trackHeight.toPx() }

    var barWidth by remember { mutableStateOf(1f) }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val ratio = (offset.x / barWidth).coerceIn(0f, 1f)
                        val tappedValue = snapPoints.closestTo(ratio)
                        onValueChange(tappedValue)
                    }
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .padding(horizontal = 6.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val ratio = (change.position.x / barWidth).coerceIn(0f, 1f)
                            val snapped = snapPoints.closestTo(ratio)
                            onValueChange(snapped)
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

                val progressRatio = (value - snapPoints.first()) / (snapPoints.last() - snapPoints.first())
                val progressWidth = progressRatio * size.width
                drawRoundRect(
                    color = Color(0xFF1E93EF),
                    topLeft = Offset(0f, size.height / 2 - trackHeightPx / 2),
                    size = Size(progressWidth, trackHeightPx),
                    cornerRadius = CornerRadius(trackHeightPx / 2, trackHeightPx / 2)
                )

                drawCircle(
                    color = Color(0xFF1E93EF),
                    radius = thumbRadiusPx,
                    center = Offset(progressWidth, size.height / 2)
                )
            }
        }
    }
}

fun List<Float>.closestTo(ratio: Float): Float {
    val target = 0.5f + ratio * (2.0f - 0.5f)
    return this.minByOrNull { kotlin.math.abs(it - target) } ?: this.first()
}