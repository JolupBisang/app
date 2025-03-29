package com.imhungry.jjongseol.data.repository

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.imhungry.jjongseol.BuildConfig
import javax.inject.Inject

class AuthRepository @Inject constructor() {
    fun launchGoogleOAuth(activityContext: Context) {
        val authUrl = Uri.parse(
            "https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount" +
                    "?client_id=${BuildConfig.OAUTH_CLIENT_ID}" +
                    "&redirect_uri=${BuildConfig.OAUTH_REDIRECT_URI}" +
                    "&response_type=code" +
                    "&scope=email profile")

        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(activityContext, authUrl)
    }
}
