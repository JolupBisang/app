package com.imhungry.jjongseol.ui.meeting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.SilRokNavigation
import kotlinx.coroutines.delay

@Composable
fun MeetingEndScreen(
    onFinish: (SilRokNavigation) -> Unit
) {
    var isCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(3000)
        isCompleted = true
    }

    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            delay(1000)
            onFinish(SilRokNavigation.CompletedMeeting)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isCompleted) "회의록 완성!" else "회의록 생성 중...",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Image(
            painter = painterResource(
                id = if (isCompleted) R.drawable.complete else R.drawable.loading
            ),
            contentDescription = if (isCompleted) "회의록 완성 이미지" else "회의록 생성 중 이미지",
            modifier = Modifier
                .size(200.dp)
        )
    }
}
