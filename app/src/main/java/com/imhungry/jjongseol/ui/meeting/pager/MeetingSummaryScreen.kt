package com.imhungry.jjongseol.ui.meeting.pager

import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.geometry.Size
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextLayoutResult
import com.imhungry.jjongseol.data.model.participationrate.response.ParticipationRateHistoryRes
import com.imhungry.jjongseol.data.model.summary.dto.SummaryDto
import com.imhungry.jjongseol.data.model.summary.response.SummaryListRes
import com.imhungry.jjongseol.data.model.user.response.UserInfoResponse
import com.imhungry.jjongseol.data.network.config.AppPrefs
import com.imhungry.jjongseol.ui.theme.gray400
import com.imhungry.jjongseol.ui.theme.primaryBackground
import com.imhungry.jjongseol.util.DateTimeUtils
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MeetingSummaryScreen(
    meetingViewModel: MeetingViewModel,
    agendaViewModel: AgendaViewModel,
    meetingId: Long,
    participantInfos: List<UserInfoResponse>,
    startTime: Long?,
    summaries: List<SummaryListRes>,
    usrParticipationRates: List<ParticipationRateHistoryRes.UserParticipationRate>
) {
    val context = LocalContext.current

    val agendas by agendaViewModel.agendaItems.collectAsState()
    val firstUncheckedIndex = agendas.indexOfFirst { !it.isCompleted }
    val peekIndex = if (firstUncheckedIndex == -1) agendas.lastIndex else firstUncheckedIndex
    val hasAgendas = agendas.isNotEmpty()
    val summaryList by meetingViewModel.summaryList.collectAsState()
    val nicknameMap = remember(participantInfos) {
        participantInfos.associateBy({ it.id }, { it.nickname })
    }
    val participationRates by meetingViewModel.participationRates.collectAsState()
    val sortedRates = participationRates.sortedByDescending { it.rate }
    //val participantData = sortedRates.map { (it.rate * 100f) }
    //val participantNames = sortedRates.map { nicknameMap[it.userId] ?: "알 수 없음" }
    var expanded by remember { mutableStateOf(true) }

//    LaunchedEffect(summaries) {
//        val summaryDtos = summaries.map { SummaryDto(it.timestamp, it.content) }
//        meetingViewModel.setSummaryList(summaryDtos)
//    }

    val (participantData, participantNames) = remember(usrParticipationRates, participationRates) {
        if (usrParticipationRates.isNotEmpty()) {
            val sortedRates = usrParticipationRates.sortedByDescending { it.rate }
            val data = sortedRates.map { it.rate * 100f }
            val names = sortedRates.map { it.nickname }
            data to names
        } else {
            val sortedRates = participationRates.sortedByDescending { it.rate }
            val data = sortedRates.map { it.rate * 100f }
            val names = sortedRates.map { nicknameMap[it.userId] ?: "알 수 없음" }
            data to names
        }
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryBackground)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(horizontal = 20.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
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
                                        size = Size(size.width, underlineHeight)
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

                if (expanded && participantData.isNotEmpty() && participantNames.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    ConversationSummaryBar(
                        participantData = participantData,
                        participantNames = participantNames
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
            )
        }
        item {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 16.dp)
                    .drawBehind {
                        val underlineHeight = 7.dp.toPx()
                        drawRect(
                            color = Color(0x40186848),
                            topLeft = Offset(0f, size.height - underlineHeight),
                            size = Size(size.width, underlineHeight)
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
            val elapsed = DateTimeUtils.getElapsedString(startTime, summary.timestamp)
            SummaryListItem(summary.summary, elapsed)
            Spacer(Modifier.height(12.dp))
            if (index == summaryList.lastIndex) {
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

