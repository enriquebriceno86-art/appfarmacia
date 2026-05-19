package com.app.administradorfarmadon.ActivityInventario.ClasesProductos

import com.app.administradorfarmadon.ActivityInventario.ProductUtils

data class LoteConsumoResolucion(
    val lotesPorCreacion: List<Pair<String, LoteProducto>>,
    val lotesActivosPorFefo: List<Pair<String, LoteProducto>>,
    val loteRecomendadoFefo: Pair<String, LoteProducto>?,
    val loteSeleccionManual: Pair<String, LoteProducto>?,
    val loteActual: Pair<String, LoteProducto>?,
    val tramosConsumo: List<LoteConsumoTramo>,
    val usaSeleccionManual: Boolean,
    val seleccionManualConfigurada: Boolean,
    val seleccionManualInvalida: Boolean,
    val loteManualInsuficiente: Boolean,
    val loteActualTieneStockSuficiente: Boolean,
    val consumoParcialConFallback: Boolean,
    val consumoAutomaticoMultiLote: Boolean,
    val cantidadCubiertaPorTramos: Int,
    val stockFefoDisponible: Int
)

data class LoteConsumoTramo(
    val clave: String,
    val lote: LoteProducto,
    val cantidad: Int
)

object LoteConsumoRules {

    fun resolver(
        producto: MoldeProductos,
        unidadesRequeridas: Int = 1
    ): LoteConsumoResolucion {
        return resolver(
            lotes = producto.lotes,
            loteSeleccionado = producto.loteConsumoSeleccionado,
            seleccionManual = producto.loteConsumoSeleccionManual,
            unidadesRequeridas = unidadesRequeridas
        )
    }

    fun resolver(
        lotes: Map<String, LoteProducto>,
        loteSeleccionado: String,
        seleccionManual: Boolean,
        unidadesRequeridas: Int = 1
    ): LoteConsumoResolucion {
        val requeridas = unidadesRequeridas.coerceAtLeast(1)
        val lotesPorCreacion = ordenarPorCreacion(lotes)
        val lotesActivosPorFefo = ordenarActivosPorFefo(lotes)
        val recomendadoFefo = lotesActivosPorFefo.firstOrNull()
        val stockFefoDisponible = lotesActivosPorFefo.sumOf {
            ProductUtils.cantidadVendibleLote(it.second).toInt()
        }
        val seleccionManualConfigurada = seleccionManual && loteSeleccionado.isNotBlank()
        val loteManual = if (seleccionManualConfigurada) {
            encontrarLote(lotesPorCreacion, loteSeleccionado)
        } else {
            null
        }
        val loteManualValido = loteManual?.takeIf { (_, lote) ->
            ProductUtils.esLoteValidoParaConsumo(lote)
        }
        val tramosConsumo = construirTramosConsumo(
            lotesActivosPorFefo = lotesActivosPorFefo,
            loteManualPrioritario = loteManualValido,
            unidadesRequeridas = requeridas
        )
        val loteAutomatico = tramosConsumo.firstOrNull()?.let { it.clave to it.lote }
            ?: recomendadoFefo
        val loteManualInsuficiente = loteManualValido != null &&
            loteManualValido.second.cantidad.toInt() < requeridas
        val seleccionManualInvalida = seleccionManualConfigurada && loteManualValido == null
        val usaSeleccionManual = loteManualValido != null
        val loteActual = tramosConsumo.firstOrNull()?.let { it.clave to it.lote } ?: loteAutomatico
        val cantidadCubiertaPorTramos = tramosConsumo.sumOf { it.cantidad }
        val loteActualTieneStockSuficiente =
            cantidadCubiertaPorTramos >= requeridas && tramosConsumo.isNotEmpty()
        val consumoParcialConFallback = loteManualInsuficiente && loteActualTieneStockSuficiente
        val consumoAutomaticoMultiLote = tramosConsumo.size > 1

        return LoteConsumoResolucion(
            lotesPorCreacion = lotesPorCreacion,
            lotesActivosPorFefo = lotesActivosPorFefo,
            loteRecomendadoFefo = recomendadoFefo,
            loteSeleccionManual = loteManual,
            loteActual = loteActual,
            tramosConsumo = tramosConsumo,
            usaSeleccionManual = usaSeleccionManual,
            seleccionManualConfigurada = seleccionManualConfigurada,
            seleccionManualInvalida = seleccionManualInvalida,
            loteManualInsuficiente = loteManualInsuficiente,
            loteActualTieneStockSuficiente = loteActualTieneStockSuficiente,
            consumoParcialConFallback = consumoParcialConFallback,
            consumoAutomaticoMultiLote = consumoAutomaticoMultiLote,
            cantidadCubiertaPorTramos = cantidadCubiertaPorTramos,
            stockFefoDisponible = stockFefoDisponible
        )
    }

    fun ordenarPorCreacion(lotes: Map<String, LoteProducto>): List<Pair<String, LoteProducto>> {
        return lotes.entries
            .map { it.key to it.value }
            .sortedWith(
                compareBy<Pair<String, LoteProducto>>(
                    { it.second.fecha.trim().ifBlank { "9999-99-99" } },
                    { it.second.numero.trim().ifBlank { it.first } }
                )
            )
    }

    fun ordenarActivosPorFefo(lotes: Map<String, LoteProducto>): List<Pair<String, LoteProducto>> {
        return lotes.entries
            .map { it.key to it.value }
            .filter { ProductUtils.esLoteValidoParaConsumo(it.second) }
            .sortedWith(
                compareBy<Pair<String, LoteProducto>>(
                    { ProductUtils.diasHastaVencerLote(ProductUtils.normalizarVencimiento(it.second.vencimiento)) ?: Int.MAX_VALUE },
                    { ProductUtils.normalizarVencimiento(it.second.vencimiento).ifBlank { "9999-99-99" } },
                    { it.second.fecha.trim().ifBlank { "9999-99-99" } },
                    { it.second.numero.trim().ifBlank { it.first } }
                )
            )
    }

    private fun encontrarLote(
        lotesPorCreacion: List<Pair<String, LoteProducto>>,
        loteSeleccionado: String
    ): Pair<String, LoteProducto>? {
        val loteNormalizado = loteSeleccionado.trim()
        return lotesPorCreacion.firstOrNull { (clave, lote) ->
            clave.equals(loteNormalizado, ignoreCase = true) ||
                lote.numero.trim().equals(loteNormalizado, ignoreCase = true)
        }
    }

    private fun construirTramosConsumo(
        lotesActivosPorFefo: List<Pair<String, LoteProducto>>,
        loteManualPrioritario: Pair<String, LoteProducto>?,
        unidadesRequeridas: Int
    ): List<LoteConsumoTramo> {
        val tramos = mutableListOf<LoteConsumoTramo>()
        var restante = unidadesRequeridas.coerceAtLeast(1)

        loteManualPrioritario?.let { (claveManual, loteManual) ->
            val disponiblesManual = ProductUtils.cantidadVendibleLote(loteManual).toInt()
            if (disponiblesManual > 0 && restante > 0) {
                val consumoManual = minOf(disponiblesManual, restante)
                tramos += LoteConsumoTramo(
                    clave = claveManual,
                    lote = loteManual,
                    cantidad = consumoManual
                )
                restante -= consumoManual
            }
        }

        if (restante <= 0) return tramos

        val clavesYaIncluidas = tramos.map { it.clave }.toSet()
        tramos += construirTramosConsumoFefo(
            lotesActivosPorFefo = lotesActivosPorFefo.filterNot { it.first in clavesYaIncluidas },
            unidadesRequeridas = restante
        )
        return tramos
    }

    private fun construirTramosConsumoFefo(
        lotesActivosPorFefo: List<Pair<String, LoteProducto>>,
        unidadesRequeridas: Int
    ): List<LoteConsumoTramo> {
        var restante = unidadesRequeridas.coerceAtLeast(1)
        val tramos = mutableListOf<LoteConsumoTramo>()

        lotesActivosPorFefo.forEach { (clave, lote) ->
            if (restante <= 0) return@forEach
            val disponibles = ProductUtils.cantidadVendibleLote(lote).toInt()
            if (disponibles <= 0) return@forEach

            val consumo = minOf(disponibles, restante)
            tramos += LoteConsumoTramo(
                clave = clave,
                lote = lote,
                cantidad = consumo
            )
            restante -= consumo
        }

        return tramos
    }
}
