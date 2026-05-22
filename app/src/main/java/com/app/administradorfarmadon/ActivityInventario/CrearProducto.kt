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
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.stockFisicoBase
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.stockMinimoBase
import com.app.administradorfarmadon.ClasesDatabase.DbPaths
import com.app.administradorfarmadon.ActivityInventario.reference.normalizeProductName
import com.app.administradorfarmadon.ActivityInventario.domain.PresentacionRules
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductControlType
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductPresentation
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductScreen
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductState
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductStep
import com.app.administradorfarmadon.ActivityInventario.ui.calculateTotalBaseStock
import com.app.administradorfarmadon.ActivityInventario.ui.calculateBaseMinimumStock
import com.app.administradorfarmadon.ActivityInventario.ui.getPhysicalUnitLabel
import com.app.administradorfarmadon.ActivityInventario.ui.resolveMinimumStockControlMode
import com.app.administradorfarmadon.ActivityInventario.ui.StockControlMode
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductStockEntryMode
import com.app.administradorfarmadon.ActivityInventario.ui.ProductCreatedSummary
import com.app.administradorfarmadon.ActivityInventario.ui.formatCreateProductNumber
import com.app.administradorfarmadon.ActivityInventario.ui.SmartProductHint
import com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionViewModel
import com.app.administradorfarmadon.ActivityInventario.reference.PresentationSuggestionRules
import com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel
import com.app.administradorfarmadon.ActivityInventario.reference.TipoControlDetectado
import com.app.administradorfarmadon.ClasesDatabase.PresentacionesTiendaConfigManager
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.R
import com.google.firebase.database.FirebaseDatabase
import java.text.Normalizer
import java.util.Locale
import java.util.UUID
import com.app.administradorfarmadon.ActivityInventario.ui.getEffectiveStockControlMode
import com.app.administradorfarmadon.ActivityInventario.ui.getPurchasePresentationName
import com.app.administradorfarmadon.ActivityInventario.ui.isStockEntryModeValidForControlType
import com.app.administradorfarmadon.ActivityInventario.ui.resetStockEntryConfiguration
import com.app.administradorfarmadon.ActivityInventario.ui.buildInitialStockEntrySummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.app.administradorfarmadon.ClasesDatabase.FeedbackCajaController
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Color as ComposeColor
import com.google.android.material.card.MaterialCardView


import com.app.administradorfarmadon.ActivityInventario.logicainventario.CrearProductoViewModel
import com.app.administradorfarmadon.ActivityInventario.logicainventario.CrearProductoViewModel.SaveResult

class CrearProducto : AppCompatActivity() {

    private val crearProductoViewModel: CrearProductoViewModel by viewModels()

    // Feature de sugerencia de categoría por IA (Gemini).
    private val categorySuggestionViewModel: CategorySuggestionViewModel by viewModels()
    // Feature de escaneo OCR de etiquetas (cámara + Gemini Vision).
    private val labelScannerViewModel: LabelScannerViewModel by viewModels()
    private var createProductUiState by mutableStateOf(CreateProductState())
    private var createProductLoading by mutableStateOf(false)
    private var createProductSuccessSummary by mutableStateOf<ProductCreatedSummary?>(null)
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
        private const val PATH_PRODUCTOS = "Productos"
        private const val PATH_MARGENES = "ConfiguracionMargenes"
        private const val PATH_CODIGOS = "CodigosProductos"
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
            val categorySuggestionState by categorySuggestionViewModel.state.collectAsState()
            val labelScannerState by labelScannerViewModel.state.collectAsState()
            val savedPresentations by com.app.administradorfarmadon.ClasesDatabase.PresentacionesTiendaConfigManager.presentaciones.collectAsState()

            // V18.0: Sincronizar estado de identificación de barcode
            LaunchedEffect(
                categorySuggestionState.estaIdentificandoBarcode,
                categorySuggestionState.barcodeAiResult,
                categorySuggestionState.barcodeMismatchDetected,
                categorySuggestionState.barcodeMismatchOriginalName,
                categorySuggestionState.infoUsoProducto,
                categorySuggestionState.estaCargandoInfoUso,
                savedPresentations
            ) {
                createProductUiState = createProductUiState.copy(
                    isIdentifyingBarcode = categorySuggestionState.estaIdentificandoBarcode,
                    barcodeAiResult = categorySuggestionState.barcodeAiResult,
                    barcodeMismatchDetected = categorySuggestionState.barcodeMismatchDetected,
                    barcodeMismatchOriginalName = categorySuggestionState.barcodeMismatchOriginalName,
                    aiUsageInfo = categorySuggestionState.infoUsoProducto,
                    isFetchingUsageInfo = categorySuggestionState.estaCargandoInfoUso,
                    savedPresentations = savedPresentations
                )
            }

            // V16.10: Observamos la sugerencia de tipo para aplicación AUTOMÁTICA si la confianza es ALTA
            // V17.82: Restaurada la auto-selección silenciosa para un flujo sin fricciones.
            // V28.8: Sincronización robusta - Depende también de la categoría actual para evitar el "timeout" visual.
            val currentCategory = createProductUiState.category
            LaunchedEffect(categorySuggestionState.sugerenciaTipoManual, currentCategory) {
                val suggestion = categorySuggestionState.sugerenciaTipoManual
                if (suggestion != null &&
                    suggestion.confianza == com.app.administradorfarmadon.ActivityInventario.reference.ConfianzaIA.ALTA &&
                    createProductUiState.controlType == null &&
                    !createProductUiState.typeSelectedManually &&
                    currentCategory.isNotBlank()) {

                    delay(300) // Delay visual premium antes de la aplicación

                    // Verificación de contexto (P1 Fix)
                    val nombreActual = normalizeProductName(createProductUiState.name)
                    val catActual = normalizeProductName(currentCategory)
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
                if (bitmap != null) {
                    when (pendingCameraAction) {
                        "PHOTO" -> labelScannerViewModel.procesar(bitmap)
                        "INVOICE" -> {
                            val base64 = ProductUtils.bitmapToBase64(bitmap)
                            actualizarCreateProductState(createProductUiState.copy(
                                invoiceImageBase64 = base64,
                                isCapturingInvoice = false
                            ))
                        }
                    }
                } else {
                    actualizarCreateProductState(createProductUiState.copy(isCapturingInvoice = false))
                }
                pendingCameraAction = null
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
                        "INVOICE" -> cameraLauncher.launch(null)
                    }
                }
                pendingCameraAction = null
            }

            LaunchedEffect(createProductUiState.isCapturingInvoice) {
                if (createProductUiState.isCapturingInvoice && createProductUiState.invoiceImageBase64 == null) {
                    val granted = androidx.core.content.ContextCompat
                        .checkSelfPermission(this@CrearProducto, android.Manifest.permission.CAMERA) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED
                    if (granted) {
                        pendingCameraAction = "INVOICE"
                        cameraLauncher.launch(null)
                    } else {
                        pendingCameraAction = "INVOICE"
                        permissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                }
            }

            MaterialTheme {
                val saveStatus by crearProductoViewModel.saveStatus.collectAsState()

                LaunchedEffect(saveStatus) {
                    when (val status = saveStatus) {
                        is SaveResult.Success -> {
                            createProductLoading = false
                            triggerSuccessAnimation()
                            val prod = ultimoProductoCreadoCompose
                            if (prod != null) {
                                createProductSuccessSummary = ProductCreatedSummary(
                                    indice = prod.indice,
                                    name = prod.nombre,
                                    category = prod.categoria,
                                    mainPrice = MonedaHelper.formatear(prod.presentaciones.firstOrNull()?.precioventa ?: 0.0),
                                    stockAvailable = com.app.administradorfarmadon.ActivityInventario.ui.buildStockAvailableSummary(createProductUiState),
                                    lotNumber = createProductUiState.lotNumber,
                                    expirationDate = createProductUiState.expirationDate
                                )
                            }
                            crearProductoViewModel.resetStatus()
                        }
                        is SaveResult.Conflict -> {
                            createProductLoading = false
                            createProductUiState = createProductUiState.copy(
                                currentStep = CreateProductStep.PRODUCTO,
                                errors = createProductUiState.errors + ("name" to status.message)
                            )
                            triggerErrorAnimation()
                            crearProductoViewModel.resetStatus()
                        }
                        is SaveResult.Error -> {
                            createProductLoading = false
                            Toast.makeText(this@CrearProducto, status.message, Toast.LENGTH_LONG).show()
                            triggerErrorAnimation()
                            crearProductoViewModel.resetStatus()
                        }
                        else -> Unit
                    }
                }

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
                        categorySuggestionState = categorySuggestionState,
                        onSwitchToManualCategory = ::cambiarACategoriaManual,
                        onBackToAiCategory = ::volverASugerenciaIaCategoria,
                        onSearchIA = { inmediato: Boolean -> buscarConIACompose(inmediato) },
                        onApplyNameCorrection = ::aplicarCorreccionNombreIA,
                        onDismissNameCorrection = ::descartarCorreccionNombreIA,
                        onAsistManualName = { name: String ->
                            categorySuggestionViewModel.asistirManualConNombre(name, com.app.administradorfarmadon.ClasesDatabase.SessionManager.paisOperacion)
                        },
                        onAsistManualCategory = { texto: String ->
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
                        onBack = ::manejarAtrasFlujoComposeCrearProducto,
                        onStateChange = ::actualizarCreateProductState,
                        onNext = ::avanzarPasoCreateProduct,
                        onPrevious = ::retrocederPasoCreateProduct,
                        onSave = ::guardarProductoDesdeCompose,
                        onCreateAnother = ::reiniciarFlujoComposeCrearProducto,
                        onViewProduct = ::abrirProductoRecienCreado,
                        onViewSpecificProduct = { indice -> abrirEditorProducto(indice) },
                        onSavePresentation = ::guardarPresentacionEnDb,
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
                        onIdentificarBarcode = { barcode: String, image: String? ->
                            categorySuggestionViewModel.identificarBarcode(barcode, image, createProductCategoryOptions.toList())
                        },
                        onCheckBarcodeIntegrity = { barcode: String, nombre: String ->
                            categorySuggestionViewModel.verificarIntegridadBarcode(barcode, nombre)
                        },
                        onClearBarcodeIntegrityConflict = {
                            categorySuggestionViewModel.limpiarConflictoBarcode()
                        },
                        onSaveSupplier = { nombre, idFiscal -> guardarNuevoProveedor(nombre, idFiscal) },
                        showNoBarcodeConfirmDialog = showNoBarcodeConfirmDialog,
                        onDismissNoBarcodeConfirm = {
                            showNoBarcodeConfirmDialog = false
                        },
                        onConfirmNoBarcodeContinue = {
                            showNoBarcodeConfirmDialog = false
                            createProductUiState = createProductUiState.copy(
                                currentStep = CreateProductStep.LOTE_INICIAL,
                                barcode = "",
                                productoSinCodigoBarra = true,
                                barcodeAiResult = null,
                                barcodeAiApplied = false,
                                errors = createProductUiState.errors - "barcode"
                            )
                        }
                    )

                    if (createProductUiState.isBarcodeScanning) {
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
                                        lotScanned = true,
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
                        RealisticFluidSuccessAnimation(
                            onFinished = {
                                isSuccessAnimVisible = false
                                reiniciarFlujoComposeCrearProducto()
                            }
                        )
                    }
                    if (isErrorAnimVisible) {
                        ErrorAnimationOverlay()
                    }
                }

                LaunchedEffect(isSuccessAnimVisible, isErrorAnimVisible) {
                    if (isSuccessAnimVisible) {
                        updateBarsForAnimation(android.graphics.Color.TRANSPARENT, false)
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
            obtenerProveedores()
        }
    }

    private fun obtenerProveedores() {
        FirebaseDatabase.getInstance()
            .getReference(DbPaths.INVENTARIO_PROVEEDORES)
            .get()
            .addOnSuccessListener { snapshot ->
                val lista = mutableListOf<com.app.administradorfarmadon.ActivityInventario.ClasesProductos.Proveedor>()
                snapshot.children.forEach { child ->
                    child.getValue(com.app.administradorfarmadon.ActivityInventario.ClasesProductos.Proveedor::class.java)?.let {
                        lista.add(it)
                    }
                }
                actualizarCreateProductState(createProductUiState.copy(suppliers = lista))
            }
    }

    private fun guardarNuevoProveedor(nombre: String, idFiscal: String) {
        val db = FirebaseDatabase.getInstance()
        val ref = db.getReference(DbPaths.INVENTARIO_PROVEEDORES)
        val id = ref.push().key ?: UUID.randomUUID().toString()
        
        // V22.1: Guardar solo campos no vacíos para evitar nodos vacíos en Firebase
        val datosProveedor = mutableMapOf<String, Any>()
        datosProveedor["id"] = id
        datosProveedor["nombre"] = nombre
        if (idFiscal.isNotBlank()) datosProveedor["idFiscal"] = idFiscal

        val nuevo = com.app.administradorfarmadon.ActivityInventario.ClasesProductos.Proveedor(
            id = id,
            nombre = nombre,
            idFiscal = idFiscal
        )
        
        // Usar copia directa para evitar ráfagas de validación en actualizarCreateProductState
        createProductUiState = createProductUiState.copy(
            isSavingSupplier = true,
            supplierSaveSuccess = false
        )
        
        ref.child(id).setValue(datosProveedor).addOnSuccessListener {
            createProductUiState = createProductUiState.copy(
                supplierSaveSuccess = true,
                suppliers = createProductUiState.suppliers + nuevo,
                supplierId = id,
                supplierName = nombre,
                showAddSupplierDialog = false
            )

            // Esperar 5 segundos antes de ocultar el loading
            lifecycleScope.launch {
                delay(5000)
                createProductUiState = createProductUiState.copy(
                    isSavingSupplier = false,
                    supplierSaveSuccess = false
                )
            }
        }.addOnFailureListener {
            createProductUiState = createProductUiState.copy(
                isSavingSupplier = false
            )
            Toast.makeText(this, "Error al guardar proveedor", Toast.LENGTH_SHORT).show()
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
        com.app.administradorfarmadon.ClasesDatabase.PresentacionesTiendaConfigManager.precargar()
    }

    private fun cargarPresentacionesGuardadas() {
        // Feature migrada a PresentacionesTiendaConfigManager.
    }

    private fun guardarPresentacionEnDb(saved: com.app.administradorfarmadon.ActivityInventario.ui.SavedPresentation) {
        com.app.administradorfarmadon.ClasesDatabase.PresentacionesTiendaConfigManager.guardarPresentacion(saved)
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

    private fun limpiarDependenciasPorCambioTipoControl(
        estado: CreateProductState,
        tipoAnterior: CreateProductControlType?
    ): CreateProductState {
        if (estado.controlType == tipoAnterior) return estado

        return estado.copy(
            presentations = emptyList(),
            mainPresentationId = "",
            addPresentationExpanded = false,
            draftPresentationName = "",
            draftPresentationCustomMode = false,
            draftPresentationCustomAmount = "",
            draftPresentationCustomUnit = "",
            stockEntryMode = null,
            receivedUnitsText = "",
            boxesReceivedText = "",
            unitsPerBoxText = "",
            packagesPerBoxText = "",
            unitsPerPackageText = "",
            unitsPerItemText = "",
            stockEntryUnit = "",
            minimumStockText = "",
            minimumStockUnit = "",
            minimumStockUnits = 0,
            stockControlMode = null,
            stockEntryConfigured = false,
            showStockEntryDialog = false,
            errors = estado.errors - setOf(
                "controlType",
                "stockEntryMode",
                "receivedUnits",
                "boxesReceived",
                "unitsPerBox",
                "packagesPerBox",
                "unitsPerPackage",
                "unitsPerItem",
                "minimumStock",
                "presentations",
                "mainPresentation",
                "presentations_total"
            )
        )
    }

    private fun manejarAtrasFlujoComposeCrearProducto() {
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
        val estadoConPresentaciones = limpiarDependenciasPorCambioTipoControl(
            estado = nuevoEstado,
            tipoAnterior = estadoAnterior.controlType
        )

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
            val barcodeActual = createProductUiState.barcode.trim()
            // V18.0: Limpiar estados IA al cambiar código
            createProductUiState = createProductUiState.copy(
                barcodeAiResult = null,
                isIdentifyingBarcode = false,
                barcodeAiApplied = false,
                barcodeAiError = null,
                scannedImageBase64 = imageBase64,
                productoSinCodigoBarra = if (barcodeActual.isNotBlank()) false else createProductUiState.productoSinCodigoBarra
            )
            programarValidacionBarcodeCompose(createProductUiState.barcode, imageBase64)
        }

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
            
            // V20.0: Si ya pasamos el paso 1, re-evaluar info de uso al cambiar nombre
            if (createProductUiState.currentStep.number >= CreateProductStep.PRESENTACIONES.number) {
                categorySuggestionViewModel.buscarInfoUsoProducto(createProductUiState.name)
            }
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
                    inmediato = false // V19.2: No inmediato para evitar ráfagas de red durante el tipeo
                )
            }
        }
    }

    private fun cambiarACategoriaManual() {
        createProductUiState = limpiarDependenciasPorCambioTipoControl(
            estado = createProductUiState.copy(
                category = "",
                controlType = null,
                requiresPrescription = false,
                active = true,
                errors = createProductUiState.errors - "category" - "controlType"
            ),
            tipoAnterior = createProductUiState.controlType
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

        // 3. Re-lanzar búsqueda IA con el nombre estandarizado (Automático)
        // La validación de duplicados ya se dispara por el actualizarCreateProductState -> programarValidacionNombreCompose
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
            // V18.3: Optimización de debounce. Sin retraso si viene de la cámara (imageBase64 != null),
            // y 300ms (igual que validación de nombre) si se escribe a mano o con lector de código físico.
            val delayMs = if (imageBase64 != null) 0L else 300L
            delay(delayMs)

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
            delay(600) // V28.2: Debounce aumentado para garantizar escritura fluida (Premium Performance)
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
                verificarNombreDuplicadoEnFirebase(
                    nombre = currentName,
                    onExiste = { productoExistente ->
                        // Solo actualizamos si el nombre sigue siendo el mismo después de la red
                        if (normalizeProductName(createProductUiState.name) == normalized) {
                            actualizarCreateProductState(createProductUiState.copy(
                                duplicateProductFound = productoExistente,
                                isAnalyzingKeywords = false
                            ))
                        }
                    },
                    onContinuar = {
                        if (normalizeProductName(createProductUiState.name) == normalized) {
                            actualizarCreateProductState(createProductUiState.copy(
                                isAnalyzingKeywords = false,
                                isValidatingNameRemote = false
                            ))
                        }
                    },
                    alEscribir = true
                )
                return@launch
            }

            // 2. Verificar contra Firebase (caso general)
            var esDuplicadoEnFirebase = false

            verificarNombreDuplicadoEnFirebase(
                nombre = currentName,
                onExiste = { productoExistente ->
                    esDuplicadoEnFirebase = true
                    if (normalizeProductName(createProductUiState.name) == normalized) {
                        createProductUiState = createProductUiState.copy(
                            duplicateProductFound = productoExistente,
                            errors = createProductUiState.errors + ("name" to "Ya existe un producto registrado con este nombre."),
                            isValidatingNameRemote = false
                        )
                        categorySuggestionViewModel.reset()
                        actualizarCreateProductState(createProductUiState.copy(isAnalyzingKeywords = false))
                    }
                },
                onContinuar = {
                    if (!esDuplicadoEnFirebase && normalizeProductName(createProductUiState.name) == normalized) {
                        createProductUiState = createProductUiState.copy(isValidatingNameRemote = false)

                        val numWords = normalized.split(" ").filter { it.isNotBlank() }.size
                        val isStable = normalized.length >= 10 || numWords >= 2

                        if (isStable && !createProductUiState.name.endsWith(" ")) {
                            actualizarCreateProductState(createProductUiState.copy(
                                isAnalyzingKeywords = false,
                                duplicateProductFound = null
                            ))
                        } else {
                            actualizarCreateProductState(createProductUiState.copy(
                                isAnalyzingKeywords = false,
                                duplicateProductFound = null,
                                isValidatingNameRemote = false
                            ))
                        }
                    }
                },
                alEscribir = true
            )
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
                                
                                // V21.10: También buscamos el producto para el diálogo informativo
                                if (loteIndexado.productoId.isNotBlank()) {
                                    FirebaseDatabase.getInstance()
                                        .getReference(DbPaths.INVENTARIO_PRODUCTOS)
                                        .child(loteIndexado.productoId)
                                        .get()
                                        .addOnSuccessListener { prodSnap ->
                                            val prod = prodSnap.toMoldeProductos()
                                            if (prod != null) {
                                                actualizarCreateProductState(
                                                    createProductUiState.copy(
                                                        lotConflictProduct = prod,
                                                        showLotConflictDialog = true
                                                    )
                                                )
                                            }
                                        }
                                }
                            } else {
                                lotConflictInfo = "¡Cuidado! Lote asignado a: ${loteIndexado.productoNombre}"
                                lotConflictColor = android.graphics.Color.parseColor("#EF4444")
                                lotConflictSeverity = 2
                                
                                // V21.10: Buscar el objeto producto completo para el diálogo de conflicto
                                if (loteIndexado.productoId.isNotBlank()) {
                                    FirebaseDatabase.getInstance()
                                        .getReference(DbPaths.INVENTARIO_PRODUCTOS)
                                        .child(loteIndexado.productoId)
                                        .get()
                                        .addOnSuccessListener { prodSnap ->
                                            val prod = prodSnap.toMoldeProductos()
                                            if (prod != null) {
                                                actualizarCreateProductState(
                                                    createProductUiState.copy(
                                                        lotConflictProduct = prod,
                                                        showLotConflictDialog = true
                                                    )
                                                )
                                            }
                                        }
                                }
                            }
                        }
                    } else {
                        lotConflictInfo = "Lote disponible"
                        lotConflictColor = android.graphics.Color.parseColor("#0E8F63")
                        lotConflictSeverity = 0
                        actualizarCreateProductState(
                            createProductUiState.copy(
                                lotConflictProduct = null,
                                showLotConflictDialog = false
                            )
                        )
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

        createProductUiState = estadoActual.copy(
            currentStep = siguientePaso
        )

        if (siguientePaso == CreateProductStep.PRESENTACIONES) {
            asegurarPresentacionesGuardadasCargadas()
            categorySuggestionViewModel.buscarInfoUsoProducto(estadoActual.name)
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
        createProductUiState = createProductUiState.copy(lotScanned = false)

        // Reinicia también el ciclo de sugerencias IA: limpia el set de
        // rechazos y cualquier sugerencia visible para el próximo producto.
        categorySuggestionViewModel.reset()
        labelScannerViewModel.reset()
        limpiarFormularioCompose()
    }

    private fun limpiarFormularioCompose() {
        val proveedoresActuales = createProductUiState.suppliers
        createProductUiState = construirEstadoInicialCreateProduct().copy(
            suppliers = proveedoresActuales
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
        // Solo mientras la IA identifica un código ya escaneado (sin delay extra; suelta al terminar)
        if (estado.barcode.trim().isNotBlank() && estado.isIdentifyingBarcode) return false

        // V17.86: Bloqueo explícito si se encontró un producto duplicado (Sincronización con UI)
        if (estado.duplicateProductFound != null) return false

        // El botón se enciende si hay datos escritos
        if (estado.name.trim().isBlank()) return false
        if (estado.category.trim().isBlank()) return false
        if (estado.controlType == null) return false
        if (estado.location.trim().isBlank()) return false

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
        val stockMinimo = calculateBaseMinimumStock(estado)
        if (stockMinimo <= 0.0) return false

        // El cálculo del stock base para la alerta se hace internamente
        // al guardar, aquí solo validamos que la opción elegida sea positiva.

        val costo = estado.purchaseCost
            .replace(com.app.administradorfarmadon.ClasesDatabase.SessionManager.monedaSimbolo, "", ignoreCase = true)
            .trim()
            .replace(",", ".")
            .toDoubleOrNull()
        if (costo == null || costo <= 0.0) return false

        // V27.0: Validación de Condición de Pago
        if (estado.paymentCondition == "CREDITO" && !esFechaCompletaValida(estado.paymentDueDate)) return false

        return true
    }

    private fun puedeAvanzarPasoPresentaciones(estado: CreateProductState): Boolean {
        if (estado.presentations.isEmpty()) return false
        val recibidoBase = calculateTotalBaseStock(estado)
        var totalAsignado = 0.0

        estado.presentations.forEach { presentation ->
            val equivRaw = presentation.equivalenceText.trim().replace(",", ".")
            val equivDouble = equivRaw.toDoubleOrNull() ?: 0.0

            // Requerimos que la equivalencia sea un entero positivo.
            // Evita que el UI acepte decimales que luego se truncarían al guardar
            // (presentacion.cantidad es Int). Si viene como decimal, lo marcamos
            // inválido para prevenir inconsistencias.
            if (equivDouble <= 0.0 || equivDouble % 1.0 != 0.0) return false

            val equivalencia = equivDouble.toInt()

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

                if (estado.location.trim().isBlank()) {
                    errors["location"] = "Ingresa la ubicación física del producto"
                } else {
                    errors.remove("location")
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
                        val quantity = parseComposeNumber(estado.receivedUnitsText)
                        val content = parseComposeNumber(estado.unitsPerItemText)

                        if (quantity <= 0.0) {
                            errors["receivedUnits"] = "Ingresa la cantidad recibida"
                        } else {
                            errors.remove("receivedUnits")
                        }

                        if (estado.controlType != CreateProductControlType.UNIDAD) {
                            if (content <= 0.0) {
                                errors["unitsPerItem"] = when (estado.controlType) {
                                    CreateProductControlType.LIQUIDO -> "Ingresa el contenido por frasco"
                                    CreateProductControlType.PESO -> "Ingresa el peso por envase"
                                    else -> "Ingresa el contenido"
                                }
                            } else {
                                errors.remove("unitsPerItem")
                            }
                        }

                        if (estado.controlType == CreateProductControlType.UNIDAD) errors.remove("unitsPerItem")
                        errors.remove("boxesReceived")
                        errors.remove("unitsPerBox")
                        errors.remove("packagesPerBox")
                        errors.remove("unitsPerPackage")
                    }

                    CreateProductStockEntryMode.CAJA -> {
                        val boxes = parseComposeNumber(estado.boxesReceivedText)
                        val unitsPerBox = parseComposeNumber(estado.unitsPerBoxText)
                        val content = parseComposeNumber(estado.unitsPerItemText)

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

                        if (estado.controlType != CreateProductControlType.UNIDAD) {
                            if (content <= 0.0) {
                                errors["unitsPerItem"] = when (estado.controlType) {
                                    CreateProductControlType.LIQUIDO -> "Ingresa el contenido por frasco"
                                    CreateProductControlType.PESO -> "Ingresa el peso por envase"
                                    else -> "Ingresa el contenido"
                                }
                            } else {
                                errors.remove("unitsPerItem")
                            }
                        } else {
                            errors.remove("unitsPerItem")
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
                val minimumMode = resolveMinimumStockControlMode(estado)
                val recibidoFisico = com.app.administradorfarmadon.ActivityInventario.ui.calculateTotalPhysicalUnits(estado)

                // La validación ahora usa minimumStockUnits en lugar del campo de texto antiguo.
                if (minimumStockBase <= 0.0) {
                    errors["minimumStock"] = "Selecciona una cantidad de alerta"
                } else if (
                    minimumMode == StockControlMode.INDIVISIBLE &&
                    estado.minimumStockUnits > recibidoFisico
                ) {
                    errors["minimumStock"] = "La alerta no puede superar la cantidad de productos completos"
                } else if (
                    minimumMode == StockControlMode.DIVISIBLE &&
                    estado.stockEntryMode != null &&
                    minimumStockBase > recibidoEnBase
                ) {
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

                // V27.0: Validación de Pago a Crédito
                if (estado.paymentCondition == "CREDITO") {
                    if (!esFechaCompletaValida(estado.paymentDueDate)) {
                        errors["paymentDueDate"] = "Selecciona una fecha válida (DD/MM/AAAA)"
                    } else if (estaFechaPasada(estado.paymentDueDate)) {
                        errors["paymentDueDate"] = "La fecha de pago ya pasó"
                    } else {
                        errors.remove("paymentDueDate")
                    }
                } else {
                    errors.remove("paymentDueDate")
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
                    val equivRaw = presentation.equivalenceText.trim().replace(",", ".")
                    val equivDouble = equivRaw.toDoubleOrNull() ?: 0.0

                    // Validamos que la equivalencia sea un entero positivo
                    if (equivDouble <= 0.0 || equivDouble % 1.0 != 0.0) {
                        errors["equivalence_${presentation.id}"] = "Equivalencia inválida (debe ser entero positivo)"
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

                    // Sumar la equivalencia como entero para validaciones coherentes con el guardado
                    if (equivDouble > 0.0 && equivDouble % 1.0 == 0.0) {
                        totalAsignado += equivDouble.toInt()
                    }
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
        
        return try {
            val expiryYearMonth = YearMonth.of(fullYear, month)
            val currentYearMonth = YearMonth.now()
            expiryYearMonth.isBefore(currentYearMonth)
        } catch (e: Exception) {
            false
        }
    }

    private fun esFechaCompletaValida(value: String): Boolean {
        val cleaned = value.trim()
        if (!cleaned.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) return false
        return try {
            val parts = cleaned.split("/")
            LocalDate.of(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun estaFechaPasada(value: String): Boolean {
        if (!esFechaCompletaValida(value)) return false
        return try {
            val parts = value.trim().split("/")
            val date = LocalDate.of(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
            date.isBefore(LocalDate.now())
        } catch (e: Exception) {
            false
        }
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
        onContinuar: () -> Unit,
        alEscribir: Boolean = false
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
                                    createProductLoading = false
                                    if (alEscribir) {
                                        if (normalizeProductName(createProductUiState.name) == normalized) {
                                            onContinuar()
                                        }
                                        Toast.makeText(
                                            this,
                                            "No se pudo verificar este nombre. Prueba otro o continúa.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        mostrarErrorGuardado(
                                            "Error de integridad: El nombre existe pero los datos del producto no. Por favor, contacte a soporte."
                                        )
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                createProductLoading = false
                                if (alEscribir) {
                                    if (normalizeProductName(createProductUiState.name) == normalized) {
                                        onContinuar()
                                    }
                                    Toast.makeText(
                                        this,
                                        "No se pudo verificar si el nombre ya existe. Puedes continuar o reintentar.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    if (normalizeProductName(createProductUiState.name) == normalized) {
                                        actualizarCreateProductState(createProductUiState.copy(
                                            isAnalyzingKeywords = false,
                                            isValidatingNameRemote = false
                                        ))
                                    }
                                    mostrarErrorGuardado(
                                        "No se pudo verificar el producto debido a un error de red: ${e.message}. Intente de nuevo."
                                    )
                                }
                            }
                    } else {
                        createProductLoading = false
                        if (alEscribir) {
                            if (normalizeProductName(createProductUiState.name) == normalized) {
                                onContinuar()
                            }
                            Toast.makeText(
                                this,
                                "No se pudo verificar este nombre. Prueba otro o continúa.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            mostrarErrorGuardado(
                                "Error de integridad: Se encontró el nombre pero el índice está corrupto. Guardado bloqueado por seguridad."
                            )
                        }
                    }
                } else {
                    // No existe, procedemos normal
                    onContinuar()
                }
            }
            .addOnFailureListener { _ ->
                createProductLoading = false
                if (alEscribir) {
                    if (normalizeProductName(createProductUiState.name) == normalized) {
                        onContinuar()
                    }
                    Toast.makeText(
                        this,
                        "No se pudo verificar si el nombre ya existe. Puedes continuar o reintentar.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    mostrarErrorGuardado(
                        "No se puede validar la existencia del producto (Error de red). Guardado cancelado para evitar duplicados."
                    )
                }
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
                    val conflictProd = snapshot.toMoldeProductos()
                    if (conflictProd != null) {
                        actualizarCreateProductState(
                            createProductUiState.copy(
                                lotConflictProduct = conflictProd,
                                showLotConflictDialog = true
                            )
                        )
                    } else {
                        mostrarErrorGuardado("Conflicto de ID: Ya existe un registro con este código pero no se puede leer. Por favor, intenta con un nombre ligeramente diferente.")
                    }
                } else {
                    guardarProductoFinalCompose(producto)
                }
            }
            .addOnFailureListener { e ->
                mostrarErrorGuardado(e.message)
            }
    }

    private fun guardarProductoFinalCompose(producto: MoldeProductos) {
        // Preparar operación atómica (V3.16)
        val updates = mutableMapOf<String, Any?>()

        // 1. Datos del producto
        val productPath = "${DbPaths.INVENTARIO_PRODUCTOS}/${producto.indice}"
        updates[productPath] = productoParaFirebase(producto)

        val presentacionesPath = "${DbPaths.INVENTARIO_PRODUCTO_PRESENTACIONES}/${producto.indice}"
        val lotesPath = "${DbPaths.INVENTARIO_PRODUCTO_LOTES}/${producto.indice}"
        val fichaPath = "${DbPaths.INVENTARIO_FICHA_TECNICA}/${producto.indice}"
        
        updates[presentacionesPath] = relacionesPresentacionesParaFirebase(producto)
        updates[lotesPath] = relacionesLotesParaFirebase(producto)
        updates[fichaPath] = fichaTecnicaParaFirebase(producto)

        // V22.0: Generar Índices de Búsqueda Inversa (ElasticSearch style)
        val searchIndices = com.app.administradorfarmadon.ActivityFragmentos.Fragmentos.logicainventario.BuscadorIndicesManager.prepararIndicesParaFirebase(producto)
        updates.putAll(searchIndices)

        // V23.0: Actualización de Contadores de Resumen (Atómica)
        val estadoVencimiento = com.app.administradorfarmadon.ActivityInventario.ProductUtils.obtenerEstadoVencimiento(producto)
        if (estadoVencimiento == "VENCIDO") {
            updates["Inventario/Resumen/conteoVencidos"] = com.google.firebase.database.ServerValue.increment(1)
        } else if (estadoVencimiento == "POR_VENCER") {
            updates["Inventario/Resumen/conteoPorVencer"] = com.google.firebase.database.ServerValue.increment(1)
        }

        val current = producto.stockFisicoBase()
        val min = producto.stockMinimoBase()
        if (current < min && min > 0) {
            updates["Inventario/Resumen/conteoStockBajo"] = com.google.firebase.database.ServerValue.increment(1)
        }

        if (!producto.tieneCodigoBarra && producto.codigo.isBlank()) {
            updates["Inventario/Resumen/conteoSinCodigo"] = com.google.firebase.database.ServerValue.increment(1)
        }

        // Indexar categoría si es nueva
        if (producto.categoria.isNotBlank()) {
            updates["Inventario/Resumen/categorias/${producto.categoria}"] = true
        }

        // 1.1 Código de barras (solo si el producto tiene codigo registrado)
        if (producto.tieneCodigoBarra && producto.codigo.isNotBlank()) {
            updates["${DbPaths.INVENTARIO_CODIGOS}/${producto.codigo}"] = mapOf(
                "productoId" to producto.indice,
                "productoNombre" to producto.nombre,
                "categoria" to producto.categoria
            )
        }

        // 3. Índice plano de lotes (V17.46)
        producto.lotes.forEach { (loteId, lote) ->
            val numLote = lote.numero.trim().uppercase()
            if (numLote.isNotBlank()) {
                val keyLote = ProductUtils.encodeLotKey(numLote)
                val registroLote = LoteIndexado(
                    numero = numLote,
                    productoId = producto.indice,
                    productoNombre = producto.nombre,
                    loteId = loteId,
                    lotePath = "${DbPaths.INVENTARIO_PRODUCTO_LOTES}/${producto.indice}/$loteId",
                    vencimiento = lote.vencimiento
                )
                updates["${DbPaths.INVENTARIO_LOTES_POR_NUMERO}/$keyLote"] = registroLote

                // V26.1: Generar el primer registro del Kardex (Entrada Inicial)
                val movId = UUID.randomUUID().toString()
                val primerMovimiento = com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MovimientoInventario(
                    id = movId,
                    productoId = producto.indice,
                    loteId = loteId,
                    numeroLote = numLote,
                    tipo = "COMPRA",
                    cantidad = lote.cantidad,
                    stockAnterior = 0.0,
                    stockResultante = lote.cantidad,
                    referencia = if (producto.proveedorNombre.isNotBlank()) "Compra - ${producto.proveedorNombre}" else "Registro Inicial / Compra",
                    fecha = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(java.util.Date()),
                    usuarioNombre = com.app.administradorfarmadon.ClasesDatabase.SessionManager.nombreCajera,
                    unidadVisual = producto.unidadVisualInventario
                )
                updates["${DbPaths.INVENTARIO_MOVIMIENTOS}/${producto.indice}/$movId"] = primerMovimiento

                // V27.1: Registro del Movimiento Financiero (Crédito o Contado)
                val ctaId = UUID.randomUUID().toString()
                val datosPago = mutableMapOf<String, Any?>(
                    "id" to ctaId,
                    "nroFactura" to lote.nroFactura,
                    "proveedorId" to producto.proveedorId,
                    "proveedorNombre" to producto.proveedorNombre,
                    "totalAPagar" to (producto.preciodecompra.toDoubleOrNull() ?: 0.0),
                    "condicion" to lote.condicionPago,
                    "estadoPago" to if (lote.condicionPago == "CREDITO") "PENDIENTE" else "PAGADO",
                    "fechaCompra" to java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date()),
                    "fechaVencimientoPago" to (if (lote.condicionPago == "CREDITO") lote.fechaVencimientoPago else lote.fecha),
                    "productoId" to producto.indice,
                    "productoNombre" to producto.nombre,
                    "desgloseIngreso" to buildInitialStockEntrySummary(createProductUiState),
                    "unidadVisual" to producto.unidadVisualInventario,
                    "fotoFactura" to createProductUiState.invoiceImageBase64
                )
                
                // Si es crédito, va a CuentasPorPagar para seguimiento de deuda
                if (lote.condicionPago == "CREDITO") {
                    updates["${DbPaths.INVENTARIO_CUENTAS_POR_PAGAR}/$ctaId"] = datosPago
                }
                
                // Siempre va al historial general de pagos a proveedores (Tener esa info siempre)
                updates["${DbPaths.INVENTARIO_PAGOS_PROVEEDORES}/$ctaId"] = datosPago
            }
        }

        // V28.0: Delegar el guardado al ViewModel para usar Reserva Atómica
        val normalized = normalizeProductName(producto.nombre)
        val nameKey = buildNormalizedNameKey(normalized)
        val finalUpdates = mutableMapOf<String, Any>()
        updates.forEach { (k, v) -> if (v != null) finalUpdates[k] = v }
        crearProductoViewModel.guardarProductoBlindado(producto, nameKey, finalUpdates)
    }

    private fun triggerSuccessAnimation() {
        isSuccessAnimVisible = true
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

        // V21.5: Habilitar dibujo detrás de las barras si es transparente
        WindowCompat.setDecorFitsSystemWindows(window, color != android.graphics.Color.TRANSPARENT)
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

        // V21.0: Sincronización Profesional de Unidades Visuales
        // El stockDisponibleBase siempre está en unidad mínima (g, mL, Unidad)
        val stockDisponibleBase = calculateTotalBaseStock(state)
        val stockMinimoBase = calculateBaseMinimumStock(state)
        
        // Normalización Crítica: Siempre guardar en unidad mínima (g, mL, Unidad)
        val unidadBase = when (controlType) {
            CreateProductControlType.UNIDAD -> "Unidad"
            CreateProductControlType.PESO -> "g"
            CreateProductControlType.LIQUIDO -> "mL"
        }
        val unidadVisualInventario = when (controlType) {
            CreateProductControlType.UNIDAD -> "Unidad"
            CreateProductControlType.PESO -> state.stockEntryUnit.ifBlank { "g" }
            CreateProductControlType.LIQUIDO -> state.stockEntryUnit.ifBlank { "mL" }
        }

        val effectiveMode = resolveMinimumStockControlMode(state)
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

        val unidadesPorPresentacionCompraRaw = when (state.stockEntryMode) {
            null -> 1.0
            CreateProductStockEntryMode.UNIDAD -> {
                if (controlType == CreateProductControlType.UNIDAD) {
                    val content = parseComposeNumber(state.unitsPerItemText)
                    if (content > 0.0) content else 1.0
                }
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
        }

        val unidadesPorPresentacionCompra = (if (state.stockEntryUnit == "kg" || state.stockEntryUnit == "L") {
            unidadesPorPresentacionCompraRaw * 1000.0
        } else {
            unidadesPorPresentacionCompraRaw
        }).toInt().coerceAtLeast(1)

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
            cantidad = stockDisponibleBase,
            fecha = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(java.util.Date()),
            costoCompraUnitario = calcularCostoUnitario(precioCompra, stockDisponibleBase),
            costoUltimoIngreso = precioCompra.toDoubleOrNull() ?: 0.0,
            costoUltimoIngresoUnitario = calcularCostoUnitario(precioCompra, stockDisponibleBase),
            nroFactura = state.invoiceNumber,
            condicionPago = state.paymentCondition,
            fechaVencimientoPago = state.paymentDueDate,
            estadoPago = if (state.paymentCondition == "CREDITO") "PENDIENTE" else "PAGADO"
        )

        val codigoBarra = state.barcode.trim()
        val tieneCodigoBarra = codigoBarra.isNotBlank() && !state.productoSinCodigoBarra

        return MoldeProductos(
            nombre = nombreProducto,
            codigo = if (tieneCodigoBarra) codigoBarra else "",
            tieneCodigoBarra = tieneCodigoBarra,
            vencimiento = state.expirationDate.trim(),
            categoria = categoriaProducto,
            preciodecompra = precioCompra,
            cantidadinicial = formatearCantidadCompose(stockDisponibleBase),
            stockminimo = formatearCantidadCompose(stockMinimoBase),
            stockMinimoContenedores = if (effectiveMode == StockControlMode.INDIVISIBLE) {
                state.minimumStockUnits
            } else {
                stockMinimoBase.toInt() // Valor en unidad mínima para alertas rápidas
            },
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
            ubicacion = state.location,
            proveedorId = state.supplierId,
            proveedorNombre = state.supplierName,
            unidadesPorPresentacionCompra = unidadesPorPresentacionCompra,
            basePorUnidad = calcularBasePorUnidadProfesional(state, controlType),
            imagenUrl = "",
            lotes = mapOf(claveLote to primerLote),
            // V20.0: Poblado de información de referencia IA para Step 4 y ficha técnica
            referenceUseCases = state.aiUsageInfo?.usos ?: emptyList(),
            referenceHowToUse = state.aiUsageInfo?.instrucciones.orEmpty(),
            referenceWarnings = listOfNotNull(state.aiUsageInfo?.contraindicaciones).filter { it.isNotBlank() }
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
        data["tieneCodigoBarra"] = producto.tieneCodigoBarra
        if (producto.tieneCodigoBarra) {
            putText("codigo", producto.codigo)
        } else {
            data["codigo"] = ""
        }
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
        putText("ubicacion", producto.ubicacion)
        putText("proveedorId", producto.proveedorId)
        putText("proveedorNombre", producto.proveedorNombre)

        data["requierereceta"] = producto.requierereceta
        data["estadodelproducto"] = producto.estadodelproducto
        putText("indice", producto.indice)
        data["tienePresentaciones"] = producto.tienePresentaciones
        putNumber("unidadesPorPresentacionCompra", producto.unidadesPorPresentacionCompra)
        putNumber("basePorUnidad", producto.basePorUnidad)
        putText("imagenUrl", producto.imagenUrl)
        putText("loteConsumoSeleccionado", producto.loteConsumoSeleccionado)
        if (producto.loteConsumoSeleccionManual) data["loteConsumoSeleccionManual"] = true

        return data
    }

    private fun fichaTecnicaParaFirebase(producto: MoldeProductos): Map<String, Any?> {
        val data = linkedMapOf<String, Any?>()
        
        if (producto.referenceUseCases.isNotEmpty()) data["referenceUseCases"] = producto.referenceUseCases
        if (producto.referenceHowToUse.isNotBlank()) data["referenceHowToUse"] = producto.referenceHowToUse
        if (producto.referenceWarnings.isNotEmpty()) data["referenceWarnings"] = producto.referenceWarnings
        
        if (producto.referenceCommonUse.isNotBlank()) data["referenceCommonUse"] = producto.referenceCommonUse
        if (producto.referenceNotRecommendedFor.isNotEmpty()) data["referenceNotRecommendedFor"] = producto.referenceNotRecommendedFor
        if (producto.referenceKeywords.isNotEmpty()) data["referenceKeywords"] = producto.referenceKeywords
        
        if (producto.referenceSourceName.isNotBlank()) data["referenceSourceName"] = producto.referenceSourceName
        if (producto.referenceSourceUrl.isNotBlank()) data["referenceSourceUrl"] = producto.referenceSourceUrl
        
        if (producto.referenceConfidence > 0.0) data["referenceConfidence"] = producto.referenceConfidence
        if (producto.referenceLanguage.isNotBlank()) data["referenceLanguage"] = producto.referenceLanguage

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
        putText("nroFactura", lote.nroFactura)
        putText("condicionPago", lote.condicionPago)
        putText("fechaVencimientoPago", lote.fechaVencimientoPago)
        putText("estadoPago", lote.estadoPago)

        return data
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
        if (effectiveMode == StockControlMode.DIVISIBLE) {
            return when (controlType) {
                CreateProductControlType.PESO -> state.stockEntryUnit.ifBlank { "g" }
                CreateProductControlType.LIQUIDO -> state.stockEntryUnit.ifBlank { "mL" }
                CreateProductControlType.UNIDAD -> "unidades"
            }
        }

        // Para INDIVISIBLE, la unidad es el contenedor (Frasco, Blíster, etc.)
        return getPhysicalUnitLabel(state, 1)
    }

    private fun calcularBasePorUnidadProfesional(
        state: CreateProductState,
        controlType: CreateProductControlType
    ): Double {
        val rawBase = when (state.stockEntryMode) {
            CreateProductStockEntryMode.UNIDAD,
            CreateProductStockEntryMode.CAJA -> parseComposeNumber(state.unitsPerItemText)
            CreateProductStockEntryMode.CAJA_CON_PAQUETES -> parseComposeNumber(state.unitsPerPackageText)
            null -> 0.0
        }

        val result = rawBase.takeIf { it > 0.0 } ?: 0.0

        if (controlType == CreateProductControlType.UNIDAD) return result

        // Normalizar a unidad m\u00ednima (g o mL)
        return if (state.stockEntryUnit == "kg" || state.stockEntryUnit == "L") {
            result * 1000.0
        } else {
            result
        }
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
        
        // V21.9: Resumen de stock escalado profesional (kg/L)
        val factor = if (producto.unidadVisualInventario == "kg" || producto.unidadVisualInventario == "L") 1000.0 else 1.0
        val baseCant = producto.cantidadinicial.toDoubleOrNull() ?: 0.0
        val visualCant = baseCant / factor
        val stockTexto = "${formatearCantidadCompose(visualCant)} ${producto.unidadVisualInventario}"

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

    private fun abrirProductoRecienCreado() {
        val indice = createProductSuccessSummary?.indice.orEmpty()
        abrirEditorProducto(indice)
    }

    private fun abrirEditorProducto(indice: String) {
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
    private fun RealisticFluidSuccessAnimation(onFinished: () -> Unit) {
        val infiniteTransition = rememberInfiniteTransition(label = "fluid")

        // Animaciones de fase para el movimiento de "agua"
        val phase1 by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart), label = "p1"
        )
        val phase2 by infiniteTransition.animateFloat(
            initialValue = 360f, targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart), label = "p2"
        )

        val colors = listOf(
            ComposeColor(0xFFD32F2F), // Rojo
            ComposeColor(0xFF1976D2), // Azul
            ComposeColor(0xFF388E3C), // Verde
            ComposeColor(0xFF7B1FA2), // Morado
            ComposeColor(0xFFD32F2F)  // Ciclo
        )

        LaunchedEffect(Unit) {
            // Sonido sintético premium de 5 segundos
            feedbackController.ventaExitosaLong(5000)
            delay(5000)
            onFinished()
        }

        Box(modifier = Modifier.fillMaxSize().background(ComposeColor.Black)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Capa 1: Movimiento horizontal
                drawRect(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = colors,
                        start = Offset(canvasWidth * kotlin.math.cos(Math.toRadians(phase1.toDouble())).toFloat(), 0f),
                        end = Offset(canvasWidth * kotlin.math.sin(Math.toRadians(phase1.toDouble())).toFloat(), canvasHeight)
                    ),
                    alpha = 0.6f
                )

                // Capa 2: Movimiento diagonal (Interferencia tipo agua)
                drawRect(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = colors.reversed(),
                        center = Offset(
                            canvasWidth / 2 + (canvasWidth / 3 * kotlin.math.cos(Math.toRadians(phase2.toDouble()))).toFloat(),
                            canvasHeight / 2 + (canvasHeight / 3 * kotlin.math.sin(Math.toRadians(phase2.toDouble()))).toFloat()
                        ),
                        radius = canvasWidth * 1.5f
                    ),
                    blendMode = androidx.compose.ui.graphics.BlendMode.Screen,
                    alpha = 0.5f
                )
            }

            // Ícono central con sutil pulso
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f, targetValue = 1.2f,
                animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale"
            )

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = ComposeColor.White,
                    modifier = Modifier.size(150.dp).graphicsLayer(scaleX = scale, scaleY = scale)
                )
            }
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
