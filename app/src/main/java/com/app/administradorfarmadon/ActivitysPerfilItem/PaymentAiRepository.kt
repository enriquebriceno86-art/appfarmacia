package com.app.administradorfarmadon.ActivitysPerfilItem

import com.app.administradorfarmadon.ActivityInventario.reference.GeminiApi
import com.app.administradorfarmadon.ActivityInventario.reference.GeminiContent
import com.app.administradorfarmadon.ActivityInventario.reference.GeminiGenerationConfig
import com.app.administradorfarmadon.ActivityInventario.reference.GeminiPart
import com.app.administradorfarmadon.ActivityInventario.reference.GeminiRequest
import com.app.administradorfarmadon.ActivityInventario.reference.GeminiResponse
import com.app.administradorfarmadon.ActivityInventario.reference.GeminiSchema
import com.app.administradorfarmadon.ActivityInventario.reference.GeminiSchemaProperty
import com.app.administradorfarmadon.BuildConfig
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

data class PaymentAiInsight(
    val title: String,
    val detail: String,
    val priority: String,
    val actionType: String,
    val targetMethodTitle: String? = null
)

object PaymentAiRepository {

    private const val MODEL = "gemini-2.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val responseAdapter: JsonAdapter<PaymentAiResponse> =
        moshi.adapter(PaymentAiResponse::class.java).lenient()

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

    suspend fun reviewPaymentSetup(
        country: String,
        methods: List<MetodoPagoConfig>,
        suggestions: List<PaymentMethodSuggestion>
    ): List<PaymentAiInsight> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) return emptyList()
        if (methods.isEmpty() && suggestions.isEmpty()) return emptyList()

        val payload = buildContextJson(country, methods, suggestions)
        val systemPrompt = """
            Eres un asesor comercial para una farmacia que configura sus metodos de pago.
            Analiza SOLO el contexto dado y responde recomendaciones practicas y cortas.

            REGLAS:
            - No inventes metodos fuera del pais si no aparecen en sugeridos.
            - Prioriza acciones utiles para caja: falta QR, falta banco, falta titular, falta efectivo, falta metodo popular del pais.
            - Maximo 4 recomendaciones.
            - Si todo esta sano, devuelve una sola recomendacion positiva con actionType="NONE".
            - targetMethodTitle debe ser exactamente el titulo del metodo existente o sugerido cuando corresponda.
            - Responde únicamente JSON válido con la clave 'insights' conteniendo un array de objetos con: title, detail, priority, actionType, targetMethodTitle.
            - actionType permitido:
              - "ADD_SUGGESTED"
              - "EDIT_EXISTING"
              - "NONE"
        """.trimIndent()

        val userPrompt = """
            CONTEXTO:
            $payload
        """.trimIndent()

        val schema = GeminiSchema(
            type = "object",
            properties = mapOf(
                "insights" to GeminiSchemaProperty(
                    type = "array",
                    items = GeminiSchemaProperty(
                        type = "object",
                        properties = mapOf(
                            "title" to GeminiSchemaProperty("string"),
                            "detail" to GeminiSchemaProperty("string"),
                            "priority" to GeminiSchemaProperty("string"),
                            "actionType" to GeminiSchemaProperty("string"),
                            "targetMethodTitle" to GeminiSchemaProperty("string")
                        ),
                        required = listOf("title", "detail", "priority", "actionType", "targetMethodTitle")
                    )
                )
            ),
            required = listOf("insights")
        )

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(userPrompt))
                )
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(systemPrompt))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.2,
                responseSchema = schema
            )
        )

        val response = runCatching {
            api.generateContent(MODEL, apiKey, request)
        }.getOrNull() ?: return emptyList()

        val rawJson = response.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?.trim()
            ?: return emptyList()

        val parsed = responseAdapter.fromJson(rawJson) ?: return emptyList()
        return parsed.insights
            .mapNotNull { item ->
                val title = item.title.trim()
                val detail = item.detail.trim()
                val actionType = item.actionType.trim().uppercase()
                if (title.isBlank() || detail.isBlank()) return@mapNotNull null
                PaymentAiInsight(
                    title = title,
                    detail = detail,
                    priority = item.priority.trim().ifBlank { "MEDIA" },
                    actionType = actionType,
                    targetMethodTitle = item.targetMethodTitle.trim().ifBlank { null }
                )
            }
            .take(4)
    }

    private fun buildContextJson(
        country: String,
        methods: List<MetodoPagoConfig>,
        suggestions: List<PaymentMethodSuggestion>
    ): String {
        val methodsText = methods.joinToString(
            prefix = "[",
            postfix = "]",
            separator = ","
        ) { method ->
            """
            {
              "title":"${escape(method.titulo)}",
              "category":"${escape(method.categoria)}",
              "active":${method.activo},
              "usesQr":${method.usaQR},
              "hasQrUrl":${method.qrUrl.isNotBlank()},
              "requiresReference":${method.permiteReferencia},
              "bank":"${escape(method.banco)}",
              "accountNumber":"${escape(method.numeroCuenta)}",
              "bankOwner":"${escape(method.titularBanco)}",
              "walletPhone":"${escape(method.telefonoBilletera)}",
              "walletOwner":"${escape(method.titularBilletera)}"
            }
            """.trimIndent()
        }

        val suggestionsText = suggestions.joinToString(
            prefix = "[",
            postfix = "]",
            separator = ","
        ) { suggestion ->
            """
            {
              "title":"${escape(suggestion.title)}",
              "category":"${escape(suggestion.category)}",
              "usesQr":${suggestion.usaQr},
              "requiresReference":${suggestion.permiteReferencia}
            }
            """.trimIndent()
        }

        return """
        {
          "country":"${escape(country)}",
          "configuredMethods":$methodsText,
          "availableSuggestions":$suggestionsText
        }
        """.trimIndent()
    }

    private fun escape(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", " ")
            .trim()
    }

    private data class PaymentAiResponse(
        val insights: List<PaymentAiInsightRaw> = emptyList()
    )

    private data class PaymentAiInsightRaw(
        val title: String = "",
        val detail: String = "",
        val priority: String = "",
        val actionType: String = "",
        val targetMethodTitle: String = ""
    )
}
