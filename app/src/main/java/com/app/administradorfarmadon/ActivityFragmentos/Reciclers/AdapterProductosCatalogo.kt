package com.app.administradorfarmadon.ActivityFragmentos.Reciclers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.PresentacionProducto
import com.app.administradorfarmadon.ActivityInventario.ProductAvatarHelper
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.R
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import java.util.Locale

class AdapterProductosCatalogo(
    private val onProductoClick: (MoldeProductos, View) -> Unit
) : RecyclerView.Adapter<AdapterProductosCatalogo.ProductoViewHolder>() {

    private val listaProductos = mutableListOf<MoldeProductos>()

    class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardProducto: MaterialCardView = itemView.findViewById(R.id.cardProductoCatalogo)
        val imgProducto: ImageView = itemView.findViewById(R.id.imgProductoCatalogo)
        val tvNombreProducto: TextView = itemView.findViewById(R.id.tvNombreProductoCatalogo)
        val tvPresentacion: TextView = itemView.findViewById(R.id.tvPresentacionProductoCatalogo)
        val tvEstadoProducto: TextView = itemView.findViewById(R.id.tvEstadoProductoCatalogo)
        val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecioProductoCatalogo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_catalogo, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = listaProductos[position]
        val presentacionSeleccionada = obtenerPresentacionParaVenta(producto)
        val precioPrincipal = obtenerPrecioPrincipal(producto, presentacionSeleccionada)
        val stockTotal = producto.cantidadinicial.trim().toIntOrNull() ?: 0
        val productoAgotado = stockTotal <= 0
        val principalDisponible = presentacionSeleccionada?.let { presentacionDisponible(it, stockTotal) } ?: (stockTotal > 0)
        val hayOtraPresentacionDisponible = obtenerPrimeraPresentacionDisponible(producto, stockTotal)?.let {
            presentacionSeleccionada == null || !it.nombre.equals(presentacionSeleccionada.nombre, ignoreCase = true) || it.cantidad != presentacionSeleccionada.cantidad
        } ?: false

        // Nombre + presentación
        val nombreProducto = producto.nombre.ifBlank { "Producto" }


        holder.tvNombreProducto.text = nombreProducto

        if (presentacionSeleccionada != null) {
            val nombre = presentacionSeleccionada.nombre
            val cantidad = presentacionSeleccionada.cantidad
            val unidad = producto.unidadbase

            holder.tvPresentacion.text = "$nombre ($cantidad $unidad)"
        } else {
            holder.tvPresentacion.text = producto.unidadbase
        }
        holder.tvPrecio.text = MonedaHelper.formatearSimple(precioPrincipal)

        // Solo mostramos badge cuando el producto esta agotado o sin la presentacion principal disponible.
        // El estado "Stock disponible" ya no se muestra para no saturar la tarjeta.
        when {
            productoAgotado -> {
                holder.tvEstadoProducto.visibility = View.VISIBLE
                holder.tvEstadoProducto.text = "Producto agotado"
                holder.tvEstadoProducto.setTextColor(android.graphics.Color.parseColor("#C94747"))
                holder.tvEstadoProducto.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_estado_agotado_chip)
            }
            !principalDisponible && hayOtraPresentacionDisponible -> {
                holder.tvEstadoProducto.visibility = View.VISIBLE
                holder.tvEstadoProducto.text = "Disponible en otras presentaciones"
                holder.tvEstadoProducto.setTextColor(android.graphics.Color.parseColor("#B54708"))
                holder.tvEstadoProducto.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_estado_pendiente)
            }
            else -> {
                holder.tvEstadoProducto.visibility = View.GONE
            }
        }
        holder.cardProducto.strokeColor = if (productoAgotado) {
            android.graphics.Color.parseColor("#F2B8B5")
        } else {
            android.graphics.Color.parseColor("#E8EAF0")
        }
        holder.cardProducto.setCardBackgroundColor(
            if (productoAgotado) android.graphics.Color.parseColor("#FFF7F7")
            else android.graphics.Color.WHITE
        )
        holder.itemView.alpha = if (productoAgotado) 0.84f else 1f



        // Imagen
        val imagenUrl = producto.imagenUrl.trim()
        Glide.with(holder.imgProducto).clear(holder.imgProducto)
        ProductAvatarHelper.loadInto(
            imageView = holder.imgProducto,
            imageUrl = imagenUrl,
            productName = producto.nombre,
            category = producto.categoria
        )

        // Click
        holder.itemView.isEnabled = !productoAgotado
        holder.itemView.isClickable = !productoAgotado
        holder.itemView.setOnClickListener(
            if (productoAgotado) null
            else View.OnClickListener { v -> onProductoClick(producto, v) }
        )
    }

    override fun getItemCount(): Int = listaProductos.size

    fun actualizarProductos(nuevosProductos: List<MoldeProductos>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = listaProductos.size

            override fun getNewListSize(): Int = nuevosProductos.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return listaProductos[oldItemPosition].obtenerClaveEstable() ==
                    nuevosProductos[newItemPosition].obtenerClaveEstable()
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return listaProductos[oldItemPosition] == nuevosProductos[newItemPosition]
            }
        })

        listaProductos.clear()
        listaProductos.addAll(nuevosProductos)
        diff.dispatchUpdatesTo(this)
    }

    override fun onViewRecycled(holder: ProductoViewHolder) {
        Glide.with(holder.imgProducto).clear(holder.imgProducto)
        holder.imgProducto.setImageDrawable(null)
        super.onViewRecycled(holder)
    }

    private fun obtenerPresentacionParaVenta(producto: MoldeProductos): PresentacionProducto? {
        if (producto.presentaciones.isEmpty()) return null

        val principal = producto.presentacionprincipal.trim()

        return producto.presentaciones.firstOrNull {
            it.nombre.trim().equals(principal, ignoreCase = true)
        } ?: producto.presentaciones.firstOrNull()
    }

    private fun presentacionDisponible(presentacion: PresentacionProducto, stock: Int): Boolean {
        val unidades = if (presentacion.cantidad <= 0) 1 else presentacion.cantidad
        return stock >= unidades
    }

    private fun obtenerPrimeraPresentacionDisponible(producto: MoldeProductos, stock: Int): PresentacionProducto? {
        if (producto.presentaciones.isEmpty()) {
            return null
        }
        return producto.presentaciones.firstOrNull { presentacionDisponible(it, stock) }
    }

    private fun obtenerPrecioPrincipal(
        producto: MoldeProductos,
        presentacionSeleccionada: PresentacionProducto?
    ): Double {
        if (presentacionSeleccionada != null && presentacionSeleccionada.precioventa > 0.0) {
            return presentacionSeleccionada.precioventa
        }

        return producto.preciodecompra.trim().toDoubleOrNull() ?: 0.0
    }

    private fun MoldeProductos.obtenerClaveEstable(): String {
        return indice.ifBlank {
            codigo.ifBlank {
                "${nombre.trim()}_${categoria.trim()}"
            }
        }
    }
}
