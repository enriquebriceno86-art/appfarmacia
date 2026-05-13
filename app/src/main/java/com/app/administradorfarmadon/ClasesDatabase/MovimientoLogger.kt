package com.app.administradorfarmadon.ClasesDatabase

import android.content.Context
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import java.util.Locale

object MovimientoLogger {

    // ---------------------------------------------------------------------
    // Construcción del payload
    // ---------------------------------------------------------------------
    //
    // La idea de este logger es mantener un formato consistente para TODA la app:
    // - tipo/modulo/titulo/descripcion: lo que se ve en UI
    // - timestamp/timestampServidor: auditoría (local + servidor)
    // - metadata: campos opcionales como "antes/después", ids de referencia, etc.
    //
    internal data class MovimientoBase(
        val idMovimiento: String,
        val fecha: String,
        val hora: String,
        val data: HashMap<String, Any>
    )

    internal fun construirMovimientoBase(
        tipo: String,
        modulo: String,
        titulo: String,
        descripcion: String,
        fechaHoraOficial: FechaHoraServidorHelper.FechaHoraOficial,
        idUsuario: String,
        nombreUsuario: String,
        monto: Double? = null,
        referenciaId: String = "",
        idCaja: String = "",
        nombreCaja: String = "",
        extra: Map<String, Any?> = emptyMap()
    ): MovimientoBase {
        val timestamp = fechaHoraOficial.timestampServidorMs
        val fecha = fechaHoraOficial.fechaFirebase
        val hora = fechaHoraOficial.horaTexto
        val idMovimiento = FirebaseDatabase.getInstance().reference.push().key ?: ""

        val data = hashMapOf<String, Any>(
            "idMovimiento" to idMovimiento,
            "tipo" to tipo,
            "modulo" to modulo,
            "titulo" to titulo,
            "descripcion" to descripcion,
            "fecha" to fecha,
            "hora" to hora,
            "timestamp" to timestamp,
            "timestampServidor" to ServerValue.TIMESTAMP,
            "idUsuario" to idUsuario,
            "nombreUsuario" to nombreUsuario
        )

        if (monto != null) data["monto"] = monto
        if (referenciaId.isNotBlank()) data["referenciaId"] = referenciaId
        if (idCaja.isNotBlank()) data["idCaja"] = idCaja
        if (nombreCaja.isNotBlank()) data["nombreCaja"] = nombreCaja

        val metadata = extra
            .filterValues { it != null }
            .mapValues { it.value as Any }
            .toMutableMap()

        val motivo = (metadata["motivo"] as? String)?.trim().orEmpty()
        if (motivo.isNotBlank()) {
            data["motivo"] = motivo
        }

        val cambiosAntesDespues = metadata["cambiosAntesDespues"] as? Map<*, *>
        if (!metadata.containsKey("cantidadCambios") && !cambiosAntesDespues.isNullOrEmpty()) {
            metadata["cantidadCambios"] = cambiosAntesDespues.size
        }

        if (metadata.isNotEmpty()) {
            data["metadata"] = metadata
        }

        return MovimientoBase(
            idMovimiento = idMovimiento,
            fecha = fecha,
            hora = hora,
            data = data
        )
    }

    fun registrarConSesion(
        context: Context,
        tipo: String,
        modulo: String,
        titulo: String,
        descripcion: String,
        monto: Double? = null,
        referenciaId: String = "",
        idCaja: String = "",
        nombreCaja: String = "",
        extra: Map<String, Any?> = emptyMap()
    ) {
        SessionManager.cargarSesion(context)
        registrar(
            tipo = tipo,
            modulo = modulo,
            titulo = titulo,
            descripcion = descripcion,
            idUsuario = SessionManager.idCajera,
            nombreUsuario = SessionManager.nombreCajera,
            monto = monto,
            referenciaId = referenciaId,
            idCaja = idCaja,
            nombreCaja = nombreCaja,
            extra = extra
        )
    }

    fun registrar(
        tipo: String,
        modulo: String,
        titulo: String,
        descripcion: String,
        idUsuario: String,
        nombreUsuario: String,
        monto: Double? = null,
        referenciaId: String = "",
        idCaja: String = "",
        nombreCaja: String = "",
        extra: Map<String, Any?> = emptyMap(),
        database: FirebaseDatabase = FirebaseDatabase.getInstance()
    ) {
        FechaHoraServidorHelper.obtenerMomentoActual(
            database = database,
            onSuccess = { momento ->
                persistirMovimiento(
                    fechaHoraOficial = momento,
                    tipo = tipo,
                    modulo = modulo,
                    titulo = titulo,
                    descripcion = descripcion,
                    idUsuario = idUsuario,
                    nombreUsuario = nombreUsuario,
                    monto = monto,
                    referenciaId = referenciaId,
                    idCaja = idCaja,
                    nombreCaja = nombreCaja,
                    extra = extra,
                    database = database
                )
            },
            onError = {
                persistirMovimiento(
                    fechaHoraOficial = FechaHoraServidorHelper.estimarMomentoActualDesdeCache(),
                    tipo = tipo,
                    modulo = modulo,
                    titulo = titulo,
                    descripcion = descripcion,
                    idUsuario = idUsuario,
                    nombreUsuario = nombreUsuario,
                    monto = monto,
                    referenciaId = referenciaId,
                    idCaja = idCaja,
                    nombreCaja = nombreCaja,
                    extra = extra,
                    database = database
                )
            }
        )
    }

    private fun persistirMovimiento(
        fechaHoraOficial: FechaHoraServidorHelper.FechaHoraOficial,
        tipo: String,
        modulo: String,
        titulo: String,
        descripcion: String,
        idUsuario: String,
        nombreUsuario: String,
        monto: Double?,
        referenciaId: String,
        idCaja: String,
        nombreCaja: String,
        extra: Map<String, Any?>,
        database: FirebaseDatabase
    ) {
        val movimiento = construirMovimientoBase(
            tipo = tipo,
            modulo = modulo,
            titulo = titulo,
            descripcion = descripcion,
            fechaHoraOficial = fechaHoraOficial,
            idUsuario = idUsuario,
            nombreUsuario = nombreUsuario,
            monto = monto,
            referenciaId = referenciaId,
            idCaja = idCaja,
            nombreCaja = nombreCaja,
            extra = extra
        )

        val updates = hashMapOf<String, Any>(
            "${DbPaths.MOVIMIENTOS_GENERAL}/${movimiento.fecha}/${movimiento.idMovimiento}" to movimiento.data
        )

        database.reference.updateChildren(updates)
    }
}
