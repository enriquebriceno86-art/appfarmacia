package com.app.administradorfarmadon.ActivityFragmentos.Fragmentos

import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ProductUtils
import com.app.administradorfarmadon.ActivityInventario.domain.SmartSearchEngine
import com.app.administradorfarmadon.ActivityInventario.reference.normalizeProductName

object InventarioFilterRules {

    private val searchEngine = SmartSearchEngine()

    fun filtrarProductos(
        productos: List<MoldeProductos>,
        criteria: InventarioFilterCriteria
    ): InventarioFilterResult {
        val query = criteria.textoBusqueda.trim()
        
        // Primero aplicamos la búsqueda inteligente si hay texto
        val productosBuscados = if (query.isEmpty()) {
            productos
        } else {
            searchEngine.search(query, productos).map { it.product }
        }

        val filtrados = productosBuscados.filter { producto ->
            val coincideChip = when (criteria.filtroActual) {
                "BAJO_STOCK" -> {
                    (producto.cantidadinicial.toIntOrNull() ?: 0) <=
                        (producto.stockminimo.toIntOrNull() ?: 0)
                }
                "SUFICIENTE" -> {
                    (producto.cantidadinicial.toIntOrNull() ?: 0) >
                        (producto.stockminimo.toIntOrNull() ?: 0)
                }
                "VENCIDOS" -> ProductUtils.obtenerVencimientosParaEvaluacion(producto)
                    .any { ProductUtils.estaVencido(it) }
                "AGOTADO_O_VENCE" -> {
                    val agotado = (producto.cantidadinicial.toIntOrNull() ?: 0) <= 0
                    val vencidoOPorVencer = ProductUtils.obtenerVencimientosParaEvaluacion(producto)
                        .let { lista ->
                            lista.any { ProductUtils.estaVencido(it) } ||
                                lista.mapNotNull { ProductUtils.diasHastaVencerLote(it) }
                                    .any { it in 0..90 }
                        }
                    agotado || vencidoOPorVencer
                }
                "PROXIMOS_VENCER" -> ProductUtils.obtenerVencimientosParaEvaluacion(producto)
                    .mapNotNull { ProductUtils.diasHastaVencerLote(it) }
                    .any { it in 0..90 }
                else -> true
            }

            val coincideCategoria = if (criteria.categoriaSeleccionada == "Todas las categorías") {
                true
            } else {
                producto.categoria == criteria.categoriaSeleccionada
            }

            coincideChip && coincideCategoria
        }

        return InventarioFilterResult(
            filtrados = filtrados,
            hayFiltrosActivos = query.isNotEmpty() ||
                criteria.filtroActual != "TODOS" ||
                criteria.categoriaSeleccionada != "Todas las categorías"
        )
    }

}
