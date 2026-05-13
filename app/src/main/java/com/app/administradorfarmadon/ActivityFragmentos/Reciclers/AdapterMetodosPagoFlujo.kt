package com.app.administradorfarmadon.ActivityFragmentos.Reciclers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ActivitysPerfilItem.MetodoPagoConfig
import com.app.administradorfarmadon.R
import com.google.android.material.card.MaterialCardView

class AdapterMetodosPagoFlujo(
    private val lista: List<MetodoPagoConfig>,
    private val onClick: (MetodoPagoConfig) -> Unit
) : RecyclerView.Adapter<AdapterMetodosPagoFlujo.MetodoPagoFlujoViewHolder>() {

    class MetodoPagoFlujoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.cardMetodoPagoFlujo)
        val icono: ImageView = itemView.findViewById(R.id.imgMetodoPagoFlujo)
        val titulo: TextView = itemView.findViewById(R.id.tvTituloMetodoPagoFlujo)
        val subtitulo: TextView = itemView.findViewById(R.id.tvSubtituloMetodoPagoFlujo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetodoPagoFlujoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_metodo_pago_flujo, parent, false)
        return MetodoPagoFlujoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MetodoPagoFlujoViewHolder, position: Int) {
        val metodo = lista[position]
        holder.titulo.text = metodo.titulo.ifBlank { metodo.categoria }
        holder.subtitulo.text = construirSubtitulo(metodo)
        holder.icono.setImageResource(iconoParaMetodo(metodo))
        holder.icono.imageTintList = ContextCompat.getColorStateList(holder.itemView.context, R.color.selector_nav_icons)
        holder.card.setOnClickListener { onClick(metodo) }
    }

    override fun getItemCount(): Int = lista.size

    private fun construirSubtitulo(metodo: MetodoPagoConfig): String {
        return when {
            metodo.esMixtoLike() -> "Combina varios métodos para completar el cobro"
            metodo.esTransferenciaLike() -> {
                val cuenta = metodo.numeroCuenta.takeLast(4)
                if (cuenta.isNotBlank()) "Cuenta terminada en $cuenta" else "Transferencia bancaria"
            }
            metodo.esBilleteraLike() -> {
                metodo.aliasBilletera.ifBlank {
                    metodo.telefonoBilletera.ifBlank { "Billetera digital" }
                }
            }
            metodo.esEfectivoLike() -> "Pago inmediato con vuelto automático"
            metodo.descripcion.isNotBlank() -> metodo.descripcion
            else -> "Sin datos adicionales"
        }
    }

    private fun iconoParaMetodo(metodo: MetodoPagoConfig): Int {
        return when {
            metodo.esTransferenciaLike() -> R.drawable.iconopagotransferencia
            metodo.esBilleteraLike() -> R.drawable.outline_barcode_scanner_24
            metodo.esMixtoLike() -> R.drawable.ic_more_vert_24
            else -> R.drawable.iconoefectivo
        }
    }

    private fun MetodoPagoConfig.esEfectivoLike(): Boolean {
        return categoria == "efectivo" || permiteVuelto || solicitaMontoRecibido || calculaVuelto
    }

    private fun MetodoPagoConfig.esTransferenciaLike(): Boolean {
        return categoria == "transferencia_bancaria"
    }

    private fun MetodoPagoConfig.esBilleteraLike(): Boolean {
        return categoria == "billetera_digital"
    }

    private fun MetodoPagoConfig.esMixtoLike(): Boolean {
        return categoria == "mixto"
    }
}
