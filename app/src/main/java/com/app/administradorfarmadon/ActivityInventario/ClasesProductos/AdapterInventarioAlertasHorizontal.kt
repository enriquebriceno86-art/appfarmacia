package com.app.administradorfarmadon.ActivityInventario.ClasesProductos

import android.content.Intent
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ActivityInventario.EditarProductodelInventario
import com.app.administradorfarmadon.ActivityInventario.ProductAvatarHelper
import com.app.administradorfarmadon.ActivityInventario.ProductUtils
import com.app.administradorfarmadon.ClasesDatabase.UnidadBaseHelper
import com.app.administradorfarmadon.R
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import java.util.Locale

/**
 * Adapter para el carrusel horizontal de productos en alerta (stock bajo,
 * vencidos o por vencer). Cada tarjeta ocupa ~86% del ancho de pantalla
 * para que la siguiente "asome" y el usuario sepa que puede hacer scroll.
 */
class AdapterInventarioAlertasHorizontal(
    private var listaAlertas: MutableList<MoldeProductos> = mutableListOf(),
    private val onVerLotesClick: ((MoldeProductos) -> Unit)? = null,
    private val onIngresarStockClick: ((MoldeProductos) -> Unit)? = null
) : RecyclerView.Adapter<AdapterInventarioAlertasHorizontal.AlertaViewHolder>() {

    fun updateList(nueva: List<MoldeProductos>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = listaAlertas.size
            override fun getNewListSize() = nueva.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) =
                listaAlertas[oldPos].indice == nueva[newPos].indice
            override fun areContentsTheSame(oldPos: Int, newPos: Int) =
                listaAlertas[oldPos] == nueva[newPos]
        })
        listaAlertas.clear()
        listaAlertas.addAll(nueva)
        diff.dispatchUpdatesTo(this)
    }

    class AlertaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreAlerta)
        val tvCategoria: TextView = view.findViewById(R.id.tvCategoriaAlerta)
        val tvBadge: TextView = view.findViewById(R.id.tvBadgeAlerta)
        val tvStock: TextView = view.findViewById(R.id.tvStockAlerta)
        val tvVence: TextView = view.findViewById(R.id.tvVenceAlerta)
        val tvMinimo: TextView = view.findViewById(R.id.tvMinimoAlerta)
        val ivProducto: ShapeableImageView = view.findViewById(R.id.ivProductoAlerta)
        val btnVerLotes: MaterialButton = view.findViewById(R.id.btnVerLotesAlerta)
        val btnIngresarStock: MaterialButton = view.findViewById(R.id.btnIngresarStockAlerta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventario_alerta_card, parent, false)
        // Cada tarjeta ocupa ~86% del ancho de pantalla — la siguiente "asoma"
        val anchoPantalla = Resources.getSystem().displayMetrics.widthPixels
        view.layoutParams.width = (anchoPantalla * 0.86f).toInt()
        return AlertaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertaViewHolder, position: Int) {
        val producto = listaAlertas[position]
        val context = holder.itemView.context

        val cantidadActual = producto.cantidadinicial.toIntOrNull() ?: 0
        val stockMinimo = producto.stockminimo.toIntOrNull() ?: 0
        val categoria = producto.categoria.ifBlank { "Sin categoría" }
        val presentacion = producto.presentacionprincipal.ifBlank { "" }
        val fechaGeneralVisible = ProductUtils.obtenerVencimientoGeneralVisible(producto)
        val estadoVencimiento = ProductUtils.obtenerEstadoVencimiento(producto).orEmpty()

        // Nombre + subtítulo "Categoría · Presentación"
        holder.tvNombre.text = producto.nombre.ifBlank { "Sin nombre" }
        holder.tvCategoria.text = if (presentacion.isNotBlank()) {
            "$categoria · $presentacion"
        } else {
            categoria
        }

        // Badge: prioridad → Vencido > Por vencer > Stock bajo
        val esBajoStock = stockMinimo > 0 && cantidadActual <= stockMinimo
        val (textoBadge, colorBadgeFondo, colorBadgeTexto) = when {
            estadoVencimiento == "VENCIDO" -> Triple("Vencido", 0xFFFDEAEA.toInt(), 0xFFB42318.toInt())
            estadoVencimiento == "POR_VENCER" -> Triple("Por vencer", 0xFFFDE6B0.toInt(), 0xFFB45309.toInt())
            cantidadActual <= 0 -> Triple("Sin stock", 0xFFFDEAEA.toInt(), 0xFFB42318.toInt())
            esBajoStock -> Triple("Stock bajo", 0xFFFDE6B0.toInt(), 0xFFB45309.toInt())
            else -> Triple("Atención", 0xFFFDE6B0.toInt(), 0xFFB45309.toInt())
        }
        holder.tvBadge.text = textoBadge
        holder.tvBadge.setTextColor(colorBadgeTexto)
        holder.tvBadge.background?.setTint(colorBadgeFondo)

        // Stock visible: prioriza la presentacion principal si existe equivalencia valida
        holder.tvStock.text = ProductUtils.formatearStockVisible(producto)
        holder.tvStock.setTextColor(
            if (cantidadActual <= 0) 0xFFB42318.toInt() else 0xFFB45309.toInt()
        )

        // Fecha de vencimiento (formato MM/AA)
        holder.tvVence.text = formatearFechaCorta(fechaGeneralVisible)
        holder.tvVence.setTextColor(
            when (estadoVencimiento) {
                "VENCIDO" -> 0xFFB42318.toInt()
                "POR_VENCER" -> 0xFFB45309.toInt()
                else -> 0xFF7C5300.toInt()
            }
        )

        // Mínimo
        val unidadMin = resolverUnidadAbreviada(producto.unidadbase, stockMinimo.coerceAtLeast(0))
        holder.tvMinimo.text = "${stockMinimo.coerceAtLeast(0)} $unidadMin"

        // Imagen del producto
        ProductAvatarHelper.loadInto(
            imageView = holder.ivProducto,
            imageUrl = producto.imagenUrl,
            productName = producto.nombre,
            category = producto.categoria
        )

        // Click en la tarjeta → abrir editar producto
        holder.itemView.setOnClickListener {
            val intent = Intent(context, EditarProductodelInventario::class.java)
            intent.putExtra("indice", producto.indice)
            context.startActivity(intent)
        }

        // Botón Ver lotes
        holder.btnVerLotes.setOnClickListener {
            if (onVerLotesClick != null) {
                onVerLotesClick.invoke(producto)
            } else {
                val intent = Intent(context, EditarProductodelInventario::class.java)
                intent.putExtra("indice", producto.indice)
                context.startActivity(intent)
            }
        }

        // Botón Ingresar stock
        holder.btnIngresarStock.setOnClickListener {
            if (onIngresarStockClick != null) {
                onIngresarStockClick.invoke(producto)
            } else {
                val intent = Intent(context, EditarProductodelInventario::class.java)
                intent.putExtra("indice", producto.indice)
                intent.putExtra("auto_ingreso_stock", true)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = listaAlertas.size

    private fun resolverUnidadAbreviada(unidadBaseRaw: String, cantidad: Int): String {
        return when (unidadBaseRaw.trim().lowercase(Locale.getDefault())) {
            "", "unidad", "unidades", "und", "uds" -> "uds"
            "caja", "cajas" -> if (cantidad == 1) "caja" else "cajas"
            "frasco", "frascos" -> if (cantidad == 1) "frasco" else "frascos"
            "blister", "blíster", "blisters", "blísteres" -> "blíst."
            "tableta", "tabletas", "tab", "tabs" -> "tabs"
            "ml" -> "uds"
            "g" -> "uds"
            else -> UnidadBaseHelper.formatear(unidadBaseRaw, maxOf(cantidad, 1))
        }
    }

    private fun formatearFechaCorta(fechaCruda: String): String {
        val normalizada = fechaCruda.replace("_", "/").trim()
        if (normalizada.isBlank()) return "—"
        val partes = normalizada.split("/")
        return if (partes.size >= 2) {
            "${partes[0].padStart(2, '0')}/${partes[1].takeLast(2)}"
        } else {
            normalizada
        }
    }
}
