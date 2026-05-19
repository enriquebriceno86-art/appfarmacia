package com.app.administradorfarmadon.ActivityInventario.reference

import com.squareup.moshi.Json

/**
 * Modelo para la investigaci\u00f3n de mercado estructurada v\u00eda Gemini.
 * (V7.0: Arquitectura de Investigador Estructurado)
 */
data class MarketResearchResult(
    val productoInterpretado: InterpretedProductInfo,
    val referencias: List<MarketStoreReference>,
    val presentacionesAgrupadas: List<GroupedMarketPresentation>,
    val advertencias: List<String>,
    val fuenteGeneral: String = "Investigaci\u00f3n IA"
)

data class InterpretedProductInfo(
    val nombreBase: String,
    val concentracion: String,
    val forma: String,
    val requiereReceta: Boolean,
    val confianzaInterpretacion: Int
)

data class MarketStoreReference(
    val tienda: String,
    val url: String,
    val nombreDetectado: String,
    val presentacionDetectada: String,
    val equivalenciaUnidadBase: Int,
    val precio: Double,
    val moneda: String = "S/",
    val confianza: Int,
    val observacion: String
)

data class GroupedMarketPresentation(
    val nombrePresentacion: String,
    val equivalenciaUnidadBase: Int,
    val precioMin: Double,
    val precioMax: Double,
    val precioPromedio: Double,
    val cantidadReferencias: Int,
    val nivelConfianza: String, // ALTA | MEDIA | BAJA | SIN_CONFIANZA
    val fuentes: List<String>
)
