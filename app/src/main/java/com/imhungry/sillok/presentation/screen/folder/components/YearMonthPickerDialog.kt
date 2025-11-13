package com.imhungry.sillok.presentation.screen.folder.components

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.imhungry.sillok.ui.components.MediumSillokButton
import com.imhungry.sillok.ui.theme.dialogBackGround
import com.imhungry.sillok.ui.theme.green300
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.primaryButton
import com.imhungry.sillok.ui.theme.primaryTextColor
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun YearPickerDialog(
    visible: Boolean,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(enabled = visible) {
        onDismiss()
    }

    if (visible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1001f)
                .background(dialogBackGround)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
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
                        .height(400.dp)
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                        .clickable(enabled = false) { }
                ) {
                    Text(
                        text = "연도 선택",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = primaryTextColor
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        val currentYear = LocalDate.now().year
                        val years = (currentYear - 4..currentYear + 4).toList().reversed()
                        
                        items(years) { year ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onYearSelected(year) }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${year}년",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (year == selectedYear) green300 else primaryTextColor,
                                    fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    MediumSillokButton(
                        text = "확인",
                        onClick = onDismiss,
                        backgroundColor = primaryBackground,
                        borderColor = primaryButton,
                        textColor = primaryButton
                    )
                }
            }
        }
    }
}

@Composable
fun MonthPickerDialog(
    visible: Boolean,
    selectedMonth: Int,
    onMonthSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(enabled = visible) {
        onDismiss()
    }

    if (visible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1001f)
                .background(dialogBackGround)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
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
                        .height(400.dp)
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                        .clickable(enabled = false) { }
                ) {
                    Text(
                        text = "월 선택",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = primaryTextColor
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        val months = (1..12).toList()
                        
                        items(months) { month ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onMonthSelected(month) }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${month}월",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (month == selectedMonth) green300 else primaryTextColor,
                                    fontWeight = if (month == selectedMonth) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    MediumSillokButton(
                        text = "확인",
                        onClick = onDismiss,
                        backgroundColor = primaryBackground,
                        borderColor = primaryButton,
                        textColor = primaryButton
                    )
                }
            }
        }
    }
}

