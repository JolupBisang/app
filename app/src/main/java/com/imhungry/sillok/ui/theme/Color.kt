package com.imhungry.sillok.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val primaryTextColor = Color(0xFF121212)
val beige = Color(0xFFF2EEE5)
val outline = Color(0xFF747775)
val googleButtonText = Color(0xFF1F1F1F)
val primaryBackground = Color(0xFFFAFAF9)
val blackBackGround = Color(0xFF121212)
val inverse = Color(0xFFFAFAF9)
val tertiary = Color(0xFF888888)
val whiteBackground = Color(0xFFFAFAF9)
val primaryButton = Color(0xFF228F64)
val secondaryButton = Color(0xFFD0E7DE)
val lightGrayButton = Color(0xFFE0E0E0)
val danger = Color(0xFFFB3939)
val orange100 = Color(0xFFFF7043)
val green100 = Color(0xFF144330)
val green200 = Color(0xFF186848)
val green500 = Color(0xFFD0E7DE)
val gray200 = Color(0xFF555555)
val gray300 = Color(0xFF888888)
val gray400 = Color(0xFFBBBBBB)
val gray500 = Color(0xFFE0E0E0)
val primarySurface = Color(0xFF186848)
val lightSurface = Color(0x2D186848)
val border = Color(0x97888888)
val disabled = Color(0xFFBBBBBB)
val shadow = Color(0x33A1A1A1)
val pagerIndicatorBackground = Color(0xFFD9D9D9)
val highlight = Color(0x40186848)
val brown200 = Color(0xFF857658)
val brown500 = Color(0xFFF2EEE5)

val gradientColors = listOf(
    green100,
    Color(0xFF439E69),
    Color(0xFFEDEB82)
)

val gradientBrush = Brush.linearGradient(
    colors = gradientColors,
    start = Offset(0f, 0f),
    end = Offset(400f, 0f)
)