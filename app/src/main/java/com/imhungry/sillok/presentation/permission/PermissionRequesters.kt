package com.imhungry.sillok.presentation.permission

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.sillok.presentation.viewmodel.shared.PermissionViewModel

@Composable
fun HandleVoicePermissions(
    permissionViewModel: PermissionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val permState by permissionViewModel.state.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionViewModel.onPermissionResult(isGranted)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionViewModel.onNotificationPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        permissionViewModel.checkAndRequestAllPermissions(context)
    }

    LaunchedEffect(permState.shouldRequestPermission) {
        if (permState.shouldRequestPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(permState.shouldRequestNotificationPermission) {
        if (permState.shouldRequestNotificationPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}


