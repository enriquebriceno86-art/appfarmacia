package com.app.administradorfarmadon.ActivitysCaja

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ClasesDatabase.FechaHoraServidorHelper
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.ClasesDatabase.SessionManager
import com.app.administradorfarmadon.ClasesDatabase.obtenerDoubleFlexible
import com.app.administradorfarmadon.ClasesDatabase.obtenerTexto
import com.app.administradorfarmadon.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Locale


class ActivityHistorialVentasCaja : AppCompatActivity() {

    companion object {
        const val EXTRA_FECHA_VENTAS = "extra_fecha_ventas"
        const val EXTRA_ID_CAJA_VENTAS = "extra_id_caja_ventas"
        const val EXTRA_ID_TURNO_VENTAS = "extra_id_turno_ventas"
        const val EXTRA_NOMBRE_CAJA_VENTAS = "extra_nombre_caja_ventas"
        const val EXTRA_NOMBRE_CAJERO_VENTAS = "extra_nombre_cajero_ventas"
    }

    private data class ResumenCambioVentaHistorial(
        val lineas: List<String> = emptyList()
    )

    private lateinit var tvFechaSeleccionada: TextView
    private lateinit var tvCantidadVentas: TextView
    private lateinit var tvTotalDia: TextView
    private lateinit var tvPromedio: TextView
    private lateinit var rvVentas: RecyclerView
    private lateinit var btnExportar: MaterialButton
    private val simboloMoneda: String
        get() = SessionManager.monedaSimbolo.trim().ifBlank { "S/" }
    private lateinit var ventaAdapter: VentaAdapter

    private var listaActual: List<VentaHistorial> = emptyList()
    private var fechaActualConsulta: String = ""
    private var fechaOficialHoyFirebase: String = ""
    private var idCajaFiltro: String = ""
    private var idTurnoFiltro: String = ""
    private var nombreCajaFiltro: String = ""
    private var nombreCajeroFiltro: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_ventas_caja)

        inicializarVistas()
        SessionManager.cargarSesion(this)
        leerExtras()
        configurarToolbar()
        setupRecycler()
        configurarBotonExportar()
        if (tieneFiltroTurno()) {
            findViewById<View>(R.id.cardFecha).visibility = View.GONE
        }
        inicializarFechaConsulta()

        findViewById<View>(R.id.layoutFecha).setOnClickListener {
            if (!tieneFiltroTurno()) {
                mostrarDatePicker()
            }
        }


        findViewById<View>(R.id.btnInfoPromedio).setOnClickListener {
            mostrarInfoPromedio()
        }

    }


    private fun mostrarInfoPromedio() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("¿Qué significa promedio?")
            .setMessage(
                "Es el valor medio de las ventas del día.\n\n" +
                        "Se obtiene dividiendo el total vendido entre la cantidad de ventas.\n\n" +
                        "No significa que todos los clientes pagaron lo mismo.\n\n" +
                        "Ejemplo:\n" +
                        "Venta 1 = ${simboloMoneda}12\n" +
                        "Venta 2 = ${simboloMoneda}6\n" +
                        "Promedio = ${simboloMoneda}9"
            )
            .setPositiveButton("Entendido", null)
            .show()
    }

    private fun inicializarFechaConsulta() {
        val fechaIntent = intent.getStringExtra(EXTRA_FECHA_VENTAS).orEmpty().trim()
        if (fechaIntent.isNotBlank()) {
            fechaActualConsulta = fechaIntent
            fechaOficialHoyFirebase = fechaIntent
            tvFechaSeleccionada.text = obtenerFechaBonitaDesdeFirebase(fechaActualConsulta)
            cargarVentasPorFecha(fechaActualConsulta)
            return
        }

        FechaHoraServidorHelper.obtenerMomentoActual(
            onSuccess = { momento ->
                fechaOficialHoyFirebase = momento.fechaFirebase
                fechaActualConsulta = momento.fechaFirebase
                tvFechaSeleccionada.text = obtenerFechaBonitaDesdeFirebase(fechaActualConsulta)
                cargarVentasPorFecha(fechaActualConsulta)
            },
            onError = {
                val momento = FechaHoraServidorHelper.estimarMomentoActualDesdeCache()
                fechaOficialHoyFirebase = momento.fechaFirebase
                fechaActualConsulta = momento.fechaFirebase
                tvFechaSeleccionada.text = obtenerFechaBonitaDesdeFirebase(fechaActualConsulta)
                cargarVentasPorFecha(fechaActualConsulta)
            }
        )
    }

    private fun inicializarVistas() {
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada)
        tvCantidadVentas = findViewById(R.id.tvCantidadVentas)
        tvTotalDia = findViewById(R.id.tvTotalDia)
        tvPromedio = findViewById(R.id.tvPromedio)
        rvVentas = findViewById(R.id.rvVentas)
        btnExportar = findViewById(R.id.btnExportar)
    }

    private fun leerExtras() {
        idCajaFiltro = intent.getStringExtra(EXTRA_ID_CAJA_VENTAS).orEmpty().trim()
        idTurnoFiltro = intent.getStringExtra(EXTRA_ID_TURNO_VENTAS).orEmpty().trim()
        nombreCajaFiltro = intent.getStringExtra(EXTRA_NOMBRE_CAJA_VENTAS).orEmpty().trim()
        nombreCajeroFiltro = intent.getStringExtra(EXTRA_NOMBRE_CAJERO_VENTAS).orEmpty().trim()
    }

    private fun configurarToolbar() {
        val toolbarTitle = findViewById<TextView>(R.id.tvToolbarTitle)
        toolbarTitle?.text = if (tieneFiltroTurno()) {
            "Ventas del turno"
        } else {
            "Historial de ventas"
        }

        val subtitle = findViewById<TextView>(R.id.tvToolbarSubtitle)
        if (subtitle != null) {
            val texto = buildString {
                if (nombreCajaFiltro.isNotBlank()) append(nombreCajaFiltro)
                if (nombreCajeroFiltro.isNotBlank()) {
                    if (isNotBlank()) append(" • ")
                    append(nombreCajeroFiltro)
                }
            }
            subtitle.text = texto
            subtitle.visibility = if (texto.isBlank() || !tieneFiltroTurno()) View.GONE else View.VISIBLE
        }
    }

    private fun setupRecycler() {
        ventaAdapter = VentaAdapter(emptyList()) { venta ->
            abrirDetalleVenta(venta)
        }

        rvVentas.layoutManager = LinearLayoutManager(this)
        rvVentas.adapter = ventaAdapter

    }

    private fun configurarBotonExportar() {
        btnExportar.setOnClickListener {
            val estadoUi = HistorialVentasSummaryRules.construirEstadoUi(listaActual)
            if (!estadoUi.puedeExportar) {
                Toast.makeText(this, "No hay ventas para exportar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val pdfFile = generarPdfVentas()
                Toast.makeText(this, "Reporte PDF guardado en el celular", Toast.LENGTH_LONG).show()
                abrirPdf(pdfFile)
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Error al generar PDF: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun cargarVentasPorFecha(fechaSeleccionada: String) {
        if (tieneFiltroTurno()) {
            cargarVentasPorTurno(fechaSeleccionada)
            return
        }

        val ventasRef = FirebaseDatabase.getInstance()
            .reference
            .child("Ventas")
            .child(fechaSeleccionada)

        ventasRef.get()
            .addOnSuccessListener { snapshot ->
                val listaVentas = mutableListOf<VentaHistorial>()

                if (!snapshot.exists()) {
                    listaActual = emptyList()
                    ventaAdapter.actualizarLista(listaActual)
                    aplicarEstadoHistorialVentas(listaActual)
                    Toast.makeText(this, "No hay ventas en esta fecha", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                for (ventaSnap in snapshot.children) {
                    construirVentaHistorialSegura(ventaSnap)?.let(listaVentas::add)
                }

                listaActual = listaVentas
                ventaAdapter.actualizarLista(listaActual)
                aplicarEstadoHistorialVentas(listaActual)
              }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al cargar ventas: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
              }
    }

    private fun cargarVentasPorTurno(fechaSeleccionada: String) {
        FirebaseDatabase.getInstance()
            .reference
            .child("VentasPorCajera")
            .child(idCajaFiltro)
            .child(fechaSeleccionada)
            .get()
            .addOnSuccessListener { snapshot ->
                val listaVentas = mutableListOf<VentaHistorial>()

                if (!snapshot.exists()) {
                    listaActual = emptyList()
                    ventaAdapter.actualizarLista(listaActual)
                    aplicarEstadoHistorialVentas(listaActual)
                    Toast.makeText(this, "No hay ventas en este turno", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                for (ventaSnap in snapshot.children) {
                    val idTurnoVenta = ventaSnap.child("idTurno").getValue(String::class.java).orEmpty()
                        .ifBlank { ventaSnap.child("infoVenta").child("idTurno").getValue(String::class.java).orEmpty() }
                    if (idTurnoFiltro.isNotBlank() && idTurnoVenta.isNotBlank() && idTurnoVenta != idTurnoFiltro) {
                        continue
                    }
                    construirVentaHistorialSegura(ventaSnap)?.let(listaVentas::add)
                }

                listaActual = listaVentas
                ventaAdapter.actualizarLista(listaActual)
                aplicarEstadoHistorialVentas(listaActual)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al cargar ventas del turno: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun construirVentaHistorialSegura(ventaSnap: DataSnapshot): VentaHistorial? {
        val infoVenta = ventaSnap.child("infoVenta")
            .getValue(InfoVenta::class.java)
            ?: return null

        val productosSnap = ventaSnap.child("productos")
        val totalUnidades = contarCantidadTotalProductos(productosSnap)

        val ventaParseada = HistorialVentasRemoteRules.construirVentaHistorial(
            VentaHistorialRemoteData(
                idVenta = ventaSnap.key ?: "",
                totalRaw = infoVenta.total,
                metodoPago = infoVenta.metodoPago,
                estado = infoVenta.estado,
                fecha = infoVenta.fecha,
                hora = infoVenta.hora,
                timestampVentaServidor = ventaSnap.child("infoVenta").child("timestampVentaServidor").getValue(Long::class.java)
                    ?: ventaSnap.child("infoVenta").child("timestampServidor").getValue(Long::class.java)
                    ?: 0L,
                cantidadProductos = totalUnidades
            )
        )

        return ventaParseada
    }

    private fun contarCantidadTotalProductos(productosSnap: DataSnapshot): Int {
        var totalUnidades = 0

        for (producto in productosSnap.children) {
            val cantidad = HistorialVentasRemoteRules.parsearCantidad(
                producto.child("cantidad").value
            )

            totalUnidades += cantidad
        }

        return totalUnidades
    }

    private fun aplicarEstadoHistorialVentas(lista: List<VentaHistorial>) {
        val resumen = HistorialVentasSummaryRules.construirResumen(lista)
        actualizarResumen(resumen)
    }

    private fun actualizarResumen(resumen: HistorialVentasResumen) {
        tvCantidadVentas.text = "Ventas: ${resumen.cantidadVentas}"
        tvTotalDia.text = MonedaHelper.formatear(resumen.totalDia)
        tvPromedio.text = "Promedio: ${MonedaHelper.formatear(resumen.promedio)}"
    }

    private fun mostrarDatePicker() {
        val fechaBase = fechaActualConsulta.ifBlank {
            fechaOficialHoyFirebase.ifBlank { FechaHoraServidorHelper.estimarMomentoActualDesdeCache().fechaFirebase }
        }
        val calendar = FechaHoraServidorHelper.calendarDesdeFechaFirebase(fechaBase)
            ?: FechaHoraServidorHelper.calendarDesdeTimestamp(
                FechaHoraServidorHelper.estimarMomentoActualDesdeCache().timestampServidorMs
            )
        val hoyCalendar = FechaHoraServidorHelper.calendarDesdeFechaFirebase(
            fechaOficialHoyFirebase.ifBlank { FechaHoraServidorHelper.estimarMomentoActualDesdeCache().fechaFirebase }
        ) ?: FechaHoraServidorHelper.calendarDesdeTimestamp(
            FechaHoraServidorHelper.estimarMomentoActualDesdeCache().timestampServidorMs
        )

        val dialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val calSeleccionado = FechaHoraServidorHelper.calendarDesdeTimestamp(hoyCalendar.timeInMillis).apply {
                    set(year, month, dayOfMonth)
                }

                tvFechaSeleccionada.text = obtenerFechaBonita(calSeleccionado.timeInMillis)
                fechaActualConsulta = obtenerFechaFirebaseDesdeCalendar(calSeleccionado)
                cargarVentasPorFecha(fechaActualConsulta)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        dialog.datePicker.maxDate = hoyCalendar.timeInMillis
        dialog.show()
    }

    private fun abrirDetalleVenta(venta: VentaHistorial) {
        val intent = Intent(this, DetalleVentaActivity::class.java).apply {
            putExtra(DetalleVentaActivity.EXTRA_FECHA_VENTA, fechaActualConsulta)
            putExtra(DetalleVentaActivity.EXTRA_ID_VENTA, venta.id)
        }
        startActivity(intent)
    }

    private fun tieneFiltroTurno(): Boolean {
        return idCajaFiltro.isNotBlank() && idTurnoFiltro.isNotBlank()
    }

    private fun obtenerFechaBonitaDesdeFirebase(fechaFirebase: String): String {
        return FechaHoraServidorHelper.formatearFechaVisibleDesdeFirebase(fechaFirebase)
    }

    private fun obtenerFechaActualParaFirebase(): String {
        return FechaHoraServidorHelper.estimarMomentoActualDesdeCache().fechaFirebase
    }

    private fun obtenerFechaFirebaseDesdeCalendar(calendar: Calendar): String {
        return FechaHoraServidorHelper.formatearFechaFirebase(calendar.timeInMillis)
    }

    private fun obtenerFechaBonita(timeMillis: Long): String {
        return FechaHoraServidorHelper.formatearFechaVisible(timeMillis)
    }

    private fun obtenerDetalleVenta(
        fecha: String,
        idVenta: String,
        onSuccess: (InfoVenta, List<ProductoCaja>, ResumenCambioVentaHistorial) -> Unit,
        onError: (String) -> Unit
    ) {
        val ref = FirebaseDatabase.getInstance().reference
            .child("Ventas")
            .child(fecha)
            .child(idVenta)

        ref.get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    onError("No se encontró la venta")
                    return@addOnSuccessListener
                }

                val infoVentaOriginal = snapshot.child("infoVenta")
                    .getValue(InfoVenta::class.java)

                  if (infoVentaOriginal == null) {
                      onError("No se encontró la información de la venta")
                      return@addOnSuccessListener
                  }

                  val infoVenta = HistorialVentasDetailRules.construirInfoVentaDetalle(
                      VentaDetalleRemoteData(
                          idVentaFallback = snapshot.key ?: idVenta,
                          infoVentaOriginal = infoVentaOriginal,
                          timestampVentaServidor = snapshot.child("infoVenta").child("timestampVentaServidor").getValue(Long::class.java)
                              ?: snapshot.child("infoVenta").child("timestampServidor").getValue(Long::class.java)
                              ?: 0L
                      )
                  )

                  val listaProductos = construirProductosDetalleSeguros(snapshot.child("productos"))
                  val resumenCambio = construirResumenCambioVenta(snapshot)

                  onSuccess(infoVenta, listaProductos, resumenCambio)
              }
              .addOnFailureListener { e ->
                  onError(e.message ?: "Error al leer venta")
              }
      }

    private fun construirResumenCambioVenta(snapshot: DataSnapshot): ResumenCambioVentaHistorial {
        val devolucionSnap = snapshot.child("devolucion")
        if (!devolucionSnap.exists()) return ResumenCambioVentaHistorial()

        val eventos = if (devolucionSnap.child("eventos").exists()) {
            devolucionSnap.child("eventos").children.toList()
        } else {
            listOf(devolucionSnap)
        }

        val lineas = eventos.mapNotNull { eventoSnap ->
            val tipoResolucion = eventoSnap.child("tipoResolucion").obtenerTexto()
                .ifBlank { devolucionSnap.child("tipoResolucion").obtenerTexto() }
                .trim()
                .lowercase(Locale.getDefault())
            if (tipoResolucion != "cambio_producto") return@mapNotNull null

            val montoCobrado = eventoSnap.child("ajusteFinanciero")
                .child("montoCobradoCliente")
                .obtenerDoubleFlexible()
                ?: eventoSnap.child("montoCobradoCliente").obtenerDoubleFlexible()
                ?: 0.0
            if (montoCobrado <= 0.0) return@mapNotNull null

            val sustituto = eventoSnap.child("sustitucion").child("nombre").obtenerTexto()
                .ifBlank { devolucionSnap.child("sustitucionUltima").child("nombre").obtenerTexto() }
            val medioPago = eventoSnap.child("pagoDiferencia").child("metodoPago").obtenerTexto()
                .ifBlank { devolucionSnap.child("metodoPagoDiferenciaUltimo").obtenerTexto() }
                .ifBlank {
                    devolucionSnap.child("pagoDiferenciaUltimo")
                        .child("metodoPago")
                        .obtenerTexto()
                }
            val montoPagado = eventoSnap.child("pagoDiferencia").child("montoPagado").obtenerDoubleFlexible()
                ?: devolucionSnap.child("montoPagoDiferenciaUltimo").obtenerDoubleFlexible()
                ?: devolucionSnap.child("pagoDiferenciaUltimo").child("montoPagado").obtenerDoubleFlexible()
                ?: montoCobrado
            val motivo = eventoSnap.child("motivo").obtenerTexto()
                .ifBlank { devolucionSnap.child("motivo").obtenerTexto() }

            buildString {
                append("Cambio de producto")
                if (sustituto.isNotBlank()) {
                    append(": ")
                    append(sustituto)
                }
                append(". Diferencia cobrada: ")
                append(MonedaHelper.formatear(montoPagado))
                if (medioPago.isNotBlank()) {
                    append(" por ")
                    append(medioPago)
                }
                if (motivo.isNotBlank()) {
                    append(". Motivo: ")
                    append(motivo)
                }
            }
        }.distinct()

        return ResumenCambioVentaHistorial(lineas)
    }

    private fun construirProductosDetalleSeguros(productosSnap: DataSnapshot): List<ProductoCaja> {
        val productos = productosSnap.children.map { productoSnap ->
            productoSnap.getValue(ProductoCaja::class.java)
        }
        return HistorialVentasDetailRules.construirListaProductosDetalle(productos)
    }

    private fun generarPdfVentas(): File {
        val pdfDocument = PdfDocument()

        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paintTitle = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
        }

        val paintSubTitle = Paint().apply {
            textSize = 13f
            isFakeBoldText = true
        }

        val paintNormal = Paint().apply {
            textSize = 11f
        }

        val paintSmall = Paint().apply {
            textSize = 10f
        }

        val paintLine = Paint().apply {
            strokeWidth = 1f
        }

        var y = 40

        val totalDia = listaActual.sumOf { it.total }
        val cantidadVentas = listaActual.size
        val promedio = if (cantidadVentas > 0) totalDia / cantidadVentas else 0.0
        val totalProductos = listaActual.sumOf { it.cantidadProductos }

        val ventasEfectivo = listaActual
            .filter { it.metodoPago.equals("efectivo", true) }
            .sumOf { it.total }

        val ventasTarjeta = listaActual
            .filter { it.metodoPago.equals("tarjeta", true) }
            .sumOf { it.total }

        val ventasTransferencia = listaActual
            .filter { it.metodoPago.equals("transferencia", true) }
            .sumOf { it.total }

        canvas.drawText("FARMADON", 40f, y.toFloat(), paintTitle)
        y += 24
        canvas.drawText("Reporte diario de ventas", 40f, y.toFloat(), paintSubTitle)
        y += 18
        canvas.drawText("Fecha: $fechaActualConsulta", 40f, y.toFloat(), paintNormal)
        y += 24

        canvas.drawLine(40f, y.toFloat(), 555f, y.toFloat(), paintLine)
        y += 20

        canvas.drawText("RESUMEN GENERAL", 40f, y.toFloat(), paintSubTitle)
        y += 18
        canvas.drawText("Cantidad de ventas: $cantidadVentas", 40f, y.toFloat(), paintNormal)
        y += 16
        canvas.drawText("Productos vendidos: $totalProductos", 40f, y.toFloat(), paintNormal)
        y += 16
        canvas.drawText(
            "Monto total del día: $simboloMoneda ${String.format(Locale.US, "%.2f", totalDia)}",
            40f,
            y.toFloat(),
            paintNormal
        )
        y += 16
        canvas.drawText(
            "Ticket promedio: $simboloMoneda ${String.format(Locale.US, "%.2f", promedio)}",
            40f,
            y.toFloat(),
            paintNormal
        )
        y += 24

        canvas.drawText("RESUMEN POR MÉTODO DE PAGO", 40f, y.toFloat(), paintSubTitle)
        y += 18
        canvas.drawText(
            "Efectivo: $simboloMoneda ${String.format(Locale.US, "%.2f", ventasEfectivo)}",
            40f,
            y.toFloat(),
            paintNormal
        )
        y += 16
        canvas.drawText(
            "Tarjeta: $simboloMoneda ${String.format(Locale.US, "%.2f", ventasTarjeta)}",
            40f,
            y.toFloat(),
            paintNormal
        )
        y += 16
        canvas.drawText(
            "Transferencia: $simboloMoneda ${String.format(Locale.US, "%.2f", ventasTransferencia)}",
            40f,
            y.toFloat(),
            paintNormal
        )
        y += 24

        canvas.drawLine(40f, y.toFloat(), 555f, y.toFloat(), paintLine)
        y += 20

        canvas.drawText("DETALLE DE VENTAS", 40f, y.toFloat(), paintSubTitle)
        y += 18

        canvas.drawText("ID", 40f, y.toFloat(), paintSubTitle)
        canvas.drawText("Hora", 160f, y.toFloat(), paintSubTitle)
        canvas.drawText("Método", 240f, y.toFloat(), paintSubTitle)
        canvas.drawText("Prod.", 360f, y.toFloat(), paintSubTitle)
        canvas.drawText("Total", 440f, y.toFloat(), paintSubTitle)
        y += 14

        canvas.drawLine(40f, y.toFloat(), 555f, y.toFloat(), paintLine)
        y += 18

        for (venta in listaActual) {
            canvas.drawText(venta.id.takeLast(8), 40f, y.toFloat(), paintSmall)
            canvas.drawText(venta.hora, 160f, y.toFloat(), paintSmall)
            canvas.drawText(
                venta.metodoPago.replaceFirstChar { it.uppercase() },
                240f,
                y.toFloat(),
                paintSmall
            )
            canvas.drawText(venta.cantidadProductos.toString(), 370f, y.toFloat(), paintSmall)
            canvas.drawText(
                "$simboloMoneda ${String.format(Locale.US, "%.2f", venta.total)}",
                440f,
                y.toFloat(),
                paintSmall
            )
            y += 18

            if (y > 760) break
        }

        y += 12
        canvas.drawLine(40f, y.toFloat(), 555f, y.toFloat(), paintLine)
        y += 20
        canvas.drawText(
            "Documento generado desde el módulo Historial de Ventas",
            40f,
            y.toFloat(),
            paintSmall
        )
        y += 14
        canvas.drawText("FARMADON", 40f, y.toFloat(), paintSmall)

        pdfDocument.finishPage(page)

        val carpeta = File(getExternalFilesDir(null), "reportes")
        if (!carpeta.exists()) {
            carpeta.mkdirs()
        }

        val archivo = File(carpeta, "reporte_ventas_$fechaActualConsulta.pdf")

        FileOutputStream(archivo).use { output ->
            pdfDocument.writeTo(output)
        }

        pdfDocument.close()
        return archivo
    }

    private fun generarTicketPdfProfesional(
        infoVenta: InfoVenta,
        productos: List<ProductoCaja>,
        lineasCambioVenta: List<String> = emptyList()
    ): File {
        val pdfDocument = PdfDocument()

        val pageWidth = 300
        val pageHeight = 1000
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paintTitle = Paint().apply {
            textSize = 15f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        val paintCenter = Paint().apply {
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }

        val paintBold = Paint().apply {
            textSize = 10f
            isFakeBoldText = true
        }

        val paintNormal = Paint().apply {
            textSize = 10f
        }

        val paintLine = Paint().apply {
            strokeWidth = 1f
        }

        var y = 28

        canvas.drawText("FARMADON", (pageWidth / 2).toFloat(), y.toFloat(), paintTitle)
        y += 16
        canvas.drawText("RUC: 12345678901", (pageWidth / 2).toFloat(), y.toFloat(), paintCenter)
        y += 14
        canvas.drawText("Dirección: Av. Principal 123", (pageWidth / 2).toFloat(), y.toFloat(), paintCenter)
        y += 14
        canvas.drawText("Teléfono: 999 999 999", (pageWidth / 2).toFloat(), y.toFloat(), paintCenter)
        y += 18

        canvas.drawLine(16f, y.toFloat(), (pageWidth - 16).toFloat(), y.toFloat(), paintLine)
        y += 16

        val tipoComprobanteTicket = when (infoVenta.tipoComprobante.trim().lowercase(Locale.getDefault())) {
            "factura" -> "FACTURA DE VENTA"
            "boleta", "boleta simple" -> "BOLETA DE VENTA"
            else -> "COMPROBANTE DE VENTA"
        }
        canvas.drawText(tipoComprobanteTicket, (pageWidth / 2).toFloat(), y.toFloat(), paintTitle)
        y += 18



        canvas.drawText("Nro Venta: ${infoVenta.id.ifEmpty { "Sin ID" }}", 16f, y.toFloat(), paintNormal)
        y += 14
        canvas.drawText("Fecha: ${infoVenta.fecha}", 16f, y.toFloat(), paintNormal)
        y += 14
        canvas.drawText("Hora: ${infoVenta.hora}", 16f, y.toFloat(), paintNormal)
        y += 14
        canvas.drawText("Pago: ${infoVenta.metodoPago}", 16f, y.toFloat(), paintNormal)
        y += 14

        if (lineasCambioVenta.isNotEmpty()) {
            canvas.drawLine(16f, y.toFloat(), (pageWidth - 16).toFloat(), y.toFloat(), paintLine)
            y += 14
            canvas.drawText("CAMBIO / DEVOLUCION", 16f, y.toFloat(), paintBold)
            y += 14
            lineasCambioVenta.forEach { linea ->
                partirTexto(linea, 36).forEach { parte ->
                    canvas.drawText(parte, 16f, y.toFloat(), paintNormal)
                    y += 12
                }
                y += 4
            }
        }

        canvas.drawLine(16f, y.toFloat(), (pageWidth - 16).toFloat(), y.toFloat(), paintLine)
        y += 14

        canvas.drawText("Producto", 16f, y.toFloat(), paintBold)
        canvas.drawText("Cant", 175f, y.toFloat(), paintBold)
        canvas.drawText("P.Unit", 205f, y.toFloat(), paintBold)
        canvas.drawText("Subt", 245f, y.toFloat(), paintBold)
        y += 12

        canvas.drawLine(16f, y.toFloat(), (pageWidth - 16).toFloat(), y.toFloat(), paintLine)
        y += 14


        for (producto in productos) {
            val nombreBase = producto.nombre.orEmpty().ifEmpty { "Producto" }
            val presentacion = producto.presentacion.orEmpty()

            val nombre = if (presentacion.isNotEmpty()) {
                "$nombreBase - $presentacion"
            } else {
                nombreBase
            }

            val cantidad = producto.cantidad.ifEmpty { "0" }
            val precio = producto.precioUnitario.ifEmpty { "0.00" }
            val subtotal = producto.total.ifEmpty { "0.00" }

            val lineasNombre = partirTexto(nombre, 24)

            for ((index, linea) in lineasNombre.withIndex()) {
                if (index == 0) {
                    canvas.drawText(linea, 16f, y.toFloat(), paintNormal)
                    canvas.drawText(cantidad, 180f, y.toFloat(), paintNormal)
                    canvas.drawText(precio, 208f, y.toFloat(), paintNormal)
                    canvas.drawText(subtotal, 248f, y.toFloat(), paintNormal)
                } else {
                    y += 12
                    canvas.drawText(linea, 16f, y.toFloat(), paintNormal)
                }
            }

            y += 18
        }

        canvas.drawLine(16f, y.toFloat(), (pageWidth - 16).toFloat(), y.toFloat(), paintLine)
        y += 16

        val total = infoVenta.total.ifEmpty { "0.00" }
        val recibido = infoVenta.montoRecibido.ifEmpty { "0.00" }
        val vuelto = infoVenta.vuelto.ifEmpty { "0.00" }

        canvas.drawText("TOTAL:", 16f, y.toFloat(), paintBold)
        canvas.drawText("$simboloMoneda $total", 210f, y.toFloat(), paintBold)
        y += 16

        canvas.drawText("RECIBIDO:", 16f, y.toFloat(), paintNormal)
        canvas.drawText("$simboloMoneda $recibido", 210f, y.toFloat(), paintNormal)
        y += 16

        canvas.drawText("VUELTO:", 16f, y.toFloat(), paintNormal)
        canvas.drawText("$simboloMoneda $vuelto", 210f, y.toFloat(), paintNormal)
        y += 20

        canvas.drawLine(16f, y.toFloat(), (pageWidth - 16).toFloat(), y.toFloat(), paintLine)
        y += 16

        canvas.drawText("Gracias por su compra", (pageWidth / 2).toFloat(), y.toFloat(), paintCenter)
        y += 14
        canvas.drawText("Conserve este comprobante", (pageWidth / 2).toFloat(), y.toFloat(), paintCenter)

        pdfDocument.finishPage(page)

        val carpeta = File(getExternalFilesDir(null), "tickets")
        if (!carpeta.exists()) {
            carpeta.mkdirs()
        }

        val nombreArchivo = "ticket_${infoVenta.id.ifEmpty { System.currentTimeMillis().toString() }}.pdf"
        val archivo = File(carpeta, nombreArchivo)

        FileOutputStream(archivo).use { output ->
            pdfDocument.writeTo(output)
        }

        pdfDocument.close()
        return archivo
    }

    private fun partirTexto(texto: String?, maxCaracteres: Int): List<String> {
        val textoSeguro = texto.orEmpty()

        if (textoSeguro.length <= maxCaracteres) return listOf(textoSeguro)

        val palabras = textoSeguro.split(" ")
        val lineas = mutableListOf<String>()
        var lineaActual = ""

        for (palabra in palabras) {
            val nuevaLinea = if (lineaActual.isEmpty()) {
                palabra
            } else {
                "$lineaActual $palabra"
            }

            if (nuevaLinea.length <= maxCaracteres) {
                lineaActual = nuevaLinea
            } else {
                if (lineaActual.isNotEmpty()) {
                    lineas.add(lineaActual)
                }
                lineaActual = palabra
            }
        }

        if (lineaActual.isNotEmpty()) {
            lineas.add(lineaActual)
        }

        return lineas
    }

    private fun abrirPdf(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, "Abrir PDF"))
    }

    fun atras(view: View) =finish()
}


class VentaAdapter(
    private var listaVentas: List<VentaHistorial>,
    private val onDetalleClick: (VentaHistorial) -> Unit
) : RecyclerView.Adapter<VentaAdapter.VentaViewHolder>() {

    class VentaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNumeroVenta: TextView = itemView.findViewById(R.id.tvNumeroVenta)
        val tvHoraVenta: TextView = itemView.findViewById(R.id.tvHoraVenta)
        val tvCantidadProductos: TextView = itemView.findViewById(R.id.tvCantidadProductos)
        val tvMontoPrincipal: TextView = itemView.findViewById(R.id.tvMontoPrincipal)
        val tvTotalLabel: TextView = itemView.findViewById(R.id.tvTotalLabel)
        val tvVerDetalle: TextView = itemView.findViewById(R.id.tvVerDetalle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_venta, parent, false)
        return VentaViewHolder(view)
    }

    override fun onBindViewHolder(holder: VentaViewHolder, position: Int) {
        val venta = listaVentas[position]

        holder.tvNumeroVenta.text = "Venta #${venta.id.takeLast(5)}"
        holder.tvHoraVenta.text = venta.hora
        holder.tvCantidadProductos.text =
            if (venta.cantidadProductos == 1) "1 producto"
            else "${venta.cantidadProductos} productos"

        val totalTexto = MonedaHelper.formatear(venta.total)
        holder.tvMontoPrincipal.text = totalTexto
        holder.tvTotalLabel.text = "Total: $totalTexto"

        holder.tvVerDetalle.setOnClickListener {
            onDetalleClick(venta)
        }

        holder.itemView.setOnClickListener {
            onDetalleClick(venta)
        }
    }

    override fun getItemCount(): Int = listaVentas.size

    fun actualizarLista(nuevaLista: List<VentaHistorial>) {
        listaVentas = nuevaLista
        notifyDataSetChanged()
    }


}

data class VentaHistorial(
    val id: String = "",
    val cantidadProductos: Int = 0,
    val total: Double = 0.0,
    val metodoPago: String = "",
    val estado: String = "",
    val fecha: String="",
    val hora: String=""
)

data class InfoVenta(
    val id: String = "",
    val fecha: String ="",
    val hora: String ="",
    val metodoPago: String = "",
    val tipoComprobante: String = "",
    val total: String = "0.00",
    val montoRecibido: String = "0.00",
    val vuelto: String = "0.00",
    val estado: String = ""
)
