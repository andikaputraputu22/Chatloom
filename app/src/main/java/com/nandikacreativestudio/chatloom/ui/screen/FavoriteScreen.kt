package com.nandikacreativestudio.chatloom.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nandikacreativestudio.chatloom.ui.component.EmptyPlaceholder
import com.nandikacreativestudio.chatloom.ui.component.FavoriteHeader
import com.nandikacreativestudio.chatloom.ui.component.FavoriteRoomItem
import com.nandikacreativestudio.chatloom.viewmodel.ChatViewModel

@Composable
fun FavoriteScreen(
    navController: NavHostController,
    viewModel: ChatViewModel
) {
    val colors = MaterialTheme.colorScheme
    val favoriteRooms by viewModel.favoriteRooms.collectAsState()
    var isFavoriteScreenVisible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        viewModel.observeFavoriteRooms()
        isFavoriteScreenVisible = true
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopFavoriteRoomsObserver() }
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
                FavoriteHeader(
                    onBackClick = {
                        isFavoriteScreenVisible = false
                        navController.popBackStack()
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (favoriteRooms.isEmpty()) {
                EmptyPlaceholder(
                    modifier = Modifier.fillMaxSize(),
                    title = "No Favorite Room"
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(favoriteRooms, key = { it.id }) { room ->
                        AnimatedVisibility(
                            visible = isFavoriteScreenVisible,
                            enter = fadeIn(animationSpec = tween(600)) + expandVertically(),
                            exit = fadeOut(animationSpec = tween(400)) + shrinkVertically()
                        ) {
                            FavoriteRoomItem(
                                room = room,
                                onClick = {
                                    navController.popBackStack()
                                    viewModel.onRoomClick(room.id)
                                    viewModel.setHasSendMessage(true)
                                },
                                onRemoveFavorite = { room ->
                                    viewModel.setFavorite(room.id, room.isOnFavorite)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}