package com.app.administradorfarmadon.ActivityFragmentos

/**
 * HUMANO: Este coordinador convierte el estado global de acceso ya resuelto
 * en una escena visual simple para overlays y contenido principal.
 */
object AppBloqueoOverlayCoordinator {

    data class AppBloqueoOverlayScene(
        val mostrarOverlaySinConexion: Boolean,
        val mostrarOverlayUsuarioInactivo: Boolean,
        val mostrarOverlayConfigTienda: Boolean,
        val mostrarOverlayHorario: Boolean,
        val mostrarContenidoPrincipal: Boolean
    )

    fun coordinarEscena(
        uiModel: AppAccesoUiBuilder.AppAccesoUiModel
    ): AppBloqueoOverlayScene {
        val overlays = AppAccesoUiBuilder.construirOverlaysVisuales(uiModel)
        val contenido = AppAccesoUiBuilder.construirContenidoVisual(uiModel)

        return AppBloqueoOverlayScene(
            mostrarOverlaySinConexion = overlays.mostrarOverlaySinConexion,
            mostrarOverlayUsuarioInactivo = overlays.mostrarOverlayUsuarioInactivo,
            mostrarOverlayConfigTienda = overlays.mostrarOverlayConfigTienda,
            mostrarOverlayHorario = overlays.mostrarOverlayHorario,
            mostrarContenidoPrincipal = contenido.mostrarContenidoPrincipal
        )
    }
}
