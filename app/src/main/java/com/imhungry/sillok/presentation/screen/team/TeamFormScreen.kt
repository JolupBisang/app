package com.imhungry.sillok.presentation.screen.team

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.sillok.presentation.screen.meetingform.components.ErrorText
import com.imhungry.sillok.presentation.screen.meetingform.components.SelectedEmailsList
import com.imhungry.sillok.presentation.viewmodel.team.TeamFormViewModel
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.components.ScreenHeader
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.theme.border
import com.imhungry.sillok.ui.theme.gray400
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.primaryTextColor

@Composable
fun TeamFormScreen(
    onBackClick: () -> Unit,
    onComplete: (String, List<String>) -> Unit,
    viewModel: TeamFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    BasicBox(
        statusBarColor = primaryBackground,
        navigationBarColor = primaryBackground,
        backgroundColor = primaryBackground,
        isLoading = state.isLoading
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                }
        ) {
            ScreenHeader(
                title = "팀 생성",
                onBackClick = {
                    if (state.showMemberInvite) {
                        viewModel.hideMemberInviteScreen()
                    } else {
                        onBackClick()
                    }
                }
            )

            if (!state.showMemberInvite) {
                // 팀 이름 입력 화면
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.height(120.dp))

                    // 안내 문구
                    Text(
                        text = "생성할 팀 이름을 입력해주세요.",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 팀 이름 입력 필드
                    Column {
                        BasicTextField(
                            value = state.teamName,
                            onValueChange = { viewModel.updateTeamName(it) },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = if (state.teamName.isEmpty()) gray400 else primaryTextColor,
                                fontWeight = FontWeight.Normal
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, border, RoundedCornerShape(4.dp))
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (state.teamName.isEmpty()) {
                                    Text(
                                        text = "팀 이름 입력 ex) 고라니팀",
                                        color = gray400,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                                innerTextField()
                            }
                        )
                        // 팀 이름 에러 메시지
                        if (state.showValidationErrors && state.teamName.trim().isEmpty()) {
                            ErrorText(
                                text = "팀 이름을 입력해주세요.",
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 다음 버튼
                    SillokButton(
                        text = "다음",
                        onClick = {
                            viewModel.showMemberInviteScreen()
                        }
                    )
                }
            } else {
                // 멤버 초대 화면
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.height(120.dp))

                    // 안내 문구
                    Text(
                        text = "추가할 멤버들의 이메일을 입력해주세요.",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 이메일 입력 필드
                    Column {
                        BasicTextField(
                            value = state.emailInput,
                            onValueChange = { viewModel.updateEmailInput(it) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    viewModel.addEmail()
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = if (state.emailInput.isEmpty()) gray400 else primaryTextColor,
                                fontWeight = FontWeight.Normal
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, border, RoundedCornerShape(4.dp))
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (state.emailInput.isEmpty()) {
                                    Text(
                                        text = "이메일 입력",
                                        color = gray400,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                                innerTextField()
                            }
                        )
                        // 이메일 에러 메시지
                        if (state.showValidationErrors && state.memberEmails.isEmpty()) {
                            ErrorText(
                                text = "최소 1명 이상의 멤버를 추가해주세요.",
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // 추가된 이메일 목록
                    if (state.memberEmails.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SelectedEmailsList(
                            emails = state.memberEmails,
                            onEmailRemoved = { index ->
                                viewModel.removeEmail(index)
                            },
                            isReadOnly = false,
                            hostEmail = null,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 하단 버튼들
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SillokButton(
                            text = "이전으로",
                            onClick = {
                                viewModel.hideMemberInviteScreen()
                            },
                            modifier = Modifier.weight(1f),
                            backgroundColor = primaryTextColor
                        )

                        SillokButton(
                            text = "완료",
                            onClick = {
                                viewModel.createTeam { teamId ->
                                    onComplete(state.teamName.trim(), state.memberEmails)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
