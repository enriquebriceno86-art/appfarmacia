package com.app.administradorfarmadon.ActivitysUsuarios

import com.app.administradorfarmadon.ClasesDatabase.Trabajadores

object UsuarioAccessRules {

    fun decidirCambioAcceso(
        usuarioObjetivo: Trabajadores,
        contexto: UsuarioAccessContext
    ): UsuarioAccessDecision {
        if (usuarioObjetivo.id.isBlank()) {
            return UsuarioAccessDecision(
                permitido = false,
                mensajeError = "No se puede actualizar este usuario"
            )
        }

        if (usuarioObjetivo.id == contexto.idUsuarioActual) {
            return UsuarioAccessDecision(
                permitido = false,
                mensajeError = "No puedes bloquear tu propia cuenta"
            )
        }

        val yoSoyAdmin = contexto.rolUsuarioActual.equals("administrador", ignoreCase = true)
        val objetivoEsAdmin = usuarioObjetivo.rol.equals("administrador", ignoreCase = true)
        if (objetivoEsAdmin && !yoSoyAdmin) {
            return UsuarioAccessDecision(
                permitido = false,
                mensajeError = "Solo un administrador puede cambiar el acceso de otro administrador"
            )
        }

        val accesoActual = usuarioObjetivo.acceso.equals("true", ignoreCase = true)
        val nuevoEstado = if (accesoActual) "false" else "true"
        val descripcionCambio = if (nuevoEstado == "false") {
            "Se desactivó el acceso de ${usuarioObjetivo.usuario.ifBlank { usuarioObjetivo.id }}."
        } else {
            "Se activó nuevamente el acceso de ${usuarioObjetivo.usuario.ifBlank { usuarioObjetivo.id }}."
        }

        return UsuarioAccessDecision(
            permitido = true,
            accesoActual = accesoActual,
            nuevoEstado = nuevoEstado,
            motivoCambio = if (nuevoEstado == "false") "bloqueado_manual" else "reactivado_manual",
            descripcionCambio = descripcionCambio
        )
    }

    fun decidirAprobacionSolicitud(solicitud: SolicitudTrabajador): SolicitudDecision {
        return if (solicitud.id.isBlank()) {
            SolicitudDecision(
                permitido = false,
                mensajeError = "Error: id vacío"
            )
        } else {
            SolicitudDecision(permitido = true)
        }
    }

    fun decidirRechazoSolicitud(solicitud: SolicitudTrabajador): SolicitudDecision {
        return if (solicitud.id.isBlank()) {
            SolicitudDecision(
                permitido = false,
                mensajeError = "Error: ID vacío"
            )
        } else {
            SolicitudDecision(permitido = true)
        }
    }

    fun normalizarAcceso(valor: Any?): String {
        return when (valor) {
            is Boolean -> valor.toString()
            is Number -> (valor.toInt() != 0).toString()
            is String -> valor.trim().equals("true", ignoreCase = true).toString()
            else -> "false"
        }
    }

    fun construirTrabajadorSeguro(
        snapshotId: String,
        usuario: String,
        rol: String,
        contrasena: String,
        acceso: Any?,
        documento: String
    ): TrabajadorSeguroData? {
        if (snapshotId.isBlank() && usuario.isBlank() && rol.isBlank()) return null

        return TrabajadorSeguroData(
            id = snapshotId,
            usuario = usuario,
            rol = rol,
            contrasena = contrasena,
            accesoNormalizado = normalizarAcceso(acceso),
            documento = documento
        )
    }
}
