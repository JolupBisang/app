package com.imhungry.jjongseol.ui.component.checklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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

@Composable
fun CheckItem(
    text: String,
    checked: Boolean,
    isFocused: Boolean,
    onToggle: () -> Unit,
    topPadding: Dp = 16.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
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

        Spacer(modifier = Modifier.width(12.dp))

        val textColor = if (checked) Color(0xFFB7B7B7) else if (isFocused) Color.Gray else Color(0xFFB7B7B7)
        val textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal,
            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
        )

        Text(
            text = text,
            style = textStyle,
            color = textColor,
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}