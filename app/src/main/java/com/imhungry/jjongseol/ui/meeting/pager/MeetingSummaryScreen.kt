package com.imhungry.jjongseol.ui.meeting.pager

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.component.checklist.CheckItem
import com.imhungry.jjongseol.ui.component.layout.TopSheet
import com.imhungry.jjongseol.ui.component.summary.ConversationSummaryBar
import com.imhungry.jjongseol.ui.component.summary.SummaryListItem
import com.imhungry.jjongseol.ui.theme.Pretend
import com.imhungry.jjongseol.viewmodel.AgendaViewModel
import com.imhungry.jjongseol.viewmodel.MeetingViewModel

@Composable
fun MeetingSummaryScreen(
    meetingViewModel: MeetingViewModel,
    agendaViewModel: AgendaViewModel
) {
    val agendas by agendaViewModel.agendaItems.collectAsState()
    var isTopSheetExpanded by remember { mutableStateOf(false) }

    val firstUncheckedIndex = agendas.indexOfFirst { !it.isCompleted }
    val peekIndex = if (firstUncheckedIndex == -1) agendas.lastIndex else firstUncheckedIndex
    val hasAgendas = agendas.isNotEmpty()
    val summaryList by meetingViewModel.summaryList.collectAsState()
    val data = listOf(45f, 30f, 20f, 10f, 5f)
    val names = listOf("김부장", "조사원", "정대리", "정과장", "김상병")
    var expanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (hasAgendas && peekIndex in agendas.indices) {
            TopSheet(
                modifier = Modifier.background(Color(0xFFE5E5E5)),
                expanded = isTopSheetExpanded,
                onExpandedChange = { isTopSheetExpanded = it },
                peekContent = {
                    CheckItem(
                        text = agendas[peekIndex].content,
                        checked = agendas[peekIndex].isCompleted,
                        isFocused = !agendas[peekIndex].isCompleted,
                        onToggle = { agendaViewModel.onToggleAgenda(peekIndex) }
                    )
                },
                content = {
                    LazyColumn (
                        modifier = Modifier.heightIn(max = 161.dp)
                    ) {
                        itemsIndexed(agendas) { i, item ->
                            CheckItem(
                                text = item.content,
                                checked = item.isCompleted,
                                isFocused = !item.isCompleted && firstUncheckedIndex == i,
                                onToggle = { agendaViewModel.onToggleAgenda(i) }
                            )
                        }
                    }
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 24.dp)
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
                        Text(
                            text = "대화 점유율",
                            fontFamily = Pretend,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
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

                Divider(
                    color = Color(0xFF000000),
                    thickness = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = "중간 요약",
                        fontFamily = Pretend,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    summaryList.forEach { summary ->
                        SummaryListItem(summary.summary, summary.timestamp)
                    }
                }
            }
        }
    }
}



