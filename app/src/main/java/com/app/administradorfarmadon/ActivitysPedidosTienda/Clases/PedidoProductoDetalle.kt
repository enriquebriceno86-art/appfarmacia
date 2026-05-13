package com.app.administradorfarmadon.ActivitysPedidosTienda.Clases

data class PedidoProductoDetalle(
    var nombre: String = "",
    var cantidad: String = "",
    var presentacion: String = "",
    var descripcionPresentacion: String = "",
    var requiereReceta: Boolean = false,
    var recetaUrl: String = "",
    var vencimiento: String = "",
    var imagen: String = "",
    var precio: String = ""
)
