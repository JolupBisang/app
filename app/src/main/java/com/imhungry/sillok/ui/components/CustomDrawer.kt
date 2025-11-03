package com.imhungry.sillok.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class DrawerState(
    private val drawerWidthPx: Float,
    private val drawerOffset: Animatable<Float, *>,
    private val scope: kotlinx.coroutines.CoroutineScope
) {
    var isOpen by mutableStateOf(false)
        private set
    
    var isDragging by mutableStateOf(false)
        internal set
    
    var dragStartOffset by mutableFloatStateOf(0f)
        internal set
    
    val offset: Float
        get() = drawerOffset.value
    
    fun open() {
        isOpen = true
    }
    
    fun close() {
        isOpen = false
    }
    
    fun toggle() {
        isOpen = !isOpen
    }
    
    internal suspend fun animateTo(target: Float) {
        drawerOffset.animateTo(
            targetValue = target,
            animationSpec = tween(durationMillis = 300)
        )
    }
    
    internal suspend fun snapTo(target: Float) {
        drawerOffset.snapTo(target)
    }
}

@Composable
fun rememberDrawerState(drawerWidth: Dp = 280.dp): DrawerState {
    val drawerWidthPx = with(LocalDensity.current) { drawerWidth.toPx() }
    val scope = rememberCoroutineScope()
    val drawerOffset = remember { Animatable(-drawerWidthPx) }
    
    return remember {
        DrawerState(
            drawerWidthPx = drawerWidthPx,
            drawerOffset = drawerOffset,
            scope = scope
        )
    }
}

@Composable
fun CustomDrawer(
    drawerState: DrawerState,
    drawerWidth: Dp = 280.dp,
    edgeThreshold: Dp = 50.dp,
    swipeThreshold: Float = 0.1f,
    drawerContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val drawerWidthPx = with(LocalDensity.current) { drawerWidth.toPx() }
    val edgeThresholdPx = with(LocalDensity.current) { edgeThreshold.toPx() }
    val scope = rememberCoroutineScope()
    
    // isOpen 상태에 따라 애니메이션
    LaunchedEffect(drawerState.isOpen) {
        drawerState.animateTo(
            target = if (drawerState.isOpen) 0f else -drawerWidthPx
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(drawerState.isOpen) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        if (drawerState.isOpen) {
                            // Drawer가 열려있을 때: Drawer 영역 바깥에서만 드래그 가능
                            if (offset.x > drawerWidthPx) {
                                drawerState.isDragging = true
                                drawerState.dragStartOffset = drawerState.offset
                            }
                        } else {
                            // Drawer가 닫혀있을 때: 화면 왼쪽 가장자리에서만 드래그 가능
                            if (offset.x < edgeThresholdPx) {
                                drawerState.isDragging = true
                                drawerState.dragStartOffset = drawerState.offset
                            }
                        }
                    },
                    onDragEnd = {
                        if (drawerState.isDragging) {
                            drawerState.isDragging = false
                            // 드래그 변화량을 기준으로 열림/닫힘 결정
                            val dragDelta = drawerState.offset - drawerState.dragStartOffset
                            val threshold = drawerWidthPx * swipeThreshold
                            
                            scope.launch {
                                when {
                                    dragDelta > threshold -> drawerState.open()
                                    dragDelta < -threshold -> drawerState.close()
                                    // threshold 이하의 작은 움직임은 현재 상태 유지
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        if (drawerState.isDragging) {
                            drawerState.isDragging = false
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        if (drawerState.isDragging) {
                            scope.launch {
                                val newOffset = (drawerState.offset + dragAmount)
                                    .coerceIn(-drawerWidthPx, 0f)
                                drawerState.snapTo(newOffset)
                            }
                        }
                    }
                )
            }
    ) {
        // 메인 콘텐츠
        content()
        
        // Drawer 배경 오버레이 (클릭 차단)
        val overlayAlpha = ((drawerState.offset + drawerWidthPx) / drawerWidthPx * 0.5f).coerceIn(0f, 0.5f)
        if (overlayAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = overlayAlpha))
                    .zIndex(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // 클릭 이벤트를 소비하여 하위 컴포넌트로 전달되지 않도록 함
                    }
            )
        }
        
        // Drawer (드래그 가능)
        if (drawerState.offset > -drawerWidthPx) {
            Column(
                modifier = Modifier
                    .width(drawerWidth)
                    .fillMaxSize()
                    .offset { IntOffset(drawerState.offset.roundToInt(), 0) }
                    .zIndex(2f)
            ) {
                drawerContent()
            }
        }
    }
}

