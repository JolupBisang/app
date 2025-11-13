package com.imhungry.sillok.presentation.screen.meetingform.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.imhungry.sillok.presentation.screen.home.SearchBar
import com.imhungry.sillok.presentation.state.meetingform.TeamInfo
import com.imhungry.sillok.ui.components.MediumSillokButton
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.components.SmallSillokButton
import com.imhungry.sillok.ui.theme.dialogBackGround
import com.imhungry.sillok.ui.theme.gray500
import com.imhungry.sillok.ui.theme.green300
import com.imhungry.sillok.ui.theme.green500
import com.imhungry.sillok.ui.theme.primaryButton

private fun clearSearchFocus(
    focusManager: androidx.compose.ui.focus.FocusManager,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?,
    onComplete: () -> Unit
) {
    focusManager.clearFocus()
    keyboardController?.hide()
    onComplete()
}

@Composable
fun TeamSearchDialog(
    visible: Boolean,
    teams: List<TeamInfo>,
    searchText: String,
    selectedTeam: TeamInfo?,
    onDismiss: () -> Unit,
    onSearchTextChange: (String) -> Unit,
    onTeamSelected: (TeamInfo) -> Unit = {},
    onInviteClick: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var isSearchFocused by remember { mutableStateOf(false) }

    // 다이얼로그가 열릴 때 포커스 요청
//    LaunchedEffect(visible) {
//        if (visible) {
//            focusRequester.requestFocus()
//        }
//    }

    // 뒤로가기 처리
    BackHandler(enabled = visible) {
        onDismiss()
    }

    if (visible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1000f)
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
                    SearchBar(
                        modifier = Modifier.fillMaxWidth(),
                        focusRequester = focusRequester,
                        text = searchText,
                        innerText = "팀 이름으로 검색",
                        onTextChange = onSearchTextChange,
                        onFocusChange = { isSearchFocused = it },
                        onImeAction = {
                            clearSearchFocus(focusManager, keyboardController) {
                                isSearchFocused = false
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 검색된 팀 리스트
                    val filteredTeams = if (searchText.isBlank()) {
                        teams
                    } else {
                        val query = searchText.trim().lowercase()
                        teams.filter { team ->
                            team.name.lowercase().contains(query)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTeams) { team ->
                            val isSelected = selectedTeam?.id == team.id
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(green500)
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(1.dp, green300, RoundedCornerShape(4.dp))
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .clickable {
                                        onTeamSelected(team)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = team.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = primaryButton,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    MediumSillokButton(
                        text = "선택하기",
                        onClick = onInviteClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedTeam != null
                    )
                }
            }
        }
    }
}