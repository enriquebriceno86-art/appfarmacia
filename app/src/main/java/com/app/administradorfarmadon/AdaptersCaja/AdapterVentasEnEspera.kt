package com.app.administradorfarmadon.AdaptersCaja

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.R
import com.google.android.material.card.MaterialCardView

/**
 * Adaptador para mostrar las ventas en espera en la caja.
 * Incluye lógica visual para resaltar ventas antiguas.
 */
class AdapterVentasEnEspera(
    private val items: List<VentaEnEsperaUi>,
    private val minutosAlerta: Int = 10,
    private val minutosCriticos: Int = 20,
    private val onSeleccionar: (VentaEnEsperaUi) -> Unit,
    private val onEliminar: (VentaEnEsperaUi) -> Unit
) : RecyclerView.Adapter<AdapterVentasEnEspera.VentaEnEsperaViewHolder>() {

    class VentaEnEsperaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardVenta: MaterialCardView = view.findViewById(R.id.cardVentaEnEspera)
        val tvTitulo: TextView = view.findViewById(R.id.tvTituloVentaEspera)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoVentaEspera)
        val tvDetalle: TextView = view.findViewById(R.id.tvDetalleVentaEspera)
        val tvMonto: TextView = view.findViewById(R.id.tvMontoVentaEspera)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminarVentaEspera)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentaEnEsperaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_venta_en_espera, parent, false)
        return VentaEnEsperaViewHolder(view)
    }

    override fun onBindViewHolder(holder: VentaEnEsperaViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitulo.text = item.titulo
        
        val horaTexto = item.hora.ifBlank { "--:--" }
        val usuarioTexto = if (item.nombreUsuario.isNotBlank()) " • ${item.nombreUsuario}" else ""
        holder.tvDetalle.text = "${item.cantidadProductos} productos • $horaTexto$usuarioTexto"
        holder.tvMonto.text = MonedaHelper.formatear(item.total)
        
        aplicarEstadoVisual(holder, item.timestamp)
        
        holder.btnEliminar.setOnClickListener { onEliminar(item) }
        holder.cardVenta.setOnClickListener { onSeleccionar(item) }
    }

    private fun aplicarEstadoVisual(holder: VentaEnEsperaViewHolder, timestamp: Long) {
        val minutos = ((System.currentTimeMillis() - timestamp) / 60000L).coerceAtLeast(0L)
        val texto: String
        val fondo: String
        val borde: String
        val colorTexto: String

        when {
            minutos >= minutosCriticos -> {
                texto = "Mucho tiempo"
                fondo = "#FDEAEA"
                borde = "#F2B8B5"
                colorTexto = "#B3261E"
            }
            minutos >= minutosAlerta -> {
                texto = "Pendiente"
                fondo = "#FFF6E5"
                borde = "#F3D19C"
                colorTexto = "#9A6700"
            }
            else -> {
                texto = "Reciente"
                fondo = "#ECFDF3"
                borde = "#A7F3D0"
                colorTexto = "#027A48"
            }
        }

        holder.tvEstado.text = texto
        holder.tvEstado.background = GradientDrawable().apply {
            setColor(Color.parseColor(fondo))
            cornerRadius = 999f
            setStroke(1, Color.parseColor(borde))
        }
        holder.tvEstado.setTextColor(Color.parseColor(colorTexto))
        holder.cardVenta.strokeColor = Color.parseColor(borde)
    }

    override fun getItemCount(): Int = items.size
}
