package com.app.administradorfarmadon.ActivityFragmentos.Reciclers

import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.PresentacionProducto

/**
 * Modelo de UI para mostrar un producto agrupado con sus presentaciones
 * en los resultados de búsqueda inline de la caja.
 */
data class ProductoAgrupadoUi(
    val producto: MoldeProductos,
    val presentaciones: List<PresentacionUi>
)

/**
 * Modelo de UI para una presentación individual dentro de un producto agrupado.
 */
data class PresentacionUi(
    val presentacion: PresentacionProducto,
    val lote: String = "",
    val expiracion: String = "",
    val stockTotal: Int = 0,
    val precio: Double = 0.0,
    val nombrePresentacion: String = ""
)
