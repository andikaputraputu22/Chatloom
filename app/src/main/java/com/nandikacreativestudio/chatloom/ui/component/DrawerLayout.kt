package com.nandikacreativestudio.chatloom.ui.component

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nandikacreativestudio.chatloom.models.ChatRoom
import com.nandikacreativestudio.chatloom.viewmodel.ChatViewModel

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("ReturnFromAwaitPointerEventScope")
@Composable
fun DrawerLayout(
    viewModel: ChatViewModel,
    isSearchFocused: Boolean,
    onSearchFocusChange: (Boolean) -> Unit,
    chatRooms: List<ChatRoom>,
    currentRoomId: String?,
    onRoomClick: (String) -> Unit,
    onDeleteRoom: (String) -> Unit,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var selectedRoomToDelete by remember {
        mutableStateOf<ChatRoom?>(null)
    }
    var showLogoutDialog by remember {
        mutableStateOf(false)
    }
    var searchQuery by remember { mutableStateOf("") }

    val filteredRooms = remember(searchQuery, chatRooms) {
        if (searchQuery.isBlank()) chatRooms
        else chatRooms.filter {
            it.title.contains(searchQuery, ignoreCase = true)
        }
    }

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userData by viewModel.userData.collectAsState()

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
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
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
                    items(filteredRooms) { room ->
                        val isSelected = room.id == currentRoomId
                        val backgroundColor = if (isSelected) colors.primary.copy(alpha = 0.1f)
                        else Color.Transparent
                        val textColor = if (isSelected) colors.primary else colors.onSurface

                        Text(
                            text = room.title,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
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
            if (isLoggedIn) {
                ProfileLayout(
                    userData = userData,
                    onLogoutClick = { showLogoutDialog = true }
                )
            } else {
                LoginLayout(onLoginClick)
            }
        }

        selectedRoomToDelete?.let { room ->
            ConfirmationDialog(
                title = "Delete Chat",
                message = "Are you sure want to delete this chat?",
                confirmText = "Delete",
                confirmColor = Color.Red,
                onConfirm = {
                    onDeleteRoom(room.id)
                    selectedRoomToDelete = null
                },
                onDismiss = { selectedRoomToDelete = null }
            )
        }

        if (showLogoutDialog) {
            ConfirmationDialog(
                title = "Sign Out",
                message = "Are you sure want to sign out?",
                confirmText = "Yes",
                confirmColor = Color.Green,
                onConfirm = {
                    onLogoutClick()
                    showLogoutDialog = false
                },
                onDismiss = { showLogoutDialog = false }
            )
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    onRequestClearFocus: () -> Unit
) {
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
            onValueChange = onSearchQueryChange,
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