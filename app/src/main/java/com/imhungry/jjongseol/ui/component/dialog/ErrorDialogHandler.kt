package com.imhungry.jjongseol.ui.component.dialog

import androidx.compose.runtime.*
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.viewmodel.LoginViewModel
import kotlin.system.exitProcess

private val networkErrorPatterns = listOf(
    "failed to connect",
    "unable to resolve host",
    "timeout"
)

fun String?.isNetworkError(): Boolean {
    if (this == null) return false
    return networkErrorPatterns.any { pattern ->
        this.contains(pattern, ignoreCase = true)
    }
}

@Composable
fun ErrorDialogHandler(
    errorMessage: String?,
    showDialog: Boolean,
    onFinish: (SilRokNavigation) -> Unit,
    clearError: () -> Unit,
    loginViewModel: LoginViewModel
) {
    val isTokenExpired = errorMessage == "만료된 토큰입니다."
    val isNetworkError = errorMessage.isNetworkError()
    val isNotHostError = errorMessage == "해당 작업은 회의 리더만 수행할 수 있습니다."
    val isServerInternalError = errorMessage == "서버 내부 오류입니다. 관리자에게 문의해주세요."

    if (showDialog && errorMessage != null) {
        CustomDialog(
            description = when {
                isTokenExpired -> "로그인 정보가 만료되었어요.\n다시 로그인해주세요."
                isNetworkError -> "네트워크 연결에 문제가 있습니다.\n인터넷 상태를 확인해주세요."
                else -> errorMessage
            },
            confirmText = when {
                isTokenExpired -> "로그인 하기"
                else -> "확인"
            },
            showDismissButton = false,
            onDismissRequest = {},
            onConfirmExit = {
                clearError()
                when {
                    isTokenExpired -> {
                        loginViewModel.clearToken()
                        onFinish(SilRokNavigation.Login)
                    }
                    isNetworkError -> {
                        exitProcess(0)
                    }
                    isServerInternalError -> {
                        exitProcess(0)
                    }
                    isNotHostError -> {
                    }
                    else -> {
                        onFinish(SilRokNavigation.Home)
                    }
                }
            }
        )
    }
}
