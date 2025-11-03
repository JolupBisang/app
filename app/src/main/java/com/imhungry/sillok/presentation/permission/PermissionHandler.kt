package com.imhungry.sillok.presentation.permission

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun PermissionHandler(
    onPermissionsGranted: () -> Unit = {},
    onPermissionsDenied: (List<String>) -> Unit = {}
) {
    val permissions = buildList {
        // 음성 녹음 권한
        add(Manifest.permission.RECORD_AUDIO)
        
        // 알림 권한 (Android 13 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val deniedPermissions = permissionsMap.filterValues { !it }.keys.toList()
        
        if (deniedPermissions.isEmpty()) {
            onPermissionsGranted()
        } else {
            onPermissionsDenied(deniedPermissions)
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(permissions.toTypedArray())
    }
}

