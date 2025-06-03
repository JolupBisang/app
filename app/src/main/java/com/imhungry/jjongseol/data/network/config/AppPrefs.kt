package com.imhungry.jjongseol.data.network.config

import android.content.Context

class AppPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun isVoiceTutorialCompleted(): Boolean = prefs.getBoolean("voice_tutorial_completed", false)

    fun setVoiceTutorialCompleted() {
        prefs.edit().putBoolean("voice_tutorial_completed", true).apply()
    }
}
