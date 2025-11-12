package com.imhungry.sillok.presentation.screen.team

import android.util.Patterns
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.imhungry.sillok.R
import com.imhungry.sillok.ui.theme.border
import com.imhungry.sillok.ui.theme.danger
import com.imhungry.sillok.ui.theme.dialogBackGround
import com.imhungry.sillok.ui.theme.gray400
import com.imhungry.sillok.ui.theme.green300
import com.imhungry.sillok.ui.theme.primaryTextColor

@Composable
fun MemberInvitationDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onInvite: (String, (Boolean, String?) -> Unit) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // 다이얼로그가 닫힐 때 상태 초기화
    if (!visible) {
        email = ""
        resultMessage = null
        isSuccess = false
    }

    // 이메일 유효성 검사만 수행하는 함수
    val validateEmail: () -> Boolean = {
        val trimmedEmail = email.trim()
        
        when {
            trimmedEmail.isEmpty() -> {
                resultMessage = "이메일을 입력해주세요."
                isSuccess = false
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> {
                resultMessage = "올바른 이메일 형식이 아닙니다."
                isSuccess = false
                false
            }
            else -> {
                // 유효성 검사 통과 시 결과 메시지 초기화
                resultMessage = null
                true
            }
        }
    }

    // 초대 처리 함수 (유효성 검사 + API 호출)
    val handleInvite: () -> Unit = {
        // 먼저 유효성 검사
        if (validateEmail()) {
            // 유효성 검사 통과 시 API 호출
            val trimmedEmail = email.trim()
            onInvite(trimmedEmail) { success, message ->
                if (success) {
                    resultMessage = "성공적으로 초대되었습니다!"
                    isSuccess = true
                    // 성공 시 입력 필드 초기화
                    email = ""
                    focusManager.clearFocus()
                    keyboardController?.hide()
                } else {
                    resultMessage = "존재하지 않는 계정입니다."
                    isSuccess = false
                }
            }
        }
    }

    // 뒤로가기 처리
    BackHandler(enabled = visible) {
        onDismiss()
    }

    if (visible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1000f)
                .background(dialogBackGround)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
                .padding(bottom = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 20.dp,
                modifier = Modifier.clickable(enabled = false) { }
            ) {
                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .height(180.dp)
                        .clickable(enabled = false) { },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "추가할 멤버의 이메일을 입력해주세요.",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 이메일 입력 필드
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp, end = 32.dp, bottom = 4.dp)
                    ) {
                        BasicTextField(
                            value = email,
                            onValueChange = { 
                                email = it
                                // 입력 시 결과 메시지 초기화
                                if (resultMessage != null) {
                                    resultMessage = null
                                    isSuccess = false
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    // 키보드 완료도 API 호출 수행
                                    handleInvite()
                                }
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = if (email.isEmpty()) gray400 else primaryTextColor,
                                fontWeight = FontWeight.Normal
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, border, RoundedCornerShape(4.dp))
                                .padding(horizontal = 12.dp, vertical = 9.dp)
                                .padding(end = 32.dp), // 전송 버튼 공간 확보
                            decorationBox = { innerTextField ->
                                if (email.isEmpty()) {
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
                        // 전송 버튼 (초록색 종이비행기 아이콘) - 입력 필드 위에 겹치도록 배치
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(32.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    handleInvite()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.invitation),
                                contentDescription = "초대",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // 결과 메시지
                    if (resultMessage != null) {
                        ResultText(
                            text = resultMessage!!,
                            color = if (isSuccess) green300 else danger,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResultText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(color = color),
    )
}