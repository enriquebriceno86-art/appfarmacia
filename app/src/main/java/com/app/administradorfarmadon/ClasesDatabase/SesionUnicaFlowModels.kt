package com.app.administradorfarmadon.ClasesDatabase

data class HeartbeatPlan(
    val uid: String,
    val localSessionId: String,
    val puedeIniciar: Boolean
)

sealed class LecturaSesionRemotaResultado {
    data object SinSesion : LecturaSesionRemotaResultado()
    data class ConSesion(
        val info: SesionUnicaManager.SesionRemotaInfo
    ) : LecturaSesionRemotaResultado()
}

data class ReclamoSesionResultado(
    val exito: Boolean,
    val sessionIdNueva: String,
    val debePersistirSessionLocal: Boolean
)

data class HeartbeatActualizacionResultado(
    val debeActualizar: Boolean,
    val expiraEnTimestamp: Long
)

data class CierreSesionResultado(
    val puedeIntentarCerrar: Boolean,
    val debeEliminarSesionRemota: Boolean
)

data class ReclamoSesionMetadata(
    val sessionId: String,
    val usuario: String,
    val rol: String,
    val dispositivo: String,
    val ingresoHoraTexto: String,
    val lastHeartbeatAt: Long,
    val expiraEnTimestamp: Long,
    val estadoConexion: Boolean
)
