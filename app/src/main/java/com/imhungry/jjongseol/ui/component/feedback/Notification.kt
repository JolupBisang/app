package com.imhungry.jjongseol.ui.component.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.imhungry.jjongseol.ui.theme.Pretend
import com.imhungry.jjongseol.ui.theme.gray500
import com.imhungry.jjongseol.ui.theme.orange100
import com.imhungry.jjongseol.ui.theme.orange500
import com.imhungry.jjongseol.ui.theme.primaryTextColor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
                .padding(horizontal = 20.dp)
                .background(
                    color = gray500,
                    shape = MaterialTheme.shapes.medium
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
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = message,
                        fontFamily = Pretend,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium,
                        color = primaryTextColor
                    )
                }

                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(top = 8.dp, bottom = 8.dp, end = 20.dp)
                ) {
                    val (timeRef, dotRef) = createRefs()

                    Text(
                        text = extractTimeOnly(time),
                        fontFamily = Pretend,
                        fontWeight = FontWeight.Medium,
                        color = primaryTextColor,
                        style = MaterialTheme.typography.bodySmall,
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
                                .background(orange100, shape = CircleShape)
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

fun extractTimeOnly(isoString: String): String {
    val dt = LocalDateTime.parse(isoString)
    return dt.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
}