package com.app.administradorfarmadon.ActivitysCaja

import com.app.administradorfarmadon.R

/**
 * HUMANO: Este builder solo decide cómo debe verse el estado de la caja.
 * No conoce Views ni Firebase; solo recibe banderas ya resueltas y devuelve
 * un modelo puro para que FragmentoCaja aplique el resultado en la UI.
 */
object CajaEstadoUiBuilder {

    data class CajaEstadoUiModel(
        val textoEstado: String,
        val colorEstadoHex: String,
        val backgroundEstadoResId: Int?,
        val mostrarBotonMenuMovil: Boolean,
        val mostrarCambiarCajaTablet: Boolean,
        val mostrarRegistrarEgresoTablet: Boolean,
        val mostrarCerrarTurnoTablet: Boolean,
        val mostrarResumenTurnoTablet: Boolean,
        val mostrarDevolucionTablet: Boolean
    )

    /**
     * HUMANO: Conserva exactamente la misma prioridad visual actual:
     * 1. Verificando
     * 2. Sin verificar
     * 3. Turno pendiente
     * 4. Turno abierto
     * 5. Turno cerrado
     */
    fun construirEstadoUi(
        usuarioPuedeCambiarCaja: Boolean,
        verificacionTurnoDisponible: Boolean,
        verificacionTurnoEnCurso: Boolean,
        turnoAbierto: Boolean,
        turnoPendienteDiaAnterior: Boolean,
        fechaTurnoActivo: String
    ): CajaEstadoUiModel {
        val textoEstado: String
        val colorEstadoHex: String
        val backgroundEstadoResId: Int?

        when {
            !verificacionTurnoDisponible && verificacionTurnoEnCurso -> {
                textoEstado = "● Verificando turno..."
                colorEstadoHex = "#7A869A"
                backgroundEstadoResId = R.drawable.bg_estado_pendiente
            }

            !verificacionTurnoDisponible -> {
                textoEstado = "● Turno sin verificar"
                colorEstadoHex = "#D14343"
                backgroundEstadoResId = R.drawable.bg_estado_inactivo
            }

            turnoAbierto && turnoPendienteDiaAnterior -> {
                textoEstado = "● Turno pendiente: $fechaTurnoActivo"
                colorEstadoHex = "#D98E04"
                backgroundEstadoResId = R.drawable.bg_estado_pendiente
            }

            turnoAbierto -> {
                textoEstado = "● Turno abierto"
                colorEstadoHex = "#1F9D55"
                backgroundEstadoResId = R.drawable.bg_estado_activo
            }

            else -> {
                textoEstado = "● Turno cerrado"
                colorEstadoHex = "#D14343"
                backgroundEstadoResId = R.drawable.bg_estado_inactivo
            }
        }

        return CajaEstadoUiModel(
            textoEstado = textoEstado,
            colorEstadoHex = colorEstadoHex,
            backgroundEstadoResId = backgroundEstadoResId,
            mostrarBotonMenuMovil = CajaTurnoRules.debeMostrarBotonMenuMovil(
                usuarioPuedeCambiarCaja = usuarioPuedeCambiarCaja,
                turnoAbierto = turnoAbierto
            ),
            mostrarCambiarCajaTablet = CajaTurnoRules.debeMostrarCambiarCajaTablet(
                usuarioPuedeCambiarCaja = usuarioPuedeCambiarCaja
            ),
            mostrarRegistrarEgresoTablet = CajaTurnoRules.debeMostrarRegistrarEgresoTablet(
                turnoAbierto = turnoAbierto,
                turnoPendienteDiaAnterior = turnoPendienteDiaAnterior
            ),
            mostrarCerrarTurnoTablet = CajaTurnoRules.debeMostrarCerrarTurnoTablet(
                turnoAbierto = turnoAbierto
            ),
            mostrarResumenTurnoTablet = CajaTurnoRules.debeMostrarResumenTurnoTablet(
                turnoAbierto = turnoAbierto
            ),
            mostrarDevolucionTablet = CajaTurnoRules.debeMostrarDevolucionTablet(
                turnoAbierto = turnoAbierto,
                turnoPendienteDiaAnterior = turnoPendienteDiaAnterior
            )
        )
    }
}
