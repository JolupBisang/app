package com.imhungry.sillok.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.sillok.ui.theme.gray400
import com.imhungry.sillok.ui.theme.gray500
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.secondaryButton
import com.imhungry.sillok.ui.theme.whiteBackground

enum class TimeFilterType {
    YEAR,    // 연
    MONTH,   // 월
    ALL      // 전체
}

@Composable
fun SegmentedControl(
    selectedType: TimeFilterType,
    onTypeSelected: (TimeFilterType) -> Unit,
    modifier: Modifier = Modifier,
    selectedYear: Int? = null,
    selectedMonth: Int? = null,
    onYearClick: (() -> Unit)? = null,
    onMonthClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(gray500)
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            TimeFilterType.values().forEach { type ->
                val isSelected = selectedType == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isSelected) whiteBackground else Color.Transparent
                        )
                        .clickable { 
                            when (type) {
                                TimeFilterType.YEAR -> {
                                    if (onYearClick != null) {
                                        onYearClick()
                                    } else {
                                        onTypeSelected(type)
                                    }
                                }
                                TimeFilterType.MONTH -> {
                                    if (onMonthClick != null) {
                                        onMonthClick()
                                    } else {
                                        onTypeSelected(type)
                                    }
                                }
                                TimeFilterType.ALL -> {
                                    onTypeSelected(type)
                                }
                            }
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (type) {
                            TimeFilterType.YEAR -> {
                                if (selectedYear != null) "${selectedYear}년" else "년"
                            }
                            TimeFilterType.MONTH -> {
                                if (selectedMonth != null) "${selectedMonth}월" else "월"
                            }
                            TimeFilterType.ALL -> "전체"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp,
                        color = if (isSelected) primaryTextColor else primaryTextColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

