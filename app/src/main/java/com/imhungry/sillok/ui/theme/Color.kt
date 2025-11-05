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
val dialogBackGround = Color(0x4DE0E0E0)
val placeHolder = Color(0xFFD9D9D9)
val inverse = Color(0xFFFAFAF9)
val tertiary = Color(0xFF888888)
val whiteBackground = Color(0xFFFAFAF9)
val primaryButton = Color(0xFF228F64)
val secondaryButton = Color(0xFFD0E7DE)
val sideBar = Color(0xD4F4F4F4)
val danger = Color(0xFFFB3939)
val orange100 = Color(0xFFFF7043)
val green100 = Color(0xFF144330)
val green200 = Color(0xFF186848)
val green300 = Color(0xFF00975B)
val green400 = Color(0xFF00492C)
val green500 = Color(0xFFF4F4F4)
val green600 = Color(0xFF00693F)
val meetingOutline = Color(0x56006B41)
val lightMeetingOutline = Color(0x3CDCDCDC)
val selectedDate = Color(0xFFDCE9E3)
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
val brown100 = Color(0xFF443A25)
val brown200 = Color(0xFF857658)
val brown400 = Color(0xFFE7E1D5)
val brown500 = Color(0xFFF2EEE5)
val cancledMeeting = Color(0XFFC85000)
val inProgressMeeting = Color(0xFF00975B)
val completedMeeting = Color(0xFF888888)
val waitingMeeting = Color(0xFFE4CD00)
val blurBackground = Color(0xD6F4F4F4)

val gradientColors = listOf(
    green100,
    Color(0xFF439E69),
    Color(0xFFEDEB82)
)

val gradientColors2 = listOf(
    Color(0xFFD4F4D9),
    Color(0xFFFAFAF9)
)

val gradientColors3 = listOf(
    Color(0xFFFFF6E1),
    Color(0xFFFAFAF9)
)

val gradientBrush = Brush.linearGradient(
    colors = gradientColors,
    start = Offset(0f, 0f),
    end = Offset(400f, 0f)
)

val gradientBrush2 = Brush.verticalGradient(
    colors = gradientColors2,
    startY = 0f,
    endY = 400f
)

val gradientBrush3 = Brush.verticalGradient(
    colors = gradientColors3,
    startY = 0f,
    endY = 400f
)