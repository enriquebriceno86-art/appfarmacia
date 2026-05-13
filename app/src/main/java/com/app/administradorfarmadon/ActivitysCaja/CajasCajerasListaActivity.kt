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
import com.app.administradorfarmadon.ActivitysCaja.AdapterCajasCajerasLista.CajaCajeraResumen
import com.app.administradorfarmadon.ActivitysCaja.AdapterCajasCajerasLista.TipoEstado
import com.app.administradorfarmadon.ClasesDatabase.FechaHoraServidorHelper
import com.app.administradorfarmadon.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Pantalla "Ver cajas". Lista las cajas de cada cajera con actividad reciente
 * (últimos 7 días). Tap en una caja → CajaDetalleCajeraActivity.
 *
 * Diseño: limpio, una columna, un solo tap para entrar al detalle.
 */
class CajasCajerasListaActivity : AppCompatActivity() {

    companion object {
        private const val DIAS_VENTANA_ACTIVA = 7
    }

    private lateinit var rv: RecyclerView
    private lateinit var etBuscar: TextInputEditText
    private lateinit var layoutVacio: LinearLayout
    private lateinit var tvDetalleVacio: TextView
    private lateinit var progress: ProgressBar
    private lateinit var tvSubtitulo: TextView

    private lateinit var adapter: AdapterCajasCajerasLista
    private val listaTotal = mutableListOf<CajaCajeraResumen>()
    private val listaVisible = mutableListOf<CajaCajeraResumen>()
    private var fechaOficialHoyFirebase: String =
        FechaHoraServidorHelper.estimarMomentoActualDesdeCache().fechaFirebase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cajas_cajeras_lista)

        val toolbar = findViewById<Toolbar>(R.id.toolbarCajasLista)
        toolbar.setNavigationOnClickListener { finish() }

        rv = findViewById(R.id.rvCajasCajeras)
        etBuscar = findViewById(R.id.etBuscarCajera)
        layoutVacio = findViewById(R.id.layoutVacioCajasLista)
        tvDetalleVacio = findViewById(R.id.tvDetalleVacioCajasLista)
        progress = findViewById(R.id.progressCajasLista)
        tvSubtitulo = findViewById(R.id.tvSubtituloCajasLista)

        adapter = AdapterCajasCajerasLista(listaVisible) { caja ->
            abrirDetalleCaja(caja)
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        etBuscar.doAfterTextChanged { editable ->
            aplicarFiltro(editable?.toString().orEmpty())
        }
    }

    override fun onResume() {
        super.onResume()
        inicializarFechaOficialYCargarCajas()
    }

    private fun inicializarFechaOficialYCargarCajas() {
        FechaHoraServidorHelper.obtenerMomentoActual(
            onSuccess = { momento ->
                fechaOficialHoyFirebase = momento.fechaFirebase
                cargarCajas()
            },
            onError = {
                fechaOficialHoyFirebase =
                    FechaHoraServidorHelper.estimarMomentoActualDesdeCache().fechaFirebase
                cargarCajas()
            }
        )
    }

    private fun abrirDetalleCaja(caja: CajaCajeraResumen) {
        val intent = Intent(this, CajaDetalleCajeraActivity::class.java).apply {
            putExtra(CajaDetalleCajeraActivity.EXTRA_ID_CAJERA, caja.idCajera)
            putExtra(CajaDetalleCajeraActivity.EXTRA_NOMBRE_CAJERA, caja.nombreCajera)
        }
        startActivity(intent)
    }

    private fun cargarCajas() {
        progress.visibility = View.VISIBLE
        layoutVacio.visibility = View.GONE
        rv.visibility = View.GONE

        val db = FirebaseDatabase.getInstance()
        // Cargamos en paralelo trabajadores + CorteCaja + ventas de hoy.
        db.getReference("trabajadores").get()
            .addOnSuccessListener { snapTrab ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener

                db.getReference("CorteCaja").child("Cajeras").get()
                    .addOnSuccessListener { snapCorte ->
                        if (isFinishing || isDestroyed) return@addOnSuccessListener
                        construirLista(snapTrab, snapCorte)
                    }
                    .addOnFailureListener {
                        if (isFinishing || isDestroyed) return@addOnFailureListener
                        construirLista(snapTrab, null)
                    }
            }
            .addOnFailureListener {
                if (isFinishing || isDestroyed) return@addOnFailureListener
                construirLista(null, null)
            }
    }

    private fun construirLista(trabajadoresSnap: DataSnapshot?, corteSnap: DataSnapshot?) {
        val hoy = fechaOficialHoyFirebase
        val limite = (FechaHoraServidorHelper.calendarDesdeFechaFirebase(hoy)
            ?: FechaHoraServidorHelper.calendarDesdeTimestamp(
                FechaHoraServidorHelper.estimarMomentoActualDesdeCache().timestampServidorMs
            )).apply {
            add(Calendar.DAY_OF_YEAR, -DIAS_VENTANA_ACTIVA)
        }.time

        listaTotal.clear()
        trabajadoresSnap?.children?.forEach { uSnap ->
            val uid = uSnap.child("id").value?.toString().orEmpty().ifBlank { uSnap.key.orEmpty() }
            val nombre = uSnap.child("usuario").value?.toString().orEmpty()
            if (uid.isBlank() || nombre.isBlank()) return@forEach

            val cajaCorte = corteSnap?.child(uid)
            val ultima = obtenerUltimaActividad(cajaCorte)
            if (ultima == null || ultima.before(limite)) return@forEach

            val (estadoHoy, tuvoTurnoHoy) = derivarEstadoTurnos(cajaCorte?.child(hoy)?.child("turnos"))
            val estadoFinal = when (estadoHoy) {
                TipoEstado.CON_DIFERENCIA -> TipoEstado.CON_DIFERENCIA
                TipoEstado.ABIERTA -> TipoEstado.ABIERTA
                else -> TipoEstado.CERRADA  // si no hay turno hoy pero sí dentro de la ventana
            }

            // Total y conteo de ventas de HOY para mostrar en la fila
            val (total, count) = totalesVentasDelDia(uid, hoy)
            listaTotal.add(
                CajaCajeraResumen(
                    idCajera = uid,
                    nombreCajera = nombre,
                    estado = estadoFinal,
                    totalDia = total,
                    cantidadVentas = count,
                    tuvoTurnoHoy = tuvoTurnoHoy
                )
            )
        }

        // Si tenemos cajas pero las ventas se cargan async, refrescamos
        // la fila cuando llegue cada respuesta. Para simplicidad, primero
        // mostramos con totales 0 y luego completamos.
        ordenarYRender()
        cargarTotalesVentasAsync(hoy)
    }

    /**
     * Llamada inicial síncrona — devuelve 0/0 porque las ventas se traen aparte
     * en `cargarTotalesVentasAsync` para no bloquear esta primera renderización.
     */
    private fun totalesVentasDelDia(idCajera: String, fecha: String): Pair<Double, Int> {
        return 0.0 to 0
    }

    private fun cargarTotalesVentasAsync(fecha: String) {
        if (listaTotal.isEmpty()) return
        val db = FirebaseDatabase.getInstance()
        for ((index, caja) in listaTotal.withIndex()) {
            val idCajera = caja.idCajera
            db.getReference("VentasPorCajera")
                .child(idCajera)
                .child(fecha)
                .get()
                .addOnSuccessListener { snap ->
                    if (isFinishing || isDestroyed) return@addOnSuccessListener
                    var total = 0.0
                    var conteo = 0
                    if (snap.exists()) {
                        for (ventaSnap in snap.children) {
                            val raw = ventaSnap.child("infoVenta").child("total").value
                                ?: ventaSnap.child("total").value
                            val parsed = HistorialVentasRemoteRules.parsearMonto(raw) ?: 0.0
                            total += parsed
                            conteo++
                        }
                    }
                    // Reemplazar el item con totales actualizados
                    if (index < listaTotal.size && listaTotal[index].idCajera == idCajera) {
                        listaTotal[index] = listaTotal[index].copy(
                            totalDia = total,
                            cantidadVentas = conteo
                        )
                        aplicarFiltro(etBuscar.text?.toString().orEmpty())
                    }
                }
        }
    }

    private fun ordenarYRender() {
        // Orden: abiertas primero, luego con diferencia, luego cerradas.
        listaTotal.sortWith(
            compareBy<CajaCajeraResumen>(
                { ordenEstado(it.estado) },
                { it.nombreCajera.lowercase(Locale.getDefault()) }
            )
        )
        aplicarFiltro(etBuscar.text?.toString().orEmpty())
    }

    private fun aplicarFiltro(texto: String) {
        val q = texto.trim().lowercase(Locale.getDefault())
        listaVisible.clear()
        if (q.isBlank()) {
            listaVisible.addAll(listaTotal)
        } else {
            for (caja in listaTotal) {
                if (caja.nombreCajera.lowercase(Locale.getDefault()).contains(q)) {
                    listaVisible.add(caja)
                }
            }
        }
        adapter.notifyDataSetChanged()

        progress.visibility = View.GONE
        if (listaVisible.isEmpty()) {
            rv.visibility = View.GONE
            layoutVacio.visibility = View.VISIBLE
            tvDetalleVacio.text = if (listaTotal.isEmpty()) {
                "Aún ninguna cajera ha trabajado en los últimos 7 días."
            } else {
                "No se encontró ninguna cajera con \"$texto\"."
            }
        } else {
            rv.visibility = View.VISIBLE
            layoutVacio.visibility = View.GONE
        }

        // Subtítulo: cuántas cajas activas hay
        tvSubtitulo.text = when (listaTotal.size) {
            0 -> "Sin cajeras activas"
            1 -> "1 cajera con actividad reciente"
            else -> "${listaTotal.size} cajeras con actividad reciente"
        }
    }

    private fun ordenEstado(estado: TipoEstado): Int = when (estado) {
        TipoEstado.ABIERTA -> 0
        TipoEstado.CON_DIFERENCIA -> 1
        TipoEstado.CERRADA -> 2
        TipoEstado.INACTIVA_HOY -> 3
    }

    private fun obtenerUltimaActividad(
        cajaCorte: DataSnapshot?
    ): Date? {
        if (cajaCorte == null || !cajaCorte.exists()) return null
        var masReciente: Date? = null
        for (fechaSnap in cajaCorte.children) {
            val clave = fechaSnap.key.orEmpty()
            val fecha = FechaHoraServidorHelper.calendarDesdeFechaFirebase(clave)?.time ?: continue
            val turnos = fechaSnap.child("turnos")
            if (!turnos.exists() || !turnos.children.iterator().hasNext()) continue
            if (masReciente == null || fecha.after(masReciente)) {
                masReciente = fecha
            }
        }
        return masReciente
    }

    private fun derivarEstadoTurnos(turnosSnap: DataSnapshot?): Pair<TipoEstado, Boolean> {
        if (turnosSnap == null || !turnosSnap.exists()) return TipoEstado.CERRADA to false

        var hayAbierto = false
        var hayCerradoConDiferencia = false
        var hayCerrado = false

        for (turnoSnap in turnosSnap.children) {
            val estadoTurno = turnoSnap.child("estado").getValue(String::class.java).orEmpty()
                .lowercase(Locale.getDefault())
            val horaCierre = turnoSnap.child("horaCierre").getValue(String::class.java).orEmpty()
            val diferencia = (
                turnoSnap.child("diferenciaEfectivo").value as? Number
                )?.toDouble()
                ?: turnoSnap.child("diferenciaEfectivo").value?.toString()?.replace(",", ".")?.toDoubleOrNull()
                ?: 0.0

            val cerrado = estadoTurno.contains("cerr") || horaCierre.isNotBlank()
            if (!cerrado) hayAbierto = true
            else {
                hayCerrado = true
                if (abs(diferencia) >= 0.01) hayCerradoConDiferencia = true
            }
        }

        val estado = when {
            hayAbierto -> TipoEstado.ABIERTA
            hayCerradoConDiferencia -> TipoEstado.CON_DIFERENCIA
            else -> TipoEstado.CERRADA
        }
        return estado to (hayAbierto || hayCerrado)
    }
}
