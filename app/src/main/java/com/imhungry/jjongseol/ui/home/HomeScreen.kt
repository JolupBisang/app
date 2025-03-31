package com.imhungry.jjongseol.ui.home

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext


@Composable
fun HomeScreen(
) {
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                Toast.makeText( context,"test", Toast.LENGTH_SHORT).show()

                val intent = Intent(context, CreateNewMeetingActivity::class.java)
                context.startActivity(intent)
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "추가")
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("홈 화면 test3")
            }
        }
    )
}

