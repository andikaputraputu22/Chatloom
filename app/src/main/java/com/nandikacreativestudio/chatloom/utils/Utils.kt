package com.nandikacreativestudio.chatloom.utils

class Utils {

    fun getRole(role: Int): String {
        return if (role == Constants.ROLE_USER) {
            "user"
        } else {
            "assistant"
        }
    }
}