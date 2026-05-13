package com.app.administradorfarmadon.ClasesDatabase

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

/**
 * Utility for uniform currency formatting throughout the app.
 * Uses SessionManager configuration for symbol and currency code.
 */
object MonedaHelper {
    
    private val format = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))

    private fun simbolo(): String = SessionManager.monedaSimbolo.ifBlank { "S/" }
    private fun codigo(): String = SessionManager.monedaCodigo.ifBlank { "PEN" }

    fun parsear(texto: String): Double? {
        // Eliminar todo lo que no sea dígito, punto o coma
        val limpio = texto
            .replace(Regex("[^\\d,.-]"), "")
            .replace(",", ".")
            // Eliminar más de un punto decimal
            .replace(Regex("(?<=\\..*)\\."), "")
        return limpio.toDoubleOrNull()
    }

    /** Símbolo de la moneda activa (ej. "S/", "$"). Útil para prefijos de inputs. */
    fun simboloMoneda(): String = simbolo()

    /**
     * Formats a double value: "S/ 1,250.00 (PEN)"
     */
    fun formatear(monto: Any?): String {
        val valor = when (monto) {
            is Number -> monto.toDouble()
            is String -> monto.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
        return "${simbolo()} ${format.format(valor)} (${codigo()})"
    }

    /**
     * Formats with sign for balance/movements: "+ S/ 50.00" or "- S/ 20.00"
     */
    fun formatearConSigno(monto: Double): String {
        val signo = when {
            monto > 0.009 -> "+ "
            monto < -0.009 -> "- "
            else -> ""
        }
        return "$signo${simbolo()} ${format.format(abs(monto))}"
    }
    
    /**
     * Simple format without currency code: "S/ 1,250.00"
     */
    fun formatearSimple(monto: Double): String {
        return "${simbolo()} ${format.format(monto)}"
    }

    /**
     * Numeric format without currency symbol/code: "1,250.00"
     */
    fun formatearNumero(monto: Any?): String {
        val valor = when (monto) {
            is Number -> monto.toDouble()
            is String -> monto.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
        return format.format(valor)
    }
}
