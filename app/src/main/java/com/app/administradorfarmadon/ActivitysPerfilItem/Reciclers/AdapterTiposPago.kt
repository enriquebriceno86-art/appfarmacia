package com.app.administradorfarmadon.ActivitysPerfilItem.Reciclers

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ActivitysPerfilItem.MetodoPagoConfig
import com.app.administradorfarmadon.R
import com.app.administradorfarmadon.databinding.ItemTipoPagoBinding

class AdapterTiposPago(
    private val lista: MutableList<MetodoPagoConfig>,
    private val onEditarClick: (MetodoPagoConfig) -> Unit,
    private val onEliminarClick: (MetodoPagoConfig) -> Unit
) : RecyclerView.Adapter<AdapterTiposPago.TipoPagoViewHolder>() {

    class TipoPagoViewHolder(val binding: ItemTipoPagoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipoPagoViewHolder {
        val binding = ItemTipoPagoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TipoPagoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TipoPagoViewHolder, position: Int) {
        val item = lista[position]
        val b = holder.binding

        b.textTituloMetodo.text = item.titulo
        b.textCategoriaMetodo.text = obtenerNombreCategoria(item.categoria)
        b.textResumenMetodo.text = construirResumen(item)
        b.imageMetodoIcono.setImageResource(obtenerIconoCategoria(item.categoria))

        if (item.activo) {
            b.textEstadoMetodo.text = "Activo"
            b.textEstadoMetodo.setBackgroundResource(R.drawable.bg_estado_activo)
            b.textEstadoMetodo.setTextColor(Color.parseColor("#1B5E20"))
        } else {
            b.textEstadoMetodo.text = "Inactivo"
            b.textEstadoMetodo.setBackgroundResource(R.drawable.bg_estado_inactivo)
            b.textEstadoMetodo.setTextColor(Color.parseColor("#666666"))
        }

        b.btnEditarMetodo.setOnClickListener {
            onEditarClick(item)
        }

        b.btnEliminarMetodo.setOnClickListener {
            onEliminarClick(item)
        }
    }

    override fun getItemCount(): Int = lista.size

    private fun obtenerNombreCategoria(categoria: String): String {
        return when (categoria) {
            "efectivo" -> "Efectivo"
            "tarjeta" -> "Tarjeta"
            "transferencia_bancaria" -> "Transferencia bancaria"
            "billetera_digital" -> "Billetera digital"
            "mixto" -> "Cobro mixto"
            "otro" -> "Otro"
            else -> categoria
        }
    }

    private fun obtenerIconoCategoria(categoria: String): Int {
        return when (categoria) {
            "efectivo" -> R.drawable.iconoefectivo
            "tarjeta" -> R.drawable.iconotarjetadepago
            "transferencia_bancaria" -> R.drawable.iconopagotransferencia
            "billetera_digital" -> R.drawable.pos
            "mixto" -> R.drawable.ic_receipt
            else -> R.drawable.ic_receipt
        }
    }

    private fun construirResumen(item: MetodoPagoConfig): String {
        return when (item.categoria) {
            "efectivo" -> {
                val partes = mutableListOf<String>()
                if (item.permiteVuelto) partes.add("permite vuelto")
                if (item.solicitaMontoRecibido) partes.add("solicita monto recibido")
                if (item.calculaVuelto) partes.add("calcula vuelto")
                if (partes.isEmpty()) "Configuración básica" else partes.joinToString(", ")
            }

            "tarjeta" -> {
                val partes = mutableListOf<String>()
                if (item.permiteReferencia) partes.add("pide referencia")
                if (item.disponibleMixto) partes.add("disponible en mixto")
                if (partes.isEmpty()) "Pago con tarjeta" else partes.joinToString(", ")
            }

            "transferencia_bancaria" -> {
                val partes = mutableListOf<String>()
                if (item.banco.isNotBlank()) partes.add(item.banco)
                if (item.tipoCuenta.isNotBlank()) partes.add(item.tipoCuenta)
                if (item.usaQR) partes.add("con QR")
                if (partes.isEmpty()) "Transferencia configurada" else partes.joinToString(" · ")
            }

            "billetera_digital" -> {
                val partes = mutableListOf<String>()
                if (item.telefonoBilletera.isNotBlank()) partes.add(item.telefonoBilletera)
                if (item.titularBilletera.isNotBlank()) partes.add(item.titularBilletera)
                if (item.usaQR) partes.add("con QR")
                if (partes.isEmpty()) "Billetera configurada" else partes.joinToString(" · ")
            }

            "mixto" -> "Permite combinar varios métodos de pago"
            "otro" -> if (item.descripcion.isNotBlank()) item.descripcion else "Método personalizado"
            else -> "Método configurado"
        }
    }
}
