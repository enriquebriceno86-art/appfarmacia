package com.app.administradorfarmadon.ActivitysPerfilItem

object ReporteCierreDetailRules {

    fun construirDetalleData(
        fechaTurno: String,
        item: ListaReportesCaja.ReporteTurnoItem
    ): ReporteDetalleData {
        return ReporteDetalleData(
            fechaTurno = fechaTurno,
            idCaja = item.idCaja,
            nombreCaja = item.nombreCaja,
            idTurno = item.idTurno,
            nombreCajero = item.nombreCajero,
            estadoTurno = item.estadoTurno,
            estadoCuadre = item.estadoCuadre,
            cajaInicial = item.cajaInicial,
            totalVentas = item.ventas,
            totalEgresos = item.totalEgresos,
            totalDevoluciones = item.totalDevoluciones,
            totalEsperado = item.esperado,
            reporteConfirmado = item.reporteConfirmado,
            horaApertura = item.horaApertura,
            horaCierre = item.horaCierre,
            efectivoRegistrado = item.registrado,
            diferencia = item.diferencia
        )
    }
}
