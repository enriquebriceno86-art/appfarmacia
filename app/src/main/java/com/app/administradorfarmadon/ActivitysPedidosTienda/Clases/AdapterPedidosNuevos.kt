package com.app.administradorfarmadon.ActivitysPedidosTienda.Clases

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.R
import com.google.android.material.button.MaterialButton

class AdapterPedidosNuevos(
    private val lista: ArrayList<PedidoInformacion>,
    private val onClickItem: (PedidoInformacion) -> Unit
) : RecyclerView.Adapter<AdapterPedidosNuevos.PedidoViewHolder>() {

    class PedidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardPedido: com.google.android.material.card.MaterialCardView =
            itemView.findViewById(R.id.cardPedido)
        val tvNombreCliente: TextView = itemView.findViewById(R.id.tvNombreCliente)
        val tvDireccion: TextView = itemView.findViewById(R.id.tvDireccion)
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotal)
        val tvFormaEntrega: TextView = itemView.findViewById(R.id.tvFormaEntrega)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pedido_nuevo, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = lista[position]

        holder.tvNombreCliente.text = pedido.nombreCliente
        holder.tvDireccion.text = pedido.direccion
        holder.tvEstado.text = pedido.estado.replaceFirstChar { it.uppercase() }
        holder.tvTotal.text = pedido.total
        holder.tvFormaEntrega.text = pedido.formaEntrega

        when (pedido.estado.trim().lowercase()) {
            "pendiente" -> {
                holder.tvEstado.setTextColor(Color.parseColor("#F57C00"))
                holder.tvEstado.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#FFF3E0"))
            }
            "aceptado", "preparando" -> {
                holder.tvEstado.setTextColor(Color.parseColor("#2E7D32"))
                holder.tvEstado.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#E8F5E9"))
            }
            "encamino", "en camino" -> {
                holder.tvEstado.setTextColor(Color.parseColor("#1565C0"))
                holder.tvEstado.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#E3F2FD"))
            }
            "entregado", "completado" -> {
                holder.tvEstado.setTextColor(Color.parseColor("#6D4C41"))
                holder.tvEstado.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#EFEBE9"))
            }
        }

        holder.cardPedido.setOnClickListener {
            onClickItem(pedido)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<PedidoInformacion>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}