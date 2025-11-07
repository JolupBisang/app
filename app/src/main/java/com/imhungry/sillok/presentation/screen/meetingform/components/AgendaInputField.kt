package com.imhungry.sillok.presentation.screen.meetingform.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import com.imhungry.sillok.R
import com.imhungry.sillok.ui.theme.gray400
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.secondaryButton

@Composable
fun AgendaInputField(
    label: String,
    agendas: List<String>,
    onAgendaChanged: (Int, String) -> Unit,
    onAgendaAdded: () -> Unit,
    onAgendaRemoved: (Int) -> Unit,
    isReadOnly: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            LabelText(
                text = label
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 기존 아젠다 목록
                agendas.forEachIndexed { index, agenda ->
                    AgendaItem(
                        agenda = agenda,
                        onAgendaChanged = { newValue ->
                            onAgendaChanged(index, newValue)
                        },
                        onAgendaRemoved = {
                            onAgendaRemoved(index)
                        },
                        isReadOnly = isReadOnly
                    )
                }
                
                // 새로운 아젠다 추가 버튼 (읽기 전용일 때는 표시하지 않음)
                if (!isReadOnly) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(secondaryButton)
                            .clickable { onAgendaAdded() }
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.add),
                            contentDescription = "아젠다 추가",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AgendaItem(
    agenda: String,
    onAgendaChanged: (String) -> Unit,
    onAgendaRemoved: () -> Unit,
    isReadOnly: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyLarge,
            color = primaryTextColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        BasicTextField(
            value = agenda,
            onValueChange = { newValue ->
                if (!isReadOnly) {
                    onAgendaChanged(newValue)
                }
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = primaryTextColor,
                fontWeight = FontWeight.Normal
            ),
            modifier = Modifier.weight(1f),
            singleLine = true,
            enabled = !isReadOnly,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    // 포커스 제거 및 키보드 닫기
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            ),
            decorationBox = { innerTextField ->
                if (agenda.isEmpty()) {
                    Text(
                        text = "주제를 입력하세요",
                        color = gray400,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal
                    )
                }
                innerTextField()
            }
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        if (!isReadOnly) {
            Image(
                painter = painterResource(id = R.drawable.cancel2),
                contentDescription = "아젠다 삭제",
                modifier = Modifier
                    .size(9.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onAgendaRemoved() }
            )
        }
    }
}
