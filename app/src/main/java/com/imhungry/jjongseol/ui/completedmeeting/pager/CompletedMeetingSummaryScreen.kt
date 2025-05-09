package com.imhungry.jjongseol.ui.completedmeeting.pager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.data.model.meeting.SummaryItem
import com.imhungry.jjongseol.ui.component.summary.ConversationSummaryBar
import com.imhungry.jjongseol.ui.component.summary.SummaryListItem

@Composable
fun CompletedMeetingSummaryScreen() {
    var isExpanded by remember { mutableStateOf(true) }
    var isExpanded2 by remember { mutableStateOf(true) }
    var isExpanded3 by remember { mutableStateOf(true) }
    var isExpanded4 by remember { mutableStateOf(true) }

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
        SummaryItem("은경은 매운 음식(불닭)을 먹고 싶다고 의견을 냄.", "11:52:42"),
        SummaryItem("지안이 점심 메뉴를 제안하며, 가볍고 건강한 음식을 원한다고 말함.", "11:51:00"),
        SummaryItem("상정은 귀찮아하면서 빠른 결정을 원함. 과거에 자주 돈가스를 먹었다고 언급.", "11:51:10"),
        SummaryItem("원영은 삼겹살을 먹고 싶다고 강하게 주장함.", "11:51:35"),
        SummaryItem("유진은 채식 중이기 때문에 고기 메뉴가 어렵다며, 샐러드바를 제안함.", "11:52:23"),
        SummaryItem("은경은 매운 음식(불닭)을 먹고 싶다고 의견을 냄.", "11:52:42"),)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            .navigationBarsPadding()
    ) {
        item {
            SectionWithToggle(
                title = "진행 시간",
                isExpanded = isExpanded,
                onToggle = { isExpanded = !isExpanded },
                content = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 36.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "13:00", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.width(20.dp))
                        Text(text = "~", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.width(20.dp))
                        Text(text = "15:00", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.width(32.dp))
                        Text(text = "120분", style = MaterialTheme.typography.bodyLarge)
                    }
                },
                expanded = isExpanded
            )
        }

        item {
            SectionWithToggle(
                title = "대화 점유율",
                isExpanded = isExpanded2,
                onToggle = { isExpanded2 = !isExpanded2 },
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 36.dp)
                    ) {
                        ConversationSummaryBar(
                            participantData = data,
                            participantNames = names
                        )
                    }
                },
                expanded = isExpanded2
            )
        }

        item {
            SectionWithToggle(
                title = "전체 요약",
                isExpanded = isExpanded3,
                onToggle = { isExpanded3 = !isExpanded3 },
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 36.dp)
                    ) {
                        Text(
                            text = """
                        이번 회의에서는 점심 식사 메뉴를 결정하기 위한 활발한 논의가 이루어졌다. 지안이 비교적 기름지지 않은 음식을 선호하며 가볍게 먹고 싶다는 의견을 먼저 제시하면서 대화가 시작되었다. 이에 원영은 매번 같은 패턴으로 돈가스를 선택하는 상황을 유쾌하게 언급하며 빠르게 결정을 유도하였다.

                        상정은 삼겹살을 강하게 주장하며 고기 욕구를 드러냈고, 유진은 현재 채식 중이라는 개인 사정을 언급하며 샐러드바가 있는 메뉴를 제안하였다. 은경은 매운 음식에 대한 강한 선호를 표현하며 불닭을 언급하였으나, 지안은 그 선택이 속에 부담이 될 수 있다며 조심스러운 반응을 보였다. 이후, 지안은 고기와 채소가 함께 있는 ‘샤브샤브’를 타협안으로 제안하였고, 이는 팀원들에게 좋은 반응을 얻었다.

                        상정은 고기가 포함된 메뉴라면 괜찮다는 입장을 보였고, 유진은 야채를 많이 먹을 수 있다는 점에서 샤브샤브에 긍정적인 반응을 보였다. 원영 역시 고기, 야채, 매운 맛이 모두 가능한 메뉴라며 찬성 의사를 밝혔다. 은경도 맵게 먹을 수 있다면 만족스럽다는 반응을 보이며 최종적으로 전원이 동의하는 결론에 도달하였다.

                        최종적으로 메뉴는 샤브샤브로 결정되었으며, 메뉴 선택에 약 10분가량이 소요되었다. 원영은 이 정도면 꽤 빠르게 결정된 편이라며 대화를 마무리 지었고, 모두가 만족하는 선택으로 자연스럽게 외출 준비가 이어졌다.

                        이 회의는 서로의 취향과 상황을 존중하면서도 유머를 잃지 않은 분위기 속에서 효율적인 의사결정을 이끌어낸 좋은 예시라 할 수 있다.
                    """.trimIndent(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                expanded = isExpanded3
            )
        }
        item {
            SectionWithToggle(
                title = "중간 요약",
                isExpanded = isExpanded4,
                onToggle = { isExpanded4 = !isExpanded4 },
                showDivider = false,
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 36.dp)
                    ) {
                        summaryList.forEach { summary ->
                            SummaryListItem(summary.text, "11:51:50")
                        }
                    }
                },
                expanded = isExpanded4
            )
        }
    }
}

@Composable
fun SectionWithToggle(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
    expanded: Boolean,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggle
                )
                .padding(horizontal = 36.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Image(
                painter = painterResource(id = R.drawable.fold),
                contentDescription = "접기",
                modifier = Modifier
                    .size(26.dp)
                    .rotate(if (expanded) 180f else 0f)
            )
        }

        AnimatedVisibility(visible = expanded) {
            content()
        }

        if (showDivider) {
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (expanded) Modifier.padding(top = 12.dp) else Modifier),
                thickness = 1.dp,
                color = Color(0xFFDCDCDC)
            )
        }
    }
}


