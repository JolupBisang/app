package com.imhungry.sillok.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.imhungry.sillok.ui.theme.dialogBackGround
import com.imhungry.sillok.ui.theme.inverse
import com.imhungry.sillok.ui.theme.primaryButton
import com.imhungry.sillok.ui.theme.tertiary

@Composable
fun SillokDialog(
    visible: Boolean,
    message: String = "앱을 종료하시겠습니까?",
    confirmText: String = "예",
    cancelText: String = "취소",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (visible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1000f)
                .background(dialogBackGround)
                .clickable(enabled = false) { },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 12.dp,
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
                        Text(
                            text = cancelText,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = tertiary,
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 4.dp)
                                .clickable { onDismiss() }
                        )

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
                                    .clickable { onConfirm() }
                            )
                        }
                    }
                }
            }
        }
    }
}
