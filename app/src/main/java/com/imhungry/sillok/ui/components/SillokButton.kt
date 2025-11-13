package com.imhungry.sillok.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.sillok.ui.theme.gray500
import com.imhungry.sillok.ui.theme.inverse
import com.imhungry.sillok.ui.theme.primaryButton
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.secondaryButton
import com.imhungry.sillok.ui.theme.whiteBackground

@Composable
fun SillokButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = primaryButton,
    textColor: Color = inverse,
    borderColor: Color? = null,
    borderWith: Dp = 1.dp,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        border = borderColor?.let { BorderStroke(borderWith, it) },
        shape = MaterialTheme.shapes.small,
        enabled = enabled
    ) {
        Text(
            text,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}

@Composable
fun SmallSillokButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = primaryButton,
    textColor: Color = whiteBackground,
    borderColor: Color? = null,
    borderWith: Dp = 1.dp,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 32.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        border = borderColor?.let { BorderStroke(borderWith, it) },
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = 36.dp, vertical = 4.dp),
        enabled = enabled
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MediumSillokButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = primaryButton,
    textColor: Color = whiteBackground,
    borderColor: Color? = null,
    borderWith: Dp = 1.dp,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 36.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        border = borderColor?.let { BorderStroke(borderWith, it) },
        shape = MaterialTheme.shapes.extraSmall,
        enabled = enabled
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SillokButtonRow(
    onBack: () -> Unit,
    onEnter: () -> Unit,
    onModify: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SillokButton(
            text = "숨기기",
            onClick = onBack,
            modifier = Modifier.weight(1f),
            backgroundColor = gray500,
            textColor = primaryTextColor
        )

        Spacer(modifier = Modifier.width(12.dp))

        SillokButton(
            text = "입장",
            onClick = onEnter,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(12.dp))

        SillokButton(
            text = "수정",
            onClick = onModify,
            modifier = Modifier.weight(1f),
            backgroundColor = secondaryButton,
            textColor = primaryTextColor
        )
    }
}