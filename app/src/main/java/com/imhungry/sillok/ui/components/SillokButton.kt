package com.imhungry.sillok.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.imhungry.sillok.ui.theme.gray500
import com.imhungry.sillok.ui.theme.inverse
import com.imhungry.sillok.ui.theme.primaryButton
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.secondaryButton

@Composable
fun SillokButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = primaryButton,
    textColor: Color = inverse
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 6.dp)
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
            text = "취소",
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