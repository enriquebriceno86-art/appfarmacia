package com.app.administradorfarmadon.ClasesDatabase

import java.util.Locale

/**
 * Formatea la unidad base para mostrarla en la UI de forma consistente.
 *
 * Reglas:
 * - g  -> "g"
 * - mL -> "mL"
 * - Unidad -> "unidad" / "unidades" segun cantidad
 * - Otros (Tableta, Capsula, Frasco, Botella, etc.) -> en minuscula (sin inventar plurales raros)
 */
object UnidadBaseHelper {

    fun formatear(unidadBaseRaw: String?, cantidad: Int? = null): String {
        val unidad = unidadBaseRaw?.trim().orEmpty()

        if (unidad.isBlank()) {
            return if (cantidad == 1) "unidad" else "unidades"
        }

        return when {
            unidad.equals("g", ignoreCase = true) -> "g"
            unidad.equals("kg", ignoreCase = true) -> "kg"
            unidad.equals("ml", ignoreCase = true) -> "mL"
            unidad.equals("l", ignoreCase = true) -> "L"
            unidad.equals("unidad", ignoreCase = true) -> if (cantidad == 1) "unidad" else "unidades"
            else -> unidad.lowercase(Locale.getDefault())
        }
    }
}
