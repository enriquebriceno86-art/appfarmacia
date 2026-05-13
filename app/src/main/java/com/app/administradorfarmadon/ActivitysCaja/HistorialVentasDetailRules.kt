package com.app.administradorfarmadon.ActivitysCaja

import com.app.administradorfarmadon.ClasesDatabase.FechaHoraServidorHelper

object HistorialVentasDetailRules {

    fun construirInfoVentaDetalle(
        data: VentaDetalleRemoteData
    ): InfoVenta {
        val infoBase = if (data.infoVentaOriginal.id.isEmpty()) {
            data.infoVentaOriginal.copy(id = data.idVentaFallback)
        } else {
            data.infoVentaOriginal
        }

        if (data.timestampVentaServidor <= 0L) {
            return infoBase
        }

        return infoBase.copy(
            fecha = FechaHoraServidorHelper.formatearFechaFirebase(data.timestampVentaServidor),
            hora = FechaHoraServidorHelper.formatearHora(data.timestampVentaServidor)
        )
    }

    fun construirListaProductosDetalle(
        productos: List<ProductoCaja?>
    ): List<ProductoCaja> {
        return productos.filterNotNull()
    }
}
