package com.app.administradorfarmadon.ClasesDatabase

import kotlin.math.round

object CalculadoraVentasHelper {

    /**
     * Redondea un valor Double a 2 decimales para evitar errores de precisión binaria.
     */
    fun redondear(valor: Double): Double {
        return round(valor * 100.0) / 100.0
    }

    /**
     * Calcula el total asegurando que no existan valores negativos ni descuentos ilógicos.
     * Aplica redondeo estricto a 2 decimales.
     */
    fun calcularTotalConDescuento(
        cantidad: Int,
        precioUnitario: Double,
        descuento: Double,
        tipoDescuento: String
    ): Double {
        // 1. Evitar cantidades o precios negativos por errores de base de datos
        val cantSegura = cantidad.coerceAtLeast(0)
        val precioSeguro = precioUnitario.coerceAtLeast(0.0)
        val descuentoSeguro = descuento.coerceAtLeast(0.0)

        val bruto = redondear(cantSegura * precioSeguro)

        // 2. Cálculo protegido con redondeo
        val total = when (tipoDescuento) {
            "pct" -> {
                // Limita el porcentaje a un máximo de 99.9% (no regalar el producto)
                val pctSeguro = descuentoSeguro.coerceAtMost(99.9)
                bruto * (1.0 - (pctSeguro / 100.0))
            }
            "monto" -> {
                // Evita que el total sea menor a 0 si el descuento fijo es muy alto
                (bruto - (descuentoSeguro * cantSegura)).coerceAtLeast(0.0)
            }
            else -> bruto
        }
        
        return redondear(total)
    }

    /**
     * Validador para la Interfaz de Usuario (UI). Devuelve el mensaje de error o null si es válido.
     */
    fun validarDescuentoPermitido(precioUnitario: Double, descuento: Double, tipoDescuento: String): String? {
        if (descuento <= 0.0) return "Ingresa un valor mayor a cero"
        
        return when (tipoDescuento) {
            "pct" -> if (descuento >= 100.0) "El descuento no puede ser del 100% o más" else null
            "monto" -> if (descuento >= precioUnitario) "El descuento supera el precio del producto" else null
            else -> "Tipo de descuento no válido"
        }
    }
}
