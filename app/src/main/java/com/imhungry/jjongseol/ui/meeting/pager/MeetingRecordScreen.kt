package com.imhungry.jjongseol.ui.meeting.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.imhungry.jjongseol.ui.component.Notification
import com.imhungry.jjongseol.ui.component.TopSheet

@Composable
fun MeetingRecordScreen() {
    val items = listOf(
        "저메추", "지구는 평평한가?", "35세는 어린이인가?", "가르마 왼쪽 vs 오른쪽", "왼손잡이는 똑똑할까?"
    )
    val checkedStates = remember { mutableStateListOf(false, false, false, false, false) }
    val lastCheckedIndex = remember { mutableStateOf(0) }

    val firstUncheckedIndex = checkedStates.indexOfFirst { !it }
    val peekIndex = if (firstUncheckedIndex == -1) lastCheckedIndex.value else firstUncheckedIndex

    val showNotification = remember { mutableStateOf(false) }
    val message = remember { mutableStateOf("우리, 이쁜.딸. 얼굴만큼.고운.말 스자.^^ -엄마가") }
    val timestamp = remember { mutableStateOf("00:02:10") }

    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 90.dp)
        ) {
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 0.dp)
        ) {
            TopSheet(
                collapsedHeight = 60.dp,
                peekContent = {
                    CheckItem(
                        text = items[peekIndex],
                        checked = checkedStates[peekIndex],
                        isFocused = !checkedStates[peekIndex],
                        onToggle = {
                            checkedStates[peekIndex] = !checkedStates[peekIndex]
                            if (checkedStates[peekIndex]) lastCheckedIndex.value = peekIndex
                        }
                    )
                },
                content = {
                    Column {
                        items.forEachIndexed { i, item ->
                            CheckItem(
                                text = item,
                                checked = checkedStates[i],
                                isFocused = !checkedStates[i] && firstUncheckedIndex == i,
                                onToggle = {
                                    checkedStates[i] = !checkedStates[i]
                                    if (checkedStates[i]) lastCheckedIndex.value = i
                                }
                            )
                        }
                    }
                }
            )

            if (showNotification.value) {
                Spacer(modifier = Modifier.height(8.dp))
                Notification(
                    visible = true,
                    message = message.value,
                    time = timestamp.value,
                    isRead = true
                )
            }
        }
    }
}

@Composable
fun CheckItem(
    text: String,
    checked: Boolean,
    isFocused: Boolean,
    onToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Box(
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onToggle
            )
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFCBE7FD),
                    uncheckedColor = Color(0xFF2196F3),
                    checkmarkColor = Color.White,
                )
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal,
                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = when {
                checked -> Color(0xFFB7B7B7)
                isFocused -> Color.Gray
                else -> Color(0xFFB7B7B7)
            },
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}
