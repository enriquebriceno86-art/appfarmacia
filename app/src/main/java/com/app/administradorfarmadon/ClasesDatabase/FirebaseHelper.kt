package com.app.administradorfarmadon.ClasesDatabase

import com.google.firebase.database.DataSnapshot

/**
 * Utilidades centralizadas para la lectura segura de datos desde Firebase Data
 * Snapshot.
 */

/**
 * Obtiene el valor como String, eliminando espacios en blanco.
 * Si es nulo, devuelve una cadena vacía.
 */
fun DataSnapshot.obtenerTexto(): String {
    return value?.toString().orEmpty().trim()
}

/**
 * Intenta convertir un valor de Firebase (Number o String) a Double de forma flexible.
 * Maneja casos donde el decimal viene con coma o punto.
 */
fun DataSnapshot.obtenerDoubleFlexible(): Double? {
    // Si el valor no existe en Firebase, retornamos null de inmediato
    val v = value ?: return null

    return when (v) {
        is Number -> v.toDouble()
        is String -> v.toDoubleSeguro(null)
        // Si no es ni número ni texto, no intentamos forzarlo, devolvemos null
        else -> null
    }
}

/**
 * Limpia un String de símbolos de moneda y espacios, luego intenta convertirlo a Double.
 * Soporta coma y punto como separadores decimales.
 */
fun String?.toDoubleSeguro(default: Double? = 0.0): Double? {
    if (this == null) return default
    return try {
        val limpio = this.trim()
            .replace("$", "")
            .replace(" ", "")
            .replace(",", ".")
            .replace("[^0-9.-]".toRegex(), "")
        limpio.toDoubleOrNull() ?: default
    } catch (e: Exception) {
        default
    }
}

/**
 * Intenta convertir un valor de Firebase a Long de forma flexible.
 */
fun DataSnapshot.obtenerLongFlexible(): Long? {
    return when (val v = value) {
        is Long -> v
        is Int -> v.toLong()
        is String -> v.trim().toLongOrNull()
        else -> v?.toString()?.trim()?.toLongOrNull()
    }
}

/**
 * Intenta convertir un valor de Firebase a Int de forma flexible.
 */
fun DataSnapshot.obtenerIntFlexible(): Int? {
    return when (val v = value) {
        is Int -> v
        is Long -> v.toInt()
        is String -> v.trim().toIntOrNull()
        else -> v?.toString()?.trim()?.toIntOrNull()
    }
}
