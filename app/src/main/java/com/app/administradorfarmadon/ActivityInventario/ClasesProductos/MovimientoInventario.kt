package com.app.administradorfarmadon.ActivityInventario.ClasesProductos

/**
 * Representa un registro de entrada o salida en el Kardex del inventario.
 * Es la base de la auditoría profesional de la farmacia.
 */
data class MovimientoInventario(
    val id: String = "",
    val productoId: String = "",
    val loteId: String = "",           // ID del lote (si aplica)
    val numeroLote: String = "",       // Texto visual del lote
    val tipo: String = "",             // COMPRA, VENTA, AJUSTE_MERMA, AJUSTE_VENCIDO
    val cantidad: Double = 0.0,        // Cantidad del movimiento (ej: +50, -2)
    val stockAnterior: Double = 0.0,   // Stock antes del movimiento
    val stockResultante: Double = 0.0, // Stock después del movimiento
    val referencia: String = "",       // # Factura, # Ticket o Motivo
    val fecha: String = "",            // yyyy-MM-dd HH:mm:ss
    val usuarioNombre: String = "",    // Quién realizó el movimiento
    val unidadVisual: String = ""      // Unidad (Unidad, kg, L) para el historial
)
