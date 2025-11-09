package com.imhungry.sillok.presentation.screen.team

import android.util.Patterns
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.sillok.presentation.screen.meetingform.components.ErrorText
import com.imhungry.sillok.presentation.screen.meetingform.components.SelectedEmailsList
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.components.ScreenHeader
import com.imhungry.sillok.ui.components.ScreenHeaderWithNotification
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.theme.border
import com.imhungry.sillok.ui.theme.gray400
import com.imhungry.sillok.ui.theme.gray500
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.primaryTextColor

@Composable
fun TeamFormScreen(
    onBackClick: () -> Unit,
    onComplete: (String, List<String>) -> Unit
) {
    var teamName by remember { mutableStateOf("") }
    var showMemberInvite by remember { mutableStateOf(false) }
    var memberEmails by remember { mutableStateOf<List<String>>(emptyList()) }
    var emailInput by remember { mutableStateOf("") }
    var showValidationErrors by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    BasicBox(
        statusBarColor = primaryBackground,
        navigationBarColor = primaryBackground,
        backgroundColor = primaryBackground,
        isLoading = false
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
                    if (showMemberInvite) {
                        showMemberInvite = false
                    } else {
                        onBackClick()
                    }
                }
            )

            if (!showMemberInvite) {
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
                            value = teamName,
                            onValueChange = { 
                                teamName = it
                                if (showValidationErrors && it.trim().isNotEmpty()) {
                                    showValidationErrors = false
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = if (teamName.isEmpty()) gray400 else primaryTextColor,
                                fontWeight = FontWeight.Normal
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, border, RoundedCornerShape(4.dp))
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (teamName.isEmpty()) {
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
                        if (showValidationErrors && teamName.trim().isEmpty()) {
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
                            if (teamName.trim().isNotEmpty()) {
                                showValidationErrors = false
                                showMemberInvite = true
                            } else {
                                showValidationErrors = true
                            }
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
                            value = emailInput,
                            onValueChange = { 
                                emailInput = it
                                if (showValidationErrors && memberEmails.isNotEmpty()) {
                                    showValidationErrors = false
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    val trimmedEmail = emailInput.trim()
                                    if (trimmedEmail.isNotEmpty() &&
                                        Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() &&
                                        !memberEmails.contains(trimmedEmail)
                                    ) {
                                        memberEmails = memberEmails + trimmedEmail
                                        emailInput = ""
                                        if (showValidationErrors) {
                                            showValidationErrors = false
                                        }
                                    }
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = if (emailInput.isEmpty()) gray400 else primaryTextColor,
                                fontWeight = FontWeight.Normal
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, border, RoundedCornerShape(4.dp))
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (emailInput.isEmpty()) {
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
                        if (showValidationErrors && memberEmails.isEmpty()) {
                            ErrorText(
                                text = "최소 1명 이상의 멤버를 추가해주세요.",
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // 추가된 이메일 목록
                    if (memberEmails.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SelectedEmailsList(
                            emails = memberEmails,
                            onEmailRemoved = { index ->
                                memberEmails = memberEmails.filterIndexed { i, _ -> i != index }
                                if (showValidationErrors && memberEmails.size == 1) {
                                    showValidationErrors = false
                                }
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
                                showMemberInvite = false
                            },
                            modifier = Modifier.weight(1f),
                            backgroundColor = primaryTextColor
                        )

                        SillokButton(
                            text = "완료",
                            onClick = {
                                if (teamName.trim().isEmpty()) {
                                    showValidationErrors = true
                                } else if (memberEmails.isEmpty()) {
                                    showValidationErrors = true
                                } else {
                                    showValidationErrors = false
                                    onComplete(teamName.trim(), memberEmails)
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
