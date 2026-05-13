package com.app.administradorfarmadon.ActivitysCaja

import com.app.administradorfarmadon.ActivityFragmentos.Fragmentos.CajaTurnoHelper
import com.app.administradorfarmadon.ClasesDatabase.FechaHoraServidorHelper
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import java.util.Locale

data class CajaTurnoDetectado(
    val fecha: String,
    val idTurno: String,
    val montoApertura: Double,
    val timestampAperturaServidor: Long = 0L
)

data class CajaEstadoTurno(
    val turnoDetectado: CajaTurnoDetectado?,
    val turnosPendientes: List<CajaTurnoDetectado>
) {
    val turnoAbierto: Boolean
        get() = turnoDetectado != null
}

/**
 * CajaService
 *
 * Esta clase concentra la lógica "pura" de caja/turnos para que el Fragment/Activity
 * se enfoque en UI. La idea es:
 * - El Fragment consulta snapshots y decide qué pantalla/overlay mostrar.
 * - CajaService interpreta esos snapshots y devuelve un estado consistente.
 *
 * Nota importante sobre fechas:
 * - Por ahora usamos la fecha del dispositivo (`yyyy-MM-dd`) para construir los nodos diarios.
 * - Si el dispositivo tiene la fecha mal, puede "mover" el turno a otro día. Por eso existe la
 *   lógica de turnos pendientes: si detectamos un turno abierto en un día anterior, bloqueamos
 *   operación y obligamos cierre antes de seguir.
 */
object CajaService {

    // ---------------------------------------------------------------------
    // Fecha/Hora
    // ---------------------------------------------------------------------
    //
    // Antes usábamos hora de servidor. Por estabilidad (y para no bloquear caja si falla),
    // esta implementación devuelve la fecha local del dispositivo.
    //
    @Suppress("UNUSED_PARAMETER")
    fun obtenerFechaActualServidor(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        FechaHoraServidorHelper.obtenerMomentoActual(
            onSuccess = { momento ->
                onSuccess(momento.fechaFirebase)
            },
            onError = onError
        )
    }

    // ---------------------------------------------------------------------
    // Apertura de turno
    // ---------------------------------------------------------------------
    //
    // `puedeAbrirTurno` valida si en la fecha actual no hay un turno "abierta" en curso.
    // Si ya existe un turno abierto, se debe forzar cierre (o impedir apertura).
    //
    fun puedeAbrirTurno(
        fechaActualMap: MutableMap<String, Any?>
    ): Boolean {
        val turnoActivoExistente = fechaActualMap["turnoActivoId"]?.toString().orEmpty()
        val turnosActuales = CajaTurnoHelper.mutableMapFromValue(fechaActualMap["turnos"])

        if (turnoActivoExistente.isBlank()) return true

        val turnoExistente = turnosActuales[turnoActivoExistente] as? Map<*, *>
        val estadoExistente = turnoExistente?.get("estado")?.toString().orEmpty()
        return !estadoExistente.equals("abierta", ignoreCase = true)
    }

    /**
     * Aplica en memoria los cambios de apertura dentro del nodo `{fecha}`.
     * Se usa dentro de transacciones/updates para mantener consistencia.
     */
    fun aplicarAperturaTurnoEnFecha(
        fechaActualMap: MutableMap<String, Any?>,
        nuevoTurnoId: String,
        fechaActual: String,
        horaActual: String,
        monto: Double,
        idCajaOperativa: String,
        nombreCajaOperativa: String,
        idUsuarioApertura: String,
        nombreUsuarioApertura: String
    ): MutableMap<String, Any?> {
        val turnosActuales = CajaTurnoHelper.mutableMapFromValue(fechaActualMap["turnos"])

        turnosActuales[nuevoTurnoId] = mapOf(
            "idTurno" to nuevoTurnoId,
            "estado" to "abierta",
            "montoApertura" to String.format(Locale.US, "%.2f", monto),
            
            // UI Fields
            "fechaAperturaLocal" to fechaActual,
            "horaAperturaLocal" to horaActual,

            // Accounting Fields
            "timestampAperturaServidor" to ServerValue.TIMESTAMP,
            "ultimaActualizacionServidor" to ServerValue.TIMESTAMP,

            "idCajaOperativa" to idCajaOperativa,
            "nombreCajaOperativa" to nombreCajaOperativa,
            "idUsuarioApertura" to idUsuarioApertura,
            "nombreUsuarioApertura" to nombreUsuarioApertura,
            "numeroVentas" to 0L,
            "totalVentas" to String.format(Locale.US, "%.2f", 0.0),
            "totalEgresos" to String.format(Locale.US, "%.2f", 0.0),
            "totalDevoluciones" to String.format(Locale.US, "%.2f", 0.0),
            "devolucionesEfectivo" to String.format(Locale.US, "%.2f", 0.0),
            "efectivoEsperado" to String.format(Locale.US, "%.2f", monto)
        )

        fechaActualMap["nombre"] = nombreCajaOperativa
        fechaActualMap["turnoActivoId"] = nuevoTurnoId
        fechaActualMap["ultimaActualizacionServidor"] = ServerValue.TIMESTAMP
        fechaActualMap["turnos"] = turnosActuales
        return fechaActualMap
    }

    /**
     * Puntero global: permite ubicar el turno abierto sin depender del dispositivo que lo abrió.
     * Ojo: si este puntero queda apuntando a un día anterior, lo tratamos como "pendiente".
     */
    fun crearPunteroGlobalApertura(
        nuevoTurnoId: String,
        fechaActual: String,
        horaActual: String,
        idUsuarioApertura: String,
        nombreUsuarioApertura: String
    ): HashMap<String, Any> {
        return hashMapOf(
            "idTurno" to nuevoTurnoId,
            "fecha" to fechaActual,
            "estado" to "abierta",
            
            // UI Fields
            "fechaAperturaLocal" to fechaActual,
            "horaAperturaLocal" to horaActual,

            // Accounting Fields
            "timestampAperturaServidor" to ServerValue.TIMESTAMP,
            "ultimaActualizacionServidor" to ServerValue.TIMESTAMP,

            "idUsuarioApertura" to idUsuarioApertura,
            "nombreUsuarioApertura" to nombreUsuarioApertura
        )
    }

    // ---------------------------------------------------------------------
    // Cierre de turno
    // ---------------------------------------------------------------------
    fun puedeCerrarTurno(turnoActual: Map<String, Any?>): Boolean {
        if (turnoActual.isEmpty()) return false
        val estadoActual = turnoActual["estado"]?.toString().orEmpty()
        return estadoActual.equals("abierta", ignoreCase = true)
    }

    /**
     * Construye el mapa final del turno al cerrarlo.
     * Delegamos el detalle a CajaTurnoHelper para mantener un solo "source of truth".
     */
    fun aplicarCierreTurno(
        turnoActual: MutableMap<String, Any?>,
        fechaActual: String,
        horaCierre: String,
        idUsuarioCierre: String,
        nombreUsuarioCierre: String,
        efectivoReal: Double,
        diferencia: Double,
        estadoCuadre: String,
        montoSobrante: Double,
        montoFaltante: Double,
        observacionCierre: String,
        observacionCierreManual: String,
        totalGeneralTurno: Double
    ): Map<String, Any?> {
        return CajaTurnoHelper.aplicarCierreTurno(
            turnoActual = turnoActual,
            fechaCierre = fechaActual,
            horaCierre = horaCierre,
            idUsuarioCierre = idUsuarioCierre,
            nombreUsuarioCierre = nombreUsuarioCierre,
            efectivoReal = efectivoReal,
            diferencia = diferencia,
            estadoCuadre = estadoCuadre,
            montoSobrante = montoSobrante,
            montoFaltante = montoFaltante,
            observacionCierre = observacionCierre,
            observacionCierreManual = observacionCierreManual,
            totalGeneralTurno = totalGeneralTurno
        )
    }

    /**
     * Mensaje visible para el usuario según el cuadre del cierre.
     */
    fun mensajeResumenCierre(
        estadoCuadre: String,
        montoSobrante: Double,
        montoFaltante: Double
    ): String {
        return when (estadoCuadre) {
            "sobrante" -> "Turno cerrado con sobrante de ${MonedaHelper.formatear(montoSobrante)}"
            "faltante" -> "Turno cerrado con faltante de ${MonedaHelper.formatear(montoFaltante)}"
            else -> "Turno cerrado correctamente"
        }
    }

    // ---------------------------------------------------------------------
    // Detección/estado del turno (hoy vs pendientes)
    // ---------------------------------------------------------------------
    //
    // Reglas:
    // 1) Si hay turnos abiertos en días anteriores => se consideran "pendientes" y se fuerza cierre.
    // 2) Si no hay pendientes y hoy hay turno abierto => se usa el de hoy.
    // 3) Si no hay turno hoy, pero el puntero global apunta a uno abierto => se muestra como abierto.
    //
    fun resolverEstadoTurno(snapshotCaja: DataSnapshot, fechaActual: String): CajaEstadoTurno {
        val turnoGlobal = obtenerTurnoAbiertoDesdePunteroGlobal(snapshotCaja)
        val turnoHoy = obtenerTurnoAbiertoDesdeFecha(snapshotCaja.child(fechaActual))

        // Siempre calculamos pendientes comparando con la fecha actual.
        // Si el puntero global apunta a un turno de un dia anterior, tambien debe contarse como pendiente.
        val turnosPendientes = buildList {
            addAll(obtenerTurnosAbiertosAnteriores(snapshotCaja, fechaActual))
            if (turnoGlobal != null && turnoGlobal.fecha < fechaActual) {
                add(turnoGlobal)
            }
        }
            .distinctBy { "${it.fecha}|${it.idTurno}" }
            .sortedBy { it.timestampAperturaServidor }

        val turnoDetectado = when {
            turnosPendientes.isNotEmpty() -> turnosPendientes.first()
            turnoHoy != null -> turnoHoy
            else -> turnoGlobal
        }

        return CajaEstadoTurno(
            turnoDetectado = turnoDetectado,
            turnosPendientes = turnosPendientes
        )
    }

    fun obtenerTurnosAbiertosAnteriores(
        snapshotCaja: DataSnapshot,
        fechaActual: String
    ): List<CajaTurnoDetectado> {
        return snapshotCaja.children
            .filter {
                val key = it.key.orEmpty()
                key.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) && key < fechaActual
            }
            .mapNotNull { obtenerTurnoAbiertoDesdeFecha(it) }
            .sortedBy { it.timestampAperturaServidor }
    }

    private fun obtenerTurnoAbiertoDesdeFecha(snapshotFecha: DataSnapshot): CajaTurnoDetectado? {
        val fecha = snapshotFecha.key.orEmpty()
        if (fecha.isBlank()) return null

        val idTurno = snapshotFecha.child("turnoActivoId").getValue(String::class.java).orEmpty()
        if (idTurno.isBlank()) return null

        val turnoSnapshot = snapshotFecha.child("turnos").child(idTurno)
        val estado = turnoSnapshot.child("estado").getValue(String::class.java).orEmpty()
        if (!estado.equals("abierta", ignoreCase = true)) return null

        val montoApertura = turnoSnapshot.child("montoApertura")
            .getValue(String::class.java)
            ?.toDoubleOrNull() ?: 0.0

        val timestampServidor = turnoSnapshot.child("timestampAperturaServidor").getValue(Long::class.java) ?: 0L

        return CajaTurnoDetectado(
            fecha = fecha,
            idTurno = idTurno,
            montoApertura = montoApertura,
            timestampAperturaServidor = timestampServidor
        )
    }

    private fun obtenerTurnoAbiertoDesdePunteroGlobal(snapshotCaja: DataSnapshot): CajaTurnoDetectado? {
        val fecha = snapshotCaja.child("turnoActivoGlobal/fecha").getValue(String::class.java).orEmpty()
        val idTurno = snapshotCaja.child("turnoActivoGlobal/idTurno").getValue(String::class.java).orEmpty()
        if (fecha.isBlank() || idTurno.isBlank()) return null

        val turnoSnapshot = snapshotCaja.child(fecha).child("turnos").child(idTurno)
        val estado = turnoSnapshot.child("estado").getValue(String::class.java).orEmpty()
        if (!estado.equals("abierta", ignoreCase = true)) return null

        val montoApertura = turnoSnapshot.child("montoApertura")
            .getValue(String::class.java)
            ?.toDoubleOrNull() ?: 0.0

        val timestampServidor = snapshotCaja.child("turnoActivoGlobal/timestampAperturaServidor").getValue(Long::class.java)
            ?: turnoSnapshot.child("timestampAperturaServidor").getValue(Long::class.java)
            ?: 0L

        return CajaTurnoDetectado(
            fecha = fecha,
            idTurno = idTurno,
            montoApertura = montoApertura,
            timestampAperturaServidor = timestampServidor
        )
    }

    /**
     * Busca en el historial de la caja el último saldo de efectivo real con el que se cerró.
     * Esto sirve para sugerir el monto de apertura del siguiente turno.
     */
    fun obtenerUltimoSaldoCierre(snapshotCaja: DataSnapshot): Double {
        return snapshotCaja.children
            .filter { it.key.orEmpty().matches(Regex("\\d{4}-\\d{2}-\\d{2}")) }
            .sortedByDescending { it.key.orEmpty() }
            .firstNotNullOfOrNull { fechaSnapshot ->
                val turnosSnapshot = fechaSnapshot.child("turnos")
                turnosSnapshot.children
                    .filter { it.child("estado").getValue(String::class.java) == "cerrada" }
                    .sortedByDescending { 
                        it.child("timestampCierreServidor").getValue(Long::class.java) 
                            ?: it.child("timestampCierre").getValue(Long::class.java) 
                            ?: 0L 
                    }
                    .firstOrNull()
                    ?.let { ultimoTurno ->
                        ultimoTurno.child("efectivoReal").getValue(String::class.java)?.toDoubleOrNull()
                            ?: ultimoTurno.child("efectivoReal").getValue(Double::class.java)
                            ?: 0.0
                    }
            } ?: 0.0
    }
}
