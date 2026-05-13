package com.app.administradorfarmadon.ActivitysInicio

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.administradorfarmadon.ActivityFragmentos.ActivityFragmentos
import com.app.administradorfarmadon.ActivitysPerfilItem.ListaReportesCaja
import com.app.administradorfarmadon.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class AlertasActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ALERTA_INICIAL = "extra_alerta_inicial"
        const val ALERTA_BAJO_STOCK = "bajo_stock"
        const val ALERTA_POR_VENCER = "por_vencer"
        const val ALERTA_CAJA_DIFERENCIA = "caja_diferencia"
    }

    private lateinit var tvResumenAlertas: TextView
    private lateinit var tvBajoStock: TextView
    private lateinit var tvPorVencer: TextView
    private lateinit var tvCajaDiferencia: TextView
    private lateinit var tvDescripcionPantalla: TextView
    private lateinit var btnIrInventarioBajoStock: MaterialButton
    private lateinit var btnIrInventarioPorVencer: MaterialButton
    private lateinit var btnIrReportesDiferencia: MaterialButton

    private var conteoBajoStock = 0
    private var conteoPorVencer = 0
    private var conteoCajasConDiferencia = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alertas)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        inicializarVistas()
        configurarToolbar()
        configurarAcciones()
        renderizarContextoInicial()
        cargarAlertas()
    }

    private fun inicializarVistas() {
        tvResumenAlertas = findViewById(R.id.tvResumenAlertas)
        tvBajoStock = findViewById(R.id.tvAlertaBajoStockDetalle)
        tvPorVencer = findViewById(R.id.tvAlertaPorVencerDetalle)
        tvCajaDiferencia = findViewById(R.id.tvAlertaCajaDiferenciaDetalle)
        tvDescripcionPantalla = findViewById(R.id.tvDescripcionAlertas)
        btnIrInventarioBajoStock = findViewById(R.id.btnIrInventarioBajoStock)
        btnIrInventarioPorVencer = findViewById(R.id.btnIrInventarioPorVencer)
        btnIrReportesDiferencia = findViewById(R.id.btnIrReportesDiferencia)
    }

    private fun configurarToolbar() {
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarAlertas)
            .setNavigationOnClickListener { finish() }
    }

    private fun configurarAcciones() {
        btnIrInventarioBajoStock.setOnClickListener {
            abrirFragmentos(R.id.nav_productos)
        }
        btnIrInventarioPorVencer.setOnClickListener {
            abrirFragmentos(R.id.nav_productos)
        }
        btnIrReportesDiferencia.setOnClickListener {
            startActivity(Intent(this, ListaReportesCaja::class.java))
        }
    }

    private fun renderizarContextoInicial() {
        val alertaInicial = intent.getStringExtra(EXTRA_ALERTA_INICIAL).orEmpty()
        tvDescripcionPantalla.text = when (alertaInicial) {
            ALERTA_BAJO_STOCK -> "Aqui puedes ver que productos conviene reponer primero."
            ALERTA_POR_VENCER -> "Aqui puedes revisar que productos y lotes necesitan salida rapida."
            ALERTA_CAJA_DIFERENCIA -> "Aqui puedes revisar que cajas o turnos necesitan atencion."
            else -> "Aqui puedes revisar en un solo lugar lo mas importante del dia."
        }
    }

    private fun cargarAlertas() {
        cargarAlertasInventario()
        cargarAlertasCaja()
    }

    private fun cargarAlertasInventario() {
        FirebaseDatabase.getInstance().getReference("Inventario").child("Productos").get()
            .addOnSuccessListener { snapshot ->
                val conteos = contarAlertasInventario(snapshot)
                conteoBajoStock = conteos.first
                conteoPorVencer = conteos.second

                tvBajoStock.text = when (conteoBajoStock) {
                    0 -> "Todo bien por ahora. No hay productos con stock bajo."
                    1 -> "Hay 1 producto con stock bajo. Conviene reponerlo hoy."
                    else -> "Hay $conteoBajoStock productos con stock bajo. Conviene revisarlos hoy."
                }

                tvPorVencer.text = when (conteoPorVencer) {
                    0 -> "No hay productos por vencer pronto."
                    1 -> "Hay 1 producto o lote proximo a vencer. Conviene moverlo hoy."
                    else -> "Hay $conteoPorVencer productos o lotes proximos a vencer. Conviene revisarlos hoy."
                }

                actualizarBotonesAlertas()
                actualizarResumen()
            }
            .addOnFailureListener {
                conteoBajoStock = 0
                conteoPorVencer = 0
                tvBajoStock.text = "No se pudo cargar el bajo stock por ahora."
                tvPorVencer.text = "No se pudo cargar los vencimientos por ahora."
                actualizarBotonesAlertas()
                actualizarResumen()
            }
    }

    private fun cargarAlertasCaja() {
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        FirebaseDatabase.getInstance().getReference("CorteCaja").child("Cajeras").get()
            .addOnSuccessListener { snapshot ->
                var cajasConDiferencia = 0
                for (cajaSnap in snapshot.children) {
                    val turnosHoy = cajaSnap.child(fechaHoy).child("turnos")
                    for (turnoSnap in turnosHoy.children) {
                        val diferencia = turnoSnap.child("diferenciaEfectivo").obtenerDoubleFlexible() ?: 0.0
                        val estadoCuadre = turnoSnap.child("estadoCuadre").value?.toString().orEmpty()
                        if (abs(diferencia) > 0.009 ||
                            (!estadoCuadre.equals("cuadre_exacto", ignoreCase = true) &&
                                estadoCuadre.isNotBlank())
                        ) {
                            cajasConDiferencia++
                        }
                    }
                }

                conteoCajasConDiferencia = cajasConDiferencia
                tvCajaDiferencia.text = when (conteoCajasConDiferencia) {
                    0 -> "No hay cajas con diferencia hoy."
                    1 -> "Hay 1 caja con diferencia hoy. Conviene revisarla cuanto antes."
                    else -> "Hay $conteoCajasConDiferencia cajas con diferencia hoy. Conviene revisarlas cuanto antes."
                }

                actualizarBotonesAlertas()
                actualizarResumen()
            }
            .addOnFailureListener {
                conteoCajasConDiferencia = 0
                tvCajaDiferencia.text = "No se pudo cargar las diferencias de caja por ahora."
                actualizarBotonesAlertas()
                actualizarResumen()
            }
    }

    private fun contarAlertasInventario(snapshot: DataSnapshot): Pair<Int, Int> {
        var bajoStock = 0
        var porVencer = 0
        val hoy = Calendar.getInstance().time
        val limiteVencimiento = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 30)
        }.time
        val parserFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (productoSnap in snapshot.children) {
            val cantidad = productoSnap.child("cantidadinicial").value?.toString()?.toIntOrNull() ?: 0
            val stockMinimo = productoSnap.child("stockminimo").value?.toString()?.toIntOrNull() ?: 0
            if (stockMinimo > 0 && cantidad <= stockMinimo) bajoStock++

            val vencimientoStr = productoSnap.child("vencimiento").value?.toString().orEmpty()
            if (vencimientoStr.isNotBlank()) {
                val fechaVenc = runCatching { parserFecha.parse(vencimientoStr) }.getOrNull()
                if (fechaVenc != null && !fechaVenc.before(hoy) && fechaVenc.before(limiteVencimiento)) {
                    porVencer++
                }
            }

            val lotesSnap = productoSnap.child("lotes")
            if (lotesSnap.exists()) {
                for (loteSnap in lotesSnap.children) {
                    val vencLote = loteSnap.child("vencimiento").value?.toString().orEmpty()
                    if (vencLote.isBlank()) continue
                    val fecha = runCatching { parserFecha.parse(vencLote) }.getOrNull() ?: continue
                    if (!fecha.before(hoy) && fecha.before(limiteVencimiento)) {
                        porVencer++
                    }
                }
            }
        }

        return bajoStock to porVencer
    }

    private fun actualizarResumen() {
        val activas = listOf(
            conteoBajoStock > 0,
            conteoPorVencer > 0,
            conteoCajasConDiferencia > 0
        ).count { it }

        tvResumenAlertas.text = when (activas) {
            0 -> "Todo en orden por ahora"
            1 -> "Hay 1 punto que conviene revisar hoy"
            else -> "Hay $activas puntos que conviene revisar hoy"
        }
    }

    private fun actualizarBotonesAlertas() {
        btnIrInventarioBajoStock.text = if (conteoBajoStock > 0) {
            "Ver productos con stock bajo"
        } else {
            "Abrir inventario"
        }

        btnIrInventarioPorVencer.text = if (conteoPorVencer > 0) {
            "Revisar productos por vencer"
        } else {
            "Abrir inventario"
        }

        btnIrReportesDiferencia.text = if (conteoCajasConDiferencia > 0) {
            "Revisar cajas con diferencia"
        } else {
            "Abrir reportes"
        }
    }

    private fun abrirFragmentos(destino: Int) {
        startActivity(
            Intent(this, ActivityFragmentos::class.java).apply {
                putExtra(ActivityFragmentos.EXTRA_DESTINO_INICIAL, destino)
            }
        )
    }

    private fun DataSnapshot.obtenerDoubleFlexible(): Double? {
        return when (val v = value) {
            null -> null
            is Number -> v.toDouble()
            is String -> v.replace(",", ".").trim().toDoubleOrNull()
            else -> v.toString().replace(",", ".").trim().toDoubleOrNull()
        }
    }
}
