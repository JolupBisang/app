package com.imhungry.sillok.presentation.screen.meeting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.imhungry.sillok.domain.model.participation.UserParticipationRate
import com.imhungry.sillok.ui.theme.generateParticipantColors
import com.imhungry.sillok.ui.theme.green500

@Composable
fun ConversationSummaryBar(
    participationRates: List<UserParticipationRate>
) {
    val colors = generateParticipantColors(participationRates.size)

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        ProportionalBarChart(
            proportions = participationRates.map { it.rate },
            colors = colors,
            modifier = Modifier.fillMaxWidth()
        )

        val columnCount =3
        val minColumnSize = participationRates.size / columnCount
        val extra = participationRates.size % columnCount

        val indices = (0 until columnCount).map { col ->
            val start = (0 until col).sumOf { minColumnSize + if (it < extra) 1 else 0 }
            val end = start + minColumnSize + if (col < extra) 1 else 0
            start until end
        }

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 20.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            indices.forEachIndexed { col, range ->
                Column(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    range.forEach { i ->
                        LegendItem(name = participationRates[i].nickname, color = colors.getOrElse(i) { green500 })
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(name: String, color: Color) {
    Row(
        modifier = Modifier
            .wrapContentWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Start
        )
    }
}