package com.app.administradorfarmadon.ActivitysCaja

/**
 * HUMANO: Reglas pequenas y puras del flujo de verificacion.
 * Solo responden preguntas de estado; el Fragment sigue coordinando
 * Firebase, listeners y efectos visuales finales.
 */
object CajaVerificacionUiRules {

    fun debeProcesarComoTurnoActivo(
        resultado: CajaVerificacionTurnoMapper.CajaVerificacionTurnoModel
    ): Boolean = resultado.hayTurnoActivo && resultado.turnoAbierto

    fun debeMostrarAvisoTurnoPendiente(
        resultado: CajaVerificacionTurnoMapper.CajaVerificacionTurnoModel
    ): Boolean = !resultado.fechaAvisoTurnoPendiente.isNullOrBlank()

    fun debeLimpiarAvisoPendiente(
        resultado: CajaVerificacionTurnoMapper.CajaVerificacionTurnoModel
    ): Boolean = resultado.fechaAvisoTurnoPendiente.isNullOrBlank()

    fun debeMostrarOverlayApertura(
        resultado: CajaVerificacionTurnoMapper.CajaVerificacionTurnoModel
    ): Boolean = !resultado.hayTurnoActivo
}
