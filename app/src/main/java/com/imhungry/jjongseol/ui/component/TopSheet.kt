package com.imhungry.jjongseol.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.imhungry.jjongseol.R

@Composable
fun TopSheet(
    modifier: Modifier = Modifier,
    collapsedHeight: Dp = 60.dp,
    sheetBackgroundColor: Color = Color(0xFFF6F6F6),
    sheetShape: RoundedCornerShape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
    sheetElevation: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
    peekContent: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(sheetElevation, sheetShape)
            .background(sheetBackgroundColor, sheetShape)
            .animateContentSize()
            .padding(top = 28.dp, bottom = 4.dp, start = 24.dp, end = 24.dp)
    ) {
        if (expanded) {
            content()
            Spacer(modifier = Modifier.height(12.dp))
        } else {
            peekContent()
        }

        Image(
            painter = painterResource(id = if (expanded) R.drawable.up else R.drawable.down),
            contentDescription = if (expanded) "접기" else "펼치기",
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterHorizontally)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    expanded = !expanded
                }
        )
    }
}
