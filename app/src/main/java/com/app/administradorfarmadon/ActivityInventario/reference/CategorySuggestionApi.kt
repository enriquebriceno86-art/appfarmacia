package com.app.administradorfarmadon.ActivityInventario.reference

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz Retrofit para el endpoint REST de Gemini.
 *
 * Documentación oficial:
 * https://ai.google.dev/api/generate-content
 *
 * El modelo viaja en la ruta; la API key como query param porque es el
 * formato más simple. Si en el futuro se rota por App Check / OAuth, basta
 * cambiar este punto.
 */
interface GeminiApi {

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
