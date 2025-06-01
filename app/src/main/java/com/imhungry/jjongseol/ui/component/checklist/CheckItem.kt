package com.imhungry.jjongseol.ui.component.checklist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.jjongseol.ui.theme.Pretend

@Composable
fun CheckItem(
    text: String,
    checked: Boolean,
    isFocused: Boolean,
    onToggle: () -> Unit,
    topPadding: Dp = 8.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding)
    ) {
        Box(
            modifier = Modifier
                .size(21.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onToggle
                )
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = null,
                modifier = Modifier
                    .size(21.dp)
                    .border(
                        width = 1.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(4.dp)
                    ),
                colors = CheckboxDefaults.colors(
                    checkedColor = Color.White,
                    uncheckedColor = Color.White,
                    checkmarkColor = Color.Black,
                    disabledCheckedColor = Color.White,
                    disabledUncheckedColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        val textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = if (isFocused) FontWeight.ExtraBold else FontWeight.Medium,
            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
            fontSize = 16.sp
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            Text(
                text = text,
                style = textStyle,
                color = Color.Black,
                fontFamily = Pretend,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}