package com.app.administradorfarmadon.ActivityInventario.domain

/**
 * Reune calculos puros del formulario de inventario.
 *
 * La idea es que Crear y Editar solo lean valores desde la UI y deleguen aqui
 * la conversion a stock/base en unidad real, sin depender de Views ni Binding.
 */
object ProductoInventarioMetrics {

    fun obtenerStockActualEnUnidadBase(
        stockTag: Int?,
        stockTexto: String
    ): Int {
        return stockTag
            ?: stockTexto.trim().toIntOrNull()
            ?: 0
    }

    fun obtenerBasePorUnidadActual(
        unidadBase: String,
        esPorPaquetes: Boolean,
        valorPaqueteTexto: String,
        basePorUnidadTexto: String?,
        usarCampoBasePorUnidad: Boolean,
        basePorUnidadFallback: Double = 0.0
    ): Double {
        val esMlOg = unidadBase.equals("mL", ignoreCase = true) ||
            unidadBase.equals("g", ignoreCase = true)

        return when {
            esMlOg && esPorPaquetes ->
                valorPaqueteTexto.trim().replace(",", ".").toDoubleOrNull() ?: 0.0

            usarCampoBasePorUnidad ->
                basePorUnidadTexto.orEmpty().trim().replace(",", ".").toDoubleOrNull() ?: 0.0

            else -> basePorUnidadFallback
        }
    }

    fun obtenerStockMinimoEnUnidadBase(
        unidadBase: String,
        esPorPaquetes: Boolean,
        stockMinimoTexto: String,
        stockMinimoContenedoresTexto: String,
        valorPorContenedorTexto: String,
        basePorUnidadActual: Double,
        stockMinimoTag: Int? = null,
        valorPorContenedorFallback: Int = 0,
        multiplicarPorBaseSiNoEsMlOg: Boolean = false
    ): Int {
        if (!esPorPaquetes) {
            return stockMinimoTag
                ?: stockMinimoTexto.trim().toIntOrNull()
                ?: 0
        }

        val stockMinContenedores = stockMinimoContenedoresTexto.trim().toIntOrNull() ?: 0
        val valorPorContenedor = valorPorContenedorTexto.trim().toIntOrNull()
            ?: valorPorContenedorFallback
        val totalEnUnidadBase = stockMinContenedores * valorPorContenedor

        val esMlOg = unidadBase.equals("mL", ignoreCase = true) ||
            unidadBase.equals("g", ignoreCase = true)

        return if (!esMlOg && multiplicarPorBaseSiNoEsMlOg && basePorUnidadActual > 0.0) {
            (totalEnUnidadBase * basePorUnidadActual).toInt()
        } else {
            totalEnUnidadBase
        }
    }
}
