package com.app.administradorfarmadon.ActivityInventario.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.animation.core.Spring

import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Brush
import com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestion
import com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionStatus
import com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState
import com.app.administradorfarmadon.ActivityInventario.reference.ConfianzaIA
import com.app.administradorfarmadon.ActivityInventario.reference.TipoControlDetectado
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material3.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material.icons.outlined.AllInbox
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.R
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

val CreateBackground = Color(0xFFF7F9FC)
val CreateBorder = Color(0xFFE5EAF0)
val CreateGreen = Color(0xFF15A05C)
val CreateGreenSoft = Color(0xFFF1FBF5)
val CreateOrangeSoft = Color(0xFFFFF5EB)
val CreateTextPrimary = Color(0xFF111827)
val CreateTextSecondary = Color(0xFF667085)
val CreateRed = Color(0xFFD92D20)
val CreateOrange = Color(0xFFE17B00)
val CreateBlue = Color(0xFF15A05C)
val CreateBlueSoft = Color(0xFFF1FBF5)
val CreateAiFocus = Color(0xFFF6C343)
val CreateAiFocusSoft = Color(0xFFFFF7D6)
val CreateInfoGraySoft = Color(0xFFF8FAFC)
val CreateSurfaceSoft = Color(0xFFF8FAFC)
val CreateYellow = Color(0xFFF5B820)
val CreateYellowSoft = Color(0xFFFFF4CC)

// Gradiente Premium para Diálogos (Verde suave a Azul brillante)
val PremiumGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFE8F5E9), // Verde muy claro
        Color(0xFFE0F7FA), // Azul muy claro
        Color(0xFFB2EBF2), // Azul brillante
    )
)

// Gradiente Vivo Tipo Agua (Verde suave a Morado suave)
val WaterGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFE8F5E9), // Verde muy claro
        Color(0xFFF3E5F5), // Morado muy claro
        Color(0xFFE0F2F1), // Cian agua claro
    )
)

// Gradiente de Advertencia (Rojo a Naranja)
val WarningGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFD92D20), // Rojo Farmadon
        Color(0xFFE17B00), // Naranja
    )
)

// Gradiente Azul Eléctrico Premium (Moderno)
val ElectricBlueGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1E3A8A), // Azul muy oscuro (Slate)
        Color(0xFF2563EB), // Azul Eléctrico (Blue 600)
        Color(0xFF60A5FA), // Azul Claro brillante
    )
)

private enum class ExpirationStatusKind {
    VIGENTE,
    CORTO_PLAZO,
    UN_MES,
    POR_VENCER,
    VENCIDO,
    INVALIDO
}

private data class ExpirationStatus(
    val kind: ExpirationStatusKind,
    val message: String,
    val color: Color
)

enum class CreateProductStep(val number: Int, val title: String, val subtitle: String) {
    PRODUCTO(1, "Producto", ""),
    LOTE_INICIAL(2, "Registro del Lote", ""),
    PRESENTACIONES(3, "Presentaciones de ventas", ""),
    RESUMEN(4, "Resumen del producto", "")
}

enum class CreateProductControlType(
    val label: String,
    val helper: String,
    val example: String,
    val baseUnitLabel: String
)
{
    UNIDAD(
        label = "Unidad",
        helper = "Se cuenta por piezas o unidades",
        example = "Tabletas, capsulas, ampollas",
        baseUnitLabel = "unidades"
    ),
    PESO(
        label = "Peso",
        helper = "Se mide por gramos o kilogramos",
        example = "Polvos, cremas, pomadas",
        baseUnitLabel = "g"
    ),
    LIQUIDO(
        label = "Liquido",
        helper = "Se mide por ml o litros",
        example = "Jarabes, alcohol, soluciones",
        baseUnitLabel = "mL"
    )
}

private fun TipoControlDetectado.toCreateProductControlTypeOrNull(): CreateProductControlType? {
    return when (this) {
        TipoControlDetectado.UNIDAD -> CreateProductControlType.UNIDAD
        TipoControlDetectado.PESO -> CreateProductControlType.PESO
        TipoControlDetectado.LIQUIDO -> CreateProductControlType.LIQUIDO
        else -> null
    }
}

enum class CreateProductStockEntryMode(
    val label: String,
    val helper: String
) {
    UNIDAD(
        label = "Unidades sueltas",
        helper = "Recibiste tabletas, cápsulas, frascos individuales (sin caja)"
    ),
    CAJA(
        label = "Caja con unidades directas",
        helper = "Cada caja contiene una cantidad en unidades base (ej. caja de 60 tabletas)"
    ),
    CAJA_CON_PAQUETES(
        label = "Caja con frascos / blísteres",
        helper = "Cada caja contiene varios frascos o blísteres, y cada uno tiene su cantidad (ej. 10 cajas x 5 frascos x 120 mL)"
    )
}

enum class StockControlMode {
    INDIVISIBLE, // Control por envase (frasco, caja, etc.)
    DIVISIBLE    // Control por unidad (tableta, cápsula, etc.)
}

@Immutable
data class CreateProductPresentation(
    val id: String,
    val name: String,
    val equivalenceText: String,
    val salePriceText: String,
    // Si fue propuesta por la IA. Se muestra un ✨ a su lado. Se vuelve false
    // en cuanto el usuario edita el nombre o la equivalencia, para indicar
    // que ya es de él (no de la IA).
    val isAiSuggested: Boolean = false,
    val marketPriceReference: Double? = null,
    val marketConfidence: Int? = null,
    val aiDescription: String? = null,
    val isGeneric: Boolean = false,
    val isBrand: Boolean = false
)

data class CategoryMarginRule(
    val category: String = "",
    val minMarginPercent: Double = 0.0,
    val suggestedMarginPercent: Double = 0.0
)

@Immutable
data class SmartProductHint(
    val category: String? = null,
    val controlType: CreateProductControlType? = null,
    val suggestedPresentations: List<String> = emptyList(),
    val suggestedReceivedPresentation: String? = null,
    val confidence: Float = 0f,
    val sourceLabel: String? = null
)

@Immutable
data class ProductCreatedSummary(
    val indice: String,
    val name: String,
    val category: String,
    val mainPrice: String,
    val stockAvailable: String,
    val lotNumber: String,
    val expirationDate: String
)

@Immutable
data class SavedPresentation(
    val id: String = "",
    val name: String = "",
    val equivalenceBase: String = "", // en unidades base: g, mL o cantidad
    val controlType: String = ""      // "UNIDAD" | "PESO" | "LIQUIDO"
) {
    // Alias para compatibilidad con código antiguo
    val nombreCanonical: String get() = id
    val nombreVisible: String get() = name
}

data class CreateProductState(
    val currentStep: CreateProductStep = CreateProductStep.PRODUCTO,
    val name: String = "",
    val barcode: String = "",
    val isBarcodeManualMode: Boolean = false,
    val isBarcodeScanning: Boolean = false,
    val isLabelScanning: Boolean = false,
    val category: String = "",
    val location: String = "",
    val supplierId: String = "",
    val supplierName: String = "",
    val suppliers: List<com.app.administradorfarmadon.ActivityInventario.ClasesProductos.Proveedor> = emptyList(),
    val showAddSupplierDialog: Boolean = false,
    val controlType: CreateProductControlType? = null,
    val typeSelectedManually: Boolean = false,
    val categorySelectedFromAi: Boolean = false,
    val categoryStatus: String? = null,
    val requiresPrescription: Boolean = false,
    val active: Boolean = true,
    val pendingHighlight: Boolean = false,
    val lastAppliedCorrection: String = "",
    val dismissedCorrections: Set<String> = emptySet(),
    val duplicateProductFound: MoldeProductos? = null,
    val isValidatingNameRemote: Boolean = false,
    val isAnalyzingKeywords: Boolean = false,

    val lotNumber: String = "",
    val expirationDate: String = "",
    val lotScanned: Boolean = false,
    val invoiceNumber: String = "",
    val invoiceImageBase64: String? = null,
    val isCapturingInvoice: Boolean = false,
    val paymentCondition: String = "CONTADO", // CONTADO o CREDITO
    val paymentDueDate: String = "",

    val forceManualProductEntry: Boolean = false,
    val barcodeAiResult: com.app.administradorfarmadon.ActivityInventario.reference.BarcodeAiResult? = null,
    val isIdentifyingBarcode: Boolean = false,
    val barcodeAiError: String? = null,
    val barcodeAiApplied: Boolean = false,
    val scannedImageBase64: String? = null,

    // Campos viejos: los dejamos para no romper otras funciones existentes.
    val receivedPresentation: String = "",
    val receivedQuantity: String = "",

    // Nuevo flujo facil para entrada de inventario.
    val stockEntryMode: CreateProductStockEntryMode? = null,
    val receivedUnitsText: String = "",
    val boxesReceivedText: String = "",
    val unitsPerBoxText: String = "",
    val packagesPerBoxText: String = "",
    val unitsPerPackageText: String = "",
    val unitsPerItemText: String = "",
    val stockEntryUnit: String = "",

    // Aviso de bajo inventario.
    val minimumStockText: String = "",
    // Unidad seleccionada para mostrar el stock minimo (ej. "g","kg","mL","L","unidades").
    val minimumStockUnit: String = "",
    // Porcentaje del stock recibido que dispara el aviso de stock bajo.
    // Default 20%. El usuario lo ajusta con un stepper visual y la cantidad
    // en unidades (`minimumStockText`) se recalcula en vivo.
    val minimumStockPercent: Int = 20,

    // --- NUEVA LOGICA DE STOCK MÍNIMO POR UNIDADES FÍSICAS DISCRETAS ---
    // Cantidad de unidades físicas (frascos o tabletas) para la alerta.
    val minimumStockUnits: Int = 0,
    // Modo de control: INDIVISIBLE (envase) o DIVISIBLE (unidades sueltas).
    val stockControlMode: StockControlMode? = null,

    val purchaseCost: String = "",
    val presentations: List<CreateProductPresentation> = emptyList(),
    val mainPresentationId: String = "",
    val addPresentationExpanded: Boolean = false,
    val draftPresentationName: String = "",
    // Modo personalizado dentro del expansor de "Nueva presentacion"
    val draftPresentationCustomMode: Boolean = false,
    val draftPresentationCustomAmount: String = "",
    val draftPresentationCustomUnit: String = "", // "g","kg","mL","L" o "" para UNIDAD
    val draftPresentationSalePriceText: String = "",
    val savedPresentations: List<SavedPresentation> = emptyList(),
    val marginRules: List<CategoryMarginRule> = emptyList(),
    val errors: Map<String, String> = emptyMap(),
    val stockEntryConfigured: Boolean = false,
    val showStockEntryDialog: Boolean = false,
    val barcodeMismatchDetected: Boolean = false,
    val barcodeMismatchOriginalName: String? = null,
    /** Usuario confirmo crear el producto sin codigo de barras. */
    val productoSinCodigoBarra: Boolean = false,
    /** V21.10: Producto que causa el conflicto de lote */
    val lotConflictProduct: MoldeProductos? = null,
    val showLotConflictDialog: Boolean = false,
    /** Control visual para el diálogo de stock 100% distribuido */
    val showStockCompleteDialog: Boolean = false,
    /** Control visual para el diálogo de error (supera stock) */
    val showStockErrorDialog: Boolean = false,
    /** Control visual para el diálogo de advertencia de reinicio del paso 3 */
    val showStep3ResetDialog: Boolean = false,
    /** Almacena el modo de entrada pendiente para aplicar tras confirmación */
    val pendingStockEntryMode: CreateProductStockEntryMode? = null,
    /** Almacena el tipo de control pendiente para aplicar tras confirmación */
    val pendingControlType: CreateProductControlType? = null,
    
    // V20.0: Información útil del producto
    val aiUsageInfo: com.app.administradorfarmadon.ActivityInventario.reference.UsageInfoAiResult? = null,
    val aiUsageInfoFetchedFor: String = "",
    val isFetchingUsageInfo: Boolean = false,

    // V22.0: Estados para guardado de proveedor
    val isSavingSupplier: Boolean = false,
    val supplierSaveSuccess: Boolean = false
)


@Composable
fun CreateProductScreen(
    state: CreateProductState,
    categoryOptions: List<String>,
    smartHint: SmartProductHint?,
    existingLotNumbers: Set<String> = emptySet(),
    nextEnabled: Boolean,
    // Sugerencia de categoria con IA (Gemini).
    categorySuggestionState: com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState =
        com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState(),
    onSwitchToManualCategory: () -> Unit = {},
    onBackToAiCategory: () -> Unit = {},
    // Escaner OCR de etiqueta (camara + Gemini Vision).
    labelScannerState: com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.UiState =
        com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.UiState(),
    onRequestLabelScan: () -> Unit = {},
    onConsumeLabelScan: () -> Unit = {},
    loading: Boolean,
    successSummary: ProductCreatedSummary?,
    onBack: () -> Unit,
    onStateChange: (CreateProductState) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSave: () -> Unit,
    onCreateAnother: () -> Unit,
    onViewProduct: () -> Unit,
    onViewSpecificProduct: (String) -> Unit = {},
    onSavePresentation: (SavedPresentation) -> Unit = {},
    isCheckingLotRemote: Boolean = false,
    isCheckingBarcodeRemote: Boolean = false,
    lotConflictInfo: String? = null,
    lotConflictColor: Color = Color.Transparent,
    lotConflictSeverity: Int = 0,
    onRequestBarcodeScan: () -> Unit = {},
    onAsistManualName: (String) -> Unit = {},
    onAsistManualCategory: (String) -> Unit = {},
    onClearAsistManual: () -> Unit = {},
    onDuplicateClicked: (MoldeProductos) -> Unit = {},
    onApplyNameCorrection: (String, String) -> Unit = { _, _ -> },
    onDismissNameCorrection: (String, String) -> Unit = { _, _ -> },
    onSearchIA: (Boolean) -> Unit = {},
    aiInventoryEnabled: Boolean = true,
    onAddStockToExistingProduct: (MoldeProductos) -> Unit = {},
    onIdentificarBarcode: (String, String?) -> Unit = { _, _ -> },
    onCheckBarcodeIntegrity: (String, String) -> Unit = { _, _ -> },
    onClearBarcodeIntegrityConflict: () -> Unit = {},
    onSaveSupplier: (String, String) -> Unit = { _, _ -> },
    showNoBarcodeConfirmDialog: Boolean = false,
    onDismissNoBarcodeConfirm: () -> Unit = {},
    onConfirmNoBarcodeContinue: () -> Unit = {}
) {
    // Fix: se eliminaron lecturas y cálculos que se descartaban sin asignarse.
    // Solo se conserva la detección real de teclado, que sí se usa abajo para
    // ocultar el BottomStepActions y ajustar el padding inferior del contenido.
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val navigationBottom = WindowInsets.navigationBars.getBottom(density)
    val keyboardVisible = imeBottom > navigationBottom
    val scope = rememberCoroutineScope()

    // "Todo listo" usa la misma regla que el botón Siguiente (nextEnabled)
    val canShowReadyMessageForStep = state.currentStep == CreateProductStep.PRODUCTO ||
        state.currentStep == CreateProductStep.LOTE_INICIAL
    val isCurrentStepReady = canShowReadyMessageForStep && nextEnabled && !loading
    var showReadyMessage by remember { mutableStateOf(false) }
    var hasShownReadyForCurrentValid by remember { mutableStateOf(false) }

    // V18.9: Estado temporal para el efecto de "regresar atrÃ¡s" al limpiar cÃ³digo
    var isReturningToInitial by remember { mutableStateOf(false) }

    LaunchedEffect(isCurrentStepReady, state.currentStep) {
        if (!canShowReadyMessageForStep) {
            showReadyMessage = false
            hasShownReadyForCurrentValid = false
            return@LaunchedEffect
        }

        if (isCurrentStepReady) {
            if (!hasShownReadyForCurrentValid) {
                showReadyMessage = true
                hasShownReadyForCurrentValid = true
            }
        } else {
            showReadyMessage = false
            hasShownReadyForCurrentValid = false
        }
    }

    // V18.9: El diálogo de conflicto ahora es persistente y obliga a una acción correctiva.
    // Se elimina el auto-cierre para garantizar la integridad de los datos.
    /*
    LaunchedEffect(state.barcodeMismatchDetected) {
        if (state.barcodeMismatchDetected) {
            delay(5000)
            onStateChange(state.copy(barcodeMismatchDetected = false))
        }
    }
    */

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreateBackground)
    ) {
        when {
            successSummary != null -> {
                ProductCreatedSuccessScreen(
                    summary = successSummary,
                    onViewProduct = onViewProduct,
                    onCreateAnother = onCreateAnother
                )
            }

            else -> {
                val isTablet = LocalConfiguration.current.smallestScreenWidthDp >= 600

                val nextLabel = when (state.currentStep) {
                    CreateProductStep.PRODUCTO -> "Siguiente"
                    CreateProductStep.LOTE_INICIAL -> "Siguiente"
                    CreateProductStep.PRESENTACIONES -> "Siguiente"
                    CreateProductStep.RESUMEN -> "Guardar producto"
                }

                if (isTablet) {
                    CreateProductTabletSidebarLayout(
                        state = state,
                        categoryOptions = categoryOptions,
                        existingLotNumbers = existingLotNumbers,
                        saveEnabled = nextEnabled,
                        onBack = onBack,
                        onStateChange = onStateChange,
                        onNext = onNext,
                        onPrevious = onPrevious,
                        onSave = onSave,
                        loading = loading,
                        categorySuggestionState = categorySuggestionState,
                        onSwitchToManualCategory = onSwitchToManualCategory,
                        onBackToAiCategory = onBackToAiCategory,
                        onSearchIA = onSearchIA,
                        aiInventoryEnabled = aiInventoryEnabled,
                        onAddStockToExistingProduct = onAddStockToExistingProduct,
                        onRequestBarcodeScan = onRequestBarcodeScan,
                        isCheckingBarcodeRemote = isCheckingBarcodeRemote,
                        onIdentificarBarcode = onIdentificarBarcode,
                        onRequestLabelScan = onRequestLabelScan,
                        onCheckBarcodeIntegrity = onCheckBarcodeIntegrity,
                        onClearBarcodeIntegrityConflict = onClearBarcodeIntegrityConflict,
                        isCheckingLotRemote = isCheckingLotRemote,
                        lotConflictInfo = lotConflictInfo,
                        lotConflictColor = lotConflictColor,
                        lotConflictSeverity = lotConflictSeverity,
                        onSaveSupplier = onSaveSupplier
                    )
                } else {
                    // V18.5: Determinamos si estamos en el flujo de IA por cÃ³digo de barras.
                    // En este modo, la card central tiene el control y ocultamos el botÃ³n "Siguiente" inferior.
                    val isAiBarcodeFlow = aiInventoryEnabled &&
                            state.currentStep == CreateProductStep.PRODUCTO &&
                            !state.forceManualProductEntry &&
                            !state.barcodeAiApplied

                    Scaffold(
                        containerColor = CreateBackground,
                        bottomBar = {
                            if (!keyboardVisible && !isAiBarcodeFlow) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // V18.7: NotificaciÃ³n de "Todo listo" sobre el botÃ³n Siguiente
                                    AnimatedVisibility(
                                        visible = showReadyMessage && canShowReadyMessageForStep,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()
                                    ) {
                                        Surface(
                                            color = Color.White,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp, vertical = 8.dp),
                                            shape = RoundedCornerShape(18.dp),
                                            border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.18f)),
                                            shadowElevation = 2.dp
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.CheckCircle,
                                                    contentDescription = null,
                                                    tint = CreateGreen,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Todo listo para continuar",
                                                    color = CreateGreen,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }

                                    // V19.5: Banner fijo para agregar presentaciones (Paso 3)
                                    // Solo visible en el paso 3 y si no estÃ¡ ya expandido el formulario.
                                    val totalStock = calculateTotalBaseStock(state)
                                    val assignedStock = state.presentations.sumOf { it.equivalenceText.toDoubleOrNull() ?: 0.0 }
                                    val hasStockAvailable = (totalStock - assignedStock) > 0.0

                                    AnimatedVisibility(
                                        visible = state.currentStep == CreateProductStep.PRESENTACIONES && !state.addPresentationExpanded,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()
                                    ) {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp, vertical = 4.dp)
                                                .clickable(
                                                    enabled = hasStockAvailable,
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = { onStateChange(state.copy(addPresentationExpanded = true)) }
                                                ),
                                            color = if (hasStockAvailable) CreateGreenSoft else CreateInfoGraySoft,
                                            shape = RoundedCornerShape(18.dp),
                                            border = BorderStroke(1.5.dp, if (hasStockAvailable) CreateGreen.copy(alpha = 0.22f) else CreateBorder)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (hasStockAvailable) Icons.Default.Add else Icons.Outlined.CheckCircle,
                                                    contentDescription = null,
                                                    tint = if (hasStockAvailable) CreateGreen else CreateTextSecondary.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = if (hasStockAvailable) "Agregar otra forma de venta" else "Mercancía totalmente distribuida",
                                                    color = if (hasStockAvailable) CreateGreen else CreateTextSecondary,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 15.sp,
                                                    letterSpacing = 0.3.sp
                                                )
                                            }
                                        }
                                    }

                                    BottomStepActions(
                                        step = state.currentStep,
                                        nextLabel = nextLabel,
                                        nextEnabled = nextEnabled,
                                        onNext = if (state.currentStep == CreateProductStep.RESUMEN) onSave else onNext,
                                        onPrevious = onPrevious,
                                        loading = loading
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        ProductStepContent(
                            modifier = Modifier
                                .padding(innerPadding),
                            step = state.currentStep,
                            contentBottomPadding = if (keyboardVisible) 24.dp else 88.dp,
                            onBack = onBack,
                            header = {
                                CreateProductStepHeader(
                                    step = state.currentStep,
                                    onBack = onBack
                                )
                            },
                            body = {
                                when (state.currentStep) {
                                    CreateProductStep.PRODUCTO -> ProductBasicStep(
                                        state = state,
                                        categoryOptions = categoryOptions,
                                        onStateChange = onStateChange,
                                        categorySuggestionState = categorySuggestionState,
                                        onSwitchToManualCategory = onSwitchToManualCategory,
                                        onBackToAiCategory = onBackToAiCategory,
                                        onAsistManualName = onAsistManualName,
                                        onAsistManualCategory = onAsistManualCategory,
                                        onClearAsistManual = onClearAsistManual,
                                        onApplyNameCorrection = onApplyNameCorrection,
                                        onDismissNameCorrection = onDismissNameCorrection,
                                        onSearchIA = onSearchIA,
                                        aiInventoryEnabled = aiInventoryEnabled,
                                        onAddStockToExistingProduct = onAddStockToExistingProduct,
                                        onRequestBarcodeScan = onRequestBarcodeScan,
                                        isCheckingBarcodeRemote = isCheckingBarcodeRemote,
                                        onIdentificarBarcode = onIdentificarBarcode,
                                        onNext = onNext,
                                        onRequestLabelScan = onRequestLabelScan,
                                        onCheckBarcodeIntegrity = onCheckBarcodeIntegrity,
                                        onClearBarcodeIntegrityConflict = onClearBarcodeIntegrityConflict,
                                        onSaveSupplier = onSaveSupplier
                                    )

                                    CreateProductStep.LOTE_INICIAL -> InitialLotStep(
                                        state = state,
                                        existingLotNumbers = existingLotNumbers,
                                        onStateChange = onStateChange,
                                        labelScannerState = labelScannerState,
                                        onRequestLabelScan = onRequestLabelScan,
                                        onConsumeLabelScan = onConsumeLabelScan,
                                        isCheckingLotRemote = isCheckingLotRemote,
                                        lotConflictInfo = lotConflictInfo,
                                        lotConflictColor = lotConflictColor,
                                        lotConflictSeverity = lotConflictSeverity,
                                        onSaveSupplier = onSaveSupplier
                                    )

                                    CreateProductStep.PRESENTACIONES -> PresentationsPricesStep(
                                        state = state,
                                        onStateChange = onStateChange
                                    )

                                    CreateProductStep.RESUMEN -> ProductSummaryStep(
                                        state = state,
                                        onStateChange = onStateChange
                                    )
                                }
                            }
                        )

                        if (state.currentStep == CreateProductStep.PRESENTACIONES && state.addPresentationExpanded) {
                            CreatePresentationDialog(
                                state = state,
                                onStateChange = onStateChange,
                                onSavePresentation = onSavePresentation,
                                onDismiss = { onStateChange(state.copy(addPresentationExpanded = false)) }
                            )
                        }
                    }
                }
            }
        }

        // --- DIALOGO PERSISTENTE DE ALERTA DE INTEGRIDAD ---
        if (state.barcodeMismatchDetected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = CreateRed,
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.QrCodeScanner,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Conflicto de Identidad",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "El nombre no parece coincidir con el codigo de barras escaneado.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        state.barcodeMismatchOriginalName?.let { original ->
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "El codigo pertenece a: $original",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Botones de acción forzada para resolver el conflicto
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val correctedName = state.barcodeMismatchOriginalName.orEmpty()
                                    onClearBarcodeIntegrityConflict()
                                    onStateChange(state.copy(
                                        name = correctedName.ifBlank { state.name },
                                        barcodeMismatchDetected = false,
                                        barcodeMismatchOriginalName = null,
                                        errors = state.errors - "barcode" - "name"
                                    ))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = CreateRed),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Usar nombre del codigo", fontWeight = FontWeight.Bold)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            isReturningToInitial = true
                                            delay(350)
                                            onClearBarcodeIntegrityConflict()
                                            onStateChange(state.copy(
                                                barcode = "",
                                                name = "",
                                                category = "",
                                                controlType = null,
                                                requiresPrescription = false,
                                                barcodeMismatchDetected = false,
                                                barcodeMismatchOriginalName = null,
                                                scannedImageBase64 = null,
                                                barcodeAiResult = null,
                                                barcodeAiError = null,
                                                barcodeAiApplied = false,
                                                duplicateProductFound = null,
                                                forceManualProductEntry = false,
                                                errors = state.errors - setOf("barcode", "name", "category", "controlType")
                                            ))
                                            delay(500)
                                            isReturningToInitial = false
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Limpiar codigo")
                                }

                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            isReturningToInitial = true
                                            delay(350)
                                            onClearBarcodeIntegrityConflict()
                                            onStateChange(state.copy(
                                                barcode = "",
                                                name = "",
                                                category = "",
                                                controlType = null,
                                                requiresPrescription = false,
                                                barcodeMismatchDetected = false,
                                                barcodeMismatchOriginalName = null,
                                                scannedImageBase64 = null,
                                                barcodeAiResult = null,
                                                barcodeAiError = null,
                                                barcodeAiApplied = false,
                                                duplicateProductFound = null,
                                                forceManualProductEntry = false,
                                                errors = state.errors - setOf("barcode", "name", "category", "controlType")
                                            ))
                                            delay(450)
                                            isReturningToInitial = false
                                            onRequestBarcodeScan()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Escanear otro")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showNoBarcodeConfirmDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(30.dp),
                    shadowElevation = 16.dp,
                    border = BorderStroke(1.dp, CreateOrange.copy(alpha = 0.22f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(58.dp),
                            color = CreateOrangeSoft,
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = CreateOrange,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        Text(
                            text = "Continuar sin codigo",
                            color = CreateTextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Puedes crear este producto sin codigo de barras. Luego no se encontrara por escaner, pero podras agregarlo despues desde su ficha.",
                            color = CreateTextSecondary,
                            fontSize = 15.sp,
                            lineHeight = 21.sp,
                            textAlign = TextAlign.Center
                        )

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = CreateOrangeSoft.copy(alpha = 0.72f),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, CreateOrange.copy(alpha = 0.16f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = CreateOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Recomendado: escanea si el producto tiene etiqueta.",
                                    color = CreateTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismissNoBarcodeConfirm,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, CreateBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = CreateTextSecondary
                                )
                            ) {
                                Text("Volver", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onConfirmNoBarcodeContinue,
                                modifier = Modifier
                                    .weight(1.15f)
                                    .height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CreateOrange,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Continuar", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        if (loading || isReturningToInitial || state.isSavingSupplier) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White.copy(alpha = 0.96f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = when {
                            state.isSavingSupplier && state.supplierSaveSuccess -> androidx.compose.ui.graphics.Color(0xFF0E8F63)
                            state.isSavingSupplier -> androidx.compose.ui.graphics.Color(0xFFF59E0B)
                            else -> CreateGreen
                        },
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = when {
                            state.isSavingSupplier && state.supplierSaveSuccess -> "¡Proveedor guardado!"
                            state.isSavingSupplier -> "Guardando proveedor..."
                            isReturningToInitial -> "Regresando al asistente IA..."
                            else -> "Guardando producto..."
                        },
                        color = CreateTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (state.showStockCompleteDialog) {
            StockUsageCompleteDialog(
                state = state,
                onDismiss = { onStateChange(state.copy(showStockCompleteDialog = false)) }
            )
        }

        if (state.showStockErrorDialog) {
            StockLimitErrorDialog(
                state = state,
                onDismiss = { onStateChange(state.copy(showStockErrorDialog = false)) }
            )
        }

        if (state.showStep3ResetDialog) {
            val isFromStep1 = state.pendingControlType != null

            Step3ResetWarningDialog(
                state = state,
                title = if (isFromStep1) "¡Cambio de Tipo!" else "¡Acción Destructiva!",
                message = if (isFromStep1)
                    "Al cambiar el tipo de control (Unidad/Peso/Líquido), se borrarán todos los datos de inventario y precios que ya configuraste."
                else "Si cambias la forma de ingreso, se borrarán todas las presentaciones de venta que ya habías creado en el Paso 3.",
                onDismiss = {
                    onStateChange(
                        state.copy(
                            showStep3ResetDialog = false,
                            pendingStockEntryMode = null,
                            pendingControlType = null
                        )
                    )
                },
                onConfirm = {
                    if (isFromStep1) {
                        val newType = state.pendingControlType
                        onStateChange(
                            state.resetStockEntryConfiguration().copy(
                                controlType = newType,
                                typeSelectedManually = true,
                                presentations = emptyList(),
                                mainPresentationId = "",
                                showStep3ResetDialog = false,
                                pendingControlType = null,
                                errors = state.errors - setOf(
                                    "controlType",
                                    "receivedUnits",
                                    "boxesReceived",
                                    "unitsPerBox",
                                    "presentations"
                                )
                            )
                        )
                    } else {
                        val mode = state.pendingStockEntryMode
                        onStateChange(
                            state.copy(
                                presentations = emptyList(),
                                mainPresentationId = "",
                                stockEntryConfigured = false,
                                showStockEntryDialog = mode != null,
                                stockEntryMode = mode,
                                receivedUnitsText = "",
                                boxesReceivedText = "",
                                unitsPerBoxText = "",
                                packagesPerBoxText = "",
                                unitsPerPackageText = "",
                                showStep3ResetDialog = false,
                                pendingStockEntryMode = null,
                                errors = state.errors - setOf(
                                    "receivedUnits",
                                    "boxesReceived",
                                    "unitsPerBox",
                                    "packagesPerBox",
                                    "unitsPerPackage",
                                    "presentations",
                                    "mainPresentation"
                                )
                            )
                        )
                    }
                }
            )
        }

        // --- DIÁLOGO DE CONFLICTO DE LOTE (V21.10) ---
        if (state.showLotConflictDialog && state.lotConflictProduct != null) {
            val isLotConflict = lotConflictSeverity != 0
            
            ConflictManagementDialog(
                title = if (isLotConflict) "Lote Duplicado" else "Producto ya Registrado",
                message = if (isLotConflict) 
                    "El número de lote '${state.lotNumber}' ya está asignado a otro producto en el inventario."
                    else "Hemos detectado que ya existe un producto con este nombre o identificación en el sistema.",
                conflictProduct = state.lotConflictProduct,
                onDismiss = { 
                    if (isLotConflict) {
                        onStateChange(state.copy(lotNumber = "", lotScanned = false, showLotConflictDialog = false))
                    } else {
                        onStateChange(state.copy(showLotConflictDialog = false))
                    }
                },
                onAddStock = { onAddStockToExistingProduct(state.lotConflictProduct!!) }
            )
        }
    }
}

@Composable
fun ConflictManagementDialog(
    title: String,
    message: String,
    conflictProduct: MoldeProductos,
    onDismiss: () -> Unit,
    onAddStock: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(horizontal = 24.dp)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 24.dp,
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // ENCABEZADO DE ADVERTENCIA PREMIUM
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(WarningGradient)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                color = Color.White.copy(alpha = 0.25f),
                                shape = CircleShape,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.PriorityHigh, null, tint = Color.White, modifier = Modifier.size(32.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = title,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = message,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CreateTextSecondary,
                            lineHeight = 20.sp
                        )

                        // TARJETA DEL PRODUCTO EXISTENTE
                        Surface(
                            color = CreateBackground,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, CreateBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Inventory2, null, tint = CreateOrange, modifier = Modifier.size(24.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = conflictProduct.nombre,
                                        color = CreateTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = conflictProduct.categoria,
                                        color = CreateTextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // ACCIONES PROFESIONALES
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onAddStock,
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CreateOrange)
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AÑADIR STOCK AL EXISTENTE", fontWeight = FontWeight.Black)
                            }

                            TextButton(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("CORREGIR NOMBRE / CANCELAR", color = CreateRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Step3ResetWarningDialog(
    state: CreateProductState,
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 32.dp)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 12.dp,
                border = BorderStroke(2.dp, CreateRed.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
            ) {
                Box(modifier = Modifier.background(WarningGradient)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = message,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "Esta acción no se puede deshacer.",
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("Cancelar", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onConfirm,
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = CreateRed)
                            ) {
                                Text("Sí, borrar", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StockLimitErrorDialog(
    state: CreateProductState,
    onDismiss: () -> Unit
) {
    val total = calculateTotalBaseStock(state)
    val assigned = state.presentations.sumOf { it.equivalenceText.toDoubleOrNull() ?: 0.0 }
    val remaining = (total - assigned).coerceAtLeast(0.0)
    val unit = resolveStockUnitLabel(state)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 32.dp)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 12.dp,
                border = BorderStroke(2.dp, CreateRed.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
            ) {
                Box(modifier = Modifier.background(WarningGradient)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Exceso de Inventario",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "La cantidad que intentas agregar supera lo que tienes disponible en este lote.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("STOCK DISPONIBLE", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("${formatCreateProductNumber(remaining)} $unit", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = CreateRed)
                        ) {
                            Text("Ajustar cantidad", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StockUsageCompleteDialog(
    state: CreateProductState,
    onDismiss: () -> Unit
) {
    val total = calculateTotalBaseStock(state)
    val unit = resolveStockUnitLabel(state)
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            shadowElevation = 12.dp,
            border = BorderStroke(1.5.dp, CreateGreen.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Icono Animado / Central
                Surface(
                    color = CreateGreenSoft,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = CreateGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "¡Mercancía Agotada!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = CreateTextPrimary
                    )
                    Text(
                        text = "Has distribuido el 100% de lo ingresado en las presentaciones de venta.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CreateTextSecondary
                    )
                }
                
                // Gráfico de Balance
                Surface(
                    color = CreateSurfaceSoft,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, CreateBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Total Ingreso", color = CreateTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${formatCreateProductNumber(total)} $unit", color = CreateTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }
                        
                        Icon(Icons.Filled.ChevronRight, null, tint = CreateBorder)
                        
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("Distribuido", color = CreateGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("100%", color = CreateGreen, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CreateGreen)
                ) {
                    Text("Entendido", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun CreatePresentationDialog(
    state: CreateProductState,
    onStateChange: (CreateProductState) -> Unit,
    onSavePresentation: (SavedPresentation) -> Unit = {},
    onDismiss: () -> Unit
) {
    val totalStock = calculateTotalBaseStock(state)
    val assignedStock = state.presentations.sumOf { it.equivalenceText.toDoubleOrNull() ?: 0.0 }
    val remainingStock = (totalStock - assignedStock).coerceAtLeast(0.0)

    val unitToUse = state.draftPresentationCustomUnit.ifBlank {
        when (state.controlType) {
            CreateProductControlType.PESO -> "g"
            CreateProductControlType.LIQUIDO -> "mL"
            else -> ""
        }
    }

    val inputAmount = state.draftPresentationCustomAmount.toDoubleOrNull() ?: 0.0
    val inputInBase = when (state.controlType) {
        CreateProductControlType.PESO -> if (unitToUse == "kg") inputAmount * 1000.0 else inputAmount
        CreateProductControlType.LIQUIDO -> if (unitToUse == "L") inputAmount * 1000.0 else inputAmount
        else -> inputAmount
    }

    val canAdd = state.draftPresentationName.isNotBlank() &&
            inputAmount > 0 &&
            state.draftPresentationSalePriceText.toDoubleOrNull()?.let { it > 0 } == true

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 32.dp)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Box(modifier = Modifier.fillMaxWidth().background(WaterGradient)) {
                    val configuration = LocalConfiguration.current
                    val maxHeight = (configuration.screenHeightDp.dp * 0.7f)

                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .heightIn(max = maxHeight)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Text(
                            text = "Nueva Presentación",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = CreateTextPrimary
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // 1. Nombre (Ahora siempre visible para cualquier tipo)
                            AppTextField(
                                label = "NOMBRE (EJ. BLÍSTER, CAJA, FRASCO)",
                                value = state.draftPresentationName,
                                placeholder = "Escribe el nombre...",
                                onValueChange = { onStateChange(state.copy(draftPresentationName = it)) }
                            )

                            // 2. Cantidad y Selector de Unidades
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = if (state.controlType == CreateProductControlType.UNIDAD) "CANTIDAD DE UNIDADES" else "CONTENIDO / PESO",
                                    color = CreateTextSecondary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        AppTextField(
                                            label = "",
                                            value = state.draftPresentationCustomAmount,
                                            placeholder = "Ej. 500",
                                            keyboardType = KeyboardType.Decimal,
                                            onValueChange = { onStateChange(state.copy(draftPresentationCustomAmount = it)) }
                                        )
                                    }
                                    
                                    // Selector tipo burbuja
                                    if (state.controlType != CreateProductControlType.UNIDAD) {
                                        val options = if (state.controlType == CreateProductControlType.PESO) listOf("g", "kg") else listOf("mL", "L")
                                        PremiumUnitSelector(
                                            selected = unitToUse,
                                            options = options,
                                            onSelected = { onStateChange(state.copy(draftPresentationCustomUnit = it)) }
                                        )
                                    }
                                }
                            }

                            // 3. Precio
                            AppTextField(
                                label = "PRECIO DE VENTA (BS)",
                                value = state.draftPresentationSalePriceText,
                                placeholder = "Ej. 45.50",
                                keyboardType = KeyboardType.Decimal,
                                leadingIcon = Icons.Outlined.AttachMoney,
                                onValueChange = { onStateChange(state.copy(draftPresentationSalePriceText = it)) }
                            )
                        }

                        // Resumen de disponibilidad
                        Surface(
                            color = Color.White.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "Equivale a ${formatCreateProductNumber(inputInBase)} ${resolveStockUnitLabel(state)}. Stock libre: ${formatCreateProductNumber(remainingStock)}",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = CreateTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f).height(54.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Cancelar", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    // Validación final de stock antes de agregar
                                    if (inputInBase > remainingStock) {
                                        onStateChange(state.copy(showStockErrorDialog = true))
                                        return@Button
                                    }

                                    val (draftName, equivalence) = when (state.controlType) {
                                        CreateProductControlType.UNIDAD -> {
                                            val nombre = state.draftPresentationName.trim().ifBlank { "Pack $inputAmount" }
                                            nombre to formatCreateProductNumber(inputAmount)
                                        }
                                        else -> state.draftPresentationName.trim() to formatCreateProductNumber(inputInBase)
                                    }

                                    val newPresentation = CreateProductPresentation(
                                        id = draftName.lowercase().replace(" ", "_") + "_" + java.util.UUID.randomUUID().toString().take(4),
                                        name = draftName,
                                        equivalenceText = equivalence,
                                        salePriceText = state.draftPresentationSalePriceText,
                                        isAiSuggested = false
                                    )

                                    val (nextList, idFinal) = mergePresentacionEvitandoDuplicado(state.presentations, newPresentation)
                                    val nextAssigned = nextList.sumOf { it.equivalenceText.toDoubleOrNull() ?: 0.0 }
                                    
                                    onStateChange(state.copy(
                                        presentations = nextList,
                                        mainPresentationId = if (nextList.any { it.id == state.mainPresentationId }) state.mainPresentationId else idFinal,
                                        addPresentationExpanded = false,
                                        draftPresentationName = "",
                                        draftPresentationCustomAmount = "",
                                        draftPresentationCustomUnit = "",
                                        draftPresentationSalePriceText = "",
                                        showStockCompleteDialog = (totalStock - nextAssigned) <= 0.0
                                    ))
                                },
                                enabled = canAdd,
                                modifier = Modifier.weight(1.2f).height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CreateGreen)
                            ) {
                                Text("Agregar", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateProductTabletSidebarLayout(
    state: CreateProductState,
    categoryOptions: List<String>,
    existingLotNumbers: Set<String>,
    saveEnabled: Boolean,
    onBack: () -> Unit,
    onStateChange: (CreateProductState) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSave: () -> Unit,
    loading: Boolean = false,
    // Sugerencia IA propagada también en tablet para consistencia móvil/tablet.
    categorySuggestionState: com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState =
        com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState(),
    onSwitchToManualCategory: () -> Unit = {},
    onBackToAiCategory: () -> Unit = {},
    onSearchIA: (Boolean) -> Unit = {},
    aiInventoryEnabled: Boolean = true,
    onAddStockToExistingProduct: (MoldeProductos) -> Unit = {},
    onRequestBarcodeScan: () -> Unit = {},
    isCheckingBarcodeRemote: Boolean = false,
    onIdentificarBarcode: (String, String?) -> Unit = { _, _ -> },
    onRequestLabelScan: () -> Unit = {},
    onCheckBarcodeIntegrity: (String, String) -> Unit = { _, _ -> },
    onClearBarcodeIntegrityConflict: () -> Unit = {},
    isCheckingLotRemote: Boolean = false,
    lotConflictInfo: String? = null,
    lotConflictColor: Color = Color.Transparent,
    lotConflictSeverity: Int = 0,
    onSaveSupplier: (String, String) -> Unit = { _, _ -> }
) {
    val currentStep = state.currentStep
    val isLast = currentStep == CreateProductStep.RESUMEN
    val isFirst = currentStep == CreateProductStep.PRODUCTO

    Scaffold(
        containerColor = CreateBackground,
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 28.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = if (isFirst) onBack else onPrevious,
                        modifier = Modifier.weight(1f),
                        enabled = !loading
                    ) {
                        Text(if (isFirst) "Cancelar" else "Atras")
                    }
                    Button(
                        onClick = if (isLast) onSave else onNext,
                        modifier = Modifier.weight(2f),
                        enabled = saveEnabled && !loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CreateGreen,
                            contentColor = Color.White,
                            disabledContainerColor = CreateBorder,
                            disabledContentColor = CreateTextSecondary.copy(alpha = 0.5f)
                        )
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Procesando...", fontWeight = FontWeight.Bold)
                        } else {
                            Text(if (isLast) "Guardar producto" else "Siguiente")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // â”€â”€ Sidebar izquierdo: lista de pasos â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            Surface(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight(),
                color = Color.White,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, CreateBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CreateBackground)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                                contentDescription = "Atras",
                                tint = CreateTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Crear producto",
                            color = CreateTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "PASOS",
                        color = CreateTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val pasos = listOf(
                        CreateProductStep.PRODUCTO,
                        CreateProductStep.LOTE_INICIAL,
                        CreateProductStep.PRESENTACIONES,
                        CreateProductStep.RESUMEN
                    )
                    // Fix: el sidebar permitÃ­a saltar a cualquier paso, ignorando
                    // las validaciones del flujo (`nextEnabled`). Ahora solo se
                    // puede retroceder a pasos previos o quedarse en el actual;
                    // para avanzar es obligatorio usar el botÃ³n "Siguiente",
                    // que sÃ­ respeta la validaciÃ³n.
                    pasos.forEach { paso ->
                        val isCurrent = paso == currentStep
                        val isCompleted = paso.number < currentStep.number
                        val canNavigate = isCurrent || isCompleted
                        TabletStepRow(
                            step = paso,
                            isCurrent = isCurrent,
                            isCompleted = isCompleted,
                            onClick = {
                                if (canNavigate) {
                                    onStateChange(state.copy(currentStep = paso))
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // â”€â”€ Contenido derecho: formulario del paso actual â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                color = Color.White,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, CreateBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text(
                            text = currentStep.title,
                            color = CreateTextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentStep.subtitle,
                            color = CreateTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    when (currentStep) {
                        CreateProductStep.PRODUCTO -> ProductBasicStep(
                            state = state,
                            categoryOptions = categoryOptions,
                            onStateChange = onStateChange,
                            categorySuggestionState = categorySuggestionState,
                            onSwitchToManualCategory = onSwitchToManualCategory,
                            onBackToAiCategory = onBackToAiCategory,
                            onSearchIA = onSearchIA,
                            aiInventoryEnabled = aiInventoryEnabled,
                            onAddStockToExistingProduct = onAddStockToExistingProduct,
                            onRequestBarcodeScan = onRequestBarcodeScan,
                            isCheckingBarcodeRemote = isCheckingBarcodeRemote,
                            onIdentificarBarcode = onIdentificarBarcode,
                            onNext = onNext,
                            onRequestLabelScan = onRequestLabelScan,
                            onCheckBarcodeIntegrity = onCheckBarcodeIntegrity,
                            onClearBarcodeIntegrityConflict = onClearBarcodeIntegrityConflict
                        )
                        CreateProductStep.LOTE_INICIAL -> InitialLotStep(
                            state = state,
                            existingLotNumbers = existingLotNumbers,
                            onStateChange = onStateChange,
                            isCheckingLotRemote = isCheckingLotRemote,
                            lotConflictInfo = lotConflictInfo,
                            lotConflictColor = lotConflictColor,
                            lotConflictSeverity = lotConflictSeverity,
                            onSaveSupplier = onSaveSupplier
                        )
                        CreateProductStep.PRESENTACIONES -> PresentationsPricesStep(
                            state = state,
                            onStateChange = onStateChange
                        )
                        CreateProductStep.RESUMEN -> ProductSummaryStep(
                            state = state,
                            onStateChange = onStateChange
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun TabletStepRow(
    step: CreateProductStep,
    isCurrent: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isCurrent -> CreateGreenSoft
        else -> Color.Transparent
    }
    val borderColor = when {
        isCurrent -> CreateGreen
        else -> Color.Transparent
    }
    val numberBg = when {
        isCurrent -> CreateGreen
        isCompleted -> CreateGreen
        else -> CreateBorder
    }
    val numberTextColor = when {
        isCurrent || isCompleted -> Color.White
        else -> CreateTextSecondary
    }
    val titleColor = if (isCurrent) CreateGreen else CreateTextPrimary

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(if (isCurrent) 1.dp else 0.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = numberBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = step.number.toString(),
                        color = numberTextColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = step.title,
                    color = titleColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = step.subtitle,
                    color = CreateTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CreateProductStepHeader(
    step: CreateProductStep,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, CreateBorder, CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                    contentDescription = "Atras",
                    tint = CreateTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Título animado al lado de la flecha
            AnimatedContent(
                targetState = step.title,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    (fadeIn() + slideInVertically { it / 2 }).togetherWith(fadeOut() + slideOutVertically { -it / 2 })
                },
                label = "header_title_anim"
            ) { title ->
                Text(
                    text = title,
                    color = CreateTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Indicador de pasos en la esquina (tipo badge)
            Surface(
                color = CreateGreenSoft,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.2f))
            ) {
                Text(
                    text = "${step.number}/4",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = CreateGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Subtítulo compacto debajo para mantener contexto
        AnimatedContent(
            targetState = step.subtitle,
            modifier = Modifier.padding(start = 52.dp, top = 2.dp),
            transitionSpec = { fadeIn().togetherWith(fadeOut()) },
            label = "header_subtitle_anim"
        ) { subtitle ->
            Text(
                text = subtitle,
                color = CreateTextSecondary,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

    }
}

@Composable
fun ProductStepContent(
    modifier: Modifier = Modifier,
    step: CreateProductStep,
    contentBottomPadding: Dp = 72.dp,
    onBack: () -> Unit,
    header: @Composable () -> Unit,
    body: @Composable () -> Unit
) {
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // --- NUEVO: Ocultar teclado suavemente al hacer scroll (Solo si es manual) ---
    val isDragged by listState.interactionSource.collectIsDraggedAsState()
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress && isDragged) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }
    // ---------------------------------------------------------

    LaunchedEffect(step) {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        listState.scrollToItem(0)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        state = listState,
        contentPadding = PaddingValues(bottom = contentBottomPadding),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item(key = "header_${step.name}") {
            header()
        }

        item(key = "body_${step.name}") {
            body()
        }
    }
}

@Composable
fun AiBarcodeProductStep(
    state: CreateProductState,
    onStateChange: (CreateProductState) -> Unit,
    onRequestBarcodeScan: () -> Unit,
    onAddStockToExistingProduct: (MoldeProductos) -> Unit,
    onIdentificarBarcode: (String, String?) -> Unit,
    isCheckingBarcodeRemote: Boolean,
    onNext: () -> Unit,
    onRequestLabelScan: () -> Unit
) {
    val resetAndScanAgain = {
        onStateChange(
            state.copy(
                barcode = "",
                name = "",
                category = "",
                controlType = null,
                requiresPrescription = false,
                forceManualProductEntry = false,
                scannedImageBase64 = null,
                barcodeAiResult = null,
                barcodeAiError = null,
                barcodeAiApplied = false,
                duplicateProductFound = null,
                barcodeMismatchDetected = false,
                barcodeMismatchOriginalName = null,
                errors = state.errors - setOf("barcode", "name", "category", "controlType")
            )
        )
        onRequestBarcodeScan()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val result = state.barcodeAiResult
        val hasDuplicate = state.duplicateProductFound != null

        when {
            // 1. Producto existente detectado por Firebase
            hasDuplicate -> {
                state.duplicateProductFound?.let { producto ->
                    DuplicateProductCard(
                        producto = producto,
                        onAddStock = { onAddStockToExistingProduct(producto) }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = resetAndScanAgain,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Escanear otro", color = CreateTextSecondary)
                        }

                        OutlinedButton(
                            onClick = { onStateChange(state.copy(forceManualProductEntry = true)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Ajustar", color = CreateTextSecondary)
                        }
                    }
                }
            }

            // 2. Resultado ya aplicado (resumen compacto o ir a manual)
            state.barcodeAiApplied -> {
                ProductAppliedSummaryCard(
                    state = state,
                    onEditManual = { onStateChange(state.copy(barcodeAiApplied = false, forceManualProductEntry = true)) },
                    onScanOther = resetAndScanAgain
                )
            }

            // 3. Identificando...
            state.isIdentifyingBarcode || isCheckingBarcodeRemote -> {
                IdentifyingBarcodeCard(barcode = state.barcode)
            }

            // 4. Resultado IA disponible
            result != null -> {
                if (result.estado == "IDENTIFICADO") {
                    BarcodeIdentifiedCard(
                        result = result,
                        onApply = {
                            val mappedType = when (result.tipoControl) {
                                "UNIDAD" -> CreateProductControlType.UNIDAD
                                "PESO" -> CreateProductControlType.PESO
                                "LIQUIDO" -> CreateProductControlType.LIQUIDO
                                else -> null
                            }

                            if (mappedType != null) {
                                onStateChange(state.copy(
                                    name = result.nombre.orEmpty(),
                                    category = result.categoria.orEmpty(),
                                    controlType = mappedType,
                                    requiresPrescription = result.requiereReceta,
                                    barcode = result.codigo.ifBlank { state.barcode },
                                    barcodeAiApplied = true,
                                    currentStep = CreateProductStep.LOTE_INICIAL,
                                    errors = state.errors - setOf("name", "category", "controlType", "barcode")
                                ))
                                // V18.5: Avance automÃ¡tico al Paso 2 tras aplicar datos exitosos de la IA
                            } else {
                                // Si el tipo es DESCONOCIDO, forzar manual para que el usuario elija
                                onStateChange(state.copy(
                                    name = result.nombre.orEmpty(),
                                    category = result.categoria.orEmpty(),
                                    requiresPrescription = result.requiereReceta,
                                    forceManualProductEntry = true,
                                    errors = state.errors + ("controlType" to "âœ¨ La IA hizo su magia, solo confirma como se controla este producto.")
                                ))
                            }
                        },
                        onScanOther = resetAndScanAgain,
                        onEditManual = {
                            val mappedType = when (result.tipoControl) {
                                "UNIDAD" -> CreateProductControlType.UNIDAD
                                "PESO" -> CreateProductControlType.PESO
                                "LIQUIDO" -> CreateProductControlType.LIQUIDO
                                else -> null
                            }
                            onStateChange(state.copy(
                                name = result.nombre.orEmpty(),
                                category = result.categoria.orEmpty(),
                                controlType = mappedType,
                                requiresPrescription = result.requiereReceta,
                                forceManualProductEntry = true
                            ))
                        }
                    )
                } else {
                    BarcodeNotIdentifiedCard(
                        barcode = state.barcode,
                        onScanOther = resetAndScanAgain,
                        onEditManual = { onStateChange(state.copy(forceManualProductEntry = true)) },
                        onRequestLabelScan = onRequestLabelScan
                    )
                }
            }

            // 5. Estado inicial o codigo vacio
            state.barcode.isBlank() -> {
                InitialScanCard(
                    onStartScan = onRequestBarcodeScan
                )
            }

            // 6. Error de red o validacion del cÃ³digo.
            state.errors["barcode"] != null -> {
                BarcodeValidationErrorCard(
                    message = state.errors["barcode"].orEmpty(),
                    onRetry = resetAndScanAgain,
                    onAdjust = { onStateChange(state.copy(forceManualProductEntry = true)) }
                )
            }

            // 7. Codigo nuevo sin resultado todavia  (esperando).
            else -> {
                IdentifyingBarcodeCard(barcode = state.barcode)
                // V18.3: Eliminado el LaunchedEffect que duplicaba llamadas a IA
                // y causaba carreras con la validación de Firebase. La validación
                // ahora fluye únicamente desde programarValidacionBarcodeCompose
            }
        }
    }
}

@Composable
fun InitialScanCard(onStartScan: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, CreateBorder)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.outline_barcode_scanner_24),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = CreateGreen
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Escanea el codigo del producto",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CreateTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Identificaremos el producto automáticamente para ahorrarte tiempo.",
                style = MaterialTheme.typography.bodyMedium,
                color = CreateTextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onStartScan,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CreateGreen)
            ) {
                Text("Escanear codigo")
            }
        }
    }
}

@Composable
fun IdentifyingBarcodeCard(barcode: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, CreateBorder)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = CreateGreen, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Identificando producto...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Codigo: $barcode",
                style = MaterialTheme.typography.bodySmall,
                color = CreateTextSecondary
            )
        }

    }
}

@Composable
fun BarcodeValidationErrorCard(
    message: String,
    onRetry: () -> Unit,
    onAdjust: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFF5F5),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, CreateRed.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = CreateRed.copy(alpha = 0.12f),
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.QrCodeScanner,
                        contentDescription = null,
                        tint = CreateRed,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Text(
                text = "No pudimos validar el codigo",
                color = CreateRed,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                color = CreateTextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CreateBorder)
                ) {
                    Text("Reintentar", color = CreateTextSecondary)
                }

                Button(
                    onClick = onAdjust,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CreateRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ajustar")
                }
            }
        }
    }
}

@Composable
fun BarcodeIdentifiedCard(
    result: com.app.administradorfarmadon.ActivityInventario.reference.BarcodeAiResult,
    onApply: () -> Unit,
    onScanOther: () -> Unit,
    onEditManual: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = CreateGreenSoft,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.12f))
                ) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = CreateGreen,
                        modifier = Modifier.padding(10.dp).size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Producto identificado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = CreateGreen
                    )
                    Text(
                        text = "Revisa y aplica los datos sugeridos",
                        style = MaterialTheme.typography.bodySmall,
                        color = CreateTextSecondary
                    )
                }
                Surface(
                    color = CreateGreen.copy(alpha = 0.09f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "IA",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = CreateGreen
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CreateGreenSoft.copy(alpha = 0.46f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = result.nombre.orEmpty().ifBlank { "Producto sin nombre confirmado" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = CreateTextPrimary,
                        lineHeight = 28.sp
                    )

                    Text(
                        text = "Codigo ${result.codigo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CreateTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BarcodeInfoPill("Categoria", result.categoria.orEmpty(), CreateGreen, CreateGreenSoft)
                        BarcodeInfoPill("Control", result.tipoControl.orEmpty(), CreateGreen, CreateGreenSoft)
                        BarcodeInfoPill(
                            "Receta",
                            if (result.requiereReceta) "Requiere" else "No requiere",
                            CreateTextPrimary,
                            CreateBackground
                        )
                    }
                }
            }

            val isTypeUnknown = result.tipoControl !in listOf("UNIDAD", "PESO", "LIQUIDO")

            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CreateGreen)
            ) {
                Text(
                    text = if (isTypeUnknown) "Completar tipo de control" else "Aplicar datos",
                    modifier = Modifier.padding(vertical = 6.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onScanOther,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CreateBorder)
                ) {
                    Text("Otro", color = CreateTextSecondary)
                }
                OutlinedButton(
                    onClick = onEditManual,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CreateBorder)
                ) {
                    Text("Ajustar", color = CreateTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun BarcodeInfoPill(
    label: String,
    value: String,
    color: Color,
    background: Color
) {
    Surface(
        color = background.copy(alpha = 0.82f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.14f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color.copy(alpha = 0.78f),
                letterSpacing = 0.4.sp
            )
            Text(
                text = value.ifBlank { "Sin dato" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = CreateTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

    }
}
@Composable
fun BarcodeNotIdentifiedCard(
    barcode: String,
    onScanOther: () -> Unit,
    onEditManual: () -> Unit,
    onRequestLabelScan: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFEF2F2),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFFEE2E2))
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.AutoMirrored.Outlined.Label,
                contentDescription = null,
                tint = CreateRed,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No pude identificarlo por codigo",
                fontWeight = FontWeight.Bold,
                color = CreateRed,
                textAlign = TextAlign.Center
            )
            Text(
                "El codigo $barcode no arroja resultados claros.",
                style = MaterialTheme.typography.bodySmall,
                color = CreateTextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onEditManual,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CreateRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Crear con ayuda")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onRequestLabelScan,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CreateRed.copy(alpha = 0.5f))
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.Label,
                    contentDescription = null,
                    tint = CreateRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Escanear etiqueta o foto", color = CreateRed)
            }

            TextButton(onClick = onScanOther) {
                Text("Escanear otro codigo", color = CreateTextSecondary)
            }
        }
    }
}

@Composable
fun ProductAppliedSummaryCard(state: CreateProductState, onEditManual: () -> Unit, onScanOther: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = CreateGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Datos cargados correctamente", fontWeight = FontWeight.SemiBold, color = CreateTextPrimary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(state.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(state.category, style = MaterialTheme.typography.bodySmall, color = CreateTextSecondary)

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onScanOther, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                    Text("Cambiar", fontSize = 12.sp)
                }
                Button(onClick = onEditManual, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                    Text("Ajustar", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = CreateTextSecondary)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = CreateTextPrimary)
    }
}

@Composable
fun ProductBasicStep(
    state: CreateProductState,
    categoryOptions: List<String>,
    onStateChange: (CreateProductState) -> Unit,
    isSearching: Boolean = false,
    categorySuggestionState: com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState =
        com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState(),
    onSwitchToManualCategory: () -> Unit = {},
    onBackToAiCategory: () -> Unit = {},
    onAsistManualName: (String) -> Unit = {},
    onAsistManualCategory: (String) -> Unit = {},
    onClearAsistManual: () -> Unit = {},
    onDuplicateClicked: (MoldeProductos) -> Unit = {},
    onApplyNameCorrection: (String, String) -> Unit = { _, _ -> },
    onDismissNameCorrection: (String, String) -> Unit = { _, _ -> },
    onSearchIA: (Boolean) -> Unit = {},
    aiInventoryEnabled: Boolean = true,
    onAddStockToExistingProduct: (MoldeProductos) -> Unit = {},
    onRequestBarcodeScan: () -> Unit = {},
    isCheckingBarcodeRemote: Boolean = false,
    onIdentificarBarcode: (String, String?) -> Unit = { _, _ -> },
    onNext: () -> Unit = {},
    onRequestLabelScan: () -> Unit = {},
    onCheckBarcodeIntegrity: (String, String) -> Unit = { _, _ -> },
    onClearBarcodeIntegrityConflict: () -> Unit = {},
    onSaveSupplier: (String, String) -> Unit = { _, _ -> }
) {
    val nameRequester = remember { BringIntoViewRequester() }
    val categoryRequester = remember { BringIntoViewRequester() }
    val requirementsRequester = remember { BringIntoViewRequester() }

    var isMagicScrolling by remember { mutableStateOf(false) }
    var previouslyMagicTriggered by remember { 
        mutableStateOf(state.categorySelectedFromAi && state.controlType != null) 
    }

    LaunchedEffect(state.categorySelectedFromAi, state.controlType) {
        val currentTrigger = state.categorySelectedFromAi && state.controlType != null
        if (currentTrigger && !previouslyMagicTriggered) {
            isMagicScrolling = true
            delay(150)
            requirementsRequester.bringIntoView()
            delay(800) // Un poco más de tiempo para que se vea suave
            isMagicScrolling = false
        }
        previouslyMagicTriggered = currentTrigger
    }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    val firstErrorKey = remember(state.errors) {
        when {
            !state.errors["name"].isNullOrBlank() -> "name"
            !state.errors["category"].isNullOrBlank() -> "category"
            else -> null
        }
    }

    LaunchedEffect(firstErrorKey) {
        when (firstErrorKey) {
            "name" -> nameRequester.bringIntoView()
            "category" -> categoryRequester.bringIntoView()
        }
    }

    // IA silenciosa: un solo debounce (300 ms en el ViewModel), sin espera extra en UI.
    LaunchedEffect(state.name, state.category, state.categorySelectedFromAi, state.duplicateProductFound) {
        if (
            state.name.length >= 5 && // V28.3: Subimos a 5 letras para evitar lag en tipeo corto
            state.category.isBlank() &&
            !state.categorySelectedFromAi &&
            state.duplicateProductFound == null
        ) {
            delay(400) // V28.3: Respiro extra antes de activar IA
            onSearchIA(false)
        }
    }

    // V18.8: Validación de Integridad (Nombre vs Código) con Debounce
    LaunchedEffect(state.name, state.barcode, state.barcodeAiResult) {
        if (state.name.length > 5 && state.barcode.isNotBlank() && state.barcodeAiResult != null) {
            delay(1000)
            onCheckBarcodeIntegrity(state.barcode, state.name)
        }
    }

    val hasDuplicateProduct = state.duplicateProductFound != null
    val aiBarcodeResult = state.barcodeAiResult
    val aiControlType = when (aiBarcodeResult?.tipoControl) {
        "UNIDAD" -> CreateProductControlType.UNIDAD
        "PESO" -> CreateProductControlType.PESO
        "LIQUIDO" -> CreateProductControlType.LIQUIDO
        else -> null
    }
    val shouldShowAiPrescriptionInfo =
        state.forceManualProductEntry &&
            aiBarcodeResult?.estado == "IDENTIFICADO" &&
            !state.barcodeAiApplied &&
            !state.typeSelectedManually &&
            state.name.trim().stripAccents().equals(aiBarcodeResult.nombre.orEmpty().trim().stripAccents(), ignoreCase = true) &&
            state.category.trim().stripAccents().equals(aiBarcodeResult.categoria.orEmpty().trim().stripAccents(), ignoreCase = true) &&
            state.controlType == aiControlType
    val manualAiPrescriptionSuggestion = categorySuggestionState.sugerenciaTipoManual
        ?.takeIf { suggestion ->
            state.name.trim().stripAccents().equals(suggestion.productName.trim().stripAccents(), ignoreCase = true) &&
                state.category.trim().stripAccents().equals(suggestion.category.trim().stripAccents(), ignoreCase = true)
        }
        ?.requiereReceta

    if (aiInventoryEnabled && !state.forceManualProductEntry && !state.barcodeAiApplied) {
        AiBarcodeProductStep(
            state = state,
            onStateChange = onStateChange,
            onRequestBarcodeScan = onRequestBarcodeScan,
            onAddStockToExistingProduct = onAddStockToExistingProduct,
            onIdentificarBarcode = onIdentificarBarcode,
            isCheckingBarcodeRemote = isCheckingBarcodeRemote,
            onNext = onNext,
            onRequestLabelScan = onRequestLabelScan
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SECCIÓN: IDENTIFICACIÓN (CÓDIGO DE BARRAS) ---
        // Se extrae para optimizar la recomposición cuando el código cambia.
        BarcodeManagementSection(
            state = state,
            isCheckingBarcodeRemote = isCheckingBarcodeRemote,
            onStateChange = onStateChange,
            onClearBarcodeIntegrityConflict = onClearBarcodeIntegrityConflict,
            onRequestBarcodeScan = onRequestBarcodeScan,
            onAddStockToExistingProduct = onAddStockToExistingProduct
        )

        // --- SECCIÓN: IDENTIDAD (BLOQUE UNIFICADO) ---
        val isIdentityValid = state.name.isNotBlank() && state.category.isNotBlank() && state.controlType != null
        
        CreateSectionCard(
            title = "IDENTIDAD DEL PRODUCTO",
            subtitle = "Define el nombre, su clasificación y control",
            icon = Icons.AutoMirrored.Outlined.Label,
            isSuccess = isIdentityValid
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                ProductNameSection(
                    state = state,
                    nameRequester = nameRequester,
                    onStateChange = onStateChange,
                    onClearAsistManual = onClearAsistManual
                )

                CategorySelectionSection(
                    state = state,
                    categoryRequester = categoryRequester,
                    categoryOptions = categoryOptions,
                    categorySuggestionState = categorySuggestionState,
                    onStateChange = onStateChange,
                    onSwitchToManualCategory = onSwitchToManualCategory,
                    onBackToAiCategory = onBackToAiCategory,
                    onClearAsistManual = onClearAsistManual,
                    onControlTypeChange = { controlType ->
                        if (state.stockEntryConfigured || state.presentations.isNotEmpty()) {
                            onStateChange(state.copy(
                                showStep3ResetDialog = true,
                                pendingControlType = controlType
                            ))
                        } else {
                            keyboardController?.hide()
                            focusManager.clearFocus(force = true)
                            val nextState = state.resetStockEntryConfiguration().copy(
                                controlType = controlType,
                                typeSelectedManually = true,
                                presentations = emptyList(),
                                mainPresentationId = "",
                                errors = state.errors - "controlType"
                            )
                            onStateChange(nextState)
                            scope.launch {
                                delay(400)
                                requirementsRequester.bringIntoView()
                            }
                        }
                    }
                )
            }
        }

        // --- SECCIÓN: REQUISITOS (RECETA MÉDICA) ---
        // Oculta hasta que los datos base estén listos.
        ProductRequirementsSection(
            state = state,
            requirementsRequester = requirementsRequester,
            shouldShowAiPrescriptionInfo = shouldShowAiPrescriptionInfo,
            aiPrescriptionSuggestion = manualAiPrescriptionSuggestion,
            onStateChange = onStateChange
        )

        // --- SECCIÓN: UBICACIÓN FÍSICA ---
        ProductLocationSection(
            state = state,
            onStateChange = onStateChange
        )
    }

    if (state.showAddSupplierDialog) {
        AddSupplierDialog(
            onDismiss = { onStateChange(state.copy(showAddSupplierDialog = false)) },
            onSave = onSaveSupplier
        )
    }

    if (isMagicScrolling) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White.copy(alpha = 0.96f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = CreateGreen)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analizando producto...",
                        color = CreateTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Encapsula toda la logica de gestion de codigos de barras (escaneo, manual y duplicados).
 * Su extraccion evita recomposiciones masivas cuando se detecta un conflicto de integridad.
 */
@Composable
private fun BarcodeManagementSection(
    state: CreateProductState,
    isCheckingBarcodeRemote: Boolean,
    onStateChange: (CreateProductState) -> Unit,
    onClearBarcodeIntegrityConflict: () -> Unit,
    onRequestBarcodeScan: () -> Unit,
    onAddStockToExistingProduct: (MoldeProductos) -> Unit
) {
    val hasDuplicateProduct = state.duplicateProductFound != null

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // --- AQUÍ ESTÁ EL CAMBIO ---
        // Quitamos el 'if' restrictivo para que siempre se vea el escáner
        BarcodeSection(
            barcode = state.barcode,
            isManualMode = state.isBarcodeManualMode,
            isChecking = isCheckingBarcodeRemote,
            error = state.errors["barcode"],
            onStartScan = {
                onClearBarcodeIntegrityConflict()

                val resetIdentityForAi = state.barcodeAiApplied || state.barcodeAiResult != null

                onStateChange(state.copy(
                    barcode = "",
                    scannedImageBase64 = null,
                    barcodeAiResult = null,
                    barcodeAiError = null,
                    barcodeAiApplied = false,
                    forceManualProductEntry = if (resetIdentityForAi) false else true,
                    isBarcodeManualMode = false,
                    duplicateProductFound = null,
                    barcodeMismatchDetected = false,
                    barcodeMismatchOriginalName = null,
                    name = if (resetIdentityForAi) "" else state.name,
                    category = if (resetIdentityForAi) "" else state.category,
                    controlType = if (resetIdentityForAi) null else state.controlType,
                    requiresPrescription = if (resetIdentityForAi) false else state.requiresPrescription,
                    errors = state.errors - setOf("barcode", "name", "category", "controlType")
                ))
                onRequestBarcodeScan()
            },
            onManualMode = { onStateChange(state.copy(isBarcodeManualMode = true, duplicateProductFound = null)) },
            onBarcodeChange = { onStateChange(state.copy(barcode = it, duplicateProductFound = null, errors = state.errors - "barcode")) },
            onConfirmManual = { onStateChange(state.copy(isBarcodeManualMode = false)) },
            onClear = {
                onClearBarcodeIntegrityConflict()
                onStateChange(state.copy(
                    barcode = "",
                    barcodeAiResult = null,
                    barcodeAiApplied = false,
                    forceManualProductEntry = true,
                    isBarcodeManualMode = false,
                    duplicateProductFound = null,
                    errors = state.errors - "barcode"
                ))
            }
        )

        // Tarjeta de duplicado: solo aparece si realmente hay un duplicado
        AnimatedVisibility(visible = hasDuplicateProduct) {
            state.duplicateProductFound?.let { producto ->
                DuplicateProductCard(
                    producto = producto,
                    onAddStock = { onAddStockToExistingProduct(producto) }
                )
            }
        }
    }
}

/**
 * Maneja Uicamente el campo de texto del nombre del producto.
 * Optimiza la fluidez del teclado al aislar los cambios de 'state.name'.
 */
@Composable
private fun ProductNameSection(
    state: CreateProductState,
    nameRequester: BringIntoViewRequester,
    onStateChange: (CreateProductState) -> Unit,
    onClearAsistManual: () -> Unit
) {
    val isNameValid = state.name.isNotBlank() && state.errors["name"] == null && !state.isValidatingNameRemote

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppTextField(
            modifier = Modifier.bringIntoViewRequester(nameRequester),
            label = "NOMBRE DEL PRODUCTO",
            value = state.name,
            error = state.errors["name"],
            isSuccess = isNameValid,
            placeholder = "Escribe el nombre o marca...",
            leadingIcon = Icons.AutoMirrored.Outlined.Label,
            trailingIcon = if (state.isValidatingNameRemote) {
                { CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp, color = CreateGreen) }
            } else if (state.name.length >= 3 && state.errors["name"].isNullOrBlank()) {
                { Icon(Icons.Outlined.CheckCircle, null, tint = CreateGreen, modifier = Modifier.size(20.dp)) }
            } else null,
            onValueChange = { input ->
                if (input.isBlank()) {
                    onStateChange(state.copy(
                        name = "",
                        category = "",
                        controlType = null,
                        requiresPrescription = false,
                        categorySelectedFromAi = false,
                        duplicateProductFound = null,
                        errors = state.errors - setOf("name", "category", "controlType")
                    ))
                    onClearAsistManual()
                } else {
                    onStateChange(state.copy(
                        name = input,
                        duplicateProductFound = null,
                        errors = state.errors - "name"
                    ))
                }
            }
        )
    }
}

@Composable
private fun ProductLocationSection(
    state: CreateProductState,
    onStateChange: (CreateProductState) -> Unit
) {
    val hasDuplicateProduct = state.duplicateProductFound != null
    val mostrarUbicacion = !hasDuplicateProduct && 
                          state.name.isNotBlank() && 
                          state.category.isNotBlank() && 
                          state.controlType != null

    AnimatedVisibility(
        visible = mostrarUbicacion,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        CreateSectionCard(
            title = "UBICACIÓN FÍSICA",
            subtitle = "Indica dónde se guarda el producto",
            icon = Icons.Default.Place,
            isSuccess = state.location.isNotBlank()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(
                    label = "",
                    value = state.location,
                    error = state.errors["location"],
                    isSuccess = state.location.isNotBlank(),
                    placeholder = "Ej: Estante B-3, Vitrina 1...",
                    onValueChange = { 
                        onStateChange(state.copy(
                            location = it,
                            errors = state.errors - "location"
                        )) 
                    }
                )
                Text(
                    text = "Es obligatorio indicar la ubicación para continuar.",
                    fontSize = 11.sp,
                    color = CreateTextSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SupplierSelectionSection(
    state: CreateProductState,
    onStateChange: (CreateProductState) -> Unit
) {
    val hasDuplicateProduct = state.duplicateProductFound != null

    if (!hasDuplicateProduct && state.name.isNotBlank()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "PROVEEDOR",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = CreateTextSecondary,
                letterSpacing = 1.2.sp
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Dropdown sutil
                var expanded by remember { mutableStateOf(false) }
                
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = CreateSurfaceSoft,
                    border = BorderStroke(1.dp, CreateBorder)
                ) {
                    Box(modifier = Modifier.clickable { expanded = true }.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Business, null, tint = CreateTextSecondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = state.supplierName.ifBlank { "Seleccionar proveedor..." },
                                color = if (state.supplierName.isBlank()) CreateTextSecondary else CreateTextPrimary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, null, tint = CreateTextSecondary)
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.7f).background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Ninguno", fontSize = 14.sp) },
                                onClick = { 
                                    onStateChange(state.copy(supplierId = "", supplierName = ""))
                                    expanded = false 
                                }
                            )
                            state.suppliers.forEach { proveedor ->
                                DropdownMenuItem(
                                    text = { Text(proveedor.nombre, fontSize = 14.sp) },
                                    onClick = { 
                                        onStateChange(state.copy(supplierId = proveedor.id, supplierName = proveedor.nombre))
                                        expanded = false 
                                    }
                                )
                            }
                        }
                    }
                }

                // Botón Añadir
                IconButton(
                    onClick = { onStateChange(state.copy(showAddSupplierDialog = true)) },
                    modifier = Modifier.size(48.dp).background(CreateGreen.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Add, null, tint = CreateGreen)
                }
            }
        }
    }
}

@Composable
private fun AddSupplierDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var idFiscal by remember { mutableStateOf("") }
    
    val pais = com.app.administradorfarmadon.ClasesDatabase.SessionManager.paisOperacion.lowercase()
    val labelId = when {
        pais.contains("venezuela") -> "RIF"
        pais.contains("peru") || pais.contains("perú") -> "RUC"
        pais.contains("colombia") -> "NIT"
        pais.contains("mexico") || pais.contains("méxico") -> "RFC"
        else -> "Identificación Fiscal"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Proveedor", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre / Razón Social") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = idFiscal,
                    onValueChange = { idFiscal = it },
                    label = { Text(labelId) },
                    placeholder = { Text("Nro de identificación...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (nombre.isNotBlank()) onSave(nombre, idFiscal) },
                colors = ButtonDefaults.buttonColors(containerColor = CreateGreen),
                enabled = nombre.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar Proveedor", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = CreateTextSecondary)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * Gestiona el campo de categoria, el asistente de Gemini y el selector de tipo.
 */
@Composable
private fun CategorySelectionSection(
    state: CreateProductState,
    categoryRequester: BringIntoViewRequester,
    categoryOptions: List<String>,
    categorySuggestionState: com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState,
    onStateChange: (CreateProductState) -> Unit,
    onSwitchToManualCategory: () -> Unit,
    onBackToAiCategory: () -> Unit,
    onClearAsistManual: () -> Unit,
    onControlTypeChange: (CreateProductControlType) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val hasDuplicateProduct = state.duplicateProductFound != null

    // V28.7: Comportamiento Premium - Ocultar teclado cuando el usuario espera la sugerencia
    LaunchedEffect(categorySuggestionState.status) {
        val currentStatus = categorySuggestionState.status
        val isWaiting = currentStatus == com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionStatus.LOADING ||
                       currentStatus == com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionStatus.READY
        
        // Solo ocultamos si NO estamos en modo manual forzado por el usuario y el campo está vacío
        if (isWaiting && state.category.isBlank()) {
            delay(100)
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        }
    }

    if (!hasDuplicateProduct && state.name.isNotBlank()) {
        CategoryAutocompleteField(
            modifier = Modifier.bringIntoViewRequester(categoryRequester),
            value = state.category,
            productName = state.name,
            options = categoryOptions,
            error = state.errors["category"],
            controlType = state.controlType,
            controlTypeError = state.errors["controlType"],
            suggestionState = categorySuggestionState,
            onSwitchToManual = onSwitchToManualCategory,
            onBackToAi = onBackToAiCategory,
            categorySelectedFromAi = state.categorySelectedFromAi,
            onValueChange = {
                onStateChange(state.copy(
                    category = it,
                    errors = state.errors - "category",
                    categorySelectedFromAi = false
                ))
            },
            onCategorySelectedFromSuggestion = { selected ->
                onStateChange(state.copy(
                    category = selected,
                    categorySelectedFromAi = true,
                    typeSelectedManually = false,
                    controlType = null,
                    errors = state.errors - "category"
                ))
                onClearAsistManual()
                // Toque Premium: Ocultar teclado suavemente
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
            },
            onControlTypeChange = onControlTypeChange
        )
    }
}

/**
 * Seccion final de requisitos adicionales como receta medica.
 * Solo aparece cuando los datos base son validos.
 */
@Composable
private fun ProductRequirementsSection(
    state: CreateProductState,
    requirementsRequester: BringIntoViewRequester,
    shouldShowAiPrescriptionInfo: Boolean,
    aiPrescriptionSuggestion: Boolean?,
    onStateChange: (CreateProductState) -> Unit
) {
    val hasDuplicateProduct = state.duplicateProductFound != null
    val mostrarCamposSecundarios =
        !hasDuplicateProduct &&
        state.name.isNotBlank() &&
        state.category.isNotBlank() &&
        state.controlType != null

    AnimatedVisibility(
        visible = mostrarCamposSecundarios,
        enter = fadeIn() + slideInVertically { it / 6 },
        exit = fadeOut() + slideOutVertically { -it / 6 }
    ) {
        Column(
            modifier = Modifier.bringIntoViewRequester(requirementsRequester),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "REQUISITOS",
                color = CreateTextSecondary.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(
                    width = if (state.name.isNotBlank() && state.category.isNotBlank() && state.controlType != null) 2.dp else 1.dp,
                    color = if (state.name.isNotBlank() && state.category.isNotBlank() && state.controlType != null) CreateGreen else CreateBorder
                )
            ) {
                Column {
                    CompactSwitchRow(
                        title = "¿Requiere receta médica?",
                        subtitle = "Actívalo si solicitas receta al vender",
                        checked = state.requiresPrescription,
                        onCheckedChange = {
                            onStateChange(state.copy(requiresPrescription = it))
                        }
                    )
                    
                    if (shouldShowAiPrescriptionInfo || aiPrescriptionSuggestion != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(CreateBorder.copy(alpha = 0.55f))
                        )
                        val sourceRequires = if (shouldShowAiPrescriptionInfo) 
                            state.barcodeAiResult?.requiereReceta == true 
                        else aiPrescriptionSuggestion == true
                        
                        AiPrescriptionInfoRow(requiresPrescription = sourceRequires)
                    }
                }
            }
        }
    }
}

@Composable
fun InitialLotStep(
    state: CreateProductState,
    existingLotNumbers: Set<String>,
    onStateChange: (CreateProductState) -> Unit,
    // Estado y disparador del escaner OCR (camara + Gemini Vision).
    labelScannerState: com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.UiState =
        com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.UiState(),
    onRequestLabelScan: () -> Unit = {},
    onConsumeLabelScan: () -> Unit = {},
    isCheckingLotRemote: Boolean = false,
    lotConflictInfo: String? = null,
    lotConflictColor: Color = Color.Transparent,
    lotConflictSeverity: Int = 0,
    onSaveSupplier: (String, String) -> Unit = { _, _ -> }
) {
    val lotRequester = remember { BringIntoViewRequester() }
    val expirationRequester = remember { BringIntoViewRequester() }
    val entryModeRequester = remember { BringIntoViewRequester() }
    val minimumStockRequester = remember { BringIntoViewRequester() }
    val purchaseCostRequester = remember { BringIntoViewRequester() }

    var isMagicScrolling by remember { mutableStateOf(false) }
    var previouslyConfigured by remember { mutableStateOf(state.stockEntryConfigured) }

    LaunchedEffect(state.stockEntryConfigured) {
        if (state.stockEntryConfigured && !previouslyConfigured) {
            isMagicScrolling = true
            delay(150)
            minimumStockRequester.bringIntoView()
            delay(600)
            isMagicScrolling = false
        }
        previouslyConfigured = state.stockEntryConfigured
    }

    val expirationStatus = remember(state.expirationDate) {
        evaluateExpirationStatus(state.expirationDate)
    }

    // Fix: se removieron un `when` y un `remember` cuyos valores no se asignaban
    // ni se consuman. El resumen ya se calcula dentro de StockConfiguredCard y
    // StockEntryDialog, y la etiqueta de unidad la usa cada subcomponente que
    // la necesita. Aqui solo se manteniean como codigo muerto que disparaba
    // recomposiciones innecesarias.

    val normalizedLot = remember(state.lotNumber) {
        state.lotNumber.trim().uppercase()
    }

    val lotExists = remember(normalizedLot, existingLotNumbers) {
        normalizedLot.isNotBlank() && existingLotNumbers.contains(normalizedLot)
    }

    val isLotInfoReady = remember(
        state.lotNumber,
        state.expirationDate,
        state.errors,
        expirationStatus,
        lotExists,
        lotConflictSeverity
    ) {
        state.lotNumber.isNotBlank() &&
            state.expirationDate.isNotBlank() &&
            !lotExists &&
            lotConflictSeverity == 0 &&
            state.errors["lotNumber"].isNullOrBlank() &&
            state.errors["expirationDate"].isNullOrBlank() &&
            expirationStatus?.kind != ExpirationStatusKind.INVALIDO &&
            expirationStatus?.kind != ExpirationStatusKind.VENCIDO
    }

    val suggestedLot = remember(state.name, normalizedLot, existingLotNumbers) {
        suggestUniqueLotNumber(
            productName = state.name,
            currentLot = normalizedLot,
            existingLots = existingLotNumbers
        )
    }

    val firstErrorKey = remember(state.errors, expirationStatus, lotExists) {
        when {
            lotExists || !state.errors["lotNumber"].isNullOrBlank() -> "lotNumber"
            expirationStatus?.kind == ExpirationStatusKind.VENCIDO ||
                    expirationStatus?.kind == ExpirationStatusKind.INVALIDO ||
                    !state.errors["expirationDate"].isNullOrBlank() -> "expirationDate"
            !state.errors["stockEntryMode"].isNullOrBlank() -> "stockEntryMode"
            !state.errors["minimumStock"].isNullOrBlank() -> "minimumStock"
            !state.errors["purchaseCost"].isNullOrBlank() -> "purchaseCost"
            else -> null
        }
    }

    LaunchedEffect(firstErrorKey) {
        when (firstErrorKey) {
            "lotNumber" -> lotRequester.bringIntoView()
            "expirationDate" -> expirationRequester.bringIntoView()
            "stockEntryMode" -> entryModeRequester.bringIntoView()
            "minimumStock" -> minimumStockRequester.bringIntoView()
            "purchaseCost" -> purchaseCostRequester.bringIntoView()
        }
    }

    var showManualLotEntry by remember {
        mutableStateOf(state.lotNumber.isNotBlank() || state.expirationDate.isNotBlank())
    }
    LaunchedEffect(state.lotNumber, state.expirationDate) {
        if (state.lotNumber.isNotBlank() || state.expirationDate.isNotBlank()) {
            showManualLotEntry = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        CreateSectionCard(
            title = "Lote y vencimiento",
            subtitle = "Escanea o completa manualmente.",
            icon = Icons.Outlined.Inventory2,
            isSuccess = isLotInfoReady,
            onClear = if (showManualLotEntry || state.lotScanned) {
                {
                    showManualLotEntry = false
                    onStateChange(state.copy(
                        lotNumber = "",
                        expirationDate = "",
                        lotScanned = false,
                        errors = state.errors - setOf("lotNumber", "expirationDate")
                    ))
                }
            } else null
        ) {
            val isScanning = labelScannerState.status == com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.Status.LOADING ||
                             labelScannerState.status == com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.Status.READY

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // CASO 1: Ninguno seleccionado (Estado Inicial)
                if (!showManualLotEntry && !isScanning) {
                    Button(
                        onClick = onRequestLabelScan,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CreateGreen)
                    ) {
                        Text("Escanear", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showManualLotEntry = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, CreateBorder)
                    ) {
                        Text("Ingresar manual", color = CreateTextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
                // CASO 2: Modo Escanear Activo
                else if (isScanning) {
                    OutlinedButton(
                        onClick = onConsumeLabelScan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, CreateRed.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CreateRed)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Escanear", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                // CASO 3: Modo Manual o Escaneado (Ahora se maneja con la 'X' en el header)
            }

            // Boton para escanear la etiqueta con camara + Gemini Vision.
            // Llena automÃ¡ticamente "Numero de lote" y "Vencimiento" si la
            // foto los muestra legibles. El usuario siempre puede editar.
            LabelScannerRow(
                state = labelScannerState,
                onRequestScan = onRequestLabelScan,
                onApplyResult = { etiqueta ->
                    // Auto-aplicar resultados al formulario. NO reseteamos
                    // el ViewModel despues: queremos que la tarjeta READY
                    // se quede visible como confirmacion + opcion "Tomar
                    // otra foto". El usuario podra¡ tomar otra foto sin
                    // necesidad de pulsar "Aplicar" cada vez.
                    var nuevoEstado = state
                    etiqueta.loteNumero?.let { lote ->
                        nuevoEstado = nuevoEstado.copy(
                            lotNumber = lote,
                            errors = nuevoEstado.errors - "lotNumber",
                            lotScanned = true
                        )
                    }
                    // Solo aplicamos el vencimiento si está vigente. Vencidos
                    // o inválidos se ignoran para no contaminar el formulario;
                    // la tarjeta de escaneo ya advierte al usuario.
                    etiqueta.vencimientoMmAa?.let { venc ->
                        if (esVencimientoVigenteSimple(venc)) {
                            nuevoEstado = nuevoEstado.copy(
                                expirationDate = venc,
                                errors = nuevoEstado.errors - "expirationDate",
                                lotScanned = true
                            )
                        }
                    }
                    onStateChange(nuevoEstado)
                },
                onDismiss = onConsumeLabelScan,
                showScanButton = false
            )

            // Solo ocultamos los inputs durante LOADING (procesamiento de la
            // foto). En READY los datos ya se aplicaron automÃ¡ticamente y los
            // inputs se muestran rellenados para que el usuario pueda
            // ajustarlos si la IA detecta algo distinto a lo que ve.
            val escaneoActivo = labelScannerState.status ==
                com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.Status.LOADING
            val escaneoListo = labelScannerState.status ==
                com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.Status.READY

            // V19.3: Si escaneó con éxito, mostramos un Card de Resumen en lugar de inputs.
            AnimatedVisibility(
                visible = escaneoListo,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    color = CreateAiFocusSoft.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, CreateAiFocus.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Lightbulb,
                                    contentDescription = null,
                                    tint = CreateAiFocus,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "LOTE DETECTADO POR IA",
                                    color = CreateAiFocus,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }

                            // X para cancelar el resultado escaneado
                            IconButton(
                                onClick = {
                                    onConsumeLabelScan() // Cancela el estado del scanner
                                    onStateChange(state.copy(
                                        lotNumber = "",
                                        expirationDate = "",
                                        errors = state.errors - setOf("lotNumber", "expirationDate")
                                    ))
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancelar resultado",
                                    tint = CreateRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Número de Lote", color = CreateTextSecondary, fontSize = 11.sp)
                                Text(
                                    text = state.lotNumber.ifBlank { "No detectado" },
                                    color = CreateTextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("Vencimiento", color = CreateTextSecondary, fontSize = 11.sp)
                                Text(
                                    text = state.expirationDate.ifBlank { "No detectada" },
                                    color = if (expirationStatus?.kind == ExpirationStatusKind.POR_VENCER) CreateOrange else CreateTextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (expirationStatus != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• ${expirationStatus.message}",
                                color = expirationStatus.color,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = !escaneoActivo && !escaneoListo && showManualLotEntry,
                enter = fadeIn() + slideInVertically { it / 8 },
                exit = fadeOut() + slideOutVertically { -it / 8 }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val isLotValidated = state.lotNumber.isNotBlank() && 
                                !isCheckingLotRemote && 
                                lotConflictSeverity != 2 &&
                                state.errors["lotNumber"].isNullOrBlank()
            val isExpirationValid = state.expirationDate.isNotBlank() &&
                    expirationStatus?.kind != ExpirationStatusKind.VENCIDO &&
                    expirationStatus?.kind != ExpirationStatusKind.INVALIDO &&
                    state.errors["expirationDate"].isNullOrBlank()

            val lotField: @Composable (Modifier) -> Unit = { fieldModifier ->
                AppTextField(
                    modifier = fieldModifier.bringIntoViewRequester(lotRequester),
                    label = "",
                    value = state.lotNumber,
                    isSuccess = isLotValidated && !lotExists,
                    error = when {
                        lotExists -> "Ese lote ya existe"
                        else -> state.errors["lotNumber"]
                    },
                    helper = when {
                        isCheckingLotRemote -> "Verificando disponibilidad de lote..."
                        lotExists -> "Ese lote ya existe"
                        else -> null
                    },
                    helperHighlighted = !isCheckingLotRemote && lotConflictSeverity == 0 && !state.lotNumber.isBlank(),
                    placeholder = "Nro de lote",
                    keyboardType = KeyboardType.Ascii,
                    trailingIcon = if (isCheckingLotRemote) {
                        { CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = CreateGreen) }
                    } else if (isLotValidated && lotConflictSeverity == 0) {
                        { Icon(Icons.Outlined.CheckCircle, null, tint = CreateGreen, modifier = Modifier.size(18.dp)) }
                    } else null,
                    onValueChange = {
                        val sanitized = it.uppercase().replace(" ", "")
                        onStateChange(
                            state.copy(
                                lotNumber = sanitized,
                                errors = state.errors - "lotNumber"
                            )
                        )
                    }
                )
            }

            val expirationField: @Composable (Modifier) -> Unit = { fieldModifier ->
                ExpirationDateField(
                    modifier = fieldModifier.bringIntoViewRequester(expirationRequester),
                    label = "",
                    value = state.expirationDate,
                    isSuccess = isExpirationValid,
                    enabled = isLotValidated, // V19.4: Solo habilitar si el lote es vÃ¡lido
                    error = when {
                        expirationStatus?.kind == ExpirationStatusKind.VENCIDO ->
                            "La fecha ingresada ya vencio."
                        expirationStatus?.kind == ExpirationStatusKind.INVALIDO ->
                            expirationStatus.message
                        else -> state.errors["expirationDate"]
                    },
                    helper = expirationStatus?.takeIf {
                        it.kind != ExpirationStatusKind.INVALIDO &&
                            it.kind != ExpirationStatusKind.VENCIDO
                    }?.message,
                    helperColor = expirationStatus?.color ?: CreateTextSecondary,
                    onValueChange = {
                        onStateChange(
                            state.copy(
                                expirationDate = formatExpirationDateInput(it),
                                errors = state.errors - "expirationDate"
                            )
                        )
                    }
                )
            }

            // V19.2: Fila horizontal 50/50 para Lote y Vencimiento (Solicitud usuario)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    lotField(Modifier.fillMaxWidth())
                }
                Box(modifier = Modifier.weight(1f)) {
                    expirationField(Modifier.fillMaxWidth())
                }
            }
                }  // fin Column dentro de AnimatedVisibility
            }      // fin AnimatedVisibility (escaneo activo)
        }

        AnimatedVisibility(
            visible = isLotInfoReady,
            enter = fadeIn() + slideInVertically { it / 6 },
            exit = fadeOut() + slideOutVertically { -it / 10 }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                // V27.0: SECCIÓN - DATOS DE PAGO Y PROVEEDOR
                val isPaymentDataValid = state.supplierId.isNotBlank() && 
                                       (state.purchaseCost.trim().replace(",", ".").toDoubleOrNull()?.let { it > 0.0 } ?: false) &&
                                       (if (state.paymentCondition == "CREDITO") state.paymentDueDate.isNotBlank() && state.errors["paymentDueDate"] == null else true)

                CreateSectionCard(
                    title = "Datos de pago",
                    subtitle = "Factura, costo y condición de pago",
                    icon = Icons.AutoMirrored.Outlined.Assignment,
                    isSuccess = isPaymentDataValid
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // 1. PROVEEDOR (Movido al paso 2)
                        SupplierSelectionSection(
                            state = state,
                            onStateChange = onStateChange
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CreateBorder.copy(alpha = 0.5f)))
                        Spacer(modifier = Modifier.height(8.dp))

                        // 2. FACTURA Y COSTO
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                AppTextField(
                                    label = "NRO FACTURA",
                                    value = state.invoiceNumber,
                                    placeholder = "Ej: F001-234",
                                    keyboardType = KeyboardType.Ascii,
                                    onValueChange = { onStateChange(state.copy(invoiceNumber = it)) }
                                )
                            }
                            
                            // Botón para foto de factura
                            Surface(
                                onClick = { onStateChange(state.copy(isCapturingInvoice = true)) },
                                color = if (state.invoiceImageBase64 != null) CreateGreenSoft else CreateBackground,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (state.invoiceImageBase64 != null) CreateGreen else CreateBorder),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (state.invoiceImageBase64 != null) Icons.Outlined.CheckCircle else Icons.Default.Receipt,
                                        contentDescription = "Foto factura",
                                        tint = if (state.invoiceImageBase64 != null) CreateGreen else CreateTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                AppTextField(
                                    label = "COSTO TOTAL",
                                    value = state.purchaseCost,
                                    error = state.errors["purchaseCost"],
                                    isSuccess = state.purchaseCost.trim().replace(",", ".").toDoubleOrNull()?.let { it > 0.0 } ?: false,
                                    placeholder = "0.00",
                                    keyboardType = KeyboardType.Decimal,
                                    onValueChange = {
                                        onStateChange(
                                            state.copy(
                                                purchaseCost = it,
                                                errors = state.errors - "purchaseCost"
                                            )
                                        )
                                    }
                                )
                            }
                        }

                        // 3. CONDICIÓN DE PAGO
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "CONDICIÓN DE PAGO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = CreateTextSecondary,
                                letterSpacing = 1.2.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                listOf("CONTADO", "CREDITO").forEach { condition ->
                                    val isSel = state.paymentCondition == condition
                                    Surface(
                                        modifier = Modifier.weight(1f).clickable { onStateChange(state.copy(paymentCondition = condition)) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSel) CreateGreen.copy(alpha = 0.1f) else CreateSurfaceSoft,
                                        border = BorderStroke(1.dp, if (isSel) CreateGreen else CreateBorder)
                                    ) {
                                        Box(modifier = Modifier.padding(14.dp), contentAlignment = Alignment.Center) {
                                            Text(
                                                text = condition,
                                                color = if (isSel) CreateGreen else CreateTextSecondary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(visible = state.paymentCondition == "CREDITO") {
                            FullDateField(
                                label = "VENCIMIENTO DEL PAGO",
                                value = state.paymentDueDate,
                                isSuccess = state.paymentDueDate.isNotBlank() && state.errors["paymentDueDate"] == null,
                                error = state.errors["paymentDueDate"],
                                onValueChange = {
                                    onStateChange(state.copy(
                                        paymentDueDate = it,
                                        errors = state.errors - "paymentDueDate"
                                    ))
                                }
                            )
                        }

                        // 4. COSTO UNITARIO DERIVADO (Informativo)
                        val costoTotal = state.purchaseCost
                            .replace("Bs", "", ignoreCase = true)
                            .trim()
                            .replace(",", ".")
                            .toDoubleOrNull()
                        val recibidoBase = calculateRecibidoBase(state)
                        if (costoTotal != null && costoTotal > 0.0 && recibidoBase > 0.0) {
                            val unitario = costoTotal / recibidoBase
                            val unitLabel = resolveStockUnitLabel(state)
                            Surface(
                                color = CreateSurfaceSoft,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, CreateBorder.copy(alpha = 0.75f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = CreateGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Costo por $unitLabel:",
                                        color = CreateTextSecondary,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Bs " + String.format(Locale.US, "%.4f", unitario)
                                            .trimEnd('0').trimEnd('.'),
                                        color = CreateTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = isPaymentDataValid,
                    enter = fadeIn() + slideInVertically { it / 6 },
                    exit = fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        CreateSectionCard(
                            title = "Cómo se controla",
                            subtitle = if (state.stockEntryConfigured) "Resumen del ingreso actual" else "Elige cómo se descontará en inventario",
                            icon = Icons.Outlined.AllInbox,
                            isSuccess = state.stockEntryConfigured
                        ) {
                            Box(modifier = Modifier.bringIntoViewRequester(entryModeRequester)) {
                                if (!state.stockEntryConfigured) {
                                    PremiumStockEntryModeSelector(
                                        controlType = state.controlType,
                                        selected = state.stockEntryMode,
                                        onSelected = { mode ->
                                            if (state.presentations.isNotEmpty()) {
                                                onStateChange(state.copy(
                                                    showStep3ResetDialog = true,
                                                    pendingStockEntryMode = mode
                                                ))
                                            } else {
                                                onStateChange(
                                                    state.copy(
                                                        stockEntryMode = mode,
                                                        showStockEntryDialog = true,
                                                        errors = state.errors -
                                                                setOf(
                                                                    "stockEntryMode",
                                                                    "receivedUnits",
                                                                    "boxesReceived",
                                                                    "unitsPerBox",
                                                                    "packagesPerBox",
                                                                    "unitsPerPackage"
                                                                )
                                                    )
                                                )
                                            }
                                        }
                                    )
                                } else {
                                    StockConfiguredCard(
                                        state = state,
                                        onEditQuantity = {
                                            onStateChange(state.copy(showStockEntryDialog = true))
                                        },
                                        onChangeType = {
                                            if (state.presentations.isNotEmpty()) {
                                                onStateChange(state.copy(
                                                    showStep3ResetDialog = true,
                                                    pendingStockEntryMode = null
                                                ))
                                            } else {
                                                onStateChange(
                                                    state.copy(
                                                        stockEntryConfigured = false,
                                                        showStockEntryDialog = false,
                                                        stockEntryMode = null,
                                                        receivedUnitsText = "",
                                                        boxesReceivedText = "",
                                                        unitsPerBoxText = "",
                                                        packagesPerBoxText = "",
                                                        unitsPerPackageText = "",
                                                        errors = state.errors -
                                                                setOf(
                                                                    "receivedUnits",
                                                                    "boxesReceived",
                                                                    "unitsPerBox",
                                                                    "packagesPerBox",
                                                                    "unitsPerPackage"
                                                                )
                                                    )
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = state.stockEntryConfigured,
                            enter = fadeIn() + slideInVertically { it / 6 },
                            exit = fadeOut()
                        ) {
                            Box(modifier = Modifier.bringIntoViewRequester(minimumStockRequester)) {
                                MinimumStockInputCard(
                                    state = state,
                                    onStateChange = onStateChange
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showAddSupplierDialog) {
        AddSupplierDialog(
            onDismiss = { onStateChange(state.copy(showAddSupplierDialog = false)) },
            onSave = onSaveSupplier
        )
    }

    if (isMagicScrolling) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White.copy(alpha = 0.96f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = CreateGreen)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Configurando inventario...",
                        color = CreateTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    if (state.showStockEntryDialog) {
        StockEntryDialog(
            state = state,
            onStateChange = onStateChange,
            onApply = {
                // El stock mÃ­nimo se calcula automÃ¡ticamente en la tarjeta
                // `MinimumStockStepperCard` a partir del porcentaje (default 20%).
                // Ya no auto-rellenamos aquÃ­; el LaunchedEffect del stepper
                // lo sincroniza en cuanto se renderiza.
                onStateChange(
                    state.copy(
                        showStockEntryDialog = false,
                        stockEntryConfigured = true
                    )
                )
            },
            onDismiss = {
                onStateChange(
                    if (state.stockEntryConfigured) {
                        state.copy(showStockEntryDialog = false)
                    } else {
                        state.copy(
                            showStockEntryDialog = false,
                            stockEntryMode = null,
                            receivedUnitsText = "",
                            boxesReceivedText = "",
                            unitsPerBoxText = "",
                            packagesPerBoxText = "",
                            unitsPerPackageText = "",
                            errors = state.errors - setOf(
                                "stockEntryMode",
                                "receivedUnits",
                                "boxesReceived",
                                "unitsPerBox",
                                "packagesPerBox",
                                "unitsPerPackage"
                            )
                        )
                    }
                )
            }
        )
    }
}


/**
 * Tarjeta de stock mínimo basada en porcentaje.
 *
 * Si el inventario se controla en piezas enteras (frascos, blísteres, cajas),
 * solo permite opciones enteras válidas. Así 5 frascos generan 20%, 40%,
 * 60%, 80% y 100%, nunca 5%.
 */
@Composable
fun MinimumStockInputCard(
    state: CreateProductState,
    onStateChange: (CreateProductState) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val mode = remember(state.controlType, state.stockEntryMode, state.stockControlMode) {
        resolveMinimumStockControlMode(state)
    }
    
    // V21.0: Sincronización Profesional - El stepper ahora opera en la unidad VISUAL
    // seleccionada por el usuario (kg, L, etc.) para evitar números absurdos como "1000 kg".
    val totalReference = remember(state, mode) {
        when (mode) {
            StockControlMode.INDIVISIBLE -> calculateTotalPhysicalUnits(state)
            StockControlMode.DIVISIBLE -> {
                val base = calculateTotalBaseStock(state)
                if (state.stockEntryUnit == "kg" || state.stockEntryUnit == "L") {
                    (base / 1000.0).toInt()
                } else {
                    base.toInt()
                }
            }
        }.coerceAtLeast(0)
    }
    
    val validOptions = remember(totalReference, mode) {
        calculateValidStockOptions(totalReference, mode)
    }
    
    val currentSelection = state.minimumStockUnits
    val unitLabel = resolveMinimumStockLabel(state, currentSelection, mode)
    val storedUnit = resolveMinimumStockStoredUnit(state, currentSelection, mode)
    
    val percentage = remember(currentSelection, totalReference) {
        if (totalReference > 0) {
            (currentSelection.toDouble() / totalReference.toDouble()) * 100.0
        } else 0.0
    }
    
    val formattedPercent = remember(percentage) {
        if (percentage % 1.0 == 0.0) "${percentage.toInt()}%"
        else "aprox. ${String.format(Locale.US, "%.1f", percentage)}%"
    }
    
    val totalLabel = resolveMinimumStockLabel(state, totalReference, mode)

    // Solo auto-inicializamos si el valor actual no es válido para el nuevo rango
    LaunchedEffect(validOptions) {
        if (validOptions.isNotEmpty() && currentSelection !in validOptions) {
            val nextValue = if (currentSelection > totalReference) {
                validOptions.last()
            } else if (currentSelection <= 0) {
                // Default al 20% si es posible
                val target = (totalReference * 0.2).toInt()
                validOptions.minByOrNull { kotlin.math.abs(it - target) } ?: validOptions.first()
            } else {
                validOptions.minByOrNull { kotlin.math.abs(it - currentSelection) } ?: validOptions.first()
            }
            
            onStateChange(
                state.copy(
                    minimumStockUnits = nextValue,
                    minimumStockText = nextValue.toString(),
                    minimumStockUnit = resolveMinimumStockStoredUnit(state, nextValue, mode),
                    errors = state.errors - "minimumStock"
                )
            )
        }
    }

    val isMinimumStockValid = state.minimumStockUnits > 0 && state.errors["minimumStock"] == null

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = if (isMinimumStockValid) 2.dp else 1.dp,
            color = if (isMinimumStockValid) CreateGreen else CreateBorder.copy(alpha = 0.85f)
        ),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = CreateGreenSoft,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = CreateGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Stock mínimo",
                        color = CreateTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Define desde qué nivel quieres recibir alerta.",
                        color = CreateTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = CreateSurfaceSoft,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CreateBorder.copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        Text(
                            text = "Control",
                            color = CreateTextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = resolveMinimumStockLabel(state, 2, mode).replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                            },
                            color = CreateTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    color = CreateSurfaceSoft,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CreateBorder.copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        Text(
                            text = "Total recibido",
                            color = CreateTextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "$totalReference $totalLabel",
                            color = CreateTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Alertar cuando queden",
                    color = CreateTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                // V21.8: Visualización Profesional por "Contenedores" (Frascos/Blísteres)
                val visualValue = currentSelection
                val label = unitLabel
                val baseEquivalence = when (mode) {
                    StockControlMode.INDIVISIBLE -> {
                        when (state.controlType) {
                            CreateProductControlType.UNIDAD -> {
                                if (state.stockEntryMode == CreateProductStockEntryMode.CAJA_CON_PAQUETES)
                                    parseCreateProductNumber(state.unitsPerPackageText)
                                else 1.0
                            }
                            else -> parseCreateProductNumber(state.unitsPerItemText)
                        }
                    }
                    else -> 1.0
                }
                val totalUnits = (visualValue * baseEquivalence).toInt()
                val baseUnit = resolveStockUnitLabel(state)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = visualValue.toString(),
                            color = CreateGreen,
                            fontWeight = FontWeight.Black,
                            fontSize = 44.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            color = CreateTextSecondary,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    // Sub-texto de equivalencia pequeña para que el usuario entienda la escala
                    if (baseEquivalence > 1.0) {
                        Surface(
                            color = CreateBackground,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "Equivale a $totalUnits $baseUnit",
                                color = CreateTextSecondary.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = CreateGreenSoft.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "$formattedPercent del stock actual",
                        color = CreateTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentIndex = validOptions.indexOf(currentSelection)
                val canDecrease = currentIndex > 0
                val canIncrease = currentIndex >= 0 && currentIndex < validOptions.size - 1

                IconButton(
                    onClick = {
                        if (canDecrease) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val next = validOptions[currentIndex - 1]
                            onStateChange(
                                state.copy(
                                    minimumStockUnits = next,
                                    minimumStockText = next.toString(),
                                    minimumStockUnit = resolveMinimumStockStoredUnit(state, next, mode),
                                    errors = state.errors - "minimumStock"
                                )
                            )
                        }
                    },
                    enabled = canDecrease,
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            if (canDecrease) CreateSurfaceSoft else CreateBackground,
                            CircleShape
                        )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_remove_24),
                        contentDescription = "Disminuir",
                        tint = if (canDecrease) CreateGreen else CreateTextSecondary.copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                IconButton(
                    onClick = {
                        if (canIncrease) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val next = validOptions[currentIndex + 1]
                            onStateChange(
                                state.copy(
                                    minimumStockUnits = next,
                                    minimumStockText = next.toString(),
                                    minimumStockUnit = resolveMinimumStockStoredUnit(state, next, mode),
                                    errors = state.errors - "minimumStock"
                                )
                            )
                        }
                    },
                    enabled = canIncrease,
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            if (canIncrease) CreateGreenSoft else CreateBackground,
                            CircleShape
                        )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_add_24),
                        contentDescription = "Aumentar",
                        tint = if (canIncrease) CreateGreen else CreateTextSecondary.copy(alpha = 0.4f)
                    )
                }
            }

            Text(
                text = if (mode == StockControlMode.INDIVISIBLE)
                    "Solo se permiten cantidades completas."
                else
                    "La alerta sigue tramos válidos sobre el total ingresado.",
                color = CreateTextSecondary.copy(alpha = 0.8f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            if (!state.errors["minimumStock"].isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = state.errors["minimumStock"].orEmpty(),
                    color = CreateRed,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StepperButton(
    icon: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }
    LaunchedEffect(pressed) {
        if (pressed) {
            scale.animateTo(0.88f, spring(stiffness = Spring.StiffnessMedium))
        } else {
            scale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium))
        }
    }
    Surface(
        modifier = Modifier
            .size(52.dp)
            .graphicsScale(scale.value)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        shape = CircleShape,
        color = if (enabled) CreateGreen else Color(0xFFE5EAF0),
        shadowElevation = if (enabled) 2.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = icon,
                color = if (enabled) Color.White else CreateTextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            )
        }

    }
}

/**
 * Texto corto y entendible del desglose: "5 cajas Ã— 100 u = 500 u",
 * "10 cajas Ã— 5 frascos Ã— 120 mL = 6000 mL", etc.
 */
private fun descripcionRecibidoCorto(
    state: CreateProductState,
    unitLabel: String
): String {
    fun plural(n: Double, s: String, p: String) = if (n == 1.0) s else p
    
    return when (state.stockEntryMode) {
        CreateProductStockEntryMode.UNIDAD -> {
            val q = parseCreateProductNumber(state.receivedUnitsText)
            if (q <= 0.0) "" else "${formatCreateProductNumber(q)} $unitLabel"
        }
        CreateProductStockEntryMode.CAJA -> {
            val boxes = parseCreateProductNumber(state.boxesReceivedText)
            val perBox = parseCreateProductNumber(state.unitsPerBoxText)
            val total = calculateTotalBaseStock(state)
            val visualTotal = if (state.stockEntryUnit == "kg" || state.stockEntryUnit == "L") total / 1000.0 else total
            
            if (boxes <= 0.0 || perBox <= 0.0) "" else
                "Recibiste ${formatCreateProductNumber(boxes)} ${plural(boxes, "caja", "cajas")} (${formatCreateProductNumber(visualTotal)} $unitLabel total)"
        }
        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
            val boxes = parseCreateProductNumber(state.boxesReceivedText)
            val total = calculateTotalBaseStock(state)
            
            if (boxes <= 0.0) "" else
                "Recibiste ${formatCreateProductNumber(boxes)} ${plural(boxes, "blíster", "blísters")} (${formatCreateProductNumber(total)} $unitLabel total)"
        }
        null -> ""
    }
}

@Composable
fun StockConfiguredCard(
    state: CreateProductState,
    onEditQuantity: () -> Unit,
    onChangeType: () -> Unit
) {
    val summary = buildInitialStockEntrySummary(state)
    val modeTitle = remember(state.stockEntryMode, state.controlType) {
        configuredStockModeTitle(state)
    }
    val modeSubtitle = remember(state.stockEntryMode, state.controlType) {
        configuredStockModeSubtitle(state)
    }
    val availableText = remember(state) { buildStockAvailableSummary(state) }

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, CreateBorder.copy(alpha = 0.85f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = CreateGreenSoft,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = CreateGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ingreso configurado",
                        color = CreateTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = modeSubtitle,
                        color = CreateTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = CreateSurfaceSoft,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CreateBorder.copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        Text(
                            text = "Control",
                            color = CreateTextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = modeTitle,
                            color = CreateTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    color = CreateSurfaceSoft,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CreateBorder.copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        Text(
                            text = "Disponible",
                            color = CreateTextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = availableText,
                            color = CreateGreen,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (summary.isNotBlank()) {
                Surface(
                    color = CreateSurfaceSoft,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, CreateBorder.copy(alpha = 0.75f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Resumen",
                            color = CreateTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = summary,
                            color = CreateTextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onEditQuantity,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CreateBorder),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text(
                        text = "Editar",
                        color = CreateTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onChangeType,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CreateTextPrimary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text(
                        text = "Cambiar modo",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun configuredStockModeTitle(state: CreateProductState): String {
    return when (state.stockEntryMode) {
        CreateProductStockEntryMode.UNIDAD -> when (state.controlType) {
            CreateProductControlType.LIQUIDO -> "Frascos directos"
            CreateProductControlType.PESO -> "Envases directos"
            else -> "Unidades directas"
        }
        CreateProductStockEntryMode.CAJA -> when (state.controlType) {
            CreateProductControlType.LIQUIDO -> "Cajas con frascos"
            CreateProductControlType.PESO -> "Cajas con envases"
            else -> "Cajas con unidades"
        }
        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> when (state.controlType) {
            CreateProductControlType.LIQUIDO -> "Frascos directos"
            CreateProductControlType.PESO -> "Envases directos"
            else -> "Blísteres"
        }
        null -> "Sin configurar"
    }
}

private fun configuredStockModeSubtitle(state: CreateProductState): String {
    return when (state.stockEntryMode) {
        CreateProductStockEntryMode.UNIDAD -> when (state.controlType) {
            CreateProductControlType.LIQUIDO -> "Se controlará por frasco y contenido."
            CreateProductControlType.PESO -> "Se controlará por envase y peso."
            else -> "Se controlará como unidades directas."
        }
        CreateProductStockEntryMode.CAJA -> when (state.controlType) {
            CreateProductControlType.LIQUIDO -> "Se calcularán frascos y contenido total."
            CreateProductControlType.PESO -> "Se calcularán envases y peso total."
            else -> "Se calcularán unidades dentro de cada caja."
        }
        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> when (state.controlType) {
            CreateProductControlType.LIQUIDO -> "Se controlará por frasco y contenido."
            CreateProductControlType.PESO -> "Se controlará por envase y peso."
            else -> "Se controlará por blíster y unidades."
        }
        null -> "Todavía no hay un modo de ingreso seleccionado."
    }
}

@Composable
fun StockEntryDialog(
    state: CreateProductState,
    onStateChange: (CreateProductState) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    // 1. DETERMINAR TERMINOLOGÍA SEGÚN EL TIPO DE PRODUCTO (Nivel 2)
    val itemType = when (state.controlType) {
        CreateProductControlType.LIQUIDO -> "FRASCO / AMPOLLA"
        CreateProductControlType.PESO -> "ENVASE / EMPAQUE"
        CreateProductControlType.UNIDAD -> if (state.stockEntryMode == CreateProductStockEntryMode.CAJA_CON_PAQUETES) "BLÍSTER" else "PIEZA"
        else -> "UNIDAD"
    }

    val pluralItemType = when (state.controlType) {
        CreateProductControlType.LIQUIDO -> "frascos"
        CreateProductControlType.PESO -> "envases"
        CreateProductControlType.UNIDAD -> if (state.stockEntryMode == CreateProductStockEntryMode.CAJA_CON_PAQUETES) "blísters" else "piezas"
        else -> "unidades"
    }

    // 2. DETERMINAR UNIDAD MÍNIMA (Nivel 3)
    val baseUnitName = when (state.controlType) {
        CreateProductControlType.LIQUIDO -> "MILILITROS (mL)"
        CreateProductControlType.PESO -> "GRAMOS (g)"
        CreateProductControlType.UNIDAD -> "CONTENIDO (Pastillas / Cápsulas)"
        else -> "UNIDADES"
    }

    val (unidadBase, unidadGrande) = when (state.controlType) {
        CreateProductControlType.LIQUIDO -> "mL" to "L"
        CreateProductControlType.PESO -> "g" to "kg"
        else -> "" to ""
    }
    val mostrarSelectorUnidad = state.controlType == CreateProductControlType.PESO ||
            state.controlType == CreateProductControlType.LIQUIDO

    var unitForItem by remember(state.controlType, state.stockEntryUnit) {
        mutableStateOf(state.stockEntryUnit.ifBlank { unidadBase })
    }
    var itemFieldValue by remember { mutableStateOf(TextFieldValue()) }

    LaunchedEffect(state.unitsPerItemText, unitForItem) {
        val displayStr = state.unitsPerItemText
            .takeIf { it.isNotBlank() && parseCreateProductNumber(it) > 0.0 }
            .orEmpty()
        if (itemFieldValue.text != displayStr) {
            itemFieldValue = TextFieldValue(displayStr, selection = TextRange(displayStr.length))
        }
    }

    fun onItemValueChange(newValue: TextFieldValue) {
        val raw = newValue.text
        if (raw.isBlank()) {
            itemFieldValue = newValue
            onStateChange(state.copy(unitsPerItemText = ""))
            return
        }
        val sanitized = sanitizeDecimalInput(raw)
        val selection = TextRange(sanitized.length)
        itemFieldValue = if (sanitized == raw) newValue else TextFieldValue(sanitized, selection)
        val number = sanitized.toDoubleOrNull() ?: run {
            onStateChange(state.copy(unitsPerItemText = ""))
            return
        }
        onStateChange(
            state.copy(
                unitsPerItemText = formatCreateProductNumber(number),
                stockEntryUnit = unitForItem
            )
        )
    }

    fun changeItemUnit(newUnit: String) {
        if (unitForItem == newUnit) return
        unitForItem = newUnit
        onStateChange(state.copy(stockEntryUnit = newUnit))
        val currentValue = itemFieldValue.text
        itemFieldValue = TextFieldValue(currentValue, selection = TextRange(currentValue.length))
    }

    val summary = buildInitialStockEntrySummary(state)
    val configuration = LocalConfiguration.current
    val dialogMaxHeight = (configuration.screenHeightDp.dp * 0.88f)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 24.dp)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 24.dp,
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PremiumGradient)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = dialogMaxHeight)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Desglose de Inventario",
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                color = CreateTextPrimary,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Define cómo se organiza lo que recibiste.",
                                color = CreateTextSecondary,
                                fontSize = 14.sp
                            )
                        }

                        // BADGE DE MODO SELECCIONADO
                        Surface(
                            color = CreateGreenSoft,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = when (state.stockEntryMode) {
                                    CreateProductStockEntryMode.UNIDAD -> "INGRESO POR ${itemType}S"
                                    CreateProductStockEntryMode.CAJA -> "INGRESO POR CAJAS"
                                    CreateProductStockEntryMode.CAJA_CON_PAQUETES -> "INGRESO POR CAJAS Y BLÍSTERS"
                                    null -> ""
                                },
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                color = CreateGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            when (state.stockEntryMode) {
                                null -> Unit

                                CreateProductStockEntryMode.UNIDAD -> {
                                    AppTextField(
                                        label = "CANTIDAD DE ${itemType}S RECIBIDOS",
                                        value = state.receivedUnitsText,
                                        error = state.errors["receivedUnits"],
                                        placeholder = "Ej. 50",
                                        trailingLabel = pluralItemType,
                                        keyboardType = KeyboardType.Number,
                                        onValueChange = { onStateChange(state.copy(receivedUnitsText = it.filter { c -> c.isDigit() })) }
                                    )

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "¿CUÁNTO TRAE CADA ${itemType}?",
                                            color = CreateTextSecondary,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp,
                                            letterSpacing = 1.sp
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                AppTextField(
                                                    label = "",
                                                    value = itemFieldValue.text,
                                                    error = state.errors["unitsPerItem"],
                                                    placeholder = "Ej. 120",
                                                    keyboardType = KeyboardType.Decimal,
                                                    onValueChange = { onItemValueChange(TextFieldValue(it)) }
                                                )
                                            }
                                            if (mostrarSelectorUnidad) {
                                                PremiumUnitSelector(
                                                    selected = unitForItem,
                                                    options = listOf(unidadBase, unidadGrande),
                                                    onSelected = { changeItemUnit(it) }
                                                )
                                            } else {
                                                Text(
                                                    text = if (state.controlType == CreateProductControlType.UNIDAD) "pastillas" else "",
                                                    color = CreateTextPrimary,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                CreateProductStockEntryMode.CAJA -> {
                                    AppTextField(
                                        label = "CANTIDAD DE CAJAS",
                                        value = state.boxesReceivedText,
                                        error = state.errors["boxesReceived"],
                                        placeholder = "Ej. 10",
                                        trailingLabel = "cajas",
                                        keyboardType = KeyboardType.Number,
                                        onValueChange = { onStateChange(state.copy(boxesReceivedText = it.filter { it.isDigit() })) }
                                    )

                                    AppTextField(
                                        label = "¿CUÁNTOS ${itemType}S TRAE CADA CAJA?",
                                        value = state.unitsPerBoxText,
                                        error = state.errors["unitsPerBox"],
                                        placeholder = "Ej. 12",
                                        trailingLabel = pluralItemType,
                                        keyboardType = KeyboardType.Number,
                                        onValueChange = { onStateChange(state.copy(unitsPerBoxText = it.filter { it.isDigit() })) }
                                    )

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "¿CUÁNTO TRAE CADA ${itemType}? ($baseUnitName)",
                                            color = CreateTextSecondary,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp,
                                            letterSpacing = 1.sp
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                AppTextField(
                                                    label = "",
                                                    value = itemFieldValue.text,
                                                    error = state.errors["unitsPerItem"],
                                                    placeholder = "Ej. 500",
                                                    keyboardType = KeyboardType.Decimal,
                                                    onValueChange = { onItemValueChange(TextFieldValue(it)) }
                                                )
                                            }
                                            if (mostrarSelectorUnidad) {
                                                PremiumUnitSelector(
                                                    selected = unitForItem,
                                                    options = listOf(unidadBase, unidadGrande),
                                                    onSelected = { changeItemUnit(it) }
                                                )
                                            }
                                        }
                                    }
                                }

                                CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
                                    AppTextField(
                                        label = "CANTIDAD DE CAJAS",
                                        value = state.boxesReceivedText,
                                        error = state.errors["boxesReceived"],
                                        placeholder = "Ej. 10",
                                        trailingLabel = "cajas",
                                        keyboardType = KeyboardType.Number,
                                        onValueChange = { onStateChange(state.copy(boxesReceivedText = it.filter { it.isDigit() })) }
                                    )

                                    AppTextField(
                                        label = "¿CUÁNTOS BLÍSTERS TRAE CADA CAJA?",
                                        value = state.packagesPerBoxText,
                                        error = state.errors["packagesPerBox"],
                                        placeholder = "Ej. 5",
                                        trailingLabel = "blísters",
                                        keyboardType = KeyboardType.Number,
                                        onValueChange = { onStateChange(state.copy(packagesPerBoxText = it.filter { it.isDigit() })) }
                                    )

                                    AppTextField(
                                        label = "¿CUÁNTAS PASTILLAS TRAE CADA BLÍSTER?",
                                        value = state.unitsPerPackageText,
                                        error = state.errors["unitsPerPackage"],
                                        placeholder = "Ej. 10",
                                        trailingLabel = "pastillas",
                                        keyboardType = KeyboardType.Number,
                                        onValueChange = { onStateChange(state.copy(unitsPerPackageText = it.filter { it.isDigit() })) }
                                    )
                                }
                            }
                        }

                        // TOTALIZADOR EN TIEMPO REAL
                        if (summary.isNotBlank()) {
                            Surface(
                                color = CreateSurfaceSoft,
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, CreateBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "RESUMEN DEL INGRESO",
                                        color = CreateTextSecondary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.2.sp
                                    )
                                    
                                    val totalBase = calculateTotalBaseStock(state)
                                    val unit = when (state.controlType) {
                                        CreateProductControlType.UNIDAD -> "Unidades"
                                        CreateProductControlType.LIQUIDO -> state.stockEntryUnit.ifBlank { "mL" }
                                        CreateProductControlType.PESO -> state.stockEntryUnit.ifBlank { "g" }
                                        else -> ""
                                    }

                                    Text(
                                        text = "${formatCreateProductNumber(totalBase)} $unit",
                                        color = CreateGreen,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black
                                    )

                                    Text(
                                        text = summary,
                                        color = CreateTextPrimary,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f).height(60.dp),
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.5.dp, CreateBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CreateTextSecondary)
                            ) {
                                Text("CANCELAR", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Button(
                                onClick = onApply,
                                modifier = Modifier.weight(1f).height(60.dp),
                                enabled = summary.isNotBlank(),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CreateGreen, contentColor = Color.White),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Text("CONFIRMAR", fontWeight = FontWeight.Black, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumUnitSelector(
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.4f))
            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = selected == option
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelected(option) },
                color = if (isSelected) CreateGreen else Color.Transparent,
            ) {
                Text(
                    text = option,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = if (isSelected) Color.White else CreateTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Data class auxiliar
private data class Tuple4<T1, T2, T3, T4>(val a: T1, val b: T2, val c: T3, val d: T4)

@Composable
fun StockAllocationPanel(state: CreateProductState) {
    val totalStock = calculateTotalBaseStock(state)
    val assignedStock = state.presentations.sumOf { it.equivalenceText.toDoubleOrNull() ?: 0.0 }
    val remainingStock = (totalStock - assignedStock).coerceAtLeast(0.0)
    
    // V21.9: Soporte para unidades escaladas en el panel de distribución (kg/L)
    val factor = if (state.stockEntryUnit == "kg" || state.stockEntryUnit == "L") 1000.0 else 1.0
    val visualRemaining = remainingStock / factor
    val visualTotal = totalStock / factor
    val unit = if (state.stockEntryUnit.isNotBlank()) state.stockEntryUnit else resolveStockUnitLabel(state)
    
    val usageRatio = if (totalStock > 0) (assignedStock / totalStock).toFloat().coerceIn(0f, 1f) else 0f
    val isComplete = remainingStock <= 0.0 && totalStock > 0.0

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.5.dp, if (isComplete) CreateGreen else CreateBorder),
        shadowElevation = if (isComplete) 4.dp else 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isComplete) "STOCK 100% DISTRIBUIDO" else "DISTRIBUCIÓN DE MERCANCÍA",
                        color = if (isComplete) CreateGreen else CreateTextSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formatCreateProductNumber(if (isComplete) visualTotal else visualRemaining),
                            color = if (isComplete) CreateGreen else CreateTextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = unit,
                            color = CreateTextSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
                
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(54.dp),
                        color = CreateBorder.copy(alpha = 0.5f),
                        strokeWidth = 5.dp,
                        trackColor = Color.Transparent
                    )
                    CircularProgressIndicator(
                        progress = { if (isComplete) 1f else usageRatio },
                        modifier = Modifier.size(54.dp),
                        color = if (isComplete) CreateGreen else CreateOrange,
                        strokeWidth = 5.dp,
                        strokeCap = StrokeCap.Round,
                        trackColor = Color.Transparent
                    )
                    Icon(
                        imageVector = if (isComplete) Icons.Outlined.CheckCircle else Icons.Outlined.Inventory,
                        contentDescription = null,
                        tint = if (isComplete) CreateGreen else CreateTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(CreateBorder.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (isComplete) 1f else usageRatio)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = if (isComplete) listOf(CreateGreen.copy(alpha = 0.7f), CreateGreen)
                                else listOf(CreateOrange.copy(alpha = 0.7f), CreateOrange)
                            )
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoMiniTag("Total: ${formatCreateProductNumber(visualTotal)} $unit", CreateInfoGraySoft)
                Spacer(modifier = Modifier.weight(1f))
                if (isComplete) {
                    InfoMiniTag("¡Listo para guardar!", CreateGreenSoft, CreateGreen)
                } else {
                    InfoMiniTag("Por asignar: ${formatCreateProductNumber(visualRemaining)} $unit", CreateOrangeSoft, CreateOrange)
                }
            }
        }
    }
}

@Composable
fun InfoMiniTag(text: String, background: Color, textColor: Color = CreateTextSecondary) {
    Surface(
        color = background,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun PresentationsPricesStep(
    state: CreateProductState,
    onStateChange: (CreateProductState) -> Unit
) {
    val presentationsRequester = remember { BringIntoViewRequester() }
    val mainPresentationRequester = remember { BringIntoViewRequester() }

    val firstErrorKey = remember(state.errors) {
        when {
            !state.errors["presentations"].isNullOrBlank() -> "presentations"
            state.errors.keys.any { it.startsWith("price_") || it.startsWith("equivalence_") } -> "presentations"
            !state.errors["mainPresentation"].isNullOrBlank() -> "mainPresentation"
            else -> null
        }
    }

    LaunchedEffect(firstErrorKey) {
        when (firstErrorKey) {
            "presentations" -> presentationsRequester.bringIntoView()
            "mainPresentation" -> mainPresentationRequester.bringIntoView()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // V19.6: Nuevo Panel de Control de Mercancía (Elegante y Tecnológico)
        StockAllocationPanel(state = state)

        // V21.0: Chips de presentaciones sugeridas (Recientes)
        val recentOptions = remember(state.savedPresentations, state.controlType) {
            state.savedPresentations.filter { 
                it.controlType == (state.controlType?.name ?: "UNIDAD") 
            }.take(10)
        }

        if (recentOptions.isNotEmpty() && !state.addPresentationExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "PRESENTACIONES RECIENTES",
                    color = CreateTextSecondary,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recentOptions.forEach { opt ->
                        Surface(
                            modifier = Modifier.clickable {
                                onStateChange(state.copy(
                                    addPresentationExpanded = true,
                                    draftPresentationName = opt.name,
                                    draftPresentationCustomAmount = opt.equivalenceBase,
                                    draftPresentationCustomUnit = "",
                                    draftPresentationSalePriceText = ""
                                ))
                            },
                            color = Color.White,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, CreateBorder),
                            shadowElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, null, tint = CreateGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = opt.name,
                                    color = CreateTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = "Formas de venta y precios",
            color = CreateTextPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            modifier = Modifier.bringIntoViewRequester(presentationsRequester)
        )

        state.presentations.forEach { presentation ->
            PresentationPriceCard(
                presentation = presentation,
                unitLabel = state.controlType?.baseUnitLabel.orEmpty(),
                isMain = state.mainPresentationId == presentation.id,
                error = state.errors["price_${presentation.id}"]
                    ?: state.errors["equivalence_${presentation.id}"],
                onEquivalenceChange = { value ->
                    onStateChange(
                        state.copy(
                            presentations = state.presentations.map {
                                if (it.id == presentation.id) {
                                    // Si el usuario edita la equivalencia, la
                                    // presentaciÃ³n deja de "pertenecer" a la IA.
                                    it.copy(equivalenceText = value, isAiSuggested = false)
                                } else {
                                    it
                                }
                            },
                            errors = state.errors - "equivalence_${presentation.id}"
                        )
                    )
                },
                onPriceChange = { value ->
                    onStateChange(
                        state.copy(
                            presentations = state.presentations.map {
                                if (it.id == presentation.id) {
                                    it.copy(salePriceText = value)
                                } else {
                                    it
                                }
                            },
                            errors = state.errors - "price_${presentation.id}"
                        )
                    )
                },
                onRemove = {
                    val nextList = state.presentations.filterNot { it.id == presentation.id }

                    val nextMain = when {
                        nextList.isEmpty() -> ""
                        state.mainPresentationId == presentation.id -> nextList.first().id
                        else -> state.mainPresentationId
                    }

                    onStateChange(
                        state.copy(
                            presentations = nextList,
                            mainPresentationId = nextMain,
                            errors = state.errors - "presentations" - "mainPresentation"
                        )
                    )
                }
            )

            // V29.0: GUARDÍAN DE MARGEN (PROFIT GUARD) - Feedback en tiempo real
            val costoTotal = state.purchaseCost.replace(com.app.administradorfarmadon.ClasesDatabase.SessionManager.monedaSimbolo, "", ignoreCase = true).trim().replace(",", ".").toDoubleOrNull() ?: 0.0
            val stockTotalBase = calculateTotalBaseStock(state)
            val precioVenta = presentation.salePriceText.replace(com.app.administradorfarmadon.ClasesDatabase.SessionManager.monedaSimbolo, "", ignoreCase = true).trim().replace(",", ".").toDoubleOrNull() ?: 0.0
            val equiv = presentation.equivalenceText.toDoubleOrNull() ?: 1.0

            if (costoTotal > 0 && stockTotalBase > 0 && precioVenta > 0) {
                val costoUnitarioBase = costoTotal / stockTotalBase
                val costoEstaPresentacion = costoUnitarioBase * equiv
                val gananciaAbsoluta = precioVenta - costoEstaPresentacion
                val margenPorcentaje = (gananciaAbsoluta / precioVenta) * 100

                val (colorMargen, mensajeMargen) = when {
                    margenPorcentaje < 10 -> CreateRed to "¡Pérdida o margen muy bajo!"
                    margenPorcentaje < 20 -> CreateOrange to "Margen ajustado"
                    else -> CreateGreen to "Margen saludable"
                }

                Surface(
                    modifier = Modifier.padding(horizontal = 8.dp).offset(y = (-12).dp),
                    color = colorMargen.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                    border = BorderStroke(1.dp, colorMargen.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(colorMargen, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ganancia: ${String.format(Locale.US, "%.1f", margenPorcentaje)}%",
                                color = colorMargen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = mensajeMargen,
                            color = colorMargen.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (state.errors["presentations"] != null) {
            Text(
                text = state.errors["presentations"].orEmpty(),
                color = Color(0xFFB42318),
                fontSize = 13.sp
            )
        }

        if (state.addPresentationExpanded) {
            // Se maneja vÃ­a Dialog fuera del flujo de la lista para mayor elegancia.
        }

        if (state.presentations.size > 1) {
            MainPresentationRadioGroup(
                modifier = Modifier.bringIntoViewRequester(mainPresentationRequester),
                presentations = state.presentations,
                selectedId = state.mainPresentationId,
                error = state.errors["mainPresentation"],
                onSelected = {
                    onStateChange(
                        state.copy(
                            mainPresentationId = it,
                            errors = state.errors - "mainPresentation"
                        )
                    )
                }
            )
        }

    }
}

@Composable
private fun AddPresentationModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (selected) CreateGreenSoft else Color.White,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, if (selected) CreateGreen else CreateBorder)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = if (selected) CreateGreen else CreateTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CustomPresentationFields(
    controlType: CreateProductControlType?,
    amount: String,
    unit: String,
    customName: String,
    onAmountChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onCustomNameChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (controlType) {
            CreateProductControlType.UNIDAD -> {
                AppTextField(
                    label = "Nombre de la presentacion",
                    value = customName,
                    placeholder = "Ej. Pack de 12, Caja",
                    onValueChange = onCustomNameChange
                )
                AppTextField(
                    label = "Cantidad de unidades",
                    value = amount,
                    placeholder = "Ej. 12",
                    keyboardType = KeyboardType.Number,
                    onValueChange = { v ->
                        onAmountChange(v.filter { it.isDigit() })
                    }
                )
            }
            CreateProductControlType.PESO -> {
                Text(
                    text = "Cantidad y unidad",
                    color = CreateTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        AppTextField(
                            label = "Cantidad",
                            value = amount,
                            placeholder = "Ej. 750",
                            keyboardType = KeyboardType.Decimal,
                            onValueChange = { v ->
                                onAmountChange(v.filter { it.isDigit() || it == '.' })
                            }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UnitChoiceChip("g", unit == "g") { onUnitChange("g") }
                    UnitChoiceChip("kg", unit == "kg") { onUnitChange("kg") }
                }
            }
            CreateProductControlType.LIQUIDO -> {
                Text(
                    text = "Cantidad y unidad",
                    color = CreateTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        AppTextField(
                            label = "Cantidad",
                            value = amount,
                            placeholder = "Ej. 250",
                            keyboardType = KeyboardType.Decimal,
                            onValueChange = { v ->
                                onAmountChange(v.filter { it.isDigit() || it == '.' })
                            }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UnitChoiceChip("mL", unit == "mL") { onUnitChange("mL") }
                    UnitChoiceChip("L", unit == "L") { onUnitChange("L") }
                }
            }
            null -> {
                Text(
                    text = "Selecciona primero el tipo de control en el paso Producto.",
                    color = CreateTextSecondary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun UnitChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (selected) CreateGreen else Color.White,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, if (selected) CreateGreen else CreateBorder)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            color = if (selected) Color.White else CreateTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ProductSummaryStep(
    state: CreateProductState,
    onStateChange: (CreateProductState) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // V20.0: Bloque de Información Útil (IA)
        if (state.isFetchingUsageInfo) {
            SummaryCard(
                title = "Información Útil",
                icon = Icons.Outlined.Lightbulb,
                color = CreateAiFocus
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = CreateAiFocus,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Consultando con farmacéutico IA...",
                        color = CreateTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else if (state.aiUsageInfo != null) {
            SummaryCard(
                title = "Información Útil",
                icon = Icons.Outlined.Lightbulb,
                color = CreateAiFocus
            ) {
                // Usos
                Text(
                    text = "PARA QUÉ SIRVE",
                    color = CreateAiFocus,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.aiUsageInfo.usos.forEach { uso ->
                        Surface(
                            color = CreateAiFocusSoft.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CreateAiFocus.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = uso,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = CreateTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CreateBorder.copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.height(8.dp))

                // Instrucciones
                SummaryItem("Instrucciones de Uso", state.aiUsageInfo.instrucciones)
                
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CreateBorder.copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.height(8.dp))

                // Contraindicaciones
                Column {
                    Text(
                        text = "CUÁNDO NO TOMAR",
                        color = CreateRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.aiUsageInfo.contraindicaciones,
                        color = CreateTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Bloques de igual tamaño visual (estilo dashboard)
        // --- CARD PASO 1: IDENTIDAD ---
        SummaryCard(
            title = "Identidad del Producto",
            icon = Icons.AutoMirrored.Outlined.Label,
            color = CreateBlue
        ) {
            SummaryItem("Nombre Comercial", state.name.ifBlank { "(Sin nombre)" })
            SummaryItem("Categoría", state.category.ifBlank { "(Sin categoria)" })
            SummaryItem("Tipo de Control", state.controlType?.label ?: "(No definido)")
            SummaryItem("Venta", if (state.requiresPrescription) "Bajo Receta Médica" else "Venta Libre")
        }

        // --- CARD PASO 2: INVENTARIO ---
        SummaryCard(
            title = "Configuración de Lote",
            icon = Icons.Outlined.Inventory2,
            color = CreateOrange
        ) {
            val expirationStatus = evaluateExpirationStatus(state.expirationDate)
            SummaryItem("Número de Lote", state.lotNumber.ifBlank { "(Sin lote)" })
            SummaryItem(
                "Vencimiento",
                state.expirationDate.ifBlank { "(Sin fecha)" },
                helper = expirationStatus?.message,
                helperColor = expirationStatus?.color ?: CreateTextSecondary
            )
            SummaryItem("Stock de Ingreso", buildInitialStockEntrySummary(state).ifBlank { "(No configurado)" })
            SummaryItem("Costo Total", formatBs(state.purchaseCost), highlight = true)

            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CreateBorder.copy(alpha = 0.5f)))
            Spacer(modifier = Modifier.height(12.dp))

            val minimumUnits = state.minimumStockUnits
            val minimumLabel = resolveMinimumStockLabel(state, minimumUnits)
            val baseMinEquivalence = when (resolveMinimumStockControlMode(state)) {
                StockControlMode.INDIVISIBLE -> {
                    when (state.controlType) {
                        CreateProductControlType.UNIDAD -> {
                            if (state.stockEntryMode == CreateProductStockEntryMode.CAJA_CON_PAQUETES)
                                parseCreateProductNumber(state.unitsPerPackageText)
                            else 1.0
                        }
                        else -> parseCreateProductNumber(state.unitsPerItemText)
                    }
                }
                else -> 1.0
            }
            val totalBaseUnits = (minimumUnits * baseMinEquivalence).toInt()
            val baseUnitLabel = resolveStockUnitLabel(state)

            SummaryItem(
                "Alerta de Stock Bajo",
                "Notificar al llegar a $minimumUnits $minimumLabel",
                helper = if (baseMinEquivalence > 1.0) "Equivale a $totalBaseUnits $baseUnitLabel" else null,
                highlight = true
            )
        }

        // --- CARD PASO 3: COMERCIALIZACIÓN ---
        SummaryCard(
            title = "Venta y Alertas",
            icon = Icons.Outlined.AttachMoney,
            color = CreateGreen
        ) {
            Text(
                text = "PRESENTACIONES CONFIGURADAS",
                color = CreateTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )
            
            if (state.presentations.isEmpty()) {
                Text(text = "(Sin presentaciones)", color = CreateTextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            } else {
                state.presentations.forEach { pres ->
                    val isMain = pres.id == state.mainPresentationId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isMain) Icon(Icons.Outlined.CheckCircle, null, tint = CreateGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(if (isMain) 8.dp else 0.dp))
                            Text(
                                text = "${pres.name} (x${pres.equivalenceText})",
                                color = if (isMain) CreateTextPrimary else CreateTextSecondary,
                                fontSize = 14.sp,
                                fontWeight = if (isMain) FontWeight.Black else FontWeight.Medium
                            )
                        }
                        Text(
                            text = formatBs(pres.salePriceText),
                            color = CreateGreen,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SummaryCard(
    title: String,
    icon: ImageVector,
    color: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, CreateBorder.copy(alpha = 0.8f)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ENCABEZADO SEPARADO POR RAYA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = color.copy(alpha = 0.12f),
                    shape = CircleShape,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = title.uppercase(),
                    color = CreateTextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }
            
            // RAYA SEPARADORA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(CreateBorder.copy(alpha = 0.6f))
            )

            // CONTENIDO
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    helper: String? = null,
    helperColor: Color = CreateTextSecondary,
    highlight: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            color = CreateTextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = if (highlight) CreateGreen else CreateTextPrimary,
            fontSize = 16.sp,
            fontWeight = if (highlight) FontWeight.Black else FontWeight.SemiBold
        )
        if (helper != null) {
            Text(
                text = "• $helper",
                color = helperColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

    }
}

@Composable
fun ProductCreatedSuccessScreen(
    summary: ProductCreatedSummary,
    onViewProduct: () -> Unit,
    onCreateAnother: () -> Unit
) {
    // Pantalla post-guardado simplificada: la sección de "búsqueda inteligente"
    // (CleanSmartReferenceCard) y los botones asociados se eliminaron del
    // flujo a pedido del usuario para mantener la experiencia limpia.
    // Solo mostramos confirmación + acceso rápido al producto creado.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreateBackground)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(CircleShape)
                    .background(CreateGreenSoft)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check_circle),
                    contentDescription = null,
                    tint = CreateGreen,
                    modifier = Modifier.size(42.dp)
                )
            }

            Text(
                text = "¡Producto creado!",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = CreateTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )

            Text(
                text = "Ya está disponible en tu inventario.",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = CreateTextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, CreateBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = summary.name,
                        color = CreateTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    Text(
                        text = summary.category,
                        color = CreateTextSecondary,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCreateAnother,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text("Crear otro")
            }
            Button(
                onClick = onViewProduct,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text("Ver producto")
            }
        }
    }
}

@Composable
fun ProductTypeSelector(
    selected: CreateProductControlType?,
    error: String?,
    suggestedType: CreateProductControlType? = null,
    showSuggestionBadge: Boolean = false,
    onSelected: (CreateProductControlType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Cuadrícula de 3 tarjetas modernas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CreateProductControlType.entries.forEach { controlType ->
                val isSelected = selected == controlType
                val isAiSuggested = showSuggestionBadge && suggestedType == controlType

                // --- 1. ANIMACIÓN MEJORADA (Escala y Color) ---
                val pulseScale = remember { Animatable(1f) }
                val surfaceColor = remember { androidx.compose.animation.Animatable(Color.White) }

                LaunchedEffect(isSelected, isAiSuggested) {
                    if (isSelected && isAiSuggested) {
                        // Pausa inteligente: Esperamos que el teclado baje y la vista se acomode
                        delay(300)

                        // Fase 1: Se ilumina con foco IA amarillo y crece
                        launch {
                            surfaceColor.animateTo(
                                targetValue = CreateAiFocusSoft,
                                animationSpec = androidx.compose.animation.core.tween(durationMillis = 200)
                            )
                        }
                        pulseScale.animateTo(1.08f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessLow))

                        // Fase 2: Vuelve a blanco y a su tamaño normal suavemente
                        delay(150)
                        launch {
                            surfaceColor.animateTo(
                                targetValue = Color.White,
                                animationSpec = androidx.compose.animation.core.tween(durationMillis = 400)
                            )
                        }
                        pulseScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow))
                    } else {
                        // Si se selecciona manualmente, no hay animación de IA
                        surfaceColor.snapTo(Color.White)
                        pulseScale.snapTo(1f)
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .graphicsLayer {
                            scaleX = pulseScale.value
                            scaleY = pulseScale.value
                        }
                        .clickable { onSelected(controlType) },
                    color = surfaceColor.value, // --- 2. APLICAMOS EL COLOR ANIMADO ---
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) CreateGreen else CreateBorder
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = when(controlType) {
                                    CreateProductControlType.UNIDAD -> Icons.Outlined.Inventory2
                                    CreateProductControlType.PESO -> Icons.Outlined.MonitorWeight
                                    CreateProductControlType.LIQUIDO -> Icons.Outlined.Opacity
                                },
                                contentDescription = null,
                                tint = if (isSelected) CreateGreen else CreateTextSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = controlType.label,
                                color = if (isSelected) CreateGreen else CreateTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = CreateGreen,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(18.dp)
                                    .background(Color.White, CircleShape)
                            )
                        }

                        if (isAiSuggested && !isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(CreateAiFocus, CircleShape)
                                    .size(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Panel de información inteligente (aparece al seleccionar)
        AnimatedVisibility(
            visible = selected != null,
            enter = fadeIn() + expandVertically(),
            exit = shrinkVertically() + fadeOut()
        ) {
            selected?.let { type ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CreateInfoGraySoft,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CreateBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = CreateAiFocusSoft,
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Lightbulb,
                                    contentDescription = null,
                                    tint = CreateAiFocus,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = type.helper,
                                color = CreateTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Ejemplos: ${type.example}",
                                color = CreateTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        if (!error.isNullOrBlank()) {
            Text(
                text = error,
                color = CreateRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

    }
}





@Composable
fun PremiumStockEntryModeSelector(
    controlType: CreateProductControlType?,
    selected: CreateProductStockEntryMode?,
    onSelected: (CreateProductStockEntryMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val availableModes = remember(controlType) {
        CreateProductStockEntryMode.entries
            .filter { isStockEntryModeValidForControlType(it, controlType) }
    }
    val selectedMeta = selected
        ?.takeIf { isStockEntryModeValidForControlType(it, controlType) }
        ?.stockModeMeta(controlType)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, CreateBorder.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Forma de ingreso",
                color = CreateTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )

            Text(
                text = "Elige cómo se registrará este stock.",
                color = CreateTextSecondary,
                fontSize = 13.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                availableModes.forEach { mode ->
                    PremiumStockModeCard(
                        controlType = controlType,
                        mode = mode,
                        selected = selected == mode,
                        onClick = { onSelected(mode) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (selectedMeta != null) {
                PremiumStockModeInfoBox(
                    title = selectedMeta.infoTitle,
                    description = selectedMeta.infoDescription
                )
            }
        }
    }
}

@Composable
private fun PremiumStockModeCard(
    controlType: CreateProductControlType?,
    mode: CreateProductStockEntryMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val meta = mode.stockModeMeta(controlType)
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .height(126.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) CreateGreenSoft.copy(alpha = 0.45f) else Color.White,
        shadowElevation = if (selected) 3.dp else 0.dp,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) CreateGreen else CreateBorder.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) CreateGreen.copy(alpha = 0.12f)
                        else CreateSurfaceSoft
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = meta.icon,
                    contentDescription = meta.title,
                    tint = if (selected) CreateGreen else CreateTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = meta.title,
                color = CreateTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Text(
                text = meta.shortDescription,
                color = CreateTextSecondary,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            if (selected) {
                Surface(
                    color = CreateGreen,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "Seleccionado",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(22.dp))
            }
        }
    }
}

@Composable
private fun PremiumStockModeInfoBox(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CreateSurfaceSoft,
        border = BorderStroke(1.dp, CreateBorder.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CreateGreenSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = CreateGreen,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = CreateTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = description,
                    color = CreateTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

private data class StockModeMeta(
    val title: String,
    val shortDescription: String,
    val infoTitle: String,
    val infoDescription: String,
    val icon: ImageVector
)

private fun CreateProductStockEntryMode.stockModeMeta(controlType: CreateProductControlType?): StockModeMeta {
    return when (this) {
        CreateProductStockEntryMode.UNIDAD -> StockModeMeta(
            title = when (controlType) {
                CreateProductControlType.LIQUIDO -> "Frascos"
                CreateProductControlType.PESO -> "Envases"
                else -> "Unidades"
            },
            shortDescription = when (controlType) {
                CreateProductControlType.LIQUIDO -> "Ingreso directo"
                CreateProductControlType.PESO -> "Ingreso directo"
                else -> "Piezas directas"
            },
            infoTitle = when (controlType) {
                CreateProductControlType.LIQUIDO -> "Control por frasco"
                CreateProductControlType.PESO -> "Control por envase"
                else -> "Se descuenta una a una"
            },
            infoDescription = when (controlType) {
                CreateProductControlType.LIQUIDO -> "Registra frascos sueltos y el contenido de cada uno."
                CreateProductControlType.PESO -> "Registra envases sueltos y el peso de cada uno."
                else -> "Úsalo cuando vendes unidades sueltas o blísteres individuales."
            },
            icon = Icons.Outlined.Inventory2
        )

        CreateProductStockEntryMode.CAJA -> StockModeMeta(
            title = "Cajas",
            shortDescription = when (controlType) {
                CreateProductControlType.LIQUIDO -> "Con frascos"
                CreateProductControlType.PESO -> "Con envases"
                else -> "Con unidades"
            },
            infoTitle = when (controlType) {
                CreateProductControlType.LIQUIDO -> "Caja con frascos"
                CreateProductControlType.PESO -> "Caja con envases"
                else -> "Caja con unidades"
            },
            infoDescription = when (controlType) {
                CreateProductControlType.LIQUIDO -> "Registra cuántas cajas recibiste, cuántos frascos trae cada caja y el contenido por frasco."
                CreateProductControlType.PESO -> "Registra cuántas cajas recibiste, cuántos envases trae cada caja y el peso por envase."
                else -> "Registra cajas y cuántas unidades trae cada una."
            },
            icon = Icons.Outlined.Inventory
        )

        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> StockModeMeta(
            title = "Blísters",
            shortDescription = "Paquetes internos",
            infoTitle = "Control por blíster o paquete",
            infoDescription = "Úsalo cuando la unidad real de salida es el blíster o paquete interno.",
            icon = Icons.Outlined.AllInbox
        )
    }
}

@Composable
fun CreateSectionCard(
    title: String? = null,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClear: (() -> Unit)? = null,
    isSuccess: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val hasHeader = !title.isNullOrBlank() || !subtitle.isNullOrBlank() || icon != null || onClear != null

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            width = if (isSuccess) 2.dp else 1.dp,
            color = if (isSuccess) CreateGreen else CreateBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            if (hasHeader) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (icon != null) {
                            Surface(
                                color = CreateGreenSoft,
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = CreateGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (!title.isNullOrBlank()) {
                                Text(
                                    text = title,
                                    color = CreateTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            }

                            if (!subtitle.isNullOrBlank()) {
                                Text(
                                    text = subtitle,
                                    color = if (title.isNullOrBlank()) CreateTextPrimary else CreateTextSecondary,
                                    fontWeight = if (title.isNullOrBlank()) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = if (title.isNullOrBlank()) 17.sp else 13.sp
                                )
                            }
                        }
                    }

                    if (onClear != null) {
                        Surface(
                            onClick = onClear,
                            color = CreateRed.copy(alpha = 0.08f),
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar",
                                    tint = CreateRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(CreateBorder.copy(alpha = 0.6f))
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            content()
        }
    }
}
@Composable
fun PresentationPriceCard(
    presentation: CreateProductPresentation,
    unitLabel: String,
    isMain: Boolean,
    error: String?,
    onEquivalenceChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    val bloquearEquivalencia = isMain || presentation.name.equals("Unidad", ignoreCase = true)
    
    val isComplete = presentation.equivalenceText.toDoubleOrNull()?.let { it > 0 } == true &&
            presentation.salePriceText.toDoubleOrNull()?.let { it > 0 } == true &&
            error == null

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            width = if (isComplete) 2.dp else 1.5.dp,
            color = if (isComplete) CreateGreen else CreateBorder
        ),
        shadowElevation = if (isComplete) 3.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // ENCABEZADO CON ICONO Y TÍTULO
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono de la presentación
                Surface(
                    color = CreateGreenSoft,
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when {
                                presentation.name.contains("blister", true) -> Icons.Outlined.AllInbox
                                presentation.name.contains("caja", true) -> Icons.Outlined.Inventory2
                                presentation.name.contains("frasco", true) -> Icons.Outlined.Opacity
                                else -> Icons.AutoMirrored.Outlined.Label
                            },
                            contentDescription = null,
                            tint = CreateGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = presentation.name,
                            color = CreateTextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (presentation.isAiSuggested) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("✨", fontSize = 14.sp)
                        }
                    }
                    if (isMain) {
                        Text(
                            text = "PRINCIPAL",
                            color = CreateGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Botón de eliminar tipo card
                Surface(
                    onClick = onRemove,
                    color = CreateRed.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(40.dp),
                    border = BorderStroke(1.dp, CreateRed.copy(alpha = 0.1f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Eliminar",
                            tint = CreateRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            
            // RAYA SEPARADORA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(CreateBorder.copy(alpha = 0.6f))
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // INPUTS DE CANTIDAD Y PRECIO
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    if (bloquearEquivalencia) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "EQUIVALENCIA",
                                color = CreateTextSecondary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp
                            )
                            Surface(
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(28.dp),
                                color = CreateBackground,
                                border = BorderStroke(1.dp, CreateBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Outlined.Inventory, null, tint = CreateTextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "${presentation.equivalenceText} $unitLabel",
                                        color = CreateTextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    } else {
                        AppTextField(
                            label = "EQUIVALENCIA",
                            value = presentation.equivalenceText,
                            placeholder = "Ej. 10",
                            keyboardType = KeyboardType.Decimal,
                            leadingIcon = Icons.Outlined.Inventory,
                            trailingLabel = unitLabel,
                            onValueChange = onEquivalenceChange
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    AppTextField(
                        label = "PRECIO VENTA",
                        value = presentation.salePriceText,
                        placeholder = "Ej. 50.00",
                        keyboardType = KeyboardType.Decimal,
                        leadingIcon = Icons.Outlined.AttachMoney,
                        trailingLabel = "Bs",
                        onValueChange = onPriceChange
                    )
                }
            }

            if (!error.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = CreateRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun MainPresentationRadioGroup(
    modifier: Modifier = Modifier,
    presentations: List<CreateProductPresentation>,
    selectedId: String,
    error: String?,
    onSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Presentación principal para vender",
            color = CreateTextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = modifier
        )
        Text(
            text = "Se usara por defecto al vender este producto.",
            color = CreateTextSecondary,
            fontSize = 14.sp
        )
        presentations.forEach { presentation ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelected(presentation.id) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedId == presentation.id,
                    onClick = { onSelected(presentation.id) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = presentation.name,
                    color = CreateTextPrimary,
                    fontSize = 15.sp
                )
            }
        }
        if (!error.isNullOrBlank()) {
            Text(
                text = error,
                color = Color(0xFFB42318),
                fontSize = 13.sp
            )
        }

    }
}

@Composable
fun BottomStepActions(
    step: CreateProductStep,
    nextLabel: String,
    nextEnabled: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    loading: Boolean = false
) {
    Surface(shadowElevation = 8.dp, color = Color.White) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isLast = step == CreateProductStep.RESUMEN

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isLast) 62.dp else 54.dp)
                    .then(
                        if (isLast) {
                            Modifier.graphicsLayer {
                                translationY = if (loading) 0f else -2f
                                shadowElevation = 12f
                            }
                        } else Modifier
                    ),
                enabled = nextEnabled && !loading,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CreateGreen,
                    contentColor = Color.White,
                    disabledContainerColor = CreateBorder.copy(alpha = 0.9f),
                    disabledContentColor = CreateTextSecondary.copy(alpha = 0.55f)
                ),
                border = if (isLast) BorderStroke(1.dp, CreateGreen.copy(alpha = 0.5f)) else null,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (isLast) 8.dp else 2.dp,
                    pressedElevation = if (isLast) 2.dp else 4.dp
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Procesando...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    } else {
                        Text(
                            text = if (isLast) "GUARDAR PRODUCTO" else nextLabel, 
                            fontWeight = FontWeight.Black, 
                            fontSize = if (isLast) 17.sp else 16.sp,
                            letterSpacing = if (isLast) 0.5.sp else 0.sp
                        )
                        if (!isLast) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MovementDetailRow(label: String, value: String) {
    SummaryRow(label = label, value = value)
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = CreateTextSecondary,
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            color = CreateTextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
    }
}



@Composable
private fun AiPrescriptionInfoRow(requiresPrescription: Boolean) {
    val title = if (requiresPrescription) {
        "Este producto requiere receta"
    } else {
        "Este producto no requiere receta"
    }
    val subtitle = "Te brindamos esta información. Si editas el switch, respetaremos tu decisión."
    val accent = if (requiresPrescription) CreateOrange else CreateGreen
    val background = CreateInfoGraySoft

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = background,
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = CreateTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = CreateTextSecondary,
                fontSize = 13.sp,
                lineHeight = 16.sp
            )
        }

    }
}

@Composable
private fun CompactSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = CreateGreenSoft,
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Assignment,
                    contentDescription = null,
                    tint = CreateGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = CreateTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = CreateTextSecondary,
                fontSize = 13.sp,
                lineHeight = 16.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CreateGreen,
                uncheckedThumbColor = CreateBorder,
                uncheckedTrackColor = CreateBackground
            )
        )
    }
}


private fun String.stripAccents(): String {
    return java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
}

@Composable
private fun AppTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    error: String? = null,
    helper: String? = null,
    helperActionLabel: String? = null,
    onHelperAction: (() -> Unit)? = null,
    helperHighlighted: Boolean = false,
    placeholder: String = "",
    trailingLabel: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isSuccess: Boolean = false,
    focusRequester: FocusRequester? = null,
    onImeAction: (() -> Unit)? = null,
    labelColor: Color? = null,
    onValueChange: (String) -> Unit
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val bringIntoViewScope = rememberCoroutineScope()
    var hasFocus by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                color = labelColor ?: if (error != null) CreateRed else CreateTextSecondary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Surface(
            color = Color.White,
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(
                width = if (hasFocus || isSuccess) 2.dp else 1.dp,
                color = when {
                    error != null -> CreateRed
                    isSuccess -> CreateGreen
                    hasFocus -> CreateGreen
                    else -> CreateBorder
                }
            ),
            shadowElevation = if (hasFocus) 3.dp else 0.dp
        ) {
            val interactionSource = remember { MutableInteractionSource() }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .onFocusChanged { hasFocus = it.isFocused },
                singleLine = true,
                interactionSource = interactionSource,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onImeAction?.invoke()
                        bringIntoViewScope.launch {
                            delay(60)
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = CreateTextPrimary
                ),
                decorationBox = { innerTextField ->
                    TextFieldDefaults.DecorationBox(
                        value = value,
                        innerTextField = innerTextField,
                        enabled = true,
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = interactionSource,
                        isError = !error.isNullOrBlank(),
                        placeholder = {
                            if (placeholder.isNotBlank()) {
                                Text(
                                    text = placeholder,
                                    color = CreateTextSecondary.copy(alpha = 0.5f),
                                    fontSize = 15.sp
                                )
                            }
                        },
                        leadingIcon = leadingIcon?.let {
                            {
                                Icon(
                                    imageVector = it,
                                    contentDescription = null,
                                    tint = if (hasFocus) CreateGreen else CreateTextSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        trailingIcon = if (trailingIcon != null || trailingLabel != null) {
                            {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 12.dp)
                                ) {
                                    trailingIcon?.invoke()
                                    if (trailingLabel != null) {
                                        Text(
                                            text = trailingLabel,
                                            color = CreateTextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        } else null,
                        shape = RoundedCornerShape(28.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent,
                            cursorColor = CreateGreen
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        container = {}
                    )
                }
            )
        }

        if (!error.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⚠️", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = error,
                    color = CreateRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else if (!helper.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            if (!helperActionLabel.isNullOrBlank() && onHelperAction != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = helper,
                        color = if (helperHighlighted) CreateGreen else CreateTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (helperHighlighted) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = onHelperAction,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(helperActionLabel, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                }
            } else {
                Text(
                    text = helper,
                    color = if (helperHighlighted) CreateGreen else CreateTextSecondary,
                    fontWeight = if (helperHighlighted) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}


/**
 * BotÃ³n "ðŸ“· Escanear etiqueta" + estados (idle/loading/result/error).
 *
 * Cuando hay un resultado nuevo, muestra una tarjeta con lo detectado y
 * dos acciones: [Aplicar] (rellena los campos) y [Descartar].
 */
@Composable
private fun LabelScannerRow(
    state: com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.UiState,
    onRequestScan: () -> Unit,
    onApplyResult: (com.app.administradorfarmadon.ActivityInventario.reference.EtiquetaDetectada) -> Unit,
    onDismiss: () -> Unit,
    showScanButton: Boolean = true
) {
    val statusLoading = com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.Status.LOADING
    val statusReady = com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.Status.READY
    val statusError = com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.Status.ERROR

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (showScanButton) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = state.status != statusLoading) { onRequestScan() },
            color = Color(0xFFF5F6FF),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E5F7))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "📷", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (state.status == statusLoading)
                            "Leyendo etiqueta…"
                        else "Escanear etiqueta",
                        color = CreateTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Detecta automáticamente lote y vencimiento",
                        color = CreateTextSecondary,
                        fontSize = 12.sp
                    )
                }
                if (state.status == statusLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = CreateGreen,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
        }

        AnimatedVisibility(
            visible = state.status == statusReady && state.resultado != null,
            enter = fadeIn() + slideInVertically { it / 8 },
            exit = fadeOut()
        ) {
            val r = state.resultado
            if (r != null) {
                val periodo = r.vencimientoMmAa?.let { periodoResumidoVencimiento(it) }
                val estaVencido = periodo == "Vencido"
                val vencDetectado = !r.vencimientoMmAa.isNullOrBlank()

                // Auto-aplicar al aparecer la tarjeta READY: no hay botÃ³n
                // "Aplicar" â€” los datos se inyectan al formulario apenas
                // se detectan. Si el vencimiento estÃ¡ vencido NO se aplica
                // (la callback interna ya filtra fechas pasadas).
                LaunchedEffect(r) {
                    onApplyResult(r)
                }

                val cardColor = if (estaVencido) Color(0xFFFFF1F0) else CreateGreenSoft
                val borderColor = if (estaVencido)
                    CreateRed.copy(alpha = 0.45f)
                else CreateGreen.copy(alpha = 0.4f)
                val headerColor = if (estaVencido) CreateRed else CreateGreen
                val headerText = if (estaVencido)
                    "⚠️ Fecha vencida – no se aplicó"
                else "✓ Datos aplicados"

                Surface(
                    color = cardColor,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = headerText,
                            color = headerColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (!r.loteNumero.isNullOrBlank()) {
                            Text(
                                text = "Lote: ${r.loteNumero}",
                                color = CreateTextPrimary,
                                fontSize = 14.sp
                            )
                        } else {
                            Text(
                                text = "Lote: no detectado",
                                color = CreateTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        if (vencDetectado) {
                            Text(
                                text = "Vencimiento: ${r.vencimientoMmAa}",
                                color = if (estaVencido) CreateRed else CreateTextPrimary,
                                fontWeight = if (estaVencido) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                            if (periodo != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = periodo,
                                    color = if (estaVencido) CreateRed else CreateGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Text(
                                text = "Vencimiento: no detectado",
                                color = CreateTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        if (estaVencido) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Este lote ya venció. Toma otra foto o edita los campos manualmente.",
                                color = CreateRed,
                                fontSize = 12.sp
                            )
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Puedes editarlos abajo si fuera necesario.",
                                color = CreateTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onRequestScan,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📷 Tomar otra foto")
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = state.status == statusError) {
            Surface(
                color = Color(0xFFFFF1F0),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CreateRed.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "No pudimos leer la etiqueta. Inténtalo con mejor luz.",
                        color = CreateRed,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onDismiss) { Text("OK") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpirationDateField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    error: String? = null,
    helper: String? = null,
    helperColor: Color = CreateTextSecondary,
    isSuccess: Boolean = false,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val bringIntoViewScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val navigationBottom = WindowInsets.navigationBars.getBottom(density)
    val keyboardVisible = imeBottom > navigationBottom

    val focusManager = LocalFocusManager.current

    // Estado del picker visual de fecha. Se abre al tocar el campo.
    var showDatePicker by remember { mutableStateOf(false) }

    var hasFocus by remember { mutableStateOf(false) }
    var fieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }

    LaunchedEffect(value) {
        if (fieldValue.text != value) {
            fieldValue = TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        }
    }

    LaunchedEffect(hasFocus, keyboardVisible, imeBottom) {
        if (hasFocus) {
            delay(if (keyboardVisible) 60 else 180)
            bringIntoViewRequester.bringIntoView()
        }
    }

    Column(modifier = modifier) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                color = if (!enabled) CreateTextSecondary.copy(alpha = 0.5f) else if (error != null) CreateRed else CreateTextSecondary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Surface(
            color = if (enabled) Color.White else CreateBackground,
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(
                width = if (enabled && (hasFocus || isSuccess)) 2.dp else 1.dp,
                color = when {
                    !enabled -> CreateBorder.copy(alpha = 0.5f)
                    error != null -> CreateRed
                    isSuccess -> CreateGreen
                    hasFocus -> CreateGreen
                    else -> CreateBorder
                }
            ),
            shadowElevation = if (enabled && hasFocus) 3.dp else 0.dp,
            modifier = Modifier.clickable(enabled = enabled) { 
                showDatePicker = true 
            }
        ) {
            val interactionSource = remember { MutableInteractionSource() }

            BasicTextField(
                value = fieldValue,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .onFocusChanged { focusState ->
                        hasFocus = focusState.isFocused
                        if (enabled && focusState.isFocused) {
                            showDatePicker = true
                        }
                    },
                readOnly = true,
                enabled = enabled,
                singleLine = true,
                interactionSource = interactionSource,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = if (enabled) CreateTextPrimary else CreateTextSecondary.copy(alpha = 0.5f)
                ),
                decorationBox = { innerTextField ->
                    TextFieldDefaults.DecorationBox(
                        value = fieldValue.text,
                        innerTextField = innerTextField,
                        enabled = enabled,
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = interactionSource,
                        isError = !error.isNullOrBlank(),
                        placeholder = {
                            Text(
                                text = "MM/AA",
                                color = if (enabled) CreateTextSecondary else CreateTextSecondary.copy(alpha = 0.3f)
                            )
                        },
                        shape = RoundedCornerShape(28.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        container = {}
                    )
                }
            )
        }

        if (enabled && !error.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = error,
                color = CreateRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        } else if (enabled && !helper.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(helperColor)
                )

                Text(
                    text = helper,
                    color = helperColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    if (showDatePicker) {
        ExpirationMonthYearDialog(
            value = value,
            onDismiss = { 
                showDatePicker = false
                focusManager.clearFocus()
            },
            onConfirm = { formatted ->
                fieldValue = TextFieldValue(
                    text = formatted,
                    selection = TextRange(formatted.length)
                )
                onValueChange(formatted)
                showDatePicker = false
                focusManager.clearFocus()
            }
        )
    }
}

@Composable
fun FullDateField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    error: String? = null,
    helper: String? = null,
    helperColor: Color = CreateTextSecondary,
    isSuccess: Boolean = false,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val focusManager = LocalFocusManager.current
    var showDatePicker by remember { mutableStateOf(false) }
    var hasFocus by remember { mutableStateOf(false) }

    var fieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }

    LaunchedEffect(value) {
        if (fieldValue.text != value) {
            fieldValue = TextFieldValue(text = value, selection = TextRange(value.length))
        }
    }

    Column(modifier = modifier) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                color = if (!enabled) CreateTextSecondary.copy(alpha = 0.5f) else if (error != null) CreateRed else CreateTextSecondary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Surface(
            color = if (enabled) Color.White else CreateBackground,
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(
                width = if (enabled && (hasFocus || isSuccess)) 2.dp else 1.dp,
                color = when {
                    !enabled -> CreateBorder.copy(alpha = 0.5f)
                    error != null -> CreateRed
                    isSuccess -> CreateGreen
                    hasFocus -> CreateGreen
                    else -> CreateBorder
                }
            ),
            shadowElevation = if (enabled && hasFocus) 3.dp else 0.dp,
            modifier = Modifier.clickable(enabled = enabled) { 
                showDatePicker = true 
            }
        ) {
            val interactionSource = remember { MutableInteractionSource() }

            BasicTextField(
                value = fieldValue,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .onFocusChanged { focusState ->
                        hasFocus = focusState.isFocused
                        if (enabled && focusState.isFocused) {
                            showDatePicker = true
                        }
                    },
                readOnly = true,
                enabled = enabled,
                singleLine = true,
                interactionSource = interactionSource,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = if (enabled) CreateTextPrimary else CreateTextSecondary.copy(alpha = 0.5f)
                ),
                decorationBox = { innerTextField ->
                    TextFieldDefaults.DecorationBox(
                        value = fieldValue.text,
                        innerTextField = innerTextField,
                        enabled = enabled,
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = interactionSource,
                        isError = !error.isNullOrBlank(),
                        placeholder = {
                            Text(
                                text = "DD/MM/AAAA",
                                color = if (enabled) CreateTextSecondary else CreateTextSecondary.copy(alpha = 0.3f)
                            )
                        },
                        shape = RoundedCornerShape(28.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        container = {}
                    )
                }
            )
        }

        if (enabled && !error.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = error,
                color = CreateRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        } else if (enabled && !helper.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = helper,
                color = helperColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    if (showDatePicker) {
        FullDatePickerDialog(
            value = value,
            onDismiss = { 
                showDatePicker = false
                focusManager.clearFocus()
            },
            onConfirm = { formatted ->
                fieldValue = TextFieldValue(text = formatted, selection = TextRange(formatted.length))
                onValueChange(formatted)
                showDatePicker = false
                focusManager.clearFocus()
            }
        )
    }
}

@Composable
private fun FullDatePickerDialog(
    value: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val today = LocalDate.now()
    
    // Parse current value or use today
    val initialDate = try {
        val parts = value.split("/")
        if (parts.size == 3) {
            LocalDate.of(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
        } else today
    } catch (e: Exception) {
        today
    }

    var selectedYear by remember { mutableStateOf(initialDate.year) }
    var selectedMonth by remember { mutableStateOf(initialDate.monthValue) }
    var selectedDay by remember { mutableStateOf(initialDate.dayOfMonth) }

    // Ensure selectedDay is valid for the selected month/year
    val daysInMonth = remember(selectedMonth, selectedYear) {
        YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
    }
    
    LaunchedEffect(daysInMonth) {
        if (selectedDay > daysInMonth) {
            selectedDay = daysInMonth
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 16.dp,
            border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.15f))
        ) {
            Box(modifier = Modifier.fillMaxWidth().background(PremiumGradient)) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Vencimiento del pago",
                        color = CreateTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )

                    // SELECTOR DE AÑO
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Año:", color = CreateTextSecondary, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                        OutlinedButton(
                            onClick = { selectedYear-- },
                            enabled = selectedYear > today.year - 5,
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp)
                        ) { Text("-", fontSize = 20.sp) }
                        
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = selectedYear.toString(),
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = CreateTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        }

                        OutlinedButton(
                            onClick = { selectedYear++ },
                            enabled = selectedYear < today.year + 10,
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp)
                        ) { Text("+", fontSize = 20.sp) }
                    }

                    // SELECTOR DE MES
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mes:", color = CreateTextSecondary, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                        val monthName = YearMonth.of(selectedYear, selectedMonth).month.getDisplayName(java.time.format.TextStyle.FULL, Locale("es", "ES"))
                            .replaceFirstChar { it.uppercase() }
                        
                        IconButton(onClick = { if (selectedMonth > 1) selectedMonth-- else selectedMonth = 12 }) {
                            Icon(Icons.Default.ChevronRight, null, modifier = Modifier.graphicsLayer { rotationZ = 180f })
                        }
                        
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = monthName,
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = CreateTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        IconButton(onClick = { if (selectedMonth < 12) selectedMonth++ else selectedMonth = 1 }) {
                            Icon(Icons.Default.ChevronRight, null)
                        }
                    }

                    // GRID DE DÍAS
                    Text("Día:", color = CreateTextSecondary, fontWeight = FontWeight.Bold)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (day in 1..daysInMonth) {
                            val isSelected = selectedDay == day
                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedDay = day },
                                color = if (isSelected) CreateGreen else Color.White.copy(alpha = 0.3f),
                                border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = day.toString(),
                                        color = if (isSelected) Color.White else CreateTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(54.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Cancelar", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                val d = selectedDay.toString().padStart(2, '0')
                                val m = selectedMonth.toString().padStart(2, '0')
                                onConfirm("$d/$m/$selectedYear")
                            },
                            modifier = Modifier.weight(1f).height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CreateGreen)
                        ) {
                            Text("Confirmar", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpirationMonthYearDialog(
    value: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val today = LocalDate.now()
    val currentMonth = today.monthValue
    val currentYear = today.year
    val initialParts = value.split("/")
    var selectedYear by remember(value) {
        mutableStateOf((initialParts.getOrNull(1)?.toIntOrNull()?.plus(2000) ?: currentYear).coerceIn(currentYear, currentYear + 10))
    }
    var selectedMonth by remember(value, selectedYear) {
        val initialMonth = initialParts.getOrNull(0)?.toIntOrNull()
            ?.takeIf { it in 1..12 }
            ?: currentMonth
        mutableStateOf(
            if (selectedYear == currentYear) initialMonth.coerceAtLeast(currentMonth) else initialMonth
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 16.dp,
            border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PremiumGradient)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = "Vencimiento del lote",
                        color = CreateTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Selecciona mes y año. No se muestran meses vencidos.",
                        color = CreateTextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedYear = (selectedYear - 1).coerceAtLeast(currentYear)
                                if (selectedYear == currentYear) selectedMonth = selectedMonth.coerceAtLeast(currentMonth)
                            },
                            enabled = selectedYear > currentYear,
                            modifier = Modifier.size(52.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            border = BorderStroke(1.dp, CreateBorder)
                        ) {
                            Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = selectedYear.toString(),
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = CreateGreen,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        }

                        OutlinedButton(
                            onClick = { selectedYear = (selectedYear + 1).coerceAtMost(currentYear + 10) },
                            enabled = selectedYear < currentYear + 10,
                            modifier = Modifier.size(52.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            border = BorderStroke(1.dp, CreateBorder)
                        ) {
                            Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (1..12).forEach { month ->
                            val enabled = selectedYear > currentYear || month >= currentMonth
                            val isSelected = selectedMonth == month

                            Surface(
                                modifier = Modifier
                                    .size(width = 64.dp, height = 44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = enabled) { selectedMonth = month },
                                color = if (isSelected) CreateGreen else if (enabled) Color.White.copy(alpha = 0.4f) else CreateBackground.copy(alpha = 0.5f),
                                border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = month.toString().padStart(2, '0'),
                                        color = if (isSelected) Color.White else if (enabled) CreateTextPrimary else CreateTextSecondary.copy(alpha = 0.4f),
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CreateBorder)
                        ) {
                            Text("Cancelar", color = CreateTextSecondary, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                val mm = selectedMonth.toString().padStart(2, '0')
                                val aa = (selectedYear % 100).toString().padStart(2, '0')
                                onConfirm("$mm/$aa")
                            },
                            modifier = Modifier.weight(1f).height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CreateGreen)
                        ) {
                            Text("Aplicar", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Convierte un texto "MM/AA" a millis UTC para inicializar el DatePicker.
 * Si el texto estÃ¡ vacÃ­o o es invÃ¡lido, retorna la fecha actual.
 */
private fun parseExpirationToMillis(
    value: String,
    today: java.time.LocalDate
): Long {
    val parts = value.trim().split("/")
    val month = parts.getOrNull(0)?.toIntOrNull()
    val year2 = parts.getOrNull(1)?.toIntOrNull()
    val ld = if (month != null && month in 1..12 && year2 != null) {
        runCatching {
            java.time.YearMonth.of(2000 + year2, month).atEndOfMonth()
        }.getOrNull() ?: today
    } else today
    return ld.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
}

/**
 * Picker de categorÃ­a dirigido por IA (Gemini).
 *
 * Renderiza distintas vistas segÃºn el `status`:
 *  - INITIAL: tarjeta vacÃ­a invitando a escribir el nombre del producto.
 *  - LOADING: tarjeta con shimmer animado mientras Gemini consulta.
 *  - READY: tarjeta hermosa con el emoji + categorÃ­a + confianza + razÃ³n.
 *           El campo `state.category` ya fue auto-rellenado por el caller,
 *           asÃ­ que aquÃ­ solo hay un botÃ³n discreto para "escribir manualmente".
 *  - MANUAL / FALLBACK_MANUAL: aparece el text field tradicional con
 *           autocomplete sobre las categorÃ­as existentes.
 *
 * El text field NO se muestra fuera de los estados MANUAL/FALLBACK_MANUAL;
 * eso evita la confusiÃ³n visual de "dos lugares para escribir".
 */
@Composable
private fun CategoryAutocompleteField(
    modifier: Modifier = Modifier,
    value: String,
    productName: String = "",
    options: List<String>,
    error: String?,
    controlType: CreateProductControlType?,
    controlTypeError: String?,
    onValueChange: (String) -> Unit,
    onControlTypeChange: (CreateProductControlType) -> Unit,

    suggestionState: CategorySuggestionUiState = CategorySuggestionUiState(),
    onSwitchToManual: () -> Unit = {},
    onBackToAi: () -> Unit = {},
    onCategorySelectedFromSuggestion: (String) -> Unit = onValueChange,
    categorySelectedFromAi: Boolean = false
) {
    val status = suggestionState.status
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // 1. NUEVO: Creamos un requester especÃ­fico para la tarjeta de carga
    val loadingRequester = remember { BringIntoViewRequester() }

    val suggestedControlType = remember(suggestionState.sugerenciaTipoManual, value, suggestionState.suggestion, categorySelectedFromAi) {
        if (categorySelectedFromAi) {
            // Revisamos primero la sugerencia principal de Gemini
            val tipoPrincipal = suggestionState.suggestion?.tipoControl?.toCreateProductControlTypeOrNull()
            if (tipoPrincipal != null) {
                tipoPrincipal
            } else {
                // Si no, caemos en la sugerencia secundaria
                suggestionState.sugerenciaTipoManual
                    ?.takeIf { it.confianza == ConfianzaIA.ALTA }
                    ?.tipo
                    ?.toCreateProductControlTypeOrNull()
            }
        } else null
    }

    // 2. NUEVO: Forzar el scroll hacia la tarjeta cuando el estado cambie a LOADING
    LaunchedEffect(suggestionState.status) {
        if (suggestionState.status == CategorySuggestionStatus.LOADING) {
            delay(150) // Breve pausa para dejar que la animaciÃ³n nazca
            loadingRequester.bringIntoView()
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val isAnalyzing = suggestionState.status == CategorySuggestionStatus.LOADING ||
                suggestionState.estaCargandoAsistencia
            val isReady = suggestionState.status == CategorySuggestionStatus.READY
            val isManual = suggestionState.status == CategorySuggestionStatus.MANUAL ||
                suggestionState.status == CategorySuggestionStatus.FALLBACK_MANUAL
            val isInitial = suggestionState.status == CategorySuggestionStatus.INITIAL

            // 1. ESTADO DE CARGA / ANÁLISIS
            AnimatedVisibility(
                visible = isAnalyzing,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(loadingRequester),
                    color = Color.White,
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, CreateBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = CreateGreen,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Buscando la mejor categoría...",
                                color = CreateTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "analizando el nombre",
                                color = CreateTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 2. SUGERENCIA IA (NUEVA UX PREMIUM: Sólo se muestra si NO estamos en manual)
            AnimatedVisibility(
                visible = isReady && suggestionState.suggestion != null && !isManual && value.isBlank(),
                enter = fadeIn() + slideInVertically { it / 4 },
                exit = fadeOut() + shrinkVertically()
            ) {
                suggestionState.suggestion?.let { suggestion ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "CATEGORÍA SUGERIDA",
                            color = CreateTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        // TARJETA DE SELECCIÓN COMPACTA Y SIN ICONOS
                        Surface(
                            modifier = Modifier
                                .wrapContentWidth()
                                .clickable { onCategorySelectedFromSuggestion(suggestion.categoria) },
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 2.dp,
                            border = BorderStroke(1.5.dp, CreateGreen.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = suggestion.categoria,
                                    color = CreateGreen,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .background(CreateGreen.copy(alpha = 0.1f), CircleShape)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "ESCOGER",
                                        color = CreateGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        // Botón sutil para forzar manual
                        TextButton(
                            onClick = onSwitchToManual,
                            modifier = Modifier.align(Alignment.Start),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = CreateTextSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Escribir manualmente",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CreateTextSecondary
                            )
                        }
                    }
                }
            }

            // 3. ENTRADA MANUAL (SÓLO SI SE SOLICITA O SI YA HAY UN VALOR SELECCIONADO/ESCRITO)
            // Se muestra si el usuario lo pide (isManual) o si ya tenemos un valor (value no vacío)
            val showManualEntry = isManual || value.isNotBlank()
            val isCategoryValid = value.isNotBlank() && error == null

            AnimatedVisibility(
                visible = showManualEntry,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                CategoryManualEntry(
                    modifier = Modifier,
                    value = value,
                    options = options,
                    error = error,
                    isSuccess = isCategoryValid,
                    showBackToAi = false, // V28.5: Desactivado para no distraer al usuario tras su decisión
                    onValueChange = onValueChange,
                    onCategorySelectedFromSuggestion = { selected ->
                        onCategorySelectedFromSuggestion(selected)
                        focusManager.clearFocus(force = true)
                        keyboardController?.hide()
                    },
                    onBackToAi = onBackToAi,
                    suggestionState = suggestionState,
                    label = "CATEGORÍA",
                    placeholder = "Ej. Analgésicos",
                    leadingIcon = Icons.Outlined.Grass,
                    trailingIcon = null,
                    suppressSuggestions = categorySelectedFromAi
                )
            }

            // Selector de tipo de producto
            AnimatedVisibility(
                visible = value.isNotBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ProductTypeSelector(
                    selected = controlType,
                    error = controlTypeError,
                    suggestedType = suggestedControlType,
                    showSuggestionBadge = suggestedControlType != null,
                    onSelected = onControlTypeChange
                )
            }
        }
    }
}

@Composable
private fun CategoryManualEntry(
    modifier: Modifier,
    value: String,
    options: List<String>,
    error: String?,
    isSuccess: Boolean = false,
    showBackToAi: Boolean,
    onValueChange: (String) -> Unit,
    onCategorySelectedFromSuggestion: (String) -> Unit = onValueChange,
    onBackToAi: () -> Unit,
    suggestionState: CategorySuggestionUiState = CategorySuggestionUiState(),
    label: String = "",
    placeholder: String = "Ej. Analgésicos",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    suppressSuggestions: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var hasCategoryFocus by remember { mutableStateOf(false) }

    val suggestionsRequester = remember { BringIntoViewRequester() }

    val suggestions = remember(value, hasCategoryFocus, options, suggestionState.asistenciaManualCategorias, suggestionState.suggestion, suppressSuggestions) {
        val normalizedValue = value.trim().stripAccents().lowercase()

        // Incorporar la sugerencia principal de la IA
        val mainAiResult = suggestionState.suggestion?.categoria
        val aiOptions = (listOfNotNull(mainAiResult) + suggestionState.asistenciaManualCategorias)
            .filter { it.isNotBlank() }
            .distinctBy { it.trim().stripAccents().lowercase() }

        // --- MEJORA: Eliminamos la restricciÃ³n de !hasCategoryFocus ---
        // Ahora, si la IA tiene un resultado (mainAiResult), se muestra SIEMPRE.
        // Si no hay IA, mantenemos el comportamiento anterior (esperar foco o valor).

        val shouldShowAiSuggestion = mainAiResult != null

        if (suppressSuggestions) {
            emptyList()
        } else if (shouldShowAiSuggestion) {
            aiOptions.filter {
                val opt = it.trim().stripAccents().lowercase()
                normalizedValue.isBlank() || opt.startsWith(normalizedValue)
            }.take(6)
        } else if (hasCategoryFocus) {
            if (value.isBlank()) options.take(6)
            else options.filter { it.contains(value, ignoreCase = true) }.take(6)
        } else {
            emptyList()
        }
    }

    val mainAiResult = suggestionState.suggestion?.categoria


    // Auto-scroll para que el teclado no tape las sugerencias
    LaunchedEffect(suggestions.isNotEmpty(), hasCategoryFocus) {
        if (suggestions.isNotEmpty() && hasCategoryFocus) {
            delay(400) // Un poquito mÃ¡s de delay para asegurar que el teclado terminÃ³
            suggestionsRequester.bringIntoView()
        }
    }

    Column {
        AppTextField(
            modifier = modifier.onFocusChanged { hasCategoryFocus = it.isFocused },
            label = label,
            value = value,
            error = error,
            isSuccess = isSuccess,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon ?: if (value.isNotBlank() && error == null) {
                { Icon(Icons.Outlined.CheckCircle, null, tint = CreateGreen, modifier = Modifier.size(20.dp)) }
            } else null,
            onValueChange = onValueChange
        )

        // Sugerencias estilo YouTube integradas (nacen del editext)
        AnimatedVisibility(
            visible = suggestions.isNotEmpty(),
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
        ) {
            Column(modifier = Modifier.bringIntoViewRequester(suggestionsRequester)) {
                // Etiqueta de sugerencia solicitada: solo si no hay foco y el valor estÃ¡ vacÃ­o (modo silencioso)
                if (!hasCategoryFocus && value.isBlank() && mainAiResult != null && suggestions.contains(mainAiResult)) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "TE SUGERIMOS ESTA CATEGORÍA",
                        color = CreateTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(22.dp),
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, CreateBorder)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        suggestions.forEachIndexed { index, option ->
                            val isAiSuggestion = mainAiResult != null && option.equals(mainAiResult, ignoreCase = true)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onCategorySelectedFromSuggestion(option)
                                        hasCategoryFocus = false
                                        focusManager.clearFocus(force = true)
                                        keyboardController?.hide()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isAiSuggestion) Icons.Outlined.Lightbulb else Icons.AutoMirrored.Outlined.Label,
                                    contentDescription = null,
                                    tint = if (isAiSuggestion) CreateGreen else CreateTextSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = option,
                                    color = if (isAiSuggestion) CreateGreen else CreateTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = if (isAiSuggestion) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                            if (index < suggestions.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(CreateBorder.copy(alpha = 0.5f))
                                        .padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
                // Espacio extra para que el scroll baje un poco mÃ¡s y no quede pegado al teclado
                Spacer(modifier = Modifier.height(32.dp))
            }
        }


        if (showBackToAi) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onBackToAi) {
                Text("← Volver a sugerencia IA")
            }
        }
    }

}


/** Modificador helper para escalar uniformemente con un Float. */
private fun Modifier.graphicsScale(scale: Float): Modifier =
    this.then(
        Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    )

@Composable
private fun SelectionField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    options: List<String>,
    error: String? = null,
    helper: String? = null,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column {
        Text(
            text = label,
            color = CreateTextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (options.isNotEmpty()) {
                        focusManager.clearFocus(force = true)
                        keyboardController?.hide()
                        expanded = true
                    }
                },
            color = Color.White,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(
                1.dp,
                if (!error.isNullOrBlank()) Color(0xFFB42318) else CreateBorder
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value.ifBlank { "Selecciona una opcion" },
                    modifier = Modifier.weight(1f),
                    color = if (value.isBlank()) CreateTextSecondary else CreateTextPrimary,
                    fontSize = 16.sp
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_drop_down),
                    contentDescription = null,
                    tint = CreateTextSecondary
                )
            }
        }
        if (!error.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = error,
                color = Color(0xFFB42318),
                fontSize = 13.sp
            )
        } else if (!helper.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = helper,
                color = CreateTextSecondary,
                fontSize = 13.sp
            )
        }

        if (expanded) {
            AlertDialog(
                onDismissRequest = { expanded = false },
                confirmButton = {},
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        options.forEach { option ->
                            Text(
                                text = option,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        focusManager.clearFocus(force = true)
                                        keyboardController?.hide()
                                        onValueChange(option)
                                        expanded = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                color = CreateTextPrimary,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            )
        }

    }
}

private fun resolveReceivedPresentationFactor(
    state: CreateProductState,
    presentationName: String
): Double {
    val fromState = state.presentations.firstOrNull {
        it.name.equals(presentationName, ignoreCase = true)
    }?.equivalenceText?.replace(",", ".")?.toDoubleOrNull()
    if (fromState != null && fromState > 0.0) return fromState
    return defaultEquivalenceForPresentation(state.controlType, presentationName).toDouble()
}

private fun defaultEquivalenceForPresentation(
    controlType: CreateProductControlType?,
    presentationName: String
): Int {
    val normalized = presentationName.trim().lowercase()
    return when (controlType) {
        CreateProductControlType.UNIDAD -> when {
            normalized == "unidad" -> 1
            normalized.contains("blister") -> 10
            normalized.contains("caja") -> 10
            normalized.contains("pack") -> 6
            normalized.contains("sobre") -> 1
            else -> 1
        }

        CreateProductControlType.PESO -> when (normalized) {
            "gr" -> 1
            "100 g" -> 100
            "500 g" -> 500
            "kg", "1 kg" -> 1000
            else -> 1
        }

        CreateProductControlType.LIQUIDO -> when (normalized) {
            "ml" -> 1
            "100 ml" -> 100
            "250 ml" -> 250
            "500 ml" -> 500
            "litro", "1 litro" -> 1000
            else -> 1
        }

        null -> 1
    }
}

// Fix: sanea la entrada decimal manteniendo solo dígitos y, como máximo,
// un único punto decimal. Antes los campos de cantidad aceptaban
// múltiples puntos (ej. "12.3.4"), generando un texto que `toDoubleOrNull`
// rechazaba y dejando el campo en un estado inválido silencioso.
private fun sanitizeDecimalInput(raw: String): String {
    val sb = StringBuilder(raw.length)
    var dotSeen = false
    for (ch in raw) {
        when {
            ch.isDigit() -> sb.append(ch)
            ch == '.' && !dotSeen -> {
                sb.append(ch)
                dotSeen = true
            }
        }
    }
    return sb.toString()
}

private fun parseCreateProductNumber(value: String): Double {
    return value
        .trim()
        .replace(",", ".")
        .toDoubleOrNull()
        ?: 0.0
}

fun calculateTotalBaseStock(state: CreateProductState): Double {
    val rawAmount = when (state.stockEntryMode) {
        null -> 0.0
        CreateProductStockEntryMode.UNIDAD -> {
            val quantity = parseCreateProductNumber(state.receivedUnitsText)
            val content = parseCreateProductNumber(state.unitsPerItemText)
            if (state.controlType == CreateProductControlType.UNIDAD) {
                if (content > 0.0) quantity * content else quantity
            } else if (content > 0.0) {
                quantity * content
            } else {
                0.0
            }
        }
        CreateProductStockEntryMode.CAJA -> {
            val boxes = parseCreateProductNumber(state.boxesReceivedText)
            val itemsPerBox = parseCreateProductNumber(state.unitsPerBoxText)
            val unitsPerItem = parseCreateProductNumber(state.unitsPerItemText)
            
            if (state.controlType == CreateProductControlType.UNIDAD) {
                val totalItems = boxes * itemsPerBox
                if (unitsPerItem > 0.0) totalItems * unitsPerItem else totalItems
            } else if (unitsPerItem > 0.0) {
                // Para Líquido/Peso: Cajas * Envases en cada caja * Contenido de cada envase
                boxes * itemsPerBox * unitsPerItem
            } else {
                0.0
            }
        }
        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
            val boxes = parseCreateProductNumber(state.boxesReceivedText)
            val packagesPerBox = parseCreateProductNumber(state.packagesPerBoxText).takeIf { it > 0 } ?: 1.0
            val unitsPerPackage = parseCreateProductNumber(state.unitsPerPackageText)
            boxes * packagesPerBox * unitsPerPackage
        }
    }

    // Normalizar a unidad base (g o mL) si se usaron unidades grandes (kg o L)
    return if (state.stockEntryUnit == "kg" || state.stockEntryUnit == "L") {
        rawAmount * 1000.0
    } else {
        rawAmount
    }
}

fun calculateBaseMinimumStock(state: CreateProductState): Double {
    val selectedUnits = state.minimumStockUnits.takeIf { it > 0 }?.toDouble()
        ?: parseCreateProductNumber(state.minimumStockText).takeIf { it > 0.0 }
        ?: 0.0
    if (selectedUnits <= 0.0) return 0.0

    return when (resolveMinimumStockControlMode(state)) {
        StockControlMode.DIVISIBLE -> {
            // V21.0: Escalar el valor visual a la unidad base para cálculos internos
            if (state.stockEntryUnit == "kg" || state.stockEntryUnit == "L") {
                selectedUnits * 1000.0
            } else {
                selectedUnits
            }
        }
        StockControlMode.INDIVISIBLE -> when (state.controlType) {
            CreateProductControlType.LIQUIDO,
            CreateProductControlType.PESO -> {
                val basePorContenedor = parseCreateProductNumber(state.unitsPerItemText)
                    .takeIf { it > 0.0 }
                    ?: 1.0
                
                // Si la unidad del contenedor está en kg/L, la basePorContenedor ya viene en gramos/mL (por el diálogo)
                selectedUnits * basePorContenedor
            }
            CreateProductControlType.UNIDAD -> when (state.stockEntryMode) {
                CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
                    val unidadesPorBlister = parseCreateProductNumber(state.unitsPerPackageText)
                        .takeIf { it > 0.0 }
                        ?: 1.0
                    selectedUnits * unidadesPorBlister
                }
                CreateProductStockEntryMode.CAJA -> {
                    val unitsPerItem = parseCreateProductNumber(state.unitsPerItemText).takeIf { it > 0.0 } ?: 1.0
                    selectedUnits * unitsPerItem
                }
                CreateProductStockEntryMode.UNIDAD -> {
                    val unitsPerItem = parseCreateProductNumber(state.unitsPerItemText).takeIf { it > 0.0 } ?: 1.0
                    selectedUnits * unitsPerItem
                }
                else -> selectedUnits
            }
            null -> selectedUnits
        }
    }.coerceAtLeast(0.0)
}

fun getPurchasePresentationName(mode: CreateProductStockEntryMode?): String {
    return when (mode) {
        CreateProductStockEntryMode.UNIDAD -> "Unidad"
        CreateProductStockEntryMode.CAJA -> "Caja"
        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> "Caja"
        null -> "Unidad"
    }
}

fun getPurchasePresentationName(
    controlType: CreateProductControlType?,
    mode: CreateProductStockEntryMode?
): String {
    return when (mode) {
        CreateProductStockEntryMode.UNIDAD -> when (controlType) {
            CreateProductControlType.LIQUIDO -> "Frasco"
            CreateProductControlType.PESO -> "Empaque"
            else -> "Unidad"
        }
        CreateProductStockEntryMode.CAJA -> "Caja"
        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> when (controlType) {
            CreateProductControlType.LIQUIDO -> "Frasco"
            CreateProductControlType.PESO -> "Empaque"
            else -> "Blister"
        }
        null -> getPurchasePresentationName(mode)
    }
}

fun isStockEntryModeValidForControlType(
    mode: CreateProductStockEntryMode?,
    controlType: CreateProductControlType?
): Boolean {
    if (mode == null || controlType == null) return false
    return when (controlType) {
        CreateProductControlType.LIQUIDO,
        CreateProductControlType.PESO -> mode != CreateProductStockEntryMode.CAJA_CON_PAQUETES
        CreateProductControlType.UNIDAD -> true
    }
}

fun isStockEntryModeValidForControlType(
    controlType: CreateProductControlType?,
    mode: CreateProductStockEntryMode?
): Boolean = isStockEntryModeValidForControlType(mode, controlType)

fun CreateProductState.resetStockEntryConfiguration(): CreateProductState {
    return copy(
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
        purchaseCost = "",
        stockEntryConfigured = false,
        errors = errors - "stockEntryMode" - "purchaseCost" - "minimumStock"
    )
}

fun formatCreateProductNumber(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        value.toString()
    }
}

/**
 * Devuelve el total recibido del lote actual en unidad base
 * (g, mL o unidades). Se usa para calcular sugerencias derivadas
 * (stock mínimo automático, costo unitario, etc.) sin duplicar la
 * lógica del cÃ¡lculo principal.
 */
/**
 * Devuelve un identificador del "grupo de sinónimos" al que pertenece el
 * nombre de una presentación. Dos presentaciones con el mismo grupo se
 * consideran equivalentes (ej. "Tableta" y "Cápsula" representan la misma
 * unidad mínima de venta y no tendría sentido tener ambas).
 *
 * Devuelve `null` si el nombre no calza con ningún grupo conocido, en cuyo
 * caso no se aplica la regla de reemplazo por similitud.
 */
private fun grupoSinonimoPresentacion(nombre: String): String? {
    val n = nombre.trim().lowercase(Locale.getDefault())
        .let { java.text.Normalizer.normalize(it, java.text.Normalizer.Form.NFD) }
        .replace(Regex("\\p{M}+"), "")
    return when (n) {
        // Unidad mínima de piezas: todas estas representan "1 unidad base"
        "tableta", "tabletas", "comprimido", "comprimidos",
        "pastilla", "pastillas", "pildora", "pildoras",
        "capsula", "capsulas", "perla", "perlas",
        "unidad", "unidades", "pieza", "piezas" -> "unidad-pieza"

        "blister", "blisters", "tira", "tiras", "foil" -> "blister"
        "caja", "cajas", "estuche", "estuches", "envase", "envases" -> "caja"
        "frasco", "frascos", "botella", "botellas" -> "frasco"
        "tubo", "tubos" -> "tubo"
        "sobre", "sobres", "sachet", "sachets" -> "sobre"
        "bolsa", "bolsas" -> "bolsa"
        "ampolla", "ampollas", "vial", "viales" -> "ampolla"

        else -> null
    }
}

/**
 * Inserta una presentación nueva en la lista evitando duplicados lógicos.
 *
 * Si encuentra un match (mismo nombre, mismo grupo de sinónimos o misma
 * equivalencia exacta) reemplaza la existente conservando el precio si la
 * anterior tenía uno. Si no hay match, simplemente la agrega al final.
 *
 * Devuelve la nueva lista y el id de la presentación final ("principal").
 */
fun mergePresentacionEvitandoDuplicado(
    existentes: List<CreateProductPresentation>,
    nueva: CreateProductPresentation
): Pair<List<CreateProductPresentation>, String> {
    val grupoNueva = grupoSinonimoPresentacion(nueva.name)
    val equivNueva = nueva.equivalenceText.trim().replace(",", ".").toDoubleOrNull()

    val match = existentes.firstOrNull { existing ->
        val mismoNombre = existing.name.equals(nueva.name, ignoreCase = true)
        val mismoGrupo = grupoNueva != null &&
            grupoSinonimoPresentacion(existing.name) == grupoNueva
        val mismaEquivalencia = equivNueva != null &&
            existing.equivalenceText.trim().replace(",", ".").toDoubleOrNull() == equivNueva
        mismoNombre || mismoGrupo || mismaEquivalencia
    }

    return if (match != null) {
        val precioPrevio = match.salePriceText.ifBlank { nueva.salePriceText }
        val reemplazada = nueva.copy(
            id = match.id,
            salePriceText = precioPrevio
        )
        val lista = existentes.map { if (it.id == match.id) reemplazada else it }
        lista to reemplazada.id
    } else {
        (existentes + nueva) to nueva.id
    }
}

private fun calculateRecibidoBase(state: CreateProductState): Double {
    return calculateTotalBaseStock(state)
}

private fun calculateInitialStockValue(state: CreateProductState): Double {
    return calculateTotalBaseStock(state)
}

/**
 * Deduce el modo de control efectivo.
 * Si el usuario no ha elegido uno explícitamente (stockControlMode == null),
 * se deduce según el ControlType:
 *  - LIQUIDO / PESO -> INDIVISIBLE (Control por envase)
 *  - UNIDAD -> DIVISIBLE (Control por unidades sueltas)
 */
fun getEffectiveStockControlMode(state: CreateProductState): StockControlMode {
    state.stockControlMode?.let { return it }

    return when (state.controlType) {
        CreateProductControlType.LIQUIDO, CreateProductControlType.PESO -> StockControlMode.INDIVISIBLE
        else -> StockControlMode.DIVISIBLE
    }
}

/**
 * Calcula la cantidad total de unidades físicas disponibles para control.
 *  - En INDIVISIBLE: cuenta cuántos "envases" hay (frascos o cajas).
 *  - En DIVISIBLE: cuenta el total de unidades base (tabletas, etc).
 */
fun calculateTotalPhysicalUnits(state: CreateProductState): Int {
    return when (resolveMinimumStockControlMode(state)) {
        StockControlMode.INDIVISIBLE -> {
            when (state.controlType) {
                CreateProductControlType.LIQUIDO,
                CreateProductControlType.PESO -> when (state.stockEntryMode) {
                    CreateProductStockEntryMode.UNIDAD -> parseCreateProductNumber(state.receivedUnitsText).toInt()
                    CreateProductStockEntryMode.CAJA -> (
                        parseCreateProductNumber(state.boxesReceivedText) *
                            parseCreateProductNumber(state.unitsPerBoxText)
                        ).toInt()
                    CreateProductStockEntryMode.CAJA_CON_PAQUETES -> (
                        parseCreateProductNumber(state.boxesReceivedText) *
                            (parseCreateProductNumber(state.packagesPerBoxText).takeIf { it > 0 } ?: 1.0)
                        ).toInt()
                    null -> 0
                }
                CreateProductControlType.UNIDAD -> when (state.stockEntryMode) {
                    CreateProductStockEntryMode.UNIDAD -> parseCreateProductNumber(state.receivedUnitsText).toInt()
                    CreateProductStockEntryMode.CAJA -> (
                        parseCreateProductNumber(state.boxesReceivedText) *
                            parseCreateProductNumber(state.unitsPerBoxText)
                        ).toInt()
                    CreateProductStockEntryMode.CAJA_CON_PAQUETES -> (
                        parseCreateProductNumber(state.boxesReceivedText) *
                            (parseCreateProductNumber(state.packagesPerBoxText).takeIf { it > 0 } ?: 1.0)
                        ).toInt()
                    null -> 0
                }
                null -> 0
            }
        }
        StockControlMode.DIVISIBLE -> {
            calculateInitialStockValue(state).toInt()
        }
    }.coerceAtLeast(0)
}

/**
 * Genera la lista de opciones válidas (en unidades físicas) para el stock mínimo.
 */
fun calculateValidStockOptions(totalPhysicalUnits: Int, mode: StockControlMode): List<Int> {
    if (totalPhysicalUnits <= 0) return emptyList()
    if (totalPhysicalUnits == 1) return listOf(1)

    val options = mutableListOf<Int>()

    when (mode) {
        StockControlMode.INDIVISIBLE -> {
            // Paso de 1 en 1 para envases.
            for (i in 1..totalPhysicalUnits) {
                options.add(i)
            }
        }
        StockControlMode.DIVISIBLE -> {
            // Paso del 10% redondeado hacia arriba.
            val step = kotlin.math.ceil(totalPhysicalUnits * 0.10).toInt().coerceAtLeast(1)
            var current = step
            while (current < totalPhysicalUnits) {
                options.add(current)
                current += step
            }
            if (options.isEmpty() || options.last() != totalPhysicalUnits) {
                options.add(totalPhysicalUnits)
            }
        }
    }

    return options.distinct().sorted()
}

/**
 * Retorna la etiqueta de la unidad física (singular o plural).
 */
fun getPhysicalUnitLabel(state: CreateProductState, quantity: Int): String {
    val mode = resolveMinimumStockControlMode(state)
    val isPlural = quantity != 1

    return when (mode) {
        StockControlMode.INDIVISIBLE -> {
            when (state.controlType) {
                CreateProductControlType.LIQUIDO -> if (isPlural) "frascos" else "frasco"
                CreateProductControlType.PESO -> if (isPlural) "envases" else "envase"
                CreateProductControlType.UNIDAD -> {
                    when (state.stockEntryMode) {
                        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> if (isPlural) "blísteres" else "blíster"
                        CreateProductStockEntryMode.CAJA -> {
                             val unitsPerItem = parseCreateProductNumber(state.unitsPerItemText)
                             if (unitsPerItem > 1.0) {
                                 if (isPlural) "frascos/empaques" else "frasco/empaque"
                             } else {
                                 if (isPlural) "unidades" else "unidad"
                             }
                        }
                        CreateProductStockEntryMode.UNIDAD -> {
                             val unitsPerItem = parseCreateProductNumber(state.unitsPerItemText)
                             if (unitsPerItem > 1.0) {
                                 if (isPlural) "frascos/empaques" else "frasco/empaque"
                             } else {
                                 if (isPlural) "unidades" else "unidad"
                             }
                        }
                        else -> if (isPlural) "unidades" else "unidad"
                    }
                }
                null -> if (isPlural) "unidades" else "unidad"
            }
        }
        StockControlMode.DIVISIBLE -> {
            if (isPlural) "unidades" else "unidad"
        }
    }
}

fun resolveMinimumStockControlMode(state: CreateProductState): StockControlMode {
    state.stockControlMode?.let { return it }

    return when (state.controlType) {
        CreateProductControlType.LIQUIDO,
        CreateProductControlType.PESO -> StockControlMode.INDIVISIBLE
        CreateProductControlType.UNIDAD -> when (state.stockEntryMode) {
            CreateProductStockEntryMode.CAJA_CON_PAQUETES -> StockControlMode.INDIVISIBLE
            CreateProductStockEntryMode.CAJA -> {
                // V22.1: Si la caja contiene items discretos (ej. frascos con tabletas), 
                // permitimos control por contenedor indivisible.
                if (parseCreateProductNumber(state.unitsPerItemText) > 1.0) {
                    StockControlMode.INDIVISIBLE
                } else {
                    StockControlMode.DIVISIBLE
                }
            }
            CreateProductStockEntryMode.UNIDAD,
            null -> StockControlMode.DIVISIBLE
        }
        null -> StockControlMode.DIVISIBLE
    }
}

fun resolveMinimumStockLabel(
    state: CreateProductState,
    quantity: Int,
    mode: StockControlMode = resolveMinimumStockControlMode(state)
): String {
    val singular = quantity == 1

    return when (mode) {
        StockControlMode.DIVISIBLE -> when (state.controlType) {
            CreateProductControlType.PESO -> state.stockEntryUnit.ifBlank { "g" }
            CreateProductControlType.LIQUIDO -> state.stockEntryUnit.ifBlank { "mL" }
            else -> if (singular) "unidad" else "unidades"
        }
        StockControlMode.INDIVISIBLE -> when (state.controlType) {
            CreateProductControlType.LIQUIDO -> if (singular) "frasco" else "frascos"
            CreateProductControlType.PESO -> if (singular) "envase" else "envases"
            CreateProductControlType.UNIDAD -> {
                if (state.stockEntryMode == CreateProductStockEntryMode.CAJA_CON_PAQUETES) {
                    if (singular) "blíster" else "blísteres"
                } else {
                    if (singular) "unidad" else "unidades"
                }
            }
            null -> if (singular) "unidad" else "unidades"
        }
    }
}

fun resolveMinimumStockStoredUnit(
    state: CreateProductState,
    quantity: Int,
    mode: StockControlMode = resolveMinimumStockControlMode(state)
): String {
    return resolveMinimumStockLabel(state, quantity, mode)
}

private fun calculateInitialStockInt(state: CreateProductState): Int {
    return calculateInitialStockValue(state)
        .toInt()
        .coerceAtLeast(0)
}

fun buildInitialStockEntrySummary(state: CreateProductState): String {
    val unit = resolveStockUnitLabel(state)
    
    fun plural(n: Double, singular: String, plural: String): String {
        return if (n == 1.0) singular else plural
    }

    fun itemLabel(n: Double): String {
        return when (state.controlType) {
            CreateProductControlType.LIQUIDO -> plural(n, "frasco", "frascos")
            CreateProductControlType.PESO -> plural(n, "envase", "envases")
            else -> plural(n, "unidad", "unidades")
        }
    }

    return when (state.stockEntryMode) {
        null -> ""

        CreateProductStockEntryMode.UNIDAD -> {
            val quantity = parseCreateProductNumber(state.receivedUnitsText)
            val content = parseCreateProductNumber(state.unitsPerItemText)
            if (quantity <= 0.0) return ""

            val total = calculateTotalBaseStock(state)
            val visualTotal = if (state.stockEntryUnit == "kg" || state.stockEntryUnit == "L") total / 1000.0 else total
            
            if (state.controlType == CreateProductControlType.UNIDAD) {
                if (content > 1.0) {
                    "Recibiste ${formatCreateProductNumber(quantity)} ${itemLabel(quantity)} (${formatCreateProductNumber(content)} u. c/u). Total: ${formatCreateProductNumber(visualTotal)} $unit."
                } else {
                    "Recibiste ${formatCreateProductNumber(quantity)} $unit."
                }
            } else {
                "Recibiste ${formatCreateProductNumber(quantity)} ${itemLabel(quantity)} de ${formatCreateProductNumber(content)} ${state.stockEntryUnit.ifBlank { unit }}."
            }
        }

        CreateProductStockEntryMode.CAJA -> {
            val boxes = parseCreateProductNumber(state.boxesReceivedText)
            val unitsPerBox = parseCreateProductNumber(state.unitsPerBoxText)
            val total = calculateTotalBaseStock(state)
            val visualTotal = if (state.stockEntryUnit == "kg" || state.stockEntryUnit == "L") total / 1000.0 else total

            if (boxes <= 0.0 || unitsPerBox <= 0.0) return ""

            "Recibiste ${formatCreateProductNumber(boxes)} ${plural(boxes, "caja", "cajas")} con ${formatCreateProductNumber(unitsPerBox)} ${itemLabel(unitsPerBox)} cada una. Total: ${formatCreateProductNumber(visualTotal)} $unit."
        }

        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
            val boxes = parseCreateProductNumber(state.boxesReceivedText)
            val unitsPerPackage = parseCreateProductNumber(state.unitsPerPackageText)
            val total = boxes * unitsPerPackage

            if (boxes <= 0.0 || unitsPerPackage <= 0.0) return ""

            "Recibiste ${formatCreateProductNumber(boxes)} ${plural(boxes, "blíster", "blísters")} con ${formatCreateProductNumber(unitsPerPackage)} $unit cada uno. Total: ${formatCreateProductNumber(total)} $unit."
        }
    }
}

fun buildStockAvailableSummary(state: CreateProductState): String {
    val total = calculateInitialStockValue(state)
    val totalText = formatCreateProductNumber(total)
    val unit = resolveStockUnitLabel(state)
    return "$totalText $unit"
}

private fun resolveStockUnitLabel(state: CreateProductState): String {
    return when (state.controlType) {
        CreateProductControlType.PESO -> state.stockEntryUnit.ifBlank { "g" }
        CreateProductControlType.LIQUIDO -> state.stockEntryUnit.ifBlank { "mL" }
        else -> "unidades"
    }
}

private fun formatBs(value: String?): String {
    val clean = value?.trim().orEmpty()
    return if (clean.isBlank()) "Bs 0.00" else if (clean.startsWith("Bs")) clean else "Bs $clean"
}

private fun formatFactor(value: Double, unit: String): String {
    val number = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
    return "$number $unit"
}

// Fix: antes, al teclear "1" y luego un dígito que formaba mes 13..19 (p. ej.
// "13", "15"), la función devolvía "13" sin barra. El usuario quedaba
// atascado: como ya había 2 dígitos sin separador, no podía seguir escribiendo
// y la rama de autoprefijo "0X/" solo se aplicaba a 2..9. Ahora, cualquier mes
// inválido en 2 dígitos se reinterpreta como "0<primer-dígito>/<segundo>",
// pasando "13" → "01/3". También se descarta "00" (mes cero) reduciéndolo a
// "0" para que el usuario pueda corregir.
private fun formatExpirationDateInput(raw: String, deleting: Boolean = false): String {
    if (deleting) {
        return raw
            .filter { it.isDigit() || it == '/' }
            .take(5)
    }

    val digits = raw.filter { it.isDigit() }.take(4)
    if (digits.isEmpty()) return ""

    return when (digits.length) {
        1 -> digits
        2 -> {
            val month = digits.toIntOrNull()
            when {
                month != null && month in 1..12 -> "$digits/"
                digits.first() == '0' -> digits.first().toString()
                else -> "0${digits.first()}/${digits.last()}"
            }
        }
        3 -> {
            val month = digits.take(2).toIntOrNull()
            when {
                month != null && month in 1..12 -> "${digits.take(2)}/${digits.drop(2)}"
                digits.first() == '0' -> "0${digits[1]}/${digits.drop(2)}"
                else -> "0${digits.first()}/${digits.drop(1)}"
            }
        }
        else -> {
            val month = digits.take(2).toIntOrNull()
            when {
                month != null && month in 1..12 -> "${digits.take(2)}/${digits.drop(2).take(2)}"
                digits.first() == '0' -> "0${digits[1]}/${digits.drop(2).take(2)}"
                else -> "0${digits.first()}/${digits.drop(1).take(2)}"
            }
        }
    }
}

/**
 * Versión simple para decidir si una cadena MM/AA representa una fecha
 * todavía vigente (final de mes en el futuro). Se usa al recibir
 * resultados de OCR para no contaminar el formulario con fechas vencidas.
 */
private fun esVencimientoVigenteSimple(mmAa: String): Boolean {
    if (!mmAa.matches(Regex("\\d{2}/\\d{2}"))) return false
    val parts = mmAa.split("/")
    val month = parts.getOrNull(0)?.toIntOrNull() ?: return false
    val year = parts.getOrNull(1)?.toIntOrNull() ?: return false
    if (month !in 1..12) return false
    val expira = runCatching { YearMonth.of(2000 + year, month).atEndOfMonth() }
        .getOrNull() ?: return false
    return !expira.isBefore(LocalDate.now())
}

/**
 * Devuelve un período resumido en español ("Vencido", "Menos de 1 mes",
 * "Menos de 3 meses", "Menos de 6 meses", "Menos de 1 año", "Más de 1 año").
 * Esto evita mostrar números poco accionables como "18 meses" — el usuario
 * solo necesita una idea de proximidad de vencimiento.
 */
private fun periodoResumidoVencimiento(mmAa: String): String? {
    if (!mmAa.matches(Regex("\\d{2}/\\d{2}"))) return null
    val parts = mmAa.split("/")
    val month = parts.getOrNull(0)?.toIntOrNull() ?: return null
    val year = parts.getOrNull(1)?.toIntOrNull() ?: return null
    if (month !in 1..12) return null
    val expira = runCatching { YearMonth.of(2000 + year, month).atEndOfMonth() }
        .getOrNull() ?: return null
    val hoy = LocalDate.now()
    if (expira.isBefore(hoy)) return "Vencido"
    val meses = ChronoUnit.MONTHS.between(YearMonth.from(hoy), YearMonth.from(expira))
    return when {
        meses < 1 -> "Vence en menos de 1 mes"
        meses < 3 -> "Vence en menos de 3 meses"
        meses < 6 -> "Vence en menos de 6 meses"
        meses < 12 -> "Vence en menos de 1 año"
        else -> "Vence en más de 1 año"
    }
}

private fun evaluateExpirationStatus(value: String): ExpirationStatus? {
    if (value.isBlank()) return null
    if (!value.matches(Regex("\\d{2}/\\d{2}"))) {
        return ExpirationStatus(
            kind = ExpirationStatusKind.INVALIDO,
            message = "Usa el formato MM/AA",
            color = CreateRed
        )
    }

    val parts = value.split("/")
    val month = parts.getOrNull(0)?.toIntOrNull() ?: return null
    val year = parts.getOrNull(1)?.toIntOrNull() ?: return null

    // Validar mes
    if (month !in 1..12) {
        return ExpirationStatus(
            kind = ExpirationStatusKind.INVALIDO,
            message = "Mes inválido (1-12)",
            color = CreateRed
        )
    }

    // Año completo (asumimos 2000+)
    val fullYear = 2000 + year
    val maxYear = LocalDate.now().year + 10

    if (fullYear > maxYear) {
        return ExpirationStatus(
            kind = ExpirationStatusKind.INVALIDO,
            message = "El año no puede superar ${maxYear} (actual +10 años)",
            color = CreateRed
        )
    }

    // Crear fecha de vencimiento (Comparación de Mes/Año)
    val expiryYearMonth = try {
        YearMonth.of(fullYear, month)
    } catch (e: Exception) {
        null
    }

    if (expiryYearMonth == null) {
        return ExpirationStatus(
            kind = ExpirationStatusKind.INVALIDO,
            message = "Fecha inválida",
            color = CreateRed
        )
    }

    val currentYearMonth = YearMonth.now()
    if (expiryYearMonth.isBefore(currentYearMonth)) {
        return ExpirationStatus(
            kind = ExpirationStatusKind.VENCIDO,
            message = "Este lote ya venció",
            color = CreateRed
        )
    }

    // Unificamos el helper visual con los mismos rangos que usa el escáner.
    // Evitamos números puntuales ("18 meses") y mostramos rangos breves
    // que dan una idea accionable de proximidad.
    val monthsLeft = ChronoUnit.MONTHS.between(
        currentYearMonth,
        expiryYearMonth
    )

    return when {
        monthsLeft < 1 -> ExpirationStatus(
            kind = ExpirationStatusKind.CORTO_PLAZO,
            message = "Vence en menos de 1 mes",
            color = CreateOrange
        )
        monthsLeft < 3 -> ExpirationStatus(
            kind = ExpirationStatusKind.POR_VENCER,
            message = "Vence en menos de 3 meses",
            color = CreateOrange
        )
        monthsLeft < 6 -> ExpirationStatus(
            kind = ExpirationStatusKind.VIGENTE,
            message = "Vence en menos de 6 meses",
            color = CreateGreen
        )
        monthsLeft < 12 -> ExpirationStatus(
            kind = ExpirationStatusKind.VIGENTE,
            message = "Vence en menos de 1 año",
            color = CreateGreen
        )
        else -> ExpirationStatus(
            kind = ExpirationStatusKind.VIGENTE,
            message = "Vence en más de 1 año",
            color = CreateGreen
        )
    }
}



private fun suggestUniqueLotNumber(
    productName: String,
    currentLot: String,
    existingLots: Set<String>
): String {
    val prefix = productName.trim()
        .firstOrNull { it.isLetterOrDigit() }
        ?.uppercaseChar()
        ?.toString()
        ?: currentLot.trim().firstOrNull()?.uppercaseChar()?.toString()
        ?: "L"

    val digits = currentLot.trim().takeLastWhile { it.isDigit() }
    val width = digits.length.coerceAtLeast(3)
    var next = digits.toIntOrNull()?.plus(1) ?: 1
    var candidate: String

    do {
        candidate = prefix + next.toString().padStart(width, '0')
        next++
    } while (existingLots.contains(candidate.uppercase()))

    return candidate
}

@Composable
fun DuplicateProductCard(
    producto: MoldeProductos,
    onAddStock: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CreateTextPrimary.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = CreateTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Producto ya registrado",
                    style = MaterialTheme.typography.titleSmall,
                    color = CreateTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Hemos encontrado '${producto.nombre}' con este mismo código de barras.",
                style = MaterialTheme.typography.bodyMedium,
                color = CreateTextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddStock,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CreateGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrar ingreso de stock")
            }
        }
    }
}


@Composable
private fun BarcodeSection(
    barcode: String,
    isManualMode: Boolean,
    isChecking: Boolean,
    error: String? = null,
    onStartScan: () -> Unit,
    onManualMode: () -> Unit,
    onBarcodeChange: (String) -> Unit,
    onConfirmManual: () -> Unit,
    onClear: () -> Unit
) {
    val isConfirmed = barcode.isNotBlank() && !isManualMode
    val isBarcodeValid = barcode.isNotBlank() && error == null

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = if (isBarcodeValid) 2.dp else 1.dp,
            color = if (isBarcodeValid) CreateGreen else CreateBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (!isConfirmed) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.QrCodeScanner,
                            contentDescription = null,
                            tint = CreateGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Código de barras opcional",
                            style = MaterialTheme.typography.titleSmall,
                            color = CreateTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isChecking) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = CreateGreen)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isManualMode) {
                    AppTextField(
                        label = "INGRESA EL CÓDIGO",
                        value = barcode,
                        error = error,
                        placeholder = "Ej. 193968381356",
                        keyboardType = KeyboardType.Number,
                        leadingIcon = Icons.Outlined.QrCodeScanner,
                        trailingIcon = {
                            if (barcode.isNotBlank()) {
                                IconButton(onClick = onConfirmManual) {
                                    Icon(Icons.Outlined.CheckCircle, null, tint = CreateGreen, modifier = Modifier.size(24.dp))
                                }
                            }
                        },
                        onValueChange = onBarcodeChange,
                        onImeAction = onConfirmManual
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onStartScan,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.35f))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.QrCodeScanner,
                                contentDescription = null,
                                tint = CreateGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Escanear", color = CreateGreen, fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = onManualMode,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text(
                                text = "Escribir código",
                                color = CreateTextSecondary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = CreateGreenSoft,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.QrCodeScanner,
                                contentDescription = null,
                                tint = CreateGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Código de producto",
                            color = CreateTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = barcode,
                            color = CreateTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = onStartScan,
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Cambiar",
                                    color = CreateGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Icon(
                                    imageVector = Icons.Filled.ChevronRight,
                                    contentDescription = null,
                                    tint = CreateGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        IconButton(onClick = onClear) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Quitar código",
                                tint = CreateRed.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (!error.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = CreateRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
