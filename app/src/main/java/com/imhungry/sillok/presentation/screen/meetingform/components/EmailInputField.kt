package com.imhungry.sillok.presentation.screen.meetingform.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.imhungry.sillok.R
import com.imhungry.sillok.ui.theme.border
import com.imhungry.sillok.ui.theme.gray400
import com.imhungry.sillok.ui.theme.green300
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.secondaryButton
import com.imhungry.sillok.ui.theme.tertiary

@Composable
fun EmailInputFieldWithAutocomplete(
    label: String,
    value: String = "",
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    emailSuggestions: List<String> = emptyList(),
    showEmailSuggestions: Boolean = false,
    isSearching: Boolean = false,
    participantEmails: List<String>,
    onEmailSelected: (String) -> Unit,
    onEmailSubmitted: (String) -> Unit,
    onEmailRemoved: (Int) -> Unit,
    isReadOnly: Boolean = false,
    hostEmail: String? = null,
    modifier: Modifier = Modifier
) {
    // 이미 선택된 이메일을 제외한 필터링된 제안 목록
    val filteredSuggestions = emailSuggestions.filter { email ->
        !participantEmails.contains(email)
    }
    
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!isReadOnly) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LabelText(
                    text = label
                )
                Box(
                    modifier = modifier.weight(1f)
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                // 포커스 제거 및 키보드 닫기 (직접 입력 추가 불가)
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = if (value.isEmpty()) gray400 else primaryTextColor,
                            fontWeight = FontWeight.Normal
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, border, RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        decorationBox = { innerTextField ->
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = gray400,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }
            // 선택된 이메일 목록을 입력 필드 아래에 표시
            if (participantEmails.isNotEmpty()) {
                SelectedEmailsList(
                    emails = participantEmails,
                    onEmailRemoved = onEmailRemoved,
                    isReadOnly = isReadOnly,
                    hostEmail = hostEmail,
                    modifier = Modifier.padding(start = 65.dp, top = 8.dp)
                )
            }
        }

        if (isReadOnly) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                LabelText(
                    text = label
                )
                // 선택된 이메일 목록을 입력 필드 아래에 표시
                if (participantEmails.isNotEmpty()) {
                    SelectedEmailsList(
                        emails = participantEmails,
                        onEmailRemoved = onEmailRemoved,
                        isReadOnly = isReadOnly,
                        hostEmail = hostEmail
                    )
                }
            }
        }
    }
}

@Composable
fun SelectedEmailsList(
    emails: List<String>,
    onEmailRemoved: (Int) -> Unit,
    isReadOnly: Boolean = false,
    hostEmail: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        emails.forEachIndexed { index, email ->
            val isHost = hostEmail == email
            val borderColor = when {
                isReadOnly && isHost -> green300
                else -> secondaryButton
            }
            val textColor = when {
                isReadOnly && isHost -> green300
                else -> tertiary
            }
            
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    .background(secondaryButton)
                    .padding(horizontal = 16.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = email,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor
                )

                // 생성 모드가 아닐 때만 삭제 버튼 표시
                if (!isReadOnly) {
                    Spacer(modifier = Modifier.width(16.dp))

                    Image(
                        painter = painterResource(id = R.drawable.delete),
                        contentDescription = "삭제",
                        modifier = Modifier
                            .size(9.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onEmailRemoved(index)
                            }
                    )
                }
            }
        }
    }
}
