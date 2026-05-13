package com.app.administradorfarmadon.ActivitysPerfilItem.Reciclers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ActivitysPerfilItem.HorarioTienda
import com.app.administradorfarmadon.R
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdapterHorarios(
    private var listaHorarios: MutableList<HorarioTienda> = mutableListOf(),
    private val onEditarClick: (HorarioTienda) -> Unit
) : RecyclerView.Adapter<AdapterHorarios.ViewHolder>() {

    fun updateList(newList: List<HorarioTienda>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = listaHorarios.size
            override fun getNewListSize(): Int = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return listaHorarios[oldItemPosition].id == newList[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return listaHorarios[oldItemPosition] == newList[newItemPosition]
            }
        })
        listaHorarios.clear()
        listaHorarios.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDia: TextView = itemView.findViewById(R.id.txtDia)
        val txtHoraInicio: TextView = itemView.findViewById(R.id.txtHoraInicio)
        val txtHoraFin: TextView = itemView.findViewById(R.id.txtHoraFin)
        val layoutHoras: LinearLayout = itemView.findViewById(R.id.layoutHoras)
        val layoutAbiertoAhora: View = itemView.findViewById(R.id.layoutAbiertoAhora)
        val dotStatus: View = itemView.findViewById(R.id.dotStatus)
        val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        val txtCerrado: TextView = itemView.findViewById(R.id.txtCerrado)
        val btnEditar: View = itemView.findViewById(R.id.btnEditar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_horario, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listaHorarios.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val horario = listaHorarios[position]

        holder.txtDia.text = horario.dia

        // Lógica de visualización de estados
        when {
            horario.cerrado -> {
                holder.layoutHoras.visibility = View.GONE
                holder.txtCerrado.visibility = View.VISIBLE
                holder.txtCerrado.text = "Cerrado"
            }
            horario.veinticuatroHoras -> {
                holder.layoutHoras.visibility = View.GONE
                holder.txtCerrado.visibility = View.VISIBLE
                holder.txtCerrado.text = "24 horas"
            }
            else -> {
                holder.layoutHoras.visibility = View.VISIBLE
                holder.txtCerrado.visibility = View.GONE
                holder.txtHoraInicio.text = horario.horaApertura
                holder.txtHoraFin.text = horario.horaCierre
            }
        }

        // Indicador de Estado Dinámico (Abierto / Cierra Pronto)
        actualizarIndicadorEstado(holder, horario)

        holder.btnEditar.setOnClickListener {
            onEditarClick(horario)
        }
    }

    private fun actualizarIndicadorEstado(holder: ViewHolder, horario: HorarioTienda) {
        val estado = obtenerEstadoHorario(horario)
        
        if (estado == EstadoTienda.CERRADO) {
            holder.layoutAbiertoAhora.visibility = View.GONE
            return
        }

        holder.layoutAbiertoAhora.visibility = View.VISIBLE
        
        when (estado) {
            EstadoTienda.ABIERTO -> {
                holder.layoutAbiertoAhora.setBackgroundResource(R.drawable.bg_verde_suave_redondo)
                holder.dotStatus.setBackgroundResource(R.drawable.circulo_verde)
                holder.txtStatus.text = "Abierto ahora"
                holder.txtStatus.setTextColor(holder.itemView.context.getColor(R.color.black)) // O un verde oscuro si tienes
            }
            EstadoTienda.CIERRA_PRONTO -> {
                holder.layoutAbiertoAhora.setBackgroundResource(R.drawable.bg_naranja_suave_redondo)
                holder.dotStatus.setBackgroundResource(R.drawable.circulo_naranja)
                holder.txtStatus.text = "Cierra pronto"
                holder.txtStatus.setTextColor(holder.itemView.context.getColor(R.color.black))
            }
            else -> holder.layoutAbiertoAhora.visibility = View.GONE
        }
    }

    enum class EstadoTienda { ABIERTO, CIERRA_PRONTO, CERRADO }

    private fun obtenerEstadoHorario(horario: HorarioTienda): EstadoTienda {
        if (horario.cerrado) return EstadoTienda.CERRADO

        val calendar = Calendar.getInstance()
        val localeEs = Locale("es")
        val diaActual = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, localeEs)
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeEs) else it.toString() } ?: ""
        
        if (horario.dia.lowercase() != diaActual.lowercase()) return EstadoTienda.CERRADO
        if (horario.veinticuatroHoras) return EstadoTienda.ABIERTO

        return try {
            val sdf = SimpleDateFormat("h:mm a", Locale.US)
            val horaActual = sdf.parse(sdf.format(calendar.time))
            val apertura = sdf.parse(horario.horaApertura)
            val cierre = sdf.parse(horario.horaCierre)

            if (horaActual != null && apertura != null && cierre != null) {
                val abierto = if (cierre.before(apertura)) {
                    horaActual.after(apertura) || horaActual.before(cierre)
                } else {
                    horaActual.after(apertura) && horaActual.before(cierre)
                }

                if (abierto) {
                    // Lógica para "Cierra pronto" (menos de 60 min)
                    val diff = cierre.time - horaActual.time
                    val diffMinutes = diff / (1000 * 60)
                    if (diffMinutes in 1..60) EstadoTienda.CIERRA_PRONTO else EstadoTienda.ABIERTO
                } else EstadoTienda.CERRADO
            } else EstadoTienda.CERRADO
        } catch (e: Exception) {
            EstadoTienda.CERRADO
        }
    }
}