package com.app.administradorfarmadon.ActivityInventario.reference

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Interfaz Retrofit para la API de DeepSeek.
 * Utiliza el formato de chat de OpenAI.
 */
interface DeepSeekApi {
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: DeepSeekChatRequest
    ): DeepSeekChatResponse

    @GET("models")
    suspend fun getModels(
        @Header("Authorization") authorization: String
    ): Any
}
