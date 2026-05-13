package com.app.administradorfarmadon.ActivitysPerfilItem

import android.content.Intent
import android.os.Bundle
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.ClasesDatabase.MovimientoLogger
import com.app.administradorfarmadon.ClasesDatabase.SessionManager
import com.app.administradorfarmadon.ActivityFragmentos.Fragmentos.CajaTurnoHelper
import com.app.administradorfarmadon.ActivityFragmentos.Fragmentos.DevolucionCierreLinea
import com.app.administradorfarmadon.ActivitysCaja.ActivityHistorialVentasCaja
import android.view.WindowManager
import com.app.administradorfarmadon.ClasesDatabase.obtenerDoubleFlexible
import com.app.administradorfarmadon.ClasesDatabase.obtenerLongFlexible
import com.app.administradorfarmadon.ClasesDatabase.obtenerTexto
import com.app.administradorfarmadon.R
import com.app.administradorfarmadon.databinding.ActivityDetalleReporteBinding
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

class activity_detalle_reporte : AppCompatActivity() {

    companion object {
        const val EXTRA_FECHA_TURNO = "extra_fecha_turno"
        const val EXTRA_ID_CAJA = "extra_id_caja"
        const val EXTRA_NOMBRE_CAJA = "extra_nombre_caja"
        const val EXTRA_ID_TURNO = "extra_id_turno"
        const val EXTRA_NOMBRE_CAJERO = "extra_nombre_cajero"
        const val EXTRA_ESTADO_TURNO = "extra_estado_turno"
        const val EXTRA_ESTADO_CUADRE = "extra_estado_cuadre"
        const val EXTRA_CAJA_INICIAL = "extra_caja_inicial"
        const val EXTRA_TOTAL_VENTAS = "extra_total_ventas"
        const val EXTRA_TOTAL_EGRESOS = "extra_total_egresos"
        const val EXTRA_TOTAL_DEVOLUCIONES = "extra_total_devoluciones"
        const val EXTRA_TOTAL_ESPERADO = "extra_total_esperado"
        const val EXTRA_EFECTIVO_REGISTRADO = "extra_efectivo_registrado"
        const val EXTRA_DIFERENCIA = "extra_diferencia"
        const val EXTRA_HORA_APERTURA = "extra_hora_apertura"
        const val EXTRA_HORA_CIERRE = "extra_hora_cierre"
        const val EXTRA_REPORTE_CONFIRMADO = "extra_reporte_confirmado"
    }

    private var _binding: ActivityDetalleReporteBinding? = null
    private val binding get() = _binding!!

    private data class DetalleReporteArgs(
        val fechaTurno: String,
        val idCaja: String,
        val nombreCaja: String,
        val idTurno: String,
        val nombreCajero: String,
        val estadoTurno: String,
        val estadoCuadre: String,
        val cajaInicial: Double,
        val totalVentas: Double,
        val totalEgresos: Double,
        val totalDevoluciones: Double,
        val totalEsperado: Double,
        val efectivoRegistrado: Double?,
        val diferencia: Double?,
        val horaApertura: String,
        val horaCierre: String,
        val vieneConfirmado: Boolean
    )

    private lateinit var args: DetalleReporteArgs
    private lateinit var metodosPagoAdapter: MetodosPagoAdapter
    private var reporteConfirmadoManual = false
    private var confirmandoReporte = false
    private var dialogSubiendoConfirmacion: AlertDialog? = null
    private var turnoSnapshotActual: DataSnapshot? = null
    private var resumenVentasVisiblesActual = 0.0
    private var resumenEgresosActual = 0.0
    private var resumenDevolucionesActual = 0.0
    private var resumenDevolucionesDetectadas = 0.0
    private var numeroVentasActual = 0L
    private var diferenciaActual = 0.0
    private val esTurnoEnCurso: Boolean
        get() = !args.estadoTurno.equals("cerrada", ignoreCase = true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Bloqueo de capturas de pantalla para proteger datos financieros
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        _binding = ActivityDetalleReporteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        SessionManager.cargarSesion(this)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        args = leerExtras()
        configurarToolbar(args)
        configurarRecycler()
        renderizarDetalle(args)
        cargarResumenMetodosDesdeTurno(args)
        configurarAccionConfirmarReporte()
        configurarAccionVerVentasTurno()
    }

    private fun configurarRecycler() {
        metodosPagoAdapter = MetodosPagoAdapter()
        binding.rvMetodosPagoDetalle.layoutManager = LinearLayoutManager(this)
        binding.rvMetodosPagoDetalle.adapter = metodosPagoAdapter
    }

    private fun configurarAccionVerVentasTurno() {
        binding.btnVerVentasTurno.setOnClickListener {
            if (args.fechaTurno.isBlank() || args.idCaja.isBlank() || args.idTurno.isBlank()) {
                Toast.makeText(this, "Este turno no tiene datos suficientes para ver ventas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startActivity(
                Intent(this, ActivityHistorialVentasCaja::class.java).apply {
                    putExtra(ActivityHistorialVentasCaja.EXTRA_FECHA_VENTAS, args.fechaTurno)
                    putExtra(ActivityHistorialVentasCaja.EXTRA_ID_CAJA_VENTAS, args.idCaja)
                    putExtra(ActivityHistorialVentasCaja.EXTRA_ID_TURNO_VENTAS, args.idTurno)
                    putExtra(ActivityHistorialVentasCaja.EXTRA_NOMBRE_CAJA_VENTAS, args.nombreCaja)
                    putExtra(ActivityHistorialVentasCaja.EXTRA_NOMBRE_CAJERO_VENTAS, args.nombreCajero)
                }
            )
        }
    }

    private fun configurarToolbar(args: DetalleReporteArgs) {
        binding.toolbarDetalleReporte.title = formatearFechaVisible(args.fechaTurno)
        binding.toolbarDetalleReporte.setNavigationOnClickListener { finish() }
    }

    private fun leerExtras(): DetalleReporteArgs {
        val extras = intent.extras
        return DetalleReporteArgs(
            fechaTurno = extras?.getString(EXTRA_FECHA_TURNO).orEmpty(),
            idCaja = extras?.getString(EXTRA_ID_CAJA).orEmpty(),
            nombreCaja = extras?.getString(EXTRA_NOMBRE_CAJA).orEmpty().ifBlank { "Caja" },
            idTurno = extras?.getString(EXTRA_ID_TURNO).orEmpty(),
            nombreCajero = extras?.getString(EXTRA_NOMBRE_CAJERO).orEmpty(),
            estadoTurno = extras?.getString(EXTRA_ESTADO_TURNO).orEmpty(),
            estadoCuadre = extras?.getString(EXTRA_ESTADO_CUADRE).orEmpty(),
            cajaInicial = extras?.getDouble(EXTRA_CAJA_INICIAL, 0.0) ?: 0.0,
            totalVentas = extras?.getDouble(EXTRA_TOTAL_VENTAS, 0.0) ?: 0.0,
            totalEgresos = extras?.getDouble(EXTRA_TOTAL_EGRESOS, 0.0) ?: 0.0,
            totalDevoluciones = extras?.getDouble(EXTRA_TOTAL_DEVOLUCIONES, 0.0) ?: 0.0,
            totalEsperado = extras?.getDouble(EXTRA_TOTAL_ESPERADO, 0.0) ?: 0.0,
            efectivoRegistrado = if (extras?.containsKey(EXTRA_EFECTIVO_REGISTRADO) == true) extras.getDouble(EXTRA_EFECTIVO_REGISTRADO, 0.0) else null,
            diferencia = if (extras?.containsKey(EXTRA_DIFERENCIA) == true) extras.getDouble(EXTRA_DIFERENCIA, 0.0) else null,
            horaApertura = extras?.getString(EXTRA_HORA_APERTURA).orEmpty().ifBlank { "--:--" },
            horaCierre = extras?.getString(EXTRA_HORA_CIERRE).orEmpty().ifBlank { "--:--" },
            vieneConfirmado = extras?.getBoolean(EXTRA_REPORTE_CONFIRMADO, false) == true
        )
    }

    private fun limpiarNombreVisibleEncabezado(nombre: String): String {
        val limpio = nombre.trim()
        return when {
            limpio.isBlank() -> ""
            limpio.equals("sin usuario", ignoreCase = true) -> ""
            limpio.equals("sin cajero", ignoreCase = true) -> ""
            limpio == "--" -> ""
            else -> limpio
        }
    }

    private fun resolverNombreCajeroTurno(snapshot: DataSnapshot, fallback: String): String {
        val candidatos = listOf(
            snapshot.child("nombreCajero").obtenerTexto(),
            snapshot.child("nombreUsuarioCierre").obtenerTexto(),
            snapshot.child("nombreUsuarioApertura").obtenerTexto(),
            snapshot.child("nombreUsuario").obtenerTexto(),
            snapshot.child("usuario").obtenerTexto(),
            snapshot.child("infoTurno").child("nombreCajero").obtenerTexto(),
            snapshot.child("infoTurno").child("nombreUsuario").obtenerTexto(),
            fallback
        )

        return candidatos
            .map { it.trim() }
            .firstOrNull { limpiarNombreVisibleEncabezado(it).isNotBlank() }
            .orEmpty()
    }

    private fun actualizarEncabezadoCaja(args: DetalleReporteArgs, nombreCajeroForzado: String? = null) {
        val turnoCorto = args.idTurno.takeLast(8).ifBlank { "--" }
        val nombreCajaLimpio = args.nombreCaja.trim()
        val nombreCajeroLimpio = limpiarNombreVisibleEncabezado(nombreCajeroForzado ?: args.nombreCajero)

        binding.tvCajaCajera.text = when {
            nombreCajaLimpio.isBlank() && nombreCajeroLimpio.isBlank() -> "--"
            nombreCajaLimpio.isBlank() -> nombreCajeroLimpio
            nombreCajeroLimpio.isBlank() -> nombreCajaLimpio
            nombreCajaLimpio.equals(nombreCajeroLimpio, ignoreCase = true) -> nombreCajaLimpio
            else -> "$nombreCajaLimpio - $nombreCajeroLimpio"
        }
        binding.tvTurno.text = getString(R.string.reportes_turno_info, turnoCorto, args.horaApertura, args.horaCierre)
    }

    private fun renderizarDetalle(args: DetalleReporteArgs) {
        binding.tvTituloCajaInicio.text = if (esTurnoEnCurso) {
            getString(R.string.reportes_titulo_resumen_parcial)
        } else {
            "Resumen del turno"
        }
        binding.tvTituloIngresosEgresos.text = "Conciliación de efectivo"
        binding.tvLabelInicioCon.text = "Apertura en efectivo:"
        binding.tvLabelCerroCon.text = "Total neto del turno:"
        binding.tvLabelIngresosTotales.text = "Apertura en efectivo:"
        binding.tvLabelEgresosTotales.text = "Ventas en efectivo:"
        binding.tvTituloIngresosEgresos.text = "Conciliacion de efectivo"
        binding.tvLabelVentasTurno?.text = if (esTurnoEnCurso) {
            getString(R.string.reportes_label_ventas_acumuladas)
        } else {
            "Ventas totales:"
        }
        binding.tvLabelEgresosResumenTurno?.text = if (esTurnoEnCurso) {
            getString(R.string.reportes_label_egresos_acumulados)
        } else {
            "Egresos totales:"
        }
        binding.tvLabelDevolucionesResumenTurno?.text = if (esTurnoEnCurso) {
            getString(R.string.reportes_label_devoluciones_acumuladas)
        } else {
            "Devoluciones totales:"
        }
        binding.tvLabelCerroCon.text = if (esTurnoEnCurso) {
            getString(R.string.reportes_label_resultado_parcial) + ":"
        } else {
            "Resultado del turno:"
        }
        binding.tvLabelEsperadoEfectivo?.text = "Efectivo esperado en caja:"
        binding.tvLabelContadoEfectivo?.text = "Efectivo contado:"
        binding.tvLabelTotalRegistrado?.text = "Diferencia en caja"

        actualizarEncabezadoCaja(args)

        binding.tvMontoInicioCon.text = MonedaHelper.formatear(args.cajaInicial)
        binding.tvValorCantidadVentas?.text = "0"
        binding.tvValorVentasTurno?.text = MonedaHelper.formatear(args.totalVentas)
        binding.tvValorEgresosResumenTurno?.text =
            if (args.totalEgresos > 0.0) MonedaHelper.formatearConSigno(-args.totalEgresos) else MonedaHelper.formatear(0.0)
        binding.tvValorDevolucionesResumenTurno?.text =
            if (args.totalDevoluciones > 0.0) MonedaHelper.formatearConSigno(-args.totalDevoluciones) else MonedaHelper.formatear(0.0)
        binding.tvMontoCerroCon.text =
            MonedaHelper.formatear(args.totalVentas - args.totalEgresos - args.totalDevoluciones)
        binding.tvMontoIngresosTotales.text = MonedaHelper.formatear(args.cajaInicial)
        binding.tvMontoEgresosTotales.text = MonedaHelper.formatear(0.0)
        binding.tvValorEgresosEfectivo?.text = MonedaHelper.formatear(0.0)
        binding.tvValorDevolucionesEfectivo?.text = MonedaHelper.formatear(0.0)
        binding.tvValorEsperadoEfectivo?.text = MonedaHelper.formatear(args.totalEsperado)
        binding.tvValorContadoEfectivo?.text =
            MonedaHelper.formatear(args.efectivoRegistrado ?: args.totalEsperado)
        renderizarResumenDiferenciaCaja(args.diferencia ?: 0.0)
        diferenciaActual = args.diferencia ?: 0.0

        aplicarEstadoConciliacion(args.diferencia ?: 0.0)
        binding.cardDetalleEgreso.visibility = View.GONE
        binding.cardEgresoComprobante.visibility = View.GONE
        binding.tvNotaGananciaSinApertura?.text =
            if (esTurnoEnCurso) {
                getString(R.string.reportes_nota_resumen_parcial)
            } else {
                "Resultado = ventas - egresos - devoluciones"
            }
        binding.cardObservacionReporte?.visibility = View.GONE
        if (esTurnoEnCurso) {
            binding.cardIngresosEgresos.visibility = View.GONE
            binding.cardTotalRegistrado.visibility = View.GONE
        }

        actualizarResumenHumanoTurno()
        actualizarEstadoAccionReporte(args)
        renderizarMetodosPagoDinamicos(emptyList())
    }

    private fun actualizarEstadoAccionReporte(args: DetalleReporteArgs) {
        val puedeConfirmar = SessionManager.puedeCambiarCaja()
        val esPendiente =
            args.estadoTurno.equals("cerrada", ignoreCase = true) &&
                !args.vieneConfirmado &&
                !reporteConfirmadoManual

        if (esTurnoEnCurso) {
            binding.btnConfirmarReporte.visibility = View.GONE
            return
        }

        binding.btnConfirmarReporte.visibility = View.VISIBLE
        when {
            esPendiente && puedeConfirmar -> {
                binding.btnConfirmarReporte.text = "Confirmar reporte"
                binding.btnConfirmarReporte.isEnabled = true
                binding.btnConfirmarReporte.backgroundTintList =
                    ColorStateList.valueOf("#FFF3E0".toColorInt())
                binding.btnConfirmarReporte.setTextColor("#9A6400".toColorInt())
                binding.btnConfirmarReporte.strokeColor =
                    ColorStateList.valueOf("#F2C785".toColorInt())
                binding.btnConfirmarReporte.strokeWidth =
                    resources.displayMetrics.density.times(1).toInt().coerceAtLeast(1)
            }
            esPendiente -> {
                binding.btnConfirmarReporte.text = "Pendiente de confirmación"
                binding.btnConfirmarReporte.isEnabled = false
                binding.btnConfirmarReporte.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.darker_gray))
                binding.btnConfirmarReporte.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                binding.btnConfirmarReporte.strokeColor =
                    ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.transparent))
                binding.btnConfirmarReporte.strokeWidth = 0
            }
            else -> {
                binding.btnConfirmarReporte.text = "Reporte confirmado"
                binding.btnConfirmarReporte.isEnabled = false
                binding.btnConfirmarReporte.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.darker_gray))
                binding.btnConfirmarReporte.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                binding.btnConfirmarReporte.strokeColor =
                    ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.transparent))
                binding.btnConfirmarReporte.strokeWidth = 0
            }
        }
    }

    private fun aplicarEstadoConciliacion(diferencia: Double) {
        val diferenciaNormalizada = if (abs(diferencia) < 0.009) 0.0 else diferencia
        val (titulo, color, fondo) = when {
            diferenciaNormalizada > 0.0 -> Triple("Sobrante", "#9A6400".toColorInt(), R.drawable.bg_estado_pendiente)
            diferenciaNormalizada < 0.0 -> Triple("Faltante", "#B3261E".toColorInt(), R.drawable.bg_estado_perdida)
            else -> Triple("Caja cuadrada", "#245B4B".toColorInt(), R.drawable.bg_estado_confirmado)
        }

        binding.layoutGanancia.setBackgroundResource(fondo)
        binding.tvGanancia.text = titulo
        binding.tvGanancia.setTextColor(color)
        binding.tvMontoGanancia.setTextColor(color)
        binding.tvMontoGanancia.text = if (diferenciaNormalizada == 0.0) {
            MonedaHelper.formatear(0.0)
        } else {
            MonedaHelper.formatearConSigno(diferenciaNormalizada)
        }
    }

    private fun renderizarResumenDiferenciaCaja(diferencia: Double) {
        val diferenciaNormalizada = if (abs(diferencia) < 0.009) 0.0 else diferencia
        binding.tvMontoTotalRegistrado.text = if (diferenciaNormalizada == 0.0) {
            MonedaHelper.formatear(0.0)
        } else {
            MonedaHelper.formatearConSigno(diferenciaNormalizada)
        }
        binding.tvMontoTotalRegistrado.setTextColor(
            when {
                diferenciaNormalizada > 0.0 -> "#9A6400".toColorInt()
                diferenciaNormalizada < 0.0 -> "#B3261E".toColorInt()
                else -> "#111827".toColorInt()
            }
        )
    }

    private fun renderizarResumenTurno(snapshot: DataSnapshot, args: DetalleReporteArgs) {
        val numeroVentas = snapshot.child("numeroVentas").value?.toString()?.toLongOrNull() ?: 0L
        val totalVentasNetas = snapshot.child("totalVentas").obtenerDoubleFlexible() ?: (args.totalVentas - args.totalDevoluciones)
        val totalEgresos = snapshot.child("totalEgresos").obtenerDoubleFlexible() ?: args.totalEgresos
        val totalDevolucionesTurno = snapshot.child("totalDevoluciones").obtenerDoubleFlexible() ?: args.totalDevoluciones
        val totalDevoluciones = maxOf(totalDevolucionesTurno, resumenDevolucionesDetectadas)
        val totalVentasVisibles = (totalVentasNetas + totalDevoluciones).coerceAtLeast(0.0)
        numeroVentasActual = numeroVentas
        aplicarResumenTurnoUi(
            numeroVentas = numeroVentas,
            apertura = args.cajaInicial,
            totalVentasVisibles = totalVentasVisibles,
            totalEgresos = totalEgresos,
            totalDevoluciones = totalDevoluciones
        )
        actualizarResumenHumanoTurno()
    }

    private fun sincronizarResumenConDevoluciones(totalDevolucionesReales: Double) {
        resumenDevolucionesDetectadas = maxOf(
            resumenDevolucionesDetectadas,
            totalDevolucionesReales.coerceAtLeast(0.0)
        )
        val snapshot = turnoSnapshotActual
        if (snapshot != null) {
            renderizarResumenTurno(snapshot, args)
            return
        }

        aplicarResumenTurnoUi(
            numeroVentas = binding.tvValorCantidadVentas?.text?.toString()?.toLongOrNull() ?: 0L,
            apertura = args.cajaInicial,
            totalVentasVisibles = resumenVentasVisiblesActual,
            totalEgresos = resumenEgresosActual,
            totalDevoluciones = maxOf(resumenDevolucionesActual, resumenDevolucionesDetectadas)
        )
    }

    private fun aplicarResumenTurnoUi(
        numeroVentas: Long,
        apertura: Double,
        totalVentasVisibles: Double,
        totalEgresos: Double,
        totalDevoluciones: Double
    ) {
        val totalNeto = totalVentasVisibles - totalEgresos - totalDevoluciones
        resumenVentasVisiblesActual = totalVentasVisibles
        resumenEgresosActual = totalEgresos
        resumenDevolucionesActual = totalDevoluciones

        binding.tvMontoInicioCon.text = MonedaHelper.formatear(apertura)
        binding.tvValorCantidadVentas?.text = numeroVentas.toString()
        binding.tvValorVentasTurno?.text = MonedaHelper.formatear(totalVentasVisibles)
        binding.tvValorEgresosResumenTurno?.text =
            if (totalEgresos > 0.0) MonedaHelper.formatearConSigno(-totalEgresos) else MonedaHelper.formatear(0.0)
        binding.tvValorDevolucionesResumenTurno?.text =
            if (totalDevoluciones > 0.0) MonedaHelper.formatearConSigno(-totalDevoluciones) else MonedaHelper.formatear(0.0)
        binding.tvMontoCerroCon.text = MonedaHelper.formatear(totalNeto)
        binding.tvMontoCerroCon.setTextColor(
            if (totalNeto < -0.009) "#B3261E".toColorInt() else "#111827".toColorInt()
        )
    }

    private fun calcularTotalDevolucionVisible(devoluciones: List<DevolucionVentaResumen>): Double {
        val totalPorItems = devoluciones.sumOf { devolucion ->
            devolucion.items.sumOf { item -> item.monto.coerceAtLeast(0.0) }
        }
        return if (totalPorItems > 0.009) {
            totalPorItems
        } else {
            devoluciones.sumOf { it.montoDevuelto.coerceAtLeast(0.0) }
        }
    }

    private fun renderizarConciliacionEfectivo(
        snapshot: DataSnapshot,
        args: DetalleReporteArgs,
        devolucionesEfectivo: Double
    ) {
        val apertura = snapshot.child("montoApertura").obtenerDoubleFlexible() ?: args.cajaInicial
        val ventasEfectivoNetas = snapshot.child("ventasEfectivo").obtenerDoubleFlexible() ?: 0.0
        val egresosEfectivo = snapshot.child("totalEgresos").obtenerDoubleFlexible() ?: args.totalEgresos
        val ventasEfectivoVisibles = (ventasEfectivoNetas + devolucionesEfectivo).coerceAtLeast(0.0)
        val esperado = apertura + ventasEfectivoVisibles - egresosEfectivo - devolucionesEfectivo
        val contado = snapshot.child("efectivoReal").obtenerDoubleFlexible() ?: args.efectivoRegistrado ?: esperado
        val diferencia = contado - esperado

        binding.tvMontoIngresosTotales.text = MonedaHelper.formatear(apertura)
        binding.tvMontoEgresosTotales.text = MonedaHelper.formatear(ventasEfectivoVisibles)
        binding.tvValorEgresosEfectivo?.text =
            if (egresosEfectivo > 0.0) MonedaHelper.formatearConSigno(-egresosEfectivo) else MonedaHelper.formatear(0.0)
        binding.tvValorDevolucionesEfectivo?.text =
            if (devolucionesEfectivo > 0.0) MonedaHelper.formatearConSigno(-devolucionesEfectivo) else MonedaHelper.formatear(0.0)
        binding.tvValorEsperadoEfectivo?.text = MonedaHelper.formatear(esperado)
        binding.tvValorContadoEfectivo?.text = MonedaHelper.formatear(contado)
        diferenciaActual = diferencia
        renderizarResumenDiferenciaCaja(diferencia)
        aplicarEstadoConciliacion(diferencia)
        actualizarResumenHumanoTurno()
    }

    private fun actualizarResumenHumanoTurno() {
        val titulo: String
        val detalle: String
        val ayuda: String

        when {
            esTurnoEnCurso && numeroVentasActual == 0L -> {
                titulo = "Turno recien iniciado"
                detalle = "Todavia no hay ventas registradas en este turno."
                ayuda = "Puedes revisar como va avanzando desde aqui durante el dia."
            }
            esTurnoEnCurso -> {
                titulo = "Turno en curso"
                detalle = if (numeroVentasActual == 1L) {
                    "Ya se registro 1 venta en este turno."
                } else {
                    "Ya se registraron $numeroVentasActual ventas en este turno."
                }
                ayuda = "Este resumen puede cambiar mientras la caja siga abierta."
            }
            abs(diferenciaActual) < 0.009 -> {
                titulo = "Turno en orden"
                detalle = "La caja cierra sin diferencia y el turno cuadra correctamente."
                ayuda = construirAyudaResumenTurno()
            }
            diferenciaActual > 0.0 -> {
                titulo = "Hay sobrante por revisar"
                detalle = "Se conto mas efectivo del esperado al cerrar este turno."
                ayuda = construirAyudaResumenTurno()
            }
            else -> {
                titulo = "Hay faltante por revisar"
                detalle = "Se conto menos efectivo del esperado al cerrar este turno."
                ayuda = construirAyudaResumenTurno()
            }
        }

        binding.tvResumenTurnoEstado.text = titulo
        binding.tvResumenTurnoDetalle.text = detalle
        binding.tvResumenTurnoAyuda.text = ayuda
    }

    private fun construirAyudaResumenTurno(): String {
        return when {
            resumenDevolucionesActual > 0.009 && resumenEgresosActual > 0.009 ->
                "Conviene revisar devoluciones, egresos y ventas del turno para entender el cierre completo."
            resumenDevolucionesActual > 0.009 ->
                "Conviene revisar las devoluciones registradas para entender mejor este cierre."
            resumenEgresosActual > 0.009 ->
                "Conviene revisar los egresos registrados para entender mejor este cierre."
            numeroVentasActual == 0L ->
                "No hubo ventas en este turno."
            else ->
                "Puedes abrir las ventas del turno para revisar el detalle completo."
        }
    }

    private fun renderizarObservacionReporte(snapshot: DataSnapshot) {
        val observacionManual = snapshot.child("observacionCierreManual").obtenerTexto().trim()
        val tieneObservacion = observacionManual.isNotBlank()

        binding.cardObservacionReporte?.visibility = if (tieneObservacion) View.VISIBLE else View.GONE
        if (!tieneObservacion) {
            binding.tvContenidoObservacionReporte?.text = ""
            return
        }

        binding.tvContenidoObservacionReporte?.text = observacionManual
    }

    private fun cargarResumenMetodosDesdeTurno(args: DetalleReporteArgs) {
        if (args.idCaja.isBlank() || args.fechaTurno.isBlank() || args.idTurno.isBlank()) return

        FirebaseDatabase.getInstance().reference
            .child("CorteCaja").child("Cajeras").child(args.idCaja)
            .child(args.fechaTurno).child("turnos").child(args.idTurno)
            .get().addOnSuccessListener { snapshot ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener
                turnoSnapshotActual = snapshot
                actualizarEncabezadoCaja(args, resolverNombreCajeroTurno(snapshot, args.nombreCajero))
                reporteConfirmadoManual = snapshot.child("confirmadoReporte").getValue(Boolean::class.java) == true
                resolverDevolucionesEfectivoTurno(args, snapshot) { devolucionesEfectivo ->
                    if (isFinishing || isDestroyed) return@resolverDevolucionesEfectivoTurno
                    renderizarResumenTurno(snapshot, args)
                    renderizarConciliacionEfectivo(snapshot, args, devolucionesEfectivo)
                    renderizarObservacionReporte(snapshot)
                    actualizarEstadoAccionReporte(args)
                    renderizarMetodosPagoDinamicos(obtenerMetodosPagoDinamicos(snapshot))
                    renderizarDiferenciasCambio(obtenerDiferenciasCambioResumen(snapshot))
                    renderizarDetalleEgreso(snapshot)
                }
            }

        // Cargar devoluciones de VentasPorCajera
        cargarDevolucionesDeTurnoDetallado(args)
    }

    private fun resolverDevolucionesEfectivoTurno(
        args: DetalleReporteArgs,
        turnoSnapshot: DataSnapshot,
        onResultado: (Double) -> Unit
    ) {
        val persistido = turnoSnapshot.child("devolucionesEfectivo").obtenerDoubleFlexible()
        val totalDevoluciones = turnoSnapshot.child("totalDevoluciones").obtenerDoubleFlexible() ?: args.totalDevoluciones
        val puedeUsarPersistido = persistido != null && (persistido > 0.009 || totalDevoluciones <= 0.009)
        if (puedeUsarPersistido) {
            onResultado(persistido.coerceAtLeast(0.0))
            return
        }

        FirebaseDatabase.getInstance().reference
            .child("VentasPorCajera").child(args.idCaja).child(args.fechaTurno)
            .get()
            .addOnSuccessListener { ventasSnapshot ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener
                var total = 0.0

                for (ventaSnap in ventasSnapshot.children) {
                    val idTurnoVenta = ventaSnap.child("idTurno").obtenerTexto()
                        .ifBlank { ventaSnap.child("infoVenta").child("idTurno").obtenerTexto() }
                        .ifBlank { ventaSnap.child("devolucion").child("idTurno").obtenerTexto() }
                    if (idTurnoVenta.isNotBlank() && idTurnoVenta != args.idTurno) continue

                    val metodoPagoVenta = ventaSnap.child("infoVenta").child("metodoPago").obtenerTexto()
                        .ifBlank { ventaSnap.child("metodoPago").obtenerTexto() }
                    if (!metodoPagoVenta.contains("efectivo", ignoreCase = true)) continue

                    val devolucionSnap = ventaSnap.child("devolucion")
                    if (!devolucionSnap.exists()) continue

                    val eventosSnap = devolucionSnap.child("eventos")
                    if (eventosSnap.exists()) {
                        eventosSnap.children.forEach { eventoSnap ->
                            val idTurnoEvento = eventoSnap.child("idTurno").obtenerTexto()
                                .ifBlank { devolucionSnap.child("idTurno").obtenerTexto() }
                            if (idTurnoEvento.isNotBlank() && idTurnoEvento != args.idTurno) return@forEach

                            val montoEvento = eventoSnap.child("ajusteFinanciero")
                                .child("montoDevueltoCliente")
                                .obtenerDoubleFlexible()
                                ?: eventoSnap.child("montoDevueltoCliente").obtenerDoubleFlexible()
                                ?: eventoSnap.child("montoDevuelto").obtenerDoubleFlexible()
                                ?: 0.0
                            total += montoEvento.coerceAtLeast(0.0)
                        }
                    } else {
                        val montoLegacy = devolucionSnap.child("montoDevueltoAcumulado").obtenerDoubleFlexible()
                            ?: devolucionSnap.child("montoDevuelto").obtenerDoubleFlexible()
                            ?: devolucionSnap.child("montoDevueltoUltimo").obtenerDoubleFlexible()
                            ?: 0.0
                        total += montoLegacy.coerceAtLeast(0.0)
                    }
                }

                onResultado(total)
            }
            .addOnFailureListener {
                onResultado(0.0)
            }
    }

    private data class DevolucionItemResumen(
        val textoHumano: String = "",
        val nombre: String = "",
        val cantidad: Int = 0,
        val presentacion: String = "",
        val monto: Double = 0.0,
        val motivo: String = "",
        val resolucion: String = "",
        val productoSustituto: String = "",
        val medioPagoDiferencia: String = "",
        val montoCobradoCliente: Double = 0.0,
        val montoDevueltoCliente: Double = 0.0,
        val montoPagadoDiferencia: Double = 0.0,
        val detalle: String = ""
    )

    private data class DiferenciaCambioResumen(
        val titulo: String = "",
        val monto: Double = 0.0
    )

    private data class DevolucionVentaResumen(
        val idVenta: String = "",
        val hora: String = "",
        val estadoGeneral: String = "",
        val montoDevuelto: Double,
        val items: List<DevolucionItemResumen>
    )

    private fun textoEstadoGeneralDevolucion(estado: String): String {
        return when (estado.trim().lowercase(Locale.getDefault())) {
            "devuelta_total", "devuelta" -> "Devuelta total"
            "devolucion_parcial" -> "Con devolucion parcial"
            else -> "Vigente"
        }
    }

    private fun textoResolucionDevolucion(tipo: String): String {
        return when (tipo.trim().lowercase(Locale.getDefault())) {
            "cambio_producto" -> "devuelto por cambio"
            "dinero_devuelto" -> "devuelto con dinero"
            else -> "devuelto"
        }
    }

    private fun cargarDevolucionesDeTurnoDetallado(args: DetalleReporteArgs) {
        if (args.idCaja.isBlank() || args.fechaTurno.isBlank() || args.idTurno.isBlank()) return

        FirebaseDatabase.getInstance().reference
            .child("VentasPorCajera").child(args.idCaja).child(args.fechaTurno)
            .get().addOnSuccessListener { snapshot ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener

                val devoluciones = mutableListOf<DevolucionVentaResumen>()
                for (ventaSnap in snapshot.children) {
                    val idTurnoVenta = ventaSnap.child("idTurno").obtenerTexto()
                        .ifBlank { ventaSnap.child("infoVenta").child("idTurno").obtenerTexto() }
                        .ifBlank { ventaSnap.child("devolucion").child("idTurno").obtenerTexto() }
                    if (idTurnoVenta.isNotBlank() && idTurnoVenta != args.idTurno) continue

                    val devolucionSnap = ventaSnap.child("devolucion")
                    if (!devolucionSnap.exists()) continue

                    val estadoGeneral = devolucionSnap.child("estadoGeneral").obtenerTexto()
                        .ifBlank { devolucionSnap.child("estado").obtenerTexto() }
                    if (estadoGeneral.isBlank() || estadoGeneral.equals("vigente", ignoreCase = true)) continue

                    val eventosSnap = devolucionSnap.child("eventos")
                    if (eventosSnap.exists()) {
                        eventosSnap.children.forEach { eventoSnap ->
                            val idTurnoEvento = eventoSnap.child("idTurno").obtenerTexto()
                                .ifBlank { devolucionSnap.child("idTurno").obtenerTexto() }
                            if (idTurnoEvento.isNotBlank() && idTurnoEvento != args.idTurno) return@forEach

                            val montoEvento = eventoSnap.child("montoDevuelto").obtenerDoubleFlexible()
                                ?: eventoSnap.child("valorProductosDevueltos").obtenerDoubleFlexible()
                                ?: 0.0

                            val items = construirLineasHumanasDevolucion(
                                detalleSnap = eventoSnap.child("detalleProductos"),
                                motivo = eventoSnap.child("motivo").obtenerTexto()
                                    .ifBlank { devolucionSnap.child("motivo").obtenerTexto() },
                                tipoResolucion = eventoSnap.child("tipoResolucion").obtenerTexto()
                                    .ifBlank { devolucionSnap.child("tipoResolucion").obtenerTexto() },
                                sustitucionNombre = eventoSnap.child("sustitucion").child("nombre").obtenerTexto(),
                                montoCobradoCliente = eventoSnap.child("ajusteFinanciero")
                                    .child("montoCobradoCliente")
                                    .obtenerDoubleFlexible()
                                    ?: eventoSnap.child("montoCobradoCliente").obtenerDoubleFlexible()
                                    ?: 0.0,
                                montoDevueltoCliente = eventoSnap.child("ajusteFinanciero")
                                    .child("montoDevueltoCliente")
                                    .obtenerDoubleFlexible()
                                    ?: 0.0,
                                medioPagoDiferencia = eventoSnap.child("pagoDiferencia")
                                    .child("metodoPago")
                                    .obtenerTexto(),
                                montoPagadoDiferencia = eventoSnap.child("pagoDiferencia")
                                    .child("montoPagado")
                                    .obtenerDoubleFlexible()
                                    ?: 0.0,
                                montoFallback = montoEvento
                            )

                            if (items.isNotEmpty()) {
                                devoluciones += DevolucionVentaResumen(
                                    montoDevuelto = montoEvento,
                                    items = items
                                )
                            }
                        }
                    } else {
                        val montoDevuelto = devolucionSnap.child("montoDevueltoAcumulado").obtenerDoubleFlexible()
                            ?: devolucionSnap.child("montoDevuelto").obtenerDoubleFlexible()
                            ?: 0.0

                        val items = construirLineasHumanasDevolucion(
                            detalleSnap = devolucionSnap.child("detalleProductos"),
                            motivo = devolucionSnap.child("motivo").obtenerTexto(),
                            tipoResolucion = devolucionSnap.child("tipoResolucion").obtenerTexto(),
                            sustitucionNombre = devolucionSnap.child("sustitucionUltima").child("nombre").obtenerTexto(),
                            montoCobradoCliente = devolucionSnap.child("montoCobradoClienteUltimo").obtenerDoubleFlexible()
                                ?: devolucionSnap.child("montoCobradoClienteAcumulado").obtenerDoubleFlexible()
                                ?: 0.0,
                            montoDevueltoCliente = devolucionSnap.child("montoDevueltoUltimo").obtenerDoubleFlexible()
                                ?: 0.0,
                            medioPagoDiferencia = devolucionSnap.child("metodoPagoDiferenciaUltimo").obtenerTexto()
                                .ifBlank {
                                    devolucionSnap.child("pagoDiferenciaUltimo")
                                        .child("metodoPago")
                                        .obtenerTexto()
                                },
                            montoPagadoDiferencia = devolucionSnap.child("montoPagoDiferenciaUltimo").obtenerDoubleFlexible()
                                ?: devolucionSnap.child("pagoDiferenciaUltimo")
                                    .child("montoPagado")
                                    .obtenerDoubleFlexible()
                                ?: 0.0,
                            montoFallback = montoDevuelto
                        )

                        if (items.isNotEmpty()) {
                            devoluciones += DevolucionVentaResumen(
                                montoDevuelto = montoDevuelto,
                                items = items
                            )
                        }
                    }
                }

                renderizarDevolucionesDetalladas(devoluciones)
            }
        return

        FirebaseDatabase.getInstance().reference
            .child("VentasPorCajera").child(args.idCaja).child(args.fechaTurno)
            .get().addOnSuccessListener { snapshot ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener

                val devoluciones = mutableListOf<DevolucionVentaResumen>()
                for (ventaSnap in snapshot.children) {
                    val idTurnoVenta = ventaSnap.child("idTurno").obtenerTexto()
                        .ifBlank { ventaSnap.child("infoVenta").child("idTurno").obtenerTexto() }
                        .ifBlank { ventaSnap.child("devolucion").child("idTurno").obtenerTexto() }
                    if (idTurnoVenta.isNotBlank() && idTurnoVenta != args.idTurno) continue

                    val devolucionSnap = ventaSnap.child("devolucion")
                    if (!devolucionSnap.exists()) continue

                    val estadoGeneral = devolucionSnap.child("estadoGeneral").obtenerTexto()
                        .ifBlank { devolucionSnap.child("estado").obtenerTexto() }
                    if (estadoGeneral.isBlank() || estadoGeneral.equals("vigente", ignoreCase = true)) continue

                    val montoDevuelto = devolucionSnap.child("montoDevueltoAcumulado").obtenerDoubleFlexible()
                        ?: devolucionSnap.child("montoDevuelto").obtenerDoubleFlexible()
                        ?: 0.0
                    val hora = devolucionSnap.child("horaUltimaDevolucion").obtenerTexto()
                        .ifBlank { devolucionSnap.child("hora").obtenerTexto() }

                    val items = mutableListOf<DevolucionItemResumen>()
                    val itemsSnap = devolucionSnap.child("items")
                    if (itemsSnap.exists()) {
                        itemsSnap.children.forEach { itemSnap ->
                            val nombre = itemSnap.child("nombre").obtenerTexto().ifBlank { "Producto" }
                            val presentacion = itemSnap.child("presentacion").obtenerTexto()
                            val cantidadVendida = itemSnap.child("cantidadVendida").getValue(Int::class.java)
                                ?: itemSnap.child("cantidadVendida").value?.toString()?.toIntOrNull()
                                ?: 0
                            val cantidadDevuelta = itemSnap.child("cantidadDevuelta").getValue(Int::class.java)
                                ?: itemSnap.child("cantidadDevuelta").value?.toString()?.toIntOrNull()
                                ?: 0
                            val estadoItem = itemSnap.child("estado").obtenerTexto()
                            val tipoResolucion = itemSnap.child("tipoResolucion").obtenerTexto()
                                .ifBlank { itemSnap.child("tipoResolucionUltima").obtenerTexto() }
                            if (cantidadDevuelta <= 0) return@forEach

                            val estadoTexto = if (estadoItem.equals("parcialmente_devuelto", ignoreCase = true)) {
                                "parcialmente devuelto"
                            } else {
                                textoResolucionDevolucion(tipoResolucion)
                            }

                            val detalle = buildString {
                                append(estadoTexto)
                                if (cantidadVendida > 0) append(" · $cantidadDevuelta/$cantidadVendida")
                                if (presentacion.isNotBlank()) append(" $presentacion")
                            }
                            items += DevolucionItemResumen(nombre = nombre, detalle = detalle)
                        }
                    } else {
                        devolucionSnap.child("detalleProductos").children.forEach { itemSnap ->
                            val nombre = itemSnap.child("nombre").obtenerTexto().ifBlank { "Producto" }
                            val presentacion = itemSnap.child("presentacion").obtenerTexto()
                            val cantidadVendida = itemSnap.child("cantidadVendida").getValue(Int::class.java)
                                ?: itemSnap.child("cantidadVendida").value?.toString()?.toIntOrNull()
                                ?: 0
                            val detalle = buildString {
                                append("devuelto")
                                if (cantidadVendida > 0) append(" · $cantidadVendida")
                                if (presentacion.isNotBlank()) append(" $presentacion")
                            }
                            items += DevolucionItemResumen(nombre = nombre, detalle = detalle)
                        }
                    }

                    devoluciones += DevolucionVentaResumen(
                        idVenta = ventaSnap.key.orEmpty(),
                        hora = hora,
                        estadoGeneral = textoEstadoGeneralDevolucion(estadoGeneral),
                        montoDevuelto = montoDevuelto,
                        items = items
                    )
                }

                renderizarDevolucionesDetalladasHumanas(devoluciones)
            }
    }

    private fun renderizarDevolucionesDetalladasHumanas(devoluciones: List<DevolucionVentaResumen>) {
        val card = binding.cardDevoluciones
        if (devoluciones.isEmpty()) {
            card.visibility = View.GONE
            sincronizarResumenConDevoluciones(0.0)
            return
        }

        val totalItems = devoluciones.sumOf { it.items.size }
        val totalDevuelto = calcularTotalDevolucionVisible(devoluciones)
        binding.tvConteoDevolucionesReporte.text =
            "$totalItems devolucion${if (totalItems != 1) "es" else ""} en este turno"
        binding.tvMontoDevolucionesReporte.text =
            "Total devuelto: ${MonedaHelper.formatear(totalDevuelto)}"
        sincronizarResumenConDevoluciones(totalDevuelto)

        val contenedor = binding.layoutDetalleDevolucionesReporte
        contenedor.removeAllViews()

        devoluciones.forEach { devolucion ->
            devolucion.items.forEach { item ->
                val bloque = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(0, 8, 0, 12)
                }

                bloque.addView(
                    TextView(this).apply {
                        text = item.nombre.ifBlank { "Producto" }
                        textSize = 13.5f
                        setTextColor("#374151".toColorInt())
                    }
                )

                fun agregarLinea(texto: String) {
                    val valor = texto.trim()
                    if (valor.isBlank()) return
                    bloque.addView(
                        TextView(this).apply {
                            this.text = valor
                            textSize = 12.5f
                            setTextColor("#6B7280".toColorInt())
                            setPadding(0, 2, 0, 0)
                        }
                    )
                }

                agregarLinea(
                    "Cantidad: ${
                        construirCantidadPresentacionHumana(
                            cantidad = item.cantidad,
                            presentacion = item.presentacion
                        )
                    }"
                )
                agregarLinea("Monto: ${MonedaHelper.formatear(item.monto)}")
                agregarLinea("Motivo: ${item.motivo}")
                agregarLinea("Resolución: ${item.resolucion}")

                if (item.productoSustituto.isNotBlank()) {
                    agregarLinea("Producto sustituto: ${item.productoSustituto}")
                }
                if (item.montoCobradoCliente > 0.0) {
                    agregarLinea("Diferencia cobrada: ${MonedaHelper.formatear(item.montoCobradoCliente)}")
                }
                if (item.medioPagoDiferencia.isNotBlank() && item.montoPagadoDiferencia > 0.0) {
                    agregarLinea(
                        "Pago de diferencia: ${humanizarMetodoPago(item.medioPagoDiferencia)}" +
                            " (${MonedaHelper.formatear(item.montoPagadoDiferencia)})"
                    )
                }
                if (item.montoDevueltoCliente > 0.0) {
                    agregarLinea("Diferencia devuelta: ${MonedaHelper.formatear(item.montoDevueltoCliente)}")
                }

                contenedor.addView(bloque)
            }
        }

        card.visibility = View.VISIBLE
    }

    private fun renderizarDevolucionesDetalladas(devoluciones: List<DevolucionVentaResumen>) {
        val card = binding.cardDevoluciones
        if (devoluciones.isEmpty()) {
            card.visibility = View.GONE
            sincronizarResumenConDevoluciones(0.0)
            return
        }

        val totalItems = devoluciones.sumOf { it.items.size }
        val totalDevueltoHumano = calcularTotalDevolucionVisible(devoluciones)
        binding.tvConteoDevolucionesReporte.text =
            "$totalItems devolucion${if (totalItems != 1) "es" else ""} en este turno"
        binding.tvMontoDevolucionesReporte.text =
            "Total devuelto: ${MonedaHelper.formatear(totalDevueltoHumano)}"
        sincronizarResumenConDevoluciones(totalDevueltoHumano)

        val contenedorHumano = binding.layoutDetalleDevolucionesReporte
        contenedorHumano.removeAllViews()

        devoluciones.forEach { devolucion ->
            devolucion.items.forEach { item ->
                val tv = TextView(this).apply {
                    text = "• ${item.textoHumano}"
                    textSize = 12.5f
                    setTextColor("#6B7280".toColorInt())
                    setPadding(0, 6, 0, 6)
                }
                contenedorHumano.addView(tv)
            }
        }

        card.visibility = View.VISIBLE
        return

        val totalDevuelto = devoluciones.sumOf { it.montoDevuelto }
        binding.tvConteoDevolucionesReporte.text =
            "${devoluciones.size} venta${if (devoluciones.size != 1) "s" else ""} con devolucion"
        binding.tvMontoDevolucionesReporte.text =
            "Total devuelto: ${MonedaHelper.formatear(totalDevuelto)}"

        val contenedor = binding.layoutDetalleDevolucionesReporte
        contenedor.removeAllViews()

        devoluciones.forEach { devolucion ->
            val tv = TextView(this).apply {
                text = buildString {
                    append("• Venta #")
                    append(devolucion.idVenta.takeLast(6).ifBlank { devolucion.idVenta })
                    append(" · ")
                    append(devolucion.estadoGeneral)
                    if (devolucion.hora.isNotBlank()) {
                        append(" · ")
                        append(devolucion.hora)
                    }
                    append("\n  Monto: ${MonedaHelper.formatear(devolucion.montoDevuelto)}")
                    devolucion.items.forEach { item ->
                        append("\n  ")
                        append(item.nombre)
                        append(": ")
                        append(item.detalle)
                    }
                }
                textSize = 12.5f
                setTextColor("#6B7280".toColorInt())
                setPadding(0, 6, 0, 6)
            }
            contenedor.addView(tv)
        }

        card.visibility = View.VISIBLE
    }

    private fun obtenerDiferenciasCambioResumen(snapshot: DataSnapshot): List<DiferenciaCambioResumen> {
        return snapshot.child("resumenDiferenciasCobradas").children.mapNotNull { child ->
            val titulo = child.child("titulo").obtenerTexto()
            val monto = child.child("monto").obtenerDoubleFlexible() ?: 0.0
            if (titulo.isBlank() || monto <= 0.0) null else DiferenciaCambioResumen(titulo, monto)
        }.sortedBy { it.titulo.lowercase(Locale.getDefault()) }
    }

    private fun renderizarDiferenciasCambio(diferencias: List<DiferenciaCambioResumen>) {
        val card = binding.cardDiferenciasCambio ?: return
        val tvConteo = binding.tvConteoDiferenciasCambioReporte ?: return
        val tvMonto = binding.tvMontoDiferenciasCambioReporte ?: return
        val contenedor = binding.layoutDetalleDiferenciasCambioReporte ?: return
        if (diferencias.isEmpty()) {
            card.visibility = View.GONE
            return
        }

        val totalCobrado = diferencias.sumOf { it.monto }
        tvConteo.text =
            "${diferencias.size} cobro${if (diferencias.size != 1) "s" else ""} de diferencia registrado${if (diferencias.size != 1) "s" else ""}"
        tvMonto.text =
            "Total cobrado: ${MonedaHelper.formatear(totalCobrado)}"

        contenedor.removeAllViews()

        diferencias.forEach { diferencia ->
            val tv = TextView(this).apply {
                text = "• ${diferencia.titulo}: ${MonedaHelper.formatear(diferencia.monto)}"
                textSize = 12.5f
                setTextColor("#4B5563".toColorInt())
                setPadding(0, 6, 0, 6)
            }
            contenedor.addView(tv)
        }

        card.visibility = View.VISIBLE
    }

    private fun construirLineasHumanasDevolucion(
        detalleSnap: DataSnapshot,
        motivo: String,
        tipoResolucion: String,
        sustitucionNombre: String,
        montoCobradoCliente: Double,
        montoDevueltoCliente: Double,
        medioPagoDiferencia: String,
        montoPagadoDiferencia: Double,
        montoFallback: Double
    ): List<DevolucionItemResumen> {
        if (!detalleSnap.exists()) return emptyList()

        val detalles = detalleSnap.children.toList()
        val montoUnitarioFallback = if (detalles.size == 1) montoFallback else 0.0

        return detalles.mapNotNull { itemSnap ->
            val cantidad = itemSnap.child("cantidadDevueltaAhora").value?.toString()?.toIntOrNull()
                ?: itemSnap.child("cantidadDevuelta").value?.toString()?.toIntOrNull()
                ?: itemSnap.child("cantidadVendida").value?.toString()?.toIntOrNull()
                ?: 0
            if (cantidad <= 0) return@mapNotNull null

            val presentacion = itemSnap.child("presentacion").obtenerTexto()
            val motivoHumano = resolverMotivoDevolucionHumano(
                motivoGeneral = motivo,
                itemSnap = itemSnap
            )
            val resolucionHumana = textoResolucionDevolucionHumana(tipoResolucion)

            val linea = DevolucionCierreLinea(
                producto = itemSnap.child("nombre").obtenerTexto().ifBlank { "Producto" },
                cantidad = cantidad,
                presentacion = presentacion,
                monto = itemSnap.child("subtotalDevuelto").obtenerDoubleFlexible() ?: montoUnitarioFallback,
                motivo = motivoHumano,
                tipoResolucion = tipoResolucion,
                productoSustituto = sustitucionNombre,
                montoCobradoCliente = montoCobradoCliente,
                medioPagoDiferencia = medioPagoDiferencia,
                montoPagadoDiferencia = montoPagadoDiferencia,
                montoDevueltoCliente = if (tipoResolucion.trim().lowercase(Locale.getDefault()) == "cambio_producto") {
                    montoDevueltoCliente
                } else {
                    0.0
                }
            )

            DevolucionItemResumen(
                textoHumano = CajaTurnoHelper.construirTextoObservacionDevolucion(linea),
                nombre = linea.producto,
                cantidad = cantidad,
                presentacion = presentacion,
                monto = linea.monto,
                motivo = motivoHumano,
                resolucion = resolucionHumana,
                productoSustituto = sustitucionNombre,
                medioPagoDiferencia = medioPagoDiferencia,
                montoCobradoCliente = montoCobradoCliente,
                montoDevueltoCliente = linea.montoDevueltoCliente,
                montoPagadoDiferencia = montoPagadoDiferencia
            )
        }
    }

    private fun resolverMotivoDevolucionHumano(
        motivoGeneral: String,
        itemSnap: DataSnapshot
    ): String {
        val candidatos = listOf(
            motivoGeneral,
            itemSnap.child("motivo").obtenerTexto(),
            itemSnap.child("motivoDevolucion").obtenerTexto(),
            itemSnap.child("motivoUltimo").obtenerTexto(),
            itemSnap.child("motivoBloqueoAplicado").obtenerTexto(),
            itemSnap.child("motivoBloqueoUltimo").obtenerTexto()
        )

        return candidatos
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() && !it.equals("Sin motivo", ignoreCase = true) }
            ?: "Motivo no registrado"
    }

    private fun construirCantidadPresentacionHumana(
        cantidad: Int,
        presentacion: String
    ): String {
        if (cantidad <= 0) return "0"
        return if (presentacion.isBlank()) {
            "$cantidad"
        } else {
            "$cantidad $presentacion"
        }
    }

    private fun textoResolucionDevolucionHumana(tipo: String): String {
        return when (tipo.trim().lowercase(Locale.getDefault())) {
            "cambio_producto" -> "Cambiar por otro producto"
            "dinero_devuelto" -> "Devolver dinero"
            else -> "Devolución registrada"
        }
    }

    private fun humanizarMetodoPago(metodo: String): String {
        return when (metodo.trim().lowercase(Locale.getDefault())) {
            "efectivo" -> "Efectivo"
            "transferencia" -> "Transferencia"
            "pago_movil", "pago movil" -> "Pago móvil"
            "tarjeta" -> "Tarjeta"
            "otro" -> "Otro"
            else -> metodo.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }
    }

    private fun cargarDevolucionesDeTurno(args: DetalleReporteArgs) {
        if (args.idCaja.isBlank() || args.fechaTurno.isBlank() || args.idTurno.isBlank()) return

        FirebaseDatabase.getInstance().reference
            .child("VentasPorCajera").child(args.idCaja).child(args.fechaTurno)
            .get().addOnSuccessListener { snapshot ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener

                // Triple: (hora, monto, descripcion)
                val devoluciones = mutableListOf<Triple<String, Double, String>>()

                for (ventaSnap in snapshot.children) {
                    // idTurno puede estar en varios niveles según cómo se guardó la venta
                    val idTurnoVenta = ventaSnap.child("idTurno").obtenerTexto()
                        .ifBlank { ventaSnap.child("infoVenta").child("idTurno").obtenerTexto() }
                        .ifBlank { ventaSnap.child("devolucion").child("idTurno").obtenerTexto() }
                    if (idTurnoVenta.isNotBlank() && idTurnoVenta != args.idTurno) continue

                    val devolucionSnap = ventaSnap.child("devolucion")
                    if (!devolucionSnap.exists()) continue

                    val estado = devolucionSnap.child("estado").obtenerTexto()
                    if (!estado.equals("devuelta", ignoreCase = true)) continue

                    val montoDevuelto = devolucionSnap.child("montoDevuelto").obtenerDoubleFlexible() ?: 0.0
                    if (montoDevuelto <= 0.0) continue

                    val hora = devolucionSnap.child("hora").obtenerTexto()
                    val motivo = devolucionSnap.child("motivo").obtenerTexto().let {
                        if (it.equals("Sin motivo", ignoreCase = true)) "" else it
                    }
                    val cantProductos = devolucionSnap.child("productosDevueltos")
                        .getValue(Int::class.java) ?: 0
                    val descripcion = buildString {
                        if (cantProductos > 0) append("$cantProductos producto${if (cantProductos != 1) "s" else ""}")
                        if (motivo.isNotBlank()) {
                            if (isNotEmpty()) append(" · ")
                            append(motivo)
                        }
                    }

                    devoluciones.add(Triple(hora, montoDevuelto, descripcion))
                }

                renderizarDevoluciones(devoluciones)
            }
    }

    private fun renderizarDevoluciones(devoluciones: List<Triple<String, Double, String>>) {
        val card = binding.cardDevoluciones
        if (devoluciones.isEmpty()) {
            card.visibility = View.GONE
            return
        }

        val totalDevuelto = devoluciones.sumOf { it.second }
        val conteo = devoluciones.size
        val palabraVenta = if (conteo == 1) getString(R.string.devolucion_singular) else getString(R.string.devolucion_plural)

        binding.tvConteoDevolucionesReporte.text = getString(R.string.reportes_devoluciones_conteo, conteo, palabraVenta)
        binding.tvMontoDevolucionesReporte.text = getString(R.string.reportes_devoluciones_monto, MonedaHelper.formatear(totalDevuelto))

        val contenedor = binding.layoutDetalleDevolucionesReporte
        contenedor.removeAllViews()

        devoluciones.forEach { (hora, monto, detalle) ->
            val tv = TextView(this).apply {
                val horaTexto = if (hora.isNotBlank()) " · $hora" else ""
                text = buildString {
                    append("• ${MonedaHelper.formatear(monto)}")
                    append(horaTexto)
                    if (detalle.isNotBlank()) {
                        append("\n  $detalle")
                    }
                }
                textSize = 12.5f
                setTextColor("#6B7280".toColorInt())
                setPadding(0, 4, 0, 4)
            }
            contenedor.addView(tv)
        }

        card.visibility = View.VISIBLE
    }

    private fun renderizarDetalleEgreso(turnoSnapshot: DataSnapshot) {
        val egresosSnapshot = turnoSnapshot.child("egresos")
        val ultimoEgreso = egresosSnapshot.children.maxByOrNull { 
            it.child("timestampServidor").obtenerLongFlexible() ?: it.child("timestamp").obtenerLongFlexible() ?: 0L 
        }

        if (ultimoEgreso == null) {
            binding.cardDetalleEgreso.visibility = View.GONE
            return
        }

        val motivo = ultimoEgreso.child("motivo").obtenerTexto()
        val comprobanteUrl = ultimoEgreso.child("comprobanteUrl").obtenerTexto()
        val monto = ultimoEgreso.child("monto").obtenerDoubleFlexible()
        val hora = ultimoEgreso.child("hora").obtenerTexto()

        binding.cardDetalleEgreso.visibility = if (motivo.isNotBlank() || comprobanteUrl.isNotBlank()) View.VISIBLE else View.GONE
        binding.tvEgresoMotivo.text = if (motivo.isNotBlank()) "${getString(R.string.reportes_egreso_motivo_label)} $motivo" else getString(R.string.reportes_egreso_motivo_label)

        val meta = listOfNotNull(
            monto?.let { "Monto: ${MonedaHelper.formatear(it)}" },
            hora.takeIf { it.isNotBlank() }?.let { "Hora: $it" }
        ).joinToString("  •  ")
        
        binding.tvEgresoMeta.visibility = if (meta.isBlank()) View.GONE else View.VISIBLE
        binding.tvEgresoMeta.text = meta

        if (comprobanteUrl.isNotBlank()) {
            binding.cardEgresoComprobante.visibility = View.VISIBLE
            Glide.with(this).load(comprobanteUrl).centerCrop().into(binding.ivEgresoComprobante)
        } else {
            binding.cardEgresoComprobante.visibility = View.GONE
        }
    }

    private fun configurarAccionConfirmarReporte() {
        binding.btnConfirmarReporte.setOnClickListener { confirmarReporteYSalir() }
    }

    private fun confirmarReporteYSalir() {
        if (confirmandoReporte) return
        confirmandoReporte = true
        binding.btnConfirmarReporte.isEnabled = false
        mostrarDialogoSubiendoConfirmacion(true)
        val turnoRef = FirebaseDatabase.getInstance().reference
            .child("CorteCaja").child("Cajeras").child(args.idCaja)
            .child(args.fechaTurno).child("turnos").child(args.idTurno)

        val updates = hashMapOf<String, Any>(
            "confirmadoReporte" to true,
            "confirmadoReportePorId" to SessionManager.idCajera,
            "confirmadoReportePorNombre" to SessionManager.nombreCajera,
            "timestampConfirmacionReporte" to System.currentTimeMillis(),
            "timestampConfirmacionReporteServidor" to ServerValue.TIMESTAMP
        )

        turnoRef.updateChildren(updates).addOnSuccessListener {
            MovimientoLogger.registrarConSesion(
                context = this,
                tipo = "reporte_caja_confirmado",
                modulo = "reportes_caja",
                titulo = "Reporte confirmado",
                descripcion = "Se confirmó el reporte del turno ${args.idTurno.takeLast(8)}.",
                referenciaId = args.idTurno,
                idCaja = args.idCaja,
                nombreCaja = args.nombreCaja
            )
            mostrarDialogoSubiendoConfirmacion(false)
            setResult(RESULT_OK)
            finish()
        }.addOnFailureListener {
            confirmandoReporte = false
            binding.btnConfirmarReporte.isEnabled = true
            mostrarDialogoSubiendoConfirmacion(false)
            Toast.makeText(this, "Error al confirmar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoSubiendoConfirmacion(mostrar: Boolean) {
        if (mostrar) {
            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(60, 60, 60, 60)
                gravity = android.view.Gravity.CENTER
                addView(ProgressBar(this@activity_detalle_reporte))
                addView(TextView(this@activity_detalle_reporte).apply { 
                    text = getString(R.string.reportes_confirmando)
                    gravity = android.view.Gravity.CENTER
                    setPadding(0, 20, 0, 0)
                })
            }
            dialogSubiendoConfirmacion = AlertDialog.Builder(this).setView(container).setCancelable(false).show()
        } else {
            dialogSubiendoConfirmacion?.dismiss()
        }
    }

    private class MetodosPagoAdapter : RecyclerView.Adapter<MetodosPagoAdapter.MetodoPagoViewHolder>() {
        private val items = mutableListOf<MetodoPagoResumen>()
        class MetodoPagoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvMetodoNombre: TextView = view.findViewById(R.id.tvMetodoNombre)
            val tvMetodoMonto: TextView = view.findViewById(R.id.tvMetodoMonto)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MetodoPagoViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_resumen_pago_cierre, parent, false)
        )
        override fun onBindViewHolder(holder: MetodoPagoViewHolder, position: Int) {
            val item = items[position]
            holder.tvMetodoNombre.text = item.titulo
            holder.tvMetodoMonto.text = MonedaHelper.formatear(item.monto)
        }
        override fun getItemCount() = items.size
        fun actualizar(nuevos: List<MetodoPagoResumen>) {
            val oldSize = items.size
            val newSize = nuevos.size
            items.clear()
            items.addAll(nuevos)
            when {
                newSize > oldSize -> {
                    // Elementos agregados
                    if (oldSize > 0) notifyItemRangeChanged(0, oldSize)
                    notifyItemRangeInserted(oldSize, newSize - oldSize)
                }
                newSize < oldSize -> {
                    // Elementos removidos
                    notifyItemRangeRemoved(newSize, oldSize - newSize)
                    if (newSize > 0) notifyItemRangeChanged(0, newSize)
                }
                newSize > 0 -> {
                    // Misma cantidad
                    notifyItemRangeChanged(0, newSize)
                }
            }
        }
    }

    private data class MetodoPagoResumen(val titulo: String, val monto: Double)

    private fun obtenerMetodosPagoDinamicos(snapshot: DataSnapshot): List<MetodoPagoResumen> {
        return snapshot.child("resumenMetodos").children.mapNotNull {
            val t = it.child("titulo").obtenerTexto()
            val m = it.child("monto").obtenerDoubleFlexible() ?: 0.0
            if (t.isBlank() || m <= 0.0) null else MetodoPagoResumen(t, m)
        }
    }

    private fun renderizarMetodosPagoDinamicos(metodos: List<MetodoPagoResumen>) {
        binding.rvMetodosPagoDetalle.visibility = if (metodos.isEmpty()) View.GONE else View.VISIBLE
        binding.tvSinMetodosPagoDetalle.visibility = if (metodos.isEmpty()) View.VISIBLE else View.GONE
        metodosPagoAdapter.actualizar(metodos)
    }

    private fun formatearFechaVisible(fecha: String): String = try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fecha)
        SimpleDateFormat("dd MMM yyyy", Locale.Builder().setLanguage("es").setRegion("PE").build()).format(date!!)
    } catch (_: Exception) { fecha }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
