package com.app.administradorfarmadon.ActivitysCaja

/**
 * HUMANO: Este coordinador solo decide la escena visual de apertura/cierre
 * y la visibilidad de acciones del header. No toca operaciones ni Firebase.
 */
object CajaOverlayCoordinator {

    data class CajaPanelScene(
        val mostrarApertura: Boolean? = null,
        val mostrarCierre: Boolean? = null,
        val nuevoCierreForzadoPendienteActivo: Boolean? = null,
        val limpiarObservacionCierre: Boolean = false
    )

    data class CajaToolbarScene(
        val mostrarBotonMenuMovil: Boolean,
        val mostrarCambiarCajaTablet: Boolean,
        val mostrarRegistrarEgresoTablet: Boolean,
        val mostrarCerrarTurnoTablet: Boolean,
        val mostrarResumenTurnoTablet: Boolean,
        val mostrarDevolucionTablet: Boolean
    )

    fun coordinarPreparacionVerificacion(): CajaPanelScene {
        return CajaPanelScene(
            mostrarApertura = null, // No forzamos ocultar; dejamos que se mantenga si ya estaba
            mostrarCierre = false,
            nuevoCierreForzadoPendienteActivo = false,
            limpiarObservacionCierre = true
        )
    }

    fun coordinarOverlayAperturaVisible(): CajaPanelScene {
        return CajaPanelScene(
            mostrarApertura = true
        )
    }

    fun coordinarPreparacionCierre(
        turnoPendienteDiaAnterior: Boolean
    ): CajaPanelScene {
        return CajaPanelScene(
            nuevoCierreForzadoPendienteActivo = turnoPendienteDiaAnterior
        )
    }

    fun coordinarMostrarCierre(): CajaPanelScene {
        return CajaPanelScene(
            mostrarCierre = true
        )
    }

    fun coordinarOcultarCierreInterno(): CajaPanelScene {
        return CajaPanelScene(
            mostrarCierre = false,
            nuevoCierreForzadoPendienteActivo = false,
            limpiarObservacionCierre = true
        )
    }

    fun coordinarToolbar(
        estadoUi: CajaEstadoUiBuilder.CajaEstadoUiModel,
        esTablet: Boolean
    ): CajaToolbarScene {
        return if (esTablet) {
            CajaToolbarScene(
                mostrarBotonMenuMovil = false,
                mostrarCambiarCajaTablet = estadoUi.mostrarCambiarCajaTablet,
                mostrarRegistrarEgresoTablet = estadoUi.mostrarRegistrarEgresoTablet,
                mostrarCerrarTurnoTablet = estadoUi.mostrarCerrarTurnoTablet,
                mostrarResumenTurnoTablet = estadoUi.mostrarResumenTurnoTablet,
                mostrarDevolucionTablet = estadoUi.mostrarDevolucionTablet
            )
        } else {
            CajaToolbarScene(
                mostrarBotonMenuMovil = estadoUi.mostrarBotonMenuMovil,
                mostrarCambiarCajaTablet = false,
                mostrarRegistrarEgresoTablet = false,
                mostrarCerrarTurnoTablet = false,
                mostrarResumenTurnoTablet = false,
                mostrarDevolucionTablet = false
            )
        }
    }
}
