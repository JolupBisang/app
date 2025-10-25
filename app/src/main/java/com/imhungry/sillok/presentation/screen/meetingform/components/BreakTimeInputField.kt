package com.imhungry.sillok.presentation.screen.meetingform.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.imhungry.sillok.ui.theme.border
import com.imhungry.sillok.ui.theme.gray400
import com.imhungry.sillok.ui.theme.primaryTextColor

@Composable
fun BreakTimeInputField(
    label: String,
    breakInterval: String,
    breakDuration: String,
    onBreakIntervalChanged: (String) -> Unit,
    onBreakDurationChanged: (String) -> Unit,
    isReadOnly: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabelText(
                text = label
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 쉬는 시간 간격 입력 필드
                BasicTextField(
                    value = breakInterval,
                    onValueChange = { newValue ->
                        if (!isReadOnly) {
                            onBreakIntervalChanged(newValue)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = if (breakInterval.isEmpty()) gray400 else primaryTextColor,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier
                        .width(60.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, border, RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    singleLine = true,
                    enabled = !isReadOnly,
                    decorationBox = { innerTextField ->
                        if (breakInterval.isEmpty()) {
                            Text(
                                text = "MM",
                                color = gray400,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        innerTextField()
                    }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // "분 마다" 텍스트
                Text(
                    text = "분 마다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = primaryTextColor,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 쉬는 시간 지속 시간 입력 필드
                BasicTextField(
                    value = breakDuration,
                    onValueChange = { newValue ->
                        if (!isReadOnly) {
                            onBreakDurationChanged(newValue)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = if (breakDuration.isEmpty()) gray400 else primaryTextColor,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier
                        .width(60.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, border, RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    singleLine = true,
                    enabled = !isReadOnly,
                    decorationBox = { innerTextField ->
                        if (breakDuration.isEmpty()) {
                            Text(
                                text = "MM",
                                color = gray400,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        innerTextField()
                    }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // "분" 텍스트
                Text(
                    text = "분",
                    style = MaterialTheme.typography.bodyMedium,
                    color = primaryTextColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
