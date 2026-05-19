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
        
        // Tokenización y expansión (V4.9: Usando Diccionario Centralizado)
        val rawTokens = q.split(" ").filter { it.length >= 3 }
        val synonyms = getSynonymsForQuery(q, rawTokens)

        return products.mapNotNull { product ->
            evaluateMatch(q, rawTokens, synonyms, product)
        }.sortedWith(
            compareBy<SearchResult> { it.matchType.priority }
                .thenBy { normalize(it.product.nombre) } // V4.11: Orden determinista y agnóstico a acentos
        )
    }

    private fun getSynonymsForQuery(q: String, tokens: List<String>): List<String> {
        val synonyms = mutableSetOf<String>()
        for (group in ClinicalDictionary.synonymGroups) {
            // Un grupo se activa si el query completo o alguno de sus tokens es parte del grupo
            if (group.any { it.contains(q) || q.contains(it) } || 
                tokens.any { token -> group.any { it.contains(token) || token.contains(it) } }) {
                synonyms.addAll(group)
            }
        }
        // Quitamos el query original de los sinónimos para evaluar prioridad por separado
        synonyms.remove(q)
        tokens.forEach { synonyms.remove(it) }
        return synonyms.toList()
    }

    private fun evaluateMatch(q: String, tokens: List<String>, synonyms: List<String>, product: MoldeProductos): SearchResult? {
        // 1. Prioridad Máxima: Nombre
        val normalizedName = normalize(product.nombre)
        if (normalizedName.contains(q)) return SearchResult(product, MatchType.NAME)
        if (tokens.isNotEmpty() && tokens.all { normalizedName.contains(it) }) {
            return SearchResult(product, MatchType.NAME)
        }

        // 2. Prioridad Media: Categoría
        val normalizedCategory = normalize(product.categoria)
        if (normalizedCategory.contains(q)) return SearchResult(product, MatchType.CATEGORY)

        // 3. Prioridad IA: Búsqueda Clínica (V4.9: Jerarquía de prioridad CLÍNICA)
        val clinicalSources = (product.referenceKeywords + product.referenceCommonUse + product.referenceUseCases)
            .filter { it.isNotBlank() }
            .map { normalize(it) }

        // A. Match Directo (Precisión Máxima)
        clinicalSources.find { it.contains(q) || (tokens.isNotEmpty() && tokens.all { t -> it.contains(t) }) }?.let { matched ->
            return SearchResult(product, MatchType.SYMPTOM_DIRECT, matchedText = matched)
        }

        // B. Match por Sinónimos (Prioridad Secundaria)
        synonyms.find { syn -> clinicalSources.any { it.contains(syn) } }?.let { matchedSyn ->
            return SearchResult(product, MatchType.SYMPTOM_EXPANDED, matchedText = matchedSyn)
        }

        // 4. Otros campos (Código de barras)
        if (normalize(product.codigo).contains(q)) {
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
     * Actualizado V4.9 para priorizar Clinical Direct sobre Clinical Expanded.
     */
    enum class MatchType(val priority: Int) {
        NAME(1),
        CATEGORY(2),
        SYMPTOM_DIRECT(3),    // Match exacto con el síntoma buscado
        SYMPTOM_EXPANDED(4),  // Match a través de sinónimos
        OTHER(5),
        NONE(99)
    }
}
