package com.app.administradorfarmadon.ActivityFragmentos.Reciclers

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.R
import com.google.android.material.card.MaterialCardView

class AdapterCategorias(
    private var listaCategorias: List<String>,
    private val onCategoriaClick: (String) -> Unit
) : RecyclerView.Adapter<AdapterCategorias.CategoriaViewHolder>() {

    // -1 = ninguna seleccionada (default = mostrar todo)
    private var posicionSeleccionada = -1

    class CategoriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardCategoria: MaterialCardView = itemView.findViewById(R.id.cardCategoria)
        val tvNombreCategoria: TextView = itemView.findViewById(R.id.tvNombreCategoria)
        val tvCantidadCategoria: TextView = itemView.findViewById(R.id.tvCantidadCategoria)
        val imgQuitarCategoria: ImageView? = itemView.findViewById(R.id.imgQuitarCategoria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_categoria_pos, parent, false)
        return CategoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        val categoria = listaCategorias[position]
        val seleccionado = position == posicionSeleccionada

        holder.tvNombreCategoria.text = categoria
        holder.tvCantidadCategoria.visibility = View.GONE

        if (seleccionado) {
            holder.cardCategoria.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.verdebotones)
            )
            holder.cardCategoria.strokeWidth = 0
            holder.tvNombreCategoria.setTextColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.white)
            )
            holder.imgQuitarCategoria?.visibility = View.VISIBLE
            holder.imgQuitarCategoria?.setColorFilter(Color.WHITE)
        } else {
            holder.cardCategoria.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.white)
            )
            holder.cardCategoria.strokeWidth = 1
            holder.cardCategoria.strokeColor = Color.parseColor("#E8E8E8")
            holder.tvNombreCategoria.setTextColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.black)
            )
            holder.imgQuitarCategoria?.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val nuevaPosicion = holder.adapterPosition
            if (nuevaPosicion == RecyclerView.NO_POSITION) return@setOnClickListener

            val anterior = posicionSeleccionada
            if (nuevaPosicion == anterior) {
                // Click en el chip ya activo: deseleccionar (volver al default = todas).
                posicionSeleccionada = -1
                notifyItemChanged(anterior)
                onCategoriaClick("")
            } else {
                posicionSeleccionada = nuevaPosicion
                if (anterior != -1) notifyItemChanged(anterior)
                notifyItemChanged(posicionSeleccionada)
                onCategoriaClick(categoria)
            }
        }
    }

    override fun getItemCount(): Int = listaCategorias.size

    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nuevaLista: List<String>) {
        this.listaCategorias = nuevaLista
        this.posicionSeleccionada = -1
        notifyDataSetChanged()
    }
}
