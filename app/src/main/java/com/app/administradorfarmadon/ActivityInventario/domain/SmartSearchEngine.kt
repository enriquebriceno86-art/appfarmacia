package com.app.administradorfarmadon.ActivityInventario.domain

import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ActivityInventario.reference.normalizeProductName
import java.util.Locale

/**
 * Motor de búsqueda inteligente para el inventario.
 * Separa la lógica de filtrado y priorización para permitir búsquedas por síntomas (keywords).
 */
class SmartSearchEngine {

    /**
     * Filtra y ordena una lista de productos basándose en una consulta de texto.
     * 
     * Prioridad de resultados:
     * 1. Coincidencia en Nombre (el cliente sabe lo que busca).
     * 2. Coincidencia en Categoría (búsqueda por grupo).
     * 3. Coincidencia en Síntomas/Keywords (búsqueda asistida por IA).
     */
    fun search(query: String, products: List<MoldeProductos>): List<SearchResult> {
        val q = normalize(query)
        if (q.isBlank()) return products.map { SearchResult(it, MatchType.NONE) }

        return products.mapNotNull { product ->
            evaluateMatch(q, product)
        }.sortedBy { it.matchType.priority }
    }

    private fun evaluateMatch(q: String, product: MoldeProductos): SearchResult? {
        // 1. Prioridad Máxima: Nombre
        val normalizedName = normalize(product.nombre)
        if (normalizedName.contains(q)) {
            return SearchResult(product, MatchType.NAME)
        }

        // 2. Prioridad Media: Categoría
        val normalizedCategory = normalize(product.categoria)
        if (normalizedCategory.contains(q)) {
            return SearchResult(product, MatchType.CATEGORY)
        }

        // 3. Prioridad IA: Síntomas y Usos (Keywords)
        // Buscamos si alguno de los síntomas detectados por Gemini coincide
        val matchingKeyword = product.referenceKeywords.find { 
            normalize(it).contains(q) 
        }
        if (matchingKeyword != null) {
            return SearchResult(
                product = product, 
                matchType = MatchType.SYMPTOM, 
                matchedText = matchingKeyword
            )
        }

        // 4. Otros campos (Código, Usos comunes)
        if (normalize(product.codigo).contains(q) || 
            normalize(product.referenceCommonUse).contains(q)) {
            return SearchResult(product, MatchType.OTHER)
        }

        return null
    }

    private fun normalize(text: String): String {
        return normalizeProductName(text)
    }

    /**
     * Representa un resultado de búsqueda con su tipo de coincidencia.
     */
    data class SearchResult(
        val product: MoldeProductos,
        val matchType: MatchType,
        val matchedText: String? = null // El síntoma específico que coincidió
    )

    /**
     * Define la prioridad de los resultados (menor valor = mayor prioridad).
     */
    enum class MatchType(val priority: Int) {
        NAME(1),
        CATEGORY(2),
        SYMPTOM(3),
        OTHER(4),
        NONE(99)
    }
}
