package com.imhungry.sillok.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.imhungry.sillok.R

val Pretend = FontFamily(
    Font(R.font.pretendard_extra_bold, FontWeight.ExtraBold),
    Font(R.font.pretendard_black, FontWeight.Black),
    Font(R.font.pretendard_bold, FontWeight.Bold),
    Font(R.font.pretendard_semi_bold, FontWeight.SemiBold),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_light, FontWeight.Light),
    Font(R.font.pretendard_thin, FontWeight.Thin),
    Font(R.font.pretendard_extra_light, FontWeight.ExtraLight)
)

val Roboto = Font(R.font.roboto_medium, FontWeight.Medium)

val Typography = Typography(
    labelSmall = TextStyle(
        fontFamily = Pretend,
        fontWeight = FontWeight.Normal,
        color = primaryTextColor,
        fontSize = 13.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Pretend,
        fontWeight = FontWeight.ExtraBold,
        color = primaryTextColor,
        fontSize = 15.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Pretend,
        fontWeight = FontWeight.Medium,
        color = primaryTextColor,
        fontSize = 16.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Pretend,
        fontWeight = FontWeight.Normal,
        color = primaryTextColor,
        fontSize = 15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Pretend,
        fontWeight = FontWeight.Bold,
        color = primaryTextColor,
        fontSize = 15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Pretend,
        fontWeight = FontWeight.SemiBold,
        color = primaryTextColor,
        fontSize = 15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Pretend,
        fontWeight = FontWeight.ExtraBold,
        color = primaryTextColor
    ),
    titleMedium = TextStyle(
        fontFamily = Pretend,
        fontWeight = FontWeight.ExtraBold,
        color = primaryTextColor,
        fontSize = 16.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Pretend,
        fontWeight = FontWeight.SemiBold,
        color = primaryTextColor,
        fontSize = 28.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Pretend,
        fontWeight = FontWeight.ExtraBold,
        color = primaryTextColor,
        fontSize = 18.sp
    )
)