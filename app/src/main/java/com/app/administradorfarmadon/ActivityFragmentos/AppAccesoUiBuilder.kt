package com.app.administradorfarmadon.ActivityFragmentos

/**
 * HUMANO: Este builder transforma los flags actuales de acceso
 * en un único modelo de UI para que la Activity solo aplique visibilidad.
 */
object AppAccesoUiBuilder {

    data class AppAccesoUiModel(
        val bloqueoPrioritario: AppAccesoRules.BloqueoPrioritario,
        val bloquearNavegacion: Boolean,
        val mostrarOverlaySinConexion: Boolean,
        val mostrarOverlayUsuarioInactivo: Boolean,
        val mostrarOverlayConfigTienda: Boolean,
        val mostrarOverlayHorario: Boolean,
        val mostrarContenidoPrincipal: Boolean
    )

    data class AppOverlaysVisualModel(
        val mostrarOverlaySinConexion: Boolean,
        val mostrarOverlayUsuarioInactivo: Boolean,
        val mostrarOverlayConfigTienda: Boolean,
        val mostrarOverlayHorario: Boolean
    )

    data class AppContenidoVisualModel(
        val mostrarContenidoPrincipal: Boolean
    )

    fun construirEstadoAcceso(
        sinConexion: Boolean,
        bloqueadoPorUsuarioInactivo: Boolean,
        bloqueadoPorConfigTienda: Boolean,
        bloqueadoPorHorario: Boolean
    ): AppAccesoUiModel {
        val bloqueoPrioritario = AppAccesoRules.resolverBloqueoPrioritario(
            sinConexion = sinConexion,
            bloqueadoPorUsuarioInactivo = bloqueadoPorUsuarioInactivo,
            bloqueadoPorConfigTienda = bloqueadoPorConfigTienda,
            bloqueadoPorHorario = bloqueadoPorHorario
        )

        return AppAccesoUiModel(
            bloqueoPrioritario = bloqueoPrioritario,
            bloquearNavegacion = AppAccesoRules.debeBloquearNavegacion(bloqueoPrioritario),
            mostrarOverlaySinConexion = bloqueoPrioritario == AppAccesoRules.BloqueoPrioritario.SIN_CONEXION,
            mostrarOverlayUsuarioInactivo = bloqueoPrioritario == AppAccesoRules.BloqueoPrioritario.USUARIO_INACTIVO,
            mostrarOverlayConfigTienda = bloqueoPrioritario == AppAccesoRules.BloqueoPrioritario.CONFIG_TIENDA,
            mostrarOverlayHorario = bloqueoPrioritario == AppAccesoRules.BloqueoPrioritario.HORARIO,
            mostrarContenidoPrincipal = bloqueoPrioritario == AppAccesoRules.BloqueoPrioritario.NINGUNO
        )
    }

    fun construirOverlaysVisuales(
        uiModel: AppAccesoUiModel
    ): AppOverlaysVisualModel {
        return AppOverlaysVisualModel(
            mostrarOverlaySinConexion = uiModel.mostrarOverlaySinConexion,
            mostrarOverlayUsuarioInactivo = uiModel.mostrarOverlayUsuarioInactivo,
            mostrarOverlayConfigTienda = uiModel.mostrarOverlayConfigTienda,
            mostrarOverlayHorario = uiModel.mostrarOverlayHorario
        )
    }

    fun construirContenidoVisual(
        uiModel: AppAccesoUiModel
    ): AppContenidoVisualModel {
        return AppContenidoVisualModel(
            mostrarContenidoPrincipal = uiModel.mostrarContenidoPrincipal
        )
    }
}
