package com.app.administradorfarmadon.ActivitysPedidosTienda.Clases

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.R
import com.app.administradorfarmadon.databinding.ItemDetalleProductoPedidoBinding
import com.bumptech.glide.Glide

class AdapterProductosPedidoDetalle(
    private val lista: MutableList<PedidoProductoDetalle>
) : RecyclerView.Adapter<AdapterProductosPedidoDetalle.ProductoViewHolder>() {

    class ProductoViewHolder(
        private val binding: ItemDetalleProductoPedidoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PedidoProductoDetalle) {
            binding.tvNombreProductoPedidoDetalle.text = "${item.cantidad}x ${item.nombre}"

            val presentacionTexto = item.descripcionPresentacion.ifBlank {
                item.presentacion.takeIf { it.isNotBlank() }?.let { "Presentación: $it" }.orEmpty()
            }
            binding.tvPresentacionProductoPedidoDetalle.isVisible = presentacionTexto.isNotBlank()
            binding.tvPresentacionProductoPedidoDetalle.text = if (
                presentacionTexto.startsWith("Presentación:")
            ) {
                presentacionTexto
            } else {
                "Presentación: $presentacionTexto"
            }

            binding.tvVencimientoProductoPedidoDetalle.isVisible = item.vencimiento.isNotBlank()
            binding.tvVencimientoProductoPedidoDetalle.text = "Vence: ${item.vencimiento}"

            val recetaTexto = when {
                item.recetaUrl.isNotBlank() -> "Receta adjunta"
                item.requiereReceta -> "Requiere receta"
                else -> ""
            }
            binding.tvRecetaProductoPedidoDetalle.isVisible = recetaTexto.isNotBlank()
            binding.tvRecetaProductoPedidoDetalle.text = recetaTexto

            binding.ivRecetaProductoPedidoDetalle.isVisible = item.recetaUrl.isNotBlank()
            if (item.recetaUrl.isNotBlank()) {
                Glide.with(binding.root)
                    .load(item.recetaUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(binding.ivRecetaProductoPedidoDetalle)
            } else {
                Glide.with(binding.root).clear(binding.ivRecetaProductoPedidoDetalle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDetalleProductoPedidoBinding.inflate(inflater, parent, false)
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount(): Int = lista.size

    fun actualizar(nuevaLista: List<PedidoProductoDetalle>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}
