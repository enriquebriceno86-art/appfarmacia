package com.app.administradorfarmadon.ActivityFragmentos.Reciclers

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ActivitysPerfilItem.MetodoPagoConfig
import com.google.android.material.card.MaterialCardView

class AdapterMetodosPagoCobro(
    private val lista: List<MetodoPagoConfig>,
    private val onClick: (MetodoPagoConfig) -> Unit
) : RecyclerView.Adapter<AdapterMetodosPagoCobro.MetodoViewHolder>() {

    private var metodoSeleccionadoId: String? = null

    class MetodoViewHolder(
        val card: MaterialCardView,
        val indicador: View,
        val titulo: TextView,
        val subtitulo: TextView
    ) : RecyclerView.ViewHolder(card)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetodoViewHolder {
        val context = parent.context

        val card = MaterialCardView(context).apply {
            radius = 20f
            strokeWidth = 1
            strokeColor = "#E4E8EF".toColorInt()
            cardElevation = 0f
            setCardBackgroundColor("#FCFCFD".toColorInt())

            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
        }

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = 76
            setPadding(20, 18, 20, 18)
        }

        val indicador = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(16, 16)
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor("#FFFFFF".toColorInt())
                setStroke(2, "#C7D0DC".toColorInt())
            }
        }

        val textos = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = 14
            }
        }

        val titulo = TextView(context).apply {
            setTextColor(Color.BLACK)
            textSize = 15.5f
            setTypeface(typeface, Typeface.BOLD)
        }

        val subtitulo = TextView(context).apply {
            setTextColor("#7A8494".toColorInt())
            textSize = 12.5f
            visibility = View.GONE
        }

        textos.addView(titulo)
        textos.addView(subtitulo)

        root.addView(indicador)
        root.addView(textos)
        card.addView(root)

        return MetodoViewHolder(
            card = card,
            indicador = indicador,
            titulo = titulo,
            subtitulo = subtitulo
        )
    }

    override fun onBindViewHolder(holder: MetodoViewHolder, position: Int) {
        val item = lista[position]
        val seleccionado = item.id == metodoSeleccionadoId

        holder.titulo.text = item.titulo.ifBlank { item.categoria }

        holder.card.strokeColor =
            if (seleccionado) "#111111".toColorInt() else "#E4E8EF".toColorInt()
        holder.card.strokeWidth = if (seleccionado) 2 else 1
        holder.card.setCardBackgroundColor(
            if (seleccionado) "#FFFFFF".toColorInt() else "#FCFCFD".toColorInt()
        )
        holder.indicador.background = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            if (seleccionado) {
                setColor("#111111".toColorInt())
                setStroke(2, "#111111".toColorInt())
            } else {
                setColor("#FFFFFF".toColorInt())
                setStroke(2, "#C7D0DC".toColorInt())
            }
        }

        holder.card.setOnClickListener {
            metodoSeleccionadoId = item.id
            notifyDataSetChanged()
            onClick(item)
        }
    }

    override fun getItemCount(): Int = lista.size
}
