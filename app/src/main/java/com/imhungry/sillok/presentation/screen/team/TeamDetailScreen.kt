package com.imhungry.sillok.presentation.screen.team

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.state.team.TeamMember
import com.imhungry.sillok.presentation.viewmodel.team.TeamDetailViewModel
import com.imhungry.sillok.ui.components.BasicBoxWithGradientBurshBackground
import com.imhungry.sillok.ui.components.ScreenHeaderWithNotification
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.theme.gray200
import com.imhungry.sillok.ui.theme.gray400
import com.imhungry.sillok.ui.theme.green300
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.primaryTextColor

@Composable
fun TeamDetailScreen(
    teamId: Long,
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit = {},
    viewModel: TeamDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentUserId = state.currentUserId
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isEditMode by remember { mutableStateOf(false) }
    var editableMembers by remember { mutableStateOf(state.members) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var isEditingDescription by remember { mutableStateOf(false) }
    var editedDescription by remember { mutableStateOf(TextFieldValue("")) }
    val descriptionFocusRequester = remember { FocusRequester() }

    // teamId가 변경될 때 데이터 로드
    LaunchedEffect(teamId) {
        viewModel.loadTeamDetail(teamId)
    }

    // state의 members가 변경될 때 editableMembers 업데이트 (편집 모드가 아닐 때만)
    LaunchedEffect(state.members) {
        if (!isEditMode) {
            editableMembers = state.members
        }
    }

    // 편집 모드 시작 시 포커스 요청
    LaunchedEffect(isEditingDescription) {
        if (isEditingDescription) {
            descriptionFocusRequester.requestFocus()
        }
    }

    // state의 teamDescription이 변경될 때 editedDescription 업데이트
    LaunchedEffect(state.teamDescription) {
        if (!isEditingDescription) {
            editedDescription = TextFieldValue(state.teamDescription)
        }
    }

    // 시스템 뒤로가기 버튼 처리
    BackHandler(enabled = isEditingDescription || isEditMode) {
        when {
            isEditingDescription -> {
                // 한줄소개 편집 모드 종료
                isEditingDescription = false
                focusManager.clearFocus()
                keyboardController?.hide()
            }
            isEditMode -> {
                // 멤버 편집 모드 종료
                isEditMode = false
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        BasicBoxWithGradientBurshBackground(
            statusBarColor = primaryBackground,
            navigationBarColor = primaryBackground,
            backgroundColor = primaryBackground,
            isLoading = state.isLoading
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 헤더
                ScreenHeaderWithNotification(
                    title = "팀 정보",
                    onBackClick = {
                        when {
                            isEditingDescription -> {
                                // 한줄소개 편집 모드 종료
                                isEditingDescription = false
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                            isEditMode -> {
                                // 멤버 편집 모드 종료
                                isEditMode = false
                            }
                            else -> {
                                // 편집 모드가 아니면 화면 나가기
                                onBackClick()
                            }
                        }
                    },
                    onNavigateToNotificationHistory = onNotificationClick,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        // 팀 이름 섹션
                        item {
                            Spacer(Modifier.height(32.dp))
                            Text(
                                text = state.teamName.ifEmpty { "팀 이름" },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }

                        // 한줄소개 섹션
//                        item {
//                            Spacer(Modifier.height(4.dp))
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                if (isEditingDescription) {
//                                    BasicTextField(
//                                        value = editedDescription,
//                                        onValueChange = { newValue ->
//                                            if (newValue.text.length <= 50) {
//                                                editedDescription = newValue
//                                            }
//                                        },
//                                        modifier = Modifier
//                                            .weight(1f)
//                                            .focusRequester(descriptionFocusRequester)
//                                            .clip(RoundedCornerShape(4.dp))
//                                            .padding(end = 6.dp),
//                                        textStyle = MaterialTheme.typography.bodyMedium.copy(
//                                            fontWeight = FontWeight.Medium,
//                                            fontSize = 14.sp
//                                        ),
//                                        maxLines = 2,
//                                        keyboardOptions = KeyboardOptions(
//                                            imeAction = ImeAction.Done
//                                        ),
//                                        keyboardActions = KeyboardActions(
//                                            onDone = {
//                                                viewModel.updateTeamDescription(editedDescription.text)
//                                                isEditingDescription = false
//                                                focusManager.clearFocus()
//                                                keyboardController?.hide()
//                                            }
//                                        ),
//                                        decorationBox = { innerTextField ->
//                                            if (editedDescription.text.isEmpty()) {
//                                                Text(
//                                                    text = "한줄소개(50자)",
//                                                    color = gray400,
//                                                    style = MaterialTheme.typography.bodyMedium,
//                                                    fontWeight = FontWeight.Medium,
//                                                    fontSize = 14.sp
//                                                )
//                                            }
//                                            innerTextField()
//                                        }
//                                    )
//                                } else {
//                                Text(
//                                        text = state.teamDescription.ifEmpty { "한줄소개(50자)" },
//                                    style = MaterialTheme.typography.bodyMedium,
//                                    fontWeight = FontWeight.Medium,
//                                    fontSize = 14.sp,
//                                    color = gray200,
//                                    modifier = Modifier.weight(1f)
//                                )
//                                }
//                                Box(
//                                    modifier = Modifier.size(30.dp),
//                                    contentAlignment = Alignment.BottomCenter
//                                ) {
//                                    Image(
//                                        painter = painterResource(
//                                            id = if (isEditingDescription) R.drawable.save else R.drawable.edit
//                                        ),
//                                        contentDescription = if (isEditingDescription) "저장" else "편집",
//                                        modifier = Modifier
//                                            .size(16.dp)
//                                            .clickable(
//                                                interactionSource = remember { MutableInteractionSource() },
//                                                indication = null
//                                            ) {
//                                                if (isEditingDescription) {
//                                                    // 저장
//                                                    viewModel.updateTeamDescription(editedDescription.text)
//                                                    isEditingDescription = false
//                                                    focusManager.clearFocus()
//                                                    keyboardController?.hide()
//                                                } else {
//                                                    // 편집 모드 시작 - 커서를 텍스트 끝에 위치
//                                                    val text = state.teamDescription
//                                                    editedDescription = TextFieldValue(
//                                                        text = text,
//                                                        selection = TextRange(text.length)
//                                                    )
//                                                    isEditingDescription = true
//                                                }
//                                            }
//                                    )
//                                }
//                            }
//                        }

                        // 멤버 섹션
                        item {
                            Spacer(Modifier.height(28.dp))
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "멤버  ",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                    Text(
                                        text = "${editableMembers.size}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = if (isEditMode) "완료" else "관리",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = green300,
                                        modifier = Modifier.clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            if (isEditMode) {
                                                // 완료 버튼 클릭 시 편집 모드 종료
                                                isEditMode = false
                                            } else {
                                                // 관리 버튼 클릭 시 편집 모드 시작
                                                isEditMode = true
                                                editableMembers = state.members.toList()
                                            }
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                // 멤버 리스트
                                if (editableMembers.isNotEmpty()) {
                                    Column {
                                        editableMembers.forEach { member ->
                                            MemberItem(
                                                nickname = member.nickname,
                                                email = member.email,
                                                profileImage = member.profileImage,
                                                userId = member.id,
                                                isEditMode = isEditMode,
                                                canRemove = currentUserId != null && member.id != currentUserId,
                                                onRemove = {
                                                    editableMembers = editableMembers.filter { it.id != member.id }
                                                    viewModel.removeMember(member.id)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 하단 초대하기 버튼
                SillokButton(
                    text = "초대하기",
                    onClick = {
                        showInviteDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = primaryTextColor,
                )
            }
        }
        // 멤버 초대 다이얼로그
        MemberInvitationDialog(
            visible = showInviteDialog,
            onDismiss = {
                showInviteDialog = false
            },
            onInvite = { email, onResult ->
                viewModel.addTeamMember(email, onResult)
            }
        )
    }
}
