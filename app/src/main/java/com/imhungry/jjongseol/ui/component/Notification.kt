package com.imhungry.jjongseol.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout

@Composable
fun Notification(
    visible: Boolean,
    message: String,
    time: String,
    isRead: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .padding(horizontal = 24.dp)
                .background(
                    color = Color(0xFFECECEC),
                    shape = MaterialTheme.shapes.large
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }

                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(top = 8.dp, bottom = 8.dp, end = 20.dp)
                ) {
                    val (timeRef, dotRef) = createRefs()

                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFAFAFAF),
                        modifier = Modifier.constrainAs(timeRef) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                    )

                    if (!isRead) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color.Red, shape = CircleShape)
                                .constrainAs(dotRef) {
                                    top.linkTo(parent.top)
                                    end.linkTo(parent.end)
                                }
                        )
                    }
                }
            }
        }
    }
}