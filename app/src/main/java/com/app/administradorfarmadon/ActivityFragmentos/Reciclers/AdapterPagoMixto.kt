package com.app.administradorfarmadon.ActivityFragmentos.Reciclers

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AdapterPagoMixto(
    private val lista: MutableList<ItemPagoMixtoUi>,
    private val onCambio: () -> Unit
) : RecyclerView.Adapter<AdapterPagoMixto.PagoMixtoViewHolder>() {

    class PagoMixtoViewHolder(
        val root: View,
        val card: MaterialCardView,
        val check: CheckBox,
        val titulo: TextView,
        val ayuda: TextView,
        val layoutMonto: TextInputLayout,
        val monto: TextInputEditText
    ) : RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagoMixtoViewHolder {
        val root = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pago_mixto, parent, false)

        return PagoMixtoViewHolder(
            root = root,
            card = root.findViewById(R.id.cardPagoMixtoItem),
            check = root.findViewById(R.id.checkPagoMixto),
            titulo = root.findViewById(R.id.tvTituloPagoMixto),
            ayuda = root.findViewById(R.id.tvAyudaPagoMixto),
            layoutMonto = root.findViewById(R.id.layoutMontoPagoMixto),
            monto = root.findViewById(R.id.edtMontoPagoMixto)
        )
    }

    override fun onBindViewHolder(holder: PagoMixtoViewHolder, position: Int) {
        val item = lista[position]

        holder.titulo.text = item.metodo.titulo.ifBlank { item.metodo.categoria }
        holder.check.setOnCheckedChangeListener(null)

        val oldWatcher = holder.monto.tag as? TextWatcher
        if (oldWatcher != null) {
            holder.monto.removeTextChangedListener(oldWatcher)
        }

        holder.check.isChecked = item.seleccionado
        holder.layoutMonto.visibility = if (item.seleccionado) View.VISIBLE else View.GONE
        holder.layoutMonto.error = null
        holder.ayuda.text = if (item.seleccionado) {
            "Escribe cuanto pagara el cliente con este metodo"
        } else {
            "Activalo para asignarle una parte del cobro"
        }
        actualizarEstadoSeleccion(holder, item.seleccionado)

        val textoActual = holder.monto.text?.toString().orEmpty()
        if (textoActual != item.monto) {
            holder.monto.setText(item.monto)
        }

        holder.check.setOnCheckedChangeListener { _, isChecked ->
            item.seleccionado = isChecked

            if (!isChecked) {
                item.monto = ""
                holder.monto.setText("")
                holder.layoutMonto.error = null
            }

            holder.layoutMonto.visibility = if (isChecked) View.VISIBLE else View.GONE
            holder.ayuda.text = if (isChecked) {
                "Escribe cuanto pagara el cliente con este metodo"
            } else {
                "Activalo para asignarle una parte del cobro"
            }
            actualizarEstadoSeleccion(holder, isChecked)
            onCambio()
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                item.monto = s?.toString().orEmpty()

                holder.layoutMonto.error = if (item.seleccionado) {
                    val montoNumero = item.monto.trim().toDoubleOrNull()
                    when {
                        item.monto.trim().isEmpty() -> null
                        montoNumero == null -> "Monto invalido"
                        montoNumero <= 0.0 -> "Debe ser mayor a 0"
                        else -> null
                    }
                } else {
                    null
                }

                onCambio()
            }

            override fun afterTextChanged(s: Editable?) = Unit
        }

        holder.monto.addTextChangedListener(watcher)
        holder.monto.tag = watcher
    }

    override fun getItemCount(): Int = lista.size

    override fun onViewAttachedToWindow(holder: PagoMixtoViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.setIsRecyclable(false)
    }

    override fun onViewRecycled(holder: PagoMixtoViewHolder) {
        super.onViewRecycled(holder)

        val watcher = holder.monto.tag as? TextWatcher
        if (watcher != null) {
            holder.monto.removeTextChangedListener(watcher)
        }
    }

    private fun actualizarEstadoSeleccion(holder: PagoMixtoViewHolder, seleccionado: Boolean) {
        holder.card.strokeColor = if (seleccionado) 0xFF111111.toInt() else 0xFFE3E7EE.toInt()
        holder.card.strokeWidth = if (seleccionado) 2 else 1
    }
}
