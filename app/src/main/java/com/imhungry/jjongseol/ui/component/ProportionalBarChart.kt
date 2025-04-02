package com.imhungry.jjongseol.ui.component

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
    proportions: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    height: Int = 40,
    cornerRadius: Int = 8
) {
    val total = proportions.sum().takeIf { it > 0 } ?: 1f
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
                    .weight(fraction)
                    .fillMaxHeight()
                    .background(colors.getOrElse(index) { Color.Gray })
            )
        }
    }
}
