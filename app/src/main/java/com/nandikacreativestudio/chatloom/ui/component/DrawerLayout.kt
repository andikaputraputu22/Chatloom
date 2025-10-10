package com.nandikacreativestudio.chatloom.ui.component

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nandikacreativestudio.chatloom.R
import com.nandikacreativestudio.chatloom.models.ChatRoom

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("ReturnFromAwaitPointerEventScope")
@Composable
fun DrawerLayout(
    isSearchFocused: Boolean,
    onSearchFocusChange: (Boolean) -> Unit,
    chatRooms: List<ChatRoom>,
    currentRoomId: String?,
    onRoomClick: (String) -> Unit,
    onDeleteRoom: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var selectedRoomToDelete by remember {
        mutableStateOf<ChatRoom?>(null)
    }

    BackHandler(enabled = isSearchFocused) {
        onSearchFocusChange(false)
    }

    val modifier = if (isSearchFocused) {
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    }
                }
            }
    } else {
        Modifier
            .width(300.dp)
            .fillMaxHeight()
    }

    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.surface)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            SearchBar(
                isFocused = isSearchFocused,
                onFocusChange = onSearchFocusChange,
                onRequestClearFocus = { onSearchFocusChange(false) }
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = "History Chats",
                    color = colors.onSurface,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(start = 12.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp)
                ) {
                    items(chatRooms) { room ->
                        val isSelected = room.id == currentRoomId
                        val backgroundColor = if (isSelected) colors.primary.copy(alpha = 0.1f)
                        else Color.Transparent
                        val textColor = if (isSelected) colors.primary else colors.onSurface

                        Text(
                            text = room.title,
                            color = textColor,
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = backgroundColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(vertical = 8.dp, horizontal = 12.dp)
                                .combinedClickable(
                                    onClick = { onRoomClick(room.id) },
                                    onLongClick = { selectedRoomToDelete = room }
                                )
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.default_photo_profile),
                    contentDescription = "Photo Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "I Putu Andika Putra",
                    color = colors.onSurface,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown Icon",
                    tint = colors.onSurface,
                    modifier = Modifier
                        .padding(start = 4.dp)
                )
            }
//            Column {
//                Text(
//                    text = "Save your chat history and personalize your experience.",
//                    color = colors.onSurface.copy(alpha = 0.7f),
//                    fontSize = 14.sp
//                )
//                Spacer(modifier = Modifier.height(12.dp))
//                Button(
//                    onClick = {},
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(48.dp),
//                    shape = RoundedCornerShape(24.dp)
//                ) {
//                    Text(
//                        text = "Log In or Sign Up",
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.SemiBold
//                    )
//                }
//            }
        }

        selectedRoomToDelete?.let { room ->
            AlertDialog(
                onDismissRequest = { selectedRoomToDelete = null },
                title = {
                    Text(text = "Delete Chat")
                },
                text = {
                    Text(text = "Are you sure want to delete this chat?")
                },
                confirmButton = { 
                    TextButton(onClick = {
                        onDeleteRoom(room.id)
                        selectedRoomToDelete = null
                    }) {
                        Text(text = "Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedRoomToDelete = null }) {
                        Text(text = "Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SearchBar(
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    onRequestClearFocus: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val colors = MaterialTheme.colorScheme

    LaunchedEffect(isFocused) {
        if (isFocused) {
            focusRequester.requestFocus()
        } else {
            focusManager.clearFocus()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "Search",
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                if (isFocused) {
                    IconButton(onClick = { onRequestClearFocus() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon"
                    )
                }
            },
            shape = RoundedCornerShape(50),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged {
                    onFocusChange(it.isFocused)
                },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceVariant,
                unfocusedContainerColor = colors.surfaceVariant,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            )
        )
    }
}