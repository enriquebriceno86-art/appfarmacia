package com.app.administradorfarmadon.ActivitysCaja

/**
 * HUMANO: Reglas puras y pequeñas del estado operativo de la caja.
 * Aquí vive solo la decisión booleana; FragmentoCaja sigue mostrando
 * mensajes, navegando y aplicando UI.
 */
object CajaTurnoRules {

    fun puedeOperarTurnoActual(
        verificacionTurnoDisponible: Boolean,
        turnoPendienteDiaAnterior: Boolean
    ): Boolean {
        return verificacionTurnoDisponible && !turnoPendienteDiaAnterior
    }

    fun debeMostrarBotonMenuMovil(
        usuarioPuedeCambiarCaja: Boolean,
        turnoAbierto: Boolean
    ): Boolean {
        return usuarioPuedeCambiarCaja || turnoAbierto
    }

    fun debeMostrarCambiarCajaTablet(
        usuarioPuedeCambiarCaja: Boolean
    ): Boolean {
        return usuarioPuedeCambiarCaja
    }

    fun debeMostrarRegistrarEgresoTablet(
        turnoAbierto: Boolean,
        turnoPendienteDiaAnterior: Boolean
    ): Boolean {
        return turnoAbierto && !turnoPendienteDiaAnterior
    }

    fun debeMostrarCerrarTurnoTablet(
        turnoAbierto: Boolean
    ): Boolean {
        return turnoAbierto
    }

    fun debeMostrarResumenTurnoTablet(
        turnoAbierto: Boolean
    ): Boolean {
        return turnoAbierto
    }

    fun debeMostrarDevolucionTablet(
        turnoAbierto: Boolean,
        turnoPendienteDiaAnterior: Boolean
    ): Boolean {
        return turnoAbierto && !turnoPendienteDiaAnterior
    }
}
