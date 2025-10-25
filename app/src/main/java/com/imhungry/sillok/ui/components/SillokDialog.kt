package com.imhungry.sillok.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.imhungry.sillok.ui.theme.inverse
import com.imhungry.sillok.ui.theme.primaryButton
import com.imhungry.sillok.ui.theme.tertiary

@Composable
fun SillokDialog(
    message: String,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 36.dp)
                    .width(230.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(28.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent
                    ) {
                        Text(
                            text = cancelText,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = tertiary,
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 4.dp)
                                .align(Alignment.CenterVertically)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {
                                    onDismiss()
                                }
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(80.dp))
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = primaryButton
                    ) {
                        Text(
                            text = confirmText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = inverse,
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 4.dp)
                                .align(Alignment.CenterVertically)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {
                                    onConfirm()
                                }
                        )
                    }
                }
            }
        }
    }
} 