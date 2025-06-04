package com.imhungry.jjongseol.ui.meeting.pager

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.component.summary.ConversationSummaryBar
import com.imhungry.jjongseol.ui.component.summary.SummaryListItem
import com.imhungry.jjongseol.ui.theme.Pretend
import com.imhungry.jjongseol.viewmodel.AgendaViewModel
import com.imhungry.jjongseol.viewmodel.MeetingViewModel
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import com.imhungry.jjongseol.ui.theme.gray400
import com.imhungry.jjongseol.ui.theme.primaryBackground

@Composable
fun MeetingSummaryScreen(
    meetingViewModel: MeetingViewModel,
    agendaViewModel: AgendaViewModel
) {
    val agendas by agendaViewModel.agendaItems.collectAsState()

    val firstUncheckedIndex = agendas.indexOfFirst { !it.isCompleted }
    val peekIndex = if (firstUncheckedIndex == -1) agendas.lastIndex else firstUncheckedIndex
    val hasAgendas = agendas.isNotEmpty()
    val summaryList by meetingViewModel.summaryList.collectAsState()
    val data = listOf(45f, 30f, 20f, 10f, 5f)
    val names = listOf("김부장", "조사원", "정대리", "정과장", "김상병")
    var expanded by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryBackground)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 20.dp)
                    .animateContentSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { expanded = !expanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .drawBehind {
                                    val underlineHeight = 7.dp.toPx()
                                    drawRect(
                                        color = Color(0x40186848),
                                        topLeft = Offset(0f, size.height - underlineHeight),
                                        size = androidx.compose.ui.geometry.Size(size.width, underlineHeight)
                                    )
                                }
                        ) {
                            Text(
                                text = "대화 점유율",
                                fontFamily = Pretend,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    Icon(
                        painter = painterResource(
                            id = if (expanded) R.drawable.collapse else R.drawable.expand2
                        ),
                        contentDescription = if (expanded) "접기" else "펼치기",
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (expanded) {
                    ConversationSummaryBar(
                        participantData = data,
                        participantNames = names
                    )
                }
            }
        }
        item {
            Divider(
                color = gray400,
                thickness = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
        }
        item {
            Box(
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)
                    .drawBehind {
                        val underlineHeight = 7.dp.toPx()
                        drawRect(
                            color = Color(0x40186848),
                            topLeft = Offset(0f, size.height - underlineHeight),
                            size = androidx.compose.ui.geometry.Size(size.width, underlineHeight)
                        )
                    }
            ) {
                Text(
                    text = "중간 요약",
                    fontFamily = Pretend,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        itemsIndexed(summaryList) { index, summary ->
            SummaryListItem(summary.summary, summary.timestamp)
            Spacer(Modifier.height(12.dp))
            if (index == summaryList.lastIndex) {
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}