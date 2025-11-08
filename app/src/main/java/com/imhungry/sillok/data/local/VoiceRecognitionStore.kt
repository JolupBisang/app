package com.imhungry.sillok.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.voiceRecognitionDataStore: DataStore<Preferences> by preferencesDataStore(name = "voice_recognition_store")

@Singleton
class VoiceRecognitionStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.voiceRecognitionDataStore

    companion object {
        private val IS_COMPLETED_KEY = booleanPreferencesKey("is_completed")
        private val CURRENT_STEP_KEY = intPreferencesKey("current_step")
    }

    val isCompleted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_COMPLETED_KEY] ?: false
    }

    val currentStep: Flow<Int> = dataStore.data.map { preferences ->
        preferences[CURRENT_STEP_KEY] ?: 1
    }

    suspend fun setCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_COMPLETED_KEY] = completed
        }
    }

    suspend fun setCurrentStep(step: Int) {
        dataStore.edit { preferences ->
            preferences[CURRENT_STEP_KEY] = step
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
