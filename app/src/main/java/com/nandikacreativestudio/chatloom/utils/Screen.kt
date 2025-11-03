package com.nandikacreativestudio.chatloom.utils

sealed class Screen(val route: String) {
    data object Chat: Screen("chat")
    data object Favorite: Screen("favorite")
}