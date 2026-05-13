package com.app.administradorfarmadon.ActivitysInicio

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.R
import com.app.administradorfarmadon.ActivitysInicio.reciclermovimientos.AdapterMovimientosAnalisis
import com.app.administradorfarmadon.ActivitysInicio.reciclermovimientos.CambioAuditoriaItem
import com.app.administradorfarmadon.ActivitysInicio.reciclermovimientos.MovimientoAnalisisItem
import com.app.administradorfarmadon.ClasesDatabase.DbPaths
import com.app.administradorfarmadon.ClasesDatabase.FechaHoraServidorHelper
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.databinding.ActivityAnalisisMovimientosBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AnalisisMovimientos : AppCompatActivity() {

    private enum class RangoChip { HOY, AYER, SEMANA }

    private lateinit var binding: ActivityAnalisisMovimientosBinding
    private lateinit var movimientosAdapter: AdapterMovimientosAnalisis

    private val database by lazy { FirebaseDatabase.getInstance() }
    private var movimientosRef: DatabaseReference? = null
    private var movimientosListener: ValueEventListener? = null

    private var movimientosSemanaActual: List<MovimientoAnalisisItem> = emptyList()

    private var posicionHoy = -1
    private var posicionAyer = -1
    private var posicionSemana = -1
    private var resumenSeccionesActual = AnalisisMovimientosSecciones(
        posicionHoy = -1,
        posicionAyer = -1,
        posicionSemana = -1
    )

    private var actualizandoChipProgramaticamente = false
    private var scrollIniciadoPorUsuario = false
    private var ultimoRangoValidoSeleccionado = RangoChip.HOY
    private var timestampOficialReferenciaMs: Long =
        FechaHoraServidorHelper.estimarMomentoActualDesdeCache().timestampServidorMs

    private val scrollChipSyncListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            when (newState) {
                RecyclerView.SCROLL_STATE_DRAGGING -> scrollIniciadoPorUsuario = true
                RecyclerView.SCROLL_STATE_IDLE -> scrollIniciadoPorUsuario = false
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (!scrollIniciadoPorUsuario) return
            sincronizarChipSegunScroll()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAnalisisMovimientosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        configurarToolbar()
        configurarRecycler()
        configurarChips()
        inicializarRelojOficialMovimientos()
    }

    override fun onDestroy() {
        limpiarListenerMovimientos()
        binding.rvMovimientos.removeOnScrollListener(scrollChipSyncListener)
        super.onDestroy()
    }

    private fun configurarToolbar() {
        binding.toolbarMovimientos.setNavigationOnClickListener { finish() }
    }

    private fun configurarRecycler() {
        movimientosAdapter = AdapterMovimientosAnalisis { movimiento ->
            mostrarDetalleMovimiento(movimiento)
        }
        binding.rvMovimientos.layoutManager = LinearLayoutManager(this)
        binding.rvMovimientos.adapter = movimientosAdapter
        binding.rvMovimientos.setHasFixedSize(true)
        binding.rvMovimientos.addOnScrollListener(scrollChipSyncListener)
    }

    private fun configurarChips() {
        binding.chipGroupFecha.setOnCheckedStateChangeListener { _, checkedIds ->
            if (actualizandoChipProgramaticamente || checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val rangoSolicitado = when (checkedIds.first()) {
                R.id.chipAyer -> RangoChip.AYER
                R.id.chipSemana -> RangoChip.SEMANA
                else -> RangoChip.HOY
            }

            if (!tieneDatosParaRango(rangoSolicitado)) {
                Toast.makeText(
                    this,
                    "No hay datos para mostrar",
                    Toast.LENGTH_SHORT
                ).show()
                seleccionarChipProgramaticamente(ultimoRangoValidoSeleccionado)
                actualizarTituloRango(ultimoRangoValidoSeleccionado)
                return@setOnCheckedStateChangeListener
            }

            moverASeccion(rangoSolicitado, suave = true)
            ultimoRangoValidoSeleccionado = rangoSolicitado
        }
    }

    private fun inicializarRelojOficialMovimientos() {
        FechaHoraServidorHelper.obtenerMomentoActual(
            onSuccess = { momento ->
                timestampOficialReferenciaMs = momento.timestampServidorMs
                cargarMovimientosUltimosSieteDias()
            },
            onError = {
                timestampOficialReferenciaMs =
                    FechaHoraServidorHelper.estimarMomentoActualDesdeCache().timestampServidorMs
                cargarMovimientosUltimosSieteDias()
            }
        )
    }

    private fun cargarMovimientosUltimosSieteDias() {
        limpiarListenerMovimientos()

        movimientosRef = database
            .getReference(DbPaths.ROOT_MOVIMIENTOS)
            .child("movimientos")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fechasPermitidas = obtenerFechasFirebaseUltimosSieteDias()
                val movimientos = snapshot.children.flatMap { fechaSnapshot ->
                    val fechaKey = fechaSnapshot.key.orEmpty()
                    if (!fechasPermitidas.contains(fechaKey)) return@flatMap emptyList()

                    fechaSnapshot.children.mapNotNull { movimientoSnapshot ->
                        construirMovimientoSeguro(
                            fechaKey = fechaKey,
                            movimientoSnapshot = movimientoSnapshot
                        )
                    }
                }

                aplicarMovimientosCargados(movimientos)
            }

            override fun onCancelled(error: DatabaseError) {
                movimientosSemanaActual = emptyList()
                movimientosAdapter.actualizarLista(emptyList())
                renderEstadoVacio(vacio = true)
                Toast.makeText(
                    this@AnalisisMovimientos,
                    "No se pudieron cargar los movimientos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        movimientosListener = listener
        movimientosRef?.addValueEventListener(listener)
    }

    private fun construirMovimientoSeguro(
        fechaKey: String,
        movimientoSnapshot: DataSnapshot
    ): MovimientoAnalisisItem? {
        val movimientoParseado = MovimientoRemoteRules.construirMovimientoRemoto(
            MovimientoRemoteData(
                idMovimiento = movimientoSnapshot.key.orEmpty(),
                titulo = movimientoSnapshot.child("titulo")
                    .getValue(String::class.java)
                    .orEmpty(),
                descripcion = movimientoSnapshot.child("descripcion")
                    .getValue(String::class.java)
                    .orEmpty(),
                detalleLotes = movimientoSnapshot.obtenerDetalleLotesTexto(),
                tipo = movimientoSnapshot.child("tipo")
                    .getValue(String::class.java)
                    .orEmpty(),
                tituloMetodoMetadata = movimientoSnapshot
                    .child("metadata")
                    .child("tituloMetodo")
                    .getValue(String::class.java)
                    .orEmpty(),
                modulo = movimientoSnapshot.child("modulo")
                    .getValue(String::class.java)
                    .orEmpty(),
                autor = movimientoSnapshot.child("nombreUsuario")
                    .getValue(String::class.java)
                    .orEmpty(),
                hora = movimientoSnapshot.child("hora")
                    .getValue(String::class.java)
                    .orEmpty(),
                fechaNodo = fechaKey,
                fechaMovimiento = movimientoSnapshot.child("fecha")
                    .getValue(String::class.java)
                    .orEmpty(),
                timestamp = movimientoSnapshot.child("timestamp")
                    .getValue(Long::class.java) ?: 0L,
                timestampServidor = movimientoSnapshot.child("timestampServidor")
                    .getValue(Long::class.java) ?: 0L,
                monto = movimientoSnapshot.child("monto").obtenerDoubleFlexible(),
                cambiosAntesDespues = movimientoSnapshot.obtenerCambiosAntesDespues()
            )
        ) ?: return null

        return MovimientoAnalisisItem(
            idMovimiento = movimientoParseado.idMovimiento,
            titulo = construirTituloVisible(
                tipo = movimientoParseado.tipo,
                titulo = movimientoParseado.titulo,
                descripcion = movimientoParseado.descripcion,
                tituloMetodoMetadata = movimientoParseado.tituloMetodoMetadata
            ),
            descripcion = movimientoParseado.descripcion,
            detalleLotes = movimientoParseado.detalleLotes,
            tipo = movimientoParseado.tipo,
            modulo = movimientoParseado.modulo,
            monto = movimientoParseado.monto,
            autor = movimientoParseado.autor,
            hora = movimientoParseado.hora,
            fecha = movimientoParseado.fecha,
            timestamp = movimientoParseado.timestamp,
            cambiosAntesDespues = movimientoParseado.cambiosAntesDespues
        )
    }

    private fun aplicarMovimientosCargados(movimientos: List<MovimientoAnalisisItem>) {
        val ordenados = movimientos.sortedByDescending { it.timestamp }
        val resumen = MovimientoSummaryRules.construirResumenMovimientos(
            lista = ordenados,
            rangoActual = obtenerRangoSeleccionado().name
        )
        movimientosSemanaActual = ordenados
        movimientosAdapter.actualizarLista(ordenados)

        recalcularPosicionesSecciones(resumen.secciones)
        aplicarEstadoInicialChipsYScroll(resumen.rangoInicialSugerido)
        renderEstadoVacio(resumen.listaVacia)
    }

    private fun aplicarEstadoInicialChipsYScroll(rangoInicialSugerido: String? = null) {
        if (movimientosSemanaActual.isEmpty()) {
            actualizarTituloRango(RangoChip.HOY)
            return
        }

        val rangoConDatos = rangoInicialSugerido
            ?.let(::rangoChipDesdeClave)
            ?: RangoChip.HOY

        ultimoRangoValidoSeleccionado = rangoConDatos
        seleccionarChipProgramaticamente(rangoConDatos)
        moverASeccion(rangoConDatos, suave = false)
    }

    private fun recalcularPosicionesSecciones(secciones: AnalisisMovimientosSecciones) {
        resumenSeccionesActual = secciones
        posicionHoy = secciones.posicionHoy
        posicionAyer = secciones.posicionAyer
        posicionSemana = secciones.posicionSemana
    }

    private fun moverASeccion(rangoSolicitado: RangoChip, suave: Boolean) {
        val rangoDestino = rangoSolicitado

        val posicion = obtenerPosicionPorRango(rangoDestino)
        if (posicion < 0) {
            val mensaje = when (rangoSolicitado) {
                RangoChip.HOY -> "No hay movimientos para hoy"
                RangoChip.AYER -> "No hay movimientos para ayer"
                RangoChip.SEMANA -> "No hay movimientos en la ultima semana"
            }
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            // Mantenemos el chip seleccionado por el usuario (aunque no tenga registros)
            // para evitar la sensacion de que "no agarra" al tocarlo.
            actualizarTituloRango(rangoSolicitado)
            return
        }

        actualizarTituloRango(rangoDestino)

        val layoutManager = binding.rvMovimientos.layoutManager as? LinearLayoutManager ?: return
        if (suave) {
            binding.rvMovimientos.smoothScrollToPosition(posicion)
        } else {
            layoutManager.scrollToPositionWithOffset(posicion, 0)
        }
    }

    private fun sincronizarChipSegunScroll() {
        val rangoVisible = categoriaVisibleActual() ?: return
        if (rangoVisible != obtenerRangoSeleccionado()) {
            ultimoRangoValidoSeleccionado = rangoVisible
            seleccionarChipProgramaticamente(rangoVisible)
            actualizarTituloRango(rangoVisible)
        }
    }

    private fun categoriaVisibleActual(): RangoChip? {
        if (movimientosSemanaActual.isEmpty()) return null
        val layoutManager = binding.rvMovimientos.layoutManager as? LinearLayoutManager ?: return null
        val primeraPosicion = layoutManager.findFirstVisibleItemPosition()
        if (primeraPosicion == RecyclerView.NO_POSITION) return null
        if (primeraPosicion !in movimientosSemanaActual.indices) return null
        return categoriaParaFecha(movimientosSemanaActual[primeraPosicion].fecha)
    }

    private fun seleccionarChipProgramaticamente(rango: RangoChip) {
        actualizandoChipProgramaticamente = true
        when (rango) {
            RangoChip.HOY -> binding.chipHoy.isChecked = true
            RangoChip.AYER -> binding.chipAyer.isChecked = true
            RangoChip.SEMANA -> binding.chipSemana.isChecked = true
        }
        actualizandoChipProgramaticamente = false
    }

    private fun actualizarTituloRango(rango: RangoChip) {
        val hoy = calendarOficial()
        val ayer = calendarOficial(diasDelta = -1)
        val haceUnaSemana = calendarOficial(diasDelta = -7)

        val textoHoy = formatearFechaVisible(hoy.time)
        val textoAyer = formatearFechaVisible(ayer.time)
        val textoInicioSemana = formatearFechaVisible(haceUnaSemana.time)

        binding.tvFechaRango.text = when (rango) {
            RangoChip.HOY -> "Hoy ($textoHoy)"
            RangoChip.AYER -> "Ayer ($textoAyer)"
            RangoChip.SEMANA -> "Del $textoInicioSemana al $textoHoy"
        }
    }

    private fun obtenerRangoSeleccionado(): RangoChip {
        return when {
            binding.chipAyer.isChecked -> RangoChip.AYER
            binding.chipSemana.isChecked -> RangoChip.SEMANA
            else -> RangoChip.HOY
        }
    }

    private fun primerRangoDisponible(): RangoChip? {
        return MovimientoSummaryRules.primerRangoDisponible(resumenSeccionesActual)
            ?.let(::rangoChipDesdeClave)
    }

    private fun obtenerPosicionPorRango(rango: RangoChip): Int {
        return MovimientoSummaryRules.obtenerPosicionPorRango(
            rango = rango.name,
            secciones = resumenSeccionesActual
        )
    }

    private fun tieneDatosParaRango(rango: RangoChip): Boolean {
        return MovimientoSummaryRules.tieneDatosParaRango(
            rango = rango.name,
            secciones = resumenSeccionesActual
        )
    }

    private fun categoriaParaFecha(fechaFirebase: String): RangoChip {
        val hoy = obtenerFechaFirebase(calendarOficial())
        val ayerFirebase = obtenerFechaFirebase(calendarOficial(diasDelta = -1))

        return rangoChipDesdeClave(
            MovimientoSummaryRules.resolverCategoriaFecha(
                fechaFirebase = fechaFirebase,
                hoy = hoy,
                ayer = ayerFirebase
            )
        )
    }

    private fun rangoChipDesdeClave(clave: String): RangoChip {
        return when (clave) {
            RangoChip.HOY.name -> RangoChip.HOY
            RangoChip.AYER.name -> RangoChip.AYER
            else -> RangoChip.SEMANA
        }
    }

    private fun obtenerFechasFirebaseUltimosSieteDias(): Set<String> {
        val fechas = linkedSetOf<String>()
        val calendar = calendarOficial()
        repeat(7) {
            fechas.add(obtenerFechaFirebase(calendar))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return fechas
    }

    private fun obtenerFechaFirebase(calendar: Calendar): String {
        return FechaHoraServidorHelper.formatearFechaFirebase(calendar.timeInMillis)
    }

    private fun renderEstadoVacio(vacio: Boolean) {
        val estado = MovimientoPresentationRules.construirEstadoVacio(vacio)
        binding.rvMovimientos.visibility = if (estado.mostrarLista) View.VISIBLE else View.GONE
        binding.tvSinMovimientos.visibility =
            if (estado.mostrarEstadoVacio) View.VISIBLE else View.GONE
        if (estado.mostrarEstadoVacio) {
            binding.tvSinMovimientos.text = estado.mensajeEstadoVacio
        }
    }

    private fun limpiarListenerMovimientos() {
        movimientosListener?.let { listener ->
            movimientosRef?.removeEventListener(listener)
        }
        movimientosListener = null
        movimientosRef = null
    }

    private fun formatearFechaVisible(date: Date): String {
        return FechaHoraServidorHelper.formatearFechaVisible(date.time)
    }

    private fun mostrarDetalleMovimiento(item: MovimientoAnalisisItem) {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottomsheet_detalle_movimiento, null)
        dialog.setContentView(view)

        val tvTitulo = view.findViewById<TextView>(R.id.tvTituloDetalleMovimiento)
        val tvSubtitulo = view.findViewById<TextView>(R.id.tvSubtituloDetalleMovimiento)
        val tvDescripcion = view.findViewById<TextView>(R.id.tvDescripcionDetalleMovimiento)
        val tvInfo = view.findViewById<TextView>(R.id.tvInfoDetalleMovimiento)
        val tvSinCambios = view.findViewById<TextView>(R.id.tvSinCambiosDetalle)
        val layoutCambios = view.findViewById<LinearLayout>(R.id.layoutCambiosDetalleMovimiento)
        val btnCopiar = view.findViewById<MaterialButton>(R.id.btnCopiarDetalleMovimiento)
        val btnCerrar = view.findViewById<MaterialButton>(R.id.btnCerrarDetalleMovimiento)

        tvTitulo.text = item.titulo.ifBlank { "Movimiento registrado" }
        val moduloLegible = when (item.modulo.lowercase(Locale.getDefault())) {
            "configuracion_tienda" -> "Config. tienda"
            "inventario" -> "Inventario"
            "caja" -> "Caja"
            else -> "General"
        }
        val tipoLegible = when {
            item.tipo.lowercase(Locale.getDefault()).startsWith("metodo_pago") -> "Metodo de pago"
            item.tipo.lowercase(Locale.getDefault()).contains("venta") -> "Venta"
            item.tipo.lowercase(Locale.getDefault()).contains("egreso") -> "Egreso"
            item.tipo.lowercase(Locale.getDefault()).contains("turno") -> "Turno"
            item.tipo.lowercase(Locale.getDefault()).contains("inventario") -> "Inventario"
            else -> item.tipo
                .replace("_", " ")
                .replaceFirstChar { it.uppercase() }
        }
        tvSubtitulo.text = "${item.hora} | $moduloLegible | $tipoLegible"
        tvDescripcion.text = item.descripcion.ifBlank { "Sin descripción registrada." }

        val fechaTexto = obtenerFechaDetalle(item)

        val montoTexto = item.monto?.let { MonedaHelper.formatear(it) } ?: "No aplica"
        tvInfo.text = buildList {
            add("Fecha: $fechaTexto")
            add("Usuario: ${item.autor.ifBlank { "sin autor" }}")
            add("Monto: $montoTexto")
            if (item.detalleLotes.isNotBlank()) {
                add("Lotes: ${item.detalleLotes}")
            }
        }.joinToString("\n")

        layoutCambios.removeAllViews()
        if (item.cambiosAntesDespues.isEmpty()) {
            tvSinCambios.visibility = View.VISIBLE
        } else {
            tvSinCambios.visibility = View.GONE
            item.cambiosAntesDespues
                .toSortedMap()
                .forEach { (campo, cambio) ->
                    val itemView = LayoutInflater.from(this)
                        .inflate(R.layout.item_cambio_auditoria, layoutCambios, false)

                    val tvCampo = itemView.findViewById<TextView>(R.id.tvCampoCambioAuditoria)
                    val tvAntes = itemView.findViewById<TextView>(R.id.tvAntesCambioAuditoria)
                    val tvDespues = itemView.findViewById<TextView>(R.id.tvDespuesCambioAuditoria)

                    tvCampo.text = campo
                        .replace(Regex("([a-z])([A-Z])"), "$1 $2")
                        .replace("_", " ")
                        .replaceFirstChar { it.uppercase() }
                    tvAntes.text = valorAuditoriaTexto(cambio.antes)
                    tvDespues.text = valorAuditoriaTexto(cambio.despues)

                    layoutCambios.addView(itemView)
                }
        }

        btnCopiar.setOnClickListener {
            copiarAuditoriaAlPortapapeles(item)
        }
        btnCerrar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun copiarAuditoriaAlPortapapeles(item: MovimientoAnalisisItem) {
        val texto = construirTextoAuditoriaParaCopiar(item)
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager
        if (clipboard == null) {
            Toast.makeText(this, "No se pudo acceder al portapapeles", Toast.LENGTH_SHORT).show()
            return
        }

        clipboard.setPrimaryClip(
            ClipData.newPlainText("auditoria_movimiento_${item.idMovimiento}", texto)
        )
        Toast.makeText(this, "Auditoria copiada", Toast.LENGTH_SHORT).show()
    }

    private fun construirTextoAuditoriaParaCopiar(item: MovimientoAnalisisItem): String {
        val fechaTexto = obtenerFechaDetalle(item)
        val montoTexto = item.monto?.let { MonedaHelper.formatear(it) } ?: "No aplica"

        val builder = StringBuilder()
        builder.appendLine(item.titulo.ifBlank { "Movimiento registrado" })
        builder.appendLine("Fecha: $fechaTexto")
        builder.appendLine("Usuario: ${item.autor.ifBlank { "sin autor" }}")
        builder.appendLine("Modulo: ${item.modulo.ifBlank { "general" }}")
        builder.appendLine("Tipo: ${item.tipo.ifBlank { "general" }}")
        builder.appendLine("Monto: $montoTexto")
        if (item.detalleLotes.isNotBlank()) {
            builder.appendLine("Lotes: ${item.detalleLotes}")
        }
        builder.appendLine()

        if (item.cambiosAntesDespues.isEmpty()) {
            builder.appendLine("Cambios: sin detalle")
            return builder.toString().trim()
        }

        builder.appendLine("Cambios (Antes/Despues):")
        item.cambiosAntesDespues
            .toSortedMap()
            .forEach { (campo, cambio) ->
                val campoLegible = campo
                    .replace(Regex("([a-z])([A-Z])"), "$1 $2")
                    .replace("_", " ")
                    .replaceFirstChar { it.uppercase() }
                builder.appendLine("- $campoLegible")
                builder.appendLine("  Antes: ${valorAuditoriaTexto(cambio.antes)}")
                builder.appendLine("  Despues: ${valorAuditoriaTexto(cambio.despues)}")
            }
        return builder.toString().trim()
    }

    private fun obtenerFechaDetalle(item: MovimientoAnalisisItem): String {
        return if (item.timestamp > 0L) {
            FechaHoraServidorHelper.formatearFechaHoraVisible(item.timestamp)
        } else {
            item.fecha
        }
    }

    private fun calendarOficial(diasDelta: Int = 0): Calendar {
        return FechaHoraServidorHelper.calendarDesdeTimestamp(timestampOficialReferenciaMs).apply {
            if (diasDelta != 0) {
                add(Calendar.DAY_OF_YEAR, diasDelta)
            }
        }
    }

    private fun valorAuditoriaTexto(valor: Any?): String {
        return when (valor) {
            null -> "(vacio)"
            is String -> valor.ifBlank { "(vacio)" }
            is Number, is Boolean -> valor.toString()
            is Map<*, *> -> {
                if (valor.isEmpty()) return "(vacio)"
                valor.entries.joinToString("\n") { (k, v) ->
                    val key = k?.toString().orEmpty().ifBlank { "(campo)" }
                    "$key: ${valorAuditoriaTexto(v)}"
                }
            }
            is List<*> -> {
                if (valor.isEmpty()) return "(vacio)"
                valor.joinToString("\n") { elemento -> "• ${valorAuditoriaTexto(elemento)}" }
            }
            else -> valor.toString()
        }
    }

    private fun DataSnapshot.obtenerDoubleFlexible(): Double? {
        return MovimientoRemoteRules.parsearMonto(value)
    }

    private fun DataSnapshot.obtenerCambiosAntesDespues(): Map<String, CambioAuditoriaItem> {
        val cambiosSnapshot = child("metadata").child("cambiosAntesDespues")
        if (!cambiosSnapshot.exists()) return emptyMap()

        val cambios = cambiosSnapshot.children.map { campoSnapshot ->
            CambioAuditoriaRemoteData(
                campo = campoSnapshot.key.orEmpty(),
                antes = campoSnapshot.child("antes").value,
                despues = campoSnapshot.child("despues").value
            )
        }
        return MovimientoRemoteRules.construirCambiosAntesDespues(cambios)
    }

    private fun DataSnapshot.obtenerDetalleLotesTexto(): String {
        val metadata = child("metadata")
        val detallePlano = metadata
            .child("detalleLotesTexto")
            .getValue(String::class.java)
            .orEmpty()
            .trim()
        if (detallePlano.isNotBlank()) return detallePlano

        val detalles = metadata.child("lotesConsumidosDetalle").children.mapNotNull { detalle ->
            val numero = detalle.child("numero").getValue(String::class.java).orEmpty().trim()
                .ifBlank { detalle.child("clave").getValue(String::class.java).orEmpty().trim() }
            val cantidad = detalle.child("cantidad").value?.toString()?.toIntOrNull()
            if (numero.isBlank() || cantidad == null || cantidad <= 0) {
                null
            } else {
                "$numero: $cantidad"
            }
        }
        if (detalles.isNotEmpty()) return detalles.joinToString(", ")

        val loteNumero = metadata.child("loteNumeroConsumido")
            .getValue(String::class.java)
            .orEmpty()
            .trim()
            .ifBlank {
                metadata.child("loteNumero")
                    .getValue(String::class.java)
                    .orEmpty()
                    .trim()
            }
        if (loteNumero.isBlank()) return ""

        val cantidad = child("cantidad").value?.toString()?.toIntOrNull()
        return if (cantidad != null && cantidad > 0) {
            "$loteNumero: $cantidad"
        } else {
            loteNumero
        }
    }

    private fun construirTituloVisible(
        tipo: String,
        titulo: String,
        descripcion: String,
        tituloMetodoMetadata: String
    ): String {
        return MovimientoPresentationRules.construirTituloVisible(
            MovimientoTituloData(
                tipo = tipo,
                titulo = titulo,
                descripcion = descripcion,
                tituloMetodoMetadata = tituloMetodoMetadata
            )
        )
    }
}
