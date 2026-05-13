package com.app.administradorfarmadon.ActivityFragmentos

/**
 * HUMANO: Estas reglas son puras y solo resuelven la prioridad global
 * de bloqueos y si la navegación debe quedar detenida.
 */
object AppAccesoRules {

    enum class BloqueoPrioritario {
        NINGUNO,
        SIN_CONEXION,
        USUARIO_INACTIVO,
        CONFIG_TIENDA,
        HORARIO
    }

    fun resolverBloqueoPrioritario(
        sinConexion: Boolean,
        bloqueadoPorUsuarioInactivo: Boolean,
        bloqueadoPorConfigTienda: Boolean,
        bloqueadoPorHorario: Boolean
    ): BloqueoPrioritario {
        return when {
            sinConexion -> BloqueoPrioritario.SIN_CONEXION
            bloqueadoPorUsuarioInactivo -> BloqueoPrioritario.USUARIO_INACTIVO
            bloqueadoPorConfigTienda -> BloqueoPrioritario.CONFIG_TIENDA
            bloqueadoPorHorario -> BloqueoPrioritario.HORARIO
            else -> BloqueoPrioritario.NINGUNO
        }
    }

    fun debeBloquearNavegacion(
        bloqueoPrioritario: BloqueoPrioritario
    ): Boolean = bloqueoPrioritario != BloqueoPrioritario.NINGUNO
}
