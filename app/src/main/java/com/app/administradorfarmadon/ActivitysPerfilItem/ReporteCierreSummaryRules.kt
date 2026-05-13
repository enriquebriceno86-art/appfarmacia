package com.app.administradorfarmadon.ActivitysPerfilItem

import com.app.administradorfarmadon.R

object ReporteCierreSummaryRules {

    fun construirEstadoLista(
        reportes: List<ListaReportesCaja.ReporteTurnoItem>,
        mostrarPendientes: Boolean,
        esModoDual: Boolean
    ): ReporteListaUiState {
        val pendientes = reportes.filter { !it.confirmado }
        val confirmados = reportes.filter { it.confirmado }
        val filtradosMovil = if (mostrarPendientes) pendientes else confirmados

        return ReporteListaUiState(
            pendientes = pendientes,
            confirmados = confirmados,
            mostrarRvReportes = !esModoDual && mostrarPendientes && filtradosMovil.isNotEmpty(),
            mostrarRvConfirmados = if (esModoDual) confirmados.isNotEmpty() else !mostrarPendientes && filtradosMovil.isNotEmpty(),
            mostrarSinReportes = !esModoDual && filtradosMovil.isEmpty(),
            mostrarRvPendientesTablet = esModoDual && pendientes.isNotEmpty(),
            mostrarSinPendientesTablet = esModoDual && pendientes.isEmpty(),
            mostrarRvConfirmadosTablet = esModoDual && confirmados.isNotEmpty(),
            mostrarSinConfirmadosTablet = esModoDual && confirmados.isEmpty()
        )
    }

    fun construirResultadoVisual(
        resultadoNeto: Double,
        textoGanancia: String,
        textoPerdida: String,
        textoNeutro: String,
        montoFormateado: String,
        montoConSignoFormateado: String,
        montoCeroFormateado: String
    ): ReporteResultadoVisual {
        return when {
            resultadoNeto > 0.009 -> ReporteResultadoVisual(
                backgroundResId = R.drawable.bg_estado_confirmado,
                label = textoGanancia,
                colorHex = "#245B4B",
                montoTexto = montoFormateado
            )
            resultadoNeto < -0.009 -> ReporteResultadoVisual(
                backgroundResId = R.drawable.bg_estado_perdida,
                label = textoPerdida,
                colorHex = "#B3261E",
                montoTexto = montoConSignoFormateado
            )
            else -> ReporteResultadoVisual(
                backgroundResId = R.drawable.bg_estado_neutro,
                label = textoNeutro,
                colorHex = "#4B5563",
                montoTexto = montoCeroFormateado
            )
        }
    }

    fun construirEstadoConfirmadoVisual(
        estadoCuadre: String,
        diferenciaAbsolutaFormateada: String,
        textoBadgeConfirmado: String,
        textoBotonVerReporte: String,
        textoCuadreExacto: String,
        textoFaltante: String,
        textoSobrante: String,
        textoCierreConfirmado: String
    ): ReporteEstadoVisual {
        val detalle = when (estadoCuadre.lowercase()) {
            "exacto" -> textoCuadreExacto to "#137333"
            "faltante" -> textoFaltante.format(diferenciaAbsolutaFormateada) to "#B3261E"
            "sobrante" -> textoSobrante.format(diferenciaAbsolutaFormateada) to "#9A6400"
            else -> textoCierreConfirmado to "#137333"
        }

        return ReporteEstadoVisual(
            badgeText = textoBadgeConfirmado,
            badgeBackgroundResId = R.drawable.bg_estado_confirmado,
            badgeColorHex = "#137333",
            iconResId = R.drawable.checkventa,
            iconColorHex = "#137333",
            detalleText = detalle.first,
            detalleColorHex = detalle.second,
            botonText = textoBotonVerReporte,
            botonEnabled = true,
            botonAlpha = 1f
        )
    }

    fun construirEstadoPendienteVisual(
        turnoCerrado: Boolean,
        textoBadgePendiente: String,
        textoPendienteConfirmacion: String,
        textoPendienteCierre: String,
        textoBotonRevisarCierre: String
    ): ReporteEstadoVisual {
        return ReporteEstadoVisual(
            badgeText = textoBadgePendiente,
            badgeBackgroundResId = R.drawable.bg_estado_pendiente,
            badgeColorHex = "#9A6400",
            iconResId = R.drawable.baseline_access_time_24,
            iconColorHex = "#9A6400",
            detalleText = if (turnoCerrado) textoPendienteConfirmacion else textoPendienteCierre,
            detalleColorHex = "#9A6400",
            botonText = textoBotonRevisarCierre,
            botonEnabled = turnoCerrado,
            botonAlpha = 1f
        )
    }

    fun construirEstadoEnCursoVisual(
        textoBadgeEnCurso: String,
        textoPendienteCierre: String,
        textoBotonVerAvance: String
    ): ReporteEstadoVisual {
        return ReporteEstadoVisual(
            badgeText = textoBadgeEnCurso,
            badgeBackgroundResId = R.drawable.bg_estado_neutro,
            badgeColorHex = "#6B7280",
            iconResId = R.drawable.baseline_access_time_24,
            iconColorHex = "#6B7280",
            detalleText = textoPendienteCierre,
            detalleColorHex = "#6B7280",
            botonText = textoBotonVerAvance,
            botonEnabled = true,
            botonAlpha = 1f
        )
    }

}
