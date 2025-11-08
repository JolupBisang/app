package com.imhungry.sillok.presentation.screen.meeting.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.imhungry.sillok.R
import com.imhungry.sillok.ui.theme.blackBackGround
import com.imhungry.sillok.ui.theme.gray400
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.shadow
import com.imhungry.sillok.ui.theme.tertiary
import com.imhungry.sillok.ui.theme.whiteBackground

@Composable
fun TopSheet(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    peekContent: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(whiteBackground)
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (expanded) {
                Box(modifier = Modifier.weight(1f)) {
                    Column { content() }
                }
            } else {
                Box(modifier = Modifier.weight(1f)) {
                    peekContent()
                }
            }

            Image(
                painter = painterResource(id = if (expanded) R.drawable.collapse else R.drawable.expand),
                contentDescription = if (expanded) "접기" else "펼치기",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onExpandedChange(!expanded)
                    }
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        shadow,
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
fun CheckItem(
    text: String,
    checked: Boolean,
    isFocused: Boolean,
    onToggle: () -> Unit,
    bottomPadding: Dp = 8.dp
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = bottomPadding)
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
                        color = if (checked) gray400 else blackBackGround,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(
                        color = whiteBackground,
                        shape = RoundedCornerShape(4.dp)
                    ),
                colors = CheckboxDefaults.colors(
                    checkedColor = whiteBackground,
                    uncheckedColor = whiteBackground,
                    checkmarkColor = if (checked) gray400 else blackBackGround,
                    disabledCheckedColor = whiteBackground,
                    disabledUncheckedColor = whiteBackground
                )
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        val textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = if (isFocused) FontWeight.ExtraBold else FontWeight.Medium,
            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
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
                color = if (checked) tertiary else primaryTextColor,
            )
        }
    }
}