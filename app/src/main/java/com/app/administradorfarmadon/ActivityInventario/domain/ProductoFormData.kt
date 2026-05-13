package com.app.administradorfarmadon.ActivityInventario.domain

import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.PresentacionProducto

/**
 * Snapshot liviano del formulario.
 *
 * La idea es aislar las validaciones del Activity para que Crear/Editar
 * solo armen este objeto y luego deleguen las reglas.
 */
data class ProductoFormData(
    val nombre: String = "",
    val vencimiento: String = "",
    val categoria: String = "",
    val costoCompraTexto: String = "",
    val requiereCostoCompraBase: Boolean = true,
    val unidadBase: String = "",
    val usaPresentaciones: Boolean = false,
    val presentacionPrincipal: String = "",
    val presentacionPrincipalElegidaManualmente: Boolean = false,
    val registroPorUnidades: Boolean = false,
    val registroPorPaquetes: Boolean = false,
    val cantidadTexto: String = "",
    val stockMinimoTexto: String = "",
    val cantidadPresentacionesTexto: String = "",
    val unidadesPorPresentacionTexto: String = "",
    val stockMinimoPresentacionTexto: String = "",
    val listaPresentaciones: List<PresentacionProducto> = emptyList(),
    val stockTotalEnUnidadBase: Int = 0,
    val maxUnidadesPorContenedor: Int? = null,
    val validarPrecioPresentaciones: Boolean = false,
    val requiereVencimientoGeneral: Boolean = true
) {
    val costoCompra: Double?
        get() = costoCompraTexto.trim().replace(",", ".").toDoubleOrNull()

    val cantidad: Int?
        get() = cantidadTexto.trim().toIntOrNull()

    val stockMinimo: Int?
        get() = stockMinimoTexto.trim().toIntOrNull()

    val cantidadPresentaciones: Int?
        get() = cantidadPresentacionesTexto.trim().toIntOrNull()

    val unidadesPorPresentacion: Int?
        get() = unidadesPorPresentacionTexto.trim().toIntOrNull()

    val stockMinimoPresentacion: Int?
        get() = stockMinimoPresentacionTexto.trim().toIntOrNull()
}
