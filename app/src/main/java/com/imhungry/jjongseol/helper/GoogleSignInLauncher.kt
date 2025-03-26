package com.imhungry.jjongseol.helper

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.imhungry.jjongseol.viewmodel.LoginViewModel

@Composable
fun rememberGoogleSignInLauncher(
    loginViewModel: LoginViewModel
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { result ->
    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
    try {
        val account = task.getResult(ApiException::class.java)
        loginViewModel.onGoogleLoginResult(account)
    } catch (e: ApiException) {
        Log.e("GoogleSignIn", "Login failed: ${e.statusCode}", e)
    }
}
