package com.app.administradorfarmadon.ActivityInventario.reference

import java.text.Normalizer
import java.util.Locale

/**
 * Utilidades para normalizar nombres de productos y asegurar consistencia en el sistema.
 */

/**
 * Normaliza el nombre de un producto para comparaciones y búsquedas.
 * Elimina acentos, convierte a minúsculas y limpia espacios extra.
 */
fun normalizeProductName(name: String): String {
    if (name.isBlank()) return ""
    
    val sinAcentos = Normalizer.normalize(name, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .lowercase(Locale.ROOT)
        .trim()
        
    // Eliminar caracteres especiales no deseados para búsqueda (manteniendo alfanuméricos y espacios)
    return sinAcentos.replace(Regex("[^a-z0-9 ]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}
