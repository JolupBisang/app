package com.imhungry.sillok.presentation.screen.folder

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.viewmodel.folder.FolderDetailViewModel
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.components.ScreenHeader
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.theme.danger
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.primaryTextColor

data class FolderMeetingItem(
    val id: Long,
    val title: String,
    val date: String,
    val isSelected: Boolean = false
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FolderDetailScreen(
    folderId: Long,
    folderName: String = "",
    onBackClick: () -> Unit,
    onMeetingToggle: (Long, Boolean) -> Unit = { _, _ -> },
    onAddClick: () -> Unit = {},
    viewModel: FolderDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchFocused by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var isEditMode by remember { mutableStateOf(false) }

    LaunchedEffect(folderId, folderName) {
        viewModel.loadFolderDetail(folderId, folderName)
    }

    // 시스템 뒤로가기 버튼 처리
    BackHandler(enabled = isEditMode) {
        isEditMode = false
    }

    BasicBox(
        statusBarColor = primaryBackground,
        navigationBarColor = primaryBackground,
        backgroundColor = primaryBackground,
        isLoading = state.isLoading
    ) {
        if (state.meetings.isEmpty()) {
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
                        text = "아직 회의록이 없어요",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "폴더가 비어 있습니다.",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "회의록을 추가하여 체계적으로 관리해보세요",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(64.dp))
                    SillokButton(
                        text = "회의록 추가하기",
                        onClick = { onAddClick() },
                        modifier = Modifier.padding(horizontal = 48.dp)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                ScreenHeader(
                    title = folderName,
                    onBackClick = {
                        if (isEditMode) {
                            isEditMode = false
                        } else {
                            onBackClick()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(18.dp))

                // 회의 리스트
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    items(state.meetings) { meeting ->
                        if (isEditMode) {
                            FolderMeetingEditItem(
                                title = meeting.title,
                                date = meeting.date,
                                isSelected = meeting.isSelected,
                                onToggle = {
                                    viewModel.toggleMeetingSelection(meeting.id, !meeting.isSelected)
                                    onMeetingToggle(meeting.id, !meeting.isSelected)
                                }
                            )
                        } else {
                            FolderMeetingItem(
                                title = meeting.title,
                                date = meeting.date
                            )
                        }
                    }
                }

                // 하단 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isEditMode) {
                        SillokButton(
                            text = "이전으로",
                            onClick = {
                                isEditMode = false
                            },
                            modifier = Modifier.weight(1f),
                            backgroundColor = primaryTextColor
                        )
                        SillokButton(
                            text = "삭제하기",
                            onClick = {
                                val selectedMeetingIds = state.meetings
                                    .filter { it.isSelected }
                                    .map { it.id }
                                if (selectedMeetingIds.isNotEmpty()) {
                                    viewModel.removeMeetings(
                                        meetingIds = selectedMeetingIds,
                                        onSuccess = {
                                            isEditMode = false
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            backgroundColor = danger,
                            enabled = state.meetings.any { it.isSelected } && !state.isLoading
                        )
                    } else {
                        SillokButton(
                            text = "수정하기",
                            onClick = { isEditMode = true },
                            modifier = Modifier.weight(1f),
                            backgroundColor = primaryTextColor
                        )

                        SillokButton(
                            text = "추가하기",
                            onClick = { onAddClick() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

