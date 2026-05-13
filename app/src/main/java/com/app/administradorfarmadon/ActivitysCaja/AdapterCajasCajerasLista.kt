package com.app.administradorfarmadon.ActivitysCaja

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.R

class AdapterCajasCajerasLista(
    private val items: MutableList<CajaCajeraResumen>,
    private val onCajaClick: (CajaCajeraResumen) -> Unit
) : RecyclerView.Adapter<AdapterCajasCajerasLista.CajaViewHolder>() {

    enum class TipoEstado { ABIERTA, CERRADA, CON_DIFERENCIA, INACTIVA_HOY }

    data class CajaCajeraResumen(
        val idCajera: String,
        val nombreCajera: String,
        val estado: TipoEstado,
        val totalDia: Double,
        val cantidadVentas: Int,
        val tuvoTurnoHoy: Boolean
    )

    class CajaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: View = view.findViewById(R.id.cardItemCajaCajera)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreItemCajera)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoItemCajera)
        val dotEstado: View = view.findViewById(R.id.dotEstadoItemCajera)
        val tvVentas: TextView = view.findViewById(R.id.tvVentasItemCajera)
        val tvTotal: TextView = view.findViewById(R.id.tvTotalItemCajera)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CajaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_caja_cajera_lista, parent, false)
        return CajaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CajaViewHolder, position: Int) {
        val item = items[position]
        val ctx = holder.itemView.context

        holder.tvNombre.text = item.nombreCajera.ifBlank { "Cajera sin nombre" }

        val (textoEstado, colorEstado, dotDrawable) = when (item.estado) {
            TipoEstado.ABIERTA -> Triple(
                "Abierta",
                "#0AA25D",
                R.drawable.bg_dot_dashboard_success
            )
            TipoEstado.CERRADA -> Triple(
                "Cerrada",
                "#5B6680",
                R.drawable.bg_dot_dashboard_neutral
            )
            TipoEstado.CON_DIFERENCIA -> Triple(
                "Con diferencia",
                "#C78100",
                R.drawable.bg_dot_dashboard_warning
            )
            TipoEstado.INACTIVA_HOY -> Triple(
                "Inactiva hoy",
                "#5B6680",
                R.drawable.bg_dot_dashboard_neutral
            )
        }
        holder.tvEstado.text = textoEstado
        holder.tvEstado.setTextColor(Color.parseColor(colorEstado))
        holder.dotEstado.background = ContextCompat.getDrawable(ctx, dotDrawable)

        holder.tvVentas.text = when (item.cantidadVentas) {
            0 -> "Sin ventas hoy"
            1 -> "1 venta hoy"
            else -> "${item.cantidadVentas} ventas hoy"
        }
        holder.tvTotal.text = MonedaHelper.formatear(item.totalDia)

        holder.card.setOnClickListener { onCajaClick(item) }
    }

    override fun getItemCount(): Int = items.size
}
