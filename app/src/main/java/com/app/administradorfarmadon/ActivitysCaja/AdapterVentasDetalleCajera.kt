package com.app.administradorfarmadon.ActivitysCaja

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.R

class AdapterVentasDetalleCajera(
    private val items: MutableList<VentaResumenUi>,
    private val onVentaClick: (VentaResumenUi) -> Unit
) : RecyclerView.Adapter<AdapterVentasDetalleCajera.VentaViewHolder>() {

    data class VentaResumenUi(
        val idVenta: String,
        val fecha: String,             // yyyy-MM-dd
        val hora: String,              // texto crudo (ej: "3:20 p. m.")
        val total: Double,
        val cantidadProductos: Int,
        val metodoPago: String,
        val tieneDevolucion: Boolean
    )

    class VentaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: View = view.findViewById(R.id.cardItemVentaDetalle)
        val tvHora: TextView = view.findViewById(R.id.tvHoraVentaItem)
        val tvId: TextView = view.findViewById(R.id.tvIdVentaItem)
        val tvDetalle: TextView = view.findViewById(R.id.tvDetalleVentaItem)
        val tvTotal: TextView = view.findViewById(R.id.tvTotalVentaItem)
        val tvBadgeDev: TextView = view.findViewById(R.id.tvBadgeDevolucionItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_venta_detalle_cajera, parent, false)
        return VentaViewHolder(view)
    }

    override fun onBindViewHolder(holder: VentaViewHolder, position: Int) {
        val item = items[position]
        holder.tvHora.text = item.hora.ifBlank { "--:--" }
        holder.tvId.text = "ID #${idCorto(item.idVenta)}"

        val partes = mutableListOf<String>()
        partes += "${item.cantidadProductos} ${if (item.cantidadProductos == 1) "producto" else "productos"}"
        if (item.metodoPago.isNotBlank()) partes += metodoCorto(item.metodoPago)
        holder.tvDetalle.text = partes.joinToString(" · ")

        holder.tvTotal.text = MonedaHelper.formatear(item.total)
        holder.tvBadgeDev.visibility = if (item.tieneDevolucion) View.VISIBLE else View.GONE

        holder.card.setOnClickListener { onVentaClick(item) }
    }

    override fun getItemCount(): Int = items.size

    private fun idCorto(id: String): String {
        if (id.length <= 8) return id
        return id.takeLast(8)
    }

    private fun metodoCorto(texto: String): String {
        val t = texto.trim().lowercase()
        return when {
            t.contains("efectivo") -> "Efectivo"
            t.contains("tarjeta") || t.contains("debito") || t.contains("credito") -> "Tarjeta"
            t.contains("transfer") -> "Transferencia"
            t.contains("billet") || t.contains("yape") || t.contains("plin") -> "Billetera"
            t.contains("mixto") -> "Mixto"
            t.isNotBlank() -> texto.replaceFirstChar { it.uppercaseChar() }
            else -> ""
        }
    }
}
