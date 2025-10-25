package com.imhungry.sillok.presentation.viewmodel.shared

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.data.local.PermissionStateData
import com.imhungry.sillok.data.local.PermissionStore
import com.imhungry.sillok.presentation.permission.PermissionController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val permissionStore: PermissionStore,
    private val permissionController: PermissionController
) : ViewModel() {
    private val _state = MutableStateFlow(PermissionStateData())
    val state: StateFlow<PermissionStateData> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            permissionStore.permissionState.collect { saved ->
                _state.update { saved }
            }
        }
    }

    fun checkAndRequestAllPermissions(context: Context) {
        val r = permissionController.evaluate(context)
        val newState = PermissionStateData(
            hasPermission = r.hasPermission,
            hasNotificationPermission = r.hasNotificationPermission,
            shouldRequestPermission = r.shouldRequestPermission,
            shouldRequestNotificationPermission = r.shouldRequestNotificationPermission
        )
        _state.update { newState }
        viewModelScope.launch { permissionStore.savePermissionState(newState) }
    }

    fun onPermissionResult(granted: Boolean) {
        val current = _state.value
        val r = permissionController.onAudioPermissionResult(granted, current.hasNotificationPermission)
        val newState = PermissionStateData(
            hasPermission = r.hasPermission,
            hasNotificationPermission = r.hasNotificationPermission,
            shouldRequestPermission = r.shouldRequestPermission,
            shouldRequestNotificationPermission = r.shouldRequestNotificationPermission
        )
        _state.update { newState }
        viewModelScope.launch { permissionStore.savePermissionState(newState) }
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        val current = _state.value
        val r = permissionController.onNotificationPermissionResult(granted, current.hasPermission)
        val newState = PermissionStateData(
            hasPermission = r.hasPermission,
            hasNotificationPermission = r.hasNotificationPermission,
            shouldRequestPermission = r.shouldRequestPermission,
            shouldRequestNotificationPermission = r.shouldRequestNotificationPermission
        )
        _state.update { newState }
        viewModelScope.launch { permissionStore.savePermissionState(newState) }
    }
}


