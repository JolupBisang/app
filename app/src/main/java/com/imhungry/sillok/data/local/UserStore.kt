package com.imhungry.sillok.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.imhungry.sillok.domain.model.user.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_store")

@Singleton
class UserStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.userDataStore

    companion object {
        private val USER_ID_KEY = longPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_NICKNAME_KEY = stringPreferencesKey("user_nickname")
        private val USER_PROFILE_IMAGE_KEY = stringPreferencesKey("user_profile_image")
        private const val TAG = "UserStore"
    }

    val user: Flow<User?> = dataStore.data.map { preferences ->
        val id = preferences[USER_ID_KEY]
        val email = preferences[USER_EMAIL_KEY]
        val nickname = preferences[USER_NICKNAME_KEY]
        val profileImage = preferences[USER_PROFILE_IMAGE_KEY] ?: ""
        val exists = (id != null && email != null && nickname != null)

        if (exists) {
            User(id = id!!, email = email!!, nickname = nickname!!, profileImage = profileImage)
        } else {
            null
        }
    }

    suspend fun saveUser(user: User) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = user.id
            preferences[USER_EMAIL_KEY] = user.email
            preferences[USER_NICKNAME_KEY] = user.nickname
            preferences[USER_PROFILE_IMAGE_KEY] = user.profileImage
        }
    }

    suspend fun clearUser() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(USER_NICKNAME_KEY)
            preferences.remove(USER_PROFILE_IMAGE_KEY)
        }
    }
}

