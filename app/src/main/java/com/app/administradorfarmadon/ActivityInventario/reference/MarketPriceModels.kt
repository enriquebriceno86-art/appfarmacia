package com.app.administradorfarmadon.ActivityInventario.reference

/**
 * Representa una referencia de precio agregada para una presentaci\u00f3n espec\u00edfica.
 */
data class MarketPriceReference(
    val presentationName: String,     // ej: "Unidad", "Caja x 100"
    val equivalenceBase: Int,         // ej: 1, 100
    val averagePrice: Double,
    val minPrice: Double,
    val maxPrice: Double,
    val currency: String = "S/",
    val sourceCount: Int,
    val sources: List<MarketSourceItem>,
    val confidence: Int,              // 0-100
    val label: String,                // ej: "Referencia web", "Sugerencia IA"
    val isVerifiedWebSource: Boolean,
    val advertencia: String? = null
)

/**
 * Representa un item individual encontrado en una fuente web.
 */
data class MarketSourceItem(
    val storeName: String,
    val productName: String,
    val presentationText: String,
    val price: Double,
    val url: String,
    val confidenceMatch: Int = 100    // Qu\u00e9 tanto se parece el nombre al buscado
)

enum class MarketConfidenceLevel {
    HIGH,      // Varias fuentes consistentes
    MEDIUM,    // Pocas fuentes coherentes
    LOW,       // Una sola fuente o datos ruidosos
    NONE       // Sin datos suficientes
}
