package com.imhungry.jjongseol.ui.completedmeeting.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.jjongseol.ui.theme.Pretend
import com.imhungry.jjongseol.ui.theme.primaryButton
import com.imhungry.jjongseol.ui.theme.tertiary

@Composable
fun MeetingTabRow(
    selectedTab: Int,
    onTabClick: (Int) -> Unit
) {
    val items = listOf(
        Triple("회의 분석", if (selectedTab == 0) primaryButton else tertiary, if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium),
        Triple("전체 기록", if (selectedTab == 1) primaryButton else tertiary, if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium),
        Triple("피드백 기록", if (selectedTab == 2) primaryButton else tertiary, if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, (text, color, weight) ->
            Text(
                text = text,
                color = color,
                fontFamily = Pretend,
                fontWeight = weight,
                fontSize = 15.sp,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabClick(index) }
            )
        }
    }
}
