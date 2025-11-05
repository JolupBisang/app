package com.imhungry.sillok.ui.theme

import androidx.compose.ui.graphics.Color

fun generateParticipantColors(count: Int): List<Color> {
    val baseColors = listOf(
        Color(0xFFDB6A00),
        Color(0xFFE282FF),
        Color(0xFFFFF386),
        Color(0xFFB4E66E),
        Color(0xFFFF9E3D),
        Color(0xFFE74C3C),
        Color(0xFFF39C12),
        Color(0xFFF1C40F),
        Color(0xFF27AE60),
        Color(0xFF2980B9),
        Color(0xFF1ABC9C),
        Color(0xFFD35400),
        Color(0xFF2ECC71),
        Color(0xFF16A085),
        Color(0xFF9B59B6),
        Color(0xFF3498DB),
        Color(0xFF8E44AD),
        Color(0xFF34495E),
        Color(0xFF95A5A6),
        Color(0xFF7F8C8D)
    )

    val softColors = listOf(
        Color(0xFFF28B82),
        Color(0xFFFFAB91),
        Color(0xFFFDD663),
        Color(0xFFA8D5BA),
        Color(0xFF81C995),
        Color(0xFF80DEEA),
        Color(0xFFAECBFA),
        Color(0xFFB5D3E7),
        Color(0xFFB39DDB),
        Color(0xFFE1BEE7),
        Color(0xFFF6C5C0),
        Color(0xFFE6B8AF),
        Color(0xFFB2DFDB),
        Color(0xFFD7CCC8),
        Color(0xFFCFD8DC)
    )

    return List(count) { index -> baseColors[index % baseColors.size] }
}
