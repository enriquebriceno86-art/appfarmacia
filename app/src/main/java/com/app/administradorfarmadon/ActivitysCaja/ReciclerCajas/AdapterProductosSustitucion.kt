package com.app.administradorfarmadon.ActivitysCaja.ReciclerCajas

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ProductUtils
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.R
import com.app.administradorfarmadon.databinding.ItemProductoSustitucionBinding
import com.bumptech.glide.Glide

class AdapterProductosSustitucion(
    private val onProductoSeleccionado: (MoldeProductos) -> Unit
) : RecyclerView.Adapter<AdapterProductosSustitucion.ProductoSustitucionViewHolder>() {

    private val productos = mutableListOf<MoldeProductos>()
    private var indiceSeleccionado: String = ""

    class ProductoSustitucionViewHolder(
        val binding: ItemProductoSustitucionBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoSustitucionViewHolder {
        val binding = ItemProductoSustitucionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductoSustitucionViewHolder(binding)
    }

    override fun getItemCount(): Int = productos.size

    override fun onBindViewHolder(holder: ProductoSustitucionViewHolder, position: Int) {
        val producto = productos[position]
        val binding = holder.binding
        val stockVendible = ProductUtils.stockVendibleProducto(producto).toInt()

        binding.tvNombreProductoSustitucion.text = producto.nombre.ifBlank { "Producto sin nombre" }
        binding.tvCategoriaProductoSustitucion.text =
            producto.categoria.ifBlank { "Sin categoría" }
        binding.tvStockProductoSustitucion.text =
            "Stock vendible: $stockVendible ${producto.unidadbase.ifBlank { "unidades" }}"
        binding.tvPrecioProductoSustitucion.text =
            MonedaHelper.formatear(obtenerPrecioPrincipal(producto))

        Glide.with(binding.root)
            .load(producto.imagenUrl)
            .placeholder(R.drawable.iconpastillas)
            .into(binding.imgProductoSustitucion)

        val seleccionado = producto.indice.trim() == indiceSeleccionado
        binding.cardProductoSustitucion.strokeWidth = if (seleccionado) 2 else 1
        binding.cardProductoSustitucion.strokeColor =
            binding.root.context.getColor(
                if (seleccionado) android.R.color.holo_blue_light else android.R.color.transparent
            )
        binding.btnSeleccionarProductoSustitucion.text =
            if (seleccionado) "Seleccionado" else "Seleccionar"

        val clickSeleccion = {
            indiceSeleccionado = producto.indice.trim()
            notifyDataSetChanged()
            onProductoSeleccionado(producto)
        }
        binding.cardProductoSustitucion.setOnClickListener { clickSeleccion() }
        binding.btnSeleccionarProductoSustitucion.setOnClickListener { clickSeleccion() }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun actualizarProductos(
        nuevosProductos: List<MoldeProductos>,
        indiceSeleccionadoActual: String
    ) {
        productos.clear()
        productos.addAll(nuevosProductos)
        indiceSeleccionado = indiceSeleccionadoActual.trim()
        notifyDataSetChanged()
    }

    private fun obtenerPrecioPrincipal(producto: MoldeProductos): Double {
        val principal = producto.presentaciones
            .firstOrNull { it.nombre.equals(producto.presentacionprincipal, ignoreCase = true) }
            ?: producto.presentaciones.firstOrNull()

        return when {
            principal != null -> principal.precioventa
            else -> producto.preciodecompra.toDoubleOrNull() ?: 0.0
        }
    }
}
