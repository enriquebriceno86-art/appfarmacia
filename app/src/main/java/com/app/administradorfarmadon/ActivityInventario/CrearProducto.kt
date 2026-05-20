package com.app.administradorfarmadon.ActivityInventario

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import java.time.LocalDate
import java.time.YearMonth
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteProducto
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.PresentacionProducto
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.SugerenciaPresentacion
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.toMoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteIndexado
import com.app.administradorfarmadon.ClasesDatabase.DbPaths
import com.app.administradorfarmadon.ActivityInventario.reference.normalizeProductName
import com.app.administradorfarmadon.ActivityInventario.domain.PresentacionRules
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductControlType
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductPresentation
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductScreen
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductState
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductStep
import com.app.administradorfarmadon.ActivityInventario.ui.sugerirPresentacionesIniciales
import com.app.administradorfarmadon.ActivityInventario.ui.calculateTotalBaseStock
import com.app.administradorfarmadon.ActivityInventario.ui.calculateBaseMinimumStock
import com.app.administradorfarmadon.ActivityInventario.ui.ProductCreatedSummary
import com.app.administradorfarmadon.ActivityInventario.ui.formatCreateProductNumber
import com.app.administradorfarmadon.ActivityInventario.ui.SmartProductHint
import com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestion
import com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionStatus
import com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionViewModel
import com.app.administradorfarmadon.ActivityInventario.reference.PresentationSuggestionRules
import com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel
import com.app.administradorfarmadon.ActivityInventario.reference.TipoControlDetectado
import com.app.administradorfarmadon.ActivityInventario.reference.ProductReference
import com.app.administradorfarmadon.ActivityInventario.reference.ProductReferenceViewModel
import com.app.administradorfarmadon.ClasesDatabase.PresentacionesTiendaConfigManager
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.R
import com.google.firebase.database.FirebaseDatabase
import java.text.Normalizer
import java.util.Locale
import java.util.UUID
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductStockEntryMode
import com.app.administradorfarmadon.ActivityInventario.ui.StockControlMode
import com.app.administradorfarmadon.ActivityInventario.ui.getEffectiveStockControlMode
import com.app.administradorfarmadon.ActivityInventario.ui.getPurchasePresentationName
import com.app.administradorfarmadon.ActivityInventario.ui.isStockEntryModeValidForControlType
import com.app.administradorfarmadon.ActivityInventario.ui.resetStockEntryConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.app.administradorfarmadon.ClasesDatabase.FeedbackCajaController
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Color as ComposeColor
import com.google.android.material.card.MaterialCardView


class CrearProducto : AppCompatActivity() {

    private val productReferenceViewModel: ProductReferenceViewModel by viewModels()
    // Feature de sugerencia de categoría por IA (Gemini).
    private val categorySuggestionViewModel: CategorySuggestionViewModel by viewModels()
    // Feature de escaneo OCR de etiquetas (cámara + Gemini Vision).
    private val labelScannerViewModel: LabelScannerViewModel by viewModels()
    private var createProductUiState by mutableStateOf(CreateProductState())
    private var createProductLoading by mutableStateOf(false)
    private var createProductSuccessSummary by mutableStateOf<ProductCreatedSummary?>(null)
    private var createProductShowReference by mutableStateOf(false)
    private var createProductReferenceDismissed by mutableStateOf(false)
    private var showNoBarcodeConfirmDialog by mutableStateOf(false)
    private val createProductCategoryOptions = mutableStateListOf<String>()
    private val createProductExistingNormalizedNames = mutableStateListOf<String>()
    private var ultimoProductoCreadoCompose: MoldeProductos? = null
    private var createProductCategoryValidationJob: Job? = null
    private var createProductNameValidationJob: Job? = null
    private var createProductBarcodeValidationJob: Job? = null
    private var createProductLotValidationJob: Job? = null // V17.45: Job para validación remota de lote

    // V17.49: Estados para validación remota de lote (Blindado)
    private var isCheckingLotRemote by mutableStateOf(false)
    private var isCheckingBarcodeRemote by mutableStateOf(false)
    private var currentLotRequestId = 0 // Contador para proteger spinner de respuestas viejas
    private var currentBarcodeRequestId = 0
    private var lotValidatedFor by mutableStateOf<String?>(null) 
    private var lotConflictInfo by mutableStateOf<String?>(null)
    private var lotConflictColor by mutableStateOf(0) 
    private var lotConflictSeverity by mutableStateOf(0) 
    private var marginRulesLoadStarted = false
    private var savedPresentationsLoadStarted = false
    // Fix (M5): recuerda el último (nombre|categoria) por el que ya se pidió
    // la búsqueda de referencia para evitar lanzar requests duplicados cuando
    // el usuario va y vuelve entre pasos sin cambiar esos campos.
    private var ultimaReferenciaSolicitada: String? = null

    private val listaCategoria = mutableListOf<CategoriaProductos>()
    private val listaPresentaciones = mutableListOf<PresentacionProducto>()
    private var presentacionPrincipalElegidaManualmente = false
    // Fix (M1): se eliminaron campos heredados del antiguo flujo XML que
    // ya no se leen en ningún punto: `listaNombreAutoComplete`,
    // `reglaPresentacionBaseObligatoriaActiva`,
    // `nombrePresentacionBaseObligatoriaActual`,
    // `ultimoPrecioPresentacionBaseObligatoria`, `imagenUri`,
    // `cameraImageUri`. También se quitaron sus reseteos en `limpiarFormularioCompose`.

    private var statusBarColorOriginal: Int? = null
    private var statusBarLightOriginal: Boolean? = null
    private var navigationBarColorOriginal: Int? = null
    private var navigationBarLightOriginal: Boolean? = null

    // V15.4: Estado reactivo para la IA
    private var aiInventoryEnabled by mutableStateOf(true)

    private val feedbackController by lazy { FeedbackCajaController(this) }
    private var isSuccessAnimVisible by mutableStateOf(false)
    private var isErrorAnimVisible by mutableStateOf(false)







    companion object {
        private const val PATH_INVENTARIO = "Inventario"
        private const val PATH_CATEGORIAS = "CategoriasInventario"
        private const val PATH_NOMBRES = "NombresProductos"
        private const val PATH_NOMBRES_NORMALIZADOS = "NombresProductosNormalizados"
        private const val PATH_PRODUCTOS = "Productos"
        private const val PATH_BUSQUEDA = "BusquedaProductos"
        private const val PATH_PRESENTACIONES = "Presentaciones"
        private const val PATH_MARGENES = "ConfiguracionMargenes"
        private const val PATH_CODIGOS = "CodigosProductos"

        private const val CLOUDINARY_CLOUD_NAME = "dluvatyh7"
        private const val CLOUDINARY_UPLOAD_PRESET = "productos_app"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                manejarAtrasFlujoComposeCrearProducto()
            }
        })

        aplicarStatusBarBlanca()

        createProductUiState = construirEstadoInicialCreateProduct()
        
        // V13.7: Warm-up de conexión IA condicional
        // El warmup de IA se retrasa hasta despues del primer frame para no
        // competir con la apertura visual de la pantalla.

        mostrarArranqueLigero()

        window.decorView.post {
            setContent {
            val referenceState by productReferenceViewModel.referenceState.collectAsState()
            val categorySuggestionState by categorySuggestionViewModel.state.collectAsState()
            val labelScannerState by labelScannerViewModel.state.collectAsState()

            // V18.0: Sincronizar estado de identificación de barcode
            LaunchedEffect(
                categorySuggestionState.estaIdentificandoBarcode,
                categorySuggestionState.barcodeAiResult,
                categorySuggestionState.barcodeMismatchDetected,
                categorySuggestionState.barcodeMismatchOriginalName
            ) {
                createProductUiState = createProductUiState.copy(
                    isIdentifyingBarcode = categorySuggestionState.estaIdentificandoBarcode,
                    barcodeAiResult = categorySuggestionState.barcodeAiResult,
                    barcodeMismatchDetected = categorySuggestionState.barcodeMismatchDetected,
                    barcodeMismatchOriginalName = categorySuggestionState.barcodeMismatchOriginalName
                )
            }

            // V16.10: Observamos la sugerencia de tipo para aplicación AUTOMÁTICA si la confianza es ALTA
            // V17.82: Restaurada la auto-selección silenciosa para un flujo sin fricciones.
            LaunchedEffect(categorySuggestionState.sugerenciaTipoManual) {
                val suggestion = categorySuggestionState.sugerenciaTipoManual
                if (suggestion != null && 
                    suggestion.confianza == com.app.administradorfarmadon.ActivityInventario.reference.ConfianzaIA.ALTA &&
                    createProductUiState.controlType == null &&
                    !createProductUiState.typeSelectedManually) {
                    
                    delay(350) // Delay visual premium antes de la aplicación
                    
                    // Verificación de contexto post-delay (P1 Fix)
                    val nombreActual = normalizeProductName(createProductUiState.name)
                    val catActual = normalizeProductName(createProductUiState.category)
                    val nombreSugerido = normalizeProductName(suggestion.productName)
                    val catSugerida = normalizeProductName(suggestion.category)

                    if (nombreActual == nombreSugerido && catActual == catSugerida) {
                        val mappedType = when (suggestion.tipo) {
                            com.app.administradorfarmadon.ActivityInventario.reference.TipoControlDetectado.UNIDAD -> CreateProductControlType.UNIDAD
                            com.app.administradorfarmadon.ActivityInventario.reference.TipoControlDetectado.PESO -> CreateProductControlType.PESO
                            com.app.administradorfarmadon.ActivityInventario.reference.TipoControlDetectado.LIQUIDO -> CreateProductControlType.LIQUIDO
                            else -> null
                        }

                        if (mappedType != null) {
                            actualizarCreateProductState(
                                createProductUiState.copy(
                                    controlType = mappedType,
                                    requiresPrescription = if (
                                        createProductUiState.categorySelectedFromAi &&
                                        suggestion.requiereReceta != null
                                    ) {
                                        suggestion.requiereReceta
                                    } else {
                                        createProductUiState.requiresPrescription
                                    },
                                    errors = createProductUiState.errors - "controlType"
                                )
                            )
                        }
                    }
                }
            }

            var pendingCameraAction by remember { mutableStateOf<String?>(null) }

            // Launcher de la cámara que captura un Bitmap (preview).
            // El bitmap se le pasa al ViewModel para procesarlo con Gemini.
            val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
            ) { bitmap ->
                if (bitmap != null) labelScannerViewModel.procesar(bitmap)
            }

            // Launcher del permiso CAMERA. Si el usuario aprueba, abrimos la
            // acción pendiente; si rechaza, no hacemos nada (el botón sigue disponible
            // por si el usuario re-intenta).
            val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) {
                    when (pendingCameraAction) {
                        "BARCODE" -> actualizarCreateProductState(createProductUiState.copy(isBarcodeScanning = true))
                        "LABEL" -> actualizarCreateProductState(createProductUiState.copy(isLabelScanning = true))
                        "PHOTO" -> cameraLauncher.launch(null)
                    }
                }
                pendingCameraAction = null
            }

            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    CreateProductScreen(
                        state = createProductUiState,
                        categoryOptions = createProductCategoryOptions.toList(),
                        smartHint = null,
                        // Fix (F1): el botón "Siguiente"/"Guardar" antes solo se
                        // desactivaba si había error en `name`. Ahora refleja la
                        // validez completa del paso actual mediante un chequeo
                        // puro (sin mutar el estado), de modo que el usuario ve
                        // visualmente cuándo puede avanzar.
                        nextEnabled = !createProductLoading &&
                                puedeAvanzarPasoCreateProduct(
                                    createProductUiState
                                ),
                        aiInventoryEnabled = aiInventoryEnabled,
                        referenceState = referenceState,
                        categorySuggestionState = categorySuggestionState,
                        onAcceptCategorySuggestion = ::aceptarSugerenciaCategoria,
                        onSwitchToManualCategory = ::cambiarACategoriaManual,
                        onBackToAiCategory = ::volverASugerenciaIaCategoria,
                        onSearchIA = { inmediato -> buscarConIACompose(inmediato as? Boolean ?: false) },
                        onApplyNameCorrection = ::aplicarCorreccionNombreIA,
                        onDismissNameCorrection = ::descartarCorreccionNombreIA,
                        onAsistManualName = { 
                            categorySuggestionViewModel.asistirManualConNombre(it, com.app.administradorfarmadon.ClasesDatabase.SessionManager.paisOperacion) 
                        },
                        onAsistManualCategory = { texto -> 
                            categorySuggestionViewModel.asistirManualConTextoCategoria(
                                texto = texto, 
                                productName = createProductUiState.name,
                                mercadoActivo = com.app.administradorfarmadon.ClasesDatabase.SessionManager.paisOperacion
                            )
                        },
                        onClearAsistManual = {
                            categorySuggestionViewModel.limpiarAsistenciaManual()
                        },
                        labelScannerState = labelScannerState,
                        onRequestLabelScan = {
                            val granted = androidx.core.content.ContextCompat
                                .checkSelfPermission(this@CrearProducto, android.Manifest.permission.CAMERA) ==
                                android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (granted) actualizarCreateProductState(createProductUiState.copy(isLabelScanning = true))
                            else {
                                pendingCameraAction = "LABEL"
                                permissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        },
                        onConsumeLabelScan = { labelScannerViewModel.reset() },
                        loading = createProductLoading,
                        successSummary = createProductSuccessSummary,
                        showReferenceScreen = createProductShowReference,
                        showReferenceSection = !createProductReferenceDismissed,
                        onBack = ::manejarAtrasFlujoComposeCrearProducto,
                        onStateChange = ::actualizarCreateProductState,
                        onNext = ::avanzarPasoCreateProduct,
                        onPrevious = ::retrocederPasoCreateProduct,
                        onSave = ::guardarProductoDesdeCompose,
                        onCreateAnother = ::reiniciarFlujoComposeCrearProducto,
                        onViewProduct = ::abrirProductoRecienCreado,
                        onRetryReference = {
                            productReferenceViewModel.retry(
                                productName = createProductUiState.name,
                                category = createProductUiState.category
                            )
                        },
                        onOpenReference = { createProductShowReference = true },
                        onDismissReference = { createProductShowReference = false },
                        onConfirmReference = {
                            createProductReferenceDismissed = false
                            createProductShowReference = false
                            guardarReferenciaSugeridaEnProducto(it)
                        },
                        onSkipReference = {
                            reiniciarFlujoComposeCrearProducto()
                        },
                        onAddStockToExistingProduct = ::abrirIngresoStockProductoExistente,
                        // V17.45: Pasamos estados de validación remota de lote
                        isCheckingLotRemote = isCheckingLotRemote,
                        isCheckingBarcodeRemote = isCheckingBarcodeRemote,
                        lotConflictInfo = lotConflictInfo,
                        lotConflictColor = androidx.compose.ui.graphics.Color(lotConflictColor),
                        lotConflictSeverity = lotConflictSeverity,
                        onRequestBarcodeScan = {
                            val granted = androidx.core.content.ContextCompat
                                .checkSelfPermission(this@CrearProducto, android.Manifest.permission.CAMERA) ==
                                android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (granted) actualizarCreateProductState(createProductUiState.copy(isBarcodeScanning = true))
                            else {
                                pendingCameraAction = "BARCODE"
                                permissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        },
                        onIdentificarBarcode = { barcode, image ->
                            categorySuggestionViewModel.identificarBarcode(barcode, image, createProductCategoryOptions.toList())
                        },
                        onCheckBarcodeIntegrity = { barcode, nombre ->
                            categorySuggestionViewModel.verificarIntegridadBarcode(barcode, nombre)
                        },
                        onClearBarcodeIntegrityConflict = {
                            categorySuggestionViewModel.limpiarConflictoBarcode()
                        },
                        showNoBarcodeConfirmDialog = showNoBarcodeConfirmDialog,
                        onDismissNoBarcodeConfirm = {
                            showNoBarcodeConfirmDialog = false
                        },
                        onConfirmNoBarcodeContinue = {
                            showNoBarcodeConfirmDialog = false
                            createProductUiState = createProductUiState.copy(
                                currentStep = CreateProductStep.LOTE_INICIAL
                            )
                        }
                    )

                    if (createProductUiState.isBarcodeScanning) {
                        // Usamos el scanner con OptIn (V17.90)
                        @OptIn(androidx.camera.core.ExperimentalGetImage::class)
                        com.app.administradorfarmadon.ActivityInventario.ui.BarcodeScannerOverlay(
                            onBarcodeDetected = { result ->
                                
                                // V18.2: Usamos el resultado con imagen
                                actualizarCreateProductState(
                                    createProductUiState.copy(
                                        barcode = result.code,
                                        isBarcodeScanning = false,
                                        isBarcodeManualMode = false
                                    ),
                                    imageBase64 = result.imageBase64
                                )
                            },
                            onDismiss = {
                                actualizarCreateProductState(createProductUiState.copy(isBarcodeScanning = false))
                            }
                        )
                    }

                    if (createProductUiState.isLabelScanning) {
                        @OptIn(androidx.camera.core.ExperimentalGetImage::class)
                        com.app.administradorfarmadon.ActivityInventario.ui.LiveOcrScannerOverlay(
                            onResultDetected = { lote, venc ->
                                feedbackController.ventaExitosa(null)
                                actualizarCreateProductState(
                                    createProductUiState.copy(
                                        lotNumber = lote ?: createProductUiState.lotNumber,
                                        expirationDate = venc ?: createProductUiState.expirationDate,
                                        isLabelScanning = false
                                    )
                                )
                            },
                            onDismiss = {
                                actualizarCreateProductState(createProductUiState.copy(isLabelScanning = false))
                            }
                        )
                    }

                    if (isSuccessAnimVisible) {
                        SuccessAnimationOverlay()
                    }
                    if (isErrorAnimVisible) {
                        ErrorAnimationOverlay()
                    }
                }

                LaunchedEffect(isSuccessAnimVisible, isErrorAnimVisible) {
                    if (isSuccessAnimVisible) {
                        updateBarsForAnimation(android.graphics.Color.parseColor("#2E7D32"), false)
                    } else if (isErrorAnimVisible) {
                        updateBarsForAnimation(android.graphics.Color.parseColor("#C62828"), false)
                    } else {
                        restaurarStatusBarOriginal()
                    }
                }
            }
            }

            // Solo lo esencial para el Paso 1 (Ligero)
            PresentacionesTiendaConfigManager.precargar()
            obtenerCategoriasDeProductos()
        }
    }

    private fun mostrarArranqueLigero() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            setPadding(40, 40, 40, 40)
        }

        val progress = ProgressBar(this).apply {
            isIndeterminate = true
        }

        val title = TextView(this).apply {
            text = "Preparando producto"
            textSize = 22f
            setTextColor(Color.parseColor("#111827"))
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val subtitle = TextView(this).apply {
            text = "Cargando formulario..."
            textSize = 14f
            setTextColor(Color.parseColor("#6B7280"))
            gravity = Gravity.CENTER
        }

        root.addView(progress)
        root.addView(title, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 20 })
        root.addView(subtitle, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 8 })

        setContentView(root)
    }

    private fun asegurarReglasMargenCargadas() {
        if (marginRulesLoadStarted) return
        marginRulesLoadStarted = true
        obtenerReglasDeMargen()
    }

    private fun asegurarPresentacionesGuardadasCargadas() {
        if (savedPresentationsLoadStarted) return
        savedPresentationsLoadStarted = true
        cargarPresentacionesGuardadas()
    }

    private fun cargarPresentacionesGuardadas() {
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child("PresentacionesGuardadas")
            .get()
            .addOnSuccessListener { snapshot ->
                val opciones = mutableListOf<com.app.administradorfarmadon.ActivityInventario.ui.SavedPresentation>()
                snapshot.children.forEach { ctTypeSnap ->
                    val controlType = ctTypeSnap.key ?: return@forEach
                    ctTypeSnap.children.forEach { entry ->
                        val nombre = entry.child("nombre").getValue(String::class.java)
                            ?: entry.key.orEmpty()
                        val equiv = entry.child("equivalencia").getValue(String::class.java)
                            ?: ""
                        if (nombre.isNotBlank()) {
                            opciones.add(
                                com.app.administradorfarmadon.ActivityInventario.ui.SavedPresentation(
                                    name = nombre,
                                    equivalenceBase = equiv,
                                    controlType = controlType
                                )
                            )
                        }
                    }
                }
                createProductUiState = createProductUiState.copy(savedPresentationOptions = opciones)
            }
    }

    private fun guardarPresentacionEnDb(saved: com.app.administradorfarmadon.ActivityInventario.ui.SavedPresentation) {
        if (saved.name.isBlank() || saved.controlType.isBlank()) return
        val key = saved.name.lowercase(Locale.getDefault()).replace(Regex("[^a-z0-9]+"), "_")
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child("PresentacionesGuardadas")
            .child(saved.controlType)
            .child(key)
            .setValue(mapOf("nombre" to saved.name, "equivalencia" to saved.equivalenceBase))
    }

    override fun onStart() {
        super.onStart()
        // Actualizar preferencia cada vez que la pantalla vuelve al frente
        aiInventoryEnabled = com.app.administradorfarmadon.ClasesDatabase.PreferenciasFeedbackCaja.estaIaInventarioActiva(this)
    }

    override fun onDestroy() {
        restaurarStatusBarOriginal()
        super.onDestroy()
    }



    /**
     * Fix (M4): antes el cuerpo era `CreateProductState(controlType = null)`
     * y un comentario "// ... resto de valores". `null` ya es el valor
     * por defecto en el data class, así que se reduce a usar el
     * constructor directamente. Se mantiene como función para que el
     * llamado en `limpiarFormularioCompose` quede expresivo.
     */
    private fun construirEstadoInicialCreateProduct(): CreateProductState {
        return CreateProductState()
    }

    private fun manejarAtrasFlujoComposeCrearProducto() {
        if (createProductShowReference) {
            createProductShowReference = false
            return
        }
        if (createProductSuccessSummary != null) {
            reiniciarFlujoComposeCrearProducto()
            return
        }
        if (createProductUiState.currentStep != CreateProductStep.PRODUCTO) {
            retrocederPasoCreateProduct()
            return
        }
        finish()
    }

    private fun actualizarCreateProductState(nuevoEstado: CreateProductState, imageBase64: String? = null) {
        val estadoAnterior = createProductUiState

        // V19.0: OPTIMIZACIÓN - Solo recalculamos presentaciones si el controlType cambió REALMENTE
        val estadoConPresentaciones = if (nuevoEstado.controlType != estadoAnterior.controlType && nuevoEstado.controlType != null) {
            nuevoEstado.copy(
                presentations = sugerirPresentacionesIniciales(nuevoEstado.controlType),
                mainPresentationId = "",
                draftPresentationName = "",
                addPresentationExpanded = false
            )
        } else {
            nuevoEstado
        }

        // V19.0: OPTIMIZACIÓN - Solo normalizamos el mainPresentationId si la lista de presentaciones cambió
        val finalEstado = if (estadoConPresentaciones.presentations != estadoAnterior.presentations || estadoConPresentaciones.mainPresentationId != estadoAnterior.mainPresentationId) {
            val list = estadoConPresentaciones.presentations
            val currentMainId = estadoConPresentaciones.mainPresentationId
            val nextMainId = when {
                list.isEmpty() -> ""
                list.any { it.id == currentMainId } -> currentMainId
                else -> list.first().id
            }
            estadoConPresentaciones.copy(mainPresentationId = nextMainId)
        } else {
            estadoConPresentaciones
        }

        createProductUiState = finalEstado

        if (estadoAnterior.barcode != createProductUiState.barcode) {
            // V18.0: Limpiar estados IA al cambiar código
            createProductUiState = createProductUiState.copy(
                barcodeAiResult = null,
                isIdentifyingBarcode = false,
                barcodeAiApplied = false,
                barcodeAiError = null,
                scannedImageBase64 = imageBase64
            )
            programarValidacionBarcodeCompose(createProductUiState.barcode, imageBase64)
        }

        // Persistir cualquier nueva presentacion guardada por el usuario
        val nuevasPresentaciones = nuevoEstado.savedPresentationOptions.filterNot { nueva ->
            estadoAnterior.savedPresentationOptions.any { existente ->
                existente.name.equals(nueva.name, ignoreCase = true) &&
                    existente.controlType == nueva.controlType
            }
        }
        nuevasPresentaciones.forEach { guardarPresentacionEnDb(it) }

        // V16.10: Si cambia el nombre o la categoría, pedimos sugerencia de tipo (Asistente Silencioso)
        // V16.12: La sugerencia de tipo SOLO se dispara si la categoría fue elegida desde IA (Sugerencia)
        // V17.2: Si la categoría es de IA, lanzamos la consulta de tipo de forma INMEDIATA
        if (estadoAnterior.lotNumber != createProductUiState.lotNumber) {
            validarLoteRemotoCompose(createProductUiState.lotNumber)
        }

        if (estadoAnterior.name != createProductUiState.name) {
            // V17.1: Si cambia el nombre, invalidamos la selección previa de IA
            // para garantizar coherencia en el próximo asistente de tipo.
            // V17.15: Protegemos el reseteo para que no ocurra al navegar entre pasos
            if (createProductUiState.currentStep == CreateProductStep.PRODUCTO) {
                createProductUiState = createProductUiState.copy(
                    categorySelectedFromAi = false
                )
            }

            // Sugerencia IA: Actualización LOCAL inmediata de interpretación (V5.10)
            categorySuggestionViewModel.actualizarInterpretacionLocal(createProductUiState.name)
            
            programarValidacionNombreCompose(createProductUiState.name)
            // Sugerencia IA: re-evaluar la categoría cuando cambia el nombre.
            // Sincronización V5.4: Ya no llamamos a categorySuggestionViewModel.onNombreCambio aquí
            // para evitar doble debounce y cache keys inconsistentes con los defaults.
            // Ahora se centraliza en programarValidacionNombreCompose() tras verificar duplicados
            // y con el contexto de mercado (país) y moneda reales.
        }
        if (estadoAnterior.category != createProductUiState.category) {
            programarValidacionCategoriaCompose(createProductUiState.category)
        }

        // V17.84: Solución al "Empleado Celoso" (IA ya no es ciega al teclado manual)
        if (createProductUiState.currentStep == CreateProductStep.PRODUCTO &&
            (estadoAnterior.category != createProductUiState.category || estadoAnterior.name != createProductUiState.name)) {

            // Si la categoría tiene texto, el asistente analiza el producto SIEMPRE, sin importar el origen.
            if (createProductUiState.category.isNotBlank() && createProductUiState.name.isNotBlank()) {
                categorySuggestionViewModel.asistirManualConTipo(
                    nombre = createProductUiState.name,
                    categoria = createProductUiState.category,
                    inmediato = true
                )
            }
        }
    }

    /**
     * Se invoca AUTOMÁTICAMENTE desde la tarjeta READY (la UI llama esto
     * sin que el usuario tenga que tocar un botón "Usar"). Hace varias cosas:
     *  1. Auto-rellena `state.category` con la categoría sugerida.
     *  2. Auto-rellena `state.controlType` con el tipo detectado por la IA
     *     (UNIDAD/PESO/LIQUIDO). Si la IA devolvió DESCONOCIDO no se toca
     *     el controlType: el usuario lo elegirá manualmente al ir a manual.
     *  3. Agrega la categoría a las opciones locales si era nueva.
     *  4. Le indica al ViewModel que la sugerencia fue aceptada, lo que
     *     dispara el registro de la decisión en Firebase
     *     (`Inventario/DecisionesCategoria/{key}`).
     */
    private fun aceptarSugerenciaCategoria(suggestion: CategorySuggestion) {
        val categoria = suggestion.categoria.trim()
        if (categoria.isBlank()) return
        val nombreCorregido = suggestion.nombreCorregido?.trim().orEmpty()

        if (!createProductCategoryOptions.any { it.equals(categoria, ignoreCase = true) }) {
            createProductCategoryOptions.add(categoria)
        }

        val controlTypeDetectado = when (suggestion.tipoControl) {
            TipoControlDetectado.UNIDAD -> CreateProductControlType.UNIDAD
            TipoControlDetectado.PESO -> CreateProductControlType.PESO
            TipoControlDetectado.LIQUIDO -> CreateProductControlType.LIQUIDO
            TipoControlDetectado.DESCONOCIDO -> null
        }

        val cambios = mutableMapOf<String, Any?>()
        var nuevoEstado = createProductUiState
        if (nombreCorregido.isNotBlank() && !nombreCorregido.equals(nuevoEstado.name, ignoreCase = true)) {
            nuevoEstado = nuevoEstado.copy(
                name = nombreCorregido,
                pendingHighlight = true,
                duplicateProductFound = null,
                errors = nuevoEstado.errors - "name"
            )
            cambios["name"] = nombreCorregido
        }
        if (nuevoEstado.category != categoria) {
            nuevoEstado = nuevoEstado.copy(
                category = categoria,
                errors = nuevoEstado.errors - "category"
            )
            cambios["category"] = categoria
        }
        if (controlTypeDetectado != null && nuevoEstado.controlType != controlTypeDetectado) {
            // V13.1: En este flujo ya no hay presentaciones de la IA inicialmente
            nuevoEstado = nuevoEstado.copy(
                controlType = controlTypeDetectado,
                presentations = sugerirPresentacionesIniciales(controlTypeDetectado),
                mainPresentationId = "",
                errors = nuevoEstado.errors - "controlType"
            )
            cambios["controlType"] = controlTypeDetectado.name
        }
        // Auto-rellenar el switch de receta con la detección de la IA. El
        // usuario siempre puede toggle el switch si la IA se equivocó: el
        // switch aparece junto con la tarjeta una vez READY.
        if (nuevoEstado.requiresPrescription != suggestion.requiereReceta) {
            nuevoEstado = nuevoEstado.copy(
                requiresPrescription = suggestion.requiereReceta
            )
            cambios["requiresPrescription"] = suggestion.requiereReceta
        }

        if (cambios.isNotEmpty()) {
            createProductUiState = nuevoEstado
        }
        categorySuggestionViewModel.aceptar()
        
        // V13.1: Siempre avanzamos al siguiente paso ya que la IA rápida es final para el paso 1.
        avanzarPasoCreateProduct()
    }

    private fun cambiarACategoriaManual() {
        createProductUiState = createProductUiState.copy(
            category = "",
            controlType = null,
            presentations = emptyList(),
            mainPresentationId = "",
            requiresPrescription = false,
            active = true,
            errors = createProductUiState.errors - "category" - "controlType"
        )
        categorySuggestionViewModel.cambiarAManual()
    }

    /**
     * Desde el modo manual, el usuario tocó "Volver a sugerencia IA":
     * limpia el campo y vuelve a consultar Gemini con el nombre actual.
     */
    private fun volverASugerenciaIaCategoria() {
        createProductUiState = createProductUiState.copy(
            category = "",
            errors = createProductUiState.errors - "category"
        )
        categorySuggestionViewModel.volverASugerenciaIA(
            nombre = createProductUiState.name,
            categoriasExistentes = createProductCategoryOptions.toList(),
            mercadoActivo = com.app.administradorfarmadon.ClasesDatabase.SessionManager.paisOperacion,
            monedaSimbolo = com.app.administradorfarmadon.ClasesDatabase.SessionManager.monedaSimbolo
        )
    }

    private fun buscarConIACompose(inmediato: Boolean = false) {
        val currentName = createProductUiState.name
        val normalized = normalizeProductName(currentName)
        
        android.util.Log.d("CategoryAI", "buscarConIACompose: '$currentName', inmediato: $inmediato")

        // V17.6: El Modo Manual Silencioso NO se bloquea por la preferencia global, 
        // ya que es una ayuda ligera no invasiva.

        if (normalized.length < 3) {
            android.util.Log.w("CategoryAI", "Nombre demasiado corto para IA: '$normalized'")
            return
        }

        actualizarCreateProductState(createProductUiState.copy(isAnalyzingKeywords = true))
        
        categorySuggestionViewModel.buscarSugerenciaIA(
            nombre = currentName,
            categoriasExistentes = createProductCategoryOptions.toList(),
            mercadoActivo = com.app.administradorfarmadon.ClasesDatabase.SessionManager.paisOperacion,
            monedaSimbolo = com.app.administradorfarmadon.ClasesDatabase.SessionManager.monedaSimbolo,
            inmediato = inmediato,
            onStateChange = { loading ->
                if (normalizeProductName(createProductUiState.name) == normalized) {
                    actualizarCreateProductState(createProductUiState.copy(isAnalyzingKeywords = loading))
                }
            }
        )
    }

    private fun aplicarCorreccionNombreIA(original: String, corregido: String) {
        // 1. Actualizar el nombre y limpiar estados de error/duplicados del nombre viejo
        val key = "$original|$corregido"
        val nextState = createProductUiState.copy(
            name = corregido,
            lastAppliedCorrection = key,
            duplicateProductFound = null,
            errors = createProductUiState.errors - "name",
            pendingHighlight = true // V10.0: Activar resaltado visual del nuevo nombre
        )
        actualizarCreateProductState(nextState)

        // 2. Sincronizar con el ViewModel de IA
        categorySuggestionViewModel.actualizarInterpretacionLocal(corregido)
        categorySuggestionViewModel.marcarCorreccionAplicada()

        // 3. Re-validar duplicados inmediatamente con el nombre nuevo
        validarNombreInmediatoCompose(corregido)

        // 4. Re-lanzar búsqueda IA con el nombre estandarizado (Automático)
        buscarConIACompose()
    }

    private fun descartarCorreccionNombreIA(original: String, corregido: String) {
        val key = "$original|$corregido"
        val nextDismissed = createProductUiState.dismissedCorrections + key
        actualizarCreateProductState(
            createProductUiState.copy(dismissedCorrections = nextDismissed)
        )
        // Sincronizar con el ViewModel para que no vuelva a sugerirlo en la burbuja
        categorySuggestionViewModel.descartarCorreccion(original, corregido)
    }

    private fun validarNombreInmediatoCompose(name: String) {
        val normalized = normalizeProductName(name)
        if (normalized.isBlank()) return

        val key = buildNormalizedNameKey(normalized)
        verificarNombreDuplicadoEnFirebase(key, { productoExistente ->
            if (normalizeProductName(createProductUiState.name) == normalized) {
                actualizarCreateProductState(createProductUiState.copy(
                    duplicateProductFound = productoExistente
                ))
            }
        }, {})
    }



    private fun normalizeProductName(name: String): String {
        return com.app.administradorfarmadon.ActivityInventario.reference.normalizeProductName(name)
    }

    private fun existeNombreDuplicadoCompose(name: String): Boolean {
        val normalized = normalizeProductName(name)
        if (normalized.isBlank()) return false
        return createProductExistingNormalizedNames.contains(normalized)
    }

    private fun buildNormalizedNameKey(normalizedName: String): String {
        return sanitizarNombreArchivo(normalizedName)
    }

    private fun programarValidacionBarcodeCompose(barcode: String, imageBase64: String? = null) {
        createProductBarcodeValidationJob?.cancel()

        val value = barcode.trim()
        val requestId = ++currentBarcodeRequestId
        
        // V3.30: Limpiar duplicado previo inmediatamente al cambiar el código
        createProductUiState = createProductUiState.copy(
            errors = createProductUiState.errors - "barcode",
            duplicateProductFound = if (createProductUiState.duplicateProductFound?.codigo == value) 
                                      createProductUiState.duplicateProductFound else null,
            scannedImageBase64 = imageBase64
        )
        isCheckingBarcodeRemote = true

        if (value.isBlank()) {
            isCheckingBarcodeRemote = false
            categorySuggestionViewModel.identificarBarcode("", null, emptyList())
            return
        }

        createProductBarcodeValidationJob = lifecycleScope.launch {
            delay(500)
            if (createProductUiState.barcode.trim() != value) {
                isCheckingBarcodeRemote = false
                return@launch
            }
            
            FirebaseDatabase.getInstance()
                .getReference(PATH_INVENTARIO)
                .child(PATH_CODIGOS)
                .child(value)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (requestId == currentBarcodeRequestId) {
                        isCheckingBarcodeRemote = false
                        if (snapshot.exists()) {
                            val productInfo = snapshot.value as? Map<*, *>
                            val productId = productInfo?.get("productoId") as? String
                            if (productId != null) {
                                buscarProductoPorIdYMostrarDuplicado(productId)
                            }
                        } else if (aiInventoryEnabled && !createProductUiState.forceManualProductEntry && !createProductUiState.barcodeAiApplied) {
                            // V18.2: Si no existe, invocar identificación IA (Vision si hay imagen)
                            categorySuggestionViewModel.identificarBarcode(value, imageBase64, createProductCategoryOptions.toList())
                        }
                    }
                }
                .addOnFailureListener {
                    if (requestId == currentBarcodeRequestId) {
                        isCheckingBarcodeRemote = false
                        actualizarCreateProductState(createProductUiState.copy(
                            errors = createProductUiState.errors + ("barcode" to "No se pudo validar el código. Revisa tu conexión.")
                        ))
                    }
                }
        }
    }

    private fun buscarProductoPorIdYMostrarDuplicado(productId: String) {
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRODUCTOS)
            .child(productId)
            .get()
            .addOnSuccessListener { snapshot ->
                val productoExistente = snapshot.toMoldeProductos()
                if (productoExistente != null) {
                    actualizarCreateProductState(createProductUiState.copy(
                        duplicateProductFound = productoExistente,
                        errors = createProductUiState.errors + ("barcode" to "Este código ya pertenece a otro producto.")
                    ))
                }
            }
    }

    private fun programarValidacionNombreCompose(name: String) {
        createProductNameValidationJob?.cancel()

        val normalized = normalizeProductName(name)

        // Sincronización V3.3: Limpiar duplicado previo inmediatamente al cambiar el nombre
        createProductUiState = createProductUiState.copy(
            errors = createProductUiState.errors - "name",
            duplicateProductFound = null,
            pendingHighlight = false, // V10.0: Resetear el resaltado si el usuario escribe
            isValidatingNameRemote = true // V17.85: Encendemos el semáforo al iniciar validación
        )

        if (normalized.isBlank()) {
            createProductUiState = createProductUiState.copy(isValidatingNameRemote = false)
            return
        }

        createProductNameValidationJob = lifecycleScope.launch {
            delay(300) // V17.11: Reducido a 300ms para sincronizar con la IA silenciosa
            val currentName = createProductUiState.name
            if (normalizeProductName(currentName) != normalized) {
                // Si el nombre ya cambió, esta corrutina ya no es válida. 
                // No apagamos el semáforo porque la nueva corrutina lo hará.
                return@launch
            }

            // 1. Bloqueo inmediato si ya existe en la caché local
            if (existeNombreDuplicadoCompose(currentName)) {
                createProductUiState = createProductUiState.copy(
                    errors = createProductUiState.errors + ("name" to "Ya existe un producto con este nombre."),
                    isValidatingNameRemote = false // V17.85: Apagamos el semáforo
                )
                // Sincronización V3.7: Si es duplicado, reseteamos IA inmediatamente para no mostrar dos tarjetas
                categorySuggestionViewModel.reset()

                // Sincronización V3.3: Buscamos el objeto real para la mini-card aunque sea local
                val key = buildNormalizedNameKey(currentName)
                verificarNombreDuplicadoEnFirebase(key, { productoExistente ->
                    // Solo actualizamos si el nombre sigue siendo el mismo después de la red
                    if (normalizeProductName(createProductUiState.name) == normalized) {
                        actualizarCreateProductState(createProductUiState.copy(
                            duplicateProductFound = productoExistente,
                            isAnalyzingKeywords = false
                        ))
                    }
                }, {
                    // En caso de error de red (el helper ya mostró el Toast)
                    if (normalizeProductName(createProductUiState.name) == normalized) {
                        actualizarCreateProductState(createProductUiState.copy(
                            isAnalyzingKeywords = false,
                            isValidatingNameRemote = false // V17.85: Reset por seguridad
                        ))
                    }
                })
                return@launch
            }

            // 2. Verificar contra Firebase (caso general)
            val key = buildNormalizedNameKey(currentName)
            var esDuplicadoEnFirebase = false

            verificarNombreDuplicadoEnFirebase(key, { productoExistente ->
                esDuplicadoEnFirebase = true
                if (normalizeProductName(createProductUiState.name) == normalized) {
                    createProductUiState = createProductUiState.copy(
                        duplicateProductFound = productoExistente,
                        errors = createProductUiState.errors + ("name" to "Ya existe un producto registrado con este nombre."),
                        isValidatingNameRemote = false // V17.85: Apagamos el semáforo
                    )
                    // Sincronización V3.7: Si Firebase confirma duplicado, cancelamos cualquier sugerencia IA
                    categorySuggestionViewModel.reset()
                    actualizarCreateProductState(createProductUiState.copy(isAnalyzingKeywords = false))
                }
            }, {
                // Sincronización V3.7: Solo si NO es duplicado notificamos el cambio al ViewModel
                if (!esDuplicadoEnFirebase && normalizeProductName(createProductUiState.name) == normalized) {
                    
                    // V17.85: Apagamos el semáforo
                    createProductUiState = createProductUiState.copy(isValidatingNameRemote = false)

                    // V5.5: Reglas de estabilidad (solo para limpieza de estados, no dispara red)
                    val numWords = normalized.split(" ").filter { it.isNotBlank() }.size
                    val isStable = normalized.length >= 10 || numWords >= 2
                    
                    if (isStable && !createProductUiState.name.endsWith(" ")) {
                        actualizarCreateProductState(createProductUiState.copy(
                            isAnalyzingKeywords = false, // Solo es true durante búsqueda IA manual
                            duplicateProductFound = null
                        ))
                    } else {
                        // Si no es estable, aseguramos que la IA esté reseteada o en estado inicial
                        actualizarCreateProductState(createProductUiState.copy(
                            isAnalyzingKeywords = false,
                            duplicateProductFound = null,
                            isValidatingNameRemote = false // V17.85: Reset por seguridad
                        ))
                    }
                }
            })
        }
    }


    private fun programarValidacionCategoriaCompose(category: String) {
        createProductCategoryValidationJob?.cancel()

        val value = category.trim()

        createProductUiState = createProductUiState.copy(
            categoryStatus = null
        )

        if (value.isBlank()) return

        createProductCategoryValidationJob = lifecycleScope.launch {
            delay(450)

            if (createProductUiState.category.trim() != value) return@launch

            val existe = createProductCategoryOptions.any {
                it.equals(value, ignoreCase = true)
            }

            createProductUiState = createProductUiState.copy(
                categoryStatus = if (existe) "Categoría existente" else "Categoría nueva"
            )
        }
    }

    /**
     * V17.47: Función de Backfill para indexar todos los lotes existentes.
     * Solo debe usarse una vez para migrar la base de datos antigua al nuevo índice plano.
     */
    private fun migrarLotesAIndicePlano() {
        val rootRef = FirebaseDatabase.getInstance().reference
        rootRef.child("Inventario/Productos").get().addOnSuccessListener { snapshot ->
            val updates = mutableMapOf<String, Any?>()
            snapshot.children.forEach { prodSnap ->
                val prod = prodSnap.toMoldeProductos() ?: return@forEach
                prod.lotes.forEach { (_, lote) ->
                    val numLote = lote.numero.trim().uppercase()
                    if (numLote.isNotBlank()) {
                        val keyLote = ProductUtils.encodeLotKey(numLote)
                        val registroLote = LoteIndexado(
                            numero = numLote,
                            productoId = prod.indice,
                            productoNombre = prod.nombre,
                            vencimiento = lote.vencimiento
                        )
                        updates["${DbPaths.INVENTARIO_LOTES_POR_NUMERO}/$keyLote"] = registroLote
                    }
                }
            }
            if (updates.isNotEmpty()) {
                rootRef.updateChildren(updates).addOnSuccessListener {
                    Toast.makeText(this, "Migración de lotes completada", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun validarLoteRemotoCompose(numeroLote: String) {
        createProductLotValidationJob?.cancel()
        
        val loteLimpio = numeroLote.trim().uppercase()
        if (loteLimpio.isBlank()) {
            lotConflictInfo = null
            lotConflictColor = 0
            lotConflictSeverity = 0
            lotValidatedFor = null
            isCheckingLotRemote = false
            return
        }

        createProductLotValidationJob = lifecycleScope.launch {
            delay(600)
            if (createProductUiState.lotNumber.trim().uppercase() != loteLimpio) {
                return@launch
            }
            isCheckingLotRemote = true
            val requestId = ++currentLotRequestId // V17.49: Capturamos ID de esta petición
            lotConflictInfo = null
            
            // V17.49: Usamos la función compartida y segura (Base64)
            val keyLote = ProductUtils.encodeLotKey(loteLimpio)
            
            FirebaseDatabase.getInstance()
                .getReference(DbPaths.INVENTARIO_LOTES_POR_NUMERO)
                .child(keyLote)
                .get()
                .addOnSuccessListener { snapshot ->
                    // V17.49: El spinner solo se apaga si esta es la petición más reciente
                    if (requestId == currentLotRequestId) {
                        isCheckingLotRemote = false
                    }

                    // V17.48: Protección contra lógica de negocio vieja
                    if (loteLimpio != createProductUiState.lotNumber.trim().uppercase()) {
                        return@addOnSuccessListener
                    }
                    
                    lotValidatedFor = loteLimpio 
                    
                    if (snapshot.exists()) {
                        val loteIndexado = snapshot.getValue(LoteIndexado::class.java)
                        if (loteIndexado != null) {
                            val esMismoProducto = normalizeProductName(loteIndexado.productoNombre) == normalizeProductName(createProductUiState.name)
                            
                            if (esMismoProducto) {
                                lotConflictInfo = "Lote detectado: se sumará al stock existente"
                                lotConflictColor = android.graphics.Color.parseColor("#F59E0B")
                                lotConflictSeverity = 1
                            } else {
                                lotConflictInfo = "¡Cuidado! Lote asignado a: ${loteIndexado.productoNombre}"
                                lotConflictColor = android.graphics.Color.parseColor("#EF4444")
                                lotConflictSeverity = 2
                            }
                        }
                    } else {
                        lotConflictInfo = "Lote disponible"
                        lotConflictColor = android.graphics.Color.parseColor("#0E8F63")
                        lotConflictSeverity = 0
                    }
                }
                .addOnFailureListener {
                    if (requestId == currentLotRequestId) {
                        isCheckingLotRemote = false
                        lotValidatedFor = null
                        lotConflictInfo = "Error de conexión: No se pudo validar el lote"
                        lotConflictColor = android.graphics.Color.GRAY
                        lotConflictSeverity = 2
                    }
                }
        }
    }

    private fun avanzarPasoCreateProduct() {
        val estadoActual = createProductUiState

        // V17.83: Validación de avance inteligente (Eliminación de choques por debounce)
        if (estadoActual.currentStep == CreateProductStep.PRODUCTO) {
            if (!puedeAvanzarPasoProducto(estadoActual)) {
                validarPasoCreateProduct(CreateProductStep.PRODUCTO)
                val firstProductError = createProductUiState.errors
                    .filter { it.key in setOf("name", "category", "controlType", "barcode") }
                    .values
                    .firstOrNull()

                if (!firstProductError.isNullOrBlank()) {
                    Toast.makeText(this, firstProductError, Toast.LENGTH_SHORT).show()
                }
                return
            }

            if (estadoActual.barcode.trim().isBlank()) {
                showNoBarcodeConfirmDialog = true
                return
            }
        } else if (!validarPasoCreateProduct(estadoActual.currentStep)) {
            // V17.67: Obtener los errores específicos del paso actual para el Toast
            val relevantKeys = when (estadoActual.currentStep) {
                CreateProductStep.PRODUCTO -> setOf("name", "category", "controlType", "barcode")
                CreateProductStep.LOTE_INICIAL -> setOf(
                    "lotNumber", "expirationDate", "stockEntryMode", 
                    "receivedUnits", "boxesReceived", "unitsPerBox", 
                    "unitsPerPackage", "minimumStock", "purchaseCost"
                )
                CreateProductStep.PRESENTACIONES -> setOf("presentations", "mainPresentation", "presentations_total")
                CreateProductStep.RESUMEN -> emptySet()
            }
            
            val firstRelevantError = createProductUiState.errors
                .filter { it.key in relevantKeys || it.key.startsWith("price_") || it.key.startsWith("equivalence_") }
                .values.firstOrNull()

            if (!firstRelevantError.isNullOrBlank()) {
                Toast.makeText(this, firstRelevantError, Toast.LENGTH_SHORT).show()
            }
            return
        }

        val siguientePaso = when (estadoActual.currentStep) {
                CreateProductStep.PRODUCTO -> CreateProductStep.LOTE_INICIAL
                CreateProductStep.LOTE_INICIAL -> CreateProductStep.PRESENTACIONES
                CreateProductStep.PRESENTACIONES -> CreateProductStep.RESUMEN
                CreateProductStep.RESUMEN -> CreateProductStep.RESUMEN
            }
        createProductUiState = if (
            estadoActual.currentStep == CreateProductStep.LOTE_INICIAL &&
            siguientePaso == CreateProductStep.PRESENTACIONES
        ) {
            // Sincronización V3.1: Fusionar presentación real de compra con sugerencias IA
            val presentacionesCompra = presentacionesDesdeIngreso(estadoActual)
            val sugerenciasIA = categorySuggestionViewModel.state.value
                .suggestion?.presentacionesSugeridas.orEmpty()

            val presentacionesFinales = if (sugerenciasIA.isNotEmpty()) {
                val mapeadasIA = mapearPresentacionesIA(sugerenciasIA)
                var resultado = mapeadasIA
                
                // Forzamos la inclusión de la presentación de compra real.
                // Si la IA sugirió una con el mismo nombre (ej. "Caja"), 
                // mergePresentacionEvitandoDuplicado la reemplazará con la real
                // de compra (que tiene la equivalencia física configurada).
                presentacionesCompra.forEach { real ->
                    resultado = com.app.administradorfarmadon.ActivityInventario.ui.mergePresentacionEvitandoDuplicado(
                        resultado, real
                    ).first
                }
                resultado
            } else {
                presentacionesCompra
            }

            estadoActual.copy(
                currentStep = siguientePaso,
                presentations = presentacionesFinales,
                mainPresentationId = deducirPresentacionPrincipalInteligente(presentacionesFinales)
            )
        } else {
            estadoActual.copy(
                currentStep = siguientePaso
            )
        }
        if (estadoActual.currentStep == CreateProductStep.PRODUCTO &&
            siguientePaso == CreateProductStep.LOTE_INICIAL
        ) {
            // Fix (M5): solo solicita la referencia si cambió la combinación
            // de nombre + categoría. Antes, ir y volver del paso LOTE_INICIAL
            // gatillaba una llamada de red por cada avance.
            val claveActual = "${estadoActual.name.trim().lowercase(Locale.getDefault())}|" +
                estadoActual.category.trim().lowercase(Locale.getDefault())
            if (claveActual != ultimaReferenciaSolicitada) {
                ultimaReferenciaSolicitada = claveActual
                productReferenceViewModel.startProductReferenceSearch(
                    productName = estadoActual.name,
                    category = estadoActual.category
                )
            }
        }

        if (siguientePaso == CreateProductStep.PRESENTACIONES) {
            asegurarPresentacionesGuardadasCargadas()
        }

        if (siguientePaso == CreateProductStep.RESUMEN) {
            asegurarReglasMargenCargadas()
        }
    }

    private fun retrocederPasoCreateProduct() {
        createProductUiState = createProductUiState.copy(
            currentStep = when (createProductUiState.currentStep) {
                CreateProductStep.PRODUCTO -> CreateProductStep.PRODUCTO
                CreateProductStep.LOTE_INICIAL -> CreateProductStep.PRODUCTO
                CreateProductStep.PRESENTACIONES -> CreateProductStep.LOTE_INICIAL
                CreateProductStep.RESUMEN -> CreateProductStep.PRESENTACIONES
            }
        )
    }

    private fun reiniciarFlujoComposeCrearProducto() {
        createProductLoading = false
        createProductSuccessSummary = null
        createProductShowReference = false
        createProductReferenceDismissed = false
        ultimoProductoCreadoCompose = null
        isSuccessAnimVisible = false
        isErrorAnimVisible = false

        // V17.86: Limpieza exhaustiva de validación remota de lote
        isCheckingLotRemote = false
        isCheckingBarcodeRemote = false
        lotValidatedFor = null
        lotConflictInfo = null
        lotConflictColor = 0
        lotConflictSeverity = 0
        currentLotRequestId++ // Invalida cualquier respuesta de red pendiente
        currentBarcodeRequestId++

        // Fix (M5): liberar la clave cacheada para que el siguiente producto
        // dispare una búsqueda fresca aunque coincida en nombre/categoría.
        ultimaReferenciaSolicitada = null
        productReferenceViewModel.reset()
        // Reinicia también el ciclo de sugerencias IA: limpia el set de
        // rechazos y cualquier sugerencia visible para el próximo producto.
        categorySuggestionViewModel.reset()
        labelScannerViewModel.reset()
        limpiarFormularioCompose()
    }

    private fun limpiarFormularioCompose() {
        val savedAnterior = createProductUiState.savedPresentationOptions
        createProductUiState = construirEstadoInicialCreateProduct().copy(
            savedPresentationOptions = savedAnterior
        )
        listaPresentaciones.clear()
        presentacionPrincipalElegidaManualmente = false
        // Fix (M1): los reseteos de variables heredadas se eliminaron junto
        // con sus declaraciones; ya no quedan campos huérfanos del flujo XML.
    }





    private fun actualizarCategoriasComposeDesdeLista() {
        val nuevasCategorias = listaCategoria.map { it.nombre.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        createProductCategoryOptions.clear()
        createProductCategoryOptions.addAll(nuevasCategorias)
    }








    // Fix (M2): se eliminaron `inferirPresentacionLiquida`,
    // `inferirPresentacionPeso` e `inferirPresentacionUnidad`. Eran
    // helpers heredados del flujo XML antiguo y no se invocaban en
    // ningún punto del flujo Compose actual.

    // Fix (M2/M3): `equivalenciaPorDefecto` se eliminó. Su único caller
    // era `normalizarPresentacionesSegunLote`, también removida. Existe
    // una variante equivalente (`defaultEquivalenceForPresentation`)
    // dentro de `CreateProductScreen.kt` para la lógica de UI.


    // Fix (M3): `normalizarPresentacionesSegunLote` se eliminó. Dependía de
    // `state.receivedPresentation`, un campo del flujo viejo que nunca se
    // setea en el flujo Compose, así que su llamado siempre devolvía la
    // lista sin cambios.

    /**
     * Convierte las sugerencias jerárquicas de Gemini al modelo de UI.
     * Cada presentación queda marcada con `isAiSuggested = true` para que
     * la UI le agregue un ✨; ese flag se vuelve false cuando el usuario
     * la edita (entonces deja de "pertenecer" a la IA).
     */
    private fun mapearPresentacionesIA(
        sugerencias: List<com.app.administradorfarmadon.ActivityInventario.reference.PresentacionSugerida>
    ): List<CreateProductPresentation> {
        val mapped = sugerencias.map { sug ->
            val nombreLimpio = sug.nombre.trim()
            val idBase = nombreLimpio.lowercase(Locale.getDefault()).replace(" ", "_")
            val (inferredGeneric, inferredBrand) = PresentationSuggestionRules.inferCommercialType(nombreLimpio, sug.descripcion)
            val isGeneric = sug.tipoComercial == "GENERICO" || inferredGeneric
            val isBrand = sug.tipoComercial == "MARCA" || inferredBrand

            CreateProductPresentation(
                id = idBase + "_" + UUID.randomUUID().toString().take(4),
                name = nombreLimpio,
                equivalenceText = sug.equivalenciaUnidadBase.toString(),
                salePriceText = "",
                isAiSuggested = true,
                marketPriceReference = sug.precioMercadoReferencial,
                marketConfidence = sug.confianzaPrecio,
                aiDescription = sug.descripcion.ifBlank { null },
                isGeneric = isGeneric,
                isBrand = isBrand
            )
        }
        return PresentationSuggestionRules.deduplicateSuggestions(mapped)
    }

    private fun presentacionesDesdeIngreso(state: CreateProductState): List<CreateProductPresentation> {
        // Si ya hay presentaciones, no sobrescribir, solo actualizar la presentación de compra si existe
        if (state.presentations.isNotEmpty()) {
            // Buscar si ya existe una presentación que represente la unidad de compra (ej. "Unidad", "Caja")
            val existingCompra = when (state.stockEntryMode) {
                CreateProductStockEntryMode.UNIDAD -> state.presentations.find {
                    it.name.equals("Unidad", ignoreCase = true)
                }
                CreateProductStockEntryMode.CAJA -> state.presentations.find {
                    it.name.equals("Caja", ignoreCase = true)
                }
                CreateProductStockEntryMode.CAJA_CON_PAQUETES -> state.presentations.find {
                    it.name.equals("Caja", ignoreCase = true)
                }
                null -> null
            }

            // Si existe la presentación de compra, actualizar su equivalencia y precio (si es necesario)
            return if (existingCompra != null) {
                state.presentations.map { presentation ->
                    if (presentation.id == existingCompra.id) {
                        val newEquivalence = when (state.stockEntryMode) {
                            CreateProductStockEntryMode.UNIDAD -> {
                                if (state.controlType == CreateProductControlType.UNIDAD) {
                                    1.0
                                } else {
                                    parseComposeNumber(state.unitsPerItemText)
                                }
                            }
                            CreateProductStockEntryMode.CAJA -> {
                                // V17.61: La caja equivale al volumen total contenido en ella
                                if (state.controlType == CreateProductControlType.UNIDAD) {
                                    val contenidoPorItem = parseComposeNumber(state.unitsPerItemText)
                                        .takeIf { it > 0.0 }
                                        ?: 1.0
                                    parseComposeNumber(state.unitsPerBoxText) * contenidoPorItem
                                } else {
                                    parseComposeNumber(state.unitsPerBoxText) * parseComposeNumber(state.unitsPerItemText)
                                }
                            }
                            CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
                                parseComposeNumber(state.unitsPerPackageText)
                            }
                            null -> parseComposeNumber(presentation.equivalenceText)
                        }
                        
                        // Normalizar por unidades (Kg/L)
                        val finalEquivalence = if (state.stockEntryUnit == "kg" || state.stockEntryUnit == "L") {
                            newEquivalence * 1000.0
                        } else {
                            newEquivalence
                        }

                        presentation.copy(
                            equivalenceText = if (finalEquivalence > 0) formatCreateProductNumber(finalEquivalence) else presentation.equivalenceText,
                            salePriceText = presentation.salePriceText.takeIf { it.isNotBlank() }
                                ?: state.presentations.firstOrNull()?.salePriceText.orEmpty()
                        )
                    } else {
                        presentation
                    }
                }
            } else {
                // Si no existe, agregamos la presentación de compra al principio de la lista
                val baseEquiv = when (state.stockEntryMode) {
                    CreateProductStockEntryMode.UNIDAD -> {
                        if (state.controlType == CreateProductControlType.UNIDAD) {
                            1.0
                        } else {
                            parseComposeNumber(state.unitsPerItemText)
                        }
                    }
                    CreateProductStockEntryMode.CAJA -> {
                        if (state.controlType == CreateProductControlType.UNIDAD) {
                            val contenidoPorItem = parseComposeNumber(state.unitsPerItemText)
                                .takeIf { it > 0.0 }
                                ?: 1.0
                            parseComposeNumber(state.unitsPerBoxText) * contenidoPorItem
                        } else {
                            parseComposeNumber(state.unitsPerBoxText) * parseComposeNumber(state.unitsPerItemText)
                        }
                    }
                    CreateProductStockEntryMode.CAJA_CON_PAQUETES -> parseComposeNumber(state.unitsPerPackageText)
                    null -> 0.0
                }

                val finalEquiv = if (state.stockEntryUnit == "kg" || state.stockEntryUnit == "L") baseEquiv * 1000.0 else baseEquiv

                val nuevaPresentacion = when (state.stockEntryMode) {
                    CreateProductStockEntryMode.UNIDAD -> CreateProductPresentation(
                        id = "unidad",
                        name = getPurchasePresentationName(state.controlType, CreateProductStockEntryMode.UNIDAD),
                        equivalenceText = formatCreateProductNumber(finalEquiv),
                        salePriceText = state.presentations.firstOrNull()?.salePriceText.orEmpty()
                    )
                    CreateProductStockEntryMode.CAJA -> CreateProductPresentation(
                        id = "caja",
                        name = getPurchasePresentationName(state.controlType, CreateProductStockEntryMode.CAJA),
                        equivalenceText = formatCreateProductNumber(finalEquiv),
                        salePriceText = state.presentations.firstOrNull()?.salePriceText.orEmpty()
                    )
                    CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
                        CreateProductPresentation(
                            id = "blister",
                            name = getPurchasePresentationName(state.controlType, CreateProductStockEntryMode.CAJA_CON_PAQUETES),
                            equivalenceText = formatCreateProductNumber(finalEquiv),
                            salePriceText = state.presentations.firstOrNull()?.salePriceText.orEmpty()
                        )
                    }
                    null -> null
                }
                if (nuevaPresentacion != null) listOf(nuevaPresentacion) + state.presentations
                else state.presentations
            }
        }

        // Si no hay presentaciones, generar la lista inicial según el modo de ingreso
        val baseEquiv = when (state.stockEntryMode) {
            CreateProductStockEntryMode.UNIDAD -> {
                if (state.controlType == CreateProductControlType.UNIDAD) {
                    1.0
                } else {
                    parseComposeNumber(state.unitsPerItemText)
                }
            }
            CreateProductStockEntryMode.CAJA -> {
                if (state.controlType == CreateProductControlType.UNIDAD) {
                    val contenidoPorItem = parseComposeNumber(state.unitsPerItemText)
                        .takeIf { it > 0.0 }
                        ?: 1.0
                    parseComposeNumber(state.unitsPerBoxText) * contenidoPorItem
                } else {
                    parseComposeNumber(state.unitsPerBoxText) * parseComposeNumber(state.unitsPerItemText)
                }
            }
            CreateProductStockEntryMode.CAJA_CON_PAQUETES -> parseComposeNumber(state.unitsPerPackageText)
            null -> 0.0
        }
        val finalEquiv = if (state.stockEntryUnit == "kg" || state.stockEntryUnit == "L") baseEquiv * 1000.0 else baseEquiv

        return when (state.stockEntryMode) {
            CreateProductStockEntryMode.UNIDAD -> listOf(
                CreateProductPresentation(
                    "unidad",
                    getPurchasePresentationName(state.controlType, CreateProductStockEntryMode.UNIDAD),
                    formatCreateProductNumber(finalEquiv),
                    ""
                )
            )
            CreateProductStockEntryMode.CAJA -> listOf(
                CreateProductPresentation(
                    "caja",
                    getPurchasePresentationName(state.controlType, CreateProductStockEntryMode.CAJA),
                    formatCreateProductNumber(finalEquiv),
                    ""
                )
            )
            CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
                listOf(
                    CreateProductPresentation(
                        "blister",
                        getPurchasePresentationName(state.controlType, CreateProductStockEntryMode.CAJA_CON_PAQUETES),
                        formatCreateProductNumber(finalEquiv),
                        ""
                    )
                )
            }
            null -> state.presentations
        }
    }

    /**
     * Fix (F1): chequeo puro (no muta `errors`) que decide si el botón
     * "Siguiente" / "Guardar" debe estar habilitado. Se usa solo para
     * activar/desactivar el botón en la UI; el control de errores y
     * mensajes lo sigue haciendo `validarPasoCreateProduct` al pulsar.
     *
     * Para el paso actual revisa los requisitos mínimos:
     *  - PRODUCTO: nombre + categoria + tipo de control y sin error de duplicado.
     *  - LOTE_INICIAL: lote único, vencimiento válido y no vencido ni fuera
     *    de rango, modo de ingreso configurado con cantidades > 0,
     *    aviso mínimo > 0 y costo de compra > 0.
     *  - PRESENTACIONES: al menos una presentación, todas con equivalencia
     *    y precio > 0, principal seleccionada si hay más de una.
     *  - RESUMEN: los tres anteriores en conjunto.
     */
    private fun puedeAvanzarPasoCreateProduct(
        estado: CreateProductState
    ): Boolean {
        return when (estado.currentStep) {
            CreateProductStep.PRODUCTO -> puedeAvanzarPasoProducto(estado)
            CreateProductStep.LOTE_INICIAL -> puedeAvanzarPasoLote(estado)
            CreateProductStep.PRESENTACIONES -> puedeAvanzarPasoPresentaciones(estado)
            CreateProductStep.RESUMEN ->
                puedeAvanzarPasoProducto(estado) &&
                puedeAvanzarPasoLote(estado) &&
                puedeAvanzarPasoPresentaciones(estado)
        }
    }

    private fun puedeAvanzarPasoProducto(estado: CreateProductState): Boolean {
        // V17.85: Bloqueo de avance mientras la validación de red está en curso (Semáforo)
        if (estado.isValidatingNameRemote || isCheckingBarcodeRemote) return false

        // V17.86: Bloqueo explícito si se encontró un producto duplicado (Sincronización con UI)
        if (estado.duplicateProductFound != null) return false

        // El botón se enciende si hay datos escritos
        if (estado.name.trim().isBlank()) return false
        if (estado.category.trim().isBlank()) return false
        if (estado.controlType == null) return false
        
        // El botón solo se apaga si la corrutina de fondo YA terminó y confirmó un error real en pantalla
        if (!estado.errors["name"].isNullOrBlank()) return false
        if (!estado.errors["barcode"].isNullOrBlank()) return false

        // V18.9: Bloqueo estricto de integridad si el nombre no coincide con el código
        if (estado.barcodeMismatchDetected) return false

        return true
    }

    private fun puedeAvanzarPasoLote(
        estado: CreateProductState
    ): Boolean {
        val lote = estado.lotNumber.trim().uppercase(Locale.getDefault())
        if (lote.isBlank()) return false
        
        // V17.48: Bloqueo de avance si la validación remota no ha terminado o falló
        if (isCheckingLotRemote) return false
        if (lotValidatedFor != lote) return false // No ha validado el texto actual aún
        if (lotConflictSeverity == 2) return false // Rojo (Conflicto con otro producto)

        if (!esVencimientoValido(estado.expirationDate)) return false
        if (estaVencido(estado.expirationDate)) return false
        if (!esVencimientoDentroDeRango(estado.expirationDate)) return false

        val modo = estado.stockEntryMode ?: return false
        if (!isStockEntryModeValidForControlType(estado.controlType, modo)) return false
        if (!estado.stockEntryConfigured) return false
        
        // V17.61: Uso de la Calculadora Oficial para validar stock
        val recibido = calculateTotalBaseStock(estado)
        if (recibido <= 0.0) return false

        // La validación ahora usa minimumStockUnits (el valor físico calculado)
        // en lugar de minimumStockText (que ya no se usa en el stepper moderno).
        if (estado.minimumStockUnits <= 0) return false
        
        // El cálculo del stock base para la alerta se hace internamente
        // al guardar, aquí solo validamos que la opción elegida sea positiva.

        val costo = estado.purchaseCost
            .replace(com.app.administradorfarmadon.ClasesDatabase.SessionManager.monedaSimbolo, "", ignoreCase = true)
            .trim()
            .replace(",", ".")
            .toDoubleOrNull()
        if (costo == null || costo <= 0.0) return false

        return true
    }

    private fun puedeAvanzarPasoPresentaciones(estado: CreateProductState): Boolean {
        if (estado.presentations.isEmpty()) return false
        val recibidoBase = calculateTotalBaseStock(estado)
        var totalAsignado = 0.0

        estado.presentations.forEach { presentation ->
            val equivalencia = presentation.equivalenceText
                .trim()
                .replace(",", ".")
                .toDoubleOrNull() ?: 0.0
            
            if (equivalencia <= 0.0) return false
            
            val precio = presentation.salePriceText
                .replace(com.app.administradorfarmadon.ClasesDatabase.SessionManager.monedaSimbolo, "", ignoreCase = true)
                .trim()
                .replace(",", ".")
                .toDoubleOrNull() ?: 0.0
            
            if (precio <= 0.0) return false
            totalAsignado += equivalencia
        }
        
        // V17.65: Regla de oro - La suma de presentaciones no puede superar el stock recibido
        if (totalAsignado > recibidoBase) return false

        if (estado.presentations.size > 1 && estado.mainPresentationId.isBlank()) return false
        return true
    }

    private fun validarPasoCreateProduct(step: CreateProductStep): Boolean {
        val estado = createProductUiState
        val errors = estado.errors.toMutableMap()

        when (step) {
            CreateProductStep.PRODUCTO -> {
                if (estado.name.trim().isBlank()) {
                    errors["name"] = "Ingresa el nombre del producto"
                } else if (existeNombreDuplicadoCompose(estado.name)) {
                    errors["name"] = "Ya existe un producto con este nombre."
                } else {
                    errors.remove("name")
                }

                if (estado.category.trim().isBlank()) {
                    errors["category"] = "Ingresa o selecciona una categoria"
                } else {
                    errors.remove("category")
                }

                if (estado.controlType == null) {
                    errors["controlType"] = "Selecciona el tipo de control"
                } else {
                    errors.remove("controlType")
                }

                if (estado.barcodeMismatchDetected) {
                    errors["barcode"] = "El código de barras no coincide con el nombre del producto."
                } else if (estado.errors["barcode"].isNullOrBlank()) {
                    errors.remove("barcode")
                }
            }

            CreateProductStep.LOTE_INICIAL -> {
                if (estado.lotNumber.trim().isBlank()) {
                    errors["lotNumber"] = "Ingresa el numero de lote"
                } else if (lotConflictSeverity == 2) {
                    errors["lotNumber"] = lotConflictInfo ?: "Lote no disponible"
                } else {
                    errors.remove("lotNumber")
                }

                // Fix (F3): antes la validación dejaba pasar años fuera de
                // rango (ej. "07/99" → 2099). `estaVencido` retornaba false
                // para esos años, así que el paso avanzaba con vencimientos
                // absurdos. Ahora se rechaza explícitamente con
                // `esVencimientoDentroDeRango`.
                if (!esVencimientoValido(estado.expirationDate)) {
                    errors["expirationDate"] = "Usa el formato MM/AA"
                } else if (estaVencido(estado.expirationDate)) {
                    errors["expirationDate"] = "La fecha ingresada ya vencio"
                } else if (!esVencimientoDentroDeRango(estado.expirationDate)) {
                    errors["expirationDate"] =
                        "El año no puede superar ${getMaxAllowedYear() % 100} (máx. ${getMaxAllowedYear()})"
                } else {
                    errors.remove("expirationDate")
                }

                if (estado.stockEntryMode == null || !estado.stockEntryConfigured) {
                    errors["stockEntryMode"] = "Selecciona como recibiste el producto"
                } else if (!isStockEntryModeValidForControlType(estado.controlType, estado.stockEntryMode)) {
                    errors["stockEntryMode"] = "Selecciona una presentación válida para este tipo de producto"
                } else {
                    errors.remove("stockEntryMode")
                }

                when (estado.stockEntryMode) {
                    null -> Unit
                    CreateProductStockEntryMode.UNIDAD -> {
                        val quantity = if (estado.controlType == CreateProductControlType.UNIDAD) {
                            parseComposeNumber(estado.unitsPerItemText)
                        } else {
                            parseComposeNumber(estado.receivedUnitsText)
                        }
                        val content = parseComposeNumber(estado.unitsPerItemText)

                        if (quantity <= 0.0) {
                            if (estado.controlType == CreateProductControlType.UNIDAD) {
                                errors["unitsPerItem"] = "Ingresa la cantidad recibida"
                            } else {
                                errors["receivedUnits"] = "Ingresa la cantidad recibida"
                            }
                        } else {
                            if (estado.controlType == CreateProductControlType.UNIDAD) {
                                errors.remove("unitsPerItem")
                            } else {
                                errors.remove("receivedUnits")
                            }
                        }

                        if (estado.controlType != CreateProductControlType.UNIDAD) {
                            if (content <= 0.0) {
                                errors["unitsPerItem"] = "Ingresa el contenido por item"
                            } else {
                                errors.remove("unitsPerItem")
                            }
                        }

                        if (estado.controlType == CreateProductControlType.UNIDAD) errors.remove("receivedUnits")
                        errors.remove("boxesReceived")
                        errors.remove("unitsPerBox")
                        errors.remove("packagesPerBox")
                        errors.remove("unitsPerPackage")
                    }

                    CreateProductStockEntryMode.CAJA -> {
                        val boxes = parseComposeNumber(estado.boxesReceivedText)
                        val unitsPerBox = parseComposeNumber(estado.unitsPerBoxText)

                        if (boxes <= 0.0) {
                            errors["boxesReceived"] = "Ingresa cuantas cajas recibiste"
                        } else {
                            errors.remove("boxesReceived")
                        }

                        if (unitsPerBox <= 0.0) {
                            errors["unitsPerBox"] = "Ingresa cuanto trae cada caja"
                        } else {
                            errors.remove("unitsPerBox")
                        }

                        errors.remove("receivedUnits")
                        errors.remove("packagesPerBox")
                        errors.remove("unitsPerPackage")
                    }

                    CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
                        val blisters = parseComposeNumber(estado.boxesReceivedText)
                        val unitsPerPackage = parseComposeNumber(estado.unitsPerPackageText)

                        if (blisters <= 0.0) {
                            errors["boxesReceived"] = "Ingresa cuántos blísteres recibiste"
                        } else {
                            errors.remove("boxesReceived")
                        }

                        if (unitsPerPackage <= 0.0) {
                            errors["unitsPerPackage"] = "Ingresa cuántas unidades trae cada blíster"
                        } else {
                            errors.remove("unitsPerPackage")
                        }

                        errors.remove("receivedUnits")
                        errors.remove("unitsPerBox")
                        errors.remove("packagesPerBox")
                    }
                }

                val minimumStockBase = calculateBaseMinimumStock(estado)
                // V17.61: Uso de la Calculadora Oficial
                val recibidoEnBase = calculateTotalBaseStock(estado)

                // La validación ahora usa minimumStockUnits en lugar del campo de texto antiguo.
                if (estado.minimumStockUnits <= 0) {
                    errors["minimumStock"] = "Selecciona una cantidad de alerta"
                } else if (estado.stockEntryMode != null && minimumStockBase > recibidoEnBase) {
                    // Aunque el stepper pre-calcula opciones válidas, mantenemos esta
                    // seguridad lógica por si hay cambios de stock recibidos después.
                    errors["minimumStock"] = "La alerta no puede superar el stock recibido"
                } else {
                    errors.remove("minimumStock")
                }

                val purchaseCost = estado.purchaseCost
                    .replace(com.app.administradorfarmadon.ClasesDatabase.SessionManager.monedaSimbolo, "", ignoreCase = true)
                    .trim()
                    .replace(",", ".")
                    .toDoubleOrNull()

                if (purchaseCost == null || purchaseCost <= 0.0) {
                    errors["purchaseCost"] = "Ingresa el costo de compra"
                } else {
                    errors.remove("purchaseCost")
                }
            }

            CreateProductStep.PRESENTACIONES -> {
                if (estado.presentations.isEmpty()) {
                    errors["presentations"] = "Agrega al menos una presentacion"
                } else {
                    errors.remove("presentations")
                }

                val recibidoBase = calculateTotalBaseStock(estado)
                var totalAsignado = 0.0

                estado.presentations.forEach { presentation ->
                    val equivalence = presentation.equivalenceText
                        .trim()
                        .replace(",", ".")
                        .toDoubleOrNull() ?: 0.0

                    if (equivalence <= 0.0) {
                        errors["equivalence_${presentation.id}"] = "Equivalencia invalida"
                    } else {
                        errors.remove("equivalence_${presentation.id}")
                    }

                    val price = presentation.salePriceText
                        .replace(com.app.administradorfarmadon.ClasesDatabase.SessionManager.monedaSimbolo, "", ignoreCase = true)
                        .trim()
                        .replace(",", ".")
                        .toDoubleOrNull() ?: 0.0

                    if (price <= 0.0) {
                        errors["price_${presentation.id}"] = "Precio requerido"
                    } else {
                        errors.remove("price_${presentation.id}")
                    }
                    
                    totalAsignado += equivalence
                }
                
                // V17.65: Error general si la suma supera la capacidad del lote
                if (totalAsignado > recibidoBase) {
                    errors["presentations_total"] = "Las presentaciones superan el stock recibido (${formatCreateProductNumber(recibidoBase)})"
                } else {
                    errors.remove("presentations_total")
                }

                if (estado.presentations.size > 1 && estado.mainPresentationId.isBlank()) {
                    errors["mainPresentation"] = "Selecciona la presentacion principal"
                } else {
                    errors.remove("mainPresentation")
                }
            }

            CreateProductStep.RESUMEN -> {
                return validarPasoCreateProduct(CreateProductStep.PRODUCTO) &&
                        validarPasoCreateProduct(CreateProductStep.LOTE_INICIAL) &&
                        validarPasoCreateProduct(CreateProductStep.PRESENTACIONES)
            }
        }

        createProductUiState = estado.copy(errors = errors)

        return errors.none { entry ->
            when (step) {
                CreateProductStep.PRODUCTO ->
                    entry.key in setOf("name", "category", "controlType", "barcode")

                CreateProductStep.LOTE_INICIAL ->
                    entry.key in setOf(
                        "lotNumber",
                        "expirationDate",
                        "stockEntryMode",
                        "receivedUnits",
                        "boxesReceived",
                        "unitsPerBox",
                        "packagesPerBox",
                        "unitsPerPackage",
                        "minimumStock",
                        "purchaseCost"
                    )

                CreateProductStep.PRESENTACIONES ->
                    entry.key == "presentations" ||
                            entry.key == "mainPresentation" ||
                            entry.key.startsWith("price_") ||
                            entry.key.startsWith("equivalence_")

                CreateProductStep.RESUMEN -> false
            }
        }
    }

    private fun esVencimientoValido(value: String): Boolean {
        val cleaned = value.trim()
        if (!cleaned.matches(Regex("\\d{2}/\\d{2}"))) return false
        val parts = cleaned.split("/")
        val month = parts.getOrNull(0)?.toIntOrNull() ?: return false
        return month in 1..12
    }

    private fun estaVencido(value: String): Boolean {
        if (!esVencimientoValido(value)) return false
        val parts = value.trim().split("/")
        val month = parts.getOrNull(0)?.toIntOrNull() ?: return false
        val year = parts.getOrNull(1)?.toIntOrNull() ?: return false
        val fullYear = 2000 + year
        // Fix (F3): el corto-circuito anterior `if (fullYear > maxAllowed) return false`
        // ocultaba años futuros lejanos como si fueran "no vencidos". Ese chequeo
        // se movió a `esVencimientoDentroDeRango`, que sí es consultado por la
        // validación del paso. Aquí `estaVencido` se mantiene fiel a su nombre.
        val expirationDate = runCatching {
            YearMonth.of(fullYear, month).atEndOfMonth()
        }.getOrNull() ?: return false
        return expirationDate.isBefore(LocalDate.now())
    }

    /**
     * Fix (F3): valida que el año del vencimiento no exceda el límite
     * permitido (año actual + 10). Antes este chequeo vivía dentro de
     * `estaVencido` retornando `false`, lo que dejaba pasar fechas como
     * "07/99" en el paso LOTE_INICIAL. Ahora es un chequeo explícito.
     */
    private fun esVencimientoDentroDeRango(value: String): Boolean {
        if (!esVencimientoValido(value)) return false
        val year = value.trim().split("/").getOrNull(1)?.toIntOrNull() ?: return false
        val fullYear = 2000 + year
        return fullYear <= getMaxAllowedYear()
    }

    private fun getMaxAllowedYear(): Int {
        return LocalDate.now().year + 10
    }

    private fun guardarProductoDesdeCompose() {
        if (!validarPasoCreateProduct(CreateProductStep.RESUMEN)) return

        if (existeNombreDuplicadoCompose(createProductUiState.name)) {
            createProductUiState = createProductUiState.copy(
                currentStep = CreateProductStep.PRODUCTO,
                errors = createProductUiState.errors + ("name" to "Ya existe un producto con este nombre.")
            )
            return
        }

        val producto = construirProductoDesdeCompose(createProductUiState)

        listaPresentaciones.clear()
        listaPresentaciones.addAll(producto.presentaciones)

        ultimoProductoCreadoCompose = producto
        createProductLoading = true
        createProductShowReference = false

        // Fix duplicados: chequeo final contra Firebase para cubrir el caso
        // en que la caché local `createProductExistingNormalizedNames` esté
        // vacía o desactualizada (p. ej. otro usuario creó el producto en
        // paralelo). Si existe, se cancela el guardado.
        verificarNombreDuplicadoEnFirebase(
            nombre = producto.nombre,
            onExiste = {
                createProductLoading = false
                createProductUiState = createProductUiState.copy(
                    currentStep = CreateProductStep.PRODUCTO,
                    errors = createProductUiState.errors + (
                        "name" to "Ya existe un producto con este nombre."
                    )
                )
            },
            onContinuar = {
                verificarProductoComposeAntesDeGuardar(producto)
            }
        )
    }

    /**
     * Sincronización V3.17 (Estricta): Verifica si el nombre normalizado ya
     * está en el índice global de `NombresProductos`. 
     *
     * Si el nombre ya existe, llama a `onExiste` con el producto completo.
     * Si no existe, llama a `onContinuar`.
     *
     * IMPORTANTE: Si ocurre un error de red o de Firebase, ya NO se asume
     * que no existe. Se detiene el proceso y se informa al usuario para
     * evitar la creación de duplicados por falta de conectividad.
     */
    private fun verificarNombreDuplicadoEnFirebase(
        nombre: String,
        onExiste: (MoldeProductos) -> Unit,
        onContinuar: () -> Unit
    ) {
        val normalized = normalizeProductName(nombre)
        val key = buildNormalizedNameKey(normalized)
        if (normalized.isBlank() || key.isBlank()) {
            onContinuar()
            return
        }
        
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_NOMBRES)
            .child(key)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    if (!createProductExistingNormalizedNames.contains(normalized)) {
                        createProductExistingNormalizedNames.add(normalized)
                    }
                    
                    val indice = snapshot.child("indice").getValue(String::class.java).orEmpty()
                    if (indice.isNotBlank()) {
                        FirebaseDatabase.getInstance()
                            .getReference(PATH_INVENTARIO)
                            .child(PATH_PRODUCTOS)
                            .child(indice)
                            .get()
                            .addOnSuccessListener { productSnapshot ->
                                val prod = productSnapshot.toMoldeProductos()
                                if (prod != null) {
                                    onExiste(prod)
                                } else {
                                    // Índice huérfano (existe nombre pero no producto)
                                    // Por seguridad en V3.17, tratamos como error de datos
                                    createProductLoading = false
                                    mostrarErrorGuardado("Error de integridad: El nombre existe pero los datos del producto no. Por favor, contacte a soporte.")
                                }
                            }
                            .addOnFailureListener { e ->
                                createProductLoading = false
                                mostrarErrorGuardado("No se pudo verificar el producto debido a un error de red: ${e.message}. Intente de nuevo.")
                            }
                    } else {
                        // Seguridad V3.21: Si el nombre existe pero el índice está vacío o corrupto,
                        // bloqueamos por precaución para evitar duplicados "ciegos".
                        createProductLoading = false
                        mostrarErrorGuardado("Error de integridad: Se encontró el nombre pero el índice está corrupto. Guardado bloqueado por seguridad.")
                    }
                } else {
                    // No existe, procedemos normal
                    onContinuar()
                }
            }
            .addOnFailureListener { e ->
                // Seguridad V3.17: Si falla la red, bloqueamos el guardado
                createProductLoading = false
                mostrarErrorGuardado("No se puede validar la existencia del producto (Error de red). Guardado cancelado para evitar duplicados.")
            }
    }

    private fun verificarProductoComposeAntesDeGuardar(producto: MoldeProductos) {
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRODUCTOS)
            .child(producto.indice)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    createProductLoading = false

                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.titulo_verificacion_producto))
                        .setMessage("Este producto ya existe. Se generara un codigo nuevo para no sobrescribirlo.")
                        .setPositiveButton(getString(R.string.boton_continuar)) { _, _ ->
                            createProductLoading = true

                            val productoConIndiceUnico = producto.copy(
                                indice = generarIndiceProductoUnico(producto.indice)
                            )

                            ultimoProductoCreadoCompose = productoConIndiceUnico
                            guardarProductoFinalCompose(productoConIndiceUnico)
                        }
                        .setNegativeButton(getString(R.string.boton_cancelar)) { _, _ ->
                            createProductLoading = false
                        }
                        .show()
                } else {
                    guardarProductoFinalCompose(producto)
                }
            }
            .addOnFailureListener { e ->
                mostrarErrorGuardado(e.message)
            }
    }

    private fun guardarProductoFinalCompose(producto: MoldeProductos) {
        val rootRef = FirebaseDatabase.getInstance().getReference(PATH_INVENTARIO)
        
        // Preparar operación atómica (V3.16)
        val updates = mutableMapOf<String, Any?>()
        
        // 1. Datos del producto
        val productPath = "$PATH_PRODUCTOS/${producto.indice}"
        updates[productPath] = productoParaFirebase(producto)

        val presentacionesPath = "ProductoPresentaciones/${producto.indice}"
        val lotesPath = "ProductoLotes/${producto.indice}"
        updates[presentacionesPath] = relacionesPresentacionesParaFirebase(producto)
        updates[lotesPath] = relacionesLotesParaFirebase(producto)

        // 1.1 Código de barras (Índice y dato en producto)
        if (producto.codigo.isNotBlank()) {
            updates["$PATH_CODIGOS/${producto.codigo}"] = mapOf(
                "productoId" to producto.indice,
                "productoNombre" to producto.nombre,
                "categoria" to producto.categoria
            )
        }
        
        // 2. Índice de nombre para búsqueda/duplicados
        val normalized = normalizeProductName(producto.nombre)
        if (normalized.isNotBlank()) {
            val key = buildNormalizedNameKey(normalized)
            if (key.isNotBlank()) {
                val registroNombre = NombreProductos(
                    id = key,
                    nombre = producto.nombre,
                    indice = producto.indice,
                    normalizedName = normalized
                )
                updates["$PATH_NOMBRES/$key"] = registroNombre
            }
        }

        // 3. Índice plano de lotes (V17.46)
        producto.lotes.forEach { (loteId, lote) ->
            val numLote = lote.numero.trim().uppercase()
            if (numLote.isNotBlank()) {
                val keyLote = ProductUtils.encodeLotKey(numLote)
                val lotePath = "${DbPaths.INVENTARIO_PRODUCTO_LOTES}/${producto.indice}/$loteId"
                val registroLote = LoteIndexado(
                    numero = numLote,
                    productoId = producto.indice,
                    productoNombre = producto.nombre,
                    loteId = loteId,
                    lotePath = lotePath,
                    vencimiento = lote.vencimiento
                )
                updates["LotesPorNumero/$keyLote"] = registroLote
            }
        }

        rootRef.updateChildren(updates)
            .addOnSuccessListener {
                // Sincronización V3.20: Solo actualizar caché local si la escritura fue exitosa
                if (normalized.isNotBlank() && !createProductExistingNormalizedNames.contains(normalized)) {
                    createProductExistingNormalizedNames.add(normalized)
                }

                createProductLoading = false
                ultimoProductoCreadoCompose = producto
                // Sincronización V3.21: Animación de éxito completa (Sin resumen fijo)
                triggerSuccessAnimation()
            }
            .addOnFailureListener { e ->
                mostrarErrorGuardado(e.message)
            }
    }

    private fun triggerSuccessAnimation() {
        isSuccessAnimVisible = true
        feedbackController.ventaExitosa(null)
        lifecycleScope.launch {
            delay(3500)
            isSuccessAnimVisible = false
            reiniciarFlujoComposeCrearProducto()
        }
    }

    private fun triggerErrorAnimation() {
        isErrorAnimVisible = true
        feedbackController.error(null)
        lifecycleScope.launch {
            delay(3500)
            isErrorAnimVisible = false
        }
    }

    private fun updateBarsForAnimation(color: Int, isLight: Boolean) {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        window.statusBarColor = color
        window.navigationBarColor = color
        insetsController.isAppearanceLightStatusBars = isLight
        insetsController.isAppearanceLightNavigationBars = isLight
    }

    private fun generarIndiceProductoUnico(indiceBase: String): String {
        val baseNormalizada = sanitizarNombreArchivo(indiceBase).ifBlank { "producto" }
        val sufijo = UUID.randomUUID().toString().take(8).lowercase(Locale.getDefault())
        return "${baseNormalizada}_$sufijo"
    }

    private fun sanitizarNombreArchivo(nombre: String): String {
        return ProductUtils.sanitizarTexto(nombre)
    }

    /**
     * Fix (F2): serializa cantidades respetando si son enteras o decimales.
     * Antes se hacía `.toString()` sobre un `Int`, perdiendo los decimales.
     * Ahora, si la parte fraccionaria es 0, se serializa como entero
     * ("500" en vez de "500.0") para mantener compatibilidad con productos
     * antiguos; si es decimal real, se conserva ("500.5").
     */
    private fun formatearCantidadCompose(value: Double): String {
        return if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
    }

    private fun parseComposeNumber(value: String): Double {
        return value
            .trim()
            .replace(com.app.administradorfarmadon.ClasesDatabase.SessionManager.monedaSimbolo, "", ignoreCase = true)
            .replace(",", ".")
            .toDoubleOrNull()
            ?: 0.0
    }

    private fun construirProductoDesdeCompose(state: CreateProductState): MoldeProductos {
        val controlType = state.controlType ?: CreateProductControlType.UNIDAD
        val nombreProducto = normalizarTextoVisible(state.name)
        val categoriaProducto = normalizarTextoVisible(state.category)

        // V17.60: Sincronización absoluta con la Calculadora Oficial del Paso 2
        val stockDisponible = calculateTotalBaseStock(state)
        val stockMinimo = calculateBaseMinimumStock(state)
        
        val unidadBase = when (controlType) {
            CreateProductControlType.UNIDAD -> "Unidad"
            CreateProductControlType.PESO -> "g"
            CreateProductControlType.LIQUIDO -> "mL"
        }
        val unidadVisualInventario = when (controlType) {
            CreateProductControlType.UNIDAD -> unidadBase
            CreateProductControlType.PESO -> state.stockEntryUnit.ifBlank { "g" }
            CreateProductControlType.LIQUIDO -> state.stockEntryUnit.ifBlank { "mL" }
        }

        val effectiveMode = state.stockControlMode ?: getEffectiveStockControlMode(state)
        val unidadStockMinimo = resolverUnidadStockMinimoProfesional(state, controlType, effectiveMode)

        val presentaciones = state.presentations.map { presentation ->
            PresentacionProducto(
                nombre = normalizarTextoVisible(presentation.name),
                cantidad = presentation.equivalenceText
                    .trim()
                    .replace(",", ".")
                    .toDoubleOrNull()
                    ?.toInt()
                    ?.coerceAtLeast(1)
                    ?: 1,
                precioventa = presentation.salePriceText
                    .replace(com.app.administradorfarmadon.ClasesDatabase.SessionManager.monedaSimbolo, "", ignoreCase = true)
                    .trim()
                    .replace(",", ".")
                    .toDoubleOrNull()
                    ?: 0.0
            )
        }.toMutableList()

        val unidadesPorPresentacionCompra = when (state.stockEntryMode) {
            null -> 1.0
            CreateProductStockEntryMode.UNIDAD -> {
                if (controlType == CreateProductControlType.UNIDAD) 1.0 
                else parseComposeNumber(state.unitsPerItemText)
            }
            CreateProductStockEntryMode.CAJA -> {
                if (controlType == CreateProductControlType.UNIDAD) {
                    val contenidoPorItem = parseComposeNumber(state.unitsPerItemText)
                        .takeIf { it > 0.0 }
                        ?: 1.0
                    parseComposeNumber(state.unitsPerBoxText) * contenidoPorItem
                } else {
                    // V17.61: La caja equivale al volumen total contenido en ella
                    parseComposeNumber(state.unitsPerBoxText) * parseComposeNumber(state.unitsPerItemText)
                }
            }
            CreateProductStockEntryMode.CAJA_CON_PAQUETES -> parseComposeNumber(state.unitsPerPackageText)
        }.let { raw ->
            // Normalizar por unidades (Kg/L) si aplica
            if (state.stockEntryUnit == "kg" || state.stockEntryUnit == "L") raw * 1000.0 else raw
        }.toInt().coerceAtLeast(1)

        val mainPresentation = when {
            presentaciones.isEmpty() -> ""
            presentaciones.size == 1 -> presentaciones.first().nombre
            else -> {
                val mainFromState = state.presentations.firstOrNull {
                    it.id == state.mainPresentationId
                }?.name

                mainFromState
                    ?: presentaciones.firstOrNull()?.nombre
                    ?: ""
            }
        }

        val precioCompra = state.purchaseCost
            .replace(com.app.administradorfarmadon.ClasesDatabase.SessionManager.monedaSimbolo, "", ignoreCase = true)
            .trim()
            .replace(",", ".")

        val vencimientoLimpio = state.expirationDate
            .trim()
            .replace("/", "_")

        val indiceBase =
            "${nombreProducto}_${categoriaProducto}_$vencimientoLimpio"

        val indiceSanitizado = sanitizarNombreArchivo(indiceBase)

        val numeroLote = state.lotNumber.trim().uppercase(Locale.getDefault())
        val claveLote = ProductUtils.encodeLotKey(numeroLote)

        val primerLote = LoteProducto(
            numero = numeroLote,
            vencimiento = state.expirationDate.trim(),
            cantidad = stockDisponible,
            fecha = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(java.util.Date()),
            costoCompraUnitario = calcularCostoUnitario(precioCompra, stockDisponible),
            costoUltimoIngreso = precioCompra.toDoubleOrNull() ?: 0.0,
            costoUltimoIngresoUnitario = calcularCostoUnitario(precioCompra, stockDisponible)
        )

        return MoldeProductos(
            nombre = nombreProducto,
            codigo = state.barcode,
            vencimiento = state.expirationDate.trim(),
            categoria = categoriaProducto,
            preciodecompra = precioCompra,
            cantidadinicial = formatearCantidadCompose(stockDisponible),
            stockminimo = formatearCantidadCompose(stockMinimo),
            stockMinimoContenedores = state.minimumStockUnits,
            unidadStockMinimo = unidadStockMinimo,
            unidadbase = unidadBase,
            unidadVisualInventario = unidadVisualInventario,
            tipoBaseInventario = PresentacionRules.tipoBaseInventarioDesdeUnidadBase(unidadBase),
            presentacionprincipal = mainPresentation,
            requierereceta = state.requiresPrescription,
            estadodelproducto = state.active,
            indice = indiceSanitizado,
            tienePresentaciones = presentaciones.isNotEmpty(),
            presentaciones = presentaciones,
            unidadesPorPresentacionCompra = unidadesPorPresentacionCompra,
            basePorUnidad = calcularBasePorUnidadProfesional(state, controlType),
            referenceKeywords = state.selectedKeywords.toList(), // Guardar solo las seleccionadas
            imagenUrl = "",
            lotes = mapOf(claveLote to primerLote)
        )
    }

    private fun productoParaFirebase(producto: MoldeProductos): Map<String, Any?> {
        val data = linkedMapOf<String, Any?>()

        fun putText(key: String, value: String) {
            value.trim().takeIf { it.isNotBlank() }?.let { data[key] = it }
        }

        fun putNumber(key: String, value: Number) {
            val asDouble = value.toDouble()
            if (asDouble != 0.0) data[key] = value
        }

        putText("nombre", producto.nombre)
        putText("categoria", producto.categoria)
        putText("codigo", producto.codigo)
        putText("vencimiento", producto.vencimiento)
        putText("preciodecompra", producto.preciodecompra)
        putText("cantidadinicial", producto.cantidadinicial)
        putText("stockminimo", producto.stockminimo)
        putNumber("stockMinimoContenedores", producto.stockMinimoContenedores)
        putText("unidadStockMinimo", producto.unidadStockMinimo)
        putText("unidadbase", producto.unidadbase)
        putText("unidadVisualInventario", producto.unidadVisualInventario)
        putText("tipoBaseInventario", producto.tipoBaseInventario)
        putText("presentacionprincipal", producto.presentacionprincipal)

        data["requierereceta"] = producto.requierereceta
        data["estadodelproducto"] = producto.estadodelproducto
        putText("indice", producto.indice)
        data["tienePresentaciones"] = producto.tienePresentaciones
        putNumber("unidadesPorPresentacionCompra", producto.unidadesPorPresentacionCompra)
        putNumber("basePorUnidad", producto.basePorUnidad)
        putText("imagenUrl", producto.imagenUrl)
        putText("loteConsumoSeleccionado", producto.loteConsumoSeleccionado)
        if (producto.loteConsumoSeleccionManual) data["loteConsumoSeleccionManual"] = true

        putText("referenceCommonUse", producto.referenceCommonUse)
        if (producto.referenceUseCases.isNotEmpty()) data["referenceUseCases"] = producto.referenceUseCases
        putText("referenceHowToUse", producto.referenceHowToUse)
        if (producto.referenceNotRecommendedFor.isNotEmpty()) data["referenceNotRecommendedFor"] = producto.referenceNotRecommendedFor
        if (producto.referenceKeywords.isNotEmpty()) data["referenceKeywords"] = producto.referenceKeywords
        if (producto.referenceWarnings.isNotEmpty()) data["referenceWarnings"] = producto.referenceWarnings
        putText("referenceSourceName", producto.referenceSourceName)
        putText("referenceSourceUrl", producto.referenceSourceUrl)
        putText("referenceRxcui", producto.referenceRxcui)
        putText("referenceNdc", producto.referenceNdc)
        putText("referenceMatchedName", producto.referenceMatchedName)
        putNumber("referenceConfidence", producto.referenceConfidence)
        if (producto.referenceLanguage.isNotBlank() && productoTieneReferencia(producto)) {
            data["referenceLanguage"] = producto.referenceLanguage
        }

        return data
    }

    private fun relacionesPresentacionesParaFirebase(producto: MoldeProductos): List<Map<String, Any?>> {
        return producto.presentaciones
            .filter { it.nombre.trim().isNotBlank() }
            .map { presentacion ->
                linkedMapOf<String, Any?>(
                    "nombre" to presentacion.nombre.trim(),
                    "cantidad" to presentacion.cantidad.coerceAtLeast(1),
                    "precioventa" to presentacion.precioventa
                )
            }
    }

    private fun relacionesLotesParaFirebase(producto: MoldeProductos): Map<String, Map<String, Any?>> {
        return producto.lotes
            .filterKeys { it.isNotBlank() }
            .mapValues { (loteId, lote) ->
                loteParaFirebase(
                    lote = lote,
                    loteId = loteId,
                    lotePath = "${DbPaths.INVENTARIO_PRODUCTO_LOTES}/${producto.indice}/$loteId"
                )
            }
            .filterValues { it.isNotEmpty() }
    }

    private fun loteParaFirebase(
        lote: LoteProducto,
        loteId: String = "",
        lotePath: String = ""
    ): Map<String, Any?> {
        val data = linkedMapOf<String, Any?>()

        fun putText(key: String, value: String) {
            value.trim().takeIf { it.isNotBlank() }?.let { data[key] = it }
        }

        fun putNumber(key: String, value: Number) {
            val asDouble = value.toDouble()
            if (asDouble != 0.0) data[key] = value
        }

        putText("loteId", loteId)
        putText("lotePath", lotePath)
        putText("numero", lote.numero)
        putText("vencimiento", lote.vencimiento)
        putNumber("cantidad", lote.cantidad)
        putNumber("cantidadBloqueada", lote.cantidadBloqueada)
        putText("fecha", lote.fecha)
        putNumber("costoCompraUnitario", lote.costoCompraUnitario)
        putNumber("costoUltimoIngreso", lote.costoUltimoIngreso)
        putNumber("costoUltimoIngresoUnitario", lote.costoUltimoIngresoUnitario)
        putText("observaciones", lote.observaciones)
        putText("motivoBloqueo", lote.motivoBloqueo)
        putNumber("timestampUltimoBloqueo", lote.timestampUltimoBloqueo)

        return data
    }

    private fun productoTieneReferencia(producto: MoldeProductos): Boolean {
        return producto.referenceCommonUse.isNotBlank() ||
                producto.referenceUseCases.isNotEmpty() ||
                producto.referenceHowToUse.isNotBlank() ||
                producto.referenceNotRecommendedFor.isNotEmpty() ||
                producto.referenceKeywords.isNotEmpty() ||
                producto.referenceWarnings.isNotEmpty() ||
                producto.referenceSourceName.isNotBlank() ||
                producto.referenceSourceUrl.isNotBlank() ||
                producto.referenceRxcui.isNotBlank() ||
                producto.referenceNdc.isNotBlank() ||
                producto.referenceMatchedName.isNotBlank() ||
                producto.referenceConfidence > 0.0
    }

    private fun calcularCostoUnitario(precioCompra: String, stockDisponible: Double): Double {
        val total = precioCompra.toDoubleOrNull() ?: 0.0
        return if (total > 0.0 && stockDisponible > 0.0) total / stockDisponible else 0.0
    }

    private fun resolverUnidadStockMinimoProfesional(
        state: CreateProductState,
        controlType: CreateProductControlType,
        effectiveMode: StockControlMode
    ): String {
        if (effectiveMode != StockControlMode.INDIVISIBLE) {
            return when (controlType) {
                CreateProductControlType.UNIDAD -> "unidades"
                CreateProductControlType.PESO -> "g"
                CreateProductControlType.LIQUIDO -> "mL"
            }
        }

        return when (controlType) {
            CreateProductControlType.LIQUIDO -> "frasco"
            CreateProductControlType.PESO -> "envase"
            CreateProductControlType.UNIDAD -> when (state.stockEntryMode) {
                CreateProductStockEntryMode.CAJA_CON_PAQUETES -> "blister"
                else -> "item"
            }
        }
    }

    private fun calcularBasePorUnidadProfesional(
        state: CreateProductState,
        controlType: CreateProductControlType
    ): Double {
        if (controlType == CreateProductControlType.UNIDAD) return 0.0

        val rawBase = when (state.stockEntryMode) {
            CreateProductStockEntryMode.UNIDAD,
            CreateProductStockEntryMode.CAJA -> parseComposeNumber(state.unitsPerItemText)
            CreateProductStockEntryMode.CAJA_CON_PAQUETES -> parseComposeNumber(state.unitsPerPackageText)
            null -> 0.0
        }

        val baseNormalizada = if (state.stockEntryUnit == "kg" || state.stockEntryUnit == "L") {
            rawBase * 1000.0
        } else {
            rawBase
        }

        return baseNormalizada.takeIf { it > 0.0 } ?: 0.0
    }

    private fun normalizarTextoVisible(value: String): String {
        val compactado = value.trim()
            .replace(Regex("\\s+"), " ")
            .replace(Regex("(?i)(\\d+(?:[.,]\\d+)?)(mg|mcg|ml|l|kg|g|ui)\\b")) { match ->
                "${match.groupValues[1]} ${normalizarUnidadVisible(match.groupValues[2])}"
            }

        if (compactado.isBlank()) return ""

        return compactado
            .split(" ")
            .joinToString(" ") { token -> normalizarTokenVisible(token) }
    }

    private fun normalizarTokenVisible(token: String): String {
        val limpio = token.trim()
        if (limpio.isBlank()) return ""

        val lower = limpio.lowercase(Locale.getDefault())
        val unidad = normalizarUnidadVisible(limpio)
        if (lower in setOf("ml", "l", "mg", "g", "kg", "mcg", "ui")) return unidad

        if (limpio.any { it.isDigit() } || limpio.all { !it.isLetter() }) return limpio
        if (limpio.length <= 2 && limpio.all { it.isUpperCase() }) return limpio

        return limpio.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
        }
    }

    private fun normalizarUnidadVisible(token: String): String {
        return when (token.trim().lowercase(Locale.getDefault())) {
            "ml" -> "mL"
            "l" -> "L"
            "mg" -> "mg"
            "g" -> "g"
            "kg" -> "kg"
            "mcg" -> "mcg"
            "ui" -> "UI"
            else -> token
        }
    }

    private fun construirResumenExitoCompose(producto: MoldeProductos): ProductCreatedSummary {
        val precioPrincipal = producto.presentaciones.firstOrNull { presentacion ->
            presentacion.nombre.equals(producto.presentacionprincipal, ignoreCase = true)
        }?.precioventa ?: producto.presentaciones.firstOrNull()?.precioventa ?: 0.0
        val stockTexto = when {
            producto.cantidadinicial.isBlank() -> "0"
            else -> "${producto.cantidadinicial} ${unidadResumenSegunTipo(producto.tipoBaseInventario)}"
        }
        val loteInicial = producto.lotes.values.firstOrNull()

        return ProductCreatedSummary(
            indice = producto.indice,
            name = producto.nombre,
            category = producto.categoria,
            mainPrice = MonedaHelper.formatearSimple(precioPrincipal),
            stockAvailable = stockTexto,
            lotNumber = loteInicial?.numero.orEmpty().ifBlank { "Sin lote" },
            expirationDate = loteInicial?.vencimiento.orEmpty().ifBlank { producto.vencimiento }
        )
    }

    private fun unidadResumenSegunTipo(tipoBaseInventario: String): String {
        return when (tipoBaseInventario.trim().lowercase(Locale.getDefault())) {
            "peso" -> "g"
            "liquido" -> "mL"
            else -> "unidades"
        }
    }

    private fun abrirProductoRecienCreado() {
        val indice = createProductSuccessSummary?.indice.orEmpty()
        if (indice.isBlank()) return
        startActivity(
            android.content.Intent(this, EditarProductodelInventario::class.java).apply {
                putExtra("indice", indice)
            }
        )
    }

    /**
     * Elige automáticamente la mejor presentación para mostrar como "Principal" al vender.
     * Prioriza unidades minoristas (Unidad, Frasco, Tableta) sobre bultos (Caja, Pack).
     */
    private fun deducirPresentacionPrincipalInteligente(
        presentaciones: List<com.app.administradorfarmadon.ActivityInventario.ui.CreateProductPresentation>
    ): String {
        if (presentaciones.isEmpty()) return ""
        
        // Palabras clave de unidades que suelen ser la unidad mínima de venta (retail)
        val retailKeywords = listOf(
            "unidad", "pieza", "frasco", "botella", "ampolla", "vial", 
            "tableta", "pastilla", "comprimido", "capsula", "sobre", "sachet", 
            "tubo", "crema", "pomada", "g", "ml"
        )

        // Buscamos la primera que contenga una palabra de retail como palabra completa (word boundary)
        // para evitar que "g" o "ml" den falsos positivos dentro de otras palabras.
        val priorizada = presentaciones.firstOrNull { pres ->
            val nombre = pres.name.lowercase(Locale.getDefault())
            retailKeywords.any { key -> 
                // Expresión regular para buscar la palabra exacta con límites (\b)
                val regex = "\\b${Regex.escape(key)}\\b".toRegex()
                nombre.contains(regex)
            }
        }

        return priorizada?.id ?: presentaciones.first().id
    }

    private fun abrirIngresoStockProductoExistente(producto: com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos) {
        val indice = producto.indice.trim()
        if (indice.isBlank()) return

        startActivity(
            android.content.Intent(this, EditarProductodelInventario::class.java).apply {
                putExtra("indice", indice)
                putExtra("auto_ingreso_stock", true)
            }
        )
    }

    private fun guardarReferenciaSugeridaEnProducto(reference: ProductReference) {
        val producto = ultimoProductoCreadoCompose ?: return
        val indice = producto.indice.ifBlank { createProductSuccessSummary?.indice.orEmpty() }
        if (indice.isBlank()) return

        val updates = hashMapOf<String, Any>(
            "referenceCommonUse" to reference.commonUse.orEmpty(),
            "referenceUseCases" to reference.useCases,
            "referenceHowToUse" to reference.howToUse.orEmpty(),
            "referenceNotRecommendedFor" to reference.notRecommendedFor,
            "referenceKeywords" to reference.searchKeywords,
            "referenceWarnings" to reference.warnings,
            "referenceSourceName" to reference.sourceName,
            "referenceSourceUrl" to reference.sourceUrl.orEmpty(),
            "referenceRxcui" to reference.rxcui.orEmpty(),
            "referenceNdc" to reference.ndc.orEmpty(),
            "referenceMatchedName" to reference.matchedName.orEmpty(),
            "referenceConfidence" to reference.confidence,
            "referenceLanguage" to reference.language
        )

        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRODUCTOS)
            .child(indice)
            .updateChildren(updates)
            .addOnSuccessListener {
                ultimoProductoCreadoCompose = producto.copy(
                    referenceCommonUse = reference.commonUse.orEmpty(),
                    referenceUseCases = reference.useCases,
                    referenceHowToUse = reference.howToUse.orEmpty(),
                    referenceNotRecommendedFor = reference.notRecommendedFor,
                    referenceKeywords = reference.searchKeywords,
                    referenceWarnings = reference.warnings,
                    referenceSourceName = reference.sourceName,
                    referenceSourceUrl = reference.sourceUrl.orEmpty(),
                    referenceRxcui = reference.rxcui.orEmpty(),
                    referenceNdc = reference.ndc.orEmpty(),
                    referenceMatchedName = reference.matchedName.orEmpty(),
                    referenceConfidence = reference.confidence,
                    referenceLanguage = reference.language
                )
                productReferenceViewModel.setEditedReference(reference)
                Toast.makeText(
                    this,
                    "Referencia guardada correctamente",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    error.message ?: "No se pudo guardar la referencia",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun aplicarStatusBarBlanca() {
        if (statusBarColorOriginal == null) {
            statusBarColorOriginal = window.statusBarColor
        }
        if (navigationBarColorOriginal == null) {
            navigationBarColorOriginal = window.navigationBarColor
        }

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (statusBarLightOriginal == null) {
            statusBarLightOriginal = insetsController.isAppearanceLightStatusBars
        }
        if (navigationBarLightOriginal == null) {
            navigationBarLightOriginal = insetsController.isAppearanceLightNavigationBars
        }

        window.statusBarColor = Color.WHITE
        insetsController.isAppearanceLightStatusBars = true
    }

    private fun restaurarStatusBarOriginal() {
        statusBarColorOriginal?.let { color ->
            window.statusBarColor = color
        }
        navigationBarColorOriginal?.let { color ->
            window.navigationBarColor = color
        }

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        statusBarLightOriginal?.let { lightStatusBar ->
            insetsController.isAppearanceLightStatusBars = lightStatusBar
        }
        navigationBarLightOriginal?.let { lightNav ->
            insetsController.isAppearanceLightNavigationBars = lightNav
        }
    }




    private fun obtenerCategoriasDeProductos() {
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_CATEGORIAS)
            .get()
            .addOnSuccessListener { snapshot ->
                listaCategoria.clear()

                snapshot.children.forEach { data ->
                    val categoria = data.getValue(CategoriaProductos::class.java)
                    if (categoria != null) {
                        listaCategoria.add(categoria)
                    }
                }

                actualizarCategoriasComposeDesdeLista()
            }
    }

    private fun obtenerReglasDeMargen() {
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_MARGENES)
            .get()
            .addOnSuccessListener { snapshot ->
                val reglas = mutableListOf<com.app.administradorfarmadon.ActivityInventario.ui.CategoryMarginRule>()
                snapshot.children.forEach { data ->
                    val regla = data.getValue(com.app.administradorfarmadon.ActivityInventario.ui.CategoryMarginRule::class.java)
                    if (regla != null) {
                        reglas.add(regla)
                    }
                }
                actualizarCreateProductState(createProductUiState.copy(marginRules = reglas))
            }
    }





    // ---------------------------------------------------------------------------------------------
    // SUGERENCIAS GLOBALES
    // ---------------------------------------------------------------------------------------------







    private fun aplicarDestelloRojoDesvanecido(view: View) {
        val width = view.width.takeIf { it > 0 } ?: view.measuredWidth
        val height = view.height.takeIf { it > 0 } ?: view.measuredHeight

        if (width <= 0 || height <= 0) {
            view.post { aplicarDestelloRojoDesvanecido(view) }
            return
        }

        val overlay = ColorDrawable(0x66D93025.toInt()).apply {
            alpha = 0
            setBounds(0, 0, width, height)
        }

        view.overlay.add(overlay)

        ValueAnimator.ofInt(110, 0).apply {
            duration = 650L
            addUpdateListener { animator ->
                overlay.alpha = animator.animatedValue as Int
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.overlay.remove(overlay)
                }

                override fun onAnimationCancel(animation: Animator) {
                    view.overlay.remove(overlay)
                }
            })
            start()
        }
    }


    private fun mostrarErrorGuardado(mensaje: String?) {
        createProductLoading = false
        triggerErrorAnimation()

        Toast.makeText(
            this,
            mensaje ?: getString(R.string.error_generico_guardar),
            Toast.LENGTH_LONG
        ).show()
    }


    @androidx.compose.runtime.Composable
    private fun SuccessAnimationOverlay() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ComposeColor(0xFF2E7D32)), // Green 800
            contentAlignment = Alignment.Center
        ) {
            AnimatedCheckmark(Modifier.size(180.dp))
        }
    }

    @androidx.compose.runtime.Composable
    private fun ErrorAnimationOverlay() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ComposeColor(0xFFC62828)), // Red 800
            contentAlignment = Alignment.Center
        ) {
            AnimatedCross(Modifier.size(180.dp))
        }
    }

    @androidx.compose.runtime.Composable
    private fun AnimatedCheckmark(modifier: Modifier = Modifier) {
        val progress = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            progress.animateTo(1f, animationSpec = tween(800, easing = LinearOutSlowInEasing))
        }

        Canvas(modifier = modifier) {
            val strokeWidth = 16.dp.toPx()
            val w = size.width
            val h = size.height

            val p1 = Offset(w * 0.25f, h * 0.5f)
            val p2 = Offset(w * 0.45f, h * 0.7f)
            val p3 = Offset(w * 0.75f, h * 0.35f)

            if (progress.value > 0f) {
                val firstSegmentProgress = (progress.value / 0.4f).coerceAtMost(1f)
                drawLine(
                    color = ComposeColor.White,
                    start = p1,
                    end = Offset(
                        p1.x + (p2.x - p1.x) * firstSegmentProgress,
                        p1.y + (p2.y - p1.y) * firstSegmentProgress
                    ),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            if (progress.value > 0.4f) {
                val secondSegmentProgress = ((progress.value - 0.4f) / 0.6f).coerceAtMost(1f)
                drawLine(
                    color = ComposeColor.White,
                    start = p2,
                    end = Offset(
                        p2.x + (p3.x - p2.x) * secondSegmentProgress,
                        p2.y + (p3.y - p2.y) * secondSegmentProgress
                    ),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun AnimatedCross(modifier: Modifier = Modifier) {
        val progress = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            progress.animateTo(1f, animationSpec = tween(800, easing = LinearOutSlowInEasing))
        }

        Canvas(modifier = modifier) {
            val strokeWidth = 16.dp.toPx()
            val w = size.width
            val h = size.height

            if (progress.value > 0f) {
                val firstLineProgress = (progress.value / 0.5f).coerceAtMost(1f)
                drawLine(
                    color = ComposeColor.White,
                    start = Offset(w * 0.3f, h * 0.3f),
                    end = Offset(
                        w * 0.3f + (w * 0.4f) * firstLineProgress,
                        h * 0.3f + (h * 0.4f) * firstLineProgress
                    ),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            if (progress.value > 0.5f) {
                val secondLineProgress = ((progress.value - 0.5f) / 0.5f).coerceAtMost(1f)
                drawLine(
                    color = ComposeColor.White,
                    start = Offset(w * 0.7f, h * 0.3f),
                    end = Offset(
                        w * 0.7f - (w * 0.4f) * secondLineProgress,
                        h * 0.3f + (h * 0.4f) * secondLineProgress
                    ),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }







}

data class CategoriaProductos(val id: String="",val nombre: String="",var indice: String? = null,var color: String? = null)

data class NombreProductos(
    val id: String = "",
    val nombre: String = "",
    var indice: String? = null,
    val normalizedName: String? = null
)
