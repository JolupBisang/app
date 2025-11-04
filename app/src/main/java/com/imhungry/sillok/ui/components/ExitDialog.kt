package com.imhungry.sillok.ui.components

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable

@Composable
fun ExitDialog(
    visible: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val activity = LocalActivity.current
    SillokDialog(
        visible = visible,
        onConfirm = {
            onConfirm()
            activity?.finish()
        },
        onDismiss = onDismiss
    )
}