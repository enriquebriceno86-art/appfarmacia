package com.app.administradorfarmadon.ActivitysCaja

import com.app.administradorfarmadon.ClasesDatabase.FechaHoraServidorHelper

object HistorialVentasRemoteRules {

    fun construirVentaHistorial(
        data: VentaHistorialRemoteData
    ): VentaHistorial {
        return VentaHistorial(
            id = data.idVenta,
            cantidadProductos = data.cantidadProductos,
            total = parsearMonto(data.totalRaw) ?: 0.0,
            metodoPago = normalizarTexto(data.metodoPago),
            estado = normalizarTexto(data.estado),
            fecha = if (data.timestampVentaServidor > 0L) {
                FechaHoraServidorHelper.formatearFechaFirebase(data.timestampVentaServidor)
            } else {
                normalizarTexto(data.fecha)
            },
            hora = if (data.timestampVentaServidor > 0L) {
                FechaHoraServidorHelper.formatearHora(data.timestampVentaServidor)
            } else {
                normalizarTexto(data.hora)
            }
        )
    }

    fun parsearMonto(raw: Any?): Double? {
        return when (raw) {
            is Double -> raw
            is Long -> raw.toDouble()
            is Int -> raw.toDouble()
            is String -> raw.toDoubleOrNull()
            else -> null
        }
    }

    fun parsearCantidad(raw: Any?): Int {
        return when (raw) {
            is Int -> raw
            is Long -> raw.toInt()
            is Double -> raw.toInt()
            is String -> raw.toIntOrNull() ?: 0
            else -> 0
        }
    }

    fun normalizarTexto(valor: String): String {
        return valor
    }
}
