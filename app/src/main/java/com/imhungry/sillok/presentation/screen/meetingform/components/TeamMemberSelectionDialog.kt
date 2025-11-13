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
import com.imhungry.sillok.presentation.state.meetingform.MemberInfo
import com.imhungry.sillok.presentation.state.meetingform.TeamInfo
import com.imhungry.sillok.ui.components.MediumSillokButton
import com.imhungry.sillok.ui.components.SillokTextButton
import com.imhungry.sillok.ui.theme.dialogBackGround
import com.imhungry.sillok.ui.theme.green300
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.primaryButton

@Composable
fun TeamMemberSelectionDialog(
    visible: Boolean,
    team: TeamInfo?,
    members: List<MemberInfo>,
    selectedMemberIds: Set<Long>,
    onDismiss: () -> Unit,
    onMemberToggle: (Long) -> Unit,
    onSelectAll: () -> Unit,
    onConfirm: () -> Unit
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
                        .height(420.dp)
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                        .clickable(enabled = false) { }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = team.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        SillokTextButton(
                            text = "전체 선택",
                            textColor = green300,
                            onClick = { onSelectAll() }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(members) { member ->
                            MemberEditItem(
                                name = member.name,
                                email = member.email,
                                profileImage = member.profileImage,
                                isSelected = selectedMemberIds.contains(member.id),
                                onToggle = {
                                    onMemberToggle(member.id)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    MediumSillokButton(
                        text = "초대하기",
                        onClick = onConfirm,
                        backgroundColor = primaryBackground,
                        borderColor = primaryButton,
                        textColor = primaryButton
                    )
                }
            }
        }
    }
}
