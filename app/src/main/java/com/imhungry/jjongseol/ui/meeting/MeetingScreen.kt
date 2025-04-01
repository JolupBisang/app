package com.imhungry.jjongseol.ui.meeting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.imhungry.jjongseol.ui.meeting.bottom.MeetingControlPanel
import com.imhungry.jjongseol.ui.meeting.pager.MeetingFeedbackScreen
import com.imhungry.jjongseol.ui.meeting.pager.MeetingRecordScreen
import com.imhungry.jjongseol.ui.meeting.pager.MeetingSummaryScreen

@OptIn(ExperimentalPagerApi::class)
@Composable
fun MeetingScreen() {
    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background)
    ) {
        val pagerState = rememberPagerState(initialPage = 1)

        HorizontalPager(
            count = 3,
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.70f)
        ) { page ->
            when (page) {
                0 -> MeetingSummaryScreen()
                1 -> MeetingRecordScreen()
                2 -> MeetingFeedbackScreen()
            }
        }

        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 4.dp, bottom = 4.dp),
            activeColor = Color(0xFF1E93EF),
            inactiveColor = Color.LightGray,
            indicatorWidth = 6.dp,
            spacing = 4.dp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Divider(
                color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        MeetingControlPanel(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.20f)
                .background(MaterialTheme.colors.background)
        )
    }
}
