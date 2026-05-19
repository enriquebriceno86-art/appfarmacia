package com.app.administradorfarmadon.ActivityInventario.reference

import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductControlType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Orquestador principal para la b\u00fasqueda de precios de mercado reales.
 */
object MarketPriceRepository {

    private val cache = mutableMapOf<String, List<MarketPriceReference>>()
    private val cacheTimestamp = mutableMapOf<String, Long>()
    private const val CACHE_DURATION_MS = 1000 * 60 * 30 // 30 minutos

    /**
     * Obtiene las referencias de precio de mercado.
     * Primero intenta fuentes reales (scraping), y si no hay nada, permite que
     * el flujo de IA sirva como fallback (manejado por el caller).
     */
    suspend fun getMarketPrices(
        productName: String,
        controlType: CreateProductControlType?
    ): List<MarketPriceReference> = withContext(Dispatchers.IO) {
        val query = productName.trim().lowercase()
        if (query.length < 3) return@withContext emptyList()

        // 1. Verificar Cache
        val now = System.currentTimeMillis()
        if (cache.containsKey(query) && (now - (cacheTimestamp[query] ?: 0L)) < CACHE_DURATION_MS) {
            return@withContext cache[query]!!
        }

        // 2. Scraping
        val rawItems = MarketPriceScraper.searchPeru(query)
        
        // 3. Normalizaci\u00f3n
        val aggregated = MarketPriceNormalizer.aggregate(query, rawItems, controlType)
        
        // 4. Guardar en Cache
        if (aggregated.isNotEmpty()) {
            cache[query] = aggregated
            cacheTimestamp[query] = now
        }

        aggregated
    }

    /**
     * Limpia el cach\u00e9 manual (por si el usuario quiere refrescar).
     */
    fun clearCache() {
        cache.clear()
        cacheTimestamp.clear()
    }
}
