package com.imhungry.jjongseol.ui.newmeeting.agenda

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.jjongseol.data.model.agenda.AgendaItem
import com.imhungry.jjongseol.ui.theme.UserGreen2
import java.util.*

@Composable
fun AgendaListScreen(agendaList: SnapshotStateList<String>, enabled: Boolean) {
    val itemList = remember {
        mutableStateListOf<AgendaItem>().apply {
            agendaList.forEach { text ->
                add(AgendaItem(id = UUID.randomUUID().hashCode().toLong(), text = text, isPlaceholder = false))
            }
        }
    }

    LaunchedEffect(itemList.size) {
        agendaList.clear()
        agendaList.addAll(itemList.map { it.text })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (enabled) 180.dp else 100.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
        ) {
            items(
                items = itemList,
                key = { it.id ?: UUID.randomUUID().hashCode().toLong() }
            ) { item ->
                ListItemWithCircle(
                    item = item,
                    onEdit = { newText ->
                        val index = itemList.indexOf(item)
                        if (index != -1) {
                            itemList[index] = item.copy(
                                id = UUID.randomUUID().hashCode().toLong(),
                                text = newText,
                                isPlaceholder = false
                            )
                            agendaList.clear()
                            agendaList.addAll(itemList.map { it.text })
                        }
                    },
                    onDelete = {
                        itemList.remove(item)
                        agendaList.clear()
                        agendaList.addAll(itemList.map { it.text })
                    },
                    enabled = enabled
                )
            }
        }

        if (enabled) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp, bottom = 8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = UserGreen2),
                onClick = {
                    val newItem = AgendaItem(
                        id = UUID.randomUUID().hashCode().toLong(),
                        text = "새 아젠다",
                        isPlaceholder = true
                    )
                    itemList.add(newItem)
                    agendaList.add(newItem.text)
                }
            ) {
                Text("+", style = TextStyle(color = Color.Black, fontSize = 25.sp))
            }
        }
    }
}

@Composable
fun AgendaListScreen(
    agendaItems: List<AgendaItem>,
    onEdit: (AgendaItem, String) -> Unit,
    onDelete: (AgendaItem) -> Unit,
    onAdd: () -> Unit,
    enabled: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (enabled) 180.dp else 100.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
        ) {
            items(
                items = agendaItems,
                key = { it.id ?: it.text.hashCode().toLong() }
            ) { item ->
                ListItemWithCircle(
                    item = item,
                    onEdit = { newText -> onEdit(item, newText) },
                    onDelete = { onDelete(item) },
                    enabled = enabled
                )
            }
        }

        if (enabled) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp, bottom = 8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = UserGreen2),
                onClick = onAdd
            ) {
                Text("+", style = TextStyle(color = Color.Black, fontSize = 25.sp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListItemWithCircle(item: AgendaItem, onEdit: (String) -> Unit, onDelete: () -> Unit, enabled: Boolean) {
    var editText by remember { mutableStateOf(item.text) }
    var editing by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .combinedClickable(
                enabled = enabled,
                onClick = {},
                onLongClick = {
                    editing = true
                    if (item.isPlaceholder) editText = ""
                }
            )
    ) {
        Box(
            modifier = Modifier
                .padding(start = 15.dp)
                .size(5.dp)
                .background(color = Color.LightGray, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))

        if (editing) {
            EditableTextField(
                value = editText,
                onValueChange = { editText = it },
                onDone = {
                    editing = false
                    onEdit(editText)
                },
                enabled = enabled,
                placeholderText = item.text.takeIf { item.isPlaceholder }
            )
        } else {
            Text(
                text = editText,
                style = TextStyle(
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    color = if (item.isPlaceholder) Color.Gray else Color.Black
                ),
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                maxLines = 1
            )
        }

        if (enabled) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier
                .clickable {
                    editing = true
                    if (item.isPlaceholder) editText = ""
                }
                .padding(5.dp)
            )
            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier
                .clickable { onDelete() }
                .padding(5.dp)
            )
        }
    }
}

@Composable
fun EditableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    enabled: Boolean,
    placeholderText: String? = null
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 200.dp)
                .onFocusChanged { focusState ->
                    if (isFocused && !focusState.isFocused) {
                        onDone()
                        keyboardController?.hide()
                    }
                    isFocused = focusState.isFocused
                },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                onDone()
                keyboardController?.hide()
                focusManager.clearFocus()
            }),
            textStyle = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, color = Color.Black),
            placeholder = {
                placeholderText?.let {
                    Text(it, style = TextStyle(color = Color.Gray))
                }
            },
            enabled = enabled,
            readOnly = !enabled
        )
    }
}
