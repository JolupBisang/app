package com.imhungry.sillok.presentation.screen.meetingform.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.imhungry.sillok.presentation.state.meetingform.SelectedTeamInfo
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.theme.dialogBackGround

@Composable
fun TeamMemberSelectionResultDialog(
    visible: Boolean,
    team: SelectedTeamInfo?,
    onDismiss: () -> Unit,
) {
    // 뒤로가기 처리
    BackHandler(enabled = visible) {
        onDismiss()
    }

    if (visible && team != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1001f)
                .background(dialogBackGround)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 20.dp,
                modifier = Modifier.clickable(enabled = false) { }
            ) {
                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .height(360.dp)
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                        .clickable(enabled = false) { }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = team.teamName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(team.selectedMembers) { member ->
                            MemberItem(
                                name = member.name,
                                email = member.email,
                                profileImage = member.profileImage,
                                isSelected = true, // 모든 멤버가 선택된 상태로 표시
                            )
                        }
                    }
                }
            }
        }
    }
}