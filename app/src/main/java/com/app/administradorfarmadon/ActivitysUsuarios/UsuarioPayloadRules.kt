package com.app.administradorfarmadon.ActivitysUsuarios

import com.app.administradorfarmadon.ClasesDatabase.Trabajadores
import com.google.firebase.database.ServerValue

object UsuarioPayloadRules {

    fun construirPayloadCambioAcceso(
        nuevoEstado: String,
        idOperador: String,
        nombreOperador: String,
        timestampActualizacion: Long,
        motivoCambio: String
    ): UsuarioAccesoPayload {
        return UsuarioAccesoPayload(
            updates = linkedMapOf(
                "acceso" to nuevoEstado,
                "estadoAccesoActualizadoPorId" to idOperador,
                "estadoAccesoActualizadoPorNombre" to nombreOperador,
                "estadoAccesoActualizadoEn" to timestampActualizacion,
                "estadoAccesoActualizadoEnServidor" to ServerValue.TIMESTAMP,
                "motivoEstadoAcceso" to motivoCambio
            )
        )
    }

    fun construirPayloadAprobacionSolicitud(
        solicitud: SolicitudTrabajador
    ): SolicitudAprobacionPayload {
        val id = solicitud.id
        val trabajadorAprobado = Trabajadores(
            id = solicitud.id,
            usuario = solicitud.usuario,
            rol = solicitud.rol,
            contrasena = solicitud.contrasena,
            acceso = "true",
            documento = solicitud.documento
        )

        return SolicitudAprobacionPayload(
            id = id,
            trabajadorAprobado = trabajadorAprobado,
            updates = linkedMapOf(
                "/trabajadores/$id" to trabajadorAprobado,
                "/solicitudregistrotrabajador/$id" to null
            )
        )
    }

    fun construirPayloadRechazoSolicitud(
        solicitud: SolicitudTrabajador
    ): SolicitudRechazoPayload {
        return SolicitudRechazoPayload(id = solicitud.id)
    }
}
