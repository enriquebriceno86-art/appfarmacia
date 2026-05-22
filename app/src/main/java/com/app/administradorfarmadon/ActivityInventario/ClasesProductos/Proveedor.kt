package com.app.administradorfarmadon.ActivityInventario.ClasesProductos

/**
 * Representa un proveedor de la farmacia.
 */
data class Proveedor(
    val id: String = "",
    val nombre: String = "",         // Razón Social
    val contacto: String = "",       // Persona de contacto o teléfono
    val idFiscal: String = "",       // RIF, RUC, NIT, etc.
    val direccion: String = "",
    val ultimaFactura: String = ""   // Datos de referencia rápida
) : java.io.Serializable
