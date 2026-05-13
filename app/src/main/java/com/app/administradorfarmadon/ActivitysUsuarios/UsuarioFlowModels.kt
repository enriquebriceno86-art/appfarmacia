package com.app.administradorfarmadon.ActivitysUsuarios

import com.app.administradorfarmadon.ClasesDatabase.Trabajadores

data class UsuarioAccessContext(
    val idUsuarioActual: String,
    val rolUsuarioActual: String,
    val nombreOperador: String
)

data class UsuarioAccessDecision(
    val permitido: Boolean,
    val mensajeError: String? = null,
    val accesoActual: Boolean = false,
    val nuevoEstado: String = "",
    val motivoCambio: String = "",
    val descripcionCambio: String = ""
)

data class SolicitudDecision(
    val permitido: Boolean,
    val mensajeError: String? = null
)

data class TrabajadorSeguroData(
    val id: String,
    val usuario: String,
    val rol: String,
    val contrasena: String,
    val accesoNormalizado: String,
    val documento: String
)

data class SolicitudSeguraData(
    val id: String,
    val usuario: String,
    val rol: String,
    val contrasena: String,
    val documento: String,
    val estado: String = "Pendiente"
)

data class SolicitudesUiResult(
    val mostrarBotonSolicitudes: Boolean,
    val debeCerrarPantallaSolicitudes: Boolean
)

data class BuscadorUiState(
    val mostrarBuscador: Boolean,
    val mostrarIconoBusqueda: Boolean
)

data class PantallaUsuariosUiState(
    val mostrarSolicitudes: Boolean,
    val mostrarListaUsuarios: Boolean,
    val mostrarBotonSolicitudes: Boolean
)

data class UsuarioAccesoPayload(
    val updates: Map<String, Any>
)

data class SolicitudAprobacionPayload(
    val id: String,
    val trabajadorAprobado: Trabajadores,
    val updates: Map<String, Any?>
)

data class SolicitudRechazoPayload(
    val id: String
)
