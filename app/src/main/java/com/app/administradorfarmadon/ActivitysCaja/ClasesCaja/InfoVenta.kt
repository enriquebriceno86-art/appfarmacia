package com.app.administradorfarmadon.ActivitysCaja.ClasesCaja

data class InfoVenta(
    val id: String = "",
    val fecha: String = "",
    val metodoPago: String = "",
    val tipoComprobante: String = "",
    val total: String = "",
    val montoRecibido: String = "",
    val vuelto: String = "",
    val estado: String = "finalizada",
    val hora: String
)
