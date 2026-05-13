package com.app.administradorfarmadon.ActivityFragmentos.Reciclers

import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteConsumoRules
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.PresentacionProducto
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.R
import java.util.Locale

class AdapterResultadosBusquedaCaja(
    private val onAgregarPresentacion: (MoldeProductos, PresentacionProducto, View) -> Unit
) : RecyclerView.Adapter<AdapterResultadosBusquedaCaja.GroupViewHolder>() {

    private val listaProductos = mutableListOf<MoldeProductos>()
    private var queryActual = ""

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreProductoGroup)
        val tvBadge: TextView = itemView.findViewById(R.id.tvBadgeCantPresentaciones)
        val container: LinearLayout = itemView.findViewById(R.id.containerPresentaciones)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_agrupado_busqueda, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val producto = listaProductos[position]
        
        // Aplicar resaltado al nombre del producto
        holder.tvNombre.text = resaltarTexto(producto.nombre.ifBlank { "Producto" }, queryActual)

        val presentaciones = if (producto.tienePresentaciones && producto.presentaciones.isNotEmpty()) {
            producto.presentaciones
        } else {
            listOf(PresentacionProducto(
                nombre = producto.presentacionprincipal.ifBlank { "Unidad" },
                cantidad = 1,
                precioventa = producto.preciodecompra.toDoubleOrNull() ?: 0.0
            ))
        }

        holder.tvBadge.text = if (presentaciones.size == 1) "1 presentación" else "${presentaciones.size} presentaciones"
        
        holder.container.removeAllViews()
        val inflater = LayoutInflater.from(holder.itemView.context)

        presentaciones.forEach { pres ->
            val row = inflater.inflate(R.layout.item_presentacion_busqueda, holder.container, false)
            val tvNombreRow: TextView = row.findViewById(R.id.tvNombrePresRow)
            val tvLoteRow: TextView = row.findViewById(R.id.tvLotePresRow)
            val tvStockRow: TextView = row.findViewById(R.id.tvStockPresRow)
            val tvPrecioRow: TextView = row.findViewById(R.id.tvPrecioPresRow)
            val btnAdd: MaterialButton = row.findViewById(R.id.btnAgregarPresRow)

            val unidadesPres = if (pres.cantidad <= 0) 1 else pres.cantidad
            val stockTotal = producto.cantidadinicial.toIntOrNull() ?: 0
            val stockEnEstaPres = if (unidadesPres > 0) stockTotal / unidadesPres else 0

            tvNombreRow.text = pres.nombre.ifBlank { "Unidad" }
            tvPrecioRow.text = String.format(Locale.US, "%.2f", pres.precioventa)
            tvStockRow.text = "$stockEnEstaPres disp."
            
            val res = LoteConsumoRules.resolver(producto, unidadesPres)
            val loteActual = res.loteActual
            if (loteActual != null) {
                val numLote = loteActual.second.numero.ifBlank { loteActual.first }
                val venc = loteActual.second.vencimiento.ifBlank { "Sin fecha" }
                tvLoteRow.text = "Lote: $numLote · Vence: $venc"
                tvLoteRow.visibility = View.VISIBLE
            } else {
                tvLoteRow.visibility = View.GONE
            }

            btnAdd.isEnabled = stockEnEstaPres > 0
            btnAdd.alpha = if (stockEnEstaPres > 0) 1f else 0.4f
            
            // Blindaje de foco para evitar que el teclado se oculte al presionar
            btnAdd.isFocusable = false
            btnAdd.isFocusableInTouchMode = false

            btnAdd.setOnClickListener { 
                btnAdd.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                
                // --- MICRO-ANIMACIÓN DE ÉXITO EN EL BOTÓN ---
                btnAdd.animate()
                    .scaleX(0.85f)
                    .scaleY(0.85f)
                    .setDuration(80L)
                    .withEndAction {
                        btnAdd.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(120L)
                            .setInterpolator(android.view.animation.OvershootInterpolator())
                            .start()
                    }
                    .start()

                onAgregarPresentacion(producto, pres, btnAdd) 
            }

            holder.container.addView(row)
        }
    }

    override fun getItemCount(): Int = listaProductos.size

    fun actualizarProductos(nuevosProductos: List<MoldeProductos>, query: String = "") {
        this.queryActual = query
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = listaProductos.size
            override fun getNewListSize(): Int = nuevosProductos.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                listaProductos[oldItemPosition].indice == nuevosProductos[newItemPosition].indice
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                listaProductos[oldItemPosition] == nuevosProductos[newItemPosition]
        })
        listaProductos.clear()
        listaProductos.addAll(nuevosProductos)
        diff.dispatchUpdatesTo(this)
    }

    private fun resaltarTexto(textoFull: String, query: String): CharSequence {
        if (query.isBlank() || !textoFull.contains(query, ignoreCase = true)) return textoFull
        
        val spannable = SpannableString(textoFull)
        val lowerTexto = textoFull.lowercase(Locale.getDefault())
        val lowerQuery = query.lowercase(Locale.getDefault())
        
        var start = lowerTexto.indexOf(lowerQuery)
        while (start != -1) {
            val end = start + lowerQuery.length
            spannable.setSpan(
                StyleSpan(Typeface.BOLD), 
                start, 
                end, 
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            start = lowerTexto.indexOf(lowerQuery, end)
        }
        return spannable
    }
}
