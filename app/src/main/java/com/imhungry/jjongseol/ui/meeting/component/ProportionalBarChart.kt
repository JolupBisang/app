package com.imhungry.jjongseol.ui.meeting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun ProportionalBarChart(
    proportions: List<Double>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    height: Int = 17,
    cornerRadius: Int = 4
) {
    val safeProportions = if (proportions.isEmpty()) listOf(1.0) else proportions
    val total = safeProportions.sum().takeIf { it > 0 && it.isFinite() } ?: 1.0
    val normalized = safeProportions.map { (it / total).coerceIn(0.0, 1.0) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(cornerRadius.dp))
    ) {
        normalized.forEachIndexed { index, fraction ->
            val safeWeight = fraction.toFloat().takeIf { it > 0f && it.isFinite() } ?: 0.0001f
            Box(
                modifier = Modifier
                    .weight(safeWeight)
                    .fillMaxHeight()
                    .background(colors.getOrElse(index) { Color(0xFFF5F5F5) })
            )
        }
    }
}
