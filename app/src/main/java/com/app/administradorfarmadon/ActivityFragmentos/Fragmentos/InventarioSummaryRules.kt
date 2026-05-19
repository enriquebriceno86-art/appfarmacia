package com.app.administradorfarmadon.ActivityFragmentos.Fragmentos

import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ProductUtils
import java.util.Calendar

object InventarioSummaryRules {

    fun construirResumen(productos: List<MoldeProductos>): InventarioResumenState {
        val total = productos.size
        val bajoStock = productos.count { producto ->
            val cantidad = producto.cantidadinicial.toIntOrNull() ?: 0
            val minimo = producto.stockminimo.toIntOrNull() ?: 0
            cantidad <= minimo && producto.estadodelproducto
        }
        val porVencer = productos.count { producto ->
            ProductUtils.obtenerVencimientosParaEvaluacion(producto)
                .mapNotNull { ProductUtils.diasHastaVencerLote(it) }
                .any { it in 0..90 }
        }
        return InventarioResumenState(
            total = total,
            bajoStock = bajoStock,
            porVencer = porVencer,
            mostrarStats = total > 0
        )
    }

    fun construirBannerGeneral(productos: List<MoldeProductos>): InventarioBannerGeneralState {
        var bajoStock = 0
        var proximosVencer = 0

        for (producto in productos) {
            if (!producto.estadodelproducto) continue

            val stock = producto.cantidadinicial.trim().toIntOrNull() ?: 0
            val minimo = producto.stockminimo.trim().toIntOrNull() ?: 0
            if (minimo > 0 && stock <= minimo) bajoStock++

            val tieneLotePorVencer = ProductUtils.obtenerVencimientosParaEvaluacion(producto)
                .mapNotNull { ProductUtils.diasHastaVencerLote(it) }
                .any { it in 0..30 }
            if (tieneLotePorVencer) {
                proximosVencer++
            }
        }

        if (bajoStock == 0 && proximosVencer == 0) {
            return InventarioBannerGeneralState(
                mostrar = false,
                mensaje = ""
            )
        }

        val partes = mutableListOf<String>()
        if (bajoStock > 0) partes.add("🔴 $bajoStock con stock bajo")
        if (proximosVencer > 0) partes.add("🟡 $proximosVencer próximos a vencer")

        return InventarioBannerGeneralState(
            mostrar = true,
            mensaje = partes.joinToString("  ·  ")
        )
    }

    fun construirStockBajoTablet(productos: List<MoldeProductos>): InventarioStockBajoTabletState {
        val cantidadBajoStock = productos.count { producto ->
            val cantidad = producto.cantidadinicial.toIntOrNull() ?: 0
            val minimo = producto.stockminimo.toIntOrNull() ?: 0
            cantidad <= minimo && producto.estadodelproducto
        }

        return InventarioStockBajoTabletState(
            mostrar = cantidadBajoStock > 0,
            mensaje = "⚠️ $cantidadBajoStock producto${if (cantidadBajoStock == 1) "" else "s"} con stock bajo o agotado"
        )
    }

    fun construirEstadoVacio(
        isEmpty: Boolean,
        hayFiltrosActivos: Boolean,
        query: String = ""
    ): InventarioEstadoVacioState {
        return if (isEmpty && hayFiltrosActivos) {
            val q = query.lowercase().trim()
            val descripcion = if (q.length >= 2) {
                // Sugerencias inteligentes basadas en el query (V4.9: Sincronizadas con ClinicalDictionary)
                val sugerenciasDinamicas = com.app.administradorfarmadon.ActivityInventario.domain.ClinicalDictionary.findSuggestionsForQuery(q)
                
                val sugerenciaTexto = if (sugerenciasDinamicas.isNotEmpty()) {
                    val items = sugerenciasDinamicas.filter { !it.contains(q) }.take(3)
                    if (items.isNotEmpty()) "Prueba con: ${items.joinToString(", ")}."
                    else "Prueba con términos más generales."
                } else {
                    "Prueba con sinónimos o términos más generales."
                }
                
                "No encontramos resultados para \"$query\".\n$sugerenciaTexto"
            } else {
                "No encontramos productos con esos filtros."
            }
            InventarioEstadoVacioState(
                mostrarVacio = true,
                mostrarLista = false,
                titulo = "Sin resultados",
                descripcion = descripcion,
                mostrarBotonLimpiar = true
            )
        } else if (isEmpty) {
            InventarioEstadoVacioState(
                mostrarVacio = true,
                mostrarLista = false,
                titulo = "Inventario vacío",
                descripcion = "Agrega tu primer producto para empezar a vender y controlar stock.",
                mostrarBotonLimpiar = false
            )
        } else {
            InventarioEstadoVacioState(
                mostrarVacio = false,
                mostrarLista = true,
                titulo = "",
                descripcion = "",
                mostrarBotonLimpiar = false
            )
        }
    }

    fun construirAlertasLotes(
        productos: List<MoldeProductos>,
        resolverDiasHastaVencerLote: (String) -> Int?
    ): InventarioAlertasLotesState {
        val alertas = mutableListOf<InventarioLoteAlertaItem>()

        productos.forEach { producto ->
            producto.lotes.forEach { (_, lote) ->
                if (lote.vencimiento.isBlank()) return@forEach
                val dias = resolverDiasHastaVencerLote(lote.vencimiento) ?: return@forEach
                if (dias <= 60) {
                    alertas.add(
                        InventarioLoteAlertaItem(
                            nombreProducto = producto.nombre,
                            numeroLote = lote.numero.ifBlank { "Sin nro." },
                            vencimiento = lote.vencimiento,
                            diasRestantes = dias
                        )
                    )
                }
            }
        }

        val alertasOrdenadas = alertas.sortedBy { it.diasRestantes }
        val vencidos = alertasOrdenadas.count { it.diasRestantes < 0 }
        val proximos = alertasOrdenadas.count { it.diasRestantes in 0..60 }

        val textoBanner = buildString {
            append("⚠️  ")
            if (vencidos > 0) {
                append("$vencidos lote${if (vencidos == 1) "" else "s"} vencido${if (vencidos == 1) "" else "s"}")
                if (proximos > 0) append("  ·  ")
            }
            if (proximos > 0) {
                append("$proximos por vencer pronto")
            }
        }

        return InventarioAlertasLotesState(
            alertas = alertasOrdenadas,
            vencidos = vencidos,
            proximos = proximos,
            mostrarBanner = alertasOrdenadas.isNotEmpty(),
            textoBanner = textoBanner
        )
    }

}
