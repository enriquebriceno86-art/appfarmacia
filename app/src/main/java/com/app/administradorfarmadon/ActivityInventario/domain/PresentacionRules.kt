package com.app.administradorfarmadon.ActivityInventario.domain

import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.PresentacionProducto
import com.app.administradorfarmadon.ClasesDatabase.PresentacionHelper
import java.util.Locale

/**
 * Reglas puras del dominio de presentaciones.
 *
 * No toca UI, no toca Firebase y no conoce Views.
 */
object PresentacionRules {

    enum class ModoControlStock {
        UNIDADES,
        PESO,
        VOLUMEN
    }

    fun modoDesdeUnidadBase(unidadBase: String): ModoControlStock {
        return when {
            unidadBase.equals("g", ignoreCase = true) ||
                unidadBase.equals("kg", ignoreCase = true) -> ModoControlStock.PESO
            unidadBase.equals("mL", ignoreCase = true) ||
                unidadBase.equals("L", ignoreCase = true) -> ModoControlStock.VOLUMEN
            else -> ModoControlStock.UNIDADES
        }
    }

    fun tipoBaseInventarioDesdeUnidadBase(unidadBase: String): String {
        return when (modoDesdeUnidadBase(unidadBase)) {
            ModoControlStock.UNIDADES -> "UNIDADES"
            ModoControlStock.PESO -> "PESO"
            ModoControlStock.VOLUMEN -> "VOLUMEN"
        }
    }

    fun normalizarTipoBaseInventario(
        tipoBaseInventario: String?,
        unidadBaseFallback: String = ""
    ): String {
        return when (tipoBaseInventario?.trim().orEmpty().uppercase(Locale.ROOT)) {
            "PESO" -> "PESO"
            "VOLUMEN" -> "VOLUMEN"
            "UNIDAD", "UNIDADES" -> "UNIDADES"
            else -> tipoBaseInventarioDesdeUnidadBase(unidadBaseFallback)
        }
    }

    fun nombreVisibleTipoBaseInventario(tipoBaseInventario: String?): String {
        return when (normalizarTipoBaseInventario(tipoBaseInventario)) {
            "PESO" -> "Peso"
            "VOLUMEN" -> "Volumen"
            else -> "Unidad"
        }
    }

    fun esUnidadPesoOVolumen(unidadBase: String): Boolean {
        return unidadBase.equals("g", ignoreCase = true) ||
            unidadBase.equals("kg", ignoreCase = true) ||
            unidadBase.equals("mL", ignoreCase = true) ||
            unidadBase.equals("L", ignoreCase = true)
    }

    fun opcionesUnidadBaseSegunModo(
        modo: ModoControlStock,
        unidadBaseOpcionesTodas: List<String>
    ): List<String> {
        return when (modo) {
            ModoControlStock.PESO -> listOf("g")
            ModoControlStock.VOLUMEN -> listOf("mL")
            ModoControlStock.UNIDADES -> {
                val permitidas = setOf(
                    "Unidad",
                    "Tableta",
                    "Capsula",
                    "Blister",
                    "Caja",
                    "Paquete",
                    "Pack",
                    "Par",
                    "Jeringa"
                )
                unidadBaseOpcionesTodas.filter { opcion ->
                    permitidas.any { it.equals(opcion, ignoreCase = true) }
                }
            }
        }
    }

    fun existePresentacionPrincipal(
        presentacionPrincipal: String,
        listaPresentaciones: List<PresentacionProducto>
    ): Boolean {
        return listaPresentaciones.any {
            PresentacionHelper.sonNombresEquivalentes(it.nombre, presentacionPrincipal)
        }
    }

    fun primeraPresentacionQueSuperaContenedor(
        listaPresentaciones: List<PresentacionProducto>,
        maxUnidadesPorContenedor: Int?
    ): PresentacionProducto? {
        val maximo = maxUnidadesPorContenedor?.takeIf { it > 0 } ?: return null
        return listaPresentaciones.firstOrNull { it.cantidad > maximo }
    }

    fun primeraPresentacionQueSuperaStockTotal(
        listaPresentaciones: List<PresentacionProducto>,
        stockTotalEnUnidadBase: Int
    ): PresentacionProducto? {
        if (stockTotalEnUnidadBase <= 0) return null
        return listaPresentaciones.firstOrNull { it.cantidad > stockTotalEnUnidadBase }
    }

    fun presentacionesSinPrecio(
        listaPresentaciones: List<PresentacionProducto>
    ): List<PresentacionProducto> {
        return listaPresentaciones.filter { it.precioventa <= 0.0 }
    }
}
