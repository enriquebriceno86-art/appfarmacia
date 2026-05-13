package com.app.administradorfarmadon.ActivitysPerfilItem

import android.util.Patterns

object TiendaConfigRules {

    private const val ERROR_CAMPO_OBLIGATORIO = "Campo obligatorio"
    private const val ERROR_CORREO_INVALIDO = "Correo no válido"
    private const val ERROR_PORCENTAJE_INVALIDO = "Porcentaje no válido"
    private const val ERROR_PORCENTAJE_RANGO = "Debe estar entre 0 y 100"
    private const val ERROR_TIPOS_PAGO = "Debe existir al menos un tipo de pago"

    fun validarFormulario(data: TiendaConfigFormData): TiendaConfigValidationResult {
        val nombreTienda = normalizarTexto(data.nombreTienda)
        val razonSocial = normalizarTexto(data.razonSocial)
        val identificacionFiscal = normalizarTexto(data.identificacionFiscal)
        val telefono = normalizarTexto(data.telefono)
        val correo = normalizarTexto(data.correo)
        val direccion = normalizarTexto(data.direccion)
        val ciudad = normalizarTexto(data.ciudad)
        val estadoProvincia = normalizarTexto(data.estadoProvincia)
        val pais = normalizarTexto(data.pais)
        val monedaVisual = normalizarTexto(data.monedaVisual)
        val simboloMoneda = normalizarTexto(data.simboloMonedaIngresado)
        val mensajeTicket = normalizarTexto(data.mensajeTicket)
        val porcentajeTexto = normalizarTexto(data.porcentajeImpuestoTexto)

        val nombreTiendaError = nombreTienda.errorSiBlank()
        val razonSocialError = razonSocial.errorSiBlank()
        val identificacionFiscalError = identificacionFiscal.errorSiBlank()
        val telefonoError = telefono.errorSiBlank()
        val correoError = when {
            correo.isBlank() -> ERROR_CAMPO_OBLIGATORIO
            !validarCorreo(correo) -> ERROR_CORREO_INVALIDO
            else -> null
        }

        val direccionError = direccion.errorSiBlank()
        val ciudadError = ciudad.errorSiBlank()
        val estadoProvinciaError = estadoProvincia.errorSiBlank()
        val paisError = pais.errorSiBlank()

        val monedaError = monedaVisual.errorSiBlank()
        val simboloMonedaError = simboloMoneda.errorSiBlank()
        val mensajeTicketError = mensajeTicket.errorSiBlank()
        val porcentajeImpuestoError = if (data.cobrarImpuestos) {
            when {
                porcentajeTexto.isBlank() -> ERROR_CAMPO_OBLIGATORIO
                porcentajeTexto.toDoubleOrNull() == null -> ERROR_PORCENTAJE_INVALIDO
                (porcentajeTexto.toDoubleOrNull() ?: 0.0) !in 0.0..100.0 -> ERROR_PORCENTAJE_RANGO
                else -> null
            }
        } else {
            null
        }

        val tiposPagoError = if (data.cantidadTiposPago <= 0) ERROR_TIPOS_PAGO else null

        val esValidoGeneral = listOf(
            nombreTiendaError,
            razonSocialError,
            identificacionFiscalError,
            telefonoError,
            correoError
        ).all { it == null }

        val esValidoUbicacion = listOf(
            direccionError,
            ciudadError,
            estadoProvinciaError,
            paisError
        ).all { it == null }

        val esValidoConfiguracion = listOf(
            monedaError,
            simboloMonedaError,
            mensajeTicketError,
            porcentajeImpuestoError
        ).all { it == null }

        val esValidoTiposPago = tiposPagoError == null

        return TiendaConfigValidationResult(
            esValido = esValidoGeneral && esValidoUbicacion && esValidoConfiguracion && esValidoTiposPago,
            esValidoGeneral = esValidoGeneral,
            esValidoUbicacion = esValidoUbicacion,
            esValidoConfiguracion = esValidoConfiguracion,
            esValidoTiposPago = esValidoTiposPago,
            nombreTiendaError = nombreTiendaError,
            razonSocialError = razonSocialError,
            identificacionFiscalError = identificacionFiscalError,
            telefonoError = telefonoError,
            correoError = correoError,
            direccionError = direccionError,
            ciudadError = ciudadError,
            estadoProvinciaError = estadoProvinciaError,
            paisError = paisError,
            monedaError = monedaError,
            simboloMonedaError = simboloMonedaError,
            mensajeTicketError = mensajeTicketError,
            porcentajeImpuestoError = porcentajeImpuestoError,
            tiposPagoError = tiposPagoError
        )
    }

    fun construirConfiguracionNormalizada(
        data: TiendaConfigFormData,
        monedasDisponibles: List<Moneda>
    ): ConfiguracionTienda {
        val monedaVisual = normalizarTexto(data.monedaVisual)
        val simboloMoneda = normalizarTexto(data.simboloMonedaIngresado)
        val codigoMonedaFinal = if (data.monedaCodigoSeleccionado.isNotBlank()) {
            normalizarTexto(data.monedaCodigoSeleccionado)
        } else {
            obtenerCodigoDesdeTextoMoneda(monedaVisual, monedasDisponibles)
        }

        val simboloMonedaFinal = if (simboloMoneda.isNotEmpty()) {
            simboloMoneda
        } else {
            monedasDisponibles
                .firstOrNull { it.codigo == codigoMonedaFinal }
                ?.simbolo
                .orEmpty()
        }

        return ConfiguracionTienda(
            nombreTienda = normalizarTexto(data.nombreTienda),
            razonSocial = normalizarTexto(data.razonSocial),
            identificacionFiscal = normalizarTexto(data.identificacionFiscal),
            telefono = normalizarTexto(data.telefono),
            correo = normalizarTexto(data.correo),
            direccion = normalizarTexto(data.direccion),
            ciudad = normalizarTexto(data.ciudad),
            estadoProvincia = normalizarTexto(data.estadoProvincia),
            pais = normalizarTexto(data.pais),
            monedaVisual = monedaVisual,
            monedaCodigo = codigoMonedaFinal,
            monedaSimbolo = simboloMonedaFinal,
            cobrarImpuestos = data.cobrarImpuestos,
            porcentajeImpuesto = if (data.cobrarImpuestos) {
                normalizarTexto(data.porcentajeImpuestoTexto).toDoubleOrNull() ?: 0.0
            } else {
                0.0
            },
            mensajeTicket = normalizarTexto(data.mensajeTicket)
        )
    }

    fun obtenerCodigoDesdeTextoMoneda(
        texto: String,
        monedasDisponibles: List<Moneda>
    ): String {
        val textoNormalizado = normalizarTexto(texto)
        return monedasDisponibles.firstOrNull { moneda ->
            val etiqueta = "${moneda.nombre} (${moneda.codigo})"
            normalizarTexto(etiqueta) == textoNormalizado ||
                normalizarTexto(moneda.codigo) == textoNormalizado ||
                normalizarTexto(moneda.nombre) == textoNormalizado
        }?.codigo.orEmpty()
    }

    fun normalizarConfiguracion(configuracion: ConfiguracionTienda): ConfiguracionTienda {
        return configuracion.copy(
            nombreTienda = normalizarTexto(configuracion.nombreTienda),
            razonSocial = normalizarTexto(configuracion.razonSocial),
            identificacionFiscal = normalizarTexto(configuracion.identificacionFiscal),
            telefono = normalizarTexto(configuracion.telefono),
            correo = normalizarTexto(configuracion.correo),
            direccion = normalizarTexto(configuracion.direccion),
            ciudad = normalizarTexto(configuracion.ciudad),
            estadoProvincia = normalizarTexto(configuracion.estadoProvincia),
            pais = normalizarTexto(configuracion.pais),
            monedaVisual = normalizarTexto(configuracion.monedaVisual),
            monedaCodigo = normalizarTexto(configuracion.monedaCodigo),
            monedaSimbolo = normalizarTexto(configuracion.monedaSimbolo),
            mensajeTicket = normalizarTexto(configuracion.mensajeTicket)
        )
    }

    private fun validarCorreo(correo: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(correo).matches()
    }

    private fun String.errorSiBlank(): String? = if (isBlank()) ERROR_CAMPO_OBLIGATORIO else null

    private fun normalizarTexto(texto: String): String = texto.trim()
}
