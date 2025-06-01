package com.imhungry.jjongseol.ui.component.layout

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.imhungry.jjongseol.R

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
            .padding(
                WindowInsets.statusBars.asPaddingValues()
            )
            .padding(top = 4.dp, bottom = 12.dp, start = 20.dp, end = 20.dp)
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
                painter = painterResource(id = if (expanded) R.drawable.collapse else R.drawable.expend),
                contentDescription = if (expanded) "접기" else "펼치기",
                modifier = Modifier
                    .padding(top = 8.dp)
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
}

