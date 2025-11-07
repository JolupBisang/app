package com.imhungry.sillok.presentation.screen.meetingform.components

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.imhungry.sillok.R
import com.imhungry.sillok.ui.components.Calendar
import com.imhungry.sillok.ui.theme.border
import com.imhungry.sillok.ui.theme.gray400
import com.imhungry.sillok.ui.theme.inverse
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.primaryButton
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.tertiary
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isReadOnly: Boolean = false
) {
    var isCalendarView by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

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

            Box(
                modifier = Modifier.width(171.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, border, RoundedCornerShape(4.dp))
                        .background(primaryBackground)
                        .then(
                            if (!isReadOnly) {
                                Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    isCalendarView = true
                                }
                            } else {
                                Modifier
                            }
                        )
                        .padding(start = 12.dp, end = 12.dp, top = 9.dp, bottom = 9.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (value.isNotEmpty()) {
                                formatDisplayDate(value)
                            } else {
                                placeholder
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (value.isEmpty()) gray400 else primaryTextColor,
                            fontWeight = FontWeight.Normal
                        )

                        Image(
                            painter = painterResource(id = R.drawable.calendar),
                            contentDescription = "달력",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        if (isCalendarView && !isReadOnly) {
            Dialog(
                onDismissRequest = { isCalendarView = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = primaryBackground,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 32.dp, horizontal = 20.dp)
                ) {
                    // 달력 컴포넌트
                    Calendar(
                        selectedDate = selectedDate,
                        onDateSelected = { date -> selectedDate = date },
                        onDateCleared = { selectedDate = null }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
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
                                        isCalendarView = false
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
                                        // 선택된 날짜를 YYYYMMDD 형식으로 변환하여 입력 필드에 설정
                                        val dateString = String.format("%04d%02d%02d", selectedDate.year, selectedDate.monthValue, selectedDate.dayOfMonth)
                                        onValueChange(dateString)
                                        isCalendarView = false
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

// 날짜를 표시용 형식으로 변환하는 함수
private fun formatDisplayDate(dateString: String): String {
    return when {
        dateString.length >= 8 -> {
            val year = dateString.substring(0, 4)
            val month = dateString.substring(4, 6)
            val day = dateString.substring(6, 8)
            "$year / $month / $day"
        }
        dateString.length >= 6 -> {
            val year = dateString.substring(0, 4)
            val month = dateString.substring(4, 6)
            "$year / $month"
        }
        dateString.length >= 4 -> {
            val year = dateString.substring(0, 4)
            year
        }
        else -> dateString
    }
}