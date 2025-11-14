package com.imhungry.sillok.presentation.screen.folder.components

import android.os.Build
import android.widget.NumberPicker
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.imhungry.sillok.ui.components.MediumSillokButton
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun YearPickerDialog(
    visible: Boolean,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    var currentSelectedYear by remember { mutableStateOf(selectedYear) }
    val currentYear = LocalDate.now().year
    val minYear = currentYear - 10
    val maxYear = currentYear + 10
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // BottomSheet가 열릴 때만 초기값으로 설정
    LaunchedEffect(visible) {
        if (visible) {
            currentSelectedYear = selectedYear
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // 년도 선택
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AndroidView(
                        factory = { ctx ->
                            NumberPicker(ctx).apply {
                                minValue = minYear
                                maxValue = maxYear
                                value = currentSelectedYear
                                setOnValueChangedListener { _, _, newVal ->
                                    currentSelectedYear = newVal
                                }
                            }
                        },
                        update = { picker ->
                            picker.value = currentSelectedYear
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 확인 버튼
            MediumSillokButton(
                text = "확인",
                onClick = {
                    onYearSelected(currentSelectedYear)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthPickerDialog(
    visible: Boolean,
    selectedMonth: Int,
    onMonthSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    var currentSelectedMonth by remember { mutableStateOf(selectedMonth) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // BottomSheet가 열릴 때만 초기값으로 설정
    LaunchedEffect(visible) {
        if (visible) {
            currentSelectedMonth = selectedMonth
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // 월 선택
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AndroidView(
                        factory = { ctx ->
                            NumberPicker(ctx).apply {
                                minValue = 1
                                maxValue = 12
                                value = currentSelectedMonth
                                setOnValueChangedListener { _, _, newVal ->
                                    currentSelectedMonth = newVal
                                }
                            }
                        },
                        update = { picker ->
                            picker.value = currentSelectedMonth
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 확인 버튼
            MediumSillokButton(
                text = "확인",
                onClick = {
                    onMonthSelected(currentSelectedMonth)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

