package com.app.administradorfarmadon.ActivityFragmentos

/**
 * HUMANO: Estas reglas no deciden qué bloqueo gana.
 * Solo resuelven qué reevaluaciones conviene disparar y cuándo.
 */
object AppAccesoReevaluationRules {

    data class AppReevaluacionPlan(
        val debeEscucharEstadoUsuario: Boolean = false,
        val debeActualizarEstadoConexion: Boolean = false,
        val debeDetenerEscuchaHorario: Boolean = false,
        val debeCancelarSesionUnicaEnProceso: Boolean = false,
        val debeInvalidarIntentosSesionUnica: Boolean = false,
        val debeMostrarToastSinConexion: Boolean = false,
        val debeIntentarReanudarSesionUnica: Boolean = false,
        val debeIniciarEscuchaHorario: Boolean = false,
        val debeVerificarConfigTienda: Boolean = false,
        val debeCargarProximaApertura: Boolean = false
    )

    fun planParaForeground(
        sinConexion: Boolean,
        bloqueadoPorHorario: Boolean
    ): AppReevaluacionPlan {
        return AppReevaluacionPlan(
            debeEscucharEstadoUsuario = true,
            debeActualizarEstadoConexion = true,
            debeCargarProximaApertura = !sinConexion && bloqueadoPorHorario
        )
    }

    fun planParaCambioConexion(
        sinConexion: Boolean,
        huboCambio: Boolean,
        forzarToast: Boolean,
        sesionUnicaInicializada: Boolean
    ): AppReevaluacionPlan {
        return if (sinConexion) {
            AppReevaluacionPlan(
                debeDetenerEscuchaHorario = true,
                debeCancelarSesionUnicaEnProceso = true,
                debeInvalidarIntentosSesionUnica = true,
                debeMostrarToastSinConexion = forzarToast
            )
        } else {
            AppReevaluacionPlan(
                debeIntentarReanudarSesionUnica = !sesionUnicaInicializada,
                debeIniciarEscuchaHorario = true,
                debeVerificarConfigTienda = huboCambio || forzarToast
            )
        }
    }

    fun debeVerificarConfiguracionTienda(
        sinConexion: Boolean,
        verificacionConfigEnCurso: Boolean
    ): Boolean = !sinConexion && !verificacionConfigEnCurso

    fun debeIniciarEscuchaEstadoUsuario(
        idUsuario: String,
        usuarioEstadoRefKey: String?,
        existeListener: Boolean
    ): Boolean {
        if (idUsuario.isBlank()) return false
        return !(usuarioEstadoRefKey == idUsuario && existeListener)
    }

    fun debeIniciarEscuchaHorario(
        sinConexion: Boolean,
        diaKey: String,
        diaHorarioEscuchado: String,
        horarioDiaRefExiste: Boolean,
        horarioDiaListenerExiste: Boolean
    ): Boolean {
        if (sinConexion) return false
        return !(
            diaHorarioEscuchado == diaKey &&
                horarioDiaRefExiste &&
                horarioDiaListenerExiste
            )
    }
}
