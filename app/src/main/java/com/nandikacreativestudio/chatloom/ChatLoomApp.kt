package com.nandikacreativestudio.chatloom

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Create
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nandikacreativestudio.chatloom.ui.component.DrawerLayout
import kotlinx.coroutines.launch

@Composable
fun ChatLoomApp() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isSearchFocused by remember { mutableStateOf(false) }
    var hasExitedSearch by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isSearchFocused,
        drawerContent = {
            DrawerLayout(
                isSearchFocused = isSearchFocused,
                onSearchFocusChange = {
                    isSearchFocused = it
                    if (!it) hasExitedSearch = true
                }
            )
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            ChatScreen(
                modifier = Modifier
                    .padding(innerPadding),
                onMenuClick = {
                    scope.launch { drawerState.open() }
                }
            )
        }
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
}

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = colors.background)
            .padding(24.dp)
    ) {
        ChatHeader(onMenuClick = onMenuClick)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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
        Suggestion()
        ChatInputBar()
    }
}

@Composable
fun ChatHeader(
    onMenuClick: () -> Unit = {}
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
            onClick = {},
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
fun ChatInputBar() {
    val colors = MaterialTheme.colorScheme
    var input by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChatTextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                value = input,
                onValueChange = { input = it }
            )
            IconButton(
                onClick = {},
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
    placeholder: String = "Ask anything"
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
fun Suggestion() {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(0.5f)
        ) {
            SuggestionChip(
                text = "Why are we allowed to dream?",
                backgroundColor = colors.tertiary
            )
        }
        Box(
            modifier = Modifier
                .weight(0.5f)
        ) {
            SuggestionChip(
                text = "Even though we don't have anything.",
                backgroundColor = colors.tertiary
            )
        }
    }
}

@Composable
fun SuggestionChip(
    text: String,
    backgroundColor: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor.copy(alpha = 0.15f),
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