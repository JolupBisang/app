package com.imhungry.jjongseol.ui.meeting.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.imhungry.jjongseol.ui.component.Notification

@Composable
fun MeetingFeedbackScreen() {
    val notifications = listOf(
        Triple("대화 시작을 자연스럽게 열어주셔서 좋아요.", "11:51:00", false),
        Triple("\"기름진 거만 먹은 것 같아서\"보다는 \"가볍게 먹고 싶은데\"라고 하면 어떨까요?", "11:51:10", true),
        Triple("\"아무거나\"보다는 \"적당히 빨리 정할 수 있는 메뉴 없을까요?\"처럼 말하면 다른 사람들도 의견 내기 편했을 것 같아요.", "11:51:35", false),
        Triple("본인의 취향을 분명하게 말해주셔서 좋아요.", "11:52:23", false),
        Triple("\"삼겹살 어때요?\"처럼 상대 의견도 함께 묻는 흐름이면 더 부드러웠을 것 같아요.", "11:52:42", false),
        Triple("분위기를 바꿔주는 중요한 역할 잘 해주셨어요.", "11:54:11", true),
        Triple("샐러드 외 메뉴도 하나쯤 제안해주셨으면 더 다양했을 것 같아요.", "11:54:24", false),
        Triple("\"오늘 같은 날은 매운 게 좋지 않아요?\"처럼 이유가 살짝 들어가면 더 설득력 있었을 것 같아요.", "11:54:40", true),
        Triple("매운 메뉴 대신 중간 옵션도 하나쯤 언급하면 더 매끄러웠을 것 같아요.", "11:54:58", false)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 56.dp, bottom = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        notifications.forEach { (message, time, isRead) ->
            Notification(
                visible = true,
                message = message,
                time = time,
                isRead = isRead
            )
        }
    }
}
