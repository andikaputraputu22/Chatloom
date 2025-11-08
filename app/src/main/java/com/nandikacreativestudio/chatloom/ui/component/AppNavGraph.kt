package com.nandikacreativestudio.chatloom.ui.component

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nandikacreativestudio.chatloom.ChatScreen
import com.nandikacreativestudio.chatloom.ui.screen.FavoriteScreen
import com.nandikacreativestudio.chatloom.utils.Screen
import com.nandikacreativestudio.chatloom.viewmodel.ChatViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: ChatViewModel,
    openDrawer: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Chat.route
    ) {
        composable(Screen.Chat.route) {
            ChatScreen(
                viewModel = viewModel,
                onMenuClick = openDrawer,
                onFavoriteClick = {
                    navController.navigate(Screen.Favorite.route)
                }
            )
        }

        composable(Screen.Favorite.route) {
            FavoriteScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}