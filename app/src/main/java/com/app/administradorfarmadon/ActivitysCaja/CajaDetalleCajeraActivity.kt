package com.app.administradorfarmadon.ActivitysCaja

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ActivityFragmentos.Reciclers.AdapterDiasCierreDashboard
import com.app.administradorfarmadon.ActivityFragmentos.Reciclers.AdapterDiasCierreDashboard.DiaCierreUi
import com.app.administradorfarmadon.ActivitysCaja.AdapterVentasDetalleCajera.VentaResumenUi
import com.app.administradorfarmadon.ClasesDatabase.FechaHoraServidorHelper
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Detalle de la caja de UNA cajera específica.
 *
 * Flujo: chips de días con datos (últimos 7) → KPIs (Ventas, Egresos, Devoluciones, Total)
 * → Buscador por monto o ID → Lista de ventas del día → Tap = DetalleVentaActivity.
 *
 * Diseño: una sola pantalla, scroll vertical, sin sub-pantallas internas.
 */
class CajaDetalleCajeraActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_CAJERA = "extra_id_cajera"
        const val EXTRA_NOMBRE_CAJERA = "extra_nombre_cajera"
        private const val DIAS_VENTANA_ACTIVA = 7
    }

    private var idCajera: String = ""
    private var nombreCajera: String = ""

    // Vistas
    private lateinit var rvDias: RecyclerView
    private lateinit var rvVentas: RecyclerView
    private lateinit var etBuscar: TextInputEditText
    private lateinit var layoutVacio: LinearLayout
    private lateinit var tvVacioTitulo: TextView
    private lateinit var tvVacioDetalle: TextView
    private lateinit var progress: ProgressBar
    private lateinit var tvNombre: TextView
    private lateinit var tvSubtitulo: TextView
    private lateinit var tvContador: TextView

    private lateinit var tvKpiVentasMonto: TextView
    private lateinit var tvKpiVentasConteo: TextView
    private lateinit var tvKpiEgresosMonto: TextView
    private lateinit var tvKpiEgresosConteo: TextView
    private lateinit var tvKpiDevolucionesMonto: TextView
    private lateinit var tvKpiDevolucionesConteo: TextView
    private lateinit var tvTotalNeto: TextView

    // Adapters / datos
    private lateinit var adapterDias: AdapterDiasCierreDashboard
    private lateinit var adapterVentas: AdapterVentasDetalleCajera
    private val listaDias = mutableListOf<DiaCierreUi>()
    private val ventasVisibles = mutableListOf<VentaResumenUi>()

    // Cache: fecha -> lista de ventas; fecha -> egresos; fecha -> devoluciones
    private val ventasPorDia = linkedMapOf<String, MutableList<VentaResumenUi>>()
    private val egresosPorDia = mutableMapOf<String, Pair<Double, Int>>()  // fecha -> (monto, conteo)
    private val devolucionesPorDia = mutableMapOf<String, Pair<Double, Int>>()

    private var diaSeleccionado: String = ""
    private var fechaOficialHoyFirebase: String =
        FechaHoraServidorHelper.estimarMomentoActualDesdeCache().fechaFirebase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_caja_detalle_cajera)

        idCajera = intent.getStringExtra(EXTRA_ID_CAJERA).orEmpty()
        nombreCajera = intent.getStringExtra(EXTRA_NOMBRE_CAJERA).orEmpty()

        if (idCajera.isBlank()) {
            finish()
            return
        }

        bindViews()
        configurarAdapters()
        configurarBuscador()

        inicializarFechaOficialYCargarTodo()
    }

    private fun inicializarFechaOficialYCargarTodo() {
        FechaHoraServidorHelper.obtenerMomentoActual(
            onSuccess = { momento ->
                fechaOficialHoyFirebase = momento.fechaFirebase
                cargarTodo()
            },
            onError = {
                fechaOficialHoyFirebase =
                    FechaHoraServidorHelper.estimarMomentoActualDesdeCache().fechaFirebase
                cargarTodo()
            }
        )
    }

    private fun bindViews() {
        val toolbar = findViewById<Toolbar>(R.id.toolbarDetalleCajera)
        toolbar.setNavigationOnClickListener { finish() }

        tvNombre = findViewById(R.id.tvNombreCajeraDetalle)
        tvSubtitulo = findViewById(R.id.tvSubtituloDetalleCajera)
        tvNombre.text = nombreCajera.ifBlank { "Cajera" }

        rvDias = findViewById(R.id.rvDiasDetalleCajera)
        rvVentas = findViewById(R.id.rvVentasDetalle)
        etBuscar = findViewById(R.id.etBuscarVenta)
        layoutVacio = findViewById(R.id.layoutVacioVentas)
        tvVacioTitulo = findViewById(R.id.tvVacioVentasTitulo)
        tvVacioDetalle = findViewById(R.id.tvVacioVentasDetalle)
        progress = findViewById(R.id.progressDetalleCajera)
        tvContador = findViewById(R.id.tvContadorVentas)

        tvKpiVentasMonto = findViewById(R.id.tvKpiVentasMonto)
        tvKpiVentasConteo = findViewById(R.id.tvKpiVentasConteo)
        tvKpiEgresosMonto = findViewById(R.id.tvKpiEgresosMonto)
        tvKpiEgresosConteo = findViewById(R.id.tvKpiEgresosConteo)
        tvKpiDevolucionesMonto = findViewById(R.id.tvKpiDevolucionesMonto)
        tvKpiDevolucionesConteo = findViewById(R.id.tvKpiDevolucionesConteo)
        tvTotalNeto = findViewById(R.id.tvTotalNetoDetalle)
    }

    private fun configurarAdapters() {
        adapterDias = AdapterDiasCierreDashboard(listaDias) { dia ->
            diaSeleccionado = dia.fechaIso
            renderizarDiaSeleccionado()
        }
        rvDias.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvDias.adapter = adapterDias

        adapterVentas = AdapterVentasDetalleCajera(ventasVisibles) { venta ->
            abrirDetalleVenta(venta)
        }
        rvVentas.layoutManager = LinearLayoutManager(this)
        rvVentas.adapter = adapterVentas
    }

    private fun configurarBuscador() {
        etBuscar.doAfterTextChanged { editable ->
            aplicarBusqueda(editable?.toString().orEmpty())
        }
    }

    private fun abrirDetalleVenta(venta: VentaResumenUi) {
        val intent = Intent(this, DetalleVentaActivity::class.java).apply {
            putExtra(DetalleVentaActivity.EXTRA_FECHA_VENTA, venta.fecha)
            putExtra(DetalleVentaActivity.EXTRA_ID_VENTA, venta.idVenta)
        }
        startActivity(intent)
    }

    // ----------------- CARGA DE DATOS -----------------

    private fun cargarTodo() {
        progress.visibility = View.VISIBLE
        rvVentas.visibility = View.GONE
        layoutVacio.visibility = View.GONE

        val hoy = fechaOficialHoyFirebase
        val limite = (FechaHoraServidorHelper.calendarDesdeFechaFirebase(hoy)
            ?: FechaHoraServidorHelper.calendarDesdeTimestamp(
                FechaHoraServidorHelper.estimarMomentoActualDesdeCache().timestampServidorMs
            )).apply {
            add(Calendar.DAY_OF_YEAR, -DIAS_VENTANA_ACTIVA)
        }.time

        ventasPorDia.clear()
        egresosPorDia.clear()
        devolucionesPorDia.clear()

        val db = FirebaseDatabase.getInstance()

        // 1. Cargar ventas de la cajera
        db.getReference("VentasPorCajera").child(idCajera).get()
            .addOnSuccessListener { snapVentas ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener
                construirVentasPorDia(snapVentas, limite)

                // 2. Cargar egresos desde CorteCaja/Cajeras/<idCajera>
                db.getReference("CorteCaja").child("Cajeras").child(idCajera).get()
                    .addOnSuccessListener { snapCorte ->
                        if (isFinishing || isDestroyed) return@addOnSuccessListener
                        construirEgresosPorDia(snapCorte, limite)
                        terminarCarga(hoy)
                    }
                    .addOnFailureListener {
                        if (isFinishing || isDestroyed) return@addOnFailureListener
                        terminarCarga(hoy)
                    }
            }
            .addOnFailureListener {
                if (isFinishing || isDestroyed) return@addOnFailureListener
                terminarCarga(hoy)
            }
    }

    private fun construirVentasPorDia(
        snapVentas: DataSnapshot,
        limite: Date
    ) {
        if (!snapVentas.exists()) return
        for (fechaSnap in snapVentas.children) {
            val fechaStr = fechaSnap.key.orEmpty()
            val fechaParsed = FechaHoraServidorHelper.calendarDesdeFechaFirebase(fechaStr)?.time ?: continue
            if (fechaParsed.before(limite)) continue

            val listaDia = mutableListOf<VentaResumenUi>()
            var totalDevolucion = 0.0
            var conteoDevolucion = 0

            for (ventaSnap in fechaSnap.children) {
                val info = ventaSnap.child("infoVenta")
                val totalRaw = info.child("total").value ?: ventaSnap.child("total").value
                val total = HistorialVentasRemoteRules.parsearMonto(totalRaw) ?: 0.0
                val hora = info.child("hora").getValue(String::class.java).orEmpty()
                    .ifBlank { ventaSnap.child("hora").getValue(String::class.java).orEmpty() }
                val metodoPago = info.child("metodoPago").getValue(String::class.java).orEmpty()
                    .ifBlank { ventaSnap.child("metodoPago").getValue(String::class.java).orEmpty() }

                val productos = ventaSnap.child("productos")
                var unidades = 0
                for (p in productos.children) {
                    unidades += HistorialVentasRemoteRules.parsearCantidad(p.child("cantidad").value)
                }

                val tieneDev = ventaSnap.child("devolucion").exists()
                if (tieneDev) {
                    conteoDevolucion++
                    totalDevolucion += extraerMontoDevolucion(ventaSnap.child("devolucion"))
                }

                listaDia.add(
                    VentaResumenUi(
                        idVenta = ventaSnap.key.orEmpty(),
                        fecha = fechaStr,
                        hora = hora,
                        total = total,
                        cantidadProductos = unidades,
                        metodoPago = metodoPago,
                        tieneDevolucion = tieneDev
                    )
                )
            }

            // Ordenar ventas por hora descendente (más reciente primero) usando texto
            listaDia.sortByDescending { it.hora }
            ventasPorDia[fechaStr] = listaDia
            if (conteoDevolucion > 0) {
                devolucionesPorDia[fechaStr] = totalDevolucion to conteoDevolucion
            }
        }
    }

    private fun extraerMontoDevolucion(devolucionSnap: DataSnapshot): Double {
        if (!devolucionSnap.exists()) return 0.0
        // Sumamos montos de eventos si existen, si no tomamos un campo único
        val eventosSnap = devolucionSnap.child("eventos")
        if (eventosSnap.exists()) {
            var total = 0.0
            for (ev in eventosSnap.children) {
                val raw = ev.child("monto").value ?: ev.child("montoDevuelto").value
                total += HistorialVentasRemoteRules.parsearMonto(raw) ?: 0.0
            }
            return total
        }
        val rawSimple = devolucionSnap.child("monto").value
            ?: devolucionSnap.child("montoDevuelto").value
            ?: devolucionSnap.child("montoPagoDiferenciaUltimo").value
        return HistorialVentasRemoteRules.parsearMonto(rawSimple) ?: 0.0
    }

    private fun construirEgresosPorDia(
        snapCorte: DataSnapshot,
        limite: Date
    ) {
        if (!snapCorte.exists()) return
        for (fechaSnap in snapCorte.children) {
            val fechaStr = fechaSnap.key.orEmpty()
            val fechaParsed = FechaHoraServidorHelper.calendarDesdeFechaFirebase(fechaStr)?.time ?: continue
            if (fechaParsed.before(limite)) continue

            val turnos = fechaSnap.child("turnos")
            if (!turnos.exists()) continue

            var totalEgresosDia = 0.0
            var conteoEgresos = 0
            for (turnoSnap in turnos.children) {
                // El campo agregado del turno
                val totalRaw = turnoSnap.child("totalEgresos").value
                val totalTurno = HistorialVentasRemoteRules.parsearMonto(totalRaw) ?: 0.0
                if (totalTurno > 0.0) {
                    totalEgresosDia += totalTurno
                }
                // Conteo de egresos individuales si están detallados
                val egresosNode = turnoSnap.child("egresos")
                if (egresosNode.exists()) {
                    for (e in egresosNode.children) conteoEgresos++
                }
            }
            if (totalEgresosDia > 0.0 || conteoEgresos > 0) {
                egresosPorDia[fechaStr] = totalEgresosDia to conteoEgresos
            }
        }
    }

    private fun terminarCarga(hoy: String) {
        progress.visibility = View.GONE

        // Días disponibles: union de los que tienen ventas/egresos/devoluciones.
        // Garantizamos siempre que HOY aparezca como chip aunque no haya datos —
        // así al abrir la pantalla se ve la "caja de hoy" en cero, no la de un día pasado.
        val claves = (ventasPorDia.keys + egresosPorDia.keys + devolucionesPorDia.keys).toMutableSet()
        claves.add(hoy)

        val diasOrdenados = claves
            .mapNotNull { fechaStr ->
                val parsed = FechaHoraServidorHelper.calendarDesdeFechaFirebase(fechaStr)?.time
                if (parsed != null) fechaStr to parsed else null
            }
            .sortedByDescending { it.second.time }

        // Por defecto SIEMPRE entramos viendo HOY (aunque tenga 0 ventas).
        // Si el usuario quiere revisar otro día, toca un chip a la izquierda.
        diaSeleccionado = hoy

        listaDias.clear()
        for ((fechaStr, fechaParsed) in diasOrdenados) {
            val ventasCount = ventasPorDia[fechaStr]?.size ?: 0
            listaDias.add(
                DiaCierreUi(
                    fechaIso = fechaStr,
                    etiqueta = etiquetaCortaDeDia(fechaParsed, hoy),
                    conteo = ventasCount,
                    seleccionado = (fechaStr == diaSeleccionado)
                )
            )
        }
        adapterDias.notifyDataSetChanged()
        rvDias.visibility = if (listaDias.size > 1) View.VISIBLE else View.GONE

        renderizarDiaSeleccionado()
    }

    // ----------------- RENDER POR DÍA -----------------

    private fun renderizarDiaSeleccionado() {
        val ventasDia = ventasPorDia[diaSeleccionado].orEmpty()
        val totalVentas = ventasDia.sumOf { it.total }
        val (montoEgresos, conteoEgresos) = egresosPorDia[diaSeleccionado] ?: (0.0 to 0)
        val (montoDevoluciones, conteoDevoluciones) =
            devolucionesPorDia[diaSeleccionado] ?: (0.0 to 0)
        val totalNeto = (totalVentas - montoEgresos - montoDevoluciones).coerceAtLeast(0.0)

        tvKpiVentasMonto.text = MonedaHelper.formatear(totalVentas)
        tvKpiVentasConteo.text = textoConteo(ventasDia.size, "ticket", "tickets")
        tvKpiEgresosMonto.text = MonedaHelper.formatear(montoEgresos)
        tvKpiEgresosConteo.text = textoConteo(conteoEgresos, "retiro", "retiros")
        tvKpiDevolucionesMonto.text = MonedaHelper.formatear(montoDevoluciones)
        tvKpiDevolucionesConteo.text = textoConteo(conteoDevoluciones, "caso", "casos")
        tvTotalNeto.text = MonedaHelper.formatear(totalNeto)

        tvSubtitulo.text = etiquetaSubtituloPorFecha(diaSeleccionado)
        aplicarBusqueda(etBuscar.text?.toString().orEmpty())
    }

    private fun aplicarBusqueda(texto: String) {
        val q = texto.trim().lowercase(Locale.getDefault())
        val ventasDia = ventasPorDia[diaSeleccionado].orEmpty()

        ventasVisibles.clear()
        if (q.isBlank()) {
            ventasVisibles.addAll(ventasDia)
        } else {
            for (v in ventasDia) {
                val totalStr = String.format(Locale.US, "%.2f", v.total)
                if (
                    v.idVenta.lowercase(Locale.getDefault()).contains(q) ||
                    totalStr.contains(q) ||
                    v.hora.lowercase(Locale.getDefault()).contains(q)
                ) {
                    ventasVisibles.add(v)
                }
            }
        }
        adapterVentas.notifyDataSetChanged()

        if (ventasVisibles.isEmpty()) {
            rvVentas.visibility = View.GONE
            layoutVacio.visibility = View.VISIBLE
            if (q.isBlank() && ventasDia.isEmpty()) {
                val hoyStr = fechaOficialHoyFirebase
                if (diaSeleccionado == hoyStr) {
                    tvVacioTitulo.text = "Sin ventas todavía hoy"
                    tvVacioDetalle.text = "Las ventas que se registren hoy aparecerán acá."
                } else {
                    tvVacioTitulo.text = "Sin ventas en este día"
                    tvVacioDetalle.text = "No se registraron ventas en la fecha seleccionada."
                }
            } else {
                tvVacioTitulo.text = "Sin coincidencias"
                tvVacioDetalle.text = "Probá con otro monto o ID."
            }
        } else {
            rvVentas.visibility = View.VISIBLE
            layoutVacio.visibility = View.GONE
        }

        tvContador.text = when (ventasVisibles.size) {
            0 -> ""
            1 -> "1 venta"
            else -> "${ventasVisibles.size} ventas"
        }
    }

    // ----------------- HELPERS -----------------

    private fun textoConteo(n: Int, singular: String, plural: String): String {
        return when (n) {
            0 -> "Sin $plural"
            1 -> "1 $singular"
            else -> "$n $plural"
        }
    }

    private fun etiquetaCortaDeDia(
        fecha: Date,
        hoyStr: String
    ): String {
        val fechaStr = FechaHoraServidorHelper.formatearFechaFirebase(fecha.time)
        if (fechaStr == hoyStr) return "Hoy"
        val ayerStr = FechaHoraServidorHelper.formatearFechaFirebase(
            (FechaHoraServidorHelper.calendarDesdeFechaFirebase(hoyStr)
                ?: FechaHoraServidorHelper.calendarDesdeTimestamp(
                    FechaHoraServidorHelper.estimarMomentoActualDesdeCache().timestampServidorMs
                )).apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }.timeInMillis
        )
        if (fechaStr == ayerStr) return "Ayer"
        return FechaHoraServidorHelper.formatearFechaVisible(fecha.time)
    }

    private fun etiquetaSubtituloPorFecha(fechaIso: String): String {
        if (fechaIso.isBlank()) return "Sin actividad reciente"
        val parser = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fecha = runCatching { parser.parse(fechaIso) }.getOrNull() ?: return "Resumen del día"
        val hoyStr = parser.format(Date())
        val ayerStr = parser.format(
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
        )
        return when (fechaIso) {
            hoyStr -> "Hoy"
            ayerStr -> "Ayer"
            else -> {
                val l = Locale.forLanguageTag("es-ES")
                java.text.SimpleDateFormat("EEEE d 'de' MMMM", l).format(fecha)
                    .replaceFirstChar { it.uppercaseChar() }
            }
        }
    }

    @Suppress("unused")
    private fun absSafe(d: Double) = abs(d)
}
