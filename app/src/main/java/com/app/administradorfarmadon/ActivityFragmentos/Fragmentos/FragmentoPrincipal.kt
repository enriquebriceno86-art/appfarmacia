package com.app.administradorfarmadon.ActivityFragmentos.Fragmentos

import android.content.Intent
import com.app.administradorfarmadon.ActivityFragmentos.showElegantemente
import com.app.administradorfarmadon.ActivityFragmentos.hideElegantemente
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ActivityFragmentos.Reciclers.AdapterDiasCierreDashboard
import com.app.administradorfarmadon.ActivityFragmentos.Reciclers.AdapterDiasCierreDashboard.DiaCierreUi
import com.app.administradorfarmadon.ActivityFragmentos.Reciclers.AdapterEstadoCajasDashboard
import com.app.administradorfarmadon.ActivityFragmentos.Reciclers.AdapterEstadoCajasDashboard.Estado
import com.app.administradorfarmadon.ActivityFragmentos.Reciclers.AdapterEstadoCajasDashboard.EstadoCajaDashboardUi
import com.app.administradorfarmadon.ActivityFragmentos.Reciclers.AdapterUltimosCierresDashboard
import com.app.administradorfarmadon.ActivityFragmentos.Reciclers.AdapterUltimosCierresDashboard.UltimoCierreDashboardUi
import com.app.administradorfarmadon.ActivitysCaja.CajasCajerasListaActivity
import com.app.administradorfarmadon.ActivitysInicio.AlertasActivity
import com.app.administradorfarmadon.ActivitysPerfilItem.ListaReportesCaja
import android.widget.TextView
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.ClasesDatabase.SessionManager
import com.app.administradorfarmadon.R
import com.app.administradorfarmadon.databinding.FragmentFragmentoPrincipalBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class FragmentoPrincipal : Fragment() {

    // Stubs mantenidos por compatibilidad con helpers Dashboard*Rules y Dashboard*Models
    // (no se usan en el nuevo dashboard, pero los helpers siguen referenciándolos por nombre).
    data class ReporteCierreUi(
        val nombreCajero: String = "",
        val horaApertura: String = "",
        val horaCierre: String = "",
        val cajaInicial: Double = 0.0,
        val ventas: Double = 0.0,
        val esperado: Double = 0.0,
        val cajaFinal: Double = 0.0,
        val diferencia: Double = 0.0,
        val estadoCuadre: String = "",
        val totalEgresos: Double = 0.0,
        val timestampCierre: Long = 0L,
        val idCaja: String = "",
        val idTurno: String = "",
        val fecha: String = "",
        val nombreCaja: String = "",
        val estadoTurno: String = ""
    )

    data class MovimientoUiItem(
        val titulo: String = "",
        val descripcion: String = "",
        val tipo: String = "",
        val monto: Double? = null,
        val autor: String = "",
        val hora: String = "",
        val timestamp: Long = 0L
    )

    private var _binding: FragmentFragmentoPrincipalBinding? = null
    private val binding get() = _binding!!

    private val handlerCargaInicial = Handler(Looper.getMainLooper())
    private var runnableOcultarCargaInicial: Runnable? = null
    private val cargasInicialesPendientes = mutableSetOf<String>()
    private var cargaInicialAnimadaCompletada = false

    private val listaCajasDashboard = mutableListOf<EstadoCajaDashboardUi>()
    private val listaUltimosCierres = mutableListOf<UltimoCierreDashboardUi>()
    private val listaDiasCierre = mutableListOf<DiaCierreUi>()
    // Mapa fechaIso -> lista de cierres de ese día (ya ordenados por hora desc)
    private val cierresPorDia = linkedMapOf<String, MutableList<UltimoCierreDashboardUi>>()
    private var diaCierreSeleccionado: String = ""
    private lateinit var adapterCajasDashboard: AdapterEstadoCajasDashboard
    private lateinit var adapterUltimosCierres: AdapterUltimosCierresDashboard
    private lateinit var adapterDiasCierre: AdapterDiasCierreDashboard
    private var ticketsHoyActual = 0
    private var bajoStockActual = 0
    private var porVencerActual = 0
    private var cajasConDiferenciaActual = 0

    // Caches simples para evitar parpadeos
    private var snapshotCorteCaja: DataSnapshot? = null
    private var snapshotTrabajadores: DataSnapshot? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFragmentoPrincipalBinding.inflate(inflater, container, false)

        configurarCabecera()
        configurarListas()
        configurarAcciones()

        iniciarCargaInicial()
        cargarDatos()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (!isHidden && _binding != null) {
            cargarDatos()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && isResumed && _binding != null) {
            cargarDatos()
        }
    }

    override fun onDestroyView() {
        handlerCargaInicial.removeCallbacksAndMessages(null)
        runnableOcultarCargaInicial = null
        _binding = null
        super.onDestroyView()
    }

    private fun configurarCabecera() {
        SessionManager.cargarSesion(requireContext())

        val nombreUsuario = SessionManager.nombreCajera
            .trim()
            .ifBlank { SessionManager.nombreCajeraEnUso.trim() }

        binding.tvSaludoPrincipal.text = if (nombreUsuario.isBlank()) {
            "Hola"
        } else {
            "Hola, $nombreUsuario"
        }

        binding.tvFechaPrincipal.text = obtenerFechaLarga()
    }

    private fun configurarListas() {
        adapterCajasDashboard = AdapterEstadoCajasDashboard(listaCajasDashboard) { caja ->
            // Tap en una caja del dashboard: abrimos lista de reportes
            // (es la pantalla más adecuada disponible para inspeccionar el detalle)
            abrirListaReportes()
        }
        binding.rvEstadoCajas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEstadoCajas.adapter = adapterCajasDashboard
        binding.rvEstadoCajas.isNestedScrollingEnabled = false

        adapterUltimosCierres = AdapterUltimosCierresDashboard(listaUltimosCierres) { _ ->
            abrirListaReportes()
        }
        binding.rvUltimosCierres.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUltimosCierres.adapter = adapterUltimosCierres
        binding.rvUltimosCierres.isNestedScrollingEnabled = false

        adapterDiasCierre = AdapterDiasCierreDashboard(listaDiasCierre) { dia ->
            if (!isAdded || _binding == null) return@AdapterDiasCierreDashboard
            diaCierreSeleccionado = dia.fechaIso
            renderizarCierresDelDiaSeleccionado()
        }
        val rvDias = binding.root.findViewById<RecyclerView>(R.id.rvDiasCierre)
        rvDias?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvDias?.adapter = adapterDiasCierre
    }

    private fun configurarAcciones() {
        binding.tvVerMasCierre.setOnClickListener { abrirListaReportes() }
        binding.tvVerCajas.setOnClickListener { abrirListaCajasCajeras() }
        binding.cardPrioridadDia.setOnClickListener { abrirAlertas("") }
        binding.tvAccionPrioridadDia.setOnClickListener { abrirAlertas("") }
        binding.rowAlertaCajaDiferencia.setOnClickListener {
            abrirAlertas(AlertasActivity.ALERTA_CAJA_DIFERENCIA)
        }
        binding.rowAlertaBajoStock.setOnClickListener {
            abrirAlertas(AlertasActivity.ALERTA_BAJO_STOCK)
        }
        binding.rowAlertaPorVencer.setOnClickListener {
            abrirAlertas(AlertasActivity.ALERTA_POR_VENCER)
        }
    }

    private fun cargarDatos() {
        cargarVentasDelDia()
        cargarTrabajadoresYCierres()
        cargarAlertasInventario()
    }

    private fun abrirListaReportes() {
        startActivity(Intent(requireContext(), ListaReportesCaja::class.java))
    }

    private fun abrirListaCajasCajeras() {
        startActivity(Intent(requireContext(), CajasCajerasListaActivity::class.java))
    }

    private fun abrirAlertas(tipo: String) {
        startActivity(
            Intent(requireContext(), AlertasActivity::class.java).apply {
                putExtra(AlertasActivity.EXTRA_ALERTA_INICIAL, tipo)
            }
        )
    }

    // -------------------- Carga inicial / loading --------------------

    private fun iniciarCargaInicial() {
        if (cargaInicialAnimadaCompletada) return

        cargasInicialesPendientes.clear()
        cargasInicialesPendientes.addAll(
            listOf(CARGA_VENTAS, CARGA_CAJAS, CARGA_INVENTARIO)
        )

        runnableOcultarCargaInicial?.let(handlerCargaInicial::removeCallbacks)

        binding.layoutLoadingInicialPrincipal.showElegantemente(120L)

        val runnable = Runnable {
            if (!isAdded || _binding == null || cargaInicialAnimadaCompletada) return@Runnable
            ocultarCargaInicial()
        }
        runnableOcultarCargaInicial = runnable
        handlerCargaInicial.postDelayed(runnable, 4000L)
    }

    private fun marcarCargaInicialCompleta(clave: String) {
        if (cargaInicialAnimadaCompletada || _binding == null) return
        cargasInicialesPendientes.remove(clave)
        if (cargasInicialesPendientes.isNotEmpty()) return
        runnableOcultarCargaInicial?.let(handlerCargaInicial::removeCallbacks)
        runnableOcultarCargaInicial = null
        ocultarCargaInicial()
    }

    private fun ocultarCargaInicial() {
        cargaInicialAnimadaCompletada = true
        handlerCargaInicial.postDelayed({
            _binding?.layoutLoadingInicialPrincipal?.hideElegantemente(180L)
        }, 60L)
    }

    // -------------------- Ventas del día (KPIs) --------------------

    private data class TotalesVenta(
        val total: Double,
        val tickets: Int,
        val efectivo: Double,
        val tarjeta: Double,
        val ganancia: Double
    )

    private fun cargarVentasDelDia() {
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hoy = formatoFecha.format(Date())
        val ayer = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }.time.let(formatoFecha::format)

        val ventasRef = FirebaseDatabase.getInstance().getReference("Ventas")

        ventasRef.child(hoy)
            .get()
            .addOnSuccessListener { snapshotHoy ->
                if (!isAdded || _binding == null) return@addOnSuccessListener
                val totalesHoy = sumarTotales(snapshotHoy)

                ventasRef.child(ayer).get()
                    .addOnSuccessListener { snapshotAyer ->
                        if (!isAdded || _binding == null) return@addOnSuccessListener
                        val totalAyer = sumarTotales(snapshotAyer).total
                        renderizarVentas(totalesHoy, totalAyer)
                        actualizarPrioridadDelDia()
                        marcarCargaInicialCompleta(CARGA_VENTAS)
                    }
                    .addOnFailureListener {
                        if (!isAdded || _binding == null) return@addOnFailureListener
                        renderizarVentas(totalesHoy, 0.0)
                        actualizarPrioridadDelDia()
                        marcarCargaInicialCompleta(CARGA_VENTAS)
                    }
            }
            .addOnFailureListener {
                if (!isAdded || _binding == null) return@addOnFailureListener
                renderizarVentas(TotalesVenta(0.0, 0, 0.0, 0.0, 0.0), 0.0)
                actualizarPrioridadDelDia()
                marcarCargaInicialCompleta(CARGA_VENTAS)
            }
    }

    private fun sumarTotales(snapshot: DataSnapshot): TotalesVenta {
        var total = 0.0
        var tickets = 0
        var efectivo = 0.0
        var tarjeta = 0.0
        var ganancia = 0.0

        snapshot.children.forEach { venta ->
            val totalVenta = venta.child("total").obtenerDoubleFlexible() ?: 0.0
            total += totalVenta
            tickets++

            val gananciaVenta = venta.child("ganancia").obtenerDoubleFlexible()
                ?: venta.child("utilidad").obtenerDoubleFlexible() ?: 0.0
            ganancia += gananciaVenta

            // Detectar método de pago
            val metodoPago = (
                venta.child("metodoPago").getValue(String::class.java)
                    ?: venta.child("tipoPago").getValue(String::class.java)
                    ?: venta.child("categoria").getValue(String::class.java)
                    ?: ""
                ).lowercase(Locale.getDefault())

            val esEfectivo = metodoPago.contains("efectivo") || metodoPago.contains("cash")
            val esTarjeta = metodoPago.contains("tarjeta") || metodoPago.contains("card") ||
                metodoPago.contains("debito") || metodoPago.contains("credito") ||
                metodoPago.contains("pos")

            // Soporte pago mixto: leemos detalle si existe
            val pagosNode = venta.child("pagos")
            val mixtoNode = venta.child("mixto")
            when {
                pagosNode.exists() -> {
                    pagosNode.children.forEach { pago ->
                        val monto = pago.child("monto").obtenerDoubleFlexible() ?: 0.0
                        val cat = (
                            pago.child("categoria").getValue(String::class.java)
                                ?: pago.child("metodo").getValue(String::class.java)
                                ?: ""
                            ).lowercase(Locale.getDefault())
                        if (cat.contains("efectivo")) efectivo += monto
                        if (cat.contains("tarjeta") || cat.contains("card") ||
                            cat.contains("debito") || cat.contains("credito")) tarjeta += monto
                    }
                }
                mixtoNode.exists() -> {
                    mixtoNode.children.forEach { pago ->
                        val monto = pago.child("monto").obtenerDoubleFlexible() ?: 0.0
                        val cat = (
                            pago.child("categoria").getValue(String::class.java)
                                ?: pago.child("metodo").getValue(String::class.java)
                                ?: ""
                            ).lowercase(Locale.getDefault())
                        if (cat.contains("efectivo")) efectivo += monto
                        if (cat.contains("tarjeta") || cat.contains("card") ||
                            cat.contains("debito") || cat.contains("credito")) tarjeta += monto
                    }
                }
                esEfectivo -> efectivo += totalVenta
                esTarjeta -> tarjeta += totalVenta
            }
        }

        return TotalesVenta(total, tickets, efectivo, tarjeta, ganancia)
    }

    private fun renderizarVentas(totales: TotalesVenta, totalAyer: Double) {
        val b = _binding ?: return
        ticketsHoyActual = totales.tickets
        b.tvVentaNetaDia.text = MonedaHelper.formatear(totales.total)
        b.tvTicketsCount.text = totales.tickets.toString()
        b.tvGananciaEstimada.text = MonedaHelper.formatear(totales.ganancia)
        b.tvEfectivoDia.text = MonedaHelper.formatear(totales.efectivo)
        b.tvTarjetaDia.text = MonedaHelper.formatear(totales.tarjeta)

        if (totalAyer > 0.009) {
            val porcentaje = (((totales.total - totalAyer) / totalAyer) * 100).toInt()
            val texto = when {
                porcentaje > 0 -> "+$porcentaje% vs ayer"
                porcentaje < 0 -> "-${abs(porcentaje)}% vs ayer"
                else -> "Igual que ayer"
            }
            b.tvComparativaAyer.text = texto
            b.tvComparativaAyer.setTextColor(
                android.graphics.Color.parseColor(
                    when {
                        porcentaje >= 0 -> "#0AA25D"
                        else -> "#C62828"
                    }
                )
            )
            b.tvComparativaAyer.showElegantemente()
        } else if (totales.total > 0.009) {
            b.tvComparativaAyer.text = "Primera venta del día"
            b.tvComparativaAyer.setTextColor(android.graphics.Color.parseColor("#0AA25D"))
            b.tvComparativaAyer.showElegantemente()
        } else {
            b.tvComparativaAyer.hideElegantemente()
        }

        // Mensaje del card "Resumen del día" (solo presente en layout-sw720dp)
        val mensajeResumen = b.root.findViewById<TextView>(R.id.tvMensajeResumenDia)
        if (mensajeResumen != null) {
            mensajeResumen.text = when {
                totales.tickets > 0 ->
                    "Llevas ${totales.tickets} ${if (totales.tickets == 1) "venta" else "ventas"} hoy por un total de ${MonedaHelper.formatear(totales.total)}."
                else ->
                    "Aún no hay ventas registradas hoy. ¡Buen momento para comenzar!"
            }
        }
    }

    // -------------------- Estado de cajas + últimos cierres --------------------

    private fun cargarTrabajadoresYCierres() {
        val database = FirebaseDatabase.getInstance()

        database.getReference("trabajadores").get()
            .addOnSuccessListener { snapshotTrabajadoresOk ->
                if (!isAdded || _binding == null) return@addOnSuccessListener
                snapshotTrabajadores = snapshotTrabajadoresOk

                database.getReference("CorteCaja").child("Cajeras").get()
                    .addOnSuccessListener { snapshotCorte ->
                        if (!isAdded || _binding == null) return@addOnSuccessListener
                        snapshotCorteCaja = snapshotCorte
                        renderizarEstadoCajas(snapshotTrabajadoresOk, snapshotCorte)
                        renderizarUltimosCierres(snapshotCorte)
                        marcarCargaInicialCompleta(CARGA_CAJAS)
                    }
                    .addOnFailureListener {
                        if (!isAdded || _binding == null) return@addOnFailureListener
                        renderizarEstadoCajas(snapshotTrabajadoresOk, null)
                        renderizarUltimosCierres(null)
                        marcarCargaInicialCompleta(CARGA_CAJAS)
                    }
            }
            .addOnFailureListener {
                if (!isAdded || _binding == null) return@addOnFailureListener
                renderizarEstadoCajas(null, null)
                renderizarUltimosCierres(null)
                marcarCargaInicialCompleta(CARGA_CAJAS)
            }
    }

    private fun renderizarEstadoCajas(
        trabajadoresSnap: DataSnapshot?,
        corteSnap: DataSnapshot?
    ) {
        val b = _binding ?: return
        val parserFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hoy = parserFecha.format(Date())

        // Ventana mínima: solo mostramos cajas con actividad real reciente.
        // Si la caja nunca abrió un turno o su última actividad es de hace más de
        // DIAS_VENTANA_CAJA_ACTIVA días, NO debe aparecer en el dashboard.
        val limiteActividad = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -DIAS_VENTANA_CAJA_ACTIVA)
        }.time

        // 1. Lista base de cajas: solo trabajadores con actividad real reciente.
        val cajas = mutableListOf<EstadoCajaDashboardUi>()
        trabajadoresSnap?.children?.forEach { uSnap ->
            val uid = uSnap.child("id").value?.toString().orEmpty().ifBlank {
                uSnap.key.orEmpty()
            }
            val nombre = uSnap.child("usuario").value?.toString().orEmpty()
            if (uid.isBlank() || nombre.isBlank()) return@forEach

            val cajaCorte = corteSnap?.child(uid)
            val ultimaActividad = obtenerFechaUltimaActividad(cajaCorte, parserFecha)

            // Filtros del usuario:
            //  - Si nunca abrió un turno → fuera (no es una caja activa).
            //  - Si su último turno fue hace más de la ventana → fuera.
            if (ultimaActividad == null) return@forEach
            if (ultimaActividad.before(limiteActividad)) return@forEach

            // Estado para hoy (puede no haber turno hoy aunque sí lo tuvo en la ventana).
            val turnosHoy = cajaCorte?.child(hoy)?.child("turnos")
            val (estadoHoy, tieneTurnoHoy) = derivarEstadoCajaDeTurnos(turnosHoy)

            // Si no hay turno hoy pero sí lo tuvo dentro de la ventana, lo marcamos
            // como "Cerrada" (su último turno está cerrado y dentro de los 7 días).
            val estadoFinal = if (estadoHoy == Estado.SIN_ACTIVIDAD) Estado.CERRADA else estadoHoy

            cajas.add(
                EstadoCajaDashboardUi(
                    idCaja = uid,
                    nombreCaja = nombre,
                    estado = estadoFinal,
                    tieneTurnoHoy = tieneTurnoHoy
                )
            )
        }

        // Orden: abiertas primero, luego con diferencia, luego cerradas, luego sin actividad
        val ordenado = cajas.sortedWith(
            compareBy(
                { ordenEstado(it.estado) },
                { it.nombreCaja.lowercase(Locale.getDefault()) }
            )
        )

        listaCajasDashboard.clear()
        listaCajasDashboard.addAll(ordenado)
        adapterCajasDashboard.notifyDataSetChanged()

        // Chips de resumen
        val total = cajas.size
        val abiertas = cajas.count { it.estado == Estado.ABIERTA }
        val cerradas = cajas.count { it.estado == Estado.CERRADA }
        val conDiferencia = cajas.count { it.estado == Estado.CON_DIFERENCIA }
        cajasConDiferenciaActual = conDiferencia

        b.chipTotalCajas.text = "$total ${if (total == 1) "caja" else "cajas"}"
        b.chipCajasAbiertas.text = "$abiertas ${if (abiertas == 1) "abierta" else "abiertas"}"
        b.chipCajasCerradas.text = "$cerradas ${if (cerradas == 1) "cerrada" else "cerradas"}"
        if (conDiferencia > 0) {
            b.chipCajasDiferencia.text =
                "$conDiferencia con diferencia"
            b.chipCajasDiferencia.showElegantemente()
        } else {
            b.chipCajasDiferencia.hideElegantemente()
        }

        // Estado vacío
        if (cajas.isEmpty()) {
            b.rvEstadoCajas.hideElegantemente()
            b.tvSinCajasDashboard.showElegantemente()
        } else {
            b.rvEstadoCajas.showElegantemente()
            b.tvSinCajasDashboard.hideElegantemente()
        }

        // Alerta de caja con diferencia (sincronizada)
        b.tvAlertaCajaDiferencia.text = when {
            conDiferencia == 0 -> "Sin cajas con diferencia hoy"
            conDiferencia == 1 -> "1 caja con diferencia"
            else -> "$conDiferencia cajas con diferencia"
        }
        actualizarPrioridadDelDia()
    }

    private fun ordenEstado(estado: Estado): Int = when (estado) {
        Estado.ABIERTA -> 0
        Estado.CON_DIFERENCIA -> 1
        Estado.CERRADA -> 2
        Estado.SIN_ACTIVIDAD -> 3
    }

    /**
     * Devuelve la fecha del turno más reciente registrado para la caja.
     * Recorre todos los nodos de fecha bajo CorteCaja/Cajeras/<uid>/ y se queda
     * con la fecha más nueva que tenga al menos un turno real.
     * Retorna `null` si la caja nunca abrió un turno.
     */
    private fun obtenerFechaUltimaActividad(
        cajaCorte: DataSnapshot?,
        parserFecha: SimpleDateFormat
    ): Date? {
        if (cajaCorte == null || !cajaCorte.exists()) return null

        var masReciente: Date? = null
        for (fechaSnap in cajaCorte.children) {
            val clave = fechaSnap.key.orEmpty()
            // Solo tomamos en cuenta nodos que parezcan una fecha yyyy-MM-dd con turnos.
            val fecha = runCatching { parserFecha.parse(clave) }.getOrNull() ?: continue
            val turnos = fechaSnap.child("turnos")
            if (!turnos.exists()) continue
            if (!turnos.children.iterator().hasNext()) continue
            if (masReciente == null || fecha.after(masReciente)) {
                masReciente = fecha
            }
        }
        return masReciente
    }

    private fun derivarEstadoCajaDeTurnos(turnosSnap: DataSnapshot?): Pair<Estado, Boolean> {
        if (turnosSnap == null || !turnosSnap.exists()) return Estado.SIN_ACTIVIDAD to false

        var hayAbierto = false
        var hayCerradoConDiferencia = false
        var hayCerrado = false

        for (turnoSnap in turnosSnap.children) {
            val estadoTurno = turnoSnap.child("estado").getValue(String::class.java).orEmpty()
                .lowercase(Locale.getDefault())
            val horaCierre = turnoSnap.child("horaCierre").getValue(String::class.java).orEmpty()
            val diferencia = turnoSnap.child("diferenciaEfectivo").obtenerDoubleFlexible() ?: 0.0

            val cerrado = estadoTurno.contains("cerr") || horaCierre.isNotBlank()
            if (!cerrado) {
                hayAbierto = true
            } else {
                hayCerrado = true
                if (abs(diferencia) >= 0.01) hayCerradoConDiferencia = true
            }
        }

        val estado = when {
            hayAbierto -> Estado.ABIERTA
            hayCerradoConDiferencia -> Estado.CON_DIFERENCIA
            hayCerrado -> Estado.CERRADA
            else -> Estado.SIN_ACTIVIDAD
        }
        return estado to (hayAbierto || hayCerrado)
    }

    private fun renderizarUltimosCierres(corteSnap: DataSnapshot?) {
        val b = _binding ?: return
        val parserFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hoyStr = parserFecha.format(Date())

        // Misma ventana que "Estado de cajas".
        val limiteActividad = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -DIAS_VENTANA_CAJA_ACTIVA)
        }.time

        // 1. Recolectamos TODOS los cierres de la ventana, ya agrupados por fecha.
        cierresPorDia.clear()
        if (corteSnap != null && corteSnap.exists()) {
            for (cajaSnap in corteSnap.children) {
                val idCaja = cajaSnap.key.orEmpty()
                val nombreCaja = cajaSnap.child("nombre").getValue(String::class.java)
                    .orEmpty()
                    .ifBlank { obtenerNombreTrabajador(idCaja) }

                for (fechaSnap in cajaSnap.children) {
                    val claveFecha = fechaSnap.key.orEmpty()
                    val fechaParseada = runCatching { parserFecha.parse(claveFecha) }.getOrNull()
                        ?: continue
                    if (fechaParseada.before(limiteActividad)) continue

                    val turnos = fechaSnap.child("turnos")
                    if (!turnos.exists()) continue

                    for (turnoSnap in turnos.children) {
                        val horaCierre = turnoSnap.child("horaCierre")
                            .getValue(String::class.java).orEmpty()
                        val estadoTurno = turnoSnap.child("estado")
                            .getValue(String::class.java).orEmpty()
                            .lowercase(Locale.getDefault())
                        val cerrado = estadoTurno.contains("cerr") || horaCierre.isNotBlank()
                        if (!cerrado) continue

                        val diferencia = turnoSnap.child("diferenciaEfectivo")
                            .obtenerDoubleFlexible() ?: 0.0
                        val tsServer = turnoSnap.child("timestampCierreServidor")
                            .getValue(Long::class.java) ?: 0L
                        val tsLocal = turnoSnap.child("timestampCierre")
                            .getValue(Long::class.java) ?: 0L
                        val timestampCierre = if (tsServer > 0L) tsServer else tsLocal

                        val (texto, color) = formatearDiferenciaUi(diferencia)

                        // Cuando ya filtramos por día, mostramos solo la hora; el día
                        // está implícito en el chip seleccionado.
                        val horaTexto = horaCierre.ifBlank { "--:--" }

                        val item = UltimoCierreDashboardUi(
                            idCaja = idCaja,
                            idTurno = turnoSnap.key.orEmpty(),
                            fecha = claveFecha,
                            nombreCaja = nombreCaja,
                            horaCierre = horaTexto,
                            diferenciaTexto = texto,
                            diferenciaColorHex = color
                        )

                        val lista = cierresPorDia.getOrPut(claveFecha) { mutableListOf() }
                        lista.add(item)
                        cierreTimestamps[turnoSnap.key.orEmpty() + "@" + idCaja + "@" + claveFecha] =
                            timestampCierre
                    }
                }
            }
        }

        // 2. Ordenar internamente cada día por timestamp descendente.
        for ((_, listaDia) in cierresPorDia) {
            listaDia.sortByDescending { item ->
                val ts = cierreTimestamps[item.idTurno + "@" + item.idCaja + "@" + item.fecha]
                if (ts != null && ts > 0L) ts
                else runCatching { parserFecha.parse(item.fecha)?.time }.getOrNull() ?: 0L
            }
        }

        // 3. Construir la lista de días disponibles, ordenada de más reciente a más antiguo.
        val diasOrdenados = cierresPorDia.keys
            .mapNotNull { fechaStr ->
                val parsed = runCatching { parserFecha.parse(fechaStr) }.getOrNull()
                if (parsed != null) fechaStr to parsed else null
            }
            .sortedByDescending { it.second.time }

        // 4. Determinar día seleccionado:
        //    - Mantener el seleccionado anterior si sigue existiendo
        //    - Si no, intentar HOY
        //    - Si tampoco, el más reciente
        val diaPrev = diaCierreSeleccionado
        diaCierreSeleccionado = when {
            diasOrdenados.any { it.first == diaPrev } && diaPrev.isNotBlank() -> diaPrev
            diasOrdenados.any { it.first == hoyStr } -> hoyStr
            diasOrdenados.isNotEmpty() -> diasOrdenados.first().first
            else -> ""
        }

        // 5. Construir chips de días.
        listaDiasCierre.clear()
        for ((fechaStr, fechaParseada) in diasOrdenados) {
            val conteo = cierresPorDia[fechaStr]?.size ?: 0
            listaDiasCierre.add(
                DiaCierreUi(
                    fechaIso = fechaStr,
                    etiqueta = etiquetaCortaDeDia(fechaParseada, hoyStr, parserFecha),
                    conteo = conteo,
                    seleccionado = (fechaStr == diaCierreSeleccionado)
                )
            )
        }
        adapterDiasCierre.notifyDataSetChanged()

        val rvDias = b.root.findViewById<RecyclerView>(R.id.rvDiasCierre)
        rvDias?.visibility = if (listaDiasCierre.size > 1) View.VISIBLE else View.GONE

        // 6. Render del día seleccionado
        renderizarCierresDelDiaSeleccionado()
    }

    private fun renderizarCierresDelDiaSeleccionado() {
        val b = _binding ?: return
        val cierresDelDia = cierresPorDia[diaCierreSeleccionado].orEmpty()

        listaUltimosCierres.clear()
        listaUltimosCierres.addAll(cierresDelDia.take(MAX_ULTIMOS_CIERRES))
        adapterUltimosCierres.notifyDataSetChanged()

        if (listaUltimosCierres.isEmpty()) {
            b.rvUltimosCierres.hideElegantemente()
            b.tvSinUltimosCierres.showElegantemente()
        } else {
            b.rvUltimosCierres.showElegantemente()
            b.tvSinUltimosCierres.hideElegantemente()
        }
    }

    private fun etiquetaCortaDeDia(
        fecha: Date,
        hoyStr: String,
        parserFecha: SimpleDateFormat
    ): String {
        val fechaStr = parserFecha.format(fecha)
        if (fechaStr == hoyStr) return "Hoy"
        val ayerStr = parserFecha.format(
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
        )
        if (fechaStr == ayerStr) return "Ayer"
        val locale = Locale.forLanguageTag("es-ES")
        val etiqueta = SimpleDateFormat("EEE d", locale).format(fecha)
        return etiqueta.replaceFirstChar { it.lowercaseChar() }
    }

    private val cierreTimestamps = mutableMapOf<String, Long>()

    private fun formatearDiferenciaUi(diferencia: Double): Pair<String, String> {
        val texto = "Bs " + String.format(Locale.US, "%.2f", abs(diferencia))
        val color = when {
            abs(diferencia) < 0.01 -> "#0AA25D"
            diferencia > 0 -> "#0AA25D"  // Sobró → no es problema crítico
            else -> "#C62828"             // Faltó dinero
        }
        val signo = when {
            abs(diferencia) < 0.01 -> "Bs 0.00"
            diferencia > 0 -> "+$texto"
            else -> "-$texto"
        }
        return signo to color
    }

    private fun obtenerNombreTrabajador(idTrabajador: String): String {
        if (idTrabajador.isBlank()) return ""
        val snap = snapshotTrabajadores ?: return ""
        if (!snap.exists()) return ""
        for (uSnap in snap.children) {
            val uid = uSnap.child("id").value?.toString().orEmpty().ifBlank { uSnap.key.orEmpty() }
            if (uid == idTrabajador) {
                return uSnap.child("usuario").value?.toString().orEmpty()
            }
        }
        return ""
    }

    // -------------------- Alertas de inventario --------------------

    private fun cargarAlertasInventario() {
        FirebaseDatabase.getInstance().getReference("Inventario").child("Productos").get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded || _binding == null) return@addOnSuccessListener
                renderizarAlertasInventario(snapshot)
                marcarCargaInicialCompleta(CARGA_INVENTARIO)
            }
            .addOnFailureListener {
                if (!isAdded || _binding == null) return@addOnFailureListener
                renderizarAlertasInventario(null)
                marcarCargaInicialCompleta(CARGA_INVENTARIO)
            }
    }

    private fun renderizarAlertasInventario(snapshot: DataSnapshot?) {
        val b = _binding ?: return

        var bajoStock = 0
        var porVencer = 0

        if (snapshot != null && snapshot.exists()) {
            val hoy = Calendar.getInstance().time
            val limiteVencimiento = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, DIAS_PROXIMO_VENCIMIENTO)
            }.time
            val parserFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            for (productoSnap in snapshot.children) {
                val cantidad = productoSnap.child("cantidadinicial").value?.toString()?.toIntOrNull() ?: 0
                val stockMinimo = productoSnap.child("stockminimo").value?.toString()?.toIntOrNull() ?: 0
                if (stockMinimo > 0 && cantidad <= stockMinimo) bajoStock++

                val vencimientoStr = productoSnap.child("vencimiento").value?.toString().orEmpty()
                if (vencimientoStr.isNotBlank()) {
                    val fechaVenc = runCatching { parserFecha.parse(vencimientoStr) }.getOrNull()
                    if (fechaVenc != null &&
                        !fechaVenc.before(hoy) &&
                        fechaVenc.before(limiteVencimiento)
                    ) porVencer++
                }

                // Lotes con vencimiento (cuando los productos los manejan por lote)
                val lotesSnap = productoSnap.child("lotes")
                if (lotesSnap.exists()) {
                    for (loteSnap in lotesSnap.children) {
                        val vencLote = loteSnap.child("vencimiento").value?.toString().orEmpty()
                        if (vencLote.isBlank()) continue
                        val fecha = runCatching { parserFecha.parse(vencLote) }.getOrNull() ?: continue
                        if (!fecha.before(hoy) && fecha.before(limiteVencimiento)) {
                            // Solo contamos un producto una sola vez si ya pasó por el campo principal
                            // Para mantener el conteo simple: cada lote suma 1.
                            porVencer++
                        }
                    }
                }
            }
        }

        b.tvAlertaBajoStock.text = if (bajoStock == 1) {
            "1 producto con bajo stock"
        } else {
            "$bajoStock productos con bajo stock"
        }
        b.tvAlertaPorVencer.text = if (porVencer == 1) {
            "1 producto por vencer"
        } else {
            "$porVencer productos por vencer"
        }

        // Calcular cuántas cajas tienen diferencia HOY (basado en lo que ya cargamos
        // en "Estado de cajas").
        bajoStockActual = bajoStock
        porVencerActual = porVencer
        val cajasConDif = listaCajasDashboard.count { it.estado == Estado.CON_DIFERENCIA }

        // Mostrar cada fila SOLO si su conteo es > 0 (evita filas "vacías" como
        // "Sin productos por vencer" cuando todo está bien).
        if (bajoStock > 0) b.rowAlertaBajoStock.showElegantemente() else b.rowAlertaBajoStock.hideElegantemente()
        if (porVencer > 0) b.rowAlertaPorVencer.showElegantemente() else b.rowAlertaPorVencer.hideElegantemente()
        if (cajasConDif > 0) b.rowAlertaCajaDiferencia.showElegantemente() else b.rowAlertaCajaDiferencia.hideElegantemente()

        // El mensaje "Sin alertas por ahora" aparece solo cuando NO hay nada que
        // alertar — sustituye a las 3 filas vacías.
        val sinAlertas = bajoStock == 0 && porVencer == 0 && cajasConDif == 0
        if (sinAlertas) b.tvSinAlertasDashboard.showElegantemente() else b.tvSinAlertasDashboard.hideElegantemente()
        actualizarPrioridadDelDia()
    }

    private fun actualizarPrioridadDelDia() {
        val b = _binding ?: return
        val titulo: String
        val resumen: String
        val accion: String
        val tipoAlerta: String

        when {
            cajasConDiferenciaActual > 0 -> {
                titulo = "Revisar cajas con diferencia"
                resumen = if (cajasConDiferenciaActual == 1) {
                    "Hay 1 caja que no cuadra y conviene revisarla primero."
                } else {
                    "Hay $cajasConDiferenciaActual cajas con diferencia que necesitan revisión."
                }
                accion = "Abrir reportes"
                tipoAlerta = AlertasActivity.ALERTA_CAJA_DIFERENCIA
            }
            porVencerActual > 0 -> {
                titulo = "Mover primero productos por vencer"
                resumen = if (porVencerActual == 1) {
                    "Hay 1 producto o lote próximo a vencer. Conviene revisarlo hoy."
                } else {
                    "Hay $porVencerActual productos o lotes próximos a vencer. Conviene revisarlos hoy."
                }
                accion = "Ver alertas"
                tipoAlerta = AlertasActivity.ALERTA_POR_VENCER
            }
            bajoStockActual > 0 -> {
                titulo = "Conviene reponer algunos productos"
                resumen = if (bajoStockActual == 1) {
                    "Hay 1 producto con stock bajo. Vale la pena revisarlo cuanto antes."
                } else {
                    "Hay $bajoStockActual productos con stock bajo. Vale la pena revisarlos cuanto antes."
                }
                accion = "Abrir inventario"
                tipoAlerta = AlertasActivity.ALERTA_BAJO_STOCK
            }
            ticketsHoyActual == 0 -> {
                titulo = "Todo listo para comenzar"
                resumen = "Todavía no hay ventas hoy y no se detectan alertas urgentes."
                accion = "Ver alertas"
                tipoAlerta = ""
            }
            else -> {
                titulo = "Todo en orden por ahora"
                resumen = if (ticketsHoyActual == 1) {
                    "Ya registraste 1 venta hoy y no hay alertas urgentes."
                } else {
                    "Ya registraste $ticketsHoyActual ventas hoy y no hay alertas urgentes."
                }
                accion = "Ver alertas"
                tipoAlerta = ""
            }
        }

        b.tvTituloPrioridadDia.text = titulo
        b.tvResumenPrioridadDia.text = resumen
        b.tvAccionPrioridadDia.text = accion
        b.cardPrioridadDia.setOnClickListener { abrirAlertas(tipoAlerta) }
        b.tvAccionPrioridadDia.setOnClickListener { abrirAlertas(tipoAlerta) }
    }

    // -------------------- Helpers --------------------

    private fun DataSnapshot.obtenerDoubleFlexible(): Double? {
        return when (val v = value) {
            null -> null
            is Number -> v.toDouble()
            is String -> v.replace(",", ".").trim().toDoubleOrNull()
            else -> v.toString().replace(",", ".").trim().toDoubleOrNull()
        }
    }

    private fun obtenerFechaLarga(): String {
        val localeEs = Locale.forLanguageTag("es-ES")
        val fecha = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", localeEs).format(Date())
        return fecha.replaceFirstChar { it.uppercaseChar() }
    }

    companion object {
        private const val CARGA_VENTAS = "ventas"
        private const val CARGA_CAJAS = "cajas"
        private const val CARGA_INVENTARIO = "inventario"
        private const val MAX_ULTIMOS_CIERRES = 4
        private const val DIAS_PROXIMO_VENCIMIENTO = 30
        // Solo se muestran cajas con un turno abierto en los últimos N días.
        // Cajas sin actividad o con actividad más antigua quedan ocultas.
        private const val DIAS_VENTANA_CAJA_ACTIVA = 7
    }
}
