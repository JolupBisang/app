package com.imhungry.jjongseol.ui.component.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.jjongseol.ui.theme.Pretend

@Composable
fun CustomDialog(
    description : String? = null,
    confirmText: String = "확인",
    dismissText: String = "취소",
    showDismissButton: Boolean = true,
    onDismissRequest: () -> Unit,
    onConfirmExit: () -> Unit,
    dialogWidth: Float = 0.85f
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = Color.White,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(dialogWidth)
                    .padding(top = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = description.toString(),
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontFamily = Pretend,
                    fontSize = 15.sp
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth(dialogWidth),
                horizontalArrangement = Arrangement.Center
            ) {
                if (showDismissButton) {
                    Text(
                        text = dismissText,
                        color = Color.DarkGray,
                        fontFamily = Pretend,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDismissRequest
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = confirmText,
                    color = Color.DarkGray,
                    fontFamily = Pretend,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onConfirmExit
                        )
                )
            }
        },
        dismissButton = {}
    )
}
