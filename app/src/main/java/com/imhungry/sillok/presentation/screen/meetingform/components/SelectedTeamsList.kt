package com.imhungry.sillok.presentation.screen.meetingform.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.state.meetingform.SelectedTeamInfo
import com.imhungry.sillok.ui.theme.green300
import com.imhungry.sillok.ui.theme.inverse
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.secondaryButton
import com.imhungry.sillok.ui.theme.tertiary

@Composable
fun SelectedTeamsList(
    teams: List<SelectedTeamInfo>,
    onTeamRemoved: (Long) -> Unit,
    onTeamClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        teams.forEach { team ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, green300, RoundedCornerShape(12.dp))
                    .background(primaryBackground)
                    .clickable { onTeamClick(team.teamId) }
                    .padding(start = 12.dp, end = 12.dp, top = 3.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = team.teamName,
                    style = MaterialTheme.typography.labelSmall,
                    color = green300
                )

                Spacer(modifier = Modifier.width(16.dp))

                // 삭제 버튼을 Box로 감싸서 클릭 이벤트 전파 차단
                Box(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onTeamRemoved(team.teamId)
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.delete),
                        contentDescription = "삭제",
                        modifier = Modifier.size(9.dp)
                    )
                }
            }
        }
    }
}

