package com.imhungry.sillok.presentation.permission

import android.content.Context
import com.imhungry.sillok.presentation.util.PermissionUtils
import javax.inject.Inject
import javax.inject.Singleton

data class PermissionStateUpdate(
    val hasPermission: Boolean,
    val hasNotificationPermission: Boolean,
    val shouldRequestPermission: Boolean,
    val shouldRequestNotificationPermission: Boolean
)

@Singleton
class PermissionController @Inject constructor() {
    fun evaluate(context: Context): PermissionStateUpdate {
        val r = PermissionUtils.evaluateVoicePermissions(context)
        return PermissionStateUpdate(
            hasPermission = r.hasAudioPermission,
            hasNotificationPermission = r.hasNotificationPermission,
            shouldRequestPermission = r.shouldRequestAudio,
            shouldRequestNotificationPermission = r.shouldRequestNotification
        )
    }

    fun onAudioPermissionResult(
        granted: Boolean,
        currentHasNotificationPermission: Boolean
    ): PermissionStateUpdate {
        return PermissionStateUpdate(
            hasPermission = granted,
            hasNotificationPermission = currentHasNotificationPermission,
            shouldRequestPermission = false,
            shouldRequestNotificationPermission = granted && !currentHasNotificationPermission
        )
    }

    fun onNotificationPermissionResult(granted: Boolean, currentHasPermission: Boolean): PermissionStateUpdate {
        return PermissionStateUpdate(
            hasPermission = currentHasPermission,
            hasNotificationPermission = granted,
            shouldRequestPermission = false,
            shouldRequestNotificationPermission = false
        )
    }
}


