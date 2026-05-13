package com.app.administradorfarmadon.ClasesDatabase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

object FechaHoraServidorHelper {

    const val ZONA_HORARIA_TIENDA_POR_DEFECTO = "America/Lima"
    const val MENSAJE_ERROR_FECHA_HORA =
        "La fecha, hora o zona horaria de este dispositivo es incorrecta. Activa Fecha y hora automáticas y Zona horaria automática para continuar."

    private const val DESFASE_MAXIMO_PERMITIDO_MS = 5 * 60 * 1000L

    private val localeEsPe: Locale = Locale.forLanguageTag("es-PE")
    private val formatoFechaFirebase: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val formatoFechaVisible: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy", localeEsPe)
    private val formatoFechaVisibleLarga: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", localeEsPe)
    private val formatoHoraVisible: DateTimeFormatter =
        DateTimeFormatter.ofPattern("h:mm a", localeEsPe)
    private val formatoFechaHoraVisible: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a", localeEsPe)

    @Volatile
    private var zonaHorariaTiendaCacheId: String = ZONA_HORARIA_TIENDA_POR_DEFECTO

    @Volatile
    private var ultimoOffsetServidorMs: Long? = null

    data class FechaHoraOficial(
        val timestampServidorMs: Long,
        val zonaHorariaTiendaId: String,
        val fechaFirebase: String,
        val horaTexto: String
    )

    data class ResultadoValidacion(
        val fechaHoraOficial: FechaHoraOficial,
        val offsetServidorMs: Long,
        val zonaHorariaDispositivoId: String
    )

    fun validarMomentoActual(
        database: FirebaseDatabase = FirebaseDatabase.getInstance(),
        onSuccess: (ResultadoValidacion) -> Unit,
        onError: (String) -> Unit
    ) {
        resolverMomentoActual(
            database = database,
            validarDispositivo = true,
            onSuccess = { fechaHoraOficial, offsetMs ->
                onSuccess(
                    ResultadoValidacion(
                        fechaHoraOficial = fechaHoraOficial,
                        offsetServidorMs = offsetMs,
                        zonaHorariaDispositivoId = ZoneId.systemDefault().id
                    )
                )
            },
            onError = onError
        )
    }

    fun obtenerMomentoActual(
        database: FirebaseDatabase = FirebaseDatabase.getInstance(),
        onSuccess: (FechaHoraOficial) -> Unit,
        onError: (String) -> Unit
    ) {
        resolverMomentoActual(
            database = database,
            validarDispositivo = false,
            onSuccess = { fechaHoraOficial, _ -> onSuccess(fechaHoraOficial) },
            onError = onError
        )
    }

    fun estimarMomentoActualDesdeCache(): FechaHoraOficial {
        val offsetMs = ultimoOffsetServidorMs ?: 0L
        val timestampServidorMs = System.currentTimeMillis() + offsetMs
        return construirFechaHoraOficial(timestampServidorMs, zonaHorariaTiendaCacheId)
    }

    fun formatearFechaFirebase(timestampMs: Long, zonaHorariaId: String = zonaHorariaTiendaCacheId): String {
        return zonedDateTime(timestampMs, zonaHorariaId).toLocalDate().format(formatoFechaFirebase)
    }

    fun formatearHora(timestampMs: Long, zonaHorariaId: String = zonaHorariaTiendaCacheId): String {
        return zonedDateTime(timestampMs, zonaHorariaId)
            .format(formatoHoraVisible)
            .lowercase(localeEsPe)
    }

    fun formatearFechaVisible(timestampMs: Long, zonaHorariaId: String = zonaHorariaTiendaCacheId): String {
        return zonedDateTime(timestampMs, zonaHorariaId)
            .format(formatoFechaVisible)
            .lowercase(localeEsPe)
    }

    fun formatearFechaVisibleDesdeFirebase(
        fechaFirebase: String,
        zonaHorariaId: String = zonaHorariaTiendaCacheId
    ): String {
        val fecha = parsearFechaFirebase(fechaFirebase) ?: return fechaFirebase
        val inicioDia = fecha.atStartOfDay(zoneId(zonaHorariaId))
        return inicioDia.format(formatoFechaVisible).lowercase(localeEsPe)
    }

    fun formatearFechaVisibleLarga(
        fechaFirebase: String,
        zonaHorariaId: String = zonaHorariaTiendaCacheId
    ): String {
        val fecha = parsearFechaFirebase(fechaFirebase) ?: return fechaFirebase
        val inicioDia = fecha.atStartOfDay(zoneId(zonaHorariaId))
        return inicioDia.format(formatoFechaVisibleLarga)
            .replaceFirstChar { it.uppercase(localeEsPe) }
    }

    fun formatearFechaHoraVisible(timestampMs: Long, zonaHorariaId: String = zonaHorariaTiendaCacheId): String {
        return zonedDateTime(timestampMs, zonaHorariaId)
            .format(formatoFechaHoraVisible)
            .lowercase(localeEsPe)
    }

    fun parsearFechaFirebase(fechaFirebase: String): LocalDate? {
        return runCatching {
            LocalDate.parse(fechaFirebase, formatoFechaFirebase)
        }.getOrNull()
    }

    fun calendarDesdeFechaFirebase(
        fechaFirebase: String,
        zonaHorariaId: String = zonaHorariaTiendaCacheId
    ): Calendar? {
        val fecha = parsearFechaFirebase(fechaFirebase) ?: return null
        return Calendar.getInstance(java.util.TimeZone.getTimeZone(zoneId(zonaHorariaId)), localeEsPe).apply {
            timeInMillis = fecha.atStartOfDay(zoneId(zonaHorariaId)).toInstant().toEpochMilli()
        }
    }

    fun calendarDesdeTimestamp(
        timestampMs: Long,
        zonaHorariaId: String = zonaHorariaTiendaCacheId
    ): Calendar {
        return Calendar.getInstance(java.util.TimeZone.getTimeZone(zoneId(zonaHorariaId)), localeEsPe).apply {
            timeInMillis = timestampMs
        }
    }

    fun obtenerZonaHorariaCacheId(): String = zonaHorariaTiendaCacheId

    fun obtenerOffsetServidorCacheMs(): Long? = ultimoOffsetServidorMs

    private fun resolverMomentoActual(
        database: FirebaseDatabase,
        validarDispositivo: Boolean,
        onSuccess: (FechaHoraOficial, Long) -> Unit,
        onError: (String) -> Unit
    ) {
        fun intentarObtenerOffset(intentosRestantes: Int) {
            obtenerOffsetServidor(database,
                onSuccess = { offsetMs ->
                    obtenerZonaHorariaTienda(database) { zonaHorariaTiendaId ->
                        val timestampServidorMs = System.currentTimeMillis() + offsetMs
                        val fechaHoraOficial = construirFechaHoraOficial(timestampServidorMs, zonaHorariaTiendaId)

                        if (validarDispositivo) {
                            // 1. Validar desfase de minutos (Límite de seguridad de 5 minutos)
                            val absOffsetMs = abs(offsetMs)
                            if (absOffsetMs > DESFASE_MAXIMO_PERMITIDO_MS) {
                                val minutos = absOffsetMs / 60000
                                val detalle = if (offsetMs > 0) "adelantado $minutos min" else "atrasado $minutos min"
                                onError("Tu reloj está $detalle respecto al servidor (${fechaHoraOficial.horaTexto}). Activa 'Fecha y hora automáticas'.")
                                return@obtenerZonaHorariaTienda
                            }

                            // 2. Validar Zona Horaria (Primero por ID, luego por Offset matemático)
                            val ahora = Instant.now()
                            val zoneTienda = ZoneId.of(zonaHorariaTiendaId)
                            val zoneDispositivo = ZoneId.systemDefault()
                            
                            val idCoincide = zoneTienda.id == zoneDispositivo.id
                            
                            if (!idCoincide) {
                                val offsetTienda = zoneTienda.rules.getOffset(ahora).totalSeconds
                                val offsetDispositivo = zoneDispositivo.rules.getOffset(ahora).totalSeconds
                                
                                if (offsetTienda != offsetDispositivo) {
                                    val gmtTienda = String.format("GMT%+d", offsetTienda / 3600)
                                    val gmtDisp = String.format("GMT%+d", offsetDispositivo / 3600)
                                    
                                    val msg = buildString {
                                        append("Zona horaria incompatible.\n")
                                        append("Dispositivo: ${zoneDispositivo.id} ($gmtDisp)\n")
                                        append("Farmacia: $zonaHorariaTiendaId ($gmtTienda)\n")
                                        append("Hora servidor: ${fechaHoraOficial.horaTexto}\n\n")
                                        append("Cámbialo a 'Perú (Lima)' en Ajustes.")
                                    }
                                    onError(msg)
                                    return@obtenerZonaHorariaTienda
                                }
                            }
                        }
                        onSuccess(fechaHoraOficial, offsetMs)
                    }
                },
                onError = {
                    if (intentosRestantes > 0) {
                        // Esperar 1 segundo antes de reintentar
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            intentarObtenerOffset(intentosRestantes - 1)
                        }, 1000L)
                    } else {
                        onError("Sin conexión estable con Firebase. No se pudo validar la hora del servidor.")
                    }
                }
            )
        }

        esperarConexionFirebase(
            database = database,
            onConectado = {
                intentarObtenerOffset(3)
            },
            onError = {
                onError("Firebase no logró conectarse para validar la hora del servidor.")
            }
        )
    }

    private fun esperarConexionFirebase(
        database: FirebaseDatabase,
        timeoutMs: Long = 8_000L,
        onConectado: () -> Unit,
        onError: () -> Unit
    ) {
        val ref = database.getReference(".info/connected")
        val handler = android.os.Handler(android.os.Looper.getMainLooper())

        var finalizado = false
        lateinit var listener: ValueEventListener

        val timeout = Runnable {
            if (!finalizado) {
                finalizado = true
                ref.removeEventListener(listener)
                onError()
            }
        }

        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val conectado = snapshot.getValue(Boolean::class.java) ?: false
                if (conectado && !finalizado) {
                    finalizado = true
                    handler.removeCallbacks(timeout)
                    ref.removeEventListener(this)
                    onConectado()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!finalizado) {
                    finalizado = true
                    handler.removeCallbacks(timeout)
                    ref.removeEventListener(this)
                    onError()
                }
            }
        }

        ref.addValueEventListener(listener)
        handler.postDelayed(timeout, timeoutMs)
    }

    private fun obtenerZonaHorariaTienda(
        database: FirebaseDatabase,
        onResult: (String) -> Unit
    ) {
        database.getReference("ConfiguracionTienda")
            .child("datosGenerales")
            .child("zonaHoraria")
            .get()
            .addOnSuccessListener { snapshot ->
                val zonaId = normalizarZonaHorariaId(snapshot.getValue(String::class.java))
                zonaHorariaTiendaCacheId = zonaId
                onResult(zonaId)
            }
            .addOnFailureListener {
                zonaHorariaTiendaCacheId = normalizarZonaHorariaId(zonaHorariaTiendaCacheId)
                onResult(zonaHorariaTiendaCacheId)
            }
    }

    private fun obtenerOffsetServidor(
        database: FirebaseDatabase,
        maxRetries: Int = 3,
        onSuccess: (Long) -> Unit,
        onError: () -> Unit
    ) {
        var currentRetry = 0
        fun attempt() {
            val ref = database.getReference(".info/serverTimeOffset")
            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            var listener: ValueEventListener? = null
            var timeoutRunnable: Runnable? = null

            val cleanup = {
                timeoutRunnable?.let { handler.removeCallbacks(it) }
                listener?.let { ref.removeEventListener(it) }
            }

            timeoutRunnable = Runnable {
                if (listener != null) {
                    cleanup()
                    if (currentRetry < maxRetries) {
                        currentRetry++
                        attempt()
                    } else {
                        onError()
                    }
                }
            }

            listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    cleanup()
                    val offset = (snapshot.value as? Number)?.toLong()
                    if (offset != null) {
                        ultimoOffsetServidorMs = offset
                        onSuccess(offset)
                    } else {
                        if (currentRetry < maxRetries) {
                            currentRetry++
                            attempt()
                        } else {
                            onError()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    cleanup()
                    if (currentRetry < maxRetries) {
                        currentRetry++
                        attempt()
                    } else {
                        onError()
                    }
                }
            }

            ref.addValueEventListener(listener)
            handler.postDelayed(timeoutRunnable, 5000L)
        }
        attempt()
    }

    private fun construirFechaHoraOficial(
        timestampServidorMs: Long,
        zonaHorariaTiendaId: String
    ): FechaHoraOficial {
        return FechaHoraOficial(
            timestampServidorMs = timestampServidorMs,
            zonaHorariaTiendaId = zoneId(zonaHorariaTiendaId).id,
            fechaFirebase = formatearFechaFirebase(timestampServidorMs, zonaHorariaTiendaId),
            horaTexto = formatearHora(timestampServidorMs, zonaHorariaTiendaId)
        )
    }

    private fun zonedDateTime(timestampMs: Long, zonaHorariaId: String): ZonedDateTime {
        return Instant.ofEpochMilli(timestampMs).atZone(zoneId(zonaHorariaId))
    }

    private fun normalizarZonaHorariaId(raw: String?): String {
        val candidata = raw.orEmpty().trim().ifBlank { ZONA_HORARIA_TIENDA_POR_DEFECTO }
        return runCatching { ZoneId.of(candidata).id }
            .getOrElse { ZONA_HORARIA_TIENDA_POR_DEFECTO }
    }

    private fun zoneId(zonaHorariaId: String): ZoneId {
        return ZoneId.of(normalizarZonaHorariaId(zonaHorariaId))
    }
}
