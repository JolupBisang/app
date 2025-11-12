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

private val Context.teamDescriptionDataStore: DataStore<Preferences> by preferencesDataStore(name = "team_description_store")

@Singleton
class TeamDescriptionStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.teamDescriptionDataStore
    private val gson = Gson()

    companion object {
        private val TEAM_DESCRIPTIONS_KEY = stringPreferencesKey("team_descriptions")
    }

    /**
     * 모든 팀의 한줄소개를 Flow로 반환
     */
    val teamDescriptions: Flow<Map<Long, String>> = dataStore.data.map { preferences ->
        val descriptionsJson = preferences[TEAM_DESCRIPTIONS_KEY] ?: return@map emptyMap()
        try {
            val type = object : TypeToken<Map<String, String>>() {}.type
            val stringMap = gson.fromJson<Map<String, String>>(descriptionsJson, type) ?: emptyMap()
            stringMap.mapKeys { it.key.toLongOrNull() ?: -1L }
                .filterKeys { it != -1L }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * 특정 팀의 한줄소개 조회
     */
    suspend fun getTeamDescription(teamId: Long): String? {
        val descriptions = teamDescriptions.first()
        return descriptions[teamId]
    }

    /**
     * 특정 팀의 한줄소개 저장
     */
    suspend fun saveTeamDescription(teamId: Long, description: String) {
        dataStore.edit { preferences ->
            val currentDescriptionsJson = preferences[TEAM_DESCRIPTIONS_KEY]
            val currentDescriptions = if (currentDescriptionsJson != null) {
                try {
                    val type = object : TypeToken<Map<String, String>>() {}.type
                    gson.fromJson<Map<String, String>>(currentDescriptionsJson, type) ?: emptyMap()
                } catch (e: Exception) {
                    emptyMap()
                }
            } else {
                emptyMap()
            }

            val updatedDescriptions = currentDescriptions.toMutableMap()
            updatedDescriptions[teamId.toString()] = description
            preferences[TEAM_DESCRIPTIONS_KEY] = gson.toJson(updatedDescriptions)
        }
    }

    /**
     * 특정 팀의 한줄소개 삭제
     */
    suspend fun removeTeamDescription(teamId: Long) {
        dataStore.edit { preferences ->
            val currentDescriptionsJson = preferences[TEAM_DESCRIPTIONS_KEY]
            val currentDescriptions = if (currentDescriptionsJson != null) {
                try {
                    val type = object : TypeToken<Map<String, String>>() {}.type
                    gson.fromJson<Map<String, String>>(currentDescriptionsJson, type) ?: emptyMap()
                } catch (e: Exception) {
                    emptyMap()
                }
            } else {
                emptyMap()
            }

            val updatedDescriptions = currentDescriptions.toMutableMap()
            updatedDescriptions.remove(teamId.toString())
            preferences[TEAM_DESCRIPTIONS_KEY] = gson.toJson(updatedDescriptions)
        }
    }
}

