package com.imhungry.jjongseol.ui.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.jjongseol.ui.theme.Pretend
import com.imhungry.jjongseol.ui.theme.inverseText
import com.imhungry.jjongseol.ui.theme.primaryButton
import com.imhungry.jjongseol.ui.theme.tertiary
import com.imhungry.jjongseol.ui.theme.whiteColor

@Composable
fun CustomDialog(
    description : String? = null,
    confirmText: String = "예",
    dismissText: String = "취소",
    showDismissButton: Boolean = true,
    onDismissRequest: () -> Unit,
    onConfirmExit: () -> Unit,
    dialogWidth: Float = 0.85f
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = whiteColor,
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
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth(dialogWidth),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showDismissButton) {
                    Text(
                        text = dismissText,
                        color = tertiary,
                        fontFamily = Pretend,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDismissRequest
                            )
                    )
                    Spacer(modifier = Modifier.width(56.dp))
                }
                Box(
                    modifier = Modifier
                        .background(
                            color = primaryButton,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(onClick = onConfirmExit)
                        .padding(horizontal = 20.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = confirmText,
                        fontFamily = Pretend,
                        fontWeight = FontWeight.SemiBold,
                        color = inverseText,
                        fontSize = 15.sp
                    )
                }
            }
        },
        dismissButton = {}
    )
}
