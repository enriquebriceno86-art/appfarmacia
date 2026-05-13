package com.app.administradorfarmadon.ActivitysPerfilItem

data class TiendaConfigFormData(
    val nombreTienda: String,
    val razonSocial: String,
    val identificacionFiscal: String,
    val telefono: String,
    val correo: String,
    val direccion: String,
    val ciudad: String,
    val estadoProvincia: String,
    val pais: String,
    val cobrarImpuestos: Boolean,
    val porcentajeImpuestoTexto: String,
    val monedaCodigoSeleccionado: String,
    val monedaSimboloSeleccionado: String,
    val monedaVisual: String,
    val simboloMonedaIngresado: String,
    val mensajeTicket: String,
    val cantidadTiposPago: Int
)

data class TiendaConfigValidationResult(
    val esValido: Boolean,
    val esValidoGeneral: Boolean,
    val esValidoUbicacion: Boolean,
    val esValidoConfiguracion: Boolean,
    val esValidoTiposPago: Boolean,
    val nombreTiendaError: String? = null,
    val razonSocialError: String? = null,
    val identificacionFiscalError: String? = null,
    val telefonoError: String? = null,
    val correoError: String? = null,
    val direccionError: String? = null,
    val ciudadError: String? = null,
    val estadoProvinciaError: String? = null,
    val paisError: String? = null,
    val monedaError: String? = null,
    val simboloMonedaError: String? = null,
    val mensajeTicketError: String? = null,
    val porcentajeImpuestoError: String? = null,
    val tiposPagoError: String? = null
)
