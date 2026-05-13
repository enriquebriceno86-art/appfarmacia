package com.app.administradorfarmadon.ActivitysCaja

object HistorialVentasSummaryRules {

    fun construirResumen(lista: List<VentaHistorial>): HistorialVentasResumen {
        val cantidadVentas = lista.size
        val totalDia = lista.sumOf { it.total }
        val promedio = if (cantidadVentas > 0) totalDia / cantidadVentas else 0.0

        return HistorialVentasResumen(
            cantidadVentas = cantidadVentas,
            totalDia = totalDia,
            promedio = promedio
        )
    }

    fun construirEstadoUi(lista: List<VentaHistorial>): HistorialVentasUiState {
        return HistorialVentasUiState(
            listaVacia = lista.isEmpty(),
            puedeExportar = lista.isNotEmpty()
        )
    }
}
