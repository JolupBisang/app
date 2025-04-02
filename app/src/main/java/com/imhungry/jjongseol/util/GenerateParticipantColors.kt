package com.imhungry.jjongseol.util

import androidx.compose.ui.graphics.Color

fun generateParticipantColors(count: Int): List<Color> {
    val baseColors = listOf(
        Color(0xFFE74C3C),
        Color(0xFFF39C12),
        Color(0xFFF1C40F),
        Color(0xFF2ECC71),
        Color(0xFF3498DB),
        Color(0xFF9B59B6),
        Color(0xFF16A085),
        Color(0xFF34495E),
    )
    return List(count) { index -> baseColors[index % baseColors.size] }
}
