package com.app.administradorfarmadon.ActivityInventario.reference

import org.json.JSONArray
import java.text.Normalizer

enum class ProductReferenceStatus {
    IDLE,
    LOADING,
    READY,
    NOT_FOUND,
    ERROR
}

data class ProductReference(
    val productId: String? = null,
    val originalName: String,
    val normalizedName: String,
    val matchedName: String?,
    val rxcui: String?,
    val ndc: String?,
    val category: String?,
    val commonUse: String?,
    val useCases: List<String> = emptyList(),
    val howToUse: String? = null,
    val notRecommendedFor: List<String> = emptyList(),
    val searchKeywords: List<String>,
    val warnings: List<String>,
    val internalNote: String?,
    val sourceName: String,
    val sourceUrl: String?,
    val confidence: Double,
    val language: String = "es",
    val status: ProductReferenceStatus
)

data class ProductReferenceSummary(
    val productName: String,
    val matchedName: String?,
    val rxcui: String?,
    val shortUse: String?,
    val useCases: List<String>,
    val howToUse: String?,
    val notRecommendedFor: List<String>,
    val keywords: List<String>,
    val sourceName: String?,
    val sourceUrl: String?,
    val confidence: Double
)

data class ProductReferenceUiState(
    val status: ProductReferenceStatus = ProductReferenceStatus.IDLE,
    val reference: ProductReference? = null,
    val errorMessage: String? = null
)

data class ResolvedRxCui(
    val rxcui: String,
    val name: String,
    val confidence: Double
)

data class ProductReferenceEntity(
    val normalizedName: String,
    val originalName: String,
    val matchedName: String?,
    val rxcui: String?,
    val ndc: String?,
    val category: String?,
    val commonUse: String?,
    val useCasesJson: String,
    val howToUse: String?,
    val notRecommendedForJson: String,
    val searchKeywordsJson: String,
    val warningsJson: String,
    val internalNote: String?,
    val sourceName: String,
    val sourceUrl: String?,
    val confidence: Double,
    val language: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
)

fun ProductReference.toSummary(): ProductReferenceSummary {
    return ProductReferenceSummary(
        productName = originalName,
        matchedName = matchedName,
        rxcui = rxcui,
        shortUse = commonUse,
        useCases = useCases.take(5),
        howToUse = howToUse,
        notRecommendedFor = notRecommendedFor.take(4),
        keywords = searchKeywords.take(10),
        sourceName = sourceName,
        sourceUrl = sourceUrl,
        confidence = confidence
    )
}

fun ProductReferenceEntity.toDomain(): ProductReference {
    return ProductReference(
        originalName = originalName,
        normalizedName = normalizedName,
        matchedName = matchedName,
        rxcui = rxcui,
        ndc = ndc,
        category = category,
        commonUse = commonUse,
        useCases = fromJsonList(useCasesJson),
        howToUse = howToUse,
        notRecommendedFor = fromJsonList(notRecommendedForJson),
        searchKeywords = fromJsonList(searchKeywordsJson),
        warnings = fromJsonList(warningsJson),
        internalNote = internalNote,
        sourceName = sourceName,
        sourceUrl = sourceUrl,
        confidence = confidence,
        language = language,
        status = runCatching { ProductReferenceStatus.valueOf(status) }
            .getOrDefault(ProductReferenceStatus.ERROR)
    )
}

fun ProductReference.toEntity(
    existingCreatedAt: Long? = null
): ProductReferenceEntity {
    val now = System.currentTimeMillis()
    return ProductReferenceEntity(
        normalizedName = normalizedName,
        originalName = originalName,
        matchedName = matchedName,
        rxcui = rxcui,
        ndc = ndc,
        category = category,
        commonUse = commonUse,
        useCasesJson = toJsonList(useCases),
        howToUse = howToUse,
        notRecommendedForJson = toJsonList(notRecommendedFor),
        searchKeywordsJson = toJsonList(searchKeywords),
        warningsJson = toJsonList(warnings),
        internalNote = internalNote,
        sourceName = sourceName,
        sourceUrl = sourceUrl,
        confidence = confidence,
        language = language,
        status = status.name,
        createdAt = existingCreatedAt ?: now,
        updatedAt = now
    )
}

fun normalizeProductName(name: String): String {
    val base = name.trim().lowercase()
        .replace(Regex("\\s+"), " ")
    val normalized = Normalizer.normalize(base, Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    return normalized.replace("ñ", "n")
}

fun stripHtml(input: String): String {
    return input
        .replace(Regex("<[^>]*>"), " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace(Regex("\\s+"), " ")
        .trim()
}

private fun toJsonList(values: List<String>): String {
    return JSONArray(values).toString()
}

private fun fromJsonList(raw: String): List<String> {
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val value = array.optString(index).trim()
                if (value.isNotBlank()) add(value)
            }
        }
    }.getOrDefault(emptyList())
}
