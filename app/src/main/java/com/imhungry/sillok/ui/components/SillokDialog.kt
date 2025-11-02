package com.imhungry.sillok.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.imhungry.sillok.ui.theme.beige
import com.imhungry.sillok.ui.theme.dialogBackGround
import com.imhungry.sillok.ui.theme.inverse
import com.imhungry.sillok.ui.theme.primaryButton
import com.imhungry.sillok.ui.theme.tertiary

@Composable
fun SillokDialogHost(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    message: String,
    confirmText: String,
    cancelText: String,
    screenContent: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. background content
        screenContent()

        if (showDialog) {
            // 2. blur layer
            Box(
                Modifier
                    .matchParentSize()
                    .then(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Modifier.graphicsLayer {
                                renderEffect = RenderEffect
                                    .createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
                                    .asComposeRenderEffect()
                            }
                        } else Modifier.blur(16.dp)
                    )
                    .background(dialogBackGround)
            )

            // 3. dialog overlay
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                SillokDialog(
                    message = message,
                    confirmText = confirmText,
                    cancelText = cancelText,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
fun SillokDialog(
    message: String,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 8.dp,
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
