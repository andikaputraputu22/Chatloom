package com.nandikacreativestudio.chatloom.api

import com.nandikacreativestudio.chatloom.models.ChatResponse
import com.nandikacreativestudio.chatloom.models.request.ChatRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("chat/completions")
    suspend fun getChatCompletion(
        @Body request: ChatRequest
    ): Response<ChatResponse>
}