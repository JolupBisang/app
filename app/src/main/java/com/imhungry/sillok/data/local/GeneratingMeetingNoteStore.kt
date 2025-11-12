package com.imhungry.sillok.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.generatingMeetingNoteDataStore: DataStore<Preferences> by preferencesDataStore(name = "generating_meeting_note_store")

@Singleton
class GeneratingMeetingNoteStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.generatingMeetingNoteDataStore

    companion object {
        private val GENERATING_MEETING_NOTE_ID_KEY = longPreferencesKey("generating_meeting_note_id")
    }

    /**
     * 회의록 생성 중인 회의 ID를 Flow로 반환
     */
    val generatingMeetingNoteId: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[GENERATING_MEETING_NOTE_ID_KEY]
    }

    /**
     * 회의록 생성 중인 회의 ID 조회
     */
    suspend fun getGeneratingMeetingNoteId(): Long? {
        return generatingMeetingNoteId.first()
    }

    /**
     * 회의록 생성 중인 회의 ID 저장
     */
    suspend fun setGeneratingMeetingNoteId(meetingId: Long) {
        dataStore.edit { preferences ->
            preferences[GENERATING_MEETING_NOTE_ID_KEY] = meetingId
        }
    }

    /**
     * 회의록 생성 중인 회의 ID 제거
     */
    suspend fun clearGeneratingMeetingNoteId() {
        dataStore.edit { preferences ->
            preferences.remove(GENERATING_MEETING_NOTE_ID_KEY)
        }
    }
}

