package com.app.administradorfarmadon.ActivityInventario.ClasesProductos

data class LoteProducto(
    val numero: String = "",          // Ej: "L001", "AB-2024-05"
    val vencimiento: String = "",     // Formato MM/AA, ej: "01/26"
    val cantidad: Double = 0.0,       // Cantidad ingresada en este lote
    val cantidadBloqueada: Double = 0.0,
    val fecha: String = "",           // Fecha de ingreso "yyyy-MM-dd"
    val costoCompraUnitario: Double = 0.0,
    val costoUltimoIngreso: Double = 0.0,
    val costoUltimoIngresoUnitario: Double = 0.0,
    val observaciones: String = "",
    val motivoBloqueo: String = "",
    val timestampUltimoBloqueo: Long = 0L,
    val nroFactura: String = "",
    val condicionPago: String = "CONTADO", // CONTADO o CREDITO
    val fechaVencimientoPago: String = "",
    val estadoPago: String = "PAGADO"     // PAGADO o PENDIENTE
)
