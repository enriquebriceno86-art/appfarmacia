package com.app.administradorfarmadon.ClasesDatabase

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CierreTurnoPdfGenerator {

    data class DatosCierre(
        val nombreCaja: String,
        val nombreCajero: String,
        val fecha: String,
        val horaApertura: String,
        val horaCierre: String,
        val montoApertura: Double,
        val totalVentas: Double,
        val ventasEfectivo: Double,
        val totalEgresos: Double,
        val totalDevoluciones: Double,
        val efectivoEsperado: Double,
        val efectivoReal: Double,
        val diferencia: Double,
        /** "cuadrado" | "faltante" | "sobrante" */
        val estadoCuadre: String,
        val observacion: String,
        val resumenMetodos: List<Pair<String, Double>> = emptyList(),
        val totalDiferenciasCobradas: Double = 0.0,
        val resumenDiferenciasCobradas: List<Pair<String, Double>> = emptyList()
    )

    /** Genera el PDF en cache y devuelve el File. */
    fun generar(context: Context, datos: DatosCierre): File {
        val pageW = 595
        val pageH = 842
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val margin = 40f
        val contentW = pageW - margin * 2
        val cur = Cursor(margin)

        // ── Paints ──
        val pTitle   = mkPaint(18f, Typeface.BOLD,   "#1A1C1E")
        val pSub     = mkPaint(10f, Typeface.NORMAL, "#6B7280")
        val pSection = mkPaint(10f, Typeface.BOLD,   "#374151")
        val pLabel   = mkPaint(10f, Typeface.NORMAL, "#6B7280")
        val pValue   = mkPaint(10f, Typeface.BOLD,   "#1A1C1E")
        val pGreen   = mkPaint(10f, Typeface.BOLD,   "#166534")
        val pRed     = mkPaint(10f, Typeface.BOLD,   "#DC2626")
        val pAmber   = mkPaint(10f, Typeface.BOLD,   "#92400E")
        val pFooter  = mkPaint(8f,  Typeface.NORMAL, "#9AA0A6")
        val pDivider = Paint().apply {
            color = Color.parseColor("#E5E7EB")
            strokeWidth = 1f
        }

        // ── ENCABEZADO ──
        canvas.drawText("REPORTE DE CIERRE DE TURNO", margin, cur.adv(14f), pTitle)
        canvas.drawText(datos.nombreCaja, margin, cur.adv(20f), pSub)
        canvas.drawText("Cajero/a: ${datos.nombreCajero}", margin, cur.adv(14f), pSub)
        canvas.drawText(
            "Fecha: ${datos.fecha}  ·  Apertura: ${datos.horaApertura}  ·  Cierre: ${datos.horaCierre}",
            margin, cur.adv(14f), pSub
        )

        divider(canvas, margin, cur.adv(12f), pageW - margin, pDivider)

        // ── RESUMEN FINANCIERO ──
        cur.adv(12f)
        canvas.drawText("RESUMEN FINANCIERO", margin, cur.v, pSection)
        cur.adv(16f)

        fila(canvas, margin, contentW, cur, "Apertura de caja",
            MonedaHelper.formatear(datos.montoApertura), pLabel, pValue)
        fila(canvas, margin, contentW, cur, "+ Ventas del turno",
            MonedaHelper.formatear(datos.totalVentas), pLabel, pValue)
        if (datos.totalEgresos > 0.01)
            fila(canvas, margin, contentW, cur, "− Egresos",
                MonedaHelper.formatear(datos.totalEgresos), pLabel, pValue)
        if (datos.totalDevoluciones > 0.01)
            fila(canvas, margin, contentW, cur, "− Devoluciones",
                MonedaHelper.formatear(datos.totalDevoluciones), pLabel, pValue)

        divider(canvas, margin, cur.adv(6f), pageW - margin, pDivider)
        cur.adv(10f)

        fila(canvas, margin, contentW, cur, "= Efectivo esperado en caja",
            MonedaHelper.formatear(datos.efectivoEsperado), pLabel, pValue)
        fila(canvas, margin, contentW, cur, "Contado físicamente",
            MonedaHelper.formatear(datos.efectivoReal), pLabel, pValue)

        // diferencia coloreada
        val difPaint = when (datos.estadoCuadre) {
            "sobrante" -> pAmber
            "faltante" -> pRed
            else       -> pGreen
        }
        val difLabel = when (datos.estadoCuadre) {
            "sobrante" -> "Diferencia (sobrante)"
            "faltante" -> "Diferencia (faltante)"
            else       -> "Diferencia"
        }
        val difStr = if (kotlin.math.abs(datos.diferencia) < 0.01)
            MonedaHelper.formatear(0.0)
        else
            (if (datos.diferencia > 0) "+" else "") + MonedaHelper.formatear(datos.diferencia)
        fila(canvas, margin, contentW, cur, difLabel, difStr, pLabel, difPaint)

        cur.adv(4f)
        val estadoTxt = when (datos.estadoCuadre) {
            "sobrante" -> "↑ Sobrante"
            "faltante" -> "↓ Faltante"
            else       -> "✓ Cuadrado"
        }
        canvas.drawText("Estado del cuadre: $estadoTxt", margin, cur.v, difPaint)
        cur.adv(16f)

        // ── DESGLOSE POR MÉTODO ──
        if (datos.resumenMetodos.isNotEmpty()) {
            divider(canvas, margin, cur.adv(4f), pageW - margin, pDivider)
            cur.adv(12f)
            canvas.drawText("DESGLOSE POR MÉTODO DE PAGO", margin, cur.v, pSection)
            cur.adv(16f)
            for ((metodo, monto) in datos.resumenMetodos) {
                fila(canvas, margin, contentW, cur, metodo,
                    MonedaHelper.formatear(monto), pLabel, pValue)
            }
        }

        // ── VENTAS EN EFECTIVO (referencia cuadre) ──
        if (datos.ventasEfectivo > 0.01) {
            cur.adv(4f)
            fila(canvas, margin, contentW, cur,
                "  (de los cuales en efectivo)", MonedaHelper.formatear(datos.ventasEfectivo),
                pLabel, pLabel)
        }

        // ── OBSERVACIÓN ──
        if (datos.observacion.isNotBlank()) {
            divider(canvas, margin, cur.adv(14f), pageW - margin, pDivider)
            cur.adv(12f)
            canvas.drawText("OBSERVACIÓN", margin, cur.v, pSection)
            cur.adv(16f)
            // wrap texto largo manualmente
            wrapText(canvas, datos.observacion, margin, cur, contentW, pValue)
        }

        // ── FOOTER ──
        if (datos.resumenDiferenciasCobradas.isNotEmpty()) {
            divider(canvas, margin, cur.adv(14f), pageW - margin, pDivider)
            cur.adv(12f)
            canvas.drawText("DIFERENCIAS COBRADAS POR CAMBIOS", margin, cur.v, pSection)
            cur.adv(16f)
            fila(
                canvas,
                margin,
                contentW,
                cur,
                "Total cobrado por diferencias",
                MonedaHelper.formatear(datos.totalDiferenciasCobradas),
                pLabel,
                pValue
            )
            for ((metodo, monto) in datos.resumenDiferenciasCobradas) {
                fila(canvas, margin, contentW, cur, metodo, MonedaHelper.formatear(monto), pLabel, pValue)
            }
        }

        val yFooter = (pageH - 30).toFloat()
        divider(canvas, margin, yFooter - 8f, pageW - margin, pDivider)
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        canvas.drawText(
            "Generado el ${sdf.format(Date())}  ·  AdministradorFarmadon",
            margin, yFooter, pFooter
        )

        doc.finishPage(page)

        val file = File(context.cacheDir, "cierre_${datos.fecha}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    // ── Utilidades de dibujo ──

    private class Cursor(init: Float) {
        var v: Float = init
        fun adv(delta: Float): Float { v += delta; return v }
    }

    private fun mkPaint(size: Float, style: Int, hex: String) = Paint().apply {
        textSize = size
        typeface = Typeface.create(Typeface.DEFAULT, style)
        color = Color.parseColor(hex)
        isAntiAlias = true
    }

    private fun divider(canvas: Canvas, x1: Float, y: Float, x2: Float, p: Paint) {
        canvas.drawLine(x1, y, x2, y, p)
    }

    private fun fila(
        canvas: Canvas, margin: Float, contentW: Float, cur: Cursor,
        label: String, value: String, pLabel: Paint, pValue: Paint
    ) {
        canvas.drawText(label, margin, cur.v, pLabel)
        canvas.drawText(value, margin + contentW - pValue.measureText(value), cur.v, pValue)
        cur.adv(15f)
    }

    private fun wrapText(
        canvas: Canvas, text: String, x: Float, cur: Cursor,
        maxWidth: Float, p: Paint
    ) {
        val words = text.split(" ")
        val sb = StringBuilder()
        for (word in words) {
            val test = if (sb.isEmpty()) word else "$sb $word"
            if (p.measureText(test) > maxWidth) {
                canvas.drawText(sb.toString(), x, cur.v, p)
                cur.adv(15f)
                sb.clear().append(word)
            } else {
                if (sb.isNotEmpty()) sb.append(" ")
                sb.append(word)
            }
        }
        if (sb.isNotEmpty()) {
            canvas.drawText(sb.toString(), x, cur.v, p)
            cur.adv(15f)
        }
    }
}
