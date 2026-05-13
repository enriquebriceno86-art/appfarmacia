package com.app.administradorfarmadon.ActivitysInicio.reciclermovimientos

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ClasesDatabase.FechaHoraServidorHelper
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.R
import com.google.android.material.card.MaterialCardView
import java.util.Locale

class AdapterMovimientosAnalisis(
    private val onClickMovimiento: (MovimientoAnalisisItem) -> Unit = {}
) : RecyclerView.Adapter<AdapterMovimientosAnalisis.MovimientoViewHolder>() {

    private val items = mutableListOf<MovimientoAnalisisItem>()

    class MovimientoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val lineaSuperior: View = view.findViewById(R.id.viewLineaSuperior)
        val lineaInferior: View = view.findViewById(R.id.viewLineaInferior)
        val punto: View = view.findViewById(R.id.viewPunto)
        val cardMovimiento: MaterialCardView = view.findViewById(R.id.cardMovimiento)
        val containerIcono: FrameLayout = view.findViewById(R.id.containerIcono)
        val ivTipoMovimiento: ImageView = view.findViewById(R.id.ivTipoMovimiento)
        val tvMontoMovimiento: TextView = view.findViewById(R.id.tvMontoMovimiento)
        val tvTituloMovimiento: TextView = view.findViewById(R.id.tvTituloMovimiento)
        val tvHoraMovimiento: TextView = view.findViewById(R.id.tvHoraMovimiento)
        val tvFechaMovimiento: TextView = view.findViewById(R.id.tvFechaMovimiento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovimientoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movimiento, parent, false)
        return MovimientoViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MovimientoViewHolder, position: Int) {
        val item = items[position]
        val estilo = resolverEstilo(item)

        holder.tvTituloMovimiento.text = item.titulo.ifBlank { "Movimiento registrado" }

        val moduloLegible = moduloLegible(item.modulo)
        val tipoLegible = tipoLegible(item.tipo)
        holder.tvHoraMovimiento.text = "${item.hora} | $moduloLegible | $tipoLegible"

        holder.tvFechaMovimiento.text = construirTextoFecha(item)

        if (item.monto != null) {
            holder.tvMontoMovimiento.visibility = View.VISIBLE
            holder.tvMontoMovimiento.text = MonedaHelper.formatear(item.monto)
        } else {
            holder.tvMontoMovimiento.visibility = View.GONE
        }

        holder.ivTipoMovimiento.setImageResource(estilo.iconoRes)
        holder.ivTipoMovimiento.imageTintList = ColorStateList.valueOf(Color.WHITE)

        ViewCompat.setBackgroundTintList(holder.containerIcono, ColorStateList.valueOf(estilo.colorPrincipal))
        ViewCompat.setBackgroundTintList(holder.punto, ColorStateList.valueOf(estilo.colorPrincipal))
        holder.cardMovimiento.strokeColor = estilo.colorBordeCard

        holder.lineaSuperior.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
        holder.lineaInferior.visibility = if (position == itemCount - 1) View.INVISIBLE else View.VISIBLE

        holder.itemView.setOnClickListener {
            onClickMovimiento(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun actualizarLista(nuevaLista: List<MovimientoAnalisisItem>) {
        items.clear()
        items.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    private fun construirTextoFecha(item: MovimientoAnalisisItem): String {
        val fechaFormateada = formatearFecha(item.fecha, item.timestamp)
        val autor = item.autor.ifBlank { "sin autor" }
        return "$fechaFormateada | $autor"
    }

    private fun moduloLegible(modulo: String): String {
        return when (modulo.lowercase(Locale.getDefault())) {
            "configuracion_tienda" -> "Config. tienda"
            "inventario" -> "Inventario"
            "caja" -> "Caja"
            else -> "General"
        }
    }

    private fun tipoLegible(tipo: String): String {
        val tipoNormalizado = tipo.lowercase(Locale.getDefault())
        return when {
            tipoNormalizado.startsWith("metodo_pago") -> "Metodo de pago"
            tipoNormalizado.contains("venta") -> "Venta"
            tipoNormalizado.contains("egreso") -> "Egreso"
            tipoNormalizado.contains("turno") -> "Turno"
            tipoNormalizado.contains("inventario") -> "Inventario"
            tipoNormalizado.isBlank() -> "General"
            else -> tipo
                .replace("_", " ")
                .replaceFirstChar { it.uppercase() }
        }
    }

    private fun formatearFecha(fecha: String, timestamp: Long): String {
        if (fecha.isNotBlank()) {
            return FechaHoraServidorHelper.formatearFechaVisibleDesdeFirebase(fecha)
        }

        if (timestamp > 0L) {
            return FechaHoraServidorHelper.formatearFechaVisible(timestamp)
        }

        return "--"
    }

    private fun resolverEstilo(item: MovimientoAnalisisItem): EstiloMovimiento {
        return when {
            item.modulo.equals("caja", ignoreCase = true) -> EstiloMovimiento(
                iconoRes = R.drawable.iconventascajashistorial,
                colorPrincipal = Color.parseColor("#2563EB"),
                colorBordeCard = Color.parseColor("#DCE8FF")
            )

            item.modulo.equals("inventario", ignoreCase = true) -> EstiloMovimiento(
                iconoRes = R.drawable.inventory_2icon,
                colorPrincipal = Color.parseColor("#7C3AED"),
                colorBordeCard = Color.parseColor("#E9DDFE")
            )

            item.modulo.equals("configuracion_tienda", ignoreCase = true) -> EstiloMovimiento(
                iconoRes = R.drawable.configuracionicon,
                colorPrincipal = Color.parseColor("#0F766E"),
                colorBordeCard = Color.parseColor("#D6F2EE")
            )

            item.tipo.contains("egreso", ignoreCase = true) -> EstiloMovimiento(
                iconoRes = R.drawable.iconoefectivo,
                colorPrincipal = Color.parseColor("#B45309"),
                colorBordeCard = Color.parseColor("#FDEBD2")
            )

            else -> EstiloMovimiento(
                iconoRes = R.drawable.baseline_access_time_24,
                colorPrincipal = Color.parseColor("#4B5563"),
                colorBordeCard = Color.parseColor("#E8EAF1")
            )
        }
    }

    private data class EstiloMovimiento(
        val iconoRes: Int,
        val colorPrincipal: Int,
        val colorBordeCard: Int
    )
}
