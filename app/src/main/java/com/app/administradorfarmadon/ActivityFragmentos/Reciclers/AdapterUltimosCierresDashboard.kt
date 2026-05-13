package com.app.administradorfarmadon.ActivityFragmentos.Reciclers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.R

class AdapterUltimosCierresDashboard(
    private val items: MutableList<UltimoCierreDashboardUi>,
    private val onCierreClick: (UltimoCierreDashboardUi) -> Unit
) : RecyclerView.Adapter<AdapterUltimosCierresDashboard.CierreViewHolder>() {

    data class UltimoCierreDashboardUi(
        val idCaja: String,
        val idTurno: String,
        val fecha: String,
        val nombreCaja: String,
        val horaCierre: String,
        val diferenciaTexto: String,
        val diferenciaColorHex: String
    )

    class CierreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreCajaCierreItem)
        val tvHora: TextView = view.findViewById(R.id.tvHoraCierreItem)
        val tvDiferencia: TextView = view.findViewById(R.id.tvDiferenciaCierreItem)
        val root: View = view.findViewById(R.id.rootUltimoCierre)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CierreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ultimo_cierre_dashboard, parent, false)
        return CierreViewHolder(view)
    }

    override fun onBindViewHolder(holder: CierreViewHolder, position: Int) {
        val item = items[position]
        holder.tvNombre.text = item.nombreCaja.ifBlank { "Caja sin nombre" }
        holder.tvHora.text = item.horaCierre.ifBlank { "--:--" }
        holder.tvDiferencia.text = item.diferenciaTexto
        holder.tvDiferencia.setTextColor(android.graphics.Color.parseColor(item.diferenciaColorHex))
        holder.root.setOnClickListener { onCierreClick(item) }
    }

    override fun getItemCount(): Int = items.size
}
