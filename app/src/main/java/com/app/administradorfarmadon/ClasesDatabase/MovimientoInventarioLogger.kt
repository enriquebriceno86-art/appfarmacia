package com.app.administradorfarmadon.ClasesDatabase

import android.content.Context
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

object MovimientoInventarioLogger {

    // ---------------------------------------------------------------------
    // Movimientos del inventario (trazabilidad)
    // ---------------------------------------------------------------------
    //
    // Este logger escribe en: Movimientos/movimientosInventario/{fecha}/{idMovimiento}
    // Usamos un formato similar al logger general, pero agregamos:
    // - indiceProducto, cantidad, stockAntes/stockDespues, motivo, presentacion, etc.
    //
    fun registrarConSesion(
        context: Context,
        indiceProducto: String,
        tipo: String,
        titulo: String,
        descripcion: String,
        cantidad: Int? = null,
        stockAntes: Int? = null,
        stockDespues: Int? = null,
        motivo: String = "",
        referenciaId: String = "",
        extra: Map<String, Any?> = emptyMap()
    ) {
        SessionManager.cargarSesion(context)
        registrar(
            indiceProducto = indiceProducto,
            tipo = tipo,
            titulo = titulo,
            descripcion = descripcion,
            idUsuario = SessionManager.idCajera,
            nombreUsuario = SessionManager.nombreCajera,
            cantidad = cantidad,
            stockAntes = stockAntes,
            stockDespues = stockDespues,
            motivo = motivo,
            referenciaId = referenciaId,
            extra = extra
        )
    }

    fun registrar(
        indiceProducto: String,
        tipo: String,
        titulo: String,
        descripcion: String,
        idUsuario: String,
        nombreUsuario: String,
        cantidad: Int? = null,
        stockAntes: Int? = null,
        stockDespues: Int? = null,
        motivo: String = "",
        referenciaId: String = "",
        extra: Map<String, Any?> = emptyMap(),
        database: FirebaseDatabase = FirebaseDatabase.getInstance()
    ) {
        if (indiceProducto.isBlank()) return

        val movimiento = MovimientoLogger.construirMovimientoBase(
            tipo = tipo,
            modulo = "inventario",
            titulo = titulo,
            descripcion = descripcion,
            fechaHoraOficial = FechaHoraServidorHelper.estimarMomentoActualDesdeCache(),
            idUsuario = idUsuario,
            nombreUsuario = nombreUsuario,
            referenciaId = referenciaId,
            extra = extra
        )

        val data = hashMapOf<String, Any>(
            "idMovimiento" to movimiento.idMovimiento,
            "indiceProducto" to indiceProducto,
            "tipo" to tipo,
            "modulo" to "inventario",
            "titulo" to titulo,
            "descripcion" to descripcion,
            "fecha" to movimiento.fecha,
            "hora" to movimiento.hora,
            "timestamp" to movimiento.data["timestamp"].toString().toLong(),
            "timestampServidor" to ServerValue.TIMESTAMP,
            "idUsuario" to idUsuario,
            "nombreUsuario" to nombreUsuario
        )

        if (cantidad != null) data["cantidad"] = cantidad
        if (stockAntes != null) data["stockAntes"] = stockAntes
        if (stockDespues != null) data["stockDespues"] = stockDespues
        if (motivo.isNotBlank()) data["motivo"] = motivo
        if (referenciaId.isNotBlank()) data["referenciaId"] = referenciaId

        val metadata = extra
            .filterValues { it != null }
            .mapValues { it.value as Any }
            .toMutableMap()

        if (!metadata.containsKey("cambiosAntesDespues") && (stockAntes != null || stockDespues != null)) {
            metadata["cambiosAntesDespues"] = mapOf(
                "stockTotalUnidades" to mapOf(
                    "antes" to valorAuditoriaInventario(stockAntes),
                    "despues" to valorAuditoriaInventario(stockDespues)
                )
            )
        }

        if (metadata.isNotEmpty()) {
            data["metadata"] = metadata
        }

        val updates = hashMapOf<String, Any>(
            "${DbPaths.MOVIMIENTOS_INVENTARIO}/${movimiento.fecha}/${movimiento.idMovimiento}" to data
        )

        database.reference.updateChildren(updates)
    }

    private fun valorAuditoriaInventario(valor: Any?): Any {
        return when (valor) {
            null -> "(vacio)"
            is String -> valor.ifBlank { "(vacio)" }
            is Number, is Boolean -> valor
            else -> valor.toString()
        }
    }
}
