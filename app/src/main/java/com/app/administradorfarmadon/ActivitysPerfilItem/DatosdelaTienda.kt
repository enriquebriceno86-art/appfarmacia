package com.app.administradorfarmadon.ActivitysPerfilItem

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.content.Intent
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.administradorfarmadon.ActivitysPerfilItem.Reciclers.AdapterTiposPago
import com.app.administradorfarmadon.ClasesDatabase.MovimientoLogger
import com.app.administradorfarmadon.ClasesDatabase.SessionManager
import com.app.administradorfarmadon.R
import com.app.administradorfarmadon.databinding.ActivityDatosdelaTiendaBinding
import com.app.administradorfarmadon.databinding.BottomsheetCrearTipoPagoBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.UUID
import kotlin.math.max

class DatosdelaTienda : AppCompatActivity() {

    companion object {
        const val EXTRA_ABRIR_SECCION = "extra_abrir_seccion"
        const val SECCION_METODOS_PAGO = "seccion_metodos_pago"
    }

    private data class SeccionEditableTienda(
        val titulo: String,
        val subtitulo: String,
        val content: View,
        val botonEditar: View
    )

    private lateinit var binding: ActivityDatosdelaTiendaBinding

    private lateinit var adapterTiposPago: AdapterTiposPago
    private val listaTiposPago = mutableListOf<MetodoPagoConfig>()

    private val database by lazy { FirebaseDatabase.getInstance() }

    private var monedaSeleccionadaCodigo: String = ""
    private var monedaSeleccionadaSimbolo: String = ""

    private var creandoMetodoPorDefecto = false

    private fun aplicarCierreDropdownAlRecibirFoco(autoComplete: AutoCompleteTextView) {
        autoComplete.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                (v as? AutoCompleteTextView)?.dismissDropDown()
            }
        }
    }
    private var cargandoFormulario = false
    private var guardandoDatos = false
    private var hayDatosGuardados = false

    private var snapshotInicial: ConfiguracionTienda? = null
    private val handlerAnimacionSubida = Handler(Looper.getMainLooper())
    private var runnableAnimacionSubida: Runnable? = null
    private var mensajeBaseAnimacionSubida: String = "Subiendo datos"
    private var bottomSheetSeccionActual: BottomSheetDialog? = null
    private var seccionesEditables: List<SeccionEditableTienda> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDatosdelaTiendaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bottomInset = max(systemBars.bottom, ime.bottom)
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomInset)
            insets
        }

        configurarVistaInicial()
        configurarEventos()
        cargarTiposPago()
        cargarDatosTienda()

        binding.edtSimboloMoneda.setOnClickListener {
            if (binding.edtSimboloMoneda.text.toString().isEmpty())mensaje("Escoge una moneda")
        }
    }

    fun mensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }



    private fun configurarVistaInicial() {
        binding.layoutPorcentajeImpuesto.visibility = View.GONE
        binding.btnGuardarDatosTienda.isEnabled = false
        binding.btnGuardarDatosTienda.alpha = 0.9f
        binding.btnGuardarDatosTienda.text = "Guardar datos de la farmacia"

        binding.toolbar.setNavigationOnClickListener { finish() }

        configurarRecyclerTiposPago()
        configurarSelectorMoneda()
        configurarAutoScrollEnCampos()
        configurarResumenesYBottomSheets()
    }



    private fun configurarResumenesYBottomSheets() {
        seccionesEditables = listOfNotNull(
            SeccionEditableTienda(
                titulo = "Editar información del negocio",
                subtitulo = "Actualiza los datos visibles en tickets y comprobantes",
                content = binding.layoutContenidoNegocio ?: return,
                botonEditar = binding.btnEditarNegocio ?: return
            ),
            SeccionEditableTienda(
                titulo = "Editar ubicación",
                subtitulo = "Actualiza la dirección y la referencia geográfica de la farmacia",
                content = binding.layoutContenidoUbicacion ?: return,
                botonEditar = binding.btnEditarUbicacion ?: return
            ),
            SeccionEditableTienda(
                titulo = "Editar facturación y moneda",
                subtitulo = "Configura impuestos, moneda y el mensaje final del ticket",
                content = binding.layoutContenidoFacturacion ?: return,
                botonEditar = binding.btnEditarFacturacion ?: return
            )
        )

        seccionesEditables.forEach { section ->
            section.content.visibility = View.GONE
            section.botonEditar.setOnClickListener { mostrarBottomSheetSeccion(section) }
        }

        actualizarResumenesDatosTienda()
    }

    private fun mostrarBottomSheetSeccion(section: SeccionEditableTienda) {
        if (bottomSheetSeccionActual?.isShowing == true) return

        val contentView = section.content
        val originalParent = contentView.parent as? ViewGroup ?: return
        val originalIndex = originalParent.indexOfChild(contentView)
        originalParent.removeView(contentView)
        contentView.visibility = View.VISIBLE

        val sheetView = layoutInflater.inflate(R.layout.bottomsheet_editar_seccion_producto, null)
        val titleView = sheetView.findViewById<TextView>(R.id.tvTituloBottomSheetEditar)
        val subtitleView = sheetView.findViewById<TextView>(R.id.tvSubtituloBottomSheetEditar)
        val container = sheetView.findViewById<FrameLayout>(R.id.contenedorBottomSheetEditar)
        val buttonClose = sheetView.findViewById<MaterialButton>(R.id.btnCancelarBottomSheetEditar)
        val buttonDone = sheetView.findViewById<MaterialButton>(R.id.btnGuardarBottomSheetEditar)

        titleView.text = section.titulo
        subtitleView.text = section.subtitulo
        container.addView(contentView)

        val dialog = BottomSheetDialog(this)
        bottomSheetSeccionActual = dialog
        dialog.setContentView(sheetView)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnDismissListener {
            container.removeAllViews()
            if (contentView.parent != null) {
                (contentView.parent as? ViewGroup)?.removeView(contentView)
            }
            originalParent.addView(contentView, originalIndex)
            contentView.visibility = View.GONE
            actualizarResumenesDatosTienda()
            bottomSheetSeccionActual = null
        }

        buttonClose.setOnClickListener { dialog.dismiss() }
        buttonDone.setOnClickListener {
            actualizarResumenesDatosTienda()
            actualizarEstadoBotonGuardar()
            dialog.dismiss()
        }

        dialog.show()
        dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.let { sheet ->
            BottomSheetBehavior.from(sheet).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
            }
            sheet.layoutParams = sheet.layoutParams.apply {
                height = (resources.displayMetrics.heightPixels * 0.92f).toInt()
            }
            sheet.requestLayout()
        }
    }

    private fun actualizarResumenesDatosTienda() {
        val nombre = binding.edtNombreTienda.text?.toString().orEmpty().trim().ifBlank { "Sin nombre comercial" }
        val razon = binding.edtRazonSocial.text?.toString().orEmpty().trim().ifBlank { "Sin razón social" }
        val fiscal = binding.edtIdentificacionFiscal.text?.toString().orEmpty().trim().ifBlank { "Sin identificación fiscal" }
        val telefono = binding.edtTelefono.text?.toString().orEmpty().trim().ifBlank { "Sin teléfono" }
        val correo = binding.edtCorreo.text?.toString().orEmpty().trim().ifBlank { "Sin correo" }
        binding.tvResumenNegocioNombre.text = "Nombre comercial: $nombre"
        binding.tvResumenNegocioRazon.text = "Razón social: $razon"
        binding.tvResumenNegocioFiscal.text = "RIF / RUC / NIT: $fiscal"
        binding.tvResumenNegocioContacto.text = "$telefono • $correo"

        val direccion = binding.edtDireccion.text?.toString().orEmpty().trim().ifBlank { "Sin dirección" }
        val ciudad = binding.edtCiudad.text?.toString().orEmpty().trim().ifBlank { "Sin ciudad" }
        val estado = binding.edtEstadoProvincia.text?.toString().orEmpty().trim().ifBlank { "Sin estado" }
        val pais = binding.edtPais.text?.toString().orEmpty().trim().ifBlank { "Sin país" }
        binding.tvResumenUbicacionDireccion.text = "Dirección: $direccion"
        binding.tvResumenUbicacionDetalle.text = "$ciudad, $estado, $pais"

        val impuestos = if (binding.switchCobrarImpuestos.isChecked) {
            val porcentaje = binding.edtPorcentajeImpuesto.text?.toString().orEmpty().trim().ifBlank { "0" }
            "Impuestos: activados ($porcentaje%)"
        } else {
            "Impuestos: desactivados"
        }
        val moneda = binding.autoMoneda.text?.toString().orEmpty().trim().ifBlank { "Sin moneda seleccionada" }
        val simbolo = binding.edtSimboloMoneda.text?.toString().orEmpty().trim().ifBlank { "-" }
        val mensaje = binding.edtMensajeTicket.text?.toString().orEmpty().trim().ifBlank { "Sin mensaje final" }
        binding.tvResumenFacturacionImpuestos.text = impuestos
        binding.tvResumenFacturacionMoneda.text = "Moneda: $moneda • Símbolo: $simbolo"
        binding.tvResumenFacturacionTicket.text = "Mensaje: $mensaje"


    }

    private fun configurarEventos() {
        configurarEventosGenerales()
        configurarWatchersFormulario()
    }

    private fun configurarEventosGenerales() {
        binding.btnAgregarTipoPago.setOnClickListener {
            BottomSheetCrearTipoPago()
                .show(supportFragmentManager, "BottomSheetCrearTipoPago")
        }



        binding.btnGuardarDatosTienda.setOnClickListener {
            if (guardandoDatos) return@setOnClickListener
            if (!validarFormulario(mostrarErrores = true)) return@setOnClickListener

            if (hayDatosGuardados) {
                mostrarDialogoConfirmarEdicion()
            } else {
                guardarDatosTienda()
            }
        }

        binding.switchCobrarImpuestos.setOnCheckedChangeListener { _, isChecked ->
            if (cargandoFormulario) return@setOnCheckedChangeListener

            binding.layoutPorcentajeImpuesto.visibility =
                if (isChecked) View.VISIBLE else View.GONE

            if (!isChecked) {
                binding.edtPorcentajeImpuesto.setText("")
                binding.layoutPorcentajeImpuesto.error = null
            }

            actualizarEstadoBotonGuardar()
        }
    }

    private fun configurarWatchersFormulario() {
        binding.edtNombreTienda.doAfterTextChanged {
            binding.layoutNombreTienda.error = null
            actualizarEstadoBotonGuardar()
        }

        binding.edtRazonSocial.doAfterTextChanged {
            binding.layoutRazonSocial.error = null
            actualizarEstadoBotonGuardar()
        }

        binding.edtIdentificacionFiscal.doAfterTextChanged {
            binding.layoutIdentificacionFiscal.error = null
            actualizarEstadoBotonGuardar()
        }

        binding.edtTelefono.doAfterTextChanged {
            binding.layoutTelefono.error = null
            actualizarEstadoBotonGuardar()
        }

        binding.edtCorreo.doAfterTextChanged {
            binding.layoutCorreo.error = null
            actualizarEstadoBotonGuardar()
        }

        binding.edtDireccion.doAfterTextChanged {
            binding.layoutDireccion.error = null
            actualizarEstadoBotonGuardar()
        }

        binding.edtCiudad.doAfterTextChanged {
            binding.layoutCiudad.error = null
            actualizarEstadoBotonGuardar()
        }

        binding.edtEstadoProvincia.doAfterTextChanged {
            binding.layoutEstadoProvincia.error = null
            actualizarEstadoBotonGuardar()
        }

        binding.edtPais.doAfterTextChanged {
            binding.layoutPais.error = null
            actualizarEstadoBotonGuardar()
        }

        binding.autoMoneda.doAfterTextChanged {
            binding.layoutMoneda.error = null
            actualizarEstadoBotonGuardar()
        }

        binding.edtSimboloMoneda.doAfterTextChanged {
            binding.layoutSimboloMoneda.error = null
            actualizarEstadoBotonGuardar()
        }

        binding.edtMensajeTicket.doAfterTextChanged {
            binding.layoutMensajeTicket.error = null
            actualizarEstadoBotonGuardar()
        }

        binding.edtPorcentajeImpuesto.doAfterTextChanged {
            binding.layoutPorcentajeImpuesto.error = null
            actualizarEstadoBotonGuardar()
        }
    }

    private fun configurarAutoScrollEnCampos() {
        val campos = listOf(
            binding.edtNombreTienda,
            binding.edtRazonSocial,
            binding.edtIdentificacionFiscal,
            binding.edtTelefono,
            binding.edtCorreo,
            binding.edtDireccion,
            binding.edtCiudad,
            binding.edtEstadoProvincia,
            binding.edtPais,
            binding.autoMoneda,
            binding.edtSimboloMoneda,
            binding.edtMensajeTicket,
            binding.edtPorcentajeImpuesto
        )

        campos.forEach { campo ->
            campo.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    desplazarFormularioParaCampo(view)
                }
            }

            campo.setOnClickListener { desplazarFormularioParaCampo(campo) }
        }
    }

    private fun desplazarFormularioParaCampo(view: View) {
        binding.scrollDatosTienda.post {
            if (!view.isAttachedToWindow) return@post

            val rect = Rect()
            view.getDrawingRect(rect)
            binding.scrollDatosTienda.offsetDescendantRectToMyCoords(view, rect)

            val margenSuperior = (resources.displayMetrics.density * 96).toInt()
            val destino = (rect.top - margenSuperior).coerceAtLeast(0)
            binding.scrollDatosTienda.smoothScrollTo(0, destino)
            ajustarCampoConAreaVisible(view)
        }

        // Reintentos cortos para cuando el teclado aún está animando su aparición.
        binding.scrollDatosTienda.postDelayed({ ajustarCampoConAreaVisible(view) }, 140)
        binding.scrollDatosTienda.postDelayed({ ajustarCampoConAreaVisible(view) }, 280)
    }

    private fun ajustarCampoConAreaVisible(view: View) {
        if (!view.isAttachedToWindow) return

        val visibleFrame = Rect()
        binding.root.getWindowVisibleDisplayFrame(visibleFrame)

        val campoRect = Rect()
        view.getGlobalVisibleRect(campoRect)

        val margenExtra = (resources.displayMetrics.density * 16).toInt()
        val zonaBoton = binding.btnGuardarDatosTienda.height + (resources.displayMetrics.density * 24).toInt()
        val limiteInferiorSeguro = visibleFrame.bottom - zonaBoton - margenExtra

        val faltanteInferior = campoRect.bottom - limiteInferiorSeguro
        if (faltanteInferior > 0) {
            binding.scrollDatosTienda.smoothScrollBy(0, faltanteInferior + margenExtra)
        }
    }

    private fun configurarRecyclerTiposPago() {
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

        binding.recyclerTiposPago.layoutManager = LinearLayoutManager(this)
        binding.recyclerTiposPago.adapter = adapterTiposPago
    }

    private fun configurarSelectorMoneda() {
        val monedas = obtenerMonedas()

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            monedas
        )

        binding.autoMoneda.setAdapter(adapter)
        aplicarCierreDropdownAlRecibirFoco(binding.autoMoneda)

        binding.autoMoneda.setOnItemClickListener { parent, _, position, _ ->
            val moneda = parent.getItemAtPosition(position) as Moneda

            monedaSeleccionadaCodigo = moneda.codigo
            monedaSeleccionadaSimbolo = moneda.simbolo

            binding.edtSimboloMoneda.setText(moneda.simbolo)
            binding.layoutMoneda.error = null
            binding.layoutSimboloMoneda.error = null

            actualizarEstadoBotonGuardar()
        }
    }

    private fun cargarTiposPago() {
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
                    actualizarEstadoListaTiposPago()

                    if (listaTiposPago.isEmpty() && !creandoMetodoPorDefecto) {
                        crearMetodoPagoPorDefecto()
                    }

                    actualizarEstadoBotonGuardar()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@DatosdelaTienda,
                        "Error al cargar métodos de pago",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun actualizarEstadoListaTiposPago() {
        val vacio = listaTiposPago.isEmpty()
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
            permiteReferencia = false,
            usaQR = false,
            disponibleMixto = true,

            banco = "",
            tipoCuenta = "",
            numeroCuenta = "",
            titularBanco = "",
            documentoBanco = "",

            telefonoBilletera = "",
            titularBilletera = "",
            aliasBilletera = "",
            qrUrl = "",
            instrucciones = "",

            descripcion = "Método creado automáticamente por defecto",
            orden = 1
        )

        ref.child(nuevoId)
            .setValue(metodoPorDefecto.toFirebaseMapCompact())
            .addOnSuccessListener {
                creandoMetodoPorDefecto = false
                MovimientoLogger.registrarConSesion(
                    context = this,
                    tipo = "metodo_pago_creado_automatico",
                    modulo = "configuracion_tienda",
                    titulo = "Metodo de pago por defecto creado: Efectivo",
                    descripcion = "Se creo automaticamente el metodo de pago Efectivo.",
                    referenciaId = nuevoId,
                    extra = mapOf(
                        "seccion" to "metodosPago",
                        "metodoId" to nuevoId,
                        "tituloMetodo" to "Efectivo",
                        "categoria" to "efectivo",
                        "motivo" to "Inicializacion automatica de metodos de pago",
                        "cambiosAntesDespues" to construirCambiosAltaMetodoPago(metodoPorDefecto)
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
        if (metodo.id.isBlank()) return

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
            .setMessage("¿Seguro que deseas eliminar \"${metodo.titulo}\"?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                database.getReference("ConfiguracionTienda")
                    .child("metodosPago")
                    .child(metodo.id)
                    .removeValue()
                    .addOnSuccessListener {
                        val cambiosAntesDespues = construirCambiosBajaMetodoPago(metodo)
                        MovimientoLogger.registrarConSesion(
                            context = this,
                            tipo = "metodo_pago_eliminado",
                            modulo = "configuracion_tienda",
                            titulo = "Metodo de pago eliminado: ${metodo.titulo}",
                            descripcion = "Se elimino el metodo '${metodo.titulo}'.",
                            referenciaId = metodo.id,
                            extra = mapOf(
                                "seccion" to "metodosPago",
                                "metodoId" to metodo.id,
                                "tituloMetodo" to metodo.titulo,
                                "categoria" to metodo.categoria,
                                "motivo" to "Eliminacion manual de metodo de pago",
                                "cambiosAntesDespues" to cambiosAntesDespues
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

    private fun construirCambiosAltaMetodoPago(
        metodo: MetodoPagoConfig
    ): Map<String, Map<String, Any>> {
        val cambios = linkedMapOf<String, Map<String, Any>>()
        metodo.toFirebaseMapCompact()
            .forEach { (campo, valor) ->
                if (campo == "id") return@forEach
                if (!debeIncluirCampoAuditoriaCreacion(valor)) return@forEach
                cambios[campo] = mapOf(
                    "antes" to "(vacio)",
                    "despues" to valorAuditoria(valor)
                )
            }
        return cambios
    }

    private fun construirCambiosBajaMetodoPago(
        metodo: MetodoPagoConfig
    ): Map<String, Map<String, Any>> {
        val cambios = linkedMapOf<String, Map<String, Any>>()
        metodo.toFirebaseMapCompact()
            .forEach { (campo, valor) ->
                if (campo == "id") return@forEach
                if (!debeIncluirCampoAuditoriaCreacion(valor)) return@forEach
                cambios[campo] = mapOf(
                    "antes" to valorAuditoria(valor),
                    "despues" to "(eliminado)"
                )
            }
        return cambios
    }

    private fun debeIncluirCampoAuditoriaCreacion(valor: Any?): Boolean {
        return when (valor) {
            null -> false
            is String -> valor.isNotBlank()
            is Number -> valor.toDouble() != 0.0
            is Boolean -> valor
            else -> true
        }
    }

    private fun cargarDatosTienda() {
        cargandoFormulario = true

        database.getReference("ConfiguracionTienda")
            .child("datosGenerales")
            .get()
            .addOnSuccessListener { snapshot ->
                val datos = snapshot.getValue(ConfiguracionTienda::class.java)

                if (datos == null) {
                    hayDatosGuardados = false
                    snapshotInicial = null
                    cargandoFormulario = false
                    actualizarEstadoBotonGuardar()
                    return@addOnSuccessListener
                }

                poblarFormulario(datos)

                hayDatosGuardados = true
                snapshotInicial = datos.normalizada()
                cargandoFormulario = false
                actualizarEstadoBotonGuardar()
            }
            .addOnFailureListener {
                hayDatosGuardados = false
                snapshotInicial = null
                cargandoFormulario = false

                Toast.makeText(
                    this,
                    "No se pudieron cargar los datos de la tienda",
                    Toast.LENGTH_SHORT
                ).show()

                actualizarEstadoBotonGuardar()
            }
    }

    private fun poblarFormulario(datos: ConfiguracionTienda) {
        binding.edtNombreTienda.setText(datos.nombreTienda)
        binding.edtRazonSocial.setText(datos.razonSocial)
        binding.edtIdentificacionFiscal.setText(datos.identificacionFiscal)
        binding.edtTelefono.setText(datos.telefono)
        binding.edtCorreo.setText(datos.correo)

        binding.edtDireccion.setText(datos.direccion)
        binding.edtCiudad.setText(datos.ciudad)
        binding.edtEstadoProvincia.setText(datos.estadoProvincia)
        binding.edtPais.setText(datos.pais)

        binding.switchCobrarImpuestos.isChecked = datos.cobrarImpuestos
        binding.layoutPorcentajeImpuesto.visibility =
            if (datos.cobrarImpuestos) View.VISIBLE else View.GONE

        binding.edtPorcentajeImpuesto.setText(
            if (datos.cobrarImpuestos) datos.porcentajeImpuesto.toString() else ""
        )

        binding.autoMoneda.setText(datos.monedaVisual, false)
        binding.edtSimboloMoneda.setText(datos.monedaSimbolo)
        binding.edtMensajeTicket.setText(datos.mensajeTicket)

        monedaSeleccionadaCodigo = datos.monedaCodigo
        monedaSeleccionadaSimbolo = datos.monedaSimbolo
    }

    private fun mostrarDialogoConfirmarEdicion() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirmar edición")
            .setMessage("Ya existen datos guardados de la tienda. ¿Deseas actualizar la configuración?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Sí, actualizar") { _, _ ->
                guardarDatosTienda()
            }
            .show()
    }

    private fun guardarDatosTienda() {
        if (guardandoDatos) return
        if (!validarFormulario(mostrarErrores = true)) return

        if (!tieneInternetDisponible()) {
            mostrarDialogoSinInternet()
            return
        }

        val configuracion = construirConfiguracionDesdeFormulario().normalizada()
        val guardarSoloCambios = hayDatosGuardados && snapshotInicial != null
        val esEdicion = guardarSoloCambios
        val cambios = construirCambiosDatosTienda(configuracion)

        if (guardarSoloCambios && cambios.isEmpty()) {
            actualizarEstadoBotonGuardar()
            Toast.makeText(this, "No hay cambios para guardar", Toast.LENGTH_SHORT).show()
            return
        }

        bloquearGuardado(true)
        mostrarOverlaySubida(true, "Verificando conexión")
        guardarDatosTiendaConReintento(
            configuracion = configuracion,
            cambios = cambios,
            guardarSoloCambios = guardarSoloCambios,
            esEdicion = esEdicion,
            intentoActual = 1
        )
    }

    private fun guardarDatosTiendaConReintento(
        configuracion: ConfiguracionTienda,
        cambios: Map<String, Any>,
        guardarSoloCambios: Boolean,
        esEdicion: Boolean,
        intentoActual: Int
    ) {
        val maxIntentos = 3
        val objetivo = if (guardarSoloCambios) "cambios" else "datos"
        val mensajeIntento = if (intentoActual == 1) {
            "Subiendo $objetivo"
        } else {
            "Reintentando subida ($intentoActual/$maxIntentos)"
        }

        mostrarOverlaySubida(true, mensajeIntento)

        val refDatos = database.getReference("ConfiguracionTienda").child("datosGenerales")
        val intentoFallido: (Exception?) -> Unit = { e ->
            if (intentoActual < maxIntentos && tieneInternetDisponible()) {
                val delayMs = (700L * intentoActual).coerceAtMost(1800L)
                mostrarOverlaySubida(true, "Reconectando")
                binding.root.postDelayed({
                    guardarDatosTiendaConReintento(
                        configuracion = configuracion,
                        cambios = cambios,
                        guardarSoloCambios = guardarSoloCambios,
                        esEdicion = esEdicion,
                        intentoActual = intentoActual + 1
                    )
                }, delayMs)
            } else {
                onGuardadoDatosFallido(e, intentoActual)
            }
        }

        if (guardarSoloCambios) {
            refDatos.updateChildren(cambios)
                .addOnSuccessListener {
                    onGuardadoDatosExitoso(
                        configuracion = configuracion,
                        fueGuardadoParcial = true,
                        cantidadCambios = cambios.size,
                        esEdicion = esEdicion,
                        camposAfectados = cambios.keys.toList()
                    )
                }
                .addOnFailureListener { e -> intentoFallido(e) }
        } else {
            refDatos.setValue(configuracion)
                .addOnSuccessListener {
                    onGuardadoDatosExitoso(
                        configuracion = configuracion,
                        fueGuardadoParcial = false,
                        cantidadCambios = configuracion.toFirebaseMap().size,
                        esEdicion = esEdicion,
                        camposAfectados = configuracion.toFirebaseMap().keys.toList()
                    )
                }
                .addOnFailureListener { e -> intentoFallido(e) }
        }
    }

    private fun onGuardadoDatosExitoso(
        configuracion: ConfiguracionTienda,
        fueGuardadoParcial: Boolean,
        cantidadCambios: Int,
        esEdicion: Boolean,
        camposAfectados: List<String>
    ) {
        val snapshotAnterior = snapshotInicial
        bloquearGuardado(false)
        mostrarOverlaySubida(false)
        hayDatosGuardados = true
        snapshotInicial = configuracion
        SessionManager.guardarMonedaConfigurada(
            context = this,
            codigo = configuracion.monedaCodigo,
            simbolo = configuracion.monedaSimbolo
        )
        actualizarEstadoBotonGuardar()
        registrarMovimientoConfiguracionTienda(
            configuracionAnterior = snapshotAnterior,
            configuracion = configuracion,
            esEdicion = esEdicion,
            cantidadCambios = cantidadCambios,
            camposAfectados = camposAfectados
        )

        val mensaje = if (fueGuardadoParcial) {
            "Cambios guardados correctamente ($cantidadCambios)"
        } else {
            "Datos de la tienda guardados correctamente"
        }

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun registrarMovimientoConfiguracionTienda(
        configuracionAnterior: ConfiguracionTienda?,
        configuracion: ConfiguracionTienda,
        esEdicion: Boolean,
        cantidadCambios: Int,
        camposAfectados: List<String>
    ) {
        val tipo = if (esEdicion) {
            "datos_tienda_actualizados"
        } else {
            "datos_tienda_creados"
        }

        val titulo = if (esEdicion) {
            "Configuracion de tienda actualizada"
        } else {
            "Configuracion de tienda creada"
        }

        val descripcion = if (esEdicion) {
            "Se actualizaron datos generales de la tienda."
        } else {
            "Se registraron por primera vez los datos generales de la tienda."
        }

        val campos = camposAfectados
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        val cambiosAntesDespues = if (esEdicion && configuracionAnterior != null) {
            construirCambiosAntesDespues(
                mapaAntes = configuracionAnterior.toFirebaseMap(),
                mapaDespues = configuracion.toFirebaseMap(),
                clavesAfectadas = campos
            )
        } else {
            construirCambiosAltaDatosTienda(configuracion)
        }

        val extraMovimiento = mutableMapOf<String, Any>(
            "seccion" to "datosGenerales",
            "esEdicion" to esEdicion,
            "cantidadCambios" to cantidadCambios,
            "camposAfectados" to campos,
            "nombreTienda" to configuracion.nombreTienda,
            "monedaCodigo" to configuracion.monedaCodigo,
            "cobrarImpuestos" to configuracion.cobrarImpuestos,
            "motivo" to if (esEdicion) {
                "Edicion manual de datos de la tienda"
            } else {
                "Registro inicial de datos de la tienda"
            }
        )

        if (cambiosAntesDespues.isNotEmpty()) {
            extraMovimiento["cambiosAntesDespues"] = cambiosAntesDespues
        }

        MovimientoLogger.registrarConSesion(
            context = this,
            tipo = tipo,
            modulo = "configuracion_tienda",
            titulo = titulo,
            descripcion = descripcion,
            extra = extraMovimiento
        )
    }

    private fun onGuardadoDatosFallido(error: Exception?, ultimoIntento: Int) {
        bloquearGuardado(false)
        mostrarOverlaySubida(false)
        actualizarEstadoBotonGuardar()

        if (!tieneInternetDisponible()) {
            mostrarDialogoSinInternet()
            return
        }

        val detalle = error?.message?.trim().orEmpty().ifBlank { "desconocido" }
        val mensaje = if (ultimoIntento > 1) {
            "No se pudo guardar después de $ultimoIntento intentos.\nDetalle: $detalle"
        } else {
            "No se pudo guardar.\nDetalle: $detalle"
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Error al subir datos")
            .setMessage(mensaje)
            .setNegativeButton("Cerrar", null)
            .setPositiveButton("Reintentar") { _, _ ->
                guardarDatosTienda()
            }
            .show()
    }

    private fun actualizarEstadoBotonGuardar() {
        if (guardandoDatos) {
            binding.btnGuardarDatosTienda.isEnabled = false
            binding.btnGuardarDatosTienda.alpha = 0.6f
            binding.btnGuardarDatosTienda.text = "Guardando datos..."
            return
        }

        val formularioValido = validarFormulario(mostrarErrores = false)
        val hayCambios = hayCambiosPendientes()

        when {
            !formularioValido -> {
                binding.btnGuardarDatosTienda.isEnabled = false
                binding.btnGuardarDatosTienda.alpha = 0.6f
                binding.btnGuardarDatosTienda.text = "Guardar datos de la farmacia"
            }

            hayDatosGuardados && !hayCambios -> {
                binding.btnGuardarDatosTienda.isEnabled = false
                binding.btnGuardarDatosTienda.alpha = 0.6f
                binding.btnGuardarDatosTienda.text = "Guardar datos de la farmacia"
            }

            hayDatosGuardados && hayCambios -> {
                binding.btnGuardarDatosTienda.isEnabled = true
                binding.btnGuardarDatosTienda.alpha = 1f
                binding.btnGuardarDatosTienda.text = "Guardar datos de la farmacia"
            }

            else -> {
                binding.btnGuardarDatosTienda.isEnabled = true
                binding.btnGuardarDatosTienda.alpha = 1f
                binding.btnGuardarDatosTienda.text = "Guardar datos de la farmacia"
            }
        }
    }

    private fun validarFormulario(mostrarErrores: Boolean): Boolean {
        if (mostrarErrores) limpiarErroresFormulario()

        val resultado = validarFormularioActual()
        val validoGeneral = validarInformacionGeneral(mostrarErrores, resultado)
        val validoUbicacion = validarUbicacion(mostrarErrores, resultado)
        val validoConfiguracion = validarConfiguracionAdicional(mostrarErrores, resultado)
        val validoTiposPago = validarTiposPago(mostrarErrores, resultado)

        return validoGeneral && validoUbicacion && validoConfiguracion && validoTiposPago
    }

    private fun validarInformacionGeneral(
        mostrarErrores: Boolean,
        resultado: TiendaConfigValidationResult = validarFormularioActual()
    ): Boolean {
        if (mostrarErrores) {
            binding.layoutNombreTienda.error = resultado.nombreTiendaError
            binding.layoutRazonSocial.error = resultado.razonSocialError
            binding.layoutIdentificacionFiscal.error = resultado.identificacionFiscalError
            binding.layoutTelefono.error = resultado.telefonoError
            binding.layoutCorreo.error = resultado.correoError
        }

        return resultado.esValidoGeneral
    }

    private fun validarUbicacion(
        mostrarErrores: Boolean,
        resultado: TiendaConfigValidationResult = validarFormularioActual()
    ): Boolean {
        if (mostrarErrores) {
            binding.layoutDireccion.error = resultado.direccionError
            binding.layoutCiudad.error = resultado.ciudadError
            binding.layoutEstadoProvincia.error = resultado.estadoProvinciaError
            binding.layoutPais.error = resultado.paisError
        }

        return resultado.esValidoUbicacion
    }

    private fun validarConfiguracionAdicional(
        mostrarErrores: Boolean,
        resultado: TiendaConfigValidationResult = validarFormularioActual()
    ): Boolean {
        if (mostrarErrores) {
            binding.layoutMoneda.error = resultado.monedaError
            binding.layoutSimboloMoneda.error = resultado.simboloMonedaError
            binding.layoutMensajeTicket.error = resultado.mensajeTicketError
            binding.layoutPorcentajeImpuesto.error = resultado.porcentajeImpuestoError
        }

        return resultado.esValidoConfiguracion
    }

    private fun validarTiposPago(
        mostrarErrores: Boolean,
        resultado: TiendaConfigValidationResult = validarFormularioActual()
    ): Boolean {
        if (!resultado.esValidoTiposPago && mostrarErrores) {
            Toast.makeText(
                this,
                resultado.tiposPagoError ?: "Debe existir al menos un tipo de pago",
                Toast.LENGTH_SHORT
            ).show()
        }

        return resultado.esValidoTiposPago
    }

    private fun hayCambiosPendientes(): Boolean {
        if (!hayDatosGuardados) return true

        val actual = construirConfiguracionDesdeFormulario().normalizada()
        val inicial = snapshotInicial ?: return true

        return actual != inicial
    }

    private fun construirConfiguracionDesdeFormulario(): ConfiguracionTienda {
        return TiendaConfigRules.construirConfiguracionNormalizada(
            data = construirFormularioActual(),
            monedasDisponibles = obtenerMonedas()
        )
    }

    private fun construirCambiosDatosTienda(actual: ConfiguracionTienda): Map<String, Any> {
        val inicial = snapshotInicial ?: return actual.toFirebaseMap()
        val mapaInicial = inicial.toFirebaseMap()
        val mapaActual = actual.toFirebaseMap()
        val cambios = linkedMapOf<String, Any>()

        mapaActual.forEach { (clave, valorActual) ->
            val valorInicial = mapaInicial[clave]
            if (!sonValoresIgualesParaGuardado(clave, valorInicial, valorActual)) {
                cambios[clave] = valorActual
            }
        }

        return cambios
    }

    private fun ConfiguracionTienda.toFirebaseMap(): Map<String, Any> {
        return linkedMapOf(
            "nombreTienda" to nombreTienda,
            "razonSocial" to razonSocial,
            "identificacionFiscal" to identificacionFiscal,
            "telefono" to telefono,
            "correo" to correo,
            "direccion" to direccion,
            "ciudad" to ciudad,
            "estadoProvincia" to estadoProvincia,
            "pais" to pais,
            "cobrarImpuestos" to cobrarImpuestos,
            "porcentajeImpuesto" to porcentajeImpuesto,
            "monedaCodigo" to monedaCodigo,
            "monedaSimbolo" to monedaSimbolo,
            "monedaVisual" to monedaVisual,
            "mensajeTicket" to mensajeTicket
        )
    }

    private fun sonValoresIgualesParaGuardado(clave: String, anterior: Any?, actual: Any?): Boolean {
        if (clave == "porcentajeImpuesto") {
            val a = (anterior as? Number)?.toDouble() ?: return actual == null
            val b = (actual as? Number)?.toDouble() ?: return false
            return kotlin.math.abs(a - b) < 0.000001
        }
        return anterior == actual
    }

    private fun construirCambiosAntesDespues(
        mapaAntes: Map<String, Any>,
        mapaDespues: Map<String, Any>,
        clavesAfectadas: List<String>
    ): Map<String, Map<String, Any>> {
        if (clavesAfectadas.isEmpty()) return emptyMap()

        val cambios = linkedMapOf<String, Map<String, Any>>()
        clavesAfectadas.forEach { clave ->
            val valorAntes = mapaAntes[clave]
            val valorDespues = mapaDespues[clave]
            if (valorAntes == valorDespues) return@forEach

            cambios[clave] = mapOf(
                "antes" to valorAuditoria(valorAntes),
                "despues" to valorAuditoria(valorDespues)
            )
        }
        return cambios
    }

    private fun construirCambiosAltaDatosTienda(
        configuracion: ConfiguracionTienda
    ): Map<String, Map<String, Any>> {
        val cambios = linkedMapOf<String, Map<String, Any>>()
        configuracion.toFirebaseMap().forEach { (campo, valor) ->
            if (!debeIncluirCampoAuditoriaCreacion(valor)) return@forEach
            cambios[campo] = mapOf(
                "antes" to "(vacio)",
                "despues" to valorAuditoria(valor)
            )
        }
        return cambios
    }

    private fun valorAuditoria(valor: Any?): Any {
        return when (valor) {
            null -> "(vacio)"
            is String -> valor.ifBlank { "(vacio)" }
            is Boolean, is Number -> valor
            is List<*> -> valor.map { valorAuditoria(it) }
            is Map<*, *> -> valor.entries.associate { (k, v) ->
                (k?.toString() ?: "(sin_clave)") to valorAuditoria(v)
            }
            else -> valor.toString()
        }
    }

    private fun bloquearGuardado(bloquear: Boolean) {
        guardandoDatos = bloquear
        binding.btnGuardarDatosTienda.isEnabled = !bloquear
        binding.btnGuardarDatosTienda.alpha = if (bloquear) 0.6f else 1f
        if (bloquear) {
            binding.btnGuardarDatosTienda.text = "Guardando datos..."
        }
    }

    private fun mostrarOverlaySubida(mostrar: Boolean, mensajeBase: String = "Subiendo datos") {
        if (mostrar) {
            mensajeBaseAnimacionSubida = mensajeBase
            binding.layoutSubidaDatosOverlay.visibility = View.VISIBLE
            binding.tvMensajeSubidaDatos.text = "$mensajeBase."
            iniciarAnimacionSubidaTexto()
            return
        }

        detenerAnimacionSubidaTexto()
        binding.layoutSubidaDatosOverlay.visibility = View.GONE
    }

    private fun iniciarAnimacionSubidaTexto() {
        detenerAnimacionSubidaTexto()

        runnableAnimacionSubida = object : Runnable {
            private var dots = 1

            override fun run() {
                if (binding.layoutSubidaDatosOverlay.visibility != View.VISIBLE) return

                val sufijo = ".".repeat(dots.coerceIn(1, 3))
                binding.tvMensajeSubidaDatos.text = mensajeBaseAnimacionSubida + sufijo
                dots = if (dots >= 3) 1 else dots + 1
                handlerAnimacionSubida.postDelayed(this, 420)
            }
        }

        handlerAnimacionSubida.post(runnableAnimacionSubida!!)
    }

    private fun detenerAnimacionSubidaTexto() {
        runnableAnimacionSubida?.let { handlerAnimacionSubida.removeCallbacks(it) }
        runnableAnimacionSubida = null
    }

    private fun tieneInternetDisponible(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val redActiva = cm.activeNetwork ?: return false
        val capacidades = cm.getNetworkCapabilities(redActiva) ?: return false

        val tieneInternet = capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val tieneTransporte = capacidades.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capacidades.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capacidades.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
            capacidades.hasTransport(NetworkCapabilities.TRANSPORT_VPN)

        return tieneInternet && tieneTransporte
    }

    private fun mostrarDialogoSinInternet() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Sin conexión a internet")
            .setMessage("Conéctate a internet para subir los datos de la tienda.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Reintentar") { _, _ ->
                if (!guardandoDatos) {
                    guardarDatosTienda()
                }
            }
            .show()
    }

    private fun limpiarErroresFormulario() {
        binding.layoutNombreTienda.error = null
        binding.layoutRazonSocial.error = null
        binding.layoutIdentificacionFiscal.error = null
        binding.layoutTelefono.error = null
        binding.layoutCorreo.error = null

        binding.layoutDireccion.error = null
        binding.layoutCiudad.error = null
        binding.layoutEstadoProvincia.error = null
        binding.layoutPais.error = null

        binding.layoutMoneda.error = null
        binding.layoutSimboloMoneda.error = null
        binding.layoutMensajeTicket.error = null
        binding.layoutPorcentajeImpuesto.error = null
    }

    private fun validarFormularioActual(): TiendaConfigValidationResult {
        return TiendaConfigRules.validarFormulario(construirFormularioActual())
    }

    private fun construirFormularioActual(): TiendaConfigFormData {
        return TiendaConfigFormData(
            nombreTienda = binding.edtNombreTienda.text?.toString().orEmpty(),
            razonSocial = binding.edtRazonSocial.text?.toString().orEmpty(),
            identificacionFiscal = binding.edtIdentificacionFiscal.text?.toString().orEmpty(),
            telefono = binding.edtTelefono.text?.toString().orEmpty(),
            correo = binding.edtCorreo.text?.toString().orEmpty(),
            direccion = binding.edtDireccion.text?.toString().orEmpty(),
            ciudad = binding.edtCiudad.text?.toString().orEmpty(),
            estadoProvincia = binding.edtEstadoProvincia.text?.toString().orEmpty(),
            pais = binding.edtPais.text?.toString().orEmpty(),
            cobrarImpuestos = binding.switchCobrarImpuestos.isChecked,
            porcentajeImpuestoTexto = binding.edtPorcentajeImpuesto.text?.toString().orEmpty(),
            monedaCodigoSeleccionado = monedaSeleccionadaCodigo,
            monedaSimboloSeleccionado = monedaSeleccionadaSimbolo,
            monedaVisual = binding.autoMoneda.text?.toString().orEmpty(),
            simboloMonedaIngresado = binding.edtSimboloMoneda.text?.toString().orEmpty(),
            mensajeTicket = binding.edtMensajeTicket.text?.toString().orEmpty(),
            cantidadTiposPago = listaTiposPago.size
        )
    }

    private fun obtenerCodigoDesdeTextoMoneda(texto: String): String {
        return TiendaConfigRules.obtenerCodigoDesdeTextoMoneda(
            texto = texto,
            monedasDisponibles = obtenerMonedas()
        )
    }

    private fun obtenerMonedas(): List<Moneda> {
        return listOf(
            Moneda("USD", "$", "Dólar estadounidense"),
            Moneda("MXN", "$", "Peso mexicano"),
            Moneda("COP", "$", "Peso colombiano"),
            Moneda("ARS", "$", "Peso argentino"),
            Moneda("EUR", "€", "Euro"),
            Moneda("PEN", "S/", "Sol peruano"),
            Moneda("CLP", "$", "Peso chileno"),
            Moneda("VES", "Bs", "Bolívar"),
            Moneda("BRL", "R$", "Real brasileño")
        )
    }
    private fun ConfiguracionTienda.normalizada(): ConfiguracionTienda {
        return TiendaConfigRules.normalizarConfiguracion(this)
    }

    override fun onDestroy() {
        detenerAnimacionSubidaTexto()
        handlerAnimacionSubida.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}


data class Moneda(
    val codigo: String,
    val simbolo: String,
    val nombre: String
) {
    override fun toString(): String {
        return "$codigo - $nombre"
    }
}

data class ConfiguracionTienda(
    val nombreTienda: String = "",
    val razonSocial: String = "",
    val identificacionFiscal: String = "",
    val telefono: String = "",
    val correo: String = "",

    val direccion: String = "",
    val ciudad: String = "",
    val estadoProvincia: String = "",
    val pais: String = "",

    val cobrarImpuestos: Boolean = false,
    val porcentajeImpuesto: Double = 0.0,

    val monedaCodigo: String = "",
    val monedaSimbolo: String = "",
    val monedaVisual: String = "",

    val mensajeTicket: String = ""


)

data class MetodoPagoConfig(
    val id: String = "",
    val titulo: String = "",
    val categoria: String = "",
    val activo: Boolean = true,

    val permiteVuelto: Boolean = false,
    val solicitaMontoRecibido: Boolean = false,
    val calculaVuelto: Boolean = false,
    val permiteReferencia: Boolean = false,
    val usaQR: Boolean = false,
    val disponibleMixto: Boolean = false,

    val banco: String = "",
    val tipoCuenta: String = "",
    val numeroCuenta: String = "",
    val titularBanco: String = "",
    val documentoBanco: String = "",

    val telefonoBilletera: String = "",
    val titularBilletera: String = "",
    val aliasBilletera: String = "",
    val qrUrl: String = "",
    val instrucciones: String = "",

    val descripcion: String = "",
    val orden: Int = 0
)

private fun MetodoPagoConfig.toFirebaseMapCompact(): Map<String, Any> {
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


class BottomSheetCrearTipoPago : BottomSheetDialogFragment() {

    private var metodoIdEditar: String? = null
    private var modoEdicion = false
    private var guardando = false
    private var metodoOriginalParaAuditoria: MetodoPagoConfig? = null

    private var _binding: BottomsheetCrearTipoPagoBinding? = null
    private val binding get() = _binding

    private var qrBilleteraUri: Uri? = null
    private var qrBilleteraUrlGuardada: String = ""
    private val handlerValidacionTitulo = Handler(Looper.getMainLooper())
    private var runnableValidacionTitulo: Runnable? = null

    private fun aplicarCierreDropdownAlRecibirFoco(autoComplete: AutoCompleteTextView) {
        autoComplete.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                (v as? AutoCompleteTextView)?.dismissDropDown()
            }
        }
    }

    private val seleccionarQrLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val b = binding ?: return@registerForActivityResult
            if (guardando) return@registerForActivityResult

            if (uri != null) {
                qrBilleteraUri = uri
                b.imgQrBilleteraPreview.setImageURI(uri)
            }
        }

    companion object {
        private const val ARG_METODO_ID = "arg_metodo_id"
        private const val CLOUDINARY_CLOUD_NAME = "dluvatyh7"
        private const val CLOUDINARY_UPLOAD_PRESET = "productos_app"

        fun newInstance(metodoId: String? = null): BottomSheetCrearTipoPago {
            return BottomSheetCrearTipoPago().apply {
                arguments = Bundle().apply {
                    putString(ARG_METODO_ID, metodoId)
                }
            }
        }
    }

    private val database by lazy { FirebaseDatabase.getInstance() }

    private val categorias = listOf(
        "Efectivo",
        "Tarjeta",
        "Transferencia bancaria",
        "Billetera digital",
        "Cobro mixto",
        "Otro"
    )

    private val tiposCuenta = listOf("Ahorro", "Corriente")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomsheetCrearTipoPagoBinding.inflate(inflater, container, false)
        return requireNotNull(binding).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        metodoIdEditar = arguments?.getString(ARG_METODO_ID)
        modoEdicion = !metodoIdEditar.isNullOrEmpty()

        reordenarCamposGenerales()
        configurarDropdowns()
        configurarEventos()
        estadoInicialFormulario()

        if (modoEdicion) {
            binding?.txtTituloBottom?.text = "Editar tipo de pago"
            cargarMetodoPagoParaEditar()
        }
    }

    private fun reordenarCamposGenerales() {
        val b = binding ?: return
        val contenedor = b.layoutTituloMetodo.parent as? ViewGroup ?: return

        val indiceTitulo = contenedor.indexOfChild(b.layoutTituloMetodo)
        val indiceCategoria = contenedor.indexOfChild(b.layoutCategoria)

        if (indiceTitulo == -1 || indiceCategoria == -1 || indiceCategoria < indiceTitulo) return

        contenedor.removeView(b.layoutCategoria)
        contenedor.addView(b.layoutCategoria, indiceTitulo)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            setCanceledOnTouchOutside(false)
            setCancelable(false)
        }
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog as? BottomSheetDialog ?: return
        val bottomSheet =
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?: return

        val behavior = BottomSheetBehavior.from(bottomSheet)
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels
        val topMargin = (screenHeight * 0.08).toInt()
        val newHeight = screenHeight - topMargin

        bottomSheet.layoutParams.height = newHeight
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        behavior.isFitToContents = true
        behavior.isDraggable = !guardando
    }

    override fun onCancel(dialog: DialogInterface) {
        if (guardando) return
        super.onCancel(dialog)
    }

    override fun onDestroyView() {
        handlerValidacionTitulo.removeCallbacksAndMessages(null)
        runnableValidacionTitulo = null
        _binding = null
        super.onDestroyView()
    }

    private fun cargarMetodoPagoParaEditar() {
        val metodoId = metodoIdEditar ?: return

        mostrarEstadoGuardando(true)

        database.getReference("ConfiguracionTienda")
            .child("metodosPago")
            .child(metodoId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!estaVistaActiva()) return@addOnSuccessListener

                val metodo = snapshot.getValue(MetodoPagoConfig::class.java)
                if (metodo == null) {
                    mostrarEstadoGuardando(false)
                    mostrarToast("No se pudo cargar el método de pago")
                    cerrarSiEsPosible()
                    return@addOnSuccessListener
                }

                metodoOriginalParaAuditoria = metodo.copy()
                llenarFormularioConMetodo(metodo)
                mostrarEstadoGuardando(false)
            }
            .addOnFailureListener { e ->
                if (!estaVistaActiva()) return@addOnFailureListener
                mostrarEstadoGuardando(false)
                mostrarToast("Error al cargar: ${e.message ?: "desconocido"}")
                cerrarSiEsPosible()
            }
    }

    private fun configurarDropdowns() {
        val ctx = context ?: return
        val b = binding ?: return

        b.autoCategoria.setAdapter(
            ArrayAdapter(ctx, android.R.layout.simple_list_item_1, categorias)
        )
        aplicarCierreDropdownAlRecibirFoco(b.autoCategoria)

        b.autoTipoCuenta.setAdapter(
            ArrayAdapter(ctx, android.R.layout.simple_list_item_1, tiposCuenta)
        )
        aplicarCierreDropdownAlRecibirFoco(b.autoTipoCuenta)
    }

    private fun configurarEventos() {
        val b = binding ?: return

        b.autoCategoria.setOnItemClickListener { _, _, position, _ ->
            if (guardando) return@setOnItemClickListener
            binding?.layoutCategoria?.error = null
            aplicarConfiguracionPorCategoria(categorias[position])
            programarValidacionTituloTiempoReal()
        }

        b.autoCategoria.doAfterTextChanged {
            if (guardando) return@doAfterTextChanged
            binding?.layoutCategoria?.error = null
        }

        b.edtTituloMetodo.doAfterTextChanged {
            if (guardando) return@doAfterTextChanged
            binding?.layoutTituloMetodo?.error = null
            programarValidacionTituloTiempoReal()
        }

        b.edtBanco.doAfterTextChanged {
            if (guardando) return@doAfterTextChanged
            binding?.layoutBanco?.error = null
        }

        b.autoTipoCuenta.doAfterTextChanged {
            if (guardando) return@doAfterTextChanged
            binding?.layoutTipoCuenta?.error = null
        }

        b.edtNumeroCuenta.doAfterTextChanged {
            if (guardando) return@doAfterTextChanged
            binding?.layoutNumeroCuenta?.error = null
        }

        b.edtTitularBanco.doAfterTextChanged {
            if (guardando) return@doAfterTextChanged
            binding?.layoutTitularBanco?.error = null
        }

        b.edtTelefonoBilletera.doAfterTextChanged {
            if (guardando) return@doAfterTextChanged
            binding?.layoutTelefonoBilletera?.error = null
        }

        b.edtTitularBilletera.doAfterTextChanged {
            if (guardando) return@doAfterTextChanged
            binding?.layoutTitularBilletera?.error = null
        }

        b.btnCancelar.setOnClickListener {
            if (guardando) return@setOnClickListener
            cerrarSiEsPosible()
        }

        b.btnGuardar.setOnClickListener {
            guardarTipoPago()
        }

        b.switchPermiteVuelto.setOnCheckedChangeListener { _, isChecked ->
            val localBinding = binding ?: return@setOnCheckedChangeListener
            if (guardando) return@setOnCheckedChangeListener

            if (!isChecked) {
                localBinding.switchCalculaVuelto.isChecked = false
            }
        }

        b.switchCalculaVuelto.setOnCheckedChangeListener { _, isChecked ->
            val localBinding = binding ?: return@setOnCheckedChangeListener
            if (guardando) return@setOnCheckedChangeListener

            if (isChecked) {
                localBinding.switchPermiteVuelto.isChecked = true
                localBinding.switchSolicitaMontoRecibido.isChecked = true
            }
        }

        b.switchQrBilleteraOpcional.setOnCheckedChangeListener { _, isChecked ->
            val localBinding = binding ?: return@setOnCheckedChangeListener
            if (guardando) return@setOnCheckedChangeListener

            localBinding.layoutQrBilletera.visibility = if (isChecked) View.VISIBLE else View.GONE

            if (!isChecked) {
                qrBilleteraUri = null
                qrBilleteraUrlGuardada = ""
                localBinding.imgQrBilleteraPreview.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }

        b.btnSeleccionarQrBilletera.setOnClickListener {
            if (guardando) return@setOnClickListener
            seleccionarQrLauncher.launch("image/*")
        }

        b.btnQuitarQrBilletera.setOnClickListener {
            if (guardando) return@setOnClickListener
            qrBilleteraUri = null
            qrBilleteraUrlGuardada = ""
            binding?.imgQrBilleteraPreview?.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    private fun estadoInicialFormulario() {
        val b = binding ?: return
        metodoOriginalParaAuditoria = null

        b.cardTransferencia.visibility = View.GONE
        b.cardBilletera.visibility = View.GONE

        b.switchActivo.isChecked = true
        b.switchPermiteVuelto.isChecked = false
        b.switchSolicitaMontoRecibido.isChecked = false
        b.switchCalculaVuelto.isChecked = false
        b.switchPermiteReferencia.isChecked = false
        b.switchUsaQR.isChecked = false
        b.switchDisponibleMixto.isChecked = false

        b.edtTituloMetodo.setText("")
        b.edtTituloMetodo.isEnabled = true
        b.layoutCategoria.hint = "Categoría"
        b.layoutTituloMetodo.hint = "Nombre del método"
        b.layoutTituloMetodo.helperText = "Escribe el nombre que verás en caja"

        b.txtDescripcionCategoria.text =
            "Selecciona una categoría para configurar automáticamente su comportamiento."

        b.switchQrBilleteraOpcional.isChecked = false
        b.layoutQrBilletera.visibility = View.GONE

        b.btnGuardar.isEnabled = false
        b.btnCancelar.isEnabled = false
        b.btnGuardar.alpha = 0.6f
        b.btnCancelar.alpha = 0.6f
    }

    private fun llenarFormularioConMetodo(metodo: MetodoPagoConfig) {
        val b = binding ?: return

        val categoriaVisible = obtenerCategoriaVisible(metodo.categoria)
        b.autoCategoria.setText(categoriaVisible, false)
        aplicarConfiguracionPorCategoria(categoriaVisible)

        b.edtTituloMetodo.setText(metodo.titulo)
        b.switchActivo.isChecked = metodo.activo

        b.switchPermiteVuelto.isChecked = metodo.permiteVuelto
        b.switchSolicitaMontoRecibido.isChecked = metodo.solicitaMontoRecibido
        b.switchCalculaVuelto.isChecked = metodo.calculaVuelto
        b.switchPermiteReferencia.isChecked = metodo.permiteReferencia
        b.switchUsaQR.isChecked = metodo.usaQR
        b.switchDisponibleMixto.isChecked = metodo.disponibleMixto

        b.edtBanco.setText(metodo.banco)
        b.autoTipoCuenta.setText(metodo.tipoCuenta, false)
        b.edtNumeroCuenta.setText(metodo.numeroCuenta)
        b.edtTitularBanco.setText(metodo.titularBanco)
        b.edtDocumentoBanco.setText(metodo.documentoBanco)

        b.edtTelefonoBilletera.setText(metodo.telefonoBilletera)
        b.edtTitularBilletera.setText(metodo.titularBilletera)
        b.edtAliasBilletera.setText(metodo.aliasBilletera)
        b.edtInstrucciones.setText(metodo.instrucciones)

        b.edtDescripcion.setText(metodo.descripcion)
        b.edtOrden.setText(if (metodo.orden == 0) "" else metodo.orden.toString())

        qrBilleteraUrlGuardada = metodo.qrUrl

        if (metodo.categoria == "billetera_digital" && metodo.qrUrl.isNotBlank()) {
            b.switchQrBilleteraOpcional.isChecked = true
            b.layoutQrBilletera.visibility = View.VISIBLE

            Glide.with(this)
                .load(metodo.qrUrl)
                .into(b.imgQrBilleteraPreview)
        } else {
            b.switchQrBilleteraOpcional.isChecked = false
            b.layoutQrBilletera.visibility = View.GONE
            b.imgQrBilleteraPreview.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    private fun obtenerCategoriaVisible(categoria: String): String {
        return when (categoria) {
            "efectivo" -> "Efectivo"
            "tarjeta" -> "Tarjeta"
            "transferencia_bancaria" -> "Transferencia bancaria"
            "billetera_digital" -> "Billetera digital"
            "mixto" -> "Cobro mixto"
            "otro" -> "Otro"
            else -> ""
        }
    }

    private fun aplicarConfiguracionPorCategoria(categoria: String) {
        val b = binding ?: return

        limpiarErrores()
        ocultarSeccionesEspeciales()
        resetRestricciones()
        aplicarNombreSegunCategoria(categoria)
        actualizarHintNombre(categoria)

        when (categoria) {
            "Efectivo" -> {
                b.txtDescripcionCategoria.text =
                    "Este método pedirá monto recibido y calculará vuelto."

                bloquearSwitch(b.switchPermiteVuelto, true)
                bloquearSwitch(b.switchSolicitaMontoRecibido, true)
                bloquearSwitch(b.switchCalculaVuelto, true)
                bloquearSwitch(b.switchPermiteReferencia, false)
                bloquearSwitch(b.switchUsaQR, false)
                b.switchDisponibleMixto.isChecked = true
                b.switchDisponibleMixto.isEnabled = true

                desactivarBloqueTransferencia()
                desactivarBloqueBilletera()
            }

            "Tarjeta" -> {
                b.txtDescripcionCategoria.text =
                    "Este método no usa vuelto ni QR. Puede pedir referencia."

                bloquearSwitch(b.switchPermiteVuelto, false)
                bloquearSwitch(b.switchSolicitaMontoRecibido, false)
                bloquearSwitch(b.switchCalculaVuelto, false)
                b.switchPermiteReferencia.isChecked = true
                b.switchPermiteReferencia.isEnabled = true
                bloquearSwitch(b.switchUsaQR, false)
                b.switchDisponibleMixto.isChecked = true
                b.switchDisponibleMixto.isEnabled = true

                desactivarBloqueTransferencia()
                desactivarBloqueBilletera()
            }

            "Transferencia bancaria" -> {
                b.txtDescripcionCategoria.text =
                    "Configura banco, cuenta, titular y referencia. El QR general no aplica aquí."

                b.cardTransferencia.visibility = View.VISIBLE

                bloquearSwitch(b.switchPermiteVuelto, false)
                bloquearSwitch(b.switchSolicitaMontoRecibido, false)
                bloquearSwitch(b.switchCalculaVuelto, false)
                b.switchPermiteReferencia.isChecked = true
                b.switchPermiteReferencia.isEnabled = true
                bloquearSwitch(b.switchUsaQR, false)
                b.switchDisponibleMixto.isChecked = true
                b.switchDisponibleMixto.isEnabled = true

                desactivarBloqueBilletera()
            }

            "Billetera digital" -> {
                b.txtDescripcionCategoria.text =
                    "Configura teléfono, titular, alias y QR opcional de la billetera."

                b.cardBilletera.visibility = View.VISIBLE

                bloquearSwitch(b.switchPermiteVuelto, false)
                bloquearSwitch(b.switchSolicitaMontoRecibido, false)
                bloquearSwitch(b.switchCalculaVuelto, false)
                b.switchPermiteReferencia.isChecked = true
                b.switchPermiteReferencia.isEnabled = true
                bloquearSwitch(b.switchUsaQR, false)
                b.switchDisponibleMixto.isChecked = true
                b.switchDisponibleMixto.isEnabled = true
                b.switchQrBilleteraOpcional.isEnabled = true

                desactivarBloqueTransferencia()
            }

            "Cobro mixto" -> {
                b.txtDescripcionCategoria.text =
                    "Permite combinar otros métodos. No usa vuelto, referencia ni QR propio."

                bloquearSwitch(b.switchPermiteVuelto, false)
                bloquearSwitch(b.switchSolicitaMontoRecibido, false)
                bloquearSwitch(b.switchCalculaVuelto, false)
                bloquearSwitch(b.switchPermiteReferencia, false)
                bloquearSwitch(b.switchUsaQR, false)
                bloquearSwitch(b.switchDisponibleMixto, false)

                desactivarBloqueTransferencia()
                desactivarBloqueBilletera()
            }

            "Otro" -> {
                b.txtDescripcionCategoria.text =
                    "Método personalizado. Configura manualmente lo que aplique."

                b.switchPermiteVuelto.isChecked = false
                b.switchSolicitaMontoRecibido.isChecked = false
                b.switchCalculaVuelto.isChecked = false
                b.switchPermiteReferencia.isChecked = false
                b.switchUsaQR.isChecked = false
                b.switchDisponibleMixto.isChecked = false

                desactivarBloqueTransferencia()
                desactivarBloqueBilletera()
            }
        }

        b.btnGuardar.isEnabled = true
        b.btnCancelar.isEnabled = true
        b.btnGuardar.alpha = 1f
        b.btnCancelar.alpha = 1f
    }

    private fun aplicarNombreSegunCategoria(categoria: String) {
        val b = binding ?: return
        val tituloActual = b.edtTituloMetodo.text?.toString()?.trim().orEmpty()
        val titulosAutomaticos = setOf("Efectivo", "Tarjeta", "Cobro mixto")

        fun configurarTituloEditable(helper: String) {
            if (!modoEdicion || tituloActual.isBlank() || tituloActual in titulosAutomaticos) {
                b.edtTituloMetodo.setText("")
            }
            b.edtTituloMetodo.isEnabled = true
            b.layoutTituloMetodo.helperText = helper
        }

        when (categoria) {
            "Efectivo" -> {
                b.edtTituloMetodo.setText("Efectivo")
                b.edtTituloMetodo.isEnabled = false
                b.layoutTituloMetodo.helperText = "Nombre automático para esta categoría"
            }

            "Tarjeta" -> {
                b.edtTituloMetodo.setText("Tarjeta")
                b.edtTituloMetodo.isEnabled = false
                b.layoutTituloMetodo.helperText = "Nombre automático para esta categoría"
            }

            "Cobro mixto" -> {
                b.edtTituloMetodo.setText("Cobro mixto")
                b.edtTituloMetodo.isEnabled = false
                b.layoutTituloMetodo.helperText = "Nombre automático para esta categoría"
            }

            "Billetera digital" -> {
                configurarTituloEditable("Ej: Yape, Zelle, Nequi")
            }

            "Transferencia bancaria" -> {
                configurarTituloEditable("Ej: BBVA, Mercantil, Banesco")
            }

            "Otro" -> {
                configurarTituloEditable("Escribe un nombre personalizado")
            }
        }
    }

    private fun actualizarHintNombre(categoria: String) {
        binding?.layoutTituloMetodo?.hint = when (categoria) {
            "Billetera digital" -> "Nombre de la billetera"
            "Transferencia bancaria" -> "Nombre del banco"
            else -> "Nombre del método"
        }
    }

    private fun ocultarSeccionesEspeciales() {
        val b = binding ?: return
        b.cardTransferencia.visibility = View.GONE
        b.cardBilletera.visibility = View.GONE
    }

    private fun guardarTipoPago() {
        if (guardando) return

        val b = binding ?: return
        limpiarErrores()

        val categoriaVisible = b.autoCategoria.text?.toString()?.trim().orEmpty()
        val categoria = mapearCategoria(categoriaVisible)
        val tituloFinal = obtenerTituloFinalFormulario()

        if (categoria.isEmpty()) {
            b.layoutCategoria.error = "Selecciona una categoría"
            return
        }

        if (tituloFinal.isEmpty()) {
            b.layoutTituloMetodo.error = "Ingresa un nombre"
            return
        }

        if (tituloFinal.length < 2) {
            b.layoutTituloMetodo.error = "El nombre es muy corto"
            return
        }

        val orden = b.edtOrden.text?.toString()?.trim()?.toIntOrNull()
        if (b.edtOrden.text?.toString()?.trim()?.isNotEmpty() == true && orden == null) {
            mostrarToast("El orden debe ser numérico")
            return
        }

        if (orden != null && orden < 0) {
            mostrarToast("El orden no puede ser negativo")
            return
        }

        if (categoria == "transferencia_bancaria" && !validarTransferencia()) return
        if (categoria == "billetera_digital" && !validarBilletera()) return

        verificarTituloDisponible(tituloFinal) { disponible ->
            if (!estaVistaActiva()) return@verificarTituloDisponible

            if (!disponible) {
                b.layoutTituloMetodo.error = "Ya existe un tipo de pago con ese nombre"
                enfocarEnCampoConError(b.edtTituloMetodo)
                return@verificarTituloDisponible
            }

            continuarGuardadoTipoPago(categoria, tituloFinal, b)
        }
    }

    private fun continuarGuardadoTipoPago(
        categoria: String,
        tituloFinal: String,
        b: BottomsheetCrearTipoPagoBinding
    ) {
        val metodosRef = database
            .getReference("ConfiguracionTienda")
            .child("metodosPago")

        val idFinal = metodoIdEditar ?: metodosRef.push().key
        if (idFinal.isNullOrEmpty()) {
            mostrarToast("No se pudo generar el id del método")
            return
        }

        mostrarEstadoGuardando(true)

        val debeSubirQr = categoria == "billetera_digital" &&
                b.switchQrBilleteraOpcional.isChecked &&
                qrBilleteraUri != null

        if (debeSubirQr) {
            subirQrBilletera(
                metodoId = idFinal,
                onSuccess = { qrUrl ->
                    if (!estaVistaActiva()) return@subirQrBilletera
                    guardarMetodoEnBase(idFinal, qrUrl, categoria, tituloFinal)
                },
                onError = { mensaje ->
                    if (!estaVistaActiva()) return@subirQrBilletera
                    mostrarEstadoGuardando(false)
                    mostrarToast(mensaje)
                }
            )
        } else {
            val qrFinal = if (
                categoria == "billetera_digital" &&
                b.switchQrBilleteraOpcional.isChecked
            ) {
                qrBilleteraUrlGuardada
            } else {
                ""
            }

            guardarMetodoEnBase(idFinal, qrFinal, categoria, tituloFinal)
        }
    }

    private fun verificarTituloDisponible(titulo: String, onResult: (Boolean) -> Unit) {
        val tituloNormalizado = normalizarTituloMetodo(titulo)

        database.getReference("ConfiguracionTienda")
            .child("metodosPago")
            .get()
            .addOnSuccessListener { snapshot ->
                if (!estaVistaActiva()) return@addOnSuccessListener

                var duplicado = false

                for (child in snapshot.children) {
                    val metodo = child.getValue(MetodoPagoConfig::class.java) ?: continue
                    if (metodo.id == metodoIdEditar) continue

                    if (normalizarTituloMetodo(metodo.titulo) == tituloNormalizado) {
                        duplicado = true
                        break
                    }
                }

                onResult(!duplicado)
            }
            .addOnFailureListener {
                if (!estaVistaActiva()) return@addOnFailureListener
                onResult(true)
            }
    }

    private fun normalizarTituloMetodo(titulo: String): String {
        return titulo
            .trim()
            .lowercase(Locale.getDefault())
            .replace(Regex("\\s+"), " ")
    }

    private fun obtenerTituloFinalFormulario(): String {
        val b = binding ?: return ""
        val categoria = mapearCategoria(b.autoCategoria.text?.toString()?.trim().orEmpty())
        val tituloIngresado = b.edtTituloMetodo.text?.toString()?.trim().orEmpty()

        return when {
            tituloIngresado.isNotEmpty() -> tituloIngresado
            categoria == "efectivo" -> tituloPorDefectoSegunCategoria(categoria)
            categoria == "tarjeta" -> tituloPorDefectoSegunCategoria(categoria)
            categoria == "mixto" -> tituloPorDefectoSegunCategoria(categoria)
            else -> ""
        }
    }

    private fun tituloPorDefectoSegunCategoria(categoria: String): String {
        return when (categoria) {
            "efectivo" -> "Efectivo"
            "tarjeta" -> "Tarjeta"
            "mixto" -> "Cobro mixto"
            else -> ""
        }
    }

    private fun MetodoPagoConfig.toAuditoriaMap(): Map<String, Any?> {
        return linkedMapOf(
            "titulo" to titulo.trim(),
            "categoria" to categoria.trim(),
            "activo" to activo,
            "permiteVuelto" to permiteVuelto,
            "solicitaMontoRecibido" to solicitaMontoRecibido,
            "calculaVuelto" to calculaVuelto,
            "permiteReferencia" to permiteReferencia,
            "usaQR" to usaQR,
            "disponibleMixto" to disponibleMixto,
            "banco" to banco.trim(),
            "tipoCuenta" to tipoCuenta.trim(),
            "numeroCuenta" to numeroCuenta.trim(),
            "titularBanco" to titularBanco.trim(),
            "documentoBanco" to documentoBanco.trim(),
            "telefonoBilletera" to telefonoBilletera.trim(),
            "titularBilletera" to titularBilletera.trim(),
            "aliasBilletera" to aliasBilletera.trim(),
            "qrUrl" to qrUrl.trim(),
            "instrucciones" to instrucciones.trim(),
            "descripcion" to descripcion.trim(),
            "orden" to orden
        )
    }

    private fun construirCambiosAntesDespuesMetodoPago(
        anterior: MetodoPagoConfig?,
        actual: MetodoPagoConfig
    ): Map<String, Map<String, Any>> {
        if (anterior == null) return emptyMap()

        val mapaAnterior = anterior.toAuditoriaMap()
        val mapaActual = actual.toAuditoriaMap()
        val cambios = linkedMapOf<String, Map<String, Any>>()

        mapaActual.forEach { (campo, valorActual) ->
            val valorAnterior = mapaAnterior[campo]
            if (valorAnterior == valorActual) return@forEach
            cambios[campo] = mapOf(
                "antes" to valorAuditoriaMetodoPago(valorAnterior),
                "despues" to valorAuditoriaMetodoPago(valorActual)
            )
        }

        return cambios
    }

    private fun construirCambiosAltaMetodoPagoBottomSheet(
        actual: MetodoPagoConfig
    ): Map<String, Map<String, Any>> {
        val cambios = linkedMapOf<String, Map<String, Any>>()
        actual.toFirebaseMapCompact()
            .forEach { (campo, valorActual) ->
                if (campo == "id") return@forEach
                if (!debeIncluirCampoAuditoriaCreacion(valorActual)) return@forEach
                cambios[campo] = mapOf(
                    "antes" to "(vacio)",
                    "despues" to valorAuditoriaMetodoPago(valorActual)
                )
            }
        return cambios
    }

    private fun valorAuditoriaMetodoPago(valor: Any?): Any {
        return when (valor) {
            null -> "(vacio)"
            is String -> valor.ifBlank { "(vacio)" }
            is Boolean, is Number -> valor
            else -> valor.toString()
        }
    }

    private fun debeIncluirCampoAuditoriaCreacion(valor: Any?): Boolean {
        return when (valor) {
            null -> false
            is String -> valor.isNotBlank()
            is Number -> valor.toDouble() != 0.0
            is Boolean -> valor
            else -> true
        }
    }

    private fun programarValidacionTituloTiempoReal() {
        runnableValidacionTitulo?.let { handlerValidacionTitulo.removeCallbacks(it) }

        runnableValidacionTitulo = Runnable {
            if (!estaVistaActiva() || guardando) return@Runnable

            val b = binding ?: return@Runnable
            val categoria = mapearCategoria(b.autoCategoria.text?.toString()?.trim().orEmpty())
            val tituloFinal = obtenerTituloFinalFormulario()

            if (categoria.isBlank() || tituloFinal.isBlank()) {
                b.layoutTituloMetodo.error = null
                return@Runnable
            }

            verificarTituloDisponible(tituloFinal) { disponible ->
                if (!estaVistaActiva()) return@verificarTituloDisponible

                binding?.layoutTituloMetodo?.error = if (disponible) {
                    null
                } else {
                    "Ya existe un pago con este título"
                }
            }
        }

        handlerValidacionTitulo.postDelayed(runnableValidacionTitulo!!, 350)
    }

    private fun enfocarPrimerError() {
        val b = binding ?: return

        when {
            !b.layoutTituloMetodo.error.isNullOrBlank() -> enfocarEnCampoConError(b.edtTituloMetodo)
            !b.layoutCategoria.error.isNullOrBlank() -> enfocarEnCampoConError(b.autoCategoria)
            !b.layoutBanco.error.isNullOrBlank() -> enfocarEnCampoConError(b.edtBanco)
            !b.layoutTipoCuenta.error.isNullOrBlank() -> enfocarEnCampoConError(b.autoTipoCuenta)
            !b.layoutNumeroCuenta.error.isNullOrBlank() -> enfocarEnCampoConError(b.edtNumeroCuenta)
            !b.layoutTitularBanco.error.isNullOrBlank() -> enfocarEnCampoConError(b.edtTitularBanco)
            !b.layoutTelefonoBilletera.error.isNullOrBlank() -> enfocarEnCampoConError(b.edtTelefonoBilletera)
            !b.layoutTitularBilletera.error.isNullOrBlank() -> enfocarEnCampoConError(b.edtTitularBilletera)
        }
    }

    private fun enfocarEnCampoConError(view: View) {
        val b = binding ?: return
        b.root.post {
            view.requestFocus()
            b.root.smoothScrollTo(0, view.top.coerceAtLeast(0))
        }
    }

    private fun subirQrBilletera(metodoId: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val uri = qrBilleteraUri
        if (uri == null) {
            onSuccess("")
            return
        }

        val mimeType = requireContext().contentResolver.getType(uri).orEmpty()
        if (!mimeType.startsWith("image/")) {
            onError("El archivo seleccionado no es una imagen válida")
            return
        }

        Thread {
            try {
                val boundary = "Boundary-${UUID.randomUUID()}"
                val nombreBase = sanitizarNombreArchivo("qr_metodo_$metodoId")
                val endpoint = URL("https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD_NAME/image/upload")
                val connection = (endpoint.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doInput = true
                    doOutput = true
                    useCaches = false
                    setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                }

                DataOutputStream(connection.outputStream).use { output ->
                    fun writeFormField(name: String, value: String) {
                        output.writeBytes("--$boundary\r\n")
                        output.writeBytes("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
                        output.writeBytes(value)
                        output.writeBytes("\r\n")
                    }

                    val extension = when (mimeType.lowercase(Locale.getDefault())) {
                        "image/png" -> "png"
                        "image/webp" -> "webp"
                        else -> "jpg"
                    }

                    writeFormField("upload_preset", CLOUDINARY_UPLOAD_PRESET)
                    writeFormField("public_id", nombreBase)

                    output.writeBytes("--$boundary\r\n")
                    output.writeBytes(
                        "Content-Disposition: form-data; name=\"file\"; filename=\"$nombreBase.$extension\"\r\n"
                    )
                    output.writeBytes("Content-Type: $mimeType\r\n\r\n")

                    requireContext().contentResolver.openInputStream(uri)?.use { input ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                        }
                    } ?: throw Exception("No se pudo leer el QR seleccionado")

                    output.writeBytes("\r\n")
                    output.writeBytes("--$boundary--\r\n")
                    output.flush()
                }

                val responseCode = connection.responseCode
                val responseBody = if (responseCode in 200..299) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                }

                if (responseCode !in 200..299) {
                    throw Exception(responseBody.ifBlank { "No se pudo subir el QR" })
                }

                val secureUrl = JSONObject(responseBody).optString("secure_url")
                if (secureUrl.isBlank()) {
                    throw Exception("Cloudinary no devolvi\u00f3 la URL del QR")
                }

                activity?.runOnUiThread {
                    if (estaVistaActiva()) {
                        onSuccess(secureUrl)
                    }
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    if (estaVistaActiva()) {
                        onError(e.message ?: "No se pudo subir el QR")
                    }
                }
            }
        }.start()
    }

    private fun sanitizarNombreArchivo(nombre: String): String {
        return nombre
            .trim()
            .lowercase(Locale.getDefault())
            .replace("/", "_")
            .replace("\\", "_")
            .replace(".", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_")
            .replace(" ", "_")
    }

    private fun guardarMetodoEnBase(
        nuevoId: String,
        qrUrl: String,
        categoria: String,
        tituloFinal: String
    ) {
        val b = binding ?: return
        val tituloSeguro = tituloFinal.ifBlank { tituloPorDefectoSegunCategoria(categoria) }
            .trim()

        // Para categorias como billetera/transferencia exigimos nombre personalizado.
        if (tituloSeguro.isBlank()) {
            mostrarEstadoGuardando(false)
            b.layoutTituloMetodo.error = "Ingresa un nombre para este metodo"
            enfocarEnCampoConError(b.edtTituloMetodo)
            return
        }

        val metodo = MetodoPagoConfig(
            id = nuevoId,
            titulo = tituloSeguro,
            categoria = categoria,
            activo = b.switchActivo.isChecked,

            permiteVuelto = b.switchPermiteVuelto.isChecked,
            solicitaMontoRecibido = b.switchSolicitaMontoRecibido.isChecked,
            calculaVuelto = b.switchCalculaVuelto.isChecked,
            permiteReferencia = b.switchPermiteReferencia.isChecked,
            usaQR = if (categoria == "billetera_digital") {
                b.switchQrBilleteraOpcional.isChecked && qrUrl.isNotBlank()
            } else {
                b.switchUsaQR.isChecked
            },
            disponibleMixto = b.switchDisponibleMixto.isChecked,

            banco = b.edtBanco.text?.toString()?.trim().orEmpty(),
            tipoCuenta = b.autoTipoCuenta.text?.toString()?.trim().orEmpty(),
            numeroCuenta = b.edtNumeroCuenta.text?.toString()?.trim().orEmpty(),
            titularBanco = b.edtTitularBanco.text?.toString()?.trim().orEmpty(),
            documentoBanco = b.edtDocumentoBanco.text?.toString()?.trim().orEmpty(),

            telefonoBilletera = b.edtTelefonoBilletera.text?.toString()?.trim().orEmpty(),
            titularBilletera = b.edtTitularBilletera.text?.toString()?.trim().orEmpty(),
            aliasBilletera = b.edtAliasBilletera.text?.toString()?.trim().orEmpty(),
            qrUrl = qrUrl,
            instrucciones = b.edtInstrucciones.text?.toString()?.trim().orEmpty(),

            descripcion = b.edtDescripcion.text?.toString()?.trim().orEmpty(),
            orden = b.edtOrden.text?.toString()?.trim()?.toIntOrNull() ?: 0
        )

        database.getReference("ConfiguracionTienda")
            .child("metodosPago")
            .child(nuevoId)
            .setValue(metodo.toFirebaseMapCompact())
            .addOnSuccessListener {
                if (!estaVistaActiva()) return@addOnSuccessListener
                val tituloMovimiento = metodo.titulo.ifBlank { "Metodo sin nombre" }
                val cambiosAntesDespues = if (modoEdicion) {
                    construirCambiosAntesDespuesMetodoPago(
                        anterior = metodoOriginalParaAuditoria,
                        actual = metodo
                    )
                } else {
                    construirCambiosAltaMetodoPagoBottomSheet(metodo)
                }

                val extraMovimiento = mutableMapOf<String, Any>(
                    "seccion" to "metodosPago",
                    "metodoId" to nuevoId,
                    "tituloMetodo" to tituloMovimiento,
                    "categoria" to metodo.categoria,
                    "activo" to metodo.activo,
                    "usaQR" to metodo.usaQR,
                    "orden" to metodo.orden,
                    "motivo" to if (modoEdicion) {
                        "Edicion manual de metodo de pago"
                    } else {
                        "Creacion manual de metodo de pago"
                    }
                )

                if (cambiosAntesDespues.isNotEmpty()) {
                    extraMovimiento["cantidadCambios"] = cambiosAntesDespues.size
                    extraMovimiento["cambiosAntesDespues"] = cambiosAntesDespues
                }

                MovimientoLogger.registrarConSesion(
                    context = requireContext(),
                    tipo = if (modoEdicion) "metodo_pago_actualizado" else "metodo_pago_creado",
                    modulo = "configuracion_tienda",
                    titulo = if (modoEdicion) {
                        "Metodo de pago actualizado: $tituloMovimiento"
                    } else {
                        "Metodo de pago creado: $tituloMovimiento"
                    },
                    descripcion = if (modoEdicion) {
                        "Se actualizo el metodo de pago '$tituloMovimiento'."
                    } else {
                        "Se creo el metodo de pago '$tituloMovimiento'."
                    },
                    referenciaId = nuevoId,
                    extra = extraMovimiento
                )
                mostrarEstadoGuardando(false)
                mostrarToast(
                    if (modoEdicion) "Tipo de pago actualizado" else "Tipo de pago guardado"
                )
                metodoOriginalParaAuditoria = metodo.copy()
                cerrarSiEsPosible()
            }
            .addOnFailureListener { e ->
                if (!estaVistaActiva()) return@addOnFailureListener
                mostrarEstadoGuardando(false)
                mostrarToast("Error al guardar: ${e.message ?: "desconocido"}")
            }
    }

    private fun validarTransferencia(): Boolean {
        val b = binding ?: return false

        val banco = b.edtBanco.text?.toString()?.trim().orEmpty()
        val tipoCuenta = b.autoTipoCuenta.text?.toString()?.trim().orEmpty()
        val numeroCuenta = b.edtNumeroCuenta.text?.toString()?.trim().orEmpty()
        val titular = b.edtTitularBanco.text?.toString()?.trim().orEmpty()

        if (banco.isEmpty()) {
            b.layoutBanco.error = "Ingresa el banco"
            enfocarPrimerError()
            return false
        }

        if (tipoCuenta.isEmpty()) {
            b.layoutTipoCuenta.error = "Selecciona el tipo de cuenta"
            enfocarPrimerError()
            return false
        }

        if (tipoCuenta !in tiposCuenta) {
            b.layoutTipoCuenta.error = "Tipo de cuenta no válido"
            return false
        }

        if (numeroCuenta.isEmpty()) {
            b.layoutNumeroCuenta.error = "Ingresa el número de cuenta"
            return false
        }

        if (numeroCuenta.length < 6) {
            b.layoutNumeroCuenta.error = "Número de cuenta demasiado corto"
            return false
        }

        if (titular.isEmpty()) {
            b.layoutTitularBanco.error = "Ingresa el titular"
            return false
        }

        return true
    }

    private fun validarBilletera(): Boolean {
        val b = binding ?: return false

        val telefono = b.edtTelefonoBilletera.text?.toString()?.trim().orEmpty()
        val titular = b.edtTitularBilletera.text?.toString()?.trim().orEmpty()

        if (telefono.isEmpty()) {
            b.layoutTelefonoBilletera.error = "Ingresa el teléfono"
            return false
        }

        if (telefono.length < 6) {
            b.layoutTelefonoBilletera.error = "Teléfono no válido"
            return false
        }

        if (titular.isEmpty()) {
            b.layoutTitularBilletera.error = "Ingresa el titular"
            return false
        }

        return true
    }

    private fun limpiarErrores() {
        val b = binding ?: return

        b.layoutTituloMetodo.error = null
        b.layoutCategoria.error = null

        b.layoutBanco.error = null
        b.layoutTipoCuenta.error = null
        b.layoutNumeroCuenta.error = null
        b.layoutTitularBanco.error = null

        b.layoutTelefonoBilletera.error = null
        b.layoutTitularBilletera.error = null
    }

    private fun mapearCategoria(categoriaVisible: String): String {
        return when (categoriaVisible) {
            "Efectivo" -> "efectivo"
            "Tarjeta" -> "tarjeta"
            "Transferencia bancaria" -> "transferencia_bancaria"
            "Billetera digital" -> "billetera_digital"
            "Cobro mixto" -> "mixto"
            "Otro" -> "otro"
            else -> ""
        }
    }

    private fun mostrarToast(mensaje: String) {
        val ctx = context ?: return
        Toast.makeText(ctx, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun resetRestricciones() {
        val b = binding ?: return

        b.switchPermiteVuelto.isEnabled = true
        b.switchSolicitaMontoRecibido.isEnabled = true
        b.switchCalculaVuelto.isEnabled = true
        b.switchPermiteReferencia.isEnabled = true
        b.switchUsaQR.isEnabled = true
        b.switchDisponibleMixto.isEnabled = true
        b.switchQrBilleteraOpcional.isEnabled = true

        b.edtBanco.isEnabled = true
        b.autoTipoCuenta.isEnabled = true
        b.edtNumeroCuenta.isEnabled = true
        b.edtTitularBanco.isEnabled = true
        b.edtDocumentoBanco.isEnabled = true

        b.edtTelefonoBilletera.isEnabled = true
        b.edtTitularBilletera.isEnabled = true
        b.edtAliasBilletera.isEnabled = true
        b.edtInstrucciones.isEnabled = true
    }

    private fun desactivarBloqueTransferencia() {
        val b = binding ?: return

        b.edtBanco.setText("")
        b.autoTipoCuenta.setText("", false)
        b.edtNumeroCuenta.setText("")
        b.edtTitularBanco.setText("")
        b.edtDocumentoBanco.setText("")

        b.edtBanco.isEnabled = false
        b.autoTipoCuenta.isEnabled = false
        b.edtNumeroCuenta.isEnabled = false
        b.edtTitularBanco.isEnabled = false
        b.edtDocumentoBanco.isEnabled = false
    }

    private fun desactivarBloqueBilletera() {
        val b = binding ?: return

        b.edtTelefonoBilletera.setText("")
        b.edtTitularBilletera.setText("")
        b.edtAliasBilletera.setText("")
        b.edtInstrucciones.setText("")

        b.switchQrBilleteraOpcional.isChecked = false
        b.switchQrBilleteraOpcional.isEnabled = false
        b.layoutQrBilletera.visibility = View.GONE
        qrBilleteraUri = null
        qrBilleteraUrlGuardada = ""
        b.imgQrBilleteraPreview.setImageResource(android.R.drawable.ic_menu_gallery)

        b.edtTelefonoBilletera.isEnabled = false
        b.edtTitularBilletera.isEnabled = false
        b.edtAliasBilletera.isEnabled = false
        b.edtInstrucciones.isEnabled = false
    }

    private fun bloquearSwitch(switch: SwitchMaterial, checked: Boolean) {
        switch.isChecked = checked
        switch.isEnabled = false
    }

    private fun mostrarEstadoGuardando(mostrar: Boolean) {
        guardando = mostrar
        val b = binding ?: return

        b.progressCrearTipoPago.visibility = if (mostrar) View.VISIBLE else View.GONE
        b.textGuardando.visibility = if (mostrar) View.VISIBLE else View.GONE

        val habilitado = !mostrar

        b.btnGuardar.isEnabled = habilitado
        b.btnCancelar.isEnabled = habilitado
        b.btnGuardar.alpha = if (habilitado) 1f else 0.6f
        b.btnCancelar.alpha = if (habilitado) 1f else 0.6f

        b.autoCategoria.isEnabled = habilitado
        b.switchActivo.isEnabled = habilitado

        aplicarConfiguracionPorCategoria(
            b.autoCategoria.text?.toString()?.trim().orEmpty()
        )

        b.edtDescripcion.isEnabled = habilitado
        b.edtOrden.isEnabled = habilitado
        b.btnSeleccionarQrBilletera.isEnabled = habilitado
        b.btnQuitarQrBilletera.isEnabled = habilitado

        (dialog as? BottomSheetDialog)?.let { dlg ->
            dlg.setCancelable(!mostrar)
            dlg.setCanceledOnTouchOutside(false)

            dlg.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let { bottomSheet ->
                BottomSheetBehavior.from(bottomSheet).isDraggable = !mostrar
            }
        }
    }

    private fun estaVistaActiva(): Boolean {
        return isAdded && _binding != null && view != null
    }

    private fun cerrarSiEsPosible() {
        if (!isAdded) return
        dismissAllowingStateLoss()
    }
}

