package com.nandikacreativestudio.chatloom.repository

import com.nandikacreativestudio.chatloom.api.ApiService
import com.nandikacreativestudio.chatloom.models.ChatResponse
import com.nandikacreativestudio.chatloom.models.request.ChatMessage
import com.nandikacreativestudio.chatloom.models.request.ChatRequest
import com.nandikacreativestudio.chatloom.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun fetchChatCompletion(
        chat: String
    ): Result<ChatResponse> = withContext(Dispatchers.IO) {
        val messages = listOf(
            ChatMessage(
                role = "developer",
                content = "You are a helpful assistant."
            ),
            ChatMessage(
                role = "user",
                content = chat
            )
        )
        val request = ChatRequest(
            model = "gpt-4.1-mini",
            messages = messages
        )
        try {
            val response = apiService.getChatCompletion(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.Success(it)
                } ?: Result.Error("Response body is null")
            } else {
                Result.Error("Failed to fetch data: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}")
        }
    }
}