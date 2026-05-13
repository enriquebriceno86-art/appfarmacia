package com.app.administradorfarmadon.ActivitysUsuarios

object UsuarioRemoteRules {

    fun construirTrabajadorRemoto(
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
            accesoNormalizado = UsuarioAccessRules.normalizarAcceso(acceso),
            documento = documento
        )
    }

    fun construirSolicitudRemota(
        snapshotId: String,
        usuario: String,
        rol: String,
        contrasena: String,
        documento: String,
        estado: String = "Pendiente"
    ): SolicitudSeguraData? {
        if (snapshotId.isBlank() && usuario.isBlank() && rol.isBlank()) return null

        return SolicitudSeguraData(
            id = snapshotId,
            usuario = usuario,
            rol = rol,
            contrasena = contrasena,
            documento = documento,
            estado = estado
        )
    }

    fun resolverEstadoSolicitudes(
        cantidadSolicitudes: Int,
        activityPage: Int
    ): SolicitudesUiResult {
        return SolicitudesUiResult(
            mostrarBotonSolicitudes = cantidadSolicitudes > 0,
            debeCerrarPantallaSolicitudes = cantidadSolicitudes == 0 && activityPage == 2
        )
    }
}
