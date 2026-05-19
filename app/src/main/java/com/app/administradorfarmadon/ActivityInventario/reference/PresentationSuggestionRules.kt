package com.app.administradorfarmadon.ActivityInventario.reference

import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductPresentation
import java.util.Locale

object PresentationSuggestionRules {

    /**
     * Devuelve un identificador del "grupo de sinónimos" al que pertenece el
     * nombre de una presentación.
     * 
     * V5.8: Mejorado para buscar formas farmacéuticas dentro de nombres largos.
     */
    fun getSynonymGroup(name: String): String? {
        val n = normalize(name)
        val words = n.split(" ").toSet()
        
        fun matches(vararg synonyms: String): Boolean {
            return synonyms.any { words.contains(it) }
        }

        return when {
            // Prioridad comercial: si el nombre contiene tanto el contenedor como la
            // unidad interna, debe ganar el formato en que realmente se vende.
            matches("blister", "blisters", "tira", "tiras", "foil") -> "blister"
            matches("caja", "cajas", "estuche", "estuches", "envase", "envases") -> "caja"
            matches("frasco", "frascos", "botella", "botellas") -> "frasco"
            matches("ampolla", "ampollas", "vial", "viales") -> "ampolla"
            matches("sobre", "sobres", "sachet", "sachets") -> "sobre"
            matches("tubo", "tubos") -> "tubo"
            matches("bolsa", "bolsas") -> "bolsa"

            matches("tableta", "tabletas", "comprimido", "comprimidos",
                "pastilla", "pastillas", "pildora", "pildoras",
                "capsula", "capsulas", "perla", "perlas",
                "unidad", "unidades", "pieza", "piezas") -> "unidad-pieza"

            else -> null
        }
    }

    /**
     * Infiere si una sugerencia es de tipo Genérico o de Marca basándose en el nombre
     * y la descripción técnica provista por la IA.
     * 
     * V5.8: Ahora Gemini provee el campo explícitamente, pero esto sirve como fallback.
     */
    fun inferCommercialType(name: String, description: String?): Pair<Boolean, Boolean> {
        val n = normalize(name)
        val d = normalize(description ?: "")

        val isGeneric = n.contains("generico") || d.contains("generico") || d.contains("principio activo") || d.contains("sin marca") || d.contains("equivalente")
        // V5.8: Si el nombre de la presentación NO contiene el nombre del producto (en CategorySuggestion),
        // pero es una marca conocida o tiene descriptor de marca.
        val isBrand = n.contains("marca") || d.contains("marca reconocida") || d.contains("patente") || d.contains("premium") || d.contains("original") || n.contains("patente")

        return isGeneric to isBrand
    }
    
    private fun normalize(text: String): String {
        return text.trim().lowercase(Locale.getDefault())
            .let { java.text.Normalizer.normalize(it, java.text.Normalizer.Form.NFD) }
            .replace(Regex("\\p{M}+"), "")
    }

    /**
     * Determina si dos sugerencias de IA son funcionalmente idénticas y deberían deduplicarse.
     * 
     * Se deduplica si:
     * - Tienen la misma equivalencia numérica.
     * - Pertenecen al mismo grupo de sinónimos.
     * - Tienen el mismo "perfil comercial" (ambas genéricas o ambas de marca).
     * 
     * V5.9: Si el perfil comercial es DESCONOCIDO en ambas, NO se deduplican
     * para evitar colapsar opciones que podrían ser distintas comercialmente.
     */
    fun shouldDeduplicate(p1: CreateProductPresentation, p2: CreateProductPresentation): Boolean {
        // 1. Equivalencia debe ser idéntica
        if (p1.equivalenceText.trim() != p2.equivalenceText.trim()) return false

        // 2. Grupo de forma farmacéutica debe ser el mismo
        val g1 = getSynonymGroup(p1.name)
        val g2 = getSynonymGroup(p2.name)
        if (g1 == null || g2 == null || g1 != g2) return false

        // 3. Perfil comercial (No deduplicar si uno es marca y otro genérico)
        // V5.9: Si no sabemos qué son (ambos false), preferimos mantener ambos para no perder data.
        val hasClearProfile1 = p1.isGeneric || p1.isBrand
        val hasClearProfile2 = p2.isGeneric || p2.isBrand
        
        if (!hasClearProfile1 || !hasClearProfile2) return false

        if (p1.isGeneric != p2.isGeneric) return false
        if (p1.isBrand != p2.isBrand) return false

        return true
    }

    /**
     * Filtra y deduplica una lista de sugerencias de IA.
     */
    fun deduplicateSuggestions(suggestions: List<CreateProductPresentation>): List<CreateProductPresentation> {
        val result = mutableListOf<CreateProductPresentation>()
        
        for (sug in suggestions) {
            val duplicate = result.find { shouldDeduplicate(it, sug) }
            if (duplicate == null) {
                result.add(sug)
            } else {
                // Si hay duplicado, podríamos preferir el que tenga mejor referencia de precio
                // o simplemente ignorar el nuevo. Aquí ignoramos el nuevo para mantener la primera intención.
            }
        }
        
        return result
    }

    /**
     * Devuelve el estado visual de la referencia de mercado seg\u00fan la confianza.
     * V7.0: Basado en investigaci\u00f3n de mercado IA.
     */
    fun getMarketReferenceStatus(confidence: Int, labelOverride: String? = null): MarketReferenceStatus {
        if (!labelOverride.isNullOrBlank()) {
            return when(labelOverride) {
                "Investigaci\u00f3n IA" -> MarketReferenceStatus.RESEARCH
                "Estimaci\u00f3n investigada" -> MarketReferenceStatus.INVESTIGATED
                "Referencia no confirmada" -> MarketReferenceStatus.NOT_CONFIRMED
                else -> MarketReferenceStatus.INSUFFICIENT
            }
        }

        return when {
            confidence >= 80 -> MarketReferenceStatus.RESEARCH
            confidence >= 50 -> MarketReferenceStatus.INVESTIGATED
            confidence >= 30 -> MarketReferenceStatus.NOT_CONFIRMED
            else -> MarketReferenceStatus.INSUFFICIENT
        }
    }

    enum class MarketReferenceStatus(val label: String) {
        RESEARCH("Investigaci\u00f3n IA"),
        INVESTIGATED("Estimaci\u00f3n investigada"),
        NOT_CONFIRMED("Referencia no confirmada"),
        INSUFFICIENT("Sin referencia confiable")
    }
}
