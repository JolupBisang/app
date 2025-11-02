package com.imhungry.sillok.domain.usecase.user

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.imhungry.sillok.BuildConfig
import javax.inject.Inject

class LaunchGoogleOAuthUseCase @Inject constructor() {
    
    operator fun invoke(context: Context) {
        val authUrl = ("https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount" +
                "?client_id=${BuildConfig.OAUTH_CLIENT_ID}" +
                "&redirect_uri=${BuildConfig.OAUTH_REDIRECT_URI}" +
                "&response_type=code" +
                "&scope=email profile").toUri()

        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(context, authUrl)
    }
}

