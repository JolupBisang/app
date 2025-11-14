package com.imhungry.sillok.presentation.screen.meetingform.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.imhungry.sillok.ui.theme.gray300
import com.imhungry.sillok.ui.theme.green300
import com.imhungry.sillok.ui.theme.placeHolder
import com.imhungry.sillok.ui.theme.whiteBackground

@Composable
fun MemberItem(
    name: String,
    email: String,
    profileImage: String? = null,
    isSelected: Boolean,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 이미지
        AsyncImage(
            model = profileImage?.ifEmpty { null },
            contentDescription = "프로필 이미지",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(placeHolder),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Normal,
            fontSize = 9.sp,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(4.dp))

        Box(
            modifier = Modifier
                .size(24.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { }
                )
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                enabled = false,
                modifier = Modifier
                    .size(24.dp)
                    .border(
                        width = 1.dp,
                        color = gray300,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(
                        color = whiteBackground,
                        shape = RoundedCornerShape(4.dp)
                    ),
                colors = CheckboxDefaults.colors(
                    checkedColor = whiteBackground,
                    uncheckedColor = whiteBackground,
                    checkmarkColor = green300,
                    disabledCheckedColor = whiteBackground,
                    disabledUncheckedColor = whiteBackground
                )
            )
        }
    }
}