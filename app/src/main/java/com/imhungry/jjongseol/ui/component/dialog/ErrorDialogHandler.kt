package com.imhungry.jjongseol.ui.component.dialog

import android.content.Intent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.imhungry.jjongseol.service.MeetingSseService
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
    val context = LocalContext.current
    val isTokenExpired = errorMessage == "만료된 토큰입니다."
    val isNetworkError = errorMessage.isNetworkError()
    val isNotHostError = errorMessage == "해당 작업은 회의 리더만 수행할 수 있습니다."
    val isServerInternalError = errorMessage == "서버 내부 오류입니다. 관리자에게 문의해주세요."
    val isUserNotFound = errorMessage == "존재하지 않는 회원입니다."
    val isNotInProgressError = errorMessage == "진행중인 회의가 아닙니다."
    val isInvalidInput = errorMessage == "잘못된 입력입니다."
    val isAgendaNotFound = errorMessage == "존재하지 않는 안건입니다."
    val isEditNotAllowedInNotWaiting = errorMessage == "대기 중인 회의에서만 안건을 수정할 수 있습니다."
    val isNoParticipationData = errorMessage == "해당 회의의 참여율 데이터가 존재하지 않습니다."
    val isNotEditableMeeting = errorMessage == "종료되었거나 취소된 회의는 수정할 수 없습니다."

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
                    isUserNotFound -> {
                        exitProcess(0)
                    }
                    isTokenExpired -> {
                        loginViewModel.clearToken()
                        onFinish(SilRokNavigation.Login)
                    }
                    isNetworkError -> {
                        context.stopService(Intent(context, MeetingSseService::class.java))
                        onFinish(SilRokNavigation.Home)
                    }
                    isServerInternalError -> {
                        context.stopService(Intent(context, MeetingSseService::class.java))
                        onFinish(SilRokNavigation.Home)
                    }
                    isNotHostError -> {
                    }
                    isNotInProgressError -> {
                        context.stopService(Intent(context, MeetingSseService::class.java))
                        onFinish(SilRokNavigation.Home)
                    }
                    isInvalidInput ||
                    isAgendaNotFound ||
                    isEditNotAllowedInNotWaiting ||
                    isNoParticipationData ||
                    isNotEditableMeeting -> {
                    }
                    else -> {
                        onFinish(SilRokNavigation.Home)
                    }
                }
            }
        )
    }
}
