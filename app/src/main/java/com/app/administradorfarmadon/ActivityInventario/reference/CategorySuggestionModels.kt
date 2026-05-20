package com.app.administradorfarmadon.ActivityInventario.reference

import com.squareup.moshi.Json

data class BarcodeAiResult(
    val estado: String, // IDENTIFICADO | NO_IDENTIFICADO
    val codigo: String = "",
    val nombre: String? = "",
    val categoria: String? = "",
    val tipoControl: String? = "DESCONOCIDO", // UNIDAD | PESO | LIQUIDO | DESCONOCIDO
    val requiereReceta: Boolean = false,
    val razon: String? = ""
)

/**
 * V18.2: Resultado del escaneo de barcode con imagen adjunta.
 */
data class BarcodeScanResult(
    val code: String,
    val imageBase64: String?
)

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
    val keywords: List<String> = emptyList(), // Palabras clave o síntomas
    val esInsegura: Boolean = false, // Flag para sugerencias de baja confianza (V3.23)
    val isRefined: Boolean = false, // V5.7: Indica si es una respuesta completa de IA

    // Nueva capa de intención (V8.0)
    val tipoConsulta: TipoConsultaDetectada = TipoConsultaDetectada.ESPECIFICO,
    val nombreCorregido: String? = null, // V9.0: Nombre estandarizado por IA
    val sugerenciaUsuario: String? = null,
    val sugerenciasBusqueda: List<String> = emptyList(),
    
    // Metadatos de la referencia (V5.0)
    val fuenteReferencia: String = "Estimaci\u00f3n IA",
    val confianzaReferencia: Int = 0, // 0-100
    
    // Referencias reales de mercado (V6.0)
    val marketPriceReferences: List<MarketPriceReference> = emptyList(),
    
    // Investigaci\u00f3n estructurada de mercado (V7.0)
    val marketResearch: MarketResearchResult? = null,
    
    // V13.0: Referencias r\u00e1pidas de mercado (reemplaza marketPriceReferences para simplificar)
    val marketReferences: List<MarketReferenceItem> = emptyList(),

    // V14.4: Validación de integridad del producto
    val estadoProducto: EstadoProducto = EstadoProducto.VALIDO,
    val opcionesSeparadas: List<String> = emptyList(),
    val puedeContinuar: Boolean = true,
    val motivoValidacion: String? = null
)

/**
 * V14.4: Clasifica la calidad/integridad de la entrada del usuario.
 */
enum class EstadoProducto {
    VALIDO,          // Producto claro y bien escrito
    CORREGIBLE,      // Producto claro pero con errores menores (tipográficos/unidades)
    CONTRADICTORIO,  // Mezcla de productos incompatibles
    DESCONOCIDO      // Información insuficiente
}

/**
 * V13.0: Item simple de referencia de mercado para la IA Profunda.
 */
data class MarketReferenceItem(
    val presentation: String,
    val price: Double,
    val source: String
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
    val descripcion: String = "",
    val precioMercadoReferencial: Double? = null,
    val tipoComercial: String = "DESCONOCIDO", // V5.8: GENERICO | MARCA | DESCONOCIDO
    
    // Metadatos específicos por presentación (V5.0)
    val confianzaPrecio: Int = 0,
    val advertencia: String? = null
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
 * Clasifica la intención del usuario al escribir el nombre (V8.0).
 * Ayuda a determinar si la IA debe ser asertiva o sugerente.
 */
enum class TipoConsultaDetectada {
    ESPECIFICO,   // Producto identificado claramente (Ej: Panadol 500mg)
    GENERAL,      // Nombre real pero genérico (Ej: Panadol)
    AMBIGUO,      // Varias interpretaciones posibles (Ej: Amoxi)
    INSUFICIENTE  // Texto sin valor farmacéutico (Ej: medicina)
}

/**
 * Estados del flujo de sugerencia.
 *
 *  - INITIAL: aún no hay nombre suficiente; tarjeta vacía invitando a escribir.
 *  - LOADING: Gemini consultando; mostrar shimmer animado.
 *  - PRELIMINARY: Sugerencia rápida (heurística o caché parcial).
 *  - READY: hay una sugerencia válida lista para auto-aceptarse.
 *  - ACCEPTED: el usuario aceptó (o pasó al siguiente paso); chip oculto.
 *  - MANUAL: el usuario eligió escribir manualmente; mostrar text field.
 *  - FALLBACK_MANUAL: Gemini falló u offline; fallback transparente a manual.
 */
enum class CategorySuggestionStatus {
    INITIAL,
    LOADING,
    PRELIMINARY,
    READY,
    ACCEPTED,
    MANUAL,
    FALLBACK_MANUAL
}

enum class ConfianzaCorreccion {
    ALTA,
    MEDIA,
    BAJA
}

/**
 * Resultado estructurado de una corrección de nombre (V12.0).
 */
data class NameCorrectionResult(
    val nombreCorregido: String? = null,
    val nivelConfianza: ConfianzaCorreccion = ConfianzaCorreccion.BAJA,
    val motivo: String = "",
    val alternativas: List<String> = emptyList(),
    val cambioPotenciaDetectado: Boolean = false,
    val localSafetyLog: String? = null
)

enum class ConfianzaIA {
    ALTA,
    MEDIA,
    BAJA,
    DESCONOCIDA
}

/**
 * V16.10: Sugerencia ligera de tipo de control para el modo manual.
 * V17.82: Añadido contexto de nombre y categoría para validación estricta.
 */
data class ManualTypeSuggestion(
    val tipo: TipoControlDetectado,
    val confianza: ConfianzaIA,
    val motivo: String = "",
    val productName: String = "",
    val category: String = "",
    val requiereReceta: Boolean? = null,
    val razonReceta: String = ""
)

data class CategorySuggestionUiState(
    val status: CategorySuggestionStatus = CategorySuggestionStatus.INITIAL,
    val suggestion: CategorySuggestion? = null,
    val queryName: String = "",
    val interpreted: PharmaceuticalParser.ParsedProduct? = null, // Interpretación farmacéutica (V3.25)
    val loadingMessage: String? = null, // V5.7: Feedback de progreso
    val pendingCorrection: NameCorrectionResult? = null, // V12.0: Resultado estructurado de corrección
    
    // V14.4: Estado extendido para manejo de conflictos
    val errorValidacion: String? = null,
    val opcionesAlternativas: List<String> = emptyList(),
    
    // V16.0: Soporte para asistencia manual ligera
    val asistenciaManualCategorias: List<String> = emptyList(),
    val estaCargandoAsistencia: Boolean = false,

    // V16.10: Soporte para sugerencia silenciosa de tipo
    val sugerenciaTipoManual: ManualTypeSuggestion? = null,
    val estaCargandoTipo: Boolean = false,

    // V18.0: Barcode AI
    val barcodeAiResult: BarcodeAiResult? = null,
    val estaIdentificandoBarcode: Boolean = false,
    val barcodeAiRequestId: Long = 0L,
    
    // V18.8: Validación de Integridad
    val barcodeMismatchDetected: Boolean = false,
    val barcodeMismatchOriginalName: String? = null
)

// ─── Modelos crudos de la API de Gemini (generateContent) ────────────────────

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig,
    @param:Json(name = "system_instruction") val systemInstruction: GeminiContent? = null,
    val tools: List<GeminiTool>? = null
)

data class GeminiTool(
    @param:Json(name = "google_search")
    val googleSearch: Map<String, Any>? = null,
    @param:Json(name = "google_search_retrieval")
    val googleSearchRetrieval: Map<String, Any>? = null
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
    @param:Json(name = "responseMimeType") val responseMimeType: String? = "application/json",
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
