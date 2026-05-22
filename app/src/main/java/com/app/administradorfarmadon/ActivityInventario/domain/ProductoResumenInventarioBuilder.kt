package com.app.administradorfarmadon.ActivityInventario.domain

import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.PresentacionProducto
import java.util.Locale
import kotlin.math.abs

/**
 * Construye el resumen del inventario con el mismo formato que ya usa la UI.
 * La idea es centralizar el texto sin cambiar nada visible para el usuario.
 */
object ProductoResumenInventarioBuilder {

    data class EstadoChipStyle(
        val estado: String,
        val bg: String,
        val fg: String,
        val stroke: String
    )

    data class ResumenRapidoStock(
        val stockTexto: String,
        val minimoTexto: String,
        val style: EstadoChipStyle
    )

    data class HelperStockMinimoPresentacion(
        val suffixText: String?,
        val helperText: String?
    )

    fun construirResumen(
        unidadBase: String,
        metodo: String,
        stockActual: Int,
        stockMinimo: Int,
        basePorUnidad: Double
    ): String {
        val stockTexto = formatearStockConUnidades(stockActual, unidadBase, basePorUnidad)
        val minTexto = formatearStockConUnidades(stockMinimo, unidadBase, basePorUnidad)
        return "Base: $unidadBase | Metodo: $metodo\nStock: $stockTexto | Min: $minTexto"
    }

    fun formatearStockConUnidades(
        cantidad: Int,
        unidadBase: String,
        basePorUnidad: Double
    ): String {
        val esMl = unidadBase.equals("mL", ignoreCase = true)
        val esG = unidadBase.equals("g", ignoreCase = true)

        if ((esMl || esG) && basePorUnidad > 0.0) {
            val unidades = cantidad / basePorUnidad
            val unidadesEnteras = unidades.toInt()
            val enteroExacto = abs(unidades - unidadesEnteras) < 0.01
            val unidadesTexto = if (enteroExacto) {
                unidadesEnteras.toString()
            } else {
                String.format(Locale.getDefault(), "%.1f", unidades)
            }
            return "$unidadesTexto unidades ($cantidad $unidadBase)"
        }

        return "$cantidad $unidadBase"
    }

    fun construirResumenRapidoStock(
        unidadBaseRaw: String?,
        unidadVisual: String?,
        stockActualBase: Double,
        minimoActualBase: Double
    ): ResumenRapidoStock {
        val factor = if (unidadVisual == "kg" || unidadVisual == "L") 1000.0 else 1.0
        val visualStock = stockActualBase / factor
        val visualMin = minimoActualBase / factor

        val unitLabel = if (!unidadVisual.isNullOrBlank()) unidadVisual else unidadBaseRaw ?: "unidades"
        
        val stockStr = if (visualStock % 1.0 == 0.0) visualStock.toInt().toString() else String.format(Locale.US, "%.2f", visualStock)
        val minStr = if (visualMin % 1.0 == 0.0) visualMin.toInt().toString() else String.format(Locale.US, "%.2f", visualMin)

        val style = when {
            stockActualBase <= 0.0 -> EstadoChipStyle("Sin stock", "#FDECEC", "#B3261E", "#F5C2C7")
            minimoActualBase > 0.0 && stockActualBase <= minimoActualBase ->
                EstadoChipStyle("Bajo stock", "#FFF4E5", "#8A4B00", "#FFE0B2")
            else -> EstadoChipStyle("Disponible", "#E8F5EE", "#0F7A3A", "#BFE5CC")
        }

        return ResumenRapidoStock(
            stockTexto = "Stock: $stockStr $unitLabel",
            minimoTexto = "Mínimo: $minStr $unitLabel",
            style = style
        )
    }

    fun debeAdvertirStockMinimoInicial(
        stockActual: Int,
        stockMinimo: Int
    ): Boolean {
        return stockActual > 0 && stockMinimo > stockActual
    }

    fun construirTextoStockConfirmacion(
        unidadBase: String,
        stockActual: Int,
        basePorUnidadLocal: Double
    ): String {
        val esMl = unidadBase.equals("mL", ignoreCase = true)
        val esG = unidadBase.equals("g", ignoreCase = true)

        return if ((esMl || esG) && basePorUnidadLocal > 0) {
            val contenedores = (stockActual / basePorUnidadLocal).toInt()
            val nombreCont = if (esMl) "Frasco" else "Paquete"
            val plural = if (contenedores == 1) nombreCont else "${nombreCont}s"
            "$contenedores $plural ($stockActual $unidadBase)"
        } else {
            "$stockActual $unidadBase"
        }
    }

    fun construirTextoPresentacionesConfirmacion(
        tienePresentaciones: Boolean,
        presentaciones: List<PresentacionProducto>,
        unidadBase: String,
        nombreVisible: (String) -> String,
        formatearPrecio: (Double) -> String = { it.toString() }
    ): String {
        if (!tienePresentaciones || presentaciones.isEmpty()) return ""

        val lineas = presentaciones.joinToString("\n") { p ->
            val nombre = nombreVisible(p.nombre)
            val contenido = "${p.cantidad} $unidadBase"
            val precio = if (p.precioventa > 0.0) {
                " — se vende a ${formatearPrecio(p.precioventa)}"
            } else ""
            "  • $nombre ($contenido)$precio"
        }
        return "\nFormas de venta:\n$lineas"
    }

    fun construirHelperStockMinimoPresentacion(
        unidadBase: String,
        stockMinContenedores: Int?,
        valorPorContenedor: Int?
    ): HelperStockMinimoPresentacion {
        val esMl = unidadBase.equals("mL", ignoreCase = true)
        val esG = unidadBase.equals("g", ignoreCase = true)

        val singContenedor = when {
            esMl -> "frasco"
            esG -> "paquete"
            else -> "caja"
        }
        val plurContenedor = when {
            esMl -> "frascos"
            esG -> "paquetes"
            else -> "cajas"
        }
        val etiquetaUnidadBase = when {
            esMl -> "mL"
            esG -> "g"
            else -> unidadBase.ifBlank { "unidades" }
        }

        val helperText = when {
            valorPorContenedor == null || valorPorContenedor <= 0 ->
                "Define cuántos $etiquetaUnidadBase trae 1 $singContenedor. El mínimo se controla en $etiquetaUnidadBase."

            stockMinContenedores == null || stockMinContenedores < 0 ->
                "Ingresa el mínimo en $plurContenedor. Ej: 5 $plurContenedor x $valorPorContenedor $etiquetaUnidadBase = alerta a ${5 * valorPorContenedor} $etiquetaUnidadBase."

            else -> {
                val totalEnUnidadBase = stockMinContenedores * valorPorContenedor
                val etiquetaCont = if (stockMinContenedores == 1) singContenedor else plurContenedor
                "Mínimo: $stockMinContenedores $etiquetaCont (1 $singContenedor = $valorPorContenedor $etiquetaUnidadBase). Alerta al llegar a $totalEnUnidadBase $etiquetaUnidadBase."
            }
        }

        return HelperStockMinimoPresentacion(
            suffixText = plurContenedor,
            helperText = helperText
        )
    }
}
