package com.app.administradorfarmadon.ActivityFragmentos.Fragmentos

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.core.graphics.toColorInt
import android.os.Handler
import android.os.Looper
import com.app.administradorfarmadon.ActivityFragmentos.showElegantemente
import com.app.administradorfarmadon.ActivityFragmentos.hideElegantemente
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ActivityInventario.CategoriaProductos
import com.app.administradorfarmadon.ActivityInventario.ProductUtils
import com.app.administradorfarmadon.ActivityInventario.HistorialMovimientosInventarioActivity
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.AdapterInventarioAlertasHorizontal
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.AdapterProductosInventariosRecycler
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.toMoldeProductos
import com.app.administradorfarmadon.ActivityInventario.domain.SmartSearchEngine
import com.app.administradorfarmadon.ActivitysInicio.AlertasActivity
import com.app.administradorfarmadon.databinding.DialogAlertasVencimientoBinding
import com.app.administradorfarmadon.databinding.ItemAlertaLoteBinding
import com.app.administradorfarmadon.ActivityInventario.CrearProducto
import com.app.administradorfarmadon.R
import com.app.administradorfarmadon.ClasesDatabase.DbPaths
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.databinding.FragmentFragmentoInventarioBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.android.material.snackbar.Snackbar
import android.app.DatePickerDialog
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.core.util.Pair as AndroidxPair

class FragmentoInventario : Fragment() {
    private var productosListener: ValueEventListener? = null
    private var productosRef: DatabaseReference? = null
    private var categoriasRef: DatabaseReference? = null
    private var categoriasListener: ValueEventListener? = null
    private var movimientosRef: DatabaseReference? = null
    private var movimientosListener: ValueEventListener? = null
    private var datosActivos = false
    private var cargaInicialInventarioCompletada = false
    private var loadingInicialInventarioActivo = false
    private var productosInicialesListos = false
    private var movimientosInicialesListos = false
    private val handlerSeguridadInventario = Handler(Looper.getMainLooper())
    private var runnableTimeoutLoadingInventario: Runnable? = null

    private var _binding: FragmentFragmentoInventarioBinding? = null
    private val binding get() = _binding!!

    private val listaProductos = mutableListOf<MoldeProductos>()
    private val listaCategorias = mutableListOf<String>()
    private val smartSearchEngine = SmartSearchEngine()

    private lateinit var adapter: AdapterProductosInventariosRecycler
    private var adapterAlertas: AdapterInventarioAlertasHorizontal? = null
    private lateinit var movimientosAdapter: AdapterMovimientosInventario
    private var dialogMovimientos: BottomSheetDialog? = null
    private var dialogDetalleMovimiento: BottomSheetDialog? = null
    private var dialogMovimientosAdapter: AdapterMovimientosInventario? = null
    private var dialogRecyclerMovimientos: RecyclerView? = null
    private var dialogTvMovimientosVacio: TextView? = null
    private var dialogTvSubtituloMovimientos: TextView? = null
    private var dialogTvResumenMovimientos: TextView? = null
    private var movimientosRecientes: List<MovimientoInventarioUi> = emptyList()
    private var todosLosMovimientos: List<MovimientoInventarioUi> = emptyList()
    private var filtroActual = "TODOS"
    private var categoriaSeleccionada = "Todas las categorías"

    private enum class FiltroFecha { HOY, PERSONALIZADO, RANGO }
    private var filtroFechaMovimientos = FiltroFecha.HOY
    private var fechaPersonalizada: Calendar? = null
    // Rango personalizado (feature: filtro por rango de fechas)
    private var fechaRangoInicio: Calendar? = null
    private var fechaRangoFin: Calendar? = null

    // Referencias a chips del panel tablet (persistentes en la vista)
    private var tabletChipHoy: Chip? = null
    private var tabletChipFecha: Chip? = null
    private var tabletChipRango: Chip? = null

    // Búsqueda y filtro tipo en movimientos (features 1 & 2)
    private var textoBusquedaMovimientos = ""
    private var filtroTipoMovimiento = "TODOS"
    private var tabletEditBuscarMovimientos: android.widget.EditText? = null

    // Filtro por producto específico (feature 3)
    private var filtroProductoMovimientos: String? = null  // indice del producto, null = todos

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFragmentoInventarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFiltroEstado()
        setupListeners()
        initRecycler()
        initRecyclerMovimientos()
        configurarAccionesMovimientos()
        configurarFiltroFechaTablet()
    }

    override fun onResume() {
        super.onResume()
        activarDatosSiVisible()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            desactivarDatos()
        } else {
            activarDatosSiVisible()
        }
    }

    private fun setupListeners() {
        binding.editBuscarProducto.addTextChangedListener {
            aplicarFiltro(it.toString())
        }

        binding.btnFiltroCategoria.setOnClickListener {
            mostrarDialogoCategorias()
        }

        binding.btnInfoColores?.setOnClickListener {
            mostrarResumenInventario()
        }

        binding.editBuscarProducto.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                ocultarTeclado()
                binding.editBuscarProducto.clearFocus()
                true
            } else {
                false
            }
        }

        binding.btnLimpiarBusqueda.setOnClickListener {
            resetTodoYMostrarTodos()
        }

        binding.btnAgregarPrimerProducto?.setOnClickListener {
            startActivity(Intent(requireContext(), CrearProducto::class.java))
        }

        binding.floatingbotoncrearproducto.setOnClickListener {
            startActivity(Intent(requireContext(), CrearProducto::class.java))
        }

        binding.root.findViewById<View>(R.id.btnMovimientosInventario)?.setOnClickListener {
            startActivity(Intent(requireContext(), HistorialMovimientosInventarioActivity::class.java))
        }

        binding.cardAlertaLotesVencer.setOnClickListener {
            abrirAlertas(AlertasActivity.ALERTA_POR_VENCER)
        }

        binding.root.findViewById<View>(R.id.cardAlertaStockBajoTablet)?.setOnClickListener {
            abrirAlertas(AlertasActivity.ALERTA_BAJO_STOCK)
        }

        configurarStatusChips()
    }

    // ── Chips de estado (Todos / Suficiente / Stock bajo / Agotado-vence) ───
    private fun configurarStatusChips() {
        val cardTodos = binding.root.findViewById<View>(R.id.statusCardTodos)
        val cardSuficiente = binding.root.findViewById<View>(R.id.statusCardSuficiente)
        val cardStockBajo = binding.root.findViewById<View>(R.id.statusCardStockBajo)
        val cardAgotado = binding.root.findViewById<View>(R.id.statusCardAgotado)

        cardTodos?.visibility = View.GONE
        cardSuficiente?.setOnClickListener { toggleFiltroEstado("PROXIMOS_VENCER") }
        cardStockBajo?.setOnClickListener { toggleFiltroEstado("BAJO_STOCK") }
        cardAgotado?.setOnClickListener { toggleFiltroEstado("AGOTADO_O_VENCE") }

        binding.root.findViewById<View>(R.id.chipCategoriaActiva)?.setOnClickListener {
            limpiarCategoriaActiva()
        }

        actualizarStatusChipsUi()
        actualizarChipCategoriaActiva()
    }

    private fun limpiarCategoriaActiva() {
        categoriaSeleccionada = "Todas las categorías"
        binding.btnFiltroCategoria.strokeColor =
            ColorStateList.valueOf("#E5E7EB".toColorInt())
        binding.btnFiltroCategoria.backgroundTintList =
            ColorStateList.valueOf(Color.TRANSPARENT)
        binding.btnFiltroCategoria.iconTint =
            ColorStateList.valueOf("#1A1C1E".toColorInt())
        actualizarChipCategoriaActiva()
        aplicarFiltro(binding.editBuscarProducto.text.toString())
    }

    private fun actualizarChipCategoriaActiva() {
        val b = _binding ?: return
        val chip = b.root.findViewById<View>(R.id.chipCategoriaActiva) ?: return
        val tv = b.root.findViewById<TextView>(R.id.tvCategoriaActiva)
        if (categoriaSeleccionada.isNotBlank() && categoriaSeleccionada != "Todas las categorías") {
            tv?.text = categoriaSeleccionada
            chip.visibility = View.VISIBLE
        } else {
            chip.visibility = View.GONE
        }
    }

    private fun toggleFiltroEstado(nuevoFiltro: String) {
        filtroActual = if (filtroActual == nuevoFiltro) "TODOS" else nuevoFiltro
        actualizarStatusChipsUi()
        aplicarFiltro(binding.editBuscarProducto.text.toString())
    }

    private fun actualizarStatusChipsUi() {
        if (_binding == null) return
        val activo = filtroActual
        aplicarEstiloChipEstado(
            cardId = R.id.statusCardSuficiente,
            closeId = R.id.ivCloseSuficiente,
            seleccionado = activo == "PROXIMOS_VENCER",
            colorActivoBg = "#FEF3C7",
            colorActivoStroke = "#B45309",
            mostrarCierre = true
        )
        aplicarEstiloChipEstado(
            cardId = R.id.statusCardStockBajo,
            closeId = R.id.ivCloseStockBajo,
            seleccionado = activo == "BAJO_STOCK",
            colorActivoBg = "#FEF3C7",
            colorActivoStroke = "#B54708",
            mostrarCierre = true
        )
        aplicarEstiloChipEstado(
            cardId = R.id.statusCardAgotado,
            closeId = R.id.ivCloseAgotado,
            seleccionado = activo == "AGOTADO_O_VENCE",
            colorActivoBg = "#FEE4E2",
            colorActivoStroke = "#B42318",
            mostrarCierre = true
        )
    }

    private fun aplicarEstiloChipEstado(
        cardId: Int,
        closeId: Int,
        seleccionado: Boolean,
        colorActivoBg: String,
        colorActivoStroke: String,
        mostrarCierre: Boolean
    ) {
        val b = _binding ?: return
        val card = b.root.findViewById<MaterialCardView>(cardId) ?: return
        val close = b.root.findViewById<View>(closeId)
        if (seleccionado) {
            card.setCardBackgroundColor(colorActivoBg.toColorInt())
            card.strokeColor = colorActivoStroke.toColorInt()
            card.strokeWidth = (1.5f * resources.displayMetrics.density).toInt()
            close?.visibility = if (mostrarCierre) View.VISIBLE else View.GONE
        } else {
            card.setCardBackgroundColor(0xFFFFFFFF.toInt())
            card.strokeColor = "#E4E7EC".toColorInt()
            card.strokeWidth = (1f * resources.displayMetrics.density).toInt()
            close?.visibility = View.GONE
        }
    }

    private fun abrirAlertas(tipo: String) {
        startActivity(
            Intent(requireContext(), AlertasActivity::class.java).apply {
                putExtra(AlertasActivity.EXTRA_ALERTA_INICIAL, tipo)
            }
        )
    }

    private fun cargarCategorias() {
        categoriasRef = FirebaseDatabase.getInstance()
            .getReference("Inventario")
            .child("CategoriasInventario")
        categoriasListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaCategorias.clear()
                for (child in snapshot.children) {
                    child.getValue(CategoriaProductos::class.java)?.nombre?.let {
                        listaCategorias.add(it)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) = Unit
        }
        categoriasRef?.addValueEventListener(categoriasListener!!)
    }

    @SuppressLint("SetTextI18n")
    private fun mostrarDialogoCategorias() {
        if (listaCategorias.isEmpty()) return

        if (requireContext().isTablet()) {
            mostrarPopupCategoriasTablet()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar categoría")
            .setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    listaCategorias
                )
            ) { _, which ->
                val elegida = listaCategorias[which]
                // Toggle: si vuelves a tocar la categoría que ya estaba activa, la quitas.
                categoriaSeleccionada = if (categoriaSeleccionada == elegida) {
                    "Todas las categorías"
                } else {
                    elegida
                }

                val esTabletAhora = requireContext().isTablet()
                if (categoriaSeleccionada == "Todas las categorías") {
                    if (!esTabletAhora) binding.btnFiltroCategoria.text = "Categorías"
                    binding.btnFiltroCategoria.strokeColor =
                        ColorStateList.valueOf("#E5E7EB".toColorInt())
                    binding.btnFiltroCategoria.backgroundTintList =
                        ColorStateList.valueOf(Color.TRANSPARENT)
                    binding.btnFiltroCategoria.setTextColor("#1A1C1E".toColorInt())
                    binding.btnFiltroCategoria.iconTint =
                        ColorStateList.valueOf("#1A1C1E".toColorInt())
                } else {
                    if (!esTabletAhora) binding.btnFiltroCategoria.text = categoriaSeleccionada
                    if (esTabletAhora) {
                        // En tablet el chip "categoría activa" ya es el indicador; mantenemos el botón neutro.
                        binding.btnFiltroCategoria.strokeColor =
                            ColorStateList.valueOf("#E5E7EB".toColorInt())
                        binding.btnFiltroCategoria.backgroundTintList =
                            ColorStateList.valueOf(Color.TRANSPARENT)
                        binding.btnFiltroCategoria.iconTint =
                            ColorStateList.valueOf("#1A1C1E".toColorInt())
                    } else {
                        binding.btnFiltroCategoria.strokeColor =
                            ColorStateList.valueOf("#0E8F63".toColorInt())
                        binding.btnFiltroCategoria.backgroundTintList =
                            ColorStateList.valueOf("#E8F5E9".toColorInt())
                        binding.btnFiltroCategoria.setTextColor("#0E8F63".toColorInt())
                        binding.btnFiltroCategoria.iconTint =
                            ColorStateList.valueOf("#0E8F63".toColorInt())
                    }
                }

                if (esTabletAhora) actualizarChipCategoriaActiva()
                aplicarFiltro(binding.editBuscarProducto.text.toString())
            }
            .show()
    }

    private fun mostrarPopupCategoriasTablet() {
        val anchor: View = binding.btnFiltroCategoria
        val popup = PopupMenu(requireContext(), anchor)
        listaCategorias.forEachIndexed { index, nombre ->
            val item = popup.menu.add(0, index, index, nombre)
            if (nombre == categoriaSeleccionada) {
                item.title = "✓  $nombre"
            }
        }
        popup.setOnMenuItemClickListener { item ->
            val elegida = listaCategorias[item.itemId]
            categoriaSeleccionada = if (categoriaSeleccionada == elegida) {
                "Todas las categorías"
            } else {
                elegida
            }
            actualizarChipCategoriaActiva()
            aplicarFiltro(binding.editBuscarProducto.text.toString())
            true
        }
        popup.show()
    }

    @SuppressLint("SetTextI18n")
    private fun resetTodoYMostrarTodos() {
        binding.editBuscarProducto.setText("")
        filtroActual = "TODOS"
        categoriaSeleccionada = "Todas las categorías"
        if (requireContext().isTablet()) {
            actualizarStatusChipsUi()
            actualizarChipCategoriaActiva()
        }

        if (!requireContext().isTablet()) binding.btnFiltroCategoria.text = "Categorías"
        binding.btnFiltroCategoria.strokeColor =
            ColorStateList.valueOf("#E5E7EB".toColorInt())
        binding.btnFiltroCategoria.backgroundTintList =
            ColorStateList.valueOf(Color.TRANSPARENT)
        binding.btnFiltroCategoria.setTextColor("#1A1C1E".toColorInt())
        binding.btnFiltroCategoria.iconTint =
            ColorStateList.valueOf("#1A1C1E".toColorInt())

        actualizarBtnFiltro()
        aplicarFiltro("")
    }

    private fun initRecycler() {
        val esTablet = requireContext().isTablet()
        adapter = AdapterProductosInventariosRecycler(
            onVerHistorialClick = if (esTablet) {
                { producto -> mostrarHistorialProducto(producto) }
            } else {
                null   // En móvil no se muestra el botón Historial para no apretar la tarjeta
            }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FragmentoInventario.adapter
        }

        // Carrusel horizontal de productos en alerta — solo en móvil
        if (!esTablet) {
            initRecyclerAlertas()
        }
    }

    private fun initRecyclerAlertas() {
        val recyclerAlertas = binding.root.findViewById<RecyclerView>(R.id.recyclerAlertasInventario) ?: return
        val nuevoAdapter = AdapterInventarioAlertasHorizontal()
        adapterAlertas = nuevoAdapter
        recyclerAlertas.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        recyclerAlertas.adapter = nuevoAdapter
        recyclerAlertas.setHasFixedSize(false)
    }

    /**
     * Construye la lista de productos en alerta (vencidos, por vencer, sin stock o stock bajo)
     * a partir de la lista FILTRADA visible. Si no hay alertas, oculta el carrusel.
     */
    private fun actualizarCarruselAlertas(productosVisibles: List<MoldeProductos>) {
        if (requireContext().isTablet()) return
        val recyclerAlertas = binding.root.findViewById<RecyclerView>(R.id.recyclerAlertasInventario)
            ?: return
        val adapterAlertas = adapterAlertas ?: return

        val alertas = productosVisibles.filter { producto ->
            if (!producto.estadodelproducto) return@filter false
            val cantidadActual = producto.cantidadinicial.toIntOrNull() ?: 0
            val stockMinimo = producto.stockminimo.toIntOrNull() ?: 0
            val estadoVencimiento = ProductUtils.obtenerEstadoVencimiento(producto).orEmpty()
            val stockBajo = stockMinimo > 0 && cantidadActual <= stockMinimo
            val vencidoOPorVencer = estadoVencimiento == "VENCIDO" || estadoVencimiento == "POR_VENCER"
            stockBajo || vencidoOPorVencer || cantidadActual <= 0
        }.sortedWith(
            // Prioridad: vencidos > por vencer > sin stock > stock bajo
            compareBy(
                { producto ->
                    when (ProductUtils.obtenerEstadoVencimiento(producto).orEmpty()) {
                        "VENCIDO" -> 0
                        "POR_VENCER" -> 1
                        else -> 2
                    }
                },
                { producto ->
                    if ((producto.cantidadinicial.toIntOrNull() ?: 0) <= 0) 0 else 1
                }
            )
        )

        if (alertas.isEmpty()) {
            recyclerAlertas.visibility = View.GONE
        } else {
            recyclerAlertas.visibility = View.VISIBLE
            adapterAlertas.updateList(alertas)
        }
    }

    /**
     * El resumen global vive en el panel de info. La lista principal queda limpia.
     */
    private fun actualizarHeaderProductosInventario(productosVisibles: List<MoldeProductos>) {
        binding.root.findViewById<View>(R.id.headerProductosInventario)?.isVisible = false
    }

    private fun initRecyclerMovimientos() {
        movimientosAdapter = crearAdapterMovimientos()

        binding.recyclerViewMovimientos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = movimientosAdapter
            setHasFixedSize(false)
        }
    }

    private fun crearAdapterMovimientos(): AdapterMovimientosInventario {
        return AdapterMovimientosInventario { movimiento ->
            dialogMovimientos?.dismiss()
            mostrarDetalleMovimientoInventario(movimiento)
        }
    }

    private fun configurarAccionesMovimientos() {
        if (requireContext().isTablet()) return
        binding.root.findViewById<View>(R.id.bottomSheetMovimientos)?.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun configurarFiltroFechaTablet() {
        if (!isAdded || !requireContext().isTablet()) return
        tabletChipHoy = binding.root.findViewById(R.id.chipHoyTablet)
        tabletChipFecha = binding.root.findViewById(R.id.chipFechaTablet)
        tabletChipRango = binding.root.findViewById(R.id.chipRangoTablet)
        tabletEditBuscarMovimientos = binding.root.findViewById(R.id.editBuscarMovimientosTablet)

        tabletChipHoy?.setOnClickListener {
            filtroFechaMovimientos = FiltroFecha.HOY
            aplicarFiltroFecha()
        }
        tabletChipFecha?.setOnClickListener {
            mostrarDatePickerMovimientos { fecha ->
                fechaPersonalizada = fecha
                filtroFechaMovimientos = FiltroFecha.PERSONALIZADO
                tabletChipFecha?.text = "📅 " + SimpleDateFormat("d MMM", Locale.Builder().setLanguage("es").build()).format(fecha.time)
                sincronizarChipsFiltro(tabletChipHoy, tabletChipFecha, tabletChipRango)
                aplicarFiltroFecha()
            }
        }
        // Chip Rango (feature: filtro por rango de fechas)
        tabletChipRango?.setOnClickListener {
            mostrarRangePickerMovimientos { inicio, fin ->
                fechaRangoInicio = inicio
                fechaRangoFin = fin
                filtroFechaMovimientos = FiltroFecha.RANGO
                tabletChipRango?.text = "📆 " + etiquetaCortaRango(inicio, fin)
                sincronizarChipsFiltro(tabletChipHoy, tabletChipFecha, tabletChipRango)
                aplicarFiltroFecha()
            }
        }

        // Búsqueda en movimientos (feature 1)
        tabletEditBuscarMovimientos?.addTextChangedListener {
            textoBusquedaMovimientos = it?.toString().orEmpty()
            aplicarFiltroFecha()
        }

        // Filtro por tipo (feature 2)
        val chipTodos = binding.root.findViewById<Chip>(R.id.chipTodosTipoTablet)
        val chipVentas = binding.root.findViewById<Chip>(R.id.chipVentasTipoTablet)
        val chipIngresos = binding.root.findViewById<Chip>(R.id.chipIngresosTipoTablet)
        val chipAjustes = binding.root.findViewById<Chip>(R.id.chipAjustesTipoTablet)
        val chipDevoluciones = binding.root.findViewById<Chip>(R.id.chipDevolucionesTipoTablet)
        val chipCreaciones = binding.root.findViewById<Chip>(R.id.chipCreacionesTipoTablet)
        chipTodos?.setOnClickListener { filtroTipoMovimiento = "TODOS"; aplicarFiltroFecha() }
        chipVentas?.setOnClickListener { filtroTipoMovimiento = "VENTAS"; aplicarFiltroFecha() }
        chipIngresos?.setOnClickListener { filtroTipoMovimiento = "INGRESOS"; aplicarFiltroFecha() }
        chipAjustes?.setOnClickListener { filtroTipoMovimiento = "AJUSTES"; aplicarFiltroFecha() }
        chipDevoluciones?.setOnClickListener { filtroTipoMovimiento = "DEVOLUCIONES"; aplicarFiltroFecha() }
        chipCreaciones?.setOnClickListener { filtroTipoMovimiento = "CREACIONES"; aplicarFiltroFecha() }
    }

    private fun aplicarFiltroFecha() {
        if (!isAdded || _binding == null) return
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hoy = fmt.format(Date())

        // 1) Filtro por fecha
        var filtrados = when (filtroFechaMovimientos) {
            FiltroFecha.HOY ->
                todosLosMovimientos.filter { it.fechaPath == hoy }
            FiltroFecha.PERSONALIZADO -> {
                val fecha = fechaPersonalizada
                if (fecha == null) todosLosMovimientos
                else {
                    val fechaStr = fmt.format(fecha.time)
                    todosLosMovimientos.filter { it.fechaPath == fechaStr }
                }
            }
            FiltroFecha.RANGO -> {
                val inicio = fechaRangoInicio
                val fin = fechaRangoFin
                if (inicio == null || fin == null) todosLosMovimientos
                else {
                    val desde = fmt.format(inicio.time)
                    val hasta = fmt.format(fin.time)
                    // Asegura que desde <= hasta sin importar el orden de selección
                    val (d, h) = if (desde <= hasta) desde to hasta else hasta to desde
                    todosLosMovimientos.filter { it.fechaPath.isNotEmpty() && it.fechaPath in d..h }
                }
            }
        }

        // 2) Filtro por tipo (feature 2)
        if (filtroTipoMovimiento != "TODOS") {
            filtrados = filtrados.filter { coincideTipo(it.tipo, filtroTipoMovimiento) }
        }

        // 3) Filtro por producto específico (feature 3)
        val indiceProductoFiltro = filtroProductoMovimientos
        if (indiceProductoFiltro != null) {
            filtrados = filtrados.filter { it.indiceProducto == indiceProductoFiltro }
        }

        // 4) Búsqueda por texto (feature 1)
        val texto = textoBusquedaMovimientos.trim().lowercase()
        if (texto.isNotEmpty()) {
            filtrados = filtrados.filter { mov ->
                mov.titulo.lowercase().contains(texto) ||
                    mov.descripcion.lowercase().contains(texto) ||
                    mov.nombreProducto.lowercase().contains(texto) ||
                    mov.indiceProducto.lowercase().contains(texto)
            }
        }

        actualizarUiMovimientos(filtrados)
    }

    private fun coincideTipo(tipo: String, filtro: String): Boolean {
        val t = tipo.lowercase()
        return when (filtro) {
            // Devolución debe tener prioridad sobre "venta" porque el tipo es "devolucion_venta".
            "DEVOLUCIONES" -> t.contains("devolu")
            "VENTAS"     -> !t.contains("devolu") && (t.contains("venta") || t.contains("salida_venta"))
            "INGRESOS"   -> t.contains("ingreso") || t.contains("entrada")
            "AJUSTES"    -> t.contains("ajuste")
            "CREACIONES" -> t.contains("creac") || t.contains("creado") || t.contains("nuevo")
            else         -> true
        }
    }

    private fun mostrarDatePickerMovimientos(onDateSelected: (Calendar) -> Unit) {
        if (!isAdded) return
        val cal = fechaPersonalizada ?: Calendar.getInstance()
        val picker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                onDateSelected(Calendar.getInstance().apply { set(year, month, day) })
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        // HUMANO: Sin fechas futuras. El usuario solo puede mirar movimientos
        // que ya ocurrieron: desde siempre hasta hoy inclusive.
        picker.datePicker.maxDate = System.currentTimeMillis()
        picker.show()
    }

    /**
     * HUMANO: Abre un MaterialDatePicker en modo rango. El usuario elige un
     * día de inicio y otro de fin, y recibimos ambos en milisegundos UTC.
     * Convertimos a Calendar local normalizando a medianoche para comparar
     * fechaPath con seguridad.
     */
    private fun mostrarRangePickerMovimientos(onRangeSelected: (Calendar, Calendar) -> Unit) {
        if (!isAdded) return

        // HUMANO: El rango es siempre "hoy hacia atrás". El usuario no puede elegir
        // fechas futuras porque no hay movimientos allí. setEnd limita hasta qué mes
        // se puede navegar en el calendario; el validator bloquea los días posteriores
        // a hoy dentro del mes actual.
        val hoyUtc = MaterialDatePicker.todayInUtcMilliseconds()
        val constraints = CalendarConstraints.Builder()
            .setEnd(hoyUtc)
            .setValidator(DateValidatorPointBackward.now())
            .build()

        val builder = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Selecciona un rango de fechas")
            .setCalendarConstraints(constraints)

        val inicioPrevio = fechaRangoInicio?.timeInMillis
        val finPrevio = fechaRangoFin?.timeInMillis
        if (inicioPrevio != null && finPrevio != null &&
            inicioPrevio <= hoyUtc && finPrevio <= hoyUtc
        ) {
            builder.setSelection(AndroidxPair(inicioPrevio, finPrevio))
        }

        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { seleccion ->
            val startMs = seleccion.first ?: return@addOnPositiveButtonClickListener
            val endMs = seleccion.second ?: return@addOnPositiveButtonClickListener
            val inicio = Calendar.getInstance().apply {
                timeInMillis = startMs
                // Normaliza: el picker devuelve UTC 00:00; pasamos al día local
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val fin = Calendar.getInstance().apply {
                timeInMillis = endMs
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            onRangeSelected(inicio, fin)
        }
        picker.show(childFragmentManager, "rangoMovimientos")
    }

    /** Texto corto para pintar dentro del chip, ej: "3–9 Nov" o "28 Oct – 2 Nov". */
    private fun etiquetaCortaRango(inicio: Calendar, fin: Calendar): String {
        val esp = Locale.Builder().setLanguage("es").build()
        val mesIgual = inicio.get(Calendar.MONTH) == fin.get(Calendar.MONTH) &&
            inicio.get(Calendar.YEAR) == fin.get(Calendar.YEAR)
        return if (mesIgual) {
            val mesTxt = SimpleDateFormat("MMM", esp).format(fin.time)
            "${inicio.get(Calendar.DAY_OF_MONTH)}–${fin.get(Calendar.DAY_OF_MONTH)} $mesTxt"
        } else {
            val fmtCorto = SimpleDateFormat("d MMM", esp)
            "${fmtCorto.format(inicio.time)} – ${fmtCorto.format(fin.time)}"
        }
    }

    private fun sincronizarChipsFiltro(
        chipHoy: Chip?,
        chipFecha: Chip?,
        chipRango: Chip?
    ) {
        when (filtroFechaMovimientos) {
            FiltroFecha.HOY -> chipHoy?.isChecked = true
            FiltroFecha.PERSONALIZADO -> chipFecha?.isChecked = true
            FiltroFecha.RANGO -> chipRango?.isChecked = true
        }
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    private fun mostrarDialogoMovimientos() {
        if (!isAdded || requireContext().isTablet()) return
        if (dialogMovimientos?.isShowing == true) return

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_movimientos_inventario, null, false)

        dialogRecyclerMovimientos = dialogView.findViewById(R.id.recyclerViewMovimientosDialog)
        dialogTvMovimientosVacio = dialogView.findViewById(R.id.tvMovimientosVacioDialog)
        dialogTvSubtituloMovimientos = dialogView.findViewById(R.id.tvSubtituloMovimientosDialog)
        dialogTvResumenMovimientos = dialogView.findViewById(R.id.tvResumenMovimientosDialog)

        dialogMovimientosAdapter = crearAdapterMovimientos()
        dialogRecyclerMovimientos?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dialogMovimientosAdapter
            setHasFixedSize(false)
        }

        // Chips de filtro de fecha (mobile dialog)
        val chipHoy = dialogView.findViewById<Chip>(R.id.chipHoyDialog)
        val chipFecha = dialogView.findViewById<Chip>(R.id.chipFechaDialog)
        val chipRango = dialogView.findViewById<Chip>(R.id.chipRangoDialog)
        sincronizarChipsFiltro(chipHoy, chipFecha, chipRango)

        chipHoy?.setOnClickListener {
            filtroFechaMovimientos = FiltroFecha.HOY
            sincronizarChipsFiltro(chipHoy, chipFecha, chipRango)
            aplicarFiltroFecha()
        }
        chipRango?.setOnClickListener {
            mostrarRangePickerMovimientos { inicio, fin ->
                fechaRangoInicio = inicio
                fechaRangoFin = fin
                filtroFechaMovimientos = FiltroFecha.RANGO
                chipRango.text = "📆 " + etiquetaCortaRango(inicio, fin)
                sincronizarChipsFiltro(chipHoy, chipFecha, chipRango)
                aplicarFiltroFecha()
            }
        }
        // Búsqueda en movimientos (feature 1)
        val editBuscar = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editBuscarMovimientosDialog)
        editBuscar?.setText(textoBusquedaMovimientos)
        editBuscar?.addTextChangedListener {
            textoBusquedaMovimientos = it?.toString().orEmpty()
            aplicarFiltroFecha()
        }

        // Alerta stock bajo (feature 4)
        val cardAlerta = dialogView.findViewById<MaterialCardView>(R.id.cardAlertaStockBajoDialog)
        val tvAlerta = dialogView.findViewById<TextView>(R.id.tvAlertaStockBajoDialog)
        val cantidadBajoStock = listaProductos.count { p ->
            val cantidad = p.cantidadinicial.toIntOrNull() ?: 0
            val minimo = p.stockminimo.toIntOrNull() ?: 0
            cantidad <= minimo && p.estadodelproducto
        }
        if (cantidadBajoStock > 0) {
            cardAlerta?.visibility = View.VISIBLE
            tvAlerta?.text = "⚠️ $cantidadBajoStock producto${if (cantidadBajoStock == 1) "" else "s"} con stock bajo o agotado"
        }

        // Título especial si es historial de un producto
        val indiceHistorial = filtroProductoMovimientos
        if (indiceHistorial != null) {
            val nombreProducto = listaProductos.find { it.indice == indiceHistorial }?.nombre ?: indiceHistorial
            dialogView.findViewById<TextView>(R.id.tvSubtituloMovimientosDialog)?.text = "Historial de: $nombreProducto"
        }

        chipFecha?.setOnClickListener {
            mostrarDatePickerMovimientos { fecha ->
                fechaPersonalizada = fecha
                filtroFechaMovimientos = FiltroFecha.PERSONALIZADO
                chipFecha.text = "📅 " + SimpleDateFormat("d MMM", Locale.Builder().setLanguage("es").build()).format(fecha.time)
                sincronizarChipsFiltro(chipHoy, chipFecha, chipRango)
                aplicarFiltroFecha()
            }
        }

        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.setOnShowListener {
            ocultarBotonesFlotantes()
            dialog.behavior.skipCollapsed = true
            dialog.behavior.isFitToContents = true
            dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        dialog.setOnDismissListener {
            mostrarBotonesFlotantes()
            dialogMovimientos = null
            dialogMovimientosAdapter = null
            dialogRecyclerMovimientos = null
            dialogTvMovimientosVacio = null
            dialogTvSubtituloMovimientos = null
            dialogTvResumenMovimientos = null
            // Limpiar filtros específicos al cerrar
            filtroProductoMovimientos = null
            textoBusquedaMovimientos = ""
            filtroTipoMovimiento = "TODOS"
        }

        dialogMovimientos = dialog
        actualizarUiMovimientos(movimientosRecientes)
        dialog.show()
    }

    @SuppressLint("InflateParams")
    private fun mostrarDetalleMovimientoInventario(movimiento: MovimientoInventarioUi) {
        if (!isAdded) return
        dialogDetalleMovimiento?.dismiss()

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_detalle_movimiento_inventario, null, false)

        dialogView.findViewById<TextView>(R.id.tvTituloDetalleMovimiento).text =
            movimiento.titulo.ifBlank { getString(R.string.movimientos_inventario_detalle_titulo_default) }
        dialogView.findViewById<TextView>(R.id.tvSubtituloDetalleMovimiento).text =
            buildString {
                append(movimiento.hora.ifBlank { getString(R.string.movimientos_inventario_valor_sin_hora) })
                if (movimiento.nombreUsuario.isNotBlank()) {
                    append(" | ")
                    append(movimiento.nombreUsuario)
                }
            }

        dialogView.findViewById<TextView>(R.id.tvValorProductoDetalleMovimiento).text =
            resolverNombreProductoMovimiento(movimiento)
        dialogView.findViewById<TextView>(R.id.tvValorPresentacionDetalleMovimiento).text =
            formatearPresentacionDetalleMovimiento(movimiento)
        dialogView.findViewById<TextView>(R.id.tvValorCantidadDetalleMovimiento).text =
            formatearCantidadDetalleMovimiento(movimiento)
        val layoutLotes = dialogView.findViewById<View>(R.id.layoutLotesDetalleMovimiento)
        val tvLotes = dialogView.findViewById<TextView>(R.id.tvValorLotesDetalleMovimiento)
        if (movimiento.detalleLotes.isNotBlank()) {
            layoutLotes.visibility = View.VISIBLE
            tvLotes.text = movimiento.detalleLotes
        } else {
            layoutLotes.visibility = View.GONE
        }
        dialogView.findViewById<TextView>(R.id.tvValorUsuarioDetalleMovimiento).text =
            movimiento.nombreUsuario.ifBlank { getString(R.string.movimientos_inventario_valor_sin_usuario) }
        dialogView.findViewById<TextView>(R.id.tvValorMotivoDetalleMovimiento).text =
            movimiento.motivo.ifBlank { getString(R.string.movimientos_inventario_valor_sin_motivo) }
        val unidadBaseMovimiento = listaProductos
            .find { it.indice == movimiento.indiceProducto }
            ?.unidadbase?.takeIf { it.isNotBlank() } ?: "unidades"
        dialogView.findViewById<TextView>(R.id.tvValorStockAntesDetalleMovimiento).text =
            formatearStockDetalleMovimiento(movimiento.stockAntes, unidadBaseMovimiento)
        dialogView.findViewById<TextView>(R.id.tvValorStockDespuesDetalleMovimiento).text =
            formatearStockDetalleMovimiento(movimiento.stockDespues, unidadBaseMovimiento)
        dialogView.findViewById<TextView>(R.id.tvValorReferenciaDetalleMovimiento).text =
            resolverReferenciaDetalleMovimiento(movimiento)

        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.setOnShowListener {
            ocultarBotonesFlotantes()
            dialog.behavior.skipCollapsed = true
            dialog.behavior.isFitToContents = true
            dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        dialog.setOnDismissListener {
            mostrarBotonesFlotantes()
            if (dialogDetalleMovimiento === dialog) {
                dialogDetalleMovimiento = null
            }
        }

        dialogView.findViewById<MaterialButton>(R.id.btnCerrarDetalleMovimiento)
            .setOnClickListener { dialog.dismiss() }

        dialogDetalleMovimiento = dialog
        dialog.show()
    }

    private fun resolverNombreProductoMovimiento(movimiento: MovimientoInventarioUi): String {
        if (movimiento.nombreProducto.isNotBlank()) return movimiento.nombreProducto

        val descripcion = movimiento.descripcion.trim()
        if (descripcion.isBlank()) {
            return movimiento.indiceProducto.ifBlank {
                getString(R.string.movimientos_inventario_valor_no_identificado)
            }
        }

        val patrones = listOf(
            Regex("(?i)para\\s+(.+?)(?:\\.\\s+lotes|$)"),
            Regex("(?i)de\\s+(.+)$"),
            Regex("(?i)se\\s+cre[oó]\\s+(.+)$"),
            Regex("(?i)se\\s+actualiz[oó]\\s+(.+)$"),
            Regex("(?i)se\\s+elimin[oó]\\s+(.+)$"),
            Regex("(?i)entr[oó]\\s+mercader[ií]a\\s+para\\s+(.+)$"),
            Regex("(?i)se\\s+ajust[oó]\\s+(.+)$")
        )

        return patrones.firstNotNullOfOrNull { patron ->
            patron.find(descripcion)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
        } ?: descripcion
    }

    private fun formatearCantidadConSigno(cantidad: Int, esSalida: Boolean): String {
        return when {
            cantidad < 0 -> cantidad.toString()
            cantidad > 0 -> "${if (esSalida) "-" else "+"}$cantidad"
            else -> "0"
        }
    }

    private fun formatearCantidadDetalleMovimiento(movimiento: MovimientoInventarioUi): String {
        val cantidad = movimiento.cantidad ?: return getString(R.string.movimientos_inventario_valor_no_registrado)
        val esSalida = esMovimientoSalida(movimiento)
        val unidadBase = listaProductos
            .find { it.indice == movimiento.indiceProducto }
            ?.unidadbase?.takeIf { it.isNotBlank() } ?: "unidades"
        val cantidadMovimiento = movimiento.cantidadMovimiento
        val unidadMovimiento = movimiento.unidadMovimiento.trim()
        val cantidadBaseTexto = formatearCantidadConSigno(cantidad, esSalida)
        val cantidadMovimientoTexto = cantidadMovimiento
            ?.takeIf { it != 0 }
            ?.let { formatearCantidadConSigno(it, esSalida) }
        return if (cantidadMovimientoTexto != null && unidadMovimiento.isNotBlank()) {
            buildString {
                append("Movimiento registrado: ")
                append(cantidadMovimientoTexto)
                append(" ")
                append(unidadMovimiento)
                append("\n")
                append("Total del movimiento en unidad base: ")
                append(cantidadBaseTexto)
                append(" ")
                append(unidadBase)
            }
        } else {
            "Movimiento registrado: $cantidadBaseTexto $unidadBase"
        }
    }

    private fun formatearStockDetalleMovimiento(stock: Int?, unidadBase: String = "unidades"): String {
        return stock?.let { "$it $unidadBase" }
            ?: getString(R.string.movimientos_inventario_valor_no_registrado)
    }

    private fun formatearPresentacionDetalleMovimiento(movimiento: MovimientoInventarioUi): String {
        val unidadBase = listaProductos
            .find { it.indice == movimiento.indiceProducto }
            ?.unidadbase?.takeIf { it.isNotBlank() } ?: "unidades"
        val unidadMovimiento = movimiento.unidadMovimiento.trim()
            .ifBlank { movimiento.presentacion.trim() }
        val unidadesPorMovimiento = movimiento.unidadesPorMovimiento?.takeIf { it > 0 }

        return when {
            unidadMovimiento.isBlank() -> "Unidad base principal: $unidadBase"
            unidadesPorMovimiento != null && unidadesPorMovimiento > 1 -> {
                "Presentaci\u00f3n usada: $unidadMovimiento\nEquivalencia: 1 $unidadMovimiento = $unidadesPorMovimiento $unidadBase"
            }
            unidadMovimiento.equals(unidadBase, ignoreCase = true) -> {
                "Unidad base principal: $unidadBase"
            }
            else -> "Presentaci\u00f3n usada: $unidadMovimiento"
        }
    }

    private fun DataSnapshot.obtenerDetalleLotesMovimientoTexto(): String {
        val metadata = child("metadata")
        val detallePlano = metadata.child("detalleLotesTexto")
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

    private fun resolverReferenciaDetalleMovimiento(movimiento: MovimientoInventarioUi): String {
        val referencia = movimiento.referenciaId.trim()
        if (referencia.isBlank()) return getString(R.string.movimientos_inventario_valor_sin_referencia)

        return when {
            esMovimientoDevolucionInventario(movimiento) -> "Venta relacionada"
            esSalidaPorVentaMovimiento(movimiento) -> "Venta relacionada"
            referencia.equals("ajuste_manual", ignoreCase = true) -> getString(R.string.movimientos_inventario_referencia_ajuste_manual)
            referencia.equals("ingreso_stock", ignoreCase = true) -> getString(R.string.movimientos_inventario_referencia_ingreso_stock)
            referencia.equals(movimiento.indiceProducto, ignoreCase = true) -> getString(
                R.string.movimientos_inventario_referencia_indice,
                referencia
            )
            else -> referencia.humanizarReferenciaInterna()
        }
    }

    private fun esSalidaPorVentaMovimiento(movimiento: MovimientoInventarioUi): Boolean {
        return movimiento.tipo.lowercase(Locale.getDefault()).contains("salida_venta")
    }

    private fun esMovimientoDevolucionInventario(movimiento: MovimientoInventarioUi): Boolean {
        return movimiento.tipo.lowercase(Locale.getDefault()).contains("devolucion")
    }

    private fun esMovimientoSalida(movimiento: MovimientoInventarioUi): Boolean {
        val tipo = movimiento.tipo.lowercase(Locale.getDefault())
        return tipo.contains("salida") || tipo.contains("eliminado")
    }

    private fun ocultarBotonesFlotantes() {
        binding.floatingbotoncrearproducto.hide()
        binding.root.findViewById<View>(R.id.btnMovimientosInventario)?.isVisible = false
    }

    private fun mostrarBotonesFlotantes() {
        binding.floatingbotoncrearproducto.show()
        binding.root.findViewById<View>(R.id.btnMovimientosInventario)?.isVisible = true
    }

    private fun iniciarLecturaProductos() {
        productosRef = FirebaseDatabase.getInstance().getReference("Inventario").child("Productos")
        productosListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                listaProductos.clear()
                for (child in snapshot.children) {
                    child.toMoldeProductos()?.let { listaProductos.add(it) }
                }
                aplicarEstadoInventarioCargado()
                marcarProductosInicialesListos()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Inventario", "Error productos: ${error.message}")
                marcarProductosInicialesListos()
            }
        }
        productosRef?.addValueEventListener(productosListener!!)
    }

    private fun aplicarEstadoInventarioCargado() {
        aplicarFiltro(binding.editBuscarProducto.text.toString())
        mostrarAlertaInventario(listaProductos)
        actualizarPrioridadInventario()
    }

    private fun mostrarAlertaInventario(productos: List<MoldeProductos>) {
        if (!isAdded || _binding == null) return

        val activos = productos.filter { it.estadodelproducto }

        val vencidos = activos.filter {
            ProductUtils.obtenerEstadoVencimiento(it).orEmpty() == "VENCIDO"
        }

        val porVencer = activos.filter {
            ProductUtils.obtenerEstadoVencimiento(it).orEmpty() == "POR_VENCER"
        }

        val bajoStock = activos.filter { p ->
            val stock = p.cantidadinicial.trim().toIntOrNull() ?: 0
            val minimo = p.stockminimo.trim().toIntOrNull() ?: 0
            minimo > 0 && stock <= minimo
        }

        val mensaje = when {
            vencidos.size == 1 -> "🔴 Vencido: ${vencidos.first().nombre}"
            vencidos.size > 1 -> "🔴 ${vencidos.size} productos vencidos"

            porVencer.size == 1 -> "🟡 Por vencer: ${porVencer.first().nombre}"
            porVencer.size > 1 -> "🟡 ${porVencer.size} productos por vencer"

            bajoStock.size == 1 -> "🔴 Stock bajo: ${bajoStock.first().nombre}"
            bajoStock.size > 1 -> "🔴 ${bajoStock.size} productos con stock bajo"

            else -> return
        }

        Snackbar.make(binding.root, mensaje, 7000)
            .setAction("Ver") {
                when {
                    vencidos.isNotEmpty() -> filtroActual = "VENCIDOS"
                    porVencer.isNotEmpty() -> filtroActual = "PROXIMOS_VENCER"
                    bajoStock.isNotEmpty() -> filtroActual = "BAJO_STOCK"
                }

                actualizarBtnFiltro()
                aplicarFiltro(binding.editBuscarProducto.text.toString())
            }
            .setBackgroundTint("#1F2937".toColorInt())
            .setTextColor(Color.WHITE)
            .show()
    }

    private fun iniciarLecturaMovimientos() {
        movimientosRef = FirebaseDatabase.getInstance()
            .getReference(DbPaths.ROOT_MOVIMIENTOS)
            .child("movimientosInventario")
        movimientosListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return

                val movimientos = mutableListOf<MovimientoInventarioUi>()

                for (fechaSnapshot in snapshot.children) {
                    val fechaKey = fechaSnapshot.key.orEmpty() // "yyyy-MM-dd"
                    for (movimientoSnapshot in fechaSnapshot.children) {
                        val indiceProducto = movimientoSnapshot.child("indiceProducto")
                            .getValue(String::class.java)
                            .orEmpty()

                        val titulo = movimientoSnapshot.child("titulo")
                            .getValue(String::class.java)
                            .orEmpty()
                        val descripcion = movimientoSnapshot.child("descripcion")
                            .getValue(String::class.java)
                            .orEmpty()

                        if (titulo.isBlank() && descripcion.isBlank()) continue

                        // Parseo robusto: Firebase puede devolver Long, Double o String
                        val tsRaw = movimientoSnapshot.child("timestamp").value
                        val timestamp = when (tsRaw) {
                            is Long -> tsRaw
                            is Double -> tsRaw.toLong()
                            is Int -> tsRaw.toLong()
                            is String -> tsRaw.toLongOrNull() ?: 0L
                            else -> 0L
                        }

                        movimientos.add(
                            MovimientoInventarioUi(
                                indiceProducto = indiceProducto,
                                tipo = movimientoSnapshot.child("tipo")
                                    .getValue(String::class.java)
                                    .orEmpty(),
                                titulo = titulo,
                                descripcion = descripcion,
                                hora = movimientoSnapshot.child("hora")
                                    .getValue(String::class.java)
                                    .orEmpty(),
                                nombreUsuario = movimientoSnapshot.child("nombreUsuario")
                                    .getValue(String::class.java)
                                    .orEmpty(),
                                referenciaId = movimientoSnapshot.child("referenciaId")
                                    .getValue(String::class.java)
                                    .orEmpty(),
                                cantidad = movimientoSnapshot.child("cantidad").value
                                    ?.toString()
                                    ?.toIntOrNull(),
                                stockAntes = movimientoSnapshot.child("stockAntes").value
                                    ?.toString()
                                    ?.toIntOrNull(),
                                stockDespues = movimientoSnapshot.child("stockDespues").value
                                    ?.toString()
                                    ?.toIntOrNull(),
                                timestamp = timestamp,
                                nombreProducto = movimientoSnapshot.child("metadata")
                                    .child("nombreProducto")
                                    .getValue(String::class.java)
                                    .orEmpty(),
                                presentacion = movimientoSnapshot.child("metadata")
                                    .child("presentacion")
                                    .getValue(String::class.java)
                                    .orEmpty(),
                                cantidadMovimiento = movimientoSnapshot.child("metadata")
                                    .child("cantidadIngresada")
                                    .value
                                    ?.toString()
                                    ?.toIntOrNull()
                                    ?: movimientoSnapshot.child("metadata")
                                        .child("cantidadEgresada")
                                        .value
                                        ?.toString()
                                        ?.toIntOrNull(),
                                unidadMovimiento = movimientoSnapshot.child("metadata")
                                    .child("unidadIngreso")
                                    .getValue(String::class.java)
                                    .orEmpty()
                                    .ifBlank {
                                        movimientoSnapshot.child("metadata")
                                            .child("unidadEgreso")
                                            .getValue(String::class.java)
                                            .orEmpty()
                                    }
                                    .ifBlank {
                                        movimientoSnapshot.child("metadata")
                                            .child("unidadAjuste")
                                            .getValue(String::class.java)
                                            .orEmpty()
                                    },
                                unidadesPorMovimiento = movimientoSnapshot.child("metadata")
                                    .child("unidadesPorIngreso")
                                    .value
                                    ?.toString()
                                    ?.toIntOrNull()
                                    ?: movimientoSnapshot.child("metadata")
                                        .child("unidadesPorEgreso")
                                        .value
                                        ?.toString()
                                        ?.toIntOrNull()
                                    ?: movimientoSnapshot.child("metadata")
                                        .child("unidadesPorAjuste")
                                        .value
                                        ?.toString()
                                        ?.toIntOrNull(),
                                motivo = movimientoSnapshot.child("motivo")
                                    .getValue(String::class.java)
                                    .orEmpty(),
                                detalleLotes = movimientoSnapshot.obtenerDetalleLotesMovimientoTexto(),
                                fechaPath = fechaKey
                            )
                        )
                    }
                }

                todosLosMovimientos = movimientos.sortedByDescending { it.timestamp }
                aplicarFiltroFecha()
                marcarMovimientosInicialesListos()
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded || _binding == null) return
                actualizarUiMovimientos(emptyList())
                Log.e("InventarioMov", "Error movimientos: ${error.message}")
                marcarMovimientosInicialesListos()
            }
        }
        movimientosRef?.addValueEventListener(movimientosListener!!)
    }

    private fun actualizarUiMovimientos(recientes: List<MovimientoInventarioUi>) {
        val b = _binding ?: return
        movimientosRecientes = recientes
        val hayMovimientos = recientes.isNotEmpty()
        val etiquetaPeriodo = when (filtroFechaMovimientos) {
            FiltroFecha.HOY -> "hoy"
            FiltroFecha.PERSONALIZADO -> fechaPersonalizada?.let {
                SimpleDateFormat("d 'de' MMMM", Locale.Builder().setLanguage("es").build()).format(it.time)
            } ?: "fecha personalizada"
            FiltroFecha.RANGO -> {
                val inicio = fechaRangoInicio
                val fin = fechaRangoFin
                if (inicio != null && fin != null) {
                    val fmtLargo = SimpleDateFormat("d 'de' MMM", Locale.Builder().setLanguage("es").build())
                    "del ${fmtLargo.format(inicio.time)} al ${fmtLargo.format(fin.time)}"
                } else "rango personalizado"
            }
        }
        val textoSubtitulo = if (hayMovimientos) {
            "${recientes.size} movimiento${if (recientes.size == 1) "" else "s"} · $etiquetaPeriodo"
        } else {
            "Sin movimientos · $etiquetaPeriodo"
        }
        val textoBadge = if (hayMovimientos) "${recientes.size}" else "0"

        if (requireContext().isTablet()) {
            movimientosAdapter.updateList(recientes)
            b.recyclerViewMovimientos.isVisible = hayMovimientos
            b.tvMovimientosVacio.isVisible = !hayMovimientos
            b.root.findViewById<TextView>(R.id.tvSubtituloMovimientos)?.text = textoSubtitulo
            b.root.findViewById<TextView>(R.id.tvResumenMovimientos)?.text = textoBadge
            // Actualizar alerta stock bajo en panel tablet (feature 4)
            actualizarAlertaStockBajoTablet()
            return
        }

        dialogMovimientosAdapter?.updateList(recientes)
        dialogRecyclerMovimientos?.isVisible = hayMovimientos
        dialogTvMovimientosVacio?.isVisible = !hayMovimientos
        dialogTvSubtituloMovimientos?.text = textoSubtitulo
        dialogTvResumenMovimientos?.text = textoBadge
    }

    private fun aplicarFiltro(textoBusqueda: String = "") {
        if (_binding == null) return
        
        // 1. Primero obtenemos los resultados de la búsqueda inteligente si hay texto
        val query = textoBusqueda.trim()
        val resultadosSmart = if (query.isEmpty()) {
            listaProductos.map { SmartSearchEngine.SearchResult(it, SmartSearchEngine.MatchType.NONE) }
        } else {
            smartSearchEngine.search(query, listaProductos)
        }

        // 2. Luego aplicamos los filtros secundarios (Categoría, Estado Chips) sobre esos resultados
        val filtradosFinales = resultadosSmart.filter { res ->
            val producto = res.product
            
            val coincideChip = when (filtroActual) {
                "BAJO_STOCK" -> {
                    (producto.cantidadinicial.toIntOrNull() ?: 0) <=
                        (producto.stockminimo.toIntOrNull() ?: 0)
                }
                "SUFICIENTE" -> {
                    (producto.cantidadinicial.toIntOrNull() ?: 0) >
                        (producto.stockminimo.toIntOrNull() ?: 0)
                }
                "VENCIDOS" -> ProductUtils.obtenerVencimientosParaEvaluacion(producto)
                    .any { ProductUtils.estaVencido(it) }
                "AGOTADO_O_VENCE" -> {
                    val agotado = (producto.cantidadinicial.toIntOrNull() ?: 0) <= 0
                    val vencidoOPorVencer = ProductUtils.obtenerVencimientosParaEvaluacion(producto)
                        .let { lista ->
                            lista.any { ProductUtils.estaVencido(it) } ||
                                lista.mapNotNull { ProductUtils.diasHastaVencerLote(it) }
                                    .any { it in 0..90 }
                        }
                    agotado || vencidoOPorVencer
                }
                "PROXIMOS_VENCER" -> ProductUtils.obtenerVencimientosParaEvaluacion(producto)
                    .mapNotNull { ProductUtils.diasHastaVencerLote(it) }
                    .any { it in 0..90 }
                else -> true
            }

            val coincideCategoria = if (categoriaSeleccionada == "Todas las categorías") {
                true
            } else {
                producto.categoria == categoriaSeleccionada
            }

            coincideChip && coincideCategoria
        }

        val hayFiltrosActivos = query.isNotEmpty() ||
                filtroActual != "TODOS" ||
                categoriaSeleccionada != "Todas las categorías"

        actualizarEstadoVacio(
            filtradosFinales.isEmpty(),
            hayFiltrosActivos
        )
        
        // Enviamos los resultados detallados al adapter para que pueda mostrar los badges de síntomas
        adapter.updateListWithResults(filtradosFinales)
        
        val soloProductos = filtradosFinales.map { it.product }
        actualizarCarruselAlertas(soloProductos)
        actualizarHeaderProductosInventario(soloProductos)
        if (requireContext().isTablet()) actualizarContadoresChipsEstado()

        if (filtradosFinales.isNotEmpty()) {
            binding.recyclerView.scrollToPosition(0)
        }
    }

    private fun actualizarContadoresChipsEstado() {
        val b = _binding ?: return
        val baseCategoria = if (categoriaSeleccionada == "Todas las categorías") {
            listaProductos
        } else {
            listaProductos.filter { it.categoria == categoriaSeleccionada }
        }

        val contarPorEstado: (String) -> Int = { estado ->
            InventarioFilterRules.filtrarProductos(
                productos = baseCategoria,
                criteria = InventarioFilterCriteria(
                    textoBusqueda = "",
                    filtroActual = estado,
                    categoriaSeleccionada = categoriaSeleccionada
                )
            ).filtrados.size
        }

        b.root.findViewById<TextView>(R.id.tvCountSuficiente)?.text =
            contarPorEstado("PROXIMOS_VENCER").toString()
        b.root.findViewById<TextView>(R.id.tvCountStockBajo)?.text =
            contarPorEstado("BAJO_STOCK").toString()
        b.root.findViewById<TextView>(R.id.tvCountAgotado)?.text =
            contarPorEstado("AGOTADO_O_VENCE").toString()
    }



    @SuppressLint("SetTextI18n")
    private fun actualizarAlertaStockBajoTablet() {
        val b = _binding ?: return
        if (!isAdded || !requireContext().isTablet()) return
        val alertaTabletEstado = InventarioSummaryRules.construirStockBajoTablet(listaProductos)
        val cardResumen = b.root.findViewById<MaterialCardView>(R.id.cardAlertaStockBajoTablet)
        val textoResumen = b.root.findViewById<TextView>(R.id.tvAlertaStockBajoTablet)
        if (alertaTabletEstado.mostrar) {
            cardResumen?.visibility = View.VISIBLE
            textoResumen?.text = alertaTabletEstado.mensaje
        } else {
            cardResumen?.visibility = View.GONE
        }
    }

    // ── Alertas de vencimiento de lotes ──────────────────────────────────────

    private data class LoteAlerta(
        val nombreProducto: String,
        val numeroLote: String,
        val vencimiento: String,
        val diasRestantes: Int   // negativo = ya vencido
    )

    /** Días entre hoy y el último día del mes MM/AA. Negativo si ya venció. */
    private fun diasHastaVencerLote(vencimiento: String): Int? {
        val fechaNormalizada = vencimiento.replace("_", "/")
        return ProductUtils.diasHastaVencerLote(fechaNormalizada)
    }

    private fun verificarAlertasLotes() {
        val b = _binding ?: return
        if (!isAdded) return
        val alertasEstado = InventarioSummaryRules.construirAlertasLotes(
            productos = listaProductos,
            resolverDiasHastaVencerLote = ::diasHastaVencerLote
        )
        val alertasDialogo = alertasEstado.alertas.map {
            LoteAlerta(
                nombreProducto = it.nombreProducto,
                numeroLote = it.numeroLote,
                vencimiento = it.vencimiento,
                diasRestantes = it.diasRestantes
            )
        }

        val bannerCardResumen = b.cardAlertaLotesVencer
        val bannerTextoResumen = b.tvTextoAlertaLotes

        if (!alertasEstado.mostrarBanner) {
            bannerCardResumen.visibility = View.GONE
            return
        }

        bannerTextoResumen.text = alertasEstado.textoBanner
        val resumenBgColor = if (alertasEstado.vencidos > 0) 0xFFFEE2E2.toInt() else 0xFFFEF3C7.toInt()
        val resumenStrokeColor = if (alertasEstado.vencidos > 0) 0xFFDC2626.toInt() else 0xFFF59E0B.toInt()
        val resumenTextColor = if (alertasEstado.vencidos > 0) 0xFF7F1D1D.toInt() else 0xFF92400E.toInt()
        bannerCardResumen.setCardBackgroundColor(resumenBgColor)
        bannerCardResumen.strokeColor = resumenStrokeColor
        bannerTextoResumen.setTextColor(resumenTextColor)
        bannerCardResumen.visibility = View.VISIBLE
        bannerCardResumen.setOnClickListener { mostrarDialogoAlertasVencimiento(alertasDialogo) }
    }

    private fun actualizarPrioridadInventario() {
        val b = _binding ?: return
        if (!isAdded) return

        val resumen = InventarioSummaryRules.construirResumen(listaProductos)
        val alertasLotes = InventarioSummaryRules.construirAlertasLotes(
            productos = listaProductos,
            resolverDiasHastaVencerLote = ::diasHastaVencerLote
        )

        val titulo: String
        val detalle: String
        val accion: String
        val onClick: () -> Unit

        when {
            alertasLotes.vencidos > 0 -> {
                titulo = "Mover primero lotes vencidos"
                detalle = if (alertasLotes.vencidos == 1) {
                    "Hay 1 lote vencido. Conviene revisarlo antes de seguir vendiendo."
                } else {
                    "Hay ${alertasLotes.vencidos} lotes vencidos. Conviene revisarlos antes de seguir vendiendo."
                }
                accion = "Ver lotes"
                onClick = {
                    mostrarDialogoAlertasVencimiento(alertasLotes.alertas.map {
                        LoteAlerta(it.nombreProducto, it.numeroLote, it.vencimiento, it.diasRestantes)
                    })
                }
            }
            alertasLotes.proximos > 0 -> {
                titulo = "Vender primero los lotes por vencer"
                detalle = if (alertasLotes.proximos == 1) {
                    "Hay 1 lote proximo a vencer. Conviene darle salida primero."
                } else {
                    "Hay ${alertasLotes.proximos} lotes proximos a vencer. Conviene darles salida primero."
                }
                accion = "Ver lotes"
                onClick = {
                    mostrarDialogoAlertasVencimiento(alertasLotes.alertas.map {
                        LoteAlerta(it.nombreProducto, it.numeroLote, it.vencimiento, it.diasRestantes)
                    })
                }
            }
            resumen.bajoStock > 0 -> {
                titulo = "Conviene reponer algunos productos"
                detalle = if (resumen.bajoStock == 1) {
                    "Hay 1 producto con stock bajo. Puedes filtrarlo rapido desde aqui."
                } else {
                    "Hay ${resumen.bajoStock} productos con stock bajo. Puedes filtrarlos rapido desde aqui."
                }
                accion = "Filtrar bajo stock"
                onClick = {
                    filtroActual = "BAJO_STOCK"
                    actualizarBtnFiltro()
                    aplicarFiltro(b.editBuscarProducto.text.toString())
                }
            }
            else -> {
                titulo = "Inventario en orden"
                detalle = if (resumen.total == 0) {
                    "Todavia no hay productos cargados en inventario."
                } else {
                    "No se detectan productos criticos para reponer o mover primero."
                }
                accion = "Ver todo"
                onClick = {
                    filtroActual = "TODOS"
                    actualizarBtnFiltro()
                    aplicarFiltro(b.editBuscarProducto.text.toString())
                }
            }
        }

        b.tvTituloPrioridadInventario.text = titulo
        b.tvResumenPrioridadInventario.text = detalle
        b.tvAccionPrioridadInventario.text = accion
        b.cardPrioridadInventario.setOnClickListener { onClick() }
        b.tvAccionPrioridadInventario.setOnClickListener { onClick() }
    }

    @SuppressLint("SetTextI18n")
    private fun mostrarDialogoAlertasVencimiento(alertas: List<LoteAlerta>) {
        if (!isAdded) return
        val dialogBinding = DialogAlertasVencimientoBinding.inflate(layoutInflater)
        val container = dialogBinding.containerAlertasLotes

        val vencidos = alertas.count { it.diasRestantes < 0 }
        val proximos = alertas.count { it.diasRestantes in 0..60 }

        dialogBinding.tvResumenAlertas.text = "${alertas.size} lote${if (alertas.size == 1) "" else "s"} requieren atención"

        if (vencidos > 0) {
            dialogBinding.chipVencidos.visibility = View.VISIBLE
            dialogBinding.tvConteoVencidos.text = "$vencidos vencido${if (vencidos == 1) "" else "s"}"
        }
        if (proximos > 0) {
            dialogBinding.chipProximosVencer.visibility = View.VISIBLE
            dialogBinding.tvConteoProximos.text = "$proximos por vencer"
        }

        alertas.forEach { alerta ->
            val rowBinding = ItemAlertaLoteBinding.inflate(layoutInflater, container, false)
            rowBinding.tvNombreProductoAlerta.text = alerta.nombreProducto
            rowBinding.tvNumeroLoteAlerta.text = "Lote ${alerta.numeroLote}"
            rowBinding.tvVencimientoAlerta.text = "Vence ${alerta.vencimiento}"

            when {
                alerta.diasRestantes < 0 -> {
                    rowBinding.barraUrgencia.setBackgroundColor(0xFFDC2626.toInt())
                    rowBinding.cardEstadoAlerta.setCardBackgroundColor(0xFFFEE2E2.toInt())
                    rowBinding.tvEstadoAlerta.text = "VENCIDO"
                    rowBinding.tvEstadoAlerta.setTextColor(0xFFDC2626.toInt())
                }
                alerta.diasRestantes <= 7 -> {
                    rowBinding.barraUrgencia.setBackgroundColor(0xFFEA580C.toInt())
                    rowBinding.cardEstadoAlerta.setCardBackgroundColor(0xFFFEF3C7.toInt())
                    rowBinding.tvEstadoAlerta.text = "${alerta.diasRestantes}d"
                    rowBinding.tvEstadoAlerta.setTextColor(0xFFEA580C.toInt())
                }
                alerta.diasRestantes <= 30 -> {
                    rowBinding.barraUrgencia.setBackgroundColor(0xFFF59E0B.toInt())
                    rowBinding.cardEstadoAlerta.setCardBackgroundColor(0xFFFEF3C7.toInt())
                    rowBinding.tvEstadoAlerta.text = "${alerta.diasRestantes}d"
                    rowBinding.tvEstadoAlerta.setTextColor(0xFF92400E.toInt())
                }
                else -> {
                    rowBinding.barraUrgencia.setBackgroundColor(0xFF6B7280.toInt())
                    rowBinding.cardEstadoAlerta.setCardBackgroundColor(0xFFF3F4F6.toInt())
                    rowBinding.tvEstadoAlerta.text = "${alerta.diasRestantes}d"
                    rowBinding.tvEstadoAlerta.setTextColor(0xFF374151.toInt())
                }
            }

            container.addView(rowBinding.root)
        }

        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Cerrar", null)
            .show()
    }



    @SuppressLint("SetTextI18n")
    private fun mostrarHistorialProducto(producto: MoldeProductos) {
        if (!isAdded) return
        filtroProductoMovimientos = producto.indice
        filtroTipoMovimiento = "TODOS"
        textoBusquedaMovimientos = ""
        // HUMANO: Default histórico = últimos 7 días usando rango.
        // Antes usaba FiltroFecha.SEMANA, que ya no existe como chip; el rango
        // hace lo mismo y además queda reflejado visualmente en el chip Rango.
        val hoyCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val hace6Cal = (hoyCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -6) }
        fechaRangoInicio = hace6Cal
        fechaRangoFin = hoyCal
        filtroFechaMovimientos = FiltroFecha.RANGO
        aplicarFiltroFecha()
        if (!requireContext().isTablet()) {
            mostrarDialogoMovimientos()
        } else {
            // Tablet: actualizar subtítulo del panel para indicar filtro activo
            val nombreProducto = producto.nombre.ifBlank { producto.indice }
            binding.root.findViewById<TextView>(R.id.tvSubtituloMovimientos)?.text =
                "Historial de: $nombreProducto"
            // Reflejar el rango en el chip Rango
            tabletChipRango?.text = "📆 " + etiquetaCortaRango(hace6Cal, hoyCal)
            sincronizarChipsFiltro(tabletChipHoy, tabletChipFecha, tabletChipRango)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun actualizarEstadoVacio(isEmpty: Boolean, hayFiltrosActivos: Boolean) {
        val b = _binding ?: return
        val estadoVacio = InventarioSummaryRules.construirEstadoVacio(isEmpty, hayFiltrosActivos)
        val inventarioRealmenteVacio = listaProductos.isEmpty()
        val mostrarBotonCrearInicial = inventarioRealmenteVacio && !hayFiltrosActivos

        b.layoutEmptySearch.isVisible = estadoVacio.mostrarVacio
        b.recyclerView.isVisible = estadoVacio.mostrarLista
        b.btnAgregarPrimerProducto?.isVisible = mostrarBotonCrearInicial
        b.floatingbotoncrearproducto.isVisible = !inventarioRealmenteVacio
        // Ocultar el título "Productos" cuando no hay nada que mostrar
        b.root.findViewById<TextView>(R.id.tvTituloProductosInventario)?.isVisible =
            estadoVacio.mostrarLista

        if (estadoVacio.mostrarVacio) {
            b.tvTituloEmpty.text = estadoVacio.titulo
            b.tvDescEmpty.text = estadoVacio.descripcion
            b.btnLimpiarBusqueda.visibility = if (estadoVacio.mostrarBotonLimpiar) View.VISIBLE else View.GONE
        }
    }

    private fun activarDatosSiVisible() {
        if (!isAdded || _binding == null || isHidden || datosActivos) return
        if (!cargaInicialInventarioCompletada) {
            mostrarSkeletonInventarioInicial()
        }
        datosActivos = true
        cargarCategorias()
        iniciarLecturaProductos()
        iniciarLecturaMovimientos()
    }

    private fun desactivarDatos() {
        productosListener?.let { productosRef?.removeEventListener(it) }
        categoriasListener?.let { categoriasRef?.removeEventListener(it) }
        movimientosListener?.let { movimientosRef?.removeEventListener(it) }
        productosListener = null
        productosRef = null
        categoriasListener = null
        categoriasRef = null
        movimientosListener = null
        movimientosRef = null
        datosActivos = false
    }

    private fun mostrarSkeletonInventarioInicial() {
        val b = _binding ?: return
        if (loadingInicialInventarioActivo) return

        loadingInicialInventarioActivo = true
        productosInicialesListos = false
        movimientosInicialesListos = requireContext().isTablet().not()

        b.layoutSkeletonInventarioInicial.showElegantemente(140L)

        runnableTimeoutLoadingInventario?.let(handlerSeguridadInventario::removeCallbacks)
        val runnable = Runnable { ocultarSkeletonInventarioInicial() }
        runnableTimeoutLoadingInventario = runnable
        handlerSeguridadInventario.postDelayed(runnable, 2500L)
    }

    private fun marcarProductosInicialesListos() {
        productosInicialesListos = true
        evaluarOcultamientoSkeletonInventario()
    }

    private fun marcarMovimientosInicialesListos() {
        movimientosInicialesListos = true
        evaluarOcultamientoSkeletonInventario()
    }

    private fun evaluarOcultamientoSkeletonInventario() {
        if (!loadingInicialInventarioActivo) return
        if (!productosInicialesListos || !movimientosInicialesListos) return
        ocultarSkeletonInventarioInicial()
    }

    private fun ocultarSkeletonInventarioInicial() {
        if (!loadingInicialInventarioActivo) return
        loadingInicialInventarioActivo = false
        cargaInicialInventarioCompletada = true

        runnableTimeoutLoadingInventario?.let(handlerSeguridadInventario::removeCallbacks)
        runnableTimeoutLoadingInventario = null

        handlerSeguridadInventario.postDelayed({
            _binding?.layoutSkeletonInventarioInicial?.hideElegantemente(180L)
        }, 60L)
    }

    private fun setupFiltroEstado() {
        actualizarBtnFiltro()
        binding.btnFiltroEstado.setOnClickListener { anchor ->
            val popup = PopupMenu(requireContext(), anchor)
            popup.menu.add(0, 1, 1, "Bajo stock")
            popup.menu.add(0, 2, 2, "Por vencer")
            popup.menu.add(0, 3, 3, "Vencidos")
            popup.setOnMenuItemClickListener { item ->
                val seleccionado = when (item.itemId) {
                    1 -> "BAJO_STOCK"
                    2 -> "PROXIMOS_VENCER"
                    3 -> "VENCIDOS"
                    else -> "TODOS"
                }
                // Toggle: si ya estaba activo el mismo filtro, vuelve a TODOS
                filtroActual = if (filtroActual == seleccionado) "TODOS" else seleccionado
                actualizarBtnFiltro()
                aplicarFiltro(binding.editBuscarProducto.text.toString())
                true
            }
            popup.show()
        }
    }

    private fun actualizarBtnFiltro() {
        val label: String
        val textColor: Int
        val strokeColor: Int
        val bgColor: Int
        when (filtroActual) {
            "BAJO_STOCK" -> {
                label = "Bajo stock"
                textColor = "#A95A37".toColorInt()
                strokeColor = "#F4D8C8".toColorInt()
                bgColor = "#FFF1E8".toColorInt()
            }
            "PROXIMOS_VENCER" -> {
                label = "Por vencer"
                textColor = "#8D5B14".toColorInt()
                strokeColor = "#F0E1A8".toColorInt()
                bgColor = "#FFF7DB".toColorInt()
            }
            "VENCIDOS" -> {
                label = "Vencidos"
                textColor = "#C04F4A".toColorInt()
                strokeColor = "#F5C8C5".toColorInt()
                bgColor = "#FDECEC".toColorInt()
            }
            else -> {
                label = "Filtrar"
                textColor = "#475467".toColorInt()
                strokeColor = "#E5E7EB".toColorInt()
                bgColor = Color.TRANSPARENT
            }
        }
        binding.btnFiltroEstado.text = label
        binding.btnFiltroEstado.setTextColor(textColor)
        binding.btnFiltroEstado.strokeColor = ColorStateList.valueOf(strokeColor)
        binding.btnFiltroEstado.backgroundTintList = ColorStateList.valueOf(bgColor)
        binding.btnFiltroEstado.iconTint = ColorStateList.valueOf(textColor)
    }

    private fun mostrarInfoColores() {
        val ctx = requireContext()
        val contenido = android.widget.LinearLayout(ctx).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(64, 32, 64, 8)
        }
        data class Fila(val color: Int, val titulo: String, val desc: String)
        listOf(
            Fila(0xFF12B76A.toInt(), "Verde — Suficiente", "El stock está por encima del mínimo."),
            Fila(0xFFF79009.toInt(), "Ámbar — Bajo", "El stock está en el límite o por debajo del mínimo."),
            Fila(0xFFF04438.toInt(), "Rojo — Agotado", "Sin unidades disponibles.")
        ).forEach { fila ->
            val fila_layout = android.widget.LinearLayout(ctx).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, 16, 0, 16)
            }
            val dot = View(ctx).apply {
                val dp10 = (10 * resources.displayMetrics.density).toInt()
                layoutParams = android.widget.LinearLayout.LayoutParams(dp10, dp10).apply {
                    marginEnd = (12 * resources.displayMetrics.density).toInt()
                }
                background = androidx.core.content.ContextCompat.getDrawable(ctx, R.drawable.bg_status_dot)
                backgroundTintList = ColorStateList.valueOf(fila.color)
            }
            val textos = android.widget.LinearLayout(ctx).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val tvTitulo = TextView(ctx).apply {
                text = fila.titulo
                setTextColor(0xFF111827.toInt())
                textSize = 14f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
            val tvDesc = TextView(ctx).apply {
                text = fila.desc
                setTextColor(0xFF667085.toInt())
                textSize = 13f
            }
            textos.addView(tvTitulo)
            textos.addView(tvDesc)
            fila_layout.addView(dot)
            fila_layout.addView(textos)
            contenido.addView(fila_layout)
        }
        AlertDialog.Builder(ctx)
            .setTitle("Indicadores de stock")
            .setView(contenido)
            .setPositiveButton("Entendido", null)
            .show()
    }

    private fun mostrarResumenInventario() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_resumen_inventario, null, false)

        val totalProductos = listaProductos.size
        val productosActivos = listaProductos.count { it.estadodelproducto }
        val productosStockBajo = listaProductos.count { producto ->
            val cantidad = producto.cantidadinicial.toIntOrNull() ?: 0
            val minimo = producto.stockminimo.toIntOrNull() ?: 0
            minimo > 0 && cantidad <= minimo
        }
        val productosPorVencer = listaProductos.count { producto ->
            val estado = ProductUtils.obtenerEstadoVencimiento(producto).orEmpty()
            estado == "POR_VENCER" || estado == "VENCIDO"
        }
        val inventarioValorizado = listaProductos.sumOf { producto ->
            val cantidad = (producto.cantidadinicial.toIntOrNull() ?: 0).coerceAtLeast(0)
            val costo = producto.preciodecompra.replace(",", ".").toDoubleOrNull() ?: 0.0
            cantidad * costo
        }

        dialogView.findViewById<TextView>(R.id.tvMetricProductosRegistrados).text = totalProductos.toString()
        dialogView.findViewById<TextView>(R.id.tvMetricProductosActivos).text = productosActivos.toString()
        dialogView.findViewById<TextView>(R.id.tvMetricInventarioValorizado).text =
            MonedaHelper.formatear(inventarioValorizado)
        dialogView.findViewById<TextView>(R.id.tvMetricStockBajo).text = productosStockBajo.toString()
        dialogView.findViewById<TextView>(R.id.tvMetricPorVencer).text = productosPorVencer.toString()

        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.setOnShowListener {
            dialog.behavior.skipCollapsed = true
            dialog.behavior.isFitToContents = true
            dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        dialogView.findViewById<MaterialButton>(R.id.btnCerrarResumenInventario)
            .setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun estaVencido(vencimiento: String): Boolean {
        return try {
            val partes = vencimiento.replace("_", "/").split("/")
            val mes = partes[0].toInt()
            val anio = partes[1].toInt() + 2000
            val cal = Calendar.getInstance()
            (anio * 12 + mes) < (cal.get(Calendar.YEAR) * 12 + (cal.get(Calendar.MONTH) + 1))
        } catch (_: Exception) {
            false
        }
    }

    private fun estaProximoAVencer(vencimiento: String): Boolean {
        return try {
            val partes = vencimiento.replace("_", "/").split("/")
            val mes = partes[0].toInt()
            val anio = partes[1].toInt() + 2000
            val cal = Calendar.getInstance()
            val diff = (anio * 12 + mes) -
                (cal.get(Calendar.YEAR) * 12 + (cal.get(Calendar.MONTH) + 1))
            diff in 1..3
        } catch (_: Exception) {
            false
        }
    }

    private fun Context.isTablet(): Boolean = resources.configuration.smallestScreenWidthDp >= 600

    private fun ocultarTeclado() {
        val imm = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.editBuscarProducto.windowToken, 0)
    }

    private fun dpToPxFloat(dp: Int): Float {
        return dp * resources.displayMetrics.density
    }

    override fun onDestroyView() {
        dialogMovimientos?.dismiss()
        dialogDetalleMovimiento?.dismiss()
        handlerSeguridadInventario.removeCallbacksAndMessages(null)
        runnableTimeoutLoadingInventario = null
        loadingInicialInventarioActivo = false
        tabletChipHoy = null
        tabletChipFecha = null
        tabletChipRango = null
        tabletEditBuscarMovimientos = null
        filtroProductoMovimientos = null
        desactivarDatos()
        super.onDestroyView()
        _binding = null
    }

    data class MovimientoInventarioUi(
        val indiceProducto: String,
        val tipo: String,
        val titulo: String,
        val descripcion: String,
        val hora: String,
        val nombreUsuario: String,
        val referenciaId: String,
        val cantidad: Int?,
        val stockAntes: Int?,
        val stockDespues: Int?,
        val timestamp: Long,
        val nombreProducto: String,
        val presentacion: String,
        val cantidadMovimiento: Int? = null,
        val unidadMovimiento: String = "",
        val unidadesPorMovimiento: Int? = null,
        val motivo: String,
        val detalleLotes: String = "",
        val fechaPath: String = ""   // clave del nodo en Firebase: "yyyy-MM-dd"
    )

    private class AdapterMovimientosInventario(
        private val onItemClick: (MovimientoInventarioUi) -> Unit
    ) : RecyclerView.Adapter<AdapterMovimientosInventario.MovimientoViewHolder>() {

        private val items = mutableListOf<MovimientoInventarioUi>()

        class MovimientoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val cardIcono: MaterialCardView = view.findViewById(R.id.cardIconoMovimiento)
            val tvIcono: TextView = view.findViewById(R.id.tvIconoMovimiento)
            val tvTitulo: TextView = view.findViewById(R.id.tvTituloMovimiento)
            val tvDetalle: TextView = view.findViewById(R.id.tvDetalleMovimiento)
            val tvCantidad: TextView = view.findViewById(R.id.tvCantidadMovimiento)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovimientoViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_movimiento_inventario, parent, false)
            return MovimientoViewHolder(view)
        }

        override fun onBindViewHolder(holder: MovimientoViewHolder, position: Int) {
            val item = items[position]
            val estilo = resolverEstilo(item)

            holder.cardIcono.setCardBackgroundColor(estilo.colorFondo)
            holder.tvIcono.text = estilo.icono
            holder.tvIcono.setTextColor(estilo.colorTexto)
            holder.tvTitulo.text = construirTitulo(item, estilo)
            holder.tvDetalle.text = buildString {
                append(item.hora.ifBlank { "Sin hora" })
                if (item.nombreUsuario.isNotBlank()) {
                    append(" · ")
                    append(item.nombreUsuario)
                }
                if (item.referenciaId.isNotBlank()) {
                    append(" · ")
                    append(formatearReferencia(item.referenciaId))
                }
                val detalleBase = item.descripcion.ifBlank { item.indiceProducto }
                if (detalleBase.isNotBlank()) {
                    append(" · ")
                    append(detalleBase)
                }
            }
            holder.tvDetalle.text = construirDetalle(item)
            holder.tvCantidad.text = item.cantidad?.let { cantidad ->
                when {
                    cantidad < 0 -> cantidad.toString()
                    cantidad > 0 -> "${if (estilo.esSalida) "-" else "+"}$cantidad"
                    else -> "0"
                }
            } ?: item.indiceProducto.uppercase(Locale.getDefault())
            holder.tvCantidad.setTextColor(estilo.colorTexto)
            holder.itemView.setOnClickListener { onItemClick(item) }
        }

        override fun getItemCount(): Int = items.size

        @SuppressLint("NotifyDataSetChanged")
        fun updateList(nuevosItems: List<MovimientoInventarioUi>) {
            items.clear()
            items.addAll(nuevosItems)
            notifyDataSetChanged()
        }

        private fun construirTitulo(item: MovimientoInventarioUi, estilo: EstiloMovimiento): String {
            return when {
                esSalidaPorVenta(item) -> "Salida por venta"
                item.titulo.isNotBlank() -> item.titulo
                else -> estilo.tituloFallback
            }
        }

        private fun construirDetalle(item: MovimientoInventarioUi): String {
            return if (esSalidaPorVenta(item)) {
                construirDetalleSalidaVenta(item)
            } else {
                construirDetalleGeneral(item)
            }
        }

        private fun construirDetalleSalidaVenta(item: MovimientoInventarioUi): String {
            val lineaCabecera = buildString {
                append(item.hora.ifBlank { "Sin hora" })
                if (item.nombreUsuario.isNotBlank()) {
                    append(" · Retirado por ")
                    append(item.nombreUsuario)
                }
            }

            val producto = resolverNombreProducto(item)
            val presentacion = item.presentacion.trim()
            val lineaProducto = buildString {
                append("Producto: ")
                append(producto.ifBlank { item.indiceProducto.ifBlank { "Sin detalle" } })
                if (presentacion.isNotBlank()) {
                    append(" · ")
                    append(presentacion)
                }
            }

            return buildList {
                add(lineaCabecera)
                add(lineaProducto)
                item.detalleLotes.takeIf { it.isNotBlank() }?.let { add("Lotes: $it") }
            }
                .filter { it.isNotBlank() }
                .joinToString("\n")
        }

        private fun construirDetalleGeneral(item: MovimientoInventarioUi): String {
            val lineaCabecera = buildString {
                append(item.hora.ifBlank { "Sin hora" })
                if (item.nombreUsuario.isNotBlank()) {
                    append(" · ")
                    append(item.nombreUsuario)
                }
            }

            val referencia = construirReferenciaHumana(item)
            val detalleBase = construirDescripcionHumana(item)

            val lineaDetalle = listOf(referencia, detalleBase)
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString(" · ")

            return buildList {
                add(lineaCabecera)
                add(lineaDetalle)
                construirTextoLotes(item)?.let { add(it) }
            }
                .filter { it.isNotBlank() }
                .joinToString("\n")
        }

        private fun construirReferenciaHumana(item: MovimientoInventarioUi): String {
            val referencia = item.referenciaId.trim()
            if (referencia.isBlank()) return ""

            return when {
                esMovimientoBloqueoDevolucion(item) -> "Devolucion bloqueada de la venta"
                esMovimientoEntradaDevolucion(item) -> "Devolucion de la venta"
                esSalidaPorVenta(item) -> "Venta relacionada"
                referencia.equals("ajuste_manual", ignoreCase = true) -> "Ajuste manual"
                referencia.equals("ingreso_stock", ignoreCase = true) -> "Ingreso de stock"
                referencia.equals(item.indiceProducto, ignoreCase = true) -> ""
                else -> formatearReferencia(referencia)
            }
        }

        private fun construirDescripcionHumana(item: MovimientoInventarioUi): String {
            val producto = resolverNombreProducto(item)
                .ifBlank { item.nombreProducto.ifBlank { item.indiceProducto } }
            if (producto.isBlank()) return ""

            return when {
                esMovimientoDevolucion(item) -> "Producto: $producto"
                else -> item.descripcion
                    .trim()
                    .sanitizarTextoVisible()
                    .ifBlank { producto }
            }
        }

        private fun construirTextoLotes(item: MovimientoInventarioUi): String? {
            val detalleLotes = item.detalleLotes.trim()
            if (detalleLotes.isBlank()) return null

            return when {
                esMovimientoBloqueoDevolucion(item) -> "Lotes bloqueados: $detalleLotes"
                esMovimientoEntradaDevolucion(item) -> "Lotes restituidos: $detalleLotes"
                else -> "Lotes: $detalleLotes"
            }
        }

        private fun resolverEstilo(item: MovimientoInventarioUi): EstiloMovimiento {
            val tipo = item.tipo.lowercase(Locale.getDefault())
            return when {
                tipo.contains("creado") -> EstiloMovimiento(
                    icono = "+",
                    colorFondo = "#E8F6EF".toColorInt(),
                    colorTexto = "#0E8F63".toColorInt(),
                    tituloFallback = "Producto creado",
                    esSalida = false
                )
                tipo.contains("editado") -> EstiloMovimiento(
                    icono = "•",
                    colorFondo = "#EEF2FF".toColorInt(),
                    colorTexto = "#4F46E5".toColorInt(),
                    tituloFallback = "Producto editado",
                    esSalida = false
                )
                tipo.contains("eliminado") -> EstiloMovimiento(
                    icono = "×",
                    colorFondo = "#FDECEC".toColorInt(),
                    colorTexto = "#C62828".toColorInt(),
                    tituloFallback = "Producto eliminado",
                    esSalida = true
                )
                tipo.contains("salida_venta") || tipo.contains("salida") -> EstiloMovimiento(
                    icono = "-",
                    colorFondo = "#FFF1F0".toColorInt(),
                    colorTexto = "#D93025".toColorInt(),
                    tituloFallback = "Salida por venta",
                    esSalida = true
                )
                else -> EstiloMovimiento(
                    icono = "↺",
                    colorFondo = "#FFF4E5".toColorInt(),
                    colorTexto = "#B26A00".toColorInt(),
                    tituloFallback = "Movimiento de inventario",
                    esSalida = false
                )
            }
        }

        private fun esSalidaPorVenta(item: MovimientoInventarioUi): Boolean {
            return item.tipo.lowercase(Locale.getDefault()).contains("salida_venta")
        }

        private fun esMovimientoDevolucion(item: MovimientoInventarioUi): Boolean {
            return item.tipo.lowercase(Locale.getDefault()).contains("devolucion")
        }

        private fun esMovimientoEntradaDevolucion(item: MovimientoInventarioUi): Boolean {
            return item.tipo.lowercase(Locale.getDefault()).contains("entrada_devolucion")
        }

        private fun esMovimientoBloqueoDevolucion(item: MovimientoInventarioUi): Boolean {
            return item.tipo.lowercase(Locale.getDefault()).contains("bloqueado_devolucion")
        }

        private fun resolverNombreProducto(item: MovimientoInventarioUi): String {
            if (item.nombreProducto.isNotBlank()) return item.nombreProducto

            val descripcion = item.descripcion.trim()
            if (descripcion.isBlank()) return ""

            val patrones = listOf(
                Regex("(?i)para\\s+(.+?)(?:\\.\\s+lotes|$)"),
                Regex("(?i)de\\s+(.+)$")
            )

            return patrones.firstNotNullOfOrNull { patron ->
                patron.find(descripcion)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
            }.orEmpty()
        }

        private fun formatearReferencia(referenciaId: String): String {
            val referencia = referenciaId.trim()
            if (referencia.isBlank()) return ""

            val referenciaLower = referencia.lowercase(Locale.getDefault())
            if (referenciaLower.startsWith("fpventa_") || referenciaLower.startsWith("venta_")) {
                return "Venta relacionada"
            }
            if (referenciaLower.startsWith("fpdevolucion_") || referenciaLower.startsWith("devolucion_")) {
                return "Devolucion relacionada"
            }

            val normalizada = referencia.replace("_", " ").replace("-", " ").trim()
            val matchVenta = Regex("(?i)venta\\s*(\\d+)").find(normalizada.replace("#", " "))
            if (matchVenta != null) {
                return "Venta #${matchVenta.groupValues[1]}"
            }

            return normalizada
                .split(Regex("\\s+"))
                .joinToString(" ") { palabra ->
                    palabra.lowercase(Locale.getDefault())
                        .replaceFirstChar { c -> c.titlecase(Locale.getDefault()) }
                }
        }

        private fun String.sanitizarTextoVisible(): String {
            return this
                .replace(Regex("(?i)venta\\s+#?[A-Za-z0-9_-]{8,}"), "venta relacionada")
                .replace(Regex("(?i)fpventa_[A-Za-z0-9_-]+"), "venta relacionada")
                .replace(Regex("(?i)fpdevolucion_[A-Za-z0-9_-]+"), "devolucion relacionada")
                .replace(Regex("\\s{2,}"), " ")
                .trim()
        }

        data class EstiloMovimiento(
            val icono: String,
            val colorFondo: Int,
            val colorTexto: Int,
            val tituloFallback: String,
            val esSalida: Boolean
        )
    }
}

private fun String.humanizarReferenciaInterna(): String {
    val referencia = trim()
    if (referencia.isBlank()) return referencia

    val referenciaLower = referencia.lowercase(Locale.getDefault())
    return when {
        referenciaLower.startsWith("fpventa_") || referenciaLower.startsWith("venta_") -> "Venta relacionada"
        referenciaLower.startsWith("fpdevolucion_") || referenciaLower.startsWith("devolucion_") -> "Devolucion relacionada"
        else -> referencia
    }
}
