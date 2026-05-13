package com.app.administradorfarmadon.AdaptersCaja

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.R
import kotlin.math.abs

/**
 * Adaptador para mostrar el resumen de métodos de pago en el diálogo de cierre de caja.
 */

class AdapterResumenPagoCierre(
    private val items: List<ResumenPagoTurnoItem>,
    private val totalVentas: Double
) : RecyclerView.Adapter<AdapterResumenPagoCierre.ResumenPagoViewHolder>() {

    class ResumenPagoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMetodoNombre: TextView = view.findViewById(R.id.tvMetodoNombre)
        val tvMetodoMonto: TextView = view.findViewById(R.id.tvMetodoMonto)
        val tvPorcentaje: TextView = view.findViewById(R.id.tvPorcentajeMetodo)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBarMetodo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResumenPagoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resumen_pago_cierre, parent, false)
        return ResumenPagoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResumenPagoViewHolder, position: Int) {
        val item = items[position]
        holder.tvMetodoNombre.text = item.titulo

        val esEgreso = item.monto < 0
        if (esEgreso) {
            holder.tvMetodoMonto.text = "-${MonedaHelper.formatearSimple(abs(item.monto))}"
            holder.tvMetodoMonto.setTextColor(Color.parseColor("#D93025"))
            holder.tvPorcentaje.visibility = View.GONE
            holder.progressBar.visibility = View.GONE
        } else {
            holder.tvMetodoMonto.text = MonedaHelper.formatearSimple(item.monto)
            holder.tvMetodoMonto.setTextColor(Color.parseColor("#111111"))
            holder.tvPorcentaje.visibility = View.VISIBLE
            holder.progressBar.visibility = View.VISIBLE

            val porcentaje = if (totalVentas > 0.0) {
                ((item.monto / totalVentas) * 100).toInt()
            } else 0
            holder.tvPorcentaje.text = "$porcentaje%"
            holder.progressBar.progress = porcentaje
        }
    }

    override fun getItemCount(): Int = items.size
}
