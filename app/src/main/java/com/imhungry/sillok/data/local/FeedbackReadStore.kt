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

/**
 * 회의별로 마지막 읽은 피드백 인덱스를 저장하는 데이터 클래스
 */
data class FeedbackReadInfo(
    val meetingId: Long,
    val lastReadFeedbackIndex: Int
)

private val Context.feedbackReadDataStore: DataStore<Preferences> by preferencesDataStore(name = "feedback_read_store")

@Singleton
class FeedbackReadStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.feedbackReadDataStore
    private val gson = Gson()

    companion object {
        private val FEEDBACK_READ_KEY = stringPreferencesKey("feedback_read")
    }

    /**
     * 모든 회의의 마지막 읽은 피드백 인덱스를 Flow로 반환
     */
    val feedbackReadInfo: Flow<Map<Long, Int>> = dataStore.data.map { preferences ->
        val readInfoJson = preferences[FEEDBACK_READ_KEY] ?: return@map emptyMap()
        try {
            val type = object : TypeToken<List<FeedbackReadInfo>>() {}.type
            val readInfoList = gson.fromJson<List<FeedbackReadInfo>>(readInfoJson, type) ?: emptyList()
            readInfoList.associate { it.meetingId to it.lastReadFeedbackIndex }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * 특정 회의의 마지막 읽은 피드백 인덱스 조회
     */
    suspend fun getLastReadFeedbackIndex(meetingId: Long): Int? {
        val readInfoMap = feedbackReadInfo.first { true }
        return readInfoMap[meetingId]
    }

    /**
     * 특정 회의의 마지막 읽은 피드백 인덱스 저장
     */
    suspend fun markFeedbackAsRead(meetingId: Long, feedbackIndex: Int) {
        dataStore.edit { preferences ->
            val currentReadInfoJson = preferences[FEEDBACK_READ_KEY]
            val currentReadInfoMap = if (currentReadInfoJson != null) {
                try {
                    val type = object : TypeToken<List<FeedbackReadInfo>>() {}.type
                    val readInfoList = gson.fromJson<List<FeedbackReadInfo>>(currentReadInfoJson, type) ?: emptyList()
                    readInfoList.associate { it.meetingId to it.lastReadFeedbackIndex }.toMutableMap()
                } catch (e: Exception) {
                    mutableMapOf()
                }
            } else {
                mutableMapOf()
            }

            // 해당 회의의 마지막 읽은 피드백 인덱스 업데이트 (더 큰 인덱스만 업데이트)
            val currentIndex = currentReadInfoMap[meetingId] ?: -1
            if (feedbackIndex > currentIndex) {
                currentReadInfoMap[meetingId] = feedbackIndex
            }

            // 다시 리스트로 변환하여 저장
            val updatedReadInfoList = currentReadInfoMap.map { (id, index) ->
                FeedbackReadInfo(id, index)
            }
            preferences[FEEDBACK_READ_KEY] = gson.toJson(updatedReadInfoList)
        }
    }

    /**
     * 특정 회의의 모든 피드백을 읽음 처리로 저장 (마지막 피드백 인덱스 저장)
     */
    suspend fun markAllFeedbacksAsRead(meetingId: Long, feedbackCount: Int) {
        dataStore.edit { preferences ->
            val currentReadInfoJson = preferences[FEEDBACK_READ_KEY]
            val currentReadInfoMap = if (currentReadInfoJson != null) {
                try {
                    val type = object : TypeToken<List<FeedbackReadInfo>>() {}.type
                    val readInfoList = gson.fromJson<List<FeedbackReadInfo>>(currentReadInfoJson, type) ?: emptyList()
                    readInfoList.associate { it.meetingId to it.lastReadFeedbackIndex }.toMutableMap()
                } catch (e: Exception) {
                    mutableMapOf()
                }
            } else {
                mutableMapOf()
            }

            // 해당 회의의 마지막 피드백 인덱스 저장 (0부터 시작하므로 count - 1)
            val lastIndex = if (feedbackCount > 0) feedbackCount - 1 else -1
            currentReadInfoMap[meetingId] = lastIndex

            // 다시 리스트로 변환하여 저장
            val updatedReadInfoList = currentReadInfoMap.map { (id, index) ->
                FeedbackReadInfo(id, index)
            }
            preferences[FEEDBACK_READ_KEY] = gson.toJson(updatedReadInfoList)
        }
    }

    /**
     * 특정 회의의 읽은 피드백 정보 삭제
     */
    suspend fun clearFeedbackReadInfo(meetingId: Long) {
        dataStore.edit { preferences ->
            val currentReadInfoJson = preferences[FEEDBACK_READ_KEY]
            val currentReadInfoMap = if (currentReadInfoJson != null) {
                try {
                    val type = object : TypeToken<List<FeedbackReadInfo>>() {}.type
                    val readInfoList = gson.fromJson<List<FeedbackReadInfo>>(currentReadInfoJson, type) ?: emptyList()
                    readInfoList.associate { it.meetingId to it.lastReadFeedbackIndex }.toMutableMap()
                } catch (e: Exception) {
                    mutableMapOf()
                }
            } else {
                mutableMapOf()
            }

            currentReadInfoMap.remove(meetingId)

            // 다시 리스트로 변환하여 저장
            val updatedReadInfoList = currentReadInfoMap.map { (id, index) ->
                FeedbackReadInfo(id, index)
            }
            preferences[FEEDBACK_READ_KEY] = gson.toJson(updatedReadInfoList)
        }
    }

    /**
     * 모든 읽은 피드백 정보 삭제
     */
    suspend fun clearAllFeedbackReadInfo() {
        dataStore.edit { preferences ->
            preferences.remove(FEEDBACK_READ_KEY)
        }
    }
}

