package com.imhungry.jjongseol

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.imhungry.jjongseol.ui.HomeScreen
import com.imhungry.jjongseol.ui.LoginScreen
import com.imhungry.jjongseol.ui.LoginScreenPreview
import com.imhungry.jjongseol.ui.theme.AppTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                LoginScreen()
            }
        }
    }
}