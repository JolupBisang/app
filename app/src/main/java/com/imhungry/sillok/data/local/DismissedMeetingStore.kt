package com.imhungry.sillok.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dismissedMeetingDataStore: DataStore<Preferences> by preferencesDataStore(name = "dismissed_meeting_store")

@Singleton
class DismissedMeetingStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dismissedMeetingDataStore
    private val gson = Gson()

    companion object {
        private val DISMISSED_ONGOING_MEETINGS_KEY = stringPreferencesKey("dismissed_ongoing_meetings")
        private val DISMISSED_SCHEDULED_MEETINGS_KEY = stringPreferencesKey("dismissed_scheduled_meetings")
        private val DISMISSED_MEETINGS_KEY = stringPreferencesKey("dismissed_meetings")
    }

    val dismissedOngoingMeetingIds: Flow<Set<Long>> = dataStore.data.map { preferences ->
        val idsJson = preferences[DISMISSED_ONGOING_MEETINGS_KEY] ?: return@map emptySet()
        try {
            val type = object : TypeToken<Set<Long>>() {}.type
            gson.fromJson<Set<Long>>(idsJson, type) ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    val dismissedScheduledMeetingIds: Flow<Set<Long>> = dataStore.data.map { preferences ->
        val idsJson = preferences[DISMISSED_SCHEDULED_MEETINGS_KEY] ?: return@map emptySet()
        try {
            val type = object : TypeToken<Set<Long>>() {}.type
            gson.fromJson<Set<Long>>(idsJson, type) ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    suspend fun addDismissedOngoingMeeting(meetingId: Long) {
        dataStore.edit { preferences ->
            val currentIdsJson = preferences[DISMISSED_ONGOING_MEETINGS_KEY]
            val currentIds = if (currentIdsJson != null) {
                try {
                    val type = object : TypeToken<Set<Long>>() {}.type
                    gson.fromJson<Set<Long>>(currentIdsJson, type) ?: emptySet()
                } catch (e: Exception) {
                    emptySet()
                }
            } else {
                emptySet()
            }

            val updatedIds = currentIds + meetingId
            preferences[DISMISSED_ONGOING_MEETINGS_KEY] = gson.toJson(updatedIds)
        }
    }

    suspend fun addDismissedScheduledMeeting(meetingId: Long) {
        dataStore.edit { preferences ->
            val currentIdsJson = preferences[DISMISSED_SCHEDULED_MEETINGS_KEY]
            val currentIds = if (currentIdsJson != null) {
                try {
                    val type = object : TypeToken<Set<Long>>() {}.type
                    gson.fromJson<Set<Long>>(currentIdsJson, type) ?: emptySet()
                } catch (e: Exception) {
                    emptySet()
                }
            } else {
                emptySet()
            }

            val updatedIds = currentIds + meetingId
            preferences[DISMISSED_SCHEDULED_MEETINGS_KEY] = gson.toJson(updatedIds)
        }
    }

    suspend fun getDismissedOngoingMeetingIds(): Set<Long> {
        return dismissedOngoingMeetingIds.first()
    }

    suspend fun getDismissedScheduledMeetingIds(): Set<Long> {
        return dismissedScheduledMeetingIds.first()
    }

    suspend fun addDismissedMeeting(meetingId: Long) {
        dataStore.edit { preferences ->
            val currentIdsJson = preferences[DISMISSED_MEETINGS_KEY]
            val currentIds = if (currentIdsJson != null) {
                try {
                    val type = object : TypeToken<Set<Long>>() {}.type
                    gson.fromJson<Set<Long>>(currentIdsJson, type) ?: emptySet()
                } catch (e: Exception) {
                    emptySet()
                }
            } else {
                emptySet()
            }

            val updatedIds = currentIds + meetingId
            preferences[DISMISSED_MEETINGS_KEY] = gson.toJson(updatedIds)
        }
    }

    suspend fun getDismissedMeetingIds(): Set<Long> {
        val idsJson = dataStore.data.first()[DISMISSED_MEETINGS_KEY]
        return if (idsJson != null) {
            try {
                val type = object : TypeToken<Set<Long>>() {}.type
                gson.fromJson<Set<Long>>(idsJson, type) ?: emptySet()
            } catch (e: Exception) {
                emptySet()
            }
        } else {
            emptySet()
        }
    }

    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.remove(DISMISSED_ONGOING_MEETINGS_KEY)
            preferences.remove(DISMISSED_SCHEDULED_MEETINGS_KEY)
            preferences.remove(DISMISSED_MEETINGS_KEY)
        }
    }
}

