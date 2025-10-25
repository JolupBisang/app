package com.imhungry.sillok.presentation.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object PermissionUtils {
    data class VoicePermissions(
        val hasAudioPermission: Boolean,
        val hasNotificationPermission: Boolean,
        val shouldRequestAudio: Boolean,
        val shouldRequestNotification: Boolean
    )

    fun evaluateVoicePermissions(context: Context): VoicePermissions {
        val hasAudioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return VoicePermissions(
            hasAudioPermission = hasAudioPermission,
            hasNotificationPermission = hasNotificationPermission,
            shouldRequestAudio = !hasAudioPermission,
            shouldRequestNotification = !hasNotificationPermission
        )
    }
}


