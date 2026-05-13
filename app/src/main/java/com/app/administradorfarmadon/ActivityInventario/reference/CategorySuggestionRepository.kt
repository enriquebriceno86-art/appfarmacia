package com.app.administradorfarmadon.ActivityInventario.reference

import com.app.administradorfarmadon.ActivityInventario.ProductUtils
import com.app.administradorfarmadon.BuildConfig
import com.google.firebase.database.FirebaseDatabase
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.Normalizer
import java.util.Locale
import java.util.concurrent.TimeUnit

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
    private const val MIN_CONFIDENCE = 60

    private val cacheProceso = mutableMapOf<String, CategorySuggestion>()

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val structuredAdapter: JsonAdapter<StructuredAnswer> =
        moshi.adapter(StructuredAnswer::class.java).lenient()

    private val api: GeminiApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                    else HttpLoggingInterceptor.Level.NONE
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    /**
     * Solicita la sugerencia a Gemini. Devuelve `null` si:
     *  - El nombre es demasiado corto.
     *  - No hay API key configurada.
     *  - Confianza por debajo del umbral.
     *  - Hubo error de red.
     *
     * No lanza excepciones: el feature es opcional.
     */
    suspend fun sugerirCategoria(
        productName: String,
        categoriasExistentes: List<String>
    ): CategorySuggestion? {
        val nombreLimpio = productName.trim()
        if (nombreLimpio.length < 4) return null

        val key = normalizar(nombreLimpio)
        cacheProceso[key]?.let { return it }

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) return null

        val resultado = runCatching {
            consultarGemini(nombreLimpio, categoriasExistentes, apiKey)
        }.getOrNull() ?: return null

        if (resultado.confianza < MIN_CONFIDENCE) return null

        cacheProceso[key] = resultado
        return resultado
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

    private suspend fun consultarGemini(
        productName: String,
        categoriasExistentes: List<String>,
        apiKey: String
    ): CategorySuggestion? {
        val listaCategorias = if (categoriasExistentes.isEmpty()) {
            "(no hay categorías previas; propón una adecuada)"
        } else {
            categoriasExistentes.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
        }

        val prompt = """
            Eres un experto en clasificación de productos de farmacia.
            Producto: "$productName"
            Categorías existentes: $listaCategorias

            Devuelve un JSON con DOS detecciones para este producto:

            1) Categoría
            - Si encaja claramente en una de las categorías existentes,
              úsala TAL CUAL (mismas mayúsculas y acentos) y marca existeEnLista=true.
            - Si ninguna encaja, propone una nueva categoría corta en español
              (1-3 palabras, capitalizada) y marca existeEnLista=false.
            - "emoji": UN solo emoji representativo de la categoría
              (ej. 💊 Analgésicos, 💉 Inyectables, 🧴 Cuidado piel, 🩺 Diagnóstico,
              🌡️ Fiebre, 👶 Bebés). Solo 1 carácter emoji, sin texto.
            - "razon": UNA línea breve en español de por qué encaja.

            2) Tipo de control de inventario
            - "tipoControl": elige EXACTAMENTE una de estas tres cadenas:
                * "UNIDAD"  → se cuenta por piezas (tabletas, cápsulas, frascos
                             sellados, ampollas, condones, jeringas, dispositivos).
                * "PESO"    → se mide en gramos o kilogramos (cremas, pomadas,
                             talcos, ungüentos, polvos, harinas medicinales).
                * "LIQUIDO" → se mide en mL o litros (jarabes, soluciones,
                             alcohol, suero, geles líquidos, antisépticos).
            - "razonTipo": UNA línea muy breve (máx. 60 caracteres) en español
              explicando por qué ese tipo de control (ej. "Se cuenta por tabletas").

            3) Receta médica
            - "requiereReceta": boolean. true si en general este producto requiere
              receta médica para venderse en una farmacia (antibióticos sistémicos,
              ansiolíticos, antidepresivos, opioides, hormonales, controlados).
              false para productos OTC: analgésicos comunes (paracetamol, ibuprofeno),
              vitaminas, antiácidos básicos, alcohol, vendas, jarabes para tos OTC,
              cuidado personal. Si dudas, prefiere false.
            - "razonReceta": UNA línea muy breve en español (máx. 60 caracteres)
              ej. "Venta libre" / "Antibiótico controlado".

            4) Presentaciones de venta sugeridas
            - "presentacionesSugeridas": arreglo ORDENADO de mayor a menor con
              las presentaciones que ese producto se vende típicamente en una
              farmacia. Cada elemento:
                · "nombre": nombre corto y reconocible
                  ("Caja", "Blister", "Tableta", "Frasco", "Tubo", "Bolsa", "Sobre").
                · "equivalenciaUnidadBase": entero positivo en la unidad base
                  (tabletas, g o mL) que representa esa presentación.
                · "descripcion": opcional, una línea muy breve.
            - REGLAS de coherencia con tipoControl:
                · UNIDAD: solo presentaciones por piezas (caja, blister,
                  tableta). Llega hasta la TABLETA/CÁPSULA SUELTA (1 unidad).
                · PESO: solo envases (caja, tubo, sobre, frasco). Llega hasta
                  el envase comercial completo. NO sugieras "1 g" ni "gramo".
                · LIQUIDO: solo envases (caja, frasco, bolsa). Llega hasta el
                  envase comercial completo. NO sugieras "1 mL" ni "5 mL"
                  (las cucharaditas no son presentación de venta en farmacia).
            - Devuelve entre 2 y 4 presentaciones lógicas. Si el producto
              solo tiene una presentación natural (ej. un frasco sin caja),
              devuelve solo esa.

            5) "keywords": Arreglo de 5 a 8 frases cortas sobre PARA QUÉ SIRVE el producto
              en español. Debe responder a la pregunta del cliente: "¿Qué me quita esto?" o
              "¿Para qué sirve?".
              Ejemplos: 
              - Amoxicilina: ["Infección de garganta", "Dolor de muela", "Infección urinaria", "Antibiótico"].
              - Panadol: ["Bajar la fiebre", "Dolor de cabeza", "Malestar de gripe", "Post-vacuna"].
              - Diclofenac: ["Inflamación", "Dolor muscular", "Golpes", "Artritis"].

            6) "confianza": entero 0-100 (certeza global de las detecciones).

            No incluyas markdown ni texto fuera del JSON.
        """.trimIndent()

        val schema = GeminiSchema(
            type = "OBJECT",
            properties = mapOf(
                "categoria" to GeminiSchemaProperty("STRING", "Categoría sugerida"),
                "emoji" to GeminiSchemaProperty("STRING", "Emoji representativo"),
                "tipoControl" to GeminiSchemaProperty(
                    "STRING",
                    "UNIDAD | PESO | LIQUIDO"
                ),
                "razonTipo" to GeminiSchemaProperty(
                    "STRING",
                    "Por qué ese tipo de control"
                ),
                "requiereReceta" to GeminiSchemaProperty(
                    "BOOLEAN",
                    "¿Requiere receta médica?"
                ),
                "razonReceta" to GeminiSchemaProperty(
                    "STRING",
                    "Breve justificación de la receta"
                ),
                "presentacionesSugeridas" to GeminiSchemaProperty(
                    type = "ARRAY",
                    description = "Presentaciones de venta jerárquicas",
                    items = GeminiSchemaProperty(
                        type = "OBJECT",
                        properties = mapOf(
                            "nombre" to GeminiSchemaProperty("STRING"),
                            "equivalenciaUnidadBase" to GeminiSchemaProperty("INTEGER"),
                            "descripcion" to GeminiSchemaProperty("STRING")
                        ),
                        required = listOf("nombre", "equivalenciaUnidadBase")
                    )
                ),
                "keywords" to GeminiSchemaProperty(
                    type = "ARRAY",
                    description = "Palabras clave o síntomas para búsqueda",
                    items = GeminiSchemaProperty("STRING")
                ),
                "existeEnLista" to GeminiSchemaProperty("BOOLEAN"),
                "confianza" to GeminiSchemaProperty("INTEGER"),
                "razon" to GeminiSchemaProperty("STRING")
            ),
            required = listOf(
                "categoria", "emoji", "tipoControl", "razonTipo",
                "requiereReceta", "razonReceta", "presentacionesSugeridas",
                "keywords", "existeEnLista", "confianza", "razon"
            )
        )

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(role = "user", parts = listOf(GeminiPart(prompt)))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.2,
                responseSchema = schema
            )
        )

        val response = api.generateContent(MODEL, apiKey, request)
        val rawJson = response.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?.trim()
            ?: return null

        val parsed = structuredAdapter.fromJson(rawJson) ?: return null
        val categoriaLimpia = parsed.categoria.trim()
        if (categoriaLimpia.isBlank()) return null

        val existeReal = categoriasExistentes.any {
            it.equals(categoriaLimpia, ignoreCase = true)
        }

        // Mapeo robusto del string del LLM a nuestro enum. Si Gemini devuelve
        // algo inesperado, el caller decidirá cómo manejarlo (por ahora se
        // muestra como DESCONOCIDO y la UI cae a manual para el tipo).
        val tipo = when (parsed.tipoControl.trim().uppercase(Locale.ROOT)) {
            "UNIDAD", "UNIDADES" -> TipoControlDetectado.UNIDAD
            "PESO", "GRAMOS", "KILOGRAMOS", "KG" -> TipoControlDetectado.PESO
            "LIQUIDO", "LÍQUIDO", "VOLUMEN", "ML", "MILILITROS" -> TipoControlDetectado.LIQUIDO
            else -> TipoControlDetectado.DESCONOCIDO
        }

        // Sanitizamos las presentaciones: limitamos cantidad, descartamos
        // equivalencias inválidas y forzamos coherencia con tipoControl.
        val presentacionesLimpias = parsed.presentacionesSugeridas
            .asSequence()
            .mapNotNull { item ->
                val nombre = item.nombre.trim()
                val equiv = item.equivalenciaUnidadBase
                if (nombre.isBlank() || equiv <= 0) null
                else PresentacionSugerida(
                    nombre = nombre.take(40),
                    equivalenciaUnidadBase = equiv,
                    descripcion = item.descripcion.trim().take(80)
                )
            }
            .distinctBy { it.nombre.lowercase(Locale.ROOT) }
            .take(4)
            .toList()

        // Nota: ya no inferimos modo de ingreso por IA. Esa decisión depende
        // del modelo de negocio (cómo compra el usuario al proveedor), no
        // del producto. El campo se mantiene en el modelo como DESCONOCIDO
        // por compatibilidad con sitios que aún lo lean.
        return CategorySuggestion(
            productName = productName,
            categoria = categoriaLimpia,
            emoji = parsed.emoji.trim().take(4).ifBlank { "💊" },
            tipoControl = tipo,
            razonTipo = parsed.razonTipo.trim().take(120),
            requiereReceta = parsed.requiereReceta,
            razonReceta = parsed.razonReceta.trim().take(120),
            modoIngreso = ModoIngresoDetectado.DESCONOCIDO,
            presentacionesSugeridas = presentacionesLimpias,
            keywords = parsed.keywords, // <--- EXTRACCIÓN DE KEYWORDS
            existeEnLista = existeReal,
            confianza = parsed.confianza.coerceIn(0, 100),
            razon = parsed.razon.trim().take(200)
        )
    }

    private fun normalizar(name: String): String {
        val sinAcentos = Normalizer
            .normalize(name, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase(Locale.ROOT)
            .trim()
        return ProductUtils.sanitizarTexto(sinAcentos)
    }

    private data class StructuredAnswer(
        val categoria: String = "",
        val emoji: String = "",
        val tipoControl: String = "",
        val razonTipo: String = "",
        val requiereReceta: Boolean = false,
        val razonReceta: String = "",
        val presentacionesSugeridas: List<StructuredPresentation> = emptyList(),
        val keywords: List<String> = emptyList(),
        val existeEnLista: Boolean = false,
        val confianza: Int = 0,
        val razon: String = ""
    )

    private data class StructuredPresentation(
        val nombre: String = "",
        val equivalenciaUnidadBase: Int = 0,
        val descripcion: String = ""
    )
}
