package com.app.administradorfarmadon.ActivityFragmentos.Fragmentos

import com.app.administradorfarmadon.R
import kotlin.math.abs

object DashboardSummaryRules {

    fun construirResumenVentas(
        totalVentas: Double,
        conteoVentas: Int
    ): DashboardVentasResumen {
        return DashboardVentasResumen(
            totalVentas = totalVentas,
            efectivoDia = totalVentas,
            ticketPromedio = if (conteoVentas > 0) totalVentas / conteoVentas else 0.0,
            gananciaEstimada = totalVentas * 0.3
        )
    }

    fun construirResumenCierre(
        reporte: FragmentoPrincipal.ReporteCierreUi?,
        textoCierreCorrecto: String,
        textoCuadreExacto: String,
        textoFaltoDinero: String,
        textoSobroDinero: String,
        formatearDiferencia: (Double) -> String
    ): DashboardCierreResumenVisual? {
        if (reporte == null) return null

        val apertura = reporte.cajaInicial
        val ventas = reporte.ventas
        val egresos = reporte.totalEgresos
        val totalTeorico = apertura + ventas - egresos
        val contadoCaja = reporte.cajaFinal
        val diferenciaCuadre = contadoCaja - reporte.esperado

        return DashboardCierreResumenVisual(
            mostrarCard = true,
            nombreCajero = reporte.nombreCajero,
            horaApertura = reporte.horaApertura,
            horaCierre = reporte.horaCierre,
            cajaInicial = apertura,
            ventas = ventas,
            egresos = egresos,
            totalTeorico = totalTeorico,
            contadoCaja = contadoCaja,
            diferenciaCuadre = diferenciaCuadre,
            estadoVisual = resolverEstadoVisualCierre(
                diferencia = diferenciaCuadre,
                textoCierreCorrecto = textoCierreCorrecto,
                textoCuadreExacto = textoCuadreExacto,
                textoFaltoDinero = textoFaltoDinero,
                textoSobroDinero = textoSobroDinero,
                formatearDiferencia = formatearDiferencia
            )
        )
    }

    fun resolverEstadoVisualCierre(
        diferencia: Double,
        textoCierreCorrecto: String,
        textoCuadreExacto: String,
        textoFaltoDinero: String,
        textoSobroDinero: String,
        formatearDiferencia: (Double) -> String
    ): DashboardCierreEstadoVisual {
        return when {
            abs(diferencia) < 0.01 -> DashboardCierreEstadoVisual(
                tituloEstado = textoCierreCorrecto,
                textoEstado = textoCuadreExacto,
                textoEstadoColorHex = "#16824A",
                fondoEstadoResId = R.drawable.bg_dashboard_pill_success,
                diferenciaColorHex = "#1E8E5A",
                tituloColorHex = "#16824A",
                bannerColorHex = "#EEF9F1"
            )

            diferencia < 0 -> DashboardCierreEstadoVisual(
                tituloEstado = textoFaltoDinero,
                textoEstado = "Faltante: ${formatearDiferencia(diferencia)}",
                textoEstadoColorHex = "#B3261E",
                fondoEstadoResId = R.drawable.bg_pill_error,
                diferenciaColorHex = "#B3261E",
                tituloColorHex = "#B3261E",
                bannerColorHex = "#FDECEC"
            )

            else -> DashboardCierreEstadoVisual(
                tituloEstado = textoSobroDinero,
                textoEstado = "Sobrante: ${formatearDiferencia(diferencia)}",
                textoEstadoColorHex = "#8C5A00",
                fondoEstadoResId = R.drawable.bg_pill_warning,
                diferenciaColorHex = "#8C5A00",
                tituloColorHex = "#8C5A00",
                bannerColorHex = "#FFF4D6"
            )
        }
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

    fun construirResumenMovimientos(
        movimientos: List<FragmentoPrincipal.MovimientoUiItem>,
        mensajeEstadoVacio: String
    ): DashboardMovimientosResumen {
        val todos = movimientos.sortedByDescending { it.timestamp }
        val recientes = todos.take(5)
        return DashboardMovimientosResumen(
            recientes = recientes,
            mostrarVerMas = todos.size > 5,
            mostrarEstadoVacio = recientes.isEmpty(),
            mensajeEstadoVacio = mensajeEstadoVacio
        )
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
}
