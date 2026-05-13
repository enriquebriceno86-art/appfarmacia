package com.app.administradorfarmadon.ActivityFragmentos.Reciclers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.R
import com.google.android.material.card.MaterialCardView

class AdapterEstadoCajasDashboard(
    private val items: MutableList<EstadoCajaDashboardUi>,
    private val onCajaClick: (EstadoCajaDashboardUi) -> Unit
) : RecyclerView.Adapter<AdapterEstadoCajasDashboard.CajaViewHolder>() {

    enum class Estado { ABIERTA, CERRADA, CON_DIFERENCIA, SIN_ACTIVIDAD }

    data class EstadoCajaDashboardUi(
        val idCaja: String,
        val nombreCaja: String,
        val estado: Estado,
        val tieneTurnoHoy: Boolean
    )

    class CajaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.cardIconoEstadoCaja)
        val img: ImageView = view.findViewById(R.id.imgIconoEstadoCaja)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreEstadoCaja)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoCajaItem)
        val dot: View = view.findViewById(R.id.dotEstadoCaja)
        val root: View = view.findViewById(R.id.rootEstadoCaja)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CajaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_estado_caja_dashboard, parent, false)
        return CajaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CajaViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.tvNombre.text = item.nombreCaja.ifBlank { "Caja sin nombre" }

        val (textoEstado, colorEstado, fondoIcono, dotDrawable) = when (item.estado) {
            Estado.ABIERTA -> Quad(
                "Abierta",
                "#0AA25D",
                "#E6F4EC",
                R.drawable.bg_dot_dashboard_success
            )
            Estado.CERRADA -> Quad(
                "Cerrada",
                "#5B6680",
                "#F2F4F7",
                R.drawable.bg_dot_dashboard_neutral
            )
            Estado.CON_DIFERENCIA -> Quad(
                "Con diferencia",
                "#C78100",
                "#FFF3D6",
                R.drawable.bg_dot_dashboard_warning
            )
            Estado.SIN_ACTIVIDAD -> Quad(
                "Sin actividad",
                "#9AA3B2",
                "#F2F4F7",
                R.drawable.bg_dot_dashboard_neutral
            )
        }

        holder.tvEstado.text = textoEstado
        holder.tvEstado.setTextColor(android.graphics.Color.parseColor(colorEstado))
        holder.dot.background = ContextCompat.getDrawable(context, dotDrawable)

        holder.card.setCardBackgroundColor(android.graphics.Color.parseColor(fondoIcono))
        val iconoTint = when (item.estado) {
            Estado.ABIERTA -> "#0AA25D"
            Estado.CON_DIFERENCIA -> "#C78100"
            else -> "#5B6680"
        }
        holder.img.setColorFilter(android.graphics.Color.parseColor(iconoTint))

        holder.root.setOnClickListener { onCajaClick(item) }
    }

    override fun getItemCount(): Int = items.size

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
