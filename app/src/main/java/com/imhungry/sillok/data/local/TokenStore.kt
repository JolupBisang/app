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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token_store")

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val FCM_TOKEN_KEY = stringPreferencesKey("fcm_token")
        private const val TAG = "TokenStore"
    }

    val accessToken: Flow<String?> = dataStore.data.map { preferences ->
        val value = preferences[ACCESS_TOKEN_KEY]
        value
    }

    val fcmToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[FCM_TOKEN_KEY]
    }

    suspend fun saveTokens(accessToken: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
        }
    }

    suspend fun saveFcmToken(fcmToken: String) {
        dataStore.edit { preferences ->
            preferences[FCM_TOKEN_KEY] = fcmToken
        }
    }

    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(FCM_TOKEN_KEY)
        }
    }
}

