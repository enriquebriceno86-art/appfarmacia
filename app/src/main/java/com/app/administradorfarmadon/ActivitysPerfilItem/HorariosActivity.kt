package com.app.administradorfarmadon.ActivitysPerfilItem

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.administradorfarmadon.ActivitysPerfilItem.Reciclers.AdapterExcepciones
import com.app.administradorfarmadon.ActivitysPerfilItem.Reciclers.AdapterHorarios
import com.app.administradorfarmadon.ClasesDatabase.MovimientoLogger
import com.app.administradorfarmadon.ClasesDatabase.SessionManager
import com.app.administradorfarmadon.R
import com.app.administradorfarmadon.databinding.ActivityHorariosBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HorariosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHorariosBinding
    private lateinit var adapterHorarios: AdapterHorarios
    private lateinit var adapterExcepciones: AdapterExcepciones
    private val listaHorarios = mutableListOf<HorarioTienda>()
    private val listaExcepciones = mutableListOf<HorarioExcepcion>()
    
    private val aiRepository = HorarioAiRepository()
    private val idsAiDescartados = mutableSetOf<String>()
    private var aiAnalysisJob: Job? = null

    private fun aplicarCierreDropdownAlRecibirFoco(autoComplete: MaterialAutoCompleteTextView) {
        autoComplete.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                (v as? MaterialAutoCompleteTextView)?.dismissDropDown()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHorariosBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        configurarRecycler()
        clics()
        verificarSiExistenHorarios()
        verificarExcepciones()
    }

    private fun iniciarAnalisisIA() {
        aiAnalysisJob?.cancel()
        aiAnalysisJob = lifecycleScope.launch {
            delay(1000) // Debounce para evitar ráfagas durante carga inicial
            
            // Regla UX: Si hay menos de 3 días, solo sugerencia inicial si corresponde.
            // Si hay horarios, esperamos a tener ventas reales para oportunidades complejas.
            val config = aiRepository.getStoreConfig() ?: return@launch
            val ventas = aiRepository.getVentasRecientes(14) 
            
            val sugerenciaInicial = if (listaHorarios.size < 3) {
                HorarioAiRules.generarSugerenciaInicial(config)
            } else null
            
            // Solo buscar oportunidades y alertas si ya hay una base mínima configurada
            val oportunidades = if (listaHorarios.size >= 3) {
                HorarioAiRules.analizarOportunidades(ventas, listaHorarios)
            } else emptyList()
            
            val alertas = if (listaHorarios.size >= 3) {
                HorarioAiRules.detectarAlertas(ventas, listaHorarios, listaExcepciones)
            } else emptyList()
            
            renderizarResultadoIA(AiAnalysisResult(sugerenciaInicial, oportunidades, alertas))
        }
    }

    private fun renderizarResultadoIA(resultado: AiAnalysisResult) {
        binding.layoutAiContainer?.removeAllViews()
        var hayCards = false
        
        // 1. Sugerencia Inicial
        resultado.sugerenciaInicial?.let { 
            if (it.id !in idsAiDescartados) {
                // Mensaje más directo
                val descDirecta = "Te faltan configurar ${7 - listaHorarios.size} días. ¿Quieres aplicar un horario base de farmacia?"
                agregarCardIA(it.id, "Configuración Rápida", descDirecta, null,
                    onViewDetail = { mostrarDetalleSugerencia(it, soloVer = true) },
                    onApply = { mostrarDetalleSugerencia(it, soloVer = false) }
                )
                hayCards = true
            }
        }
        
        // 2. Oportunidades
        resultado.oportunidades.forEach { op ->
            if (op.id !in idsAiDescartados) {
                // Mensaje más accionable
                val tituloAccion = when(op.accion) {
                    "EXTENDER" -> "Sugerencia: Extender horario"
                    "ABRIR" -> "Sugerencia: Abrir el domingo"
                    else -> op.titulo
                }
                agregarCardIA(op.id, tituloAccion, op.descripcion, op.detalleTecnico,
                    onViewDetail = { mostrarDetalleOportunidad(op, soloVer = true) },
                    onApply = { mostrarDetalleOportunidad(op, soloVer = false) }
                )
                hayCards = true
            }
        }
        
        // 3. Alertas
        resultado.alertas.forEach { alerta ->
            if (alerta.id !in idsAiDescartados) {
                agregarCardAlertaIA(alerta)
                hayCards = true
            }
        }

        binding.layoutAiContainer?.visibility = if (hayCards) View.VISIBLE else View.GONE
    }

    @SuppressLint("InflateParams")
    private fun agregarCardIA(
        id: String,
        titulo: String,
        descripcion: String,
        detalle: String?,
        onViewDetail: () -> Unit,
        onApply: () -> Unit
    ) {
        val cardView = layoutInflater.inflate(R.layout.layout_ai_card, binding.layoutAiContainer, false)
        val txtTitle = cardView.findViewById<TextView>(R.id.txtAiTitle)
        val txtDesc = cardView.findViewById<TextView>(R.id.txtAiDescription)
        val txtDetail = cardView.findViewById<TextView>(R.id.txtAiDetail)
        val btnApply = cardView.findViewById<MaterialButton>(R.id.btnApplyAi)
        val btnView = cardView.findViewById<MaterialButton>(R.id.btnViewAiDetail)
        val btnDiscard = cardView.findViewById<View>(R.id.btnDiscardAi)

        txtTitle.text = titulo
        txtDesc.text = descripcion
        if (detalle != null) {
            txtDetail.text = detalle
            txtDetail.visibility = View.VISIBLE
        }

        btnApply.setOnClickListener { onApply() }
        btnView.setOnClickListener { onViewDetail() }
        btnDiscard.setOnClickListener {
            idsAiDescartados.add(id)
            binding.layoutAiContainer?.removeView(cardView)
            if (binding.layoutAiContainer?.childCount == 0) {
                binding.layoutAiContainer?.visibility = View.GONE
            }
        }

        binding.layoutAiContainer?.addView(cardView)
    }

    private fun agregarCardAlertaIA(alerta: AiAlert) {
        val cardView = layoutInflater.inflate(R.layout.layout_ai_card, binding.layoutAiContainer, false)
        val txtTitle = cardView.findViewById<TextView>(R.id.txtAiTitle)
        val txtDesc = cardView.findViewById<TextView>(R.id.txtAiDescription)
        val badge = cardView.findViewById<TextView>(R.id.txtAiBadge)
        val btnApply = cardView.findViewById<MaterialButton>(R.id.btnApplyAi)
        val btnView = cardView.findViewById<MaterialButton>(R.id.btnViewAiDetail)
        val imgIcon = cardView.findViewById<android.widget.ImageView>(R.id.imgAiIcon)

        txtTitle.text = alerta.titulo
        txtDesc.text = alerta.descripcion
        badge.text = "ALERTA"
        badge.setBackgroundResource(R.drawable.bg_badge_ai_warning)
        badge.setTextColor("#92400E".toColorInt())
        imgIcon.setImageResource(R.drawable.ic_dashboard_warning)
        imgIcon.imageTintList = android.content.res.ColorStateList.valueOf("#D97706".toColorInt())

        btnApply.visibility = View.GONE
        btnView.text = "Entendido"
        btnView.setOnClickListener {
            idsAiDescartados.add(alerta.id)
            binding.layoutAiContainer?.removeView(cardView)
            if (binding.layoutAiContainer?.childCount == 0) {
                binding.layoutAiContainer?.visibility = View.GONE
            }
        }

        binding.layoutAiContainer?.addView(cardView)
    }

    private fun mostrarDetalleSugerencia(sugerencia: AiSuggestion, soloVer: Boolean) {
        val msg = StringBuilder()
        sugerencia.horarioSugerido.forEach {
            msg.append("${it.dia}: ${if(it.cerrado) "Cerrado" else "${it.horaApertura} - ${it.horaCierre}"}\n")
        }

        val builder = MaterialAlertDialogBuilder(this)
            .setTitle(sugerencia.titulo)
            .setMessage("${sugerencia.motivo}\n\nHorario sugerido:\n$msg")
            .setNegativeButton("Cerrar", null)
            
        if (!soloVer) {
            builder.setPositiveButton("Aplicar") { _, _ -> aplicarSugerenciaIA(sugerencia) }
        }
        
        builder.show()
    }

    private fun mostrarDetalleOportunidad(op: AiOpportunity, soloVer: Boolean) {
        val msg = StringBuilder()
        op.cambiosSugeridos.forEach {
            msg.append("${it.dia}: ${it.horaApertura} - ${it.horaCierre}\n")
        }

        val builder = MaterialAlertDialogBuilder(this)
            .setTitle(op.titulo)
            .setMessage("${op.descripcion}\n\n${op.detalleTecnico}\n\nCambios propuestos:\n$msg")
            .setNegativeButton("Cerrar", null)
            
        if (!soloVer) {
            builder.setPositiveButton("Aplicar") { _, _ -> aplicarOportunidadIA(op) }
        }
        
        builder.show()
    }

    private fun aplicarSugerenciaIA(sugerencia: AiSuggestion) {
        lifecycleScope.launch {
            val reference = FirebaseDatabase.getInstance().getReference("ConfiguracionTienda").child("Horarios")
            val updates = mutableMapOf<String, Any>()
            val cambiosAudit = mutableListOf<Pair<HorarioTienda?, HorarioTienda>>()
            
            sugerencia.horarioSugerido.forEach { horario ->
                val anterior = buscarHorarioPorDia(horario.dia)
                // Aseguramos que si la sugerencia marca el día como cerrado, se use la lógica de contexto de cierre
                val horarioConContexto = construirHorarioConContextoCierre(
                    dia = horario.dia,
                    apertura = horario.horaApertura,
                    cierre = horario.horaCierre,
                    cerrado = horario.cerrado,
                    v24 = horario.veinticuatroHoras,
                    horarioAnterior = anterior
                )
                val hFinal = HorarioPayloadRules.prepararHorarioParaGuardado(horarioConContexto)
                updates[hFinal.id] = hFinal.horario
                cambiosAudit.add(anterior to hFinal.horario)
            }
            
            reference.updateChildren(updates).addOnSuccessListener {
                cambiosAudit.forEach { (ant, act) ->
                    registrarMovimientoHorario(ant, act, "Sugerencia IA inicial aplicada")
                }
                Toast.makeText(this@HorariosActivity, "Sugerencia aplicada con éxito", Toast.LENGTH_SHORT).show()
                idsAiDescartados.add(sugerencia.id)
                iniciarAnalisisIA() // Recalcular
            }
        }
    }

    private fun aplicarOportunidadIA(op: AiOpportunity) {
        val reference = FirebaseDatabase.getInstance().getReference("ConfiguracionTienda").child("Horarios")
        val updates = mutableMapOf<String, Any>()
        val cambiosAudit = mutableListOf<Pair<HorarioTienda?, HorarioTienda>>()

        op.cambiosSugeridos.forEach { horario ->
            val anterior = buscarHorarioPorDia(horario.dia)
            // Aseguramos trazabilidad de cierre si la oportunidad implica cerrar el local
            val horarioConContexto = construirHorarioConContextoCierre(
                dia = horario.dia,
                apertura = horario.horaApertura,
                cierre = horario.horaCierre,
                cerrado = horario.cerrado,
                v24 = horario.veinticuatroHoras,
                horarioAnterior = anterior
            )
            val hFinal = HorarioPayloadRules.prepararHorarioParaGuardado(horarioConContexto)
            updates[hFinal.id] = hFinal.horario
            cambiosAudit.add(anterior to hFinal.horario)
        }

        reference.updateChildren(updates).addOnSuccessListener {
            cambiosAudit.forEach { (ant, act) ->
                registrarMovimientoHorario(ant, act, "Oportunidad detectada por IA: ${op.titulo}")
            }
            Toast.makeText(this@HorariosActivity, "Cambio aplicado con éxito", Toast.LENGTH_SHORT).show()
            idsAiDescartados.add(op.id)
            iniciarAnalisisIA()
        }
    }

    private fun configurarRecycler() {
        binding.recyclerHorarios.layoutManager = LinearLayoutManager(this)
        adapterHorarios = AdapterHorarios { horario ->
            mostrarBottomSheetCrearHorario(horario)
        }
        binding.recyclerHorarios.adapter = adapterHorarios

        binding.recyclerExcepciones?.layoutManager = LinearLayoutManager(this)
        adapterExcepciones = AdapterExcepciones { excepcion ->
            MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar Excepción")
                .setMessage("¿Estás seguro de que deseas eliminar esta excepción de horario?")
                .setPositiveButton("Eliminar") { _, _ ->
                    eliminarExcepcionConAuditoria(excepcion)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        binding.recyclerExcepciones?.adapter = adapterExcepciones
    }

    private fun clics() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.toolbar.inflateMenu(R.menu.menu_horarios)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_templates) {
                mostrarDialogoPlantillas()
                true
            } else false
        }

        binding.btnAgregarExcepcion?.setOnClickListener {
            mostrarBottomSheetCrearExcepcion()
        }

        // Configuración de chips de plantillas
        val plantillas = HorarioTemplateRules.obtenerPlantillas()
        binding.chipTemplateLaborales?.setOnClickListener { confirmarYAplicarPlantilla(plantillas[0]) }
        binding.chipTemplateFull?.setOnClickListener { confirmarYAplicarPlantilla(plantillas[1]) }
        binding.chipTemplate24h?.setOnClickListener { confirmarYAplicarPlantilla(plantillas[2]) }
        binding.chipTemplateCerrarDomingo?.setOnClickListener { confirmarYAplicarPlantilla(plantillas[3]) }

        binding.chipEmptyTemplateLaborales?.setOnClickListener { confirmarYAplicarPlantilla(plantillas[0]) }
        binding.chipEmptyTemplate24h?.setOnClickListener { confirmarYAplicarPlantilla(plantillas[2]) }

        binding.btnCrearHorario.setOnClickListener {
            mostrarBottomSheetCrearHorario(null)
        }

        binding.btnAgregarHorarioInline?.setOnClickListener {
            mostrarBottomSheetCrearHorario(null)
        }
    }

    private fun verificarSiExistenHorarios() {
        val reference = FirebaseDatabase.getInstance()
            .getReference("ConfiguracionTienda")
            .child("Horarios")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaHorarios.clear()
                if (snapshot.exists()) {
                    for (dato in snapshot.children) {
                        val horario = dato.getValue(HorarioTienda::class.java)
                        if (horario != null) {
                            listaHorarios.add(horario)
                        }
                    }
                }

                ordenarHorarios()
                actualizarVista()
                adapterHorarios.updateList(listaHorarios.toList(), listaExcepciones.toList())
                iniciarAnalisisIA()
            }

            override fun onCancelled(error: DatabaseError) {
                MaterialAlertDialogBuilder(this@HorariosActivity)
                    .setTitle("Error de carga")
                    .setMessage("No pudimos obtener los horarios de la tienda: ${error.message}")
                    .setPositiveButton("Reintentar") { _, _ -> verificarSiExistenHorarios() }
                    .setNegativeButton("Cerrar", null)
                    .show()
            }
        })
    }

    private fun verificarExcepciones() {
        val reference = FirebaseDatabase.getInstance()
            .getReference("ConfiguracionTienda")
            .child("Excepciones")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaExcepciones.clear()
                if (snapshot.exists()) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val hoyStr = sdf.format(Date()) // Fecha actual local en formato comparable

                    for (dato in snapshot.children) {
                        val exc = dato.getValue(HorarioExcepcion::class.java)
                        if (exc != null) {
                            // Solo mostrar excepciones del día de hoy o futuras
                            if (exc.fecha >= hoyStr) {
                                listaExcepciones.add(exc)
                            }
                        }
                    }
                }
                adapterExcepciones.updateList(listaExcepciones.sortedBy { it.fecha })
                adapterHorarios.updateList(listaHorarios.toList(), listaExcepciones.toList())
                iniciarAnalisisIA()
                binding.layoutExcepciones?.visibility = if (listaExcepciones.isNotEmpty()) View.VISIBLE else {
                    if (listaHorarios.isNotEmpty()) View.VISIBLE else View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun mostrarBottomSheetCrearExcepcion() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottomsheet_crear_excepcion, null)
        dialog.setContentView(view)

        val etMotivo = view.findViewById<TextInputEditText>(R.id.etMotivoExcepcion)
        val btnFecha = view.findViewById<MaterialButton>(R.id.btnSeleccionarFecha)
        val btnApertura = view.findViewById<MaterialButton>(R.id.btnHoraAperturaExc)
        val btnCierre = view.findViewById<MaterialButton>(R.id.btnHoraCierreExc)
        val switchCerrado = view.findViewById<MaterialSwitch>(R.id.switchCerradoExc)
        val switchV24 = view.findViewById<MaterialSwitch>(R.id.switchVeinticuatroHorasExc)
        val btnGuardar = view.findViewById<MaterialButton>(R.id.btnGuardarExcepcion)
        val cardFeedback = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardFeedbackExc)
        val txtFeedback = view.findViewById<TextView>(R.id.txtFeedbackMessageExc)

        var fechaSeleccionada = ""
        var aperturaSeleccionada = ""
        var cierreSeleccionada = ""

        fun updateUI() {
            btnApertura.isEnabled = !switchCerrado.isChecked && !switchV24.isChecked
            btnCierre.isEnabled = !switchCerrado.isChecked && !switchV24.isChecked
            if (switchCerrado.isChecked) {
                btnApertura.text = "Cerrado"
                btnCierre.text = "Cerrado"
            } else if (switchV24.isChecked) {
                btnApertura.text = "24h"
                btnCierre.text = "24h"
            } else {
                btnApertura.text = aperturaSeleccionada.ifBlank { "Apertura" }
                btnCierre.text = cierreSeleccionada.ifBlank { "Cierre" }
            }
        }

        btnFecha.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona la fecha")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                // El picker devuelve UTC. Para evitar desfases por zona horaria al formatear a yyyy-MM-dd,
                // usamos un Calendar configurado en UTC para extraer los campos y luego formatear.
                val utcCalendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                utcCalendar.timeInMillis = selection
                
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                
                fechaSeleccionada = sdf.format(utcCalendar.time)
                btnFecha.text = fechaSeleccionada
            }
            datePicker.show(supportFragmentManager, "DATE_PICKER")
        }

        btnApertura.setOnClickListener {
            mostrarTimePicker { aperturaSeleccionada = it; updateUI() }
        }
        btnCierre.setOnClickListener {
            mostrarTimePicker { cierreSeleccionada = it; updateUI() }
        }
        switchCerrado.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) switchV24.isChecked = false
            updateUI()
        }
        switchV24.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) switchCerrado.isChecked = false
            updateUI()
        }

        btnGuardar.setOnClickListener {
            val motivo = etMotivo.text.toString().trim()
            
            val errorMsg = when {
                fechaSeleccionada.isBlank() -> "Selecciona una fecha"
                motivo.isBlank() -> "Ingresa un motivo (ej: Feriado)"
                !switchCerrado.isChecked && !switchV24.isChecked && 
                    (aperturaSeleccionada.isBlank() || cierreSeleccionada.isBlank()) -> "Define el horario o marca como cerrado/24h"
                else -> null
            }

            if (errorMsg != null) {
                cardFeedback.visibility = View.VISIBLE
                cardFeedback.setCardBackgroundColor("#FFEBEE".toColorInt())
                txtFeedback.text = errorMsg
                txtFeedback.setTextColor("#B71C1C".toColorInt())
                return@setOnClickListener
            }
            
            val id = fechaSeleccionada.replace("-", "")
            
            val excepcion = HorarioExcepcion(
                id = id,
                fecha = fechaSeleccionada,
                motivo = motivo,
                horaApertura = aperturaSeleccionada,
                horaCierre = cierreSeleccionada,
                cerrado = switchCerrado.isChecked,
                veinticuatroHoras = switchV24.isChecked,
                creadoEn = System.currentTimeMillis()
            )

            btnGuardar.isEnabled = false
            FirebaseDatabase.getInstance().getReference("ConfiguracionTienda")
                .child("Excepciones").child(id).setValue(excepcion)
                .addOnSuccessListener {
                    registrarMovimientoExcepcion(excepcion, false)
                    dialog.dismiss()
                }
                .addOnFailureListener {
                    btnGuardar.isEnabled = true
                    cardFeedback.visibility = View.VISIBLE
                    cardFeedback.setCardBackgroundColor("#FFEBEE".toColorInt())
                    txtFeedback.text = "Error al guardar"
                    txtFeedback.setTextColor("#B71C1C".toColorInt())
                }
        }

        dialog.show()
    }

    private fun eliminarExcepcionConAuditoria(excepcion: HorarioExcepcion) {
        FirebaseDatabase.getInstance().getReference("ConfiguracionTienda")
            .child("Excepciones").child(excepcion.id).removeValue()
            .addOnSuccessListener {
                registrarMovimientoExcepcion(excepcion, true)
            }
    }

    private fun registrarMovimientoExcepcion(excepcion: HorarioExcepcion, eliminacion: Boolean) {
        MovimientoLogger.registrarConSesion(
            context = this,
            tipo = if (eliminacion) "horario_excepcion_eliminada" else "horario_excepcion_creada",
            modulo = "configuracion_tienda",
            titulo = if (eliminacion) "Excepción eliminada: ${excepcion.fecha}" else "Nueva excepción: ${excepcion.fecha}",
            descripcion = "${if (eliminacion) "Se eliminó" else "Se creó"} una excepción para el día ${excepcion.fecha} (${excepcion.motivo}).",
            referenciaId = excepcion.id,
            extra = mapOf(
                "fecha" to excepcion.fecha,
                "motivo" to excepcion.motivo,
                "cerrado" to excepcion.cerrado,
                "v24" to excepcion.veinticuatroHoras
            )
        )
    }

    private fun actualizarVista() {
        val hayHorarios = listaHorarios.isNotEmpty()
        binding.layoutSinHorarios.visibility = if (hayHorarios) View.GONE else View.VISIBLE
        
        binding.cardResumenSemanal.visibility = if (hayHorarios) View.VISIBLE else View.GONE
        binding.layoutPlantillasContenedor?.visibility = if (hayHorarios) View.VISIBLE else View.GONE
        binding.layoutRecyclerHorariosContenedor?.visibility = if (hayHorarios) View.VISIBLE else View.GONE
        binding.layoutExcepciones?.visibility = if (hayHorarios || listaExcepciones.isNotEmpty()) View.VISIBLE else View.GONE
        
        if (hayHorarios) {
            actualizarResumenSemanal()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun actualizarResumenSemanal() {
        val resumen = HorarioSummaryRules.calcularResumen(listaHorarios)
        binding.cardResumenSemanal.visibility = View.VISIBLE
        
        binding.txtDiasConfigurados.text = "${resumen.diasConfigurados}/7 días"
        binding.txtCerrados24h.text = "${resumen.diasCerrados} cerrados · ${resumen.diasVeinticuatroHoras} 24h"
        
        if (resumen.horaMasTemprana != null && resumen.horaMasTardia != null) {
            binding.txtRangoHoras.text = "${resumen.horaMasTemprana} a ${resumen.horaMasTardia}"
        } else if (resumen.diasVeinticuatroHoras > 0) {
            binding.txtRangoHoras.text = "Operación 24h"
        } else {
            binding.txtRangoHoras.text = "Sin horario definido"
        }
    }

    private fun ordenarHorarios() {
        val horariosOrdenados = HorarioRules.ordenarSegunDia(listaHorarios)
        listaHorarios.clear()
        listaHorarios.addAll(horariosOrdenados)
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun mostrarBottomSheetCrearHorario(horarioEditar: HorarioTienda?) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottomsheet_crear_horario, null)
        dialog.setContentView(view)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val txtTituloBottom = view.findViewById<TextView>(R.id.txtTituloBottom)
        val autoDia = view.findViewById<MaterialAutoCompleteTextView>(R.id.autoDia)
        val btnHoraApertura = view.findViewById<MaterialButton>(R.id.btnHoraApertura)
        val btnHoraCierre = view.findViewById<MaterialButton>(R.id.btnHoraCierre)
        val switchCerrado = view.findViewById<MaterialSwitch>(R.id.switchCerrado)
        val switchVeinticuatroHoras = view.findViewById<MaterialSwitch>(R.id.switchVeinticuatroHoras)
        val checkAplicarLaborales = view.findViewById<MaterialCheckBox>(R.id.checkAplicarLaborales)
        val btnGuardarHorario = view.findViewById<MaterialButton>(R.id.btnGuardarHorario)
        val btnEliminarHorario = view.findViewById<MaterialButton>(R.id.btnEliminarHorario)
        
        val cardFeedback = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardFeedback)
        val imgFeedbackIcon = view.findViewById<android.widget.ImageView>(R.id.imgFeedbackIcon)
        val txtFeedbackMessage = view.findViewById<TextView>(R.id.txtFeedbackMessage)

        fun mostrarFeedback(mensaje: String, tipo: String) {
            cardFeedback.visibility = View.VISIBLE
            txtFeedbackMessage.text = mensaje
            
            when (tipo) {
                "success" -> {
                    cardFeedback.setCardBackgroundColor("#E8F5E9".toColorInt())
                    txtFeedbackMessage.setTextColor("#1B5E20".toColorInt())
                    imgFeedbackIcon.setImageResource(R.drawable.ic_check_circle)
                    imgFeedbackIcon.imageTintList = android.content.res.ColorStateList.valueOf("#2E7D32".toColorInt())
                }
                "error" -> {
                    cardFeedback.setCardBackgroundColor("#FFEBEE".toColorInt())
                    txtFeedbackMessage.setTextColor("#B71C1C".toColorInt())
                    imgFeedbackIcon.setImageResource(R.drawable.ic_info)
                    imgFeedbackIcon.imageTintList = android.content.res.ColorStateList.valueOf("#D32F2F".toColorInt())
                }
                "warning" -> {
                    cardFeedback.setCardBackgroundColor("#FFF3E0".toColorInt())
                    txtFeedbackMessage.setTextColor("#E65100".toColorInt())
                    imgFeedbackIcon.setImageResource(R.drawable.ic_dashboard_warning)
                    imgFeedbackIcon.imageTintList = android.content.res.ColorStateList.valueOf("#F57C00".toColorInt())
                }
            }
        }

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                
                // Forzar que se expanda completamente
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
                
                // Ajustar la altura al 90% de la pantalla para dejar un pequeño margen arriba
                val layoutParams = bottomSheet.layoutParams
                val screenHeight = resources.displayMetrics.heightPixels
                layoutParams.height = (screenHeight * 0.90).toInt()
                bottomSheet.layoutParams = layoutParams
            }
        }

        val todosLosDias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        val diasYaUsados = listaHorarios.map { it.dia }.toSet()
        val diasDisponibles = if (horarioEditar != null) {
            listOf(horarioEditar.dia)
        } else {
            todosLosDias.filter { it !in diasYaUsados }
        }

        val adapterDias = ArrayAdapter(this, android.R.layout.simple_list_item_1, diasDisponibles)
        autoDia.setAdapter(adapterDias)
        aplicarCierreDropdownAlRecibirFoco(autoDia)

        var horaAperturaSeleccionada = ""
        var horaCierreSeleccionada = ""

        fun actualizarEstadoBotones() {
            val dia = autoDia.text?.toString().orEmpty()
            val estadoUi = HorarioRules.construirEstadoBotones(
                horaAperturaSeleccionada = horaAperturaSeleccionada,
                horaCierreSeleccionada = horaCierreSeleccionada,
                cerrado = switchCerrado.isChecked,
                veinticuatroHoras = switchVeinticuatroHoras.isChecked,
                dia = dia,
                esEdicion = horarioEditar != null
            )
            btnHoraApertura.isEnabled = estadoUi.horaAperturaHabilitada
            btnHoraCierre.isEnabled = estadoUi.horaCierreHabilitada
            btnHoraApertura.text = estadoUi.textoHoraApertura
            btnHoraCierre.text = estadoUi.textoHoraCierre

            // Validación en tiempo real para avisos/errores suaves
            if (!switchCerrado.isChecked && !switchVeinticuatroHoras.isChecked && 
                horaAperturaSeleccionada.isNotBlank() && horaCierreSeleccionada.isNotBlank()) {
                
                val res = HorarioRules.validarFormulario(
                    HorarioFormData(
                        dia = dia,
                        horaApertura = horaAperturaSeleccionada,
                        horaCierre = horaCierreSeleccionada,
                        cerrado = false,
                        veinticuatroHoras = false,
                        aplicarLaborales = false,
                        esEdicion = true
                    )
                )
                
                when {
                    !res.esValido -> mostrarFeedback(res.horasError ?: "Error en horas", "error")
                    res.aviso != null -> mostrarFeedback(res.aviso, "warning")
                    else -> cardFeedback.visibility = View.GONE
                }
            } else {
                cardFeedback.visibility = View.GONE
            }
        }

        fun validarVisibilidadCheckbox(dia: String) {
            val mostrar = HorarioRules.debeMostrarAplicarLaborales(
                dia = dia,
                esEdicion = horarioEditar != null
            )
            checkAplicarLaborales.visibility = if (mostrar) View.VISIBLE else View.GONE
        }

        if (horarioEditar != null) {
            txtTituloBottom.text = "Editar horario"
            btnGuardarHorario.text = "Actualizar"
            btnEliminarHorario.visibility = View.VISIBLE
            checkAplicarLaborales.visibility = View.GONE
            autoDia.setText(horarioEditar.dia, false)
            autoDia.isEnabled = false
            switchCerrado.isChecked = horarioEditar.cerrado
            switchVeinticuatroHoras.isChecked = horarioEditar.veinticuatroHoras
            horaAperturaSeleccionada = horarioEditar.horaApertura
            horaCierreSeleccionada = horarioEditar.horaCierre
        } else {
            txtTituloBottom.text = "Nuevo horario"
            btnGuardarHorario.text = "Guardar"
            btnEliminarHorario.visibility = View.GONE
            
            if (diasDisponibles.isNotEmpty()) {
                val diaInicial = diasDisponibles[0]
                autoDia.setText(diaInicial, false)
                validarVisibilidadCheckbox(diaInicial)
            }
            
            autoDia.setOnItemClickListener { _, _, _, _ ->
                validarVisibilidadCheckbox(autoDia.text.toString())
            }
        }

        actualizarEstadoBotones()

        btnHoraApertura.setOnClickListener {
            mostrarTimePicker { hora ->
                horaAperturaSeleccionada = hora
                actualizarEstadoBotones()
            }
        }

        btnHoraCierre.setOnClickListener {
            mostrarTimePicker { hora ->
                horaCierreSeleccionada = hora
                actualizarEstadoBotones()
            }
        }

        switchCerrado.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) switchVeinticuatroHoras.isChecked = false
            actualizarEstadoBotones()
        }

        switchVeinticuatroHoras.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) switchCerrado.isChecked = false
            actualizarEstadoBotones()
        }

        btnEliminarHorario.setOnClickListener {
            horarioEditar?.let {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Eliminar horario")
                    .setMessage("¿Estás seguro de que deseas eliminar el horario del día ${it.dia}?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        btnEliminarHorario.isEnabled = false
                        eliminarHorarioConAuditoria(it, dialog,
                            onSuccess = {
                                mostrarFeedback("Horario eliminado", "success")
                                btnEliminarHorario.postDelayed({ dialog.dismiss() }, 1000)
                            },
                            onFailure = { err ->
                                btnEliminarHorario.isEnabled = true
                                mostrarFeedback("Error al eliminar: $err", "error")
                            }
                        )
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        btnGuardarHorario.setOnClickListener {
            val dia = autoDia.text.toString().trim()
            val resultadoValidacion = HorarioRules.validarFormulario(
                HorarioFormData(
                    dia = dia,
                    horaApertura = horaAperturaSeleccionada,
                    horaCierre = horaCierreSeleccionada,
                    cerrado = switchCerrado.isChecked,
                    veinticuatroHoras = switchVeinticuatroHoras.isChecked,
                    aplicarLaborales = checkAplicarLaborales.isChecked,
                    esEdicion = horarioEditar != null
                )
            )
            if (!resultadoValidacion.esValido) {
                val mensaje = resultadoValidacion.diaError ?: resultadoValidacion.horasError
                if (!mensaje.isNullOrBlank()) {
                    mostrarFeedback(mensaje, "error")
                }
                return@setOnClickListener
            }

            // Si hay un aviso (turno nocturno o rango corto), lo mostramos como advertencia
            // pero permitimos continuar con el guardado.
            resultadoValidacion.aviso?.let {
                mostrarFeedback(it, "warning")
            }

            btnGuardarHorario.isEnabled = false
            if (checkAplicarLaborales.isChecked) {
                guardarHorariosLaboralesConAuditoria(
                    horaAperturaSeleccionada,
                    horaCierreSeleccionada,
                    switchCerrado.isChecked,
                    switchVeinticuatroHoras.isChecked,
                    dialog,
                    onSuccess = {
                        mostrarFeedback("Horarios laborales actualizados", "success")
                        btnGuardarHorario.postDelayed({ dialog.dismiss() }, 1500)
                    },
                    onFailure = {
                        btnGuardarHorario.isEnabled = true
                        mostrarFeedback("Error: ${it}", "error")
                    }
                )
            } else {
                val horarioAnterior = horarioEditar ?: buscarHorarioPorDia(dia)
                
                guardarHorarioEnFirebaseConAuditoria(
                    dia = dia,
                    apertura = horaAperturaSeleccionada,
                    cierre = horaCierreSeleccionada,
                    cerrado = switchCerrado.isChecked,
                    v24 = switchVeinticuatroHoras.isChecked,
                    horarioAnterior = horarioAnterior,
                    dialog = dialog,
                    onSuccess = {
                        mostrarFeedback("Horario guardado correctamente", "success")
                        btnGuardarHorario.postDelayed({ dialog.dismiss() }, 1500)
                    },
                    onFailure = {
                        btnGuardarHorario.isEnabled = true
                        mostrarFeedback("No se pudo guardar: ${it}", "error")
                    }
                )
            }
        }

        dialog.show()
    }

    private fun guardarHorariosLaborales(apertura: String, cierre: String, cerrado: Boolean, v24: Boolean, dialog: BottomSheetDialog) {
        val laborales = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")
        val reference = FirebaseDatabase.getInstance().getReference("ConfiguracionTienda").child("Horarios")

        val horariosPreparados = HorarioPayloadRules.prepararHorariosParaActualizacion(
            laborales.map { dia ->
                construirHorarioConContextoCierre(
                    dia = dia,
                    apertura = apertura,
                    cierre = cierre,
                    cerrado = cerrado,
                    v24 = v24,
                    horarioAnterior = buscarHorarioPorDia(dia)
                )
            }
        )

        horariosPreparados.horarios.forEach { preparado ->
            reference.child(preparado.id).setValue(preparado.horario)
        }

        Toast.makeText(this, "Horarios laborales actualizados", Toast.LENGTH_SHORT).show()
        dialog.dismiss()
    }

    private fun guardarHorarioEnFirebase(dia: String, apertura: String, cierre: String, cerrado: Boolean, v24: Boolean, dialog: BottomSheetDialog) {
        val horarioPreparado = HorarioPayloadRules.prepararHorarioParaGuardado(
            construirHorarioConContextoCierre(
                dia = dia,
                apertura = apertura,
                cierre = cierre,
                cerrado = cerrado,
                v24 = v24,
                horarioAnterior = buscarHorarioPorDia(dia)
            )
        )

        val reference = FirebaseDatabase.getInstance()
            .getReference("ConfiguracionTienda")
            .child("Horarios")
            .child(horarioPreparado.id)

        reference.setValue(horarioPreparado.horario).addOnSuccessListener {
            Toast.makeText(this, "Guardado", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }

    private fun eliminarHorarioDeFirebase(id: String, dialog: BottomSheetDialog) {
        FirebaseDatabase.getInstance().getReference("ConfiguracionTienda")
            .child("Horarios").child(id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Horario eliminado", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
    }

    private fun guardarHorariosLaboralesConAuditoria(
        apertura: String,
        cierre: String,
        cerrado: Boolean,
        v24: Boolean,
        dialog: BottomSheetDialog,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((String) -> Unit)? = null
    ) {
        val laborales = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")
        val reference = FirebaseDatabase.getInstance().getReference("ConfiguracionTienda").child("Horarios")
        val cambios = mutableListOf<Pair<HorarioTienda?, HorarioTienda>>()
        val horarios = mutableListOf<HorarioTienda>()

        laborales.forEach { dia ->
            val horarioAnterior = buscarHorarioPorDia(dia)
            val horario = construirHorarioConContextoCierre(
                dia = dia,
                apertura = apertura,
                cierre = cierre,
                cerrado = cerrado,
                v24 = v24,
                horarioAnterior = horarioAnterior
            )
            cambios.add(horarioAnterior to horario.copy())
            horarios.add(horario)
        }

        val actualizacionPreparada = HorarioPayloadRules.prepararHorariosParaActualizacion(horarios)

        reference.updateChildren(actualizacionPreparada.updates)
            .addOnSuccessListener {
                cambios.forEach { (anterior, actual) ->
                    registrarMovimientoHorario(
                        anterior = anterior,
                        actual = actual,
                        motivoManual = "Aplicacion masiva de horario en dias laborales"
                    )
                }
                onSuccess?.invoke() ?: run {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("¡Éxito!")
                        .setMessage("Los horarios laborales (Lunes a Viernes) se han actualizado correctamente.")
                        .setPositiveButton("Aceptar", null)
                        .show()
                    dialog.dismiss()
                }
            }
            .addOnFailureListener { e ->
                onFailure?.invoke(e.message ?: "Error desconocido") ?: run {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Error al guardar")
                        .setMessage("No se pudieron guardar los horarios laborales: ${e.message}")
                        .setPositiveButton("Reintentar") { _, _ -> guardarHorariosLaboralesConAuditoria(apertura, cierre, cerrado, v24, dialog) }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            }
    }

    private fun guardarHorarioEnFirebaseConAuditoria(
        dia: String,
        apertura: String,
        cierre: String,
        cerrado: Boolean,
        v24: Boolean,
        horarioAnterior: HorarioTienda?,
        dialog: BottomSheetDialog,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((String) -> Unit)? = null
    ) {
        val horarioPreparado = HorarioPayloadRules.prepararHorarioParaGuardado(
            construirHorarioConContextoCierre(
                dia = dia,
                apertura = apertura,
                cierre = cierre,
                cerrado = cerrado,
                v24 = v24,
                horarioAnterior = horarioAnterior
            )
        )

        val reference = FirebaseDatabase.getInstance()
            .getReference("ConfiguracionTienda")
            .child("Horarios")
            .child(horarioPreparado.id)

        reference.setValue(horarioPreparado.horario)
            .addOnSuccessListener {
                registrarMovimientoHorario(anterior = horarioAnterior, actual = horarioPreparado.horario)
                onSuccess?.invoke() ?: run {
                    Toast.makeText(this, "¡Horario de ${dia} guardado con éxito!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
            .addOnFailureListener { e ->
                onFailure?.invoke(e.message ?: "Error desconocido") ?: run {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Fallo de conexión")
                        .setMessage("No pudimos guardar los cambios: ${e.message}")
                        .setPositiveButton("Aceptar", null)
                        .show()
                }
            }
    }

    private fun eliminarHorarioConAuditoria(
        horario: HorarioTienda, 
        dialog: BottomSheetDialog,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((String) -> Unit)? = null
    ) {
        FirebaseDatabase.getInstance().getReference("ConfiguracionTienda")
            .child("Horarios")
            .child(horario.id)
            .removeValue()
            .addOnSuccessListener {
                val cambiosAntesDespues = construirCambiosEliminacionHorario(horario)
                MovimientoLogger.registrarConSesion(
                    context = this,
                    tipo = "horario_eliminado",
                    modulo = "configuracion_tienda",
                    titulo = "Horario eliminado: ${horario.dia}",
                    descripcion = "Se eliminó el horario de ${horario.dia}.",
                    referenciaId = horario.id,
                    extra = mapOf(
                        "seccion" to "Horarios",
                        "dia" to horario.dia,
                        "motivo" to "Eliminacion manual de horario",
                        "cambiosAntesDespues" to cambiosAntesDespues
                    )
                )
                onSuccess?.invoke() ?: run {
                    Toast.makeText(this, "Horario eliminado", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
            .addOnFailureListener { e ->
                onFailure?.invoke(e.message ?: "Error desconocido") ?: run {
                    Toast.makeText(this, "No se pudo eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun registrarMovimientoHorario(
        anterior: HorarioTienda?,
        actual: HorarioTienda,
        motivoManual: String? = null
    ) {
        val esEdicion = anterior != null
        val cambiosAntesDespues = construirCambiosAntesDespuesHorario(anterior, actual)
        if (esEdicion && cambiosAntesDespues.isEmpty()) return

        val motivo = motivoManual ?: when {
            actual.cerrado && (anterior == null || !anterior.cerrado) ->
                "Se marco el dia como cerrado"
            actual.veinticuatroHoras && (anterior == null || !anterior.veinticuatroHoras) ->
                "Se activo modalidad 24 horas"
            esEdicion ->
                "Edicion manual de horario"
            else ->
                "Creacion manual de horario"
        }

        val descripcion = when {
            actual.cerrado -> "Horario de ${actual.dia} configurado como Cerrado."
            actual.veinticuatroHoras -> "Horario de ${actual.dia} configurado como 24 horas."
            else -> "Horario de ${actual.dia}: ${actual.horaApertura} a ${actual.horaCierre}."
        }

        MovimientoLogger.registrarConSesion(
            context = this,
            tipo = if (esEdicion) "horario_actualizado" else "horario_creado",
            modulo = "configuracion_tienda",
            titulo = if (esEdicion) "Horario actualizado: ${actual.dia}" else "Horario creado: ${actual.dia}",
            descripcion = descripcion,
            referenciaId = actual.id,
            extra = mapOf(
                "seccion" to "Horarios",
                "dia" to actual.dia,
                "cerrado" to actual.cerrado,
                "veinticuatroHoras" to actual.veinticuatroHoras,
                "horaApertura" to actual.horaApertura,
                "horaCierre" to actual.horaCierre,
                "motivo" to motivo,
                "cambiosAntesDespues" to cambiosAntesDespues
            )
        )
    }

    private fun buscarHorarioPorDia(dia: String): HorarioTienda? {
        val diaNormalizado = normalizarDia(dia)
        return listaHorarios
            .firstOrNull { normalizarDia(it.dia) == diaNormalizado }
            ?.copy()
    }

    private fun normalizarDia(dia: String): String {
        return HorarioRules.normalizarDia(dia)
    }

    private fun construirCambiosAntesDespuesHorario(
        anterior: HorarioTienda?,
        actual: HorarioTienda
    ): Map<String, Map<String, Any>> {
        return HorarioAuditRules.construirCambiosAntesDespuesHorario(anterior, actual)
    }

    private fun construirCambiosEliminacionHorario(
        anterior: HorarioTienda
    ): Map<String, Map<String, Any>> {
        return HorarioAuditRules.construirCambiosEliminacionHorario(anterior)
    }

    private fun construirHorarioConContextoCierre(
        dia: String,
        apertura: String,
        cierre: String,
        cerrado: Boolean,
        v24: Boolean,
        horarioAnterior: HorarioTienda?
    ): HorarioTienda {
        val horario = HorarioRules.construirHorarioBase(
            dia = dia,
            apertura = apertura,
            cierre = cierre,
            cerrado = cerrado,
            veinticuatroHoras = v24
        )
        if (!cerrado) {
            horario.cerradoPorId = ""
            horario.cerradoPorNombre = ""
            horario.cerradoEn = 0L
            return horario
        }

        if (
            horarioAnterior?.cerrado == true &&
            horarioAnterior.cerradoPorNombre.isNotBlank() &&
            horarioAnterior.cerradoEn > 0L
        ) {
            horario.cerradoPorId = horarioAnterior.cerradoPorId
            horario.cerradoPorNombre = horarioAnterior.cerradoPorNombre
            horario.cerradoEn = horarioAnterior.cerradoEn
            return horario
        }

        val usuario = obtenerUsuarioActualCierre()
        horario.cerradoPorId = usuario.id
        horario.cerradoPorNombre = usuario.nombre
        horario.cerradoEn = System.currentTimeMillis()
        return horario
    }

    private fun obtenerUsuarioActualCierre(): UsuarioCierreInfo {
        SessionManager.cargarSesion(this)
        val prefs = getSharedPreferences("usuario", MODE_PRIVATE)
        val id = SessionManager.idCajera.trim().ifBlank {
            prefs.getString("idCajera", "").orEmpty().trim()
        }
        val nombre = SessionManager.nombreCajera.trim().ifBlank {
            prefs.getString("usuario", "").orEmpty().trim()
        }.ifBlank { "Usuario no identificado" }
        return UsuarioCierreInfo(id = id, nombre = nombre)
    }

    private data class UsuarioCierreInfo(
        val id: String,
        val nombre: String
    )

    private fun mostrarDialogoPlantillas() {
        val plantillas = HorarioTemplateRules.obtenerPlantillas()
        val nombres = plantillas.map { "${it.nombre}\n${it.descripcion}" }.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle("Plantillas Rápidas")
            .setItems(nombres) { _, which ->
                confirmarYAplicarPlantilla(plantillas[which])
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    private fun confirmarYAplicarPlantilla(plantilla: HorarioTemplate) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Aplicar ${plantilla.nombre}")
            .setMessage("¿Estás seguro? Esto sobrescribirá los horarios de los días incluidos en la plantilla ('${plantilla.descripcion}').")
            .setPositiveButton("Aplicar") { _, _ ->
                aplicarPlantilla(plantilla)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun aplicarPlantilla(plantilla: HorarioTemplate) {
        val reference = FirebaseDatabase.getInstance().getReference("ConfiguracionTienda").child("Horarios")
        val cambios = mutableListOf<Pair<HorarioTienda?, HorarioTienda>>()
        val horariosFinales = mutableListOf<HorarioTienda>()

        plantilla.horarios.forEach { horPlanti ->
            val horarioAnterior = buscarHorarioPorDia(horPlanti.dia)
            val horFinal = construirHorarioConContextoCierre(
                dia = horPlanti.dia,
                apertura = horPlanti.horaApertura,
                cierre = horPlanti.horaCierre,
                cerrado = horPlanti.cerrado,
                v24 = horPlanti.veinticuatroHoras,
                horarioAnterior = horarioAnterior
            )
            cambios.add(horarioAnterior to horFinal.copy())
            horariosFinales.add(horFinal)
        }

        val actualizacion = HorarioPayloadRules.prepararHorariosParaActualizacion(horariosFinales)

        reference.updateChildren(actualizacion.updates)
            .addOnSuccessListener {
                cambios.forEach { (ant, act) ->
                    registrarMovimientoHorario(ant, act, "Uso de plantilla: ${plantilla.nombre}")
                }
                MaterialAlertDialogBuilder(this)
                    .setTitle("¡Éxito!")
                    .setMessage("Se ha aplicado la plantilla '${plantilla.nombre}' correctamente.")
                    .setPositiveButton("Aceptar", null)
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al aplicar plantilla: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarTimePicker(onHoraSeleccionada: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            onHoraSeleccionada(formatearHora(hour, minute))
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
    }

    private fun formatearHora(hour: Int, minute: Int): String {
        return HorarioRules.formatearHora(hour, minute)
    }


}
