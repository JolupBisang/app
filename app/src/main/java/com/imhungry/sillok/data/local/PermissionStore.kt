package com.imhungry.sillok.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.permissionDataStore: DataStore<Preferences> by preferencesDataStore(name = "permission_store")

@Singleton
class PermissionStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.permissionDataStore

    companion object {
        private val HAS_PERMISSION_KEY = stringPreferencesKey("has_permission")
        private val HAS_NOTIFICATION_PERMISSION_KEY = stringPreferencesKey("has_notification_permission")
        private val SHOULD_REQUEST_PERMISSION_KEY = stringPreferencesKey("should_request_permission")
        private val SHOULD_REQUEST_NOTIFICATION_PERMISSION_KEY = stringPreferencesKey("should_request_notification_permission")
    }

    val permissionState: Flow<PermissionStateData> = dataStore.data.map { preferences ->
        PermissionStateData(
            hasPermission = preferences[HAS_PERMISSION_KEY]?.toBooleanStrictOrNull() ?: false,
            hasNotificationPermission = preferences[HAS_NOTIFICATION_PERMISSION_KEY]?.toBooleanStrictOrNull() ?: false,
            shouldRequestPermission = preferences[SHOULD_REQUEST_PERMISSION_KEY]?.toBooleanStrictOrNull() ?: false,
            shouldRequestNotificationPermission = preferences[SHOULD_REQUEST_NOTIFICATION_PERMISSION_KEY]?.toBooleanStrictOrNull() ?: false
        )
    }

    suspend fun savePermissionState(state: PermissionStateData) {
        dataStore.edit { preferences ->
            preferences[HAS_PERMISSION_KEY] = state.hasPermission.toString()
            preferences[HAS_NOTIFICATION_PERMISSION_KEY] = state.hasNotificationPermission.toString()
            preferences[SHOULD_REQUEST_PERMISSION_KEY] = state.shouldRequestPermission.toString()
            preferences[SHOULD_REQUEST_NOTIFICATION_PERMISSION_KEY] = state.shouldRequestNotificationPermission.toString()
        }
    }

    suspend fun savePermissionFlags(hasPermission: Boolean, hasNotificationPermission: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAS_PERMISSION_KEY] = hasPermission.toString()
            preferences[HAS_NOTIFICATION_PERMISSION_KEY] = hasNotificationPermission.toString()
        }
    }
}

data class PermissionStateData(
    val hasPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val shouldRequestPermission: Boolean = false,
    val shouldRequestNotificationPermission: Boolean = false
)


