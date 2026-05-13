package com.app.administradorfarmadon.ClasesDatabase

object SesionUnicaRules {

    data class SesionRemotaParseada(
        val sessionId: String,
        val usuario: String,
        val rol: String,
        val dispositivo: String,
        val ingresoTimestamp: Long,
        val ingresoHoraTexto: String,
        val lastHeartbeatAt: Long,
        val expiraEnTimestamp: Long,
        val estadoConexion: Boolean
    )

    fun puedeIniciarHeartbeat(
        idUsuario: String,
        localSessionId: String
    ): Boolean {
        return idUsuario.trim().isNotBlank() && localSessionId.trim().isNotBlank()
    }

    fun construirHeartbeatPlan(
        idUsuario: String,
        localSessionId: String
    ): HeartbeatPlan {
        val uid = idUsuario.trim()
        val sessionId = localSessionId.trim()
        return HeartbeatPlan(
            uid = uid,
            localSessionId = sessionId,
            puedeIniciar = puedeIniciarHeartbeat(uid, sessionId)
        )
    }

    fun calcularExpiracionHeartbeat(
        ahora: Long,
        expiracionMs: Long
    ): Long = ahora + expiracionMs

    fun sesionEstaExpirada(
        expiraEnTimestamp: Long,
        lastHeartbeatAt: Long,
        expiracionMs: Long,
        ahora: Long = System.currentTimeMillis()
    ): Boolean {
        val expiraEn = if (expiraEnTimestamp > 0L) {
            expiraEnTimestamp
        } else {
            lastHeartbeatAt + expiracionMs
        }
        return expiraEn > 0L && ahora > expiraEn
    }

    fun coincideSesionRemotaConLocal(
        sessionIdRemota: String,
        sessionIdLocal: String
    ): Boolean {
        val remota = sessionIdRemota.trim()
        val local = sessionIdLocal.trim()
        return remota.isNotBlank() && local.isNotBlank() && remota == local
    }

    fun puedeCerrarSesionActual(
        idUsuario: String,
        localSessionId: String
    ): Boolean {
        return idUsuario.trim().isNotBlank() && localSessionId.trim().isNotBlank()
    }

    fun resolverCierreSesionActual(
        idUsuario: String,
        localSessionId: String,
        sessionIdRemota: String
    ): CierreSesionResultado {
        val puedeIntentarCerrar = puedeCerrarSesionActual(idUsuario, localSessionId)
        return CierreSesionResultado(
            puedeIntentarCerrar = puedeIntentarCerrar,
            debeEliminarSesionRemota = puedeIntentarCerrar &&
                coincideSesionRemotaConLocal(sessionIdRemota, localSessionId)
        )
    }

    fun resolverResultadoReclamo(
        exito: Boolean,
        sessionIdNueva: String
    ): ReclamoSesionResultado {
        return ReclamoSesionResultado(
            exito = exito,
            sessionIdNueva = sessionIdNueva.trim(),
            debePersistirSessionLocal = exito && sessionIdNueva.trim().isNotBlank()
        )
    }

    fun construirSessionId(
        timestampActualMs: Long,
        uuidCorta: String
    ): String {
        return "ses_${timestampActualMs}_${uuidCorta.trim()}"
    }

    fun resolverNombreDispositivo(
        fabricante: String,
        modelo: String
    ): String {
        return "${fabricante.trim()} ${modelo.trim()}".trim()
            .ifBlank { "Dispositivo desconocido" }
    }

    fun resolverHoraIngresoTexto(
        valorFormateado: String
    ): String = valorFormateado.trim().lowercase()

    fun construirMetadataReclamo(
        uid: String,
        sessionId: String,
        nombreUsuario: String,
        rolUsuario: String,
        dispositivo: String,
        ingresoHoraTexto: String,
        ahora: Long,
        expiracionMs: Long
    ): ReclamoSesionMetadata {
        return ReclamoSesionMetadata(
            sessionId = sessionId.trim(),
            usuario = nombreUsuario.trim().ifBlank { uid.trim() },
            rol = rolUsuario.trim().ifBlank { "Caja" },
            dispositivo = dispositivo.trim().ifBlank { "Dispositivo desconocido" },
            ingresoHoraTexto = ingresoHoraTexto.trim(),
            lastHeartbeatAt = ahora,
            expiraEnTimestamp = calcularExpiracionHeartbeat(ahora, expiracionMs),
            estadoConexion = true
        )
    }

    fun resolverLecturaSesionRemota(
        existeSesion: Boolean,
        info: SesionUnicaManager.SesionRemotaInfo?
    ): LecturaSesionRemotaResultado {
        if (!existeSesion || info == null) return LecturaSesionRemotaResultado.SinSesion
        return LecturaSesionRemotaResultado.ConSesion(info)
    }

    fun resolverActualizacionHeartbeat(
        sessionIdRemota: String,
        sessionIdLocal: String,
        ahora: Long,
        expiracionMs: Long
    ): HeartbeatActualizacionResultado {
        val debeActualizar = coincideSesionRemotaConLocal(sessionIdRemota, sessionIdLocal)
        return HeartbeatActualizacionResultado(
            debeActualizar = debeActualizar,
            expiraEnTimestamp = if (debeActualizar) {
                calcularExpiracionHeartbeat(ahora, expiracionMs)
            } else {
                0L
            }
        )
    }

    fun parsearSesionRemota(
        sessionId: String,
        usuario: String,
        rol: String,
        dispositivo: String,
        ingresoTimestamp: Long,
        ingresoHoraTexto: String,
        lastHeartbeatAt: Long,
        expiraEnTimestamp: Long,
        estadoConexion: Boolean
    ): SesionRemotaParseada {
        return SesionRemotaParseada(
            sessionId = sessionId.trim(),
            usuario = usuario.trim(),
            rol = rol.trim(),
            dispositivo = dispositivo.trim(),
            ingresoTimestamp = ingresoTimestamp,
            ingresoHoraTexto = ingresoHoraTexto.trim(),
            lastHeartbeatAt = lastHeartbeatAt,
            expiraEnTimestamp = expiraEnTimestamp,
            estadoConexion = estadoConexion
        )
    }
}
