package com.app.administradorfarmadon.ActivityInventario

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.CatalogoPresentaciones
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteConsumoResolucion
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteConsumoRules
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteProducto
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteIndexado
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.PresentacionProducto
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.SugerenciaPresentacion
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.toMoldeProductos
import com.app.administradorfarmadon.ActivityInventario.reference.normalizeProductName
import com.app.administradorfarmadon.ClasesDatabase.DbPaths
import com.app.administradorfarmadon.ActivityInventario.domain.PresentacionRules
import com.app.administradorfarmadon.ActivityInventario.domain.ProductoFormData
import com.app.administradorfarmadon.ActivityInventario.domain.ProductoFormValidator
import com.app.administradorfarmadon.ActivityInventario.domain.ProductoInventarioMetrics
import com.app.administradorfarmadon.ActivityInventario.domain.ProductoResumenInventarioBuilder
import com.app.administradorfarmadon.ActivityInventario.ui.ConsumeLotSubmit
import com.app.administradorfarmadon.ActivityInventario.ui.ConsumeLotUiModel
import com.app.administradorfarmadon.ActivityInventario.ui.GeneralInfoEditorUiModel
import com.app.administradorfarmadon.ActivityInventario.ui.GeneralInfoSubmit
import com.app.administradorfarmadon.ActivityInventario.ui.InventoryEditSubmit
import com.app.administradorfarmadon.ActivityInventario.ui.InventoryEditorUiModel
import com.app.administradorfarmadon.ActivityInventario.ui.LotDetailUiModel
import com.app.administradorfarmadon.ActivityInventario.ui.LotFormSubmit
import com.app.administradorfarmadon.ActivityInventario.ui.LotFormUiModel
import com.app.administradorfarmadon.ActivityInventario.ui.LotListUiItem
import com.app.administradorfarmadon.ActivityInventario.ui.MobileEditProductScreen
import com.app.administradorfarmadon.ActivityInventario.ui.OtherDetailsSubmit
import com.app.administradorfarmadon.ActivityInventario.ui.OtherDetailsUiModel
import com.app.administradorfarmadon.ActivityInventario.ui.PresentacionUiItem
import com.app.administradorfarmadon.ActivityInventario.ui.PresentacionesEditorUiModel
import com.app.administradorfarmadon.ActivityInventario.ui.PresentacionesSubmit
import com.app.administradorfarmadon.ActivityInventario.ui.TabletEditProductScreen
import com.app.administradorfarmadon.ActivityInventario.ui.TabletEditSection
import com.app.administradorfarmadon.ActivityInventario.ui.TabletLotsPanelState
import com.app.administradorfarmadon.ActivityInventario.ui.TabletProductSummaryModel
import com.app.administradorfarmadon.ActivityInventario.ui.TabletSectionListItemModel
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.ClasesDatabase.MovimientoInventarioLogger
import com.app.administradorfarmadon.ClasesDatabase.MovimientoLogger
import com.app.administradorfarmadon.ClasesDatabase.PresentacionHelper
import com.app.administradorfarmadon.ClasesDatabase.PresentacionesTiendaConfigManager
import com.app.administradorfarmadon.ClasesDatabase.SessionManager
import com.app.administradorfarmadon.ClasesDatabase.UnidadBaseHelper
import com.app.administradorfarmadon.R
import com.app.administradorfarmadon.databinding.ActivityEditarProductodelInventarioBinding
import com.app.administradorfarmadon.databinding.BottomsheetAgregarPresentacionBinding
import com.app.administradorfarmadon.databinding.DialogAjusteStockBinding
import com.app.administradorfarmadon.databinding.DialogEgresoStockBinding
import com.app.administradorfarmadon.databinding.DialogGestionarLotesBinding
import com.app.administradorfarmadon.databinding.DialogHistorialLoteBinding
import com.app.administradorfarmadon.databinding.DialogIngresoStockBinding
import com.app.administradorfarmadon.databinding.ItemLoteBinding
import com.app.administradorfarmadon.databinding.ItemMovimientoLoteBinding
import com.app.administradorfarmadon.databinding.ItemPresentacionGuardadaBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.Locale
import java.util.UUID


class EditarProductodelInventario : AppCompatActivity() {

    private data class InventarioComposeSnapshot(
        val unidadBase: String,
        val stockActual: String,
        val stockMinActual: String,
        val usarPresentacion: Boolean,
        val stockMinSuffix: String
    )

    private fun obtenerInventarioComposeSnapshot(): InventarioComposeSnapshot {
        val unidadBase = formatearEtiquetaUi(
            binding.editTextUnidadBase.text?.toString()?.trim().orEmpty().ifBlank { "Unidad" }
        )

        val stockActual = (binding.editTextCantidad.text?.toString()?.trim()?.toIntOrNull() ?: 0)
            .coerceAtLeast(0)
            .toString()

        val usarPresentacion = binding.radioPorPaquetes.isChecked

        val stockMinActual = if (usarPresentacion) {
            binding.editTextStockPresentacion.text?.toString()?.trim().orEmpty().ifBlank { "0" }
        } else {
            binding.editTextStock.text?.toString()?.trim().orEmpty().ifBlank { "0" }
        }

        val stockMinSuffix = if (usarPresentacion) {
            pluralizarTexto(obtenerNombrePresentacionAjuste())
        } else {
            pluralizarTexto(unidadBase.lowercase(Locale.getDefault()))
        }

        return InventarioComposeSnapshot(
            unidadBase = unidadBase,
            stockActual = stockActual,
            stockMinActual = stockMinActual,
            usarPresentacion = usarPresentacion,
            stockMinSuffix = stockMinSuffix
        )
    }

    private data class CambioVisibleConfirmacion(
        val etiqueta: String,
        val antes: String,
        val despues: String
    )

    private data class LotesSheetSnapshot(
        val lotes: Map<String, LoteProducto>,
        val loteConsumoSeleccionado: String,
        val loteConsumoSeleccionManual: Boolean
    )

    private data class SeccionEdicionSnapshot(
        val nombre: String,
        val categoria: String,
        val requiereReceta: Boolean,
        val estadoProducto: Boolean,
        val costoBase: String,
        val stockMinimoUnidad: String,
        val stockMinimoPresentacion: String,
        val vencimiento: String,
        val tienePresentaciones: Boolean,
        val presentacionPrincipalTexto: String,
        val presentacionPrincipalTag: String,
        val presentacionPrincipalSeleccionManual: Boolean,
        val presentaciones: List<PresentacionProducto>
    )

    private enum class LotesScreen {
        LISTA,
        DETALLE,
        FORMULARIO,
        CONSUMO
    }

    private data class GeneralInfoComposeSnapshot(
        val productName: String,
        val category: String,
        val requiresPrescription: Boolean,
        val isActive: Boolean,
        val nameHelper: String?
    )

    private data class PresentacionesComposeSnapshot(
        val productName: String,
        val principalId: String,
        val presentaciones: List<PresentacionProducto>,
        val snapshot: String
    )

    private data class OtrosDetallesComposeSnapshot(
        val productName: String,
        val expiryInput: String
    )

    private data class ProductSummaryComposeSnapshot(
        val name: String,
        val category: String,
        val status: String,
        val vendibleChip: String,
        val fisicoChip: String,
        val vence: String,
        val stockMinimo: String,
        val imageUrl: String,
        val imageUri: String?
    )

    private var productSummaryState = ProductSummaryComposeSnapshot(
        name = "Producto",
        category = "Sin categoría",
        status = "Activo",
        vendibleChip = "Vendible: 0",
        fisicoChip = "Físico: 0",
        vence = "Vence: No definido",
        stockMinimo = "Stock mín: 0",
        imageUrl = "",
        imageUri = null
    )

    private var otrosDetallesState = OtrosDetallesComposeSnapshot(
        productName = "Producto",
        expiryInput = ""
    )

    private var presentacionesState = PresentacionesComposeSnapshot(
        productName = "Producto",
        principalId = "",
        presentaciones = emptyList(),
        snapshot = ""
    )

    private var generalInfoState = GeneralInfoComposeSnapshot(
        productName = "",
        category = "",
        requiresPrescription = false,
        isActive = true,
        nameHelper = null
    )

    private var estadoInicialFormulario: String = ""
    private var cargandoDatosIniciales = true
    private var accionDespuesDeGuardarProducto: (() -> Unit)? = null
    private var productoOriginalParaAuditoria: MoldeProductos? = null
    private var dialogoProgresoStock: AlertDialog? = null
    private var ocultarVencimientoGeneralPorLotes = false
    private var loteConsumoSeleccionadoActual: String = ""
    private var loteConsumoSeleccionManualActual = false
    private var botonGuardarSeccionActivo: MaterialButton? = null
    private var snapshotFormularioSeccionActiva: String? = null
    private var stockMinimoUnidadEnErrorTiempoReal = false
    private var stockMinimoPresentacionEnErrorTiempoReal = false
    private var stockMinimoUnidadSinAvisoTiempoReal = false
    private var stockMinimoPresentacionSinAvisoTiempoReal = false
    private var runnableActualizacionUiFormulario: Runnable? = null
    private var modoTabletDosPaneles = false
    private var modoMobileCompose = false
    private var tabletComposeContainer: ComposeView? = null
    private var mobileComposeContainer: ComposeView? = null
    private var tabletComposeContentInicializado = false
    private var mobileComposeContentInicializado = false
    private var cantidadBottomSheetsAbiertos = 0
    private var animadorIosLoader: ObjectAnimator? = null
    private var mobileComposeFormularioAbierto = false
    private var overlayCargaVisible = true
    private var seccionTabletComposeActiva: TabletEditSection? = null
    private val tabletComposeRefreshState = mutableIntStateOf(0)
    private var tabletLotesScreenActiva: LotesScreen? = null
    private var tabletLotesSnapshotUi: LotesSheetSnapshot = LotesSheetSnapshot(emptyMap(), "", false)
    private var tabletLotesSelectedKey: String? = null
    private var tabletLotesSearchQuery: String = ""
    private var snapshotSeccionTabletActiva: SeccionEdicionSnapshot? = null
    private var cambiosGuardadosEnSeccionTablet = false

    private lateinit var binding: ActivityEditarProductodelInventarioBinding

    private val listaCategoria = mutableListOf<CategoriaProductos>()
    private val listaNombreAutoComplete = mutableListOf<NombreProductos>()
    private val listaPresentaciones = mutableListOf<PresentacionProducto>()
    private var presentacionPrincipalElegidaManualmente = false
    private var categoriaAdapter: ArrayAdapter<String>? = null
    private var categoriaSugerenciasConfiguradas = false
    private var actualizandoCategoriaProgramaticamente = false
    private val offsetScrollCategoriaSugerida = 320

    private var indiceOriginal: String = ""
    private var codigoOriginal: String = ""
    private var basePorUnidad: Double = 0.0
    private var unidadBaseOriginalInmutable: String = ""
    private var tipoBaseInventarioOriginal: String = ""
    private var imagenUri: Uri? = null
    private var imagenUrlActual: String = ""
    private var cameraImageUri: Uri? = null

    // Unidad base: mensajes dinámicos y confirmación para g/mL (evita confusión con el stock)
    private var ultimaUnidadBaseConfirmada: String = ""
    private var unidadBaseOpciones: List<String> = emptyList()
    private var unidadBaseOpcionesTodas: List<String> = emptyList()
    private var cambiandoUnidadBaseProgramaticamente = false

    // Control de stock: lo que ve el usuario (Por unidades / Por peso / Por volumen)
    private enum class ModoControlStock { UNIDADES, PESO, VOLUMEN }
    private var modoControlStock: ModoControlStock = ModoControlStock.UNIDADES
    private var cambiandoModoControlStock = false

    companion object {
        private const val PATH_INVENTARIO = "Inventario"
        private const val PATH_CATEGORIAS = "CategoriasInventario"
        private const val PATH_NOMBRES = "NombresProductos"
        private const val PATH_PRODUCTOS = "Productos"
        private const val PATH_BUSQUEDA = "BusquedaProductos"
        private const val PATH_PRESENTACIONES = "Presentaciones"

        private const val CLOUDINARY_CLOUD_NAME = "dluvatyh7"
        private const val CLOUDINARY_UPLOAD_PRESET = "productos_app"
    }

    // ---------------------------------------------------------------------------------------------
    // LAUNCHERS IMAGEN
    // ---------------------------------------------------------------------------------------------

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && cameraImageUri != null) {
                imagenUri = cameraImageUri
                binding.imagevieW.visibility = View.VISIBLE
                Glide.with(this)
                    .load(imagenUri)
                    .centerCrop()
                    .into(binding.imagevieW)

                actualizarEstadoBotonGuardar()
                notificarRenderTabletCompose()
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imagenUri = it
                binding.imagevieW.visibility = View.VISIBLE
                Glide.with(this)
                    .load(it)
                    .centerCrop()
                    .into(binding.imagevieW)

                actualizarEstadoBotonGuardar()
                notificarRenderTabletCompose()
            }
        }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                abrirCamara()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarProductodelInventarioBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        mostrarCarga(
            true,
            titulo = "Cargando producto",
            mensaje = "Preparando los datos para editar..."
        )
        SessionManager.cargarSesion(this)

        instalarBackHandlerDropdowns()
        PresentacionesTiendaConfigManager.precargar()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        configurarToolbar()
        configurarAcordeonFormulario()
        configurarModoTabletDosPanelesSiAplica()
        configurarBotonesAccion()
        configurarScrollEnCampos()
        configurarAutoCompletesBase()
        configurarSelectorFecha()
        configurarLogicaStock()
        configurarValidacionStockMinimoTiempoReal()
        configurarCamposStockSoloLectura()
        configurarOcultarTecladoAlTocarFuera()
        configurarOcultarTecladoPorIme()
        if (usuarioPuedeAjustarStock()) {
            binding.btnAjustarStock.visibility = View.VISIBLE
            binding.btnIngresarStock.visibility = View.VISIBLE
            binding.btnEgresoStock.visibility = View.VISIBLE
            binding.layoutQuickActions.visibility = View.VISIBLE
            binding.tvTituloAccionesRapidas.visibility = View.VISIBLE
            configurarAjusteStock()
            configurarIngresoStock()
            configurarEgresoStock()
        } else {
            binding.btnAjustarStock.visibility = View.GONE
            binding.btnIngresarStock.visibility = View.GONE
            binding.btnEgresoStock.visibility = View.GONE
            binding.layoutQuickActions.visibility = View.GONE
            binding.tvTituloAccionesRapidas.visibility = View.GONE
        }
        configurarPresentaciones()
        configurarSeleccionImagen()

        configurarBotonGuardarPorCambios()
        configurarValidacionNombreRealTime()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (cerrarDropdownsSiEstanAbiertos()) return
                finish()
            }
        })

        obtenerIntent()
        obtenerCategoriasDeProductos()
    }

    // ---------------------------------------------------------------------------------------------
    // UI
    // ---------------------------------------------------------------------------------------------

    private fun configurarToolbar() {
        binding.materialToolbar3.setNavigationOnClickListener {
            if (cerrarDropdownsSiEstanAbiertos()) return@setNavigationOnClickListener
            finish()
        }
        binding.materialToolbar3.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.actionMasOpcionesProducto -> {
                    val popup = PopupMenu(this, binding.materialToolbar3, Gravity.END).apply {
                        menu.add(0, R.id.actionBorrarProducto, 0, "Borrar")
                        setOnMenuItemClickListener { popupItem ->
                            when (popupItem.itemId) {
                                R.id.actionBorrarProducto -> {
                                    borrarProductoClick(binding.materialToolbar3)
                                    true
                                }
                                else -> false
                            }
                        }
                    }
                    popup.show()
                    true
                }
                else -> false
            }
        }
    }

    private fun configurarAcordeonFormulario() {
        // Acordeón XML legacy retirado: la edición vive en Compose (tablet y mobile).
        // Solo refrescamos los TextViews ocultos que la UI Compose lee para el resumen.
        actualizarResumenTarjetasEdicion()
    }




    private fun configurarModoTabletDosPanelesSiAplica() {
        modoTabletDosPaneles = resources.configuration.smallestScreenWidthDp >= 720
        if (modoTabletDosPaneles) {
            if (tabletComposeContainer != null) return

            binding.constraintcontainer.visibility = View.GONE
            binding.cardResumenFijo.visibility = View.GONE
            binding.layoutBotonesInferiores.visibility = View.GONE

            val composeView = ComposeView(this).apply {
                id = View.generateViewId()
                visibility = if (overlayCargaVisible) View.INVISIBLE else View.VISIBLE
            }
            binding.main.addView(
                composeView,
                ConstraintLayout.LayoutParams(0, 0).apply {
                    topToBottom = binding.materialToolbar3.id
                    bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                    startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                }
            )
            tabletComposeContainer = composeView
            binding.layoutCargando.bringToFront()
            binding.layoutCargando.requestLayout()
            binding.layoutCargando.invalidate()
            renderPantallaTabletCompose()
            return
        }

        if (mobileComposeContainer != null) return
        modoMobileCompose = true

        binding.constraintcontainer.visibility = View.GONE
        binding.cardResumenFijo.visibility = View.GONE
        binding.layoutBotonesInferiores.visibility = View.GONE

        val composeView = ComposeView(this).apply {
            id = View.generateViewId()
            visibility = if (overlayCargaVisible) View.INVISIBLE else View.VISIBLE
        }
        binding.main.addView(
            composeView,
            ConstraintLayout.LayoutParams(0, 0).apply {
                topToBottom = binding.materialToolbar3.id
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }
        )
        mobileComposeContainer = composeView
        binding.layoutCargando.bringToFront()
        binding.layoutCargando.requestLayout()
        binding.layoutCargando.invalidate()
        renderPantallaMobileCompose()
    }

    private fun renderPantallaTabletCompose() {
        val composeView = tabletComposeContainer ?: return
        if (tabletComposeContentInicializado) return
        tabletComposeContentInicializado = true
        composeView.setContent {
            androidx.compose.material3.MaterialTheme {
                tabletComposeRefreshState.intValue
                TabletEditProductScreen(
                    summary = construirResumenProductoTablet(),
                    sectionItems = construirSeccionesTablet(),
                    generalInfoModel = construirUiInformacionGeneralSheetModel(),
                    inventoryModel = construirUiInventarioSheetModel(),
                    presentationsModel = construirUiPresentacionesSheetModel(obtenerSnapshotPresentacionesSeccion()),
                    otherDetailsModel = construirUiOtrosDetallesSheetModel(),
                    lotsPanelState = construirEstadoLotesTablet(),
                    refreshKey = tabletComposeRefreshState.intValue,
                    onSectionSelected = { section -> sincronizarSesionSeccionTabletCompose(section) },
                    onCloseSection = { section -> cancelarSesionSeccionTabletCompose(section) },
                    onSaveGeneral = { submit ->
                        aplicarInformacionGeneralDesdeCompose(submit)
                        guardarCambiosDesdeFormulario {
                            actualizarResumenTarjetasEdicion()
                            marcarGuardadoSeccionTabletCompose()
                        }
                    },
                    onSaveInventory = { submit ->
                        aplicarInventarioDesdeCompose(submit)
                        guardarCambiosDesdeFormulario {
                            actualizarResumenesStockTrasEdicion()
                            actualizarResumenTarjetasEdicion()
                            marcarGuardadoSeccionTabletCompose()
                        }
                    },
                    onAddPresentation = {
                        mostrarBottomSheetPresentacionCompacta(onComplete = { notificarRenderTabletCompose() })
                    },
                    onEditPresentation = { presentacionId ->
                        buscarPresentacionEnLista(presentacionId)?.let { presentacion ->
                            mostrarBottomSheetPresentacionCompacta(
                                presentacionEditar = presentacion,
                                onComplete = { notificarRenderTabletCompose() }
                            )
                        }
                    },
                    onDeletePresentation = { presentacionId ->
                        eliminarPresentacionDesdeSheet(presentacionId)
                        notificarRenderTabletCompose()
                    },
                    onSavePresentations = { submit ->
                        aplicarPresentacionesDesdeCompose(submit)
                        guardarCambiosDesdeFormulario {
                            actualizarResumenTarjetasEdicion()
                            marcarGuardadoSeccionTabletCompose()
                        }
                    },
                    onManageLots = { abrirLotesEnPanelTablet() },
                    onSaveOtherDetails = { submit ->
                        aplicarOtrosDetallesDesdeCompose(submit)
                        guardarCambiosDesdeFormulario {
                            actualizarResumenTarjetasEdicion()
                            marcarGuardadoSeccionTabletCompose()
                        }
                    },
                    onLotBack = { navegarAtrasLotesTablet() },
                    onLotSearchChange = {
                        tabletLotesSearchQuery = it
                        notificarRenderTabletCompose()
                    },
                    onLotItemClick = { key ->
                        tabletLotesSelectedKey = key
                        tabletLotesScreenActiva = LotesScreen.DETALLE
                        notificarRenderTabletCompose()
                    },
                    onLotEdit = {
                        tabletLotesScreenActiva = LotesScreen.FORMULARIO
                        notificarRenderTabletCompose()
                    },
                    onLotManualConsume = {
                        tabletLotesScreenActiva = LotesScreen.CONSUMO
                        notificarRenderTabletCompose()
                    },
                    onLotFormSave = { submit -> guardarLoteDesdePanelTablet(submit) },
                    onLotConsumeSave = { submit -> guardarConsumoLoteDesdePanelTablet(submit) }
                )
            }
        }
    }

    private fun renderPantallaMobileCompose() {
        val composeView = mobileComposeContainer ?: return
        if (mobileComposeContentInicializado) return
        mobileComposeContentInicializado = true
        composeView.setContent {
            androidx.compose.material3.MaterialTheme {
                tabletComposeRefreshState.intValue
                MobileEditProductScreen(
                    summary = construirResumenProductoTablet(),
                    sectionItems = construirSeccionesTablet(),
                    showQuickActions = usuarioPuedeAjustarStock(),
                    generalInfoModel = construirUiInformacionGeneralSheetModel(),
                    inventoryModel = construirUiInventarioSheetModel(),
                    presentationsModel = construirUiPresentacionesSheetModel(obtenerSnapshotPresentacionesSeccion()),
                    otherDetailsModel = construirUiOtrosDetallesSheetModel(),
                    lotsPanelState = construirEstadoLotesTablet(),
                    refreshKey = tabletComposeRefreshState.intValue,
                    onPickImage = { mostrarOpcionesImagen() },
                    onAdjustStock = { binding.btnAjustarStock.performClick() },
                    onIngresoStock = { binding.btnIngresarStock.performClick() },
                    onEgresoStock = { binding.btnEgresoStock.performClick() },
                    onSectionVisibilityChanged = { abierta ->
                        actualizarModoFormularioMobileCompose(abierta)
                    },
                    onSectionSelected = { section -> sincronizarSesionSeccionTabletCompose(section) },
                    onCloseSection = { section -> cancelarSesionSeccionTabletCompose(section) },
                    onSaveGeneral = { submit ->
                        aplicarInformacionGeneralDesdeCompose(submit)
                        guardarCambiosDesdeFormulario {
                            actualizarResumenTarjetasEdicion()
                            marcarGuardadoSeccionTabletCompose()
                        }
                    },
                    onSaveInventory = { submit ->
                        aplicarInventarioDesdeCompose(submit)
                        guardarCambiosDesdeFormulario {
                            actualizarResumenesStockTrasEdicion()
                            actualizarResumenTarjetasEdicion()
                            marcarGuardadoSeccionTabletCompose()
                        }
                    },
                    onAddPresentation = {
                        mostrarBottomSheetPresentacionCompacta(onComplete = { notificarRenderTabletCompose() })
                    },
                    onEditPresentation = { presentacionId ->
                        buscarPresentacionEnLista(presentacionId)?.let { presentacion ->
                            mostrarBottomSheetPresentacionCompacta(
                                presentacionEditar = presentacion,
                                onComplete = { notificarRenderTabletCompose() }
                            )
                        }
                    },
                    onDeletePresentation = { presentacionId ->
                        eliminarPresentacionDesdeSheet(presentacionId)
                        notificarRenderTabletCompose()
                    },
                    onSavePresentations = { submit ->
                        aplicarPresentacionesDesdeCompose(submit)
                        guardarCambiosDesdeFormulario {
                            actualizarResumenTarjetasEdicion()
                            marcarGuardadoSeccionTabletCompose()
                        }
                    },
                    onManageLots = { abrirLotesEnPanelTablet() },
                    onSaveOtherDetails = { submit ->
                        aplicarOtrosDetallesDesdeCompose(submit)
                        guardarCambiosDesdeFormulario {
                            actualizarResumenTarjetasEdicion()
                            marcarGuardadoSeccionTabletCompose()
                        }
                    },
                    onLotBack = { navegarAtrasLotesTablet() },
                    onLotSearchChange = {
                        tabletLotesSearchQuery = it
                        notificarRenderTabletCompose()
                    },
                    onLotItemClick = { key ->
                        tabletLotesSelectedKey = key
                        tabletLotesScreenActiva = LotesScreen.DETALLE
                        notificarRenderTabletCompose()
                    },
                    onLotEdit = {
                        tabletLotesScreenActiva = LotesScreen.FORMULARIO
                        notificarRenderTabletCompose()
                    },
                    onLotManualConsume = {
                        tabletLotesScreenActiva = LotesScreen.CONSUMO
                        notificarRenderTabletCompose()
                    },
                    onLotFormSave = { submit -> guardarLoteDesdePanelTablet(submit) },
                    onLotConsumeSave = { submit -> guardarConsumoLoteDesdePanelTablet(submit) }
                )
            }
        }
    }

    private fun construirResumenProductoTablet(): TabletProductSummaryModel {
        generalInfoState = obtenerGeneralInfoComposeSnapshot()
        otrosDetallesState = obtenerOtrosDetallesComposeSnapshot()
        presentacionesState = obtenerPresentacionesComposeSnapshot()
        productSummaryState = obtenerProductSummaryComposeSnapshot()

        return TabletProductSummaryModel(
            imageUrl = productSummaryState.imageUrl,
            imageUri = productSummaryState.imageUri,
            name = productSummaryState.name,
            category = productSummaryState.category,
            status = productSummaryState.status,
            vendibleChip = productSummaryState.vendibleChip,
            fisicoChip = productSummaryState.fisicoChip,
            vence = productSummaryState.vence,
            stockMinimo = productSummaryState.stockMinimo
        )
    }

    private fun obtenerProductoActualSeguro(): MoldeProductos {
        return try {
            productosEditTextGet()
        } catch (_: Exception) {
            productoOriginalParaAuditoria ?: MoldeProductos()
        }
    }

    private fun construirSeccionesTablet(): List<TabletSectionListItemModel> {
        return listOf(
            TabletSectionListItemModel(
                section = TabletEditSection.GENERAL_INFO,
                title = "Información General",
                subtitle = "Nombre, categoría y estado del producto",
                iconRes = R.drawable.ic_section_info_general
            ),
            TabletSectionListItemModel(
                section = TabletEditSection.INVENTORY,
                title = "Alerta de stock mínimo",
                subtitle = "Unidad, stock actual y mínimo de alerta",
                iconRes = R.drawable.ic_section_inventory_management
            ),
            TabletSectionListItemModel(
                section = TabletEditSection.PRESENTATIONS,
                title = "Presentaciones",
                subtitle = "Cajas, precios y disponibilidad",
                iconRes = R.drawable.ic_section_presentations
            ),
            TabletSectionListItemModel(
                section = TabletEditSection.EXPIRATION_LOTS,
                title = "Vencimientos y Lotes",
                subtitle = "Gestiona vencimientos y lotes del producto",
                iconRes = R.drawable.ic_section_other_details
            ),
        )
    }

    private fun construirEstadoLotesTablet(): TabletLotsPanelState {
        val screen = tabletLotesScreenActiva ?: return TabletLotsPanelState.Hidden
        val productoActual = try {
            productosEditTextGet()
        } catch (_: Exception) {
            productoOriginalParaAuditoria ?: MoldeProductos()
        }
        val unidadBase = formatearEtiquetaUi(
            binding.editTextUnidadBase.text?.toString()?.trim().orEmpty().ifBlank { "Unidad" }
        )
        val nombreProducto = binding.editTextNombre.text?.toString()?.trim().orEmpty().ifBlank { "Producto" }
        return when (screen) {
            LotesScreen.LISTA -> {
                val items = tabletLotesSnapshotUi.lotes
                    .filter { (clave, lote) ->
                        val filtro = tabletLotesSearchQuery.trim()
                        filtro.isBlank() ||
                            lote.numero.contains(filtro, ignoreCase = true) ||
                            clave.contains(filtro, ignoreCase = true)
                    }
                    .toSortedMap(compareBy<String> { clave ->
                        tabletLotesSnapshotUi.lotes[clave]?.vencimiento.orEmpty()
                    }.thenBy { it })
                    .map { (clave, lote) -> construirLotListUiItem(clave, lote) }
                TabletLotsPanelState.List(
                    productName = nombreProducto,
                    unitBase = unidadBase,
                    totalAvailable = "${ProductUtils.stockVendibleProducto(productoActual).toInt()} $unidadBase",
                    searchQuery = tabletLotesSearchQuery,
                    items = items
                )
            }

            LotesScreen.DETALLE -> {
                val clave = tabletLotesSelectedKey
                val lote = clave?.let { tabletLotesSnapshotUi.lotes[it] }
                if (clave == null || lote == null) {
                    TabletLotsPanelState.Hidden
                } else {
                    TabletLotsPanelState.Detail(construirDetalleLoteUi(clave, lote, unidadBase))
                }
            }

            LotesScreen.FORMULARIO -> {
                val clave = tabletLotesSelectedKey
                val lote = clave?.let { tabletLotesSnapshotUi.lotes[it] }
                if (clave == null || lote == null) {
                    TabletLotsPanelState.Hidden
                } else {
                    TabletLotsPanelState.Form(construirFormularioLoteUi(lote, tabletLotesSnapshotUi.lotes))
                }
            }

            LotesScreen.CONSUMO -> {
                val clave = tabletLotesSelectedKey
                val lote = clave?.let { tabletLotesSnapshotUi.lotes[it] }
                if (clave == null || lote == null) {
                    TabletLotsPanelState.Hidden
                } else {
                    TabletLotsPanelState.Consume(
                        ConsumeLotUiModel(
                            number = lote.numero.ifBlank { clave },
                            expiry = lote.vencimiento.ifBlank { "Sin fecha" },
                            availableText = ProductUtils.cantidadVendibleLote(lote).toInt().toString(),
                            unidadBase = unidadBase
                        )
                    )
                }
            }
        }
    }

    private fun abrirLotesEnPanelTablet() {
        cargarSnapshotLotes { snapshot ->
            tabletLotesSnapshotUi = snapshot
            tabletLotesSelectedKey = null
            tabletLotesSearchQuery = ""
            tabletLotesScreenActiva = LotesScreen.LISTA
            notificarRenderTabletCompose()
        }
    }

    private fun navegarAtrasLotesTablet() {
        when (tabletLotesScreenActiva) {
            LotesScreen.LISTA -> {
                tabletLotesScreenActiva = null
                tabletLotesSelectedKey = null
                tabletLotesSearchQuery = ""
            }
            LotesScreen.DETALLE -> tabletLotesScreenActiva = LotesScreen.LISTA
            LotesScreen.FORMULARIO -> tabletLotesScreenActiva = LotesScreen.DETALLE
            LotesScreen.CONSUMO -> tabletLotesScreenActiva = LotesScreen.DETALLE
            null -> Unit
        }
        notificarRenderTabletCompose()
    }

    private fun guardarLoteDesdePanelTablet(submit: LotFormSubmit): Boolean {
        val clave = tabletLotesSelectedKey
        val lote = clave?.let { tabletLotesSnapshotUi.lotes[it] } ?: return false
        return actualizarLoteDesdeSheet(clave, lote, tabletLotesSnapshotUi, submit) {
            cargarSnapshotLotes { snapshot ->
                tabletLotesSnapshotUi = snapshot
                tabletLotesScreenActiva = LotesScreen.DETALLE
                notificarRenderTabletCompose()
            }
        }
    }

    private fun guardarConsumoLoteDesdePanelTablet(submit: ConsumeLotSubmit): Boolean {
        val clave = tabletLotesSelectedKey
        val lote = clave?.let { tabletLotesSnapshotUi.lotes[it] } ?: return false
        return registrarConsumoManualLoteDesdeSheet(
            lote = lote,
            submit = submit,
            snapshot = tabletLotesSnapshotUi
        ) {
            cargarSnapshotLotes { snapshot ->
                tabletLotesSnapshotUi = snapshot
                tabletLotesScreenActiva = LotesScreen.DETALLE
                notificarRenderTabletCompose()
            }
        }
    }

    private fun sincronizarSesionSeccionTabletCompose(section: TabletEditSection) {
        if (!modoTabletDosPaneles && !modoMobileCompose) return
        if (seccionTabletComposeActiva == section) return

        if (!cambiosGuardadosEnSeccionTablet) {
            snapshotSeccionTabletActiva?.let { restaurarSnapshotSeccionEdicion(it) }
        }

        if (section != TabletEditSection.EXPIRATION_LOTS) {
            tabletLotesScreenActiva = null
            tabletLotesSelectedKey = null
            tabletLotesSearchQuery = ""
        }
        seccionTabletComposeActiva = section
        snapshotSeccionTabletActiva = capturarSnapshotSeccionEdicion()
        cambiosGuardadosEnSeccionTablet = false
    }

    private fun cancelarSesionSeccionTabletCompose(section: TabletEditSection) {
        if (!modoTabletDosPaneles && !modoMobileCompose) return
        if (seccionTabletComposeActiva != section) return
        if (!cambiosGuardadosEnSeccionTablet) {
            snapshotSeccionTabletActiva?.let { restaurarSnapshotSeccionEdicion(it) }
        }
        snapshotSeccionTabletActiva = capturarSnapshotSeccionEdicion()
        cambiosGuardadosEnSeccionTablet = false
        notificarRenderTabletCompose()
        ocultarTeclado()
    }

    private fun marcarGuardadoSeccionTabletCompose() {
        snapshotSeccionTabletActiva = capturarSnapshotSeccionEdicion()
        cambiosGuardadosEnSeccionTablet = false
        notificarRenderTabletCompose()
    }

    private fun notificarRenderTabletCompose() {
        if (!modoTabletDosPaneles && !modoMobileCompose) return
        tabletComposeRefreshState.intValue += 1
    }


    private fun obtenerProductSummaryComposeSnapshot(): ProductSummaryComposeSnapshot {
        val productoActual = obtenerProductoActualSeguro()

        val nombre = generalInfoState.productName.ifBlank {
            productoActual.nombre.trim().ifBlank { "Producto" }
        }

        val categoria = generalInfoState.category.ifBlank {
            productoActual.categoria.trim().ifBlank { "Sin categoría" }
        }

        val stockFisico = inventarioState.stockActual.toIntOrNull()
            ?: productoActual.cantidadinicial.trim().toIntOrNull()
            ?: 0

        val stockVendible = ProductUtils.stockVendibleProducto(productoActual).toInt().coerceAtLeast(0)

        val loteMasProximo = ProductUtils.obtenerLoteConVencimientoMasProximo(productoActual)

        val vencimiento = loteMasProximo?.vencimiento
            ?.takeIf { it.isNotBlank() }
            ?: ProductUtils.obtenerVencimientoGeneralVisible(productoActual).ifBlank { "No definido" }

        val stockMinimoTexto = if (binding.radioPorPaquetes.isChecked) {
            val valor = binding.editTextStockPresentacion.text?.toString()?.trim().orEmpty().ifBlank { "0" }
            val unit = PresentacionesTiendaConfigManager.nombreVisible(obtenerCanonicalPresentacionPrincipalActual())
                .ifBlank { "caja" }
            val suffix = if (valor == "1") unit else pluralizarTexto(unit)
            "$valor $suffix"
        } else {
            val valor = binding.editTextStock.text?.toString()?.trim().orEmpty().ifBlank { "0" }
            val unit = binding.editTextUnidadBase.text?.toString()?.trim().orEmpty().ifBlank { "Unidad" }
            val suffix = if (valor == "1") unit else pluralizarTexto(unit)
            "$valor $suffix"
        }

        return ProductSummaryComposeSnapshot(
            imageUrl = imagenUrlActual,
            imageUri = imagenUri?.toString() ?: cameraImageUri?.toString(),
            name = nombre,
            category = categoria,
            status = if (generalInfoState.isActive) "Activo" else "Inactivo",
            vendibleChip = "Vendible: $stockVendible",
            fisicoChip = "Físico: ${stockFisico.coerceAtLeast(0)}",
            vence = "Vence: $vencimiento",
            stockMinimo = "Stock mín: $stockMinimoTexto"
        )
    }

    private fun dpToPx(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun construirUiInventarioSheetModel(): InventoryEditorUiModel {
        return InventoryEditorUiModel(
            unitBase = inventarioState.unidadBase,
            stockActual = inventarioState.stockActual,
            stockMinActual = inventarioState.stockMinActual,
            stockMinInput = inventarioState.stockMinActual,
            stockMinSuffix = inventarioState.stockMinSuffix,
            helperText = "Mínimo recomendado para alertas de inventario"
        )
    }

    private var inventarioState = InventarioComposeSnapshot(
        unidadBase = "Unidad",
        stockActual = "0",
        stockMinActual = "0",
        usarPresentacion = false,
        stockMinSuffix = "unidades"
    )

    private fun capturarSnapshotSeccionEdicion(): SeccionEdicionSnapshot {
        return SeccionEdicionSnapshot(
            nombre = binding.editTextNombre.text?.toString()?.trim().orEmpty(),
            categoria = binding.editTextCategoria.text?.toString()?.trim().orEmpty(),
            requiereReceta = binding.swichtreceta.isChecked,
            estadoProducto = binding.switchestadodelproducto.isChecked,
            costoBase = binding.editTextCompra.text?.toString()?.trim().orEmpty(),
            stockMinimoUnidad = binding.editTextStock.text?.toString()?.trim().orEmpty(),
            stockMinimoPresentacion = binding.editTextStockPresentacion.text?.toString()?.trim().orEmpty(),
            vencimiento = binding.editTextVencimiento.text?.toString()?.trim().orEmpty(),
            tienePresentaciones = binding.switchPresentaciones.isChecked,
            presentacionPrincipalTexto = binding.editTextPresentacionPrincipal.text?.toString()?.trim().orEmpty(),
            presentacionPrincipalTag = (binding.editTextPresentacionPrincipal.tag as? String)?.trim().orEmpty(),
            presentacionPrincipalSeleccionManual = presentacionPrincipalElegidaManualmente,
            presentaciones = listaPresentaciones.map { it.copy() }
        )
    }

    private fun restaurarSnapshotSeccionEdicion(snapshot: SeccionEdicionSnapshot) {
        binding.editTextNombre.setText(snapshot.nombre)
        binding.editTextCategoria.setText(snapshot.categoria, false)
        binding.swichtreceta.isChecked = snapshot.requiereReceta
        binding.switchestadodelproducto.isChecked = snapshot.estadoProducto
        binding.editTextCompra.setText(snapshot.costoBase)
        binding.editTextStock.setText(snapshot.stockMinimoUnidad)
        binding.editTextStockPresentacion.setText(snapshot.stockMinimoPresentacion)
        binding.editTextVencimiento.setText(snapshot.vencimiento)
        binding.switchPresentaciones.isChecked = snapshot.tienePresentaciones
        binding.editTextPresentacionPrincipal.setText(snapshot.presentacionPrincipalTexto, false)
        binding.editTextPresentacionPrincipal.tag = snapshot.presentacionPrincipalTag.ifBlank { null }
        presentacionPrincipalElegidaManualmente = snapshot.presentacionPrincipalSeleccionManual

        listaPresentaciones.clear()
        listaPresentaciones.addAll(snapshot.presentaciones.map { it.copy() })

        actualizarOpcionesPresentacionPrincipal()
        actualizarResumenesStockTrasEdicion()
        verificarVencimiento()
        programarActualizacionUiFormulario()
    }

    private fun construirUiInformacionGeneralSheetModel(): GeneralInfoEditorUiModel {
        generalInfoState = obtenerGeneralInfoComposeSnapshot()

        return GeneralInfoEditorUiModel(
            productName = generalInfoState.productName,
            category = generalInfoState.category,
            categoryOptions = listaCategoria
                .mapNotNull { it.nombre.trim().takeIf(String::isNotBlank) }
                .distinct()
                .sorted(),
            requiresPrescription = generalInfoState.requiresPrescription,
            isActive = generalInfoState.isActive,
            nameHelper = generalInfoState.nameHelper
        )
    }

    private fun aplicarInformacionGeneralDesdeCompose(submit: GeneralInfoSubmit) {
        generalInfoState = generalInfoState.copy(
            productName = submit.productName,
            category = submit.category,
            requiresPrescription = submit.requiresPrescription,
            isActive = submit.isActive
        )

        binding.editTextNombre.setText(generalInfoState.productName)
        binding.editTextNombre.setSelection(binding.editTextNombre.text?.length ?: 0)

        actualizandoCategoriaProgramaticamente = true
        binding.editTextCategoria.setText(generalInfoState.category, false)
        binding.editTextCategoria.setSelection(binding.editTextCategoria.text?.length ?: 0)
        actualizandoCategoriaProgramaticamente = false

        actualizarSugerenciasCategoria(generalInfoState.category)

        binding.swichtreceta.isChecked = generalInfoState.requiresPrescription
        binding.switchestadodelproducto.isChecked = generalInfoState.isActive

        programarActualizacionUiFormulario()
    }

    private fun obtenerGeneralInfoComposeSnapshot(): GeneralInfoComposeSnapshot {
        val nombreActual = binding.editTextNombre.text?.toString()?.trim().orEmpty()
        val categoriaActual = binding.editTextCategoria.text?.toString()?.trim().orEmpty()
        val nombreOriginal = binding.editTextNombre.tag?.toString()?.trim().orEmpty()

        val helperNombre = when {
            nombreActual.isBlank() -> null
            nombreActual.equals(nombreOriginal, ignoreCase = true) -> null
            listaNombreAutoComplete.any { it.nombre.equals(nombreActual, ignoreCase = true) } ->
                getString(R.string.error_nombre_registrado)
            else -> {
                val similar = listaNombreAutoComplete.firstOrNull {
                    similar(it.nombre, nombreActual) &&
                            !it.nombre.equals(nombreOriginal, ignoreCase = true)
                }
                similar?.nombre?.let { getString(R.string.sugerencia_nombre_similar, it) }
            }
        }

        return GeneralInfoComposeSnapshot(
            productName = nombreActual,
            category = categoriaActual,
            requiresPrescription = binding.swichtreceta.isChecked,
            isActive = binding.switchestadodelproducto.isChecked,
            nameHelper = helperNombre
        )
    }

    private fun programarActualizacionUiFormulario() {
        runnableActualizacionUiFormulario?.let { binding.root.removeCallbacks(it) }
        val runnable = Runnable {
            actualizarEstadoBotonGuardar()
        }
        runnableActualizacionUiFormulario = runnable
        binding.root.post(runnable)
    }

    private fun configurarBackdropBottomSheet(dialog: BottomSheetDialog) {
        if (!modoTabletDosPaneles) {
            cantidadBottomSheetsAbiertos += 1
            actualizarVisibilidadToolbarPorBottomSheets()
            dialog.setOnDismissListener {
                cantidadBottomSheetsAbiertos = (cantidadBottomSheetsAbiertos - 1).coerceAtLeast(0)
                actualizarVisibilidadToolbarPorBottomSheets()
            }
        }
        dialog.window?.let { window ->
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setDimAmount(0.58f)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window.setBackgroundBlurRadius(36)
            }
        }
    }

    private fun actualizarVisibilidadToolbarPorBottomSheets() {
        if (modoTabletDosPaneles) {
            binding.materialToolbar3.visibility = View.VISIBLE
            binding.materialToolbar3.alpha = 1f
            return
        }
        val overlayActivo = cantidadBottomSheetsAbiertos > 0 || mobileComposeFormularioAbierto
        binding.materialToolbar3.visibility = View.VISIBLE
        binding.materialToolbar3.alpha = if (overlayActivo) 0f else 1f
        binding.materialToolbar3.setBackgroundColor(
            if (overlayActivo) Color.WHITE else "#F4F7F6".toColorInt()
        )
        binding.materialToolbar3.isEnabled = !overlayActivo
    }

    private fun actualizarModoFormularioMobileCompose(abierta: Boolean) {
        if (!modoMobileCompose) return
        if (mobileComposeFormularioAbierto == abierta) return
        mobileComposeFormularioAbierto = abierta
        val params = mobileComposeContainer?.layoutParams as? ConstraintLayout.LayoutParams
        if (params != null) {
            TransitionManager.beginDelayedTransition(
                binding.main,
                AutoTransition().apply { duration = 180L }
            )
            if (abierta) {
                params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                params.topToBottom = ConstraintLayout.LayoutParams.UNSET
            } else {
                params.topToTop = ConstraintLayout.LayoutParams.UNSET
                params.topToBottom = binding.materialToolbar3.id
            }
            mobileComposeContainer?.layoutParams = params
            mobileComposeContainer?.requestLayout()
        }
        actualizarVisibilidadToolbarPorBottomSheets()
    }

    private fun obtenerSnapshotPresentacionesSeccion(): String {
        val principal = obtenerCanonicalPresentacionPrincipalActual().trim()
        val items = listaPresentaciones
            .sortedBy { PresentacionHelper.normalizarClave(it.nombre) }
            .joinToString("|") { presentacion ->
                val precio = String.format(Locale.US, "%.2f", presentacion.precioventa)
                "${PresentacionHelper.normalizarClave(presentacion.nombre)}:${presentacion.cantidad}:$precio"
            }
        return "$principal||$items"
    }

    private fun obtenerPresentacionesComposeSnapshot(): PresentacionesComposeSnapshot {
        return PresentacionesComposeSnapshot(
            productName = binding.editTextNombre.text?.toString()?.trim().orEmpty().ifBlank { "Producto" },
            principalId = obtenerCanonicalPresentacionPrincipalActual().trim(),
            presentaciones = listaPresentaciones.map { it.copy() },
            snapshot = obtenerSnapshotPresentacionesSeccion()
        )
    }

    private fun construirUiPresentacionesSheetModel(snapshotInicial: String): PresentacionesEditorUiModel {
        presentacionesState = obtenerPresentacionesComposeSnapshot()
        val principal = obtenerPresentacionPrincipalActualDesdeLista()
            ?: obtenerPresentacionMayorMontoVenta()
            ?: listaPresentaciones.firstOrNull()

        val items = listaPresentaciones
            .sortedWith(
                compareByDescending<PresentacionProducto> {
                    principal?.let { principalActual ->
                        PresentacionHelper.sonNombresEquivalentes(it.nombre, principalActual.nombre)
                    } == true
                }.thenByDescending { it.cantidad }
            )
            .map { presentacion ->
                PresentacionUiItem(
                    id = presentacion.nombre,
                    name = construirTituloPresentacionSheet(presentacion),
                    detail = construirDetallePresentacionLigero(presentacion),
                    price = MonedaHelper.formatear(presentacion.precioventa),
                    iconRes = resolverIconoPresentacionMovimiento(
                        PresentacionesTiendaConfigManager.nombreVisible(presentacion.nombre)
                    ),
                    isPrincipal = principal?.let {
                        PresentacionHelper.sonNombresEquivalentes(it.nombre, presentacion.nombre)
                    } == true,
                    principalDetail = formatearEquivalenciaPresentacionCompacta(presentacion)
                )
            }

        val principalItem = items.firstOrNull { it.isPrincipal } ?: items.firstOrNull()

        return PresentacionesEditorUiModel(
            productName = presentacionesState.productName,
            productSubtitle = "Gestiona presentaciones y precios de venta",
            principal = principalItem,
            items = items,
            showPrincipalSelector = presentacionesState.presentaciones.size > 1,
            note = "Los cambios de equivalencia afectan el stock vendible",
            saveEnabled = presentacionesState.snapshot != snapshotInicial
        )
    }

    private fun obtenerOtrosDetallesComposeSnapshot(): OtrosDetallesComposeSnapshot {
        return OtrosDetallesComposeSnapshot(
            productName = binding.editTextNombre.text?.toString()?.trim().orEmpty().ifBlank { "Producto" },
            expiryInput = binding.editTextVencimiento.text?.toString()?.trim().orEmpty()
        )
    }

    private fun construirDetallePresentacionLigero(presentacion: PresentacionProducto): String {
        val unidadBase = binding.editTextUnidadBase.text?.toString()?.trim().orEmpty().ifBlank { "unidad" }
        return if (presentacion.cantidad <= 1) {
            "Equivale a 1 ${unidadBase.lowercase(Locale.getDefault())}"
        } else {
            "Equivale a ${presentacion.cantidad} ${pluralizarTexto(unidadBase.lowercase(Locale.getDefault()))}"
        }
    }

    private fun construirTituloPresentacionSheet(presentacion: PresentacionProducto): String {
        val visible = PresentacionesTiendaConfigManager.nombreVisible(presentacion.nombre)
        val sufijoUnidad = abreviarUnidadPresentacionSheet(
            binding.editTextUnidadBase.text?.toString()?.trim().orEmpty()
        )
        return if (presentacion.cantidad <= 1) {
            "$visible (1 $sufijoUnidad)"
        } else {
            "$visible (${presentacion.cantidad} $sufijoUnidad)"
        }
    }

    private fun abreviarUnidadPresentacionSheet(unidadBase: String): String {
        val normalizada = unidadBase.trim().lowercase(Locale.getDefault())
        return when {
            normalizada.isBlank() -> "u"
            normalizada == "unidad" || normalizada == "unidades" -> "u"
            normalizada == "ml" -> "mL"
            normalizada == "l" || normalizada == "lt" || normalizada == "litro" || normalizada == "litros" -> "L"
            normalizada == "g" || normalizada == "gr" || normalizada == "gramo" || normalizada == "gramos" -> "g"
            normalizada == "kg" || normalizada == "kilo" || normalizada == "kilos" -> "kg"
            else -> unidadBase.trim()
        }
    }

    private fun eliminarPresentacionDesdeSheet(presentacionId: String) {
        if (listaPresentaciones.size <= 1) {
            Toast.makeText(
                this,
                "No puedes eliminar la última presentación. Agrega otra antes de borrarla.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val presentacion = buscarPresentacionEnLista(presentacionId) ?: return
        val eraPrincipal = PresentacionHelper.sonNombresEquivalentes(
            obtenerCanonicalPresentacionPrincipalActual(),
            presentacion.nombre
        )

        listaPresentaciones.remove(presentacion)
        if (eraPrincipal) {
            val reemplazo = obtenerPresentacionMayorMontoVenta() ?: listaPresentaciones.firstOrNull()
            if (reemplazo != null) {
                asignarPresentacionPrincipal(reemplazo.nombre, seleccionManual = true)
            }
        }
        actualizarEstadoBotonGuardar()
    }

    private fun aplicarPresentacionesDesdeCompose(submit: PresentacionesSubmit) {
        val principalId = submit.principalId?.takeIf { it.isNotBlank() } ?: return

        presentacionesState = presentacionesState.copy(
            principalId = principalId
        )

        asignarPresentacionPrincipal(principalId, seleccionManual = true)
        actualizarEstadoBotonGuardar()
    }

    private fun aplicarInventarioDesdeCompose(submit: InventoryEditSubmit) {
        inventarioState = inventarioState.copy(
            stockMinActual = submit.stockMinInput
        )

        // sincronizas con XML (temporal)
        if (inventarioState.usarPresentacion) {
            binding.editTextStockPresentacion.setText(inventarioState.stockMinActual)
        } else {
            binding.editTextStock.setText(inventarioState.stockMinActual)
        }

        actualizarResumenesStockTrasEdicion()
        programarActualizacionUiFormulario()
    }

    private fun construirUiOtrosDetallesSheetModel(): OtherDetailsUiModel {
        otrosDetallesState = obtenerOtrosDetallesComposeSnapshot()
        val productoActual = try {
            productosEditTextGet()
        } catch (_: Exception) {
            productoOriginalParaAuditoria ?: MoldeProductos()
        }
        val cantidadLotes = ProductUtils.cantidadLotesRegistrados(productoActual)
        val manejaLotes = cantidadLotes > 0
        val loteMasProximo = ProductUtils.obtenerLoteConVencimientoMasProximo(productoActual)
        val estado = when (ProductUtils.obtenerEstadoVencimiento(productoActual)) {
            "VENCIDO" -> "Vencido"
            "POR_VENCER" -> "Por vencer"
            else -> "Disponible"
        }
        val vencimientoResumen = loteMasProximo?.vencimiento
            ?.takeIf { it.isNotBlank() }
            ?: ProductUtils.obtenerVencimientoGeneralVisible(productoActual).ifBlank { "Sin fecha" }
        val resolucionConsumo = if (manejaLotes) resolverConsumoLotesActual(productoActual.lotes) else null
        val loteEnConsumo = resolucionConsumo?.loteActual ?: resolucionConsumo?.loteRecomendadoFefo
        val resumenLabel = if (manejaLotes) {
            if (resolucionConsumo?.usaSeleccionManual == true) {
                "Lote seleccionado para consumo"
            } else {
                "Lote en consumo actual"
            }
        } else {
            "Próximo vencimiento"
        }
        val resumenValue = if (manejaLotes) {
            loteEnConsumo?.second?.numero?.trim()
                ?.ifBlank { loteEnConsumo.first }
                ?: "Sin lote disponible"
        } else {
            vencimientoResumen
        }
        val resumenStatus = if (manejaLotes) {
            estadoResumenLote(loteEnConsumo?.second)
        } else {
            estado
        }
        val resumenHelper = if (manejaLotes) {
            when {
                loteEnConsumo == null -> "No hay lotes vendibles para consumo"
                resolucionConsumo?.usaSeleccionManual == true -> {
                    val vencimiento = loteEnConsumo.second.vencimiento.trim()
                    if (vencimiento.isNotBlank()) {
                        "Selección manual · vence $vencimiento"
                    } else {
                        "Selección manual sin vencimiento registrado"
                    }
                }
                else -> {
                    val vencimiento = loteEnConsumo.second.vencimiento.trim()
                    if (vencimiento.isNotBlank()) {
                        "FEFO automático · vence $vencimiento"
                    } else {
                        "FEFO automático · sin vencimiento registrado"
                    }
                }
            }
        } else {
            "Fecha general del producto"
        }

        return OtherDetailsUiModel(
            productName = otrosDetallesState.productName,
            managesLots = manejaLotes,
            summaryLabel = resumenLabel,
            summaryValue = resumenValue,
            summaryStatus = resumenStatus,
            summaryHelper = resumenHelper,
            lotsCount = cantidadLotes,
            expiryInput = otrosDetallesState.expiryInput,
            expiryHelper = "Fecha general del producto en formato MM/AA"
        )
    }

    private fun estadoResumenLote(lote: LoteProducto?): String {
        lote ?: return "Sin stock"
        if (ProductUtils.cantidadVendibleLote(lote).toInt() <= 0) return "Sin stock"
        val dias = ProductUtils.diasHastaVencerLote(lote.vencimiento.trim())
        return when {
            dias == null -> "Disponible"
            dias < 0 -> "Vencido"
            dias <= 30 -> "Por vencer"
            else -> "Disponible"
        }
    }

    private fun aplicarOtrosDetallesDesdeCompose(submit: OtherDetailsSubmit) {
        otrosDetallesState = otrosDetallesState.copy(
            expiryInput = submit.expiryInput
        )

        binding.editTextVencimiento.setText(otrosDetallesState.expiryInput)
        binding.editTextVencimiento.setSelection(binding.editTextVencimiento.text?.length ?: 0)

        verificarVencimiento()
        programarActualizacionUiFormulario()
    }

    private fun cargarSnapshotLotes(onLoaded: (LotesSheetSnapshot) -> Unit) {
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRODUCTOS)
            .child(indiceOriginal)
            .get()
            .addOnSuccessListener { snapshot ->
                loteConsumoSeleccionadoActual = snapshot.child("loteConsumoSeleccionado")
                    .value?.toString().orEmpty().trim()
                loteConsumoSeleccionManualActual = snapshot.child("loteConsumoSeleccionManual")
                    .getValue(Boolean::class.java) == true

                val lotes = lotesDesdeSnapshot(snapshot.child("lotes"))
                sincronizarProductoOriginalDesdeSnapshot(snapshot)
                actualizarVisibilidadVencimientoGeneral(lotes.values.count { it.numero.trim().isNotBlank() })
                val productoActual = productoOriginalParaAuditoria
                if (productoActual != null) {
                    binding.editTextVencimiento.setText(ProductUtils.obtenerVencimientoGeneralVisible(productoActual))
                }
                verificarVencimiento()
                actualizarResumenTarjetasEdicion()

                onLoaded(
                    LotesSheetSnapshot(
                        lotes = lotes,
                        loteConsumoSeleccionado = loteConsumoSeleccionadoActual,
                        loteConsumoSeleccionManual = loteConsumoSeleccionManualActual
                    )
                )
            }
            .addOnFailureListener {
                onLoaded(
                    LotesSheetSnapshot(
                        lotes = productoOriginalParaAuditoria?.lotes.orEmpty(),
                        loteConsumoSeleccionado = loteConsumoSeleccionadoActual,
                        loteConsumoSeleccionManual = loteConsumoSeleccionManualActual
                    )
                )
            }
    }


    private fun construirLotListUiItem(clave: String, lote: LoteProducto): LotListUiItem {
        val estado = obtenerEstadoVisualLote(lote)
        val cantidadDisponible = ProductUtils.cantidadVendibleLote(lote).toInt()
        return LotListUiItem(
            key = clave,
            number = lote.numero.ifBlank { clave },
            status = estado,
            expiry = lote.vencimiento.ifBlank { "Sin fecha" },
            availableText = cantidadDisponible.toString(),
            ingresoDate = lote.fecha.ifBlank { "Sin registro" },
            secondaryText = "Vence: ${lote.vencimiento.ifBlank { "Sin fecha" }} · Disponibles: $cantidadDisponible"
        )
    }

    private fun construirDetalleLoteUi(clave: String, lote: LoteProducto, unidadBase: String): LotDetailUiModel {
        return LotDetailUiModel(
            key = clave,
            number = lote.numero.ifBlank { clave },
            status = obtenerEstadoVisualLote(lote),
            expiry = lote.vencimiento.ifBlank { "Sin fecha" },
            ingresoDate = lote.fecha.ifBlank { "Sin registro" },
            cantidadIngresada = "${ProductUtils.cantidadFisicaLote(lote).toInt()} $unidadBase",
            cantidadDisponible = "${ProductUtils.cantidadVendibleLote(lote).toInt()} $unidadBase",
            unidadBase = unidadBase,
            costoUnitario = MonedaHelper.formatear(lote.costoCompraUnitario),
            observaciones = lote.observaciones.ifBlank { "Sin observaciones" }
        )
    }

    private fun construirFormularioLoteUi(lote: LoteProducto?, lotes: Map<String, LoteProducto>): LotFormUiModel {
        val numeroActual = lote?.numero?.trim()?.uppercase(Locale.getDefault()).orEmpty()
        return LotFormUiModel(
            title = if (lote == null) "Nuevo lote" else "Editar lote",
            number = lote?.numero.orEmpty(),
            expiry = lote?.vencimiento.orEmpty(),
            cantidadIngresada = lote?.let { ProductUtils.cantidadFisicaLote(it).toInt().toString() }.orEmpty(),
            cantidadDisponible = lote?.let { ProductUtils.cantidadVendibleLote(it).toInt().toString() }.orEmpty(),
            costoUnitario = lote?.costoCompraUnitario?.takeIf { it > 0.0 }?.toString().orEmpty(),
            observaciones = lote?.observaciones.orEmpty(),
            existingLotNumbers = lotes.values
                .map { it.numero.trim().uppercase(Locale.getDefault()) }
                .filter { it.isNotBlank() && it != numeroActual }
                .toSet()
        )
    }

    private fun obtenerEstadoVisualLote(lote: LoteProducto): String {
        val disponibles = ProductUtils.cantidadVendibleLote(lote).toInt()
        val dias = diasHastaVencerLote(lote.vencimiento)
        return when {
            disponibles <= 0 -> "Sin stock"
            dias == null -> "Disponible"
            dias < 0 -> "Vencido"
            dias <= 30 -> "Por vencer"
            else -> "Disponible"
        }
    }

    private fun registrarNuevoLoteDesdeSheet(
        submit: LotFormSubmit,
        snapshot: LotesSheetSnapshot,
        onSuccess: () -> Unit
    ): Boolean {
        val numero = submit.number.trim().uppercase(Locale.getDefault())
        val vencimiento = submit.expiry.trim()
        val cantidadIngresada = submit.cantidadIngresada.trim().toIntOrNull() ?: 0
        val cantidadDisponible = (submit.cantidadDisponible.trim().toIntOrNull() ?: cantidadIngresada)
            .coerceIn(0, cantidadIngresada)
        val costoUnitario = submit.costoUnitario.trim().replace(",", ".").toDoubleOrNull() ?: 0.0

        if (numero.isBlank()) {
            Toast.makeText(this, "Ingresa el número de lote", Toast.LENGTH_LONG).show()
            return false
        }
        if (cantidadIngresada <= 0) {
            Toast.makeText(this, "Ingresa una cantidad válida", Toast.LENGTH_LONG).show()
            return false
        }

        if (snapshot.lotes.values.any { it.numero.trim().equals(numero, ignoreCase = true) }) {
            Toast.makeText(this, "Ese lote ya existe. Usa otro diferente.", Toast.LENGTH_LONG).show()
            return false
        }

        mostrarCarga(true, titulo = "Guardando cambios", mensaje = "Actualizando lote...")
        registrarIngresoStock(
            cantidadIngresada = cantidadIngresada,
            costoIngresoTotal = costoUnitario * cantidadIngresada,
            motivo = "Registro manual de lote",
            documento = "",
            nombreUnidadIngreso = binding.editTextUnidadBase.text?.toString()?.trim().orEmpty().ifBlank { "unidad" },
            unidadesPorIngreso = 1,
            fuePorPresentacion = false,
            numeroLote = numero,
            vencimientoLote = vencimiento,
            lotesExistentes = snapshot.lotes.values.toList(),
            requiereLote = true,
            requiereVencimientoLote = false
        ) {
            resolverClaveLoteExistentePorNumero(numero) { clave ->
                if (clave.isNullOrBlank()) {
                    onSuccess()
                    return@resolverClaveLoteExistentePorNumero
                }
                val updates = mutableMapOf<String, Any>(
                    "cantidad" to cantidadDisponible.toDouble(),
                    "cantidadBloqueada" to (cantidadIngresada - cantidadDisponible).coerceAtLeast(0).toDouble(),
                    "costoCompraUnitario" to costoUnitario,
                    "costoUltimoIngresoUnitario" to costoUnitario,
                    "observaciones" to submit.observaciones.trim()
                )
                FirebaseDatabase.getInstance()
                    .getReference(PATH_INVENTARIO)
                    .child(PATH_PRODUCTOS)
                    .child(indiceOriginal)
                    .child("lotes")
                    .child(clave)
                    .updateChildren(updates)
                    .addOnSuccessListener {
                        mostrarCarga(false)
                        onSuccess()
                    }
                    .addOnFailureListener {
                        mostrarCarga(false)
                        Toast.makeText(this, "Lote guardado con datos parciales", Toast.LENGTH_LONG).show()
                        onSuccess()
                    }
            }
        }
        return true
    }

    private fun actualizarLoteDesdeSheet(
        clave: String,
        loteActual: LoteProducto,
        snapshot: LotesSheetSnapshot,
        submit: LotFormSubmit,
        onSuccess: () -> Unit
    ): Boolean {
        val numero = submit.number.trim().uppercase(Locale.getDefault())
        val vencimiento = submit.expiry.trim()
        val cantidadIngresada = submit.cantidadIngresada.trim().toIntOrNull() ?: 0
        val cantidadDisponible = (submit.cantidadDisponible.trim().toIntOrNull() ?: cantidadIngresada)
            .coerceIn(0, cantidadIngresada)
        val costoUnitario = submit.costoUnitario.trim().replace(",", ".").toDoubleOrNull() ?: 0.0

        if (numero.isBlank()) {
            Toast.makeText(this, "Ingresa el número de lote", Toast.LENGTH_LONG).show()
            return false
        }
        if (cantidadIngresada < 0) {
            Toast.makeText(this, "La cantidad no puede ser negativa", Toast.LENGTH_LONG).show()
            return false
        }
        if (snapshot.lotes.any { (otraClave, lote) ->
                otraClave != clave && lote.numero.trim().equals(numero, ignoreCase = true)
            }
        ) {
            Toast.makeText(this, "Ese lote ya existe. Usa otro diferente.", Toast.LENGTH_LONG).show()
            return false
        }

        val stockActual = binding.editTextCantidad.text?.toString()?.trim()?.toIntOrNull() ?: 0
        val stockSinLote = stockActual - ProductUtils.cantidadFisicaLote(loteActual).toInt()
        val stockNuevo = (stockSinLote + cantidadIngresada).coerceAtLeast(0)

        val updates = mutableMapOf<String, Any>(
            "numero" to numero,
            "vencimiento" to vencimiento,
            "cantidad" to cantidadDisponible.toDouble(),
            "cantidadBloqueada" to (cantidadIngresada - cantidadDisponible).coerceAtLeast(0).toDouble(),
            "costoCompraUnitario" to costoUnitario,
            "costoUltimoIngresoUnitario" to costoUnitario,
            "observaciones" to submit.observaciones.trim()
        )

        val rootRef = FirebaseDatabase.getInstance().reference
        val totalUpdates = mutableMapOf<String, Any?>()
        val prodActual = obtenerProductoActualSeguro()
        
        // 1. Datos del producto
        totalUpdates["$PATH_INVENTARIO/$PATH_PRODUCTOS/$indiceOriginal/cantidadinicial"] = stockNuevo.toString()
        
        // 2. Datos del lote
        updates.forEach { (k, v) ->
            totalUpdates["$PATH_INVENTARIO/$PATH_PRODUCTOS/$indiceOriginal/lotes/$clave/$k"] = v
            totalUpdates["${DbPaths.INVENTARIO_PRODUCTO_LOTES}/$indiceOriginal/$clave/$k"] = v
        }
        totalUpdates["${DbPaths.INVENTARIO_PRODUCTO_LOTES}/$indiceOriginal/$clave/loteId"] = clave
        totalUpdates["${DbPaths.INVENTARIO_PRODUCTO_LOTES}/$indiceOriginal/$clave/lotePath"] =
            "${DbPaths.INVENTARIO_PRODUCTO_LOTES}/$indiceOriginal/$clave"

        // 3. Índice plano de lotes (V17.46)
        val numLote = (updates["numero"] as? String)?.trim()?.uppercase()
        if (numLote != null && numLote.isNotBlank()) {
            val keyLote = ProductUtils.encodeLotKey(numLote)
            val registroLote = LoteIndexado(
                numero = numLote,
                productoId = indiceOriginal,
                productoNombre = prodActual.nombre.trim(),
                loteId = clave,
                lotePath = "${DbPaths.INVENTARIO_PRODUCTO_LOTES}/$indiceOriginal/$clave",
                vencimiento = (updates["vencimiento"] as? String).orEmpty()
            )
            totalUpdates["${DbPaths.INVENTARIO_LOTES_POR_NUMERO}/$keyLote"] = registroLote
        }

        mostrarCarga(true, titulo = "Guardando cambios", mensaje = "Actualizando lote...")
        rootRef.updateChildren(totalUpdates)
            .addOnSuccessListener {
                actualizarCamposStockDespuesDeAjuste(
                    stockDespues = stockNuevo,
                    fuePorPresentacion = false,
                    unidadesPorPresentacionSeleccionada = 1
                )
                actualizarResumenTarjetasEdicion()
                mostrarCarga(false)
                Toast.makeText(this, "Lote actualizado", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
            .addOnFailureListener {
                mostrarCarga(false)
                Toast.makeText(this, "No se pudo sincronizar el lote", Toast.LENGTH_LONG).show()
            }
        return true
    }

    private fun registrarConsumoManualLoteDesdeSheet(
        lote: LoteProducto,
        submit: ConsumeLotSubmit,
        snapshot: LotesSheetSnapshot,
        onSuccess: () -> Unit
    ): Boolean {
        val cantidad = submit.cantidad.trim().toIntOrNull() ?: 0
        if (cantidad <= 0) {
            Toast.makeText(this, "Ingresa una cantidad válida", Toast.LENGTH_LONG).show()
            return false
        }

        mostrarCarga(true, titulo = "Guardando cambios", mensaje = "Registrando consumo...")
        aplicarAjusteStock(
            cantidadAjuste = cantidad,
            esSuma = false,
            motivo = submit.motivo.trim().ifBlank { "Consumo manual de lote" },
            nombreUnidadAjuste = binding.editTextUnidadBase.text?.toString()?.trim().orEmpty().ifBlank { "unidad" },
            unidadesPorAjuste = 1,
            fuePorPresentacion = false,
            numeroLote = lote.numero,
            lotesExistentes = snapshot.lotes.values.toList()
        ) {
            mostrarCarga(false)
            onSuccess()
        }
        return true
    }

    private fun actualizarResumenTarjetasEdicion() {
        inventarioState = obtenerInventarioComposeSnapshot()

        val nombre = binding.editTextNombre.text?.toString()?.trim().orEmpty().ifBlank { "Sin nombre" }
        binding.tvHeroProductName.text = nombre
        binding.tvHeroCategory.text = binding.editTextCategoria.text?.toString()?.trim().orEmpty().ifBlank { "Sin categoría" }
        binding.tvHeroEstado.text = if (binding.switchestadodelproducto.isChecked) "Activo" else "Inactivo"
        val categoria = binding.editTextCategoria.text?.toString()?.trim().orEmpty().ifBlank { "Sin categoría" }
        binding.tvInfoGeneralSummary.text = "Nombre: $nombre\nCategoría: $categoria"

        val unidadBase = formatearEtiquetaUi(
            binding.editTextUnidadBase.text?.toString()?.trim().orEmpty().ifBlank { "Unidad" }
        )
        val stockActual = binding.editTextCantidad.text?.toString()?.trim().orEmpty().ifBlank { "0" }
        
        val stockMinimoTexto = if (binding.radioPorPaquetes.isChecked) {
            val valor = binding.editTextStockPresentacion.text?.toString()?.trim().orEmpty().ifBlank { "0" }
            val unit = PresentacionesTiendaConfigManager.nombreVisible(obtenerCanonicalPresentacionPrincipalActual())
                .ifBlank { "caja" }
            val suffix = if (valor == "1") unit else pluralizarTexto(unit)
            "$valor $suffix"
        } else {
            val valor = binding.editTextStock.text?.toString()?.trim().orEmpty().ifBlank { "0" }
            val suffix = if (valor == "1") unidadBase else pluralizarTexto(unidadBase)
            "$valor $suffix"
        }
        
        binding.tvInventarioSummary.text = "Unidad: $unidadBase\nStock: $stockActual • Mínimo: $stockMinimoTexto"

        val resumenPresentaciones = when {
            !binding.switchPresentaciones.isChecked || listaPresentaciones.isEmpty() -> "Sin presentaciones"
            listaPresentaciones.size == 1 -> {
                val unica = listaPresentaciones.first()
                "${PresentacionesTiendaConfigManager.nombreVisible(unica.nombre)} · ${MonedaHelper.formatear(unica.precioventa)}"
            }
            else -> "${listaPresentaciones.size} presentaciones"
        }
        binding.tvPresentacionesSummary.text = resumenPresentaciones
        binding.root.findViewById<TextView>(R.id.tvNombreProductoPresentaciones)?.text = nombre
        //binding.tvHeroStockMinimo.text = "Stock mín: $stockMinimo"

        val productoActual = try {
            productosEditTextGet()
        } catch (_: Exception) {
            productoOriginalParaAuditoria ?: MoldeProductos()
        }
        val cantidadLotes = ProductUtils.cantidadLotesRegistrados(productoActual)
        val loteProximo = ProductUtils.obtenerLoteConVencimientoMasProximo(productoActual)
        val vencimiento = loteProximo?.vencimiento
            ?.takeIf { it.isNotBlank() }
            ?: binding.editTextVencimiento.text?.toString()?.trim().orEmpty().ifBlank { "No definido" }
        binding.tvOtrosDetallesSummary.text = if (cantidadLotes > 0) {
            "Próximo vencimiento: $vencimiento\n$cantidadLotes lote${if (cantidadLotes == 1) "" else "s"} registrado${if (cantidadLotes == 1) "" else "s"}"
        } else {
            "Vencimiento general: $vencimiento\nSin lotes registrados"
        }

        notificarRenderTabletCompose()
    }




    // ---------------------------------------------------------------------------------------------
    // BACK: si hay un dropdown abierto, solo lo cerramos (no cerramos la Activity)
    // ---------------------------------------------------------------------------------------------

    private fun instalarBackHandlerDropdowns() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (cerrarDropdownsSiEstanAbiertos()) return
                finish()
            }
        })
    }

    private fun cerrarDropdownsSiEstanAbiertos(): Boolean {
        val campos = listOf(
            binding.editTextCategoria,
            binding.editTextNombre,
            binding.editTextUnidadBase,
            binding.editTextPresentacionPrincipal
        )

        var consumioBack = false
        campos.forEach { campo ->
            if (campo.isPopupShowing) {
                campo.dismissDropDown()
                consumioBack = true
            }
            if (campo.hasFocus()) {
                campo.clearFocus()
                consumioBack = true
            }
        }

        if (consumioBack) {
            binding.main.requestFocus()
            ocultarTeclado()
        }

        return consumioBack
    }

    private fun configurarBotonesAccion() {
        binding.materialButton3.setOnClickListener {
            GuardarCambiosClickBoton(it)
        }
        binding.layoutBotonesInferiores.visibility = View.GONE
        binding.materialButtonborrar.setOnClickListener {
            borrarProductoClick(it)
        }
        binding.btnQuickIngresarStock.setOnClickListener {
            binding.btnIngresarStock.performClick()
        }
        binding.btnQuickEgresoStock.setOnClickListener {
            binding.btnEgresoStock.performClick()
        }
        binding.btnQuickAjustarStock.setOnClickListener {
            binding.btnAjustarStock.performClick()
        }
        actualizarResumenTarjetasEdicion()
    }

    private fun aplicarEstadoBotonGuardadoMaterial(button: MaterialButton, enabled: Boolean) {
        button.isEnabled = enabled
        button.backgroundTintList = ColorStateList.valueOf(
            if (enabled) "#14935C".toColorInt() else "#D0D5DD".toColorInt()
        )
        button.setTextColor(
            if (enabled) Color.WHITE else "#FFFFFF".toColorInt()
        )
        button.alpha = 1f
    }

    private fun hayCambiosPendientesFormulario(): Boolean {
        return !cargandoDatosIniciales && obtenerSnapshotFormularioActual() != estadoInicialFormulario
    }

    private fun guardarCambiosDesdeFormulario(onSuccess: (() -> Unit)? = null) {
        accionDespuesDeGuardarProducto = onSuccess

        if (!hayCambiosPendientesFormulario()) {
            accionDespuesDeGuardarProducto = null
            Toast.makeText(this, "No hay cambios para guardar", Toast.LENGTH_SHORT).show()
            return
        }

        if (!validarTipoBaseInventarioInmutable()) {
            accionDespuesDeGuardarProducto = null
            return
        }
        if (!validarCampos()) {
            accionDespuesDeGuardarProducto = null
            return
        }
        if (!validarPresentacionesAntesDeGuardar()) {
            accionDespuesDeGuardarProducto = null
            return
        }

        mostrarResumenConfirmacionEditar()
    }


    private fun configurarBotonGuardarPorCambios() {
        aplicarEstadoBotonGuardadoMaterial(binding.materialButton3, enabled = false)

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                actualizarEstadoBotonGuardar()
            }

            override fun afterTextChanged(s: Editable?) = Unit
        }

        binding.editTextNombre.addTextChangedListener(watcher)
        binding.editTextCategoria.addTextChangedListener(watcher)
        binding.editTextCompra.addTextChangedListener(watcher)
        binding.editTextCantidad.addTextChangedListener(watcher)
        binding.editTextStock.addTextChangedListener(watcher)
        binding.editCantidadPresentacion.addTextChangedListener(watcher)
        binding.editUnidadesPorCaja.addTextChangedListener(watcher)
        binding.editTextStockPresentacion.addTextChangedListener(watcher)
        binding.editTextUnidadBase.addTextChangedListener(watcher)
        binding.editTextPresentacionPrincipal.addTextChangedListener(watcher)
        binding.editTextVencimiento.addTextChangedListener(watcher)

        binding.swichtreceta.setOnCheckedChangeListener { _, isChecked ->
            actualizarEstiloTarjetaToggleReceta(isChecked)
            actualizarEstadoBotonGuardar()
        }
        // Click en cualquier parte de la tarjeta también alterna el switch
        binding.cardToggleReceta?.setOnClickListener {
            binding.swichtreceta.isChecked = !binding.swichtreceta.isChecked
        }

        binding.switchestadodelproducto.setOnCheckedChangeListener { _, isChecked ->
            actualizarEstiloTarjetaToggleActivo(isChecked)
            actualizarEstadoBotonGuardar()
        }
        binding.cardToggleActivo?.setOnClickListener {
            binding.switchestadodelproducto.isChecked = !binding.switchestadodelproducto.isChecked
        }

        // Aplicar estilo inicial según estado actual
        actualizarEstiloTarjetaToggleReceta(binding.swichtreceta.isChecked)
        actualizarEstiloTarjetaToggleActivo(binding.switchestadodelproducto.isChecked)

        binding.switchPresentaciones.setOnCheckedChangeListener { _, _ ->
            actualizarEstadoBotonGuardar()
        }
    }

    /**
     * Actualiza el estilo de la tarjeta-toggle de "Requiere receta" según el estado del switch.
     * Activa: borde y fondo verdes, ícono y textos en verde oscuro.
     * Inactiva: borde gris, fondo blanco, ícono y textos neutros.
     */
    private fun actualizarEstiloTarjetaToggleReceta(activo: Boolean) {
        val card = binding.cardToggleReceta ?: return
        val iconCard = binding.iconToggleReceta ?: return
        val img = binding.imgToggleReceta ?: return
        val tvNombre = binding.tvNombreToggleReceta ?: return
        val tvHint = binding.tvHintReceta ?: return
        if (activo) {
            card.setCardBackgroundColor(0xFFEAF3DE.toInt())
            card.strokeColor = 0xFF9FE1CB.toInt()
            iconCard.setCardBackgroundColor(0xFFC0DD97.toInt())
            img.setColorFilter(0xFF27500A.toInt())
            tvNombre.setTextColor(0xFF0F6E56.toInt())
            tvHint.setTextColor(0xFF3B6D11.toInt())
        } else {
            card.setCardBackgroundColor(0xFFFFFFFF.toInt())
            card.strokeColor = 0xFFEEF1F4.toInt()
            iconCard.setCardBackgroundColor(0xFFF1F4F7.toInt())
            img.setColorFilter(0xFF667085.toInt())
            tvNombre.setTextColor(0xFF0F172A.toInt())
            tvHint.setTextColor(0xFF667085.toInt())
        }
    }

    private fun actualizarEstiloTarjetaToggleActivo(activo: Boolean) {
        val card = binding.cardToggleActivo ?: return
        val iconCard = binding.iconToggleActivo ?: return
        val img = binding.imgToggleActivo ?: return
        val tvNombre = binding.tvNombreToggleActivo ?: return
        val tvHint = binding.tvHintActivo ?: return
        if (activo) {
            card.setCardBackgroundColor(0xFFEAF3DE.toInt())
            card.strokeColor = 0xFF9FE1CB.toInt()
            iconCard.setCardBackgroundColor(0xFFC0DD97.toInt())
            img.setColorFilter(0xFF27500A.toInt())
            tvNombre.setTextColor(0xFF0F6E56.toInt())
            tvHint.setTextColor(0xFF3B6D11.toInt())
        } else {
            card.setCardBackgroundColor(0xFFFFFFFF.toInt())
            card.strokeColor = 0xFFEEF1F4.toInt()
            iconCard.setCardBackgroundColor(0xFFF1F4F7.toInt())
            img.setColorFilter(0xFF667085.toInt())
            tvNombre.setTextColor(0xFF0F172A.toInt())
            tvHint.setTextColor(0xFF667085.toInt())
        }
    }

    private fun obtenerSnapshotFormularioActual(): String {
        val nombre = binding.editTextNombre.text.toString().trim()
        val categoria = binding.editTextCategoria.text.toString().trim()
        val compra = binding.editTextCompra.text.toString().trim()
        val cantidad = binding.editTextCantidad.text.toString().trim()
        val stock = binding.editTextStock.text.toString().trim()

        val cantPres = binding.editCantidadPresentacion.text.toString().trim()
        val unidXCaja = binding.editUnidadesPorCaja.text.toString().trim()
        val stockPres = binding.editTextStockPresentacion.text.toString().trim()
        val tipoIngreso = binding.radioGroupTipoIngreso.checkedRadioButtonId

        val unidadBase = binding.editTextUnidadBase.text.toString().trim()
        val presentacionPrincipal = obtenerCanonicalPresentacionPrincipalActual()
        val vencimiento = binding.editTextVencimiento.text.toString().trim()

        val requiereReceta = binding.swichtreceta.isChecked
        val estadoProducto = binding.switchestadodelproducto.isChecked
        val tienePresentaciones = binding.switchPresentaciones.isChecked

        val imagenActualComparable = if (imagenUri != null) {
            "imagen_local_nueva"
        } else {
            imagenUrlActual.trim()
        }

        val presentacionesSnapshot = listaPresentaciones
            .sortedBy { it.nombre.lowercase().trim() }
            .joinToString("|") {
                "${it.nombre.trim()}-${it.cantidad}-${it.precioventa}"
            }

        return listOf(
            nombre,
            categoria,
            compra,
            cantidad,
            stock,
            cantPres,
            unidXCaja,
            stockPres,
            tipoIngreso.toString(),
            unidadBase,
            presentacionPrincipal,
            vencimiento,
            requiereReceta.toString(),
            estadoProducto.toString(),
            tienePresentaciones.toString(),
            imagenActualComparable,
            presentacionesSnapshot
        ).joinToString("||")
    }

    private fun obtenerCanonicalPresentacionPrincipalActual(): String {
        val unidadBase = binding.editTextUnidadBase.text?.toString()?.trim()
            .orEmpty()
            .ifBlank { getString(R.string.por_unidades).lowercase() }

        val canonicalTag = (binding.editTextPresentacionPrincipal.tag as? String)
            ?.trim()
            .orEmpty()

        if (canonicalTag.isNotBlank()) return canonicalTag

        val visible = binding.editTextPresentacionPrincipal.text?.toString()?.trim().orEmpty()
        if (visible.isBlank()) return ""

        return PresentacionesTiendaConfigManager.canonicalDesdeVisible(unidadBase, visible) ?: visible
    }

    private fun marcarEstadoInicialFormulario() {
        estadoInicialFormulario = obtenerSnapshotFormularioActual()
        cargandoDatosIniciales = false
        actualizarEstadoBotonGuardar()
        actualizarResumenRapidoStock()
        actualizarResumenTarjetasEdicion()
    }


    private fun actualizarEstadoBotonGuardar() {
        if (cargandoDatosIniciales) {
            aplicarEstadoBotonGuardadoMaterial(binding.materialButton3, enabled = false)
            actualizarEstadoBotonGuardarSeccionActiva(false)
            actualizarResumenTarjetasEdicion()
            binding.tvHeroVence.text = "Vence: ${binding.editTextVencimiento.text?.toString()?.trim().orEmpty().ifBlank { "No definido" }}"
            return
        }

        val hayCambios = obtenerSnapshotFormularioActual() != estadoInicialFormulario

        aplicarEstadoBotonGuardadoMaterial(binding.materialButton3, enabled = hayCambios)
        val hayCambiosEnSeccion = snapshotFormularioSeccionActiva?.let {
            obtenerSnapshotFormularioActual() != it
        } ?: false
        actualizarEstadoBotonGuardarSeccionActiva(hayCambiosEnSeccion)
        actualizarResumenTarjetasEdicion()
        binding.tvHeroVence.text = "Vence: ${binding.editTextVencimiento.text?.toString()?.trim().orEmpty().ifBlank { "No definido" }}"
    }

    private fun actualizarEstadoBotonGuardarSeccionActiva(habilitado: Boolean) {
        botonGuardarSeccionActivo?.let { aplicarEstadoBotonGuardadoMaterial(it, habilitado) }
    }

    private fun configurarValidacionNombreRealTime() {
        binding.editTextNombre.addTextChangedListener { text ->
            val nombre = text.toString().trim()
            if (nombre.isEmpty()) {
                binding.tilNombre.helperText = null
                return@addTextChangedListener
            }

            // Excluir el nombre original del producto que se está editando
            val nombreOriginal = binding.editTextNombre.tag?.toString() ?: ""
            if (nombre.equals(nombreOriginal, ignoreCase = true)) {
                binding.tilNombre.helperText = null
                return@addTextChangedListener
            }

            val existe = listaNombreAutoComplete.any { it.nombre.equals(nombre, ignoreCase = true) }
            if (existe) {
                binding.tilNombre.helperText = getString(R.string.error_nombre_registrado)
                binding.tilNombre.setHelperTextColor(ColorStateList.valueOf("#FFA000".toColorInt()))
            } else {
                val productoSimilar = listaNombreAutoComplete.find { similar(it.nombre, nombre) }
                if (productoSimilar != null && !productoSimilar.nombre.equals(nombreOriginal, ignoreCase = true)) {
                    binding.tilNombre.helperText = getString(R.string.sugerencia_nombre_similar, productoSimilar.nombre)
                    binding.tilNombre.setHelperTextColor(ColorStateList.valueOf(Color.GRAY))
                } else {
                    binding.tilNombre.helperText = null
                }
            }
        }
    }

    private fun configurarScrollEnCampos() {
        val camposInventarioSinScrollAutomatico = setOf<View>(
            binding.editTextCantidad,
            binding.editTextStock,
            binding.editCantidadPresentacion,
            binding.editUnidadesPorCaja,
            binding.editTextStockPresentacion
        )

        val campos = listOfNotNull(
            binding.editTextNombre,
            binding.editTextCategoria,
            binding.editTextCompra,
            binding.editTextCantidad,
            binding.editTextStock,
            binding.editCantidadPresentacion,
            binding.editUnidadesPorCaja,
            binding.editTextStockPresentacion,
            binding.editTextUnidadBase,
            binding.editTextPresentacionPrincipal
        )

        campos.forEach { campo ->
            campo.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    if (!camposInventarioSinScrollAutomatico.contains(v)) {
                        posicionarScrollInterno(v, 200)
                    }
                } else if (
                    v === binding.editTextStock ||
                    v === binding.editTextStockPresentacion ||
                    v === binding.editUnidadesPorCaja ||
                    v === binding.editCantidadPresentacion
                ) {
                    actualizarResumenesStockTrasEdicion()
                    validarStockMinimoActivoEnTiempoReal()
                }
            }
        }
    }

    private fun posicionarScroll(view: View, offset: Int = 0) {
        posicionarScrollInterno(view, offset)
    }

    private fun posicionarScrollInterno(view: View, offset: Int = 0) {
        binding.constraintcontainer.post {
            try {
                // Verificamos si la vista sigue siendo descendiente del contenedor
                // para evitar el IllegalArgumentException
                var isDescendant = false
                var parent = view.parent
                while (parent != null) {
                    if (parent == binding.constraintcontainer) {
                        isDescendant = true
                        break
                    }
                    parent = parent.parent
                }

                if (isDescendant) {
                    val location = IntArray(2)
                    val scrollLocation = IntArray(2)

                    view.getLocationOnScreen(location)
                    binding.constraintcontainer.getLocationOnScreen(scrollLocation)

                    // Calculamos la posición relativa al ScrollView (o container)
                    val relativeTop = location[1] - scrollLocation[1] + binding.constraintcontainer.scrollY

                    binding.constraintcontainer.smoothScrollTo(0, relativeTop - offset)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun ocultarTeclado() {
        val view = currentFocus ?: return
        val inputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    private fun configurarLogicaStock() {
        binding.radioGroupTipoIngreso.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioPorUnidades) {
                binding.layoutPorUnidades.visibility = View.VISIBLE
                binding.layoutPorPaquetes.visibility = View.GONE
            } else {
                binding.layoutPorUnidades.visibility = View.GONE
                binding.layoutPorPaquetes.visibility = View.VISIBLE
            }
            actualizarHintStockMinimoSegunAviso()
            actualizarHelperStockSegunModo()
            actualizarResumenInventario()
            validarStockMinimoActivoEnTiempoReal()
            actualizarEstadoBotonGuardar()
        }

        val textWatcherCalculo = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calcularStockTotal()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.editCantidadPresentacion.addTextChangedListener(textWatcherCalculo)
        binding.editUnidadesPorCaja.addTextChangedListener(textWatcherCalculo)
    }

    private fun configurarValidacionStockMinimoTiempoReal() {
        binding.editTextStock.addTextChangedListener {
            validarStockMinimoActivoEnTiempoReal()
        }
        binding.editTextStockPresentacion.addTextChangedListener {
            validarStockMinimoActivoEnTiempoReal()
        }
    }

    private fun configurarCamposStockSoloLectura() {
        binding.editTextCantidad.apply {
            isFocusable = false
            isFocusableInTouchMode = false
            isCursorVisible = false
            isLongClickable = false
            keyListener = null
        }

        binding.editCantidadPresentacion.apply {
            isFocusable = false
            isFocusableInTouchMode = false
            isCursorVisible = false
            isLongClickable = false
            keyListener = null
        }

        actualizarHelperStockSegunModo()
    }
    private fun calcularStockTotal() {
        val cajas = binding.editCantidadPresentacion.text.toString().toIntOrNull() ?: 0
        val unidadesPorCaja = binding.editUnidadesPorCaja.text.toString().toIntOrNull() ?: 0
        val total = cajas * unidadesPorCaja
        val stockVisible = if (binding.radioPorPaquetes.isChecked && unidadesPorCaja > 0) {
            total
        } else {
            binding.editTextCantidad.text.toString().toIntOrNull() ?: 0
        }
        val unidadBaseUi = UnidadBaseHelper.formatear(binding.editTextUnidadBase.text?.toString(), stockVisible)
        binding.textStockCalculado.text = getString(
            R.string.stock_total_formato,
            getString(R.string.stock_total_prefijo),
            stockVisible,
            unidadBaseUi
        )

        if (binding.radioPorPaquetes.isChecked && unidadesPorCaja > 0) {
            binding.editTextCantidad.setText(total.toString())
            mostrarResumenEquivalenciaStock(
                stockTotalUnidades = total,
                unidadesPorPresentacion = unidadesPorCaja,
                presentacionesCompletas = cajas,
                unidadesSueltas = total % unidadesPorCaja
            )
        } else {
            val helperLayout = binding.editCantidadPresentacion.parent?.parent as? TextInputLayout
            helperLayout?.helperText = null
        }
        actualizarHelperStockMinimoPresentacion()
        sincronizarPresentacionDesdeFrascos()
    }

    private fun sincronizarPresentacionDesdeFrascos() {
        if (cargandoDatosIniciales) return  // No corromper presentaciones durante la carga inicial
        val unidadBase = binding.editTextUnidadBase.text?.toString().orEmpty().trim()
        val esMl = unidadBase.equals("mL", ignoreCase = true)
        val esG = unidadBase.equals("g", ignoreCase = true)

        val cantPorUnidad = binding.editUnidadesPorCaja.text.toString().trim().toIntOrNull() ?: 0
        if (cantPorUnidad <= 0) return

        val nombreAuto = when { esMl -> "Frasco"; esG -> "Paquete"; else -> "Caja" }

        if (!binding.switchPresentaciones.isChecked) {
            binding.switchPresentaciones.isChecked = true
        }

        val existente = listaPresentaciones.find { it.nombre.equals(nombreAuto, ignoreCase = true) }

        if (existente != null) {
            if (existente.cantidad == cantPorUnidad) return
            existente.cantidad = cantPorUnidad
            binding.contenedorPresentaciones.removeAllViews()
            listaPresentaciones.forEach { agregarPresentacionVisual(it) }
            actualizarOpcionesPresentacionPrincipal()
            sincronizarPresentacionPrincipalSegunLista()
        } else {
            val nueva = PresentacionProducto(nombre = nombreAuto, cantidad = cantPorUnidad, precioventa = 0.0)
            listaPresentaciones.add(nueva)
            agregarPresentacionVisual(nueva)
            actualizarOpcionesPresentacionPrincipal()
            if (listaPresentaciones.size == 1) {
                asignarPresentacionPrincipal(nombreAuto, seleccionManual = false)
            }
        }
    }

    private fun configurarAjusteStock() {
        binding.btnAjustarStock.setOnClickListener {
            if (!puedeAbrirMovimientoStock()) {
                return@setOnClickListener
            }
            mostrarDialogoAjusteStock()
        }
    }

    private fun configurarIngresoStock() {
        binding.btnIngresarStock.setOnClickListener {
            if (!puedeAbrirMovimientoStock()) {
                return@setOnClickListener
            }
            mostrarDialogoIngresoStock()
        }
    }

    private fun puedeAbrirMovimientoStock(): Boolean {
        if (!cargandoDatosIniciales && obtenerSnapshotFormularioActual() != estadoInicialFormulario) {
            Toast.makeText(
                this,
                "Guarda o descarta los cambios actuales antes de mover el stock",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }

    private fun mostrarDialogoAjusteStock() {
        // Cargar lotes existentes desde Firebase antes de mostrar el diálogo
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRODUCTOS)
            .child(indiceOriginal)
            .child("lotes")
            .get()
            .addOnSuccessListener { snapshot ->
                val lotesExistentes = mutableListOf<LoteProducto>()
                snapshot.children.forEach { child ->
                    child.getValue(LoteProducto::class.java)?.let { lotesExistentes.add(it) }
                }
                mostrarDialogoAjusteStockConLotes(lotesExistentes)
            }
            .addOnFailureListener {
                mostrarDialogoAjusteStockConLotes(emptyList())
            }
    }

    private fun mostrarDialogoAjusteStockConLotes(lotesExistentes: List<LoteProducto>) {
        val unidadBase = binding.editTextUnidadBase.text.toString().trim().ifBlank { "unidades" }
        val presentacionesDisponibles = obtenerPresentacionesAjustables(unidadBase)
        val nombreUnidadBaseUi = formatearEtiquetaUi(unidadBase)
        val lotesConNumero = lotesExistentes.filter { it.numero.trim().isNotBlank() }
        val hayLotesRegistrados = lotesConNumero.isNotEmpty()
        val loteUnico = lotesConNumero.singleOrNull()

        val dialogBinding = DialogAjusteStockBinding.inflate(layoutInflater)
        val radioGroup = dialogBinding.radioGroupOperacion
        val radioSumar = dialogBinding.radioSumar
        val textIndicadorScroll = dialogBinding.tvIndicadorScrollAjuste
        val layoutModo = dialogBinding.layoutModoAjuste
        val inputModo = dialogBinding.inputModoAjuste
        val layoutPresentacion = dialogBinding.layoutPresentacionAjuste
        val inputPresentacion = dialogBinding.inputPresentacionAjuste
        val layoutCantidad = dialogBinding.layoutCantidadAjuste
        val inputCantidad = dialogBinding.inputCantidadAjuste
        val cardResumen = dialogBinding.cardResumenAjuste
        val textOperacionResumen = dialogBinding.tvOperacionResumenAjuste
        val textEquivalenciaResumen = dialogBinding.tvEquivalenciaResumenAjuste
        val textStockResultado = dialogBinding.tvStockResultadoAjuste
        val textAdvertenciaResumen = dialogBinding.tvAdvertenciaResumenAjuste
        val textLoteResumen = dialogBinding.tvLoteResumenAjuste
        val layoutMotivo = dialogBinding.layoutMotivoAjuste
        val inputMotivo = dialogBinding.inputMotivoAjuste
        val layoutSeccionLote = dialogBinding.layoutSeccionLoteAjuste
        val layoutLote = dialogBinding.layoutLoteAjuste
        val inputLote = dialogBinding.inputLoteAjuste
        var refrescarResumenAjuste: (() -> Unit)? = null
        var refrescarIndicadorScrollAjuste: (() -> Unit)? = null

        layoutSeccionLote.visibility = View.VISIBLE

        val opcionesLote = lotesConNumero.map { it.numero.trim() }
        inputLote.inputType = InputType.TYPE_NULL
        inputLote.keyListener = null
        inputLote.isCursorVisible = false
        inputLote.isFocusable = false
        inputLote.isFocusableInTouchMode = false
        when {
            opcionesLote.isEmpty() -> {
                inputLote.isEnabled = false
                inputLote.setText("", false)
                inputLote.setOnClickListener(null)
                layoutLote.endIconMode = TextInputLayout.END_ICON_NONE
            }
            opcionesLote.size == 1 -> {
                inputLote.isEnabled = true
                inputLote.setAdapter(
                    ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opcionesLote)
                )
                inputLote.setText(opcionesLote.first(), false)
                inputLote.setOnClickListener(null)
                layoutLote.endIconMode = TextInputLayout.END_ICON_NONE
            }
            else -> {
                inputLote.isEnabled = true
                inputLote.setAdapter(
                    ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opcionesLote)
                )
                layoutLote.endIconMode = TextInputLayout.END_ICON_CUSTOM
                layoutLote.setEndIconDrawable(R.drawable.ic_arrow_drop_down)
                layoutLote.endIconContentDescription = "Ver lotes"
                layoutLote.setEndIconOnClickListener { inputLote.showDropDown() }
                inputLote.setOnClickListener { inputLote.showDropDown() }
                aplicarCierreDropdownAlRecibirFoco(inputLote)
            }
        }

        val opcionesModo = mutableListOf(nombreUnidadBaseUi)
        if (presentacionesDisponibles.isNotEmpty()) {
            opcionesModo.add("Presentación")
        }
        inputModo.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opcionesModo)
        )
        layoutModo.endIconMode = TextInputLayout.END_ICON_CUSTOM
        layoutModo.setEndIconDrawable(R.drawable.ic_arrow_drop_down)
        layoutModo.endIconContentDescription = "Ver opciones"
        layoutModo.setEndIconOnClickListener { inputModo.showDropDown() }
        inputModo.inputType = InputType.TYPE_NULL
        inputModo.keyListener = null
        inputModo.isCursorVisible = false
        inputModo.isFocusable = false
        inputModo.isFocusableInTouchMode = false
        inputModo.setOnClickListener { inputModo.showDropDown() }
        aplicarCierreDropdownAlRecibirFoco(inputModo)
        inputModo.setText(opcionesModo.first(), false)

        val nombresPresentaciones = presentacionesDisponibles.map { it.nombre }
        inputPresentacion.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nombresPresentaciones)
        )
        layoutPresentacion.endIconMode = TextInputLayout.END_ICON_CUSTOM
        layoutPresentacion.setEndIconDrawable(R.drawable.ic_arrow_drop_down)
        layoutPresentacion.endIconContentDescription = "Ver presentaciones"
        layoutPresentacion.setEndIconOnClickListener { inputPresentacion.showDropDown() }
        inputPresentacion.inputType = InputType.TYPE_NULL
        inputPresentacion.keyListener = null
        inputPresentacion.isCursorVisible = false
        inputPresentacion.isFocusable = false
        inputPresentacion.isFocusableInTouchMode = false
        inputPresentacion.setOnClickListener { inputPresentacion.showDropDown() }
        aplicarCierreDropdownAlRecibirFoco(inputPresentacion)
        if (nombresPresentaciones.isNotEmpty()) {
            inputPresentacion.setText(nombresPresentaciones.first(), false)
        }

        listOf(
            "Conteo fisico",
            "Merma",
            "Producto vencido",
            "Rotura",
            "Correccion administrativa"
        )
        inputMotivo.setText("Ajuste manual", false)

        fun presentacionSeleccionada(): PresentacionProducto? {
            val nombre = inputPresentacion.text?.toString()?.trim().orEmpty()
            return presentacionesDisponibles.firstOrNull { it.nombre.equals(nombre, ignoreCase = true) }
        }

        fun numeroLoteActual(): String {
            return inputLote.text?.toString()?.trim().orEmpty()
        }

        fun loteExistenteSeleccionado(): LoteProducto? {
            val numero = numeroLoteActual()
            return lotesConNumero.firstOrNull { it.numero.trim().equals(numero, ignoreCase = true) }
        }

        var refrescarEstadoBotonAjuste: (() -> Unit)? = null
        val colorCantidadNormal = inputCantidad.currentTextColor
        val colorCantidadResta = Color.parseColor("#B42318")
        var actualizandoCantidadProgramaticamente = false
        var ultimoMensajeErrorCantidad: String? = null

        fun esAjusteSuma(): Boolean = radioGroup.checkedRadioButtonId == radioSumar.id

        fun obtenerCantidadAjusteAbsoluta(): Int {
            val texto = inputCantidad.text?.toString()?.trim().orEmpty()
            return texto.replace("-", "").trim().toIntOrNull() ?: 0
        }

        fun aplicarFormatoCantidadSegunOperacion() {
            val esSuma = esAjusteSuma()
            val textoActual = inputCantidad.text?.toString().orEmpty()
            val digitos = textoActual.filter { it.isDigit() }
            val textoFormateado = when {
                esSuma -> digitos
                digitos.isBlank() -> ""
                else -> "-$digitos"
            }

            inputCantidad.inputType = if (esSuma) {
                InputType.TYPE_CLASS_NUMBER
            } else {
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
            }
            inputCantidad.setTextColor(if (esSuma) colorCantidadNormal else colorCantidadResta)

            if (textoActual == textoFormateado) {
                return
            }

            actualizandoCantidadProgramaticamente = true
            inputCantidad.setText(textoFormateado)
            inputCantidad.setSelection(inputCantidad.text?.length ?: 0)
            actualizandoCantidadProgramaticamente = false
        }

        fun actualizarContextoAjuste() {
            val esPresentacion = inputModo.text?.toString().orEmpty().trim()
                .equals("Presentación", ignoreCase = true)
            val esSuma = esAjusteSuma()
            val loteExistente = loteExistenteSeleccionado()

            layoutPresentacion.visibility =
                if (esPresentacion && presentacionesDisponibles.isNotEmpty()) View.VISIBLE else View.GONE

            layoutPresentacion.helperText = when {
                !esPresentacion -> null
                presentacionSeleccionada() != null -> {
                    val presentacion = presentacionSeleccionada()!!
                    val nombrePresentacion = formatearEtiquetaUi(presentacion.nombre).lowercase()
                    val unidadesTexto = formatearCantidadConEtiqueta(
                        cantidad = presentacion.cantidad,
                        singular = unidadBase.lowercase(),
                        plural = pluralizarTexto(unidadBase.lowercase())
                    )
                    "1 $nombrePresentacion = $unidadesTexto"
                }
                else -> "Selecciona una presentación para ver su equivalencia"
            }

            layoutCantidad.hint = if (esPresentacion) {
                val presentacion = presentacionSeleccionada()
                val nombrePresentacion =
                    formatearEtiquetaUi(presentacion?.nombre).ifBlank { "presentación" }
                "Cantidad a ajustar en ${nombrePresentacion.lowercase()}"
            } else {
                "Cantidad a ajustar en ${unidadBase.lowercase()}"
            }

            layoutLote.hint = if (esSuma) {
                "Lote que recibirá el stock"
            } else {
                "Lote del que se descontará el stock"
            }
            layoutLote.helperText = when {
                !hayLotesRegistrados ->
                    "Ajustar stock no crea lotes. Primero registra un lote para este producto."
                loteUnico != null && loteUnico.vencimiento.isNotBlank() ->
                    "Se usara automaticamente el unico lote registrado: ${loteUnico.numero} (vence ${loteUnico.vencimiento})."
                loteUnico != null ->
                    "Se usara automaticamente el unico lote registrado: ${loteUnico.numero}."
                esSuma && loteExistente != null && loteExistente.vencimiento.isNotBlank() ->
                    "Este ajuste sumará stock al lote ${loteExistente.numero} (vence ${loteExistente.vencimiento})."
                esSuma && loteExistente != null ->
                    "Este ajuste sumará stock al lote ${loteExistente.numero}."
                esSuma ->
                    "Obligatorio: selecciona el lote existente que recibira el stock."
                false ->
                    "Se creará un lote nuevo con ese número."
                false ->
                    "No puedes ajustar salida sin elegir lote. Primero registra un lote para este producto."
                loteExistente != null && loteExistente.vencimiento.isNotBlank() ->
                    "La salida se descontará exactamente del lote ${loteExistente.numero} (vence ${loteExistente.vencimiento})."
                loteExistente != null ->
                    "La salida se descontará exactamente del lote ${loteExistente.numero}."
                else ->
                    "Obligatorio: selecciona el lote del que se descontará el stock."
            }

            /*
            layoutVencimientoLote.visibility = if (creandoLoteNuevo) View.VISIBLE else View.GONE
            layoutVencimientoLote.helperText = if (requiereVencimientoLote) {
                "Obligatorio para el nuevo lote. Formato MM/AA y no vencido."
            } else {
                "Opcional para el nuevo lote. Se usará el vencimiento general si existe."
            }
            if (creandoLoteNuevo && inputVencimientoLote.text?.toString()?.trim().isNullOrBlank()) {
                val vencimientoGeneral = binding.editTextVencimiento.text?.toString()?.trim().orEmpty()
                if (vencimientoGeneral.isNotBlank()) {
                    inputVencimientoLote.setText(vencimientoGeneral)
                }
            }
            if (!creandoLoteNuevo) {
                layoutVencimientoLote.error = null
                inputVencimientoLote.text?.clear()
            }
            */
        }

        fun actualizarResumenAjuste() {
            val modoEsPresentacion = layoutPresentacion.visibility == View.VISIBLE
            val esSuma = esAjusteSuma()
            val cantidad = obtenerCantidadAjusteAbsoluta()
            val stockActual = binding.editTextCantidad.text?.toString()?.trim()?.toIntOrNull() ?: 0
            val presentacion = if (modoEsPresentacion) presentacionSeleccionada() else null
            val unidadesPorAjuste = if (modoEsPresentacion) presentacion?.cantidad ?: 0 else 1
            val totalUnidades = if (cantidad > 0 && unidadesPorAjuste > 0) cantidad * unidadesPorAjuste else 0
            val stockResultante = if (esSuma) stockActual + totalUnidades else stockActual - totalUnidades
            val verboPrincipal = if (esSuma) "Se agregarán" else "Se descontarán"
            val verboAccion = if (esSuma) "agregará" else "descontará"
            val unidadBaseUi = formatearEtiquetaUi(unidadBase)
            val numeroLote = numeroLoteActual()
            val loteExistente = loteExistenteSeleccionado()
            val stockMinimoActual = obtenerStockMinimoActualEnUnidadBase()
            val ajusteGrande = !esSuma && stockActual > 0 && totalUnidades > 0 && totalUnidades * 2 >= stockActual

            layoutModo.helperText = if (modoEsPresentacion) {
                "Usa una presentación y el sistema la convertirá a ${unidadBase.lowercase()} base."
            } else {
                "Ajustas directamente la cantidad real del producto en ${unidadBase.lowercase()}."
            }
            layoutCantidad.helperText = if (modoEsPresentacion) {
                "La cantidad escrita aquí representa presentaciones completas, no unidades sueltas."
            } else {
                "La cantidad escrita aquí se suma o se resta directamente del stock real."
            }

            layoutCantidad.helperText = if (modoEsPresentacion) {
                if (esSuma) {
                    "La cantidad escrita aquí representa presentaciones completas, no unidades sueltas."
                } else {
                    "La salida se muestra en rojo y con signo negativo. La cantidad representa presentaciones completas."
                }
            } else {
                if (esSuma) {
                    "La cantidad escrita aquí se suma directamente al stock real."
                } else {
                    "La salida se muestra en rojo y con signo negativo. Ejemplo: -2."
                }
            }

            if (cantidad <= 0) {
                cardResumen.visibility = View.GONE
                textOperacionResumen.text = ""
                textEquivalenciaResumen.text = ""
                textStockResultado.text = ""
                textAdvertenciaResumen.text = ""
                textAdvertenciaResumen.visibility = View.GONE
                textLoteResumen.text = ""
                textLoteResumen.visibility = View.GONE
                refrescarIndicadorScrollAjuste?.invoke()
                return
            }

            cardResumen.visibility = View.VISIBLE
            if (numeroLote.isNotBlank()) {
                textLoteResumen.visibility = View.VISIBLE
                textLoteResumen.text = if (esSuma && loteExistente == null) {
                    "Lote nuevo: $numeroLote"
                } else {
                    "Lote afectado: $numeroLote"
                }
            } else {
                textLoteResumen.visibility = View.GONE
                textLoteResumen.text = ""
            }
            val advertenciaResumen = when {
                !esSuma && lotesConNumero.isEmpty() ->
                    "No permitido: este producto no tiene lotes para descontar stock."
                !esSuma && loteExistente != null && totalUnidades.toDouble() > loteExistente.cantidad ->
                    "La cantidad supera el stock disponible del lote seleccionado."
                !esSuma && stockResultante < 0 ->
                    "No permitido: intentas descontar más stock del disponible."
                !esSuma && stockResultante == 0 ->
                    "Atención: este ajuste dejará el producto sin stock."
                !esSuma && stockMinimoActual > 0 && stockResultante < stockMinimoActual ->
                    "Atención: el stock quedará por debajo del mínimo configurado ($stockMinimoActual ${unidadBaseUi.lowercase()})."
                ajusteGrande ->
                    "Revisión recomendada: este descuento retirará al menos la mitad del stock actual."
                else -> null
            }
            if (advertenciaResumen.isNullOrBlank()) {
                textAdvertenciaResumen.visibility = View.GONE
                textAdvertenciaResumen.text = ""
            } else {
                textAdvertenciaResumen.visibility = View.VISIBLE
                textAdvertenciaResumen.text = advertenciaResumen
                val colorAdvertencia = if (!esSuma && (stockResultante <= 0 || (stockMinimoActual > 0 && stockResultante < stockMinimoActual))) {
                    Color.parseColor("#B42318")
                } else {
                    Color.parseColor("#B54708")
                }
                textAdvertenciaResumen.setTextColor(colorAdvertencia)
            }
            refrescarIndicadorScrollAjuste?.invoke()
            if (modoEsPresentacion && presentacion != null) {
                val nombrePresentacionUi = formatearEtiquetaUi(presentacion.nombre)
                val cantidadPresentacionesTexto = formatearCantidadConEtiqueta(
                    cantidad = cantidad,
                    singular = nombrePresentacionUi.lowercase(),
                    plural = pluralizarTexto(nombrePresentacionUi.lowercase())
                )
                val unidadesPorPresentacionTexto = formatearCantidadConEtiqueta(
                    cantidad = presentacion.cantidad,
                    singular = unidadBaseUi.lowercase(),
                    plural = pluralizarTexto(unidadBaseUi.lowercase())
                )
                val totalTexto = formatearCantidadConEtiqueta(
                    cantidad = totalUnidades,
                    singular = unidadBaseUi.lowercase(),
                    plural = pluralizarTexto(unidadBaseUi.lowercase())
                )
                textOperacionResumen.text = "$verboPrincipal $cantidadPresentacionesTexto en este ajuste."
                textEquivalenciaResumen.text =
                    "Presentación seleccionada: $nombrePresentacionUi. 1 ${nombrePresentacionUi.lowercase()} = $unidadesPorPresentacionTexto. Este ajuste equivale a $totalTexto."
                textStockResultado.text = if (!esSuma && stockResultante < 0) {
                    "Stock actual: $stockActual $unidadBaseUi.\nEste ajuste intentaría dejar el stock en $stockResultante $unidadBaseUi, lo cual no está permitido."
                } else {
                    "Stock actual: $stockActual $unidadBaseUi.\nStock final esperado: $stockResultante $unidadBaseUi.\nEl sistema $verboAccion $totalTexto del stock real."
                }
            } else if (modoEsPresentacion) {
                textOperacionResumen.text = "Selecciona la presentación para calcular el impacto real del ajuste."
                textEquivalenciaResumen.text =
                    "Cuando elijas una presentación, te mostraremos cuántas unidades base se $verboAccion del stock."
                textStockResultado.text =
                    "Stock actual del producto: $stockActual $unidadBaseUi."
            } else {
                val cantidadTexto = formatearCantidadConEtiqueta(
                    cantidad = cantidad,
                    singular = unidadBaseUi.lowercase(),
                    plural = pluralizarTexto(unidadBaseUi.lowercase())
                )
                textOperacionResumen.text = "$verboPrincipal $cantidadTexto reales al stock."
                textEquivalenciaResumen.text =
                    "Estás ajustando directamente en la unidad base del producto, sin conversiones intermedias."
                textStockResultado.text =
                    "Stock actual: $stockActual $unidadBaseUi. Stock esperado después del ajuste: $stockResultante $unidadBaseUi."
            }
        }
        refrescarResumenAjuste = ::actualizarResumenAjuste

        aplicarFormatoCantidadSegunOperacion()
        actualizarContextoAjuste()
        actualizarResumenAjuste()

        inputModo.setOnItemClickListener { _, _, _, _ ->
            layoutModo.error = null
            actualizarContextoAjuste()
            actualizarResumenAjuste()
            refrescarEstadoBotonAjuste?.invoke()
        }
        inputPresentacion.setOnItemClickListener { _, _, _, _ ->
            layoutPresentacion.error = null
            actualizarContextoAjuste()
            actualizarResumenAjuste()
            refrescarEstadoBotonAjuste?.invoke()
        }
        inputLote.setOnItemClickListener { _, _, _, _ ->
            layoutLote.error = null
            actualizarContextoAjuste()
            actualizarResumenAjuste()
            refrescarEstadoBotonAjuste?.invoke()
        }
        inputLote.addTextChangedListener {
            layoutLote.error = null
            actualizarContextoAjuste()
            actualizarResumenAjuste()
            refrescarEstadoBotonAjuste?.invoke()
        }
        /*
        inputVencimientoLote.addTextChangedListener {
            layoutVencimientoLote.error = null
            refrescarEstadoBotonAjuste?.invoke()
        }
        */
        radioGroup.setOnCheckedChangeListener { _, _ ->
            aplicarFormatoCantidadSegunOperacion()
            actualizarContextoAjuste()
            actualizarResumenAjuste()
            refrescarEstadoBotonAjuste?.invoke()
        }

        // ========== Nueva UI estilo bottom sheet ==========
        // 1) Cabecera del producto
        dialogBinding.tvNombreProductoAjuste.text =
            binding.editTextNombre.text?.toString()?.trim().orEmpty().ifBlank { "Producto" }
        dialogBinding.tvSubtituloAjuste.visibility = View.VISIBLE

        // 2) Stock en sistema (solo lectura, valor del producto cargado)
        val stockEnSistemaActual =
            binding.editTextCantidad.text?.toString()?.trim()?.toIntOrNull() ?: 0
        dialogBinding.tvStockEnSistemaAjuste.text = stockEnSistemaActual.toString()

        // 3) Pre-llenar conteo físico con el stock actual (luego el usuario ajusta)
        val inputConteoFisico = dialogBinding.inputConteoFisicoAjuste
        inputConteoFisico.setText(stockEnSistemaActual.toString())

        // 4) Chips de Presentación (sincronizan inputModo / inputPresentacion ocultos)
        val chipsPresAjuste = dialogBinding.chipsPresentacionAjuste
        chipsPresAjuste.removeAllViews()
        val cardsPresAjuste = mutableListOf<Pair<MaterialCardView, String>>()
        fun pintarChipsPresAjuste(seleccionado: String) {
            cardsPresAjuste.forEach { (card, op) ->
                val sel = op.equals(seleccionado, ignoreCase = true)
                card.setCardBackgroundColor(if (sel) 0xFFE5F4EB.toInt() else 0xFFFFFFFF.toInt())
                card.strokeColor = if (sel) 0xFF0E8F63.toInt() else 0xFFE5E7EB.toInt()
                card.strokeWidth = if (sel) (2 * resources.displayMetrics.density).toInt()
                    else (1 * resources.displayMetrics.density).toInt()
                card.findViewById<TextView>(R.id.tvChipMovimiento)?.setTextColor(
                    if (sel) 0xFF0E8F63.toInt() else 0xFF0F172A.toInt()
                )
                card.findViewById<ImageView>(R.id.imgChipMovimiento)?.setColorFilter(
                    if (sel) 0xFF0E8F63.toInt() else 0xFF667085.toInt()
                )
            }
        }
        opcionesModo.forEach { opcion ->
            val chipView = layoutInflater.inflate(
                R.layout.chip_presentacion_movimiento, chipsPresAjuste, false
            )
            val card = chipView as MaterialCardView
            chipView.findViewById<TextView>(R.id.tvChipMovimiento)?.text = opcion
            chipView.findViewById<ImageView>(R.id.imgChipMovimiento)?.setImageResource(
                resolverIconoPresentacionMovimiento(opcion)
            )
            chipView.setOnClickListener {
                inputModo.setText(opcion, false)
                if (presentacionesDisponibles.isNotEmpty() &&
                    !opcion.equals(nombreUnidadBaseUi, ignoreCase = true)
                ) {
                    val pres = presentacionesDisponibles.firstOrNull {
                        formatearEtiquetaUi(it.nombre).equals(opcion, ignoreCase = true)
                    } ?: presentacionesDisponibles.firstOrNull()
                    if (pres != null) {
                        inputPresentacion.setText(formatearEtiquetaUi(pres.nombre), false)
                    }
                }
                pintarChipsPresAjuste(opcion)
                actualizarContextoAjuste()
                actualizarResumenAjuste()
            }
            cardsPresAjuste.add(card to opcion)
            chipsPresAjuste.addView(chipView)
        }
        pintarChipsPresAjuste(opcionesModo.firstOrNull().orEmpty())

        // 5) Texto de equivalencia (1 caja = 24 unidades · 1 blíster = 10 unidades)
        run {
            val tvEq = dialogBinding.tvEquivalenciaAjuste
            val partes = presentacionesDisponibles.take(3).map { p ->
                val nombre = formatearEtiquetaUi(p.nombre).lowercase()
                "1 $nombre = ${p.cantidad} ${unidadBase.lowercase()}"
            }
            if (partes.isNotEmpty()) {
                tvEq.text = partes.joinToString(" · ")
                tvEq.visibility = View.VISIBLE
            } else {
                tvEq.visibility = View.GONE
            }
        }

        // 6) Botones +/− del conteo físico
        dialogBinding.btnIncConteoAjuste.setOnClickListener {
            val actual = inputConteoFisico.text?.toString()?.trim()?.toIntOrNull() ?: 0
            inputConteoFisico.setText((actual + 1).toString())
            inputConteoFisico.setSelection(inputConteoFisico.text?.length ?: 0)
        }
        dialogBinding.btnDecConteoAjuste.setOnClickListener {
            val actual = inputConteoFisico.text?.toString()?.trim()?.toIntOrNull() ?: 0
            val nuevo = (actual - 1).coerceAtLeast(0)
            inputConteoFisico.setText(nuevo.toString())
            inputConteoFisico.setSelection(inputConteoFisico.text?.length ?: 0)
        }

        /**
         * Sincroniza el campo "Conteo físico" con los campos compat ocultos:
         * - radioSumar / radioRestar
         * - inputCantidadAjuste (cantidad delta absoluta)
         * - tarjeta resumen (Sistema / Ajuste / Nuevo stock)
         * - texto "Diferencia: ±X unidades"
         */
        fun sincronizarDesdeConteoFisico() {
            val conteo = inputConteoFisico.text?.toString()?.trim()?.toIntOrNull() ?: 0
            val sistema = stockEnSistemaActual
            val diff = conteo - sistema

            // Actualizar campos compat para que la lógica existente funcione
            when {
                diff > 0 -> {
                    if (!dialogBinding.radioSumar.isChecked) dialogBinding.radioSumar.isChecked = true
                    inputCantidad.setText(diff.toString())
                }
                diff < 0 -> {
                    if (!dialogBinding.radioRestar.isChecked) dialogBinding.radioRestar.isChecked = true
                    inputCantidad.setText(kotlin.math.abs(diff).toString())
                }
                else -> {
                    inputCantidad.setText("")
                }
            }

            // Texto "Diferencia: ±X unidades"
            val tvDif = dialogBinding.tvDiferenciaAjuste
            tvDif.text = when {
                diff > 0 -> "Diferencia: +$diff ${unidadBase.lowercase()}"
                diff < 0 -> "Diferencia: $diff ${unidadBase.lowercase()}"
                else -> "Sin diferencia"
            }
            tvDif.setTextColor(when {
                diff > 0 -> 0xFF0E8F63.toInt()
                diff < 0 -> 0xFFB45309.toInt()
                else -> 0xFF667085.toInt()
            })

            // Tarjeta resumen
            dialogBinding.tvSistemaResumenAjuste.text = sistema.toString()
            dialogBinding.tvAjusteResumenAjuste.text = when {
                diff > 0 -> "+$diff"
                diff < 0 -> diff.toString()
                else -> "0"
            }
            dialogBinding.tvAjusteResumenAjuste.setTextColor(when {
                diff > 0 -> 0xFF0E8F63.toInt()
                diff < 0 -> 0xFFB45309.toInt()
                else -> 0xFF667085.toInt()
            })
            dialogBinding.tvNuevoStockResumenAjuste.text = conteo.toString()
        }

        inputConteoFisico.addTextChangedListener {
            sincronizarDesdeConteoFisico()
            actualizarContextoAjuste()
            actualizarResumenAjuste()
            refrescarEstadoBotonAjuste?.invoke()
        }
        sincronizarDesdeConteoFisico()

        // 7) Chips de Motivo (Conteo físico, Corrección, Merma, Vencido)
        run {
            val chipsMotivo = dialogBinding.chipsMotivoAjuste
            chipsMotivo.removeAllViews()
            data class OpcionMotivo(val label: String, val icono: Int, val valorMotivo: String)
            val opciones = listOf(
                OpcionMotivo("Conteo físico", R.drawable.ic_check_circle, "Conteo físico"),
                OpcionMotivo("Corrección", R.drawable.outline_edit_24, "Corrección"),
                OpcionMotivo("Merma", R.drawable.deleteiconitem, "Merma / descarte"),
                OpcionMotivo("Vencido", R.drawable.outline_calendar_month_24, "Vencimiento")
            )
            val cardsMotivo = mutableListOf<Pair<MaterialCardView, String>>()
            fun pintarChipsMotivoAjuste(sel: String) {
                cardsMotivo.forEach { (card, label) ->
                    val isSel = label.equals(sel, ignoreCase = true)
                    card.setCardBackgroundColor(if (isSel) 0xFFE5F4EB.toInt() else 0xFFFFFFFF.toInt())
                    card.strokeColor = if (isSel) 0xFF0E8F63.toInt() else 0xFFE5E7EB.toInt()
                    card.strokeWidth = if (isSel) (2 * resources.displayMetrics.density).toInt()
                        else (1 * resources.displayMetrics.density).toInt()
                    card.findViewById<TextView>(R.id.tvChipMovimiento)?.setTextColor(
                        if (isSel) 0xFF0E8F63.toInt() else 0xFF0F172A.toInt()
                    )
                    card.findViewById<ImageView>(R.id.imgChipMovimiento)?.setColorFilter(
                        if (isSel) 0xFF0E8F63.toInt() else 0xFF667085.toInt()
                    )
                }
            }
            opciones.forEach { op ->
                val chipView = layoutInflater.inflate(
                    R.layout.chip_presentacion_movimiento, chipsMotivo, false
                )
                val card = chipView as MaterialCardView
                chipView.findViewById<TextView>(R.id.tvChipMovimiento)?.text = op.label
                chipView.findViewById<ImageView>(R.id.imgChipMovimiento)?.setImageResource(op.icono)
                chipView.setOnClickListener {
                    inputMotivo.setText(op.valorMotivo, false)
                    pintarChipsMotivoAjuste(op.label)
                    refrescarEstadoBotonAjuste?.invoke()
                }
                cardsMotivo.add(card to op.label)
                chipsMotivo.addView(chipView)
            }
            // Por defecto seleccionar "Conteo físico"
            inputMotivo.setText("Conteo físico", false)
            pintarChipsMotivoAjuste("Conteo físico")
        }

        // 8) Campo Nota → al cambiar, agrega como sufijo del motivo
        dialogBinding.inputNotaAjuste.addTextChangedListener { txt ->
            val motivoBase = inputMotivo.text?.toString()?.substringBefore(" — ").orEmpty().ifBlank { "Conteo físico" }
            val nota = txt?.toString()?.trim().orEmpty()
            inputMotivo.setText(if (nota.isNotBlank()) "$motivoBase — $nota" else motivoBase, false)
        }

        // 9) Tap fuera → ocultar teclado
        dialogBinding.contenidoAjusteStock.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                ocultarTecladoYQuitarFoco()
                v.performClick()
            }
            false
        }

        BottomSheetDialog(this)
            .apply { setContentView(dialogBinding.root) }
            .also { dialog ->
                configurarBackdropBottomSheet(dialog)
                dialog.setCanceledOnTouchOutside(false)
                dialogBinding.btnCancelarAjusteStock.setOnClickListener { dialog.dismiss() }
                dialog.setOnKeyListener { _, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                        when {
                            inputLote.isPopupShowing -> {
                                inputLote.dismissDropDown()
                                true
                            }
                            inputPresentacion.isPopupShowing -> {
                                inputPresentacion.dismissDropDown()
                                true
                            }
                            inputModo.isPopupShowing -> {
                                inputModo.dismissDropDown()
                                true
                            }
                            else -> {
                                dialog.dismiss()
                                true
                            }
                        }
                    } else {
                        false
                    }
                }
                dialog.setOnShowListener {
                    aplicarMargenSuperiorBottomSheet(dialog)
                    val maxWidthPx = (480 * resources.displayMetrics.density).toInt()
                    val screenWidthPx = resources.displayMetrics.widthPixels
                    if (screenWidthPx > maxWidthPx) {
                        dialog.window?.setLayout(maxWidthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
                    }
                    // El diálogo NO se mueve ni achica con el teclado.
                    // El ScrollView interno recibe padding inferior = altura del teclado.
                    dialog.window?.setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
                    )
                    val rootSvAjuste = dialogBinding.root
                    rootSvAjuste.clipToPadding = false
                    val basePadBottomAjuste = rootSvAjuste.paddingBottom
                    ViewCompat.setOnApplyWindowInsetsListener(rootSvAjuste) { v, insets ->
                        val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                        v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, basePadBottomAjuste + imeBottom)
                        insets
                    }
                    fun scrollToFocused(v: View) {
                        rootSvAjuste.postDelayed({
                            val rect = Rect(0, 0, v.width, v.height)
                            v.requestRectangleOnScreen(rect, false)
                        }, 220)
                    }
                    inputCantidad.setOnFocusChangeListener { v, hasFocus ->
                        if (hasFocus) scrollToFocused(v)
                    }

                    val btnAplicar: View = dialogBinding.btnGuardarAjusteStock
                    dialogBinding.tvTituloAjuste.isFocusableInTouchMode = true
                    dialogBinding.tvTituloAjuste.isFocusable = true
                    dialogBinding.tvTituloAjuste.requestFocus()
                    inputLote.clearFocus()
                    inputPresentacion.clearFocus()
                    inputModo.clearFocus()

                    fun actualizarIndicadorScroll() {
                        val puedeSeguirBajando = dialogBinding.root.canScrollVertically(1)
                        if (puedeSeguirBajando) {
                            textIndicadorScroll.text = "Desliza hacia abajo para ver el lote afectado y más opciones."
                            textIndicadorScroll.visibility = View.VISIBLE
                        } else {
                            textIndicadorScroll.visibility = View.GONE
                        }
                    }
                    refrescarIndicadorScrollAjuste = {
                        dialogBinding.root.post { actualizarIndicadorScroll() }
                    }
                    dialogBinding.root.setOnScrollChangeListener { _, _, _, _, _ ->
                        actualizarIndicadorScroll()
                    }

                    fun actualizarEstadoBoton() {
                        val cantidadTexto = inputCantidad.text?.toString()?.trim().orEmpty()
                        val cantidadIngresada = obtenerCantidadAjusteAbsoluta()
                        val cantidadValida = cantidadIngresada > 0
                        val motivoValido = inputMotivo.text?.toString()?.trim().orEmpty().isNotBlank()
                        val modoEsPresentacion = layoutPresentacion.visibility == View.VISIBLE
                        val presentacionValida = !modoEsPresentacion || presentacionSeleccionada() != null
                        val stockActual = binding.editTextCantidad.text?.toString()?.trim()?.toIntOrNull() ?: 0
                        val unidadesPorAjuste = if (modoEsPresentacion) {
                            presentacionSeleccionada()?.cantidad ?: 0
                        } else {
                            1
                        }
                        val deltaUnidades = cantidadIngresada * unidadesPorAjuste.coerceAtLeast(1)
                        val esSuma = esAjusteSuma()
                        val loteExistente = loteExistenteSeleccionado()
                        val loteValido = loteExistente != null
                        val restaValida = esSuma || (cantidadIngresada > 0 && deltaUnidades <= stockActual)
                        val stockLoteValido = esSuma || loteExistente == null || loteExistente.cantidad >= deltaUnidades.toDouble()

                        if (cantidadTexto.isNotBlank() && cantidadIngresada <= 0) {
                            layoutCantidad.error = "Ingresa una cantidad mayor que cero"
                        } else if (!esSuma && cantidadIngresada > 0 && deltaUnidades > stockActual) {
                            layoutCantidad.error =
                                "No puedes descontar más de $stockActual ${formatearEtiquetaUi(unidadBase).lowercase()} disponibles"
                        } else if (!esSuma && loteExistente != null && deltaUnidades.toDouble() > loteExistente.cantidad) {
                            layoutCantidad.error = "La cantidad supera el stock disponible del lote seleccionado"
                        } else if (layoutCantidad.error != null) {
                            layoutCantidad.error = null
                        }
                        val mensajeErrorCantidad = layoutCantidad.error?.toString()
                        val excedeStockEnTiempoReal =
                            !esSuma &&
                                mensajeErrorCantidad != null &&
                                mensajeErrorCantidad != "Ingresa una cantidad mayor que cero"
                        if (excedeStockEnTiempoReal && mensajeErrorCantidad != ultimoMensajeErrorCantidad) {
                            sacudirCampoConError(inputCantidad)
                        }
                        ultimoMensajeErrorCantidad = mensajeErrorCantidad

                        if (modoEsPresentacion && !presentacionValida) {
                            layoutPresentacion.error = "Selecciona una presentación"
                        } else {
                            layoutPresentacion.error = null
                        }

                        btnAplicar?.isEnabled =
                            hayLotesRegistrados && cantidadValida && motivoValido && presentacionValida &&
                                restaValida && loteValido && stockLoteValido
                        btnAplicar?.alpha = if (btnAplicar?.isEnabled == true) 1f else 0.5f
                    }
                    refrescarEstadoBotonAjuste = ::actualizarEstadoBoton

                    inputCantidad.addTextChangedListener {
                        if (!actualizandoCantidadProgramaticamente) {
                            aplicarFormatoCantidadSegunOperacion()
                        }
                        actualizarResumenAjuste()
                        actualizarEstadoBoton()
                    }
                    inputMotivo.addTextChangedListener {
                        layoutMotivo.error = null
                        actualizarEstadoBoton()
                    }

                    actualizarEstadoBoton()
                    dialogBinding.root.post { actualizarIndicadorScroll() }

                    btnAplicar?.setOnClickListener {
                        val cantidadAjuste = obtenerCantidadAjusteAbsoluta()
                        val motivo = inputMotivo.text?.toString()?.trim().orEmpty()
                        val esSuma = esAjusteSuma()
                        val esPorPresentacion = inputModo.text?.toString().orEmpty().equals("Presentación", ignoreCase = true)
                        val presentacionElegida = if (esPorPresentacion) presentacionSeleccionada() else null
                        val stockActual = binding.editTextCantidad.text?.toString()?.trim()?.toIntOrNull() ?: 0
                        val unidadesPorAjuste = if (esPorPresentacion) presentacionElegida?.cantidad ?: 1 else 1
                        val deltaUnidades = cantidadAjuste * unidadesPorAjuste
                        val stockResultante = if (esSuma) stockActual + deltaUnidades else stockActual - deltaUnidades
                        val stockMinimoActual = obtenerStockMinimoActualEnUnidadBase()
                        val numeroLote = numeroLoteActual()
                        val loteExistente = loteExistenteSeleccionado()

                        fun ejecutarAjusteConfirmado() {
                            aplicarAjusteStock(
                                cantidadAjuste = cantidadAjuste,
                                esSuma = esSuma,
                                motivo = motivo,
                                nombreUnidadAjuste = if (esPorPresentacion) {
                                    presentacionElegida?.nombre.orEmpty()
                                } else {
                                    unidadBase
                                },
                                unidadesPorAjuste = unidadesPorAjuste,
                                fuePorPresentacion = esPorPresentacion,
                                numeroLote = loteExistente?.numero.orEmpty(),
                                lotesExistentes = lotesConNumero
                            ) {
                                dialog.dismiss()
                            }
                        }

                        if (cantidadAjuste <= 0) {
                            layoutCantidad.error = "Ingresa una cantidad mayor que cero"
                            inputCantidad.requestFocus()
                            return@setOnClickListener
                        }

                        if (motivo.isBlank()) {
                            layoutMotivo.error = "Ingresa el motivo del ajuste"
                            inputMotivo.requestFocus()
                            return@setOnClickListener
                        }

                        if (esPorPresentacion && presentacionElegida == null) {
                            layoutPresentacion.error = "Selecciona una presentación"
                            return@setOnClickListener
                        }

                        if (!hayLotesRegistrados) {
                            MaterialAlertDialogBuilder(this)
                                .setTitle("No se puede aplicar")
                                .setMessage("Ajustar stock no crea lotes. Primero registra un lote para este producto.")
                                .setPositiveButton("Entendido", null)
                                .show()
                            return@setOnClickListener
                        }

                        if (numeroLote.isBlank()) {
                            layoutLote.error = if (esSuma) {
                                "Debes seleccionar o crear el lote que recibirá el stock"
                            } else {
                                "Debes seleccionar el lote del que se descontará el stock"
                            }
                            if (esSuma) {
                                layoutLote.error = "Debes seleccionar el lote que recibira el stock"
                            }
                            if (opcionesLote.size > 1) inputLote.showDropDown()
                            return@setOnClickListener
                        }

                        if (!esSuma && loteExistente == null) {
                            layoutLote.error = "Debes seleccionar el lote del que se descontará el stock"
                            if (opcionesLote.size > 1) inputLote.showDropDown()
                            return@setOnClickListener
                        }

                        if (esSuma && loteExistente == null) {
                            layoutLote.error = "Debes seleccionar el lote que recibira el stock"
                            if (opcionesLote.size > 1) inputLote.showDropDown()
                            return@setOnClickListener
                        }

                        if (!esSuma && loteExistente != null && loteExistente.cantidad < deltaUnidades.toDouble()) {
                            layoutCantidad.error = "La cantidad supera el stock disponible del lote seleccionado"
                            return@setOnClickListener
                        }

                        if (!esSuma) {
                            if (stockResultante < 0) {
                                MaterialAlertDialogBuilder(this)
                                    .setTitle("No se puede aplicar")
                                    .setMessage("Este ajuste dejaría el stock en $stockResultante $unidadBase (negativo). El stock no puede ser menor que cero.")
                                    .setPositiveButton("Entendido", null)
                                    .show()
                                return@setOnClickListener
                            }

                            val totalTexto = formatearCantidadConEtiqueta(
                                cantidad = deltaUnidades,
                                singular = nombreUnidadBaseUi.lowercase(),
                                plural = pluralizarTexto(nombreUnidadBaseUi.lowercase())
                            )
                            val mensajesConfirmacion = mutableListOf<String>()
                            if (stockResultante == 0) {
                                mensajesConfirmacion +=
                                    "Este ajuste dejará el producto sin stock (0 ${nombreUnidadBaseUi.lowercase()})."
                            } else if (stockMinimoActual > 0 && stockResultante < stockMinimoActual) {
                                mensajesConfirmacion +=
                                    "Esto dejará el stock en $stockResultante ${nombreUnidadBaseUi.lowercase()}, por debajo del mínimo de $stockMinimoActual."
                            }
                            if (stockActual > 0 && deltaUnidades > 0 && deltaUnidades * 2 >= stockActual) {
                                mensajesConfirmacion +=
                                    "Este descuento retirará $totalTexto, que representa más de la mitad del stock actual ($stockActual ${nombreUnidadBaseUi.lowercase()})."
                            }
                            if (mensajesConfirmacion.isNotEmpty()) {
                                MaterialAlertDialogBuilder(this)
                                    .setTitle("Confirma este ajuste")
                                    .setMessage(mensajesConfirmacion.joinToString("\n\n") + "\n\n¿Deseas continuar?")
                                    .setNegativeButton("Cancelar", null)
                                    .setPositiveButton("Confirmar") { _, _ -> ejecutarAjusteConfirmado() }
                                    .show()
                                return@setOnClickListener
                            }
                        }

                        ejecutarAjusteConfirmado()
                    }
                }
            }
            .show()
    }

    private fun mostrarDialogoIngresoStock() {
        // Cargar lotes existentes desde Firebase antes de mostrar el diálogo
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRODUCTOS)
            .child(indiceOriginal)
            .child("lotes")
            .get()
            .addOnSuccessListener { snapshot ->
                val lotesExistentes = mutableListOf<LoteProducto>()
                snapshot.children.forEach { child ->
                    child.getValue(LoteProducto::class.java)?.let { lotesExistentes.add(it) }
                }
                mostrarDialogoIngresoStockConLotes(lotesExistentes)
            }
            .addOnFailureListener {
                mostrarDialogoIngresoStockConLotes(emptyList())
            }
    }

    private fun mostrarDialogoIngresoStockConLotes(lotesExistentes: List<LoteProducto>) {
        val unidadBase = binding.editTextUnidadBase.text.toString().trim().ifBlank { "unidades" }
        val nombreUnidadBaseUi = formatearEtiquetaUi(unidadBase)
        val presentacionesDisponibles = obtenerPresentacionesAjustables(unidadBase)
        val lotesRegistrados = lotesExistentes.filter { it.numero.isNotBlank() }
        val requiereDefinirLote = true
        val requiereVencimientoLote =
            binding.editTextVencimiento.text?.toString()?.trim().orEmpty().isNotBlank() ||
                lotesRegistrados.any { it.vencimiento.isNotBlank() }
        val dialogBinding = DialogIngresoStockBinding.inflate(layoutInflater)
        val layoutModo = dialogBinding.layoutModoIngreso
        val inputModo = dialogBinding.inputModoIngreso
        val layoutPresentacion = dialogBinding.layoutPresentacionIngreso
        val inputPresentacion = dialogBinding.inputPresentacionIngreso
        val layoutCantidad = dialogBinding.layoutCantidadIngreso
        val inputCantidad = dialogBinding.inputCantidadIngreso
        val layoutCosto = dialogBinding.layoutCostoIngreso
        val inputCosto = dialogBinding.inputCostoIngreso
        val textUnidadesCalculadas = dialogBinding.tvUnidadesCalculadasIngreso
        val textResumen = dialogBinding.tvResumenIngreso
        val textCostoResumen = dialogBinding.tvCostoIngresoResumen
        val textStockFinal = dialogBinding.tvStockFinalIngreso
        val textIndicadorScroll = dialogBinding.tvIndicadorScrollIngreso
        val layoutMotivo = dialogBinding.layoutMotivoIngreso
        val inputMotivo = dialogBinding.inputMotivoIngreso
        val layoutDocumento = dialogBinding.layoutDocumentoIngreso
        val inputDocumento = dialogBinding.inputDocumentoIngreso
        val inputNumeroLote = dialogBinding.inputNumeroLoteIngreso
        val inputVencimientoLote = dialogBinding.inputVencimientoLoteIngreso
        var refrescarIndicadorScrollIngreso: (() -> Unit)? = null

        // Configurar autocompletado de lotes existentes
        if (lotesRegistrados.isNotEmpty()) {
            val opcionesLote = lotesRegistrados.map { it.numero }.filter { it.isNotBlank() }
            inputNumeroLote.setAdapter(
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opcionesLote)
            )
            // Al seleccionar un lote existente, auto-llenar el vencimiento
            inputNumeroLote.setOnItemClickListener { _, _, position, _ ->
                val loteSeleccionado = lotesRegistrados.getOrNull(position)
                if (loteSeleccionado != null && loteSeleccionado.vencimiento.isNotBlank()) {
                    inputVencimientoLote.setText(loteSeleccionado.vencimiento)
                }
            }
            dialogBinding.layoutNumeroLoteIngreso.helperText =
                "${opcionesLote.size} lote${if (opcionesLote.size == 1) " existente" else "s existentes"} · elige uno o escribe uno nuevo"
        }

        // Icono ? muestra el dropdown solo cuando el usuario lo toca explícitamente
        dialogBinding.layoutNumeroLoteIngreso.helperText = when {
            lotesRegistrados.isEmpty() -> "Obligatorio: crea el primer lote para este ingreso"
            else -> "Obligatorio: elige un lote existente o escribe uno nuevo"
        }
        dialogBinding.layoutVencimientoLoteIngreso.helperText =
            if (requiereVencimientoLote) {
                "Obligatorio para lotes nuevos. Formato MM/AA y no vencido"
            } else {
                "Opcional para lotes nuevos. Deja en blanco para usar el vencimiento del producto"
            }
        dialogBinding.layoutNumeroLoteIngreso.setEndIconOnClickListener {
            inputNumeroLote.showDropDown()
        }

        // MM/AA auto-format para el campo de vencimiento de lote
        var isFormattingVenc = false
        inputVencimientoLote.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormattingVenc) return
                isFormattingVenc = true
                var input = s.toString().replace("/", "")
                if (input.length == 1 && input.isNotEmpty() && input[0] > '1') input = "0$input"
                if (input.length > 4) input = input.substring(0, 4)
                val formatted = StringBuilder()
                for (i in input.indices) {
                    formatted.append(input[i])
                    if (i == 1 && input.length > 2) formatted.append("/")
                }
                inputVencimientoLote.setText(formatted.toString())
                inputVencimientoLote.setSelection(formatted.length)
                isFormattingVenc = false
            }
        })

        val nombrePresentacion = presentacionesDisponibles.firstOrNull()
            ?.let { formatearEtiquetaUi(it.nombre) } ?: "Presentación"
        val opcionesModo = mutableListOf(nombreUnidadBaseUi)
        if (presentacionesDisponibles.isNotEmpty()) {
            opcionesModo.add(nombrePresentacion)
        }
        inputModo.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opcionesModo)
        )
        layoutModo.hint = "¿Llegó en ${nombreUnidadBaseUi.lowercase()} o en ${nombrePresentacion.lowercase()}?"
        layoutModo.endIconMode = TextInputLayout.END_ICON_CUSTOM
        layoutModo.setEndIconDrawable(R.drawable.ic_arrow_drop_down)
        layoutModo.endIconContentDescription = "Ver opciones"
        layoutModo.setEndIconOnClickListener { inputModo.showDropDown() }
        inputModo.inputType = InputType.TYPE_NULL
        inputModo.keyListener = null
        inputModo.isCursorVisible = false
        inputModo.isFocusable = false
        inputModo.isFocusableInTouchMode = false
        inputModo.setOnClickListener { inputModo.showDropDown() }
        inputModo.setText(opcionesModo.first(), false)
        // El dropdown "¿Llegó en unidad o en caja?" se reemplaza por las chip-cards de Presentación
        // arriba — siempre oculto. Solo sirve como almacén de estado para la lógica existente.
        layoutModo.visibility = View.GONE
        inputMotivo.setText("Compra")

        val nombresPresentaciones = presentacionesDisponibles.map { formatearEtiquetaUi(it.nombre) }
        inputPresentacion.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nombresPresentaciones)
        )
        layoutPresentacion.endIconMode = TextInputLayout.END_ICON_CUSTOM
        layoutPresentacion.setEndIconDrawable(R.drawable.ic_arrow_drop_down)
        layoutPresentacion.endIconContentDescription = "Ver presentaciones"
        layoutPresentacion.setEndIconOnClickListener { inputPresentacion.showDropDown() }
        inputPresentacion.inputType = InputType.TYPE_NULL
        inputPresentacion.keyListener = null
        inputPresentacion.isCursorVisible = false
        inputPresentacion.isFocusable = false
        inputPresentacion.isFocusableInTouchMode = false
        inputPresentacion.setOnClickListener { inputPresentacion.showDropDown() }
        if (nombresPresentaciones.isNotEmpty()) {
            inputPresentacion.setText(nombresPresentaciones.first(), false)
        }

        fun presentacionSeleccionada(): PresentacionProducto? {
            val nombre = inputPresentacion.text?.toString()?.trim().orEmpty()
            return presentacionesDisponibles.firstOrNull {
                formatearEtiquetaUi(it.nombre).equals(nombre, ignoreCase = true)
            }
        }

        fun loteExistenteSeleccionado(): LoteProducto? {
            val numero = inputNumeroLote.text?.toString()?.trim().orEmpty()
            return lotesRegistrados.firstOrNull { it.numero.trim().equals(numero, ignoreCase = true) }
        }

        fun costoIngresoSeleccionado(): Double? {
            return inputCosto.text?.toString()
                ?.trim()
                ?.replace(",", ".")
                ?.toDoubleOrNull()
                ?.takeIf { it > 0.0 }
        }

        fun errorVencimientoLote(vencimiento: String, obligatorio: Boolean): String? {
            if (vencimiento.isBlank()) {
                return if (obligatorio) "Ingresa el vencimiento del nuevo lote" else null
            }
            val dias = diasHastaVencerLote(vencimiento)
            return when {
                dias == null -> "Ingresa una fecha valida en formato MM/AA"
                dias < 0 -> "El vencimiento del lote no puede ser anterior a hoy"
                else -> null
            }
        }

        fun actualizarResumenIngreso() {
            val cantidad = inputCantidad.text?.toString()?.trim()?.toIntOrNull()
            if (cantidad == null || cantidad <= 0) {
                textUnidadesCalculadas.visibility = View.GONE
                textUnidadesCalculadas.text = ""
                textResumen.visibility = View.GONE
                textResumen.text = ""
                textCostoResumen.visibility = View.GONE
                textCostoResumen.text = ""
                textStockFinal.visibility = View.GONE
                textStockFinal.text = ""

                // Mostrar la tarjeta resumen con stock actual aunque no haya cantidad
                val stockActual = binding.editTextCantidad.text?.toString()?.trim()?.toIntOrNull() ?: 0
                dialogBinding.tvStockActualIngreso.text = stockActual.toString()
                dialogBinding.tvCambioStockIngreso.text = "+0"
                dialogBinding.tvNuevoStockIngreso.text = stockActual.toString()
                dialogBinding.cardResumenStockIngreso.visibility = View.VISIBLE

                refrescarIndicadorScrollIngreso?.invoke()
                return
            }

            val esPorPresentacion = inputModo.text?.toString().orEmpty().trim()
                .let { it.isNotEmpty() && !it.equals(nombreUnidadBaseUi, ignoreCase = true) && presentacionesDisponibles.isNotEmpty() }
            val presentacion = if (esPorPresentacion) presentacionSeleccionada() else null
            val nombreIngreso = if (esPorPresentacion) {
                presentacion?.nombre?.trim().orEmpty().ifBlank { "presentación" }
            } else {
                unidadBase
            }
            val unidadesPorIngreso = if (esPorPresentacion) presentacion?.cantidad ?: 0 else 1

            if (esPorPresentacion && unidadesPorIngreso <= 0) {
                textUnidadesCalculadas.visibility = View.GONE
                textUnidadesCalculadas.text = ""
                textResumen.visibility = View.GONE
                textResumen.text = ""
                textCostoResumen.visibility = View.GONE
                textCostoResumen.text = ""
                textStockFinal.visibility = View.GONE
                textStockFinal.text = ""
                refrescarIndicadorScrollIngreso?.invoke()
                return
            }

            val totalUnidades = cantidad * unidadesPorIngreso.coerceAtLeast(1)
            val costoIngreso = costoIngresoSeleccionado()
            val stockActual = binding.editTextCantidad.text?.toString()?.trim()?.toIntOrNull() ?: 0
            val stockFinal = stockActual + totalUnidades
            val cantidadTexto = formatearCantidadConEtiqueta(
                cantidad = cantidad,
                singular = nombreIngreso.lowercase(),
                plural = pluralizarTexto(nombreIngreso.lowercase())
            )
            val totalTexto = formatearCantidadConEtiqueta(
                cantidad = totalUnidades,
                singular = unidadBase.lowercase(),
                plural = pluralizarTexto(unidadBase.lowercase())
            )

            textUnidadesCalculadas.text = if (esPorPresentacion) {
                "Equivalencia del movimiento: $cantidadTexto = $totalTexto base"
            } else {
                "Movimiento actual en unidad base: $totalTexto"
            }
            textUnidadesCalculadas.visibility = View.VISIBLE

            textResumen.text = if (esPorPresentacion) {
                "Entrarán $cantidadTexto = $totalTexto"
            } else {
                "Entrarán $totalTexto"
            }
            textResumen.visibility = View.VISIBLE
            textStockFinal.text = "Stock actual: $stockActual. Stock final esperado: $stockFinal"
            textStockFinal.visibility = View.VISIBLE
            textResumen.text = if (esPorPresentacion) {
                "Cantidad a ingresar en este movimiento: $cantidadTexto"
            } else {
                "Cantidad a ingresar en este movimiento: $totalTexto"
            }
            textResumen.visibility = View.VISIBLE
            if (costoIngreso != null) {
                val costoUnitario = costoIngreso / totalUnidades.toDouble().coerceAtLeast(1.0)
                textCostoResumen.text =
                    "Costo total de este lote: ${MonedaHelper.formatear(costoIngreso)} · aprox ${MonedaHelper.formatear(costoUnitario)} por ${unidadBase.lowercase()}"
                textCostoResumen.visibility = View.VISIBLE
            } else {
                textCostoResumen.visibility = View.GONE
                textCostoResumen.text = ""
            }
            textStockFinal.text =
                "Stock actual del producto: $stockActual $unidadBase\nStock final esperado despues del ingreso: $stockFinal $unidadBase"

            // Actualizar tarjeta resumen visible (Stock actual / +X / Nuevo stock)
            dialogBinding.tvStockActualIngreso.text = stockActual.toString()
            dialogBinding.tvCambioStockIngreso.text = "+$totalUnidades"
            dialogBinding.tvNuevoStockIngreso.text = stockFinal.toString()
            dialogBinding.cardResumenStockIngreso.visibility = View.VISIBLE

            refrescarIndicadorScrollIngreso?.invoke()
        }

        fun actualizarContextoIngreso() {
            val modoActual = inputModo.text?.toString().orEmpty().trim()
            val esPresentacion = modoActual.isNotEmpty() &&
                !modoActual.equals(nombreUnidadBaseUi, ignoreCase = true) &&
                presentacionesDisponibles.isNotEmpty()
            // Si solo hay una presentación, seleccionarla automáticamente (sin dropdown)
            if (esPresentacion && presentacionesDisponibles.size == 1 && inputPresentacion.text.isNullOrBlank()) {
                inputPresentacion.setText(formatearEtiquetaUi(presentacionesDisponibles.first().nombre), false)
            }
            // El dropdown secundario de Presentación queda siempre oculto — las chip-cards arriba
            // ya incluyen cada presentación individual, así que esto sería redundante
            layoutPresentacion.visibility = View.GONE

            layoutPresentacion.helperText = when {
                !esPresentacion -> null
                presentacionSeleccionada() != null -> {
                    val presentacion = presentacionSeleccionada()!!
                    val nombrePresentacion = formatearEtiquetaUi(presentacion.nombre).lowercase()
                    val unidadesTexto = formatearCantidadConEtiqueta(
                        cantidad = presentacion.cantidad,
                        singular = unidadBase.lowercase(),
                        plural = pluralizarTexto(unidadBase.lowercase())
                    )
                    "Equivalencia: 1 $nombrePresentacion = $unidadesTexto"
                }
                else -> "Selecciona una presentacion para ver su equivalencia"
            }

            val etiqueta = if (esPresentacion) {
                presentacionSeleccionada()?.nombre?.trim().orEmpty().ifBlank { "presentación" }
            } else {
                unidadBase
            }
            layoutCantidad.hint = if (esPresentacion) {
                "Cuantas ${pluralizarTexto(etiqueta.lowercase())} ingresaran"
            } else {
                "Cantidad que llegó en ${etiqueta.lowercase()}"
            }
            layoutCosto.hint = if (esPresentacion) {
                "Costo total de este lote"
            } else {
                "Costo total de este ingreso"
            }
            layoutCosto.helperText = if (esPresentacion) {
                "Ingresa cuanto pagaste por las ${pluralizarTexto(etiqueta.lowercase())} que estan entrando en este lote."
            } else {
                "Ingresa cuanto pagaste por la mercaderia que esta entrando."
            }
            actualizarResumenIngreso()
        }

        var refrescarEstadoBotonIngreso: (() -> Unit)? = null

        inputModo.setOnItemClickListener { _, _, _, _ ->
            layoutModo.error = null
            actualizarContextoIngreso()
            refrescarEstadoBotonIngreso?.invoke()
        }
        inputPresentacion.setOnItemClickListener { _, _, _, _ ->
            layoutPresentacion.error = null
            actualizarContextoIngreso()
            refrescarEstadoBotonIngreso?.invoke()
        }

        // HUMANO: Neutralizar el OnFocusChangeListener interno del
        // MaterialAutoCompleteTextView (ExposedDropdownMenu). Ese listener
        // llama showDropDown() cada vez que el widget gana foco. Con gesto
        // físico de atrás, Android hace un ciclo de foco antes de entregar
        // el evento al diálogo, y ese ciclo re-enfoca el autocomplete =
        // se abre el dropdown en vez de cerrar el diálogo.
        // Si es null, el único camino para abrir el dropdown queda el tap
        // (OnClickListener interno), que es el comportamiento deseado.
        inputModo.onFocusChangeListener = null
        inputPresentacion.onFocusChangeListener = null
        dialogBinding.inputNumeroLoteIngreso.onFocusChangeListener = null

        // Refuerzo: si por cualquier motivo el widget recupera foco,
        // cerramos el dropdown explícitamente en vez de dejar que se abra.
        val cerrarDropdownEnFoco = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                (v as? MaterialAutoCompleteTextView)?.dismissDropDown()
            }
        }
        inputModo.onFocusChangeListener = cerrarDropdownEnFoco
        inputPresentacion.onFocusChangeListener = cerrarDropdownEnFoco
        dialogBinding.inputNumeroLoteIngreso.onFocusChangeListener = cerrarDropdownEnFoco

        actualizarContextoIngreso()

        // Cabecera: nombre del producto + imagen
        dialogBinding.tvNombreProductoIngreso.text =
            binding.editTextNombre.text?.toString()?.trim().orEmpty().ifBlank { "Producto" }
        dialogBinding.tvSubtituloIngreso.text = "Selecciona presentación y cantidad"
        dialogBinding.tvSubtituloIngreso.visibility = View.VISIBLE

        // Construir chips de presentación (visualmente reemplazan al dropdown oculto)
        val chipsContainer = dialogBinding.chipsPresentacionIngreso
        val cardsChips = mutableListOf<Pair<MaterialCardView, String>>()
        chipsContainer.removeAllViews()
        fun pintarChipsIngreso(seleccionado: String) {
            cardsChips.forEach { (card, opcion) ->
                val sel = opcion.equals(seleccionado, ignoreCase = true)
                card.setCardBackgroundColor(if (sel) 0xFFE5F4EB.toInt() else 0xFFFFFFFF.toInt())
                card.strokeColor = if (sel) 0xFF0E8F63.toInt() else 0xFFE5E7EB.toInt()
                card.strokeWidth = if (sel) (2 * resources.displayMetrics.density).toInt()
                    else (1 * resources.displayMetrics.density).toInt()
                val tv = card.findViewById<TextView>(R.id.tvChipMovimiento)
                tv?.setTextColor(if (sel) 0xFF0E8F63.toInt() else 0xFF0F172A.toInt())
                val img = card.findViewById<ImageView>(R.id.imgChipMovimiento)
                img?.setColorFilter(if (sel) 0xFF0E8F63.toInt() else 0xFF667085.toInt())
            }
        }
        opcionesModo.forEach { opcion ->
            val chipView = layoutInflater.inflate(R.layout.chip_presentacion_movimiento, chipsContainer, false)
            val card = chipView as MaterialCardView
            chipView.findViewById<TextView>(R.id.tvChipMovimiento)?.text = opcion
            chipView.findViewById<ImageView>(R.id.imgChipMovimiento)?.setImageResource(
                resolverIconoPresentacionMovimiento(opcion)
            )
            chipView.setOnClickListener {
                inputModo.setText(opcion, false)
                if (presentacionesDisponibles.isNotEmpty() &&
                    !opcion.equals(nombreUnidadBaseUi, ignoreCase = true)
                ) {
                    val pres = presentacionesDisponibles.firstOrNull {
                        formatearEtiquetaUi(it.nombre).equals(opcion, ignoreCase = true)
                    }
                    if (pres != null) {
                        inputPresentacion.setText(formatearEtiquetaUi(pres.nombre), false)
                    }
                }
                actualizarContextoIngreso()
                pintarChipsIngreso(opcion)
            }
            cardsChips.add(card to opcion)
            chipsContainer.addView(chipView)
        }
        pintarChipsIngreso(opcionesModo.firstOrNull().orEmpty())

        // Botones +/− para sumar/restar la cantidad (también permite escribir)
        dialogBinding.btnIncCantidadIngreso.setOnClickListener {
            val actual = inputCantidad.text?.toString()?.trim()?.toIntOrNull() ?: 0
            inputCantidad.setText((actual + 1).toString())
            inputCantidad.setSelection(inputCantidad.text?.length ?: 0)
        }
        dialogBinding.btnDecCantidadIngreso.setOnClickListener {
            val actual = inputCantidad.text?.toString()?.trim()?.toIntOrNull() ?: 0
            val nuevo = (actual - 1).coerceAtLeast(0)
            inputCantidad.setText(if (nuevo == 0) "" else nuevo.toString())
            inputCantidad.setSelection(inputCantidad.text?.length ?: 0)
        }

        // Tap fuera del campo → ocultar teclado
        dialogBinding.contenidoIngresoStock.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                ocultarTecladoYQuitarFoco()
                v.performClick()
            }
            false
        }

        BottomSheetDialog(this)
            .apply { setContentView(dialogBinding.root) }
            .also { dialog ->
                configurarBackdropBottomSheet(dialog)
                dialog.setCanceledOnTouchOutside(false)
                // HUMANO: Interceptar el botón BACK a nivel de diálogo.
                // Problema: los MaterialAutoCompleteTextView (ExposedDropdownMenu)
                // tienen un OnFocusChangeListener interno que dispara showDropDown()
                // cuando recuperan foco. El back del sistema hace que la IME/foco
                // pasen por un ciclo que re-enfoca el autocomplete y termina
                // expandiéndolo en vez de cerrar el diálogo. Acá decidimos:
                // - Si algún popup de autocomplete está visible, lo cerramos solo
                //   (y consumimos el evento).
                // - Si no, cerramos el diálogo (comportamiento esperado).
                dialog.setOnKeyListener { _, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                        when {
                            inputModo.isPopupShowing -> {
                                inputModo.dismissDropDown()
                                true
                            }
                            inputPresentacion.isPopupShowing -> {
                                inputPresentacion.dismissDropDown()
                                true
                            }
                            dialogBinding.inputNumeroLoteIngreso.isPopupShowing -> {
                                dialogBinding.inputNumeroLoteIngreso.dismissDropDown()
                                true
                            }
                            else -> {
                                dialog.dismiss()
                                true
                            }
                        }
                    } else {
                        false
                    }
                }
                dialog.setOnShowListener {
                    aplicarMargenSuperiorBottomSheet(dialog)
                    val maxWidthPx = (480 * resources.displayMetrics.density).toInt()
                    val screenWidthPx = resources.displayMetrics.widthPixels
                    if (screenWidthPx > maxWidthPx) {
                        dialog.window?.setLayout(maxWidthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
                    }
                    // El diálogo NO se mueve ni achica con el teclado.
                    // El ScrollView interno recibe padding inferior = altura del teclado.
                    dialog.window?.setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
                    )
                    val rootSvIngreso = dialogBinding.root
                    rootSvIngreso.clipToPadding = false
                    val basePadBottomIngreso = rootSvIngreso.paddingBottom
                    ViewCompat.setOnApplyWindowInsetsListener(rootSvIngreso) { v, insets ->
                        val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                        v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, basePadBottomIngreso + imeBottom)
                        insets
                    }
                    fun scrollToFocused(v: View) {
                        rootSvIngreso.postDelayed({
                            val rect = Rect(0, 0, v.width, v.height)
                            v.requestRectangleOnScreen(rect, false)
                        }, 220)
                    }
                    inputCantidad.setOnFocusChangeListener { v, hasFocus ->
                        if (hasFocus) scrollToFocused(v)
                    }
                    inputNumeroLote.setOnFocusChangeListener { v, hasFocus ->
                        if (hasFocus) scrollToFocused(v)
                    }
                    inputVencimientoLote.setOnFocusChangeListener { v, hasFocus ->
                        if (hasFocus) scrollToFocused(v)
                    }

                    val btnGuardar: View = dialogBinding.btnGuardarIngresoStock
                    dialogBinding.btnCancelarIngresoStock.setOnClickListener { dialog.dismiss() }
                    val helperLoteIngresoPorDefecto =
                        dialogBinding.layoutNumeroLoteIngreso.helperText?.toString().orEmpty()
                    val helperVencimientoIngresoPorDefecto =
                        dialogBinding.layoutVencimientoLoteIngreso.helperText?.toString().orEmpty()
                    var actualizandoVencimientoIngresoProgramaticamente = false
                    var loteAutocompletadoIngreso: LoteProducto? = null
                    var vencimientoAutocompletadoDesdeLote = false

                    dialogBinding.tvTituloIngreso.isFocusableInTouchMode = true
                    dialogBinding.tvTituloIngreso.isFocusable = true
                    dialogBinding.tvTituloIngreso.requestFocus()
                    inputModo.clearFocus()
                    inputPresentacion.clearFocus()

                    fun actualizarIndicadorScroll() {
                        val puedeSeguirBajando = dialogBinding.root.canScrollVertically(1)
                        if (puedeSeguirBajando) {
                            textIndicadorScroll.text = if (
                                dialogBinding.layoutNumeroLoteIngreso.visibility == View.VISIBLE ||
                                dialogBinding.cardEstadoLoteIngreso.visibility == View.VISIBLE
                            ) {
                                "Desliza hacia abajo para ver mas y revisar el lote."
                            } else {
                                "Desliza hacia abajo para ver mas opciones."
                            }
                            textIndicadorScroll.visibility = View.VISIBLE
                        } else {
                            textIndicadorScroll.visibility = View.GONE
                        }
                    }
                    refrescarIndicadorScrollIngreso = {
                        dialogBinding.root.post { actualizarIndicadorScroll() }
                    }
                    dialogBinding.root.setOnScrollChangeListener { _, _, _, _, _ ->
                        actualizarIndicadorScroll()
                    }

                    fun actualizarEstadoBoton() {
                        val cantidadValida =
                            inputCantidad.text?.toString()?.trim()?.toIntOrNull()?.let { it > 0 } == true
                        val presentacionValida = layoutPresentacion.visibility != View.VISIBLE || presentacionSeleccionada() != null
                        val loteValido = inputNumeroLote.text?.toString()?.trim().orEmpty().isNotBlank()
                        btnGuardar?.isEnabled = cantidadValida && presentacionValida && loteValido
                        btnGuardar?.alpha = if (btnGuardar?.isEnabled == true) 1f else 0.5f
                    }
                    fun actualizarEstadoLoteIngreso(
                        lote: LoteProducto?,
                        autocompletarVencimiento: Boolean = false
                    ) {
                        val numeroLoteActual =
                            inputNumeroLote.text?.toString()?.trim().orEmpty()
                        val esLoteExistente = lote != null
                        val esLoteNuevo = lote == null && numeroLoteActual.isNotBlank()
                        dialogBinding.layoutNumeroLoteIngreso.helperText = if (lote != null) {
                            "Este lote ya existe. El ingreso se guardara en ese lote."
                        } else {
                            helperLoteIngresoPorDefecto
                        }

                        dialogBinding.cardEstadoLoteIngreso.visibility = if (esLoteExistente || esLoteNuevo) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }

                        when {
                            esLoteExistente -> {
                                dialogBinding.cardEstadoLoteIngreso.setCardBackgroundColor(
                                    ColorStateList.valueOf(
                                        Color.parseColor("#EEF2FF")
                                    )
                                )
                                dialogBinding.tvEstadoLoteIngreso.setTextColor(
                                    Color.parseColor("#4338CA")
                                )
                                dialogBinding.tvEstadoLoteIngreso.text = "Lote existente"
                            }
                            esLoteNuevo -> {
                                dialogBinding.cardEstadoLoteIngreso.setCardBackgroundColor(
                                    ColorStateList.valueOf(
                                        Color.parseColor("#ECFDF3")
                                    )
                                )
                                dialogBinding.tvEstadoLoteIngreso.setTextColor(
                                    Color.parseColor("#027A48")
                                )
                                dialogBinding.tvEstadoLoteIngreso.text = "Lote nuevo"
                            }
                        }

                        dialogBinding.layoutVencimientoLoteIngreso.helperText = when {
                            lote == null && numeroLoteActual.isNotBlank() && requiereVencimientoLote ->
                                "Nuevo lote detectado. Escribe el vencimiento de este lote."
                            lote == null && numeroLoteActual.isNotBlank() ->
                                "Nuevo lote detectado. Si quieres, registra tambien su vencimiento."
                            lote == null -> helperVencimientoIngresoPorDefecto
                            lote.vencimiento.isNotBlank() ->
                                "Lote existente detectado. Si borras el vencimiento, el campo quedara vacio en este formulario."
                            else -> "Este lote ya existe y aun no tiene vencimiento registrado."
                        }

                        val loteAutocompletadoAnterior = loteAutocompletadoIngreso
                        val textoVencimientoActual =
                            inputVencimientoLote.text?.toString()?.trim().orEmpty()
                        val vencimientoAutocompletadoAnterior =
                            loteAutocompletadoAnterior?.vencimiento?.trim().orEmpty()

                        if (
                            loteAutocompletadoAnterior != null &&
                            !loteAutocompletadoAnterior.numero.trim()
                                .equals(lote?.numero?.trim().orEmpty(), ignoreCase = true) &&
                            textoVencimientoActual == vencimientoAutocompletadoAnterior
                        ) {
                            actualizandoVencimientoIngresoProgramaticamente = true
                            inputVencimientoLote.text?.clear()
                            actualizandoVencimientoIngresoProgramaticamente = false
                            vencimientoAutocompletadoDesdeLote = false
                            loteAutocompletadoIngreso = null
                        }

                        val debeMostrarVencimientoExistente =
                            lote?.vencimiento?.isNotBlank() == true &&
                                (
                                    autocompletarVencimiento ||
                                        textoVencimientoActual.isBlank() ||
                                        textoVencimientoActual == vencimientoAutocompletadoAnterior
                                    )

                        if (debeMostrarVencimientoExistente) {
                            actualizandoVencimientoIngresoProgramaticamente = true
                            inputVencimientoLote.setText(lote.vencimiento)
                            actualizandoVencimientoIngresoProgramaticamente = false
                            loteAutocompletadoIngreso = lote
                            vencimientoAutocompletadoDesdeLote = true
                        } else if (lote == null) {
                            loteAutocompletadoIngreso = null
                            vencimientoAutocompletadoDesdeLote = false
                        }

                        val vencimientoEditable = !esLoteExistente
                        dialogBinding.layoutVencimientoLoteIngreso.isEnabled = true
                        inputVencimientoLote.isEnabled = vencimientoEditable
                        inputVencimientoLote.isFocusable = vencimientoEditable
                        inputVencimientoLote.isFocusableInTouchMode = vencimientoEditable
                        inputVencimientoLote.isClickable = vencimientoEditable
                        dialogBinding.layoutVencimientoLoteIngreso.endIconMode =
                            if (vencimientoEditable) TextInputLayout.END_ICON_NONE
                            else TextInputLayout.END_ICON_CUSTOM
                        if (!vencimientoEditable) {
                            dialogBinding.layoutVencimientoLoteIngreso.setEndIconDrawable(R.drawable.ic_lock)
                            dialogBinding.layoutVencimientoLoteIngreso.endIconContentDescription =
                                "Vencimiento solo informativo"
                        }
                        refrescarIndicadorScrollIngreso?.invoke()
                    }
                    refrescarEstadoBotonIngreso = ::actualizarEstadoBoton

                    inputCantidad.addTextChangedListener {
                        layoutCantidad.error = null
                        actualizarResumenIngreso()
                        actualizarEstadoBoton()
                    }
                    inputCosto.addTextChangedListener {
                        layoutCosto.error = null
                        actualizarResumenIngreso()
                        actualizarEstadoBoton()
                    }
                    inputNumeroLote.addTextChangedListener {
                        dialogBinding.layoutNumeroLoteIngreso.error = null
                        dialogBinding.layoutVencimientoLoteIngreso.error = null
                        val lote = loteExistenteSeleccionado()
                        actualizarEstadoLoteIngreso(lote, autocompletarVencimiento = false)
                        actualizarEstadoBoton()
                    }
                    inputVencimientoLote.addTextChangedListener {
                        dialogBinding.layoutVencimientoLoteIngreso.error = null
                        if (!actualizandoVencimientoIngresoProgramaticamente) {
                            vencimientoAutocompletadoDesdeLote = false
                        }
                        actualizarEstadoBoton()
                    }
                    inputMotivo.addTextChangedListener {
                        layoutMotivo.error = null
                        actualizarEstadoBoton()
                    }
                    inputDocumento.addTextChangedListener {
                        layoutDocumento.error = null
                    }

                    inputNumeroLote.setOnItemClickListener { _, _, _, _ ->
                        dialogBinding.layoutNumeroLoteIngreso.error = null
                        dialogBinding.layoutVencimientoLoteIngreso.error = null
                        val lote = loteExistenteSeleccionado()
                        actualizarEstadoLoteIngreso(lote, autocompletarVencimiento = true)
                        actualizarEstadoBoton()
                    }

                    actualizarEstadoLoteIngreso(loteExistenteSeleccionado(), autocompletarVencimiento = false)
                    actualizarEstadoBoton()
                    dialogBinding.root.post { actualizarIndicadorScroll() }

                    btnGuardar?.setOnClickListener {
                        val cantidad = inputCantidad.text?.toString()?.trim().orEmpty().toIntOrNull()
                        val costoIngreso = inputCosto.text?.toString()?.trim()?.replace(",", ".")?.toDoubleOrNull()
                        val motivo = inputMotivo.text?.toString()?.trim().orEmpty()
                        val documento = inputDocumento.text?.toString()?.trim().orEmpty()
                        val esPorPresentacion = inputModo.text?.toString().orEmpty().trim()
                            .let { it.isNotEmpty() && !it.equals(nombreUnidadBaseUi, ignoreCase = true) && presentacionesDisponibles.isNotEmpty() }
                        val presentacion = if (esPorPresentacion) presentacionSeleccionada() else null

                        if (cantidad == null || cantidad <= 0) {
                            layoutCantidad.error = "Escribe una cantidad válida"
                            inputCantidad.requestFocus()
                            return@setOnClickListener
                        }

                        if (costoIngreso == null || costoIngreso <= 0.0) {
                            layoutCosto.error = "Ingresa un costo valido para este ingreso"
                            inputCosto.requestFocus()
                            return@setOnClickListener
                        }

                        if (motivo.isBlank()) {
                            layoutMotivo.error = "Cuéntanos por qué está entrando este stock"
                            inputMotivo.requestFocus()
                            return@setOnClickListener
                        }

                        if (esPorPresentacion && presentacion == null) {
                            layoutPresentacion.error = "Selecciona una presentación"
                            return@setOnClickListener
                        }

                        val numeroLoteIngresado = dialogBinding.inputNumeroLoteIngreso.text?.toString()?.trim().orEmpty()
                        val loteExistente = lotesRegistrados.firstOrNull {
                            it.numero.trim().equals(numeroLoteIngresado, ignoreCase = true)
                        }
                        val numeroLote = numeroLoteIngresado
                        val usandoLoteExistente = numeroLote.isNotBlank() &&
                            lotesRegistrados.any { it.numero.trim().equals(numeroLote, ignoreCase = true) }
                        val creandoLoteNuevo = numeroLote.isNotBlank() && !usandoLoteExistente
                        val vencimientoLote = when {
                            loteExistente != null -> loteExistente.vencimiento
                            else -> dialogBinding.inputVencimientoLoteIngreso.text?.toString()?.trim().orEmpty()
                                .ifBlank { binding.editTextVencimiento.text?.toString()?.trim().orEmpty() }
                        }

                        if (requiereDefinirLote && numeroLote.isBlank()) {
                            dialogBinding.layoutNumeroLoteIngreso.error = "Selecciona un lote existente o crea uno nuevo"
                            inputNumeroLote.requestFocus()
                            return@setOnClickListener
                        }

                        if (creandoLoteNuevo) {
                            if (numeroLote.isBlank()) {
                                dialogBinding.layoutNumeroLoteIngreso.error = "Ingresa el numero del nuevo lote"
                                inputNumeroLote.requestFocus()
                                return@setOnClickListener
                            }
                            val errorVencimiento = errorVencimientoLote(vencimientoLote, requiereVencimientoLote)
                            if (errorVencimiento != null) {
                                dialogBinding.layoutVencimientoLoteIngreso.error = errorVencimiento
                                inputVencimientoLote.requestFocus()
                                return@setOnClickListener
                            }
                        }

                        registrarIngresoStock(
                            cantidadIngresada = cantidad,
                            costoIngresoTotal = costoIngreso ?: 0.0,
                            motivo = motivo,
                            documento = documento,
                            nombreUnidadIngreso = if (esPorPresentacion) {
                                presentacion?.nombre.orEmpty()
                            } else {
                                unidadBase
                            },
                            unidadesPorIngreso = if (esPorPresentacion) {
                                presentacion?.cantidad ?: 1
                            } else {
                                1
                            },
                            fuePorPresentacion = esPorPresentacion,
                            numeroLote = numeroLote,
                            vencimientoLote = vencimientoLote,
                            lotesExistentes = lotesRegistrados,
                            requiereLote = requiereDefinirLote,
                            requiereVencimientoLote = requiereVencimientoLote
                        ) {
                            dialog.dismiss()
                        }
                    }
                }
            }
            .show()
    }

    private fun mostrarDialogoGestionarLotes() {
        val dialogBinding = DialogGestionarLotesBinding.inflate(layoutInflater)
        val container = dialogBinding.containerLotes
        val tvSubtitulo = dialogBinding.tvSubtituloLotes
        val tvConteo = dialogBinding.tvConteoLotes
        val layoutVacio = dialogBinding.layoutLotesVacio

        // Leer lotes desde Firebase
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRODUCTOS)
            .child(indiceOriginal)
            .child("lotes")
            .get()
            .addOnSuccessListener { snapshot ->
                val lotes = mutableListOf<Pair<String, LoteProducto>>()
                snapshot.children.forEach { child ->
                    val lote = child.getValue(LoteProducto::class.java)
                    if (lote != null) lotes.add(child.key.orEmpty() to lote)
                }
                actualizarVisibilidadVencimientoGeneral(lotes.count { it.second.numero.isNotBlank() })
                verificarVencimiento()
                // Ordenar por vencimiento
                lotes.sortBy { it.second.vencimiento }

                if (lotes.isEmpty()) {
                    layoutVacio.visibility = View.VISIBLE
                    tvSubtitulo.text = "Sin lotes registrados"
                    tvConteo.text = "0 lotes"
                } else {
                    layoutVacio.visibility = View.GONE
                    val unidadBase = binding.editTextUnidadBase.text.toString().trim().ifBlank { "unidades" }

                    fun badgeBg(colorHex: String): android.graphics.drawable.GradientDrawable {
                        val dp = resources.displayMetrics.density
                        return android.graphics.drawable.GradientDrawable().apply {
                            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                            cornerRadius = 50 * dp
                            setColor(Color.parseColor(colorHex))
                        }
                    }

                    // Resumen en subtítulo: cuántos vencidos / por vencer
                    val vencidos = lotes.count { (_, l) -> diasHastaVencerLote(l.vencimiento)?.let { it < 0 } == true }
                    val porVencer = lotes.count { (_, l) -> diasHastaVencerLote(l.vencimiento)?.let { it in 0..30 } == true }
                    tvSubtitulo.text = when {
                        vencidos > 0 -> "$vencidos vencido${if (vencidos > 1) "s" else ""} · próximo: ${lotes.first().second.vencimiento.ifBlank { "—" }}"
                        porVencer > 0 -> "$porVencer por vencer pronto · próximo: ${lotes.first().second.vencimiento.ifBlank { "—" }}"
                        else -> "Próximo a vencer: ${lotes.first().second.vencimiento.ifBlank { "—" }}"
                    }
                    tvConteo.text = "${lotes.size} lote${if (lotes.size == 1) "" else "s"}"

                    lotes.forEach { (clave, lote) ->
                        val rowBinding = ItemLoteBinding.inflate(layoutInflater, container, false)
                        val dias = diasHastaVencerLote(lote.vencimiento)

                        // Barra de color + badge de estado
                        when {
                            dias == null -> {
                                rowBinding.barraEstadoLote.setBackgroundColor(Color.parseColor("#D1D5DB"))
                                rowBinding.tvEstadoLote.text = "Sin fecha"
                                rowBinding.tvEstadoLote.setTextColor(Color.parseColor("#6B7280"))
                                rowBinding.tvEstadoLote.background = badgeBg("#F3F4F6")
                            }
                            dias < 0 -> {
                                rowBinding.barraEstadoLote.setBackgroundColor(Color.parseColor("#DC2626"))
                                rowBinding.tvEstadoLote.text = "VENCIDO"
                                rowBinding.tvEstadoLote.setTextColor(Color.parseColor("#DC2626"))
                                rowBinding.tvEstadoLote.background = badgeBg("#FEE2E2")
                            }
                            dias <= 7 -> {
                                rowBinding.barraEstadoLote.setBackgroundColor(Color.parseColor("#EA580C"))
                                rowBinding.tvEstadoLote.text = if (dias == 0) "Vence hoy" else "Vence en ${dias}d"
                                rowBinding.tvEstadoLote.setTextColor(Color.parseColor("#EA580C"))
                                rowBinding.tvEstadoLote.background = badgeBg("#FFF0E8")
                            }
                            dias <= 30 -> {
                                rowBinding.barraEstadoLote.setBackgroundColor(Color.parseColor("#D97706"))
                                rowBinding.tvEstadoLote.text = "Vence en ${dias}d"
                                rowBinding.tvEstadoLote.setTextColor(Color.parseColor("#D97706"))
                                rowBinding.tvEstadoLote.background = badgeBg("#FFF3CD")
                            }
                            dias <= 90 -> {
                                rowBinding.barraEstadoLote.setBackgroundColor(Color.parseColor("#2563EB"))
                                rowBinding.tvEstadoLote.text = "Vence en ${dias}d"
                                rowBinding.tvEstadoLote.setTextColor(Color.parseColor("#2563EB"))
                                rowBinding.tvEstadoLote.background = badgeBg("#EFF6FF")
                            }
                            else -> {
                                rowBinding.barraEstadoLote.setBackgroundColor(Color.parseColor("#15803D"))
                                rowBinding.tvEstadoLote.text = "Vigente"
                                rowBinding.tvEstadoLote.setTextColor(Color.parseColor("#15803D"))
                                rowBinding.tvEstadoLote.background = badgeBg("#DCFCE7")
                            }
                        }

                        rowBinding.tvNumeroLote.text = lote.numero.ifBlank { clave }

                        // Vencimiento
                        rowBinding.tvVencimientoLote.text = if (lote.vencimiento.isNotBlank())
                            "Vence ${lote.vencimiento}" else "Sin fecha de vencimiento"

                        // Cantidad en ese lote (stock actual del lote)
                        val cantInt = lote.cantidad.toInt()
                        rowBinding.tvCantidadLote.text = "$cantInt $unidadBase"
                        val valorTotalLote = if (lote.costoCompraUnitario > 0.0 && lote.cantidad > 0.0) {
                            lote.costoCompraUnitario * lote.cantidad
                        } else {
                            0.0
                        }
                        rowBinding.tvCostoLote.text = when {
                            valorTotalLote > 0.0 ->
                                "Costo total del lote: ${MonedaHelper.formatearNumero(valorTotalLote)}"
                            lote.costoUltimoIngreso > 0.0 ->
                                "Costo total del lote: ${MonedaHelper.formatearNumero(lote.costoUltimoIngreso)}"
                            else -> "Este lote aun no tiene costo registrado"
                        }

                        // Fecha de ingreso
                        rowBinding.tvFechaLote.text = lote.fecha.ifBlank { "" }

                        rowBinding.btnHistorialLote.setOnClickListener {
                            mostrarDialogoHistorialLote(clave, lote)
                        }
                        rowBinding.btnEditarLote.setOnClickListener {
                            mostrarDialogoEditarLote(clave, lote)
                        }
                        container.addView(rowBinding.root)
                    }
                }
            }
            .addOnFailureListener {
                layoutVacio.visibility = View.VISIBLE
                tvSubtitulo.text = "No se pudieron cargar los lotes"
            }

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Cerrar", null)
            .create()

        dialog.show()
    }

    private fun mostrarDialogoGestionarLotesV2() {
        val dialogBinding = DialogGestionarLotesBinding.inflate(layoutInflater)
        val container = dialogBinding.containerLotes
        val tvSubtitulo = dialogBinding.tvSubtituloLotes
        val tvConteo = dialogBinding.tvConteoLotes
        val layoutVacio = dialogBinding.layoutLotesVacio
        val tvLoteActual = dialogBinding.tvLoteConsumoActual
        val tvLoteFefo = dialogBinding.tvLoteFefoRecomendado
        val tvAyudaConsumo = dialogBinding.tvAyudaConsumoLotes

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Cerrar", null)
            .create()

        fun badgeBg(colorHex: String): android.graphics.drawable.GradientDrawable {
            val dp = resources.displayMetrics.density
            return android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 50 * dp
                setColor(Color.parseColor(colorHex))
            }
        }

        fun refrescarDialogo() {
            FirebaseDatabase.getInstance()
                .getReference(PATH_INVENTARIO)
                .child(PATH_PRODUCTOS)
                .child(indiceOriginal)
                .get()
                .addOnSuccessListener { snapshot ->
                    container.removeAllViews()
                    val lotes = snapshot.child("lotes").children.mapNotNull { loteSnapshot ->
                        val lote = loteSnapshot.getValue(LoteProducto::class.java)
                        val clave = loteSnapshot.key.orEmpty()
                        if (lote != null && clave.isNotBlank()) clave to lote else null
                    }.toMap()

                    loteConsumoSeleccionadoActual = snapshot
                        .child("loteConsumoSeleccionado")
                        .value
                        ?.toString()
                        .orEmpty()
                        .trim()
                    loteConsumoSeleccionManualActual = snapshot
                        .child("loteConsumoSeleccionManual")
                        .getValue(Boolean::class.java) == true

                    actualizarVisibilidadVencimientoGeneral(lotes.values.count { it.numero.isNotBlank() })
                    verificarVencimiento()

                    val resolucion = resolverConsumoLotesActual(lotes)
                    val lotesPorCreacion = resolucion.lotesPorCreacion
                    val unidadBase = binding.editTextUnidadBase.text.toString().trim().ifBlank { "unidades" }

                    if (lotesPorCreacion.isEmpty()) {
                        layoutVacio.visibility = View.VISIBLE
                        dialogBinding.cardResumenConsumoLotes.visibility = View.GONE
                        dialogBinding.btnUsarFefoAutomatico.visibility = View.GONE
                        tvSubtitulo.text = "Sin lotes registrados"
                        tvConteo.text = "0 lotes"
                        return@addOnSuccessListener
                    }

                    layoutVacio.visibility = View.GONE
                    dialogBinding.cardResumenConsumoLotes.visibility = View.VISIBLE
                    dialogBinding.btnUsarFefoAutomatico.visibility = View.VISIBLE

                    val vencidos = lotesPorCreacion.count { (_, lote) ->
                        diasHastaVencerLote(lote.vencimiento)?.let { it < 0 } == true
                    }
                    val porVencer = lotesPorCreacion.count { (_, lote) ->
                        diasHastaVencerLote(lote.vencimiento)?.let { it in 0..30 } == true
                    }
                    val lotesBloqueados = lotesPorCreacion.count { (_, lote) ->
                        ProductUtils.estaLoteBloqueadoParaVenta(lote)
                    }
                    val stockVendibleTotal = lotesPorCreacion.sumOf { (_, lote) ->
                        ProductUtils.cantidadVendibleLote(lote)
                    }.toInt()
                    val stockBloqueadoTotal = lotesPorCreacion.sumOf { (_, lote) ->
                        lote.cantidadBloqueada.coerceAtLeast(0.0)
                    }.toInt()
                    val stockFisicoTotal = lotesPorCreacion.sumOf { (_, lote) ->
                        ProductUtils.cantidadFisicaLote(lote)
                    }.toInt()
                    val fechaInicial = lotesPorCreacion.firstOrNull()?.second?.fecha?.ifBlank { "sin fecha" } ?: "sin fecha"

                    tvSubtitulo.text = when {
                        vencidos > 0 -> "Ordenados por registro · $vencidos vencidos · desde $fechaInicial"
                        porVencer > 0 -> "Ordenados por registro · $porVencer por vencer · desde $fechaInicial"
                        else -> "Ordenados por fecha de registro · desde $fechaInicial"
                    }
                    tvConteo.text = "${lotesPorCreacion.size} lote${if (lotesPorCreacion.size == 1) "" else "s"}"

                    tvLoteActual.text = if (resolucion.loteActual != null) {
                        val prefijo = if (resolucion.usaSeleccionManual) {
                            "Lote seleccionado para consumo: "
                        } else {
                            "Lote en consumo actual: "
                        }
                        prefijo + formatearLoteConsumoUi(resolucion.loteActual)
                    } else {
                        "Lote en consumo actual: Sin lote disponible"
                    }

                    tvLoteFefo.text = if (resolucion.loteRecomendadoFefo != null) {
                        "Recomendado por FEFO: ${formatearLoteConsumoUi(resolucion.loteRecomendadoFefo)}"
                    } else {
                        "Recomendado por FEFO: No hay lotes vendibles"
                    }

                    tvAyudaConsumo.text = when {
                        resolucion.consumoParcialConFallback ->
                            "El lote manual ya no alcanza por si solo para esta salida. En venta se consumira primero este lote y, si hace falta completar, el sistema seguira por FEFO."
                        resolucion.loteManualInsuficiente ->
                            "El lote manual ya no alcanza y el total disponible entre lotes válidos tampoco cubre toda la salida. Revisa el stock antes de vender."
                        resolucion.usaSeleccionManual &&
                            resolucion.loteRecomendadoFefo != null &&
                            resolucion.loteActual?.first != resolucion.loteRecomendadoFefo.first ->
                            "Estas usando un lote distinto al recomendado por FEFO. Se respetara tu seleccion manual."
                        resolucion.seleccionManualInvalida ->
                            "La seleccion manual anterior ya no es valida para consumo. En venta se aplicara FEFO automaticamente hasta que la corrijas."
                        else ->
                            "Toca un lote para dejarlo como lote de consumo manual. Si prefieres, puedes volver a FEFO automatico."
                    }

                    tvSubtitulo.text = when {
                        lotesBloqueados > 0 ->
                            "Vendible $stockVendibleTotal · bloqueado $stockBloqueadoTotal · fisico $stockFisicoTotal"
                        vencidos > 0 ->
                            "Vendible $stockVendibleTotal · $vencidos vencidos · desde $fechaInicial"
                        porVencer > 0 ->
                            "Vendible $stockVendibleTotal · $porVencer por vencer · desde $fechaInicial"
                        else ->
                            "Vendible $stockVendibleTotal · fisico $stockFisicoTotal · desde $fechaInicial"
                    }
                    tvConteo.text = buildString {
                        append("${lotesPorCreacion.size} lote")
                        if (lotesPorCreacion.size != 1) append("s")
                        if (lotesBloqueados > 0) {
                            append(" · $lotesBloqueados bloqueado")
                            if (lotesBloqueados != 1) append("s")
                        }
                    }
                    if (stockVendibleTotal <= 0 && (lotesBloqueados > 0 || vencidos > 0)) {
                        tvAyudaConsumo.text =
                            "No hay lotes vendibles para este producto. Revisa los lotes bloqueados o vencidos antes de volver a vender."
                    }

                    dialogBinding.btnUsarFefoAutomatico.isEnabled = loteConsumoSeleccionManualActual
                    dialogBinding.btnUsarFefoAutomatico.alpha = if (loteConsumoSeleccionManualActual) 1f else 0.5f
                    dialogBinding.btnUsarFefoAutomatico.setOnClickListener {
                        guardarPreferenciaConsumoLote(
                            numeroLote = "",
                            seleccionManual = false
                        ) {
                            Toast.makeText(
                                this,
                                "Se reactivo FEFO automatico para este producto",
                                Toast.LENGTH_SHORT
                            ).show()
                            refrescarDialogo()
                        }
                    }

                    lotesPorCreacion.forEach { (clave, lote) ->
                        val rowBinding = ItemLoteBinding.inflate(layoutInflater, container, false)
                        val dias = diasHastaVencerLote(lote.vencimiento)
                        val numeroUi = lote.numero.ifBlank { clave }
                        val esBloqueado = ProductUtils.estaLoteBloqueadoParaVenta(lote)
                        val cantidadVendible = ProductUtils.cantidadVendibleLote(lote).toInt()
                        val cantidadBloqueada = lote.cantidadBloqueada.coerceAtLeast(0.0).toInt()
                        val cantidadFisica = ProductUtils.cantidadFisicaLote(lote).toInt()

                        when {
                            esBloqueado -> {
                                rowBinding.barraEstadoLote.setBackgroundColor(Color.parseColor("#B91C1C"))
                                rowBinding.tvEstadoLote.text = "BLOQUEADO"
                                rowBinding.tvEstadoLote.setTextColor(Color.parseColor("#B91C1C"))
                                rowBinding.tvEstadoLote.background = badgeBg("#FEE2E2")
                            }
                            dias == null -> {
                                rowBinding.barraEstadoLote.setBackgroundColor(Color.parseColor("#D1D5DB"))
                                rowBinding.tvEstadoLote.text = "Sin fecha"
                                rowBinding.tvEstadoLote.setTextColor(Color.parseColor("#6B7280"))
                                rowBinding.tvEstadoLote.background = badgeBg("#F3F4F6")
                            }
                            dias < 0 -> {
                                rowBinding.barraEstadoLote.setBackgroundColor(Color.parseColor("#DC2626"))
                                rowBinding.tvEstadoLote.text = "VENCIDO"
                                rowBinding.tvEstadoLote.setTextColor(Color.parseColor("#DC2626"))
                                rowBinding.tvEstadoLote.background = badgeBg("#FEE2E2")
                            }
                            dias <= 7 -> {
                                rowBinding.barraEstadoLote.setBackgroundColor(Color.parseColor("#EA580C"))
                                rowBinding.tvEstadoLote.text = if (dias == 0) "Vence hoy" else "Vence en ${dias}d"
                                rowBinding.tvEstadoLote.setTextColor(Color.parseColor("#EA580C"))
                                rowBinding.tvEstadoLote.background = badgeBg("#FFF0E8")
                            }
                            dias <= 30 -> {
                                rowBinding.barraEstadoLote.setBackgroundColor(Color.parseColor("#D97706"))
                                rowBinding.tvEstadoLote.text = "Vence en ${dias}d"
                                rowBinding.tvEstadoLote.setTextColor(Color.parseColor("#D97706"))
                                rowBinding.tvEstadoLote.background = badgeBg("#FFF3CD")
                            }
                            dias <= 90 -> {
                                rowBinding.barraEstadoLote.setBackgroundColor(Color.parseColor("#2563EB"))
                                rowBinding.tvEstadoLote.text = "Vence en ${dias}d"
                                rowBinding.tvEstadoLote.setTextColor(Color.parseColor("#2563EB"))
                                rowBinding.tvEstadoLote.background = badgeBg("#EFF6FF")
                            }
                            else -> {
                                rowBinding.barraEstadoLote.setBackgroundColor(Color.parseColor("#15803D"))
                                rowBinding.tvEstadoLote.text = "Vigente"
                                rowBinding.tvEstadoLote.setTextColor(Color.parseColor("#15803D"))
                                rowBinding.tvEstadoLote.background = badgeBg("#DCFCE7")
                            }
                        }

                        rowBinding.tvNumeroLote.text = numeroUi
                        rowBinding.tvVencimientoLote.text = if (lote.vencimiento.isNotBlank()) {
                            "Vence ${lote.vencimiento}"
                        } else {
                            "Sin fecha de vencimiento"
                        }
                        rowBinding.tvCantidadLote.text = buildString {
                            append("$cantidadVendible vendibles")
                            if (cantidadBloqueada > 0) {
                                append(" · $cantidadBloqueada bloqueadas")
                            }
                            append(" · $cantidadFisica $unidadBase fisicas")
                        }
                        rowBinding.tvFechaLote.text = lote.fecha.ifBlank { "Sin registro" }

                        val valorTotalLote = if (lote.costoCompraUnitario > 0.0 && cantidadFisica > 0) {
                            lote.costoCompraUnitario * cantidadFisica
                        } else {
                            0.0
                        }
                        rowBinding.tvCostoLote.text = when {
                            valorTotalLote > 0.0 ->
                                "Costo total del lote: ${MonedaHelper.formatearNumero(valorTotalLote)}"
                            lote.costoUltimoIngreso > 0.0 ->
                                "Costo total del lote: ${MonedaHelper.formatearNumero(lote.costoUltimoIngreso)}"
                            else -> "Este lote aun no tiene costo registrado"
                        }

                        if (esBloqueado) {
                            rowBinding.tvBloqueoLote.visibility = View.VISIBLE
                            rowBinding.tvBloqueoLote.text =
                                "Bloqueado para venta. Motivo: ${lote.motivoBloqueo.ifBlank { "Sin motivo registrado" }}"
                        } else {
                            rowBinding.tvBloqueoLote.visibility = View.GONE
                        }

                        val esActual = resolucion.loteActual?.first == clave
                        val esFefo = resolucion.loteRecomendadoFefo?.first == clave
                        val esManual = loteConsumoSeleccionManualActual &&
                            resolucion.loteSeleccionManual?.first == clave

                        when {
                            esBloqueado -> {
                                rowBinding.tvConsumoLote.text = if (cantidadBloqueada > 0) {
                                    "Lote bloqueado para venta"
                                } else {
                                    "Lote marcado como bloqueado"
                                }
                                rowBinding.tvConsumoLote.visibility = View.VISIBLE
                                rowBinding.tvConsumoLote.backgroundTintList =
                                    ColorStateList.valueOf(Color.parseColor("#FEE2E2"))
                                rowBinding.tvConsumoLote.setTextColor(Color.parseColor("#B91C1C"))
                                rowBinding.cardContenidoLote.strokeColor =
                                    Color.parseColor("#DC2626")
                                rowBinding.cardContenidoLote.setCardBackgroundColor(
                                    Color.parseColor("#FFF8F8")
                                )
                            }
                            esActual && esManual -> {
                                rowBinding.tvConsumoLote.text = "En consumo manual"
                                rowBinding.tvConsumoLote.visibility = View.VISIBLE
                                rowBinding.tvConsumoLote.backgroundTintList =
                                    ColorStateList.valueOf(Color.parseColor("#DBEAFE"))
                                rowBinding.tvConsumoLote.setTextColor(Color.parseColor("#1D4ED8"))
                                rowBinding.cardContenidoLote.strokeColor =
                                    Color.parseColor("#2563EB")
                                rowBinding.cardContenidoLote.setCardBackgroundColor(
                                    Color.parseColor("#F8FBFF")
                                )
                            }
                            esActual && esFefo -> {
                                rowBinding.tvConsumoLote.text = "En consumo actual · FEFO"
                                rowBinding.tvConsumoLote.visibility = View.VISIBLE
                                rowBinding.tvConsumoLote.backgroundTintList =
                                    ColorStateList.valueOf(Color.parseColor("#DCFCE7"))
                                rowBinding.tvConsumoLote.setTextColor(Color.parseColor("#15803D"))
                                rowBinding.cardContenidoLote.strokeColor =
                                    Color.parseColor("#16A34A")
                                rowBinding.cardContenidoLote.setCardBackgroundColor(
                                    Color.parseColor("#F7FFF9")
                                )
                            }
                            esFefo -> {
                                rowBinding.tvConsumoLote.text = "Recomendado por FEFO"
                                rowBinding.tvConsumoLote.visibility = View.VISIBLE
                                rowBinding.tvConsumoLote.backgroundTintList =
                                    ColorStateList.valueOf(Color.parseColor("#FEF3C7"))
                                rowBinding.tvConsumoLote.setTextColor(Color.parseColor("#B45309"))
                                rowBinding.cardContenidoLote.strokeColor =
                                    Color.parseColor("#F59E0B")
                                rowBinding.cardContenidoLote.setCardBackgroundColor(
                                    Color.parseColor("#FFFCF5")
                                )
                            }
                            else -> {
                                rowBinding.tvConsumoLote.text = "Toca para consumir desde este lote"
                                rowBinding.tvConsumoLote.visibility = View.VISIBLE
                                rowBinding.tvConsumoLote.backgroundTintList =
                                    ColorStateList.valueOf(Color.parseColor("#EFF6FF"))
                                rowBinding.tvConsumoLote.setTextColor(Color.parseColor("#2563EB"))
                                rowBinding.cardContenidoLote.strokeColor =
                                    Color.parseColor("#E7EAEE")
                                rowBinding.cardContenidoLote.setCardBackgroundColor(Color.WHITE)
                            }
                        }

                        rowBinding.cardContenidoLote.setOnClickListener {
                            if (esBloqueado) {
                                Toast.makeText(
                                    this,
                                    "Ese lote está bloqueado para venta. Desbloquéalo primero si fue un error.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            }
                            if (cantidadVendible <= 0) {
                                Toast.makeText(
                                    this,
                                    "Ese lote no tiene stock disponible para consumo",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            }
                            if (!ProductUtils.esLoteValidoParaConsumo(lote)) {
                                Toast.makeText(
                                    this,
                                    "Ese lote ya no es válido para consumo. Elige uno vigente o vuelve a FEFO.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            }

                            guardarPreferenciaConsumoLote(
                                numeroLote = numeroUi,
                                seleccionManual = true
                            ) {
                                Toast.makeText(
                                    this,
                                    "Ahora se consumira desde el lote $numeroUi",
                                    Toast.LENGTH_SHORT
                                ).show()
                                refrescarDialogo()
                            }
                        }

                        rowBinding.btnHistorialLote.setOnClickListener {
                            mostrarDialogoHistorialLote(clave, lote)
                        }
                        rowBinding.btnEditarLote.setOnClickListener {
                            mostrarDialogoEditarLote(clave, lote)
                        }
                        rowBinding.btnToggleBloqueoLote.iconTint =
                            ColorStateList.valueOf(
                                Color.parseColor(
                                    if (esBloqueado) "#15803D" else "#B91C1C"
                                )
                            )
                        rowBinding.btnToggleBloqueoLote.contentDescription = if (esBloqueado) {
                            "Desbloquear lote"
                        } else {
                            "Bloquear lote"
                        }
                        rowBinding.btnToggleBloqueoLote.setOnClickListener {
                            if (esBloqueado) {
                                confirmarDesbloqueoLote(clave, lote) {
                                    refrescarDialogo()
                                }
                            } else {
                                mostrarDialogoBloquearLote(clave, lote) {
                                    refrescarDialogo()
                                }
                            }
                        }
                        container.addView(rowBinding.root)
                    }
                }
                .addOnFailureListener {
                    layoutVacio.visibility = View.VISIBLE
                    dialogBinding.cardResumenConsumoLotes.visibility = View.GONE
                    dialogBinding.btnUsarFefoAutomatico.visibility = View.GONE
                    tvSubtitulo.text = "No se pudieron cargar los lotes"
                }
        }

        refrescarDialogo()
        dialog.show()
    }

    private fun mostrarDialogoEditarLote(clave: String, lote: LoteProducto) {
        layoutInflater.inflate(R.layout.dialog_gestionar_lotes, null)
        // Usamos un AlertDialog simple con dos campos
        val tilNumero = TextInputLayout(this).apply {
            hint = "Número de lote"
            addView(TextInputEditText(context).apply {
                id = android.R.id.text1
                setText(lote.numero)
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            })
        }
        val tilVencimiento = TextInputLayout(this).apply {
            hint = "Vencimiento (MM/AA)"
            addView(TextInputEditText(context).apply {
                id = android.R.id.text2
                setText(lote.vencimiento)
                inputType = InputType.TYPE_CLASS_NUMBER
                filters = arrayOf(android.text.InputFilter.LengthFilter(5))
            })
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
            addView(tilNumero)
            addView(tilVencimiento)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Editar lote")
            .setView(container)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNumero = (tilNumero.getChildAt(0) as? TextInputEditText)?.text?.toString()?.trim().orEmpty()
                val nuevoVencimiento = (tilVencimiento.getChildAt(0) as? TextInputEditText)?.text?.toString()?.trim().orEmpty()
                val ref = FirebaseDatabase.getInstance()
                    .getReference(PATH_INVENTARIO)
                    .child(PATH_PRODUCTOS)
                    .child(indiceOriginal)
                    .child("lotes")
                    .child(clave)
                val updates = mutableMapOf<String, Any>()
                if (nuevoNumero.isNotBlank()) updates["numero"] = nuevoNumero
                if (nuevoVencimiento.isNotBlank()) updates["vencimiento"] = nuevoVencimiento
                if (updates.isNotEmpty()) {
                    ref.updateChildren(updates)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Lote actualizado", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "No se pudo actualizar", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .show()
    }

    private fun mostrarDialogoAgregarLoteManual() {
        val tilNumero = TextInputLayout(this).apply {
            hint = "Número de lote"
            addView(TextInputEditText(context).apply {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            })
        }
        val tilVencimiento = TextInputLayout(this).apply {
            hint = "Vencimiento (MM/AA)"
            addView(TextInputEditText(context).apply {
                val vencProd = binding.editTextVencimiento.text?.toString()?.trim().orEmpty()
                setText(vencProd)
                inputType = InputType.TYPE_CLASS_NUMBER
                filters = arrayOf(android.text.InputFilter.LengthFilter(5))
            })
        }
        val tilCantidad = TextInputLayout(this).apply {
            hint = "Cantidad ingresada"
            addView(TextInputEditText(context).apply {
                inputType = InputType.TYPE_CLASS_NUMBER
            })
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
            addView(tilNumero)
            addView(tilVencimiento)
            addView(tilCantidad)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Agregar lote")
            .setView(container)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar") { _, _ ->
                val numero = (tilNumero.getChildAt(0) as? TextInputEditText)?.text?.toString()?.trim().orEmpty().ifBlank { "L001" }
                val vencimiento = (tilVencimiento.getChildAt(0) as? TextInputEditText)?.text?.toString()?.trim().orEmpty()
                val cantidad = (tilCantidad.getChildAt(0) as? TextInputEditText)?.text?.toString()?.trim()?.toDoubleOrNull() ?: 0.0
                val clave = sanitizarClaveLote(numero)
                val hoy = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())
                val lote = LoteProducto(numero = numero, vencimiento = vencimiento, cantidad = cantidad, fecha = hoy)
                
                // V17.46: Actualización atómica con índice de lotes
                val rootRef = FirebaseDatabase.getInstance().reference
                val prodActual = obtenerProductoActualSeguro()
                val updates = mutableMapOf<String, Any?>()
                
                // 1. Ruta del lote dentro del producto
                updates["$PATH_INVENTARIO/$PATH_PRODUCTOS/$indiceOriginal/lotes/$clave"] = lote
                updates["${DbPaths.INVENTARIO_PRODUCTO_LOTES}/$indiceOriginal/$clave"] = lote
                updates["${DbPaths.INVENTARIO_PRODUCTO_LOTES}/$indiceOriginal/$clave/loteId"] = clave
                updates["${DbPaths.INVENTARIO_PRODUCTO_LOTES}/$indiceOriginal/$clave/lotePath"] =
                    "${DbPaths.INVENTARIO_PRODUCTO_LOTES}/$indiceOriginal/$clave"
                
                // 2. Índice plano de lotes
                val numLote = numero.trim().uppercase()
                if (numLote.isNotBlank()) {
                    val keyLote = ProductUtils.encodeLotKey(numLote)
                    val registroLote = LoteIndexado(
                        numero = numLote,
                        productoId = indiceOriginal,
                        productoNombre = prodActual.nombre.trim(),
                        loteId = clave,
                        lotePath = "${DbPaths.INVENTARIO_PRODUCTO_LOTES}/$indiceOriginal/$clave",
                        vencimiento = vencimiento
                    )
                    updates["${DbPaths.INVENTARIO_LOTES_POR_NUMERO}/$keyLote"] = registroLote
                }

                mostrarCarga(true, titulo = "Guardando", mensaje = "Registrando nuevo lote...")
                rootRef.updateChildren(updates)
                    .addOnSuccessListener {
                        mostrarCarga(false)
                        Toast.makeText(this, "Lote agregado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        mostrarCarga(false)
                        Toast.makeText(this, "No se pudo agregar el lote", Toast.LENGTH_SHORT).show()
                    }
            }
            .show()
    }

    private fun resolverClaveLoteExistentePorNumero(
        numeroLote: String,
        loteEsperado: LoteProducto? = null,
        onResult: (String?) -> Unit
    ) {
        val numeroLoteLimpio = numeroLote.trim()
        if (numeroLoteLimpio.isBlank()) {
            onResult(null)
            return
        }

        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRODUCTOS)
            .child(indiceOriginal)
            .child("lotes")
            .get()
            .addOnSuccessListener { snapshot ->
                val candidatos = snapshot.children.filter { child ->
                    child.getValue(LoteProducto::class.java)
                        ?.numero
                        ?.trim()
                        ?.equals(numeroLoteLimpio, ignoreCase = true) == true
                }

                if (candidatos.isEmpty()) {
                    onResult(null)
                    return@addOnSuccessListener
                }

                val claveExacta = if (loteEsperado != null) {
                    candidatos.firstOrNull { child ->
                        val loteFirebase = child.getValue(LoteProducto::class.java) ?: return@firstOrNull false
                        val mismaCantidad =
                            kotlin.math.abs(loteFirebase.cantidad - loteEsperado.cantidad) < 0.0001
                        loteFirebase.vencimiento.trim() == loteEsperado.vencimiento.trim() &&
                            loteFirebase.fecha.trim() == loteEsperado.fecha.trim() &&
                            mismaCantidad
                    }?.key
                } else {
                    null
                }

                val claveConVencimiento = if (loteEsperado != null) {
                    candidatos.firstOrNull { child ->
                        child.getValue(LoteProducto::class.java)
                            ?.vencimiento
                            ?.trim()
                            .orEmpty() == loteEsperado.vencimiento.trim()
                    }?.key
                } else {
                    null
                }

                onResult(claveExacta ?: claveConVencimiento ?: candidatos.firstOrNull()?.key)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    private fun aplicarAjusteStock(
        cantidadAjuste: Int,
        esSuma: Boolean,
        motivo: String,
        nombreUnidadAjuste: String,
        unidadesPorAjuste: Int,
        fuePorPresentacion: Boolean,
        numeroLote: String = "",
        lotesExistentes: List<LoteProducto> = emptyList(),
        onSuccess: () -> Unit
    ) {
        val stockAntes = binding.editTextCantidad.text?.toString()?.trim()?.toIntOrNull() ?: 0
        val unidadesNormalizadas = unidadesPorAjuste.coerceAtLeast(1)
        val numeroLoteLimpio = numeroLote.trim()
        val lotesRegistrados = lotesExistentes.filter { it.numero.isNotBlank() }
        val loteExistente = lotesRegistrados.firstOrNull {
            it.numero.trim().equals(numeroLoteLimpio, ignoreCase = true)
        }

        if (fuePorPresentacion && unidadesNormalizadas <= 0) {
            Toast.makeText(this, "Selecciona una presentación válida", Toast.LENGTH_LONG).show()
            return
        }

        if (numeroLoteLimpio.isBlank()) {
            val mensaje = if (esSuma) {
                "Debes seleccionar o crear el lote que recibirá el stock"
            } else {
                "Debes seleccionar el lote del que se descontará el stock"
            }
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
            return
        }

        val deltaUnidades = cantidadAjuste * unidadesNormalizadas
        val stockDespues = if (esSuma) stockAntes + deltaUnidades else stockAntes - deltaUnidades

        if (stockDespues < 0) {
            // Seguridad: no debería llegar aqué si se usa el diálogo de confirmación
            Toast.makeText(this, "El ajuste resultaría en stock negativo ($stockDespues)", Toast.LENGTH_LONG).show()
            return
        }

        if (loteExistente == null) {
            Toast.makeText(
                this,
                "Debes seleccionar el lote del que se descontará el stock",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (!esSuma && loteExistente.cantidad < deltaUnidades.toDouble()) {
            Toast.makeText(
                this,
                "La cantidad supera el stock disponible del lote seleccionado",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        /*
        if (creandoLoteNuevo) {
            val requiereVencimientoLote =
                binding.editTextVencimiento.text?.toString()?.trim().orEmpty().isNotBlank() ||
                    lotesRegistrados.any { it.vencimiento.isNotBlank() }
            val errorVencimiento = when {
                vencimientoLoteLimpio.isBlank() && requiereVencimientoLote -> "Ingresa el vencimiento del nuevo lote"
                vencimientoLoteLimpio.isBlank() -> null
                diasHastaVencerLote(vencimientoLoteLimpio) == null -> "Ingresa una fecha valida en formato MM/AA"
                diasHastaVencerLote(vencimientoLoteLimpio)?.let { it < 0 } == true ->
                    "El vencimiento del lote no puede ser anterior a hoy"
                else -> null
            }
            if (errorVencimiento != null) {
                Toast.makeText(this, errorVencimiento, Toast.LENGTH_LONG).show()
                return
            }
        }
        */

        resolverClaveLoteExistentePorNumero(numeroLoteLimpio, loteExistente) { claveLoteReal ->
            if (claveLoteReal.isNullOrBlank()) {
                Toast.makeText(
                    this,
                    "No se encontro la clave real del lote seleccionado. Abre Gestion de lotes y vuelve a intentarlo.",
                    Toast.LENGTH_LONG
                ).show()
                return@resolverClaveLoteExistentePorNumero
            }

            mostrarDialogoProgresoStock("Ajustando stock del producto...")
            FirebaseDatabase.getInstance()
                .getReference(PATH_INVENTARIO)
                .child(PATH_PRODUCTOS)
                .child(indiceOriginal)
                .child("cantidadinicial")
                .setValue(stockDespues.toString())
                .addOnSuccessListener {
                    val delta = if (esSuma) deltaUnidades.toDouble() else -deltaUnidades.toDouble()
                    val loteRef = FirebaseDatabase.getInstance()
                        .getReference(PATH_INVENTARIO)
                        .child(PATH_PRODUCTOS)
                        .child(indiceOriginal)
                        .child("lotes")
                        .child(claveLoteReal)
                    loteRef.child("cantidad").setValue(com.google.firebase.database.ServerValue.increment(delta))
                    guardarMovimientoEnLote(
                        claveLote = claveLoteReal,
                        tipo = "ajuste",
                        delta = delta,
                        unidad = nombreUnidadAjuste,
                        motivo = motivo
                    )

                    actualizarCamposStockDespuesDeAjuste(
                        stockDespues = stockDespues,
                        fuePorPresentacion = fuePorPresentacion,
                        unidadesPorPresentacionSeleccionada = unidadesNormalizadas
                    )
                    sincronizarUiVencimientoPorCantidadLotes(
                        cantidadLotes = lotesRegistrados.size,
                        vencimientoGeneralSugerido = binding.editTextVencimiento.text?.toString()?.trim().orEmpty()
                    )

                    val nombreProducto = binding.editTextNombre.text?.toString()?.trim().orEmpty()
                    val operacion = if (esSuma) "suma" else "resta"
                    val cambiosStock = mapOf(
                        "stockTotalUnidades" to mapOf(
                            "antes" to stockAntes,
                            "despues" to stockDespues
                        )
                    )
                    val extraLoteAjuste = mapOf(
                        "loteNumero" to numeroLoteLimpio,
                        "loteClave" to claveLoteReal
                    )

                    MovimientoInventarioLogger.registrarConSesion(
                        context = this,
                        indiceProducto = indiceOriginal,
                        tipo = "stock_ajuste_manual",
                        titulo = "Ajuste manual de stock",
                        descripcion = "Se ajustó $nombreProducto",
                        cantidad = if (esSuma) deltaUnidades else -deltaUnidades,
                        stockAntes = stockAntes,
                        stockDespues = stockDespues,
                        motivo = motivo,
                        referenciaId = "ajuste_manual",
                        extra = mapOf(
                            "operacion" to operacion,
                            "modoIngreso" to if (fuePorPresentacion) "presentacion" else "unidad",
                            "cantidadIngresada" to cantidadAjuste,
                            "unidadAjuste" to nombreUnidadAjuste,
                            "unidadesPorAjuste" to unidadesNormalizadas,
                            "cambiosAntesDespues" to cambiosStock
                        ) + extraLoteAjuste
                    )

                    MovimientoLogger.registrarConSesion(
                        context = this,
                        tipo = "ajuste_stock",
                        modulo = "inventario",
                        titulo = "Ajuste de stock",
                        descripcion = "Se ajustó el stock de $nombreProducto",
                        referenciaId = indiceOriginal,
                        extra = mapOf(
                            "motivo" to motivo,
                            "stockAntes" to stockAntes,
                            "stockDespues" to stockDespues,
                            "operacion" to operacion,
                            "unidadAjuste" to nombreUnidadAjuste,
                            "cantidadIngresada" to cantidadAjuste,
                            "cambiosAntesDespues" to cambiosStock,
                            "loteClave" to claveLoteReal
                        )
                    )

                    ocultarDialogoProgresoStock()
                    marcarEstadoInicialFormulario()
                    Toast.makeText(this, "Stock ajustado correctamente", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    ocultarDialogoProgresoStock()
                    Toast.makeText(this, "No se pudo ajustar el stock: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
        return
        /*
        mostrarDialogoProgresoStock("Ajustando stock del producto...")
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRODUCTOS)
            .child(indiceOriginal)
            .child("cantidadinicial")
            .setValue(stockDespues.toString())
            .addOnSuccessListener {
                if (numeroLoteLimpio.isNotBlank()) {
                    val claveLote = sanitizarClaveLote(numeroLoteLimpio)
                    val delta = if (esSuma) deltaUnidades.toDouble() else -deltaUnidades.toDouble()
                    val loteRef = FirebaseDatabase.getInstance()
                        .getReference(PATH_INVENTARIO)
                        .child(PATH_PRODUCTOS)
                        .child(indiceOriginal)
                        .child("lotes")
                        .child(claveLote)
                    if (creandoLoteNuevo) {
                        val hoy = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            .format(java.util.Date())
                        loteRef.child("numero").setValue(numeroLoteLimpio)
                        loteRef.child("vencimiento").setValue(vencimientoLoteLimpio)
                        loteRef.child("fecha").setValue(hoy)
                        if (lotesRegistrados.isEmpty() && vencimientoLoteLimpio.isNotBlank()) {
                            FirebaseDatabase.getInstance()
                                .getReference(PATH_INVENTARIO)
                                .child(PATH_PRODUCTOS)
                                .child(indiceOriginal)
                                .child("vencimiento")
                                .setValue(vencimientoLoteLimpio)
                        }
                    }
                    loteRef.child("cantidad").setValue(com.google.firebase.database.ServerValue.increment(delta))
                    guardarMovimientoEnLote(
                        claveLote = claveLote,
                        tipo = "ajuste",
                        delta = delta,
                        unidad = nombreUnidadAjuste,
                        motivo = motivo
                    )
                }

                actualizarCamposStockDespuesDeAjuste(
                    stockDespues = stockDespues,
                    fuePorPresentacion = fuePorPresentacion,
                    unidadesPorPresentacionSeleccionada = unidadesNormalizadas
                )
                sincronizarUiVencimientoPorCantidadLotes(
                    cantidadLotes = if (creandoLoteNuevo) lotesRegistrados.size + 1 else lotesRegistrados.size,
                    vencimientoGeneralSugerido = if (creandoLoteNuevo && lotesRegistrados.isEmpty()) {
                        vencimientoLoteLimpio
                    } else {
                        binding.editTextVencimiento.text?.toString()?.trim().orEmpty()
                    }
                )

                val nombreProducto = binding.editTextNombre.text?.toString()?.trim().orEmpty()
                val operacion = if (esSuma) "suma" else "resta"
                val cambiosStock = mapOf("stockTotalUnidades" to mapOf(
                        "antes" to stockAntes,
                        "despues" to stockDespues
                    ))

                val extraLoteAjuste = if (numeroLoteLimpio.isNotBlank()) mapOf("loteNumero" to numeroLoteLimpio) else emptyMap()
                MovimientoInventarioLogger.registrarConSesion(
                    context = this,
                    indiceProducto = indiceOriginal,
                    tipo = "stock_ajuste_manual",
                    titulo = "Ajuste manual de stock",
                    descripcion = "Se ajustó $nombreProducto",
                    cantidad = if (esSuma) deltaUnidades else -deltaUnidades,
                    stockAntes = stockAntes,
                    stockDespues = stockDespues,
                    motivo = motivo,
                    referenciaId = "ajuste_manual",
                    extra = mapOf(
                        "operacion" to operacion,
                        "modoIngreso" to if (fuePorPresentacion) "presentacion" else "unidad",
                        "cantidadIngresada" to cantidadAjuste,
                        "unidadAjuste" to nombreUnidadAjuste,
                        "unidadesPorAjuste" to unidadesNormalizadas,
                        "cambiosAntesDespues" to cambiosStock
                    ) + extraLoteAjuste
                )

                MovimientoLogger.registrarConSesion(
                    context = this,
                    tipo = "ajuste_stock",
                    modulo = "inventario",
                    titulo = "Ajuste de stock",
                    descripcion = "Se ajustó el stock de $nombreProducto",
                    referenciaId = indiceOriginal,
                    extra = mapOf(
                        "motivo" to motivo,
                        "stockAntes" to stockAntes,
                        "stockDespues" to stockDespues,
                        "operacion" to operacion,
                        "unidadAjuste" to nombreUnidadAjuste,
                        "cantidadIngresada" to cantidadAjuste,
                        "cambiosAntesDespues" to cambiosStock
                    )
                )

                ocultarDialogoProgresoStock()
                marcarEstadoInicialFormulario()
                Toast.makeText(this, "Stock ajustado correctamente", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
            .addOnFailureListener { e ->
                ocultarDialogoProgresoStock()
                Toast.makeText(this, "No se pudo ajustar el stock: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

        */
    }

    // -------------------------------------------------------------
    //  EGRESO / SALIDA  (FEFO)
    // -------------------------------------------------------------

    private fun configurarEgresoStock() {
        binding.btnEgresoStock.setOnClickListener {
            if (!puedeAbrirMovimientoStock()) return@setOnClickListener
            FirebaseDatabase.getInstance()
                .getReference(PATH_INVENTARIO)
                .child(PATH_PRODUCTOS)
                .child(indiceOriginal)
                .child("lotes")
                .get()
                .addOnSuccessListener { snapshot ->
                    val lotes = mutableListOf<LoteProducto>()
                    snapshot.children.forEach { child ->
                        child.getValue(LoteProducto::class.java)?.let { lotes.add(it) }
                    }
                    mostrarDialogoEgresoStock(lotes)
                }
                .addOnFailureListener { mostrarDialogoEgresoStock(emptyList()) }
        }
    }

    private fun mostrarDialogoEgresoStock(lotesExistentes: List<LoteProducto>) {
        val unidadBase = binding.editTextUnidadBase.text.toString().trim().ifBlank { "unidades" }
        val nombreUnidadBaseUi = formatearEtiquetaUi(unidadBase)
        val presentacionesDisponibles = obtenerPresentacionesAjustables(unidadBase)
        val dialogBinding = DialogEgresoStockBinding.inflate(layoutInflater)
        val textIndicadorScroll = dialogBinding.tvIndicadorScrollEgreso
        var refrescarIndicadorScrollEgreso: (() -> Unit)? = null

        // -- FEFO: ordenar lotes por vencimiento (el que vence antes, primero) --
        // Sin fecha van al final; vencidos también se despachan (descarte)
        val lotesOrdenados = lotesExistentes
            .filter { it.numero.isNotBlank() }
            .sortedWith(compareBy(nullsLast()) { lote ->
                val d = diasHastaVencerLote(lote.vencimiento)
                d  // null ? al final
            })

        val lotesMap = lotesOrdenados.associateBy { sanitizarClaveLote(it.numero) }
        val resolucionConsumo = resolverConsumoLotesActual(lotesMap)
        val loteFefoRecomendado = resolucionConsumo.loteRecomendadoFefo?.second ?: lotesOrdenados.firstOrNull()
        var loteSeleccionado: LoteProducto? = resolucionConsumo.loteActual?.second ?: lotesOrdenados.firstOrNull()

        // -- Banner FEFO --
        if (loteFefoRecomendado != null) {
            val fefo = loteFefoRecomendado
            val dias = diasHastaVencerLote(fefo.vencimiento)
            dialogBinding.cardFEFO.visibility = View.VISIBLE
            dialogBinding.tvFEFOLote.text = buildString {
                append(fefo.numero)
                if (fefo.vencimiento.isNotBlank()) append(" · vence ${fefo.vencimiento}")
            }
            when {
                dias == null -> {
                    dialogBinding.tvFEFODias.text = "—"
                    dialogBinding.cardFEFO.setCardBackgroundColor(Color.parseColor("#F3F4F6"))
                }
                dias < 0 -> {
                    dialogBinding.tvFEFODias.text = "VENCIDO"
                    dialogBinding.tvFEFODias.setTextColor(Color.parseColor("#DC2626"))
                    dialogBinding.cardFEFO.setCardBackgroundColor(Color.parseColor("#FEE2E2"))
                }
                dias <= 30 -> {
                    dialogBinding.tvFEFODias.text = "${dias}d"
                    dialogBinding.cardFEFO.setCardBackgroundColor(Color.parseColor("#FFF7DB"))
                }
                else -> {
                    dialogBinding.tvFEFODias.text = "${dias}d"
                    dialogBinding.cardFEFO.setCardBackgroundColor(Color.parseColor("#F0FDF4"))
                    dialogBinding.tvFEFODias.setTextColor(Color.parseColor("#15803D"))
                }
            }
        }

        // -- Sección de lote --
        when {
            lotesOrdenados.isEmpty() -> {
                dialogBinding.tvSinLotesEgreso.visibility = View.VISIBLE
                dialogBinding.tvSinLotesEgreso.text =
                    "No puedes ajustar salida sin elegir lote. Primero registra un lote para este producto."
            }
            lotesOrdenados.size == 1 -> {
                val l = lotesOrdenados.first()
                dialogBinding.tvLoteUnicoEgreso.visibility = View.VISIBLE
                dialogBinding.tvLoteUnicoEgreso.text = buildString {
                    append("Se descontará del lote: ${l.numero}")
                    if (l.vencimiento.isNotBlank()) append(" (vence ${l.vencimiento})")
                }
            }
            else -> {
                dialogBinding.layoutLoteEgreso.visibility = View.VISIBLE
                // Ordenado FEFO: primer ítem ya es el sugerido
                val opciones = lotesOrdenados.map { lote ->
                    buildString {
                        append(lote.numero)
                        if (lote.vencimiento.isNotBlank()) append(" · vence ${lote.vencimiento}")
                        val d = diasHastaVencerLote(lote.vencimiento)
                        if (d != null && d < 0) append(" ? VENCIDO")
                    }
                }
                dialogBinding.inputLoteEgreso.setAdapter(
                    ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opciones)
                )
                aplicarCierreDropdownAlRecibirFoco(dialogBinding.inputLoteEgreso)
                val indiceSeleccionado = lotesOrdenados.indexOfFirst {
                    it.numero.equals(loteSeleccionado?.numero, ignoreCase = true)
                }.takeIf { it >= 0 } ?: 0
                dialogBinding.inputLoteEgreso.setText(opciones[indiceSeleccionado], false)
                dialogBinding.inputLoteEgreso.setOnItemClickListener { _, _, pos, _ ->
                    loteSeleccionado = lotesOrdenados.getOrNull(pos)
                    dialogBinding.layoutLoteEgreso.error = null
                    refrescarIndicadorScrollEgreso?.invoke()
                }
            }
        }

        // -- Modo (unidad / presentación) --
        val nombrePresentacionEgreso = presentacionesDisponibles.firstOrNull()
            ?.let { formatearEtiquetaUi(it.nombre) } ?: "Presentación"
        val opcionesModo = mutableListOf(nombreUnidadBaseUi)
        if (presentacionesDisponibles.isNotEmpty()) opcionesModo.add(nombrePresentacionEgreso)
        dialogBinding.inputModoEgreso.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opcionesModo)
        )
        dialogBinding.layoutModoEgreso.hint = if (presentacionesDisponibles.isNotEmpty())
            "¿Salió en ${nombreUnidadBaseUi.lowercase()} o en ${nombrePresentacionEgreso.lowercase()}?"
        else nombreUnidadBaseUi
        // El dropdown queda siempre oculto; la UI usa el toggle (layoutSelectorModoEgreso)
        dialogBinding.layoutModoEgreso.visibility = View.GONE
        aplicarCierreDropdownAlRecibirFoco(dialogBinding.inputModoEgreso)
        dialogBinding.inputModoEgreso.setText(opcionesModo.first(), false)

        val nombresPresentaciones = presentacionesDisponibles.map { formatearEtiquetaUi(it.nombre) }
        dialogBinding.inputPresentacionEgreso.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nombresPresentaciones)
        )
        aplicarCierreDropdownAlRecibirFoco(dialogBinding.inputPresentacionEgreso)
        if (nombresPresentaciones.isNotEmpty()) {
            dialogBinding.inputPresentacionEgreso.setText(nombresPresentaciones.first(), false)
        }

        fun presentacionSeleccionada(): PresentacionProducto? {
            val nombre = dialogBinding.inputPresentacionEgreso.text?.toString()?.trim().orEmpty()
            return presentacionesDisponibles.firstOrNull {
                formatearEtiquetaUi(it.nombre).equals(nombre, ignoreCase = true)
            }
        }

        fun esModoPresentacionSeleccionado(): Boolean {
            val modo = dialogBinding.inputModoEgreso.text?.toString().orEmpty().trim()
            return modo.isNotEmpty() &&
                !modo.equals(nombreUnidadBaseUi, ignoreCase = true) &&
                presentacionesDisponibles.isNotEmpty()
        }

        fun sincronizarCantidadConPresentacionSeleccionada() {
            // El usuario escribe cuántas cajas/blísters/bolsas salen — no auto-rellenamos
        }

        fun cantidadEgresoConfigurada(): Int {
            // Siempre devuelve lo que el usuario escribió (sea unidades o cajas)
            return dialogBinding.inputCantidadEgreso.text?.toString()?.trim()?.toIntOrNull() ?: 0
        }

        fun actualizarContexto() {
            // El dropdown viejo queda siempre oculto: los chips son el único selector visual
            dialogBinding.layoutPresentacionEgreso.visibility = View.GONE
            if (esModoPresentacionSeleccionado()) {
                val presentacionActiva = presentacionSeleccionada() != null
                dialogBinding.inputCantidadEgreso.isEnabled = true
                dialogBinding.layoutCantidadEgreso.hint = if (presentacionActiva) {
                    val p = presentacionSeleccionada()
                    val nombrePlural = formatearEtiquetaUi(p?.nombre.orEmpty()).lowercase()
                    "\u00bfCu\u00e1ntas $nombrePlural salen?"
                } else "Cantidad que sale en ${unidadBase.lowercase()}"
                if (presentacionActiva) {
                    sincronizarCantidadConPresentacionSeleccionada()
                }
                return
            }
            // Modo unidad: cantidad libre, sin dropdown de presentación
            dialogBinding.inputCantidadEgreso.isEnabled = true
            dialogBinding.layoutCantidadEgreso.hint = "Cantidad que sale en ${unidadBase.lowercase()}"
            sincronizarCantidadConPresentacionSeleccionada()
        }

        actualizarContexto()
        dialogBinding.inputModoEgreso.setOnItemClickListener { _, _, _, _ ->
            dialogBinding.layoutModoEgreso.error = null
            if (!esModoPresentacionSeleccionado()) {
                dialogBinding.inputPresentacionEgreso.setText("", false)
                dialogBinding.layoutPresentacionEgreso.error = null
                if (dialogBinding.inputCantidadEgreso.text?.toString() != "0") {
                    dialogBinding.inputCantidadEgreso.setText("0")
                }
            }
            actualizarContexto()
        }
        dialogBinding.inputPresentacionEgreso.setOnItemClickListener { _, _, _, _ ->
            dialogBinding.layoutPresentacionEgreso.error = null
            sincronizarCantidadConPresentacionSeleccionada()
            actualizarContexto()
        }
        dialogBinding.inputPresentacionEgreso.addTextChangedListener {
            val textoActual = it?.toString()?.trim().orEmpty()
            if (textoActual.isBlank()) {
                dialogBinding.inputModoEgreso.setText(opcionesModo.first(), false)
                dialogBinding.layoutPresentacionEgreso.error = null
                dialogBinding.inputCantidadEgreso.isEnabled = true
                if (dialogBinding.inputCantidadEgreso.text?.toString() != "0") {
                    dialogBinding.inputCantidadEgreso.setText("0")
                }
            }
            actualizarContexto()
        }

        // -- Motivo (dropdown oculto, sincronizado por las chip-cards visibles) --
        dialogBinding.inputMotivoEgreso.setText("Venta al cliente", false)

        // -- Cabecera: nombre del producto --
        dialogBinding.tvNombreProductoEgreso.text =
            binding.editTextNombre.text?.toString()?.trim().orEmpty().ifBlank { "Producto" }
        dialogBinding.tvSubtituloEgreso.text = "Selecciona presentación y cantidad"
        dialogBinding.tvSubtituloEgreso.visibility = View.VISIBLE

        // -- Chip-cards de Presentación (Unidad + cada presentación) --
        val chipsPresContainerEgreso = dialogBinding.chipsPresentacionEgreso
        chipsPresContainerEgreso.removeAllViews()
        val cardsPresEgreso = mutableListOf<Triple<MaterialCardView, String, PresentacionProducto?>>()
        fun pintarChipsPresEgreso(seleccionadoNombre: String) {
            cardsPresEgreso.forEach { (card, opcion, _) ->
                val sel = opcion.equals(seleccionadoNombre, ignoreCase = true)
                card.setCardBackgroundColor(if (sel) 0xFFE5F4EB.toInt() else 0xFFFFFFFF.toInt())
                card.strokeColor = if (sel) 0xFF0E8F63.toInt() else 0xFFE5E7EB.toInt()
                card.strokeWidth = if (sel) (2 * resources.displayMetrics.density).toInt()
                    else (1 * resources.displayMetrics.density).toInt()
                val tv = card.findViewById<TextView>(R.id.tvChipMovimiento)
                tv?.setTextColor(if (sel) 0xFF0E8F63.toInt() else 0xFF0F172A.toInt())
                val img = card.findViewById<ImageView>(R.id.imgChipMovimiento)
                img?.setColorFilter(if (sel) 0xFF0E8F63.toInt() else 0xFF667085.toInt())
            }
        }
        fun agregarChipPresEgreso(nombreVisible: String, presentacion: PresentacionProducto?) {
            val chipView = layoutInflater.inflate(
                R.layout.chip_presentacion_movimiento, chipsPresContainerEgreso, false
            )
            val card = chipView as MaterialCardView
            chipView.findViewById<TextView>(R.id.tvChipMovimiento)?.text = nombreVisible
            chipView.findViewById<ImageView>(R.id.imgChipMovimiento)?.setImageResource(
                resolverIconoPresentacionMovimiento(nombreVisible)
            )
            chipView.setOnClickListener {
                if (presentacion == null) {
                    dialogBinding.inputModoEgreso.setText(nombreUnidadBaseUi, false)
                    dialogBinding.inputPresentacionEgreso.setText("", false)
                    dialogBinding.layoutPresentacionEgreso.error = null
                    dialogBinding.inputCantidadEgreso.setText("")
                } else {
                    dialogBinding.inputModoEgreso.setText(formatearEtiquetaUi(presentacion.nombre), false)
                    dialogBinding.inputPresentacionEgreso.setText(formatearEtiquetaUi(presentacion.nombre), false)
                }
                pintarChipsPresEgreso(nombreVisible)
                actualizarContexto()
            }
            cardsPresEgreso.add(Triple(card, nombreVisible, presentacion))
            chipsPresContainerEgreso.addView(chipView)
        }
        agregarChipPresEgreso(nombreUnidadBaseUi, null)
        presentacionesDisponibles.forEach { p ->
            agregarChipPresEgreso(formatearEtiquetaUi(p.nombre), p)
        }
        pintarChipsPresEgreso(nombreUnidadBaseUi)

        // -- Chip-cards de Motivo --
        val chipsMotivoContainer = dialogBinding.chipsMotivoEgreso
        chipsMotivoContainer.removeAllViews()
        val opcionesMotivo = listOf(
            Triple("Venta", R.drawable.iconventascajashistorial, "Venta al cliente"),
            Triple("Merma", R.drawable.deleteiconitem, "Descarte / vencimiento"),
            Triple("Vencido", R.drawable.outline_calendar_month_24, "Descarte / vencimiento"),
            Triple("Uso interno", R.drawable.iconconfiguracioncaja, "Uso interno")
        )
        val cardsMotivo = mutableListOf<Pair<MaterialCardView, String>>()
        fun pintarChipsMotivo(seleccionado: String) {
            cardsMotivo.forEach { (card, label) ->
                val sel = label.equals(seleccionado, ignoreCase = true)
                card.setCardBackgroundColor(if (sel) 0xFFE5F4EB.toInt() else 0xFFFFFFFF.toInt())
                card.strokeColor = if (sel) 0xFF0E8F63.toInt() else 0xFFE5E7EB.toInt()
                card.strokeWidth = if (sel) (2 * resources.displayMetrics.density).toInt()
                    else (1 * resources.displayMetrics.density).toInt()
                val tv = card.findViewById<TextView>(R.id.tvChipMovimiento)
                tv?.setTextColor(if (sel) 0xFF0E8F63.toInt() else 0xFF0F172A.toInt())
                val img = card.findViewById<ImageView>(R.id.imgChipMovimiento)
                img?.setColorFilter(if (sel) 0xFF0E8F63.toInt() else 0xFF667085.toInt())
            }
        }
        opcionesMotivo.forEach { (label, iconRes, valorParaInput) ->
            val chipView = layoutInflater.inflate(
                R.layout.chip_presentacion_movimiento, chipsMotivoContainer, false
            )
            val card = chipView as MaterialCardView
            chipView.findViewById<TextView>(R.id.tvChipMovimiento)?.text = label
            chipView.findViewById<ImageView>(R.id.imgChipMovimiento)?.setImageResource(iconRes)
            chipView.setOnClickListener {
                dialogBinding.inputMotivoEgreso.setText(valorParaInput, false)
                pintarChipsMotivo(label)
            }
            cardsMotivo.add(card to label)
            chipsMotivoContainer.addView(chipView)
        }
        pintarChipsMotivo("Venta")

        // -- Botones +/− para sumar/restar la cantidad de salida --
        dialogBinding.btnIncCantidadEgreso.setOnClickListener {
            val actual = dialogBinding.inputCantidadEgreso.text?.toString()?.trim()?.toIntOrNull() ?: 0
            dialogBinding.inputCantidadEgreso.setText((actual + 1).toString())
            dialogBinding.inputCantidadEgreso.setSelection(
                dialogBinding.inputCantidadEgreso.text?.length ?: 0
            )
        }
        dialogBinding.btnDecCantidadEgreso.setOnClickListener {
            val actual = dialogBinding.inputCantidadEgreso.text?.toString()?.trim()?.toIntOrNull() ?: 0
            val nuevo = (actual - 1).coerceAtLeast(0)
            dialogBinding.inputCantidadEgreso.setText(if (nuevo == 0) "" else nuevo.toString())
            dialogBinding.inputCantidadEgreso.setSelection(
                dialogBinding.inputCantidadEgreso.text?.length ?: 0
            )
        }

        // Tap fuera del campo → ocultar teclado
        dialogBinding.contenidoEgresoStock.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                ocultarTecladoYQuitarFoco()
                v.performClick()
            }
            false
        }

        // -- Construir diálogo (BottomSheet sin botones nativos: usamos los del layout) --
        BottomSheetDialog(this)
            .apply { setContentView(dialogBinding.root) }
            .also { dialog ->
                configurarBackdropBottomSheet(dialog)
                dialog.setCanceledOnTouchOutside(false)
                fun manejarBackDialogoEgreso(): Boolean {
                    val imeVisible = ViewCompat.getRootWindowInsets(dialogBinding.root)
                        ?.isVisible(WindowInsetsCompat.Type.ime()) == true
                    return when {
                        dialogBinding.inputMotivoEgreso.isPopupShowing -> {
                            dialogBinding.inputMotivoEgreso.dismissDropDown()
                            dialogBinding.inputMotivoEgreso.clearFocus()
                            dialogBinding.tvTituloEgreso.requestFocus()
                            true
                        }
                        dialogBinding.inputPresentacionEgreso.isPopupShowing -> {
                            dialogBinding.inputPresentacionEgreso.dismissDropDown()
                            dialogBinding.inputPresentacionEgreso.clearFocus()
                            dialogBinding.tvTituloEgreso.requestFocus()
                            true
                        }
                        dialogBinding.inputModoEgreso.isPopupShowing -> {
                            dialogBinding.inputModoEgreso.dismissDropDown()
                            dialogBinding.inputModoEgreso.clearFocus()
                            dialogBinding.tvTituloEgreso.requestFocus()
                            true
                        }
                        dialogBinding.inputLoteEgreso.isPopupShowing -> {
                            dialogBinding.inputLoteEgreso.dismissDropDown()
                            dialogBinding.inputLoteEgreso.clearFocus()
                            dialogBinding.tvTituloEgreso.requestFocus()
                            true
                        }
                        dialogBinding.inputCantidadEgreso.hasFocus() ||
                            dialogBinding.inputMotivoEgreso.hasFocus() ||
                            dialogBinding.inputDocumentoEgreso.hasFocus() -> {
                            dialogBinding.inputCantidadEgreso.clearFocus()
                            dialogBinding.inputMotivoEgreso.clearFocus()
                            dialogBinding.inputDocumentoEgreso.clearFocus()
                            dialogBinding.tvTituloEgreso.requestFocus()
                            ocultarTeclado()
                            true
                        }
                        imeVisible -> {
                            dialogBinding.inputCantidadEgreso.clearFocus()
                            dialogBinding.inputMotivoEgreso.clearFocus()
                            dialogBinding.inputDocumentoEgreso.clearFocus()
                            dialogBinding.tvTituloEgreso.requestFocus()
                            ocultarTeclado()
                            true
                        }
                        else -> {
                            dialog.dismiss()
                            true
                        }
                    }
                }
                dialog.onBackPressedDispatcher.addCallback(dialog, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        manejarBackDialogoEgreso()
                    }
                })
                dialog.setOnKeyListener { _, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                        manejarBackDialogoEgreso()
                    } else {
                        false
                    }
                }
                dialog.setOnShowListener {
                    aplicarMargenSuperiorBottomSheet(dialog)
                    val maxWidthPx = (480 * resources.displayMetrics.density).toInt()
                    val screenWidthPx = resources.displayMetrics.widthPixels
                    if (screenWidthPx > maxWidthPx) {
                        dialog.window?.setLayout(maxWidthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
                    }
                    // El diálogo NO se mueve ni achica con el teclado.
                    // En su lugar, el ScrollView interno recibe padding inferior = altura del teclado
                    // para poder scrollear el contenido por encima del teclado.
                    dialog.window?.setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
                    )
                    val rootSvEgreso = dialogBinding.root
                    rootSvEgreso.clipToPadding = false
                    val basePadBottomEgreso = rootSvEgreso.paddingBottom
                    ViewCompat.setOnApplyWindowInsetsListener(rootSvEgreso) { v, insets ->
                        val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                        v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, basePadBottomEgreso + imeBottom)
                        insets
                    }
                    // Cuando un campo recibe foco, el ScrollView lo lleva encima del teclado
                    fun scrollToFocused(v: View) {
                        rootSvEgreso.postDelayed({
                            val rect = Rect(0, 0, v.width, v.height)
                            v.requestRectangleOnScreen(rect, false)
                        }, 220)
                    }
                    dialogBinding.inputCantidadEgreso.setOnFocusChangeListener { v, hasFocus ->
                        if (hasFocus) scrollToFocused(v)
                    }

                    val btnRegistrar: View = dialogBinding.btnGuardarEgresoStock
                    dialogBinding.btnCancelarEgresoStock.setOnClickListener { dialog.dismiss() }

                    dialogBinding.tvTituloEgreso.isFocusableInTouchMode = true
                    dialogBinding.tvTituloEgreso.isFocusable = true
                    dialogBinding.tvTituloEgreso.requestFocus()
                    dialogBinding.inputMotivoEgreso.clearFocus()
                    dialogBinding.inputPresentacionEgreso.clearFocus()
                    dialogBinding.inputModoEgreso.clearFocus()
                    dialogBinding.inputLoteEgreso.clearFocus()

                    fun actualizarIndicadorScroll() {
                        val puedeSeguirBajando = dialogBinding.root.canScrollVertically(1)
                        if (puedeSeguirBajando) {
                            textIndicadorScroll.text = if (
                                dialogBinding.layoutLoteEgreso.visibility == View.VISIBLE ||
                                dialogBinding.tvLoteUnicoEgreso.visibility == View.VISIBLE
                            ) {
                                "Desliza hacia abajo para ver mas y revisar el lote."
                            } else {
                                "Desliza hacia abajo para ver mas opciones."
                            }
                            textIndicadorScroll.visibility = View.VISIBLE
                        } else {
                            textIndicadorScroll.visibility = View.GONE
                        }
                    }
                    refrescarIndicadorScrollEgreso = {
                        dialogBinding.root.post { actualizarIndicadorScroll() }
                    }
                    dialogBinding.root.setOnScrollChangeListener { _, _, _, _, _ ->
                        actualizarIndicadorScroll()
                    }

                    fun calcularDelta(): Int {
                        val cant = dialogBinding.inputCantidadEgreso.text?.toString()
                            ?.trim()?.toIntOrNull() ?: 0
                        val esPres = esModoPresentacionSeleccionado()
                        val unidadesPor = if (esPres) presentacionSeleccionada()?.cantidad ?: 1 else 1
                        return cant * unidadesPor
                    }

                    fun actualizarResumen() {
                        val stockActual = binding.editTextCantidad.text?.toString()
                            ?.trim()?.toIntOrNull() ?: 0
                        val esPres = esModoPresentacionSeleccionado()
                        val cant = dialogBinding.inputCantidadEgreso.text?.toString()
                            ?.trim()?.toIntOrNull() ?: 0
                        val pres = if (esPres) presentacionSeleccionada() else null
                        val unidadesPor = pres?.cantidad ?: 1
                        // Conversión: si el usuario eligió presentación (ej. Caja de 10) y escribió 3,
                        // delta = 3 cajas × 10 unidades/caja = 30 unidades base
                        val delta = cant * unidadesPor

                        if (cant > 0 && esPres && pres != null) {
                            dialogBinding.tvUnidadesCalculadasEgreso.visibility = View.VISIBLE
                            dialogBinding.tvUnidadesCalculadasEgreso.text =
                                "${formatearEtiquetaUi(pres.nombre)} = $delta $unidadBase"
                        } else {
                            dialogBinding.tvUnidadesCalculadasEgreso.visibility = View.GONE
                        }

                        if (cant > 0) {
                            val stockFinal = stockActual - delta
                            dialogBinding.tvResumenEgreso.visibility = View.VISIBLE
                            dialogBinding.tvResumenEgreso.text = if (esPres && pres != null) {
                                "Registrando salida de $cant ${formatearEtiquetaUi(pres.nombre).lowercase()} = $delta $unidadBase"
                            } else {
                                "Registrando salida de $delta $unidadBase"
                            }
                            dialogBinding.tvStockFinalEgreso.visibility = View.VISIBLE
                            dialogBinding.tvStockFinalEgreso.text =
                                "Stock resultante: $stockFinal $unidadBase"
                            dialogBinding.tvStockFinalEgreso.setTextColor(
                                Color.parseColor(if (stockFinal < 0) "#DC2626" else "#1A1C1E")
                            )

                            // Tarjeta resumen visible: Stock actual / -delta / Nuevo stock
                            dialogBinding.tvStockActualEgreso.text = stockActual.toString()
                            dialogBinding.tvCambioStockEgreso.text = "-$delta"
                            dialogBinding.tvNuevoStockEgreso.text = stockFinal.toString()
                            dialogBinding.tvNuevoStockEgreso.setTextColor(
                                Color.parseColor(if (stockFinal < 0) "#DC2626" else "#0E8F63")
                            )
                            dialogBinding.cardResumenStockEgreso.visibility = View.VISIBLE
                        } else {
                            dialogBinding.tvResumenEgreso.visibility = View.GONE
                            dialogBinding.tvStockFinalEgreso.visibility = View.GONE

                            // Tarjeta resumen mostrada con valores neutros
                            dialogBinding.tvStockActualEgreso.text = stockActual.toString()
                            dialogBinding.tvCambioStockEgreso.text = "-0"
                            dialogBinding.tvNuevoStockEgreso.text = stockActual.toString()
                            dialogBinding.tvNuevoStockEgreso.setTextColor(0xFF0E8F63.toInt())
                            dialogBinding.cardResumenStockEgreso.visibility = View.VISIBLE
                        }
                        refrescarIndicadorScrollEgreso?.invoke()
                    }

                    fun validarYHabilitar() {
                        val cantOk = cantidadEgresoConfigurada() > 0
                        val loteOk = loteSeleccionado != null
                        btnRegistrar?.isEnabled = cantOk && loteOk
                        btnRegistrar?.alpha = if (btnRegistrar?.isEnabled == true) 1f else 0.5f
                        if (cantOk) actualizarResumen()
                    }

                    dialogBinding.inputCantidadEgreso.addTextChangedListener {
                        dialogBinding.layoutCantidadEgreso.error = null
                        validarYHabilitar()
                    }
                    dialogBinding.inputMotivoEgreso.addTextChangedListener {
                        validarYHabilitar()
                    }
                    validarYHabilitar()
                    dialogBinding.root.post { actualizarIndicadorScroll() }

                    btnRegistrar?.setOnClickListener {
                        sincronizarCantidadConPresentacionSeleccionada()
                        val cantidad = cantidadEgresoConfigurada()
                        val motivo = dialogBinding.inputMotivoEgreso.text?.toString()
                            ?.trim().orEmpty()
                        val documento = dialogBinding.inputDocumentoEgreso.text?.toString()
                            ?.trim().orEmpty()
                        val esPres = esModoPresentacionSeleccionado()
                        val presElegida = if (esPres) presentacionSeleccionada() else null
                        val unidadesPor = presElegida?.cantidad ?: 1
                        // Siempre cantidad × unidadesPor (1 si modo unidad, N si modo presentación)
                        val delta = cantidad * unidadesPor
                        val stockActual = binding.editTextCantidad.text?.toString()
                            ?.trim()?.toIntOrNull() ?: 0

                        if (cantidad <= 0) {
                            dialogBinding.layoutCantidadEgreso.error = "Ingresa una cantidad válida"
                            return@setOnClickListener
                        }
                        if (motivo.isBlank()) return@setOnClickListener
                        if (esPres && presElegida == null) {
                            dialogBinding.layoutPresentacionEgreso.error = "Selecciona una presentación"
                            return@setOnClickListener
                        }
                        if (loteSeleccionado == null) {
                            Toast.makeText(
                                this,
                                "Debes seleccionar el lote del que se descontará el stock",
                                Toast.LENGTH_LONG
                            ).show()
                            return@setOnClickListener
                        }
                        if (delta > stockActual) {
                            dialogBinding.layoutCantidadEgreso.error =
                                "La cantidad supera el stock disponible ($stockActual $unidadBase)"
                            return@setOnClickListener
                        }
                        if (loteSeleccionado != null && loteSeleccionado!!.cantidad < delta.toDouble()) {
                            dialogBinding.layoutCantidadEgreso.error =
                                "La cantidad supera el stock disponible del lote seleccionado"
                            return@setOnClickListener
                        }

                        registrarEgresoStock(
                            cantidadEgreso = cantidad,
                            motivo = motivo,
                            documento = documento,
                            nombreUnidadEgreso = if (esPres) presElegida?.nombre.orEmpty() else unidadBase,
                            unidadesPorEgreso = unidadesPor,
                            fuePorPresentacion = esPres,
                            cantidadYaNormalizada = esPres,
                            lote = loteSeleccionado
                        ) { dialog.dismiss() }
                    }
                }
            }
            .show()
    }

    private fun registrarEgresoStock(
        cantidadEgreso: Int,
        motivo: String,
        documento: String,
        nombreUnidadEgreso: String,
        unidadesPorEgreso: Int,
        fuePorPresentacion: Boolean,
        cantidadYaNormalizada: Boolean = false,
        lote: LoteProducto?,
        onSuccess: () -> Unit
    ) {
        val stockAntes = binding.editTextCantidad.text?.toString()?.trim()?.toIntOrNull() ?: 0
        val unidadesNorm = unidadesPorEgreso.coerceAtLeast(1)
        val deltaUnidades = if (cantidadYaNormalizada) cantidadEgreso else cantidadEgreso * unidadesNorm
        if (deltaUnidades > stockAntes) {
            Toast.makeText(
                this,
                "La cantidad supera el stock disponible ($stockAntes ${binding.editTextUnidadBase.text})",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        val stockDespues = stockAntes - deltaUnidades

        mostrarDialogoProgresoStock("Registrando salida de stock...")
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRODUCTOS)
            .child(indiceOriginal)
            .child("cantidadinicial")
            .setValue(stockDespues.toString())
            .addOnSuccessListener {
                // Descontar del lote seleccionado
                if (lote != null && lote.numero.isNotBlank()) {
                    val claveLote = sanitizarClaveLote(lote.numero)
                    FirebaseDatabase.getInstance()
                        .getReference(PATH_INVENTARIO)
                        .child(PATH_PRODUCTOS)
                        .child(indiceOriginal)
                        .child("lotes")
                        .child(claveLote)
                        .child("cantidad")
                        .setValue(com.google.firebase.database.ServerValue.increment(-deltaUnidades.toDouble()))
                    guardarMovimientoEnLote(
                        claveLote = claveLote,
                        tipo = "salida",
                        delta = -deltaUnidades.toDouble(),
                        unidad = nombreUnidadEgreso,
                        motivo = motivo
                    )
                }

                actualizarCamposStockDespuesDeAjuste(
                    stockDespues = stockDespues,
                    fuePorPresentacion = fuePorPresentacion,
                    unidadesPorPresentacionSeleccionada = unidadesNorm
                )

                val nombreProducto = binding.editTextNombre.text?.toString()?.trim().orEmpty()
                val extraLote = if (lote != null && lote.numero.isNotBlank())
                    mapOf("loteNumero" to lote.numero, "loteVencimiento" to lote.vencimiento)
                else emptyMap()

                MovimientoInventarioLogger.registrarConSesion(
                    context = this,
                    indiceProducto = indiceOriginal,
                    tipo = "stock_salida",
                    titulo = "Salida de stock",
                    descripcion = "Salida registrada para $nombreProducto",
                    cantidad = deltaUnidades,
                    stockAntes = stockAntes,
                    stockDespues = stockDespues,
                    motivo = motivo,
                    referenciaId = if (documento.isNotBlank()) documento else "salida_stock",
                    extra = mapOf(
                        "modoEgreso" to if (fuePorPresentacion) "presentacion" else "unidad_base",
                        "cantidadEgresada" to cantidadEgreso,
                        "unidadEgreso" to nombreUnidadEgreso,
                        "unidadesPorEgreso" to unidadesNorm,
                        "documento" to documento,
                        "fefo" to (lote != null)
                    ) + extraLote
                )

                MovimientoLogger.registrarConSesion(
                    context = this,
                    tipo = "egreso_stock",
                    modulo = "inventario",
                    titulo = "Salida de stock",
                    descripcion = "Salió stock de $nombreProducto",
                    referenciaId = indiceOriginal,
                    extra = mapOf(
                        "motivo" to motivo,
                        "stockAntes" to stockAntes,
                        "stockDespues" to stockDespues,
                        "unidadEgreso" to nombreUnidadEgreso,
                        "cantidadEgresada" to cantidadEgreso
                    ) + extraLote
                )

                ocultarDialogoProgresoStock()
                marcarEstadoInicialFormulario()
                Toast.makeText(this, "Salida registrada correctamente", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
            .addOnFailureListener { e ->
                ocultarDialogoProgresoStock()
                Toast.makeText(this, "No se pudo registrar la salida: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun registrarIngresoStock(cantidadIngresada: Int, costoIngresoTotal: Double, motivo: String, documento: String, nombreUnidadIngreso: String, unidadesPorIngreso: Int, fuePorPresentacion: Boolean, numeroLote: String = "", vencimientoLote: String = "", lotesExistentes: List<LoteProducto> = emptyList(), requiereLote: Boolean = false, requiereVencimientoLote: Boolean = false, onSuccess: () -> Unit) {
        val stockAntes = binding.editTextCantidad.text?.toString()?.trim()?.toIntOrNull() ?: 0
        val unidadesNormalizadas = unidadesPorIngreso.coerceAtLeast(1)
        val deltaUnidades = cantidadIngresada * unidadesNormalizadas
        val stockDespues = stockAntes + deltaUnidades
        val costoUnitarioIngreso = costoIngresoTotal / deltaUnidades.toDouble().coerceAtLeast(1.0)
        val numeroLoteLimpio = numeroLote.trim()
        val vencimientoLoteLimpio = vencimientoLote.trim()
        val lotesRegistrados = lotesExistentes.filter { it.numero.isNotBlank() }
        val loteExistente = lotesRegistrados.firstOrNull {
            it.numero.trim().equals(numeroLoteLimpio, ignoreCase = true)
        }
        val creandoLoteNuevo = numeroLoteLimpio.isNotBlank() && loteExistente == null

        if (requiereLote && numeroLoteLimpio.isBlank()) {
            Toast.makeText(this, "Debes seleccionar o crear un lote para este ingreso", Toast.LENGTH_LONG).show()
            return
        }

        if (costoIngresoTotal <= 0.0) {
            Toast.makeText(this, "El costo del ingreso debe ser mayor a cero", Toast.LENGTH_LONG).show()
            return
        }

        if (numeroLoteLimpio.isNotBlank() && loteExistente == null) {
            val errorVencimiento = when {
                vencimientoLoteLimpio.isBlank() && requiereVencimientoLote -> "Ingresa el vencimiento del nuevo lote"
                vencimientoLoteLimpio.isBlank() -> null
                diasHastaVencerLote(vencimientoLoteLimpio) == null -> "Ingresa una fecha valida en formato MM/AA"
                diasHastaVencerLote(vencimientoLoteLimpio)?.let { it < 0 } == true -> "El vencimiento del lote no puede ser anterior a hoy"
                else -> null
            }
            if (errorVencimiento != null) {
                Toast.makeText(this, errorVencimiento, Toast.LENGTH_LONG).show()
                return
            }
        }

        mostrarDialogoProgresoStock("Registrando ingreso de stock...")
        
        // V17.46: Refactorización a operación atómica para incluir el índice de lotes
        val rootRef = FirebaseDatabase.getInstance().reference
        val updatesMap = mutableMapOf<String, Any?>()
        val prodActual = obtenerProductoActualSeguro()
        
        // 1. Actualización de stock total
        updatesMap["$PATH_INVENTARIO/$PATH_PRODUCTOS/$indiceOriginal/cantidadinicial"] = stockDespues.toString()

        // 2. Gestión de Lote
        if (numeroLoteLimpio.isNotBlank()) {
            val claveLote = sanitizarClaveLote(numeroLoteLimpio)
            val hoy = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())
            val cantidadAnteriorLote = loteExistente?.cantidad ?: 0.0
            val costoUnitarioAnterior = loteExistente?.costoCompraUnitario ?: 0.0
            val cantidadNuevaLote = cantidadAnteriorLote + deltaUnidades.toDouble()
            val costoPromedioLote = if (cantidadNuevaLote > 0) {
                ((cantidadAnteriorLote * costoUnitarioAnterior) + costoIngresoTotal) / cantidadNuevaLote
            } else {
                costoUnitarioIngreso
            }

            val lotPath = "$PATH_INVENTARIO/$PATH_PRODUCTOS/$indiceOriginal/lotes/$claveLote"
            val separatedLotPath = "${DbPaths.INVENTARIO_PRODUCTO_LOTES}/$indiceOriginal/$claveLote"
            updatesMap["$lotPath/numero"] = numeroLoteLimpio
            updatesMap["$separatedLotPath/numero"] = numeroLoteLimpio
            updatesMap["$separatedLotPath/loteId"] = claveLote
            updatesMap["$separatedLotPath/lotePath"] = separatedLotPath
            
            if (loteExistente == null) {
                updatesMap["$lotPath/vencimiento"] = vencimientoLoteLimpio
                updatesMap["$lotPath/fecha"] = hoy
                updatesMap["$separatedLotPath/vencimiento"] = vencimientoLoteLimpio
                updatesMap["$separatedLotPath/fecha"] = hoy
                if (lotesRegistrados.isEmpty() && vencimientoLoteLimpio.isNotBlank()) {
                    updatesMap["$PATH_INVENTARIO/$PATH_PRODUCTOS/$indiceOriginal/vencimiento"] = vencimientoLoteLimpio
                }
            } else if (loteExistente.vencimiento.isBlank() && vencimientoLoteLimpio.isNotBlank()) {
                updatesMap["$lotPath/vencimiento"] = vencimientoLoteLimpio
                updatesMap["$separatedLotPath/vencimiento"] = vencimientoLoteLimpio
            }
            
            updatesMap["$lotPath/costoCompraUnitario"] = costoPromedioLote
            updatesMap["$lotPath/costoUltimoIngreso"] = costoIngresoTotal
            updatesMap["$lotPath/costoUltimoIngresoUnitario"] = costoUnitarioIngreso
            updatesMap["$lotPath/cantidad"] = com.google.firebase.database.ServerValue.increment(deltaUnidades.toDouble())
            updatesMap["$separatedLotPath/costoCompraUnitario"] = costoPromedioLote
            updatesMap["$separatedLotPath/costoUltimoIngreso"] = costoIngresoTotal
            updatesMap["$separatedLotPath/costoUltimoIngresoUnitario"] = costoUnitarioIngreso
            updatesMap["$separatedLotPath/cantidad"] = com.google.firebase.database.ServerValue.increment(deltaUnidades.toDouble())

            // 3. Índice plano de lotes
            val numLoteLimpioKey = ProductUtils.encodeLotKey(numeroLoteLimpio)
            val registroLote = LoteIndexado(
                numero = numeroLoteLimpio.uppercase(),
                productoId = indiceOriginal,
                productoNombre = prodActual.nombre.trim(),
                loteId = claveLote,
                lotePath = "${DbPaths.INVENTARIO_PRODUCTO_LOTES}/$indiceOriginal/$claveLote",
                vencimiento = if (loteExistente != null) loteExistente.vencimiento else vencimientoLoteLimpio
            )
            updatesMap["${DbPaths.INVENTARIO_LOTES_POR_NUMERO}/$numLoteLimpioKey"] = registroLote
            
            // Nota: El movimiento de lote se guarda después del éxito de la transacción para mantener la lógica original.
        }

        rootRef.updateChildren(updatesMap)
            .addOnSuccessListener {
                if (numeroLoteLimpio.isNotBlank()) {
                    val claveLote = sanitizarClaveLote(numeroLoteLimpio)
                    guardarMovimientoEnLote(
                        claveLote = claveLote,
                        tipo = "entrada",
                        delta = deltaUnidades.toDouble(),
                        unidad = nombreUnidadIngreso,
                        motivo = motivo,
                        costoIngresoTotal = costoIngresoTotal,
                        costoUnitario = costoUnitarioIngreso
                    )
                }

                actualizarCamposStockDespuesDeAjuste(
                    stockDespues = stockDespues,
                    fuePorPresentacion = fuePorPresentacion,
                    unidadesPorPresentacionSeleccionada = unidadesNormalizadas
                )
                sincronizarUiVencimientoPorCantidadLotes(
                    cantidadLotes = if (creandoLoteNuevo) lotesRegistrados.size + 1 else lotesRegistrados.size,
                    vencimientoGeneralSugerido = if (creandoLoteNuevo && lotesRegistrados.isEmpty()) {
                        vencimientoLoteLimpio
                    } else {
                        binding.editTextVencimiento.text?.toString()?.trim().orEmpty()
                    }
                )

                val nombreProducto = binding.editTextNombre.text?.toString()?.trim().orEmpty()
                val cambiosStock = mapOf(
                    "stockTotalUnidades" to mapOf(
                        "antes" to stockAntes,
                        "despues" to stockDespues
                    )
                )
                val extraLote = if (numeroLoteLimpio.isNotBlank()) mapOf(
                    "loteNumero" to numeroLoteLimpio,
                    "loteVencimiento" to if (loteExistente != null) loteExistente.vencimiento else vencimientoLoteLimpio,
                    "costoIngresoTotal" to costoIngresoTotal,
                    "costoIngresoUnitario" to costoUnitarioIngreso
                ) else emptyMap()

                MovimientoInventarioLogger.registrarConSesion(
                    context = this,
                    indiceProducto = indiceOriginal,
                    tipo = "stock_entrada",
                    titulo = "Ingreso de stock",
                    descripcion = "Entró mercadería para $nombreProducto",
                    cantidad = deltaUnidades,
                    stockAntes = stockAntes,
                    stockDespues = stockDespues,
                    motivo = motivo,
                    referenciaId = if (documento.isNotBlank()) documento else "ingreso_stock",
                    extra = mapOf(
                        "modoIngreso" to if (fuePorPresentacion) "presentacion" else "unidad_base",
                        "cantidadIngresada" to cantidadIngresada,
                        "unidadIngreso" to nombreUnidadIngreso,
                        "unidadesPorIngreso" to unidadesNormalizadas,
                        "costoIngresoTotal" to costoIngresoTotal,
                        "costoIngresoUnitario" to costoUnitarioIngreso,
                        "documento" to documento,
                        "cambiosAntesDespues" to cambiosStock
                    ) + extraLote
                )

                MovimientoLogger.registrarConSesion(
                    context = this,
                    tipo = "ingreso_stock",
                    modulo = "inventario",
                    titulo = "Ingreso de stock",
                    descripcion = "Entró stock para $nombreProducto",
                    referenciaId = indiceOriginal,
                    extra = mapOf(
                        "motivo" to motivo,
                        "documento" to documento,
                        "stockAntes" to stockAntes,
                        "stockDespues" to stockDespues,
                        "cantidadIngresada" to cantidadIngresada,
                        "unidadIngreso" to nombreUnidadIngreso,
                        "costoIngresoTotal" to costoIngresoTotal,
                        "costoIngresoUnitario" to costoUnitarioIngreso,
                        "cambiosAntesDespues" to cambiosStock
                    ) + extraLote
                )

                ocultarDialogoProgresoStock()
                marcarEstadoInicialFormulario()
                Toast.makeText(this, "Stock ingresado correctamente", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
            .addOnFailureListener { e ->
                ocultarDialogoProgresoStock()
                Toast.makeText(this, "No se pudo ingresar el stock: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    data class MovimientoLote(
        val tipo: String = "",        // "entrada" | "salida" | "ajuste"
        val cantidad: Double = 0.0,   // positivo = entrada, negativo = salida/ajuste
        val unidad: String = "",
        val motivo: String = "",
        val costoIngresoTotal: Double = 0.0,
        val costoUnitario: Double = 0.0,
        val nombreUsuario: String = "",
        val fecha: String = "",
        val hora: String = "",
        val timestamp: Long = 0L
    )

    private fun guardarMovimientoEnLote(claveLote: String, tipo: String, delta: Double, unidad: String, motivo: String, costoIngresoTotal: Double = 0.0, costoUnitario: Double = 0.0) {

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfHora = java.text.SimpleDateFormat("h:mm a", Locale.getDefault())
        val ahora = java.util.Date()
        val nombreUsuario = SessionManager.nombreCajera.ifBlank { "Sistema" }

        val mov = MovimientoLote(
            tipo = tipo,
            cantidad = delta,
            unidad = unidad,
            motivo = motivo,
            costoIngresoTotal = costoIngresoTotal,
            costoUnitario = costoUnitario,
            nombreUsuario = nombreUsuario,
            fecha = sdf.format(ahora),
            hora = sdfHora.format(ahora),
            timestamp = ahora.time
        )

        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRODUCTOS)
            .child(indiceOriginal)
            .child("lotes")
            .child(claveLote)
            .child("movimientos")
            .push()
            .setValue(mov)
    }

    private fun mostrarDialogoHistorialLote(claveLote: String, lote: LoteProducto) {
        val dialogBinding = DialogHistorialLoteBinding.inflate(layoutInflater)

        // Header: número de lote e info
        val numeroMostrar = lote.numero.ifBlank { claveLote }
        dialogBinding.tvNumeroLoteHistorial.text = "Lote $numeroMostrar"

        val unidadBase = binding.editTextUnidadBase.text.toString().trim().ifBlank { "unidades" }
        val cantInt = lote.cantidad.toInt()
        val vencInfo = if (lote.vencimiento.isNotBlank()) "Vence ${lote.vencimiento}" else "Sin fecha"
        dialogBinding.tvInfoLoteHistorial.text = "$vencInfo · $cantInt $unidadBase actuales"

        // Badge de estado
        val valorTotalLote = if (lote.costoCompraUnitario > 0.0 && lote.cantidad > 0.0) {
            lote.costoCompraUnitario * lote.cantidad
        } else {
            0.0
        }
        val costoInfo = if (valorTotalLote > 0.0) {
            " · Costo total ${MonedaHelper.formatearNumero(valorTotalLote)}"
        } else {
            ""
        }
        dialogBinding.tvInfoLoteHistorial.text = "$vencInfo · $cantInt $unidadBase actuales$costoInfo"
        val dias = diasHastaVencerLote(lote.vencimiento)
        when {
            dias == null -> {
                dialogBinding.tvEstadoLoteHistorial.text = "Sin fecha"
                dialogBinding.tvEstadoLoteHistorial.setTextColor(Color.parseColor("#6B7280"))
            }
            dias < 0 -> {
                dialogBinding.tvEstadoLoteHistorial.text = "VENCIDO"
                dialogBinding.tvEstadoLoteHistorial.setTextColor(Color.parseColor("#DC2626"))
            }
            dias <= 30 -> {
                dialogBinding.tvEstadoLoteHistorial.text = "Vence en ${dias}d"
                dialogBinding.tvEstadoLoteHistorial.setTextColor(Color.parseColor("#D97706"))
            }
            else -> {
                dialogBinding.tvEstadoLoteHistorial.text = "Vigente"
                dialogBinding.tvEstadoLoteHistorial.setTextColor(Color.parseColor("#15803D"))
            }
        }

        val container = dialogBinding.containerMovimientosLote

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Cerrar", null)
            .create()

        // Leer movimientos desde Firebase
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRODUCTOS)
            .child(indiceOriginal)
            .child("lotes")
            .child(claveLote)
            .child("movimientos")
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                    dialogBinding.tvHistorialVacio.visibility = View.VISIBLE
                    dialogBinding.tvTotalEntradas.text = "0"
                    dialogBinding.tvTotalSalidas.text = "0"
                    dialog.show()
                    return@addOnSuccessListener
                }

                val movimientos = mutableListOf<MovimientoLote>()
                snapshot.children.forEach { child ->
                    child.getValue(MovimientoLote::class.java)?.let { movimientos.add(it) }
                }
                // Ordenar más reciente primero
                movimientos.sortByDescending { it.timestamp }

                var totalEntradas = 0.0
                var totalSalidas = 0.0

                movimientos.forEach { mov ->
                    val rowBinding = ItemMovimientoLoteBinding.inflate(layoutInflater, container, false)

                    val esEntrada = mov.tipo == "entrada" || mov.cantidad > 0
                    val esSalida = mov.tipo == "salida" || (mov.tipo != "entrada" && mov.cantidad < 0)
                    val esAjuste = mov.tipo == "ajuste"

                    // ícono y color según tipo
                    when {
                        esEntrada && !esAjuste -> {
                            rowBinding.tvIconoMovLote.text = "?"
                            rowBinding.tvIconoMovLote.setTextColor(Color.parseColor("#15803D"))
                            (rowBinding.tvIconoMovLote.parent as? MaterialCardView)
                                ?.setCardBackgroundColor(Color.parseColor("#F0FDF4"))
                            rowBinding.tvTipoMovLote.text = "Entrada"
                            rowBinding.tvTipoMovLote.setTextColor(Color.parseColor("#15803D"))
                            totalEntradas += mov.cantidad
                        }
                        esSalida && !esAjuste -> {
                            rowBinding.tvIconoMovLote.text = "?"
                            rowBinding.tvIconoMovLote.setTextColor(Color.parseColor("#DC2626"))
                            (rowBinding.tvIconoMovLote.parent as? MaterialCardView)
                                ?.setCardBackgroundColor(Color.parseColor("#FEF2F2"))
                            rowBinding.tvTipoMovLote.text = "Salida"
                            rowBinding.tvTipoMovLote.setTextColor(Color.parseColor("#DC2626"))
                            totalSalidas += kotlin.math.abs(mov.cantidad)
                        }
                        else -> {
                            rowBinding.tvIconoMovLote.text = "~"
                            rowBinding.tvIconoMovLote.setTextColor(Color.parseColor("#2563EB"))
                            (rowBinding.tvIconoMovLote.parent as? MaterialCardView)
                                ?.setCardBackgroundColor(Color.parseColor("#EFF6FF"))
                            rowBinding.tvTipoMovLote.text = "Ajuste"
                            rowBinding.tvTipoMovLote.setTextColor(Color.parseColor("#2563EB"))
                            if (mov.cantidad >= 0) totalEntradas += mov.cantidad
                            else totalSalidas += kotlin.math.abs(mov.cantidad)
                        }
                    }

                    // Cantidad con signo legible
                    val cantidadTexto = if (mov.cantidad >= 0)
                        "+${mov.cantidad.toInt()} $unidadBase"
                    else
                        "${mov.cantidad.toInt()} $unidadBase"
                    rowBinding.tvCantidadMovLote.text = cantidadTexto

                    rowBinding.tvMotivoMovLote.text = mov.motivo.ifBlank { "Sin motivo" }
                    if (mov.costoIngresoTotal > 0.0) {
                        rowBinding.tvCostoMovLote.text =
                            "Costo total de este movimiento: ${MonedaHelper.formatear(mov.costoIngresoTotal)} · ${MonedaHelper.formatear(mov.costoUnitario)} por ${unidadBase.lowercase()}"
                        rowBinding.tvCostoMovLote.visibility = View.VISIBLE
                    } else {
                        rowBinding.tvCostoMovLote.visibility = View.GONE
                    }

                    // Fecha en formato dd/MM/yy
                    val fechaMostrar = if (mov.fecha.isNotBlank()) {
                        try {
                            val partes = mov.fecha.split("-")
                            if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0].takeLast(2)}"
                            else mov.fecha
                        } catch (e: Exception) { mov.fecha }
                    } else "—"
                    rowBinding.tvFechaMovLote.text = fechaMostrar
                    rowBinding.tvUsuarioMovLote.text = mov.nombreUsuario.ifBlank { "—" }

                    container.addView(rowBinding.root)
                }

                // Totales en las tarjetas resumen
                val fmt = java.text.DecimalFormat("#,##0.#")
                dialogBinding.tvTotalEntradas.text = "${fmt.format(totalEntradas)} $unidadBase"
                dialogBinding.tvTotalSalidas.text = "${fmt.format(totalSalidas)} $unidadBase"

                dialog.show()
            }
            .addOnFailureListener {
                dialogBinding.tvHistorialVacio.visibility = View.VISIBLE
                dialogBinding.tvTotalEntradas.text = "0"
                dialogBinding.tvTotalSalidas.text = "0"
                dialog.show()
            }
    }

    private fun sanitizarClaveLote(numero: String): String {
        return numero.trim()
            .replace("[.#$\\[\\]/\\\\]".toRegex(), "-")
            .replace(" ", "_")
            .ifBlank { "L001" }
    }

    /** Devuelve los días que faltan hasta el último día del mes MM/AA.
     *  Negativo = ya venció. Null = no hay fecha. */
    private fun diasHastaVencerLote(vencimiento: String): Int? {
        return ProductUtils.diasHastaVencerLote(vencimiento)
    }

    private fun resolverConsumoLotesActual(
        lotes: Map<String, LoteProducto>,
        unidadesRequeridas: Int = 1
    ): LoteConsumoResolucion {
        return LoteConsumoRules.resolver(
            lotes = lotes,
            loteSeleccionado = loteConsumoSeleccionadoActual,
            seleccionManual = loteConsumoSeleccionManualActual,
            unidadesRequeridas = unidadesRequeridas
        )
    }

    private fun formatearLoteConsumoUi(loteInfo: Pair<String, LoteProducto>?): String {
        if (loteInfo == null) return "Sin lote disponible"
        val lote = loteInfo.second
        val numero = lote.numero.ifBlank { loteInfo.first }
        val cantidad = ProductUtils.cantidadVendibleLote(lote).toInt()
        return buildString {
            append(numero)
            if (lote.vencimiento.isNotBlank()) {
                append(" · vence ${lote.vencimiento}")
            }
            append(" · ${cantidad} disponibles")
        }
    }

    private fun lotesDesdeSnapshot(snapshot: DataSnapshot?): Map<String, LoteProducto> {
        if (snapshot == null) return emptyMap()
        return snapshot.children.mapNotNull { loteSnapshot ->
            val lote = loteSnapshot.getValue(LoteProducto::class.java)
            val clave = loteSnapshot.key.orEmpty()
            if (lote != null && clave.isNotBlank()) clave to lote else null
        }.toMap()
    }

    private fun sincronizarProductoOriginalDesdeSnapshot(snapshot: DataSnapshot?) {
        val productoActual = productoOriginalParaAuditoria ?: return
        val lotesActualizados = lotesDesdeSnapshot(snapshot?.child("lotes"))
        val cantidadActualizada = snapshot
            ?.child("cantidadinicial")
            ?.value
            ?.toString()
            ?.trim()
            .orEmpty()
            .ifBlank { productoActual.cantidadinicial }
        productoOriginalParaAuditoria = productoActual.copy(
            cantidadinicial = cantidadActualizada,
            loteConsumoSeleccionado = loteConsumoSeleccionadoActual,
            loteConsumoSeleccionManual = loteConsumoSeleccionManualActual,
            lotes = if (lotesActualizados.isNotEmpty() || snapshot?.child("lotes")?.exists() == true) {
                lotesActualizados
            } else {
                productoActual.lotes
            }
        )
        actualizarResumenRapidoStock()
        actualizarResumenInventario()
    }

    private fun bloquearLoteParaVenta(
        claveLote: String,
        motivoBloqueo: String,
        onSuccess: (() -> Unit)? = null
    ) {
        val productoRef = FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRODUCTOS)
            .child(indiceOriginal)

        productoRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val loteNode = currentData.child("lotes").child(claveLote)
                if (!loteNode.hasChildren()) return Transaction.abort()

                val cantidadVendibleActual = when (val raw = loteNode.child("cantidad").value) {
                    is Long -> raw.toDouble()
                    is Int -> raw.toDouble()
                    is Double -> raw
                    is String -> raw.trim().toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
                val cantidadBloqueadaActual = when (val raw = loteNode.child("cantidadBloqueada").value) {
                    is Long -> raw.toDouble()
                    is Int -> raw.toDouble()
                    is Double -> raw
                    is String -> raw.trim().toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }

                if (cantidadVendibleActual > 0.0) {
                    val stockNode = currentData.child("cantidadinicial")
                    val stockActual = when (val raw = stockNode.value) {
                        is Long -> raw.toInt()
                        is Int -> raw
                        is Double -> raw.toInt()
                        is String -> raw.trim().toIntOrNull() ?: 0
                        else -> 0
                    }
                    stockNode.value = (stockActual - cantidadVendibleActual.toInt()).coerceAtLeast(0).toString()
                }

                loteNode.child("cantidad").value = 0.0
                loteNode.child("cantidadBloqueada").value = cantidadBloqueadaActual + cantidadVendibleActual
                loteNode.child("motivoBloqueo").value = motivoBloqueo.ifBlank { "Bloqueo manual" }
                loteNode.child("timestampUltimoBloqueo").value = System.currentTimeMillis()

                val numeroLote = loteNode.child("numero").value?.toString().orEmpty().trim()
                val loteManualActual = currentData.child("loteConsumoSeleccionado").value?.toString().orEmpty().trim()
                val coincideSeleccion = loteManualActual.equals(claveLote, ignoreCase = true) ||
                    (numeroLote.isNotBlank() && loteManualActual.equals(numeroLote, ignoreCase = true))
                if (coincideSeleccion) {
                    currentData.child("loteConsumoSeleccionado").value = ""
                    currentData.child("loteConsumoSeleccionManual").value = false
                }

                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null || !committed) {
                    Toast.makeText(this@EditarProductodelInventario, "No se pudo bloquear el lote", Toast.LENGTH_SHORT).show()
                    return
                }

                loteConsumoSeleccionadoActual = currentData
                    ?.child("loteConsumoSeleccionado")
                    ?.value
                    ?.toString()
                    .orEmpty()
                    .trim()
                loteConsumoSeleccionManualActual = currentData
                    ?.child("loteConsumoSeleccionManual")
                    ?.getValue(Boolean::class.java) == true

                sincronizarProductoOriginalDesdeSnapshot(currentData)
                Toast.makeText(this@EditarProductodelInventario, "Lote bloqueado para venta", Toast.LENGTH_SHORT).show()
                onSuccess?.invoke()
            }
        })
    }

    private fun desbloquearLoteParaVenta(
        claveLote: String,
        onSuccess: (() -> Unit)? = null
    ) {
        val productoRef = FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRODUCTOS)
            .child(indiceOriginal)

        productoRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val loteNode = currentData.child("lotes").child(claveLote)
                if (!loteNode.hasChildren()) return Transaction.abort()

                val cantidadVendibleActual = when (val raw = loteNode.child("cantidad").value) {
                    is Long -> raw.toDouble()
                    is Int -> raw.toDouble()
                    is Double -> raw
                    is String -> raw.trim().toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
                val cantidadBloqueadaActual = when (val raw = loteNode.child("cantidadBloqueada").value) {
                    is Long -> raw.toDouble()
                    is Int -> raw.toDouble()
                    is Double -> raw
                    is String -> raw.trim().toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }

                if (cantidadBloqueadaActual > 0.0) {
                    val stockNode = currentData.child("cantidadinicial")
                    val stockActual = when (val raw = stockNode.value) {
                        is Long -> raw.toInt()
                        is Int -> raw
                        is Double -> raw.toInt()
                        is String -> raw.trim().toIntOrNull() ?: 0
                        else -> 0
                    }
                    stockNode.value = (stockActual + cantidadBloqueadaActual.toInt()).toString()
                }

                loteNode.child("cantidad").value = cantidadVendibleActual + cantidadBloqueadaActual
                loteNode.child("cantidadBloqueada").value = 0.0
                loteNode.child("motivoBloqueo").value = ""
                loteNode.child("timestampUltimoBloqueo").value = 0L

                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null || !committed) {
                    Toast.makeText(this@EditarProductodelInventario, "No se pudo desbloquear el lote", Toast.LENGTH_SHORT).show()
                    return
                }

                sincronizarProductoOriginalDesdeSnapshot(currentData)
                Toast.makeText(this@EditarProductodelInventario, "Lote desbloqueado para venta", Toast.LENGTH_SHORT).show()
                onSuccess?.invoke()
            }
        })
    }

    private fun mostrarDialogoBloquearLote(
        claveLote: String,
        lote: LoteProducto,
        onSuccess: (() -> Unit)? = null
    ) {
        val inputLayout = TextInputLayout(this).apply {
            hint = "Motivo del bloqueo"
        }
        val inputMotivo = TextInputEditText(this).apply {
            setText(lote.motivoBloqueo.ifBlank { "Mal estado" })
            setSelection(text?.length ?: 0)
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 2
        }
        inputLayout.addView(inputMotivo)

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
            addView(inputLayout)
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Bloquear lote para venta")
            .setMessage(
                "Este lote seguirá visible en gestión, pero no podrá venderse hasta que se desbloquee."
            )
            .setView(container)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Bloquear", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                val motivo = inputMotivo.text?.toString()?.trim().orEmpty()
                if (motivo.isBlank()) {
                    inputLayout.error = "Ingresa el motivo del bloqueo"
                    sacudirCampoConError(inputLayout)
                    inputMotivo.requestFocus()
                    return@setOnClickListener
                }
                inputLayout.error = null
                bloquearLoteParaVenta(
                    claveLote = claveLote,
                    motivoBloqueo = motivo
                ) {
                    dialog.dismiss()
                    onSuccess?.invoke()
                }
            }
        }
        dialog.show()
    }

    private fun confirmarDesbloqueoLote(
        claveLote: String,
        lote: LoteProducto,
        onSuccess: (() -> Unit)? = null
    ) {
        val motivoActual = lote.motivoBloqueo.trim().ifBlank { "Sin motivo registrado" }
        MaterialAlertDialogBuilder(this)
            .setTitle("Desbloquear lote")
            .setMessage(
                "Este lote volverá a quedar disponible para venta.\n\nMotivo actual: $motivoActual"
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Desbloquear") { _, _ ->
                desbloquearLoteParaVenta(claveLote, onSuccess)
            }
            .show()
    }

    private fun guardarPreferenciaConsumoLote(
        numeroLote: String,
        seleccionManual: Boolean,
        onSuccess: (() -> Unit)? = null
    ) {
        val numeroNormalizado = numeroLote.trim()
        val updates = mapOf<String, Any>(
            "loteConsumoSeleccionado" to if (seleccionManual) numeroNormalizado else "",
            "loteConsumoSeleccionManual" to seleccionManual
        )

        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRODUCTOS)
            .child(indiceOriginal)
            .updateChildren(updates)
            .addOnSuccessListener {
                loteConsumoSeleccionadoActual = if (seleccionManual) numeroNormalizado else ""
                loteConsumoSeleccionManualActual = seleccionManual
                productoOriginalParaAuditoria = productoOriginalParaAuditoria?.copy(
                    loteConsumoSeleccionado = loteConsumoSeleccionadoActual,
                    loteConsumoSeleccionManual = loteConsumoSeleccionManualActual
                )
                onSuccess?.invoke()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "No se pudo actualizar el lote de consumo",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun actualizarCamposStockDespuesDeAjuste(stockDespues: Int, fuePorPresentacion: Boolean, unidadesPorPresentacionSeleccionada: Int) {
        binding.editTextCantidad.setText(stockDespues.toString())
        binding.editTextCantidad.tag = stockDespues.toString()
        productoOriginalParaAuditoria?.cantidadinicial = stockDespues.toString()

        if (binding.radioPorPaquetes.isChecked) {
            val unidadesPorCajaPantalla = binding.editUnidadesPorCaja.text?.toString()?.trim()?.toIntOrNull() ?: 0
            if (unidadesPorCajaPantalla > 0) {
                val presentacionesCompletas = stockDespues / unidadesPorCajaPantalla
                val unidadesSueltas = stockDespues % unidadesPorCajaPantalla
                binding.editCantidadPresentacion.setText(presentacionesCompletas.toString())
                calcularStockTotal()
                mostrarResumenEquivalenciaStock(
                    stockTotalUnidades = stockDespues,
                    unidadesPorPresentacion = unidadesPorCajaPantalla,
                    presentacionesCompletas = presentacionesCompletas,
                    unidadesSueltas = unidadesSueltas
                )
            }
        } else if (fuePorPresentacion && unidadesPorPresentacionSeleccionada > 0) {
            val presentacionesCompletas = stockDespues / unidadesPorPresentacionSeleccionada
            val unidadesSueltas = stockDespues % unidadesPorPresentacionSeleccionada
            mostrarResumenEquivalenciaStock(
                stockTotalUnidades = stockDespues,
                unidadesPorPresentacion = unidadesPorPresentacionSeleccionada,
                presentacionesCompletas = presentacionesCompletas,
                unidadesSueltas = unidadesSueltas
            )
        } else {
            actualizarHelperStockSegunModo()
        }
        val stockMinimoActual = if (binding.radioPorPaquetes.isChecked) {
            val stockMinCajas = binding.editTextStockPresentacion.text?.toString()?.trim()?.toIntOrNull() ?: 0
            (stockMinCajas * (binding.editUnidadesPorCaja.text?.toString()?.trim()?.toIntOrNull() ?: 0)).toString()
        } else {
            binding.editTextStock.text?.toString()?.trim().orEmpty()
        }

        verificarCantidadYStock(stockDespues.toString(), stockMinimoActual)
    }

    private fun obtenerPresentacionesAjustables(unidadBase: String): List<PresentacionProducto> {
        val unidadBaseNormalizada = unidadBase.trim().lowercase()
        return listaPresentaciones.filter { presentacion ->
            val nombre = presentacion.nombre.trim().lowercase()
            val esUnidadDuplicada = nombre == unidadBaseNormalizada ||
                nombre == "unidad" ||
                nombre == "unidades"
            presentacion.cantidad > 1 && !esUnidadDuplicada
        }
    }

    private fun obtenerNombrePresentacionAjuste(): String {
        val canonical = (binding.editTextPresentacionPrincipal.tag as? String)?.trim().orEmpty()
        val visible = if (canonical.isNotBlank()) {
            PresentacionesTiendaConfigManager.nombreVisible(canonical)
        } else {
            binding.editTextPresentacionPrincipal.text?.toString()?.trim().orEmpty()
        }
        return visible.ifBlank { "presentación" }.lowercase()
    }

    private fun actualizarHelperStockSegunModo() {
        val helperStock = binding.editTextCantidad.parent?.parent as? TextInputLayout
        val helperPresentacion = binding.editCantidadPresentacion.parent?.parent as? TextInputLayout
        val nombrePresentacion = obtenerNombrePresentacionAjuste()
        val nombrePresentacionPlural = pluralizarTexto(nombrePresentacion)

        helperStock?.helperText = "El stock actual se cambia desde \"Ajustar stock\""
        helperPresentacion?.helperText = if (binding.radioPorPaquetes.isChecked) {
            "La cantidad de $nombrePresentacionPlural se cambia desde \"Ajustar stock\""
        } else {
            "La cantidad por presentación se cambia desde \"Ajustar stock\""
        }

        actualizarHintStockMinimoSegunAviso()
        actualizarHelperStockMinimoPresentacion()
        actualizarResumenRapidoStock()
        actualizarResumenInventario()
    }

    private fun actualizarHintStockMinimoSegunAviso() {
        val tilStockMinimo = binding.editTextStock.parent?.parent as? TextInputLayout
        val tilStockMinPresentacion = binding.editTextStockPresentacion.parent?.parent as? TextInputLayout
        val unidadBaseUi = formatearEtiquetaUi(
            binding.editTextUnidadBase.text?.toString().orEmpty().trim()
        )
        val presentacionUi = formatearEtiquetaUi(
            obtenerNombrePresentacionAjuste().trim()
        )
        val hintUnidadBase = "Stock minimo por unidad base (${unidadBaseUi.lowercase()})"
        val hintPresentacion = "Stock minimo por presentacion principal (${presentacionUi.lowercase()})"
        val placeholderUnidadBase = "Ejemplo: minimo en ${unidadBaseUi.lowercase()}"
        val placeholderPresentacion = "Ejemplo: minimo en ${presentacionUi.lowercase()}"

        if (tilStockMinimo?.hint?.toString() != hintUnidadBase) {
            tilStockMinimo?.hint = hintUnidadBase
        }
        if (tilStockMinPresentacion?.hint?.toString() != hintPresentacion) {
            tilStockMinPresentacion?.hint = hintPresentacion
        }
        tilStockMinimo?.placeholderText =
            if (binding.radioPorUnidades.isChecked) placeholderUnidadBase else null
        tilStockMinPresentacion?.placeholderText =
            if (binding.radioPorPaquetes.isChecked) placeholderPresentacion else null
    }

    private fun actualizarResumenesStockTrasEdicion() {
        actualizarHintStockMinimoSegunAviso()
        actualizarHelperStockMinimoPresentacion()
        actualizarResumenRapidoStock()
        actualizarResumenInventario()
    }

    private fun validarStockMinimoActivoEnTiempoReal() {
        if (cargandoDatosIniciales) return

        val layoutUnidadBase = binding.editTextStock.parent?.parent as? TextInputLayout
        val layoutPresentacion = binding.editTextStockPresentacion.parent?.parent as? TextInputLayout
        val esPorPresentacion = binding.radioPorPaquetes.isChecked

        val inputActivo = if (esPorPresentacion) binding.editTextStockPresentacion else binding.editTextStock
        val layoutActivo = if (esPorPresentacion) layoutPresentacion else layoutUnidadBase
        val inputInactivo = if (esPorPresentacion) binding.editTextStock else binding.editTextStockPresentacion
        val layoutInactivo = if (esPorPresentacion) layoutUnidadBase else layoutPresentacion

        limpiarEstadoTiempoRealStockMinimo(layoutInactivo, inputInactivo, esPorPresentacion = !esPorPresentacion)

        val texto = inputActivo.text?.toString()?.trim().orEmpty()
        val cantidadDisponibleActual = if (esPorPresentacion) {
            binding.editCantidadPresentacion.text?.toString()?.trim()?.toIntOrNull() ?: 0
        } else {
            binding.editTextCantidad.text?.toString()?.trim()?.toIntOrNull() ?: 0
        }

        if (texto == "0") {
            mostrarDecisionSinAvisoStockMinimo(layoutActivo, inputActivo, esPorPresentacion)
            return
        }

        val mensajeError = when {
            texto.isBlank() -> "Corrige el stock m\u00ednimo"
            texto.toIntOrNull() == null -> "El stock m\u00ednimo ingresado no es v\u00e1lido"
            texto.toIntOrNull()!! < 0 -> "El valor ingresado para stock m\u00ednimo no cumple la validaci\u00f3n permitida"
            texto.toIntOrNull()!! > cantidadDisponibleActual ->
                "El stock m\u00ednimo no puede ser mayor que la cantidad disponible actual ($cantidadDisponibleActual)"
            else -> null
        }

        if (mensajeError != null) {
            mostrarErrorTiempoRealStockMinimo(layoutActivo, inputActivo, mensajeError, esPorPresentacion)
        } else {
            limpiarEstadoTiempoRealStockMinimo(layoutActivo, inputActivo, esPorPresentacion)
        }
    }

    private fun mostrarErrorTiempoRealStockMinimo(
        layout: TextInputLayout?,
        input: TextInputEditText,
        mensaje: String,
        esPorPresentacion: Boolean
    ) {
        val yaEstabaEnError = if (esPorPresentacion) {
            stockMinimoPresentacionEnErrorTiempoReal
        } else {
            stockMinimoUnidadEnErrorTiempoReal
        }

        layout?.error = mensaje
        layout?.isErrorEnabled = true
        input.error = null

        if (!yaEstabaEnError) {
            sacudirCampoConError(layout ?: input)
        }

        if (esPorPresentacion) {
            stockMinimoPresentacionEnErrorTiempoReal = true
            stockMinimoPresentacionSinAvisoTiempoReal = false
        } else {
            stockMinimoUnidadEnErrorTiempoReal = true
            stockMinimoUnidadSinAvisoTiempoReal = false
        }
    }

    private fun mostrarDecisionSinAvisoStockMinimo(
        layout: TextInputLayout?,
        input: TextInputEditText,
        esPorPresentacion: Boolean
    ) {
        layout?.error = "✓ Sin avisar stock mínimo"
        layout?.isErrorEnabled = true
        layout?.error = "\u2713 Sin avisar stock m\u00ednimo"
        input.error = null

        if (esPorPresentacion) {
            stockMinimoPresentacionEnErrorTiempoReal = false
            stockMinimoPresentacionSinAvisoTiempoReal = true
        } else {
            stockMinimoUnidadEnErrorTiempoReal = false
            stockMinimoUnidadSinAvisoTiempoReal = true
        }
    }

    private fun limpiarEstadoTiempoRealStockMinimo(
        layout: TextInputLayout?,
        input: TextInputEditText,
        esPorPresentacion: Boolean
    ) {
        layout?.error = null
        layout?.isErrorEnabled = false
        input.error = null

        if (esPorPresentacion) {
            stockMinimoPresentacionEnErrorTiempoReal = false
            stockMinimoPresentacionSinAvisoTiempoReal = false
        } else {
            stockMinimoUnidadEnErrorTiempoReal = false
            stockMinimoUnidadSinAvisoTiempoReal = false
        }
    }

    private fun sacudirCampoConError(view: View) {
        view.animate().cancel()
        ObjectAnimator.ofFloat(
            view,
            View.TRANSLATION_X,
            0f,
            18f,
            -14f,
            10f,
            -6f,
            3f,
            0f
        ).apply {
            duration = 320L
            start()
        }
    }

    // ---------------------------------------------------------------------------------------------
    // TECLADO
    // ---------------------------------------------------------------------------------------------

    private fun configurarOcultarTecladoAlTocarFuera() {
        binding.root.setOnTouchListener { _, _ ->
            ocultarTeclado()
            binding.root.clearFocus()
            false
        }

        binding.constraintcontainer.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_MOVE) {
                ocultarTeclado()
                binding.root.clearFocus()
            }
            false
        }
    }

    private fun configurarOcultarTecladoPorIme() {
        val onDone = TextView.OnEditorActionListener { v, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_NEXT ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_GO ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND
            ) {
                actualizarResumenesStockTrasEdicion()
                ocultarTeclado()
                v.clearFocus()
            }
            false
        }

        binding.editTextNombre.setOnEditorActionListener(onDone)
        binding.editTextCategoria.setOnEditorActionListener(onDone)
        binding.editTextCompra.setOnEditorActionListener(onDone)
        binding.editTextCantidad.setOnEditorActionListener(onDone)
        binding.editTextStock.setOnEditorActionListener(onDone)
        binding.editCantidadPresentacion.setOnEditorActionListener(onDone)
        binding.editUnidadesPorCaja.setOnEditorActionListener(onDone)
        binding.editTextStockPresentacion.setOnEditorActionListener(onDone)
        binding.editTextUnidadBase.setOnEditorActionListener(onDone)
        binding.editTextPresentacionPrincipal.setOnEditorActionListener(onDone)
        binding.editTextVencimiento.setOnEditorActionListener(onDone)
    }

    private fun actualizarResumenInventario() {
        val unidadBase = binding.editTextUnidadBase.text?.toString().orEmpty().trim().ifBlank { "unidad" }

        val stockActual = (binding.editTextCantidad.text?.toString()?.trim()?.toIntOrNull() ?: 0).coerceAtLeast(0)
        val stockMinimo = obtenerStockMinimoActualEnUnidadBase()
        val basePorUnidad = obtenerBasePorUnidadActual()
        val lotesActuales = productoOriginalParaAuditoria?.lotes.orEmpty()
        val disponibilidadVenta = ProductUtils.evaluarDisponibilidadVenta(stockActual.toDouble(), lotesActuales)
        val stockFisico = disponibilidadVenta.stockFisico
        val stockVendibleTexto = ProductoResumenInventarioBuilder.formatearStockConUnidades(
            stockActual,
            unidadBase,
            basePorUnidad
        )
        ProductoResumenInventarioBuilder.formatearStockConUnidades(
            stockFisico,
            unidadBase,
            basePorUnidad
        )
        val minTexto = ProductoResumenInventarioBuilder.formatearStockConUnidades(
            stockMinimo,
            unidadBase,
            basePorUnidad
        )
        val resumen = "Unidad: $unidadBase\nStock: $stockVendibleTexto   ·   Mín: $minTexto"
        binding.tvResumenInventario.text = resumen
        binding.tvResumenInventarioFooter.text = resumen
        binding.tvInventarioSummary.text = "Unidad: ${formatearEtiquetaUi(unidadBase)}\nStock: $stockVendibleTexto • Mínimo: $minTexto"
        binding.tvHeroStockMinimo.text = "Stock mín: $minTexto"
    }

    private fun obtenerBasePorUnidadActual(): Double {
        return ProductoInventarioMetrics.obtenerBasePorUnidadActual(
            unidadBase = binding.editTextUnidadBase.text?.toString().orEmpty().trim(),
            esPorPaquetes = binding.radioPorPaquetes.isChecked,
            valorPaqueteTexto = binding.editUnidadesPorCaja.text?.toString().orEmpty(),
            basePorUnidadTexto = null,
            usarCampoBasePorUnidad = false,
            basePorUnidadFallback = basePorUnidad
        )
    }

    private fun obtenerStockMinimoActualEnUnidadBase(): Int {
        return ProductoInventarioMetrics.obtenerStockMinimoEnUnidadBase(
            unidadBase = binding.editTextUnidadBase.text?.toString().orEmpty().trim(),
            esPorPaquetes = binding.radioPorPaquetes.isChecked,
            stockMinimoTexto = binding.editTextStock.text?.toString().orEmpty(),
            stockMinimoContenedoresTexto = binding.editTextStockPresentacion.text?.toString().orEmpty(),
            valorPorContenedorTexto = binding.editUnidadesPorCaja.text?.toString().orEmpty(),
            basePorUnidadActual = obtenerBasePorUnidadActual(),
            stockMinimoTag = null,
            valorPorContenedorFallback = 0,
            multiplicarPorBaseSiNoEsMlOg = false
        ).coerceAtLeast(0)
    }

    private fun actualizarResumenRapidoStock() {
        // Resumen visible arriba: Stock | Mínimo | Estado (en unidad base)
        val stockActual = (binding.editTextCantidad.text?.toString()?.trim()?.toIntOrNull() ?: 0).coerceAtLeast(0)
        val minimoActual = obtenerStockMinimoActualEnUnidadBase()
        val lotesActuales = productoOriginalParaAuditoria?.lotes.orEmpty()
        val disponibilidadVenta = ProductUtils.evaluarDisponibilidadVenta(stockActual.toDouble(), lotesActuales)
        val stockFisico = disponibilidadVenta.stockFisico

        val resumenRapido = ProductoResumenInventarioBuilder.construirResumenRapidoStock(
            unidadBaseRaw = binding.editTextUnidadBase.text?.toString(),
            stockActual = stockActual,
            minimoActual = minimoActual
        )

        val unidadUiVendible = UnidadBaseHelper.formatear(
            binding.editTextUnidadBase.text?.toString(),
            stockActual
        )
        val unidadUiFisico = UnidadBaseHelper.formatear(
            binding.editTextUnidadBase.text?.toString(),
            stockFisico
        )
        binding.tvResumenStock.text = "Vendible: $stockActual $unidadUiVendible"
        binding.tvResumenMinimo.text = "Fisico: $stockFisico $unidadUiFisico"
        binding.tvHeroVendible.text = "Vendible: $stockActual $unidadUiVendible"
        binding.tvHeroFisico.text = "Físico: $stockFisico $unidadUiFisico"
        binding.chipResumenEstadoStock.text = resumenRapido.style.estado
        binding.chipResumenEstadoStock.chipBackgroundColor =
            ColorStateList.valueOf(Color.parseColor(resumenRapido.style.bg))
        binding.chipResumenEstadoStock.setTextColor(Color.parseColor(resumenRapido.style.fg))
        binding.chipResumenEstadoStock.chipStrokeColor =
            ColorStateList.valueOf(Color.parseColor(resumenRapido.style.stroke))
        binding.tvHeroEstado.text = resumenRapido.style.estado
    }

    private fun actualizarHelperStockMinimoPresentacion() {
        val helperLayout = binding.editTextStockPresentacion.parent?.parent as? TextInputLayout ?: return

        if (!binding.radioPorPaquetes.isChecked) {
            helperLayout.helperText = null
            helperLayout.suffixText = null
            return
        }

        val helperData = ProductoResumenInventarioBuilder.construirHelperStockMinimoPresentacion(
            unidadBase = binding.editTextUnidadBase.text.toString().trim(),
            stockMinContenedores = binding.editTextStockPresentacion.text.toString().trim().toIntOrNull(),
            valorPorContenedor = binding.editUnidadesPorCaja.text.toString().trim().toIntOrNull()
        )

        helperLayout.suffixText = helperData.suffixText
        helperLayout.helperText = helperData.helperText
    }

    private fun mostrarResumenEquivalenciaStock(stockTotalUnidades: Int, unidadesPorPresentacion: Int, presentacionesCompletas: Int, unidadesSueltas: Int) {
        val helperLayout = binding.editCantidadPresentacion.parent?.parent as? TextInputLayout ?: return
        val nombrePresentacion = obtenerNombrePresentacionAjuste()
        val nombrePresentacionTexto = formatearCantidadConEtiqueta(
            cantidad = presentacionesCompletas,
            singular = nombrePresentacion,
            plural = pluralizarTexto(nombrePresentacion)
        )
        val unidadBase = binding.editTextUnidadBase.text?.toString()?.trim().orEmpty().ifBlank { "unidades" }
        val unidadBaseTexto = formatearCantidadConEtiqueta(
            cantidad = unidadesSueltas,
            singular = unidadBase.lowercase(),
            plural = pluralizarTexto(unidadBase.lowercase())
        )

        helperLayout.helperText = when {
            unidadesSueltas > 0 -> {
                "Stock actual: $stockTotalUnidades $unidadBase. Equivale a $nombrePresentacionTexto y $unidadBaseTexto sueltas."
            }
            unidadesPorPresentacion > 0 -> {
                "Stock actual: $stockTotalUnidades $unidadBase. Equivale a $nombrePresentacionTexto."
            }
            else -> null
        }
    }

    private fun formatearCantidadConEtiqueta(cantidad: Int, singular: String, plural: String): String {
        return if (cantidad == 1) "$cantidad $singular" else "$cantidad $plural"
    }

    private fun actualizarVisibilidadVencimientoGeneral(cantidadLotes: Int) {
        ocultarVencimientoGeneralPorLotes = cantidadLotes > 1
        binding.layoutVencimientoGeneral.visibility =
            if (ocultarVencimientoGeneralPorLotes) View.GONE else View.VISIBLE
        binding.textEstadoVencimiento.visibility =
            if (ocultarVencimientoGeneralPorLotes) View.GONE else binding.textEstadoVencimiento.visibility
        binding.tvAyudaVencimientoPorLotes.visibility =
            if (ocultarVencimientoGeneralPorLotes) View.VISIBLE else View.GONE
    }

    private fun sincronizarUiVencimientoPorCantidadLotes(
        cantidadLotes: Int,
        vencimientoGeneralSugerido: String = ""
    ) {
        if (cantidadLotes <= 1 && vencimientoGeneralSugerido.isNotBlank()) {
            binding.editTextVencimiento.setText(vencimientoGeneralSugerido)
        }
        actualizarVisibilidadVencimientoGeneral(cantidadLotes)
        verificarVencimiento()
    }

    private fun pluralizarTexto(texto: String): String {
        val limpio = texto.trim().lowercase()
        if (limpio.isBlank()) return "items"

        return when {
            limpio.endsWith("s") || limpio.endsWith("x") -> limpio
            limpio.endsWith("z") -> limpio.dropLast(1) + "ces"
            limpio.endsWith("ión") -> limpio.dropLast(3) + "iones"
            else -> "${limpio}s"
        }
    }

    private fun formatearEtiquetaUi(texto: String): String {
        val limpio = texto.trim()
        if (limpio.isBlank()) return "Unidad"
        return limpio.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase() else char.toString()
        }
    }

    /**
     * Devuelve el ícono apropiado para una opción de presentación (Unidad, Caja,
     * Blíster, Sobre, etc.) usado en los chips de Ingresar stock / Registrar salida.
     */
    private fun resolverIconoPresentacionMovimiento(nombre: String): Int {
        return when (nombre.trim().lowercase(Locale.getDefault())) {
            "caja", "cajas" -> R.drawable.iconcaja
            "blister", "blíster", "blísteres", "blisters" -> R.drawable.iconpastillas
            "sobre", "sobres" -> R.drawable.inventory_2icon
            "frasco", "frascos" -> R.drawable.ic_inventory_volume
            else -> R.drawable.iconpastillas
        }
    }

    /**
     * Oculta el teclado y quita el foco de cualquier EditText en pantalla.
     * Se usa cuando el usuario toca fuera de los campos en los diálogos de movimiento.
     */
    private fun ocultarTecladoYQuitarFoco() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        currentFocus?.let { foco ->
            imm?.hideSoftInputFromWindow(foco.windowToken, 0)
            foco.clearFocus()
        }
    }

    private fun formatearEtiquetaUi(texto: CharSequence?): String {
        return formatearEtiquetaUi(texto?.toString().orEmpty())
    }

    private fun aplicarCierreDropdownAlRecibirFoco(autoComplete: MaterialAutoCompleteTextView) {
        autoComplete.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                (v as? MaterialAutoCompleteTextView)?.dismissDropDown()
            }
        }
    }

    private fun configurarAutoCompleteEditable(textInputLayout: TextInputLayout?, autoComplete: MaterialAutoCompleteTextView, lista: List<String>) {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            lista
        )

        autoComplete.setAdapter(adapter)
        autoComplete.threshold = Int.MAX_VALUE
        autoComplete.inputType =
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        autoComplete.isFocusable = true
        autoComplete.isFocusableInTouchMode = true
        autoComplete.isCursorVisible = true
        aplicarCierreDropdownAlRecibirFoco(autoComplete)

        // Abrir la lista solo desde la flecha, sin interrumpir la escritura en el campo.
        textInputLayout?.apply {
            endIconMode = TextInputLayout.END_ICON_CUSTOM
            setEndIconDrawable(R.drawable.ic_arrow_drop_down)
            endIconContentDescription = "Ver sugerencias"
            setEndIconOnClickListener {
                autoComplete.requestFocus()
                autoComplete.post {
                    if (adapter.count > 0) autoComplete.showDropDown()
                }
            }
        }

        autoComplete.setOnItemClickListener { _, _, _, _ ->
            autoComplete.dismissDropDown()
            autoComplete.clearFocus()
            binding.main.requestFocus()
            ocultarTeclado()
        }
    }

    /**
     * AutoComplete fijo (no editable): el usuario solo selecciona de opciones.
     * Ideal para campos que queremos estandarizar (ej: unidad base de stock).
     */
    private fun configurarAutoCompleteFijo(
        textInputLayout: TextInputLayout?,
        autoComplete: MaterialAutoCompleteTextView,
        lista: List<String>
    ) {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            lista
        )

        autoComplete.setAdapter(adapter)
        autoComplete.threshold = Int.MAX_VALUE
        autoComplete.inputType = InputType.TYPE_NULL
        autoComplete.keyListener = null
        autoComplete.isCursorVisible = false
        aplicarCierreDropdownAlRecibirFoco(autoComplete)

        textInputLayout?.apply {
            endIconMode = TextInputLayout.END_ICON_CUSTOM
            setEndIconDrawable(R.drawable.ic_arrow_drop_down)
            endIconContentDescription = "Ver opciones"
            setEndIconOnClickListener {
                autoComplete.post { if ((autoComplete.adapter?.count ?: 0) > 0) autoComplete.showDropDown() }
            }
        }

        autoComplete.setOnClickListener {
            autoComplete.post { if ((autoComplete.adapter?.count ?: 0) > 0) autoComplete.showDropDown() }
        }

        autoComplete.setOnItemClickListener { _, _, _, _ ->
            autoComplete.dismissDropDown()
            autoComplete.clearFocus()
            binding.main.requestFocus()
            ocultarTeclado()
        }
    }

    private fun configurarAutoCompleteSoloCampo(autoComplete: MaterialAutoCompleteTextView, lista: List<String>) {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            lista
        )

        autoComplete.setAdapter(adapter)
        autoComplete.threshold = 1
        autoComplete.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        autoComplete.isFocusable = true
        autoComplete.isFocusableInTouchMode = true
        autoComplete.isCursorVisible = true
        aplicarCierreDropdownAlRecibirFoco(autoComplete)

        autoComplete.setOnItemClickListener { _, _, _, _ ->
            autoComplete.dismissDropDown()
        }
    }

    private fun configurarAutoCompletesBase() {
        binding.editTextCategoria.setOnItemClickListener { parent, _, position, _ ->
            // Android ya pone el texto automáticamente, solo limpiamos la UI
            binding.editTextCategoria.clearFocus()
            binding.main.requestFocus()
            ocultarTeclado()
        }

        binding.editTextNombre.setOnItemClickListener { parent, _, position, _ ->
            binding.editTextNombre.clearFocus()
            binding.main.requestFocus()
            ocultarTeclado()
        }

        configurarCampoCategoriaInteligente()
    }

    private fun configurarCampoCategoriaInteligente() {
        if (!categoriaSugerenciasConfiguradas) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                mutableListOf<String>()
            )
            categoriaAdapter = adapter

            binding.editTextCategoria.setAdapter(adapter)
            binding.editTextCategoria.threshold = Int.MAX_VALUE
            binding.editTextCategoria.inputType =
                InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            binding.editTextCategoria.isFocusable = true
            binding.editTextCategoria.isFocusableInTouchMode = true
            binding.editTextCategoria.isCursorVisible = true
            aplicarCierreDropdownAlRecibirFoco(binding.editTextCategoria)

            binding.tilCategoria.apply {
                endIconMode = TextInputLayout.END_ICON_CUSTOM
                setEndIconDrawable(R.drawable.ic_arrow_drop_down)
                endIconContentDescription = "Ver categorías sugeridas"
                setEndIconOnClickListener {
                    binding.editTextCategoria.requestFocus()
                    actualizarSugerenciasCategoria(
                        binding.editTextCategoria.text?.toString().orEmpty(),
                        forzarMostrar = true
                    )
                }
            }

            binding.editTextCategoria.setOnItemClickListener { parent, _, position, _ ->
                val categoriaElegida = parent.getItemAtPosition(position)?.toString().orEmpty().trim()
                if (categoriaElegida.isNotBlank()) {
                    actualizandoCategoriaProgramaticamente = true
                    binding.editTextCategoria.setText(categoriaElegida, false)
                    binding.editTextCategoria.setSelection(categoriaElegida.length)
                    actualizandoCategoriaProgramaticamente = false
                }

                binding.editTextCategoria.dismissDropDown()
                binding.editTextCategoria.clearFocus()
                binding.main.requestFocus()
                ocultarTeclado()
            }

            binding.editTextCategoria.setOnClickListener {
                actualizarSugerenciasCategoria(binding.editTextCategoria.text?.toString().orEmpty())
            }

            binding.editTextCategoria.addTextChangedListener { editable ->
                if (actualizandoCategoriaProgramaticamente) return@addTextChangedListener
                binding.editTextCategoria.error = null
                binding.tilCategoria.error = null
                actualizarSugerenciasCategoria(editable?.toString().orEmpty())
                binding.editTextCategoria.dismissDropDown()
            }

            categoriaSugerenciasConfiguradas = true
        }

        actualizarSugerenciasCategoria(binding.editTextCategoria.text?.toString().orEmpty())
    }

    private fun actualizarSugerenciasCategoria(
        consulta: String,
        forzarMostrar: Boolean = false
    ) {
        val adapter = categoriaAdapter ?: return
        val sugerencias = ProductUtils.sugerirCategoriasSimilares(
            consulta = consulta,
            categorias = listaCategoria.map { it.nombre },
            limite = if (consulta.trim().isBlank()) 10 else 6
        )

        adapter.clear()
        adapter.addAll(sugerencias)
        adapter.notifyDataSetChanged()

        val consultaLimpia = consulta.trim()
        val sugerenciaPrincipal = sugerencias.firstOrNull()
        val coincidenciaExacta = ProductUtils.resolverCategoriaExistente(
            consultaLimpia,
            sugerencias
        ) != null
        val debeMostrar = if (forzarMostrar) {
            sugerencias.isNotEmpty()
        } else {
            false
        }
        binding.tilCategoria.helperText = null
        renderizarChipsCategoriaSugerida(
            consulta = consultaLimpia,
            sugerencias = sugerencias,
            coincidenciaExacta = coincidenciaExacta,
            sugerenciaPrincipal = sugerenciaPrincipal
        )

        binding.editTextCategoria.post {
            if (debeMostrar) {
                binding.editTextCategoria.showDropDown()
            } else {
                binding.editTextCategoria.dismissDropDown()
            }
        }
    }

    private fun renderizarChipsCategoriaSugerida(
        consulta: String,
        sugerencias: List<String>,
        coincidenciaExacta: Boolean,
        sugerenciaPrincipal: String?
    ) {
        val chips = if (
            consulta.length >= 3 &&
            !coincidenciaExacta &&
            sugerenciaPrincipal != null
        ) {
            sugerencias
                .filterNot { it.equals(consulta, ignoreCase = true) }
                .take(3)
        } else {
            emptyList()
        }

        binding.chipGroupCategoriaSugerencias.removeAllViews()
        if (chips.isEmpty()) {
            binding.chipGroupCategoriaSugerencias.visibility = View.GONE
            return
        }

        chips.forEach { sugerencia ->
            binding.chipGroupCategoriaSugerencias.addView(
                crearChipCategoriaSugerida(
                    nombre = sugerencia,
                    consulta = consulta
                )
            )
        }
        binding.chipGroupCategoriaSugerencias.visibility = View.VISIBLE
        asegurarVisibilidadSugerenciasCategoria()
    }

    private fun crearChipCategoriaSugerida(nombre: String, consulta: String): Chip {
        val density = resources.displayMetrics.density
        return Chip(this).apply {
            text = construirTextoChipCategoria(nombre, consulta)
            isCheckable = false
            isClickable = true
            isCloseIconVisible = false
            setEnsureMinTouchTargetSize(true)
            minHeight = (40 * density).toInt()
            chipCornerRadius = 18f * density
            chipStartPadding = 14f * density
            chipEndPadding = 14f * density
            textStartPadding = 4f * density
            textEndPadding = 4f * density
            chipStrokeWidth = 1f * density
            chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#CBD5E1"))
            chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#F8FAFC"))
            setTextColor(Color.parseColor("#334155"))
            setOnClickListener {
                actualizandoCategoriaProgramaticamente = true
                binding.editTextCategoria.setText(nombre, false)
                binding.editTextCategoria.setSelection(nombre.length)
                actualizandoCategoriaProgramaticamente = false
                binding.editTextCategoria.requestFocus()
                binding.editTextCategoria.dismissDropDown()
                posicionarScroll(binding.editTextCategoria, 180)
                actualizarSugerenciasCategoria(nombre)
            }
        }
    }

    private fun asegurarVisibilidadSugerenciasCategoria() {
        if (!binding.editTextCategoria.hasFocus()) return
        binding.chipGroupCategoriaSugerencias.post {
            if (binding.chipGroupCategoriaSugerencias.visibility == View.VISIBLE) {
                posicionarScroll(
                    binding.chipGroupCategoriaSugerencias,
                    offsetScrollCategoriaSugerida
                )
            }
        }
    }

    private fun construirTextoChipCategoria(nombre: String, consulta: String): CharSequence {
        val rango = ProductUtils.encontrarRangoSugerenciaCategoria(nombre, consulta) ?: return nombre
        return SpannableStringBuilder(nombre).apply {
            setSpan(
                StyleSpan(Typeface.BOLD),
                rango.first,
                rango.last + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(Color.parseColor("#0F766E")),
                rango.first,
                rango.last + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun mostrarCarga(
        cargando: Boolean,
        titulo: String? = null,
        mensaje: String? = null
    ) {
        val overlay = binding.layoutCargando
        val cardCarga = binding.root.findViewById<View>(R.id.cardCarga)
        val iosLoader = binding.root.findViewById<ImageView>(R.id.ivIosLoader)
        overlayCargaVisible = cargando

        // Textos "humanos" (se sobreescriben cuando se llama con titulo/mensaje).
        if (cargando) {
            binding.tvTituloCargando.text = titulo ?: "Cargando"
            binding.tvMensajeCargando.text = mensaje ?: "Un momento..."
        }

        overlay.animate().cancel()
        cardCarga?.animate()?.cancel()

        if (cargando) {
            overlay.bringToFront()
            overlay.translationZ = 1000f
            overlay.alpha = 0f
            cardCarga?.alpha = 0f
            cardCarga?.scaleX = 0.96f
            cardCarga?.scaleY = 0.96f
            cardCarga?.translationY = dpToPx(12).toFloat()
            iniciarAnimacionIosLoader(iosLoader)
            overlay.visibility = View.VISIBLE
            actualizarVisibilidadContenidoPrincipal(mostrar = false)
            overlay.animate()
                .alpha(1f)
                .setDuration(220L)
                .start()
            cardCarga?.animate()
                ?.alpha(1f)
                ?.scaleX(1f)
                ?.scaleY(1f)
                ?.translationY(0f)
                ?.setDuration(320L)
                ?.start()

            binding.materialButton3.isEnabled = false
            binding.materialButtonborrar.isEnabled = false
        } else {
            actualizarVisibilidadContenidoPrincipal(mostrar = true, animado = true)
            overlay.animate()
                .alpha(0f)
                .setDuration(180L)
                .withEndAction {
                    detenerAnimacionIosLoader(iosLoader)
                    overlay.visibility = View.GONE
                    overlay.alpha = 1f
                    overlay.translationZ = 0f
                    cardCarga?.alpha = 1f
                    cardCarga?.scaleX = 1f
                    cardCarga?.scaleY = 1f
                    cardCarga?.translationY = 0f
                }
                .start()
            binding.materialButtonborrar.isEnabled = true
            actualizarEstadoBotonGuardar()
        }
    }

    private fun iniciarAnimacionIosLoader(loader: ImageView?) {
        loader ?: return
        animadorIosLoader?.cancel()
        loader.rotation = 0f
        animadorIosLoader = ObjectAnimator.ofFloat(loader, View.ROTATION, 0f, 360f).apply {
            duration = 900L
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun detenerAnimacionIosLoader(loader: ImageView?) {
        animadorIosLoader?.cancel()
        animadorIosLoader = null
        loader?.rotation = 0f
    }

    private fun actualizarVisibilidadContenidoPrincipal(
        mostrar: Boolean,
        animado: Boolean = false
    ) {
        val vistas = buildList {
            add(binding.constraintcontainer)
            add(binding.cardResumenFijo)
            add(binding.layoutBotonesInferiores)
            tabletComposeContainer?.let(::add)
            mobileComposeContainer?.let(::add)
        }.distinct()

        vistas.forEach { vista ->
            vista.animate().cancel()
            if (mostrar) {
                vista.visibility = View.VISIBLE
                if (animado) {
                    vista.alpha = 0f
                    vista.translationY = dpToPx(8).toFloat()
                    vista.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(240L)
                        .start()
                } else {
                    vista.alpha = 1f
                    vista.translationY = 0f
                }
            } else {
                vista.alpha = 0f
                vista.translationY = 0f
                vista.visibility = View.INVISIBLE
            }
        }
    }

    private fun aplicarMargenSuperiorBottomSheet(
        dialog: BottomSheetDialog,
        topSpacingDp: Int = 72
    ) {
        val sheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            ?: return
        val topSpacingPx = dpToPx(topSpacingDp)
        val screenHeightPx = resources.displayMetrics.heightPixels
        val targetHeight = (screenHeightPx - topSpacingPx).coerceAtLeast(dpToPx(320))

        sheet.layoutParams = sheet.layoutParams.apply {
            height = targetHeight
        }
        sheet.requestLayout()

        val behavior = BottomSheetBehavior.from(sheet)
        behavior.skipCollapsed = true
        behavior.isFitToContents = false
        behavior.expandedOffset = topSpacingPx
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun mostrarDialogoProgresoStock(mensaje: String) {
        if (isFinishing || isDestroyed) return
        mostrarCarga(
            true,
            titulo = "Actualizando inventario",
            mensaje = mensaje
        )
    }

    private fun ocultarDialogoProgresoStock() {
        mostrarCarga(false)
        dialogoProgresoStock?.dismiss()
        dialogoProgresoStock = null
    }

    // ---------------------------------------------------------------------------------------------
    // IMAGEN
    // ---------------------------------------------------------------------------------------------

    private fun configurarSeleccionImagen() {
        binding.onclickimagen.setOnClickListener {
            mostrarOpcionesImagen()
        }
    }

    private fun mostrarOpcionesImagen() {
        val opciones = arrayOf("Tomar foto", "Elegir de galería")

        AlertDialog.Builder(this)
            .setTitle("Seleccionar imagen")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> requestCameraPermission.launch(Manifest.permission.CAMERA)
                    1 -> abrirGaleria()
                }
            }
            .show()
    }

    private fun abrirCamara() {
        val file = File.createTempFile(
            "producto_${System.currentTimeMillis()}",
            ".jpg",
            cacheDir
        )

        cameraImageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            file
        )

        cameraImageUri?.let { cameraLauncher.launch(it) }
    }

    private fun abrirGaleria() {
        galleryLauncher.launch("image/*")
    }

    private fun subirImagenProducto(indice: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val uriLocal = imagenUri
        if (uriLocal == null) {
            onSuccess(imagenUrlActual)
            return
        }

        lifecycleScope.launch {
            try {
                val secureUrl = withContext(Dispatchers.IO) {
                    val boundary = "Boundary-${UUID.randomUUID()}"
                    val endpoint =
                        URL("https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD_NAME/image/upload")
                    val connection = (endpoint.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        doInput = true
                        doOutput = true
                        useCaches = false
                        setRequestProperty(
                            "Content-Type",
                            "multipart/form-data; boundary=$boundary"
                        )
                    }

                    DataOutputStream(connection.outputStream).use { output ->
                        fun writeFormField(name: String, value: String) {
                            output.writeBytes("--$boundary\r\n")
                            output.writeBytes("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
                            output.writeBytes(value)
                            output.writeBytes("\r\n")
                        }

                        val mimeType = contentResolver.getType(uriLocal) ?: "image/jpeg"
                        val extension = when (mimeType.lowercase(Locale.getDefault())) {
                            "image/png" -> "png"
                            "image/webp" -> "webp"
                            else -> "jpg"
                        }
                        val publicIdSeguro = sanitizarPublicIdCloudinary(indice)

                        writeFormField("upload_preset", CLOUDINARY_UPLOAD_PRESET)
                        writeFormField("public_id", publicIdSeguro)

                        output.writeBytes("--$boundary\r\n")
                        output.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"$publicIdSeguro.$extension\"\r\n")
                        output.writeBytes("Content-Type: $mimeType\r\n\r\n")

                        contentResolver.openInputStream(uriLocal)?.use { input ->
                            val buffer = ByteArray(8 * 1024)
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                            }
                        } ?: throw Exception("No se pudo leer la imagen seleccionada")

                        output.writeBytes("\r\n")
                        output.writeBytes("--$boundary--\r\n")
                        output.flush()
                    }

                    val responseCode = connection.responseCode
                    val responseBody = try {
                        val stream =
                            if (responseCode in 200..299) connection.inputStream else connection.errorStream
                        stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                    } finally {
                        connection.disconnect()
                    }

                    if (responseCode !in 200..299) {
                        throw Exception(responseBody.ifBlank { "Error al subir imagen" })
                    }

                    val url = JSONObject(responseBody).optString("secure_url")
                    if (url.isBlank()) throw Exception("Error al subir imagen")
                    url
                }
                onSuccess(secureUrl)
            } catch (_: CancellationException) {
                // Corrutina cancelada con el ciclo de vida; no mostramos error.
            } catch (e: Exception) {
                onFailure(e.message ?: "Error al subir imagen")
            }
        }
    }

    private fun sanitizarPublicIdCloudinary(valor: String): String {
        return ProductUtils.sanitizarTexto(valor)
    }

    // ---------------------------------------------------------------------------------------------
    // OBTENER PRODUCTO
    // ---------------------------------------------------------------------------------------------

    private fun mostrarDialogoProductoNoDisponible(mensaje: String) {
        if (isFinishing || isDestroyed) return

        AlertDialog.Builder(this)
            .setTitle("Producto no disponible")
            .setMessage(mensaje)
            .setCancelable(false)
            .setPositiveButton("Entendido") { _, _ ->
                finish()
            }
            .show()
    }

    private fun obtenerIntent() {
        indiceOriginal = intent.getStringExtra("indice") ?: run {
            mostrarDialogoProductoNoDisponible("No se encontró el producto que intentas editar.")
            return
        }

        mostrarCarga(
            true,
            titulo = "Cargando producto",
            mensaje = "Preparando los datos para editar..."
        )

        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRODUCTOS)
            .child(indiceOriginal)
            .get()
            .addOnSuccessListener { snapshot ->
                val producto = snapshot.toMoldeProductos() ?: run {
                    mostrarDialogoProductoNoDisponible("Este producto ya no existe en el inventario.")
                    mostrarCarga(false)
                    return@addOnSuccessListener
                }
                cargarRelacionesProductoSeparadas(producto) { producto ->
                productoOriginalParaAuditoria = producto.copiaParaAuditoria()
                codigoOriginal = producto.codigo ?: ""
                basePorUnidad = producto.basePorUnidad
                loteConsumoSeleccionadoActual = producto.loteConsumoSeleccionado.trim()
                loteConsumoSeleccionManualActual = producto.loteConsumoSeleccionManual
                unidadBaseOriginalInmutable = producto.unidadbase.trim()
                tipoBaseInventarioOriginal = PresentacionRules.normalizarTipoBaseInventario(
                    producto.tipoBaseInventario,
                    unidadBaseOriginalInmutable
                )

                binding.editTextNombre.setText(producto.nombre)
                binding.editTextNombre.tag = producto.nombre
                binding.editTextCantidad.tag = producto.cantidadinicial
                binding.editTextCategoria.setText(producto.categoria, false)
                binding.editTextCompra?.setText(producto.preciodecompra)
                binding.editTextCantidad.setText(producto.cantidadinicial)
                binding.editTextCantidad.tag = producto.cantidadinicial
                binding.editTextStock.setText(producto.stockminimo)
                binding.editTextUnidadBase.setText(producto.unidadbase, false)
                ultimaUnidadBaseConfirmada = producto.unidadbase
                actualizarAyudaUnidadBaseUi(producto.unidadbase)
                actualizarHintsStockSegunUnidadBase(producto.unidadbase)
                aplicarBloqueoTipoBaseInventarioSiCorresponde()
                binding.editTextVencimiento.setText(ProductUtils.obtenerVencimientoGeneralVisible(producto))
                actualizarVisibilidadVencimientoGeneral(ProductUtils.cantidadLotesRegistrados(producto))
                binding.swichtreceta.isChecked = producto.requierereceta
                binding.switchestadodelproducto.isChecked = producto.estadodelproducto

                imagenUrlActual = producto.imagenUrl ?: ""
                imagenUri = null

                binding.imagevieW.visibility = View.VISIBLE
                ProductAvatarHelper.loadInto(
                    imageView = binding.imagevieW,
                    imageUrl = imagenUrlActual,
                    productName = producto.nombre,
                    category = producto.categoria
                )

                verificarCantidadYStock(producto.cantidadinicial, producto.stockminimo)
                verificarVencimiento()

                listaPresentaciones.clear()
                producto.presentaciones?.let { listaPresentaciones.addAll(it) }

                // Inferir modo presentación si existe una que coincida con la principal
                if (producto.tienePresentaciones && !producto.presentaciones.isNullOrEmpty()) {
                    val principal = producto.presentaciones?.find {
                        PresentacionHelper.sonNombresEquivalentes(it.nombre, producto.presentacionprincipal)
                    }
                    if (principal != null && principal.cantidad > 0) {
                        val cantTotal = producto.cantidadinicial.toIntOrNull() ?: 0
                        val stockMinTotal = producto.stockminimo.toIntOrNull() ?: 0

                        binding.editUnidadesPorCaja.setText(principal.cantidad.toString())
                        val stockExactoPorPresentacion = cantTotal % principal.cantidad == 0
                        val minimoExactoPorPresentacion = stockMinTotal % principal.cantidad == 0

                        if (stockExactoPorPresentacion && minimoExactoPorPresentacion) {
                            binding.radioPorPaquetes.isChecked = true
                            binding.layoutPorPaquetes.visibility = View.VISIBLE
                            binding.layoutPorUnidades.visibility = View.GONE
                            binding.editCantidadPresentacion.setText((cantTotal / principal.cantidad).toString())
                            binding.editTextStockPresentacion.setText((stockMinTotal / principal.cantidad).toString())
                        } else {
                            binding.radioPorUnidades.isChecked = true
                            binding.layoutPorUnidades.visibility = View.VISIBLE
                            binding.layoutPorPaquetes.visibility = View.GONE
                            binding.editCantidadPresentacion.setText("")
                            binding.editTextStockPresentacion.setText("")
                        }
                    }
                }

                if (producto.tienePresentaciones && listaPresentaciones.isNotEmpty()) {
                    binding.switchPresentaciones.isChecked = true
                    binding.layoutPresentaciones.visibility = View.VISIBLE
                    binding.tilPresentacionPrincipal.visibility = View.VISIBLE
                    binding.textPresentacionPrincipal.visibility = View.VISIBLE

                    binding.contenedorPresentaciones.removeAllViews()
                    listaPresentaciones.forEach { agregarPresentacionVisual(it) }

                    actualizarOpcionesPresentacionPrincipal()
                    val canonicalPrincipal = listaPresentaciones.firstOrNull {
                        PresentacionHelper.sonNombresEquivalentes(it.nombre, producto.presentacionprincipal)
                    }?.nombre ?: producto.presentacionprincipal.trim()
                    if (canonicalPrincipal.isNotBlank()) {
                        asignarPresentacionPrincipal(canonicalPrincipal, seleccionManual = true)
                    } else {
                        sincronizarPresentacionPrincipalSegunLista()
                    }
                } else {
                    binding.switchPresentaciones.isChecked = false
                    binding.layoutPresentaciones.visibility = View.GONE
                    binding.tilPresentacionPrincipal.visibility = View.GONE
                    binding.textPresentacionPrincipal.visibility = View.GONE
                    binding.contenedorPresentaciones.removeAllViews()
                    binding.editTextPresentacionPrincipal.tag = null
                }

                actualizarHelperStockSegunModo()
                marcarEstadoInicialFormulario()
                notificarRenderTabletCompose()
                mostrarCarga(false)

                // Auto-abrir diálogo de ingreso si viene desde el botón rápido +Stock
                if (intent.getBooleanExtra("auto_ingreso_stock", false)) {
                    binding.root.post { mostrarDialogoIngresoStock() }
                }
                }
            }
            .addOnFailureListener { e ->
                mostrarCarga(false)
                mostrarDialogoProductoNoDisponible(
                    "No se pudo cargar el producto. ${e.message ?: "Inténtalo nuevamente."}"
                )
            }
    }

    private fun cargarRelacionesProductoSeparadas(
        productoBase: MoldeProductos,
        onLoaded: (MoldeProductos) -> Unit
    ) {
        val productoId = productoBase.indice.ifBlank { indiceOriginal }
        if (productoId.isBlank()) {
            onLoaded(productoBase)
            return
        }

        val rootRef = FirebaseDatabase.getInstance().getReference(PATH_INVENTARIO)
        rootRef.child("ProductoPresentaciones")
            .child(productoId)
            .get()
            .addOnSuccessListener { presentacionesSnapshot ->
                val presentacionesSeparadas = presentacionesSnapshot.children.mapNotNull { item ->
                    item.getValue(PresentacionProducto::class.java)
                }
                val presentacionesFinales = if (presentacionesSeparadas.isNotEmpty()) {
                    presentacionesSeparadas.toMutableList()
                } else {
                    productoBase.presentaciones
                }

                rootRef.child("ProductoLotes")
                    .child(productoId)
                    .get()
                    .addOnSuccessListener { lotesSnapshot ->
                        val lotesSeparados = lotesDesdeSnapshot(lotesSnapshot)
                        val lotesFinales = if (lotesSeparados.isNotEmpty()) {
                            lotesSeparados
                        } else {
                            productoBase.lotes
                        }

                        onLoaded(
                            productoBase.copy(
                                presentaciones = presentacionesFinales,
                                lotes = lotesFinales
                            )
                        )
                    }
                    .addOnFailureListener {
                        onLoaded(productoBase.copy(presentaciones = presentacionesFinales))
                    }
            }
            .addOnFailureListener {
                onLoaded(productoBase)
            }
    }

    // ---------------------------------------------------------------------------------------------
    // FIREBASE SUGERENCIAS
    // ---------------------------------------------------------------------------------------------

    private fun obtenerCategoriasDeProductos() {
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_CATEGORIAS)
            .get()
            .addOnSuccessListener { snapshot ->
                listaCategoria.clear()

                snapshot.children.forEach { data ->
                    val categoria = data.getValue(CategoriaProductos::class.java)
                    if (categoria != null) listaCategoria.add(categoria)
                }

                actualizarAutoCompleteCategorias()

                obtenerNombresDeProductos()
                cargarSugerenciasUnidadBase()
            }
    }

    private fun obtenerNombresDeProductos() {
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_NOMBRES)
            .get()
            .addOnSuccessListener { snapshot ->
                listaNombreAutoComplete.clear()

                snapshot.children.forEach { data ->
                    val nombreProducto = data.getValue(NombreProductos::class.java)
                    if (nombreProducto != null) listaNombreAutoComplete.add(nombreProducto)
                }

                configurarAutoCompleteEditable(
                    binding.tilNombre,
                    binding.editTextNombre,
                    listaNombreAutoComplete.mapNotNull { it.nombre }
                )
            }
    }

    private fun actualizarAutoCompleteCategorias() {
        configurarCampoCategoriaInteligente()
    }

    private fun cargarSugerenciasUnidadBase() {
        // La unidad base ahora es fija (selector), no texto libre.
        unidadBaseOpcionesTodas = resources.getStringArray(R.array.unidad_base_opciones).toList()

        val actual = binding.editTextUnidadBase.text?.toString().orEmpty().trim()
        modoControlStock = modoDesdeUnidadBase(actual)
        unidadBaseOpciones = opcionesUnidadBaseSegunModo(modoControlStock)

        configurarAutoCompleteFijo(binding.tilUnidadBase, binding.editTextUnidadBase, unidadBaseOpciones)
        actualizarUiModoControlStock(modoControlStock, ajustarUnidadBase = true)

        // Estado actual (sin diálogo)
        ultimaUnidadBaseConfirmada = binding.editTextUnidadBase.text?.toString().orEmpty()
        actualizarAyudaUnidadBaseUi(ultimaUnidadBaseConfirmada)
        actualizarHintsStockSegunUnidadBase(ultimaUnidadBaseConfirmada)

        // Si el usuario cambia a g/mL, confirmamos para evitar confusión
        binding.editTextUnidadBase.setOnItemClickListener { _, _, position, _ ->
            val seleccion = unidadBaseOpciones.getOrNull(position)
                ?: binding.editTextUnidadBase.text?.toString().orEmpty()

            if (!cambiandoUnidadBaseProgramaticamente) {
                manejarSeleccionUnidadBase(seleccion, mostrarDialogo = true)
            }

            binding.editTextUnidadBase.dismissDropDown()
            binding.editTextUnidadBase.clearFocus()
            binding.main.requestFocus()
            ocultarTeclado()
        }

        binding.toggleControlStock.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked || cambiandoModoControlStock) return@addOnButtonCheckedListener
            val nuevoModo = when (checkedId) {
                binding.btnControlStockPeso.id -> ModoControlStock.PESO
                binding.btnControlStockVolumen.id -> ModoControlStock.VOLUMEN
                else -> ModoControlStock.UNIDADES
            }
            aplicarModoControlStock(nuevoModo)
        }

        aplicarBloqueoTipoBaseInventarioSiCorresponde()
    }

    private fun modoDesdeUnidadBase(unidadBase: String): ModoControlStock {
        return when (PresentacionRules.modoDesdeUnidadBase(unidadBase)) {
            PresentacionRules.ModoControlStock.PESO -> ModoControlStock.PESO
            PresentacionRules.ModoControlStock.VOLUMEN -> ModoControlStock.VOLUMEN
            PresentacionRules.ModoControlStock.UNIDADES -> ModoControlStock.UNIDADES
        }
    }

    private fun opcionesUnidadBaseSegunModo(modo: ModoControlStock): List<String> {
        val modoCompartido = when (modo) {
            ModoControlStock.PESO -> PresentacionRules.ModoControlStock.PESO
            ModoControlStock.VOLUMEN -> PresentacionRules.ModoControlStock.VOLUMEN
            ModoControlStock.UNIDADES -> PresentacionRules.ModoControlStock.UNIDADES
        }
        return PresentacionRules.opcionesUnidadBaseSegunModo(modoCompartido, unidadBaseOpcionesTodas)
    }

    private fun actualizarUiModoControlStock(modo: ModoControlStock, ajustarUnidadBase: Boolean) {
        cambiandoModoControlStock = true

        val buttonId = when (modo) {
            ModoControlStock.PESO -> binding.btnControlStockPeso.id
            ModoControlStock.VOLUMEN -> binding.btnControlStockVolumen.id
            ModoControlStock.UNIDADES -> binding.btnControlStockUnidades.id
        }
        if (binding.toggleControlStock.checkedButtonId != buttonId) {
            binding.toggleControlStock.check(buttonId)
        }

        binding.tvAyudaControlStock.text = when (modo) {
            ModoControlStock.PESO -> getString(R.string.control_stock_ayuda_peso)
            ModoControlStock.VOLUMEN -> getString(R.string.control_stock_ayuda_volumen)
            ModoControlStock.UNIDADES -> getString(R.string.control_stock_ayuda_unidades)
        }

        if (ajustarUnidadBase) {
            val nuevoValor = when (modo) {
                ModoControlStock.PESO -> "g"
                ModoControlStock.VOLUMEN -> "mL"
                ModoControlStock.UNIDADES -> {
                    val opcionesUnidades = opcionesUnidadBaseSegunModo(ModoControlStock.UNIDADES)
                    val valorActual = binding.editTextUnidadBase.text?.toString().orEmpty().trim()
                    val valorConfirmado = ultimaUnidadBaseConfirmada.trim()

                    when {
                        valorActual.isNotBlank() &&
                            !esUnidadPesoVolumen(valorActual) &&
                            opcionesUnidades.any { it.equals(valorActual, ignoreCase = true) } -> valorActual

                        valorConfirmado.isNotBlank() &&
                            !esUnidadPesoVolumen(valorConfirmado) &&
                            opcionesUnidades.any { it.equals(valorConfirmado, ignoreCase = true) } -> valorConfirmado

                        else -> opcionesUnidades.firstOrNull() ?: "Unidad"
                    }
                }
            }

            cambiandoUnidadBaseProgramaticamente = true
            binding.editTextUnidadBase.setText(nuevoValor, false)
            cambiandoUnidadBaseProgramaticamente = false
        }

        cambiandoModoControlStock = false
    }

    private fun actualizarOpcionesUnidadBase(lista: List<String>) {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            lista
        )
        binding.editTextUnidadBase.setAdapter(adapter)
    }

    private fun aplicarModoControlStock(nuevoModo: ModoControlStock) {
        modoControlStock = nuevoModo
        unidadBaseOpciones = opcionesUnidadBaseSegunModo(nuevoModo)
        actualizarOpcionesUnidadBase(unidadBaseOpciones)
        actualizarUiModoControlStock(nuevoModo, ajustarUnidadBase = true)

        manejarSeleccionUnidadBase(
            binding.editTextUnidadBase.text?.toString().orEmpty(),
            mostrarDialogo = false
        )
    }

    private fun manejarSeleccionUnidadBase(seleccion: String, mostrarDialogo: Boolean) {
        val nueva = seleccion.trim()
        if (nueva.isBlank()) return

        if (mostrarDialogo && esUnidadPesoVolumen(nueva) && !esUnidadPesoVolumen(ultimaUnidadBaseConfirmada)) {
            mostrarConfirmacionUnidadPesoVolumen(nueva)
            return
        }

        ultimaUnidadBaseConfirmada = nueva
        actualizarAyudaUnidadBaseUi(nueva)
        actualizarHintsStockSegunUnidadBase(nueva, permitirEliminarPresentacionAuto = true)

        // Si el usuario cambia la unidad base, sincronizamos el selector "Como se controla el stock".
        val modoInferido = modoDesdeUnidadBase(nueva)
        if (modoInferido != modoControlStock) {
            aplicarModoControlStock(modoInferido)
        }

        if (binding.radioPorPaquetes.isChecked) {
            calcularStockTotal()
        }
    }

    private fun esUnidadPesoVolumen(unidadBase: String): Boolean {
        return PresentacionRules.esUnidadPesoOVolumen(unidadBase)
    }

    private fun tipoBaseInventarioDesdeUnidadBase(unidadBase: String): String {
        return PresentacionRules.tipoBaseInventarioDesdeUnidadBase(unidadBase)
    }

    private fun nombreVisibleTipoBaseInventario(tipoBaseInventario: String): String {
        return PresentacionRules.nombreVisibleTipoBaseInventario(tipoBaseInventario)
    }

    private fun aplicarBloqueoTipoBaseInventarioSiCorresponde() {
        val unidadBaseBloqueada = unidadBaseOriginalInmutable.trim()
        val tipoBaseBloqueado = tipoBaseInventarioOriginal.trim()
        if (unidadBaseBloqueada.isBlank() || tipoBaseBloqueado.isBlank()) return

        binding.toggleControlStock.isEnabled = false
        listOf(
            binding.btnControlStockUnidades,
            binding.btnControlStockPeso,
            binding.btnControlStockVolumen
        ).forEach { boton ->
            boton.isEnabled = false
            boton.isClickable = false
            boton.isFocusable = false
        }

        binding.editTextUnidadBase.dismissDropDown()
        binding.editTextUnidadBase.clearFocus()
        binding.editTextUnidadBase.isEnabled = false
        binding.editTextUnidadBase.isFocusable = false
        binding.editTextUnidadBase.isFocusableInTouchMode = false
        binding.editTextUnidadBase.isCursorVisible = false
        binding.tilUnidadBase.endIconMode = TextInputLayout.END_ICON_NONE
        binding.tilUnidadBase.helperText = getString(
            R.string.tipo_base_inmutable_helper_unidad,
            unidadBaseBloqueada
        )
        binding.tvAvisoTipoBaseInmutable.visibility = View.GONE
    }

    private fun restaurarTipoBaseInventarioOriginalEnUi() {
        val unidadBaseBloqueada = unidadBaseOriginalInmutable.trim()
        if (unidadBaseBloqueada.isBlank()) return

        cambiandoUnidadBaseProgramaticamente = true
        binding.editTextUnidadBase.setText(unidadBaseBloqueada, false)
        cambiandoUnidadBaseProgramaticamente = false
        ultimaUnidadBaseConfirmada = unidadBaseBloqueada

        val modoBloqueado = modoDesdeUnidadBase(unidadBaseBloqueada)
        modoControlStock = modoBloqueado
        actualizarUiModoControlStock(modoBloqueado, ajustarUnidadBase = false)
        actualizarAyudaUnidadBaseUi(unidadBaseBloqueada)
        actualizarHintsStockSegunUnidadBase(unidadBaseBloqueada)
        aplicarBloqueoTipoBaseInventarioSiCorresponde()
    }

    private fun validarTipoBaseInventarioInmutable(): Boolean {
        val unidadActual = binding.editTextUnidadBase.text?.toString()?.trim().orEmpty()
        val unidadBloqueada = unidadBaseOriginalInmutable.trim()
        val tipoOriginal = PresentacionRules.normalizarTipoBaseInventario(
            tipoBaseInventarioOriginal,
            unidadBloqueada
        )
        val tipoActual = PresentacionRules.normalizarTipoBaseInventario(
            tipoBaseInventarioDesdeUnidadBase(unidadActual),
            unidadActual
        )

        if (unidadActual.equals(unidadBloqueada, ignoreCase = true) && tipoActual == tipoOriginal) {
            return true
        }

        restaurarTipoBaseInventarioOriginalEnUi()
        Toast.makeText(this, getString(R.string.tipo_base_inmutable_error), Toast.LENGTH_LONG).show()
        return false
    }

    private fun esTipoBaseInmutableConsistente(producto: MoldeProductos): Boolean {
        val unidadBloqueada = unidadBaseOriginalInmutable.trim()
        val tipoOriginal = PresentacionRules.normalizarTipoBaseInventario(
            tipoBaseInventarioOriginal,
            unidadBloqueada
        )
        val tipoProducto = PresentacionRules.normalizarTipoBaseInventario(
            producto.tipoBaseInventario,
            producto.unidadbase
        )

        return producto.unidadbase.trim().equals(unidadBloqueada, ignoreCase = true) &&
            tipoProducto == tipoOriginal
    }

    private fun mostrarConfirmacionUnidadPesoVolumen(unidadBase: String) {
        val anterior = ultimaUnidadBaseConfirmada.ifBlank { "Unidad" }
        val unidad = if (unidadBase.equals("g", ignoreCase = true)) "g" else "mL"
        val titulo = if (unidad == "g") "Stock por gramos" else "Stock por mililitros"
        val ejemplo1 = if (unidad == "g") "1 kg = 1000 g" else "1 L = 1000 mL"
        val ejemplo2 = if (unidad == "g") "0.5 kg = 500 g" else "0.5 L = 500 mL"

        MaterialAlertDialogBuilder(this)
            .setTitle(titulo)
            .setMessage(
                "Con '$unidad' el stock se controla por $unidad (1 = 1 $unidad).\n\n" +
                    "Ejemplos:\n$ejemplo1\n$ejemplo2\n\n" +
                    "Si vendes envases completos (botella, bolsa, frasco), te recomendamos usar una unidad por envase (ej. Botella, Bolsa, Unidad)."
            )
            .setPositiveButton("Continuar con $unidad") { _, _ ->
                cambiarUnidadBaseProgramaticamente(unidadBase)
            }
            .setNegativeButton("Usar por envase") { _, _ ->
                val opcionesUnidades = opcionesUnidadBaseSegunModo(ModoControlStock.UNIDADES)
                val anteriorSeguro = anterior
                    .takeIf { !esUnidadPesoVolumen(it) }
                    ?.takeIf { a -> opcionesUnidades.any { it.equals(a, ignoreCase = true) } }
                    ?: (opcionesUnidades.firstOrNull() ?: "Unidad")
                cambiarUnidadBaseProgramaticamente(anteriorSeguro)
            }
            .setOnCancelListener {
                // Si el usuario cancela, regresamos a la unidad anterior confirmada para evitar confusión.
                cambiarUnidadBaseProgramaticamente(anterior)
            }
            .setCancelable(true)
            .show()
    }

    private fun cambiarUnidadBaseProgramaticamente(valor: String) {
        cambiandoUnidadBaseProgramaticamente = true
        binding.editTextUnidadBase.setText(valor, false)
        cambiandoUnidadBaseProgramaticamente = false

        manejarSeleccionUnidadBase(valor, mostrarDialogo = false)
    }

    private fun actualizarAyudaUnidadBaseUi(unidadBaseRaw: String) {
        val unidadBase = unidadBaseRaw.trim()
        if (unidadBase.isBlank()) return

        when {
            unidadBase.equals("g", ignoreCase = true) -> {
                binding.tilUnidadBase.helperText = "Stock por gramos (1 = 1 g)."
                binding.tvExplicacionUnidadBase.visibility = View.GONE
            }

            unidadBase.equals("mL", ignoreCase = true) -> {
                binding.tilUnidadBase.helperText = "Stock por mililitros (1 = 1 mL)."
                binding.tvExplicacionUnidadBase.visibility = View.GONE
            }

            else -> {
                val etiqueta = unidadBase.lowercase(Locale.getDefault())
                binding.tilUnidadBase.helperText = "Unidad mínima de stock: 1 = 1 $etiqueta."
                binding.tvExplicacionUnidadBase.visibility = View.GONE
            }
        }
    }

    private fun actualizarHintsStockSegunUnidadBase(unidadBaseRaw: String, permitirEliminarPresentacionAuto: Boolean = false) {
        val unidadBase = unidadBaseRaw.trim()
        val tilStockActual = binding.editTextCantidad.parent?.parent as? TextInputLayout
        val tilStockMinimo = binding.editTextStock.parent?.parent as? TextInputLayout
        val tilCantPaquetes = binding.editCantidadPresentacion.parent?.parent as? TextInputLayout
        val tilUnidadesPorPaquete = binding.editUnidadesPorCaja.parent?.parent as? TextInputLayout
        val tilStockMinPaquetes = binding.editTextStockPresentacion.parent?.parent as? TextInputLayout
        val unidadesPorPresentacion = binding.editUnidadesPorCaja.text?.toString()?.trim()?.toIntOrNull() ?: 0
        val puedeAvisarPorPresentacion = unidadesPorPresentacion > 0

        binding.radioPorUnidades.text = getString(R.string.avisar_por_unidad_base)
        binding.radioPorPaquetes.text = getString(R.string.avisar_por_presentacion_principal)
        if (!puedeAvisarPorPresentacion && binding.radioPorPaquetes.isChecked) {
            binding.radioPorUnidades.isChecked = true
        }
        if (binding.radioGroupTipoIngreso.checkedRadioButtonId == -1) {
            binding.radioPorUnidades.isChecked = true
        }

        when {
            unidadBase.equals("g", ignoreCase = true) -> {
                tilStockActual?.helperText = "Este stock está en g. Ej: 1 kg = 1000 g, medio kg = 500 g."
                tilStockMinimo?.helperText = "La alerta se evalúa en gramos (g)."
                tilStockActual?.suffixText = "g"
                tilStockMinimo?.suffixText = "g"
                if (binding.radioGroupTipoIngreso.checkedRadioButtonId == -1) binding.radioPorUnidades.isChecked = true
                binding.radioPorPaquetes.text = getString(R.string.avisar_por_presentacion_principal)
                tilCantPaquetes?.hint = "Cantidad de paquetes"
                tilUnidadesPorPaquete?.hint = "g por paquete"
                tilStockMinPaquetes?.hint = getString(R.string.stock_minimo_presentacion_principal_hint)
            }

            unidadBase.equals("mL", ignoreCase = true) -> {
                tilStockActual?.helperText = "Este stock está en mL. Ej: 1 L = 1000 mL, medio L = 500 mL."
                tilStockMinimo?.helperText = "La alerta se evalúa en mL."
                tilStockActual?.suffixText = "mL"
                tilStockMinimo?.suffixText = "mL"
                if (binding.radioGroupTipoIngreso.checkedRadioButtonId == -1) binding.radioPorUnidades.isChecked = true
                binding.radioPorPaquetes.text = getString(R.string.avisar_por_presentacion_principal)
                tilCantPaquetes?.hint = "Cantidad de frascos"
                tilUnidadesPorPaquete?.hint = "mL por frasco"
                tilStockMinPaquetes?.hint = getString(R.string.stock_minimo_presentacion_principal_hint)
            }

            else -> {
                val sufijo = unidadBase.ifBlank { "unid." }
                tilStockActual?.helperText = null
                tilStockMinimo?.helperText = "La alerta se evalúa en ${sufijo.lowercase()}."
                tilStockActual?.suffixText = sufijo
                tilStockMinimo?.suffixText = sufijo
                if (binding.radioGroupTipoIngreso.checkedRadioButtonId == -1) {
                    binding.radioPorUnidades.isChecked = true
                }
                binding.radioPorUnidades.text = getString(R.string.avisar_por_unidad_base)
                binding.radioPorPaquetes.text = getString(R.string.avisar_por_presentacion_principal)
                tilCantPaquetes?.hint = "Cant de cajas"
                tilUnidadesPorPaquete?.hint = "${sufijo}s por caja"
                tilStockMinPaquetes?.hint = getString(R.string.stock_minimo_presentacion_principal_hint)
                // Eliminar presentación auto-creada (Frasco/Paquete/Caja) si existía
                // Solo cuando el usuario cambia explícitamente la unidad base (ej: de mL a Unidad)
                if (permitirEliminarPresentacionAuto) {
                    val autoNombres = listOf("Frasco", "Paquete", "Caja")
                    val autoExistente = listaPresentaciones.find { it.nombre in autoNombres }
                    if (autoExistente != null) {
                        listaPresentaciones.remove(autoExistente)
                        binding.contenedorPresentaciones.removeAllViews()
                        listaPresentaciones.forEach { agregarPresentacionVisual(it) }
                        actualizarOpcionesPresentacionPrincipal()
                        sincronizarPresentacionPrincipalSegunLista()
                    }
                }
            }
        }

        actualizarHintStockMinimoSegunAviso()
    }

    private fun obtenerSugerenciasGlobales(soloUnidadBase: Boolean = false, soloPresentacion: Boolean = false, callback: (List<String>) -> Unit) {
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRESENTACIONES)
            .get()
            .addOnSuccessListener { snapshot ->

                val lista = mutableListOf<String>()

                snapshot.children.forEach { child ->
                    val sugerencia =
                        child.getValue(SugerenciaPresentacion::class.java) ?: return@forEach

                    val agregar = when {
                        soloUnidadBase -> sugerencia.esUnidadBase
                        soloPresentacion -> sugerencia.esPresentacion
                        else -> true
                    }

                    if (agregar && sugerencia.nombre.isNotBlank()) {
                        lista.add(sugerencia.nombre)
                    }
                }

                callback(lista.distinct().sorted())
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    private fun guardarSugerenciaGlobal(nombre: String, esUnidadBase: Boolean, esPresentacion: Boolean) {
        if (nombre.isBlank()) return

        val ref = FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRESENTACIONES)
            .child(nombre.lowercase().trim())

        ref.get().addOnSuccessListener { snapshot ->
            val actual = snapshot.getValue(SugerenciaPresentacion::class.java)

            val nueva = SugerenciaPresentacion(
                nombre = nombre.trim(),
                esUnidadBase = actual?.esUnidadBase == true || esUnidadBase,
                esPresentacion = actual?.esPresentacion == true || esPresentacion
            )

            ref.setValue(nueva)
        }
    }

    private fun guardarSugerenciasGlobales() {
        // Nota: ya no guardamos sugerencias de unidad base porque es fija.
        listaPresentaciones.forEach { presentacion ->
            guardarSugerenciaGlobal(
                nombre = presentacion.nombre,
                esUnidadBase = false,
                esPresentacion = true
            )
        }
    }

    // ---------------------------------------------------------------------------------------------
    // FECHA
    // ---------------------------------------------------------------------------------------------

    private fun configurarSelectorFecha() {
        binding.editTextVencimiento.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                var input = s.toString().replace("/", "")

                // Manejo de "pereza" del usuario: 2-9 -> 02-09
                if (input.length == 1 && input.isNotEmpty() && input[0] > '1') {
                    input = "0$input"
                }

                if (input.length > 4) input = input.substring(0, 4)

                val formatted = StringBuilder()
                for (i in input.indices) {
                    formatted.append(input[i])
                    if (i == 1 && input.length > 2) {
                        formatted.append("/")
                    }
                }

                binding.editTextVencimiento.setText(formatted.toString())
                binding.editTextVencimiento.setSelection(binding.editTextVencimiento.text?.length ?: 0)

                isFormatting = false
                verificarVencimiento()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })
    }

    private fun verificarVencimiento() {
        if (ocultarVencimientoGeneralPorLotes) {
            binding.textEstadoVencimiento.visibility = View.GONE
            binding.editTextVencimiento.error = null
            return
        }

        val texto = binding.editTextVencimiento.text.toString().trim()

        if (texto.length != 5 || !texto.contains("/")) {
            binding.textEstadoVencimiento.visibility = View.GONE
            binding.editTextVencimiento.error = null
            return
        }

        try {
            val partes = texto.split("/")
            if (partes.size != 2) {
                binding.textEstadoVencimiento.visibility = View.GONE
                return
            }

            val mes = partes[0].toIntOrNull()
            val anio = partes[1].toIntOrNull()?.plus(2000)

            if (mes == null || anio == null) {
                binding.textEstadoVencimiento.visibility = View.GONE
                return
            }

              if (mes !in 1..12) {
                  binding.textEstadoVencimiento.visibility = View.VISIBLE
                 binding.textEstadoVencimiento.text = "Mes invalido"
                  binding.textEstadoVencimiento.setTextColor(Color.RED)
                  return
              }

            val calendario = Calendar.getInstance()
            val mesActual = calendario.get(Calendar.MONTH) + 1
            val anioActual = calendario.get(Calendar.YEAR)

            val totalActual = anioActual * 12 + mesActual
            val totalVencimiento = anio * 12 + mes
            val diferencia = totalVencimiento - totalActual

            binding.textEstadoVencimiento.visibility = View.VISIBLE

              when {
                  diferencia < 0 -> {
                     binding.textEstadoVencimiento.text = "Vencido"
                     binding.textEstadoVencimiento.setTextColor(Color.RED)
                  }
                  diferencia <= 3 -> {
                     binding.textEstadoVencimiento.text = "Por vencer"
                     binding.textEstadoVencimiento.setTextColor("#FFA000".toColorInt())
                  }
                  else -> {
                     binding.textEstadoVencimiento.text = "Disponible"
                     binding.textEstadoVencimiento.setTextColor("#4CAF50".toColorInt())
                  }
              }
        } catch (e: Exception) {
            binding.textEstadoVencimiento.visibility = View.GONE
        }
    }

    private fun verificarCantidadYStock(cantidadInicial: String, stockMinimo: String) {
        val cantidad = cantidadInicial.toIntOrNull() ?: return
        val stock = stockMinimo.toIntOrNull() ?: return

          when {
              cantidad <= 0 -> {
                 binding.textCantidadyStock.text = "Sin stock"
                 binding.textCantidadyStock.setTextColor(Color.parseColor("#F40723"))
                 binding.textCantidadyStock.visibility = View.VISIBLE
              }

              cantidad <= stock -> {
                 binding.textCantidadyStock.text = "Bajo stock"
                 binding.textCantidadyStock.setTextColor(Color.parseColor("#DE8D13"))
                 binding.textCantidadyStock.visibility = View.VISIBLE
              }

            else -> {
                binding.textCantidadyStock.visibility = View.GONE
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // PRESENTACIONES
    // ---------------------------------------------------------------------------------------------

    private fun limpiarPresentacionPrincipal(resetSeleccionManual: Boolean = true) {
        binding.editTextPresentacionPrincipal.setText("", false)
        binding.editTextPresentacionPrincipal.tag = null
        binding.editTextPresentacionPrincipal.error = null
        binding.tilPresentacionPrincipal.error = null
        if (resetSeleccionManual) {
            presentacionPrincipalElegidaManualmente = false
        }
    }

    private fun asignarPresentacionPrincipal(canonical: String, seleccionManual: Boolean) {
        binding.editTextPresentacionPrincipal.setText(
            PresentacionesTiendaConfigManager.nombreVisible(canonical),
            false
        )
        binding.editTextPresentacionPrincipal.tag = canonical
        binding.editTextPresentacionPrincipal.error = null
        binding.tilPresentacionPrincipal.error = null
        presentacionPrincipalElegidaManualmente = seleccionManual
    }

    private fun buscarPresentacionEnLista(
        nombreCanonical: String,
        presentaciones: List<PresentacionProducto> = listaPresentaciones
    ): PresentacionProducto? {
        val nombreNormalizado = nombreCanonical.trim()
        if (nombreNormalizado.isBlank()) return null
        return presentaciones.firstOrNull {
            PresentacionHelper.sonNombresEquivalentes(it.nombre, nombreNormalizado)
        }
    }

    private fun obtenerPresentacionPrincipalActualDesdeLista(
        presentaciones: List<PresentacionProducto> = listaPresentaciones
    ): PresentacionProducto? {
        return buscarPresentacionEnLista(
            obtenerCanonicalPresentacionPrincipalActual(),
            presentaciones
        )
    }

    private fun obtenerPresentacionMayorMontoVenta(
        presentaciones: List<PresentacionProducto> = listaPresentaciones
    ): PresentacionProducto? {
        return presentaciones.maxWithOrNull(
            compareByDescending<PresentacionProducto> { it.precioventa }
                .thenByDescending { it.cantidad }
                .thenBy { PresentacionHelper.normalizarClave(it.nombre) }
        )
    }

    private fun obtenerPresentacionVentaPorDefectoSinMultiples(
        presentaciones: List<PresentacionProducto> = listaPresentaciones
    ): PresentacionProducto? {
        return obtenerPresentacionPrincipalActualDesdeLista(presentaciones)
            ?: obtenerPresentacionMayorMontoVenta(presentaciones)
            ?: presentaciones.firstOrNull()
    }

    private fun obtenerNombrePresentacionTopeProducto(unidadBase: String): String {
        return when {
            unidadBase.equals("mL", ignoreCase = true) -> "frasco"
            unidadBase.equals("g", ignoreCase = true) -> "paquete"
            else -> "caja"
        }
    }

    private fun asegurarPresentacionValidaAlDesactivarMultiples(
        mostrarAviso: Boolean
    ): PresentacionProducto? {
        val presentacionConservada = obtenerPresentacionVentaPorDefectoSinMultiples()
        if (presentacionConservada != null) {
            asignarPresentacionPrincipal(presentacionConservada.nombre, seleccionManual = true)
            if (mostrarAviso) {
                val nombreVisible =
                    PresentacionesTiendaConfigManager.nombreVisible(presentacionConservada.nombre)
                Toast.makeText(
                    this,
                    "Se conservará $nombreVisible como presentación de venta.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            limpiarPresentacionPrincipal()
        }
        return presentacionConservada
    }

    private fun obtenerPresentacionesPersistidasSegunModo(): List<PresentacionProducto> {
        return if (binding.switchPresentaciones.isChecked) {
            listaPresentaciones.map { it.copy() }
        } else {
            listOfNotNull(
                obtenerPresentacionVentaPorDefectoSinMultiples()?.copy()
            )
        }
    }

    private fun sincronizarPresentacionPrincipalSegunLista() {
        if (listaPresentaciones.isEmpty()) {
            limpiarPresentacionPrincipal()
            binding.tilPresentacionPrincipal.helperText = null
            return
        }

        if (!binding.switchPresentaciones.isChecked) {
            val presentacionConservada = obtenerPresentacionVentaPorDefectoSinMultiples()
            if (presentacionConservada != null) {
                asignarPresentacionPrincipal(presentacionConservada.nombre, seleccionManual = true)
            } else {
                limpiarPresentacionPrincipal()
            }
            binding.tilPresentacionPrincipal.helperText = null
            return
        }

        val actualCanonical = obtenerCanonicalPresentacionPrincipalActual().trim()
        val actualExiste = listaPresentaciones.any {
            PresentacionHelper.sonNombresEquivalentes(it.nombre, actualCanonical)
        }

        when {
            listaPresentaciones.size == 1 -> {
                val unica = listaPresentaciones.first().nombre
                if (!actualExiste || actualCanonical.isBlank() || !presentacionPrincipalElegidaManualmente) {
                    asignarPresentacionPrincipal(unica, seleccionManual = false)
                }
                binding.tilPresentacionPrincipal.helperText =
                    "Solo hay una presentación; se usará como principal."
            }

            !actualExiste || !presentacionPrincipalElegidaManualmente -> {
                limpiarPresentacionPrincipal()
                binding.tilPresentacionPrincipal.helperText =
                    "Elige cuál será la presentación principal al vender."
            }

            else -> {
                binding.tilPresentacionPrincipal.helperText =
                    "Esta presentación se mostrará primero al cobrar."
            }
        }
    }

    private fun configurarPresentaciones() {
        binding.switchPresentaciones.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.layoutPresentaciones.visibility = View.VISIBLE
                binding.tilPresentacionPrincipal.visibility = View.VISIBLE
                binding.textPresentacionPrincipal.visibility = View.VISIBLE
                binding.contenedorPresentaciones.removeAllViews()
                listaPresentaciones.forEach { agregarPresentacionVisual(it) }
                actualizarOpcionesPresentacionPrincipal()
            } else {
                asegurarPresentacionValidaAlDesactivarMultiples(
                    mostrarAviso = !cargandoDatosIniciales && listaPresentaciones.isNotEmpty()
                )
                binding.layoutPresentaciones.visibility = View.GONE
                binding.tilPresentacionPrincipal.visibility = View.GONE
                binding.textPresentacionPrincipal.visibility = View.GONE
            }

            actualizarEstadoBotonGuardar()
        }

        binding.btnAgregarPresentacion.setOnClickListener {
            if (binding.editTextUnidadBase.text.isNullOrBlank()) {
                binding.editTextUnidadBase.error = "Primero ingrese la unidad de stock"
                posicionarScroll(binding.editTextUnidadBase)
                return@setOnClickListener
            }
            mostrarBottomSheetPresentacion()
        }

        binding.tilPresentacionPrincipal.visibility = View.GONE
        binding.textPresentacionPrincipal.visibility = View.GONE
        binding.tilPresentacionPrincipal.helperText = null
    }

    @Suppress("DEPRECATION")
    private fun mostrarBottomSheetPresentacion() {
        mostrarBottomSheetPresentacionCompacta()
        return
        val dialog = BottomSheetDialog(this)
        val sheetBinding = BottomsheetAgregarPresentacionBinding.inflate(layoutInflater)

        dialog.setContentView(sheetBinding.root)
        configurarBackdropBottomSheet(dialog)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val unidadBase = binding.editTextUnidadBase.text.toString().trim().ifEmpty { getString(R.string.por_unidades).lowercase() }

        fun limpiarNombrePresentacion(input: String): String {
            return input.trim().replace("\\s+".toRegex(), " ")
        }

        fun claveNombrePresentacion(input: String): String {
            return PresentacionHelper.normalizarClave(limpiarNombrePresentacion(input))
        }

        fun hintEquivalenciaSegunUnidadBase(): String {
            return when {
                unidadBase.equals("g", ignoreCase = true) -> "Equivale a cuántos g"
                unidadBase.equals("mL", ignoreCase = true) -> "Equivale a cuántos mL"
                else -> getString(R.string.equivale_unidades)
            }
        }

        fun ayudaEquivalenciaSegunSeleccion(nombreCanonical: String?, nombreVisible: String): String {
            return when {
                unidadBase.equals("g", ignoreCase = true) -> {
                    when {
                        nombreCanonical.equals("kg", ignoreCase = true) -> "Ej: 1 kg = 1000 g. Medio kg = 500 g."
                        nombreCanonical.equals("g", ignoreCase = true) -> "Ej: 1 g = 1 g."
                        else -> "Ingresa cuántos g contiene 1 $nombreVisible. Ej: 1 kg = 1000 g."
                    }
                }

                unidadBase.equals("mL", ignoreCase = true) -> {
                    when {
                        nombreCanonical.equals("l", ignoreCase = true) -> "Ej: 1 L = 1000 mL. Medio L = 500 mL."
                        nombreCanonical.equals("ml", ignoreCase = true) -> "Ej: 1 mL = 1 mL."
                        else -> "Ingresa cuántos mL contiene 1 $nombreVisible. Ej: 1 L = 1000 mL."
                    }
                }

                else -> getString(R.string.ayuda_cantidad_presentacion, nombreVisible, unidadBase)
            }
        }

        fun actualizarAyudaCantidadPresentacion() {
            val nombreVisible = limpiarNombrePresentacion(
                sheetBinding.etNombrePresentacionSheet.text?.toString().orEmpty()
            ).ifEmpty { getString(R.string.nombre_presentacion).lowercase() }

            val canonical = (sheetBinding.etNombrePresentacionSheet.tag as? String)
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: PresentacionesTiendaConfigManager.canonicalDesdeVisible(unidadBase, nombreVisible)

            sheetBinding.tilCantidadPresentacionSheet.hint = hintEquivalenciaSegunUnidadBase()

            val ayudaBase = ayudaEquivalenciaSegunSeleccion(canonical, nombreVisible)
            val cantidadIngresada = sheetBinding.etCantidadPresentacionSheet.text
                ?.toString()
                ?.trim()
                ?.toIntOrNull()
                ?.takeIf { it > 0 }

            fun fmt(valor: Double): String {
                return String.format(Locale.US, "%.2f", valor)
                    .trimEnd('0')
                    .trimEnd('.')
            }

            val conversion = when {
                cantidadIngresada == null -> null
                unidadBase.equals("g", ignoreCase = true) -> "Equivale a: ${fmt(cantidadIngresada / 1000.0)} kg"
                unidadBase.equals("mL", ignoreCase = true) -> "Equivale a: ${fmt(cantidadIngresada / 1000.0)} L"
                else -> null
            }

            sheetBinding.tilCantidadPresentacionSheet.helperText =
                if (conversion != null) "$ayudaBase\n$conversion" else ayudaBase
        }

        actualizarAyudaCantidadPresentacion()
        sheetBinding.etNombrePresentacionSheet.addTextChangedListener {
            actualizarAyudaCantidadPresentacion()
        }
        sheetBinding.etCantidadPresentacionSheet.addTextChangedListener {
            actualizarAyudaCantidadPresentacion()
        }

        // Selector fijo: presentaciones segun unidad base + configuracion de tienda (alias/habilitados)
        PresentacionesTiendaConfigManager.precargar()
        val opcionesPresentacion = PresentacionesTiendaConfigManager.opcionesParaUnidadBase(unidadBase)

        fun canonicalPresentacionEnProducto(nombre: String): String {
            val limpio = nombre.trim()
            if (limpio.isBlank()) return ""
            return PresentacionesTiendaConfigManager.canonicalDesdeVisible(unidadBase, limpio)
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: limpio
        }

        val presentacionesCanonicasEnUso = listaPresentaciones
            .map { canonicalPresentacionEnProducto(it.nombre) }
            .filter { it.isNotBlank() }
            .map { PresentacionHelper.normalizarClave(it) }
            .toSet()

        data class OpcionPresentacionUi(
            val nombreCanonical: String,
            val nombreVisible: String,
            val enUso: Boolean
        ) {
            override fun toString(): String = nombreVisible
        }

        val opcionesPresentacionUi = opcionesPresentacion.map { opcion ->
            OpcionPresentacionUi(
                nombreCanonical = opcion.nombreCanonical,
                nombreVisible = opcion.nombreVisible,
                enUso = presentacionesCanonicasEnUso.contains(
                    PresentacionHelper.normalizarClave(opcion.nombreCanonical)
                )
            )
        }

        val adapterPresentaciones = object : ArrayAdapter<OpcionPresentacionUi>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            opcionesPresentacionUi
        ) {
            private fun bindOpcion(view: View, item: OpcionPresentacionUi?): View {
                (view as? TextView)?.let { textView ->
                    if (item != null) {
                        textView.text = if (item.enUso) {
                            "${item.nombreVisible} - ya en uso"
                        } else {
                            item.nombreVisible
                        }
                        textView.setTextColor(
                            if (item.enUso) Color.parseColor("#98A2B3")
                            else Color.parseColor("#101828")
                        )
                        textView.alpha = if (item.enUso) 0.65f else 1f
                        textView.isEnabled = !item.enUso
                    }
                }
                return view
            }

            override fun isEnabled(position: Int): Boolean {
                return getItem(position)?.enUso != true
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                return bindOpcion(view, getItem(position))
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                return bindOpcion(view, getItem(position))
            }
        }

        sheetBinding.etNombrePresentacionSheet.setAdapter(adapterPresentaciones)
        sheetBinding.etNombrePresentacionSheet.threshold = Int.MAX_VALUE
        sheetBinding.etNombrePresentacionSheet.inputType = InputType.TYPE_NULL
        sheetBinding.etNombrePresentacionSheet.keyListener = null
        sheetBinding.etNombrePresentacionSheet.isCursorVisible = false
        sheetBinding.etNombrePresentacionSheet.isFocusable = false
        sheetBinding.etNombrePresentacionSheet.isFocusableInTouchMode = false
        aplicarCierreDropdownAlRecibirFoco(sheetBinding.etNombrePresentacionSheet)

        sheetBinding.tilNombrePresentacionSheet.endIconMode = TextInputLayout.END_ICON_CUSTOM
        sheetBinding.tilNombrePresentacionSheet.setEndIconDrawable(R.drawable.ic_arrow_drop_down)
        sheetBinding.tilNombrePresentacionSheet.endIconContentDescription = "Ver presentaciones"
        sheetBinding.tilNombrePresentacionSheet.setEndIconOnClickListener {
            sheetBinding.etNombrePresentacionSheet.post {
                if ((sheetBinding.etNombrePresentacionSheet.adapter?.count ?: 0) > 0) {
                    sheetBinding.etNombrePresentacionSheet.showDropDown()
                }
            }
        }
        sheetBinding.tilNombrePresentacionSheet.helperText =
            if (opcionesPresentacionUi.any { it.enUso }) {
                "Las opciones marcadas como 'ya en uso' no se pueden repetir en este producto."
            } else {
                null
            }

        sheetBinding.etNombrePresentacionSheet.setOnClickListener {
            sheetBinding.etNombrePresentacionSheet.showDropDown()
        }

        sheetBinding.etNombrePresentacionSheet.setOnItemClickListener { parent, _, position, _ ->
            val opcionSeleccionada = parent.getItemAtPosition(position) as? OpcionPresentacionUi
            if (opcionSeleccionada?.enUso == true) {
                sheetBinding.etNombrePresentacionSheet.error =
                    "Esa presentación ya existe en este producto. Selecciona una presentación diferente."
                sheetBinding.etNombrePresentacionSheet.dismissDropDown()
                return@setOnItemClickListener
            }
            if (opcionSeleccionada != null) {
                sheetBinding.etNombrePresentacionSheet.setText(opcionSeleccionada.nombreVisible, false)
                sheetBinding.etNombrePresentacionSheet.tag = opcionSeleccionada.nombreCanonical
            }
            sheetBinding.etNombrePresentacionSheet.dismissDropDown()
            sheetBinding.etNombrePresentacionSheet.clearFocus()
            sheetBinding.root.requestFocus()
            ocultarTeclado()
            actualizarAyudaCantidadPresentacion()
        }

        sheetBinding.btnGuardarPresentacionSheet.setOnClickListener {
            val nombreRaw = sheetBinding.etNombrePresentacionSheet.text.toString()
            val nombreVisibleSeleccionado = limpiarNombrePresentacion(nombreRaw)
            val cantidadTexto = sheetBinding.etCantidadPresentacionSheet.text.toString().trim()
            val precioTexto = sheetBinding.etPrecioPresentacionSheet.text.toString().trim()

            var esValido = true

            sheetBinding.etNombrePresentacionSheet.error = null
            sheetBinding.etCantidadPresentacionSheet.error = null
            sheetBinding.etPrecioPresentacionSheet.error = null

            val nombreCanonical = (sheetBinding.etNombrePresentacionSheet.tag as? String)
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: PresentacionesTiendaConfigManager.canonicalDesdeVisible(unidadBase, nombreVisibleSeleccionado)

            if (nombreVisibleSeleccionado.isEmpty() || nombreCanonical.isNullOrBlank()) {
                sheetBinding.etNombrePresentacionSheet.error = "Ingrese un nombre"
                esValido = false
            }

            if (!nombreCanonical.isNullOrBlank() && !CatalogoPresentaciones.esPresentacionValida(unidadBase, nombreCanonical)) {
                sheetBinding.etNombrePresentacionSheet.error =
                    "Esa presentacion no aplica para este producto. Elige una opcion real de la lista."
                esValido = false
            }

            if (cantidadTexto.isEmpty()) {
                sheetBinding.etCantidadPresentacionSheet.error = "Ingrese la cantidad"
                esValido = false
            }

            if (precioTexto.isEmpty()) {
                sheetBinding.etPrecioPresentacionSheet.error = "Ingrese el precio de venta"
                esValido = false
            }

            if (!esValido) return@setOnClickListener

            val cantidad = cantidadTexto.toIntOrNull()
            val precio = precioTexto.toDoubleOrNull()

            if (cantidad == null || cantidad <= 0) {
                val unidadBaseUi = unidadBase.ifBlank { "unidades" }
                val nombreUi = nombreVisibleSeleccionado.ifBlank { getString(R.string.nombre_presentacion) }
                val canonical = (sheetBinding.etNombrePresentacionSheet.tag as? String)
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: PresentacionesTiendaConfigManager.canonicalDesdeVisible(unidadBase, nombreVisibleSeleccionado)

                val ejemplo = when {
                    unidadBase.equals("g", ignoreCase = true) -> {
                        if (canonical.equals("kg", ignoreCase = true)) "Ej: 1 kg = 1000 g."
                        else "Ej: 1 g = 1 g."
                    }
                    unidadBase.equals("mL", ignoreCase = true) -> {
                        if (canonical.equals("l", ignoreCase = true)) "Ej: 1 L = 1000 mL."
                        else "Ej: 1 mL = 1 mL."
                    }
                    else -> "Ej: $nombreUi = 20 $unidadBaseUi."
                }
                sheetBinding.etCantidadPresentacionSheet.error =
                    "Cantidad inválida: ingresa cuántas $unidadBaseUi trae 1 $nombreUi. Debe ser mayor a 0. $ejemplo"
                return@setOnClickListener
            }

            // Regla: si el producto fue registrado por paquetes (1 caja = X unidades),
            // ninguna presentación puede exceder X.
            val maxUnidades = productoOriginalParaAuditoria
                ?.unidadesPorPresentacionCompra
                ?.takeIf { it > 0 }

            if (maxUnidades != null && cantidad != null && cantidad > maxUnidades) {
                val unidadBaseUi = unidadBase.ifBlank { "unidades" }
                val nombrePresentacionTope = obtenerNombrePresentacionTopeProducto(unidadBase)
                sheetBinding.etCantidadPresentacionSheet.error =
                    "Cantidad inválida: no puede ser mayor a $maxUnidades $unidadBaseUi porque 1 $nombrePresentacionTope = $maxUnidades $unidadBaseUi y esa es la presentación más alta registrada del producto. Otra presentación no debe superar esa cantidad."
                return@setOnClickListener
            }

            if (precio == null || precio <= 0.0) {
                sheetBinding.etPrecioPresentacionSheet.error = "Debe ser mayor a 0"
                return@setOnClickListener
            }

            val existenteMismoNombre = nombreCanonical?.let { canonicalSeleccionado ->
                listaPresentaciones.firstOrNull {
                    PresentacionHelper.normalizarClave(canonicalPresentacionEnProducto(it.nombre)) ==
                        PresentacionHelper.normalizarClave(canonicalSeleccionado)
                }
            }

        if (existenteMismoNombre != null) {
            val unidadBaseUi = unidadBase.ifBlank { "unidades" }
            val visible = PresentacionesTiendaConfigManager.nombreVisible(existenteMismoNombre.nombre)
            sheetBinding.etNombrePresentacionSheet.error = if (existenteMismoNombre.cantidad == cantidad) {
                "Esa presentación ya existe en este producto. Selecciona una presentación diferente."
            } else {
                "'$visible' ya está en uso con ${existenteMismoNombre.cantidad} $unidadBaseUi. No puedes agregar otra igual en este producto."
            }
            return@setOnClickListener
        }

            val presentacion = PresentacionProducto().apply {
                this.nombre = nombreCanonical ?: nombreVisibleSeleccionado
                this.cantidad = cantidad ?: 0
                this.precioventa = precio ?: 0.0
            }

            val cantidadAntes = listaPresentaciones.size
            listaPresentaciones.add(presentacion)
            agregarPresentacionVisual(presentacion)
            actualizarOpcionesPresentacionPrincipal()

            if (listaPresentaciones.size == 1) {
                asignarPresentacionPrincipal(presentacion.nombre, seleccionManual = false)
            } else if (cantidadAntes == 1 && !presentacionPrincipalElegidaManualmente) {
                limpiarPresentacionPrincipal()
                binding.tilPresentacionPrincipal.helperText =
                    "Agregaste varias presentaciones. Ahora elige cuál será la principal."
            }

            actualizarEstadoBotonGuardar()
            dialog.dismiss()
        }

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<FrameLayout>(
                com.google.android.material.R.id.design_bottom_sheet
            )

            bottomSheet?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet)

                behavior.skipCollapsed = true
                behavior.isFitToContents = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED

                sheet.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                sheet.requestLayout()
            }
        }

        dialog.show()
    }

    private fun agregarPresentacionVisual(presentacion: PresentacionProducto) {
        val itemBinding = ItemPresentacionGuardadaBinding.inflate(
            layoutInflater,
            binding.contenedorPresentaciones,
            false
        )

        aplicarVistaPresentacionCompacta(itemBinding, presentacion)
        binding.contenedorPresentaciones.addView(itemBinding.root)
        return

        val unidadBaseRaw = binding.editTextUnidadBase.text?.toString().orEmpty().trim()
        val esMl = unidadBaseRaw.equals("mL", ignoreCase = true)
        val esG = unidadBaseRaw.equals("g", ignoreCase = true)
        when {
            esMl -> "mL"
            esG -> "g"
            else -> UnidadBaseHelper.formatear(unidadBaseRaw, presentacion.cantidad)
                .ifEmpty { "unidades" }
        }
        val nombrePresentacionVisible =
            PresentacionesTiendaConfigManager.nombreVisible(presentacion.nombre)

        itemBinding.tvNombrePresentacionItem.text = nombrePresentacionVisible

        // Ícono y color de fondo según tipo de presentación
        // Caja/paquete → verde · Unidad → azul · Blíster/Frasco/Pack → ámbar
        val (iconBg, iconTint) = when (presentacion.nombre.trim().lowercase(Locale.getDefault())) {
            "unidad", "unidades" -> 0xFFE6F1FB.toInt() to 0xFF2176C7.toInt()
            "caja", "cajas" -> 0xFFEAF3DE.toInt() to 0xFF3B6D11.toInt()
            else -> 0xFFFAEEDA.toInt() to 0xFF7B5418.toInt()
        }
        itemBinding.cardIconoPresentacionItem.setCardBackgroundColor(iconBg)
        itemBinding.imgPresentacionItem.setImageResource(
            resolverIconoPresentacionMovimiento(nombrePresentacionVisible)
        )
        itemBinding.imgPresentacionItem.setColorFilter(iconTint)

        fun actualizarDetalle() {
            // Detalle: cantidad + unidad (ej. "5 unidades")
            val etiqDinamica = when {
                esMl -> "mL"
                esG -> "g"
                else -> UnidadBaseHelper.formatear(unidadBaseRaw, presentacion.cantidad)
                    .ifEmpty { "unidades" }
            }
            itemBinding.tvDetallePresentacionItem.text =
                "${presentacion.cantidad} ${etiqDinamica.lowercase()}"

            // Precio + "VES" en su columna derecha
            val precioFormateado = MonedaHelper.formatear(presentacion.precioventa)
            itemBinding.tvPrecioPresentacionItem.text = precioFormateado
            itemBinding.tvPrecioPresentacionItem.visibility =
                if (presentacion.precioventa > 0.0) View.VISIBLE else View.GONE
            itemBinding.tvSubMonedaPresentacionItem.visibility =
                if (presentacion.precioventa > 0.0) View.VISIBLE else View.GONE
        }
        actualizarDetalle()

        // Pre-poblar inputs del panel de edición
        itemBinding.editCantidadInline.setText(presentacion.cantidad.toString())
        if (presentacion.precioventa > 0.0) {
            itemBinding.editPrecioInline.setText(presentacion.precioventa.toString())
        }

        // Tap en la fila → expandir/colapsar panel de edición
        itemBinding.rowPresentacionItem.setOnClickListener {
            val visible = itemBinding.panelEdicionPresentacionItem.visibility == View.VISIBLE
            itemBinding.panelEdicionPresentacionItem.visibility =
                if (visible) View.GONE else View.VISIBLE
            // Resaltar fila activa al editar
            itemBinding.rowPresentacionItem.setBackgroundColor(
                if (!visible) 0xFFF8FAFB.toInt() else Color.TRANSPARENT
            )
            if (!visible) {
                itemBinding.editPrecioInline.requestFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE)
                    as? InputMethodManager
                imm?.showSoftInput(itemBinding.editPrecioInline, 0)
            }
        }

        // Botón "Guardar cambios" del panel inline
        itemBinding.btnGuardarCambiosPresentacionItem.setOnClickListener {
            val nuevaCantidad = itemBinding.editCantidadInline.text?.toString()
                ?.trim()?.toIntOrNull() ?: 0
            val nuevoPrecio = itemBinding.editPrecioInline.text?.toString()
                ?.trim()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

            if (nuevaCantidad <= 0) {
                itemBinding.tilCantidadInline.error = "Cantidad requerida"
                return@setOnClickListener
            }
            if (nuevoPrecio <= 0.0) {
                itemBinding.tilPrecioInline.error = "Precio requerido"
                return@setOnClickListener
            }
            itemBinding.tilCantidadInline.error = null
            itemBinding.tilPrecioInline.error = null

            // Actualizar el modelo en memoria
            presentacion.cantidad = nuevaCantidad
            presentacion.precioventa = nuevoPrecio

            // Refrescar los textos visibles + colapsar panel
            actualizarDetalle()
            itemBinding.panelEdicionPresentacionItem.visibility = View.GONE
            itemBinding.rowPresentacionItem.setBackgroundColor(Color.TRANSPARENT)

            // Ocultar teclado
            val imm = getSystemService(INPUT_METHOD_SERVICE)
                as? InputMethodManager
            imm?.hideSoftInputFromWindow(itemBinding.editPrecioInline.windowToken, 0)

            // Refrescar listado y tarjeta principal
            actualizarOpcionesPresentacionPrincipal()
            actualizarEstadoBotonGuardar()
            Toast.makeText(this, "Cambios aplicados", Toast.LENGTH_SHORT).show()
        }

        // Mantener TextWatcher en precio inline para reflejar cambios en vivo en el modelo
        // (necesario para validaciones del botón "Guardar todo")
        itemBinding.editPrecioInline.addTextChangedListener { text ->
            val precio = text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
            if (precio > 0.0) {
                itemBinding.tilPrecioInline.error = null
            }
        }
        itemBinding.editCantidadInline.addTextChangedListener {
            itemBinding.tilCantidadInline.error = null
        }

        itemBinding.btnEliminarPresentacionItem.setOnClickListener {
            if (listaPresentaciones.size <= 1) {
                Toast.makeText(
                    this,
                    "No puedes eliminar la última presentación. Primero debes crear una nueva presentación para poder borrar esta. El producto no puede quedar sin presentación.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val nombreEliminado = presentacion.nombre
            val eraPrincipal = PresentacionHelper.sonNombresEquivalentes(
                obtenerCanonicalPresentacionPrincipalActual(),
                nombreEliminado
            )
            binding.contenedorPresentaciones.removeView(itemBinding.root)
            listaPresentaciones.remove(presentacion)
            actualizarOpcionesPresentacionPrincipal()

            if (eraPrincipal) {
                val reemplazo = obtenerPresentacionMayorMontoVenta()
                if (reemplazo != null) {
                    asignarPresentacionPrincipal(reemplazo.nombre, seleccionManual = true)
                    val nombreVisible = PresentacionesTiendaConfigManager.nombreVisible(reemplazo.nombre)
                    binding.tilPresentacionPrincipal.helperText =
                        "Se reasignó automáticamente $nombreVisible como presentación principal por tener el mayor monto de venta."
                    Toast.makeText(
                        this,
                        "$nombreVisible ahora será la presentación principal de venta.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else if (!binding.switchPresentaciones.isChecked) {
                asegurarPresentacionValidaAlDesactivarMultiples(mostrarAviso = false)
            }
            actualizarEstadoBotonGuardar()
        }

        binding.contenedorPresentaciones.addView(itemBinding.root)
        actualizarEstadoBotonGuardar()
    }

    private fun actualizarOpcionesPresentacionPrincipal() {
        actualizarOpcionesPresentacionPrincipalCompactas()
        return
        val opcionesCanonicas = listaPresentaciones.map { it.nombre }
        val opcionesVisibles = opcionesCanonicas.map { PresentacionesTiendaConfigManager.nombreVisible(it) }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            opcionesVisibles
        )

        binding.editTextPresentacionPrincipal.setAdapter(adapter)
        binding.editTextPresentacionPrincipal.threshold = Int.MAX_VALUE
        binding.editTextPresentacionPrincipal.inputType = InputType.TYPE_NULL
        binding.editTextPresentacionPrincipal.keyListener = null
        binding.editTextPresentacionPrincipal.isCursorVisible = false
        binding.editTextPresentacionPrincipal.isFocusable = false
        binding.editTextPresentacionPrincipal.isFocusableInTouchMode = false
        aplicarCierreDropdownAlRecibirFoco(binding.editTextPresentacionPrincipal)

        binding.tilPresentacionPrincipal.setEndIconOnClickListener {
            binding.editTextPresentacionPrincipal.post {
                if ((binding.editTextPresentacionPrincipal.adapter?.count ?: 0) > 0) {
                    binding.editTextPresentacionPrincipal.showDropDown()
                }
            }
        }

        binding.editTextPresentacionPrincipal.setOnClickListener {
            binding.editTextPresentacionPrincipal.post {
                if ((binding.editTextPresentacionPrincipal.adapter?.count ?: 0) > 0) {
                    binding.editTextPresentacionPrincipal.showDropDown()
                }
            }
        }

        binding.editTextPresentacionPrincipal.setOnItemClickListener { _, _, position, _ ->
            val canonical = opcionesCanonicas.getOrNull(position)
            if (canonical != null) {
                asignarPresentacionPrincipal(canonical, seleccionManual = true)
            }
            binding.editTextPresentacionPrincipal.dismissDropDown()
            binding.editTextPresentacionPrincipal.clearFocus()
            binding.main.requestFocus()
            ocultarTeclado()
            actualizarEstadoBotonGuardar()
        }

        sincronizarPresentacionPrincipalSegunLista()
        actualizarTarjetaPrincipalDeVenta()
    }

    /**
     * Actualiza la tarjeta resaltada "Principal de venta" mostrada arriba del listado
     * de presentaciones. Muestra el nombre, ícono y la equivalencia legible
     * (p.ej. "1 caja = 5 blíster = 50 unidades").
     */
    private fun actualizarTarjetaPrincipalDeVenta() {
        actualizarTarjetaPrincipalDeVentaCompacta()
        return
        val tvNombre = binding.root.findViewById<TextView>(R.id.tvNombrePrincipalDeVenta) ?: return
        val tvEquivalencia = binding.root.findViewById<TextView>(R.id.tvEquivalenciaPrincipalDeVenta)
            ?: return
        val img = binding.root.findViewById<ImageView>(R.id.imgPrincipalDeVenta) ?: return
        val card = binding.root.findViewById<MaterialCardView>(
            R.id.cardPrincipalDeVenta
        ) ?: return

        val canonical = obtenerCanonicalPresentacionPrincipalActual().trim()
        val unidadBase = binding.editTextUnidadBase.text?.toString()?.trim().orEmpty()
            .ifBlank { "unidad" }
        val nombreVisible = if (canonical.isNotBlank()) {
            PresentacionesTiendaConfigManager.nombreVisible(canonical)
        } else {
            formatearEtiquetaUi(unidadBase)
        }

        tvNombre.text = nombreVisible
        img.setImageResource(resolverIconoPresentacionMovimiento(nombreVisible))

        // Construir equivalencia: "1 caja = 5 blíster = 50 unidades"
        val principal = listaPresentaciones.firstOrNull {
            PresentacionHelper.sonNombresEquivalentes(it.nombre, canonical)
        }
        val partes = mutableListOf<String>()
        partes.add("1 ${nombreVisible.lowercase()}")
        // Presentaciones intermedias (más pequeñas que la principal pero mayor a 1)
        val unidadesPorPrincipal = principal?.cantidad ?: 1
        listaPresentaciones
            .filter { p ->
                p.cantidad in 2 until unidadesPorPrincipal &&
                    !PresentacionHelper.sonNombresEquivalentes(p.nombre, canonical)
            }
            .sortedByDescending { it.cantidad }
            .take(2)
            .forEach { intermedia ->
                val cuantos = unidadesPorPrincipal / intermedia.cantidad
                if (cuantos > 0 && unidadesPorPrincipal % intermedia.cantidad == 0) {
                    val nombreIntermedia = PresentacionesTiendaConfigManager.nombreVisible(intermedia.nombre)
                    partes.add("$cuantos ${nombreIntermedia.lowercase()}")
                }
            }
        if (unidadesPorPrincipal > 0) {
            partes.add("$unidadesPorPrincipal ${unidadBase.lowercase()}")
        }
        tvEquivalencia.text = partes.joinToString(" = ")
        card.visibility = View.VISIBLE
    }

    private fun refrescarPresentacionesVisualesCompactas() {
        binding.contenedorPresentaciones.removeAllViews()
        listaPresentaciones.forEach { agregarPresentacionVisual(it) }
    }

    private fun formatearEquivalenciaPresentacionCompacta(presentacion: PresentacionProducto): String {
        val unidadBase = binding.editTextUnidadBase.text?.toString()?.trim().orEmpty().ifBlank { "unidad" }
        val partes = mutableListOf<String>()
        val nombrePrincipal = PresentacionesTiendaConfigManager.nombreVisible(presentacion.nombre).lowercase()
        partes.add("1 $nombrePrincipal")

        listaPresentaciones
            .filter { candidata ->
                candidata.cantidad in 2 until presentacion.cantidad &&
                    !PresentacionHelper.sonNombresEquivalentes(candidata.nombre, presentacion.nombre)
            }
            .sortedByDescending { it.cantidad }
            .forEach { intermedia ->
                if (presentacion.cantidad % intermedia.cantidad == 0) {
                    val veces = presentacion.cantidad / intermedia.cantidad
                    val nombreIntermedio =
                        PresentacionesTiendaConfigManager.nombreVisible(intermedia.nombre).lowercase()
                    partes.add("$veces $nombreIntermedio")
                }
            }

        partes.add("${presentacion.cantidad} ${unidadBase.lowercase()}")
        return partes.distinct().joinToString(" = ")
    }

    private fun actualizarTarjetaPrincipalDeVentaCompacta() {
        val principal = obtenerPresentacionPrincipalActualDesdeLista()
            ?: obtenerPresentacionMayorMontoVenta()
            ?: listaPresentaciones.firstOrNull()

        val card = binding.root.findViewById<MaterialCardView>(R.id.cardPrincipalDeVenta) ?: return
        val tvNombre = binding.root.findViewById<TextView>(R.id.tvNombrePrincipalDeVenta) ?: return
        val tvEquivalencia = binding.root.findViewById<TextView>(R.id.tvEquivalenciaPrincipalDeVenta) ?: return
        val img = binding.root.findViewById<ImageView>(R.id.imgPrincipalDeVenta) ?: return
        val tvNombreProducto = binding.root.findViewById<TextView>(R.id.tvNombreProductoPresentaciones)
        val imgProducto = binding.root.findViewById<ImageView>(R.id.imgProductoPresentaciones)

        tvNombreProducto?.text = binding.editTextNombre.text?.toString()?.trim().orEmpty().ifBlank { "Producto" }
        if (binding.imagevieW.drawable != null) {
            imgProducto?.setImageDrawable(binding.imagevieW.drawable)
            imgProducto?.clearColorFilter()
        }

        if (principal == null) {
            card.visibility = View.GONE
            return
        }

        val nombreVisible = PresentacionesTiendaConfigManager.nombreVisible(principal.nombre)
        tvNombre.text = nombreVisible
        tvEquivalencia.text = formatearEquivalenciaPresentacionCompacta(principal)
        img.setImageResource(resolverIconoPresentacionMovimiento(nombreVisible))
        card.visibility = View.VISIBLE
    }

    private fun aplicarVistaPresentacionCompacta(
        itemBinding: ItemPresentacionGuardadaBinding,
        presentacion: PresentacionProducto
    ) {
        val nombreVisible = PresentacionesTiendaConfigManager.nombreVisible(presentacion.nombre)
        val principalActual = obtenerPresentacionPrincipalActualDesdeLista()
        val esPrincipal = principalActual?.let {
            PresentacionHelper.sonNombresEquivalentes(it.nombre, presentacion.nombre)
        } == true
        val mostrarRadio = listaPresentaciones.size > 1

        val (iconBg, iconTint) = when (presentacion.nombre.trim().lowercase(Locale.getDefault())) {
            "unidad", "unidades" -> 0xFFE6F1FB.toInt() to 0xFF2176C7.toInt()
            "caja", "cajas" -> 0xFFEAF3DE.toInt() to 0xFF3B6D11.toInt()
            else -> 0xFFFAEEDA.toInt() to 0xFF7B5418.toInt()
        }

        itemBinding.cardIconoPresentacionItem.setCardBackgroundColor(iconBg)
        itemBinding.imgPresentacionItem.setImageResource(resolverIconoPresentacionMovimiento(nombreVisible))
        itemBinding.imgPresentacionItem.setColorFilter(iconTint)
        itemBinding.tvNombrePresentacionItem.text = nombreVisible
        itemBinding.tvDetallePresentacionItem.text = if (presentacion.cantidad <= 1) {
            "1 ${binding.editTextUnidadBase.text?.toString()?.trim().orEmpty().ifBlank { "unidad" }.lowercase()}"
        } else {
            formatearEquivalenciaPresentacionCompacta(presentacion).substringAfter("= ").trim()
        }
        itemBinding.tvPrecioPresentacionItem.text = MonedaHelper.formatear(presentacion.precioventa)
        itemBinding.tvSubMonedaPresentacionItem.text = "Precio de venta"
        itemBinding.radioPrincipalPresentacionItem.visibility = if (mostrarRadio) View.VISIBLE else View.GONE
        itemBinding.radioPrincipalPresentacionItem.isChecked = esPrincipal
        itemBinding.tvBadgePrincipalPresentacionItem.visibility = if (esPrincipal) View.VISIBLE else View.GONE
        itemBinding.cardPresentacionGuardada.strokeColor =
            if (esPrincipal) "#B7E4CC".toColorInt() else "#E6EAF0".toColorInt()
        itemBinding.cardPresentacionGuardada.setCardBackgroundColor(
            if (esPrincipal) "#F6FCF8".toColorInt() else Color.WHITE
        )

        val seleccionarPrincipal = {
            if (listaPresentaciones.size > 1) {
                asignarPresentacionPrincipal(presentacion.nombre, seleccionManual = true)
                actualizarOpcionesPresentacionPrincipal()
                actualizarEstadoBotonGuardar()
            }
        }

        itemBinding.rowPresentacionItem.setOnClickListener { seleccionarPrincipal() }
        itemBinding.radioPrincipalPresentacionItem.setOnClickListener { seleccionarPrincipal() }
        itemBinding.btnEditarPresentacionItem.setOnClickListener {
            mostrarBottomSheetPresentacionCompacta(presentacion)
        }
        itemBinding.btnEliminarPresentacionItem.setOnClickListener {
            if (listaPresentaciones.size <= 1) {
                Toast.makeText(
                    this,
                    "No puedes eliminar la última presentación. Agrega otra antes de borrarla.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val eraPrincipal = PresentacionHelper.sonNombresEquivalentes(
                obtenerCanonicalPresentacionPrincipalActual(),
                presentacion.nombre
            )
            listaPresentaciones.remove(presentacion)
            if (eraPrincipal) {
                val reemplazo = obtenerPresentacionMayorMontoVenta() ?: listaPresentaciones.firstOrNull()
                if (reemplazo != null) {
                    asignarPresentacionPrincipal(reemplazo.nombre, seleccionManual = true)
                }
            }
            actualizarOpcionesPresentacionPrincipal()
            actualizarEstadoBotonGuardar()
        }
    }

    @Suppress("DEPRECATION")
    private fun mostrarBottomSheetPresentacionCompacta(
        presentacionEditar: PresentacionProducto? = null,
        onComplete: (() -> Unit)? = null
    ) {
        val dialog = BottomSheetDialog(this)
        val sheetBinding = BottomsheetAgregarPresentacionBinding.inflate(layoutInflater)
        val esEdicion = presentacionEditar != null
        val unidadBase = binding.editTextUnidadBase.text.toString().trim()
            .ifEmpty { getString(R.string.por_unidades).lowercase() }

        dialog.setContentView(sheetBinding.root)
        configurarBackdropBottomSheet(dialog)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val rootContainer = sheetBinding.root.getChildAt(0) as? ViewGroup
        val titleView = rootContainer?.getChildAt(1) as? TextView
        val subtitleView = rootContainer?.getChildAt(2) as? TextView
        titleView?.text = if (esEdicion) "Editar presentación" else "Agregar presentación"
        subtitleView?.text = if (esEdicion) {
            "Actualiza equivalencia y precio de venta."
        } else {
            "Agrega una nueva forma de venta para este producto."
        }
        sheetBinding.btnGuardarPresentacionSheet.text =
            if (esEdicion) "Guardar cambios" else "Guardar presentación"

        fun limpiarNombre(input: String): String = input.trim().replace("\\s+".toRegex(), " ")

        fun ayudaSegunSeleccion(nombreCanonical: String?, nombreVisible: String): String {
            return when {
                unidadBase.equals("g", ignoreCase = true) -> when {
                    nombreCanonical.equals("kg", ignoreCase = true) -> "Ej: 1 kg = 1000 g."
                    nombreCanonical.equals("g", ignoreCase = true) -> "Ej: 1 g = 1 g."
                    else -> "Ingresa cuántos g contiene 1 $nombreVisible."
                }

                unidadBase.equals("mL", ignoreCase = true) -> when {
                    nombreCanonical.equals("l", ignoreCase = true) -> "Ej: 1 L = 1000 mL."
                    nombreCanonical.equals("ml", ignoreCase = true) -> "Ej: 1 mL = 1 mL."
                    else -> "Ingresa cuántos mL contiene 1 $nombreVisible."
                }

                else -> getString(R.string.ayuda_cantidad_presentacion, nombreVisible, unidadBase)
            }
        }

        fun ejemplosPresentacion(): String {
            return when {
                unidadBase.equals("g", ignoreCase = true) -> "Ejemplos: Bolsa (250 g), Paquete (500 g), Saco (1 kg)"
                unidadBase.equals("mL", ignoreCase = true) -> "Ejemplos: Botella (500 mL), Frasco (120 mL), Galón (1 L)"
                else -> "Ejemplos: Unidad (1), Blíster (10 u), Caja (50 u)"
            }
        }

        fun canonicalEnProducto(nombre: String): String {
            return PresentacionesTiendaConfigManager.canonicalDesdeVisible(unidadBase, nombre.trim())
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: nombre.trim()
        }

        fun actualizarAyuda() {
            val nombreVisible = limpiarNombre(sheetBinding.etNombrePresentacionSheet.text?.toString().orEmpty())
                .ifEmpty { getString(R.string.nombre_presentacion).lowercase() }
            val canonical = (sheetBinding.etNombrePresentacionSheet.tag as? String)
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: PresentacionesTiendaConfigManager.canonicalDesdeVisible(unidadBase, nombreVisible)
            sheetBinding.tilCantidadPresentacionSheet.hint = when {
                unidadBase.equals("g", ignoreCase = true) -> "Equivale a cuántos g"
                unidadBase.equals("mL", ignoreCase = true) -> "Equivale a cuántos mL"
                else -> getString(R.string.equivale_unidades)
            }
            sheetBinding.tilCantidadPresentacionSheet.helperText =
                ayudaSegunSeleccion(canonical, nombreVisible)
        }

        PresentacionesTiendaConfigManager.precargar()
        val editandoClave = presentacionEditar?.let { PresentacionHelper.normalizarClave(it.nombre) }
        val clavesEnUso = listaPresentaciones
            .filterNot { presentacion ->
                editandoClave != null && PresentacionHelper.normalizarClave(presentacion.nombre) == editandoClave
            }
            .map { canonicalEnProducto(it.nombre) }
            .filter { it.isNotBlank() }
            .map { PresentacionHelper.normalizarClave(it) }
            .toSet()

        data class OpcionPresentacionUi(
            val nombreCanonical: String,
            val nombreVisible: String,
            val enUso: Boolean = false
        ) {
            override fun toString(): String = nombreVisible
        }

        val opcionesUi = PresentacionesTiendaConfigManager.opcionesParaUnidadBase(unidadBase)
            .filterNot { opcion ->
                clavesEnUso.contains(PresentacionHelper.normalizarClave(opcion.nombreCanonical))
            }
            .map { opcion ->
                OpcionPresentacionUi(
                    opcion.nombreCanonical,
                    opcion.nombreVisible
                )
            }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            opcionesUi
        )

        sheetBinding.etNombrePresentacionSheet.setAdapter(adapter)
        sheetBinding.tilNombrePresentacionSheet.helperText = ejemplosPresentacion()
        sheetBinding.etNombrePresentacionSheet.threshold = Int.MAX_VALUE
        sheetBinding.etNombrePresentacionSheet.inputType = InputType.TYPE_NULL
        sheetBinding.etNombrePresentacionSheet.keyListener = null
        sheetBinding.etNombrePresentacionSheet.isCursorVisible = false
        sheetBinding.etNombrePresentacionSheet.isFocusable = false
        sheetBinding.etNombrePresentacionSheet.isFocusableInTouchMode = false
        aplicarCierreDropdownAlRecibirFoco(sheetBinding.etNombrePresentacionSheet)
        sheetBinding.tilNombrePresentacionSheet.endIconMode = TextInputLayout.END_ICON_CUSTOM
        sheetBinding.tilNombrePresentacionSheet.setEndIconDrawable(R.drawable.ic_arrow_drop_down)
        sheetBinding.tilNombrePresentacionSheet.setEndIconOnClickListener {
            sheetBinding.etNombrePresentacionSheet.showDropDown()
        }
        sheetBinding.etNombrePresentacionSheet.setOnClickListener {
            sheetBinding.etNombrePresentacionSheet.showDropDown()
        }
        sheetBinding.etNombrePresentacionSheet.setOnItemClickListener { parent, _, position, _ ->
            val opcion = parent.getItemAtPosition(position) as? OpcionPresentacionUi ?: return@setOnItemClickListener
            if (opcion.enUso) {
                sheetBinding.etNombrePresentacionSheet.error = "Esa presentación ya existe en este producto."
                return@setOnItemClickListener
            }
            sheetBinding.etNombrePresentacionSheet.setText(opcion.nombreVisible, false)
            sheetBinding.etNombrePresentacionSheet.tag = opcion.nombreCanonical
            actualizarAyuda()
        }

        if (presentacionEditar != null) {
            sheetBinding.etNombrePresentacionSheet.setText(
                PresentacionesTiendaConfigManager.nombreVisible(presentacionEditar.nombre),
                false
            )
            sheetBinding.etNombrePresentacionSheet.tag = presentacionEditar.nombre
            sheetBinding.etCantidadPresentacionSheet.setText(presentacionEditar.cantidad.toString())
            sheetBinding.etPrecioPresentacionSheet.setText(
                String.format(Locale.US, "%.2f", presentacionEditar.precioventa)
            )
        }

        val nombreInicial = sheetBinding.etNombrePresentacionSheet.text?.toString()?.trim().orEmpty()
        val tagInicial = (sheetBinding.etNombrePresentacionSheet.tag as? String)?.trim().orEmpty()
        val cantidadInicial = sheetBinding.etCantidadPresentacionSheet.text?.toString()?.trim().orEmpty()
        val precioInicial = sheetBinding.etPrecioPresentacionSheet.text?.toString()?.trim().orEmpty()

        fun actualizarEstadoBotonPresentacionSheet() {
            val nombreActual = sheetBinding.etNombrePresentacionSheet.text?.toString()?.trim().orEmpty()
            val tagActual = (sheetBinding.etNombrePresentacionSheet.tag as? String)?.trim().orEmpty()
            val cantidadActual = sheetBinding.etCantidadPresentacionSheet.text?.toString()?.trim().orEmpty()
            val precioActual = sheetBinding.etPrecioPresentacionSheet.text?.toString()?.trim().orEmpty()
            val hayCambios =
                nombreActual != nombreInicial ||
                    tagActual != tagInicial ||
                    cantidadActual != cantidadInicial ||
                    precioActual != precioInicial
            aplicarEstadoBotonGuardadoMaterial(sheetBinding.btnGuardarPresentacionSheet, hayCambios)
        }

        actualizarAyuda()
        actualizarEstadoBotonPresentacionSheet()
        sheetBinding.etNombrePresentacionSheet.addTextChangedListener {
            actualizarAyuda()
            actualizarEstadoBotonPresentacionSheet()
        }
        sheetBinding.etCantidadPresentacionSheet.addTextChangedListener {
            actualizarAyuda()
            actualizarEstadoBotonPresentacionSheet()
        }
        sheetBinding.etPrecioPresentacionSheet.addTextChangedListener {
            actualizarEstadoBotonPresentacionSheet()
        }

        sheetBinding.btnGuardarPresentacionSheet.setOnClickListener {
            val nombreVisible = limpiarNombre(sheetBinding.etNombrePresentacionSheet.text.toString())
            val nombreCanonical = (sheetBinding.etNombrePresentacionSheet.tag as? String)
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: PresentacionesTiendaConfigManager.canonicalDesdeVisible(unidadBase, nombreVisible)
            val cantidad = sheetBinding.etCantidadPresentacionSheet.text?.toString()?.trim()?.toIntOrNull()
            val precio = sheetBinding.etPrecioPresentacionSheet.text?.toString()?.trim()
                ?.replace(",", ".")
                ?.toDoubleOrNull()

            sheetBinding.etNombrePresentacionSheet.error = null
            sheetBinding.etCantidadPresentacionSheet.error = null
            sheetBinding.etPrecioPresentacionSheet.error = null

            if (nombreVisible.isBlank() || nombreCanonical.isNullOrBlank()) {
                sheetBinding.etNombrePresentacionSheet.error = "Selecciona una presentación"
                return@setOnClickListener
            }
            if (!CatalogoPresentaciones.esPresentacionValida(unidadBase, nombreCanonical)) {
                sheetBinding.etNombrePresentacionSheet.error = "Esa presentación no aplica para este producto."
                return@setOnClickListener
            }
            if (cantidad == null || cantidad <= 0) {
                sheetBinding.etCantidadPresentacionSheet.error = "Ingresa una equivalencia válida"
                return@setOnClickListener
            }
            if (precio == null || precio <= 0.0) {
                sheetBinding.etPrecioPresentacionSheet.error = "Ingresa un precio válido"
                return@setOnClickListener
            }

            val repetida = listaPresentaciones.firstOrNull { existente ->
                if (presentacionEditar != null && existente === presentacionEditar) return@firstOrNull false
                PresentacionHelper.normalizarClave(canonicalEnProducto(existente.nombre)) ==
                    PresentacionHelper.normalizarClave(nombreCanonical)
            }
            if (repetida != null) {
                sheetBinding.etNombrePresentacionSheet.error = "Esa presentación ya existe en este producto."
                return@setOnClickListener
            }

            val objetivo = presentacionEditar ?: PresentacionProducto()
            val nombreAnterior = objetivo.nombre
            objetivo.nombre = nombreCanonical
            objetivo.cantidad = cantidad
            objetivo.precioventa = precio

            if (presentacionEditar == null) {
                listaPresentaciones.add(objetivo)
                if (listaPresentaciones.size == 1) {
                    asignarPresentacionPrincipal(objetivo.nombre, seleccionManual = false)
                }
            } else if (PresentacionHelper.sonNombresEquivalentes(
                    obtenerCanonicalPresentacionPrincipalActual(),
                    nombreAnterior
                )
            ) {
                asignarPresentacionPrincipal(objetivo.nombre, seleccionManual = true)
            }

            actualizarOpcionesPresentacionPrincipal()
            actualizarEstadoBotonGuardar()
            dialog.dismiss()
            onComplete?.invoke()
        }

        dialog.setOnShowListener {
            dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.skipCollapsed = true
                behavior.isFitToContents = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                sheet.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                sheet.requestLayout()
            }
        }

        dialog.show()
    }

    private fun actualizarOpcionesPresentacionPrincipalCompactas() {
        binding.tilPresentacionPrincipal.visibility = View.GONE
        binding.textPresentacionPrincipal.visibility = View.GONE
        binding.tilPresentacionPrincipal.helperText = null
        val opcionesVisibles = listaPresentaciones.map {
            PresentacionesTiendaConfigManager.nombreVisible(it.nombre)
        }
        binding.editTextPresentacionPrincipal.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opcionesVisibles)
        )
        binding.editTextPresentacionPrincipal.threshold = Int.MAX_VALUE
        binding.editTextPresentacionPrincipal.inputType = InputType.TYPE_NULL
        binding.editTextPresentacionPrincipal.keyListener = null
        binding.editTextPresentacionPrincipal.isCursorVisible = false
        binding.editTextPresentacionPrincipal.isFocusable = false
        binding.editTextPresentacionPrincipal.isFocusableInTouchMode = false
        aplicarCierreDropdownAlRecibirFoco(binding.editTextPresentacionPrincipal)

        sincronizarPresentacionPrincipalSegunLista()
        if (listaPresentaciones.size > 1 && obtenerCanonicalPresentacionPrincipalActual().isBlank()) {
            listaPresentaciones.firstOrNull()?.let {
                asignarPresentacionPrincipal(it.nombre, seleccionManual = false)
            }
        }

        binding.root.findViewById<TextView>(R.id.tvAyudaSeleccionPrincipal)?.visibility =
            if (listaPresentaciones.size > 1) View.VISIBLE else View.GONE
        actualizarTarjetaPrincipalDeVentaCompacta()
        actualizarResumenTarjetasEdicion()
        refrescarPresentacionesVisualesCompactas()
    }

    private fun construirProductoFormData(validarPrecioPresentaciones: Boolean): ProductoFormData {
        val maxUnidades = productoOriginalParaAuditoria
            ?.unidadesPorPresentacionCompra
            ?.takeIf { it > 0 }
        val stockTotal = binding.editTextCantidad.text.toString().trim().toIntOrNull() ?: 0

        return ProductoFormData(
            nombre = binding.editTextNombre.text?.toString().orEmpty().trim(),
            vencimiento = binding.editTextVencimiento.text?.toString().orEmpty().trim(),
            categoria = binding.editTextCategoria.text?.toString().orEmpty().trim(),
            costoCompraTexto = binding.editTextCompra.text?.toString().orEmpty().trim(),
            requiereCostoCompraBase = false,
            unidadBase = binding.editTextUnidadBase.text?.toString().orEmpty().trim(),
            usaPresentaciones = binding.switchPresentaciones.isChecked,
            presentacionPrincipal = obtenerCanonicalPresentacionPrincipalActual().trim(),
            presentacionPrincipalElegidaManualmente = presentacionPrincipalElegidaManualmente,
            registroPorUnidades = binding.radioPorUnidades.isChecked,
            registroPorPaquetes = binding.radioPorPaquetes.isChecked,
            cantidadTexto = binding.editTextCantidad.text?.toString().orEmpty().trim(),
            stockMinimoTexto = binding.editTextStock.text?.toString().orEmpty().trim(),
            cantidadPresentacionesTexto = binding.editCantidadPresentacion.text?.toString().orEmpty().trim(),
            unidadesPorPresentacionTexto = binding.editUnidadesPorCaja.text?.toString().orEmpty().trim(),
            stockMinimoPresentacionTexto = binding.editTextStockPresentacion.text?.toString().orEmpty().trim(),
            listaPresentaciones = listaPresentaciones.toList(),
            stockTotalEnUnidadBase = stockTotal,
            maxUnidadesPorContenedor = maxUnidades,
            validarPrecioPresentaciones = validarPrecioPresentaciones,
            requiereVencimientoGeneral = !ocultarVencimientoGeneralPorLotes
        )
    }

    private fun validarPresentacionesAntesDeGuardarRefactor(): Boolean {
        if (!binding.switchPresentaciones.isChecked) return true

        val unidadBaseStr = binding.editTextUnidadBase.text.toString().trim().ifBlank { "unidades" }
        unidadBaseStr.equals("mL", ignoreCase = true) ||
                unidadBaseStr.equals("g", ignoreCase = true)

        return when (
            val resultado = ProductoFormValidator.validarPresentaciones(
                construirProductoFormData(validarPrecioPresentaciones = false)
            )
        ) {
            ProductoFormValidator.Resultado.Valido -> true
            ProductoFormValidator.Resultado.ListaPresentacionesVacia -> {
                Toast.makeText(this, "Agregue al menos una presentación", Toast.LENGTH_SHORT).show()
                false
            }
            ProductoFormValidator.Resultado.PresentacionPrincipalNoSeleccionada -> {
                binding.editTextPresentacionPrincipal.error = "Seleccione la presentación principal"
                posicionarScroll(binding.editTextPresentacionPrincipal)
                false
            }
            ProductoFormValidator.Resultado.PresentacionPrincipalDebeElegirseManualmente -> {
                binding.editTextPresentacionPrincipal.error = "Elige la presentación principal"
                posicionarScroll(binding.editTextPresentacionPrincipal)
                false
            }
            ProductoFormValidator.Resultado.PresentacionPrincipalNoExiste -> {
                binding.editTextPresentacionPrincipal.error = "La presentación principal no es válida"
                posicionarScroll(binding.editTextPresentacionPrincipal)
                false
            }
            is ProductoFormValidator.Resultado.PresentacionSuperaContenedor -> {
                val cantidadPresentacion = listaPresentaciones.firstOrNull {
                    PresentacionHelper.sonNombresEquivalentes(it.nombre, resultado.presentacion.nombre)
                }?.cantidad ?: resultado.maximo
                val nombreContenedor = obtenerNombrePresentacionTopeProducto(unidadBaseStr)
                Toast.makeText(
                    this,
                    "'${resultado.presentacion.nombre}' ($cantidadPresentacion $unidadBaseStr) no puede superar 1 $nombreContenedor (${resultado.maximo} $unidadBaseStr), porque esa es la presentación más alta registrada del producto.",
                    Toast.LENGTH_LONG
                ).show()
                false
            }
            is ProductoFormValidator.Resultado.PresentacionSuperaStockTotal -> {
                val cantidadPresentacion = listaPresentaciones.firstOrNull {
                    PresentacionHelper.sonNombresEquivalentes(it.nombre, resultado.presentacion.nombre)
                }?.cantidad ?: resultado.stockTotal
                Toast.makeText(
                    this,
                    "'${resultado.presentacion.nombre}' tiene $cantidadPresentacion $unidadBaseStr pero solo hay ${resultado.stockTotal} $unidadBaseStr en total.",
                    Toast.LENGTH_LONG
                ).show()
                false
            }
            else -> false
        }
    }

    private fun validarCamposRefactor(): Boolean {
        return when (
            val resultado = ProductoFormValidator.validarCamposBasicos(
                construirProductoFormData(validarPrecioPresentaciones = false)
            )
        ) {
            ProductoFormValidator.Resultado.Valido -> validarStockMinimoNoSupereCantidadDisponible()
            is ProductoFormValidator.Resultado.CampoVacio -> {
                when (resultado.campo) {
                    ProductoFormValidator.Campo.NOMBRE -> validarCampo(binding.editTextNombre, "Ingrese el nombre")
                    ProductoFormValidator.Campo.VENCIMIENTO -> validarCampo(binding.editTextVencimiento, "Seleccione vencimiento")
                    ProductoFormValidator.Campo.CATEGORIA -> validarCampo(binding.editTextCategoria, "Ingrese categoría")
                    ProductoFormValidator.Campo.COSTO_COMPRA -> validarCampo(binding.editTextCompra, "Ingrese el costo base referencial")
                    ProductoFormValidator.Campo.UNIDAD_BASE -> validarCampo(binding.editTextUnidadBase, "Ingrese unidad de stock")
                    ProductoFormValidator.Campo.CANTIDAD -> validarCampo(binding.editTextCantidad, "Ingrese cantidad")
                    ProductoFormValidator.Campo.STOCK_MINIMO -> validarCampo(binding.editTextStock, "Ingrese stock mínimo")
                    ProductoFormValidator.Campo.CANTIDAD_PRESENTACIONES -> validarCampo(binding.editCantidadPresentacion, "Ingrese cant. cajas")
                    ProductoFormValidator.Campo.UNIDADES_POR_PRESENTACION -> validarCampo(binding.editUnidadesPorCaja, "Ingrese unidades x caja")
                    ProductoFormValidator.Campo.STOCK_MINIMO_PRESENTACION -> validarCampo(binding.editTextStockPresentacion, "Ingrese stock mín. en cajas")
                    ProductoFormValidator.Campo.PRESENTACION_PRINCIPAL -> validarCampo(binding.editTextPresentacionPrincipal, "Seleccione la presentación principal")
                }
            }
            is ProductoFormValidator.Resultado.CampoInvalido -> {
                when (resultado.campo) {
                    ProductoFormValidator.Campo.UNIDAD_BASE ->
                        mostrarError(binding.editTextUnidadBase, "La unidad base solo debe llevar nombre, sin números")
                    ProductoFormValidator.Campo.COSTO_COMPRA ->
                        mostrarError(binding.editTextCompra, "Costo base referencial invalido")
                    ProductoFormValidator.Campo.CANTIDAD ->
                        mostrarError(binding.editTextCantidad, "Cantidad inválida")
                    ProductoFormValidator.Campo.STOCK_MINIMO ->
                        mostrarError(binding.editTextStock, "Stock mínimo inválido")
                    ProductoFormValidator.Campo.CANTIDAD_PRESENTACIONES ->
                        mostrarError(binding.editCantidadPresentacion, "Cajas inválidas")
                    ProductoFormValidator.Campo.UNIDADES_POR_PRESENTACION ->
                        mostrarError(binding.editUnidadesPorCaja, "Unidades x caja inválidas")
                    ProductoFormValidator.Campo.STOCK_MINIMO_PRESENTACION ->
                        mostrarError(binding.editTextStockPresentacion, "Stock mín. inválido")
                    else -> false
                }
            }
            else -> false
        }
    }

    private fun validarPresentacionesAntesDeGuardar(): Boolean {
        return validarPresentacionesAntesDeGuardarRefactor()
    }

    // ---------------------------------------------------------------------------------------------
    // OBTENER DATOS Y VALIDAR
    // ---------------------------------------------------------------------------------------------

    private fun productosEditTextGet(): MoldeProductos {
        val nombre = binding.editTextNombre.text.toString().trim()
        val vencimientoOriginal = binding.editTextVencimiento.text.toString().trim()
        val vencimientoLimpio = vencimientoOriginal.replace("/", "_")
        val categoria = binding.editTextCategoria.text.toString().trim()
        val unidadBase = binding.editTextUnidadBase.text.toString().trim()
        val presentacionesPersistidas = obtenerPresentacionesPersistidasSegunModo()
        val presentacionPrincipal = when {
            presentacionesPersistidas.isEmpty() -> obtenerCanonicalPresentacionPrincipalActual().trim()
            binding.switchPresentaciones.isChecked -> obtenerCanonicalPresentacionPrincipalActual()
                .trim()
                .ifBlank { presentacionesPersistidas.first().nombre }
            else -> obtenerPresentacionVentaPorDefectoSinMultiples(presentacionesPersistidas)
                ?.nombre
                .orEmpty()
        }

        val indiceBase = "${nombre}_${categoria}_${vencimientoLimpio}"
        val indice = sanitizarNombreArchivo(indiceBase)



        val unidadesPorPresentacionCompra = if (binding.radioPorPaquetes.isChecked) {
            binding.editUnidadesPorCaja.text.toString().trim().toIntOrNull() ?: 0
        } else {
            0
        }

        val esMlOg = unidadBase.equals("mL", ignoreCase = true) ||
                unidadBase.equals("g", ignoreCase = true)

        // Para mL/g: basePorUnidad = mL (o g) por frasco/paquete (viene de editUnidadesPorCaja)
        // Para otros: usar el valor de clase cargado desde Firebase
        val basePorUnidadFinal = when {
            esMlOg && binding.radioPorPaquetes.isChecked ->
                binding.editUnidadesPorCaja.text.toString().trim().toDoubleOrNull() ?: basePorUnidad
            else -> basePorUnidad
        }

        val lotesActuales = productoOriginalParaAuditoria?.lotes.orEmpty()
        val lotesRegistrados = lotesActuales.filterValues { it.numero.trim().isNotBlank() }
        val lotesSincronizados = if (!ocultarVencimientoGeneralPorLotes && lotesRegistrados.size == 1) {
            val claveLoteUnico = lotesRegistrados.keys.first()
            lotesActuales.mapValues { (clave, lote) ->
                if (clave == claveLoteUnico) {
                    lote.copy(vencimiento = vencimientoOriginal)
                } else {
                    lote
                }
            }
        } else {
            lotesActuales
        }

        // Normalización de Stock
        val cantidadFinal: String
        val stockMinimoFinal: String
        val stockMinimoContenedores: Int
        val unidadStockMinimo: String

        if (binding.radioPorPaquetes.isChecked) {
            val contenedores = binding.editCantidadPresentacion.text.toString().toIntOrNull() ?: 0
            val valorPorContenedor = binding.editUnidadesPorCaja.text.toString().toIntOrNull() ?: 0
            val stockMinContenedores = binding.editTextStockPresentacion.text.toString().toIntOrNull() ?: 0

            val totalEnUnidadBase = contenedores * valorPorContenedor
            val totalMinEnUnidadBase = stockMinContenedores * valorPorContenedor

            // Para mL/g: los valores ya están en mL/g (editUnidadesPorCaja = mL por frasco)
            // Para otros con basePorUnidad: aplicar conversión de 3 niveles
            if (!esMlOg && basePorUnidadFinal > 0.0) {
                cantidadFinal = (totalEnUnidadBase * basePorUnidadFinal).toInt().toString()
                stockMinimoFinal = (totalMinEnUnidadBase * basePorUnidadFinal).toInt().toString()
            } else {
                cantidadFinal = totalEnUnidadBase.toString()
                stockMinimoFinal = totalMinEnUnidadBase.toString()
            }

            stockMinimoContenedores = stockMinContenedores
            unidadStockMinimo = PresentacionesTiendaConfigManager.nombreVisible(presentacionPrincipal)
        } else {
            cantidadFinal = binding.editTextCantidad.text.toString().trim()
            stockMinimoFinal = binding.editTextStock.text.toString().trim()
            stockMinimoContenedores = 0
            unidadStockMinimo = unidadBase
        }

        return MoldeProductos().apply {
            this.nombre = nombre
            this.codigo = codigoOriginal
            this.vencimiento = vencimientoOriginal
            this.categoria = categoria
            this.preciodecompra = binding.editTextCompra.text.toString().trim().replace(",", ".")
            this.cantidadinicial = cantidadFinal
            this.stockminimo = stockMinimoFinal
            this.stockMinimoContenedores = stockMinimoContenedores
            this.unidadStockMinimo = unidadStockMinimo
            this.unidadbase = unidadBaseOriginalInmutable.ifBlank { unidadBase }
            this.tipoBaseInventario = PresentacionRules.normalizarTipoBaseInventario(
                tipoBaseInventarioOriginal,
                this.unidadbase
            )
            this.presentacionprincipal = presentacionPrincipal
            this.requierereceta = binding.swichtreceta.isChecked
            this.estadodelproducto = binding.switchestadodelproducto.isChecked
            this.indice = indice
            this.tienePresentaciones = binding.switchPresentaciones.isChecked
            this.presentaciones = ArrayList(presentacionesPersistidas)
            this.unidadesPorPresentacionCompra = unidadesPorPresentacionCompra
            this.basePorUnidad = basePorUnidadFinal
            this.imagenUrl = imagenUrlActual
            this.loteConsumoSeleccionado = loteConsumoSeleccionadoActual
            this.loteConsumoSeleccionManual = loteConsumoSeleccionManualActual
            this.lotes = lotesSincronizados
        }
    }

    private fun validarCampos(): Boolean {
        return validarCamposRefactor()
    }

    private fun validarCampo(editText: View?, mensaje: String): Boolean {
        if (editText is EditText) {
            if (editText.text.isNullOrBlank()) {
                editText.error = mensaje
                posicionarScroll(editText)
                return false
            }
        }
        return true
    }

    private fun mostrarError(editText: View?, mensaje: String): Boolean {
        if (editText is EditText) {
            editText.error = mensaje
            posicionarScroll(editText)
        }
        return false
    }

    // ---------------------------------------------------------------------------------------------
    // GUARDAR
    // ---------------------------------------------------------------------------------------------

    fun GuardarCambiosClickBoton(view: View) {
        guardarCambiosDesdeFormulario()
    }

    private fun mostrarResumenConfirmacionEditar() {
        val productoEditado = productosEditTextGet()
        val cambiosVisibles = construirCambiosVisiblesConfirmacion(
            productoOriginalParaAuditoria,
            productoEditado
        )

        if (cambiosVisibles.isEmpty()) {
            Toast.makeText(this, "No hay cambios para guardar", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_confirmacion_cambios_producto, null)
        dialogView.findViewById<TextView>(R.id.tvResumenProductoConfirmacion).text =
            construirResumenBreveProductoConfirmacion(productoEditado)
        dialogView.findViewById<TextView>(R.id.tvCambiosHechosConfirmacion).text =
            construirTextoCambiosConfirmacion(cambiosVisibles)

        MaterialAlertDialogBuilder(this)
            .setTitle("Confirmar cambios")
            .setView(dialogView)
            .setPositiveButton("Confirmar y guardar") { _, _ ->
                mostrarCarga(true, titulo = "Guardando cambios", mensaje = "Actualizando el producto...")
                subirImagenProducto(
                    indice = productoEditado.indice,
                    onSuccess = { url ->
                        productoEditado.imagenUrl = url
                        verificarSiExisteCategoriaNueva(productoEditado)
                    },
                    onFailure = { error ->
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                        mostrarCarga(false)
                    }
                )
            }
            .setNegativeButton("Cancelar") { _, _ ->
                accionDespuesDeGuardarProducto = null
            }
            .setOnCancelListener {
                accionDespuesDeGuardarProducto = null
            }
            .show()
    }

    private fun construirResumenBreveProductoConfirmacion(producto: MoldeProductos): CharSequence {
        val presentacion = when {
            producto.presentacionprincipal.isNotBlank() ->
                PresentacionesTiendaConfigManager.nombreVisible(producto.presentacionprincipal)
            producto.tienePresentaciones && producto.presentaciones.isNotEmpty() ->
                PresentacionesTiendaConfigManager.nombreVisible(producto.presentaciones.first().nombre)
            producto.unidadbase.isNotBlank() ->
                "Unidad base (${producto.unidadbase})"
            else -> "Sin presentaci\u00f3n"
        }

        val stockMinimo = producto.stockminimo.trim().toIntOrNull() ?: 0

        return SpannableStringBuilder().apply {
            agregarLineaResumenConfirmacion("Producto", producto.nombre.ifBlank { "(sin nombre)" })
            agregarLineaResumenConfirmacion("Presentaci\u00f3n", presentacion)
            agregarLineaResumenConfirmacion(
                "Stock m\u00ednimo",
                if (stockMinimo == 0) "Sin avisar stock m\u00ednimo" else stockMinimo.toString()
            )
            agregarLineaResumenConfirmacion("Disponible", siNo(producto.estadodelproducto))
            agregarLineaResumenConfirmacion("Requiere receta", siNo(producto.requierereceta), appendNewLine = false)
        }
    }

    private fun construirCambiosVisiblesConfirmacion(
        anterior: MoldeProductos?,
        actual: MoldeProductos
    ): List<CambioVisibleConfirmacion> {
        val cambiosCrudos = construirCambiosAntesDespuesProducto(anterior, actual)
        val ordenPreferido = listOf(
            "nombre",
            "presentacionPrincipal",
            "stockMinimoUnidades",
            "stockTotalUnidades",
            "estadoProducto",
            "requiereReceta",
            "categoria",
            "vencimiento",
            "precioCompra",
            "tienePresentaciones"
        )

        val cambiosVisibles = mutableListOf<CambioVisibleConfirmacion>()
        val consumidos = mutableSetOf<String>()

        ordenPreferido.forEach { campo ->
            val detalle = cambiosCrudos[campo] ?: return@forEach
            formatearCambioVisibleConfirmacion(
                campo = campo,
                antes = detalle["antes"],
                despues = detalle["despues"]
            )?.let {
                cambiosVisibles += it
                consumidos += campo
            }
        }

        cambiosCrudos.forEach { (campo, detalle) ->
            if (campo in consumidos) return@forEach
            formatearCambioVisibleConfirmacion(
                campo = campo,
                antes = detalle["antes"],
                despues = detalle["despues"]
            )?.let(cambiosVisibles::add)
        }

        construirCambioFotoConfirmacion(anterior, actual)?.let(cambiosVisibles::add)
        cambiosVisibles += construirCambiosPresentacionesConfirmacion(
            presentacionesOriginales = anterior?.presentaciones.orEmpty(),
            presentacionesActuales = actual.presentaciones.orEmpty()
        )

        return cambiosVisibles
    }

    private fun formatearCambioVisibleConfirmacion(
        campo: String,
        antes: Any?,
        despues: Any?
    ): CambioVisibleConfirmacion? {
        val etiqueta = when (campo) {
            "nombre" -> "Producto"
            "categoria" -> "Categor\u00eda"
            "vencimiento" -> "Vencimiento"
            "precioCompra" -> "Costo base referencial"
            "stockTotalUnidades" -> "Cantidad disponible"
            "stockMinimoUnidades" -> "Stock m\u00ednimo"
            "unidadBase" -> "Unidad base"
            "presentacionPrincipal" -> "Presentaci\u00f3n principal"
            "requiereReceta" -> "Requiere receta"
            "estadoProducto" -> "Disponible"
            "tienePresentaciones" -> "Usa presentaciones"
            "presentaciones", "imagenUrl", "tipoBaseInventario", "unidadesPorPresentacionCompra" -> return null
            "indice" -> return null
            else -> campo
        }

        val antesTexto = when (campo) {
            "precioCompra" -> formatearCostoConfirmacion(antes)
            "stockMinimoUnidades" -> formatearStockMinimoConfirmacion(antes)
            "presentacionPrincipal" -> formatearPresentacionPrincipalConfirmacion(antes)
            "presentaciones" -> formatearListaPresentacionesConfirmacion(antes)
            "tipoBaseInventario" -> formatearTipoBaseConfirmacion(antes)
            "requiereReceta", "estadoProducto", "tienePresentaciones" -> formatearBooleanoConfirmacion(antes)
            "imagenUrl" -> formatearImagenConfirmacion(antes)
            else -> valorTextoConfirmacion(antes)
        }

        val despuesTexto = when (campo) {
            "precioCompra" -> formatearCostoConfirmacion(despues)
            "stockMinimoUnidades" -> formatearStockMinimoConfirmacion(despues)
            "presentacionPrincipal" -> formatearPresentacionPrincipalConfirmacion(despues)
            "presentaciones" -> formatearListaPresentacionesConfirmacion(despues)
            "tipoBaseInventario" -> formatearTipoBaseConfirmacion(despues)
            "requiereReceta", "estadoProducto", "tienePresentaciones" -> formatearBooleanoConfirmacion(despues)
            "imagenUrl" -> formatearImagenConfirmacion(despues)
            else -> valorTextoConfirmacion(despues)
        }

        return CambioVisibleConfirmacion(etiqueta, antesTexto, despuesTexto)
    }

    private fun formatearCostoConfirmacion(valor: Any?): String {
        val numero = valor?.toString()?.replace(",", ".")?.toDoubleOrNull()
        return if (numero != null && numero > 0) {
            MonedaHelper.formatear(numero)
        } else {
            valorTextoConfirmacion(valor)
        }
    }

    private fun formatearStockMinimoConfirmacion(valor: Any?): String {
        val numero = valor?.toString()?.trim()?.toIntOrNull()
        return when {
            numero == null -> valorTextoConfirmacion(valor)
            numero == 0 -> "Sin avisar stock m\u00ednimo"
            else -> numero.toString()
        }
    }

    private fun formatearPresentacionPrincipalConfirmacion(valor: Any?): String {
        val texto = valor?.toString()?.trim().orEmpty()
        return if (texto.isBlank() || texto == "(vacio)") {
            "Sin presentaci\u00f3n principal"
        } else {
            PresentacionesTiendaConfigManager.nombreVisible(texto)
        }
    }

    private fun formatearTipoBaseConfirmacion(valor: Any?): String {
        val texto = valor?.toString()?.trim().orEmpty()
        if (texto.isBlank() || texto == "(vacio)") return "Sin tipo base"
        val normalizado = texto.lowercase(Locale.getDefault())
        return normalizado.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }

    private fun formatearBooleanoConfirmacion(valor: Any?): String {
        return siNo(valor as? Boolean == true)
    }

    private fun formatearImagenConfirmacion(valor: Any?): String {
        val texto = valor?.toString()?.trim().orEmpty()
        return if (texto.isBlank() || texto == "(vacio)") "Sin imagen" else "Imagen configurada"
    }

    private fun formatearListaPresentacionesConfirmacion(valor: Any?): String {
        val lista = (valor as? List<*>)?.mapNotNull { item ->
            val mapa = item as? Map<*, *> ?: return@mapNotNull item?.toString()
            val nombre = PresentacionesTiendaConfigManager.nombreVisible(
                mapa["nombre"]?.toString().orEmpty()
            ).ifBlank { "Presentaci\u00f3n" }
            val cantidad = mapa["cantidad"]?.toString().orEmpty()
            if (cantidad.isBlank()) nombre else "$nombre ($cantidad)"
        }.orEmpty()

        if (lista.isEmpty()) return "Sin presentaciones"
        return if (lista.size <= 3) {
            lista.joinToString(", ")
        } else {
            lista.take(3).joinToString(", ") + " +${lista.size - 3} m\u00e1s"
        }
    }

    private fun valorTextoConfirmacion(valor: Any?): String {
        val texto = valor?.toString()?.trim().orEmpty()
        return if (texto.isBlank() || texto == "(vacio)") "Vac\u00edo" else texto
    }

    private fun siNo(valor: Boolean): String = if (valor) "S\u00ed" else "No"

    private fun construirCambioFotoConfirmacion(
        anterior: MoldeProductos?,
        actual: MoldeProductos
    ): CambioVisibleConfirmacion? {
        val fotoActualizadaLocalmente = imagenUri != null
        val imagenAntes = anterior?.imagenUrl?.trim().orEmpty()
        val imagenDespues = actual.imagenUrl.trim()
        val huboCambio = fotoActualizadaLocalmente || imagenAntes != imagenDespues
        if (!huboCambio) return null

        return CambioVisibleConfirmacion(
            etiqueta = "Foto",
            antes = if (imagenAntes.isBlank()) "Sin imagen" else "Imagen actual",
            despues = if (fotoActualizadaLocalmente || imagenDespues.isNotBlank()) "Actualizada" else "Sin imagen"
        )
    }

    private fun construirCambiosPresentacionesConfirmacion(
        presentacionesOriginales: List<PresentacionProducto>,
        presentacionesActuales: List<PresentacionProducto>
    ): List<CambioVisibleConfirmacion> {
        if (presentacionesOriginales.isEmpty() && presentacionesActuales.isEmpty()) return emptyList()

        val cambios = mutableListOf<CambioVisibleConfirmacion>()
        val originalesPorClave = presentacionesOriginales
            .associateBy { clavePresentacionConfirmacion(it.nombre) }
            .toMutableMap()
        val actualesPorClave = presentacionesActuales
            .associateBy { clavePresentacionConfirmacion(it.nombre) }
            .toMutableMap()

        val clavesComunes = originalesPorClave.keys.intersect(actualesPorClave.keys).sorted()
        clavesComunes.forEach { clave ->
            val original = originalesPorClave.remove(clave) ?: return@forEach
            val actual = actualesPorClave.remove(clave) ?: return@forEach
            cambios += compararPresentacionConfirmacion(
                original = original,
                actual = actual,
                etiquetaBase = "Presentaci\u00f3n ${nombrePresentacionConfirmacion(actual)}"
            )
        }

        val originalesRestantes = originalesPorClave.values.toMutableList()
        val actualesRestantes = actualesPorClave.values.toMutableList()
        val usadosActuales = mutableSetOf<Int>()

        originalesRestantes.forEach { original ->
            val indicePareja = actualesRestantes.indexOfFirst { actual ->
                esPosibleMismaPresentacion(original, actual) &&
                    actualesRestantes.indexOf(actual) !in usadosActuales
            }

            if (indicePareja >= 0) {
                usadosActuales += indicePareja
                val actual = actualesRestantes[indicePareja]
                val nombreAntes = nombrePresentacionConfirmacion(original)
                val nombreDespues = nombrePresentacionConfirmacion(actual)

                if (!PresentacionHelper.sonNombresEquivalentes(original.nombre, actual.nombre)) {
                    cambios += CambioVisibleConfirmacion(
                        etiqueta = "Presentaci\u00f3n $nombreAntes - nombre",
                        antes = nombreAntes,
                        despues = nombreDespues
                    )
                }

                cambios += compararPresentacionConfirmacion(
                    original = original,
                    actual = actual,
                    etiquetaBase = "Presentaci\u00f3n $nombreDespues"
                )
            } else {
                cambios += CambioVisibleConfirmacion(
                    etiqueta = "Presentaci\u00f3n eliminada",
                    antes = detallePresentacionConfirmacion(original),
                    despues = "Eliminada"
                )
            }
        }

        actualesRestantes.forEachIndexed { index, actual ->
            if (index in usadosActuales) return@forEachIndexed
            cambios += CambioVisibleConfirmacion(
                etiqueta = "Presentaci\u00f3n agregada",
                antes = "No exist\u00eda",
                despues = detallePresentacionConfirmacion(actual)
            )
        }

        return cambios
    }

    private fun compararPresentacionConfirmacion(
        original: PresentacionProducto,
        actual: PresentacionProducto,
        etiquetaBase: String
    ): List<CambioVisibleConfirmacion> {
        val cambios = mutableListOf<CambioVisibleConfirmacion>()

        if (original.cantidad != actual.cantidad) {
            cambios += CambioVisibleConfirmacion(
                etiqueta = "$etiquetaBase - cantidad",
                antes = original.cantidad.toString(),
                despues = actual.cantidad.toString()
            )
        }

        if (kotlin.math.abs(original.precioventa - actual.precioventa) > 0.009) {
            cambios += CambioVisibleConfirmacion(
                etiqueta = "$etiquetaBase - precio",
                antes = MonedaHelper.formatear(original.precioventa),
                despues = MonedaHelper.formatear(actual.precioventa)
            )
        }

        return cambios
    }

    private fun esPosibleMismaPresentacion(
        original: PresentacionProducto,
        actual: PresentacionProducto
    ): Boolean {
        val cantidadIgual = original.cantidad == actual.cantidad
        val precioSimilar = kotlin.math.abs(original.precioventa - actual.precioventa) <= 0.009
        return cantidadIgual || precioSimilar || similar(
            nombrePresentacionConfirmacion(original),
            nombrePresentacionConfirmacion(actual),
            0.55
        )
    }

    private fun detallePresentacionConfirmacion(presentacion: PresentacionProducto): String {
        val nombre = nombrePresentacionConfirmacion(presentacion)
        val precioTexto = if (presentacion.precioventa > 0.0) {
            MonedaHelper.formatear(presentacion.precioventa)
        } else {
            "sin precio"
        }
        return "$nombre (${presentacion.cantidad} unid., $precioTexto)"
    }

    private fun nombrePresentacionConfirmacion(presentacion: PresentacionProducto): String {
        return PresentacionesTiendaConfigManager.nombreVisible(presentacion.nombre)
            .ifBlank { "Presentaci\u00f3n" }
    }

    private fun clavePresentacionConfirmacion(nombre: String): String {
        return PresentacionesTiendaConfigManager.nombreVisible(nombre)
            .trim()
            .lowercase(Locale.getDefault())
    }

    private fun construirTextoCambiosConfirmacion(
        cambiosVisibles: List<CambioVisibleConfirmacion>
    ): CharSequence {
        val colorEtiqueta = "#111827".toColorInt()
        val colorAntes = "#6B7280".toColorInt()
        val colorFlecha = "#9CA3AF".toColorInt()
        val colorDespues = "#166534".toColorInt()
        val colorBullet = "#0F766E".toColorInt()

        return SpannableStringBuilder().apply {
            cambiosVisibles.forEachIndexed { index, cambio ->
                agregarSegmentoConfirmacion("\u2022 ", colorBullet, negrita = true)
                agregarSegmentoConfirmacion("${cambio.etiqueta}: ", colorEtiqueta, negrita = true)
                agregarSegmentoConfirmacion(cambio.antes, colorAntes)
                agregarSegmentoConfirmacion(" \u2192 ", colorFlecha)
                agregarSegmentoConfirmacion(cambio.despues, colorDespues, negrita = true)
                if (index != cambiosVisibles.lastIndex) append("\n\n")
            }
        }
    }

    private fun SpannableStringBuilder.agregarLineaResumenConfirmacion(
        etiqueta: String,
        valor: String,
        appendNewLine: Boolean = true
    ) {
        agregarSegmentoConfirmacion("$etiqueta: ", "#4B5563".toColorInt(), negrita = true)
        agregarSegmentoConfirmacion(valor, "#111827".toColorInt(), negrita = true)
        if (appendNewLine) append("\n")
    }

    private fun SpannableStringBuilder.agregarSegmentoConfirmacion(
        texto: String,
        color: Int,
        negrita: Boolean = false
    ) {
        val inicio = length
        append(texto)
        setSpan(ForegroundColorSpan(color), inicio, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (negrita) {
            setSpan(StyleSpan(Typeface.BOLD), inicio, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun verificarSiExisteCategoriaNueva(productoEditado: MoldeProductos) {
        val dbRef = FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_CATEGORIAS)

        dbRef.get()
            .addOnSuccessListener { snapshot ->
                val categoriasExistentes = snapshot.children.mapNotNull { categoriaSnap ->
                    val nombre = categoriaSnap.child("nombre")
                        .getValue(String::class.java)
                        .orEmpty()
                        .trim()
                    if (nombre.isBlank()) return@mapNotNull null

                    CategoriaProductos(
                        id = categoriaSnap.child("id").getValue(String::class.java).orEmpty()
                            .ifBlank { categoriaSnap.key.orEmpty() },
                        nombre = nombre,
                        indice = categoriaSnap.child("indice")
                            .getValue(String::class.java)
                            .orEmpty()
                    )
                }

                ProductUtils.resolverCategoriaExistente(
                    productoEditado.categoria,
                    categoriasExistentes.map { it.nombre }
                )?.let { categoriaCanonica ->
                    productoEditado.categoria = categoriaCanonica
                }

                val indiceCategoriaCanonico = ProductUtils.normalizarClaveCategoria(productoEditado.categoria)
                val categoriaExistente = categoriasExistentes.any { categoria ->
                    ProductUtils.normalizarClaveCategoria(categoria.indice.orEmpty().ifBlank { categoria.nombre }) == indiceCategoriaCanonico
                }

                if (!categoriaExistente) {
                    val nuevoID = dbRef.push().key ?: run {
                        mostrarCarga(false)
                        Toast.makeText(
                            this,
                            "No se pudo generar ID de categoría",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addOnSuccessListener
                    }

                    val nuevaCategoria = CategoriaProductos(
                        id = nuevoID,
                        nombre = productoEditado.categoria,
                        indice = indiceCategoriaCanonico
                    )

                    dbRef.child(nuevoID).setValue(nuevaCategoria)
                        .addOnSuccessListener {
                            listaCategoria.add(nuevaCategoria)
                            actualizarAutoCompleteCategorias()
                            verificarNombreProductoFirebase(productoEditado)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Error al crear categoría: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            mostrarCarga(false)
                        }
                } else {
                    verificarNombreProductoFirebase(productoEditado)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al verificar categoría: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                mostrarCarga(false)
            }
        return

        val indiceCategoria = productoEditado.categoria.lowercase().trim()

        dbRef.orderByChild("indice").equalTo(indiceCategoria).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    val nuevoID = dbRef.push().key ?: run {
                        mostrarCarga(false)
                        Toast.makeText(
                            this,
                            "No se pudo generar ID de categoría",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addOnSuccessListener
                    }

                    val nuevaCategoria = CategoriaProductos(
                        id = nuevoID,
                        nombre = productoEditado.categoria,
                        indice = indiceCategoria
                    )

                    dbRef.child(nuevoID).setValue(nuevaCategoria)
                        .addOnSuccessListener {
                            listaCategoria.add(nuevaCategoria)
                            verificarNombreProductoFirebase(productoEditado)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Error al crear categoría: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            mostrarCarga(false)
                        }
                } else {
                    verificarNombreProductoFirebase(productoEditado)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al verificar categoría: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                mostrarCarga(false)
            }
    }

    private fun verificarNombreProductoFirebase(productoEditado: MoldeProductos) {
        val dbRef = FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_NOMBRES)

        dbRef.orderByChild("nombre").equalTo(productoEditado.nombre).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    verificarProductosEnLaBaseDatos(productoEditado)
                } else {
                    val nuevoID = dbRef.push().key ?: run {
                        mostrarCarga(false)
                        Toast.makeText(
                            this,
                            "No se pudo generar ID de nombre",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addOnSuccessListener
                    }

                    val nuevoNombre = NombreProductos(nuevoID, productoEditado.nombre)

                    dbRef.child(nuevoID).setValue(nuevoNombre)
                        .addOnSuccessListener {
                            listaNombreAutoComplete.add(nuevoNombre)
                            verificarProductosEnLaBaseDatos(productoEditado)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Error al guardar nombre: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            mostrarCarga(false)
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al verificar nombre: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                mostrarCarga(false)
            }
    }

    private fun validarStockMinimoNoSupereCantidadDisponible(): Boolean {
        val esPorPresentacion = binding.radioPorPaquetes.isChecked
        val cantidadDisponibleActual = if (esPorPresentacion) {
            binding.editCantidadPresentacion.text?.toString()?.trim()?.toIntOrNull() ?: 0
        } else {
            binding.editTextCantidad.text?.toString()?.trim()?.toIntOrNull() ?: 0
        }
        val stockMinimoActual = if (esPorPresentacion) {
            binding.editTextStockPresentacion.text?.toString()?.trim()?.toIntOrNull()
        } else {
            binding.editTextStock.text?.toString()?.trim()?.toIntOrNull()
        } ?: 0

        if (stockMinimoActual <= cantidadDisponibleActual) {
            return true
        }

        val layout = if (esPorPresentacion) {
            binding.editTextStockPresentacion.parent?.parent as? TextInputLayout
        } else {
            binding.editTextStock.parent?.parent as? TextInputLayout
        }
        val input = if (esPorPresentacion) binding.editTextStockPresentacion else binding.editTextStock
        val mensaje = "El stock mínimo no puede ser mayor que la cantidad disponible actual ($cantidadDisponibleActual)"

        mostrarErrorTiempoRealStockMinimo(layout, input, mensaje, esPorPresentacion)
        posicionarScroll(input)
        return false
    }
    private fun verificarProductosEnLaBaseDatos(producto: MoldeProductos) {
        val dbRef = FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRODUCTOS)

        dbRef.get().addOnSuccessListener { snapshot ->
            var productoIgual: MoldeProductos? = null
            var productoSimilar: MoldeProductos? = null

            for (child in snapshot.children) {
                val keyActual = child.key ?: continue
                if (keyActual == indiceOriginal) continue

                val existente = child.toMoldeProductos()
                if (existente != null) {
                    if (existente.nombre.lowercase().trim() == producto.nombre.lowercase().trim()) {
                        productoIgual = existente
                        break
                    }

                    if (similar(existente.nombre, producto.nombre)) {
                        productoSimilar = existente
                    }
                }
            }

            when {
                productoIgual != null -> {
                    mostrarDialogo(
                        "Ya existe otro producto con el mismo nombre: ${productoIgual.nombre}. ¿Desea continuar?",
                        producto
                    )
                }

                productoSimilar != null -> {
                    mostrarDialogo(
                        "Ya existe otro producto con nombre similar: ${productoSimilar.nombre}. ¿Desea continuar?",
                        producto
                    )
                }

                else -> {
                    guardarProductoEditado(producto)
                }
            }
        }.addOnFailureListener { e ->
            Toast.makeText(
                this,
                "Error al verificar productos: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            mostrarCarga(false)
        }
    }

    private fun mostrarDialogo(mensaje: String, producto: MoldeProductos) {
        mostrarCarga(false)

        AlertDialog.Builder(this)
            .setTitle("Verificación de producto")
            .setMessage(mensaje)
            .setPositiveButton("Continuar") { _, _ ->
                mostrarCarga(
                    true,
                    titulo = "Guardando cambios",
                    mensaje = "Actualizando el producto..."
                )
                guardarProductoEditado(producto)
            }
            .setNegativeButton("Cancelar") { _, _ ->
                mostrarCarga(false)
            }
            .setOnCancelListener {
                mostrarCarga(false)
            }
            .show()
    }

    private fun guardarProductoEditado(producto: MoldeProductos) {
        if (!esTipoBaseInmutableConsistente(producto)) {
            mostrarCarga(false)
            restaurarTipoBaseInventarioOriginalEnUi()
            Toast.makeText(this, getString(R.string.tipo_base_inmutable_error), Toast.LENGTH_LONG).show()
            return
        }

        val db = FirebaseDatabase.getInstance().reference

        val indiceNuevo = producto.indice
        val nombreBusquedaNuevo = sanitizarNombreArchivo(producto.nombre)
        val updates = hashMapOf<String, Any?>()

        updates["$PATH_INVENTARIO/$PATH_PRODUCTOS/$indiceNuevo"] = producto
        updates["$PATH_INVENTARIO/$PATH_BUSQUEDA/$nombreBusquedaNuevo/$indiceNuevo"] = true

        if (indiceOriginal != indiceNuevo) {
            updates["$PATH_INVENTARIO/$PATH_PRODUCTOS/$indiceOriginal"] = null

            val nombreTag = binding.editTextNombre.tag?.toString() ?: ""
            if (nombreTag.isNotBlank()) {
                val nombreBusquedaOriginal = sanitizarNombreArchivo(nombreTag)
                updates["$PATH_INVENTARIO/$PATH_BUSQUEDA/$nombreBusquedaOriginal/$indiceOriginal"] = null
            }
        }

        db.updateChildren(updates)
            .addOnSuccessListener {
                val stockAntesEdicion = binding.editTextCantidad.tag?.toString()?.toIntOrNull()
                val stockDespuesEdicion = producto.cantidadinicial.toIntOrNull() ?: 0
                val cambiosAntesDespues = construirCambiosAntesDespuesProducto(
                    anterior = productoOriginalParaAuditoria,
                    actual = producto
                )

                val extraMovimiento = mutableMapOf<String, Any>(
                    "nombreProducto" to producto.nombre,
                    "categoria" to producto.categoria,
                    "indiceAnterior" to indiceOriginal,
                    "indiceNuevo" to indiceNuevo
                )
                if (cambiosAntesDespues.isNotEmpty()) {
                    extraMovimiento["cantidadCambios"] = cambiosAntesDespues.size
                    extraMovimiento["cambiosAntesDespues"] = cambiosAntesDespues
                }

                MovimientoInventarioLogger.registrarConSesion(
                    context = this,
                    indiceProducto = indiceNuevo,
                    tipo = "producto_editado",
                    titulo = "Producto editado",
                    descripcion = "Se actualizó ${producto.nombre}",
                    stockAntes = stockAntesEdicion,
                    stockDespues = stockDespuesEdicion,
                    motivo = "Edición manual de producto",
                    referenciaId = indiceNuevo,
                    extra = extraMovimiento
                )

                MovimientoLogger.registrarConSesion(
                    context = this,
                    tipo = "editar_producto",
                    modulo = "inventario",
                    titulo = "Producto editado",
                    descripcion = "Se actualizó ${producto.nombre}",
                    referenciaId = indiceNuevo,
                    extra = extraMovimiento
                )

                imagenUrlActual = producto.imagenUrl ?: imagenUrlActual
                imagenUri = null
                indiceOriginal = indiceNuevo
                productoOriginalParaAuditoria = producto.copiaParaAuditoria()
                binding.editTextNombre.tag = producto.nombre
                binding.editTextCantidad.tag = producto.cantidadinicial

                guardarSugerenciasGlobales()
                mostrarCarga(false)

                marcarEstadoInicialFormulario()
                accionDespuesDeGuardarProducto?.invoke()
                accionDespuesDeGuardarProducto = null

                Toast.makeText(this, "Producto guardado correctamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                accionDespuesDeGuardarProducto = null
                Toast.makeText(
                    this,
                    "Error al guardar producto: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                mostrarCarga(false)
            }
    }

    // ---------------------------------------------------------------------------------------------
    // ELIMINAR
    // ---------------------------------------------------------------------------------------------

    fun borrarProductoClick(view: View) {
        val dialog = BottomSheetDialog(this)
        val viewSheet = layoutInflater.inflate(R.layout.layout_eliminar_producto_confirmar, null)
        dialog.setContentView(viewSheet)
        configurarBackdropBottomSheet(dialog)

        val btnConfirmar = viewSheet.findViewById<MaterialButton>(R.id.btnConfirmarEliminar)
        val btnCancelar = viewSheet.findViewById<MaterialButton>(R.id.btnCancelarEliminar)

        btnConfirmar.setOnClickListener {
            dialog.dismiss()
            eliminarProducto()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun eliminarProducto() {
        mostrarCarga(
            true,
            titulo = "Eliminando producto",
            mensaje = "Aplicando cambios..."
        )

        val db = FirebaseDatabase.getInstance().reference

        val tagNombre = binding.editTextNombre.tag?.toString()
        val nombreBusqueda = if (!tagNombre.isNullOrBlank()) {
            sanitizarNombreArchivo(tagNombre)
        } else {
            sanitizarNombreArchivo(binding.editTextNombre.text.toString())
        }

        val updates = hashMapOf<String, Any?>(
            "$PATH_INVENTARIO/$PATH_PRODUCTOS/$indiceOriginal" to null
        )

        if (nombreBusqueda.isNotBlank()) {
            updates["$PATH_INVENTARIO/$PATH_BUSQUEDA/$nombreBusqueda/$indiceOriginal"] = null
        }

        db.updateChildren(updates)
            .addOnSuccessListener {
                val stockActual = binding.editTextCantidad.text.toString().trim().toIntOrNull() ?: 0
                MovimientoInventarioLogger.registrarConSesion(
                    context = this,
                    indiceProducto = indiceOriginal,
                    tipo = "producto_eliminado",
                    titulo = "Producto eliminado",
                    descripcion = "Se eliminó ${binding.editTextNombre.text.toString().trim()}",
                    cantidad = stockActual,
                    stockAntes = stockActual,
                    stockDespues = 0,
                    motivo = "Eliminación manual de producto",
                    referenciaId = indiceOriginal,
                    extra = mapOf(
                        "nombreProducto" to binding.editTextNombre.text.toString().trim(),
                        "categoria" to binding.editTextCategoria.text.toString().trim()
                    )
                )

                MovimientoLogger.registrarConSesion(
                    context = this,
                    tipo = "eliminar_producto",
                    modulo = "inventario",
                    titulo = "Producto eliminado",
                    descripcion = "Se eliminó ${binding.editTextNombre.text.toString().trim()}",
                    referenciaId = indiceOriginal,
                    extra = mapOf(
                        "nombreProducto" to binding.editTextNombre.text.toString().trim(),
                        "categoria" to binding.editTextCategoria.text.toString().trim()
                    )
                )

                Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_LONG).show()
                mostrarCarga(false)
            }
    }

    // ---------------------------------------------------------------------------------------------
    // UTILIDADES
    // ---------------------------------------------------------------------------------------------

    private fun MoldeProductos.copiaParaAuditoria(): MoldeProductos {
        return this.copy(
            presentaciones = this.presentaciones
                .map { it.copy() }
                .toMutableList()
        )
    }

    private fun MoldeProductos.toAuditoriaMap(): Map<String, Any?> {
        val presentacionesNormalizadas = presentaciones
            .map {
                mapOf(
                    "nombre" to it.nombre.trim(),
                    "cantidad" to it.cantidad,
                    "precioVenta" to String.format(Locale.US, "%.2f", it.precioventa)
                )
            }
            .sortedBy { (it["nombre"] as? String).orEmpty() }

        return linkedMapOf(
            "nombre" to nombre.trim(),
            "categoria" to categoria.trim(),
            "vencimiento" to vencimiento.trim(),
            "precioCompra" to preciodecompra.trim(),
            "stockTotalUnidades" to cantidadinicial.trim(),
            "stockMinimoUnidades" to stockminimo.trim(),
            "unidadBase" to unidadbase.trim(),
            "tipoBaseInventario" to PresentacionRules.normalizarTipoBaseInventario(
                tipoBaseInventario,
                unidadbase
            ),
            "unidadesPorPresentacionCompra" to unidadesPorPresentacionCompra,
            "presentacionPrincipal" to presentacionprincipal.trim(),
            "requiereReceta" to requierereceta,
            "estadoProducto" to estadodelproducto,
            "indice" to indice.trim(),
            "tienePresentaciones" to tienePresentaciones,
            "presentaciones" to presentacionesNormalizadas,
            "imagenUrl" to imagenUrl.trim()
        )
    }

    private fun construirCambiosAntesDespuesProducto(
        anterior: MoldeProductos?,
        actual: MoldeProductos
    ): Map<String, Map<String, Any>> {
        if (anterior == null) return emptyMap()

        val mapaAnterior = anterior.toAuditoriaMap()
        val mapaActual = actual.toAuditoriaMap()
        val cambios = linkedMapOf<String, Map<String, Any>>()

        mapaActual.forEach { (campo, valorActual) ->
            val valorAnterior = mapaAnterior[campo]
            if (valorAnterior == valorActual) return@forEach

            cambios[campo] = mapOf(
                "antes" to valorAuditoriaProducto(valorAnterior),
                "despues" to valorAuditoriaProducto(valorActual)
            )
        }

        return cambios
    }

    private fun valorAuditoriaProducto(valor: Any?): Any {
        return when (valor) {
            null -> "(vacio)"
            is String -> valor.ifBlank { "(vacio)" }
            is Boolean, is Number -> valor
            is List<*> -> valor.map { valorAuditoriaProducto(it) }
            is Map<*, *> -> valor.entries.associate { (k, v) ->
                (k?.toString() ?: "(sin_clave)") to valorAuditoriaProducto(v)
            }
            else -> valor.toString()
        }
    }

    private fun sanitizarNombreArchivo(nombre: String): String {
        return ProductUtils.sanitizarTexto(nombre)
    }

    private fun similar(a: String, b: String, threshold: Double = 0.7): Boolean {
        return ProductUtils.sonSimilares(a, b, threshold)
    }

    private fun levenshtein(a: String, b: String): Int {
        return ProductUtils.calcularDistanciaLevenshtein(a, b)
    }

    private fun usuarioPuedeAjustarStock(): Boolean {
        return SessionManager.rol.equals("administrador", ignoreCase = true) ||
            SessionManager.rol.equals("supervisor", ignoreCase = true)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN && toqueFueraDeCampoEditable(ev)) {
            ocultarTeclado()
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun toqueFueraDeCampoEditable(ev: MotionEvent): Boolean {
        val foco = currentFocus ?: return false
        if (foco !is EditText) return false

        val rect = Rect()
        foco.getGlobalVisibleRect(rect)
        return !rect.contains(ev.rawX.toInt(), ev.rawY.toInt())
    }

    override fun onDestroy() {
        ocultarDialogoProgresoStock()
        super.onDestroy()
    }
}

















