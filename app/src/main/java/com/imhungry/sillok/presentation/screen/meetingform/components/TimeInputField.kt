package com.imhungry.sillok.presentation.screen.meetingform.components

import android.os.Build
import android.view.LayoutInflater
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.imhungry.sillok.R
import com.imhungry.sillok.ui.theme.border
import com.imhungry.sillok.ui.theme.gray400
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.primaryTextColor

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimeInputField(
    label: String,
    startTime: String,
    endTime: String,
    duration: String,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    startTimePlaceholder: String = "HH : MM",
    endTimePlaceholder: String = "HH : MM",
    durationPlaceholder: String = "",
    showTimePicker: Boolean = false,
    onTimePickerDismiss: () -> Unit = {},
    onStartTimeClick: () -> Unit = {},
    onEndTimeClick: () -> Unit = {},
    isReadOnly: Boolean = false
) {
    var editingField by remember { mutableStateOf<TimeField?>(null) }
    var isDurationFocused by remember { mutableStateOf(false) }

    fun calculateMissingValue() {
        when {
            // 시작 시간과 종료 시간이 모두 설정된 경우 -> 분 단위 자동 계산
            startTime.isNotEmpty() && endTime.isNotEmpty() && startTime.length >= 4 && endTime.length >= 4 -> {
                val startMinutes = parseTimeToMinutes(startTime)
                val endMinutes = parseTimeToMinutes(endTime)
                val calculatedDuration = endMinutes - startMinutes
                if (calculatedDuration > 0) {
                    onDurationChange(calculatedDuration.toString())
                }
            }
            // 시작 시간과 분 단위가 설정된 경우 -> 종료 시간 자동 계산
            startTime.isNotEmpty() && duration.isNotEmpty() && startTime.length >= 4 -> {
                val startMinutes = parseTimeToMinutes(startTime)
                val durationMinutes = duration.toIntOrNull() ?: 0
                val endMinutes = startMinutes + durationMinutes
                if (endMinutes <= 24 * 60) { // 24시간을 넘지 않는 경우만
                    val endHour = endMinutes / 60
                    val endMinute = endMinutes % 60
                    val endTimeString = String.format("%02d%02d", endHour, endMinute)
                    onEndTimeChange(endTimeString)
                }
            }
            // 종료 시간과 분 단위가 설정된 경우 -> 시작 시간 자동 계산
            endTime.isNotEmpty() && duration.isNotEmpty() && endTime.length >= 4 -> {
                val endMinutes = parseTimeToMinutes(endTime)
                val durationMinutes = duration.toIntOrNull() ?: 0
                val startMinutes = endMinutes - durationMinutes
                if (startMinutes >= 0) {
                    val startHour = startMinutes / 60
                    val startMinute = startMinutes % 60
                    val startTimeString = String.format("%02d%02d", startHour, startMinute)
                    onStartTimeChange(startTimeString)
                }
            }
        }
    }

    // TimePicker가 사라진 상태이고 분 단위 필드에 포커스가 없을 때만 자동 계산 실행
    LaunchedEffect(startTime, endTime, duration, showTimePicker, isDurationFocused) {
        if (!showTimePicker && !isDurationFocused) {
            calculateMissingValue()
        }
    }

    // TimePicker가 숨겨질 때 editingField 초기화
    LaunchedEffect(showTimePicker) {
        if (!showTimePicker) {
            editingField = null
        }
    }

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
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 시작 시간 입력 필드 (클릭만 가능)
                Box(
                    modifier = Modifier
                        .width(79.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, border, RoundedCornerShape(4.dp))
                        .background(primaryBackground)
                        .then(
                            if (!isReadOnly) {
                                Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { 
                                    if (showTimePicker && editingField == TimeField.START) {
                                        // 이미 시작 시간 TimePicker가 표시되어 있으면 숨김
                                        onTimePickerDismiss()
                                        editingField = null
                                    } else {
                                        // 시작 시간 TimePicker 표시
                                        editingField = TimeField.START
                                        onStartTimeClick()
                                    }
                                }
                            } else {
                                Modifier
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (startTime.isNotEmpty()) {
                            formatDisplayTime(startTime)
                        } else {
                            startTimePlaceholder
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (startTime.isEmpty()) gray400 else primaryTextColor,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }

                // 구분선
                Text(
                    text = " ~ ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                // 종료 시간 입력 필드 (클릭만 가능)
                Box(
                    modifier = Modifier
                        .width(79.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, border, RoundedCornerShape(4.dp))
                        .background(primaryBackground)
                        .then(
                            if (!isReadOnly) {
                                Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { 
                                    if (showTimePicker && editingField == TimeField.END) {
                                        // 이미 종료 시간 TimePicker가 표시되어 있으면 숨김
                                        onTimePickerDismiss()
                                        editingField = null
                                    } else {
                                        // 종료 시간 TimePicker 표시
                                        editingField = TimeField.END
                                        onEndTimeClick()
                                    }
                                }
                            } else {
                                Modifier
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (endTime.isNotEmpty()) {
                            formatDisplayTime(endTime)
                        } else {
                            endTimePlaceholder
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (endTime.isEmpty()) gray400 else primaryTextColor,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }

                Spacer(Modifier.width(8.dp))

                // 분 단위 입력 필드 (직접 입력 가능)
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    BasicTextField(
                        value = duration,
                        onValueChange = { newValue ->
                            if (!isReadOnly) {
                                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                    onDurationChange(newValue)
                                }
                            }
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = if (duration.isEmpty()) gray400 else primaryTextColor,
                            fontWeight = FontWeight.Normal
                        ),
                        enabled = !isReadOnly,
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(1.dp, border, RoundedCornerShape(4.dp))
                                    .background(primaryBackground)
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (duration.isEmpty()) {
                                    Text(
                                        text = durationPlaceholder,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = gray400,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                                innerTextField()
                            }
                        },
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier.onFocusChanged { focusState ->
                            val wasFocused = isDurationFocused
                            isDurationFocused = focusState.isFocused
                            
                            // 포커스가 제거되었을 때 분 단위 기준으로 종료 시간 계산
                            if (wasFocused && !focusState.isFocused && duration.isNotEmpty() && startTime.isNotEmpty() && startTime.length >= 4) {
                                val startMinutes = parseTimeToMinutes(startTime)
                                val durationMinutes = duration.toIntOrNull() ?: 0
                                val endMinutes = startMinutes + durationMinutes
                                if (endMinutes <= 24 * 60) { // 24시간을 넘지 않는 경우만
                                    val endHour = endMinutes / 60
                                    val endMinute = endMinutes % 60
                                    val endTimeString = String.format("%02d%02d", endHour, endMinute)
                                    onEndTimeChange(endTimeString)
                                }
                            }
                        }
                    )
                }

                // "분" 라벨
                Text(
                    text = "분",
                    style = MaterialTheme.typography.bodyMedium,
                    color = primaryTextColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        // 단일 TimePicker (현재 편집 중인 필드에 따라 표시, 읽기 전용일 때는 표시하지 않음)
        if (showTimePicker && editingField != null && !isReadOnly) {
            Spacer(modifier = Modifier.height(16.dp))
            InlineTimePicker(
                hour = when (editingField) {
                    TimeField.START -> parseTimeToHour(startTime)
                    TimeField.END -> parseTimeToHour(endTime)
                    null -> 0
                },
                minute = when (editingField) {
                    TimeField.START -> parseTimeToMinute(startTime)
                    TimeField.END -> parseTimeToMinute(endTime)
                    null -> 0
                },
                onTimeChange = { hour, minute ->
                    val timeString = String.format("%02d%02d", hour, minute)
                    when (editingField) {
                        TimeField.START -> onStartTimeChange(timeString)
                        TimeField.END -> onEndTimeChange(timeString)
                        null -> {}
                    }
                }
            )
        }
    }
}

// 편집 중인 시간 필드를 구분하는 enum
private enum class TimeField {
    START, END
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InlineTimePicker(
    hour: Int,
    minute: Int,
    onTimeChange: (Int, Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 40.dp)
    ) {
        AndroidView(
            factory = { context ->
                LayoutInflater.from(context).inflate(R.layout.time_picker_layout, null).apply {
                    (this as TimePicker).apply {
                        this.hour = hour
                        this.minute = minute
                    }
                } as TimePicker
            },
            update = { picker ->
                picker.hour = hour
                picker.minute = minute
                picker.setOnTimeChangedListener { _, h, m ->
                    onTimeChange(h, m)
                }
            }
        )
    }
}

private fun formatDisplayTime(timeString: String): String {
    return when {
        timeString.length >= 4 -> {
            val hour = timeString.substring(0, 2)
            val minute = timeString.substring(2, 4)
            "$hour : $minute"
        }
        timeString.length >= 2 -> {
            val hour = timeString.substring(0, 2)
            "$hour : "
        }
        else -> timeString
    }
}

private fun parseTimeToHour(timeString: String): Int {
    return when {
        timeString.length >= 2 -> timeString.substring(0, 2).toIntOrNull() ?: 0
        timeString.length == 1 -> timeString.toIntOrNull() ?: 0
        else -> 0
    }.coerceIn(0, 23)
}

private fun parseTimeToMinute(timeString: String): Int {
    return when {
        timeString.length >= 4 -> timeString.substring(2, 4).toIntOrNull() ?: 0
        timeString.length == 3 -> timeString.substring(2, 3).toIntOrNull() ?: 0
        else -> 0
    }.coerceIn(0, 59)
}

private fun parseTimeToMinutes(timeString: String): Int {
    val hour = parseTimeToHour(timeString)
    val minute = parseTimeToMinute(timeString)
    return hour * 60 + minute
}
