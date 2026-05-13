package com.app.administradorfarmadon.AdaptersCaja

import com.app.administradorfarmadon.ActivitysCaja.ProductoCaja

/**
 * Modelos de datos para la UI de Caja.
 * Extraídos para reducir la complejidad de FragmentoCaja.kt.
 */

data class ResumenPagoTurnoItem(
    val titulo: String,
    val monto: Double
)

data class TurnoDetectado(
    val fecha: String,
    val idTurno: String,
    val montoApertura: Double
)

data class VentaEnEsperaUi(
    val id: String,
    val titulo: String,
    val total: Double,
    val cantidadProductos: Int,
    val hora: String,
    val nombreUsuario: String,
    val timestamp: Long
)

data class OpcionCorreccionPresentacionCaja(
    val nombreUi: String,
    val detalleUi: String,
    val nombrePresentacionGuardada: String,
    val unidadesPorPresentacion: Int,
    val precioUnitario: Double,
    val cantidadSugerida: Int
)

data class VentaPendienteReversion(
    val idVenta: String,
    val operationId: String,
    val productos: List<ProductoCaja>
)
