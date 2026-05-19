package com.app.administradorfarmadon.ActivityInventario.bulk

import com.app.administradorfarmadon.ActivityInventario.reference.*
import com.app.administradorfarmadon.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object BulkImportAiRepository {

    private const val MODEL = "gemini-2.0-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val api: GeminiApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    suspend fun enrichDrafts(drafts: List<ImportDraftProduct>): List<ImportDraftProduct> {
        val names = drafts.filter { it.validationState != ImportValidationState.ERROR }.map { it.name }
        if (names.isEmpty()) return drafts

        val systemPrompt = """
            Eres un experto en farmacia.
            Dada una lista de nombres de productos, debes devolver un JSON con enriquecimiento técnico.
            
            REGLAS:
            - category: Categoría farmacéutica (ej. Analgésicos, Antibióticos).
            - controlType: "UNIDAD", "PESO" o "LIQUIDO".
            - keywords: Lista de hasta 5 síntomas o usos comunes (ej. ["dolor", "fiebre"]).
            - requiresPrescription: true/false.
            - Responde únicamente JSON válido con la clave 'results' conteniendo un array de objetos con: originalName, category, controlType, keywords, requiresPrescription.
        """.trimIndent()

        val userPrompt = """
            Productos a analizar:
            ${names.joinToString(", ")}
        """.trimIndent()

        val schema = GeminiSchema(
            type = "object",
            required = listOf("results"),
            properties = mapOf(
                "results" to GeminiSchemaProperty(
                    type = "array",
                    items = GeminiSchemaProperty(
                        type = "object",
                        required = listOf("originalName", "category", "controlType", "keywords", "requiresPrescription"),
                        properties = mapOf(
                            "originalName" to GeminiSchemaProperty(type = "string"),
                            "category" to GeminiSchemaProperty(type = "string"),
                            "controlType" to GeminiSchemaProperty(type = "string"),
                            "keywords" to GeminiSchemaProperty(type = "array", items = GeminiSchemaProperty(type = "string")),
                            "requiresPrescription" to GeminiSchemaProperty(type = "boolean")
                        )
                    )
                )
            )
        )

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = userPrompt)))),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                responseSchema = schema
            )
        )

        return try {
            val response = api.generateContent(
                model = MODEL,
                apiKey = BuildConfig.GEMINI_API_KEY,
                request = request
            )

            val jsonString = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: return drafts
            val adapter = moshi.adapter(BatchAiResponse::class.java)
            val batchResults = adapter.fromJson(jsonString) ?: return drafts

            drafts.map { draft ->
                val enrichment = batchResults.results.find { it.originalName.lowercase() == draft.name.lowercase() }
                if (enrichment != null) {
                    draft.copy(
                        category = enrichment.category,
                        controlType = mapToControlType(enrichment.controlType),
                        keywords = enrichment.keywords,
                        requiresPrescription = enrichment.requiresPrescription,
                        validationState = if (draft.validationState == ImportValidationState.ERROR) ImportValidationState.ERROR else ImportValidationState.READY
                    )
                } else {
                    draft
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            drafts
        }
    }

    private fun mapToControlType(type: String): com.app.administradorfarmadon.ActivityInventario.ui.CreateProductControlType {
        return when (type.uppercase()) {
            "PESO" -> com.app.administradorfarmadon.ActivityInventario.ui.CreateProductControlType.PESO
            "LIQUIDO" -> com.app.administradorfarmadon.ActivityInventario.ui.CreateProductControlType.LIQUIDO
            else -> com.app.administradorfarmadon.ActivityInventario.ui.CreateProductControlType.UNIDAD
        }
    }

    data class BatchAiResponse(
        @Json(name = "results") val results: List<DraftAiEnrichment>
    )

    data class DraftAiEnrichment(
        val originalName: String,
        val category: String,
        val controlType: String,
        val keywords: List<String>,
        val requiresPrescription: Boolean
    )
}
