package com.nandikacreativestudio.chatloom.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SharedPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val PREFERENCES_NAME = "chatloom_pref"
        const val ROOM_LIST = "room_list"
    }

    private val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun setRoomList(roomList: List<String>) {
        val json = gson.toJson(roomList)
        sharedPreferences.edit()
            .putString(ROOM_LIST, json)
            .apply()
    }

    fun getRoomList(): List<String> {
        val json = sharedPreferences.getString(ROOM_LIST, null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun addRoom(room: String) {
        val currentList = getRoomList().toMutableList()
        if (!currentList.contains(room)) {
            currentList.add(room)
            setRoomList(currentList)
        }
    }

    fun clearRooms() {
        sharedPreferences.edit()
            .remove(ROOM_LIST)
            .apply()
    }
}