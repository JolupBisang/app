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
    val total = proportions.sum().takeIf { it > 0 } ?: 1.0
    val normalized = proportions.map { it / total }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(cornerRadius.dp))
    ) {
        normalized.forEachIndexed { index, fraction ->
            Box(
                modifier = Modifier
                    .weight(fraction.toFloat())
                    .fillMaxHeight()
                    .background(colors.getOrElse(index) { Color(0xFFF5F5F5) })
            )
        }
    }
}
