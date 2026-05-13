package com.app.administradorfarmadon.ActivitysPerfilItem

import com.app.administradorfarmadon.ClasesDatabase.FechaHoraServidorHelper

object ReporteCierreRemoteRules {

    fun construirReporteTurno(
        data: ReporteTurnoRemoteData
    ): ReporteTurnoParseado? {
        if (data.idTurno.isBlank()) return null

        val horaAperturaVisible = if (data.timestampAperturaServidor > 0L) {
            FechaHoraServidorHelper.formatearHora(data.timestampAperturaServidor)
        } else {
            data.horaApertura
        }
        val horaCierreVisible = if (data.timestampCierreServidor > 0L) {
            FechaHoraServidorHelper.formatearHora(data.timestampCierreServidor)
        } else {
            data.horaCierre
        }

        return ReporteTurnoParseado(
            idCaja = data.idCaja,
            nombreCaja = data.nombreCaja,
            idTurno = data.idTurno,
            nombreCajero = data.nombreCajero,
            descripcionTurno = construirDescripcionTurno(
                idTurno = data.idTurno,
                horaApertura = horaAperturaVisible,
                horaCierre = horaCierreVisible
            ),
            estadoTurno = data.estadoTurno,
            turnoCerrado = data.estadoTurno.equals("cerrada", ignoreCase = true),
            reporteConfirmado = data.reporteConfirmado,
            estadoCuadre = data.estadoCuadre,
            cajaInicial = data.cajaInicial,
            ventas = data.ventas + data.totalDevoluciones,
            ventasEfectivo = data.ventasEfectivo,
            totalEgresos = data.totalEgresos,
            totalDevoluciones = data.totalDevoluciones,
            esperado = data.esperado,
            registrado = data.registrado,
            diferencia = data.diferencia,
            horaApertura = horaAperturaVisible,
            horaCierre = horaCierreVisible,
            timestampOrden = data.timestampOrden
        )
    }

    fun construirDescripcionTurno(
        idTurno: String,
        horaApertura: String,
        horaCierre: String
    ): String {
        return "Turno ${idTurno.takeLast(8)} | $horaApertura - $horaCierre"
    }
}
