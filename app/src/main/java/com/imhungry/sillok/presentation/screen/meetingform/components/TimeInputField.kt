package com.imhungry.sillok.presentation.screen.meetingform.components

import android.os.Build
import android.view.LayoutInflater
import android.widget.TimePicker
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.imhungry.sillok.R
import com.imhungry.sillok.ui.theme.border
import com.imhungry.sillok.ui.theme.gray400
import com.imhungry.sillok.ui.theme.inverse
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.primaryButton
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.tertiary

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
    var wasEditingStartTime by remember { mutableStateOf(false) }

    // 시작 시간과 종료 시간으로부터 목표 시간 자동 계산
    LaunchedEffect(startTime, endTime, showTimePicker) {
        if (!showTimePicker && startTime.isNotEmpty() && endTime.isNotEmpty() &&
            startTime.length >= 4 && endTime.length >= 4
        ) {
            val startMinutes = parseTimeToMinutes(startTime)
            val endMinutes = parseTimeToMinutes(endTime)
            val calculatedDuration = endMinutes - startMinutes
            if (calculatedDuration > 0) {
                onDurationChange(calculatedDuration.toString())
            } else if (calculatedDuration <= 0) {
                // 종료 시간이 시작 시간보다 이전이거나 같으면 목표 시간 초기화
                onDurationChange("")
            }
        } else if (!showTimePicker && (startTime.isEmpty() || endTime.isEmpty() ||
                    startTime.length < 4 || endTime.length < 4)
        ) {
            // 시작 시간 또는 종료 시간이 비어있으면 목표 시간 초기화
            onDurationChange("")
        }
    }

    // 시작 시간 다이얼로그가 닫혔을 때 종료 시간 다이얼로그 자동 열기
    LaunchedEffect(showTimePicker, startTime, endTime) {
        if (!showTimePicker && wasEditingStartTime) {
            // 시작 시간이 설정되었고, 종료 시간이 비어있으면 종료 시간 다이얼로그 열기
            if (startTime.isNotEmpty() && startTime.length >= 4 &&
                (endTime.isEmpty() || endTime.length < 4)
            ) {
                wasEditingStartTime = false
                editingField = TimeField.END
                onEndTimeClick()
            } else {
                wasEditingStartTime = false
                editingField = null
            }
        } else if (!showTimePicker && !wasEditingStartTime) {
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
                                        wasEditingStartTime = false
                                    } else {
                                        // 시작 시간 TimePicker 표시
                                        editingField = TimeField.START
                                        wasEditingStartTime = true
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
                                        wasEditingStartTime = false
                                    } else {
                                        // 종료 시간 TimePicker 표시
                                        editingField = TimeField.END
                                        wasEditingStartTime = false
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

                // 분 단위 표시 필드 (읽기 전용, 자동 계산)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, border, RoundedCornerShape(4.dp))
                        .background(primaryBackground)
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (duration.isNotEmpty()) {
                            duration
                        } else {
                            durationPlaceholder
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (duration.isEmpty()) gray400 else primaryTextColor,
                        fontWeight = FontWeight.Normal
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

        // 단일 TimePicker 다이얼로그 (현재 편집 중인 필드에 따라 표시, 읽기 전용일 때는 표시하지 않음)
        if (showTimePicker && editingField != null && !isReadOnly) {
            // 종료 시간 필드를 클릭했을 때 종료 시간이 비어있으면 시작 시간을 초기값으로 사용
            val initialHour = when (editingField) {
                TimeField.START -> parseTimeToHour(startTime)
                TimeField.END -> {
                    if (endTime.isEmpty() || endTime.length < 4) {
                        // 종료 시간이 비어있으면 시작 시간을 초기값으로 사용
                        parseTimeToHour(startTime)
                    } else {
                        parseTimeToHour(endTime)
                    }
                }

                null -> 0
            }
            val initialMinute = when (editingField) {
                TimeField.START -> parseTimeToMinute(startTime)
                TimeField.END -> {
                    if (endTime.isEmpty() || endTime.length < 4) {
                        // 종료 시간이 비어있으면 시작 시간을 초기값으로 사용
                        parseTimeToMinute(startTime)
                    } else {
                        parseTimeToMinute(endTime)
                    }
                }

                null -> 0
            }

            InlineTimePicker(
                title = when (editingField) {
                    TimeField.START -> "시작 시간"
                    TimeField.END -> "종료 시간"
                    null -> "시간"
                },
                hour = initialHour,
                minute = initialMinute,
                onTimeChange = { hour, minute ->
                    val timeString = String.format("%02d%02d", hour, minute)
                    when (editingField) {
                        TimeField.START -> onStartTimeChange(timeString)
                        TimeField.END -> onEndTimeChange(timeString)
                        null -> {}
                    }
                },
                onDismiss = onTimePickerDismiss
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
    title: String,
    hour: Int,
    minute: Int,
    onTimeChange: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var currentHour by remember { mutableStateOf(hour) }
    var currentMinute by remember { mutableStateOf(minute) }

    // 초기값이 변경되면 현재 선택값도 업데이트
    LaunchedEffect(hour, minute) {
        currentHour = hour
        currentMinute = minute
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = primaryBackground,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(vertical = 32.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp)
            ) {
                AndroidView(
                    factory = { context ->
                        LayoutInflater.from(context).inflate(R.layout.time_picker_layout, null)
                            .apply {
                                (this as TimePicker).apply {
                                    this.hour = currentHour
                                    this.minute = currentMinute
                                }
                            } as TimePicker
                    },
                    update = { picker ->
                        picker.hour = currentHour
                        picker.minute = currentMinute
                        picker.setOnTimeChangedListener { _, h, m ->
                            currentHour = h
                            currentMinute = m
                        }
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent
                ) {
                    Text(
                        text = "취소",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = tertiary,
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                onDismiss()
                            }
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = primaryButton
                ) {
                    Text(
                        text = "확인",
                        style = MaterialTheme.typography.bodyLarge,
                        color = inverse,
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                // 선택된 시간을 적용
                                onTimeChange(currentHour, currentMinute)
                                onDismiss()
                            }
                    )
                }
            }
        }
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
