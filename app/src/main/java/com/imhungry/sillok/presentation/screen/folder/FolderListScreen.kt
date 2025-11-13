package com.imhungry.sillok.presentation.screen.folder

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.imhungry.sillok.presentation.screen.home.SearchBar
import com.imhungry.sillok.presentation.viewmodel.folder.FolderListViewModel
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.components.ScreenHeaderWithNotification
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.theme.danger
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.primaryTextColor

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FolderListScreen(
    onBackClick: () -> Unit,
    onNavigateToCreateFolder: () -> Unit = {},
    onNavigateToFolderDetail: (Long, String) -> Unit = { _, _ -> },
    onNavigateToNotificationHistory: () -> Unit = {},
    viewModel: FolderListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchFocused by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var isEditMode by remember { mutableStateOf(false) }
    var selectedFolderIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    HandleBackPress(
        searchText = searchText,
        isSearchFocused = isSearchFocused,
        isEditMode = isEditMode,
        onSearchClose = {
            clearSearchFocus(focusManager, keyboardController) { isSearchFocused = false }
            searchText = ""
        },
        onEditModeExit = { 
            selectedFolderIds = emptySet()
            isEditMode = false 
        },
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
                    title = "회의록 폴더",
                    onBackClick = onBackClick,
                    onNavigateToNotificationHistory = onNavigateToNotificationHistory
                )

                // 검색바는 폴더가 있을 때만 표시
                if (state.folders.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    SearchBar(
                        modifier = Modifier
                            .fillMaxWidth(),
                        focusRequester = focusRequester,
                        text = searchText,
                        innerText = "회의 제목, 참석자로 검색",
                        onTextChange = { searchText = it },
                        onFocusChange = { isSearchFocused = it },
                        onImeAction = {
                            clearSearchFocus(focusManager, keyboardController) {
                                isSearchFocused = false
                            }
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 리스트 영역과 버튼을 분리하여 버튼이 항상 하단에 위치하도록
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    if (!isSearchFocused && searchText.isEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.folders) { folder ->
                                val isSelected = selectedFolderIds.contains(folder.id)
                                FolderCard(
                                    folderName = folder.name,
                                    date = folder.date,
                                    meetingTitle = folder.meetingName ?: "",
                                    timeRange = folder.timeRange,
                                    onClick = { 
                                        if (isEditMode) {
                                            selectedFolderIds = if (selectedFolderIds.contains(folder.id)) {
                                                selectedFolderIds - folder.id
                                            } else {
                                                selectedFolderIds + folder.id
                                            }
                                        } else {
                                            onNavigateToFolderDetail(folder.id, folder.name)
                                        }
                                    },
                                    editMode = isEditMode,
                                    isPast = folder.isPast,
                                    isSelected = isSelected,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        // 검색 결과 영역 (추후 구현)
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(16.dp))

                // 버튼은 항상 하단에 고정
                if (!(!isSearchFocused && searchText.isEmpty() && state.folders.isEmpty())) {
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
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (isEditMode) {
                                // Edit 모드: 이전으로, 삭제하기
                                SillokButton(
                                    text = "이전으로",
                                    onClick = { 
                                        selectedFolderIds = emptySet()
                                        isEditMode = false 
                                    },
                                    modifier = Modifier.weight(1f),
                                    backgroundColor = primaryTextColor
                                )
                                SillokButton(
                                    text = "삭제하기",
                                    onClick = {
                                        if (selectedFolderIds.isNotEmpty()) {
                                            viewModel.deleteFolders(
                                                folderIds = selectedFolderIds.toList(),
                                                onSuccess = {
                                                    selectedFolderIds = emptySet()
                                                    isEditMode = false
                                                }
                                            )
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    backgroundColor = danger,
                                    enabled = selectedFolderIds.isNotEmpty() && !state.isLoading
                                )
                            } else {
                                // 일반 모드: 팀 선택, 팀 생성
                                SillokButton(
                                    text = "폴더 선택",
                                    onClick = { 
                                        selectedFolderIds = emptySet()
                                        isEditMode = true 
                                    },
                                    modifier = Modifier.weight(1f),
                                    backgroundColor = primaryTextColor
                                )
                                SillokButton(
                                    text = "폴더 생성",
                                    onClick = onNavigateToCreateFolder,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            if (!isSearchFocused && searchText.isEmpty() && state.folders.isEmpty()) {
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
                            text = "회의록 폴더에 대해",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "회의를 더욱 체계적으로 정리할 수 있습니다.",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "폴더를 생성하여 시작해보세요!",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(64.dp))
                        SillokButton(
                            text = "폴더 생성하기",
                            onClick = onNavigateToCreateFolder,
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
    isEditMode: Boolean,
    onSearchClose: () -> Unit,
    onEditModeExit: () -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler {
        when {
            searchText.isNotBlank() || isSearchFocused -> {
                onSearchClose()
            }
            isEditMode -> {
                onEditModeExit()
            }
            else -> {
                onBackClick()
            }
        }
    }
}