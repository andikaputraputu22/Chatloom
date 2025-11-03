package com.nandikacreativestudio.chatloom

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nandikacreativestudio.chatloom.ui.component.AppNavGraph
import com.nandikacreativestudio.chatloom.ui.component.ChatLayout
import com.nandikacreativestudio.chatloom.ui.component.CircularProgress
import com.nandikacreativestudio.chatloom.ui.component.DrawerLayout
import com.nandikacreativestudio.chatloom.ui.component.LoadingChatLayout
import com.nandikacreativestudio.chatloom.utils.Screen
import com.nandikacreativestudio.chatloom.viewmodel.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatLoomApp() {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    val gesturesDrawerEnabled = currentRoute == Screen.Chat.route
    val viewModel: ChatViewModel = hiltViewModel()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isSearchFocused by remember { mutableStateOf(false) }
    var hasExitedSearch by remember { mutableStateOf(false) }
    val currentRoomId by viewModel.currentRoomId.collectAsState()
    var isReturningFromSearch by remember { mutableStateOf(false) }
    val isLoadingDelete by viewModel.isLoadingDelete.collectAsState()
    val isLoadingLogin by viewModel.isLoadingLogin.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {
            viewModel.fetchChatRooms()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesDrawerEnabled && !isSearchFocused,
        drawerContent = {
            DrawerLayout(
                viewModel = viewModel,
                isSearchFocused = isSearchFocused,
                onSearchFocusChange = {
                    isSearchFocused = it
                    if (!it) hasExitedSearch = true
                },
                chatRooms = viewModel.chatRooms.collectAsState().value,
                currentRoomId = currentRoomId,
                onRoomClick = { roomId ->
                    viewModel.onRoomClick(roomId)
                    viewModel.setHasSendMessage(true)
                    if (isSearchFocused) {
                        isSearchFocused = false
                        isReturningFromSearch = true
                    } else {
                        scope.launch { drawerState.close() }
                    }
                },
                onDeleteRoom = { roomId ->
                    viewModel.deleteChatRoom(roomId)
                },
                onLoginClick = {
                    activity?.let {
                        viewModel.signInWithGoogle(it)
                    }
                },
                onLogoutClick = {
                    viewModel.signOut()
                }
            )
        }
    ) {
        AppNavGraph(
            navController = navController,
            viewModel = viewModel,
            openDrawer = {
                scope.launch { drawerState.open() }
            }
        )
    }

    BackHandler(enabled = drawerState.isOpen && !isSearchFocused) {
        scope.launch {
            drawerState.close()
        }
    }

    LaunchedEffect(hasExitedSearch, isSearchFocused) {
        if (!isSearchFocused && hasExitedSearch) {
            // Trigger ditunda hingga keluar dari fullscreen
            // Biarkan BackHandler biasa handle drawer close
        }
    }

    LaunchedEffect(isReturningFromSearch, isSearchFocused) {
        if (isReturningFromSearch && !isSearchFocused) {
            delay(300)
            scope.launch { drawerState.close() }
            isReturningFromSearch = false
        }
    }
    
    if (isLoadingDelete) {
        CircularProgress(title = "Deleting...")
    }

    if (isLoadingLogin) {
        CircularProgress(title = "Please wait...")
    }
}

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onMenuClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val hasSendMessage by viewModel.hasSendMessage.collectAsState()
    var input by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val density = LocalDensity.current
    val navBarHeightPx = WindowInsets.navigationBars.getBottom(density)
    val navBarHeightDp = with(density) { navBarHeightPx.toDp() }
    val imeHeightPx = WindowInsets.ime.getBottom(density)
    val imeBottom = with(density) { imeHeightPx.toDp() }

    val isLoading by viewModel.isLoading.collectAsState()
    val currentRoomId by viewModel.currentRoomId.collectAsState()
    val chats by viewModel.chats.collectAsState()
    val listState = rememberLazyListState()
    val lastIndex = chats.lastIndex

//    LaunchedEffect(currentRoomId) {
//        currentRoomId?.let {
//            viewModel.observeChats(it)
//        }
//    }

    LaunchedEffect(chats) {
        if (lastIndex >= 0) {
            listState.animateScrollToItem(
                index = lastIndex,
                scrollOffset = Int.MAX_VALUE
            )
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                    .background(color = colors.background)
            ) {
                ChatHeader(
                    onMenuClick = onMenuClick,
                    onCreateNewChatClick = {
                        focusManager.clearFocus()
                        input = ""
                        viewModel.clearChat()
                        viewModel.deleteGuestRoom()
                    },
                    onFavoriteClick = onFavoriteClick
                )
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .padding(
                        bottom = if (imeBottom > 0.dp) 0.dp else navBarHeightDp
                    )
            ) {
                ChatInputBar(
                    input = input,
                    onInputChange = { input = it },
                    onSend = {
                        viewModel.sendUserMessage(input)
                        input = ""
                    },
                    focusManager = focusManager
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            if (!hasSendMessage) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CreateNewChat()
                    Spacer(modifier = Modifier.height(16.dp))
                    Suggestion(
                        onSuggestionClick = {
                            viewModel.sendUserMessage(it)
                            input = ""
                        }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp),
                    state = listState
                ) {
                    itemsIndexed(chats) { index, chat ->
                        ChatLayout(
                            chat = chat,
                            isUser = chat.role == "user",
                            userBubbleColor = colors.surfaceVariant
                        )

                        val isLastUserMessage = chat.role == "user" &&
                                (index == chats.lastIndex || chats.getOrNull(index + 1)?.role != "assistant")
                        if (isLastUserMessage && isLoading) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                LoadingChatLayout()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateNewChat(
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "What can I help with?",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onBackground,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ChatHeader(
    onMenuClick: () -> Unit = {},
    onCreateNewChatClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
                .padding(end = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu"
            )
        }
        Text(
            text = "Chatloom",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Favorite"
            )
        }
        IconButton(
            onClick = onCreateNewChatClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
        ) {
            Icon(
                imageVector = Icons.Default.Create,
                contentDescription = "Menu"
            )
        }
    }
}

@Composable
fun ChatInputBar(
    input: String,
    onInputChange: (String) -> Unit,
    focusManager: FocusManager,
    onSend: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChatTextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                value = input,
                onValueChange = onInputChange,
                onSend = {
                    if (input.isNotBlank()) {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onSend()
                    }
                }
            )
            IconButton(
                onClick = {
                    if (input.isNotBlank()) {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onSend()
                    }
                },
                modifier = Modifier
                    .background(
                        color = colors.primary.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = colors.onPrimary
                )
            }
        }
        Text(
            modifier = Modifier
                .fillMaxWidth(),
            text = "AI can make mistakes. Please double-check responses.",
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ChatTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Ask anything",
    onSend: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme

    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = colors.onSurface.copy(alpha = 0.6f)
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(16.dp)),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Send
        ),
        keyboardActions = KeyboardActions(
            onSend = {
                onSend?.invoke()
            }
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceVariant,
            unfocusedContainerColor = colors.surfaceVariant,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = colors.primary,
            focusedTextColor = colors.onSurface,
            unfocusedTextColor = colors.onSurface,
            disabledTextColor = colors.onSurface.copy(alpha = 0.4f),
            focusedPlaceholderColor = colors.onSurface.copy(alpha = 0.6f),
            unfocusedPlaceholderColor = colors.onSurface.copy(alpha = 0.4f)
        ),
        textStyle = MaterialTheme.typography.bodyLarge
    )
}

@Composable
fun Suggestion(
    onSuggestionClick: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SuggestionChip(
            text = "Why are we allowed to dream?",
            backgroundColor = colors.tertiary,
            modifier = Modifier.weight(0.5f),
            onClick = onSuggestionClick
        )
        SuggestionChip(
            text = "Even though we don't have anything.",
            backgroundColor = colors.tertiary,
            modifier = Modifier.weight(0.5f),
            onClick = onSuggestionClick
        )
    }
}

@Composable
fun SuggestionChip(
    text: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor.copy(alpha = 0.15f),
        modifier = modifier
            .clickable { onClick(text) }
    ) {
        Text(
            text = text,
            color = backgroundColor,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
        )
    }
}