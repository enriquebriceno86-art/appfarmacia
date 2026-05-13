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
import com.app.administradorfarmadon.ActivitysPerfilItem.Reciclers.AdapterHorarios
import com.app.administradorfarmadon.ClasesDatabase.MovimientoLogger
import com.app.administradorfarmadon.ClasesDatabase.SessionManager
import com.app.administradorfarmadon.R
import com.app.administradorfarmadon.databinding.ActivityHorariosBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class HorariosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHorariosBinding
    private lateinit var adapterHorarios: AdapterHorarios
    private val listaHorarios = mutableListOf<HorarioTienda>()

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
    }

    private fun configurarRecycler() {
        binding.recyclerHorarios.layoutManager = LinearLayoutManager(this)
        adapterHorarios = AdapterHorarios { horario ->
            mostrarBottomSheetCrearHorario(horario)
        }
        binding.recyclerHorarios.adapter = adapterHorarios
    }

    private fun clics() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnCrearHorario.setOnClickListener {
            mostrarBottomSheetCrearHorario(null)
        }

        binding.fabAgregarHorario.setOnClickListener {
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
                adapterHorarios.updateList(listaHorarios.toList())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HorariosActivity, "Error al cargar horarios", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun actualizarVista() {
        val hayHorarios = listaHorarios.isNotEmpty()
        binding.layoutSinHorarios.visibility = if (hayHorarios) View.GONE else View.VISIBLE
        binding.recyclerHorarios.visibility = if (hayHorarios) View.VISIBLE else View.GONE
        binding.fabAgregarHorario.visibility = if (hayHorarios) View.VISIBLE else View.GONE
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
            val estadoUi = HorarioRules.construirEstadoBotones(
                horaAperturaSeleccionada = horaAperturaSeleccionada,
                horaCierreSeleccionada = horaCierreSeleccionada,
                cerrado = switchCerrado.isChecked,
                veinticuatroHoras = switchVeinticuatroHoras.isChecked,
                dia = autoDia.text?.toString().orEmpty(),
                esEdicion = horarioEditar != null
            )
            btnHoraApertura.isEnabled = estadoUi.horaAperturaHabilitada
            btnHoraCierre.isEnabled = estadoUi.horaCierreHabilitada
            btnHoraApertura.text = estadoUi.textoHoraApertura
            btnHoraCierre.text = estadoUi.textoHoraCierre
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
                btnHoraApertura.text = hora
            }
        }

        btnHoraCierre.setOnClickListener {
            mostrarTimePicker { hora ->
                horaCierreSeleccionada = hora
                btnHoraCierre.text = hora
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
                        eliminarHorarioConAuditoria(it, dialog)
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
                    Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                }
                return@setOnClickListener
            }

            if (checkAplicarLaborales.isChecked) {
                guardarHorariosLaboralesConAuditoria(
                    horaAperturaSeleccionada,
                    horaCierreSeleccionada,
                    switchCerrado.isChecked,
                    switchVeinticuatroHoras.isChecked,
                    dialog
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
                    dialog = dialog
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
        dialog: BottomSheetDialog
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
                Toast.makeText(this, "Horarios laborales actualizados", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "No se pudo guardar horarios laborales: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun guardarHorarioEnFirebaseConAuditoria(
        dia: String,
        apertura: String,
        cierre: String,
        cerrado: Boolean,
        v24: Boolean,
        horarioAnterior: HorarioTienda?,
        dialog: BottomSheetDialog
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
                Toast.makeText(this, "Guardado", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "No se pudo guardar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarHorarioConAuditoria(horario: HorarioTienda, dialog: BottomSheetDialog) {
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
                Toast.makeText(this, "Horario eliminado", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "No se pudo eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
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
