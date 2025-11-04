package com.imhungry.sillok.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class NotificationHistoryItem(
    val meetingId: Long,
    val meetingTitle: String,
    val timestamp: Long
)

private val Context.notificationHistoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_history_store")

@Singleton
class NotificationHistoryStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.notificationHistoryDataStore
    private val gson = Gson()

    companion object {
        private val NOTIFICATION_HISTORY_KEY = stringPreferencesKey("notification_history")
        private const val MAX_HISTORY_SIZE = 100 // 최대 100개까지 저장
    }

    val notificationHistory: Flow<List<NotificationHistoryItem>> = dataStore.data.map { preferences ->
        val historyJson = preferences[NOTIFICATION_HISTORY_KEY] ?: return@map emptyList()
        try {
            val type = object : TypeToken<List<NotificationHistoryItem>>() {}.type
            gson.fromJson<List<NotificationHistoryItem>>(historyJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addNotification(meetingId: Long, meetingTitle: String) {
        dataStore.edit { preferences ->
            val currentHistoryJson = preferences[NOTIFICATION_HISTORY_KEY]
            val currentHistory = if (currentHistoryJson != null) {
                try {
                    val type = object : TypeToken<List<NotificationHistoryItem>>() {}.type
                    gson.fromJson<List<NotificationHistoryItem>>(currentHistoryJson, type) ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }

            val newItem = NotificationHistoryItem(
                meetingId = meetingId,
                meetingTitle = meetingTitle,
                timestamp = System.currentTimeMillis()
            )

            // 최신 알림을 맨 앞에 추가하고, 최대 개수 유지
            val updatedHistory = (listOf(newItem) + currentHistory).take(MAX_HISTORY_SIZE)
            preferences[NOTIFICATION_HISTORY_KEY] = gson.toJson(updatedHistory)
        }
    }

    suspend fun clearHistory() {
        dataStore.edit { preferences ->
            preferences.remove(NOTIFICATION_HISTORY_KEY)
        }
    }
}

