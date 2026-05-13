package com.app.administradorfarmadon.ActivitysCaja

data class ProductoCaja(
    val id: String = "",
    val indiceProducto: String = "",
    var nombre: String? = "",
    val categoria: String = "",
    val presentacion: String = "",
    var cantidad: String = "",
    val unidadesPorPresentacion: String = "",
    var precioUnitario: String = "",
    var total: String = "",
    var agotado: Boolean = false,
    var stockDisponibleActual: String = "",
    var mensajeStockActual: String = "",
    var loteNumero: String = "",
    var loteClaveConsumida: String = "",
    var loteVencimiento: String = "",
    var loteNumeroFefo: String = "",
    var loteVencimientoFefo: String = "",
    var loteSeleccionManual: Boolean = false,
    var lotesConsumidosDetalle: Map<String, LoteConsumoDetalleCaja> = emptyMap(),
    var descuento: Double = 0.0,
    var tipoDescuento: String = ""
)

data class LoteConsumoDetalleCaja(
    val clave: String = "",
    val numero: String = "",
    val vencimiento: String = "",
    val cantidad: Int = 0
)
