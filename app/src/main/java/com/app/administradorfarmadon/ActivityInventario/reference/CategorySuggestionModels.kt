package com.app.administradorfarmadon.ActivityInventario.reference

import com.squareup.moshi.Json

/**
 * Resultado limpio de una sugerencia generada por IA.
 *
 * Cubre dos detecciones en una sola consulta para ahorrar costo/latencia:
 *  - Categoría (con su emoji representativo).
 *  - Tipo de control de inventario (UNIDAD / PESO / LIQUIDO).
 *
 * La razón principal se refiere a la categoría; `razonTipo` explica
 * brevemente por qué la IA eligió ese método de control.
 */
data class CategorySuggestion(
    val productName: String,
    val categoria: String,
    val emoji: String,
    val tipoControl: TipoControlDetectado,
    val razonTipo: String,
    val requiereReceta: Boolean,
    val razonReceta: String,
    val modoIngreso: ModoIngresoDetectado,
    val presentacionesSugeridas: List<PresentacionSugerida>,
    val existeEnLista: Boolean,
    val confianza: Int,
    val razon: String,
    val keywords: List<String> = emptyList() // Palabras clave o síntomas
)

/**
 * Una presentación de venta sugerida por IA. La equivalencia siempre está
 * expresada en la unidad base del producto (tabletas, g, mL). Para evitar
 * sugerencias absurdas, la IA limita la mínima a algo comercialmente vendible:
 *  - UNIDAD: hasta la tableta/cápsula suelta
 *  - PESO: hasta el envase comercial completo (tubo, sobre)
 *  - LIQUIDO: hasta el envase comercial completo (frasco, bolsa)
 */
data class PresentacionSugerida(
    val nombre: String,
    val equivalenciaUnidadBase: Int,
    val descripcion: String = ""
)

/**
 * Forma típica en la que el producto llega al inventario. La IA solo
 * lo sugiere como guía visual; el usuario sigue confirmando con un clic
 * (o cambiando a otro modo si compra distinto a lo habitual).
 */
enum class ModoIngresoDetectado {
    UNIDAD,                 // unidades sueltas
    CAJA,                   // caja con unidades directas
    CAJA_CON_PAQUETES,      // caja con frascos/blisters/paquetes
    DESCONOCIDO
}

/**
 * Resultado de escanear con la cámara la etiqueta de un producto
 * usando Gemini Vision. Cualquiera de los dos campos puede venir null
 * si la imagen no era lo bastante clara o el OCR no encontró ese dato.
 */
data class EtiquetaDetectada(
    val loteNumero: String?,
    val vencimientoMmAa: String?
)

// ─── Inline data para envío de imágenes a Gemini Vision ─────────────────

data class GeminiInlineData(
    @param:Json(name = "mimeType") val mimeType: String,
    val data: String   // base64 sin prefijo data:
)

/**
 * Equivalente serializable del enum `CreateProductControlType` para no
 * acoplar el repository de IA con la capa UI. El caller mapea estos
 * valores al enum real al aplicar la sugerencia.
 */
enum class TipoControlDetectado {
    UNIDAD,
    PESO,
    LIQUIDO,
    DESCONOCIDO
}

/**
 * Estados del flujo de sugerencia.
 *
 *  - INITIAL: aún no hay nombre suficiente; tarjeta vacía invitando a escribir.
 *  - LOADING: Gemini consultando; mostrar shimmer animado.
 *  - READY: hay una sugerencia válida lista para auto-aceptarse.
 *  - ACCEPTED: el usuario aceptó (o pasó al siguiente paso); chip oculto.
 *  - MANUAL: el usuario eligió escribir manualmente; mostrar text field.
 *  - FALLBACK_MANUAL: Gemini falló u offline; fallback transparente a manual.
 */
enum class CategorySuggestionStatus {
    INITIAL,
    LOADING,
    READY,
    ACCEPTED,
    MANUAL,
    FALLBACK_MANUAL
}

data class CategorySuggestionUiState(
    val status: CategorySuggestionStatus = CategorySuggestionStatus.INITIAL,
    val suggestion: CategorySuggestion? = null,
    val queryName: String = ""
)

// ─── Modelos crudos de la API de Gemini (generateContent) ────────────────────

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig
)

data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String? = null,
    @param:Json(name = "inlineData") val inlineData: GeminiInlineData? = null
)

data class GeminiGenerationConfig(
    val temperature: Double = 0.2,
    @param:Json(name = "responseMimeType") val responseMimeType: String = "application/json",
    @param:Json(name = "responseSchema") val responseSchema: GeminiSchema? = null
)

data class GeminiSchema(
    val type: String,
    val properties: Map<String, GeminiSchemaProperty>,
    val required: List<String>
)

data class GeminiSchemaProperty(
    val type: String,
    val description: String? = null,
    // Para arrays: schema de cada item.
    val items: GeminiSchemaProperty? = null,
    // Para objetos anidados: propiedades + requeridos.
    val properties: Map<String, GeminiSchemaProperty>? = null,
    val required: List<String>? = null
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    val content: GeminiContent?
)
