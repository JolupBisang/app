package com.imhungry.sillok.presentation.screen.meetingminutesfolder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.imhungry.sillok.ui.theme.blackBackGround
import com.imhungry.sillok.ui.theme.gray300
import com.imhungry.sillok.ui.theme.gray400
import com.imhungry.sillok.ui.theme.green300
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.tertiary
import com.imhungry.sillok.ui.theme.whiteBackground

@Composable
fun FolderMeetingEditItem(
    title: String,
    date: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onToggle
                )
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                enabled = false,
                modifier = Modifier
                    .size(24.dp)
                    .border(
                        width = 1.dp,
                        color = gray300,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(
                        color = whiteBackground,
                        shape = RoundedCornerShape(4.dp)
                    ),
                colors = CheckboxDefaults.colors(
                    checkedColor = whiteBackground,
                    uncheckedColor = whiteBackground,
                    checkmarkColor = green300,
                    disabledCheckedColor = whiteBackground,
                    disabledUncheckedColor = whiteBackground
                )
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = primaryTextColor,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = date,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Normal,
            color = tertiary
        )
    }
}
