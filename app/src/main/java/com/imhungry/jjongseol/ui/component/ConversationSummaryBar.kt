package com.imhungry.jjongseol.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.imhungry.jjongseol.util.generateParticipantColors

@Composable
fun ConversationSummaryBar(participantData: List<Float>, participantNames: List<String>) {
    val colors = generateParticipantColors(participantData.size)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        ProportionalBarChart(
            proportions = participantData,
            colors = colors,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        val splitIndex = (participantNames.size + 1) / 2
        val leftColumn = participantNames.take(splitIndex)
        val rightColumn = participantNames.drop(splitIndex)

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier
                    .wrapContentWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                leftColumn.forEachIndexed { i, name ->
                    LegendItem(name = name, color = colors.getOrElse(i) { Color.Gray })
                }
            }
            Column(
                modifier = Modifier
                    .wrapContentWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rightColumn.forEachIndexed { i, name ->
                    val colorIndex = splitIndex + i
                    LegendItem(name = name, color = colors.getOrElse(colorIndex) { Color.Gray })
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
            textAlign = TextAlign.Start
        )
    }
}