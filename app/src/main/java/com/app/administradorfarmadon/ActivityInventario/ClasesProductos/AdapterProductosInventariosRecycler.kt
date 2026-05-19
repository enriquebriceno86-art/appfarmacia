package com.app.administradorfarmadon.ActivityInventario.ClasesProductos

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ActivityInventario.EditarProductodelInventario
import com.app.administradorfarmadon.ActivityInventario.ProductAvatarHelper
import com.app.administradorfarmadon.ActivityInventario.ProductUtils
import com.app.administradorfarmadon.ActivityInventario.domain.SmartSearchEngine
import com.app.administradorfarmadon.ClasesDatabase.UnidadBaseHelper
import com.app.administradorfarmadon.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import java.util.Locale
import kotlin.math.abs

class AdapterProductosInventariosRecycler(
    private var listaFiltrada: MutableList<MoldeProductos> = mutableListOf(),
    private val onQuickStockClick: ((MoldeProductos) -> Unit)? = null,
    private val onVerHistorialClick: ((MoldeProductos) -> Unit)? = null
) : RecyclerView.Adapter<AdapterProductosInventariosRecycler.ProductosViewHolder>() {

    private var ultimosResultadosBusqueda: List<SmartSearchEngine.SearchResult> = emptyList()
    private var busquedaClinicaActiva: Boolean = false // V4.7: Flag para control de badges

    private companion object {
        const val ESTADO_DISPONIBLE = "Disponible"
        const val ESTADO_BAJO_STOCK = "Bajo stock"
        const val ESTADO_POR_VENCER = "Por vencer"
        const val ESTADO_VENCIDO = "Vencido"
        const val ESTADO_SIN_STOCK = "Sin stock"
        const val ESTADO_INACTIVO = "Inactivo"
    }

    private data class EstadoInventarioUi(
        val texto: String,
        val backgroundColor: Int,
        val textColor: Int
    )

    private data class AlertaInventarioUi(
        val texto: String,
        val backgroundColor: Int,
        val textColor: Int
    )

    fun updateList(newList: List<MoldeProductos>) {
        updateListWithResults(newList.map { SmartSearchEngine.SearchResult(it, SmartSearchEngine.MatchType.NONE) })
    }

    fun updateListWithResults(results: List<SmartSearchEngine.SearchResult>, queryActiva: Boolean = false) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = ultimosResultadosBusqueda.size
            override fun getNewListSize(): Int = results.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return ultimosResultadosBusqueda[oldItemPosition].product.indice == 
                       results[newItemPosition].product.indice
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val old = ultimosResultadosBusqueda[oldItemPosition]
                val new = results[newItemPosition]
                
                // V4.7: Comparamos el producto Y el estado de búsqueda para evitar "badges fantasma"
                return old.product == new.product && 
                       old.matchType == new.matchType && 
                       old.matchedText == new.matchedText &&
                       busquedaClinicaActiva == queryActiva
            }
        })
        
        listaFiltrada.clear()
        listaFiltrada.addAll(results.map { it.product })
        ultimosResultadosBusqueda = results
        busquedaClinicaActiva = queryActiva
        diffResult.dispatchUpdatesTo(this)
    }

    class ProductosViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvCategoria: TextView = view.findViewById(R.id.tvCategoria)
        val tvSintomaSugerido: TextView = view.findViewById(R.id.tvSintomaSugerido)
        val tvResumenInventario: TextView = view.findViewById(R.id.tvResumenInventario)
        val tvCantidad: TextView = view.findViewById(R.id.tvCantidad)
        val tvDetalleStock: TextView? = view.findViewById(R.id.tvDetalleStock)
        val tvResumenMinimo: TextView = view.findViewById(R.id.tvResumenMinimo)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val cardEstado: MaterialCardView = view.findViewById(R.id.cardEstado)
        val cardSintomaBadge: MaterialCardView? = view.findViewById(R.id.cardSintomaBadge)
        val ivProducto: ShapeableImageView = view.findViewById(R.id.ivProducto)
        val cardFecha: MaterialCardView = view.findViewById(R.id.cardFecha)
        val btnQuickStock: MaterialButton = view.findViewById(R.id.btnQuickStock)
        val cardFotoFondo: MaterialCardView? = view.findViewById(R.id.cardFotoFondo)
        val btnHistorial: MaterialButton? = view.findViewById(R.id.btnHistorial)
        val viewEstadoDot: View? = view.findViewById(R.id.viewEstadoDot)
        // Badge inline tipo "Por vencer" / "Stock bajo" en la fila plana
        val tvEstadoBadgeInline: TextView? = view.findViewById(R.id.tvEstadoBadgeInline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductosViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.reciclerproductosinventarios, parent, false)
        return ProductosViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductosViewHolder, position: Int) {
        val productoItem = listaFiltrada[position]
        val context = holder.itemView.context
        val esTablet = context.resources.configuration.smallestScreenWidthDp >= 720

        val cantidadActual = productoItem.cantidadinicial.toIntOrNull() ?: 0
        val stockMinimo = productoItem.stockminimo.toIntOrNull() ?: 0
        val categoria = productoItem.categoria.ifBlank { "Sin categoria" }
        val activo = productoItem.estadodelproducto
        val cantidadLotesRegistrados = ProductUtils.cantidadLotesRegistrados(productoItem)
        val tieneMultiplesLotes = ProductUtils.tieneMultiplesLotesRegistrados(productoItem)
        val fechaGeneralVisible = ProductUtils.obtenerVencimientoGeneralVisible(productoItem)
        val estadoVencimiento = ProductUtils.obtenerEstadoVencimiento(productoItem).orEmpty()

        if (esTablet) {
            holder.itemView.setBackgroundColor(0xFFFFFFFF.toInt())
        }

        holder.tvNombre.text = productoItem.nombre.ifBlank { "Sin nombre" }
        
        // Manejo del badge de síntoma inteligente (V4.9: Solo si hay búsqueda activa)
        val resultado = ultimosResultadosBusqueda.find { it.product.indice == productoItem.indice }
        val esSintomaMatch = resultado != null && (
            resultado.matchType == SmartSearchEngine.MatchType.SYMPTOM_DIRECT || 
            resultado.matchType == SmartSearchEngine.MatchType.SYMPTOM_EXPANDED
        )
        val mostrarBadge = busquedaClinicaActiva && esSintomaMatch && !resultado?.matchedText.isNullOrBlank()

        if (mostrarBadge && resultado != null) {
            holder.cardSintomaBadge?.visibility = View.VISIBLE
            // Truncamos y simplificamos para UX: "Para: fiebre" en vez de frases largas
            val matchedText = resultado.matchedText ?: ""
            val displaySymptom = if (matchedText.length > 25) {
                matchedText.take(22) + "..."
            } else {
                matchedText
            }
            holder.tvSintomaSugerido.text = "✨ Para: ${displaySymptom.replaceFirstChar { it.uppercase() }}"
        } else {
            holder.cardSintomaBadge?.visibility = View.GONE
        }

        // En móvil el subtítulo es "Categoría · Presentación" tipo Play Store; en tablet, solo categoría
        val presentacion = productoItem.presentacionprincipal.ifBlank { "" }
        holder.tvCategoria.text = if (!esTablet && presentacion.isNotBlank()) {
            "$categoria · $presentacion"
        } else {
            categoria
        }

        val unidadBaseRaw = productoItem.unidadbase
        val unidadVisualRaw = productoItem.unidadVisualInventario.ifBlank { unidadBaseRaw }
        val basePorUnidad = productoItem.basePorUnidad
        val esMl = unidadBaseRaw.equals("mL", ignoreCase = true)
        val esG = unidadBaseRaw.equals("g", ignoreCase = true)
        val usaUnidadVisualMayor = usaUnidadVisualMayor(unidadBaseRaw, unidadVisualRaw)
        val muestraUnidades = (esMl || esG) && basePorUnidad > 0

        val stockInfo = if (usaUnidadVisualMayor) {
            ProductUtils.InfoStockEstructurada(
                "${formatearCantidadUnidadVisual(cantidadActual, unidadVisualRaw)} $unidadVisualRaw",
                ""
            )
        } else if (muestraUnidades) {
            val cantidadVisible = formatearCantidadContenedores(cantidadActual, basePorUnidad)
            val unidadVisible = if (esTablet) {
                resolverUnidadVisible(unidadBaseRaw, cantidadActual, muestraUnidades)
            } else {
                resolverUnidadAbreviada(unidadBaseRaw, cantidadActual, muestraUnidades)
            }
            ProductUtils.InfoStockEstructurada("$cantidadVisible $unidadVisible", "")
        } else {
            ProductUtils.obtenerInfoStockEstructurada(productoItem)
        }

        holder.tvCantidad.text = stockInfo.principal
        if (stockInfo.detalle.isNotBlank() && !esTablet) {
            holder.tvDetalleStock?.text = stockInfo.detalle
            holder.tvDetalleStock?.visibility = View.VISIBLE
        } else {
            holder.tvDetalleStock?.visibility = View.GONE
        }

        resolverUnidadVisible(unidadBaseRaw, cantidadActual, muestraUnidades)
        holder.tvResumenMinimo.text = if (esTablet) {
            "${stockMinimo.coerceAtLeast(0)} ${resolverUnidadVisible(unidadBaseRaw, stockMinimo, muestraUnidades)}"
        } else {
            "${stockMinimo.coerceAtLeast(0)} ${resolverUnidadAbreviada(unidadBaseRaw, stockMinimo, muestraUnidades)}"
        }
        holder.tvResumenMinimo.text = formatearMinimoVisible(productoItem, esTablet, muestraUnidades)

        holder.itemView.alpha = if (activo) 1.0f else 0.5f

        val estadoPrincipal = resolverEstadoPrincipal(
            cantidadActual = cantidadActual,
            stockMinimo = stockMinimo
        )
        holder.tvEstado.text = estadoPrincipal.texto
        holder.cardEstado.setCardBackgroundColor(ColorStateList.valueOf(estadoPrincipal.backgroundColor))
        holder.tvEstado.setTextColor(ColorStateList.valueOf(estadoPrincipal.textColor))
        holder.viewEstadoDot?.backgroundTintList = ColorStateList.valueOf(estadoPrincipal.textColor)

        // En el diseño de tarjeta móvil, coloreamos el valor de Stock según el estado
        // (verde = suficiente, naranja = bajo, rojo = agotado)
        if (!esTablet) {
            if (estadoPrincipal.texto == "Suficiente") {
                holder.tvCantidad.setTextColor(context.getColor(R.color.text_primary))
                // Detalle en gris suave si es suficiente (para no saturar de verde)
                holder.tvDetalleStock?.setTextColor(context.getColor(R.color.text_tertiary_muted))
                holder.tvDetalleStock?.alpha = 1.0f
            } else {
                holder.tvCantidad.setTextColor(estadoPrincipal.textColor)
                holder.tvDetalleStock?.setTextColor(estadoPrincipal.textColor)
                holder.tvDetalleStock?.alpha = 0.8f
            }
        }

        val vencimientoUi = resolverVencimientoUi(
            fechaGeneralVisible = fechaGeneralVisible,
            estadoVencimiento = estadoVencimiento,
            tieneMultiplesLotes = tieneMultiplesLotes,
            cantidadLotesRegistrados = cantidadLotesRegistrados
        )
        holder.cardFecha.visibility = View.VISIBLE
        holder.cardFecha.setCardBackgroundColor(ColorStateList.valueOf(vencimientoUi.backgroundColor))
        holder.tvFecha.text = vencimientoUi.fechaPrincipal
        holder.tvFecha.setTextColor(ColorStateList.valueOf(vencimientoUi.textColor))
        holder.tvResumenInventario.text = if (esTablet) {
            cantidadLotesRegistrados.coerceAtLeast(0).toString()
        } else {
            cantidadLotesRegistrados.coerceAtLeast(0).toString()
        }
        holder.btnQuickStock.text = if (esTablet) "Ver detalles" else "Ver >"

        holder.tvCategoria.visibility = View.VISIBLE
        holder.cardEstado.visibility = if (esTablet) View.VISIBLE else View.GONE
        holder.btnQuickStock.visibility = if (esTablet) View.VISIBLE else View.GONE
        holder.btnHistorial?.visibility = if (esTablet && onVerHistorialClick != null) View.VISIBLE else View.GONE
        holder.cardFecha.setCardBackgroundColor(
            ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
        )

        if (!productoItem.imagenUrl.isNullOrEmpty()) {
            if (holder.cardFotoFondo != null) {
                Glide.with(context)
                    .asBitmap()
                    .load(productoItem.imagenUrl)
                    .centerInside()
                    .placeholder(R.drawable.iconpastillas)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            holder.ivProducto.setImageBitmap(resource)
                            val palette = Palette.from(resource).generate()
                            val baseColor = palette.getLightVibrantColor(
                                palette.getVibrantColor(
                                    palette.getMutedColor(0xFFE8EAED.toInt())
                                )
                            )
                            val suave = ColorUtils.blendARGB(baseColor, android.graphics.Color.WHITE, 0.90f)
                            holder.cardFotoFondo.setCardBackgroundColor(suave)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            holder.ivProducto.setImageDrawable(placeholder)
                            holder.cardFotoFondo.setCardBackgroundColor(0xFFF8F9FA.toInt())
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            holder.ivProducto.setImageDrawable(
                                ProductAvatarHelper.createAvatarDrawable(
                                    context,
                                    productoItem.nombre,
                                    productoItem.categoria
                                )
                            )
                            holder.cardFotoFondo.setCardBackgroundColor(0xFFF8F9FA.toInt())
                        }
                    })
            } else {
                ProductAvatarHelper.loadInto(
                    imageView = holder.ivProducto,
                    imageUrl = productoItem.imagenUrl,
                    productName = productoItem.nombre,
                    category = productoItem.categoria
                )
            }
        } else {
            holder.ivProducto.setImageDrawable(
                ProductAvatarHelper.createAvatarDrawable(
                    context,
                    productoItem.nombre,
                    productoItem.categoria
                )
            )
            holder.cardFotoFondo?.setCardBackgroundColor(0xFFF8F9FA.toInt())
        }

        holder.btnQuickStock.setOnClickListener {
            if (onQuickStockClick != null) {
                onQuickStockClick.invoke(productoItem)
            } else {
                val intent = Intent(context, EditarProductodelInventario::class.java)
                intent.putExtra("indice", productoItem.indice)
                intent.putExtra("auto_ingreso_stock", true)
                context.startActivity(intent)
            }
        }

        if (onVerHistorialClick != null && esTablet) {
            holder.btnHistorial?.visibility = View.VISIBLE
            holder.btnHistorial?.setOnClickListener { onVerHistorialClick.invoke(productoItem) }
        } else {
            holder.btnHistorial?.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, EditarProductodelInventario::class.java)
            intent.putExtra("indice", productoItem.indice)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = listaFiltrada.size

    private fun formatearCantidadContenedores(cantidad: Int, basePorUnidad: Double): String {
        if (basePorUnidad <= 0.0) return cantidad.toString()
        val unidades = cantidad / basePorUnidad
        val enteras = unidades.toInt()
        val enteroExacto = abs(unidades - enteras) < 0.01
        return if (enteroExacto) {
            enteras.toString()
        } else {
            String.format(Locale.getDefault(), "%.1f", unidades)
        }
    }

    private fun usaUnidadVisualMayor(unidadBaseRaw: String, unidadVisualRaw: String): Boolean {
        return (unidadBaseRaw.equals("g", ignoreCase = true) && unidadVisualRaw.equals("kg", ignoreCase = true)) ||
            (unidadBaseRaw.equals("mL", ignoreCase = true) && unidadVisualRaw.equals("L", ignoreCase = true))
    }

    private fun formatearCantidadUnidadVisual(cantidadBase: Int, unidadVisualRaw: String): String {
        val divisor = when (unidadVisualRaw.trim()) {
            "kg", "L" -> 1000.0
            else -> 1.0
        }
        val value = cantidadBase / divisor
        val entero = value.toInt()
        return if (abs(value - entero) < 0.01) {
            entero.toString()
        } else {
            String.format(Locale.getDefault(), "%.2f", value).trimEnd('0').trimEnd(',')
        }
    }

    private fun resolverUnidadVisible(
        unidadBaseRaw: String,
        cantidad: Int,
        muestraUnidades: Boolean
    ): String {
        return if (muestraUnidades) {
            if (cantidad == 1) "unidad" else "unidades"
        } else {
            UnidadBaseHelper.formatear(unidadBaseRaw, maxOf(cantidad, 1))
        }
    }

    private fun resolverUnidadAbreviada(
        unidadBaseRaw: String,
        cantidad: Int,
        muestraUnidades: Boolean
    ): String {
        if (muestraUnidades) return "uds"

        return when (unidadBaseRaw.trim().lowercase(Locale.getDefault())) {
            "", "unidad", "unidades", "und", "uds" -> "uds"
            "caja", "cajas" -> if (cantidad == 1) "caja" else "cajas"
            "frasco", "frascos" -> if (cantidad == 1) "frasco" else "frascos"
            "blister", "blíster", "blisters", "blísteres" -> "blíst."
            "tableta", "tabletas", "tablet", "tablets", "tab", "tabs" -> "tabs"
            else -> {
                val visible = resolverUnidadVisible(unidadBaseRaw, cantidad, muestraUnidades)
                when (visible.lowercase(Locale.getDefault())) {
                    "unidad", "unidades" -> "uds"
                    "blister", "blíster", "blísteres", "blisters" -> "blíst."
                    "tableta", "tabletas" -> "tabs"
                    else -> visible
                }
            }
        }
    }

    private fun formatearMinimoVisible(
        producto: MoldeProductos,
        esTablet: Boolean,
        muestraUnidades: Boolean
    ): String {
        val minimoUnidadBase = producto.stockminimo.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val minimoContenedores = producto.stockMinimoContenedores.coerceAtLeast(0)
        val unidadMinimoGuardada = producto.unidadStockMinimo.trim()
        val unidadVisualRaw = producto.unidadVisualInventario.ifBlank { producto.unidadbase }
        if (usaUnidadVisualMayor(producto.unidadbase, unidadVisualRaw)) {
            return "${formatearCantidadUnidadVisual(minimoUnidadBase, unidadVisualRaw)} $unidadVisualRaw"
        }
        val presentacionPrincipal = producto.presentacionprincipal.trim()

        val unidadPreferida = when {
            unidadMinimoGuardada.isNotBlank() &&
                !unidadMinimoGuardada.equals("unidad", ignoreCase = true) &&
                !unidadMinimoGuardada.equals("unidades", ignoreCase = true) &&
                !unidadMinimoGuardada.equals("und", ignoreCase = true) &&
                !unidadMinimoGuardada.equals("uds", ignoreCase = true) -> unidadMinimoGuardada
            presentacionPrincipal.isNotBlank() && minimoContenedores > 0 -> presentacionPrincipal
            else -> ""
        }

        if (minimoContenedores > 0 && unidadPreferida.isNotBlank()) {
            return "$minimoContenedores ${resolverNombreUnidad(unidadPreferida, minimoContenedores)}"
        }

        return if (esTablet) {
            "${minimoUnidadBase} ${resolverUnidadVisible(producto.unidadbase, minimoUnidadBase, muestraUnidades)}"
        } else {
            "${minimoUnidadBase} ${resolverUnidadAbreviada(producto.unidadbase, minimoUnidadBase, muestraUnidades)}"
        }
    }

    private fun resolverNombreUnidad(unidadRaw: String, cantidad: Int): String {
        val unidad = unidadRaw.trim().lowercase(Locale.getDefault())
        if (unidad.isBlank()) return if (cantidad == 1) "unidad" else "unidades"

        return when {
            cantidad == 1 -> unidad
            unidad.endsWith("s") -> unidad
            unidad.endsWith("x") -> "${unidad}es"
            else -> "${unidad}s"
        }
    }

    private fun resolverEstadoPrincipal(
        cantidadActual: Int,
        stockMinimo: Int
    ): EstadoInventarioUi {
        return when {
            cantidadActual <= 0 -> EstadoInventarioUi("Agotado", 0xFFFDECEC.toInt(), 0xFFF04438.toInt())
            cantidadActual <= stockMinimo -> EstadoInventarioUi("Mínimo/bajo", 0xFFFFF4E5.toInt(), 0xFFF79009.toInt())
            else -> EstadoInventarioUi("Suficiente", 0xFFEAFBF3.toInt(), 0xFF12B76A.toInt())
        }
    }

    private fun resolverAlertaInventario(
        cantidadActual: Int,
        stockMinimo: Int,
        fechaGeneralVisible: String,
        estadoVencimiento: String,
        tieneMultiplesLotes: Boolean,
        cantidadLotesRegistrados: Int
    ): AlertaInventarioUi? {
        if (tieneMultiplesLotes && cantidadLotesRegistrados > 0) {
            return when (estadoVencimiento) {
                "VENCIDO" -> AlertaInventarioUi(
                    texto = "$cantidadLotesRegistrados lotes vencidos",
                    backgroundColor = 0xFFFDEAEA.toInt(),
                    textColor = 0xFFD93025.toInt()
                )
                "POR_VENCER" -> AlertaInventarioUi(
                    texto = "$cantidadLotesRegistrados lotes por vencer",
                    backgroundColor = 0xFFFEF7E0.toInt(),
                    textColor = 0xFFE37400.toInt()
                )
                else -> AlertaInventarioUi(
                    texto = "$cantidadLotesRegistrados lotes con vencimiento",
                    backgroundColor = 0xFFE8F0FE.toInt(),
                    textColor = 0xFF1967D2.toInt()
                )
            }
        }

        if (fechaGeneralVisible.isNotBlank()) {
            return when (estadoVencimiento) {
                "VENCIDO" -> AlertaInventarioUi(
                    texto = "Vencido: $fechaGeneralVisible",
                    backgroundColor = 0xFFFDEAEA.toInt(),
                    textColor = 0xFFD93025.toInt()
                )
                "POR_VENCER" -> AlertaInventarioUi(
                    texto = "Por vencer: $fechaGeneralVisible",
                    backgroundColor = 0xFFFEF7E0.toInt(),
                    textColor = 0xFFE37400.toInt()
                )
                else -> AlertaInventarioUi(
                    texto = "Vence: $fechaGeneralVisible",
                    backgroundColor = 0xFFE8F0FE.toInt(),
                    textColor = 0xFF1967D2.toInt()
                )
            }
        }

        if (stockMinimo > 0 && cantidadActual in 1..stockMinimo) {
            return AlertaInventarioUi(
                texto = ESTADO_BAJO_STOCK,
                backgroundColor = 0xFFFEF7E0.toInt(),
                textColor = 0xFFE37400.toInt()
            )
        }

        return null
    }

    private data class VencimientoUi(
        val fechaPrincipal: String,
        val fechaSecundaria: String,
        val backgroundColor: Int,
        val textColor: Int
    )

    private fun resolverVencimientoUi(
        fechaGeneralVisible: String,
        estadoVencimiento: String,
        tieneMultiplesLotes: Boolean,
        cantidadLotesRegistrados: Int
    ): VencimientoUi {
        val colorDisponible = 0xFFF4F7FB.toInt()
        val textoDisponible = 0xFF475467.toInt()
        val colorAdvertencia = 0xFFFFF4E5.toInt()
        val textoAdvertencia = 0xFFE37400.toInt()
        val colorPeligro = 0xFFFFEBEE.toInt()
        val textoPeligro = 0xFFD93025.toInt()

        if (tieneMultiplesLotes && cantidadLotesRegistrados > 0) {
            return when (estadoVencimiento) {
                "VENCIDO" -> VencimientoUi(
                    fechaPrincipal = "Varios",
                    fechaSecundaria = "Lotes vencidos",
                    backgroundColor = colorPeligro,
                    textColor = textoPeligro
                )
                "POR_VENCER" -> VencimientoUi(
                    fechaPrincipal = "Varios",
                    fechaSecundaria = "Lotes por vencer",
                    backgroundColor = colorAdvertencia,
                    textColor = textoAdvertencia
                )
                else -> VencimientoUi(
                    fechaPrincipal = "Varios",
                    fechaSecundaria = "Con vencimiento",
                    backgroundColor = colorDisponible,
                    textColor = textoDisponible
                )
            }
        }

        val fechaNormalizada = fechaGeneralVisible.trim()
        if (fechaNormalizada.isBlank()) {
            return VencimientoUi(
                fechaPrincipal = "—",
                fechaSecundaria = "Sin vencimiento",
                backgroundColor = colorDisponible,
                textColor = textoDisponible
            )
        }

        val partes = fechaNormalizada.replace("_", "/").split("/")
        val fechaCorta = if (partes.size >= 2) {
            "${partes[0].padStart(2, '0')}/${partes[1].takeLast(2)}"
        } else {
            fechaNormalizada
        }
        val fechaLarga = formatearFechaLarga(partes)

        return when (estadoVencimiento) {
            "VENCIDO" -> VencimientoUi(fechaCorta, fechaLarga, colorPeligro, textoPeligro)
            "POR_VENCER" -> VencimientoUi(fechaCorta, fechaLarga, colorAdvertencia, textoAdvertencia)
            else -> VencimientoUi(fechaCorta, fechaLarga, colorDisponible, textoDisponible)
        }
    }

    private fun formatearFechaLarga(partes: List<String>): String {
        if (partes.size < 2) return "Sin fecha"
        val mes = partes[0].toIntOrNull()
        val anio = partes[1].toIntOrNull()
        val nombreMes = when (mes) {
            1 -> "Ene"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Abr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Ago"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Dic"
            else -> return "Sin fecha"
        }
        val anioVisible = when {
            anio == null -> ""
            anio < 100 -> "20${anio.toString().padStart(2, '0')}"
            else -> anio.toString()
        }
        return "$nombreMes $anioVisible".trim()
    }
}
