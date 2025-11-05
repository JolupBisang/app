package com.imhungry.sillok.presentation.screen.meeting.pager

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.imhungry.sillok.R
import com.imhungry.sillok.domain.model.participation.UserParticipationRate
import com.imhungry.sillok.domain.model.summary.Summary
import com.imhungry.sillok.domain.model.user.User
import com.imhungry.sillok.presentation.screen.meeting.component.ConversationSummaryBar
import com.imhungry.sillok.presentation.screen.meeting.component.SummaryListItem
import com.imhungry.sillok.presentation.util.DateTimeUtils
import com.imhungry.sillok.presentation.viewmodel.meeting.AgendaViewModel
import com.imhungry.sillok.presentation.viewmodel.meeting.MeetingInProgressViewModel
import com.imhungry.sillok.ui.components.Divider
import com.imhungry.sillok.ui.components.HighlightText

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MeetingSummaryScreen(
    meetingInProgressViewModel: MeetingInProgressViewModel
) {
    val state by meetingInProgressViewModel.state.collectAsState()
    val summaries = state.summaries
    val participationRates = state.participationRates
    var expanded by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
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
                        HighlightText(text = "대화 점유율")
                    }
                    Icon(
                        painter = painterResource(
                            id = if (expanded) R.drawable.collapse else R.drawable.expand2
                        ),
                        contentDescription = if (expanded) "접기" else "펼치기",
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (expanded && participationRates.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    ConversationSummaryBar(participationRates = participationRates)
                }
            }
        }
        item {
            Divider()
        }
        item {
            HighlightText(
                text = "중간 요약",
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
        itemsIndexed(summaries) { index, summary ->
            SummaryListItem(
                summary = summary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(Modifier.height(14.dp))
            if (index == summaries.lastIndex) {
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}

