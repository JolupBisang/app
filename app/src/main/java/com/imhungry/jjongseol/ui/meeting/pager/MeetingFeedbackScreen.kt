package com.imhungry.jjongseol.ui.meeting.pager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.imhungry.jjongseol.ui.component.Notification

@Composable
fun MeetingFeedbackScreen() {
    val notifications = listOf(
        Triple("우리, 이쁜.딸. 얼굴만큼.고운.말", "00:02:10", false),
        Triple("우리, 이쁜.딸. 얼굴만큼.고운.말 스자.^^ -엄마가", "00:02:10", false),
        Triple("우리, 이쁜.딸. 얼굴만큼.고운.말 스자.^^ 우리, 이쁜.딸. 얼굴만큼.고운.말 스자.^^ 우리, 이쁜.딸. 얼굴만큼.고운.말 스자.^^ -엄마가", "00:02:10", false),
        Triple("우리, 이쁜.딸. 얼굴만큼.고운.말 스자.^^ -엄마가", "00:02:10", false),
        Triple("우리, 이쁜.딸. 얼굴만큼.고운.말 스자.^^ -엄마가", "00:02:10", true),
        Triple("우리, 이쁜.딸. 얼굴만큼.고운.말 스자.^^ -엄마가", "00:02:10", false),
        Triple("우리, 이쁜.딸. 얼굴만큼.고운.말 스자.^^ -엄마가", "00:02:10", true),
        Triple("우리, 이쁜.딸. 얼굴만큼.고운.말 스자.^^ -엄마가", "00:02:10", false),
        Triple("우리, 이쁜.딸. 얼굴만큼.고운.말 스자.^^ -엄마가", "00:02:10", false),
        Triple("우리, 이쁜.딸. 얼굴만큼.고운.말 스자.^^ -엄마가", "00:02:10", true),
        Triple("우리, 이쁜.딸. 얼굴만큼.고운.말 스자.^^ -엄마가", "00:02:10", false),
        Triple("우리, 이쁜.딸. 얼굴만큼.고운.말 스자.^^ -엄마가", "00:02:10", false),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 56.dp, bottom = 24.dp)
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
