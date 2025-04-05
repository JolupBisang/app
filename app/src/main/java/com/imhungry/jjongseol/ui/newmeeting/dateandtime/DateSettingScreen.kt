package com.imhungry.jjongseol.ui.newmeeting.dateandtime

import android.app.DatePickerDialog
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@Composable
fun DatePickerField() {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val dateText = remember { mutableStateOf("YYYY / MM / DD") }
    val textColor = if (dateText.value == "YYYY / MM / DD") Color.LightGray else Color.Black

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            dateText.value = "${selectedYear} / ${selectedMonth + 1} / $selectedDay"
        }, year, month, day
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(15.dp))
            .clickable {
                datePickerDialog.show()
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = dateText.value,
            style = TextStyle(
                color = textColor,
                fontSize = 16.sp
            ),
            modifier = Modifier
                .padding(10.dp)
                .padding(start = 5.dp)
        )
    }
}

