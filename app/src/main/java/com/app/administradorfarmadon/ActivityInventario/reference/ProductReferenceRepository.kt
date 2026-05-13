package com.app.administradorfarmadon.ActivityInventario.reference

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Locale
import java.util.concurrent.TimeUnit

class ProductReferenceRepository(
    private val rxNormApi: RxNormApi,
    private val medlinePlusApi: MedlinePlusApi,
    private val cache: ProductReferenceLocalCache
) {

    suspend fun enrichProductReference(
        productName: String,
        category: String?
    ): ProductReference = withContext(Dispatchers.IO) {
        val normalized = normalizeProductName(productName)
        if (normalized.isBlank()) {
            return@withContext notFoundReference(productName, category)
        }

        val cached = cache.findByNormalizedName(normalized)
        if (cached != null && !isExpired(cached)) {
            return@withContext cached.toDomain()
        }

        try {
            val resolved = resolveRxcui(productName)
            if (resolved == null) {
                val notFound = notFoundReference(productName, category)
                cache.upsert(notFound.toEntity(cached?.createdAt))
                return@withContext notFound
            }

            val medlineResponse = medlinePlusApi.getDrugInfoByRxcui(rxcui = resolved.rxcui)
            val reference = mapMedlineResponseToReference(
                originalName = productName,
                normalizedName = normalized,
                matchedName = resolved.name,
                rxcui = resolved.rxcui,
                category = category,
                confidence = resolved.confidence,
                response = medlineResponse
            )
            cache.upsert(reference.toEntity(cached?.createdAt))
            reference
        } catch (error: Exception) {
            val result = errorReference(productName, category, error)
            cache.upsert(result.toEntity(cached?.createdAt))
            result
        }
    }

    private suspend fun resolveRxcui(productName: String): ResolvedRxCui? {
        val exact = rxNormApi.findRxcuiByName(productName)
            .idGroup
            ?.rxnormId
            ?.firstOrNull()

        if (!exact.isNullOrBlank()) {
            val props = rxNormApi.getProperties(exact)
            return ResolvedRxCui(
                rxcui = exact,
                name = props.properties?.name ?: productName,
                confidence = 0.95
            )
        }

        val approximate = rxNormApi.approximateTerm(productName)
            .approximateGroup
            ?.candidate
            ?.filter { !it.rxcui.isNullOrBlank() }
            ?.maxByOrNull { it.score?.toDoubleOrNull() ?: 0.0 }

        val approxRxcui = approximate?.rxcui ?: return null
        val props = rxNormApi.getProperties(approxRxcui)
        return ResolvedRxCui(
            rxcui = approxRxcui,
            name = props.properties?.name ?: approximate.name ?: productName,
            confidence = 0.70
        )
    }

    private fun isExpired(entity: ProductReferenceEntity): Boolean {
        val age = System.currentTimeMillis() - entity.updatedAt
        return age > TimeUnit.HOURS.toMillis(24)
    }

    companion object {
        fun from(context: Context): ProductReferenceRepository {
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val rxNormApi = Retrofit.Builder()
                .baseUrl("https://rxnav.nlm.nih.gov/REST/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(RxNormApi::class.java)

            val medlinePlusApi = Retrofit.Builder()
                .baseUrl("https://connect.medlineplus.gov/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(MedlinePlusApi::class.java)

            return ProductReferenceRepository(
                rxNormApi = rxNormApi,
                medlinePlusApi = medlinePlusApi,
                cache = ProductReferenceLocalCache(context)
            )
        }
    }
}

fun mapMedlineResponseToReference(
    originalName: String,
    normalizedName: String,
    matchedName: String?,
    rxcui: String?,
    category: String?,
    confidence: Double,
    response: MedlinePlusResponse
): ProductReference {
    val firstDrugEntry = response.feed?.entry
        ?.firstOrNull { entry ->
            val title = entry.title?.resolved().orEmpty().lowercase(Locale.getDefault())
            title != "medlineplus connect" && title.isNotBlank()
        }

    if (firstDrugEntry == null) {
        return notFoundReference(originalName, category).copy(
            matchedName = matchedName,
            rxcui = rxcui
        )
    }

    val title = firstDrugEntry.title?.resolved()
        ?: matchedName
        ?: originalName
    val summary = stripHtml(firstDrugEntry.summary?.resolved().orEmpty())
    val url = firstDrugEntry.link?.firstOrNull()?.href
    val useCases = buildUseCasesSpanish(title, summary)
    val shortUse = buildShortUseSpanish(title, summary, category)
    val keywords = buildSpanishKeywords(
        originalName = originalName,
        matchedName = matchedName ?: title,
        category = category,
        title = title,
        summary = summary
    )

    return ProductReference(
        originalName = originalName,
        normalizedName = normalizedName,
        matchedName = title,
        rxcui = rxcui,
        ndc = null,
        category = category,
        commonUse = shortUse,
        useCases = useCases,
        howToUse = buildHowToUseSpanish(summary),
        notRecommendedFor = buildNotRecommendedForSpanish(summary),
        searchKeywords = keywords,
        warnings = buildSafeWarningsSpanish(),
        internalNote = "Referencia resumida para apoyo de inventario y búsqueda interna.",
        sourceName = "MedlinePlus en español",
        sourceUrl = url,
        confidence = confidence,
        language = "es",
        status = ProductReferenceStatus.READY
    )
}

fun buildShortUseSpanish(
    title: String?,
    summary: String?,
    category: String?
): String {
    val text = listOfNotNull(title, summary, category)
        .joinToString(" ")
        .lowercase(Locale.getDefault())

    return when {
        "fiebre" in text && "dolor" in text -> "Ayuda a aliviar dolor y fiebre."
        "dolor" in text -> "Ayuda a aliviar dolor."
        "fiebre" in text -> "Ayuda a bajar la fiebre."
        "tos" in text -> "Puede asociarse al alivio de la tos."
        "alerg" in text -> "Puede asociarse al alivio de molestias por alergia."
        "acidez" in text || "reflujo" in text -> "Puede asociarse al alivio de acidez o reflujo."
        else -> "Referencia de apoyo para inventario y atención en farmacia."
    }
}

fun buildUseCasesSpanish(
    title: String?,
    summary: String?
): List<String> {
    val text = listOfNotNull(title, summary)
        .joinToString(" ")
        .lowercase(Locale.getDefault())
    val cases = linkedSetOf<String>()

    if ("fiebre" in text) cases.add("Fiebre")
    if ("dolor de cabeza" in text) cases.add("Dolor de cabeza")
    if ("dolor corporal" in text || "dolor muscular" in text) cases.add("Dolor corporal")
    if ("dolor" in text && "dolor de cabeza" !in text) cases.add("Dolor")
    if ("malestar" in text) cases.add("Malestar general")
    if ("tos" in text) cases.add("Tos")
    if ("alerg" in text) cases.add("Alergia")
    if ("congestion" in text || "congestión" in text) cases.add("Congestión")
    if ("acidez" in text) cases.add("Acidez")
    if ("reflujo" in text) cases.add("Reflujo")

    return cases.take(5).ifEmpty {
        listOf("Verificar indicación oficial en empaque o receta")
    }
}

fun buildHowToUseSpanish(summary: String?): String {
    val text = summary.orEmpty().lowercase(Locale.getDefault())
    return when {
        "oral" in text || "tableta" in text || "jarabe" in text || "capsula" in text || "cápsula" in text ->
            "Seguir la dosis indicada en el empaque o receta. Generalmente se administra por vía oral si la presentación lo indica."
        else ->
            "Seguir la dosis indicada en el empaque o receta. Usar solo según la presentación y la vía indicadas oficialmente."
    }
}

fun buildNotRecommendedForSpanish(summary: String?): List<String> {
    val text = summary.orEmpty().lowercase(Locale.getDefault())
    val values = linkedSetOf<String>()
    values.add("Personas alérgicas al componente activo.")
    values.add("Personas con contraindicaciones indicadas en el empaque.")
    if ("embaraz" in text || "lact" in text) {
        values.add("Consultar al profesional si hay embarazo o lactancia.")
    } else {
        values.add("Consultar al profesional si hay embarazo, lactancia o enfermedad previa.")
    }
    return values.toList()
}

fun buildSpanishKeywords(
    originalName: String,
    matchedName: String?,
    category: String?,
    title: String?,
    summary: String?
): List<String> {
    val text = listOfNotNull(originalName, matchedName, category, title, summary)
        .joinToString(" ")
        .lowercase(Locale.getDefault())
    val keywords = linkedSetOf<String>()

    keywords.add(normalizeProductName(originalName))
    matchedName?.takeIf { it.isNotBlank() }?.let { keywords.add(normalizeProductName(it)) }
    category?.takeIf { it.isNotBlank() }?.let { keywords.add(normalizeProductName(it)) }

    if ("paracetamol" in text || "acetaminofén" in text || "acetaminofen" in text || "acetaminophen" in text || "acetaminofeno" in text) {
        keywords.addAll(
            listOf(
                "paracetamol",
                "acetaminofen",
                "acetaminofen",
                "acetaminophen",
                "acetaminofeno",
                "fiebre",
                "calentura",
                "dolor",
                "dolor de cabeza",
                "dolor corporal",
                "malestar",
                "analgesico",
                "antipiretico"
            )
        )
    }

    if ("ibuprofeno" in text || "ibuprofen" in text) {
        keywords.addAll(
            listOf(
                "ibuprofeno",
                "dolor",
                "fiebre",
                "inflamacion",
                "antiinflamatorio",
                "analgesico"
            )
        )
    }

    if ("fiebre" in text) keywords.addAll(listOf("fiebre", "calentura", "antipiretico"))
    if ("dolor" in text) keywords.addAll(listOf("dolor", "dolor de cabeza", "dolor corporal", "analgesico"))
    if ("tos" in text) keywords.add("tos")
    if ("gripe" in text || "resfriado" in text) keywords.addAll(listOf("gripe", "resfriado", "congestion"))
    if ("alerg" in text) keywords.addAll(listOf("alergia", "alergias", "antialergico"))
    if ("acidez" in text || "reflujo" in text || "estomago" in normalizeProductName(text)) {
        keywords.addAll(listOf("acidez", "reflujo", "estomago", "gastritis"))
    }

    return keywords
        .map { normalizeProductName(it) }
        .filter { it.isNotBlank() }
        .distinct()
}

fun buildSafeWarningsSpanish(): List<String> {
    return listOf(
        "Verificar empaque, lote, vencimiento, concentración e indicaciones oficiales antes de vender o recomendar.",
        "Confirmar contraindicaciones, advertencias y condiciones de uso en la etiqueta oficial del producto.",
        "Información de apoyo. No reemplaza indicaciones médicas, empaque, etiqueta oficial ni recomendación profesional."
    )
}

fun notFoundReference(
    productName: String,
    category: String?
): ProductReference {
    val normalized = normalizeProductName(productName)
    return ProductReference(
        originalName = productName,
        normalizedName = normalized,
        matchedName = null,
        rxcui = null,
        ndc = null,
        category = category,
        commonUse = null,
        useCases = emptyList(),
        howToUse = "Seguir la información del empaque o receta.",
        notRecommendedFor = listOf(
            "Personas con contraindicaciones indicadas en el empaque."
        ),
        searchKeywords = listOfNotNull(
            normalized.takeIf { it.isNotBlank() },
            category?.takeIf { it.isNotBlank() }?.let(::normalizeProductName)
        ).distinct(),
        warnings = listOf(
            "No se encontró referencia confiable en español. Verificar manualmente con el empaque del producto.",
            "Información de apoyo. No reemplaza indicaciones médicas, empaque, etiqueta oficial ni recomendación profesional."
        ),
        internalNote = "Referencia no encontrada automáticamente.",
        sourceName = "MedlinePlus en español",
        sourceUrl = null,
        confidence = 0.0,
        language = "es",
        status = ProductReferenceStatus.NOT_FOUND
    )
}

fun errorReference(
    productName: String,
    category: String?,
    error: Throwable
): ProductReference {
    val normalized = normalizeProductName(productName)
    return ProductReference(
        originalName = productName,
        normalizedName = normalized,
        matchedName = null,
        rxcui = null,
        ndc = null,
        category = category,
        commonUse = null,
        useCases = emptyList(),
        howToUse = "Seguir la información del empaque o receta.",
        notRecommendedFor = listOf(
            "Personas con contraindicaciones indicadas en el empaque."
        ),
        searchKeywords = listOfNotNull(
            normalized.takeIf { it.isNotBlank() },
            category?.takeIf { it.isNotBlank() }?.let(::normalizeProductName)
        ).distinct(),
        warnings = listOf(
            "No se pudo consultar la fuente confiable. Verificar manualmente con el empaque del producto.",
            "Información de apoyo. No reemplaza indicaciones médicas, empaque, etiqueta oficial ni recomendación profesional."
        ),
        internalNote = error.message,
        sourceName = "MedlinePlus en español",
        sourceUrl = null,
        confidence = 0.0,
        language = "es",
        status = ProductReferenceStatus.ERROR
    )
}
