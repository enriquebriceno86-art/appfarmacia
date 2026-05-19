package com.app.administradorfarmadon.ActivitysPerfilItem

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties

enum class BusinessType(val displayName: String) {
    FARMACIA("Farmacia"),
    BOTICA("Botica"),
    DROGUERIA("Drogueria")
}

enum class StoreConfigSection(val displayName: String) {
    NEGOCIO("Negocio"),
    MERCADO("Mercado"),
    FISCAL("Fiscal"),
    OPERACION("Operacion")
}

@Keep
@IgnoreExtraProperties
data class StoreConfig(
    val nombreComercial: String = "",
    val razonSocial: String = "",
    val tipoNegocio: String = BusinessType.FARMACIA.name,
    val tipoDocumentoFiscal: String = "",
    val nroDocumentoFiscal: String = "",
    val pais: String = "",
    val estado: String = "",
    val ciudad: String = "",
    val direccion: String = "",
    val monedaCodigo: String = "",
    val monedaSimbolo: String = "",
    val cobraImpuestos: Boolean = false,
    val nombreImpuesto: String = "",
    val porcentajeImpuesto: Double = 0.0,
    val usaReferenciaComercialIA: Boolean = false,
    val usarMargenMinimo: Boolean = false,
    val margenMinimoDefault: Double = 0.0,
    val controlarLotes: Boolean = false,
    val controlarVencimientos: Boolean = false,
    val mensajeTicket: String = ""
)

@Keep
@IgnoreExtraProperties
data class MetodoPagoConfig(
    val id: String = "",
    val titulo: String = "",
    val categoria: String = "",
    val activo: Boolean = true,
    val permiteVuelto: Boolean = false,
    val solicitaMontoRecibido: Boolean = false,
    val calculaVuelto: Boolean = false,
    val permiteReferencia: Boolean = false,
    val usaQR: Boolean = false,
    val disponibleMixto: Boolean = false,
    val orden: Int = 0,
    val banco: String = "",
    val tipoCuenta: String = "",
    val numeroCuenta: String = "",
    val titularBanco: String = "",
    val documentoBanco: String = "",
    val telefonoBilletera: String = "",
    val titularBilletera: String = "",
    val aliasBilletera: String = "",
    val qrUrl: String = "",
    val instrucciones: String = "",
    val descripcion: String = "",
    val placeholder: String = "",
    val preload: Boolean = false
)

data class StoreConfigUIState(
    val isLoading: Boolean = false,
    val config: StoreConfig = StoreConfig(),
    val savedConfig: StoreConfig = StoreConfig(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val saveSuccessMessage: String? = null,
    val lastSavedSection: StoreConfigSection? = null,
    val savingSection: StoreConfigSection? = null,
    val dirtySections: Set<StoreConfigSection> = emptySet(),
    val activeSection: StoreConfigSection? = null,
    val errorMessage: String? = null,
    val fieldErrors: Map<String, String> = emptyMap()
)
