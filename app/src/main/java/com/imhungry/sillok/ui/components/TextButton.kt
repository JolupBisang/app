package com.imhungry.sillok.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.imhungry.sillok.ui.theme.Pretend
import com.imhungry.sillok.ui.theme.primaryTextColor

@Composable
fun SillokTextButton(
    text: String,
    onClick: (() -> Unit),
    modifier: Modifier = Modifier,
    textColor: Color = primaryTextColor,
    pressedColor: Color? = null,
    fontWeight: FontWeight = FontWeight.Medium,
    fontSize: Int = 15
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val targetColor = if (pressedColor != null) {
        pressedColor
    } else {
        textColor.copy(alpha = 0.7f)
    }

    val animatedColor by animateColorAsState(
        targetValue = if (isPressed) targetColor else textColor,
        animationSpec = tween(durationMillis = 150),
        label = "textColor"
    )

    Text(
        text = text,
        color = animatedColor,
        fontFamily = Pretend,
        fontWeight = fontWeight,
        fontSize = fontSize.sp,
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = null
        ) { onClick() }
    )
}