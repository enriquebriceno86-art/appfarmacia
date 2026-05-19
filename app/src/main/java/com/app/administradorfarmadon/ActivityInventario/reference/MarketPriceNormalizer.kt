package com.app.administradorfarmadon.ActivityInventario.reference

import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductControlType
import java.util.Locale

/**
 * Normaliza y agrupa los resultados del scraping en presentaciones comerciales legibles.
 */
object MarketPriceNormalizer {

    /**
     * Toma una lista de items sucios de la web y los agrupa por presentaci\u00f3n.
     */
    fun aggregate(
        query: String,
        items: List<MarketSourceItem>,
        controlType: CreateProductControlType?
    ): List<MarketPriceReference> {
        if (items.isEmpty()) return emptyList()

        // 1. Limpieza b\u00e1sica y parseo de cada item
        val parsedItems = items.map { item ->
            val parsed = PharmaceuticalParser.parse(item.productName)
            val equiv = extractEquivalence(item.productName) ?: 1
            val synonymGroup = PresentationSuggestionRules.getSynonymGroup(item.productName) ?: "unknown"
            
            // P1 FIX: El par de agrupaci\u00f3n es (Equivalencia, Grupo Sem\u00e1ntico)
            val groupKey = equiv to synonymGroup
            item.copy(presentationText = parsed.form.ifBlank { "Unidad" }) to groupKey
        }

        // 2. Agrupar por equivalencia y grupo de sin\u00f3nimo
        val groups = parsedItems.groupBy { it.second } 

        return groups.mapNotNull { (groupKey, list) ->
            val (equiv, synonymGroup) = groupKey
            val sourceItems = list.map { it.first }
            val avg = sourceItems.map { it.price }.average()
            val min = sourceItems.map { it.price }.minOrNull() ?: 0.0
            val max = sourceItems.map { it.price }.maxOrNull() ?: 0.0
            
            val presentationName = inferPresentationName(equiv, sourceItems, controlType)
            
            // P1 FIX: Incluir la confianza de match en el cálculo final
            val avgMatchConfidence = sourceItems.map { it.confidenceMatch }.average().toInt()
            val baseConfidence = calculateConfidence(sourceItems)
            val finalConfidence = (baseConfidence * 0.7 + avgMatchConfidence * 0.3).toInt()
            
            MarketPriceReference(
                presentationName = presentationName,
                equivalenceBase = equiv,
                averagePrice = avg,
                minPrice = min,
                maxPrice = max,
                sourceCount = sourceItems.size,
                sources = sourceItems,
                confidence = finalConfidence,
                label = if (finalConfidence > 50) "Referencia web" else "Estimaci\u00f3n basada en fuentes",
                // P2 FIX: Solo marcar como verificado si la confianza es s\u00f3lida (> 30)
                isVerifiedWebSource = finalConfidence > 30
            )
        }.sortedBy { it.equivalenceBase }
    }

    private fun extractEquivalence(name: String): Int? {
        // Busca patrones como "x 100", "x100", "caja de 30", "blister 10"
        val regex = Regex("(?:x|de|caja|blister|tab|cap)\\s*(\\d+)", RegexOption.IGNORE_CASE)
        val match = regex.find(name)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun inferPresentationName(equiv: Int, items: List<MarketSourceItem>, controlType: CreateProductControlType?): String {
        if (equiv == 1) {
            return when (controlType) {
                CreateProductControlType.LIQUIDO -> "Frasco"
                CreateProductControlType.PESO -> "Sobre/Tubo"
                else -> "Unidad"
            }
        }
        
        // Buscar el nombre m\u00e1s común en los items
        val mostCommon = items.flatMap { it.productName.lowercase().split(" ") }
            .filter { it in listOf("caja", "blister", "frasco", "sobre", "sachet", "ampolla") }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }?.key ?: "Caja"
            
        return "${mostCommon.replaceFirstChar { it.uppercase() }} x $equiv"
    }

    private fun calculateConfidence(items: List<MarketSourceItem>): Int {
        val count = items.size
        if (count == 0) return 0
        
        var score = 0
        // Puntos por cantidad de fuentes
        score += (count * 20).coerceAtMost(60)
        
        // Puntos por diversidad de tiendas
        val stores = items.map { it.storeName }.distinct().size
        if (stores > 1) score += 20
        
        // Puntos por consistencia de precios (desviaci\u00f3n est\u00e1ndar relativa)
        if (count > 1) {
            val avg = items.map { it.price }.average()
            val variance = items.map { (it.price - avg) * (it.price - avg) }.average()
            val stdDev = Math.sqrt(variance)
            val cv = stdDev / avg // Coeficiente de variaci\u00f3n
            if (cv < 0.2) score += 20
            else if (cv < 0.5) score += 10
        } else {
            score += 10 // Una sola fuente es baja confianza
        }
        
        return score.coerceIn(0, 100)
    }
}
