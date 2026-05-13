package com.app.administradorfarmadon.ActivitysPerfilItem

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ActivitysPerfilItem.activity_detalle_reporte.Companion.EXTRA_CAJA_INICIAL
import com.app.administradorfarmadon.ActivitysPerfilItem.activity_detalle_reporte.Companion.EXTRA_DIFERENCIA
import com.app.administradorfarmadon.ActivitysPerfilItem.activity_detalle_reporte.Companion.EXTRA_EFECTIVO_REGISTRADO
import com.app.administradorfarmadon.ActivitysPerfilItem.activity_detalle_reporte.Companion.EXTRA_ESTADO_CUADRE
import com.app.administradorfarmadon.ActivitysPerfilItem.activity_detalle_reporte.Companion.EXTRA_ESTADO_TURNO
import com.app.administradorfarmadon.ActivitysPerfilItem.activity_detalle_reporte.Companion.EXTRA_FECHA_TURNO
import com.app.administradorfarmadon.ActivitysPerfilItem.activity_detalle_reporte.Companion.EXTRA_HORA_APERTURA
import com.app.administradorfarmadon.ActivitysPerfilItem.activity_detalle_reporte.Companion.EXTRA_HORA_CIERRE
import com.app.administradorfarmadon.ActivitysPerfilItem.activity_detalle_reporte.Companion.EXTRA_ID_CAJA
import com.app.administradorfarmadon.ActivitysPerfilItem.activity_detalle_reporte.Companion.EXTRA_ID_TURNO
import com.app.administradorfarmadon.ActivitysPerfilItem.activity_detalle_reporte.Companion.EXTRA_NOMBRE_CAJA
import com.app.administradorfarmadon.ActivitysPerfilItem.activity_detalle_reporte.Companion.EXTRA_NOMBRE_CAJERO
import com.app.administradorfarmadon.ActivitysPerfilItem.activity_detalle_reporte.Companion.EXTRA_REPORTE_CONFIRMADO
import com.app.administradorfarmadon.ActivitysPerfilItem.activity_detalle_reporte.Companion.EXTRA_TOTAL_EGRESOS
import com.app.administradorfarmadon.ActivitysPerfilItem.activity_detalle_reporte.Companion.EXTRA_TOTAL_DEVOLUCIONES
import com.app.administradorfarmadon.ActivitysPerfilItem.activity_detalle_reporte.Companion.EXTRA_TOTAL_ESPERADO
import com.app.administradorfarmadon.ActivitysPerfilItem.activity_detalle_reporte.Companion.EXTRA_TOTAL_VENTAS
import com.app.administradorfarmadon.ClasesDatabase.FechaHoraServidorHelper
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.ClasesDatabase.SessionManager
import com.app.administradorfarmadon.ClasesDatabase.obtenerDoubleFlexible
import com.app.administradorfarmadon.ClasesDatabase.obtenerLongFlexible
import com.app.administradorfarmadon.ClasesDatabase.obtenerTexto
import com.app.administradorfarmadon.R
import com.app.administradorfarmadon.databinding.ActivityListaReportesCajaBinding
import com.app.administradorfarmadon.databinding.ItemReporteDiaBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

class ListaReportesCaja : AppCompatActivity() {

    private var _binding: ActivityListaReportesCajaBinding? = null
    private val binding get() = _binding!!

    private var reportesPendientesAdapter: ReportesCajaAdapter? = null
    private var reportesConfirmadosAdapter: ReportesCajaAdapter? = null

    private var fechaSeleccionadaFirebase: String = ""
    private var fechaOficialHoyFirebase: String = ""
    private var filtroActual: FiltroTab = FiltroTab.PENDIENTES
    private var reportesDelDia: List<ReporteTurnoItem> = emptyList()

    private val esModoDual: Boolean by lazy {
        // En tablet (sw720dp) el rvPendientes existe, en móvil no.
        // View Binding lo genera como opcional si no está en todos los layouts.
        binding.root.findViewById<View>(R.id.rvPendientes) != null
    }

    enum class FiltroTab { PENDIENTES, CONFIRMADOS }

    data class ReporteTurnoItem(
        val idCaja: String,
        val nombreCaja: String,
        val idTurno: String,
        val nombreCajero: String,
        val descripcionTurno: String,
        val estadoTurno: String,
        val turnoCerrado: Boolean,
        val reporteConfirmado: Boolean,
        val estadoCuadre: String,
        val cajaInicial: Double,
        val numeroVentas: Long,
        val ventas: Double,
        val ventasEfectivo: Double,
        val totalEgresos: Double,
        val totalDevoluciones: Double,
        val esperado: Double,
        val registrado: Double?,
        val diferencia: Double?,
        val horaApertura: String,
        val horaCierre: String,
        val timestampOrden: Long
    ) {
        val confirmado: Boolean get() = reporteConfirmado
    }

    private val detalleReporteLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            cargarReportesPorFecha(fechaSeleccionadaFirebase)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        _binding = ActivityListaReportesCajaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        SessionManager.cargarSesion(this)
        configurarToolbar()
        configurarRecycler()
        configurarTabs()
        configurarSelectorFecha()

        cambiarFiltro(FiltroTab.PENDIENTES, refrescarLista = false)
        inicializarFechaOficial()
    }

    private fun configurarToolbar() {
        binding.toolbarReportes.setNavigationOnClickListener { finish() }
    }

    private fun configurarRecycler() {
        reportesPendientesAdapter = ReportesCajaAdapter { mostrarDetalleReporte(it) }
        reportesConfirmadosAdapter = ReportesCajaAdapter { mostrarDetalleReporte(it) }

        if (esModoDual) {
            // Configuración Tablet: Dos columnas fijas
            binding.root.findViewById<RecyclerView>(R.id.rvPendientes)?.apply {
                layoutManager = LinearLayoutManager(this@ListaReportesCaja)
                adapter = reportesPendientesAdapter
            }
        } else {
            // Configuración Móvil: Una columna que alterna
            binding.rvReportes.layoutManager = LinearLayoutManager(this)
            binding.rvReportes.adapter = reportesPendientesAdapter
        }

        binding.rvConfirmados.layoutManager = LinearLayoutManager(this)
        binding.rvConfirmados.adapter = reportesConfirmadosAdapter
    }

    private fun configurarTabs() {
        binding.tabPendientes.setOnClickListener { cambiarFiltro(FiltroTab.PENDIENTES) }
        binding.tabConfirmados.setOnClickListener { cambiarFiltro(FiltroTab.CONFIRMADOS) }
    }

    private fun configurarSelectorFecha() {
        binding.cardFecha.setOnClickListener { mostrarSelectorFecha() }
    }

    private fun inicializarFechaOficial() {
        FechaHoraServidorHelper.obtenerMomentoActual(
            onSuccess = { momento ->
                fechaOficialHoyFirebase = momento.fechaFirebase
                fechaSeleccionadaFirebase = momento.fechaFirebase
                actualizarFechaVisible(fechaSeleccionadaFirebase)
                cargarReportesPorFecha(fechaSeleccionadaFirebase)
            },
            onError = {
                val momento = FechaHoraServidorHelper.estimarMomentoActualDesdeCache()
                fechaOficialHoyFirebase = momento.fechaFirebase
                fechaSeleccionadaFirebase = momento.fechaFirebase
                actualizarFechaVisible(fechaSeleccionadaFirebase)
                cargarReportesPorFecha(fechaSeleccionadaFirebase)
            }
        )
    }

    private fun mostrarSelectorFecha() {
        val fechaBase = fechaSeleccionadaFirebase.ifBlank {
            fechaOficialHoyFirebase.ifBlank { FechaHoraServidorHelper.estimarMomentoActualDesdeCache().fechaFirebase }
        }
        val cal = parsearFechaFirebase(fechaBase)
            ?: FechaHoraServidorHelper.calendarDesdeTimestamp(
                FechaHoraServidorHelper.estimarMomentoActualDesdeCache().timestampServidorMs
            )
        val hoy = FechaHoraServidorHelper.calendarDesdeFechaFirebase(
            fechaOficialHoyFirebase.ifBlank { FechaHoraServidorHelper.estimarMomentoActualDesdeCache().fechaFirebase }
        ) ?: FechaHoraServidorHelper.calendarDesdeTimestamp(
            FechaHoraServidorHelper.estimarMomentoActualDesdeCache().timestampServidorMs
        )
        if (cal.after(hoy)) {
            cal.timeInMillis = hoy.timeInMillis
        }
        val dialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selected = FechaHoraServidorHelper.calendarDesdeTimestamp(hoy.timeInMillis).apply {
                set(year, month, dayOfMonth)
            }
            fechaSeleccionadaFirebase = obtenerFechaFirebase(selected)
            actualizarFechaVisible(fechaSeleccionadaFirebase)
            cargarReportesPorFecha(fechaSeleccionadaFirebase)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        dialog.datePicker.maxDate = hoy.timeInMillis
        dialog.show()
    }

    private fun cargarReportesPorFecha(fecha: String) {
        mostrarCargandoDia(true)
        FirebaseDatabase.getInstance().reference.child("CorteCaja").child("Cajeras")
            .get().addOnSuccessListener { snapshot ->
                if (isFinishing || _binding == null) return@addOnSuccessListener
                reportesDelDia = obtenerReportesDesdeSnapshot(snapshot, fecha)
                aplicarFiltroYMostrar()
                mostrarCargandoDia(false)
            }.addOnFailureListener {
                mostrarCargandoDia(false)
                Toast.makeText(this, "Error al cargar reportes", Toast.LENGTH_SHORT).show()
            }
    }

    private fun obtenerReportesDesdeSnapshot(snapshot: DataSnapshot, fecha: String): List<ReporteTurnoItem> {
        val lista = mutableListOf<ReporteTurnoItem>()
        for (cajaSnap in snapshot.children) {
            val idCaja = cajaSnap.key ?: continue
            val nombreCaja = cajaSnap.child("nombreCaja").getValue(String::class.java) ?: "Caja"
            val fechaSnap = cajaSnap.child(fecha)
            if (fechaSnap.exists()) {
                val turnosSnap = fechaSnap.child("turnos")
                for (turnoSnap in turnosSnap.children) {
                    val item = construirItemTurno(idCaja, nombreCaja, turnoSnap)
                    if (item != null) lista.add(item)
                }
            }
        }
        return lista.sortedByDescending { it.timestampOrden }
    }

    private fun construirItemTurno(idCaja: String, nombreCaja: String, snapshot: DataSnapshot): ReporteTurnoItem? {
        val reporteParseado = ReporteCierreRemoteRules.construirReporteTurno(
            ReporteTurnoRemoteData(
                idCaja = idCaja,
                nombreCaja = nombreCaja,
                idTurno = snapshot.key ?: return null,
                nombreCajero = resolverNombreCajeroTurno(snapshot),
                horaApertura = snapshot.child("horaAperturaLocal").obtenerTexto()
                    .ifBlank { snapshot.child("horaApertura").obtenerTexto() },
                horaCierre = snapshot.child("horaCierre").obtenerTexto(),
                timestampAperturaServidor = snapshot.child("timestampAperturaServidor").obtenerLongFlexible()
                    ?: snapshot.child("timestampApertura").obtenerLongFlexible()
                    ?: 0L,
                timestampCierreServidor = snapshot.child("timestampCierreServidor").obtenerLongFlexible()
                    ?: snapshot.child("timestampCierre").obtenerLongFlexible()
                    ?: 0L,
                estadoTurno = snapshot.child("estado").obtenerTexto(),
                reporteConfirmado = snapshot.child("confirmadoReporte").getValue(Boolean::class.java) == true,
                estadoCuadre = snapshot.child("estadoCuadre").obtenerTexto(),
                cajaInicial = snapshot.child("montoApertura").obtenerDoubleFlexible() ?: 0.0,
                ventas = snapshot.child("totalVentas").obtenerDoubleFlexible() ?: 0.0,
                ventasEfectivo = snapshot.child("ventasEfectivo").obtenerDoubleFlexible() ?: 0.0,
                totalEgresos = snapshot.child("totalEgresos").obtenerDoubleFlexible() ?: 0.0,
                totalDevoluciones = snapshot.child("totalDevoluciones").obtenerDoubleFlexible() ?: 0.0,
                esperado = snapshot.child("efectivoEsperado").obtenerDoubleFlexible() ?: 0.0,
                registrado = snapshot.child("efectivoReal").obtenerDoubleFlexible(),
                diferencia = snapshot.child("diferenciaEfectivo").obtenerDoubleFlexible(),
                timestampOrden = snapshot.child("timestampAperturaServidor").obtenerLongFlexible() 
                    ?: snapshot.child("timestampApertura").obtenerLongFlexible() 
                    ?: 0L
            )
        ) ?: return null

        return ReporteTurnoItem(
            idCaja = reporteParseado.idCaja,
            nombreCaja = reporteParseado.nombreCaja,
            idTurno = reporteParseado.idTurno,
            nombreCajero = reporteParseado.nombreCajero,
            descripcionTurno = reporteParseado.descripcionTurno,
            estadoTurno = reporteParseado.estadoTurno,
            turnoCerrado = reporteParseado.turnoCerrado,
            reporteConfirmado = reporteParseado.reporteConfirmado,
            estadoCuadre = reporteParseado.estadoCuadre,
            cajaInicial = reporteParseado.cajaInicial,
            numeroVentas = snapshot.child("numeroVentas").obtenerLongFlexible() ?: 0L,
            ventas = reporteParseado.ventas,
            ventasEfectivo = reporteParseado.ventasEfectivo,
            totalEgresos = reporteParseado.totalEgresos,
            totalDevoluciones = reporteParseado.totalDevoluciones,
            esperado = reporteParseado.esperado,
            registrado = reporteParseado.registrado,
            diferencia = reporteParseado.diferencia,
            horaApertura = reporteParseado.horaApertura,
            horaCierre = reporteParseado.horaCierre,
            timestampOrden = reporteParseado.timestampOrden
        )
    }

    private fun resolverNombreCajeroTurno(snapshot: DataSnapshot): String {
        val candidatos = listOf(
            snapshot.child("nombreCajero").obtenerTexto(),
            snapshot.child("nombreUsuarioCierre").obtenerTexto(),
            snapshot.child("nombreUsuarioApertura").obtenerTexto(),
            snapshot.child("nombreUsuario").obtenerTexto(),
            snapshot.child("usuario").obtenerTexto(),
            snapshot.child("infoTurno").child("nombreCajero").obtenerTexto(),
            snapshot.child("infoTurno").child("nombreUsuario").obtenerTexto()
        )

        return candidatos
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() }
            .orEmpty()
    }

    private fun cambiarFiltro(nuevo: FiltroTab, refrescarLista: Boolean = true) {
        filtroActual = nuevo
        actualizarEstadoVisualTabs()
        if (refrescarLista) aplicarFiltroYMostrar()
    }

    private fun actualizarEstadoVisualTabs() {
        val colorActive = Color.BLACK
        val colorInactive = Color.parseColor("#7B8190")
        
        binding.tabPendientes.setTextColor(if (filtroActual == FiltroTab.PENDIENTES) colorActive else colorInactive)
        binding.tabPendientes.setBackgroundResource(if (filtroActual == FiltroTab.PENDIENTES) R.drawable.bg_tab_selected else 0)
        binding.tabPendientes.setTypeface(null, if (filtroActual == FiltroTab.PENDIENTES) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)

        binding.tabConfirmados.setTextColor(if (filtroActual == FiltroTab.CONFIRMADOS) colorActive else colorInactive)
        binding.tabConfirmados.setBackgroundResource(if (filtroActual == FiltroTab.CONFIRMADOS) R.drawable.bg_tab_selected else 0)
        binding.tabConfirmados.setTypeface(null, if (filtroActual == FiltroTab.CONFIRMADOS) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
    }

    private fun aplicarFiltroYMostrar() {
        val estadoLista = ReporteCierreSummaryRules.construirEstadoLista(
            reportes = reportesDelDia,
            mostrarPendientes = filtroActual == FiltroTab.PENDIENTES,
            esModoDual = esModoDual
        )

        if (esModoDual) {
            binding.cardTabs.visibility = View.GONE
            binding.rvReportes.visibility = View.GONE
            binding.tvSinReportes.visibility = View.GONE

            val rvP = binding.root.findViewById<RecyclerView>(R.id.rvPendientes)
            val tvSP = binding.root.findViewById<View>(R.id.tvSinPendientes)
            rvP?.visibility = if (estadoLista.mostrarRvPendientesTablet) View.VISIBLE else View.GONE
            tvSP?.visibility = if (estadoLista.mostrarSinPendientesTablet) View.VISIBLE else View.GONE
            reportesPendientesAdapter?.actualizarLista(estadoLista.pendientes)

            val tvSC = binding.root.findViewById<View>(R.id.tvSinConfirmados)
            binding.rvConfirmados.visibility =
                if (estadoLista.mostrarRvConfirmadosTablet) View.VISIBLE else View.GONE
            tvSC?.visibility = if (estadoLista.mostrarSinConfirmadosTablet) View.VISIBLE else View.GONE
            reportesConfirmadosAdapter?.actualizarLista(estadoLista.confirmados)
        } else {
            if (filtroActual == FiltroTab.PENDIENTES) {
                binding.rvReportes.visibility = if (estadoLista.mostrarRvReportes) View.VISIBLE else View.GONE
                binding.rvConfirmados.visibility = View.GONE
                reportesPendientesAdapter?.actualizarLista(estadoLista.pendientes)
            } else {
                binding.rvReportes.visibility = View.GONE
                binding.rvConfirmados.visibility =
                    if (estadoLista.mostrarRvConfirmados) View.VISIBLE else View.GONE
                reportesConfirmadosAdapter?.actualizarLista(estadoLista.confirmados)
            }
            binding.tvSinReportes.visibility = if (estadoLista.mostrarSinReportes) View.VISIBLE else View.GONE
        }
    }

    private fun actualizarFechaVisible(fecha: String) {
        binding.tvFechaSeleccionada.text = formatearFechaVisible(fecha)
    }

    private fun mostrarCargandoDia(mostrar: Boolean) {
        binding.layoutLoadingReportes.visibility = if (mostrar) View.VISIBLE else View.GONE
        if (mostrar) {
            binding.rvReportes.visibility = View.GONE
            binding.rvConfirmados.visibility = View.GONE
            binding.tvSinReportes.visibility = View.GONE
            binding.root.findViewById<View>(R.id.rvPendientes)?.visibility = View.GONE
            binding.root.findViewById<View>(R.id.tvSinPendientes)?.visibility = View.GONE
            binding.root.findViewById<View>(R.id.tvSinConfirmados)?.visibility = View.GONE
        }
    }

    private fun mostrarDetalleReporte(item: ReporteTurnoItem) {
        val detalle = ReporteCierreDetailRules.construirDetalleData(
            fechaTurno = fechaSeleccionadaFirebase,
            item = item
        )
        val intent = Intent(this, activity_detalle_reporte::class.java).apply {
            putExtra(EXTRA_FECHA_TURNO, detalle.fechaTurno)
            putExtra(EXTRA_ID_CAJA, detalle.idCaja)
            putExtra(EXTRA_NOMBRE_CAJA, detalle.nombreCaja)
            putExtra(EXTRA_ID_TURNO, detalle.idTurno)
            putExtra(EXTRA_NOMBRE_CAJERO, detalle.nombreCajero)
            putExtra(EXTRA_ESTADO_TURNO, detalle.estadoTurno)
            putExtra(EXTRA_ESTADO_CUADRE, detalle.estadoCuadre)
            putExtra(EXTRA_CAJA_INICIAL, detalle.cajaInicial)
            putExtra(EXTRA_TOTAL_VENTAS, detalle.totalVentas)
            putExtra(EXTRA_TOTAL_EGRESOS, detalle.totalEgresos)
            putExtra(EXTRA_TOTAL_DEVOLUCIONES, detalle.totalDevoluciones)
            putExtra(EXTRA_TOTAL_ESPERADO, detalle.totalEsperado)
            putExtra(EXTRA_REPORTE_CONFIRMADO, detalle.reporteConfirmado)
            putExtra(EXTRA_HORA_APERTURA, detalle.horaApertura)
            putExtra(EXTRA_HORA_CIERRE, detalle.horaCierre)
            detalle.efectivoRegistrado?.let { putExtra(EXTRA_EFECTIVO_REGISTRADO, it) }
            detalle.diferencia?.let { putExtra(EXTRA_DIFERENCIA, it) }
        }
        detalleReporteLauncher.launch(intent)
    }

    private fun obtenerFechaFirebase(cal: Calendar): String =
        FechaHoraServidorHelper.formatearFechaFirebase(cal.timeInMillis)

    private fun formatearFechaVisible(fecha: String): String =
        FechaHoraServidorHelper.formatearFechaVisibleLarga(fecha)

    private fun parsearFechaFirebase(fecha: String): Calendar? =
        FechaHoraServidorHelper.calendarDesdeFechaFirebase(fecha)

    inner class ReportesCajaAdapter(private val onClickAccion: (ReporteTurnoItem) -> Unit) : RecyclerView.Adapter<ReportesCajaAdapter.ReporteViewHolder>() {
        private val items = mutableListOf<ReporteTurnoItem>()

        inner class ReporteViewHolder(val itemBinding: ItemReporteDiaBinding) : RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ReporteViewHolder(
            ItemReporteDiaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        override fun onBindViewHolder(holder: ReporteViewHolder, position: Int) {
            val item = items[position]
            
            with(holder.itemBinding) {
                val itemContext = root.context
                val esTurnoEnCurso = !item.turnoCerrado
                val nombreCajaLimpio = item.nombreCaja.trim()
                val nombreCajeroLimpio = item.nombreCajero.trim()
                tvCajaCajera.text = when {
                    nombreCajaLimpio.isBlank() && nombreCajeroLimpio.isBlank() -> "--"
                    nombreCajaLimpio.isBlank() -> nombreCajeroLimpio
                    nombreCajeroLimpio.isBlank() -> nombreCajaLimpio
                    nombreCajaLimpio.equals(nombreCajeroLimpio, ignoreCase = true) -> nombreCajaLimpio
                    else -> "$nombreCajaLimpio - $nombreCajeroLimpio"
                }
                tvTurno.text = item.descripcionTurno
                labelVentas.text = itemContext.getString(
                    if (esTurnoEnCurso) R.string.reportes_label_ventas_acumuladas
                    else R.string.reportes_label_ventas
                )
                labelEgresos.text = itemContext.getString(
                    if (esTurnoEnCurso) R.string.reportes_label_egresos_acumulados
                    else R.string.reportes_label_egresos
                )
                labelDevoluciones.text = itemContext.getString(
                    if (esTurnoEnCurso) R.string.reportes_label_devoluciones_acumuladas
                    else R.string.reportes_label_devoluciones
                )
                tvInicio.text = MonedaHelper.formatear(item.cajaInicial)
                tvCantidadVentas.text = item.numeroVentas.toString()
                tvVentas.text = MonedaHelper.formatear(item.ventas)
                tvEgresos.text = if (item.totalEgresos > 0.009)
                    MonedaHelper.formatearConSigno(-item.totalEgresos)
                else MonedaHelper.formatear(0.0)
                tvEgresos.setTextColor(Color.parseColor(if (item.totalEgresos > 0.009) "#A63A2F" else "#1F2937"))
                tvDevoluciones.text = if (item.totalDevoluciones > 0.009)
                    MonedaHelper.formatearConSigno(-item.totalDevoluciones)
                else MonedaHelper.formatear(0.0)
                tvDevoluciones.setTextColor(Color.parseColor(if (item.totalDevoluciones > 0.009) "#A63A2F" else "#1F2937"))

                val resultadoNeto = item.ventas - item.totalEgresos - item.totalDevoluciones
                configurarLayoutResultado(this, resultadoNeto, esTurnoEnCurso)

                if (item.confirmado) {
                    configurarEstadoConfirmado(this, item)
                } else if (esTurnoEnCurso) {
                    configurarEstadoEnCurso(this)
                } else {
                    configurarEstadoPendiente(this, item)
                }

                root.setOnClickListener { onClickAccion(item) }
                btnAccionReporte.setOnClickListener { onClickAccion(item) }
            }
        }

        private fun configurarLayoutResultado(
            binding: ItemReporteDiaBinding,
            resultadoNeto: Double,
            esTurnoEnCurso: Boolean
        ) {
            val context = binding.root.context
            val visual = ReporteCierreSummaryRules.construirResultadoVisual(
                resultadoNeto = resultadoNeto,
                textoGanancia = context.getString(
                    if (esTurnoEnCurso) R.string.reportes_label_resultado_parcial
                    else R.string.reportes_label_ganancia
                ),
                textoPerdida = context.getString(
                    if (esTurnoEnCurso) R.string.reportes_label_resultado_parcial
                    else R.string.reportes_label_perdida
                ),
                textoNeutro = context.getString(
                    if (esTurnoEnCurso) R.string.reportes_label_resultado_parcial
                    else R.string.reportes_label_neutro
                ),
                montoFormateado = MonedaHelper.formatear(resultadoNeto),
                montoConSignoFormateado = MonedaHelper.formatearConSigno(resultadoNeto),
                montoCeroFormateado = MonedaHelper.formatear(0.0)
            )
            val color = Color.parseColor(visual.colorHex)
            binding.layoutResultadoGanancia.background =
                ContextCompat.getDrawable(context, visual.backgroundResId)
            binding.tvResultadoGananciaLabel.text = visual.label
            binding.tvResultadoGananciaLabel.setTextColor(color)
            binding.tvResultadoGananciaMonto.setTextColor(color)
            binding.tvResultadoGananciaMonto.text = visual.montoTexto
        }

        private fun configurarEstadoEnCurso(binding: ItemReporteDiaBinding) {
            val context = binding.root.context
            val visual = ReporteCierreSummaryRules.construirEstadoEnCursoVisual(
                textoBadgeEnCurso = context.getString(R.string.reportes_estado_en_curso),
                textoPendienteCierre = context.getString(R.string.reportes_estado_pendiente_cierre),
                textoBotonVerAvance = context.getString(R.string.reportes_btn_ver_avance)
            )
            binding.tvEstadoBadge.text = visual.badgeText
            binding.tvEstadoBadge.background =
                ContextCompat.getDrawable(context, visual.badgeBackgroundResId)
            binding.tvEstadoBadge.setTextColor(Color.parseColor(visual.badgeColorHex))
            binding.ivEstado.setImageResource(visual.iconResId)
            binding.ivEstado.setColorFilter(Color.parseColor(visual.iconColorHex))
            binding.tvEstadoDetalle.text = visual.detalleText
            binding.tvEstadoDetalle.setTextColor(Color.parseColor(visual.detalleColorHex))
            binding.btnAccionReporte.text = visual.botonText
            binding.btnAccionReporte.isEnabled = true
            binding.btnAccionReporte.alpha = 1f
            binding.root.alpha = 0.9f
            binding.root.strokeColor = "#D9DEE7".toColorInt()
            binding.root.setCardBackgroundColor("#FBFCFE".toColorInt())
            binding.btnAccionReporte.backgroundTintList =
                ColorStateList.valueOf("#EEF2F7".toColorInt())
            binding.btnAccionReporte.setTextColor("#4B5563".toColorInt())
            binding.btnAccionReporte.strokeColor =
                ColorStateList.valueOf("#D9DEE7".toColorInt())
            binding.btnAccionReporte.strokeWidth =
                context.resources.displayMetrics.density.times(1).toInt().coerceAtLeast(1)
        }

        private fun configurarEstadoConfirmado(binding: ItemReporteDiaBinding, item: ReporteTurnoItem) {
            val context = binding.root.context
            val visual = ReporteCierreSummaryRules.construirEstadoConfirmadoVisual(
                estadoCuadre = item.estadoCuadre,
                diferenciaAbsolutaFormateada = MonedaHelper.formatear(abs(item.diferencia ?: 0.0)),
                textoBadgeConfirmado = context.getString(R.string.reportes_estado_confirmado),
                textoBotonVerReporte = context.getString(R.string.reportes_btn_ver_reporte),
                textoCuadreExacto = context.getString(R.string.reportes_estado_cuadre_exacto),
                textoFaltante = context.getString(R.string.reportes_estado_faltante, "%s"),
                textoSobrante = context.getString(R.string.reportes_estado_sobrante, "%s"),
                textoCierreConfirmado = context.getString(R.string.reportes_estado_cierre_confirmado)
            )
            binding.tvEstadoBadge.text = visual.badgeText
            binding.tvEstadoBadge.background =
                ContextCompat.getDrawable(context, visual.badgeBackgroundResId)
            binding.tvEstadoBadge.setTextColor(Color.parseColor(visual.badgeColorHex))
            binding.ivEstado.setImageResource(visual.iconResId)
            binding.ivEstado.setColorFilter(Color.parseColor(visual.iconColorHex))
            binding.btnAccionReporte.text = visual.botonText
            binding.btnAccionReporte.isEnabled = visual.botonEnabled
            binding.btnAccionReporte.alpha = visual.botonAlpha
            binding.tvEstadoDetalle.text = visual.detalleText
            binding.tvEstadoDetalle.setTextColor(Color.parseColor(visual.detalleColorHex))
            binding.root.alpha = 1f
            binding.root.strokeColor = "#E7EAF1".toColorInt()
            binding.root.setCardBackgroundColor(Color.WHITE)
            binding.btnAccionReporte.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
            binding.btnAccionReporte.setTextColor(Color.WHITE)
            binding.btnAccionReporte.strokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
            binding.btnAccionReporte.strokeWidth = 0
        }

        private fun configurarEstadoPendiente(binding: ItemReporteDiaBinding, item: ReporteTurnoItem) {
            val context = binding.root.context
            val visual = ReporteCierreSummaryRules.construirEstadoPendienteVisual(
                turnoCerrado = item.turnoCerrado,
                textoBadgePendiente = context.getString(R.string.reportes_estado_pendiente),
                textoPendienteConfirmacion = context.getString(R.string.reportes_estado_pendiente_confirmacion),
                textoPendienteCierre = context.getString(R.string.reportes_estado_pendiente_cierre),
                textoBotonRevisarCierre = context.getString(R.string.reportes_btn_revisar)
            )
            binding.tvEstadoBadge.text = visual.badgeText
            binding.tvEstadoBadge.background =
                ContextCompat.getDrawable(context, visual.badgeBackgroundResId)
            binding.tvEstadoBadge.setTextColor(Color.parseColor(visual.badgeColorHex))
            binding.ivEstado.setImageResource(visual.iconResId)
            binding.ivEstado.setColorFilter(Color.parseColor(visual.iconColorHex))
            binding.tvEstadoDetalle.text = visual.detalleText
            binding.tvEstadoDetalle.setTextColor(Color.parseColor(visual.detalleColorHex))
            binding.btnAccionReporte.text = visual.botonText
            binding.btnAccionReporte.isEnabled = true
            binding.btnAccionReporte.alpha = visual.botonAlpha
            binding.root.alpha = 1f
            binding.root.strokeColor = "#E7EAF1".toColorInt()
            binding.root.setCardBackgroundColor(Color.WHITE)
            binding.btnAccionReporte.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
            binding.btnAccionReporte.setTextColor(Color.WHITE)
            binding.btnAccionReporte.strokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
            binding.btnAccionReporte.strokeWidth = 0
        }

        override fun getItemCount() = items.size
        fun actualizarLista(nuevos: List<ReporteTurnoItem>) {
            items.clear(); items.addAll(nuevos); notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
