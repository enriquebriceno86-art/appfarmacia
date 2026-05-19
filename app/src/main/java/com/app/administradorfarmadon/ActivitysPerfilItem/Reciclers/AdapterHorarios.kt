package com.app.administradorfarmadon.ActivitysPerfilItem.Reciclers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ActivitysPerfilItem.EstadoTipo
import com.app.administradorfarmadon.ActivitysPerfilItem.HorarioExcepcion
import com.app.administradorfarmadon.ActivitysPerfilItem.HorarioRules
import com.app.administradorfarmadon.ActivitysPerfilItem.HorarioTienda
import com.app.administradorfarmadon.R
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdapterHorarios(
    private var listaHorarios: MutableList<HorarioTienda> = mutableListOf(),
    private var listaExcepciones: List<HorarioExcepcion> = emptyList(),
    private val onEditarClick: (HorarioTienda) -> Unit
) : RecyclerView.Adapter<AdapterHorarios.ViewHolder>() {

    fun updateList(newList: List<HorarioTienda>, newExcepciones: List<HorarioExcepcion> = listaExcepciones) {
        listaExcepciones = newExcepciones
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
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val hoyStr = sdf.format(Calendar.getInstance().time)
        val excepcionHoy = listaExcepciones.find { it.fecha == hoyStr }
        
        val estado = HorarioRules.calcularEstadoActual(horarioSemanal = horario, excepcionHoy = excepcionHoy)
        
        val context = holder.itemView.context
        when (estado.tipo) {
            EstadoTipo.ABIERTO -> {
                holder.layoutAbiertoAhora.visibility = View.VISIBLE
                holder.layoutAbiertoAhora.setBackgroundResource(R.drawable.bg_verde_suave_redondo)
                holder.dotStatus.setBackgroundResource(R.drawable.circulo_verde)
                holder.txtStatus.text = estado.mensaje
                holder.txtStatus.setTextColor(context.getColor(R.color.black))
            }
            EstadoTipo.CIERRA_PRONTO -> {
                holder.layoutAbiertoAhora.visibility = View.VISIBLE
                holder.layoutAbiertoAhora.setBackgroundResource(R.drawable.bg_naranja_suave_redondo)
                holder.dotStatus.setBackgroundResource(R.drawable.circulo_naranja)
                holder.txtStatus.text = estado.mensaje
                holder.txtStatus.setTextColor(context.getColor(R.color.black))
            }
            EstadoTipo.PROXIMA_APERTURA -> {
                holder.layoutAbiertoAhora.visibility = View.VISIBLE
                holder.layoutAbiertoAhora.setBackgroundResource(R.drawable.bg_chip_gris_suave)
                holder.dotStatus.setBackgroundResource(R.drawable.bg_status_dot) // Un gris o azul suave
                holder.txtStatus.text = estado.mensaje
                holder.txtStatus.setTextColor(context.getColor(R.color.text_secondary_gray))
            }
            EstadoTipo.CERRADO -> {
                holder.layoutAbiertoAhora.visibility = View.GONE
            }
        }
    }
}
