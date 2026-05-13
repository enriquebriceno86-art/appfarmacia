package com.app.administradorfarmadon.ActivitysInicio

import com.app.administradorfarmadon.ActivitysInicio.reciclermovimientos.CambioAuditoriaItem
import com.app.administradorfarmadon.ClasesDatabase.FechaHoraServidorHelper

object MovimientoRemoteRules {

    fun construirMovimientoRemoto(data: MovimientoRemoteData): MovimientoRemoteParseado? {
        if (data.titulo.isBlank() && data.descripcion.isBlank()) return null

        val timestampVisible = data.timestampServidor.takeIf { it > 0L } ?: data.timestamp
        val horaVisible = if (timestampVisible > 0L) {
            FechaHoraServidorHelper.formatearHora(timestampVisible)
        } else {
            resolverHoraVisible(data.hora)
        }
        val fechaVisible = if (timestampVisible > 0L) {
            FechaHoraServidorHelper.formatearFechaFirebase(timestampVisible)
        } else {
            resolverFechaVisible(
                fechaNodo = data.fechaNodo,
                fechaMovimiento = data.fechaMovimiento
            )
        }

        return MovimientoRemoteParseado(
            idMovimiento = data.idMovimiento,
            titulo = data.titulo,
            descripcion = data.descripcion,
            detalleLotes = data.detalleLotes,
            tipo = resolverTipoVisible(data.tipo),
            tituloMetodoMetadata = data.tituloMetodoMetadata,
            modulo = data.modulo.ifBlank { "general" },
            autor = data.autor,
            hora = horaVisible,
            fecha = fechaVisible,
            timestamp = timestampVisible,
            monto = data.monto,
            cambiosAntesDespues = data.cambiosAntesDespues
        )
    }

    fun resolverFechaVisible(
        fechaNodo: String,
        fechaMovimiento: String
    ): String {
        return fechaNodo.ifBlank { fechaMovimiento }
    }

    fun resolverHoraVisible(hora: String): String {
        return hora.ifBlank { "--:--" }
    }

    fun resolverTipoVisible(tipo: String): String {
        return tipo.ifBlank { "general" }
    }

    fun parsearMonto(raw: Any?): Double? {
        return when (raw) {
            is Double -> raw
            is Long -> raw.toDouble()
            is Int -> raw.toDouble()
            is String -> raw.toDoubleOrNull()
            else -> null
        }
    }

    fun construirCambiosAntesDespues(
        cambios: List<CambioAuditoriaRemoteData>
    ): Map<String, CambioAuditoriaItem> {
        if (cambios.isEmpty()) return emptyMap()

        val cambiosNormalizados = linkedMapOf<String, CambioAuditoriaItem>()
        for (cambio in cambios) {
            val campo = cambio.campo.trim()
            if (campo.isBlank()) continue

            cambiosNormalizados[campo] = CambioAuditoriaItem(
                antes = cambio.antes,
                despues = cambio.despues
            )
        }
        return cambiosNormalizados
    }
}
