package com.app.administradorfarmadon.ActivityInventario.reference

import android.util.Log
import com.app.administradorfarmadon.BuildConfig
import com.google.firebase.database.FirebaseDatabase
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.Normalizer
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Resuelve la categoría sugerida usando Gemini como fuente única.
 * Decisión de diseño: Gemini se consulta SIEMPRE para categorías.
 * La extracción de Lote/Vencimiento fue migrada a ML Kit local (V30.0).
 */
object CategorySuggestionRepository {

    private const val MODEL = "gemini-2.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    private const val PATH_DECISIONES = "Inventario/DecisionesCategoria"

    private const val DEEPSEEK_MODEL = "deepseek-chat"
    private const val DEEPSEEK_BASE_URL = "https://api.deepseek.com/"

    private var isGeminiWarmed = false
    private var isDeepSeekWarmed = false

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val api: GeminiApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                    else HttpLoggingInterceptor.Level.NONE
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    private val deepSeekApi: DeepSeekApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(DEEPSEEK_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(DeepSeekApi::class.java)
    }

    private val duckBarcodeSearch by lazy {
        DuckDuckGoBarcodeSearch()
    }

    private val barcodeResultMemoryCache = mutableMapOf<String, BarcodeAiResult>()

    fun warmup(apiKey: String, dsApiKey: String) {
        if (!isGeminiWarmed && apiKey.isNotBlank()) {
            @Suppress("OPT_IN_USAGE")
            GlobalScope.launch(Dispatchers.IO) {
                runCatching {
                    api.getModel(MODEL, apiKey)
                    isGeminiWarmed = true
                    Log.d("Warmup", "Gemini connection established.")
                }
            }
        }
        
        if (!isDeepSeekWarmed && dsApiKey.isNotBlank()) {
            @Suppress("OPT_IN_USAGE")
            GlobalScope.launch(Dispatchers.IO) {
                runCatching {
                    deepSeekApi.getModels("Bearer $dsApiKey")
                    isDeepSeekWarmed = true
                    Log.d("Warmup", "DeepSeek connection established.")
                }
            }
        }
    }

    suspend fun sugerirCategoriaSilenciosa(
        productName: String,
        mercadoActivo: String = "Perú"
    ): String? {
        val apiKey = BuildConfig.DEEPSEEK_API_KEY
        if (apiKey.isBlank()) return null

        val prompt = """
            Producto: "$productName"
            Mercado: "$mercadoActivo"
            Devuelve únicamente la mejor categoría profesional (ej: Analgésicos, Antibióticos, Gaseosas).
            Responde ÚNICAMENTE JSON: {"c":"Categoría"}
        """.trimIndent()

        val request = DeepSeekChatRequest(
            model = DEEPSEEK_MODEL,
            messages = listOf(
                DeepSeekMessage("system", "Responde solo JSON con clave 'c'."),
                DeepSeekMessage("user", prompt)
            ),
            temperature = 0.0,
            responseFormat = DeepSeekResponseFormat("json_object")
        )

        return runCatching {
            val response = deepSeekApi.createChatCompletion("Bearer $apiKey", request)
            val rawJson = response.choices?.firstOrNull()?.message?.content ?: return null
            Log.d("SilentAI", "Category JSON: $rawJson")
            
            val adapter = moshi.adapter(CategoryOnlyAnswer::class.java).lenient()
            adapter.fromJson(rawJson)?.c?.trim()
        }.getOrNull()
    }

    private data class CategoryOnlyAnswer(@Json(name = "c") val c: String = "")

    fun registrarDecisionAceptada(suggestion: CategorySuggestion) {
        runCatching {
            val key = normalizar(suggestion.productName).ifBlank { return@runCatching }
            val decisionId = FirebaseDatabase.getInstance()
                .getReference(PATH_DECISIONES)
                .child(key)
                .push()
                .key ?: return@runCatching
            val payload = mapOf(
                "productoOriginal" to suggestion.productName,
                "categoria" to suggestion.categoria,
                "emoji" to suggestion.emoji,
                "tipoControl" to suggestion.tipoControl.name,
                "requiereReceta" to suggestion.requiereReceta,
                "confianza" to suggestion.confianza,
                "existiaEnLista" to suggestion.existeEnLista,
                "timestamp" to System.currentTimeMillis()
            )
            FirebaseDatabase.getInstance()
                .getReference(PATH_DECISIONES)
                .child(key)
                .child(decisionId)
                .setValue(payload)
        }
    }

    suspend fun identificarProductoPorImagenConCodigoExperimental(
        barcode: String,
        imageBase64: String?,
        categoriasExistentes: List<String>
    ): BarcodeAiResult? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) return null

        val categoriasTexto = if (categoriasExistentes.isEmpty()) {
            "[]"
        } else {
            categoriasExistentes.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
        }

        val systemInstruction = """
Eres un identificador experto de productos para inventario retail general.

IMPORTANTE: 
- Si la búsqueda web previa no confirmó el producto, no afirmes que el código corresponde a un producto específico. 
- Usa lenguaje cauteloso: "Visualmente parece...".
- NO inventes contenido neto si no se lee CLARAMENTE en la imagen. 
- Si no se ve el peso/volumen, usa "1 unidades" (Ej: Lata=1 unidades).

CATEGORÍAS:
- Para comida de perro usa "Comida para perros" o "Alimentos para mascotas".
- Para comida de gato usa "Comida para gatos" o "Alimentos para mascotas".
- Para alimento humano usa "Alimentos", "Bebidas", "Snacks", "Galletas", "Cereales", "Conservas" o "Lácteos".
- Para farmacia usa categorías farmacéuticas específicas.

REGLAS DE SALIDA:
Responde solo JSON válido.

Formato:
{
  "estado": "IDENTIFICADO" | "NO_IDENTIFICADO" | "CONFLICTO_VISUAL",
  "codigo": "$barcode",
  "nombre": "nombre del producto si es seguro",
  "categoria": "categoria",
  "tipoControl": "UNIDAD" | "PESO" | "LIQUIDO" | "DESCONOCIDO",
  "requiereReceta": true | false,
  "razon": "explicación breve",
  "presentacionVentaDefault": "Lata|Botella|Caja...",
  "ventaFraccionadaPermitida": true,
  "confianzaPresentacion": 90,
  "razonPresentacion": "razón de la presentación",
  "presentacionesVentaSugeridasTexto": "Lata=1 unidades(95)",
  "confianzaPresentacionesVenta": 90,
  "razonPresentacionesVenta": "Se detecta producto en unidades; el peso no es visible."
}
""".trimIndent()

        val userPrompt = """
Código detectado: "$barcode"

Categorías disponibles:
$categoriasTexto

Identifica el producto usando el código y la imagen si está disponible.

IMPORTANTE:
- La imagen tiene prioridad para validar que el código pertenece al producto visible.
- Si la imagen muestra comida de gato, comida de perro o producto para mascotas, clasifícalo como mascotas.
- Si el código parece apuntar a otro producto distinto al que se ve, responde CONFLICTO_VISUAL.
- No confirmes Pringles, snacks u otro producto si la etiqueta visible no coincide.
- Si puedes detectar formas de venta, devuelve presentacionesVentaSugeridasTexto con el formato exacto pedido.
""".trimIndent()

        val parts = mutableListOf<GeminiPart>().apply {
            add(GeminiPart(text = userPrompt))
            if (!imageBase64.isNullOrBlank()) {
                add(GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = imageBase64)))
            }
        }

        val barcodeSchema = GeminiSchema(
            type = "OBJECT",
            properties = mapOf(
                "estado" to GeminiSchemaProperty("STRING"),
                "codigo" to GeminiSchemaProperty("STRING"),
                "nombre" to GeminiSchemaProperty("STRING"),
                "categoria" to GeminiSchemaProperty("STRING"),
                "tipoControl" to GeminiSchemaProperty("STRING"),
                "requiereReceta" to GeminiSchemaProperty("BOOLEAN"),
                "razon" to GeminiSchemaProperty("STRING"),
                "presentacionVentaDefault" to GeminiSchemaProperty("STRING"),
                "ventaFraccionadaPermitida" to GeminiSchemaProperty("BOOLEAN"),
                "confianzaPresentacion" to GeminiSchemaProperty("INTEGER"),
                "razonPresentacion" to GeminiSchemaProperty("STRING"),
                "presentacionesVentaSugeridasTexto" to GeminiSchemaProperty("STRING"),
                "confianzaPresentacionesVenta" to GeminiSchemaProperty("INTEGER"),
                "razonPresentacionesVenta" to GeminiSchemaProperty("STRING")
            ),
            required = listOf(
                "estado", "codigo", "nombre", "categoria", "tipoControl", "requiereReceta", "razon",
                "presentacionVentaDefault", "ventaFraccionadaPermitida", "confianzaPresentacion", "razonPresentacion",
                "presentacionesVentaSugeridasTexto", "confianzaPresentacionesVenta", "razonPresentacionesVenta"
            )
        )

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = parts)),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.0,
                responseMimeType = "application/json",
                responseSchema = barcodeSchema
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstruction)))
        )

        return runCatching {
            val response = api.generateContent(model = MODEL, apiKey = apiKey, request = request)
            val content = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.extractJsonObject() ?: return null

            Log.d("BarcodeAI", "Gemini Response JSON: $content")

            val parsed: BarcodeAiResult? = moshi.adapter(BarcodeAiResult::class.java)
                .lenient()
                .fromJson(content)
                ?.let { result ->
                    result.copy(
                        codigo = result.codigo.ifBlank { barcode },
                        nombre = result.nombre.orEmpty(),
                        categoria = result.categoria.orEmpty(),
                        tipoControl = result.tipoControl.orEmpty().ifBlank { "DESCONOCIDO" },
                        requiereReceta = result.requiereReceta,
                        razon = result.razon.orEmpty(),
                        presentacionVentaDefault = result.presentacionVentaDefault.orEmpty(),
                        ventaFraccionadaPermitida = result.ventaFraccionadaPermitida,
                        confianzaPresentacion = result.confianzaPresentacion.coerceIn(0, 100),
                        razonPresentacion = result.razonPresentacion.orEmpty(),
                        presentacionesVentaSugeridasTexto = result.presentacionesVentaSugeridasTexto.orEmpty(),
                        confianzaPresentacionesVenta = result.confianzaPresentacionesVenta.coerceIn(0, 100),
                        razonPresentacionesVenta = result.razonPresentacionesVenta.orEmpty()
                    )
                }

            Log.d("BarcodeAI", "Parsed Result: $parsed")

            parsed?.let { ai ->
                Log.d(
                    "AI_PRESENTACIONES",
                    """
                ===== IA PRESENTACIONES BARCODE =====
                Estado: ${ai.estado}
                Código: ${ai.codigo}
                Producto: ${ai.nombre}
                Categoría: ${ai.categoria}
                TipoControl: ${ai.tipoControl}
                Presentación default: ${ai.presentacionVentaDefault}
                Confianza default: ${ai.confianzaPresentacion}
                Fraccionada permitida: ${ai.ventaFraccionadaPermitida}
                Razón default: ${ai.razonPresentacion}
                Sugerencias venta: ${ai.presentacionesVentaSugeridasTexto}
                Confianza sugerencias: ${ai.confianzaPresentacionesVenta}
                Razón sugerencias: ${ai.razonPresentacionesVenta}
                ====================================
                """.trimIndent()
                )
            }
            parsed
        }.onFailure {
            Log.e("BarcodeAI", "Error identificando barcode con Gemini Vision", it)
        }.getOrNull()
    }

    suspend fun identificarProductoPorImagen(
        imageBase64: String,
        categoriasExistentes: List<String>
    ): BarcodeAiResult? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) return null

        if (imageBase64.isBlank()) {
            return BarcodeAiResult(
                estado = "NO_IDENTIFICADO",
                codigo = "",
                nombre = "",
                categoria = "",
                tipoControl = "DESCONOCIDO",
                requiereReceta = false,
                razon = "No se recibió imagen para analizar."
            )
        }

        val categoriasTexto = if (categoriasExistentes.isEmpty()) {
            "[]"
        } else {
            categoriasExistentes.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
        }

        val systemInstruction = """
Eres un identificador experto de productos por imagen para inventario retail general.

IMPORTANTE: 
- Usa lenguaje cauteloso: "Visualmente parece...".
- NO inventes contenido neto si no se lee CLARAMENTE en la imagen. 
- Si no se ve el peso/volumen, usa "1 unidades" (Ej: Frasco=1 unidades).

REGLAS CRÍTICAS:
- Identifica SOLO usando lo visible en la imagen.
- No inventes marca, nombre ni presentación si no se ven claramente.
- Si ves perro, gato, mascota, alimento para mascotas o lata/bolsa/sobre de comida para mascota, clasifícalo como mascotas.

REGLAS DE SALIDA:
Responde solo JSON válido.

Formato:
{
  "estado": "IDENTIFICADO" | "NO_IDENTIFICADO",
  "codigo": "",
  "nombre": "nombre del producto si es seguro",
  "categoria": "categoria",
  "tipoControl": "UNIDAD" | "PESO" | "LIQUIDO" | "DESCONOCIDO",
  "requiereReceta": true | false,
  "razon": "explicación breve",
  "presentacionVentaDefault": "Frasco",
  "ventaFraccionadaPermitida": true,
  "confianzaPresentacion": 85,
  "razonPresentacion": "razón de la presentación",
  "presentacionesVentaSugeridasTexto": "Frasco=1 unidades(85)",
  "confianzaPresentacionesVenta": 85,
  "razonPresentacionesVenta": "Se detecta producto en frasco; el contenido no es legible."
}
""".trimIndent()

        val userPrompt = """
Analiza la imagen del producto.

Categorías disponibles:
$categoriasTexto

Identifica el producto solo por lo visible en la imagen.

IMPORTANTE:
- No uses ni inventes código de barras.
- Si la imagen no muestra claramente el producto, responde NO_IDENTIFICADO.
- Si se ve un producto para perros, gatos o mascotas, clasifícalo en mascotas.
- Si puedes detectar formas de venta, devuelve presentacionesVentaSugeridasTexto con el formato exacto pedido.
- Devuelve solo JSON válido con el esquema solicitado.
""".trimIndent()

        val parts = listOf(
            GeminiPart(text = userPrompt),
            GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = imageBase64))
        )

        val imageSchema = GeminiSchema(
            type = "OBJECT",
            properties = mapOf(
                "estado" to GeminiSchemaProperty("STRING"),
                "codigo" to GeminiSchemaProperty("STRING"),
                "nombre" to GeminiSchemaProperty("STRING"),
                "categoria" to GeminiSchemaProperty("STRING"),
                "tipoControl" to GeminiSchemaProperty("STRING"),
                "requiereReceta" to GeminiSchemaProperty("BOOLEAN"),
                "razon" to GeminiSchemaProperty("STRING"),
                "presentacionVentaDefault" to GeminiSchemaProperty("STRING"),
                "ventaFraccionadaPermitida" to GeminiSchemaProperty("BOOLEAN"),
                "confianzaPresentacion" to GeminiSchemaProperty("INTEGER"),
                "razonPresentacion" to GeminiSchemaProperty("STRING"),
                "presentacionesVentaSugeridasTexto" to GeminiSchemaProperty("STRING"),
                "confianzaPresentacionesVenta" to GeminiSchemaProperty("INTEGER"),
                "razonPresentacionesVenta" to GeminiSchemaProperty("STRING")
            ),
            required = listOf(
                "estado", "codigo", "nombre", "categoria", "tipoControl", "requiereReceta", "razon",
                "presentacionVentaDefault", "ventaFraccionadaPermitida", "confianzaPresentacion", "razonPresentacion",
                "presentacionesVentaSugeridasTexto", "confianzaPresentacionesVenta", "razonPresentacionesVenta"
            )
        )

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = parts)),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.0,
                responseMimeType = "application/json",
                responseSchema = imageSchema
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstruction)))
        )

        return runCatching {
            val response = api.generateContent(model = MODEL, apiKey = apiKey, request = request)
            val content = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.extractJsonObject() ?: return null

            Log.d("ImageAI", "Gemini Image Response JSON: $content")

            val parsed: BarcodeAiResult? = moshi.adapter(BarcodeAiResult::class.java)
                .lenient()
                .fromJson(content)
                ?.let { result ->
                    result.copy(
                        codigo = "",
                        nombre = result.nombre.orEmpty(),
                        categoria = result.categoria.orEmpty(),
                        tipoControl = result.tipoControl.orEmpty().ifBlank { "DESCONOCIDO" },
                        requiereReceta = result.requiereReceta,
                        razon = result.razon.orEmpty(),
                        presentacionVentaDefault = result.presentacionVentaDefault.orEmpty(),
                        ventaFraccionadaPermitida = result.ventaFraccionadaPermitida,
                        confianzaPresentacion = result.confianzaPresentacion.coerceIn(0, 100),
                        razonPresentacion = result.razonPresentacion.orEmpty(),
                        presentacionesVentaSugeridasTexto = result.presentacionesVentaSugeridasTexto.orEmpty(),
                        confianzaPresentacionesVenta = result.confianzaPresentacionesVenta.coerceIn(0, 100),
                        razonPresentacionesVenta = result.razonPresentacionesVenta.orEmpty()
                    )
                }

            Log.d("ImageAI", "Parsed Image Result: $parsed")

            parsed?.let { ai ->
                Log.d(
                    "AI_PRESENTACIONES",
                    """
                ===== IA PRESENTACIONES IMAGEN =====
                Estado: ${ai.estado}
                Código: ${ai.codigo}
                Producto: ${ai.nombre}
                Categoría: ${ai.categoria}
                TipoControl: ${ai.tipoControl}
                Presentación default: ${ai.presentacionVentaDefault}
                Confianza default: ${ai.confianzaPresentacion}
                Fraccionada permitida: ${ai.ventaFraccionadaPermitida}
                Razón default: ${ai.razonPresentacion}
                Sugerencias venta: ${ai.presentacionesVentaSugeridasTexto}
                Confianza sugerencias: ${ai.confianzaPresentacionesVenta}
                Razón sugerencias: ${ai.razonPresentacionesVenta}
                ====================================
                """.trimIndent()
                )
            }
            parsed
        }.onFailure {
            if (it is CancellationException) throw it
            Log.e("ImageAI", "Error identificando producto por imagen con Gemini Vision", it)
        }.getOrNull()
    }

    private fun String.extractJsonObject(): String {
        val trimmed = trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        return if (start in 0..end) trimmed.substring(start, end + 1) else trimmed
    }

    suspend fun buscarProductoPorBarcodeDuckDuckGo(
        barcode: String,
        categoriasExistentes: List<String>
    ): BarcodeAiResult? {
        val cleanBarcode = barcode.trim()
        if (cleanBarcode.isBlank()) return null

        barcodeResultMemoryCache[cleanBarcode]?.let {
            Log.d("BarcodeDuckSearch", "Caché final hit: $cleanBarcode")
            return it
        }

        val resultados = duckBarcodeSearch.searchBarcode(cleanBarcode)

        Log.d(
            "BarcodeDuckSearch",
            """
            ===== WEB SEARCH DUCK FINAL =====
            Código: $cleanBarcode
            Resultados encontrados: ${resultados.size}
            ${resultados.joinToString("\n") { "- ${it.title} | ${it.link}" }}
            ================================
            """.trimIndent()
        )

        if (resultados.isEmpty()) return null

        val resultFromAi = runCatching {
            interpretarResultadosDuckConGemini(
                barcode = cleanBarcode,
                resultados = resultados,
                categoriasExistentes = categoriasExistentes
            )
        }.getOrElse {
            if (it is CancellationException) throw it
            null
        }

        if (resultFromAi != null) {
            barcodeResultMemoryCache[cleanBarcode] = resultFromAi
            return resultFromAi
        }

        // V20.5: Si Gemini falla o no responde, devolvemos provisional pero NO guardamos en caché
        // para permitir reintentos con IA en el futuro si la red mejora.
        return generarResultadoProvisional(cleanBarcode, resultados)
    }

    private fun generarResultadoProvisional(
        barcode: String,
        resultados: List<DuckBarcodeSearchItem>
    ): BarcodeAiResult {
        val primerResultado = resultados.firstOrNull()
        val nombreLimpio = primerResultado?.title?.substringBefore("|")?.trim() ?: "Producto Desconocido"

        return BarcodeAiResult(
            estado = "IDENTIFICADO",
            codigo = barcode,
            nombre = nombreLimpio,
            categoria = "Por confirmar",
            tipoControl = "DESCONOCIDO",
            requiereReceta = false,
            razon = "Resultado provisional basado en coincidencias web. Revisa y confirma antes de aplicar.",
            presentacionVentaDefault = "Envase",
            ventaFraccionadaPermitida = false,
            confianzaPresentacion = 0,
            razonPresentacion = "Extraído directamente del primer resultado web sin procesar por IA.",
            presentacionesVentaSugeridasTexto = "Envase=1 unidades(0)",
            confianzaPresentacionesVenta = 0,
            razonPresentacionesVenta = "",
            sugerenciaContenidoValor = null,
            sugerenciaContenidoUnidad = null
        )
    }

    private data class BarcodeWebCompactResult(
        val estado: String = "NO_IDENTIFICADO",
        val nombre: String = "",
        val categoria: String = "",
        val tipoControl: String = "DESCONOCIDO",
        val empaque: String = "",
        val contenidoNeto: String = "",
        val presentacionVenta: String = "",
        val valorIndividual: Double? = null,
        val unidadMedida: String? = null,
        val multiplicador: Int? = null
    )

    private suspend fun interpretarResultadosDuckConGemini(
        barcode: String,
        resultados: List<DuckBarcodeSearchItem>,
        categoriasExistentes: List<String>
    ): BarcodeAiResult? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) return null

        val resultadosTexto = resultados
            .take(4)
            .mapIndexed { index, item ->
                "${index + 1}. ${item.title}"
            }
            .joinToString("\n")

        val categoriasTexto = if (categoriasExistentes.isEmpty()) {
            "[]"
        } else {
            categoriasExistentes.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
        }

        val prompt = """
            Actúa como un experto en inventario de Retail y Farmacia. 
            Analiza estos resultados web para el código: "$barcode"

            Resultados:
            $resultadosTexto

            REGLAS CRÍTICAS PARA PRECISIÓN DEL 90%:
            1. IDENTIDAD: Usa el nombre con mayor consenso. Corrige errores ortográficos.
            2. TALLAS (MINIMARKET): "G", "S", "M" solas son tallas de pañales/ropa. IGNÓRALAS para peso. Solo acepta "g" o "gr" si hay un número pegado.
            3. FARMACIA: En concentraciones tipo "250mg/5ml", el contenido es el VOLUMEN TOTAL del frasco (ej: 60ml, 120ml). Los "mg" son concentración, NO contenido de inventario. Si no ves el volumen total, deja valorIndividual en null.
            4. PACKS: En "Pack 2 x 500ml", multiplicador=2, valorIndividual=500, unidadMedida=mL. Siempre busca el contenido de UNA sola unidad.
            5. PRECIOS: Ignora números que parezcan precios (ej. 45.00, 12.50) o que tengan símbolos de moneda.
            6. NORMALIZACIÓN: Unidades permitidas: [g, kg, mL, L, unidades].

            Categorías sugeridas:
            $categoriasTexto

            Devuelve SOLO JSON:
            {
              "estado": "IDENTIFICADO" | "NO_IDENTIFICADO",
              "nombre": "",
              "categoria": "",
              "tipoControl": "UNIDAD" | "PESO" | "LIQUIDO" | "DESCONOCIDO",
              "empaque": "Envase|Frasco|Caja...",
              "contenidoNeto": "ej: 500 mL",
              "presentacionVenta": "empaque=contenidoNeto",
              "valorIndividual": 500.0,
              "unidadMedida": "g|kg|mL|L|unidades",
              "multiplicador": 1
            }
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = prompt)
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.0,
                responseMimeType = "application/json"
            )
        )

        return runCatching {
            val response = api.generateContent(
                model = MODEL,
                apiKey = apiKey,
                request = request
            )

            val content = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?.extractJsonObject()
                ?: return@runCatching null

            Log.d("BarcodeDuckSearch", "Gemini interpreted Duck results: $content")

            val compact = moshi.adapter(BarcodeWebCompactResult::class.java)
                .lenient()
                .fromJson(content)
                ?: return@runCatching null

            if (compact.estado != "IDENTIFICADO" || compact.nombre.isBlank()) {
                return@runCatching null
            }

            // V31.8: Extracción con Heurística de Retail Blindada (90% Precisión)
            // Ya no usamos Regex ciegas en Kotlin. Confiamos en la interpretación semántica de Gemini
            // que ahora separa Valor, Unidad y Multiplicador basándose en las Reglas de Oro de Retail.
            
            val suggestedValue = compact.valorIndividual?.let {
                if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
            }
            val suggestedUnit = compact.unidadMedida
            val suggestedMultiplier = compact.multiplicador?.toString()

            BarcodeAiResult(
                estado = "IDENTIFICADO",
                codigo = barcode,
                nombre = compact.nombre,
                categoria = compact.categoria,
                tipoControl = compact.tipoControl.ifBlank { "DESCONOCIDO" },
                requiereReceta = false,
                razon = "Consenso web a partir de resultados de búsqueda.",
                presentacionVentaDefault = compact.empaque.ifBlank { "Envase" },
                ventaFraccionadaPermitida = false,
                confianzaPresentacion = 100,
                razonPresentacion = "",
                presentacionesVentaSugeridasTexto = compact.presentacionVenta.ifBlank {
                    val emp = compact.empaque.ifBlank { "Envase" }
                    val cont = compact.contenidoNeto.ifBlank { "1 unidades" }
                    "$emp=$cont(100)"
                }.let { value ->
                    if (value.contains("(")) value else "$value(100)"
                },
                confianzaPresentacionesVenta = 100,
                razonPresentacionesVenta = "",
                sugerenciaContenidoValor = suggestedValue,
                sugerenciaContenidoUnidad = suggestedUnit,
                sugerenciaMultiplicador = suggestedMultiplier
            )
        }.onFailure {
            if (it is CancellationException) throw it
            Log.e("BarcodeDuckSearch", "Error interpretando resultados DuckDuckGo con Gemini", it)
        }.getOrNull()
    }

    private fun normalizar(name: String): String {
        val sinAcentos = Normalizer
            .normalize(name, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase(Locale.ROOT)
            .trim()
        
        // V30.0: Usamos una sanitización simplificada para evitar dependencias circulares
        return sinAcentos.replace(Regex("[^a-z0-9_-]"), "_").replace(Regex("_+"), "_").trim('_')
    }

    private val cacheAsistencia = mutableMapOf<String, List<String>>()

    suspend fun asistenteManualLigero(
        productName: String? = null,
        partialCategory: String? = null,
        mercadoActivo: String = "Perú"
    ): List<String> {
        val pName = productName?.trim()?.lowercase() ?: ""
        val pCat = partialCategory?.trim()?.lowercase() ?: ""
        val key = "asist_${pName}_${pCat}"
        if (cacheAsistencia.containsKey(key)) return cacheAsistencia[key]!!

        val apiKey = BuildConfig.DEEPSEEK_API_KEY
        if (apiKey.isBlank()) return emptyList()

        val prompt = if (!productName.isNullOrBlank() && partialCategory.isNullOrBlank()) {
            "Devuelve ÚNICAMENTE la MEJOR categoría profesional (ej: Analgésicos, Antibióticos) for: $productName. Mercado: $mercadoActivo. JSON: {\"c\":[\"Categoría\"]}"
        } else {
            "Para el producto '$productName', completa la mejor categoría profesional que empiece con '$partialCategory'. JSON: {\"c\":[\"Categoría\"]}"
        }

        val request = DeepSeekChatRequest(
            model = DEEPSEEK_MODEL,
            messages = listOf(
                DeepSeekMessage("system", "Responde solo JSON con clave 'c' (array de strings)."),
                DeepSeekMessage("user", prompt)
            ),
            temperature = 0.0,
            responseFormat = DeepSeekResponseFormat("json_object")
        )

        return runCatching {
            val response = deepSeekApi.createChatCompletion("Bearer $apiKey", request)
            val rawJson = response.choices?.firstOrNull()?.message?.content ?: "{\"c\":[]}"
            val adapter = moshi.adapter(CategoryListAnswer::class.java).lenient()
            val result = adapter.fromJson(rawJson)?.c ?: emptyList()
            
            if (result.isNotEmpty()) cacheAsistencia[key] = result
            result
        }.getOrDefault(emptyList())
    }

    private data class CategoryListAnswer(@Json(name = "c") val c: List<String> = emptyList())

    suspend fun sugerirTipoControlManual(
        productName: String,
        category: String
    ): ManualTypeSuggestion? {
        val apiKey = BuildConfig.DEEPSEEK_API_KEY
        if (apiKey.isBlank()) return null

        val prompt = """
            Producto: "$productName"
            Categoría: "$category"
            Determina el tipo de control (UNIDAD, PESO, LIQUIDO), si requiere receta (r: true|false) y motivo.
            Responde ÚNICAMENTE JSON: {"t":"UNIDAD|PESO|LIQUIDO", "c":"ALTA|MEDIA|BAJA", "m":"motivo", "r":true|false, "rr":"motivo receta"}
        """.trimIndent()

        val request = DeepSeekChatRequest(
            model = DEEPSEEK_MODEL,
            messages = listOf(
                DeepSeekMessage("system", "Responde solo JSON."),
                DeepSeekMessage("user", prompt)
            ),
            temperature = 0.0,
            responseFormat = DeepSeekResponseFormat("json_object")
        )

        return runCatching {
            val response = deepSeekApi.createChatCompletion("Bearer $apiKey", request)
            val rawJson = response.choices?.firstOrNull()?.message?.content ?: return null
            Log.d("SilentAI", "Type JSON: $rawJson")

            val parsed = moshi.adapter(ManualTypeAnswer::class.java).lenient().fromJson(rawJson) ?: return null
            
            ManualTypeSuggestion(
                tipo = when (parsed.t.uppercase()) {
                    "PESO" -> TipoControlDetectado.PESO
                    "LIQUIDO" -> TipoControlDetectado.LIQUIDO
                    "UNIDAD" -> TipoControlDetectado.UNIDAD
                    else -> TipoControlDetectado.DESCONOCIDO
                },
                confianza = when (parsed.c.uppercase()) {
                    "ALTA" -> ConfianzaIA.ALTA
                    "MEDIA" -> ConfianzaIA.MEDIA
                    "BAJA" -> ConfianzaIA.BAJA
                    else -> ConfianzaIA.DESCONOCIDA
                },
                motivo = parsed.m,
                productName = productName,
                category = category,
                requiereReceta = parsed.r,
                razonReceta = parsed.rr
            )
        }.getOrNull()
    }

    suspend fun obtenerInformacionUsoProducto(productName: String): UsageInfoAiResult? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) return null

        val systemInstruction = "Eres un asistente farmacéutico experto. Proporciona información de uso."
        val prompt = "Producto: $productName. Devuelve usos, instrucciones y contraindicaciones."

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(temperature = 0.2, responseMimeType = "application/json"),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstruction)))
        )

        return runCatching {
            val response = api.generateContent(model = MODEL, apiKey = apiKey, request = request)
            val content = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?.extractJsonObject()
                ?: return null
            moshi.adapter(UsageInfoAiResult::class.java).lenient().fromJson(content)
        }.getOrNull()
    }

    private data class ManualTypeAnswer(
        @Json(name = "t") val t: String = "UNIDAD",
        @Json(name = "c") val c: String = "BAJA",
        @Json(name = "m") val m: String = "",
        @Json(name = "r") val r: Boolean? = null,
        @Json(name = "rr") val rr: String = ""
    )
}
