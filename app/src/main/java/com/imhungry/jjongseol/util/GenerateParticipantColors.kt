package com.imhungry.jjongseol.util

import androidx.compose.ui.graphics.Color

fun generateParticipantColors(count: Int): List<Color> {
    val baseColors = listOf(
        Color(0xFFE74C3C), // 빨간색
        Color(0xFFF1C40F), // 노란색
        Color(0xFF27AE60), // 진한 녹색
        Color(0xFFF39C12), // 주황색
        Color(0xFF1ABC9C), // 민트색
        Color(0xFFD35400), // 다크 오렌지
        Color(0xFF2ECC71), // 연녹색
        Color(0xFF2980B9), // 진한 파랑
        Color(0xFF16A085), // 청록색
        Color(0xFF9B59B6), // 보라색
        Color(0xFF3498DB), // 파란색
        Color(0xFF8E44AD), // 진한 보라
        Color(0xFF34495E), // 남색
        Color(0xFF95A5A6), // 회색
        Color(0xFF7F8C8D)  // 짙은 회색
    )

    val softColors = listOf(
        Color(0xFFF28B82), // 소프트 레드
        Color(0xFFFFAB91), // 소프트 오렌지
        Color(0xFFFDD663), // 소프트 옐로우
        Color(0xFFA8D5BA), // 소프트 라이트그린
        Color(0xFF81C995), // 소프트 민트그린
        Color(0xFF80DEEA), // 소프트 아쿠아
        Color(0xFFAECBFA), // 소프트 하늘색
        Color(0xFFB5D3E7), // 소프트 블루그레이
        Color(0xFFB39DDB), // 소프트 퍼플
        Color(0xFFE1BEE7), // 소프트 핑크퍼플
        Color(0xFFF6C5C0), // 소프트 코랄
        Color(0xFFE6B8AF), // 소프트 피치핑크
        Color(0xFFB2DFDB), // 소프트 청록
        Color(0xFFD7CCC8), // 소프트 모카
        Color(0xFFCFD8DC)  // 소프트 쿨그레이
    )

    return List(count) { index -> softColors[index % softColors.size] }
}
