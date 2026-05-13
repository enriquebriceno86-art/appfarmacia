package com.app.administradorfarmadon.ActivityFragmentos.Reciclers

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.R
import com.google.android.material.card.MaterialCardView

class AdapterDiasCierreDashboard(
    private val items: MutableList<DiaCierreUi>,
    private val onDiaClick: (DiaCierreUi) -> Unit
) : RecyclerView.Adapter<AdapterDiasCierreDashboard.DiaViewHolder>() {

    data class DiaCierreUi(
        val fechaIso: String,           // yyyy-MM-dd
        val etiqueta: String,           // "Hoy", "Ayer", "lun 21", etc.
        val conteo: Int,
        var seleccionado: Boolean
    )

    class DiaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.cardDiaCierre)
        val tvEtiqueta: TextView = view.findViewById(R.id.tvEtiquetaDiaCierre)
        val tvConteo: TextView = view.findViewById(R.id.tvConteoDiaCierre)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dia_cierre_dashboard, parent, false)
        return DiaViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiaViewHolder, position: Int) {
        val item = items[position]
        holder.tvEtiqueta.text = item.etiqueta
        holder.tvConteo.text = item.conteo.toString()

        if (item.seleccionado) {
            holder.card.setCardBackgroundColor(Color.parseColor("#0AA25D"))
            holder.card.strokeWidth = 0
            holder.tvEtiqueta.setTextColor(Color.WHITE)
            holder.tvConteo.setTextColor(Color.parseColor("#DDF7EA"))
        } else {
            holder.card.setCardBackgroundColor(Color.parseColor("#F2F4F7"))
            holder.card.strokeWidth = 1
            holder.card.strokeColor = Color.parseColor("#E8EAF0")
            holder.tvEtiqueta.setTextColor(Color.parseColor("#5B6680"))
            holder.tvConteo.setTextColor(Color.parseColor("#9AA3B2"))
        }

        holder.card.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            val seleccionado = items[pos]
            if (seleccionado.seleccionado) return@setOnClickListener
            // Marcar nuevo seleccionado y desmarcar resto
            for (i in items.indices) {
                items[i].seleccionado = (i == pos)
            }
            notifyDataSetChanged()
            onDiaClick(seleccionado)
        }
    }

    override fun getItemCount(): Int = items.size
}
