package com.app.administradorfarmadon.ActivityFragmentos.Reciclers

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ActivitysCaja.ProductoCaja
import com.app.administradorfarmadon.ActivitysPerfilItem.MetodoPagoConfig
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.ClasesDatabase.PresentacionHelper
import com.app.administradorfarmadon.R
import com.google.android.material.card.MaterialCardView
import java.util.Locale

class AdapterRecyclerProductosCaja(
    private val listaProductosCaja: MutableList<ProductoCaja>,
    private val cacheStockInventario: Map<String, Int> = emptyMap(),
    private val onSumarClick: (ProductoCaja, View) -> Unit,
    private val onRestarClick: (ProductoCaja, View) -> Unit,
    private val onEliminarClick: (ProductoCaja, View) -> Unit,
    private val onProductoClick: (ProductoCaja) -> Unit = {},
    private val onDescuentoClick: (ProductoCaja) -> Unit = {}
) : RecyclerView.Adapter<AdapterRecyclerProductosCaja.ProductoCajaViewHolder>() {

    private val unidadesTotalesPorProducto = mutableMapOf<String, Int>()

    class ProductoCajaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreProducto)
        val tvLoteConsumo: TextView = view.findViewById(R.id.tvLoteConsumoCaja)
        val tvSubtotal: TextView = view.findViewById(R.id.tvSubtotal)
        val tvDetalle: TextView = view.findViewById(R.id.tvDetalleCantidadPrecio)
        val tvCantidad: TextView = view.findViewById(R.id.tvCantidad)

        val tvEstadoStock: TextView? = view.findViewById(R.id.tvEstadoStockCaja)
        val tvDescuentoBadge: TextView? = view.findViewById(R.id.tvDescuentoBadge)
        val tvAccionDescuento: TextView? = view.findViewById(R.id.tvAccionDescuento)

        val btnMas: View = view.findViewById(R.id.btnMas)
        val btnMenos: View = view.findViewById(R.id.btnMenos)
        val btnEliminar: View? = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoCajaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_caja, parent, false)

        return ProductoCajaViewHolder(view)
    }

    override fun getItemCount(): Int = listaProductosCaja.size

    override fun onBindViewHolder(holder: ProductoCajaViewHolder, position: Int) {
        val producto = listaProductosCaja[position]

        // HUMANO: Validación de stock en tiempo real con el Radar
        val claveInventario = producto.indiceProducto.trim()
        val stockRealTotal = cacheStockInventario[claveInventario] ?: producto.stockDisponibleActual.toIntOrNull() ?: 999

        // Calculamos cuántas unidades totales de este mismo producto (en cualquier presentación) hay en el carrito
        val unidadesTotalesEnCarrito = unidadesTotalesPorProducto[claveInventario]
            ?: ((producto.cantidad.toIntOrNull() ?: 0) * (producto.unidadesPorPresentacion.toIntOrNull() ?: 1))

        (producto.cantidad.toIntOrNull() ?: 0) * (producto.unidadesPorPresentacion.toIntOrNull() ?: 1)

        // Si el total en el carrito supera el stock real, este ítem es inválido
        val esInvalidoPorStock = unidadesTotalesEnCarrito > stockRealTotal

        val nombre = producto.nombre?.trim().orEmpty().ifEmpty { "Producto" }
        val presentacion = PresentacionHelper.formatearPresentacionGuardadaParaUi(producto.presentacion)

        val cantidad = producto.cantidad.toIntOrNull() ?: 0
        val precio = producto.precioUnitario.toDoubleOrNull() ?: 0.0
        val total = producto.total.toDoubleOrNull() ?: (cantidad * precio)

        // Nombre + presentación inline ("queso · Caja 1000 g")
        if (presentacion.isNotBlank()) {
            val separador = " · "
            val textoCompleto = "$nombre$separador$presentacion"
            val spannable = SpannableString(textoCompleto)
            val inicioPresent = nombre.length + separador.length
            spannable.setSpan(RelativeSizeSpan(0.85f), inicioPresent, textoCompleto.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(ForegroundColorSpan(Color.parseColor("#6B7280")), inicioPresent, textoCompleto.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            holder.tvNombre.text = spannable
        } else {
            holder.tvNombre.text = nombre
        }
        holder.tvCantidad.text = cantidad.toString().padStart(2, '0')
        holder.tvDetalle.text = String.format(Locale.US, "%.2f", precio)
        holder.tvSubtotal.text = String.format(Locale.US, "%.2f", total)

        val loteActual = producto.loteNumero.trim()
        val loteFefo = producto.loteNumeroFefo.trim()
        holder.tvLoteConsumo.visibility = if (loteActual.isNotBlank() || loteFefo.isNotBlank()) {
            View.VISIBLE
        } else {
            View.GONE
        }
        holder.tvLoteConsumo.text = when {
            loteActual.isNotBlank() ->
                "Lote: $loteActual"
            loteFefo.isNotBlank() ->
                "Lote: $loteFefo"
            else -> ""
        }

        holder.tvLoteConsumo.text = construirTextoLoteConsumo(producto, loteActual, loteFefo)

        if (esInvalidoPorStock || producto.agotado) {
            holder.tvEstadoStock?.visibility = View.VISIBLE
            val mensaje = if (esInvalidoPorStock) "⚠️ Stock Insuficiente (Máx: $stockRealTotal unidades)" else producto.mensajeStockActual
            holder.tvEstadoStock?.text = "$mensaje · Toca para corregir"
            holder.tvEstadoStock?.setTextColor(Color.parseColor("#D32F2F")) // Rojo de alerta

            holder.tvNombre.alpha = 0.65f
            holder.btnMas.alpha = 0.35f
            holder.btnMas.isEnabled = false
            holder.itemView.setBackgroundColor(Color.parseColor("#FFEBEE")) // Fondo rojizo suave
        } else {
            holder.tvEstadoStock?.visibility = View.GONE
            holder.tvNombre.alpha = 1f
            holder.btnMas.alpha = 1f
            holder.btnMas.isEnabled = true
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }

        // Descuento badge
        if (producto.descuento > 0.0) {
            holder.tvDescuentoBadge?.visibility = View.VISIBLE
            val badgeText = if (producto.tipoDescuento == "pct") {
                "−${producto.descuento.toInt()}%"
            } else {
                "−${MonedaHelper.formatear(producto.descuento)}"
            }
            holder.tvDescuentoBadge?.text = badgeText
            holder.tvDescuentoBadge?.text = if (producto.tipoDescuento == "pct") {
                "Desc. ${producto.descuento.toInt()}%"
            } else {
                "Desc. -${MonedaHelper.formatear(producto.descuento)}"
            }
            holder.tvAccionDescuento?.text = "Editar desc."
            holder.tvAccionDescuento?.setTextColor(Color.parseColor("#15803D"))
        } else {
            holder.tvDescuentoBadge?.visibility = View.GONE
            holder.tvAccionDescuento?.text = "Aplicar desc."
            holder.tvAccionDescuento?.setTextColor(Color.parseColor("#4B5563"))
        }

        // Trash superior oculto: el borrado se hace por gesto (swipe) o desde el boton menos cuando cantidad=1.
        holder.btnEliminar?.visibility = View.GONE

        // El boton menos muestra siempre el icono "-" en color neutro (sin rojo, sin papelera).
        // Cuando la cantidad es 1, el tap igual elimina el producto del carrito.
        val esUltimaUnidad = cantidad <= 1

        holder.btnMenos.setOnClickListener { view ->
            if (esUltimaUnidad) onEliminarClick(producto, view) else onRestarClick(producto, view)
        }

        holder.btnMas.setOnClickListener { view -> onSumarClick(producto, view) }
        // Tap en la card del item desactivado: el cajero se equivocaba con sumar/restar y
        // se abria el bottom sheet de presentaciones. Sin listener no hay ripple visual.
        holder.itemView.setOnClickListener(null)
        holder.itemView.isClickable = false
        holder.itemView.foreground = null
        holder.tvSubtotal.setOnClickListener { onDescuentoClick(producto) }
        holder.tvAccionDescuento?.setOnClickListener { onDescuentoClick(producto) }
        holder.tvDescuentoBadge?.setOnClickListener { onDescuentoClick(producto) }
    }

    fun actualizarUnidadesTotalesPorProducto(nuevasUnidades: Map<String, Int>) {
        unidadesTotalesPorProducto.clear()
        unidadesTotalesPorProducto.putAll(nuevasUnidades)
    }

    private fun construirTextoLoteConsumo(
        producto: ProductoCaja,
        loteActual: String,
        loteFefo: String
    ): String {
        return when {
            loteActual.isNotBlank() -> "Lote: $loteActual"
            loteFefo.isNotBlank() -> "Lote: $loteFefo"
            else -> ""
        }
    }
}

data class ItemPagoMixtoUi(
    val metodo: MetodoPagoConfig,
    var seleccionado: Boolean = false,
    var monto: String = ""
)

data class CategoriaProductos(
    var id: String = "",
    var indice: String = "",
    var nombre: String = ""
)
