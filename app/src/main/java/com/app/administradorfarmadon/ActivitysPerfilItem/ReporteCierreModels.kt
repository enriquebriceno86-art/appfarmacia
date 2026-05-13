package com.app.administradorfarmadon.ActivitysPerfilItem

data class ReporteTurnoRemoteData(
    val idCaja: String,
    val nombreCaja: String,
    val idTurno: String,
    val nombreCajero: String,
    val horaApertura: String,
    val horaCierre: String,
    val timestampAperturaServidor: Long,
    val timestampCierreServidor: Long,
    val estadoTurno: String,
    val reporteConfirmado: Boolean,
    val estadoCuadre: String,
    val cajaInicial: Double,
    val ventas: Double,
    val ventasEfectivo: Double,
    val totalEgresos: Double,
    val totalDevoluciones: Double,
    val esperado: Double,
    val registrado: Double?,
    val diferencia: Double?,
    val timestampOrden: Long
)

data class ReporteTurnoParseado(
    val idCaja: String,
    val nombreCaja: String,
    val idTurno: String,
    val nombreCajero: String,
    val descripcionTurno: String,
    val estadoTurno: String,
    val turnoCerrado: Boolean,
    val reporteConfirmado: Boolean,
    val estadoCuadre: String,
    val cajaInicial: Double,
    val ventas: Double,
    val ventasEfectivo: Double,
    val totalEgresos: Double,
    val totalDevoluciones: Double,
    val esperado: Double,
    val registrado: Double?,
    val diferencia: Double?,
    val horaApertura: String,
    val horaCierre: String,
    val timestampOrden: Long
)

data class ReporteResultadoVisual(
    val backgroundResId: Int,
    val label: String,
    val colorHex: String,
    val montoTexto: String
)

data class ReporteEstadoVisual(
    val badgeText: String,
    val badgeBackgroundResId: Int,
    val badgeColorHex: String,
    val iconResId: Int,
    val iconColorHex: String,
    val detalleText: String,
    val detalleColorHex: String,
    val botonText: String,
    val botonEnabled: Boolean,
    val botonAlpha: Float
)

data class ReporteListaUiState(
    val pendientes: List<ListaReportesCaja.ReporteTurnoItem>,
    val confirmados: List<ListaReportesCaja.ReporteTurnoItem>,
    val mostrarRvReportes: Boolean,
    val mostrarRvConfirmados: Boolean,
    val mostrarSinReportes: Boolean,
    val mostrarRvPendientesTablet: Boolean,
    val mostrarSinPendientesTablet: Boolean,
    val mostrarRvConfirmadosTablet: Boolean,
    val mostrarSinConfirmadosTablet: Boolean
)

data class ReporteDetalleData(
    val fechaTurno: String,
    val idCaja: String,
    val nombreCaja: String,
    val idTurno: String,
    val nombreCajero: String,
    val estadoTurno: String,
    val estadoCuadre: String,
    val cajaInicial: Double,
    val totalVentas: Double,
    val totalEgresos: Double,
    val totalDevoluciones: Double,
    val totalEsperado: Double,
    val reporteConfirmado: Boolean,
    val horaApertura: String,
    val horaCierre: String,
    val efectivoRegistrado: Double?,
    val diferencia: Double?
)
