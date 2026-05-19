package com.app.administradorfarmadon.ActivitysPerfilItem.Reciclers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ActivitysPerfilItem.HorarioExcepcion
import com.app.administradorfarmadon.R

class AdapterExcepciones(
    private var listaExcepciones: MutableList<HorarioExcepcion> = mutableListOf(),
    private val onEliminarClick: (HorarioExcepcion) -> Unit
) : RecyclerView.Adapter<AdapterExcepciones.ViewHolder>() {

    fun updateList(newList: List<HorarioExcepcion>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = listaExcepciones.size
            override fun getNewListSize(): Int = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return listaExcepciones[oldItemPosition].id == newList[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return listaExcepciones[oldItemPosition] == newList[newItemPosition]
            }
        })
        listaExcepciones.clear()
        listaExcepciones.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtMotivo: TextView = itemView.findViewById(R.id.txtMotivo)
        val txtFecha: TextView = itemView.findViewById(R.id.txtFecha)
        val txtHorarioExcepcion: TextView = itemView.findViewById(R.id.txtHorarioExcepcion)
        val btnEliminarExcepcion: View = itemView.findViewById(R.id.btnEliminarExcepcion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_horario_excepcion, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listaExcepciones.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val excepcion = listaExcepciones[position]

        holder.txtMotivo.text = excepcion.motivo.ifBlank { "Excepción de Horario" }
        holder.txtFecha.text = excepcion.fecha
        
        holder.txtHorarioExcepcion.text = when {
            excepcion.cerrado -> "Cerrado"
            excepcion.veinticuatroHoras -> "Atención 24 horas"
            else -> "${excepcion.horaApertura} - ${excepcion.horaCierre}"
        }

        holder.btnEliminarExcepcion.setOnClickListener {
            onEliminarClick(excepcion)
        }
    }
}
