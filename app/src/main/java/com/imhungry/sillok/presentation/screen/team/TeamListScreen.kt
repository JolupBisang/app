package com.imhungry.sillok.presentation.screen.team

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.imhungry.sillok.R
import com.imhungry.sillok.domain.model.team.TeamDetailSummary
import com.imhungry.sillok.presentation.screen.home.SearchBar
import com.imhungry.sillok.presentation.viewmodel.team.TeamListViewModel
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.components.ScreenHeaderWithNotification
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.theme.danger
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.secondaryButton
import com.imhungry.sillok.ui.theme.whiteBackground

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TeamListScreen(
    onBackClick: () -> Unit,
    onNavigateToCreateTeam: () -> Unit = {},
    onNavigateToTeamDetail: (Long) -> Unit = {},
    onNavigateToNotificationHistory: () -> Unit = {},
    onSelectTeam: () -> Unit = {},
    viewModel: TeamListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchFocused by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var isEditMode by remember { mutableStateOf(false) }
    var selectedTeamIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    
    // 검색어 제출 시 ViewModel 업데이트
    // 검색어 입력 중에는 업데이트하지 않고, 제출 시에만 업데이트

    HandleBackPress(
        searchText = searchText,
        isSearchFocused = isSearchFocused,
        isEditMode = isEditMode,
        onSearchClose = {
            clearSearchFocus(focusManager, keyboardController) { isSearchFocused = false }
            searchText = ""
            viewModel.updateSearchQuery(null)
        },
        onEditModeExit = { isEditMode = false },
        onBackClick = onBackClick
    )

    BasicBox(
        statusBarColor = primaryBackground,
        navigationBarColor = primaryBackground,
        backgroundColor = primaryBackground,
        isLoading = state.isLoading
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (isSearchFocused) {
                            clearSearchFocus(focusManager, keyboardController) {
                                isSearchFocused = false
                            }
                        }
                    }
            ) {
                ScreenHeaderWithNotification(
                    title = "팀 목록",
                    onBackClick = onBackClick,
                    onNavigateToNotificationHistory = onNavigateToNotificationHistory
                )

                // 검색바 표시
                Spacer(modifier = Modifier.height(16.dp))

                SearchBar(
                    modifier = Modifier
                        .fillMaxWidth(),
                    focusRequester = focusRequester,
                    text = searchText,
                    innerText = "팀 이름으로 검색",
                    onTextChange = { searchText = it },
                    onFocusChange = { isSearchFocused = it },
                    onImeAction = {
                        val query = searchText.trim()
                        viewModel.updateSearchQuery(query.takeIf { it.isNotBlank() })
                        clearSearchFocus(focusManager, keyboardController) {
                            isSearchFocused = false
                        }
                    }
                )

                // 콘텐츠 영역
                Box(modifier = Modifier.weight(1f)) {
                    TeamListContent(
                        teams = state.teams,
                        isLoading = state.isLoading,
                        editMode = isEditMode,
                        selectedTeamIds = selectedTeamIds,
                        onRefresh = { viewModel.refresh() },
                        onNavigateToTeamDetail = onNavigateToTeamDetail,
                        onTeamToggle = { teamId ->
                            selectedTeamIds = if (selectedTeamIds.contains(teamId)) {
                                selectedTeamIds - teamId
                            } else {
                                selectedTeamIds + teamId
                            }
                        }
                    )
                }

                // 버튼 영역 (항상 표시)
                val density = LocalDensity.current
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0f),
                                        Color.White.copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0.6f),
                                        Color.White.copy(alpha = 0.9f)
                                    ),
                                    startY = 0f,
                                    endY = with(density) { 32.dp.toPx() }
                                )
                            )
                            .offset(y = (-32).dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isEditMode) {
                            // Edit 모드: 이전으로, 삭제하기
                            SillokButton(
                                text = "이전으로",
                                onClick = { isEditMode = false },
                                modifier = Modifier.weight(1f),
                                backgroundColor = primaryTextColor
                            )
                            SillokButton(
                                text = "삭제하기",
                                onClick = {
                                    if (selectedTeamIds.isNotEmpty()) {
                                        viewModel.deleteTeams(selectedTeamIds) {
                                            // 삭제 성공 후 상태 초기화
                                            selectedTeamIds = emptySet()
                                            isEditMode = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                backgroundColor = danger,
                                enabled = selectedTeamIds.isNotEmpty()
                            )
                        } else {
                            // 일반 모드: 팀 선택, 팀 생성
                            SillokButton(
                                text = "팀 선택",
                                onClick = { 
                                    selectedTeamIds = emptySet()
                                    isEditMode = true 
                                },
                                modifier = Modifier.weight(1f),
                                backgroundColor = primaryTextColor
                            )
                            SillokButton(
                                text = "팀 생성",
                                onClick = onNavigateToCreateTeam,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun clearSearchFocus(
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?,
    onFocusCleared: () -> Unit
) {
    focusManager.clearFocus()
    keyboardController?.hide()
    onFocusCleared()
}

@Composable
private fun HandleBackPress(
    searchText: String,
    isSearchFocused: Boolean,
    isEditMode: Boolean,
    onSearchClose: () -> Unit,
    onEditModeExit: () -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler {
        when {
            isEditMode -> {
                onEditModeExit()
            }
            searchText.isNotBlank() || isSearchFocused -> {
                onSearchClose()
            }
            else -> {
                onBackClick()
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TeamListContent(
    teams: List<TeamDetailSummary>,
    isLoading: Boolean,
    editMode: Boolean,
    selectedTeamIds: Set<Long>,
    onRefresh: () -> Unit,
    onNavigateToTeamDetail: (Long) -> Unit,
    onTeamToggle: (Long) -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = onRefresh
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        if (teams.isEmpty() && !isLoading) {
            // 빈 상태 표시
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(R.drawable.bubble2)
                                .decoderFactory(GifDecoder.Factory())
                                .build()
                        ),
                        contentDescription = "말풍선 gif",
                        modifier = Modifier.size(140.dp)
                    )
                    Text(
                        text = "팀 작업에 대해",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "팀을 생성하면 회의 초대가 더욱 간편해집니다.",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "지금 바로 시작해보세요!",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 12.dp,
                    bottom = 16.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(teams) { team ->
                    val isSelected = selectedTeamIds.contains(team.id)
                    
                    TeamCard(
                        team = team,
                        onClick = { 
                            if (editMode) {
                                onTeamToggle(team.id)
                            } else {
                                onNavigateToTeamDetail(team.id)
                            }
                        },
                        editMode = editMode,
                        isSelected = isSelected,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}