package com.imhungry.jjongseol.ui.meeting.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.imhungry.jjongseol.data.meeting.SummaryItem
import com.imhungry.jjongseol.ui.component.ConversationSummaryBar
import com.imhungry.jjongseol.ui.component.SummaryListItem

@Composable
fun MeetingSummaryScreen() {
    val data = listOf(45f, 30f, 20f, 10f, 5f)
    val names = listOf("지안", "상정", "원영", "유진", "은경")
    val summaryList = listOf(
        SummaryItem("지안이 점심 메뉴를 제안하며, 가볍고 건강한 음식을 원한다고 말함.", "11:51:00"),
        SummaryItem("상정은 귀찮아하면서 빠른 결정을 원함. 과거에 자주 돈가스를 먹었다고 언급.", "11:51:10"),
        SummaryItem("원영은 삼겹살을 먹고 싶다고 강하게 주장함.", "11:51:35"),
        SummaryItem("유진은 채식 중이기 때문에 고기 메뉴가 어렵다며, 샐러드바를 제안함.", "11:52:23"),
        SummaryItem("은경은 매운 음식(불닭)을 먹고 싶다고 의견을 냄.", "11:52:42"),
        SummaryItem("지안이 점심 메뉴를 제안하며, 가볍고 건강한 음식을 원한다고 말함.", "11:51:00"),
        SummaryItem("상정은 귀찮아하면서 빠른 결정을 원함. 과거에 자주 돈가스를 먹었다고 언급.", "11:51:10"),
        SummaryItem("원영은 삼겹살을 먹고 싶다고 강하게 주장함.", "11:51:35"),
        SummaryItem("유진은 채식 중이기 때문에 고기 메뉴가 어렵다며, 샐러드바를 제안함.", "11:52:23"),
        SummaryItem("은경은 매운 음식(불닭)을 먹고 싶다고 의견을 냄.", "11:52:42"),SummaryItem("지안이 점심 메뉴를 제안하며, 가볍고 건강한 음식을 원한다고 말함.", "11:51:00"),
        SummaryItem("상정은 귀찮아하면서 빠른 결정을 원함. 과거에 자주 돈가스를 먹었다고 언급.", "11:51:10"),
        SummaryItem("원영은 삼겹살을 먹고 싶다고 강하게 주장함.", "11:51:35"),
        SummaryItem("유진은 채식 중이기 때문에 고기 메뉴가 어렵다며, 샐러드바를 제안함.", "11:52:23"),
        SummaryItem("은경은 매운 음식(불닭)을 먹고 싶다고 의견을 냄.", "11:52:42"),)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 36.dp)
                ) {
                    Text(
                        text = "대화 점유율",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    ConversationSummaryBar(
                        participantData = data,
                        participantNames = names
                    )
                }

                Divider(
                    color = Color.LightGray,
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 36.dp)
                ) {
                    Text(
                        text = "요약",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    summaryList.forEach { summary ->
                        SummaryListItem(summary)
                    }
                }
            }
        }
    }
}



