package com.imhungry.sillok.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.sillok.ui.theme.highlight2

@Composable
fun HighlightText(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .drawBehind {
                val underlineHeight = 7.dp.toPx()
                drawRect(
                    color =  highlight2,
                    topLeft = Offset(0f, size.height - underlineHeight),
                    size = Size(size.width, underlineHeight)
                )
            }
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}