package com.app.administradorfarmadon.ActivityFragmentos.Fragmentos

object DashboardRemoteRules {

    private val fechaRegex = Regex("\\d{4}-\\d{2}-\\d{2}")

    fun esFechaValida(fecha: String): Boolean {
        return fecha.matches(fechaRegex)
    }

    fun construirReporteCierre(
        data: DashboardCierreRemoteData
    ): FragmentoPrincipal.ReporteCierreUi? {
        if (!data.estadoTurno.equals("cerrada", ignoreCase = true)) return null

        val timestampCierre = data.timestampCierreServidor
            .takeIf { it > 0L }
            ?: data.timestampCierreLocal

        val nombreCaja = data.nombreCaja.ifBlank { data.textoCajaSinNombre }
        val nombreCajero = data.nombreCajeroCierre
            .ifBlank { data.nombreCajeroApertura }
            .ifBlank { nombreCaja }
            .ifBlank { data.textoCajaSinNombre }

        val diferencia = data.diferenciaGuardada ?: (data.cajaFinal - data.esperado)
        val estadoCuadre = when {
            kotlin.math.abs(diferencia) < 0.01 -> "exacto"
            diferencia < 0.0 -> "faltante"
            else -> "sobrante"
        }

        return FragmentoPrincipal.ReporteCierreUi(
            nombreCajero = nombreCajero,
            horaApertura = data.horaApertura.ifBlank { data.textoHoraDefault },
            horaCierre = data.horaCierre.ifBlank { data.textoHoraDefault },
            cajaInicial = data.cajaInicial,
            ventas = data.ventas,
            esperado = data.esperado,
            cajaFinal = data.cajaFinal,
            diferencia = diferencia,
            estadoCuadre = estadoCuadre,
            totalEgresos = data.totalEgresos,
            timestampCierre = timestampCierre,
            idCaja = data.idCaja,
            idTurno = data.idTurno,
            fecha = data.fecha,
            nombreCaja = nombreCaja,
            estadoTurno = data.estadoTurno
        )
    }

    fun construirMovimientoReciente(
        data: DashboardMovimientoRemoteData,
        tituloFallback: String,
        horaFallback: String
    ): FragmentoPrincipal.MovimientoUiItem? {
        if (data.titulo.isBlank() && data.descripcion.isBlank()) return null

        return FragmentoPrincipal.MovimientoUiItem(
            titulo = data.titulo.ifBlank { tituloFallback },
            descripcion = data.descripcion,
            tipo = data.tipo.ifBlank { "general" },
            monto = data.monto,
            autor = data.autor,
            hora = data.hora.ifBlank { horaFallback },
            timestamp = data.timestamp
        )
    }
}
