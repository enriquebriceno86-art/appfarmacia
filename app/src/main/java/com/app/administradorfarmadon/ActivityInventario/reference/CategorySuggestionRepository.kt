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
    private var usarDeepSeek = true // V7.0: Cambiado a true para priorizar prueba
    
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
            android.util.Log.d("SilentAI", "Category JSON: $rawJson")
            
            val adapter = moshi.adapter(CategoryOnlyAnswer::class.java).lenient()
            adapter.fromJson(rawJson)?.c?.trim()
        }.getOrNull()
    }

    /**
     * V7.8: Salvador de último recurso. Si la IA falla, usamos lógica local
     * para no dejar al usuario sin ninguna ayuda visual.
     */

    private fun construirNombreConUnidadProbable(
        baseName: String,
        amountText: String,
        tipoControl: TipoControlDetectado
    ): String? {
        val limpio = baseName.trim().ifBlank { return null }
        val amount = amountText.trim().replace(",", ".")
        val unit = when (tipoControl) {
            TipoControlDetectado.PESO -> {
                val value = amount.toDoubleOrNull()
                if (value != null && value in 1.0..10.0) "kg" else "g"
            }
            TipoControlDetectado.LIQUIDO -> {
                val value = amount.toDoubleOrNull()
                if (value != null && value in 1.0..5.0) "L" else "mL"
            }
            TipoControlDetectado.UNIDAD -> "mg"
            TipoControlDetectado.DESCONOCIDO -> null
        } ?: return null
        return "$limpio $amount $unit".replace(Regex("\\s+"), " ").trim()
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
            Eres un OCR especializado en etiquetas farmacéuticas.
            Te paso la fotografía de una caja o frasco de un producto.
            Extrae ÚNICAMENTE estos dos datos si los ves impresos:

            1) "loteNumero": el código alfanumérico del lote.
               Suele aparecer junto a "LOTE", "LOT", "L.", "BATCH" o similar.
               Es típicamente una mezcla de letras y números (ej. "L240815B7",
               "BATCH 23A45", "LOT P0921"). Devuelve solo el código limpio,
               sin la palabra "lote". Si no es legible o no aparece, devuelve "".

            2) "vencimientoMmAa": la fecha de caducidad en formato MM/AA.
               Aparece como "EXP", "VENC", "VTO", "CAD", "USE BY".
               Acepta cualquiera de estos formatos en la etiqueta y devuélvelos
               normalizados a MM/AA:
                 - "08/2027"  → "08/27"
                 - "AGO 2027" → "08/27"
                 - "08-27"    → "08/27"
                 - "2027-08"  → "08/27"
                 - "27/08/2027" (día/mes/año) → "08/27"
               Si no se ve o es ilegible, devuelve "".

            NO inventes datos. Si la foto está borrosa o el dato no está visible,
            devuelve cadena vacía para ese campo. Responde SOLO el JSON.
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
     * Defensa adicional contra formatos raros que pueda devolver el LLM.
     * Si el texto no calza con MM/AA tras normalizar, devolvemos null para
     * que la UI no lo inyecte como inválido.
     */
    private fun normalizarVencimientoOcr(raw: String): String? {
        if (raw.isBlank()) return null
        val solo = raw.replace(Regex("[^0-9/]"), "")
        if (solo.matches(Regex("\\d{2}/\\d{2}"))) return solo
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
           Eres un identificador experto de productos por código de barras para una aplicación de farmacia y abarrotes.
    
    INSTRUCCIONES DE BÚSQUEDA (Google Search):
    - Utiliza los resultados de la herramienta de búsqueda para encontrar el nombre comercial real, marca y presentación del código de barras provisto.
    - REGLA DE DOMINIO: Solo acepta productos que correspondan a farmacia, botica, suplementos, abarrotes, higiene, cuidado personal, bebés o limpieza.
    - Si la búsqueda arroja categorías totalmente ajenas (como muebles, electrónica, herramientas, ropa), considera que el código está mal indexado en la web y responde "estado": "NO_IDENTIFICADO".

    REGLAS DE SALIDA:
    - Responde ÚNICAMENTE con un JSON válido, sin texto extra ni formato markdown.
    - estado: "IDENTIFICADO" si los resultados de búsqueda web o la imagen muestran coincidencia clara con un producto del dominio permitido. "NO_IDENTIFICADO" si no hay resultados o pertenecen a otro rubro.
    - nombre: Nombre comercial completo + marca + dosis/presentación (ej: Glicinato de Magnesio Member's Mark 180 Tabletas).
    - categoria: Una categoría lógica que describa el producto. Prioriza usar una de la lista si se te provee.
    - tipoControl: "UNIDAD" | "PESO" | "LIQUIDO" | "DESCONOCIDO".
    - requiereReceta: true | false (los suplementos vitamínicos o abarrotes son false).
    - razon: Explicación muy corta del resultado o del porqué se descartó.
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
            required = listOf(
                "estado",
                "codigo",
                "nombre",
                "categoria",
                "tipoControl",
                "requiereReceta",
                "razon"
            )
        )

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = parts)),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.0,
                responseMimeType = "application/json",
                responseSchema = barcodeSchema
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstruction))),
            tools = null
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
        return if (start >= 0 && end >= start) trimmed.substring(start, end + 1) else trimmed
    }

    private suspend fun consultarDeepSeekRapido(
        productName: String,
        categoriasExistentes: List<String>,
        mercadoActivo: String
    ): CategorySuggestion? {
        val apiKey = BuildConfig.DEEPSEEK_API_KEY

        if (apiKey.isBlank()) {
            Log.w("CategoryAI", "DeepSeek API Key no configurada")
            return null
        }
        
        Log.d("CategoryAI", "Consultando DeepSeek para: $productName")

        val categoriasTexto = if (categoriasExistentes.isEmpty()) "[]"
        else categoriasExistentes.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }


        val systemPrompt = """
            Eres un clasificador de productos para inventario de farmacia y retail.
            Ignora si buscamos  de mayuscula a minuscula y viceversa 
            Responde SOLO JSON valido con claves ep,n,c,t,r,z,s,b,q,m,os,pc.
            Estados: VALIDO claro y aplicable; CORREGIBLE typo/formato o falta confirmar presentacion; CONTRADICTORIO mezcla productos; DESCONOCIDO muy vago.
            pc=true solo si no falta confirmar nada. n=null si falta confirmar presentacion.
            No uses categorias genericas como Medicamentos, Productos, Varios u Otros.
            Farmaco: categoria terapeutica especifica. Retail: categoria funcional especifica.
            Regla critica: retail/bebida/alimento con numero SIN unidad no puede ser VALIDO, no puede pc=true y no puede inventar unidad en n.
            Para numero sin unidad usa ep=CORREGIBLE,n=null,pc=false,q=ESPECIFICO,s="Confirma la presentacion antes de continuar.",m="La cantidad no tiene unidad.",b/os=opciones posibles.
            Prohibido para numero sin unidad: "Coca Cola 25 cl", "Coca Cola 250 ml", "Agua San Luis 1.5 L" como VALIDO.
            Ejemplo: entrada "coca cola 25" => {"ep":"CORREGIBLE","n":null,"c":"Gaseosas","t":"LIQUIDO","r":false,"z":"Producto identificado, pero la presentacion esta incompleta.","s":"Confirma la presentacion antes de continuar.","b":["Coca Cola 2.5 L","Coca Cola 250 ml","Coca Cola 355 ml"],"q":"ESPECIFICO","m":"La cantidad no tiene unidad.","os":["Coca Cola 2.5 L","Coca Cola 250 ml","Coca Cola 355 ml"],"pc":false}
        """.trimIndent()

        /*
        val systemPromptVerbose = """
            CONTRATO ESTRICTO DE RESPUESTA:
            Responde SOLO JSON valido con claves ep,n,c,t,r,z,s,b,q,m,os,pc.
            ep debe ser uno de: VALIDO, CORREGIBLE, CONTRADICTORIO, DESCONOCIDO.
            pc=true solo si el usuario puede aplicar sin confirmar nada mas.
            n debe ser null si falta confirmar una presentacion o si hay ambiguedad.

            REGLA CRITICA:
            Si el producto es retail/bebida/alimento y el usuario escribio un numero SIN unidad,
            NO inventes unidad en n, NO devuelvas VALIDO y NO marques pc=true.
            Ejemplos de numero sin unidad: "coca cola 25", "agua san luis 15", "pepsi 3".
            En ese caso responde exactamente con la intencion:
            ep="CORREGIBLE", n=null, pc=false, q="ESPECIFICO",
            s="Confirma la presentacion antes de continuar.",
            m="La cantidad no tiene unidad.",
            b y os con opciones posibles.

            PROHIBIDO para numero sin unidad:
            - "Coca Cola 25 cl" como VALIDO
            - "Coca Cola 250 ml" como VALIDO
            - "Agua San Luis 1.5 L" como VALIDO
            - cualquier unidad inventada en n

            EJEMPLOS OBLIGATORIOS:
            Entrada: coca cola 25
            Salida: {"ep":"CORREGIBLE","n":null,"c":"Gaseosas","t":"LIQUIDO","r":false,"z":"Producto identificado, pero la presentacion esta incompleta.","s":"Confirma la presentacion antes de continuar.","b":["Coca Cola 2.5 L","Coca Cola 250 ml","Coca Cola 355 ml"],"q":"ESPECIFICO","m":"La cantidad no tiene unidad.","os":["Coca Cola 2.5 L","Coca Cola 250 ml","Coca Cola 355 ml"],"pc":false}

            Entrada: coca cola 2.5l
            Salida: {"ep":"VALIDO","n":"Coca Cola 2.5 L","c":"Gaseosas","t":"LIQUIDO","r":false,"z":"Producto claro con presentacion especifica.","s":"","b":[],"q":"ESPECIFICO","m":"","os":[],"pc":true}

            Entrada: ibuprofeno coca cola
            Salida: {"ep":"CONTRADICTORIO","n":null,"c":"","t":"DESCONOCIDO","r":false,"z":"La entrada mezcla productos incompatibles.","s":"Elige un solo producto para continuar.","b":["Ibuprofeno 400 mg","Coca Cola"],"q":"AMBIGUO","m":"Mezcla productos distintos.","os":["Ibuprofeno 400 mg","Coca Cola"],"pc":false}

            Eres un clasificador de productos para inventario de farmacia y retail.
            Responde únicamente JSON válido con claves ep,n,c,t,r,z,s,b,q,m,os,pc.

            Contrato:
            - VALIDO solo si el producto está claro y no falta unidad crítica.
            - CORREGIBLE si hay error menor o falta confirmar presentación.
            - CONTRADICTORIO si mezcla productos incompatibles.
            - DESCONOCIDO si es muy vago.
            - Si retail/bebida/alimento tiene número sin unidad, no inventes unidad.
            - En ese caso: ep=CORREGIBLE, n=null, pc=false, q=ESPECIFICO, b/os con opciones posibles.
            - No devuelvas como VALIDO: Coca Cola 25 cl, Coca Cola 250 ml, Agua San Luis 1.5 L si el usuario no escribió unidad.
            - No uses categorías genéricas como Medicamentos, Varios o Productos.
            - Si es fármaco, usa categoría terapéutica específica.
            - Si es retail, usa categoría funcional específica.
        """.trimIndent()
        */

        val userPrompt = """
            Mercado: "$mercadoActivo"
            Categorías existentes: $categoriasTexto
            Producto original: "$productName"
        """.trimIndent()

        val request = DeepSeekChatRequest(
            model = DEEPSEEK_MODEL,
            messages = listOf(
                DeepSeekMessage("system", systemPrompt),
                DeepSeekMessage("user", userPrompt)
            ),
            temperature = 0.0,
            responseFormat = DeepSeekResponseFormat("json_object")
        )

        return runCatching {
            val response = deepSeekApi.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            val content = response.choices?.firstOrNull()?.message?.content?.trim() ?: return null
            
            // V13.9: Logcat del JSON crudo de DeepSeek
            Log.d("CategoryAI", "DeepSeek Response JSON: $content")

            val parsed = moshi.adapter(StructuredQuickAnswer::class.java).lenient().fromJson(content) ?: return null

            val estadoProductoIa = parseEstadoProducto(parsed.ep)
            val canUseEmptyCategory = estadoProductoIa == EstadoProducto.CONTRADICTORIO ||
                estadoProductoIa == EstadoProducto.DESCONOCIDO
            val parsedCategoria = parsed.c.orEmpty()
            if (parsedCategoria.isBlank() && !canUseEmptyCategory) return null

            val mapped = CategorySuggestion(
                productName = productName,
                categoria = parsedCategoria,
                emoji = when (parsed.t.uppercase()) {
                    "LIQUIDO" -> "🧪"
                    "PESO" -> "⚖️"
                    else -> "💊"
                },
                tipoControl = when (parsed.t.uppercase()) {
                    "UNIDAD" -> TipoControlDetectado.UNIDAD
                    "LIQUIDO" -> TipoControlDetectado.LIQUIDO
                    "PESO" -> TipoControlDetectado.PESO
                    else -> TipoControlDetectado.DESCONOCIDO
                },
                razonTipo = parsed.z,
                requiereReceta = parsed.r,
                razonReceta = if (parsed.r) parsed.z else "",
                modoIngreso = ModoIngresoDetectado.DESCONOCIDO,
                presentacionesSugeridas = emptyList(),
                keywords = emptyList(),
                existeEnLista = false,
                confianza = 80,
                razon = parsed.z,
                isRefined = true,
                tipoConsulta = when(parsed.q.uppercase()) {
                    "GENERAL" -> TipoConsultaDetectada.GENERAL
                    "AMBIGUO" -> TipoConsultaDetectada.AMBIGUO
                    "INSUFICIENTE" -> TipoConsultaDetectada.INSUFICIENTE
                    else -> TipoConsultaDetectada.ESPECIFICO
                },
                nombreCorregido = parsed.n,
                sugerenciaUsuario = parsed.s,
                sugerenciasBusqueda = parsed.b.orEmpty(),
                estadoProducto = when(parsed.ep.uppercase()) {
                    "CORREGIBLE" -> EstadoProducto.CORREGIBLE
                    "CONTRADICTORIO" -> EstadoProducto.CONTRADICTORIO
                    "DESCONOCIDO" -> EstadoProducto.DESCONOCIDO
                    else -> EstadoProducto.VALIDO
                },
                opcionesSeparadas = parsed.os.orEmpty(),
                puedeContinuar = parsed.pc,
                motivoValidacion = parsed.m
            )

            val correctionFromAlternatives = seleccionarCorreccionDesdeAlternativas(productName, mapped.nombreCorregido, mapped.sugerenciasBusqueda)
            val rawNombreCorregido = correctionFromAlternatives
                ?: mapped.nombreCorregido
                ?.takeIf { esCorreccionSemantica(productName, it) }
                ?: corregirUnidadFarmaceuticaEvidente(productName, mapped.categoria)
            val safeNombreCorregido = rawNombreCorregido?.takeIf {
                CategorySuggestionViewModel.SafetyValidator.validate(productName, it, CategorySuggestionViewModel.SafetyMode.STRICT)
            }
            val categoriaFinal = if (mapped.categoria.isBlank() && canUseEmptyCategory) {
                ""
            } else {
                resolverCategoriaEspecificaSiHaceFalta(
                    productName = productName,
                    nombreCorregido = safeNombreCorregido ?: mapped.nombreCorregido,
                    categoriaIa = mapped.categoria,
                    categoriasExistentes = categoriasExistentes,
                    mercadoActivo = mercadoActivo
                ) ?: return null
            }

            val safeChips = mapped.sugerenciasBusqueda.filter { chip ->
                CategorySuggestionViewModel.SafetyValidator.validate(productName, chip, CategorySuggestionViewModel.SafetyMode.RELAXED)
            }
            val opcionesAclaracion = if (safeNombreCorregido == null && parseEstadoProducto(parsed.ep) == EstadoProducto.CORREGIBLE) {
                (mapped.opcionesSeparadas + mapped.sugerenciasBusqueda).distinct()
            } else {
                mapped.opcionesSeparadas
            }

            mapped.copy(
                categoria = categoriaFinal,
                tipoConsulta = resolverTipoConsultaFinal(
                    productName = productName,
                    nombreCorregido = safeNombreCorregido ?: mapped.nombreCorregido,
                    categoria = categoriaFinal,
                    tipoConsultaIa = parsed.q
                ),
                nombreCorregido = safeNombreCorregido,
                sugerenciasBusqueda = safeChips,
                estadoProducto = resolverEstadoProductoFinal(parsed.ep, safeNombreCorregido),
                opcionesSeparadas = opcionesAclaracion,
                puedeContinuar = mapped.puedeContinuar && opcionesAclaracion.isEmpty(),
                motivoValidacion = mapped.motivoValidacion ?: if (correctionFromAlternatives != null) {
                    "Encontramos una alternativa más clara para este nombre."
                } else null
            ).aplicarRevisionDeConflictos(productName)
        }.getOrNull()
    }


    private suspend fun resolverCategoriaEspecificaSiHaceFalta(
        productName: String,
        nombreCorregido: String?,
        categoriaIa: String,
        categoriasExistentes: List<String>,
        mercadoActivo: String
    ): String? {
        val categoria = categoriaIa.trim()
        if (categoria.isBlank()) return null
        if (!esCategoriaGenerica(categoria)) return categoria

        Log.w("CategoryAI", "Categoría genérica bloqueada: $categoria. Reintentando categoría específica.")
        val retry = consultarDeepSeekCategoriaEspecifica(
            productName = productName,
            nombreCorregido = nombreCorregido,
            categoriaGenerica = categoria,
            categoriasExistentes = categoriasExistentes,
            mercadoActivo = mercadoActivo
        )?.trim().orEmpty()

        return retry.takeIf { it.isNotBlank() && !esCategoriaGenerica(it) }
    }

    private fun esCategoriaGenerica(categoria: String): Boolean {
        val normalized = normalizar(categoria)
        return normalized in setOf(
            "medicamento",
            "medicamentos",
            "farmacia",
            "salud",
            "producto",
            "productos",
            "varios",
            "otros",
            "general",
            "miscelaneos",
            "sin categoria"
        )
    }


    private fun resolverEstadoProductoFinal(epIa: String, nombreCorregido: String?): EstadoProducto {
        val estadoIa = parseEstadoProducto(epIa)
        return when (estadoIa) {
            EstadoProducto.VALIDO -> if (!nombreCorregido.isNullOrBlank()) EstadoProducto.CORREGIBLE else EstadoProducto.VALIDO
            else -> estadoIa
        }
    }

    private fun parseEstadoProducto(epIa: String): EstadoProducto {
        return when (epIa.trim().uppercase(Locale.ROOT)) {
            "CONTRADICTORIO" -> EstadoProducto.CONTRADICTORIO
            "DESCONOCIDO" -> EstadoProducto.DESCONOCIDO
            "CORREGIBLE" -> EstadoProducto.CORREGIBLE
            else -> EstadoProducto.VALIDO
        }
    }

    private fun esCorreccionSemantica(original: String, suggested: String): Boolean {
        return normalizarIdentidadCorreccion(original) != normalizarIdentidadCorreccion(suggested) &&
            !agregaDetalleComercial(original, suggested) &&
            !cambiaDescriptorSensible(original, suggested)
    }

    private fun normalizarIdentidadCorreccion(value: String): String {
        return normalizar(value)
            .replace(Regex("""(\d+(?:[.,]\d+)?)(mg|mcg|g|kg|ml|l|ui|iu)\b"""), "$1 $2")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    private fun seleccionarCorreccionDesdeAlternativas(
        original: String,
        nombreCorregidoIa: String?,
        alternativas: List<String>
    ): String? {
        val originalNorm = normalizar(original)
        val nombreCorregidoNorm = nombreCorregidoIa?.let { normalizar(it) }
        val originalTokens = originalNorm.split(Regex("\\s+")).filter { it.isNotBlank() }
        val originalNumbers = Regex("""\d+(?:[.,]\d+)?""").findAll(originalNorm).map { it.value }.toList()

        return alternativas
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { normalizar(it) == originalNorm }
            .filterNot { nombreCorregidoNorm != null && normalizar(it) == nombreCorregidoNorm }
            .filter { candidate ->
                val candidateNorm = normalizar(candidate)
                val candidateNumbers = Regex("""\d+(?:[.,]\d+)?""").findAll(candidateNorm).map { it.value }.toList()
                    candidateNumbers == originalNumbers &&
                    comparteTokenSimilar(originalTokens, candidateNorm) &&
                    !agregaDetalleComercial(original, candidate)
            }
            .maxByOrNull { scoreCorreccion(originalNorm, it) }
    }

    private fun agregaDetalleComercial(original: String, candidate: String): Boolean {
        val originalTokens = normalizar(original).split(Regex("\\s+")).toSet()
        val candidateTokens = normalizar(candidate).split(Regex("\\s+")).toSet()
        return detallesComercialesCorreccion.any { detail ->
            detail !in originalTokens && detail in candidateTokens
        }
    }

    private fun cambiaDescriptorSensible(original: String, candidate: String): Boolean {
        val originalTokens = normalizar(original).split(Regex("\\s+")).filter { it.isNotBlank() }.toSet()
        val candidateTokens = normalizar(candidate).split(Regex("\\s+")).filter { it.isNotBlank() }.toSet()

        return gruposDescriptoresSensibles.any { group ->
            val originalDescriptors = originalTokens.intersect(group)
            val candidateDescriptors = candidateTokens.intersect(group)
            originalDescriptors.isNotEmpty() &&
                candidateDescriptors.isNotEmpty() &&
                originalDescriptors != candidateDescriptors
        }
    }

    private fun comparteTokenSimilar(originalTokens: List<String>, candidateNorm: String): Boolean {
        val candidateTokens = candidateNorm.split(Regex("\\s+")).filter { it.isNotBlank() }
        return originalTokens
            .filterNot { it.toDoubleOrNull() != null }
            .filterNot { it in unidadesMedidaConsulta }
            .any { original ->
                candidateTokens.any { candidate ->
                    candidate.length >= 4 &&
                        (candidate.startsWith(original.take(4)) || levenshtein(original, candidate) <= 3)
                }
            }
    }

    private fun scoreCorreccion(originalNorm: String, candidate: String): Int {
        val candidateNorm = normalizar(candidate)
        val hasSeparatedUnit = Regex("""\b\d+(?:[.,]\d+)?\s+(mg|mcg|g|kg|ml|l|ui|iu)\b""").containsMatchIn(candidateNorm)
        val distancePenalty = levenshtein(originalNorm, candidateNorm)
        return (if (hasSeparatedUnit) 20 else 0) - distancePenalty
    }

    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        var previous = IntArray(b.length + 1) { it }
        var current = IntArray(b.length + 1)

        for (i in a.indices) {
            current[0] = i + 1
            for (j in b.indices) {
                val cost = if (a[i] == b[j]) 0 else 1
                current[j + 1] = minOf(
                    current[j] + 1,
                    previous[j + 1] + 1,
                    previous[j] + cost
                )
            }
            val swap = previous
            previous = current
            current = swap
        }

        return previous[b.length]
    }

    private fun resolverTipoConsultaFinal(
        productName: String,
        nombreCorregido: String?,
        categoria: String,
        tipoConsultaIa: String
    ): TipoConsultaDetectada {
        val tipoIa = when (tipoConsultaIa.trim().uppercase(Locale.ROOT)) {
            "ESPECIFICO" -> TipoConsultaDetectada.ESPECIFICO
            "AMBIGUO" -> TipoConsultaDetectada.AMBIGUO
            "INSUFICIENTE" -> TipoConsultaDetectada.INSUFICIENTE
            else -> TipoConsultaDetectada.GENERAL
        }
        if (tipoIa != TipoConsultaDetectada.GENERAL) return tipoIa
        if (esCategoriaGenerica(categoria)) return TipoConsultaDetectada.GENERAL

        val nombreBase = nombreCorregido
            ?.takeIf { it.isNotBlank() }
            ?: productName

        return if (pareceProductoBaseIdentificable(nombreBase)) {
            Log.d("CategoryAI", "TipoConsulta ajustado a ESPECIFICO por producto base identificable: $nombreBase")
            TipoConsultaDetectada.ESPECIFICO
        } else {
            TipoConsultaDetectada.GENERAL
        }
    }

    private fun CategorySuggestion.aplicarRevisionDeConflictos(productName: String): CategorySuggestion {
        if (debeForzarDesconocido(productName)) {
            Log.w("CategoryAI", "Sugerencia marcada como DESCONOCIDA por entrada genérica: $productName")
            return copy(
                estadoProducto = EstadoProducto.DESCONOCIDO,
                tipoConsulta = TipoConsultaDetectada.INSUFICIENTE,
                puedeContinuar = false,
                confianza = minOf(confianza, 40),
                categoria = "",
                sugerenciaUsuario = sugerenciaUsuario
                    ?: "Especifica el nombre comercial, principio activo o presentación del producto.",
                razon = motivoValidacion
                    ?: razon.ifBlank { "La búsqueda no identifica un producto concreto." },
                opcionesSeparadas = if (opcionesSeparadas.isNotEmpty()) opcionesSeparadas else sugerenciasBusqueda,
                nombreCorregido = null
            )
        }

        val conflictReason = detectarConflictoEntrada(productName, categoria)
        if (conflictReason == null) return this

        Log.w("CategoryAI", "Sugerencia marcada como AMBIGUA: $conflictReason")
        return copy(
            tipoConsulta = TipoConsultaDetectada.AMBIGUO,
            confianza = minOf(confianza, 45),
            sugerenciaUsuario = "El nombre mezcla señales incompatibles. Revisa si estás creando un medicamento, bebida, alimento o producto de cuidado.",
            razon = conflictReason,
            sugerenciasBusqueda = emptyList()
        )
    }

    private fun CategorySuggestion.debeForzarDesconocido(productName: String): Boolean {
        if (estadoProducto == EstadoProducto.CONTRADICTORIO || estadoProducto == EstadoProducto.DESCONOCIDO) {
            return false
        }

        val normalizedQuery = normalizar(productName)
        val tokens = normalizedQuery.split(Regex("\\s+")).filter { it.isNotBlank() }
        val hasOnlyGenericIntent = tokens.isNotEmpty() && tokens.all { token ->
            token in tokensGenericosConsulta || token in symptomIntentTokens
        }
        val aiSaysGeneral = tipoConsulta == TipoConsultaDetectada.GENERAL ||
            tipoConsulta == TipoConsultaDetectada.INSUFICIENTE

        return aiSaysGeneral || hasOnlyGenericIntent
    }

    private fun detectarConflictoEntrada(productName: String, categoria: String): String? {
        val text = normalizar(productName)
        val tokens = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        val categoriaNorm = normalizar(categoria)

        val hasPharmaToken = tokens.any { it in pharmaSignalTokens } ||
            Regex("""\b\d+([.,]\d+)?\s*(mg|mcg|ui|iu|%)\b""", RegexOption.IGNORE_CASE).containsMatchIn(text)
        val hasRetailToken = tokens.any { it in retailSignalTokens } ||
            Regex("""\b\d+([.,]\d+)?\s*(ml|l|lt|kg)\b""", RegexOption.IGNORE_CASE).containsMatchIn(text)
        val hasPersonalCareToken = tokens.any { it in personalCareSignalTokens }
        val hasPharmaCategory = categoriaNorm in pharmaCategoryTokens
        val hasRetailCategory = categoriaNorm in retailCategoryTokens || categoriaNorm in personalCareCategoryTokens

        val pharmaBrands = tokens.filter { it in knownPharmaBrandConflictTokens }
        val retailBrands = tokens.filter { it in knownRetailBrandConflictTokens }
        if (pharmaBrands.isNotEmpty() && retailBrands.isNotEmpty()) {
            return "El nombre mezcla marcas o productos de contextos distintos: ${pharmaBrands.joinToString()} + ${retailBrands.joinToString()}."
        }

        if (hasPharmaToken && hasRetailToken) {
            return "El nombre mezcla señales farmacéuticas y retail en la misma búsqueda."
        }

        if ((hasRetailToken || hasPersonalCareToken) && hasPharmaCategory) {
            return "La categoría farmacéutica sugerida no coincide con señales retail/cuidado personal del nombre."
        }

        if (hasPharmaToken && hasRetailCategory && !hasPersonalCareToken) {
            return "La categoría retail sugerida no coincide con señales farmacéuticas del nombre."
        }

        return null
    }

    private fun corregirUnidadFarmaceuticaEvidente(productName: String, categoria: String): String? {
        val categoriaNorm = normalizar(categoria)
        if (categoriaNorm !in pharmaCategoryTokens) return null

        val regex = Regex("""\b(\d+(?:[.,]\d+)?)\s*g\b""", RegexOption.IGNORE_CASE)
        var changed = false
        val corrected = regex.replace(productName) { match ->
            val value = match.groupValues[1].replace(",", ".").toDoubleOrNull()
            if (value != null && value >= 100.0) {
                changed = true
                "${match.groupValues[1]} mg"
            } else {
                match.value
            }
        }.replace(Regex("\\s+"), " ").trim()

        return corrected.takeIf { changed && !it.equals(productName, ignoreCase = true) }
    }

    private val pharmaSignalTokens = setOf(
        "mg", "mcg", "ui", "iu", "jarabe", "pastilla", "pastillas", "tableta", "tabletas",
        "capsula", "capsulas", "ampolla", "ampollas", "antibiotico", "analgesico",
        "ibuprofeno", "amoxicilina", "amoxilina", "amoxicilinaaa", "paracetamol",
        "acetaminofen", "diclofenaco", "losartan", "metformina"
    )

    private val detallesComercialesCorreccion = setOf(
        "tableta", "tabletas", "tabs", "comprimido", "comprimidos",
        "capsula", "capsulas", "cap", "caps", "jarabe", "suspension",
        "solucion", "gotas", "ampolla", "ampollas", "sobre", "sobres",
        "frasco", "crema", "gel", "unguento", "pomada",
        "entera", "descremada", "semidescremada", "light", "diet",
        "regular", "clasica", "original", "sin", "lactosa", "azucar",
        "vainilla", "chocolate", "fresa", "fortificada", "instantanea",
        "evaporada", "condensada", "polvo"
    )

    private val gruposDescriptoresSensibles = listOf(
        setOf("lactea", "lacteo", "lacteas", "lacteos", "infantil", "infantiles", "bebe", "bebes", "nino", "ninos", "adulto", "adultos"),
        setOf("entera", "descremada", "semidescremada"),
        setOf("regular", "light", "diet")
    )

    private val retailSignalTokens = setOf(
        "gaseosa", "gaseosas", "bebida", "bebidas", "refresco", "refrescos", "cola",
        "harina", "arroz", "azucar", "aceite", "leche", "agua", "galleta", "galletas",
        "chocolate", "snack", "snacks", "ml", "l", "lt", "kg"
    )

    private val personalCareSignalTokens = setOf(
        "shampoo", "champu", "acondicionador", "jabon", "desodorante", "crema", "gel",
        "pasta", "dental", "cepillo"
    )

    private val pharmaCategoryTokens = setOf(
        "analgesicos", "antibioticos", "antiinflamatorios", "antigripales",
        "antialergicos", "antihipertensivos", "antidiabeticos", "vitaminas",
        "medicamentos"
    )

    private val retailCategoryTokens = setOf(
        "bebidas", "gaseosas", "alimentos", "abarrotes", "harinas", "lacteos",
        "snacks", "dulces", "limpieza"
    )

    private val personalCareCategoryTokens = setOf(
        "cuidado del cabello", "higiene personal", "cuidado personal", "cuidado bucal",
        "cosmeticos", "dermocosmetica"
    )

    private val knownPharmaBrandConflictTokens = setOf(
        "panadol", "atamel", "ibuprofeno", "amoxicilina", "amoxilina", "paracetamol",
        "acetaminofen", "diclofenaco"
    )

    private val knownRetailBrandConflictTokens = setOf(
        "coca", "cola", "pepsi", "harina", "pan", "arroz", "costeno", "costeño",
        "head", "shoulders"
    )

    private fun pareceProductoBaseIdentificable(nombre: String): Boolean {
        val tokens = normalizar(nombre)
            .split(Regex("\\s+"))
            .map { it.trim() }
            .filter { it.length >= 2 }
            .filterNot { it.toDoubleOrNull() != null }
            .filterNot { it in unidadesMedidaConsulta }

        if (tokens.isEmpty()) return false

        val distintivos = tokens.filterNot { it in tokensGenericosConsulta }
        if (distintivos.isEmpty()) return false

        return if (tokens.size >= 2) {
            distintivos.any { it.length >= 3 }
        } else {
            distintivos.first().length >= 4
        }
    }

    private val unidadesMedidaConsulta = setOf(
        "mg", "mcg", "g", "gr", "kg", "ml", "l", "lt", "ui", "iu", "und", "uds"
    )

    private val tokensGenericosConsulta = setOf(
        "producto", "productos", "medicina", "medicamento", "medicamentos",
        "pastilla", "pastillas", "tableta", "tabletas", "capsula", "capsulas",
        "jarabe", "suspension", "solucion", "gotas", "crema", "gel", "pomada",
        "bebida", "bebidas", "gaseosa", "gaseosas", "refresco", "refrescos", "cola",
        "harina", "arroz", "azucar", "aceite", "leche", "alimento", "alimentos",
        "snack", "snacks", "shampoo", "jabon", "papel", "higienico",
        "azul", "rojo", "verde", "amarillo", "blanco", "negro", "grande", "pequeno"
    )

    private val symptomIntentTokens = setOf(
        "dolor", "fiebre", "tos", "gripe", "alergia", "inflamacion", "infeccion",
        "diarrea", "vomito", "nausea", "acidez", "gastritis", "mareo", "cabeza",
        "garganta", "estomago", "muela", "muscular", "resfriado", "malestar"
    )

    private suspend fun consultarDeepSeekCategoriaEspecifica(
        productName: String,
        nombreCorregido: String?,
        categoriaGenerica: String,
        categoriasExistentes: List<String>,
        mercadoActivo: String
    ): String? {
        val apiKey = BuildConfig.DEEPSEEK_API_KEY
        if (apiKey.isBlank()) return null

        val categoriasTexto = if (categoriasExistentes.isEmpty()) {
            "[]"
        } else {
            categoriasExistentes.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
        }
        val nombreBase = nombreCorregido?.takeIf { it.isNotBlank() } ?: productName
        val prompt = """
Producto: "$nombreBase"
Original: "$productName"
Mercado: "$mercadoActivo"
Categorías existentes: $categoriasTexto
La categoría "$categoriaGenerica" es demasiado genérica.
Devuelve SOLO JSON {"c":"categoría específica"}.
Para fármacos usa acción terapéutica; para retail usa función del producto.
Prohibido: Medicamentos,Farmacia,Salud,Productos,Varios,Otros,General.
        """.trimIndent()

        val request = DeepSeekChatRequest(
            model = DEEPSEEK_MODEL,
            messages = listOf(
                DeepSeekMessage("system", "Responde únicamente JSON válido con la clave c."),
                DeepSeekMessage("user", prompt)
            ),
            temperature = 0.0,
            responseFormat = DeepSeekResponseFormat("json_object")
        )

        return runCatching {
            val response = deepSeekApi.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )
            val content = response.choices?.firstOrNull()?.message?.content?.trim() ?: return null
            Log.d("CategoryAI", "DeepSeek Category Retry JSON: $content")
            moshi.adapter(CategoryOnlyAnswer::class.java)
                .lenient()
                .fromJson(content)
                ?.c
        }.getOrNull()
    }

    private fun normalizar(name: String): String {
        val sinAcentos = Normalizer
            .normalize(name, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase(Locale.ROOT)
            .trim()
        return ProductUtils.sanitizarTexto(sinAcentos)
    }

    private data class StructuredQuickAnswer(
        @Json(name = "ep") val ep: String = "VALIDO",
        @Json(name = "n") val n: String? = null,
        @Json(name = "c") val c: String? = null,
        @Json(name = "e") val e: String = "",
        @Json(name = "t") val t: String = "",
        @Json(name = "r") val r: Boolean = false,
        @Json(name = "z") val z: String = "",
        @Json(name = "s") val s: String? = null,
        @Json(name = "b") val b: List<String>? = emptyList(),
        @Json(name = "q") val q: String = "GENERAL",
        @Json(name = "m") val m: String? = null,
        @Json(name = "os") val os: List<String>? = emptyList(),
        @Json(name = "pc") val pc: Boolean = true
    )

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
            android.util.Log.d("SilentAI", "Type JSON: $rawJson")

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

    private data class ManualTypeAnswer(
        @Json(name = "t") val t: String = "UNIDAD",
        @Json(name = "c") val c: String = "BAJA",
        @Json(name = "m") val m: String = "",
        @Json(name = "r") val r: Boolean? = null,
        @Json(name = "rr") val rr: String = ""
    )
}
