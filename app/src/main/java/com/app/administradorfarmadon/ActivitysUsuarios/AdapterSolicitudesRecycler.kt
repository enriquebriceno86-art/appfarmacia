package com.app.administradorfarmadon.ActivitysUsuarios

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.R

class AdapterSolicitudesRecycler(private val listaSolicitudes: MutableList<SolicitudTrabajador>, private val onAprobar: (SolicitudTrabajador) -> Unit, private val onRechazar: (SolicitudTrabajador) -> Unit
) : RecyclerView.Adapter<AdapterSolicitudesRecycler.SolicitudViewHolder>()  {

    class SolicitudViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNombre = view.findViewById<TextView>(R.id.txtNombreSolicitud)
        val txtRol = view.findViewById<TextView>(R.id.txtRolSolicitud)
        val txtDocumento = view.findViewById<TextView>(R.id.txtDocumentoSolicitud)
        val txtEstado = view.findViewById<TextView>(R.id.txtEstadoSolicitud)

        val cardEstado = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardEstadoSolicitud)

        val btnAprobar = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAprobarSolicitud)
        val btnRechazar = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnRechazarSolicitud)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitudViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemsolicitudesusuario, parent, false)
        return SolicitudViewHolder(view)
    }

    override fun onBindViewHolder(holder: SolicitudViewHolder, position: Int) {
        val solicitud = listaSolicitudes[position]

        holder.txtNombre.text = solicitud.usuario
        holder.txtRol.text = solicitud.rol
        holder.txtDocumento.text = "Documento: ${solicitud.documento}"

        val estado = solicitud.estado.ifEmpty { "Pendiente" }
        holder.txtEstado.text = estado

        holder.btnAprobar.setOnClickListener {
            onAprobar(solicitud)
        }

        holder.btnRechazar.setOnClickListener {
            onRechazar(solicitud)
        }


    }

    override fun getItemCount(): Int = listaSolicitudes.size
}