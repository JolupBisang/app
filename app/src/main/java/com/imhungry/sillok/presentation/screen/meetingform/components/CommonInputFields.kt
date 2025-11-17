package com.imhungry.sillok.presentation.screen.meetingform.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.sillok.ui.theme.border
import com.imhungry.sillok.ui.theme.danger
import com.imhungry.sillok.ui.theme.gray400
import com.imhungry.sillok.ui.theme.primaryTextColor

@Composable
fun LabelText(
    text: String,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        modifier = Modifier.width(65.dp)
    )
}

@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    textFieldValue: TextFieldValue = TextFieldValue(
        text = value,
        selection = TextRange(value.length)
    ),
    onTextFieldValueChange: ((TextFieldValue) -> Unit)? = null,
    isReadOnly: Boolean = false,
    onImeDone: (() -> Unit)? = null,
    focusRequester: FocusRequester? = null
) {
    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LabelText(
            text = label
        )

        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                if (!isReadOnly) {
                    if (onTextFieldValueChange != null) {
                        onTextFieldValueChange(newValue)
                    } else {
                        onValueChange(newValue.text)
                    }
                }
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = if (value.isEmpty()) gray400 else primaryTextColor,
                fontWeight = FontWeight.Normal
            ),
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(4.dp))
                .border(1.dp, border, RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp)
                .then(
                    if (focusRequester != null) {
                        Modifier.focusRequester(focusRequester)
                    } else {
                        Modifier
                    }
                ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (onImeDone != null) {
                        onImeDone()
                    } else {
                        focusManager.clearFocus()
                    }
                }
            ),
            enabled = !isReadOnly,
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = gray400,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun ErrorText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = "! $text",
        style = MaterialTheme.typography.labelSmall.copy(color = danger),
        modifier = modifier.padding(top = 4.dp)
    )
}