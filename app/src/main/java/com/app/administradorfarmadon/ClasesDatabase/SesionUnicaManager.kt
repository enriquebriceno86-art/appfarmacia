package com.app.administradorfarmadon.ClasesDatabase

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import java.util.UUID

object SesionUnicaManager {

    private const val NODO_SESIONES_ACTIVAS = "SesionesActivas"
    const val HEARTBEAT_INTERVALO_MS = 25_000L
    const val HEARTBEAT_EXPIRACION_MS = 300_000L

    private val heartbeatHandler by lazy { Handler(Looper.getMainLooper()) }
    private var heartbeatRunnable: Runnable? = null
    private var heartbeatActivo = false

    data class SesionRemotaInfo(
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

    sealed class ResultadoValidacionSesion {
        object VALIDA : ResultadoValidacionSesion()
        data class REEMPLAZADA(val info: SesionRemotaInfo) : ResultadoValidacionSesion()
        object ERROR_TEMPORAL : ResultadoValidacionSesion()
        object SIN_CONEXION : ResultadoValidacionSesion()
    }

    private fun refSesionActiva(idUsuario: String): DatabaseReference {
        return FirebaseDatabase.getInstance()
            .reference
            .child(NODO_SESIONES_ACTIVAS)
            .child(idUsuario)
    }

    private fun generarSessionId(): String {
        return SesionUnicaRules.construirSessionId(
            timestampActualMs = System.currentTimeMillis(),
            uuidCorta = UUID.randomUUID().toString().take(8)
        )
    }

    private fun nombreDispositivo(): String {
        return SesionUnicaRules.resolverNombreDispositivo(
            fabricante = Build.MANUFACTURER.orEmpty(),
            modelo = Build.MODEL.orEmpty()
        )
    }

    private fun horaIngresoTextoActual(
        momento: FechaHoraServidorHelper.FechaHoraOficial
    ): String {
        val valorFormateado = FechaHoraServidorHelper.formatearFechaHoraVisible(
            timestampMs = momento.timestampServidorMs,
            zonaHorariaId = momento.zonaHorariaTiendaId
        )
        return SesionUnicaRules.resolverHoraIngresoTexto(valorFormateado)
    }

    fun reclamarSesionActiva(
        context: Context,
        idUsuario: String,
        nombreUsuario: String,
        rolUsuario: String,
        onComplete: (ResultadoValidacionSesion) -> Unit
    ) {
        val uid = idUsuario.trim()
        if (uid.isBlank()) {
            onComplete(ResultadoValidacionSesion.ERROR_TEMPORAL)
            return
        }

        val persistirSesion: (FechaHoraServidorHelper.FechaHoraOficial) -> Unit = { momento ->
            val ahora = momento.timestampServidorMs
            val metadata = construirMetadataReclamo(
                uid = uid,
                nombreUsuario = nombreUsuario,
                rolUsuario = rolUsuario,
                momento = momento,
                ahora = ahora
            )
            val data = hashMapOf<String, Any>(
                "sessionId" to metadata.sessionId,
                "usuario" to metadata.usuario,
                "rol" to metadata.rol,
                "dispositivo" to metadata.dispositivo,
                "ingresoHoraTexto" to metadata.ingresoHoraTexto,
                "ingresoTimestamp" to ServerValue.TIMESTAMP,
                "actualizadoTimestamp" to ServerValue.TIMESTAMP,
                "lastHeartbeatAt" to metadata.lastHeartbeatAt,
                "expiraEnTimestamp" to metadata.expiraEnTimestamp,
                "estadoConexion" to metadata.estadoConexion
            )

            refSesionActiva(uid)
                .setValue(data)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        onComplete(ResultadoValidacionSesion.ERROR_TEMPORAL)
                        return@addOnCompleteListener
                    }

                    confirmarReclamoSesion(
                        context = context,
                        idUsuario = uid,
                        sessionIdEsperada = metadata.sessionId,
                        onComplete = onComplete
                    )
                }
        }

        FechaHoraServidorHelper.obtenerMomentoActual(
            onSuccess = persistirSesion,
            onError = {
                persistirSesion(FechaHoraServidorHelper.estimarMomentoActualDesdeCache())
            }
        )
    }

    fun validarORecuperarSesionLocalUnaVez(
        context: Context,
        idUsuario: String,
        nombreUsuario: String,
        rolUsuario: String,
        onComplete: (ResultadoValidacionSesion) -> Unit
    ) {
        val uid = idUsuario.trim()
        val localSessionId = SessionManager.obtenerSessionIdLocal(context).trim()
        if (uid.isBlank() || localSessionId.isBlank()) {
            onComplete(ResultadoValidacionSesion.ERROR_TEMPORAL)
            return
        }

        refSesionActiva(uid).get()
            .addOnSuccessListener { snapshot ->
                val info = snapshot.toSesionRemotaInfoOrNull()
                when {
                    info == null || sesionEstaExpirada(info) -> {
                        reclamarSesionActiva(
                            context = context,
                            idUsuario = uid,
                            nombreUsuario = nombreUsuario,
                            rolUsuario = rolUsuario,
                            onComplete = onComplete
                        )
                    }
                    SesionUnicaRules.coincideSesionRemotaConLocal(info.sessionId, localSessionId) -> {
                        onComplete(ResultadoValidacionSesion.VALIDA)
                    }
                    else -> {
                        onComplete(ResultadoValidacionSesion.REEMPLAZADA(info))
                    }
                }
            }
            .addOnFailureListener {
                onComplete(ResultadoValidacionSesion.ERROR_TEMPORAL)
            }
    }

    fun escucharSesionActiva(
        idUsuario: String,
        onChanged: (SesionRemotaInfo?) -> Unit,
        onError: ((String) -> Unit)? = null
    ): Pair<DatabaseReference, ValueEventListener>? {
        val uid = idUsuario.trim()
        if (uid.isBlank()) return null

        val ref = refSesionActiva(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lectura = SesionUnicaRules.resolverLecturaSesionRemota(
                    existeSesion = snapshot.exists(),
                    info = snapshot.toSesionRemotaInfoOrNull()
                )
                when (lectura) {
                    is LecturaSesionRemotaResultado.ConSesion -> onChanged(lectura.info)
                    LecturaSesionRemotaResultado.SinSesion -> onChanged(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onError?.invoke(error.message)
            }
        }

        ref.addValueEventListener(listener)
        return ref to listener
    }

    sealed class LecturaSesionResultado {
        data class ConSesion(val info: SesionRemotaInfo) : LecturaSesionResultado()
        object SinSesion : LecturaSesionResultado()
        data class Error(val mensaje: String) : LecturaSesionResultado()
    }

    /** Lectura única (get) de la sesión activa — no registra un listener permanente. */
    fun obtenerSesionActivaUnaVez(
        idUsuario: String,
        onResult: (LecturaSesionResultado) -> Unit
    ) {
        val uid = idUsuario.trim()
        if (uid.isBlank()) { 
            onResult(LecturaSesionResultado.Error("ID de usuario vacío"))
            return 
        }
        refSesionActiva(uid).get()
            .addOnSuccessListener { snapshot ->
                val lectura = SesionUnicaRules.resolverLecturaSesionRemota(
                    existeSesion = snapshot.exists(),
                    info = snapshot.toSesionRemotaInfoOrNull()
                )
                when (lectura) {
                    is LecturaSesionRemotaResultado.ConSesion -> onResult(LecturaSesionResultado.ConSesion(lectura.info))
                    LecturaSesionRemotaResultado.SinSesion -> onResult(LecturaSesionResultado.SinSesion)
                }
            }
            .addOnFailureListener { e -> 
                onResult(LecturaSesionResultado.Error(e.message ?: "Error de red desconocido")) 
            }
    }

    fun dejarDeEscuchar(reference: DatabaseReference?, listener: ValueEventListener?) {
        if (reference == null || listener == null) return
        reference.removeEventListener(listener)
    }

    fun iniciarHeartbeat(
        context: Context,
        idUsuario: String,
        onError: ((String) -> Unit)? = null
    ) {
        val plan = SesionUnicaRules.construirHeartbeatPlan(
            idUsuario = idUsuario,
            localSessionId = SessionManager.obtenerSessionIdLocal(context)
        )
        if (!plan.puedeIniciar) return

        detenerHeartbeat()
        heartbeatActivo = true

        val runnable = object : Runnable {
            override fun run() {
                if (!heartbeatActivo) return
                publicarHeartbeat(plan.uid, plan.localSessionId, onError)
                heartbeatHandler.postDelayed(this, HEARTBEAT_INTERVALO_MS)
            }
        }

        heartbeatRunnable = runnable
        heartbeatHandler.post(runnable)
    }

    fun detenerHeartbeat() {
        heartbeatActivo = false
        heartbeatRunnable?.let { heartbeatHandler.removeCallbacks(it) }
        heartbeatRunnable = null
    }

    fun sesionEstaExpirada(info: SesionRemotaInfo, ahora: Long = System.currentTimeMillis()): Boolean {
        return SesionUnicaRules.sesionEstaExpirada(
            expiraEnTimestamp = info.expiraEnTimestamp,
            lastHeartbeatAt = info.lastHeartbeatAt,
            expiracionMs = HEARTBEAT_EXPIRACION_MS,
            ahora = ahora
        )
    }

    fun cerrarSesionActual(
        context: Context,
        idUsuario: String,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val uid = idUsuario.trim()
        val localSessionId = SessionManager.obtenerSessionIdLocal(context).trim()
        val precondicion = SesionUnicaRules.resolverCierreSesionActual(
            idUsuario = uid,
            localSessionId = localSessionId,
            sessionIdRemota = ""
        )
        if (!precondicion.puedeIntentarCerrar) {
            onComplete?.invoke(false)
            return
        }

        refSesionActiva(uid).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentMap = currentData.value as? Map<*, *> ?: return Transaction.success(currentData)
                val sessionRemota = currentMap["sessionId"]?.toString().orEmpty()
                val cierre = SesionUnicaRules.resolverCierreSesionActual(
                    idUsuario = uid,
                    localSessionId = localSessionId,
                    sessionIdRemota = sessionRemota
                )
                if (cierre.debeEliminarSesionRemota) {
                    currentData.value = null
                }
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                onComplete?.invoke(error == null && committed)
            }
        })
    }

    fun obtenerSessionIdLocal(context: Context): String {
        return SessionManager.obtenerSessionIdLocal(context).trim()
    }

    fun formatearHoraIngreso(info: SesionRemotaInfo): String {
        if (info.ingresoTimestamp > 0L) {
            return FechaHoraServidorHelper.formatearFechaHoraVisible(info.ingresoTimestamp)
        }
        return info.ingresoHoraTexto.ifBlank { "No disponible" }
    }

    private fun DataSnapshot.toSesionRemotaInfo(): SesionRemotaInfo {
        val parseada = leerSesionRemotaParseada()
        return SesionRemotaInfo(
            sessionId = parseada.sessionId,
            usuario = parseada.usuario,
            rol = parseada.rol,
            dispositivo = parseada.dispositivo,
            ingresoTimestamp = parseada.ingresoTimestamp,
            ingresoHoraTexto = parseada.ingresoHoraTexto,
            lastHeartbeatAt = parseada.lastHeartbeatAt,
            expiraEnTimestamp = parseada.expiraEnTimestamp,
            estadoConexion = parseada.estadoConexion
        )
    }

    private fun DataSnapshot.toSesionRemotaInfoOrNull(): SesionRemotaInfo? {
        if (!exists()) return null
        return toSesionRemotaInfo()
    }

    private fun DataSnapshot.leerSesionRemotaParseada(): SesionUnicaRules.SesionRemotaParseada {
        val timestamp = child("ingresoTimestamp").leerLongRemoto()
        val lastHeartbeat = child("lastHeartbeatAt").leerLongRemoto()
        val expiraEn = child("expiraEnTimestamp").leerLongRemoto()

        return SesionUnicaRules.parsearSesionRemota(
            sessionId = child("sessionId").getValue(String::class.java).orEmpty(),
            usuario = child("usuario").getValue(String::class.java).orEmpty(),
            rol = child("rol").getValue(String::class.java).orEmpty(),
            dispositivo = child("dispositivo").getValue(String::class.java).orEmpty(),
            ingresoTimestamp = timestamp,
            ingresoHoraTexto = child("ingresoHoraTexto").getValue(String::class.java).orEmpty(),
            lastHeartbeatAt = lastHeartbeat,
            expiraEnTimestamp = expiraEn,
            estadoConexion = child("estadoConexion").getValue(Boolean::class.java) ?: false
        )
    }

    private fun DataSnapshot.leerLongRemoto(): Long {
        return when (val raw = value) {
            is Long -> raw
            is Number -> raw.toLong()
            else -> 0L
        }
    }

    private fun publicarHeartbeat(
        idUsuario: String,
        localSessionId: String,
        onError: ((String) -> Unit)? = null
    ) {
        val ahora = System.currentTimeMillis()
        refSesionActiva(idUsuario).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val mapRaw = currentData.value as? Map<*, *> ?: return Transaction.success(currentData)
                val mapActual = mapRaw.entries
                    .associate { (k, v) -> k.toString() to v }
                    .toMutableMap()
                val sessionRemota = mapActual["sessionId"]?.toString().orEmpty()
                val resultado = SesionUnicaRules.resolverActualizacionHeartbeat(
                    sessionIdRemota = sessionRemota,
                    sessionIdLocal = localSessionId,
                    ahora = ahora,
                    expiracionMs = HEARTBEAT_EXPIRACION_MS
                )
                if (!resultado.debeActualizar) {
                    return Transaction.success(currentData)
                }

                mapActual["lastHeartbeatAt"] = ahora
                mapActual["expiraEnTimestamp"] = resultado.expiraEnTimestamp
                mapActual["estadoConexion"] = true
                mapActual["actualizadoTimestamp"] = ahora
                currentData.value = mapActual
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) onError?.invoke(error.message)
            }
        })
    }

    private fun aplicarResultadoReclamoSesion(
        context: Context,
        idUsuario: String,
        resultado: ReclamoSesionResultado,
        onComplete: (ResultadoValidacionSesion) -> Unit
    ) {
        if (resultado.debePersistirSessionLocal) {
            SessionManager.guardarSessionIdLocal(context, resultado.sessionIdNueva)
        }
        
        if (resultado.exito) {
            onComplete(ResultadoValidacionSesion.VALIDA)
        } else {
            // Si el reclamo falló, verificamos si es porque alguien más tomó la sesión
            // mientras intentábamos reclamarla.
            val uid = idUsuario.trim()
            if (uid.isNotBlank()) {
                obtenerSesionActivaUnaVez(uid) { checkResultado ->
                    if (checkResultado is LecturaSesionResultado.ConSesion) {
                        onComplete(ResultadoValidacionSesion.REEMPLAZADA(checkResultado.info))
                    } else {
                        onComplete(ResultadoValidacionSesion.ERROR_TEMPORAL)
                    }
                }
            } else {
                onComplete(ResultadoValidacionSesion.ERROR_TEMPORAL)
            }
        }
    }

    private fun confirmarReclamoSesion(
        context: Context,
        idUsuario: String,
        sessionIdEsperada: String,
        onComplete: (ResultadoValidacionSesion) -> Unit
    ) {
        obtenerSesionActivaUnaVez(idUsuario) { resultado ->
            val info = (resultado as? LecturaSesionResultado.ConSesion)?.info
            val exito = info != null &&
                !sesionEstaExpirada(info) &&
                SesionUnicaRules.coincideSesionRemotaConLocal(
                    sessionIdRemota = info.sessionId,
                    sessionIdLocal = sessionIdEsperada
                )

            val rulesResultado = SesionUnicaRules.resolverResultadoReclamo(
                exito = exito,
                sessionIdNueva = if (exito) sessionIdEsperada else ""
            )
            aplicarResultadoReclamoSesion(context, idUsuario, rulesResultado, onComplete)
        }
    }

    private fun construirMetadataReclamo(
        uid: String,
        nombreUsuario: String,
        rolUsuario: String,
        momento: FechaHoraServidorHelper.FechaHoraOficial,
        ahora: Long
    ): ReclamoSesionMetadata {
        return SesionUnicaRules.construirMetadataReclamo(
            uid = uid,
            sessionId = generarSessionId(),
            nombreUsuario = nombreUsuario,
            rolUsuario = rolUsuario,
            dispositivo = nombreDispositivo(),
            ingresoHoraTexto = horaIngresoTextoActual(momento),
            ahora = ahora,
            expiracionMs = HEARTBEAT_EXPIRACION_MS
        )
    }
}
