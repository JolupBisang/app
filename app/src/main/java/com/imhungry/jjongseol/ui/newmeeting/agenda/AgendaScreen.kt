package com.imhungry.jjongseol.ui.newmeeting.agenda

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.jjongseol.ui.theme.md_theme_button_color_blue
import java.util.UUID

data class AgendaItem(val id: String = UUID.randomUUID().toString(), var text: String, var isPlaceholder: Boolean = true)

@Composable
fun AgendaListScreen() {
    val itemList = remember { mutableStateListOf<AgendaItem>() }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(15.dp))
        ) {
            items(items = itemList, key = { it.id }) { item ->
                ListItemWithCircle(
                    item = item,
                    onEdit = { newText ->
                        item.text = newText
                        item.isPlaceholder = false
                    },
                    onDelete = { itemList.remove(item) }
                )
            }
        }

        Button(modifier = Modifier.fillMaxWidth().padding(
            start = 8.dp, end = 8.dp, top = 3.dp, bottom = 8.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray),
            onClick = {
                itemList.add(AgendaItem(text = "새 아젠다", isPlaceholder = true))
            }) {
            Text("+", style = TextStyle(color = md_theme_button_color_blue, fontSize = 25.sp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListItemWithCircle(item: AgendaItem, onEdit: (String) -> Unit, onDelete: () -> Unit) {
    var editText by remember { mutableStateOf(item.text) }
    var editing by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            //고민(ExperimentalFoundationApi::class) <-- 오쪼지
            .combinedClickable(
                onClick = { },
                onLongClick = {
                    editing = true
                    if (item.isPlaceholder) {
                        editText = ""
                    }
                }
            )

    ) {
        Box(modifier = Modifier
            .padding(start = 15.dp)
            .size(10.dp)
            .background(color = Color.LightGray, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))

        if (editing) {
            TextField(
                value = editText,
                onValueChange = { editText = it },
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 200.dp),
                onDone = {
                    editing = false
                    //고민
                    /*if (editText.isEmpty()) {
                        editText = item.text
                    }*/
                    onEdit(editText)
                },
                textStyle = TextStyle(fontSize = 15.sp, color = if (item.isPlaceholder) Color.Black else Color.Black),
                placeholder = {
                    if (item.isPlaceholder) Text(item.text, style = TextStyle(color = Color.Gray))
                }
            )
        } else {
            Text(
                text = editText,
                style = TextStyle(fontSize = 15.sp, color = if (item.isPlaceholder) Color.Gray else Color.Black),
                modifier = Modifier.weight(1f)
            )
        }

        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit",
            modifier = Modifier
                .clickable {
                    editing = true
                    if (item.isPlaceholder) {
                        editText = ""
                    }
                }
                .padding(8.dp)
        )
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            modifier = Modifier
                .clickable { onDelete() }
                .padding(8.dp)
        )
    }
}

@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onDone: () -> Unit,
    textStyle: TextStyle,
    placeholder: @Composable (() -> Unit)? = null
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            onDone()
            keyboardController?.hide()
        }),
        textStyle = textStyle,
        placeholder = placeholder
    )
}
