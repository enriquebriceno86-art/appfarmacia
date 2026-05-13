package com.app.administradorfarmadon.ActivitysCaja

data class VentaHistorialRemoteData(
    val idVenta: String,
    val totalRaw: Any?,
    val metodoPago: String,
    val estado: String,
    val fecha: String,
    val hora: String,
    val timestampVentaServidor: Long,
    val cantidadProductos: Int
)

data class VentaHistorialParseada(
    val id: String,
    val cantidadProductos: Int,
    val total: Double,
    val metodoPago: String,
    val estado: String,
    val fecha: String,
    val hora: String
)

data class HistorialVentasResumen(
    val cantidadVentas: Int,
    val totalDia: Double,
    val promedio: Double
)

data class HistorialVentasUiState(
    val listaVacia: Boolean,
    val puedeExportar: Boolean
)

data class VentaDetalleRemoteData(
    val idVentaFallback: String,
    val infoVentaOriginal: InfoVenta,
    val timestampVentaServidor: Long
)
