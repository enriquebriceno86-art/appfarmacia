package com.app.administradorfarmadon.ActivitysUsuarios

import android.graphics.Color
import android.os.Looper
import com.app.administradorfarmadon.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ClasesDatabase.Trabajadores
import com.google.firebase.database.FirebaseDatabase
import java.util.logging.Handler

class AdapterUsuarioRecycler(private val listaOriginal: MutableList<Trabajadores>, private val onItemClick: (Trabajadores) -> Unit) : RecyclerView.Adapter<AdapterUsuarioRecycler.UsuarioViewHolder>() {

    private var listaFiltrada = listaOriginal.toMutableList()

    class UsuarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val nombre = view.findViewById<TextView>(R.id.txtNombre)
        val rol = view.findViewById<TextView>(R.id.txtRol)
        val estado = view.findViewById<TextView>(R.id.txtEstado)
        val txtDocumento = view.findViewById<TextView>(R.id.txtDocumento)
        val txtContrasena = view.findViewById<TextView>(R.id.txtContrasena)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerdisenousuario, parent, false)

        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {

        val usuario = listaFiltrada[position]

        holder.nombre.text = usuario.usuario
        holder.rol.text = "(${usuario.rol})"
        holder.txtContrasena.text="Contrasena: ${usuario.contrasena}"
        holder.txtDocumento.text="Documento: ${usuario.documento}"

        if (usuario.acceso == "true") {
            holder.estado.text = "● Activo"
            holder.estado.setTextColor(Color.parseColor("#2E7D32")) // verde
        }
        else {
            holder.estado.text = "● Inactivo"
            holder.estado.setTextColor(Color.RED) // rojo
        }

        holder.itemView.setOnClickListener {
            onItemClick(usuario)

        }


    }

    override fun getItemCount(): Int {
        return listaFiltrada.size
    }

    fun filtrarBusqueda(texto: String) {

        listaFiltrada = if (texto.isEmpty()) {
            listaOriginal.toMutableList()
        } else {
            listaOriginal.filter {
                it.usuario.lowercase().contains(texto.lowercase())
            }.toMutableList()
        }

        notifyDataSetChanged()
    }



    fun getUsuario(position: Int): Trabajadores {
        return listaFiltrada[position]
    }


}