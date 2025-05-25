package com.imhungry.jjongseol.ui.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.viewmodel.LoginViewModel

@Composable
fun ErrorDialogHandler(
    errorMessage: String?,
    showDialog: MutableState<Boolean>,
    onFinish: (SilRokNavigation) -> Unit,
    clearError: () -> Unit,
    loginViewModel: LoginViewModel
) {
    if (errorMessage != null) showDialog.value = true
    val isTokenExpired = errorMessage == "TOKEN_EXPIRED"

    if (showDialog.value && errorMessage != null) {
        CustomDialog(
            description = if (isTokenExpired)
                "로그인 정보가 만료되었어요.\n다시 로그인해주세요."
            else errorMessage,
            confirmText = if (isTokenExpired) "로그인 하기" else "확인",
            showDismissButton = false,
            onDismissRequest = {},
            onConfirmExit = {
                showDialog.value = false
                clearError()
                if (isTokenExpired) {
                    loginViewModel.clearToken()
                }
                val destination = if (isTokenExpired) SilRokNavigation.Login else SilRokNavigation.Home
                onFinish(destination)
            }
        )
    }
}
