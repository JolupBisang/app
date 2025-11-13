package com.imhungry.sillok.presentation.screen.team

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.sillok.domain.model.team.TeamDetailSummary
import com.imhungry.sillok.ui.theme.blackBackGround
import com.imhungry.sillok.ui.theme.danger
import com.imhungry.sillok.ui.theme.gray200
import com.imhungry.sillok.ui.theme.gray400
import com.imhungry.sillok.ui.theme.gray500
import com.imhungry.sillok.ui.theme.green300
import com.imhungry.sillok.ui.theme.green500
import com.imhungry.sillok.ui.theme.inverse
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.tertiary

@Composable
fun TeamCard(
    team: TeamDetailSummary,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    borderColor: Color = gray500.copy(alpha = 0.7f),
    editMode: Boolean = false,
    isSelected: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isInteracting = isPressed || isHovered
    val cardShape = MaterialTheme.shapes.small
    val defaultRipple = rememberRipple(bounded = true)
    val isScheduled = team.date != null && !team.isPast

    if (editMode) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(top = 4.dp)
                .shadow(
                    elevation = 1.dp,
                    shape = cardShape
                )
                .clip(cardShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = defaultRipple,
                    onClick = onClick
                )
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = cardShape,
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 1.dp
                ),
                border = BorderStroke(2.dp, if (isSelected) danger else borderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = team.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryTextColor
                        )
                        Text(
                            text = "${team.memberCount}명",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = primaryTextColor
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 회의명이 있을 때만 표시
                    team.meetingName?.let { meetingName ->
                        Text(
                            text = meetingName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = blackBackGround
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // 날짜와 시간이 있을 때만 표시
                    if (team.date != null || team.timeRange != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            team.date?.let { date ->
                                Text(
                                    text = date,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = blackBackGround,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            }
                            team.timeRange?.let { timeRange ->
                                Text(
                                    text = timeRange,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = blackBackGround,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(top = 4.dp)
                .shadow(
                    elevation = 1.dp,
                    shape = cardShape
                )
                .clip(cardShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = defaultRipple,
                    onClick = onClick
                )
                .hoverable(interactionSource = interactionSource)
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = cardShape,
                colors = CardDefaults.cardColors(
                    containerColor = if (isInteracting) green300 else Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 1.dp
                ),
                border = BorderStroke(2.dp, if (isInteracting || isScheduled) green300 else borderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = team.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isInteracting) inverse else if (isScheduled) primaryTextColor else tertiary
                        )
                        Text(
                            text = "${team.memberCount}명",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isInteracting) inverse else if (isScheduled) primaryTextColor else tertiary
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 회의명이 있을 때만 표시
                    team.meetingName?.let { meetingName ->
                        Text(
                            text = meetingName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = if (isInteracting) green500 else if (isScheduled) blackBackGround else gray400
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // 날짜와 시간이 있을 때만 표시
                    if (team.date != null || team.timeRange != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            team.date?.let { date ->
                                Text(
                                    text = date,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isInteracting) gray500 else if (isScheduled) blackBackGround else gray400,
                                    fontWeight = if (isInteracting) FontWeight.Normal else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            }
                            team.timeRange?.let { timeRange ->
                                Text(
                                    text = timeRange,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isInteracting) gray500 else if (isScheduled) blackBackGround else gray400,
                                    fontWeight = if (isInteracting) FontWeight.Normal else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

