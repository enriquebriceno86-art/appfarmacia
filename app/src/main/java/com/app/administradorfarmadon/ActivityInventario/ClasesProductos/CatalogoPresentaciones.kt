package com.app.administradorfarmadon.ActivityInventario.ClasesProductos

import com.app.administradorfarmadon.ClasesDatabase.PresentacionHelper

/**
 * Catalogo fijo de presentaciones comerciales.
 *
 * Objetivo:
 * - Evitar nombres inventados o inconsistentes ("paquete" para tabletas si no aplica).
 * - Guiar al usuario a elegir presentaciones reales segun la unidad base del stock.
 *
 * Importante:
 * - Esto no reemplaza la equivalencia numerica (cantidad); la complementa.
 * - La lista es "sugerida y controlada". Si tu negocio requiere mas tipos, se agregan aqui.
 */
object CatalogoPresentaciones {

    private fun normalizar(input: String): String {
        return PresentacionHelper.normalizarClave(input)
    }

    fun opcionesParaUnidadBase(unidadBase: String): List<String> {
        val u = normalizar(unidadBase)

        return when (u) {
            "unidad" -> listOf("Unidad", "Caja", "Blister", "Paquete", "Pack")
            "tableta" -> listOf("Tableta", "Blister", "Caja", "Pack")
            "capsula" -> listOf("Capsula", "Blister", "Caja", "Pack")
            "ampolla" -> listOf("Ampolla", "Caja")
            "vial" -> listOf("Vial", "Caja")
            "frasco" -> listOf("Frasco", "Caja", "Pack")
            "sobre" -> listOf("Sobre", "Caja", "Bolsa", "Pack")
            "bolsa" -> listOf("Bolsa", "Caja", "Pack")
            "botella" -> listOf("Botella", "Caja", "Pack")
            "tubo" -> listOf("Tubo", "Caja", "Pack")
            "jeringa" -> listOf("Jeringa", "Caja")
            "par" -> listOf("Par", "Paquete", "Caja")

            // Productos vendidos por medida
            "g" -> listOf("g", "kg")
            "ml" -> listOf("mL", "L")

            // Genérico
            else -> listOf("Unidad", "Caja", "Blister", "Paquete", "Pack")
        }
    }

    fun esPresentacionValida(unidadBase: String, presentacion: String): Boolean {
        val opciones = opcionesParaUnidadBase(unidadBase)
        val clave = normalizar(presentacion)
        return opciones.any { normalizar(it) == clave }
    }

    fun catalogoFijoParaConfig(): List<String> {
        return listOf(
            "Unidad",
            "Tableta",
            "Capsula",
            "Blister",
            "Caja",
            "Frasco",
            "Ampolla",
            "Vial",
            "Sobre",
            "Bolsa",
            "Botella",
            "Tubo",
            "Jeringa",
            "Par",
            "Paquete",
            "Pack",
            "g",
            "kg",
            "mL",
            "L"
        )
    }
}
