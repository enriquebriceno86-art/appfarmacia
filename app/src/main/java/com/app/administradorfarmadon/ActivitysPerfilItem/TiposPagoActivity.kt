package com.app.administradorfarmadon.ActivitysPerfilItem

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.administradorfarmadon.ActivitysPerfilItem.Reciclers.AdapterTiposPago
import com.app.administradorfarmadon.ClasesDatabase.MovimientoLogger
import com.app.administradorfarmadon.databinding.ActivityTiposPagoBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TiposPagoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTiposPagoBinding
    private lateinit var adapterTiposPago: AdapterTiposPago

    private val listaTiposPago = mutableListOf<MetodoPagoConfig>()
    private val database by lazy { FirebaseDatabase.getInstance() }
    private var creandoMetodoPorDefecto = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTiposPagoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        configurarRecycler()
        configurarAcciones()
        escucharTiposPago()
    }

    private fun configurarToolbar() {
        binding.toolbarTiposPago.setNavigationOnClickListener { finish() }
    }

    private fun configurarRecycler() {
        adapterTiposPago = AdapterTiposPago(
            lista = listaTiposPago,
            onEditarClick = { metodo ->
                BottomSheetCrearTipoPago
                    .newInstance(metodo.id)
                    .show(supportFragmentManager, "BottomSheetCrearTipoPago")
            },
            onEliminarClick = { metodo ->
                eliminarMetodoPago(metodo)
            }
        )

        binding.recyclerTiposPago.layoutManager = if (esTablet()) {
            GridLayoutManager(this, 2)
        } else {
            LinearLayoutManager(this)
        }
        binding.recyclerTiposPago.adapter = adapterTiposPago
        binding.recyclerTiposPago.isNestedScrollingEnabled = false
    }

    private fun configurarAcciones() {
        binding.btnAgregarTipoPagoHeader.setOnClickListener {
            BottomSheetCrearTipoPago()
                .show(supportFragmentManager, "BottomSheetCrearTipoPago")
        }
    }

    private fun escucharTiposPago() {
        database.getReference("ConfiguracionTienda")
            .child("metodosPago")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listaTiposPago.clear()

                    for (child in snapshot.children) {
                        val metodo = child.getValue(MetodoPagoConfig::class.java)
                        if (metodo != null) {
                            listaTiposPago.add(metodo)
                        }
                    }

                    listaTiposPago.sortBy { it.orden }
                    adapterTiposPago.notifyDataSetChanged()
                    actualizarResumen()

                    if (listaTiposPago.isEmpty() && !creandoMetodoPorDefecto) {
                        crearMetodoPagoPorDefecto()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@TiposPagoActivity,
                        "Error al cargar tipos de pago",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun actualizarResumen() {
        val total = listaTiposPago.size
        val activos = listaTiposPago.count { it.activo }
        val conQr = listaTiposPago.count { it.usaQR }
        val mixtos = listaTiposPago.count { it.disponibleMixto }
        val vacio = total == 0

        binding.textCantidadTipos.text = total.toString()
        binding.textActivosCantidad.text = activos.toString()
        binding.textQrCantidad.text = conQr.toString()
        binding.textMixtoCantidad.text = mixtos.toString()

        binding.textResumenHero.text = if (vacio) {
            "Prepara una experiencia de cobro elegante creando tu primer método de pago."
        } else {
            "$activos activos, $conQr con QR y $mixtos disponibles para cobro mixto."
        }

        binding.textSinTiposPago.visibility = if (vacio) View.VISIBLE else View.GONE
        binding.recyclerTiposPago.visibility = if (vacio) View.GONE else View.VISIBLE
    }

    private fun crearMetodoPagoPorDefecto() {
        creandoMetodoPorDefecto = true

        val ref = database.getReference("ConfiguracionTienda")
            .child("metodosPago")

        val nuevoId = ref.push().key
        if (nuevoId.isNullOrBlank()) {
            creandoMetodoPorDefecto = false
            Toast.makeText(
                this,
                "No se pudo crear el tipo de pago por defecto",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val metodoPorDefecto = MetodoPagoConfig(
            id = nuevoId,
            titulo = "Efectivo",
            categoria = "efectivo",
            activo = true,
            permiteVuelto = true,
            solicitaMontoRecibido = true,
            calculaVuelto = true,
            disponibleMixto = true,
            descripcion = "Metodo creado automaticamente por defecto",
            orden = 1
        )

        ref.child(nuevoId)
            .setValue(metodoPorDefecto.toFirebaseMapCompactLocal())
            .addOnSuccessListener {
                creandoMetodoPorDefecto = false
                MovimientoLogger.registrarConSesion(
                    context = this,
                    tipo = "metodo_pago_creado_automatico",
                    modulo = "tipos_pago",
                    titulo = "Metodo de pago por defecto creado: Efectivo",
                    descripcion = "Se creo automaticamente el metodo de pago Efectivo.",
                    referenciaId = nuevoId,
                    extra = mapOf(
                        "seccion" to "metodosPago",
                        "metodoId" to nuevoId,
                        "tituloMetodo" to "Efectivo",
                        "categoria" to "efectivo"
                    )
                )
            }
            .addOnFailureListener {
                creandoMetodoPorDefecto = false
                Toast.makeText(
                    this,
                    "No se pudo crear el tipo de pago por defecto",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun eliminarMetodoPago(metodo: MetodoPagoConfig) {
        if (listaTiposPago.size <= 1) {
            Toast.makeText(
                this,
                "Debe existir al menos un tipo de pago",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar tipo de pago")
            .setMessage("Se quitará \"${metodo.titulo}\" de la lista disponible para cobros.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                database.getReference("ConfiguracionTienda")
                    .child("metodosPago")
                    .child(metodo.id)
                    .removeValue()
                    .addOnSuccessListener {
                        MovimientoLogger.registrarConSesion(
                            context = this,
                            tipo = "metodo_pago_eliminado",
                            modulo = "tipos_pago",
                            titulo = "Metodo de pago eliminado: ${metodo.titulo}",
                            descripcion = "Se elimino el metodo de pago '${metodo.titulo}'.",
                            referenciaId = metodo.id,
                            extra = mapOf(
                                "seccion" to "metodosPago",
                                "metodoId" to metodo.id,
                                "tituloMetodo" to metodo.titulo,
                                "categoria" to metodo.categoria
                            )
                        )
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            "No se pudo eliminar el tipo de pago",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .show()
    }

    private fun MetodoPagoConfig.toFirebaseMapCompactLocal(): Map<String, Any> {
        val map = linkedMapOf<String, Any>(
            "id" to id.trim(),
            "titulo" to titulo.trim(),
            "categoria" to categoria.trim(),
            "activo" to activo,
            "permiteVuelto" to permiteVuelto,
            "solicitaMontoRecibido" to solicitaMontoRecibido,
            "calculaVuelto" to calculaVuelto,
            "permiteReferencia" to permiteReferencia,
            "usaQR" to usaQR,
            "disponibleMixto" to disponibleMixto,
            "orden" to orden
        )

        fun putIfNotBlank(key: String, value: String) {
            val limpio = value.trim()
            if (limpio.isNotEmpty()) {
                map[key] = limpio
            }
        }

        putIfNotBlank("banco", banco)
        putIfNotBlank("tipoCuenta", tipoCuenta)
        putIfNotBlank("numeroCuenta", numeroCuenta)
        putIfNotBlank("titularBanco", titularBanco)
        putIfNotBlank("documentoBanco", documentoBanco)
        putIfNotBlank("telefonoBilletera", telefonoBilletera)
        putIfNotBlank("titularBilletera", titularBilletera)
        putIfNotBlank("aliasBilletera", aliasBilletera)
        putIfNotBlank("qrUrl", qrUrl)
        putIfNotBlank("instrucciones", instrucciones)
        putIfNotBlank("descripcion", descripcion)

        return map
    }

    private fun esTablet(): Boolean = resources.configuration.smallestScreenWidthDp >= 720
}
