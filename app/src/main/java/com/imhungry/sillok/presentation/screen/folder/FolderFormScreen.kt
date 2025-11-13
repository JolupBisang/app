package com.imhungry.sillok.presentation.screen.folder

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.sillok.presentation.screen.meetingform.components.ErrorText
import com.imhungry.sillok.presentation.viewmodel.folder.FolderFormViewModel
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.components.ScreenHeader
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.theme.border
import com.imhungry.sillok.ui.theme.gray400
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.primaryTextColor

@Composable
fun FolderFormScreen(
    onBackClick: () -> Unit,
    onComplete: () -> Unit,
    viewModel: FolderFormViewModel = hiltViewModel()
) {
    var folderName by remember { mutableStateOf("") }
    var showValidationErrors by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val state by viewModel.state.collectAsState()

    // 성공 시 콜백 호출
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.resetSuccess()
            onComplete()
        }
    }

    // 에러가 발생하면 로컬 에러 상태도 업데이트
    LaunchedEffect(state.error) {
        if (state.error != null) {
            showValidationErrors = true
        }
    }

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
                title = "폴더 생성",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(120.dp))

                // 안내 문구
                Text(
                    text = "폴더 이름을 입력해주세요.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 폴더 이름 입력 필드
                Column {
                    BasicTextField(
                        value = folderName,
                        onValueChange = { 
                            folderName = it
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
                            color = if (folderName.isEmpty()) gray400 else primaryTextColor,
                            fontWeight = FontWeight.Normal
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, border, RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (folderName.isEmpty()) {
                                Text(
                                    text = "폴더 이름 입력",
                                    color = gray400,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                            innerTextField()
                        }
                    )
                    // 폴더 이름 에러 메시지
                    if ((showValidationErrors && folderName.trim().isEmpty()) || state.error != null) {
                        ErrorText(
                            text = "폴더 이름을 입력해주세요.",
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 하단 버튼들
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SillokButton(
                        text = "이전으로",
                        onClick = onBackClick,
                        modifier = Modifier.weight(1f),
                        backgroundColor = primaryTextColor,
                    )

                    SillokButton(
                        text = "완료",
                        onClick = {
                            if (folderName.trim().isNotEmpty()) {
                                showValidationErrors = false
                                viewModel.clearError()
                                viewModel.createFolder(folderName.trim())
                            } else {
                                showValidationErrors = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isLoading
                    )
                }
            }
        }
    }
}
