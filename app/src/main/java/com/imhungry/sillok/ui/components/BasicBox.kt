package com.imhungry.sillok.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.imhungry.sillok.R
import com.imhungry.sillok.ui.theme.dialogBackGround
import com.imhungry.sillok.ui.theme.gradientBrush2

@Composable
fun BasicBox(
    statusBarColor: Color,
    navigationBarColor: Color,
    backgroundColor: Color,
    isLoading: Boolean = false,
    content: @Composable () -> Unit
) {
    SystemBars(
        statusBarColor = statusBarColor,
        navigationBarColor = navigationBarColor,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(20.dp)
        ) {
            content()
        }
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(dialogBackGround)
                    .zIndex(1000f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(R.drawable.loading)
                            .decoderFactory(GifDecoder.Factory())
                            .build()
                    ),
                    contentDescription = "로딩 gif",
                    modifier = Modifier.size(80.dp)
                )
            }
        }
    }
}

@Composable
fun MeetingBasicBox(
    navigationBarColor: Color,
    backgroundColor: Color,
    isLoading: Boolean = false,
    content: @Composable () -> Unit
) {
    navigationBarColor(
        navigationBarColor = navigationBarColor
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            content()
        }
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(dialogBackGround)
                    .zIndex(1000f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(R.drawable.loading)
                            .decoderFactory(GifDecoder.Factory())
                            .build()
                    ),
                    contentDescription = "로딩 gif",
                    modifier = Modifier.size(80.dp)
                )
            }
        }
    }
}

@Composable
fun BasicBoxWithGradientBurshBackground(
    statusBarColor: Color,
    navigationBarColor: Color,
    backgroundColor: Color,
    gradientBrush: Brush? = gradientBrush2,
    isLoading: Boolean,
    content: @Composable () -> Unit
) {
    SystemBars(
        statusBarColor = statusBarColor,
        navigationBarColor = navigationBarColor,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .then(
                if (gradientBrush != null) {
                    Modifier.background(gradientBrush)
                } else {
                    Modifier
                }
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 12.dp)
        ) {
            content()
        }
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(dialogBackGround)
                    .zIndex(1000f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(R.drawable.loading)
                            .decoderFactory(GifDecoder.Factory())
                            .build()
                    ),
                    contentDescription = "로딩 gif",
                    modifier = Modifier.size(80.dp)
                )
            }
        }
    }
}