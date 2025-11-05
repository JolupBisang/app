package com.imhungry.sillok.presentation.screen.team

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.screen.home.SearchBar
import com.imhungry.sillok.presentation.screen.team.TeamCard
import com.imhungry.sillok.presentation.viewmodel.team.TeamListViewModel
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.components.ScreenHeader
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.theme.primaryBackground

@Composable
fun TeamListScreen(
    onBackClick: () -> Unit,
    onNavigateToCreateTeam: () -> Unit = {},
    onNavigateToTeamDetail: (Long) -> Unit = {},
    onNavigateToNotificationHistory: () -> Unit = {},
    viewModel: TeamListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchFocused by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    HandleBackPress(
        searchText = searchText,
        isSearchFocused = isSearchFocused,
        onSearchClose = {
            clearSearchFocus(focusManager, keyboardController) { isSearchFocused = false }
            searchText = ""
        },
        onBackClick = onBackClick
    )

    BasicBox(
        statusBarColor = primaryBackground,
        navigationBarColor = primaryBackground,
        backgroundColor = primaryBackground
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
                            clearSearchFocus(focusManager, keyboardController) { isSearchFocused = false }
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.prev),
                        contentDescription = "뒤로가기",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onBackClick() }
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "팀 목록",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Image(
                        painter = painterResource(id = R.drawable.alarm),
                        contentDescription = "알림",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onNavigateToNotificationHistory() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SearchBar(
                    modifier = Modifier
                        .fillMaxWidth(),
                    focusRequester = focusRequester,
                    text = searchText,
                    innerText = "팀 이름, 멤버로 검색",
                    onTextChange = { searchText = it },
                    onFocusChange = { isSearchFocused = it },
                    onImeAction = {
                        clearSearchFocus(focusManager, keyboardController) { isSearchFocused = false }
                    }
                )

                if (!isSearchFocused && searchText.isEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.teams) { team ->
                            TeamCard(
                                teamName = team.name,
                                memberCount = team.memberCount,
                                date = team.date,
                                timeRange = team.timeRange,
                                onClick = { onNavigateToTeamDetail(team.id) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    // 검색 결과 영역 (추후 구현)
                }
                
                // 팀 생성하기 버튼 (빈 상태가 아닐 때만 표시)
                if (!(!isSearchFocused && searchText.isEmpty() && state.teams.isEmpty())) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SillokButton(
                        text = "팀 생성하기",
                        onClick = onNavigateToCreateTeam,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (!isSearchFocused && searchText.isEmpty() && state.teams.isEmpty()) {
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
                        Spacer(modifier = Modifier.height(64.dp))
                        SillokButton(
                            text = "폴더 생성하기",
                            onClick = onNavigateToCreateTeam,
                            modifier = Modifier.padding(horizontal = 48.dp)
                        )
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
    onSearchClose: () -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler {
        when {
            searchText.isNotBlank() || isSearchFocused -> {
                onSearchClose()
            }
            else -> {
                onBackClick()
            }
        }
    }
}