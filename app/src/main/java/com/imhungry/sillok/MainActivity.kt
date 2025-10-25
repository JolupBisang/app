package com.imhungry.sillok

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imhungry.sillok.data.local.TokenExpirationManager
import com.imhungry.sillok.data.local.VoiceRecognitionStore
import com.imhungry.sillok.presentation.navigation.SillokNavigation
import com.imhungry.sillok.presentation.viewmodel.shared.SharedViewModel
import com.imhungry.sillok.ui.theme.SillokTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var sharedViewModel: SharedViewModel
    
    @Inject
    lateinit var tokenExpirationManager: TokenExpirationManager
    
    @Inject
    lateinit var voiceRecognitionStore: VoiceRecognitionStore
    
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SillokTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    sharedViewModel = viewModel()
                    SillokNavigation(
                        sharedViewModel = sharedViewModel,
                        tokenExpirationManager = tokenExpirationManager,
                        voiceRecognitionStore = voiceRecognitionStore
                    )
                }
            }
        }
        
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.getQueryParameter("token")?.let { token ->
            sharedViewModel.setLoginToken(token)
        }
    }
}