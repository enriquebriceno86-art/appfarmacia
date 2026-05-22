package com.app.administradorfarmadon.ActivityInventario.reference

import android.util.Log
import com.app.administradorfarmadon.ActivityInventario.ProductUtils
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Resuelve la categoría sugerida usando Gemini como fuente única.
 *
 * Decisión de diseño: **Gemini se consulta SIEMPRE** (no hay caché compartida
 * de respuestas). La única caché es la de proceso, para evitar reconsultas
 * mientras el usuario sigue tipeando el mismo nombre dentro de la sesión.
 *
 * Cuando el usuario acepta una sugerencia, se invoca `registrarDecisionAceptada`
 * que escribe la decisión en `Inventario/DecisionesCategoria/{key}` — eso sí
 * queda en Firebase, pero es un registro auditable, no una caché que afecte
 * las futuras sugerencias de otros usuarios.
 */
object CategorySuggestionRepository {

    private const val MODEL = "gemini-2.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    private const val PATH_DECISIONES = "Inventario/DecisionesCategoria"

    private const val DEEPSEEK_MODEL = "deepseek-chat"
    private const val DEEPSEEK_BASE_URL = "https://api.deepseek.com/"

    // V13.7: Estado de conexión "caliente"
    private var isGeminiWarmed = false
    private var isDeepSeekWarmed = false

    // Niveles de confianza (V3.23)

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

    /**
     * V13.7: Pre-calentamiento de conexión (Connection Warm-up).
     * Lanza peticiones HEAD o vacías para establecer el túnel SSL/TLS.
     * V13.8: Inicializa la caché persistente.
     */
    fun warmup(apiKey: String, dsApiKey: String) {
        if (!isGeminiWarmed && apiKey.isNotBlank()) {
            @Suppress("OPT_IN_USAGE")
            GlobalScope.launch(Dispatchers.IO) {
                runCatching {
                    // Petición mínima para abrir el socket
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
                    // DeepSeek no tiene getModel simple, usamos una petición de modelos
                    deepSeekApi.getModels("Bearer $dsApiKey")
                    isDeepSeekWarmed = true
                    Log.d("Warmup", "DeepSeek connection established.")
                }
            }
        }
    }

    /**
     * V17.0: IA Ultra-Ligera para Modo Manual Silencioso.
     * Solo devuelve la mejor categoría terapéutica o funcional.
     */
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
            
            // V17.0: Logcat para inspecci\u00f3n de categor\u00eda silenciosa
            Log.d("SilentAI", "Category JSON: $rawJson")
            
            val adapter = moshi.adapter(CategoryOnlyAnswer::class.java).lenient()
            adapter.fromJson(rawJson)?.c?.trim()
        }.getOrNull()
    }

    /**
     * Escanea la imagen de la etiqueta de un producto (caja, frasco, blister)
     * y devuelve el número de lote y/o el vencimiento detectados.
     *
     * Devuelve `null` si no hay API key, si Gemini falla, o si la imagen no
     * permite extraer ninguno de los dos campos. Si uno se detectó y el otro
     * no, los campos correspondientes vienen null individualmente.
     *
     * No lanza excepciones: el caller decide qué hacer con un resultado vacío.
     */
    suspend fun escanearEtiquetaConIA(imagenJpegBase64: String): EtiquetaDetectada? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || imagenJpegBase64.isBlank()) return null

        val prompt = """
            Eres un OCR especializado en etiquetas farmacéuticas de alta precisión.
            Te paso la fotografía de una caja o frasco de un producto.
            Extrae ÚNICAMENTE estos dos datos si los ves impresos:

            1) "loteNumero": el código alfanumérico del lote.
               - Suele aparecer junto a "LOTE", "LOT", "L.", "BATCH", "B/", "LOT:", "L:" o similar.
               - El código puede estar a la derecha de la etiqueta o en la LÍNEA SIGUIENTE.
               - Es típicamente una mezcla de letras y números (ej. "L240815B7", "23121", "BATCH 23A45", "2923-1").
               - Devuelve solo el código limpio. Si no es legible, devuelve "".

            2) "vencimientoMmAa": la fecha de caducidad en formato MM/AA.
               - Aparece como "EXP", "VENC", "VTO", "CAD", "USE BY", "VAL:", "EXP:".
               - Acepta y normaliza formatos de 2 o 3 segmentos a MM/AA:
                 - "27/08/2027" (Día/Mes/Año) -> "08/27"
                 - "12/01/25" (Día/Mes/Año) -> "01/25"
                 - "08/2027"  → "08/27"
                 - "2027 / 09" → "09/27"
                 - "AGO 2027" → "08/27"
               - Si ves 3 números (ej. 12/01/25), asume el formato regional Día/Mes/Año a menos que el primer número sea > 31.
               - Si no se ve o es ilegible, devuelve "".

            REGLA CRÍTICA: Prioriza la asociación visual. Si ves "Lot:" y debajo un número, ese es el lote. 
            Si ves "Exp:" y debajo una fecha, esa es la caducidad. Responde SOLO el JSON.
        """.trimIndent()

        val schema = GeminiSchema(
            type = "OBJECT",
            properties = mapOf(
                "loteNumero" to GeminiSchemaProperty("STRING"),
                "vencimientoMmAa" to GeminiSchemaProperty("STRING")
            ),
            required = listOf("loteNumero", "vencimientoMmAa")
        )

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(
                        GeminiPart(text = prompt),
                        GeminiPart(
                            inlineData = GeminiInlineData(
                                mimeType = "image/jpeg",
                                data = imagenJpegBase64
                            )
                        )
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.0,
                responseSchema = schema
            )
        )

        val response = runCatching {
            api.generateContent(MODEL, apiKey, request)
        }.getOrNull() ?: return null

        val rawJson = response.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?.trim()
            ?: return null

        val parsed = moshi.adapter(EtiquetaAnswer::class.java)
            .lenient()
            .fromJson(rawJson) ?: return null

        val lote = parsed.loteNumero.trim()
            .replace(Regex("\\s+"), "")
            .uppercase(Locale.ROOT)
            .ifBlank { null }
        val venc = parsed.vencimientoMmAa.trim()
            .let { normalizarVencimientoOcr(it) }

        if (lote == null && venc == null) return null
        return EtiquetaDetectada(loteNumero = lote, vencimientoMmAa = venc)
    }

    /**
     * Defensa adicional contra formatos que pueda devolver el LLM.
     * Si el texto no calza con MM/AA tras una normalización agresiva, 
     * devolvemos null para que la UI no lo inyecte como inválido.
     */
    private fun normalizarVencimientoOcr(raw: String): String? {
        val clean = raw.replace(Regex("[^0-9/]"), "").trim()
        if (clean.isBlank()) return null
        
        val parts = clean.split("/").filter { it.isNotBlank() }
        
        // Caso: DD/MM/YYYY o DD/MM/YY (ej: 12/01/2025 -> 01/25)
        if (parts.size >= 3) {
            val m = parts[1].padStart(2, '0')
            val y = parts.last().takeLast(2)
            return "$m/$y"
        }

        // Caso ideal: 09/27
        if (clean.matches(Regex("\\d{2}/\\d{2}"))) return clean
        
        // Caso: 09/2027 -> 09/27
        if (clean.matches(Regex("\\d{2}/\\d{4}"))) {
            return clean.substring(0, 3) + clean.substring(5, 7)
        }
        
        // Caso: 2027/09 -> 09/27
        if (clean.matches(Regex("\\d{4}/\\d{2}"))) {
            return clean.substring(5, 7) + "/" + clean.substring(2, 4)
        }

        return null
    }

    private data class EtiquetaAnswer(
        val loteNumero: String = "",
        val vencimientoMmAa: String = ""
    )

    /**
     * Guarda la decisión del usuario como evento auditable.
     *
     * No bloquea ni notifica al caller: si falla, el flujo del usuario
     * continúa intacto. La estructura permite que más adelante un dashboard
     * agregue estos eventos para medir precisión del LLM o detectar
     * categorías mal sugeridas con frecuencia.
     */
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

    suspend fun identificarProductoPorBarcode(
        barcode: String,
        imageBase64: String?,
        categoriasExistentes: List<String>
    ): BarcodeAiResult? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) return null

        val categoriasTexto = if (categoriasExistentes.isEmpty()) "[]"
        else categoriasExistentes.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }

        val systemInstruction = """
           Eres un identificador experto de productos por código de barras para una farmacia y abarrotes.
    
    INSTRUCCIONES:
    - Utiliza tu amplio conocimiento interno y la imagen de la etiqueta adjunta (si la hay) para identificar el producto al que pertenece el código de barras.
    - REGLA DE DOMINIO: Solo acepta productos que correspondan a farmacia, botica, suplementos, abarrotes, higiene, cuidado personal, bebés o limpieza.

    REGLAS DE SALIDA:
    - Responde estrictamente con la estructura solicitada.
    - estado: "IDENTIFICADO" si reconoces el producto. "NO_IDENTIFICADO" si pertenece a otro rubro o no existe.
    - nombre: Nombre comercial completo + marca + dosis/presentación (ej: Glicinato de Magnesio Member's Mark 180 Tabletas).
    - categoria: Una categoría lógica que describa el producto. Prioriza usar una de la lista si se te provee.
    - tipoControl: "UNIDAD" | "PESO" | "LIQUIDO" | "DESCONOCIDO".
    - requiereReceta: true | false.
    - razon: Explicación muy corta de qué identificaste.
""".trimIndent()

        val userPrompt = """
            Código detectado: "$barcode"
            Categorías existentes: $categoriasTexto
            Identifica el producto. Se adjunta imagen de la etiqueta si está disponible.
        """.trimIndent()

        val parts = mutableListOf<GeminiPart>()
        parts.add(GeminiPart(text = userPrompt))
        if (!imageBase64.isNullOrBlank()) {
            parts.add(GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = imageBase64)))
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
                "razon" to GeminiSchemaProperty("STRING")
            ),
            required = listOf("estado", "codigo", "nombre", "categoria", "tipoControl", "requiereReceta", "razon")
        )

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = parts)),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.0,
                responseMimeType = "application/json",
                responseSchema = barcodeSchema
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstruction))),
            tools = null // V18.4: Google Search desactivado para que la respuesta sea instantánea (< 1 seg)
        )

        return runCatching {
            val response = api.generateContent(model = MODEL, apiKey = apiKey, request = request)
            val content = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?.extractJsonObject()
                ?: return null
            
            // V18.2: Logcat del resultado IA por barcode (Gemini 2.5 Flash Vision)
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
                        razon = result.razon.orEmpty()
                    )
                }
            Log.d("BarcodeAI", "Parsed Result: $parsed")
            
            parsed
        }.onFailure {
            Log.e("BarcodeAI", "Error identificando barcode con Gemini Vision", it)
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

    private fun normalizar(name: String): String {
        val sinAcentos = Normalizer
            .normalize(name, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase(Locale.ROOT)
            .trim()
        return ProductUtils.sanitizarTexto(sinAcentos)
    }

    private data class CategoryOnlyAnswer(
        @Json(name = "c") val c: String = ""
    )

    /**
     * V16.0: Consulta ligera para asistencia en modo manual.
     * Solo devuelve categorías lógicas basadas en el nombre del producto o fragmento de categoría.
     */
    // V16.4: Cach\u00e9 de sesi\u00f3n para respuesta instant\u00e1nea (0ms)
    private val cacheAsistencia = mutableMapOf<String, List<String>>()

    suspend fun asistenteManualLigero(
        productName: String? = null,
        partialCategory: String? = null,
        mercadoActivo: String = "Per\u00fa"
    ): List<String> {
        val pName = productName?.trim()?.lowercase() ?: ""
        val pCat = partialCategory?.trim()?.lowercase() ?: ""
        val key = "asist_${pName}_${pCat}"
        if (cacheAsistencia.containsKey(key)) return cacheAsistencia[key]!!

        val apiKey = BuildConfig.DEEPSEEK_API_KEY
        if (apiKey.isBlank()) return emptyList()

        val prompt = if (!productName.isNullOrBlank() && partialCategory.isNullOrBlank()) {
            "Devuelve ÚNICAMENTE la MEJOR categoría profesional (ej: Analgésicos, Antibióticos) para: $productName. Mercado: $mercadoActivo. PROHIBIDO: términos vagos como 'jarabes', 'pastillas', 'venta libre', 'productos médicos'. Solo taxonomía terapéutica o funcional. JSON: {\"c\":[\"Categoría\"]}"
        } else {
            "Para el producto '$productName', completa la mejor categoría profesional que empiece con '$partialCategory'. PROHIBIDO: presentaciones o términos vagos. JSON: {\"c\":[\"Categoría\"]}"
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
            // Usamos DeepSeek (deepseek-chat) con cach\u00e9 de sesi\u00f3n para mitigar latencia regional
            val response = deepSeekApi.createChatCompletion("Bearer $apiKey", request)
            val rawJson = response.choices?.firstOrNull()?.message?.content ?: "{\"c\":[]}"
            val adapter = moshi.adapter(CategoryListAnswer::class.java).lenient()
            val result = adapter.fromJson(rawJson)?.c ?: emptyList()
            
            if (result.isNotEmpty()) cacheAsistencia[key] = result
            result
        }.getOrDefault(emptyList())
    }

    private data class CategoryListAnswer(@Json(name = "c") val c: List<String> = emptyList())

    /**
     * V16.10: Sugiere el tipo de control (UNIDAD, PESO, LIQUIDO) basado en nombre y categoría.
     * Diseñado para latencia mínima usando DeepSeek-chat.
     */
    suspend fun sugerirTipoControlManual(
        productName: String,
        category: String
    ): ManualTypeSuggestion? {
        val apiKey = BuildConfig.DEEPSEEK_API_KEY
        if (apiKey.isBlank()) return null

        val prompt = """
            Producto: "$productName"
            Categoría: "$category"

            Determina el tipo de control de inventario:
            - UNIDAD: se cuenta por piezas (pastillas, cajas, sobres, tubos).
            - PESO: se mide por gramos/kilos (polvos a granel).
            - LIQUIDO: se mide por ml/litros (frascos de jarabe, gotas).
            
            Determina también si normalmente requiere receta médica:
            - r=true si es medicamento de venta bajo receta.
            - r=false si es suplemento, alimento, bebida, cosmético o venta libre.
            - Si dudas, usa r=false y explica brevemente.

            Confianza:
            - ALTA: 100% seguro del tipo por el nombre o categoría específica.
            - MEDIA: Probable, pero podría ser otro (ej: polvos que se venden por sobre o por peso).
            - BAJA: Ambiguo.

            Responde ÚNICAMENTE JSON: {"t":"UNIDAD|PESO|LIQUIDO", "c":"ALTA|MEDIA|BAJA", "m":"motivo breve", "r":true|false, "rr":"motivo receta breve"}
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
            
            // V17.0: Logcat para inspecci\u00f3n de tipo silencioso
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

    /**
     * V20.0: Obtiene información útil (usos, instrucciones, contraindicaciones)
     * del producto usando Gemini.
     */
    suspend fun obtenerInformacionUsoProducto(productName: String): UsageInfoAiResult? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) return null

        val systemInstruction = """
            Eres un asistente farmacéutico experto. Proporciona información útil y segura sobre productos.
            
            REGLAS:
            - Devuelve 4 usos comunes cortos (ej: "Dolor de cabeza", "Fiebre").
            - Proporciona instrucciones de uso típicas (ej: "Tomar 1 tableta cada 8 horas").
            - Proporciona contraindicaciones críticas (ej: "No usar en embarazo", "No mezclar con alcohol").
            - Sé profesional y conciso.
        """.trimIndent()

        val prompt = "Producto: $productName. Devuelve la información de uso."

        val usageSchema = GeminiSchema(
            type = "OBJECT",
            properties = mapOf(
                "usos" to GeminiSchemaProperty(
                    type = "ARRAY",
                    items = GeminiSchemaProperty(type = "STRING")
                ),
                "instrucciones" to GeminiSchemaProperty("STRING"),
                "contraindicaciones" to GeminiSchemaProperty("STRING")
            ),
            required = listOf("usos", "instrucciones", "contraindicaciones")
        )

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.2,
                responseMimeType = "application/json",
                responseSchema = usageSchema
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstruction)))
        )

        return runCatching {
            val response = api.generateContent(model = MODEL, apiKey = apiKey, request = request)
            val content = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?.extractJsonObject()
                ?: return null
            
            Log.d("UsageAI", "Gemini Usage Response: $content")
            
            moshi.adapter(UsageInfoAiResult::class.java)
                .lenient()
                .fromJson(content)
        }.onFailure {
            Log.e("UsageAI", "Error obteniendo información de uso con Gemini", it)
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
