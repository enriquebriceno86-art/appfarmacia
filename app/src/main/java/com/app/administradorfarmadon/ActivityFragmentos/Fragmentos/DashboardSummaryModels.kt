package com.app.administradorfarmadon.ActivityFragmentos.Fragmentos

data class DashboardVentasResumen(
    val totalVentas: Double,
    val efectivoDia: Double,
    val ticketPromedio: Double,
    val gananciaEstimada: Double
)

data class DashboardCierreEstadoVisual(
    val tituloEstado: String,
    val textoEstado: String,
    val textoEstadoColorHex: String,
    val fondoEstadoResId: Int,
    val diferenciaColorHex: String,
    val tituloColorHex: String,
    val bannerColorHex: String
)

data class DashboardCierreResumenVisual(
    val mostrarCard: Boolean,
    val nombreCajero: String,
    val horaApertura: String,
    val horaCierre: String,
    val cajaInicial: Double,
    val ventas: Double,
    val egresos: Double,
    val totalTeorico: Double,
    val contadoCaja: Double,
    val diferenciaCuadre: Double,
    val estadoVisual: DashboardCierreEstadoVisual
)

data class DashboardMovimientoRemoteData(
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val autor: String,
    val hora: String,
    val timestamp: Long,
    val monto: Double?
)

data class DashboardCierreRemoteData(
    val nombreCajeroCierre: String,
    val nombreCajeroApertura: String,
    val nombreCaja: String,
    val horaApertura: String,
    val horaCierre: String,
    val cajaInicial: Double,
    val ventas: Double,
    val esperado: Double,
    val cajaFinal: Double,
    val totalEgresos: Double,
    val diferenciaGuardada: Double?,
    val timestampCierreServidor: Long,
    val timestampCierreLocal: Long,
    val idCaja: String,
    val idTurno: String,
    val fecha: String,
    val estadoTurno: String,
    val textoHoraDefault: String,
    val textoCajaSinNombre: String
)

data class DashboardMovimientosResumen(
    val recientes: List<FragmentoPrincipal.MovimientoUiItem>,
    val mostrarVerMas: Boolean,
    val mostrarEstadoVacio: Boolean,
    val mensajeEstadoVacio: String
)
