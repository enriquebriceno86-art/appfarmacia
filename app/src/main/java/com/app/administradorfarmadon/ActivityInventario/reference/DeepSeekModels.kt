package com.app.administradorfarmadon.ActivityInventario.reference

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Modelos para la API de DeepSeek (formato compatible con OpenAI).
 */

@JsonClass(generateAdapter = true)
data class DeepSeekChatRequest(
    val model: String,
    val messages: List<DeepSeekMessage>,
    val temperature: Double = 0.2,
    @Json(name = "response_format")
    val responseFormat: DeepSeekResponseFormat? = null
)

@JsonClass(generateAdapter = true)
data class DeepSeekMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class DeepSeekResponseFormat(
    val type: String
)

@JsonClass(generateAdapter = true)
data class DeepSeekChatResponse(
    val choices: List<DeepSeekChoice>?
)

@JsonClass(generateAdapter = true)
data class DeepSeekChoice(
    val message: DeepSeekMessage?
)

/**
 * Respuesta estructurada específica para la sugerencia de categorías.
 */
@JsonClass(generateAdapter = true)
data class DeepSeekCategoryAnswer(
    val tipoConsulta: String = "ESPECIFICO",
    val nombreCorregido: String? = null, // V9.0
    val categoria: String = "",
    val tipoControl: String = "DESCONOCIDO",
    val requiereReceta: Boolean = false,
    val descripcionBreveUso: String = "",
    val keywords: List<String> = emptyList(),
    val confianza: Double = 0.0,
    val razon: String = "",
    val sugerenciaUsuario: String? = null,
    val sugerenciasBusqueda: List<String> = emptyList(),
    val presentacionesMasVendidas: List<DeepSeekPresentacionMasVendida> = emptyList() // V8.7: Estructurado con costo
)

@JsonClass(generateAdapter = true)
data class DeepSeekPresentacionMasVendida(
    val nombre: String = "",
    val precioVentaReferencia: Double? = null,
    val nombreFarmaciaCompetencia: String? = null
)
