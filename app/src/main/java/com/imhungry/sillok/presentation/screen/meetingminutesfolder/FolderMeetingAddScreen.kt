package com.imhungry.sillok.presentation.screen.meetingminutesfolder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.sillok.presentation.screen.home.SearchBar
import com.imhungry.sillok.presentation.viewmodel.meetingminutesfolder.FolderDetailViewModel
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.components.ScreenHeader
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.theme.primaryBackground

@Composable
fun FolderMeetingAddScreen(
    folderId: Long,
    onBackClick: () -> Unit,
    onMeetingToggle: (Long, Boolean) -> Unit = { _, _ -> },
    onComplete: (String) -> Unit,
    viewModel: FolderDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchFocused by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var isEditMode by remember { mutableStateOf(false) }

    LaunchedEffect(folderId) {
        //viewModel.loadFolderDetail(folderId)
    }

    BasicBox(
        statusBarColor = primaryBackground,
        navigationBarColor = primaryBackground,
        backgroundColor = primaryBackground,
        isLoading = state.isLoading
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ScreenHeader(
                title = state.folderName.ifEmpty { "폴더 이름" },
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            SearchBar(
                modifier = Modifier
                    .fillMaxWidth(),
                focusRequester = focusRequester,
                text = searchText,
                innerText = "회의 제목, 참석자, 팀 이름으로 검색",
                onTextChange = { searchText = it },
                onFocusChange = { isSearchFocused = it },
                onImeAction = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    isSearchFocused = false
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 회의 리스트
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                items(state.meetings) { meeting ->
                    FolderMeetingEditItem(
                        title = meeting.title,
                        date = meeting.date,
                        isSelected = meeting.isSelected,
                        onToggle = {
                            viewModel.toggleMeetingSelection(meeting.id, !meeting.isSelected)
                            onMeetingToggle(meeting.id, !meeting.isSelected)
                        }
                    )
                }
            }

            // 하단 버튼
            SillokButton(
                text = "추가하기",
                onClick = { 
                    // TODO: 선택된 회의록 추가 처리
                    onComplete("")
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

