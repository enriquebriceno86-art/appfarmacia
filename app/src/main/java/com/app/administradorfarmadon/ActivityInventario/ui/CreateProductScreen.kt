package com.app.administradorfarmadon.ActivityInventario.ui

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition

import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.relocation.bringIntoView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import com.app.administradorfarmadon.ActivityInventario.ProductUtils
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.R
import kotlinx.coroutines.Dispatchers
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.delay
import com.app.administradorfarmadon.ActivityInventario.DialogosComposeInventario.ExpirationMonthYearDialog
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.abs


private const val INITIAL_INVENTORY_SOURCE_NAME = "Inventario inicial / Sin proveedor"

private fun CreateProductState.isInitialInventorySource(): Boolean {
    return supplierId.isBlank() && supplierName == INITIAL_INVENTORY_SOURCE_NAME
}

private fun CreateProductState.hasRealSupplier(): Boolean {
    return supplierId.isNotBlank()
}

private fun CreateProductState.hasStockOriginSelected(): Boolean {
    return hasRealSupplier() || isInitialInventorySource()
}

val CreateBackground = Color(0xFFF6F8FB)

// Superficies
val CreateSurface = Color(0xFFFFFFFF)
val CreateSurfaceSoft = Color(0xFFF8FAFC)
val CreateInfoGraySoft = Color(0xFFF8FAFC)

// Bordes
val CreateBorder = Color(0xFFE5EAF0)
val CreateBorderStrong = Color(0xFFD0D5DD)

// Verde principal
val CreateGreen = Color(0xFF16A163)
val CreateGreenDark = Color(0xFF067647)
val CreateGreenSoft = Color(0xFFEAF8F0)
val CreateGreenUltraSoft = Color(0xFFF4FBF7)

// Texto
val CreateTextPrimary = Color(0xFF101828)
val CreateTextSecondary = Color(0xFF667085)
val CreateTextMuted = Color(0xFF98A2B3)

// Azul / IA / información
val CreateBlue = Color(0xFF2563EB)
val CreateBlueSoft = Color(0xFFEFF6FF)

val CreateAiFocus = Color(0xFFF6C343)
val CreateAiFocusSoft = Color(0xFFFFF7D6)

// Advertencia
val CreateOrange = Color(0xFFF79009)
val CreateOrangeSoft = Color(0xFFFFF4E5)

// Error
val CreateRed = Color(0xFFD92D20)
val CreateRedSoft = Color(0xFFFFEAEA)

// Amarillo
val CreateYellow = Color(0xFFF5B820)
val CreateYellowSoft = Color(0xFFFFF4CC)

// Gradiente Premium para Diálogos (Verde suave a Azul brillante)
val PremiumGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFFFFFF), Color(0xFFF4FBF7), Color(0xFFEAF8F0)
    )
)

val WaterGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFFFFFF), Color(0xFFF8FAFC), Color(0xFFEFF6FF)
    )
)

val WarningGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFD92D20), Color(0xFFF79009)
    )
)

val TealGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF067647), Color(0xFF16A163)
    )
)


private enum class ExpirationStatusKind {
    VIGENTE, CORTO_PLAZO, UN_MES, POR_VENCER, VENCIDO, INVALIDO
}

private data class ExpirationStatus(
    val kind: ExpirationStatusKind, val message: String, val color: Color
)

enum class CreateProductStep(val number: Int, val title: String, val subtitle: String) {
    PRODUCTO(1, "Producto", ""), LOTE_INICIAL(2, "Registro del Lote", ""), PRESENTACIONES(
        3,
        "Presentaciones de ventas",
        ""
    ),
    RESUMEN(4, "Resumen del producto", "")
}

enum class CreateProductControlType(
    val label: String, val helper: String, val example: String, val baseUnitLabel: String
) {
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
    val label: String, val helper: String
) {
    UNIDAD(
        label = "Unidades sueltas", helper = "Recibiste unidades, frascos o envases individuales."
    ),

    CAJA(
        label = "Caja con unidades",
        helper = "Cada caja contiene unidades directas, frascos o envases."
    ),

    CAJA_CON_PAQUETES(
        label = "Caja con paquetes internos",
        helper = "Cada caja contiene subempaques internos, como sobres, tiras o paquetes."
    )
}

enum class StockControlMode {
    INDIVISIBLE, // Control por envase (frasco, caja, etc.)
    DIVISIBLE    // Control por unidad (tableta, cápsula, etc.)
}

private fun mapStockControlModeFromSuggestedUnit(unit: String?): StockControlMode? {
    val clean = unit?.trim()?.lowercase(java.util.Locale.getDefault()) ?: return null

    return when {
        clean.contains("unid") || clean.contains("und") || clean.contains("uds") || clean.contains("pieza") || clean.contains(
            "pz"
        ) || clean.contains("pza") -> StockControlMode.DIVISIBLE

        clean == "g" || clean == "gr" || clean == "grs" || clean == "kg" || clean == "ml" || clean == "ml." || clean == "l" || clean == "lt" || clean == "litro" || clean == "litros" -> StockControlMode.INDIVISIBLE

        else -> null
    }
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

    // Presentación comercial detectada por IA.
// Solo se usa si IA está encendida y el usuario aplicó una sugerencia IA.
    val defaultSalePresentationName: String = "",
    val defaultSalePresentationFromAi: Boolean = false,
    val saleByFractionSuggested: Boolean? = null,
    val defaultSalePresentationConfidence: Int = 0,
    val defaultSalePresentationReason: String = "",

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
    /** Control visual para el diálogo de advertencia de reinicio del paso 3 */
    val showStep3ResetDialog: Boolean = false,
    /** Almacena el modo de entrada pendiente para aplicar tras confirmación */
    val pendingStockEntryMode: CreateProductStockEntryMode? = null,
    /** Almacena el tipo de control pendiente para aplicar tras confirmación */
    val pendingControlType: CreateProductControlType? = null,

    val showLossConfirmDialog: Boolean = false,
    val showLowMarginConfirmDialog: Boolean = false,

    // V20.0: Información útil del producto
    val aiUsageInfo: com.app.administradorfarmadon.ActivityInventario.reference.UsageInfoAiResult? = null,
    val aiUsageInfoFetchedFor: String = "",
    val isFetchingUsageInfo: Boolean = false,

    // V22.0: Estados para guardado de proveedor
    val isSavingSupplier: Boolean = false,
    val supplierSaveSuccess: Boolean = false,
    val isSupplierHighlighting: Boolean = false,

    // V22.1: Estado para el destello de la factura
    val isInvoiceHighlighting: Boolean = false,

    // V31.8: Blindaje de Reconciliación para Packs
    val mismatchJustified: Boolean = false,
    val mismatchReason: String? = null
)

// V22.0: Estado para el efecto visual de selección de proveedor


@Composable
fun CreateProductScreen(
    state: CreateProductState,
    categoryOptions: List<String>,
    smartHint: SmartProductHint?,
    existingLotNumbers: Set<String> = emptySet(),
    nextEnabled: Boolean,
    // Sugerencia de categoria con IA (Gemini).
    categorySuggestionState: com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState = com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState(),
    onSwitchToManualCategory: () -> Unit = {},
    onBackToAiCategory: () -> Unit = {},
    // Escaner OCR de etiqueta (camara + Gemini Vision).
    labelScannerState: com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.UiState = com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.UiState(),
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


    // V18.9: Estado temporal para el efecto de "regresar atrÃ¡s" al limpiar cÃ³digo
    var isReturningToInitial by remember { mutableStateOf(false) }


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

                val guardedOnNext: () -> Unit = {
                    when {
                        state.currentStep == CreateProductStep.RESUMEN -> {
                            onSave()
                        }

                        state.currentStep == CreateProductStep.PRESENTACIONES && hasAnyPresentationLoss(
                            state
                        ) -> {
                            onStateChange(
                                state.copy(showLossConfirmDialog = true)
                            )
                        }

                        state.currentStep == CreateProductStep.PRESENTACIONES && hasAnyPresentationLowMargin(
                            state
                        ) -> {
                            onStateChange(
                                state.copy(showLowMarginConfirmDialog = true)
                            )
                        }

                        else -> {
                            onNext()
                        }
                    }
                }

                if (isTablet) {
                    CreateProductTabletSidebarLayout(
                        state = state,
                        categoryOptions = categoryOptions,
                        existingLotNumbers = existingLotNumbers,
                        saveEnabled = nextEnabled,
                        onBack = onBack,
                        onStateChange = onStateChange,
                        onNext = guardedOnNext,
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
                    val isAiBarcodeFlow =
                        aiInventoryEnabled && state.currentStep == CreateProductStep.PRODUCTO && !state.forceManualProductEntry && !state.barcodeAiApplied

                    Scaffold(
                        containerColor = CreateBackground, bottomBar = {
                            if (!keyboardVisible && !isAiBarcodeFlow) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // V18.7: NotificaciÃ³n de "Todo listo" sobre el botÃ³n Siguiente


                                    // V19.5: Banner fijo para agregar presentaciones (Paso 3)
                                    // Solo visible en el paso 3 y si no estÃ¡ ya expandido el formulario.


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
                                                    enabled = true,
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = {
                                                        onStateChange(
                                                            state.copy(addPresentationExpanded = true)
                                                        )
                                                    }
                                                ),
                                            color = CreateGreenSoft,
                                            shape = RoundedCornerShape(18.dp),
                                            border = BorderStroke(
                                                1.5.dp,
                                                CreateGreen.copy(alpha = 0.22f)
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    tint = CreateGreen,
                                                    modifier = Modifier.size(22.dp)
                                                )

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Text(
                                                    text = if (state.presentations.isEmpty()) {
                                                        "Agregar primera forma de venta"
                                                    } else {
                                                        "Agregar otra forma de venta"
                                                    },
                                                    color = CreateGreen,
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
                                        onNext = guardedOnNext,
                                        onPrevious = onPrevious,
                                        loading = loading
                                    )
                                }
                            }
                        }) { innerPadding ->
                        ProductStepContent(
                            modifier = Modifier.padding(innerPadding),
                            step = state.currentStep,
                            contentBottomPadding = if (keyboardVisible) 24.dp else 88.dp,
                            onBack = onBack,
                            header = {
                                CreateProductStepHeader(
                                    step = state.currentStep, onBack = onBack
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
                                        state = state, onStateChange = onStateChange
                                    )

                                    CreateProductStep.RESUMEN -> ProductSummaryStep(
                                        state = state, onStateChange = onStateChange
                                    )
                                }
                            })

                        if (state.currentStep == CreateProductStep.PRESENTACIONES && state.addPresentationExpanded) {
                            CreatePresentationDialog(
                                state = state,
                                onStateChange = onStateChange,
                                onSavePresentation = onSavePresentation,
                                onDismiss = { onStateChange(state.copy(addPresentationExpanded = false)) })
                        }
                    }
                }
            }
        }

        // --- DIÁLOGO GLOBAL: NUEVO PROVEEDOR ---
        if (state.showAddSupplierDialog) {
            AddSupplierDialog(
                isSaving = state.isSavingSupplier,
                isSuccess = state.supplierSaveSuccess,
                suppliers = state.suppliers,
                onDismiss = {
                    if (!state.isSavingSupplier) {
                        onStateChange(
                            state.copy(
                                showAddSupplierDialog = false, supplierSaveSuccess = false
                            )
                        )
                    }
                },
                onSave = onSaveSupplier
            )
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
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
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp, vertical = 6.dp
                                    ),
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
                                    onStateChange(
                                        state.copy(
                                            name = correctedName.ifBlank { state.name },
                                            barcodeMismatchDetected = false,
                                            barcodeMismatchOriginalName = null,
                                            errors = state.errors - "barcode" - "name"
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White, contentColor = CreateRed
                                ),
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
                                            onStateChange(state.resetForNewBarcodeScan())

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
                                            onStateChange(state.resetForNewBarcodeScan())
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
                    .padding(24.dp), contentAlignment = Alignment.Center
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
                                    .heightIn(min = 54.dp),
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
                                    .heightIn(min = 54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CreateOrange, contentColor = Color.White
                                )
                            ) {
                                Text("Continuar", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        if (loading || isReturningToInitial) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                FluidBackground()

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White, strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = if (isReturningToInitial) "Regresando al asistente IA..." else "Guardando producto...",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }



        // V30.12: Diálogo de Desglose de Inventario
        if (state.showStockEntryDialog) {
            StockEntryDialog(state = state, onStateChange = onStateChange, onApply = {
                onStateChange(
                    state.copy(
                        showStockEntryDialog = false, stockEntryConfigured = true
                    )
                )
            }, onDismiss = { onStateChange(state.copy(showStockEntryDialog = false)) })
        }

        if (state.showStep3ResetDialog) {
            val isFromStep1 = state.pendingControlType != null

            Step3ResetWarningDialog(
                state = state,
                title = if (isFromStep1) "¡Cambio de Tipo!" else "¡Acción Destructiva!",
                message = if (isFromStep1) "Al cambiar el tipo de control (Unidad/Peso/Líquido), se borrarán todos los datos de inventario y precios que ya configuraste."
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
                })
        }

        // --- DIÁLOGO DE CONFLICTO DE LOTE (V21.10) ---
        if (state.showLotConflictDialog && state.lotConflictProduct != null) {
            val isLotConflict = lotConflictSeverity != 0

            ConflictManagementDialog(
                title = if (isLotConflict) "Lote Duplicado" else "Producto ya Registrado",
                message = if (isLotConflict) "El número de lote '${state.lotNumber}' ya está asignado a otro producto en el inventario."
                else "Hemos detectado que ya existe un producto con este nombre o identificación en el sistema.",
                conflictProduct = state.lotConflictProduct,
                onDismiss = {
                    if (isLotConflict) {
                        onStateChange(
                            state.copy(
                                lotNumber = "", lotScanned = false, showLotConflictDialog = false
                            )
                        )
                    } else {
                        onStateChange(state.copy(showLotConflictDialog = false))
                    }
                },
                onAddStock = { onAddStockToExistingProduct(state.lotConflictProduct!!) })
        }

        if (state.showLossConfirmDialog) {
            LossMarginConfirmDialog(onDismiss = {
                onStateChange(
                    state.copy(showLossConfirmDialog = false)
                )
            }, onConfirm = {
                onStateChange(
                    state.copy(showLossConfirmDialog = false)
                )
                onNext()
            })
        }

        if (state.showLowMarginConfirmDialog) {
            LowMarginConfirmDialog(onDismiss = {
                onStateChange(
                    state.copy(showLowMarginConfirmDialog = false)
                )
            }, onConfirm = {
                onStateChange(
                    state.copy(showLowMarginConfirmDialog = false)
                )
                onNext()
            })
        }
    }
}

@Composable
private fun LowMarginConfirmDialog(
    onDismiss: () -> Unit, onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.42f))
                .padding(horizontal = 24.dp), contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 430.dp),
                color = Color.White,
                shape = RoundedCornerShape(30.dp),
                shadowElevation = 18.dp,
                border = BorderStroke(
                    1.dp, CreateOrange.copy(alpha = 0.22f)
                )
            ) {
                Box(
                    modifier = Modifier.background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White,
                                CreateOrangeSoft.copy(alpha = 0.72f),
                                CreateYellowSoft.copy(alpha = 0.36f)
                            )
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            color = CreateOrangeSoft,
                            shape = CircleShape,
                            border = BorderStroke(
                                1.dp, CreateOrange.copy(alpha = 0.24f)
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = CreateOrange,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Text(
                            text = "Margen muy bajo",
                            color = CreateTextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Una o más presentaciones tienen poca ganancia. No estás perdiendo dinero, pero el margen está por debajo del 10%.",
                            color = CreateTextSecondary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center
                        )

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White.copy(alpha = 0.78f),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(
                                1.dp, CreateOrange.copy(alpha = 0.16f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 14.dp, vertical = 12.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AttachMoney,
                                    contentDescription = null,
                                    tint = CreateOrange,
                                    modifier = Modifier.size(20.dp)
                                )

                                Text(
                                    text = "Recomendado: sube un poco el precio si quieres una ganancia más saludable.",
                                    color = CreateTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 54.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, CreateBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.70f),
                                    contentColor = CreateTextSecondary
                                )
                            ) {
                                Text(
                                    text = "Revisar precio", fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = onConfirm,
                                modifier = Modifier
                                    .weight(1.15f)
                                    .heightIn(min = 54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CreateOrange, contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 7.dp, pressedElevation = 2.dp
                                )
                            ) {
                                Text(
                                    text = "Continuar igual", fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LossMarginConfirmDialog(
    onDismiss: () -> Unit, onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.48f))
                .padding(horizontal = 24.dp), contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 430.dp),
                color = Color.White,
                shape = RoundedCornerShape(30.dp),
                shadowElevation = 18.dp,
                border = BorderStroke(
                    1.dp, CreateRed.copy(alpha = 0.18f)
                )
            ) {
                Box(
                    modifier = Modifier.background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White,
                                CreateRedSoft.copy(alpha = 0.72f),
                                CreateOrangeSoft.copy(alpha = 0.42f)
                            )
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            color = CreateRedSoft,
                            shape = CircleShape,
                            border = BorderStroke(
                                1.dp, CreateRed.copy(alpha = 0.20f)
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = CreateRed,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Text(
                            text = "Precio con pérdida",
                            color = CreateTextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Una o más presentaciones tienen un precio menor al costo. Puedes corregir el precio o continuar si es una decisión intencional.",
                            color = CreateTextSecondary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center
                        )

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White.copy(alpha = 0.78f),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(
                                1.dp, CreateRed.copy(alpha = 0.14f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 14.dp, vertical = 12.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AttachMoney,
                                    contentDescription = null,
                                    tint = CreateRed,
                                    modifier = Modifier.size(20.dp)
                                )

                                Text(
                                    text = "Recomendado: revisa el precio antes de guardar.",
                                    color = CreateTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 54.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, CreateBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.70f),
                                    contentColor = CreateTextSecondary
                                )
                            ) {
                                Text(
                                    text = "Corregir", fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = onConfirm,
                                modifier = Modifier
                                    .weight(1.15f)
                                    .heightIn(min = 54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CreateRed, contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 7.dp, pressedElevation = 2.dp
                                )
                            ) {
                                Text(
                                    text = "Continuar igual", fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
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
        onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(horizontal = 24.dp)
                .imePadding(), contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // ENCABEZADO DE ADVERTENCIA PREMIUM
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(WarningGradient)
                            .padding(24.dp), contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                color = Color.White.copy(alpha = 0.25f),
                                shape = CircleShape,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.PriorityHigh,
                                        null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
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
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Inventory2,
                                            null,
                                            tint = CreateOrange,
                                            modifier = Modifier.size(24.dp)
                                        )
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 54.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CreateOrange)
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AÑADIR STOCK AL EXISTENTE", fontWeight = FontWeight.Black)
                            }

                            TextButton(
                                onClick = onDismiss, modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "CORREGIR NOMBRE / CANCELAR",
                                    color = CreateRed,
                                    fontWeight = FontWeight.Bold
                                )
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
        onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 32.dp)
                .imePadding(), contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 12.dp,
                border = BorderStroke(2.dp, CreateRed.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
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

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 56.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("Cancelar", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onConfirm,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White, contentColor = CreateRed
                                )
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
fun CreatePresentationDialog(
    state: CreateProductState,
    onStateChange: (CreateProductState) -> Unit,
    onSavePresentation: (SavedPresentation) -> Unit = {},
    onDismiss: () -> Unit
) {
    val quantityPerSale = presentationDialogNumber(
        state.draftPresentationCustomAmount
    )

    val quantityIsValid =
        quantityPerSale > 0.0 && quantityPerSale % 1.0 == 0.0

    val unitContentBase = unitContentBaseFromState(state)

    val inputInBase = when (state.controlType) {
        CreateProductControlType.LIQUIDO,
        CreateProductControlType.PESO -> {
            quantityPerSale * unitContentBase
        }

        CreateProductControlType.UNIDAD -> {
            quantityPerSale
        }

        null -> 0.0
    }

    val itemUnitName = itemUnitNameForPresentation(state)
    val baseUnitLabel = baseUnitLabelForPresentation(state)

    val cleanQuantity = formatCreateProductNumber(quantityPerSale)

    val suggestedPresentationName = when {
        state.draftPresentationName.isNotBlank() -> {
            state.draftPresentationName.trim()
        }

        quantityPerSale == 1.0 -> {
            when (itemUnitName) {
                "frascos" -> "Frasco"
                "envases" -> "Envase"
                "unidades" -> "Unidad"
                else -> itemUnitName.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
            }
        }

        else -> {
            "Pack x$cleanQuantity"
        }
    }

    val normalizedDraftName = normalizePresentationNameForDuplicate(
        suggestedPresentationName
    )

    val nameAlreadyExists =
        normalizedDraftName.isNotBlank() &&
                state.presentations.any {
                    normalizePresentationNameForDuplicate(it.name) == normalizedDraftName
                }

    val equivalenceAlreadyExists =
        inputInBase > 0.0 &&
                state.presentations.any {
                    kotlin.math.abs(
                        parsePresentationDouble(it.equivalenceText) - inputInBase
                    ) < 0.0001
                }

    val priceValue = state.draftPresentationSalePriceText
        .trim()
        .replace(",", ".")
        .toDoubleOrNull() ?: 0.0

    val canAdd =
        quantityIsValid &&
                inputInBase > 0.0 &&
                priceValue > 0.0 &&
                !nameAlreadyExists

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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WaterGradient)
                ) {
                    val configuration = LocalConfiguration.current
                    val maxHeight = configuration.screenHeightDp.dp * 0.7f

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
                            AppTextField(
                                label = "NOMBRE (EJ. UNIDAD, PACK X2, SIX-PACK)",
                                value = state.draftPresentationName,
                                placeholder = "Escribe el nombre...",
                                error = if (nameAlreadyExists) {
                                    "Ya existe una presentación con ese nombre."
                                } else {
                                    state.errors["draftPresentationName"]
                                },
                                onValueChange = {
                                    onStateChange(
                                        state.copy(
                                            draftPresentationName = it,
                                            errors = state.errors - "draftPresentationName"
                                        )
                                    )
                                }
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = itemUnitName.uppercase(Locale.getDefault()) + " POR VENTA",
                                    color = CreateTextSecondary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp
                                )

                                AppTextField(
                                    label = "",
                                    value = state.draftPresentationCustomAmount,
                                    placeholder = "Ej. 1, 2, 6",
                                    keyboardType = KeyboardType.Decimal,
                                    error = if (
                                        state.draftPresentationCustomAmount.isNotBlank() &&
                                        !quantityIsValid
                                    ) {
                                        "Cantidad inválida. Usa números enteros: 1, 2, 6..."
                                    } else {
                                        null
                                    },
                                    onValueChange = {
                                        onStateChange(
                                            state.copy(
                                                draftPresentationCustomAmount = it
                                            )
                                        )
                                    }
                                )
                            }

                            val monedaSimbolo =
                                com.app.administradorfarmadon.ClasesDatabase.SessionManager
                                    .monedaSimbolo
                                    .trim()

                            AppTextField(
                                label = if (monedaSimbolo.isNotBlank()) {
                                    "PRECIO DE VENTA ($monedaSimbolo)"
                                } else {
                                    "PRECIO DE VENTA"
                                },
                                value = state.draftPresentationSalePriceText,
                                placeholder = "Ej. 45.50",
                                keyboardType = KeyboardType.Decimal,
                                leadingIcon = Icons.Outlined.AttachMoney,
                                onValueChange = {
                                    onStateChange(
                                        state.copy(
                                            draftPresentationSalePriceText = it
                                        )
                                    )
                                }
                            )
                        }

                        if (!nameAlreadyExists && equivalenceAlreadyExists) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = CreateOrangeSoft.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    1.dp,
                                    CreateOrange.copy(alpha = 0.18f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 14.dp,
                                        vertical = 12.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = CreateOrange,
                                        modifier = Modifier.size(18.dp)
                                    )

                                    Text(
                                        text = "Ya existe una presentación con la misma equivalencia. Puedes agregarla si tendrá otro nombre o precio.",
                                        color = CreateTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.6f)
                            )
                        ) {
                            Text(
                                text = when (state.controlType) {
                                    CreateProductControlType.LIQUIDO,
                                    CreateProductControlType.PESO -> {
                                        if (quantityPerSale <= 0.0 || unitContentBase <= 0.0) {
                                            "Indica cuántos $itemUnitName se venderán en esta presentación."
                                        } else {
                                            "${formatCreateProductNumber(quantityPerSale)} $itemUnitName × " +
                                                    "${formatCreateProductNumber(unitContentBase)} $baseUnitLabel = " +
                                                    "${formatCreateProductNumber(inputInBase)} $baseUnitLabel por venta"
                                        }
                                    }

                                    CreateProductControlType.UNIDAD -> {
                                        if (quantityPerSale <= 0.0) {
                                            "Indica cuántas unidades se venderán en esta presentación."
                                        } else {
                                            "Descontará ${formatCreateProductNumber(inputInBase)} $baseUnitLabel por venta"
                                        }
                                    }

                                    null -> {
                                        "Configura la cantidad que se descontará al vender."
                                    }
                                },
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
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 54.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = "Cancelar",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    val draftName = suggestedPresentationName

                                    val equivalence = formatCreateProductNumber(
                                        inputInBase
                                    )

                                    val newPresentation = CreateProductPresentation(
                                        id = draftName
                                            .lowercase(Locale.getDefault())
                                            .replace(" ", "_") +
                                                "_" +
                                                java.util.UUID.randomUUID()
                                                    .toString()
                                                    .take(4),
                                        name = draftName,
                                        equivalenceText = equivalence,
                                        salePriceText = state.draftPresentationSalePriceText,
                                        isAiSuggested = false
                                    )

                                    val duplicatedName = state.presentations.any {
                                        normalizePresentationNameForDuplicate(it.name) ==
                                                normalizePresentationNameForDuplicate(
                                                    newPresentation.name
                                                )
                                    }

                                    if (duplicatedName) {
                                        onStateChange(
                                            state.copy(
                                                errors = state.errors + (
                                                        "draftPresentationName" to
                                                                "Ya existe una presentación con ese nombre."
                                                        )
                                            )
                                        )
                                        return@Button
                                    }

                                    val nextList =
                                        state.presentations + newPresentation

                                    onStateChange(
                                        state.copy(
                                            presentations = nextList,
                                            mainPresentationId = state.mainPresentationId
                                                .ifBlank {
                                                    newPresentation.id
                                                },
                                            addPresentationExpanded = false,
                                            draftPresentationName = "",
                                            draftPresentationCustomMode = false,
                                            draftPresentationCustomAmount = "",
                                            draftPresentationCustomUnit = "",
                                            draftPresentationSalePriceText = "",
                                            errors = state.errors -
                                                    "draftPresentationName" -
                                                    "presentations" -
                                                    "presentations_total" -
                                                    "mainPresentation"
                                        )
                                    )
                                },
                                enabled = canAdd,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .heightIn(min = 54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CreateGreen
                                )
                            ) {
                                Text(
                                    text = "Agregar",
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


private fun presentationDialogNumber(raw: String): Double {
    return raw
        .trim()
        .replace(",", ".")
        .toDoubleOrNull() ?: 0.0
}

private fun unitContentBaseFromState(state: CreateProductState): Double {
    val rawContent = presentationDialogNumber(state.unitsPerItemText)

    val unit = state.stockEntryUnit
        .trim()
        .lowercase(Locale.getDefault())
        .replace(".", "")

    return when {
        state.controlType == CreateProductControlType.PESO &&
                unit in setOf("kg", "kilo", "kilos", "kilogramo", "kilogramos") -> {
            rawContent * 1000.0
        }

        state.controlType == CreateProductControlType.LIQUIDO &&
                unit in setOf("l", "lt", "lts", "litro", "litros") -> {
            rawContent * 1000.0
        }

        else -> rawContent
    }
}

private fun itemUnitNameForPresentation(state: CreateProductState): String {
    return when (state.controlType) {
        CreateProductControlType.LIQUIDO -> "envases"
        CreateProductControlType.PESO -> "envases"
        CreateProductControlType.UNIDAD -> "unidades"
        null -> "unidades"
    }
}

private fun baseUnitLabelForPresentation(state: CreateProductState): String {
    return when (state.controlType) {
        CreateProductControlType.LIQUIDO -> "mL"
        CreateProductControlType.PESO -> "g"
        CreateProductControlType.UNIDAD -> "unidades"
        null -> ""
    }
}


private fun presentationQuantityFromEquivalence(
    state: CreateProductState,
    presentation: CreateProductPresentation
): Double {
    val equivalenceBase = parsePresentationDouble(presentation.equivalenceText)
    if (equivalenceBase <= 0.0) return 0.0

    return when (state.controlType) {
        CreateProductControlType.LIQUIDO,
        CreateProductControlType.PESO -> {
            val unitContentBase = unitContentBaseFromState(state)
            if (unitContentBase <= 0.0) 0.0 else equivalenceBase / unitContentBase
        }

        CreateProductControlType.UNIDAD -> {
            equivalenceBase
        }

        null -> 0.0
    }
}

private fun formatPresentationQuantity(value: Double): String {
    val rounded = kotlin.math.round(value)
    return if (kotlin.math.abs(value - rounded) < 0.01) {
        formatCreateProductNumber(rounded)
    } else {
        formatCreateProductNumber(value)
    }
}

private fun presentationQuantityBadgeText(
    state: CreateProductState,
    presentation: CreateProductPresentation
): String {
    val quantity = presentationQuantityFromEquivalence(state, presentation)
    if (quantity <= 0.0) return ""

    val itemName = itemUnitNameForPresentation(state)

    return "${formatPresentationQuantity(quantity)} $itemName"
}

private fun presentationBreakdownText(
    state: CreateProductState,
    presentation: CreateProductPresentation
): String {
    val quantity = presentationQuantityFromEquivalence(state, presentation)
    val equivalenceBase = parsePresentationDouble(presentation.equivalenceText)

    if (quantity <= 0.0 || equivalenceBase <= 0.0) return ""

    val itemName = itemUnitNameForPresentation(state)
    val baseUnit = baseUnitLabelForPresentation(state)

    return when (state.controlType) {
        CreateProductControlType.LIQUIDO,
        CreateProductControlType.PESO -> {
            val unitContentBase = unitContentBaseFromState(state)
            if (unitContentBase <= 0.0) {
                "Descuenta ${formatCreateProductNumber(equivalenceBase)} $baseUnit por venta"
            } else {
                "${formatPresentationQuantity(quantity)} $itemName × " +
                        "${formatCreateProductNumber(unitContentBase)} $baseUnit = " +
                        "${formatCreateProductNumber(equivalenceBase)} $baseUnit por venta"
            }
        }

        CreateProductControlType.UNIDAD -> {
            "Descuenta ${formatPresentationQuantity(quantity)} unidades por venta"
        }

        null -> ""
    }
}

fun buildPresentationPublicLabel(
    state: CreateProductState,
    presentation: CreateProductPresentation
): String {
    val badge = presentationQuantityBadgeText(state, presentation)
    return if (badge.isBlank()) {
        presentation.name
    } else {
        "${presentation.name} ($badge)"
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
    categorySuggestionState: com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState = com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState(),
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
        containerColor = CreateBackground, bottomBar = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 28.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isFirst) {
                        OutlinedButton(
                            onClick = onPrevious,
                            modifier = Modifier.weight(1f),
                            enabled = !loading,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CreateBorder)
                        ) {
                            Text(
                                text = "Atras",
                                color = CreateTextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Button(
                        onClick = if (isLast) onSave else onNext,
                        modifier = Modifier.weight(if (isFirst) 1f else 2f),
                        enabled = saveEnabled && !loading,
                        shape = RoundedCornerShape(16.dp),
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
                            Text(
                                text = if (isLast) "Guardar producto" else "Siguiente",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }) { innerPadding ->
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
                            })
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
                            state = state, onStateChange = onStateChange
                        )

                        CreateProductStep.RESUMEN -> ProductSummaryStep(
                            state = state, onStateChange = onStateChange
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
    step: CreateProductStep, isCurrent: Boolean, isCompleted: Boolean, onClick: () -> Unit
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
            .clickable { onClick() }) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(28.dp), shape = CircleShape, color = numberBg
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
    step: CreateProductStep, onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            // Título animado
            AnimatedContent(
                targetState = step.title, modifier = Modifier.weight(1f), transitionSpec = {
                    (fadeIn() + slideInVertically { it / 2 }).togetherWith(fadeOut() + slideOutVertically { -it / 2 })
                }, label = "header_title_anim"
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
            modifier = Modifier.padding(start = 0.dp, top = 2.dp),
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

@Suppress("UNUSED_PARAMETER")
@Composable
private fun AiBarcodeProductStep(
    state: CreateProductState,
    onStateChange: (CreateProductState) -> Unit,
    onRequestBarcodeScan: () -> Unit,
    onAddStockToExistingProduct: (MoldeProductos) -> Unit,
    onIdentificarBarcode: (String, String?) -> Unit,
    isCheckingBarcodeRemote: Boolean,
    onNext: () -> Unit,
    onRequestLabelScan: () -> Unit,
    onClearBarcodeIntegrityConflict: () -> Unit = {}
) {


    val scope = rememberCoroutineScope()

    var isApplyingBarcodeResult by remember {
        mutableStateOf(false)
    }

    var showBarcodeLocationDialog by remember {
        mutableStateOf(false)
    }

    var pendingBarcodeApplyState by remember {
        mutableStateOf<CreateProductState?>(null)
    }

    val result = state.barcodeAiResult
    val hasDuplicateProduct = state.duplicateProductFound != null

    val resetAndScanAgain = {
        onClearBarcodeIntegrityConflict()
        onStateChange(state.resetForNewBarcodeScan())
        onRequestBarcodeScan()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                hasDuplicateProduct -> {
                    state.duplicateProductFound?.let { producto ->
                        DuplicateProductCard(
                            producto = producto, onAddStock = {
                                onAddStockToExistingProduct(producto)
                            })

                        OutlinedButton(
                            onClick = resetAndScanAgain,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 54.dp),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(
                                1.dp, CreateBorderStrong.copy(alpha = 0.72f)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White, contentColor = CreateTextSecondary
                            )
                        ) {
                            Text(
                                text = "Escanear otro producto",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                state.isIdentifyingBarcode || isCheckingBarcodeRemote -> {
                    IdentifyingBarcodeCard(
                        barcode = state.barcode
                    )
                }

                result != null -> {
                    if (result.estado == "IDENTIFICADO") {
                        BarcodeIdentifiedCard(
                            result = result, onApply = {
                                val mappedType = when (result.tipoControl) {
                                    "UNIDAD" -> CreateProductControlType.UNIDAD
                                    "PESO" -> CreateProductControlType.PESO
                                    "LIQUIDO" -> CreateProductControlType.LIQUIDO
                                    else -> null
                                }

                                isApplyingBarcodeResult = true

                                scope.launch {
                                    if (mappedType != null) {
                                        delay(650)

                                        val rawSuggestedValue =
                                            result.sugerenciaContenidoValor?.takeIf { it.isNotBlank() }

                                        val rawSuggestedUnit =
                                            result.sugerenciaContenidoUnidad?.takeIf { it.isNotBlank() }

                                        val safeSuggestedValue = safeInventoryContentValue(
                                            productName = result.nombre.orEmpty(),
                                            controlType = mappedType,
                                            value = rawSuggestedValue,
                                            unit = rawSuggestedUnit
                                        ).takeIf { it.isNotBlank() }

                                        val safeSuggestedUnit = safeInventoryContentUnit(
                                            productName = result.nombre.orEmpty(),
                                            controlType = mappedType,
                                            value = rawSuggestedValue,
                                            unit = rawSuggestedUnit
                                        ).takeIf { it.isNotBlank() }

                                        val rawSuggestedMultiplier =
                                            result.sugerenciaMultiplicador?.takeIf { it.isNotBlank() }
                                        val hasSuggestedContent =
                                            safeSuggestedValue != null && safeSuggestedUnit != null

                                        // V31.9: Normalización robusta centralizada
                                        val normalizedMultiplier =
                                            parseAiMultiplier(rawSuggestedMultiplier)
                                        val isPack = normalizedMultiplier > 1.0

                                        pendingBarcodeApplyState = state.copy(
                                            name = result.nombre.orEmpty(),
                                            category = result.categoria.orEmpty(),
                                            controlType = mappedType,
                                            requiresPrescription = result.requiereReceta,
                                            barcode = result.codigo.ifBlank { state.barcode },
                                            barcodeAiApplied = true,

                                            defaultSalePresentationName = result.presentacionVentaDefault.orEmpty(),
                                            defaultSalePresentationFromAi = !result.presentacionVentaDefault.isNullOrBlank() && result.confianzaPresentacion >= 80,
                                            saleByFractionSuggested = result.ventaFraccionadaPermitida,
                                            defaultSalePresentationConfidence = result.confianzaPresentacion,
                                            defaultSalePresentationReason = result.razonPresentacion.orEmpty(),

                                            // V31.8: Pre-llenado inteligente de Cantidad y Contenido (Paso 2)
                                            unitsPerItemText = safeSuggestedValue.orEmpty(),
                                            stockEntryUnit = safeSuggestedUnit.orEmpty(),

                                            // Si es Pack (ej: 6 latas), sugerimos modo CAJA; si es individual, UNIDAD.
                                            stockEntryMode = if (isPack) CreateProductStockEntryMode.CAJA
                                            else if (hasSuggestedContent) CreateProductStockEntryMode.UNIDAD
                                            else null,

                                            unitsPerBoxText = if (isPack) formatCreateProductNumber(
                                                normalizedMultiplier
                                            ) else "",
                                            receivedUnitsText = if (!isPack && hasSuggestedContent) "1" else "",

                                            stockControlMode = mapStockControlModeFromSuggestedUnit(
                                                safeSuggestedUnit
                                            ),
                                            stockEntryConfigured = false,
                                            // Obliga al usuario a poner la cantidad recibida

                                            currentStep = CreateProductStep.PRODUCTO,
                                            errors = state.errors - setOf(
                                                "name",
                                                "category",
                                                "controlType",
                                                "barcode",
                                                "location"
                                            )
                                        )

                                        isApplyingBarcodeResult = false
                                        showBarcodeLocationDialog = true
                                    } else {
                                        delay(650)

                                        val rawSuggestedValue =
                                            result.sugerenciaContenidoValor?.takeIf { it.isNotBlank() }
                                        val rawSuggestedUnit =
                                            result.sugerenciaContenidoUnidad?.takeIf { it.isNotBlank() }
                                        val rawSuggestedMultiplier =
                                            result.sugerenciaMultiplicador?.takeIf { it.isNotBlank() }

                                        val safeSuggestedValue = safeInventoryContentValue(
                                            productName = result.nombre.orEmpty(),
                                            controlType = null, // mappedType is not available in this scope, assume null for initial check
                                            value = rawSuggestedValue,
                                            unit = rawSuggestedUnit
                                        ).takeIf { it.isNotBlank() }

                                        val safeSuggestedUnit = safeInventoryContentUnit(
                                            productName = result.nombre.orEmpty(),
                                            controlType = null, // mappedType is not available in this scope, assume null for initial check
                                            value = rawSuggestedValue,
                                            unit = rawSuggestedUnit
                                        ).takeIf { it.isNotBlank() }


                                        val hasSuggestedContent =
                                            safeSuggestedValue != null && safeSuggestedUnit != null

                                        onStateChange(
                                            state.copy(
                                                name = result.nombre.orEmpty(),
                                                category = result.categoria.orEmpty(),
                                                requiresPrescription = result.requiereReceta,
                                                forceManualProductEntry = true,
                                                barcodeAiApplied = false,
                                                defaultSalePresentationName = "",
                                                defaultSalePresentationFromAi = false,
                                                saleByFractionSuggested = null,
                                                defaultSalePresentationConfidence = 0,
                                                defaultSalePresentationReason = "",

                                                // Pre-llenado incluso en modo manual/incompleto
                                                unitsPerItemText = safeSuggestedValue.orEmpty(),
                                                stockEntryUnit = safeSuggestedUnit.orEmpty(),
                                                stockEntryMode = if (hasSuggestedContent) CreateProductStockEntryMode.UNIDAD else null,
                                                stockControlMode = mapStockControlModeFromSuggestedUnit(
                                                    safeSuggestedUnit
                                                ),

                                                errors = state.errors + ("controlType" to "La IA detectó el producto. Solo confirma cómo se controla.")
                                            )
                                        )

                                        isApplyingBarcodeResult = false
                                    }
                                }
                            }, onScanOther = resetAndScanAgain,

                            onEditManual = {
                                val mappedType = when (result.tipoControl) {
                                    "UNIDAD" -> CreateProductControlType.UNIDAD
                                    "PESO" -> CreateProductControlType.PESO
                                    "LIQUIDO" -> CreateProductControlType.LIQUIDO
                                    else -> null
                                }

                                val rawSuggestedValue =
                                    result.sugerenciaContenidoValor?.takeIf { it.isNotBlank() }

                                val rawSuggestedUnit =
                                    result.sugerenciaContenidoUnidad?.takeIf { it.isNotBlank() }

                                val safeSuggestedValue = safeInventoryContentValue(
                                    productName = result.nombre.orEmpty(),
                                    controlType = mappedType,
                                    value = rawSuggestedValue,
                                    unit = rawSuggestedUnit
                                ).takeIf { it.isNotBlank() }

                                val safeSuggestedUnit = safeInventoryContentUnit(
                                    productName = result.nombre.orEmpty(),
                                    controlType = mappedType,
                                    value = rawSuggestedValue,
                                    unit = rawSuggestedUnit
                                ).takeIf { it.isNotBlank() }

                                val hasSafeContent =
                                    safeSuggestedValue != null && safeSuggestedUnit != null

                                onStateChange(
                                    state.copy(
                                        name = result.nombre.orEmpty(),
                                        category = result.categoria.orEmpty(),
                                        controlType = mappedType,
                                        requiresPrescription = result.requiereReceta,
                                        forceManualProductEntry = true,
                                        barcodeAiApplied = false,

                                        defaultSalePresentationName = "",
                                        defaultSalePresentationFromAi = false,
                                        saleByFractionSuggested = null,
                                        defaultSalePresentationConfidence = 0,
                                        defaultSalePresentationReason = "",

                                        // V31.96: Pre-llenado seguro también en modo manual.
                                        // Evita usar dosis farmacológica como inventario físico.
                                        unitsPerItemText = safeSuggestedValue.orEmpty(),
                                        stockEntryUnit = safeSuggestedUnit.orEmpty(),
                                        stockEntryMode = if (hasSafeContent) {
                                            CreateProductStockEntryMode.UNIDAD
                                        } else {
                                            null
                                        },
                                        stockControlMode = mapStockControlModeFromSuggestedUnit(
                                            safeSuggestedUnit
                                        ),

                                        mismatchJustified = false,
                                        mismatchReason = null,

                                        errors = state.errors - setOf(
                                            "name",
                                            "category",
                                            "barcode"
                                        )
                                    )
                                )
                            })

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = CreateSurfaceSoft.copy(alpha = 0.76f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                1.dp, CreateBorder.copy(alpha = 0.7f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 12.dp, vertical = 10.dp
                                ),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = CreateTextMuted,
                                    modifier = Modifier.size(17.dp)
                                )

                                val suggestionSourceText = if (result.codigo.isBlank()) {
                                    "Sugerencia generada a partir de la imagen del producto. Puedes editarla antes de guardar."
                                } else {
                                    "Sugerencia generada a partir del código y la etiqueta escaneada. Puedes editarla antes de guardar."
                                }

                                Text(
                                    text = suggestionSourceText,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    } else {
                        BarcodeNotIdentifiedCard(
                            barcode = state.barcode,
                            onScanOther = resetAndScanAgain,
                            onEditManual = {
                                onStateChange(
                                    state.copy(
                                        forceManualProductEntry = true,
                                        barcodeAiApplied = false,
                                        defaultSalePresentationName = "",
                                        defaultSalePresentationFromAi = false,
                                        saleByFractionSuggested = null,
                                        defaultSalePresentationConfidence = 0,
                                        defaultSalePresentationReason = ""
                                    )
                                )
                            })
                    }
                }

                state.barcode.isBlank() -> {
                    InitialScanCard(
                        onStartScan = {
                            onClearBarcodeIntegrityConflict()
                            onStateChange(state.resetForNewBarcodeScan())
                            onRequestBarcodeScan()
                        })
                }

                state.errors["barcode"] != null -> {
                    BarcodeValidationErrorCard(
                        message = state.errors["barcode"].orEmpty(),
                        onRetry = resetAndScanAgain,
                        onAdjust = {
                            onStateChange(
                                state.copy(
                                    forceManualProductEntry = true,
                                    barcodeAiApplied = false,
                                    defaultSalePresentationName = "",
                                    defaultSalePresentationFromAi = false,
                                    saleByFractionSuggested = null,
                                    defaultSalePresentationConfidence = 0,
                                    defaultSalePresentationReason = "",
                                    errors = state.errors - "barcode"
                                )
                            )
                        })
                }

                else -> {
                    IdentifyingBarcodeCard(
                        barcode = state.barcode
                    )
                }
            }
        }

        if (isApplyingBarcodeResult) {
            Dialog(
                onDismissRequest = {}, properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.66f))
                        .padding(horizontal = 30.dp), contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 420.dp),
                        color = Color.White,
                        shape = RoundedCornerShape(32.dp),
                        shadowElevation = 24.dp,
                        tonalElevation = 8.dp
                    ) {
                        Box(
                            modifier = Modifier.background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White,
                                        CreateGreenUltraSoft,
                                        CreateBlueSoft.copy(alpha = 0.42f)
                                    )
                                )
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = 28.dp, vertical = 30.dp
                                ),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(72.dp),
                                    color = CreateGreenSoft,
                                    shape = CircleShape,
                                    border = BorderStroke(
                                        1.dp, CreateGreen.copy(alpha = 0.22f)
                                    )
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(
                                            color = CreateGreen,
                                            strokeWidth = 3.dp,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Aplicando datos",
                                    color = CreateTextPrimary,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "Preparando lote inicial del producto",
                                    color = CreateTextSecondary,
                                    fontSize = 14.sp,
                                    lineHeight = 19.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showBarcodeLocationDialog && pendingBarcodeApplyState != null) {
            BarcodeLocationQuickDialog(
                productName = pendingBarcodeApplyState?.name.orEmpty(),
                initialLocation = pendingBarcodeApplyState?.location.orEmpty(),
                onDismiss = {
                    showBarcodeLocationDialog = false
                    pendingBarcodeApplyState = null
                },
                onConfirm = { location ->
                    val finalState = pendingBarcodeApplyState ?: return@BarcodeLocationQuickDialog

                    onStateChange(
                        finalState.copy(
                            location = location.trim(),
                            currentStep = CreateProductStep.LOTE_INICIAL,
                            errors = finalState.errors - "location"
                        )
                    )

                    showBarcodeLocationDialog = false
                    pendingBarcodeApplyState = null
                })
        }

    }
}


@Composable
private fun BarcodeLocationQuickDialog(
    productName: String, initialLocation: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit
) {
    var location by remember(initialLocation) {
        mutableStateOf(initialLocation)
    }

    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.62f))
                .padding(horizontal = 24.dp)
                .imePadding(), contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 430.dp),
                color = Color.White,
                shape = RoundedCornerShape(32.dp),
                shadowElevation = 22.dp,
                tonalElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier.background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White,
                                CreateGreenUltraSoft,
                                CreateBlueSoft.copy(alpha = 0.38f)
                            )
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(66.dp),
                            color = CreateGreenSoft,
                            shape = CircleShape,
                            border = BorderStroke(
                                1.dp, CreateGreen.copy(alpha = 0.22f)
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = CreateGreen,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Ubicación del producto",
                                color = CreateTextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = productName.ifBlank { "Producto identificado" },
                                color = CreateTextSecondary,
                                fontSize = 14.sp,
                                lineHeight = 19.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(
                                1.dp, CreateBorder.copy(alpha = 0.85f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "¿Dónde estará guardado?",
                                    color = CreateTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )

                                AppTextField(
                                    label = "",
                                    value = location,
                                    placeholder = "Ej: Estante B-3, Vitrina 1...",
                                    isSuccess = location.trim().isNotBlank(),
                                    error = null,
                                    onValueChange = { location = it })

                                Text(
                                    text = "Esto ayuda a encontrar rápido el producto al vender o reponer stock.",
                                    color = CreateTextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 54.dp),
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.dp, CreateBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.70f),
                                    contentColor = CreateTextSecondary
                                )
                            ) {
                                Text(
                                    text = "Volver", fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    onConfirm(location)
                                },
                                enabled = location.trim().isNotBlank(),
                                modifier = Modifier
                                    .weight(1.35f)
                                    .heightIn(min = 54.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CreateGreenDark,
                                    contentColor = Color.White,
                                    disabledContainerColor = CreateBorder,
                                    disabledContentColor = CreateTextSecondary.copy(alpha = 0.55f)
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 7.dp, pressedElevation = 2.dp
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Continuar", fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
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
            modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally
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
private fun IdentifyingBarcodeCard(barcode: String) {
    var stage by remember(barcode) { mutableStateOf(0) }

    LaunchedEffect(barcode) {
        stage = 0
        delay(700)
        stage = 1
        delay(1100)
        stage = 2
    }

    val title = when (stage) {
        0 -> "Verificando código"
        1 -> "Consultando inventario"
        else -> "Identificando con IA"
    }

    val subtitle = when (stage) {
        0 -> "Revisando si este código ya existe."
        1 -> "Buscando coincidencias en tu inventario."
        else -> "Analizando el código y la etiqueta escaneada."
    }

    val infiniteTransition = rememberInfiniteTransition(label = "barcode_loading")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f, targetValue = 1.06f, animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(34.dp),
        shadowElevation = 16.dp,
        tonalElevation = 6.dp,
        border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.14f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White, CreateGreenUltraSoft, CreateBlueSoft.copy(alpha = 0.32f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(76.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        },
                    color = CreateGreenSoft,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.22f)),
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(38.dp), color = CreateGreen, strokeWidth = 3.dp
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        color = CreateTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = subtitle,
                        color = CreateTextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Surface(
                    color = Color.White.copy(alpha = 0.76f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, CreateBorder.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.QrCodeScanner,
                            contentDescription = null,
                            tint = CreateTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = barcode.ifBlank { "Código en análisis" },
                            color = CreateTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.4.sp
                        )
                    }
                }

                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 5.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = CreateGreen,
                    trackColor = CreateGreen.copy(alpha = 0.12f)
                )
            }
        }
    }
}

@Composable
fun BarcodeValidationErrorCard(
    message: String, onRetry: () -> Unit, onAdjust: () -> Unit
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BarcodeIdentifiedCard(
    result: com.app.administradorfarmadon.ActivityInventario.reference.BarcodeAiResult,
    onApply: () -> Unit,
    onScanOther: () -> Unit,
    onEditManual: () -> Unit
) {
    val productName = result.nombre.orEmpty().ifBlank { "Producto detectado" }
    val barcode = result.codigo.ifBlank { "Código no disponible" }
    val category = result.categoria.orEmpty().ifBlank { "Sin categoría" }
    val control = result.tipoControl.orEmpty().ifBlank { "Pendiente" }
    val prescriptionText = if (result.requiereReceta) "Requiere" else "No requiere"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(34.dp),
        shadowElevation = 18.dp,
        tonalElevation = 6.dp,
        border = BorderStroke(
            width = 1.dp, color = CreateGreen.copy(alpha = 0.16f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White, CreateGreenUltraSoft, CreateBlueSoft.copy(alpha = 0.35f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(62.dp),
                        color = CreateGreenSoft,
                        shape = CircleShape,
                        border = BorderStroke(
                            1.dp, CreateGreen.copy(alpha = 0.20f)
                        ),
                        shadowElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = CreateGreen,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Producto reconocido",
                            color = CreateTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.3).sp
                        )

                        Text(
                            text = "Información detectada por IA",
                            color = CreateTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 17.sp
                        )
                    }

                    Surface(
                        color = CreateAiFocusSoft,
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(
                            1.dp, CreateAiFocus.copy(alpha = 0.35f)
                        )
                    ) {
                        Text(
                            text = "IA",
                            modifier = Modifier.padding(
                                horizontal = 12.dp, vertical = 6.dp
                            ),
                            color = CreateTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(28.dp),
                    shadowElevation = 8.dp,
                    border = BorderStroke(
                        1.dp, CreateBorder.copy(alpha = 0.72f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Surface(
                                color = CreateGreenSoft, shape = RoundedCornerShape(999.dp)
                            ) {
                                Text(
                                    text = "PRODUCTO DETECTADO",
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp, vertical = 4.dp
                                    ),
                                    color = CreateGreenDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp
                                )
                            }

                            Text(
                                text = productName,
                                color = CreateTextPrimary,
                                fontSize = 25.sp,
                                lineHeight = 32.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.6).sp
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.QrCodeScanner,
                                    contentDescription = null,
                                    tint = CreateTextMuted,
                                    modifier = Modifier.size(17.dp)
                                )

                                Text(
                                    text = barcode,
                                    color = CreateTextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.4.sp
                                )
                            }
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PremiumAiResultChip(
                                label = "Categoría",
                                value = category,
                                surfaceColor = CreateGreenSoft,
                                accentColor = CreateGreen
                            )

                            PremiumAiResultChip(
                                label = "Control",
                                value = control,
                                surfaceColor = CreateBlueSoft,
                                accentColor = CreateBlue
                            )

                            PremiumAiResultChip(
                                label = "Receta",
                                value = prescriptionText,
                                surfaceColor = if (result.requiereReceta) {
                                    CreateOrangeSoft
                                } else {
                                    CreateSurfaceSoft
                                },
                                accentColor = if (result.requiereReceta) {
                                    CreateOrange
                                } else {
                                    CreateTextSecondary
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = onApply,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 62.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CreateGreenDark, contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp, pressedElevation = 2.dp
                    )
                ) {
                    Text(
                        text = "Aplicar y continuar",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onScanOther,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 54.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(
                            1.dp, CreateBorderStrong.copy(alpha = 0.75f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.72f),
                            contentColor = CreateTextSecondary
                        )
                    ) {
                        Text(
                            text = "Escanear otro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    OutlinedButton(
                        onClick = onEditManual,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 54.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(
                            1.dp, CreateGreen.copy(alpha = 0.32f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CreateGreenUltraSoft, contentColor = CreateGreenDark
                        )
                    ) {
                        Text(
                            text = "Editar datos",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun PremiumAiResultChip(
    label: String, value: String, surfaceColor: Color, accentColor: Color
) {
    Surface(
        modifier = Modifier
            .widthIn(min = 128.dp)
            .heightIn(min = 70.dp),
        color = surfaceColor.copy(alpha = 0.92f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = 1.dp, color = accentColor.copy(alpha = 0.18f)
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 14.dp, vertical = 12.dp
            ), verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = label.uppercase(),
                color = accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                maxLines = 1
            )

            Text(
                text = value,
                color = CreateTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BarcodeInfoPill(
    label: String, value: String, color: Color, background: Color
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
private fun BarcodeNotIdentifiedCard(
    barcode: String, onScanOther: () -> Unit, onEditManual: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(34.dp),
        shadowElevation = 16.dp,
        tonalElevation = 6.dp,
        border = BorderStroke(1.dp, CreateOrange.copy(alpha = 0.20f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White, CreateOrangeSoft.copy(alpha = 0.62f), CreateSurfaceSoft
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        color = CreateOrangeSoft,
                        shape = CircleShape,
                        border = BorderStroke(
                            1.dp, CreateOrange.copy(alpha = 0.22f)
                        ),
                        shadowElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = CreateOrange,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "No pudimos identificarlo",
                            color = CreateTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            text = "Puedes escanear el código otra vez o ingresar los datos manualmente.",
                            color = CreateTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.82f),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(
                        1.dp, CreateBorder.copy(alpha = 0.72f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "CÓDIGO ESCANEADO",
                            color = CreateTextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.QrCodeScanner,
                                contentDescription = null,
                                tint = CreateTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )

                            Text(
                                text = barcode.ifBlank { "Sin código" },
                                color = CreateTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.4.sp
                            )
                        }
                    }
                }

                Button(
                    onClick = onScanOther,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CreateGreenDark, contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp, pressedElevation = 2.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Escanear código otra vez",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                OutlinedButton(
                    onClick = onEditManual,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, CreateBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.78f),
                        contentColor = CreateTextSecondary
                    )
                ) {
                    Text(
                        text = "Ingresar datos manualmente",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ProductAppliedSummaryCard(
    state: CreateProductState, onEditManual: () -> Unit, onScanOther: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = CreateGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Datos cargados correctamente",
                    fontWeight = FontWeight.SemiBold,
                    color = CreateTextPrimary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                state.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                state.category,
                style = MaterialTheme.typography.bodySmall,
                color = CreateTextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onScanOther,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cambiar", fontSize = 12.sp)
                }
                Button(
                    onClick = onEditManual,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
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
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = CreateTextPrimary
        )
    }
}

@Composable
fun ProductBasicStep(
    state: CreateProductState,
    categoryOptions: List<String>,
    onStateChange: (CreateProductState) -> Unit,
    isSearching: Boolean = false,
    categorySuggestionState: com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState = com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState(),
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
    val locationRequester = remember { BringIntoViewRequester() }

    var isMagicScrolling by remember { mutableStateOf(false) }
    var previouslyMagicTriggered by remember {
        mutableStateOf(state.categorySelectedFromAi && state.controlType != null)
    }

    LaunchedEffect(state.categorySelectedFromAi, state.controlType) {
        val currentTrigger = state.categorySelectedFromAi && state.controlType != null

        if (!currentTrigger) {
            previouslyMagicTriggered = false
            isMagicScrolling = false
            return@LaunchedEffect
        }

        if (!previouslyMagicTriggered) {
            previouslyMagicTriggered = true

            try {
                // 1. Encendemos loading antes de mover la pantalla
                isMagicScrolling = true

                // 2. Esperamos que Compose pinte el tipo seleccionado y la sección ubicación
                delay(250)

                // 3. Bajamos hasta ubicación física
                locationRequester.bringIntoView()

                // 4. Mantenemos loading para evitar parpadeo visual
                delay(1200)

            } finally {
                isMagicScrolling = false
            }
        }
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
    LaunchedEffect(
        state.name,
        state.category,
        state.categorySelectedFromAi,
        state.duplicateProductFound,
        state.errors["name"]
    ) {
        val cleanName = state.name.trim()

        val canSearch =
            cleanName.length >= 5 && state.category.isBlank() && !state.categorySelectedFromAi && state.duplicateProductFound == null && state.errors["name"].isNullOrBlank()

        if (canSearch) {
            // Esperamos a que el usuario deje de escribir.
            // Así no animamos ni movemos layout por cada letra.
            delay(650)

            val stillValid =
                state.name.trim() == cleanName && state.category.isBlank() && !state.categorySelectedFromAi && state.duplicateProductFound == null && state.errors["name"].isNullOrBlank()

            if (stillValid) {
                onSearchIA(false)
            }
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
        state.forceManualProductEntry && aiBarcodeResult?.estado == "IDENTIFICADO" && !state.barcodeAiApplied && !state.typeSelectedManually && state.name.trim()
            .stripAccents().equals(
                aiBarcodeResult.nombre.orEmpty().trim().stripAccents(), ignoreCase = true
            ) && state.category.trim().stripAccents().equals(
            aiBarcodeResult.categoria.orEmpty().trim().stripAccents(), ignoreCase = true
        ) && state.controlType == aiControlType
    val manualAiPrescriptionSuggestion =
        categorySuggestionState.sugerenciaTipoManual?.takeIf { suggestion ->
            state.name.trim().stripAccents().equals(
                suggestion.productName.trim().stripAccents(),
                ignoreCase = true
            ) && state.category.trim().stripAccents()
                .equals(suggestion.category.trim().stripAccents(), ignoreCase = true)
        }?.requiereReceta

    if (aiInventoryEnabled && !state.forceManualProductEntry && !state.barcodeAiApplied) {
        AiBarcodeProductStep(
            state = state,
            onStateChange = onStateChange,
            onRequestBarcodeScan = onRequestBarcodeScan,
            onAddStockToExistingProduct = onAddStockToExistingProduct,
            onIdentificarBarcode = onIdentificarBarcode,
            isCheckingBarcodeRemote = isCheckingBarcodeRemote,
            onNext = onNext,
            onRequestLabelScan = onRequestLabelScan,
            onClearBarcodeIntegrityConflict = onClearBarcodeIntegrityConflict
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
        val isIdentityValid =
            state.name.isNotBlank() && state.category.isNotBlank() && state.controlType != null

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
                            onStateChange(
                                state.copy(
                                    showStep3ResetDialog = true, pendingControlType = controlType
                                )
                            )
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
                    })
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
            state = state, locationRequester = locationRequester, onStateChange = onStateChange
        )
    }

    if (isMagicScrolling) {
        val infiniteTransition = rememberInfiniteTransition(label = "magic_scroll_loading")

        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.94f, targetValue = 1.06f, animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 900, easing = FastOutSlowInEasing
                ), repeatMode = RepeatMode.Reverse
            ), label = "pulse_scale"
        )

        Dialog(
            onDismissRequest = {}, properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.68f))
                    .padding(horizontal = 28.dp), contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp)
                        .wrapContentHeight(),
                    color = Color.White,
                    shape = RoundedCornerShape(32.dp),
                    shadowElevation = 26.dp,
                    tonalElevation = 10.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White,
                                        CreateGreenUltraSoft,
                                        CreateBlueSoft.copy(alpha = 0.55f)
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 28.dp, vertical = 30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(74.dp)
                                    .graphicsLayer {
                                        scaleX = pulseScale
                                        scaleY = pulseScale
                                    },
                                color = CreateGreenSoft,
                                shape = CircleShape,
                                border = BorderStroke(
                                    width = 1.dp, color = CreateGreen.copy(alpha = 0.22f)
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Lightbulb,
                                        contentDescription = null,
                                        tint = CreateGreen,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }

                            Surface(
                                color = CreateAiFocusSoft,
                                shape = RoundedCornerShape(999.dp),
                                border = BorderStroke(
                                    1.dp, CreateAiFocus.copy(alpha = 0.35f)
                                )
                            ) {
                                Text(
                                    text = "IA FARMADON",
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp, vertical = 5.dp
                                    ),
                                    color = CreateTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Configurando producto",
                                    color = CreateTextPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 19.sp,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "Validando receta, tipo de control y ubicación física",
                                    color = CreateTextSecondary,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 5.dp)
                                    .clip(RoundedCornerShape(999.dp)),
                                color = CreateGreen,
                                trackColor = CreateGreen.copy(alpha = 0.12f)
                            )

                            Text(
                                text = "Preparando la pantalla...",
                                color = CreateTextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
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
                onStateChange(state.resetForNewBarcodeScan())
                onRequestBarcodeScan()
            },
            onManualMode = {
                onStateChange(
                    state.copy(
                        isBarcodeManualMode = true, duplicateProductFound = null
                    )
                )
            },
            onBarcodeChange = {
                onStateChange(
                    state.copy(
                        barcode = it,
                        duplicateProductFound = null,
                        errors = state.errors - "barcode"
                    )
                )
            },
            onConfirmManual = { onStateChange(state.copy(isBarcodeManualMode = false)) },
            onClear = {
                onClearBarcodeIntegrityConflict()
                onStateChange(
                    state.copy(
                        barcode = "",
                        barcodeAiResult = null,
                        barcodeAiApplied = false,
                        defaultSalePresentationName = "",
                        defaultSalePresentationFromAi = false,
                        saleByFractionSuggested = null,
                        defaultSalePresentationConfidence = 0,
                        defaultSalePresentationReason = "",
                        forceManualProductEntry = true,
                        isBarcodeManualMode = false,
                        duplicateProductFound = null,
                        errors = state.errors - "barcode"
                    )
                )
            })

        // Tarjeta de duplicado: solo aparece si realmente hay un duplicado
        AnimatedVisibility(visible = hasDuplicateProduct) {
            state.duplicateProductFound?.let { producto ->
                DuplicateProductCard(
                    producto = producto, onAddStock = { onAddStockToExistingProduct(producto) })
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
    // Definimos si el nombre es válido para mostrar el check de éxito
    val isNameValid =
        state.name.isNotBlank() && state.errors["name"] == null && !state.isValidatingNameRemote

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppTextField(
            modifier = Modifier.bringIntoViewRequester(nameRequester),
            label = "",
            value = state.name,
            error = state.errors["name"],
            isSuccess = isNameValid,
            placeholder = "Escribe el nombre o marca...",
            // CAMBIO: El icono de la izquierda solo aparece si el texto está vacío
            leadingIcon = if (state.name.isEmpty()) Icons.AutoMirrored.Outlined.Label else null,
            trailingIcon = if (state.name.length >= 3 && state.errors["name"].isNullOrBlank() && !state.isValidatingNameRemote) {
                {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        null,
                        tint = CreateGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else null,
            onValueChange = { input ->
                if (input.isBlank()) {
                    onStateChange(
                        state.copy(
                            name = "",
                            category = "",
                            controlType = null,
                            requiresPrescription = false,
                            categorySelectedFromAi = false,
                            typeSelectedManually = false,
                            duplicateProductFound = null,
                            isAnalyzingKeywords = false,

                            // IMPORTANTE:
                            // Si el usuario ya está escribiendo manualmente,
                            // no lo regresamos a la pantalla inicial de IA.
                            forceManualProductEntry = true,
                            barcodeAiApplied = false,
                            defaultSalePresentationName = "",
                            defaultSalePresentationFromAi = false,
                            saleByFractionSuggested = null,
                            defaultSalePresentationConfidence = 0,
                            defaultSalePresentationReason = "",

                            errors = state.errors - setOf(
                                "name", "category", "controlType"
                            )
                        )
                    )

                    onClearAsistManual()
                } else {
                    onStateChange(
                        state.copy(
                            name = input,
                            forceManualProductEntry = true,
                            barcodeAiApplied = false,
                            defaultSalePresentationName = "",
                            defaultSalePresentationFromAi = false,
                            saleByFractionSuggested = null,
                            defaultSalePresentationConfidence = 0,
                            defaultSalePresentationReason = "",
                            isAnalyzingKeywords = false,
                            errors = state.errors - "name"
                        )
                    )
                }
            })
    }
}

@Composable
private fun ProductLocationSection(
    state: CreateProductState,
    locationRequester: BringIntoViewRequester,
    onStateChange: (CreateProductState) -> Unit
) {
    val configuration = LocalConfiguration.current

    val isTablet = configuration.smallestScreenWidthDp >= 600

    val scrollComfortSpace = remember(
        configuration.screenHeightDp, configuration.smallestScreenWidthDp
    ) {
        val percent = if (isTablet) 0.20f else 0.14f
        val calculated = (configuration.screenHeightDp * percent).dp

        calculated.coerceIn(
            minimumValue = if (isTablet) 140.dp else 90.dp,
            maximumValue = if (isTablet) 260.dp else 170.dp
        )
    }

    val hasDuplicateProduct = state.duplicateProductFound != null
    val mostrarUbicacion =
        !hasDuplicateProduct && state.name.isNotBlank() && state.category.isNotBlank() && state.controlType != null

    AnimatedVisibility(
        visible = mostrarUbicacion,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column {
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
                            onStateChange(
                                state.copy(
                                    location = it, errors = state.errors - "location"
                                )
                            )
                        })

                    Text(
                        text = "Es obligatorio indicar la ubicación para continuar.",
                        fontSize = 11.sp,
                        color = CreateTextSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(scrollComfortSpace)
                    .bringIntoViewRequester(locationRequester)
            )
        }
    }
}

@Composable
private fun SupplierSelectionSection(
    state: CreateProductState, onStateChange: (CreateProductState) -> Unit
) {
    val hasDuplicateProduct = state.duplicateProductFound != null
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // --- ANIMACIÓN DE DESTELLO (SHIMMER DE SELECCIÓN) ---
    val highlightColor by animateColorAsState(
        targetValue = if (state.isSupplierHighlighting) CreateGreen.copy(alpha = 0.15f) else CreateSurfaceSoft,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing),
        label = "highlight"
    )

    if (!hasDuplicateProduct && state.name.isNotBlank()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "PROVEEDOR",
                fontSize = if (isTablet) 13.sp else 11.sp,
                fontWeight = FontWeight.Black,
                color = CreateTextSecondary,
                letterSpacing = 1.2.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                var expanded by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = highlightColor, // APLICAMOS EL COLOR ANIMADO AQUÍ
                    border = BorderStroke(
                        width = if (state.isSupplierHighlighting) 2.dp else 1.dp,
                        color = if (state.isSupplierHighlighting) CreateGreen else CreateBorder
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .clickable { expanded = true }
                            .padding(
                                horizontal = 16.dp, vertical = if (isTablet) 18.dp else 14.dp
                            )) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (state.isSupplierHighlighting) Icons.Outlined.CheckCircle else Icons.Default.Business,
                                contentDescription = null,
                                tint = if (state.isSupplierHighlighting) CreateGreen else CreateTextSecondary,
                                modifier = Modifier.size(if (isTablet) 24.dp else 20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = state.supplierName.ifBlank { "Seleccionar origen del lote" },
                                color = if (state.supplierName.isBlank()) CreateTextSecondary else CreateTextPrimary,
                                fontSize = if (isTablet) 16.sp else 14.sp,
                                fontWeight = if (state.supplierName.isBlank()) FontWeight.Normal else FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, null, tint = CreateTextSecondary)
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .then(
                                    if (isTablet) Modifier.width(400.dp) else Modifier.fillMaxWidth(
                                        0.85f
                                    )
                                )
                                .background(Color.White)
                        ) {
                            // Opción "Ninguno"
                            DropdownMenuItem(text = {
                                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                    Text(
                                        text = "Inventario inicial / Sin proveedor",
                                        fontSize = if (isTablet) 16.sp else 14.sp,
                                        color = CreateTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "Usar cuando estás cargando stock existente sin factura.",
                                        fontSize = if (isTablet) 13.sp else 12.sp,
                                        color = CreateTextSecondary
                                    )
                                }
                            }, onClick = {
                                onStateChange(
                                    state.copy(
                                        supplierId = "",
                                        supplierName = INITIAL_INVENTORY_SOURCE_NAME,
                                        invoiceNumber = "",
                                        invoiceImageBase64 = null,
                                        paymentCondition = "CONTADO",
                                        paymentDueDate = "",
                                        isSupplierHighlighting = true,
                                        isInvoiceHighlighting = false,
                                        errors = state.errors - setOf(
                                            "supplier", "invoiceNumber", "paymentDueDate"
                                        )
                                    )
                                )
                                expanded = false
                            })

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(CreateBorder.copy(alpha = 0.5f))
                            )

                            state.suppliers.forEachIndexed { index, proveedor ->
                                DropdownMenuItem(text = {
                                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                        Text(
                                            proveedor.nombre,
                                            fontSize = if (isTablet) 17.sp else 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (proveedor.idFiscal.isNotBlank()) Text(
                                            proveedor.idFiscal,
                                            fontSize = if (isTablet) 14.sp else 12.sp,
                                            color = CreateTextSecondary
                                        )
                                    }
                                }, onClick = {
                                    // DISPARAMOS EL RESALTADO MANUAL
                                    onStateChange(
                                        state.copy(
                                            supplierId = proveedor.id,
                                            supplierName = proveedor.nombre,
                                            isSupplierHighlighting = true // ACTIVAR FLASH
                                        )
                                    )
                                    expanded = false
                                })
                                if (index < state.suppliers.size - 1) Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(CreateBorder.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }

                // --- BOTÓN AÑADIR PROVEEDOR ---
                IconButton(
                    onClick = { onStateChange(state.copy(showAddSupplierDialog = true)) },
                    modifier = Modifier
                        .size(if (isTablet) 56.dp else 48.dp)
                        .background(CreateGreen.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir",
                        tint = CreateGreen,
                        modifier = Modifier.size(if (isTablet) 28.dp else 24.dp)
                    )
                }
            }
        }
    }
}


@Composable
private fun AddSupplierDialog(
    isSaving: Boolean,
    isSuccess: Boolean,
    suppliers: List<com.app.administradorfarmadon.ActivityInventario.ClasesProductos.Proveedor>,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var idFiscal by remember { mutableStateOf("") }

    // --- VALIDACIÓN EN TIEMPO REAL ---
    val nombreExiste = remember(nombre, suppliers) {
        nombre.trim().isNotBlank() && suppliers.any {
            it.nombre.equals(
                nombre.trim(), ignoreCase = true
            )
        }
    }
    val idExiste = remember(idFiscal, suppliers) {
        idFiscal.trim().isNotBlank() && suppliers.any {
            it.idFiscal.equals(
                idFiscal.trim(), ignoreCase = true
            )
        }
    }

    // El botón solo se activa si hay nombre y no hay errores de duplicado
    val canSave = nombre.isNotBlank() && !nombreExiste && !idExiste

    val pais = com.app.administradorfarmadon.ClasesDatabase.SessionManager.paisOperacion.lowercase()
    val labelId = when {
        pais.contains("venezuela") -> "RIF"
        pais.contains("peru") || pais.contains("perú") -> "RUC"
        pais.contains("colombia") -> "NIT"
        pais.contains("mexico") || pais.contains("méxico") -> "RFC"
        else -> "Identificación Fiscal"
    }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() }, title = {
            Text(
                text = if (isSuccess) "¡Excelente!" else "Nuevo Proveedor",
                fontWeight = FontWeight.Black,
                color = if (isSuccess) CreateGreen else CreateTextPrimary
            )
        }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (isSaving || isSuccess) {
                    // --- VISTA DE CARGA / ÉXITO (SKELETON) ---
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SupplierPlaceholderItem(widthFraction = 1f)
                        SupplierPlaceholderItem(widthFraction = 0.6f)

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isSuccess) "Registrado con éxito..." else "Sincronizando con el servidor...",
                            style = MaterialTheme.typography.bodySmall,
                            color = CreateTextSecondary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // --- VISTA DE FORMULARIO CON ERRORES ---
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre / Razón Social") },
                            placeholder = { Text("Ej. Droguería Central") },
                            isError = nombreExiste,
                            supportingText = if (nombreExiste) {
                                { Text("Este nombre ya existe en tu lista", color = CreateRed) }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            enabled = !isSaving)

                        OutlinedTextField(
                            value = idFiscal,
                            onValueChange = { idFiscal = it },
                            label = { Text(labelId) },
                            placeholder = { Text("Nro de identificación...") },
                            isError = idExiste,
                            supportingText = if (idExiste) {
                                {
                                    Text(
                                        "Esta identificación ya está registrada", color = CreateRed
                                    )
                                }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            enabled = !isSaving)
                    }
                }
            }
        }, confirmButton = {
            if (!isSaving && !isSuccess) {
                Button(
                    onClick = { onSave(nombre, idFiscal) },
                    colors = ButtonDefaults.buttonColors(containerColor = CreateGreen),
                    enabled = canSave,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar Proveedor", fontWeight = FontWeight.Bold)
                }
            } else if (isSuccess) {
                // Check de éxito final
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = CreateGreen,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 16.dp)
                )
            } else {
                // Indicador de guardado progresivo
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 16.dp),
                    color = CreateGreen,
                    strokeWidth = 3.dp
                )
            }
        }, dismissButton = {
            if (!isSaving && !isSuccess) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = CreateTextSecondary)
                }
            }
        }, containerColor = Color.White, shape = RoundedCornerShape(24.dp)
    )
}

// COMPONENTE AUXILIAR PARA LA ANIMACIÓN DE CARGA (SHIMMER)
@Composable
private fun SupplierPlaceholderItem(widthFraction: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer_supplier")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f, animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(52.dp)
            .background(Color(0xFFF3F4F6).copy(alpha = alpha), RoundedCornerShape(12.dp))
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

        val shouldHideKeyboard =
            currentStatus == com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionStatus.READY && state.categorySelectedFromAi && state.category.isNotBlank()

        if (shouldHideKeyboard) {
            delay(120)
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
                onStateChange(
                    state.copy(
                        category = it,
                        isAnalyzingKeywords = false,
                        errors = state.errors - "category",
                        categorySelectedFromAi = false
                    )
                )
            },
            onCategorySelectedFromSuggestion = { selected ->
                val mappedTypeFromMainSuggestion =
                    categorySuggestionState.suggestion?.tipoControl?.toCreateProductControlTypeOrNull()

                val mappedTypeFromManualSuggestion =
                    categorySuggestionState.sugerenciaTipoManual?.takeIf { it.confianza == ConfianzaIA.ALTA }?.tipo?.toCreateProductControlTypeOrNull()

                val fallbackType = when {
                    state.name.contains(
                        "jarabe",
                        ignoreCase = true
                    ) || state.name.contains(
                        "solucion",
                        ignoreCase = true
                    ) || state.name.contains(
                        "solución",
                        ignoreCase = true
                    ) || state.name.contains(
                        "gotas",
                        ignoreCase = true
                    ) || state.name.contains(
                        "suspension",
                        ignoreCase = true
                    ) || state.name.contains(
                        "suspensión",
                        ignoreCase = true
                    ) || selected.contains(
                        "liquido",
                        ignoreCase = true
                    ) || selected.contains("líquido", ignoreCase = true) -> {
                        CreateProductControlType.LIQUIDO
                    }

                    state.name.contains("crema", ignoreCase = true) || state.name.contains(
                        "pomada",
                        ignoreCase = true
                    ) || state.name.contains(
                        "gel",
                        ignoreCase = true
                    ) || state.name.contains(
                        "polvo",
                        ignoreCase = true
                    ) || selected.contains("peso", ignoreCase = true) -> {
                        CreateProductControlType.PESO
                    }

                    else -> {
                        CreateProductControlType.UNIDAD
                    }
                }

                val mappedType =
                    mappedTypeFromMainSuggestion ?: mappedTypeFromManualSuggestion ?: fallbackType

                onStateChange(
                    state.copy(
                        category = selected,
                        categorySelectedFromAi = true,
                        typeSelectedManually = false,
                        controlType = mappedType,
                        isAnalyzingKeywords = false,
                        errors = state.errors - setOf(
                            "category", "controlType"
                        )
                    )
                )

                onClearAsistManual()
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
    // CORREGIDO: Se eliminó la marca de texto que causaba el error aquí
    val hasDuplicateProduct = state.duplicateProductFound != null

    // CORREGIDO: Se eliminó la marca de texto al final de la validación del controlType
    val mostrarCamposSecundarios =
        !hasDuplicateProduct && state.name.isNotBlank() && state.category.isNotBlank() && state.controlType != null

    AnimatedVisibility(
        visible = mostrarCamposSecundarios,
        enter = fadeIn() + slideInVertically { it / 6 },
        exit = fadeOut() + slideOutVertically { -it / 6 }) {
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
                color = Color.White, shape = RoundedCornerShape(22.dp), border = BorderStroke(
                    width = if (state.name.isNotBlank() && state.category.isNotBlank() && state.controlType != null) 2.dp else 1.dp,
                    color = if (state.name.isNotBlank() && state.category.isNotBlank() && state.controlType != null) CreateGreen else CreateBorder
                )
            ) {
                Column {
                    // Título dinámico que afirma el estado real del switch
                    val dynamicTitle = if (state.requiresPrescription) {
                        "Venta bajo receta médica"
                    } else {
                        "Venta libre (Sin receta)"
                    }

                    CompactSwitchRow(
                        title = dynamicTitle,
                        checked = state.requiresPrescription,
                        onCheckedChange = { nuevoEstado ->
                            onStateChange(state.copy(requiresPrescription = nuevoEstado))
                        })

                    // Bloque informativo de la IA
                    if (shouldShowAiPrescriptionInfo || aiPrescriptionSuggestion != null) {
                        val sourceRequires = if (shouldShowAiPrescriptionInfo) {
                            state.barcodeAiResult?.requiereReceta == true
                        } else {
                            aiPrescriptionSuggestion == true
                        }

                        // Solo se muestra si coincide con la selección actual
                        if (state.requiresPrescription == sourceRequires) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(CreateBorder.copy(alpha = 0.55f))
                            )
                            AiPrescriptionInfoRow(requiresPrescription = sourceRequires)
                        }
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
    labelScannerState: com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.UiState = com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.UiState(),
    onRequestLabelScan: () -> Unit = {},
    onConsumeLabelScan: () -> Unit = {},
    isCheckingLotRemote: Boolean = false,
    lotConflictInfo: String? = null,
    lotConflictColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Transparent,
    lotConflictSeverity: Int = 0,
    onSaveSupplier: (String, String) -> Unit = { _, _ -> }
) {
    val initialInventorySourceName = "Inventario inicial / Sin proveedor"

    val hasRealSupplier = state.supplierId.isNotBlank()
    val isInitialInventorySource =
        state.supplierId.isBlank() && state.supplierName == initialInventorySourceName

    val hasStockOriginSelected = hasRealSupplier || isInitialInventorySource


    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    val filePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { selectedUri ->
            scope.launch {
                try {
                    val base64 = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(selectedUri)?.use { inputStream ->
                            val bytes = inputStream.readBytes()
                            android.util.Base64.encodeToString(
                                bytes, android.util.Base64.NO_WRAP
                            )
                        }
                    }

                    if (!base64.isNullOrBlank()) {
                        onStateChange(
                            state.copy(
                                invoiceImageBase64 = base64, isInvoiceHighlighting = true
                            )
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FilePicker", "Error leyendo archivo", e)
                }
            }
        }
    }

    var showInvoicePreview by remember { mutableStateOf(false) }

    val expirationStatus = remember(state.expirationDate) {
        evaluateExpirationStatus(state.expirationDate)
    }

    val normalizedLot = remember(state.lotNumber) {
        state.lotNumber.trim().uppercase()
    }

    val lotExists = remember(normalizedLot, existingLotNumbers) {
        normalizedLot.isNotBlank() && existingLotNumbers.contains(normalizedLot)
    }

    val isLotAvailableForExpiration = remember(
        normalizedLot,
        isCheckingLotRemote,
        lotExists,
        lotConflictSeverity,
        lotConflictInfo,
        state.errors
    ) {
        normalizedLot.isNotBlank() && !isCheckingLotRemote && !lotExists && lotConflictSeverity == 0 && state.errors["lotNumber"] == null && lotConflictInfo?.contains(
            "disponible",
            ignoreCase = true
        ) == true
    }

    val isLotInfoReady = remember(
        state.lotNumber,
        state.expirationDate,
        state.errors,
        lotConflictSeverity,
        expirationStatus,
        lotExists
    ) {
        state.lotNumber.isNotBlank() && state.expirationDate.isNotBlank() && lotConflictSeverity == 0 && !lotExists && state.errors["lotNumber"] == null && expirationStatus?.kind != ExpirationStatusKind.VENCIDO && expirationStatus?.kind != ExpirationStatusKind.INVALIDO
    }

    val purchaseCostValue = state.purchaseCost.replace(
        com.app.administradorfarmadon.ClasesDatabase.SessionManager.monedaSimbolo,
        "",
        ignoreCase = true
    ).trim().replace(",", ".").toDoubleOrNull() ?: 0.0

    val hasPurchaseCost = purchaseCostValue > 0.0

    val hasInvoiceData = hasRealSupplier && state.invoiceNumber.isNotBlank() && hasPurchaseCost

    val isPaymentDataValid = remember(
        state.supplierId,
        state.supplierName,
        state.invoiceNumber,
        state.purchaseCost,
        state.paymentCondition,
        state.paymentDueDate
    ) {
        val costOk = state.purchaseCost.replace(
            com.app.administradorfarmadon.ClasesDatabase.SessionManager.monedaSimbolo,
            "",
            ignoreCase = true
        ).trim().replace(",", ".").toDoubleOrNull()?.let { it > 0.0 } ?: false

        val originOk =
            state.supplierId.isNotBlank() || (state.supplierId.isBlank() && state.supplierName == initialInventorySourceName)

        val invoiceOk = if (state.supplierId.isNotBlank()) {
            state.invoiceNumber.isNotBlank()
        } else {
            true
        }

        val creditOk = if (state.paymentCondition == "CREDITO") {
            val cleanedDate = state.paymentDueDate.trim()

            val validCreditDate = try {
                if (!cleanedDate.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
                    false
                } else {
                    val parts = cleanedDate.split("/")
                    val day = parts[0].toInt()
                    val month = parts[1].toInt()
                    val year = parts[2].toInt()

                    val date = java.time.LocalDate.of(year, month, day)

                    !date.isBefore(java.time.LocalDate.now())
                }
            } catch (e: Exception) {
                false
            }

            state.supplierId.isNotBlank() && validCreditDate
        } else {
            true
        }

        originOk && costOk && invoiceOk && creditOk
    }

    var showManualLotEntry by remember {
        mutableStateOf(
            state.lotNumber.isNotBlank() || state.expirationDate.isNotBlank()
        )
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
            icon = Icons.Outlined.Inventory2,
            isSuccess = isLotInfoReady,
            onClear = if (showManualLotEntry || state.lotScanned) {
                {
                    showManualLotEntry = false
                    onStateChange(
                        state.copy(
                            lotNumber = "", expirationDate = "", lotScanned = false
                        )
                    )
                }
            } else {
                null
            }) {
            val isScanning =
                labelScannerState.status != com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.Status.IDLE

            if (!showManualLotEntry && !isScanning) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onRequestLabelScan,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CreateGreen
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Escanear", fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { showManualLotEntry = true },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CreateBorder)
                    ) {
                        Text(
                            text = "Ingresar manual",
                            color = CreateTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            LabelScannerRow(
                state = labelScannerState,
                onRequestScan = onRequestLabelScan,
                onApplyResult = { etiqueta ->
                    var nuevoEstado = state

                    etiqueta.loteNumero?.let { loteDetectado ->
                        nuevoEstado = nuevoEstado.copy(
                            lotNumber = loteDetectado,
                            expirationDate = "",
                            lotScanned = true,
                            errors = nuevoEstado.errors - setOf(
                                "lotNumber", "expirationDate"
                            )
                        )
                    }

                    // No aplicamos vencimiento detectado aquí.
                    // El vencimiento solo se habilita cuando Firebase confirme "Lote disponible".

                    onStateChange(nuevoEstado)
                },
                onDismiss = onConsumeLabelScan,
                showScanButton = false
            )

            if (labelScannerState.status == com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.Status.READY) {
                Surface(
                    color = CreateAiFocusSoft.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, CreateAiFocus.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Lightbulb,
                                    contentDescription = null,
                                    tint = CreateAiFocus,
                                    modifier = Modifier.size(18.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "LOTE DETECTADO POR IA",
                                    color = CreateAiFocus,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            IconButton(
                                onClick = {
                                    onConsumeLabelScan()
                                    onStateChange(
                                        state.copy(
                                            lotNumber = "", expirationDate = "", lotScanned = false
                                        )
                                    )
                                }, modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    tint = CreateRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (showManualLotEntry) {
                if (isLotAvailableForExpiration) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            AppTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp),
                                label = "LOTE",
                                value = state.lotNumber,
                                isSuccess = state.lotNumber.isNotBlank() && !lotExists && lotConflictSeverity == 0,
                                error = if (lotExists) "Ese lote ya existe" else state.errors["lotNumber"],
                                helper = if (isCheckingLotRemote) "Verificando." else lotConflictInfo,
                                onValueChange = {
                                    onStateChange(state.copy(lotNumber = it.uppercase()))
                                })
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            ExpirationDateField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp),
                                label = "VENCIMIENTO",
                                value = state.expirationDate,
                                isSuccess = state.expirationDate.isNotBlank() && expirationStatus?.kind != ExpirationStatusKind.VENCIDO,
                                error = if (expirationStatus?.kind == ExpirationStatusKind.VENCIDO) {
                                    "Lote vencido"
                                } else {
                                    state.errors["expirationDate"]
                                },
                                helper = if (expirationStatus?.kind != ExpirationStatusKind.VENCIDO) {
                                    expirationStatus?.message
                                } else {
                                    null
                                },
                                helperColor = expirationStatus?.color ?: CreateTextSecondary,
                                onValueChange = {
                                    onStateChange(state.copy(expirationDate = it))
                                })
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppTextField(
                            label = "LOTE",
                            value = state.lotNumber,
                            isSuccess = false,
                            error = if (lotExists) {
                                "Ese lote ya existe"
                            } else {
                                state.errors["lotNumber"]
                            },
                            helper = if (isCheckingLotRemote) {
                                "Verificando..."
                            } else {
                                lotConflictInfo
                            },
                            onValueChange = { raw ->
                                val nextLot = raw.uppercase()

                                onStateChange(
                                    state.copy(
                                        lotNumber = nextLot,
                                        expirationDate = "",
                                        lotScanned = false,
                                        errors = state.errors - setOf(
                                            "lotNumber", "expirationDate"
                                        )
                                    )
                                )
                            })

                        if (state.lotNumber.isNotBlank() && !isCheckingLotRemote) {
                            Text(
                                text = "El vencimiento se habilitará cuando el lote esté disponible.",
                                color = CreateTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isLotInfoReady, enter = fadeIn(), exit = fadeOut()
        ) {
            CreateSectionCard(
                title = "Datos de pago",
                icon = Icons.AutoMirrored.Outlined.Assignment,
                isSuccess = isPaymentDataValid
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    SupplierSelectionSection(
                        state = state, onStateChange = onStateChange
                    )

                    AnimatedVisibility(
                        visible = hasStockOriginSelected, enter = fadeIn(), exit = fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(CreateBorder.copy(alpha = 0.5f))
                            )

                            AppTextField(
                                label = if (hasRealSupplier) {
                                    "COSTO TOTAL DEL LOTE (BS)"
                                } else {
                                    "COSTO ESTIMADO DEL LOTE (BS)"
                                },
                                value = state.purchaseCost,
                                keyboardType = KeyboardType.Decimal,
                                onValueChange = {
                                    onStateChange(
                                        state.copy(
                                            purchaseCost = it,
                                            errors = state.errors - "purchaseCost"
                                        )
                                    )
                                })

                            if (isInitialInventorySource) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = CreateBlueSoft,
                                    shape = RoundedCornerShape(18.dp),
                                    border = BorderStroke(1.dp, CreateBlue.copy(alpha = 0.14f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Info,
                                            contentDescription = null,
                                            tint = CreateBlue,
                                            modifier = Modifier.size(20.dp)
                                        )

                                        Text(
                                            text = "Este costo se usará como referencia para margen, reportes y valor de inventario. No se guardará factura ni cuenta por pagar.",
                                            color = CreateTextSecondary,
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = hasRealSupplier,
                        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            AppTextField(
                                label = "NRO FACTURA",
                                value = state.invoiceNumber,
                                onValueChange = {
                                    onStateChange(
                                        state.copy(
                                            invoiceNumber = it,
                                            errors = state.errors - "invoiceNumber"
                                        )
                                    )
                                })

                            val hasInvoice = state.invoiceImageBase64 != null

                            val invoiceHighlightColor by animateColorAsState(
                                targetValue = if (state.isInvoiceHighlighting) {
                                    CreateGreen.copy(alpha = 0.15f)
                                } else if (hasInvoice) {
                                    CreateGreenSoft
                                } else {
                                    CreateBackground
                                }, animationSpec = tween(500)
                            )

                            AnimatedVisibility(
                                visible = hasInvoiceData,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                if (hasInvoice) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp)
                                            .heightIn(min = 56.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            onClick = {
                                                val isPdf = state.invoiceImageBase64?.take(10)
                                                    ?.contains("JVBER") == true

                                                if (isPdf) {
                                                    ProductUtils.openBase64FileNatively(
                                                        context = context,
                                                        base64 = state.invoiceImageBase64!!
                                                    )
                                                } else {
                                                    showInvoicePreview = true
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight(),
                                            color = invoiceHighlightColor,
                                            shape = RoundedCornerShape(28.dp),
                                            border = BorderStroke(
                                                1.dp, if (state.isInvoiceHighlighting) {
                                                    CreateGreen
                                                } else {
                                                    CreateGreen.copy(alpha = 0.2f)
                                                }
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.CheckCircle,
                                                    contentDescription = null,
                                                    tint = CreateGreen,
                                                    modifier = Modifier.size(20.dp)
                                                )

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Text(
                                                    text = "Documento cargado",
                                                    color = CreateGreen,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Surface(
                                            onClick = {
                                                onStateChange(
                                                    state.copy(invoiceImageBase64 = null)
                                                )
                                            },
                                            modifier = Modifier.size(56.dp),
                                            color = CreateRed.copy(alpha = 0.1f),
                                            shape = CircleShape,
                                            border = BorderStroke(
                                                1.dp, CreateRed.copy(alpha = 0.2f)
                                            )
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Eliminar",
                                                    tint = CreateRed,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp)
                                            .heightIn(min = 60.dp),
                                        color = CreateGreenUltraSoft,
                                        shape = RoundedCornerShape(20.dp),
                                        border = BorderStroke(
                                            1.dp, CreateGreen.copy(alpha = 0.16f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .clickable {
                                                        onStateChange(
                                                            state.copy(isCapturingInvoice = true)
                                                        )
                                                    }, contentAlignment = Alignment.Center
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.PhotoCamera,
                                                        contentDescription = null,
                                                        tint = CreateGreen,
                                                        modifier = Modifier.size(18.dp)
                                                    )

                                                    Spacer(modifier = Modifier.width(10.dp))

                                                    Text(
                                                        text = "Foto",
                                                        color = CreateGreenDark,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .width(1.dp)
                                                    .height(24.dp)
                                                    .background(CreateGreen.copy(alpha = 0.10f))
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .clickable {
                                                        filePickerLauncher.launch("*/*")
                                                    }, contentAlignment = Alignment.Center
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.Assignment,
                                                        contentDescription = null,
                                                        tint = CreateGreen,
                                                        modifier = Modifier.size(18.dp)
                                                    )

                                                    Spacer(modifier = Modifier.width(10.dp))

                                                    Text(
                                                        text = "Archivo",
                                                        color = CreateGreenDark,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Condición de pago solo si ya hay proveedor real, factura y costo
                            AnimatedVisibility(
                                visible = hasInvoiceData,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "CONDICIÓN DE PAGO",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = CreateTextSecondary
                                    )

                                    var showCreditPickerTrigger by remember {
                                        mutableStateOf(false)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        listOf("CONTADO", "CREDITO").forEach { condition ->
                                            val isSel = state.paymentCondition == condition

                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        if (condition == "CONTADO") {
                                                            onStateChange(
                                                                state.copy(
                                                                    paymentCondition = "CONTADO",
                                                                    paymentDueDate = ""
                                                                )
                                                            )
                                                        } else {
                                                            // No marcamos CREDITO todavía.
                                                            // Primero obligamos al usuario a elegir fecha.
                                                            showCreditPickerTrigger = true
                                                        }
                                                    },
                                                shape = RoundedCornerShape(16.dp),
                                                color = if (isSel) {
                                                    CreateGreenUltraSoft
                                                } else {
                                                    CreateSurfaceSoft
                                                },
                                                border = BorderStroke(
                                                    width = if (isSel) 1.4.dp else 1.dp,
                                                    color = if (isSel) {
                                                        CreateGreen.copy(alpha = 0.45f)
                                                    } else {
                                                        CreateBorder
                                                    }
                                                )
                                            ) {
                                                Box(
                                                    modifier = Modifier.padding(
                                                        horizontal = 14.dp, vertical = 14.dp
                                                    ), contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = condition,
                                                        color = if (isSel) {
                                                            CreateGreenDark
                                                        } else {
                                                            CreateTextSecondary
                                                        },
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (showCreditPickerTrigger) {
                                        FullDatePickerDialog(
                                            value = state.paymentDueDate,
                                            onDismiss = {
                                                showCreditPickerTrigger = false

                                                // Si cancela sin elegir fecha, no debe quedar marcado como crédito.
                                                if (state.paymentDueDate.isBlank()) {
                                                    onStateChange(
                                                        state.copy(
                                                            paymentCondition = "CONTADO",
                                                            paymentDueDate = ""
                                                        )
                                                    )
                                                }
                                            },
                                            onConfirm = { formatted ->
                                                onStateChange(
                                                    state.copy(
                                                        paymentCondition = "CREDITO",
                                                        paymentDueDate = formatted
                                                    )
                                                )
                                                showCreditPickerTrigger = false
                                            })
                                    }

                                    AnimatedVisibility(
                                        visible = state.paymentCondition == "CREDITO" && state.paymentDueDate.isNotBlank()
                                    ) {
                                        Column {
                                            Spacer(modifier = Modifier.height(8.dp))

                                            FullDateField(
                                                label = "VENCIMIENTO DEL PAGO",
                                                value = state.paymentDueDate,
                                                isSuccess = state.paymentDueDate.isNotBlank(),
                                                onValueChange = {
                                                    onStateChange(
                                                        state.copy(paymentDueDate = it)
                                                    )
                                                })
                                        }
                                    }
                                }
                            }
                        }
                    }


                }
            }
        }


        // --- SECCIÓN 3: FORMA DE INGRESO (CÓMO SE CONTROLA) ---
        AnimatedVisibility(
            visible = isPaymentDataValid, enter = fadeIn(), exit = fadeOut()
        ) {
            CreateSectionCard(
                title = "Cómo se controla",
                subtitle = "Elige cómo se descontará en inventario",
                icon = Icons.Outlined.AllInbox,
                isSuccess = isStockEntryModeCompleted(
                    state,
                    CreateProductStockEntryMode.UNIDAD
                ) || isStockEntryModeCompleted(state, CreateProductStockEntryMode.CAJA)
            ) {
                PremiumStockEntryModeSelector(
                    controlType = state.controlType,
                    selected = state.stockEntryMode,
                    state = state,
                    onSelected = { mode ->
                        onStateChange(
                            state.copy(
                                stockEntryMode = mode,
                                showStockEntryDialog = true,
                                stockEntryConfigured = false
                            )
                        )
                    })
            }
        }

        // --- SECCIÓN 4: ALERTA DE STOCK BAJO ---
        val stockEntryCompleted = isStockEntryModeCompleted(
            state,
            CreateProductStockEntryMode.UNIDAD
        ) || isStockEntryModeCompleted(state, CreateProductStockEntryMode.CAJA)

        AnimatedVisibility(
            visible = isPaymentDataValid && stockEntryCompleted, enter = fadeIn(), exit = fadeOut()
        ) {
            MinimumStockInputCard(
                state = state, onStateChange = onStateChange
            )
        }

    }

    if (showInvoicePreview && state.invoiceImageBase64 != null) {
        Dialog(
            onDismissRequest = {
                showInvoicePreview = false
            }) {
            Surface(
                shape = RoundedCornerShape(24.dp), color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Respaldo de Factura",
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val bitmap = remember(state.invoiceImageBase64) {
                        state.invoiceImageBase64?.let {
                            ProductUtils.base64ToBitmap(it)
                        }
                    }

                    if (bitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 450.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 180.dp, max = 260.dp)
                                .background(
                                    CreateBackground, RoundedCornerShape(16.dp)
                                ),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Assignment,
                                contentDescription = null,
                                tint = CreateGreen,
                                modifier = Modifier.size(64.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "DOCUMENTO ADJUNTO",
                                color = CreateGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )

                            Text(
                                text = "Formato PDF o Documento",
                                color = CreateTextSecondary,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    ProductUtils.openBase64FileNatively(
                                        context = context, base64 = state.invoiceImageBase64!!
                                    )
                                }, colors = ButtonDefaults.buttonColors(
                                    containerColor = CreateGreen.copy(alpha = 0.1f),
                                    contentColor = CreateGreen
                                ), shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "VER ARCHIVO COMPLETO",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onStateChange(
                                    state.copy(invoiceImageBase64 = null)
                                )
                                showInvoicePreview = false
                            },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, CreateRed)
                        ) {
                            Text(
                                text = "ELIMINAR", color = CreateRed
                            )
                        }

                        Button(
                            onClick = {
                                showInvoicePreview = false
                            }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(
                                containerColor = CreateGreen
                            )
                        ) {
                            Text(text = "CERRAR")
                        }
                    }
                }
            }
        }
    }
}


/**
 * Tarjeta de stock mínimo basada en porcentaje.
 *
 * Si el inventario se controla en piezas enteras (frascos, paquetes, cajas),
 * solo permite opciones enteras válidas. Así 5 frascos generan 20%, 40%,
 * 60%, 80% y 100%, nunca 5%.
 */
@Composable
fun MinimumStockInputCard(
    state: CreateProductState, onStateChange: (CreateProductState) -> Unit
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
                validOptions.minByOrNull { kotlin.math.abs(it - currentSelection) }
                    ?: validOptions.first()
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
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
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
                            text = "Control", color = CreateTextSecondary, fontSize = 11.sp
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
                            text = "Total recibido", color = CreateTextSecondary, fontSize = 11.sp
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
                                if (state.stockEntryMode == CreateProductStockEntryMode.CAJA_CON_PAQUETES) parseCreateProductNumber(
                                    state.unitsPerPackageText
                                )
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
                    color = CreateGreenSoft.copy(alpha = 0.55f), shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "$formattedPercent del lote recibido",
                        color = CreateTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                val isVeryLowMinimum = percentage <= 10.0 && totalReference > 1

                if (isVeryLowMinimum) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = CreateOrangeSoft,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp, CreateOrange.copy(alpha = 0.25f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = CreateOrange,
                                modifier = Modifier.size(18.dp)
                            )

                            Text(
                                text = "10% es el mínimo permitido. Es un nivel bajo y puede aumentar el riesgo de agotamiento.",
                                color = CreateTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                val isHighMinimum = percentage >= 50.0 && totalReference > 1

                if (isHighMinimum) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = CreateBlueSoft,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp, CreateBlue.copy(alpha = 0.20f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = CreateBlue,
                                modifier = Modifier.size(18.dp)
                            )

                            Text(
                                text = "50% es el máximo permitido. Es una alerta temprana, no un stock crítico.",
                                color = CreateTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
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
                                    minimumStockUnit = resolveMinimumStockStoredUnit(
                                        state, next, mode
                                    ),
                                    errors = state.errors - "minimumStock"
                                )
                            )
                        }
                    }, enabled = canDecrease, modifier = Modifier
                        .size(52.dp)
                        .background(
                            if (canDecrease) CreateSurfaceSoft else CreateBackground, CircleShape
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
                                    minimumStockUnit = resolveMinimumStockStoredUnit(
                                        state, next, mode
                                    ),
                                    errors = state.errors - "minimumStock"
                                )
                            )
                        }
                    }, enabled = canIncrease, modifier = Modifier
                        .size(52.dp)
                        .background(
                            if (canIncrease) CreateGreenSoft else CreateBackground, CircleShape
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
                text = if (mode == StockControlMode.INDIVISIBLE) {
                    "Solo se permiten cantidades completas. Rango permitido: 10% a 50%."
                } else {
                    "La alerta sigue tramos válidos. Rango permitido: 10% a 50%."
                },
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
    icon: String, enabled: Boolean, onClick: () -> Unit
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
    state: CreateProductState, unitLabel: String
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
            val visualTotal =
                if (state.stockEntryUnit == "kg" || state.stockEntryUnit == "L") total / 1000.0 else total

            if (boxes <= 0.0 || perBox <= 0.0) "" else "Recibiste ${formatCreateProductNumber(boxes)} ${
                plural(
                    boxes, "caja", "cajas"
                )
            } (${formatCreateProductNumber(visualTotal)} $unitLabel total)"
        }

        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
            val boxes = parseCreateProductNumber(state.boxesReceivedText)
            val packagesPerBox = parseCreateProductNumber(state.packagesPerBoxText)
            val total = calculateTotalBaseStock(state)

            if (boxes <= 0.0 || packagesPerBox <= 0.0) {
                ""
            } else {
                "Recibiste ${formatCreateProductNumber(boxes)} ${
                    plural(
                        boxes,
                        "caja",
                        "cajas"
                    )
                } " + "con ${formatCreateProductNumber(packagesPerBox)} ${
                    plural(
                        packagesPerBox, "paquete", "paquetes"
                    )
                } c/u " + "(${formatCreateProductNumber(total)} $unitLabel total)"
            }
        }

        null -> ""
    }
}

@Composable
fun StockConfiguredCard(
    state: CreateProductState, onEditQuantity: () -> Unit, onChangeType: () -> Unit
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
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
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
                            text = "Control", color = CreateTextSecondary, fontSize = 11.sp
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
                            text = "Disponible", color = CreateTextSecondary, fontSize = 11.sp
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
                        text = "Editar", color = CreateTextPrimary, fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onChangeType,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CreateTextPrimary, contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text(
                        text = "Cambiar modo", fontWeight = FontWeight.Bold
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

        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> "Caja con paquetes"

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

        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> "Se calcularán paquetes internos y piezas por paquete."

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
    val configuration = LocalConfiguration.current
    val dialogMaxHeight = (configuration.screenHeightDp.dp * 0.9f)
    val TealGradient = androidx.compose.ui.graphics.Brush.linearGradient(
        colors = listOf(Color(0xFF00695C), Color(0xFF15A05C))
    )

    // --- LÓGICA DE BLINDAJE Y ANCLAJE (V31.8) ---
// --- LÓGICA DE BLINDAJE Y ANCLAJE (V31.97) ---
// El diálogo ya no usa directamente el contenido crudo de la IA.
// Primero filtra dosis/concentraciones farmacológicas para evitar que "500 mg"
// se trate como contenido físico inventariable.
    val rawAiValue = state.barcodeAiResult?.sugerenciaContenidoValor
    val rawAiUnit = state.barcodeAiResult?.sugerenciaContenidoUnidad

    val safeAiValue = safeInventoryContentValue(
        productName = state.name,
        controlType = state.controlType,
        value = rawAiValue,
        unit = rawAiUnit
    )

    val safeAiUnit = safeInventoryContentUnit(
        productName = state.name,
        controlType = state.controlType,
        value = rawAiValue,
        unit = rawAiUnit
    )

    val shouldUseAiInventoryContent =
        state.barcodeAiApplied &&
                safeAiValue.isNotBlank() &&
                safeAiUnit.isNotBlank()

    val isAnchored = shouldUseAiInventoryContent

    val iaMultiplier =
        if (shouldUseAiInventoryContent) {
            parseAiMultiplier(state.barcodeAiResult?.sugerenciaMultiplicador)
        } else {
            1.0
        }

    val iaContent =
        if (shouldUseAiInventoryContent) {
            parseAiContent(safeAiValue)
        } else {
            0.0
        }

    val iaUnit = if (shouldUseAiInventoryContent) safeAiUnit else ""
    val iaUnitClean = iaUnit.trim().lowercase(Locale.getDefault()).replace(".", "")

    val iaContentBase = when {
        state.controlType == CreateProductControlType.PESO &&
                iaUnitClean in setOf("kg", "kilo", "kilos", "kilogramo", "kilogramos") -> {
            iaContent * 1000.0
        }

        state.controlType == CreateProductControlType.LIQUIDO &&
                iaUnitClean in setOf("l", "lt", "lts", "litro", "litros") -> {
            iaContent * 1000.0
        }

        else -> iaContent
    }

    val iaTotalExpected = iaMultiplier * iaContentBase
    val currentTotal = calculateTotalBaseStock(state)

    val remainder = if (iaTotalExpected > 0) currentTotal % iaTotalExpected else 0.0

    val isExactMultiple =
        iaTotalExpected > 0 &&
                currentTotal > 0 &&
                (
                        kotlin.math.abs(remainder) <= 0.1 ||
                                kotlin.math.abs(remainder - iaTotalExpected) <= 0.1
                        )

    val hasMismatch =
        shouldUseAiInventoryContent &&
                iaTotalExpected > 0 &&
                currentTotal > 0 &&
                !isExactMultiple

    val isMismatchJustified = state.mismatchJustified && !state.mismatchReason.isNullOrBlank()

    // 1. TERMINOLOGÍA ADAPTABLE
    val itemType = when (state.controlType) {
        CreateProductControlType.LIQUIDO -> "FRASCO"
        CreateProductControlType.PESO -> "ENVASE"
        CreateProductControlType.UNIDAD -> if (state.stockEntryMode == CreateProductStockEntryMode.CAJA_CON_PAQUETES) "PAQUETE" else "PIEZA"
        else -> "UNIDAD"
    }
    val pluralItemType = when (state.controlType) {
        CreateProductControlType.LIQUIDO -> "frascos"
        CreateProductControlType.PESO -> "envases"
        CreateProductControlType.UNIDAD -> if (state.stockEntryMode == CreateProductStockEntryMode.CAJA_CON_PAQUETES) "paquetes" else "piezas"
        else -> "unidades"
    }

    val (unidadBase, unidadGrande) = when (state.controlType) {
        CreateProductControlType.LIQUIDO -> "mL" to "L"
        CreateProductControlType.PESO -> "g" to "kg"
        else -> "" to ""
    }
    val mostrarSelectorUnidad =
        state.controlType == CreateProductControlType.PESO || state.controlType == CreateProductControlType.LIQUIDO

    var unitForItem by remember(state.controlType, state.stockEntryUnit) {
        mutableStateOf(state.stockEntryUnit.ifBlank { unidadBase })
    }

    val summary = buildInitialStockEntrySummary(state)

    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(
            usePlatformDefaultWidth = false, decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(horizontal = 16.dp)
                .imePadding(), contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = dialogMaxHeight)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    // --- CABECERA ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = CreateGreen.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Inventory2,
                                    null,
                                    tint = CreateGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "Desglose de inventario",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                "Verificación de empaque y contenido",
                                fontSize = 13.sp,
                                color = CreateTextSecondary
                            )
                        }
                    }

                    // --- PANEL DE CONCILIACIÓN (V31.8) ---
                    if (iaTotalExpected > 0) {
                        Surface(
                            color = if (hasMismatch) CreateOrangeSoft else CreateGreenSoft,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(
                                1.dp,
                                if (hasMismatch) CreateOrange.copy(alpha = 0.3f) else CreateGreen.copy(
                                    alpha = 0.3f
                                )
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            if (hasMismatch) Icons.Default.PriorityHigh else Icons.Outlined.CheckCircle,
                                            null,
                                            tint = if (hasMismatch) CreateOrange else CreateGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            "CONCILIACIÓN DE PACK",
                                            color = if (hasMismatch) CreateOrange else CreateGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                    if (hasMismatch) {
                                        Surface(
                                            color = CreateOrange, shape = RoundedCornerShape(999.dp)
                                        ) {
                                            Text(
                                                "DESCUADRE",
                                                modifier = Modifier.padding(
                                                    horizontal = 8.dp, vertical = 3.dp
                                                ),
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            "DATO DEL CÓDIGO",
                                            color = CreateTextSecondary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "${formatCreateProductNumber(iaTotalExpected)} $unidadBase",
                                            color = CreateTextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Icon(Icons.Default.ChevronRight, null, tint = CreateBorder)
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "TU INGRESO",
                                            color = CreateTextSecondary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "${formatCreateProductNumber(currentTotal)} $unidadBase",
                                            color = if (hasMismatch) CreateOrange else CreateGreen,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // --- CAMPOS ---
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        when (state.stockEntryMode) {
                            CreateProductStockEntryMode.UNIDAD -> {
                                val vendeContenidoInterno =
                                    state.controlType == CreateProductControlType.UNIDAD && state.stockControlMode == StockControlMode.DIVISIBLE

                                PremiumEntryCard(
                                    "CANTIDAD DE ${pluralItemType.uppercase()}",
                                    state.receivedUnitsText,
                                    {
                                        onStateChange(
                                            state.copy(receivedUnitsText = it.filter { c -> c.isDigit() })
                                                .clearMismatchJustification()
                                        )
                                    },
                                    "Ej. 50",
                                    pluralItemType,
                                    Icons.AutoMirrored.Outlined.Assignment
                                )

                                if (state.controlType == CreateProductControlType.UNIDAD) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(22.dp),
                                        color = CreateGreenUltraSoft,
                                        border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.14f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Text(
                                                    "¿Vendes contenido interno?",
                                                    color = CreateTextPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    "Actívalo si venderás piezas sueltas.",
                                                    color = CreateTextSecondary,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Switch(
                                                checked = vendeContenidoInterno,
                                                onCheckedChange = { checked ->
                                                    onStateChange(
                                                        state.copy(
                                                            stockControlMode = if (checked) StockControlMode.DIVISIBLE else StockControlMode.INDIVISIBLE,
                                                            unitsPerItemText = if (isAnchored) state.unitsPerItemText else "",
                                                        ).clearMismatchJustification()
                                                    )
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color.White,
                                                    checkedTrackColor = CreateGreen
                                                )
                                            )
                                        }
                                    }
                                }

                                if (state.controlType != CreateProductControlType.UNIDAD || vendeContenidoInterno) {
                                    PremiumEntryCard(
                                        "CONTENIDO POR $itemType",
                                        state.unitsPerItemText,
                                        {
                                            if (!isAnchored) onStateChange(
                                                state.copy(
                                                    unitsPerItemText = sanitizeDecimalInput(
                                                        it
                                                    )
                                                ).clearMismatchJustification()
                                            )
                                        },
                                        "Ej. 500",
                                        if (mostrarSelectorUnidad) unitForItem else "piezas",
                                        Icons.Outlined.Opacity,
                                        mostrarSelectorUnidad,
                                        unitForItem,
                                        listOf(unidadBase, unidadGrande),
                                        {
                                            unitForItem = it
                                            onStateChange(
                                                state.copy(
                                                    stockEntryUnit = it
                                                ).clearMismatchJustification()
                                            )
                                        },
                                        readOnly = isAnchored
                                    )
                                }
                            }

                            CreateProductStockEntryMode.CAJA, CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
                                val tienePaquetesInternos =
                                    state.stockEntryMode == CreateProductStockEntryMode.CAJA_CON_PAQUETES

                                PremiumEntryCard(
                                    "CANTIDAD DE CAJAS", state.boxesReceivedText, {
                                        onStateChange(
                                            state.copy(boxesReceivedText = it.filter { c -> c.isDigit() })
                                                .clearMismatchJustification()
                                        )
                                    }, "Ej. 10", "cajas", Icons.AutoMirrored.Outlined.Assignment
                                )

                                if (tienePaquetesInternos) {
                                    PremiumEntryCard(
                                        "PAQUETES POR CAJA", state.packagesPerBoxText, {
                                            onStateChange(
                                                state.copy(packagesPerBoxText = it.filter { c -> c.isDigit() })
                                                    .clearMismatchJustification()
                                            )
                                        }, "Ej. 5", "paquetes", Icons.Default.Apps
                                    )
                                    PremiumEntryCard(
                                        "PIEZAS POR PAQUETE", state.unitsPerPackageText, {
                                            onStateChange(
                                                state.copy(unitsPerPackageText = it.filter { c -> c.isDigit() })
                                                    .clearMismatchJustification()
                                            )
                                        }, "Ej. 10", "piezas", Icons.Default.CatchingPokemon
                                    )
                                } else {
                                    PremiumEntryCard(
                                        "PIEZAS POR CAJA", state.unitsPerBoxText, {
                                            onStateChange(
                                                state.copy(unitsPerBoxText = it.filter { c -> c.isDigit() })
                                                    .clearMismatchJustification()
                                            )
                                        }, "Ej. 12", pluralItemType, Icons.Default.Apps
                                    )

                                    if (state.controlType != CreateProductControlType.UNIDAD || (state.controlType == CreateProductControlType.UNIDAD && state.stockControlMode == StockControlMode.DIVISIBLE)) {
                                        PremiumEntryCard(
                                            "CONTENIDO POR $itemType",
                                            state.unitsPerItemText,
                                            {
                                                if (!isAnchored) onStateChange(
                                                    state.copy(
                                                        unitsPerItemText = sanitizeDecimalInput(it)
                                                    ).clearMismatchJustification()
                                                )
                                            },
                                            "Ej. 500",
                                            if (mostrarSelectorUnidad) unitForItem else "piezas",
                                            Icons.Outlined.Opacity,
                                            mostrarSelectorUnidad,
                                            unitForItem,
                                            listOf(unidadBase, unidadGrande),
                                            {
                                                unitForItem = it
                                                onStateChange(
                                                    state.copy(
                                                        stockEntryUnit = it
                                                    ).clearMismatchJustification()
                                                )
                                            },
                                            readOnly = isAnchored
                                        )
                                    }
                                }
                            }

                            else -> {}
                        }
                    }

                    // --- JUSTIFICACIÓN DE DESCUADRE ---
                    if (hasMismatch) {
                        Surface(
                            color = CreateBackground,
                            shape = RoundedCornerShape(22.dp),
                            border = BorderStroke(1.dp, CreateBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    "¿POR QUÉ HAY UN DESCUADRE?",
                                    color = CreateTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                                val reasons = listOf(
                                    "Solo ingreso una parte del pack",
                                    "El pack vino incompleto",
                                    "El dato de la IA es incorrecto"
                                )
                                reasons.forEach { reason ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onStateChange(
                                                    state.copy(
                                                        mismatchJustified = true,
                                                        mismatchReason = reason
                                                    )
                                                )
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = state.mismatchReason == reason, onClick = {
                                                onStateChange(
                                                    state.copy(
                                                        mismatchJustified = true,
                                                        mismatchReason = reason
                                                    )
                                                )
                                            })
                                        Text(
                                            reason,
                                            color = CreateTextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // --- RESUMEN ---
                    if (summary.isNotBlank()) {
                        Surface(
                            color = CreateSurfaceSoft,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, CreateBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val finalUnit = resolveStockUnitLabel(state)
                                val totalVisual =
                                    if (finalUnit == "kg" || finalUnit == "L") currentTotal / 1000.0 else currentTotal
                                Text(
                                    "TOTAL A INGRESAR",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CreateTextSecondary
                                )
                                Text(
                                    "${formatCreateProductNumber(totalVisual)} $finalUnit",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (hasMismatch) CreateOrange else CreateGreen
                                )
                                Text(
                                    summary,
                                    fontSize = 12.sp,
                                    color = CreateTextPrimary.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // --- BOTONES ---
                    val canConfirm = summary.isNotBlank() && (!hasMismatch || isMismatchJustified)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                "Cancelar",
                                color = CreateTextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = onApply,
                            modifier = Modifier
                                .weight(1.2f)
                                .heightIn(min = 56.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            enabled = canConfirm,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CreateGreen, disabledContainerColor = CreateBorder
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(if (canConfirm) Modifier.background(TealGradient) else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Confirmar",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// 2. Componente auxiliar necesario para el diseño
@Composable
private fun PremiumEntryCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    unit: String,
    icon: ImageVector,
    showUnitSelector: Boolean = false,
    selectedUnit: String = "",
    unitOptions: List<String> = emptyList(),
    onUnitSelected: (String) -> Unit = {},
    readOnly: Boolean = false
) {
    val borderColor =
        if (readOnly) CreateGreen.copy(alpha = 0.4f) else CreateBorder.copy(alpha = 0.8f)
    val backgroundColor = if (readOnly) CreateGreenUltraSoft else Color.White

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (readOnly) 0.dp else 2.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = CreateBackground,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        null,
                        tint = if (readOnly) CreateGreen else CreateTextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (readOnly) CreateGreen else CreateTextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    if (readOnly) {
                        Surface(color = CreateGreen, shape = RoundedCornerShape(999.dp)) {
                            Text(
                                "ANCLADO POR IA",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = Color.White,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        singleLine = true,
                        readOnly = readOnly,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (readOnly) CreateTextPrimary else CreateTextPrimary
                        ),
                        decorationBox = { inner ->
                            if (value.isEmpty()) Text(
                                placeholder,
                                color = CreateTextSecondary.copy(alpha = 0.4f),
                                fontSize = 18.sp
                            )
                            inner()
                        })
                    if (showUnitSelector && !readOnly) {
                        PremiumUnitSelector(
                            selected = selectedUnit,
                            options = unitOptions,
                            onSelected = onUnitSelected
                        )
                    } else if (unit.isNotBlank()) {
                        Surface(
                            color = if (readOnly) CreateGreenSoft else CreateBackground,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = unit,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (readOnly) CreateGreen else CreateTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumUnitSelector(
    selected: String, options: List<String>, onSelected: (String) -> Unit
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
    val assignedStock = state.presentations.sumOf {
        it.equivalenceText.trim().replace(",", ".").toDoubleOrNull() ?: 0.0
    }
    val remainingStock = (totalStock - assignedStock).coerceAtLeast(0.0)

    // V21.9: Soporte para unidades escaladas en el panel de distribución (kg/L)
    val factor = if (state.stockEntryUnit == "kg" || state.stockEntryUnit == "L") 1000.0 else 1.0
    val visualRemaining = remainingStock / factor
    val visualTotal = totalStock / factor
    val unit =
        if (state.stockEntryUnit.isNotBlank()) state.stockEntryUnit else resolveStockUnitLabel(state)

    val usageRatio =
        if (totalStock > 0) (assignedStock / totalStock).toFloat().coerceIn(0f, 1f) else 0f
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
                                colors = if (isComplete) listOf(
                                    CreateGreen.copy(alpha = 0.7f), CreateGreen
                                )
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
                InfoMiniTag(
                    "Total: ${formatCreateProductNumber(visualTotal)} $unit", CreateInfoGraySoft
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isComplete) {
                    InfoMiniTag("¡Listo para guardar!", CreateGreenSoft, CreateGreen)
                } else {
                    InfoMiniTag(
                        "Por asignar: ${formatCreateProductNumber(visualRemaining)} $unit",
                        CreateOrangeSoft,
                        CreateOrange
                    )
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
    state: CreateProductState, onStateChange: (CreateProductState) -> Unit
) {
    val presentationsRequester = remember { BringIntoViewRequester() }
    val mainPresentationRequester = remember { BringIntoViewRequester() }

    val firstErrorKey = remember(state.errors) {
        when {
            !state.errors["presentations"].isNullOrBlank() -> "presentations"
            !state.errors["presentations_total"].isNullOrBlank() -> "presentations"
            state.errors.keys.any {
                it.startsWith("price_") || it.startsWith("equivalence_")
            } -> "presentations"

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
        SalesPresentationInfoPanel(state = state)

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
                        PremiumRecentPresentationChip(
                            name = opt.name,
                            equivalence = opt.equivalenceBase,
                            onClick = {
                                val equivalenceBase = parsePresentationDouble(opt.equivalenceBase)
                                val unitContentBase = unitContentBaseFromState(state)

                                val amountForDialog = when (state.controlType) {
                                    CreateProductControlType.LIQUIDO,
                                    CreateProductControlType.PESO -> {
                                        if (unitContentBase > 0.0) {
                                            equivalenceBase / unitContentBase
                                        } else {
                                            0.0
                                        }
                                    }

                                    CreateProductControlType.UNIDAD -> {
                                        equivalenceBase
                                    }

                                    null -> 0.0
                                }

                                onStateChange(
                                    state.copy(
                                        addPresentationExpanded = true,
                                        draftPresentationName = opt.name,
                                        draftPresentationCustomAmount = if (amountForDialog > 0.0) {
                                            formatPresentationQuantity(amountForDialog)
                                        } else {
                                            ""
                                        },
                                        draftPresentationCustomUnit = "",
                                        draftPresentationSalePriceText = ""
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }

        PremiumStep3SalesHeader(
            modifier = Modifier.bringIntoViewRequester(presentationsRequester),
            presentationsCount = state.presentations.size
        )

        if (state.presentations.isEmpty()) {
            PremiumEmptyPresentationsState(
                onClick = {
                    onStateChange(
                        state.copy(addPresentationExpanded = true)
                    )
                })
        }

        state.presentations.forEach { presentation ->
            key(presentation.id) {
                PresentationPriceCard(
                    state = state,
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
                                        it.copy(
                                            equivalenceText = value,
                                            isAiSuggested = false
                                        )
                                    } else {
                                        it
                                    }
                                },
                                errors = state.errors -
                                        "equivalence_${presentation.id}" -
                                        "presentations_total"
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
                        onStateChange(
                            state.copy(
                                presentations = state.presentations.filterNot {
                                    it.id == presentation.id
                                },
                                mainPresentationId = if (state.mainPresentationId == presentation.id) {
                                    ""
                                } else {
                                    state.mainPresentationId
                                }
                            )
                        )
                    }
                )
                val profitGuard = calculateProfitGuard(
                    costoTotal = parseDecimalSafe(state.purchaseCost),
                    stockTotalBase = calculateTotalBaseStock(state),
                    precioVenta = parseDecimalSafe(presentation.salePriceText),
                    equivalenciaBase = parseDecimalSafe(presentation.equivalenceText)
                )

                if (profitGuard != null) {
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .offset(y = (-12).dp),
                        color = profitGuard.color.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(
                            bottomStart = 12.dp, bottomEnd = 12.dp
                        ),
                        border = BorderStroke(
                            1.dp, profitGuard.color.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(
                                                profitGuard.color, CircleShape
                                            )
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Margen: ${formatCreateProductPercent(profitGuard.margenPercent)}%",
                                        color = profitGuard.color,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Text(
                                    text = profitGuard.mensaje,
                                    color = profitGuard.color.copy(alpha = 0.78f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            val isLoss = profitGuard.ganancia < 0.0
                            val profitLabel = if (isLoss) "Pérdida" else "Ganancia"
                            val profitMoney = formatCreateProductMoney(abs(profitGuard.ganancia))

                            Text(
                                text = "$profitLabel: $profitMoney",
                                color = profitGuard.color.copy(alpha = 0.86f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        val presentationsError =
            state.errors["presentations"] ?: state.errors["presentations_total"]

        if (!presentationsError.isNullOrBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CreateRedSoft,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp, CreateRed.copy(alpha = 0.18f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = 14.dp, vertical = 12.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = CreateRed,
                        modifier = Modifier.size(18.dp)
                    )

                    Text(
                        text = presentationsError,
                        color = CreateRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        if (state.addPresentationExpanded) {
            // Se maneja vía Dialog fuera del flujo de la lista.
        }


    }
}


@Composable
private fun SalesPresentationInfoPanel(state: CreateProductState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CreateBlueSoft.copy(alpha = 0.55f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CreateBlue.copy(alpha = 0.16f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = CreateBlue,
                modifier = Modifier.size(20.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Formas alternativas de venta",
                    color = CreateTextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )

                Text(
                    text = "Configura cómo venderás el producto. Esto no separa ni consume stock; el inventario se descuenta recién cuando haces una venta.",
                    color = CreateTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun PremiumEmptyPresentationsState(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(
            1.4.dp, CreateGreen.copy(alpha = 0.22f)
        ),
        shadowElevation = 5.dp
    ) {
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White, CreateGreenUltraSoft, CreateBlueSoft.copy(alpha = 0.28f)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    color = CreateGreenSoft,
                    shape = CircleShape,
                    border = BorderStroke(
                        1.dp, CreateGreen.copy(alpha = 0.20f)
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = CreateGreen,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Text(
                    text = "Crea tu primera forma de venta",
                    color = CreateTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Ejemplo: unidad, caja, frasco, paquete o cualquier presentación que vendas al cliente.",
                    color = CreateTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )

                Surface(
                    color = CreateGreen, shape = RoundedCornerShape(16.dp), shadowElevation = 5.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )

                        Text(
                            text = "Agregar presentación",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun PremiumRecentPresentationChip(
    name: String, equivalence: String, onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp, CreateGreen.copy(alpha = 0.16f)
        ),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Surface(
                modifier = Modifier.size(28.dp), color = CreateGreenSoft, shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = CreateGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = name,
                    color = CreateTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (equivalence.isNotBlank()) {
                    Text(
                        text = "Equiv. $equivalence",
                        color = CreateTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumStep3SalesHeader(
    modifier: Modifier = Modifier,
    presentationsCount: Int
) {
    val hasPresentations = presentationsCount > 0

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(26.dp),
        shadowElevation = 4.dp,
        border = BorderStroke(
            1.dp,
            if (hasPresentations) CreateGreen.copy(alpha = 0.35f) else CreateBorder
        )
    ) {
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(
                    colors = if (hasPresentations) {
                        listOf(
                            Color.White,
                            CreateGreenUltraSoft,
                            CreateGreenSoft
                        )
                    } else {
                        listOf(
                            Color.White,
                            CreateSurfaceSoft,
                            CreateBlueSoft.copy(alpha = 0.42f)
                        )
                    }
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    modifier = Modifier.size(54.dp),
                    color = if (hasPresentations) CreateGreenSoft else CreateBlueSoft,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(
                        1.dp,
                        if (hasPresentations) CreateGreen.copy(alpha = 0.20f)
                        else CreateBlue.copy(alpha = 0.16f)
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.AttachMoney,
                            contentDescription = null,
                            tint = if (hasPresentations) CreateGreen else CreateBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Formas de venta y precios",
                        color = CreateTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = when {
                            presentationsCount == 0 ->
                                "Crea cómo venderás este producto."

                            presentationsCount == 1 ->
                                "Tienes una forma de venta configurada."

                            else ->
                                "Tienes $presentationsCount formas de venta configuradas."
                        },
                        color = CreateTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    color = if (hasPresentations) CreateGreen else CreateSurfaceSoft,
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(
                        1.dp,
                        if (hasPresentations) CreateGreen else CreateBorder
                    )
                ) {
                    Text(
                        text = "$presentationsCount",
                        color = if (hasPresentations) Color.White else CreateTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddPresentationModeChip(
    label: String, selected: Boolean, onClick: () -> Unit
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
                    })
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
                            })
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
                            })
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


private fun presentationDiscountDisplayText(
    state: CreateProductState,
    presentation: CreateProductPresentation
): String {
    val equivalenceBase = parsePresentationDouble(presentation.equivalenceText)
    if (equivalenceBase <= 0.0) return ""

    val quantity = presentationQuantityFromEquivalence(
        state = state,
        presentation = presentation
    )

    val roundedQuantity = kotlin.math.round(quantity)
    val isWholePhysicalQuantity =
        quantity > 0.0 && kotlin.math.abs(quantity - roundedQuantity) <= 0.01

    val itemName = itemUnitNameForPresentation(state)
    val baseUnit = baseUnitLabelForPresentation(state)

    return when {
        // Si la equivalencia representa envases enteros, mostramos envases.
        isWholePhysicalQuantity -> {
            "${formatPresentationQuantity(roundedQuantity)} $itemName"
        }

        // Si no representa envases enteros, mostramos medida exacta.
        // Ej: 100 mL, 250 g, 0.5 unidades, etc.
        else -> {
            "${formatCreateProductNumber(equivalenceBase)} $baseUnit"
        }
    }
}

private fun presentationFriendlySubtitleText(
    state: CreateProductState,
    presentation: CreateProductPresentation
): String {
    val quantity = presentationQuantityFromEquivalence(
        state = state,
        presentation = presentation
    )

    val roundedQuantity = kotlin.math.round(quantity)
    val isWholePhysicalQuantity =
        quantity > 0.0 && kotlin.math.abs(quantity - roundedQuantity) <= 0.01

    val itemName = itemUnitNameForPresentation(state)
    val baseUnit = baseUnitLabelForPresentation(state)
    val equivalenceBase = parsePresentationDouble(presentation.equivalenceText)

    return when {
        isWholePhysicalQuantity -> {
            "${formatPresentationQuantity(roundedQuantity)} $itemName por venta"
        }

        equivalenceBase > 0.0 -> {
            "Medida exacta: ${formatCreateProductNumber(equivalenceBase)} $baseUnit por venta"
        }

        else -> ""
    }
}

@Composable
private fun UnitChoiceChip(
    label: String, selected: Boolean, onClick: () -> Unit
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
    state: CreateProductState, onStateChange: (CreateProductState) -> Unit
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
                title = "Información Útil", icon = Icons.Outlined.Lightbulb, color = CreateAiFocus
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp), color = CreateAiFocus, strokeWidth = 2.5.dp
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
            val hasUsos =
                state.aiUsageInfo.usos.isNotEmpty() && state.aiUsageInfo.usos.any { it.isNotBlank() }
            val hasInstrucciones = state.aiUsageInfo.instrucciones.isNotBlank()
            val hasContraindicaciones = state.aiUsageInfo.contraindicaciones.isNotBlank()

            if (hasUsos || hasInstrucciones || hasContraindicaciones) {
                SummaryCard(
                    title = "Información Útil",
                    icon = Icons.Outlined.Lightbulb,
                    color = CreateAiFocus
                ) {
                    var addDivider = false

                    if (hasUsos) {
                        // Usos
                        Text(
                            text = "PARA QUÉ SIRVE",
                            color = CreateAiFocus,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )

                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.aiUsageInfo.usos.filter { it.isNotBlank() }.forEach { uso ->
                                Surface(
                                    color = CreateAiFocusSoft.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, CreateAiFocus.copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        text = uso,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        ),
                                        color = CreateTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        addDivider = true
                    }

                    if (hasInstrucciones) {
                        if (addDivider) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(CreateBorder.copy(alpha = 0.5f))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        // Instrucciones
                        SummaryItem("Instrucciones de Uso", state.aiUsageInfo.instrucciones)
                        addDivider = true
                    }

                    if (hasContraindicaciones) {
                        if (addDivider) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(CreateBorder.copy(alpha = 0.5f))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
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
            SummaryItem(
                "Venta", if (state.requiresPrescription) "Bajo Receta Médica" else "Venta Libre"
            )
        }

        // --- CARD PASO 2: INVENTARIO ---
        SummaryCard(
            title = "Configuración de Lote", icon = Icons.Outlined.Inventory2, color = CreateOrange
        ) {
            val expirationStatus = evaluateExpirationStatus(state.expirationDate)
            SummaryItem("Número de Lote", state.lotNumber.ifBlank { "(Sin lote)" })
            SummaryItem(
                "Vencimiento",
                state.expirationDate.ifBlank { "(Sin fecha)" },
                helper = expirationStatus?.message,
                helperColor = expirationStatus?.color ?: CreateTextSecondary
            )
            SummaryItem(
                "Stock de Ingreso",
                buildInitialStockEntrySummary(state).ifBlank { "(No configurado)" })
            SummaryItem("Costo Total", formatBs(state.purchaseCost), highlight = true)

            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(CreateBorder.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.height(12.dp))

            val minimumUnits = state.minimumStockUnits
            val minimumLabel = resolveMinimumStockLabel(state, minimumUnits)
            val baseMinEquivalence = when (resolveMinimumStockControlMode(state)) {
                StockControlMode.INDIVISIBLE -> {
                    when (state.controlType) {
                        CreateProductControlType.UNIDAD -> {
                            if (state.stockEntryMode == CreateProductStockEntryMode.CAJA_CON_PAQUETES) parseCreateProductNumber(
                                state.unitsPerPackageText
                            )
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
            title = "Venta y Alertas", icon = Icons.Outlined.AttachMoney, color = CreateGreen
        ) {
            Text(
                text = "PRESENTACIONES CONFIGURADAS",
                color = CreateTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )

            if (state.presentations.isEmpty()) {
                Text(
                    text = "(Sin presentaciones)",
                    color = CreateTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
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
                            if (isMain) Icon(
                                Icons.Outlined.CheckCircle,
                                null,
                                tint = CreateGreen,
                                modifier = Modifier.size(16.dp)
                            )
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
    title: String, icon: ImageVector, color: Color, content: @Composable ColumnScope.() -> Unit
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
    summary: ProductCreatedSummary, onViewProduct: () -> Unit, onCreateAnother: () -> Unit
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
                    .align(Alignment.CenterHorizontally), contentAlignment = Alignment.Center
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
                        text = summary.category, color = CreateTextSecondary, fontSize = 15.sp
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
                onClick = onCreateAnother, modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
            ) {
                Text("Crear otro")
            }
            Button(
                onClick = onViewProduct, modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
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
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                        pulseScale.animateTo(
                            1.08f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessLow)
                        )

                        // Fase 2: Vuelve a blanco y a su tamaño normal suavemente
                        delay(150)
                        launch {
                            surfaceColor.animateTo(
                                targetValue = Color.White,
                                animationSpec = androidx.compose.animation.core.tween(durationMillis = 400)
                            )
                        }
                        pulseScale.animateTo(
                            1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow)
                        )
                    } else {
                        // Si se selecciona manualmente, no hay animación de IA
                        surfaceColor.snapTo(Color.White)
                        pulseScale.snapTo(1f)
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 118.dp)
                        .graphicsLayer {
                            scaleX = pulseScale.value
                            scaleY = pulseScale.value
                        }
                        .clickable { onSelected(controlType) },
                    color = surfaceColor.value,
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = if (isSelected) 5.dp else 0.dp,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) CreateGreen else CreateBorder
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(46.dp),
                                shape = CircleShape,
                                color = if (isSelected) CreateGreenSoft else CreateSurfaceSoft
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when (controlType) {
                                            CreateProductControlType.UNIDAD -> Icons.Outlined.Inventory2
                                            CreateProductControlType.PESO -> Icons.Outlined.MonitorWeight
                                            CreateProductControlType.LIQUIDO -> Icons.Outlined.Opacity
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) CreateGreen else CreateTextSecondary,
                                        modifier = Modifier.size(25.dp)
                                    )
                                }
                            }

                            Text(
                                text = controlType.label,
                                color = if (isSelected) CreateGreen else CreateTextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1
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
                                    .padding(10.dp)
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
    state: CreateProductState,
    controlType: CreateProductControlType?,
    selected: CreateProductStockEntryMode?,
    onSelected: (CreateProductStockEntryMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val availableModes = remember(controlType) {
        listOf(
            CreateProductStockEntryMode.UNIDAD, CreateProductStockEntryMode.CAJA
        ).filter { isStockEntryModeValidForControlType(it, controlType) }
    }

    val selectedVisualMode = when (selected) {
        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> CreateProductStockEntryMode.CAJA
        else -> selected
    }

    val selectedMeta =
        selectedVisualMode?.takeIf { isStockEntryModeValidForControlType(it, controlType) }
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                availableModes.forEach { mode ->
                    val isCompleted = isStockEntryModeCompleted(
                        state = state, mode = mode
                    )

                    val isCurrentMode = when (mode) {
                        CreateProductStockEntryMode.UNIDAD -> state.stockEntryMode == CreateProductStockEntryMode.UNIDAD

                        CreateProductStockEntryMode.CAJA -> state.stockEntryMode == CreateProductStockEntryMode.CAJA || state.stockEntryMode == CreateProductStockEntryMode.CAJA_CON_PAQUETES

                        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> false
                    }

                    PremiumStockModeCard(
                        controlType = controlType,
                        mode = mode,
                        selected = isCompleted,
                        isCurrentMode = isCurrentMode,
                        onClick = { onSelected(mode) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (selectedMeta != null) {
                PremiumStockModeInfoBox(
                    title = selectedMeta.infoTitle, description = selectedMeta.infoDescription
                )
            }
        }
    }
}

@Composable
fun PremiumStockModeCard(
    controlType: CreateProductControlType?,
    mode: CreateProductStockEntryMode,
    selected: Boolean,
    isCurrentMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val meta = mode.stockModeMeta(controlType)
    val haptic = LocalHapticFeedback.current

    val borderColor = when {
        selected -> CreateGreen
        isCurrentMode -> CreateGreen.copy(alpha = 0.32f)
        else -> CreateBorder
    }

    val borderWidth = when {
        selected -> 2.dp
        isCurrentMode -> 1.4.dp
        else -> 1.dp
    }

    val iconBackground = when {
        selected -> CreateGreenSoft
        isCurrentMode -> CreateGreenUltraSoft
        else -> CreateSurfaceSoft
    }

    val iconTint = when {
        selected -> CreateGreen
        isCurrentMode -> CreateGreen
        else -> CreateTextSecondary
    }

    Surface(
        modifier = modifier
            .wrapContentHeight()
            .heightIn(min = 142.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }),
        shape = RoundedCornerShape(24.dp),
        color = CreateSurface,
        shadowElevation = if (selected) 5.dp else 0.dp,
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(52.dp), shape = CircleShape, color = iconBackground
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = meta.icon,
                        contentDescription = meta.title,
                        tint = iconTint,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }

            Text(
                text = meta.title,
                color = CreateTextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = meta.shortDescription,
                color = CreateTextSecondary,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            AnimatedVisibility(visible = selected) {
                Surface(
                    color = CreateGreen, shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "Listo",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumStockModeInfoBox(
    title: String, description: String, modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CreateSurfaceSoft,
        border = BorderStroke(1.dp, CreateBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(36.dp), shape = CircleShape, color = CreateBlueSoft
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = CreateBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    color = CreateTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Text(
                    text = description,
                    color = CreateTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
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
            }, shortDescription = when (controlType) {
                CreateProductControlType.LIQUIDO -> "Ingreso directo"
                CreateProductControlType.PESO -> "Ingreso directo"
                else -> "Piezas directas"
            }, infoTitle = when (controlType) {
                CreateProductControlType.LIQUIDO -> "Control por frasco"
                CreateProductControlType.PESO -> "Control por envase"
                else -> "Se descuenta una a una"
            }, infoDescription = when (controlType) {
                CreateProductControlType.LIQUIDO -> "Registra frascos sueltos y el contenido de cada uno."
                CreateProductControlType.PESO -> "Registra envases sueltos y el peso de cada uno."
                else -> "Úsalo cuando vendes unidades sueltas o piezas individuales"
            }, icon = Icons.Outlined.Inventory2
        )

        CreateProductStockEntryMode.CAJA -> StockModeMeta(
            title = "Cajas",
            shortDescription = "Con unidades",
            infoTitle = "Ingreso por cajas",
            infoDescription = "Registra cajas completas. Dentro del diálogo podrás indicar si la caja trae unidades directas o paquetes internos.",
            icon = Icons.AutoMirrored.Outlined.Assignment
        )

        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> StockModeMeta(
            title = "Caja +",
            shortDescription = "Con paquetes",
            infoTitle = "Caja con paquetes internos",
            infoDescription = "Úsalo cuando cada caja trae subempaques internos, como sobres, tiras o paquetes.",
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
    val hasHeader =
        !title.isNullOrBlank() || !subtitle.isNullOrBlank() || icon != null || onClear != null

    Surface(
        color = CreateSurface,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = if (isSuccess) 3.dp else 1.dp,
        border = BorderStroke(
            width = if (isSuccess) 1.2.dp else 1.dp, color = if (isSuccess) {
                CreateGreen.copy(alpha = 0.22f)
            } else {
                CreateBorder
            }
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
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSuccess) CreateGreenSoft else CreateSurfaceSoft,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSuccess) CreateGreen.copy(alpha = 0.12f) else CreateBorder
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSuccess) CreateGreen else CreateTextSecondary,
                                        modifier = Modifier.size(24.dp)
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
    state: CreateProductState,
    presentation: CreateProductPresentation,
    unitLabel: String,
    isMain: Boolean,
    error: String?,
    onEquivalenceChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onRemove: () -> Unit
) {

    val quantityBadge = presentationQuantityBadgeText(
        state = state,
        presentation = presentation
    )

    val discountDisplayText = presentationDiscountDisplayText(
        state = state,
        presentation = presentation
    )

    val friendlySubtitleText = presentationFriendlySubtitleText(
        state = state,
        presentation = presentation
    )

    val bloquearEquivalencia =
        state.controlType == CreateProductControlType.LIQUIDO ||
                state.controlType == CreateProductControlType.PESO ||
                isMain ||
                presentation.name.equals("Unidad", ignoreCase = true)

    val isComplete = presentation.equivalenceText.toDoubleOrNull()
        ?.let { it > 0 } == true && presentation.salePriceText.toDoubleOrNull()
        ?.let { it > 0 } == true && error == null

    Surface(
        color = Color.White, shape = RoundedCornerShape(22.dp), border = BorderStroke(
            width = if (isComplete) 2.dp else 1.5.dp,
            color = if (isComplete) CreateGreen else CreateBorder
        ), shadowElevation = if (isComplete) 3.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // ENCABEZADO CON ICONO Y TÍTULO
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono de la presentación
                Surface(
                    color = CreateGreenSoft, shape = CircleShape, modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when {
                                presentation.name.contains(
                                    "blister", true
                                ) -> Icons.Outlined.AllInbox

                                presentation.name.contains(
                                    "caja", true
                                ) -> Icons.Outlined.Inventory2

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = presentation.name,
                            color = CreateTextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (quantityBadge.isNotBlank()) {
                            Surface(
                                color = CreateGreenSoft,
                                shape = RoundedCornerShape(999.dp),
                                border = BorderStroke(
                                    1.dp,
                                    CreateGreen.copy(alpha = 0.20f)
                                )
                            ) {
                                Text(
                                    text = quantityBadge,
                                    color = CreateGreenDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(
                                        horizontal = 9.dp,
                                        vertical = 4.dp
                                    )
                                )
                            }
                        }

                        if (presentation.isAiSuggested) {
                            Surface(
                                color = CreateAiFocusSoft,
                                shape = RoundedCornerShape(999.dp),
                                border = BorderStroke(
                                    1.dp,
                                    CreateAiFocus.copy(alpha = 0.35f)
                                )
                            ) {
                                Text(
                                    text = "IA",
                                    color = CreateTextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(
                                        horizontal = 8.dp,
                                        vertical = 4.dp
                                    )
                                )
                            }
                        }
                    }

                    if (friendlySubtitleText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = friendlySubtitleText,
                            color = CreateTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (presentation.isAiSuggested && !presentation.aiDescription.isNullOrBlank()) {
                        Text(
                            text = presentation.aiDescription,
                            color = CreateTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
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
                                text = "DESCUENTA",
                                color = CreateTextSecondary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp
                            )
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(28.dp),
                                color = CreateBackground,
                                border = BorderStroke(1.dp, CreateBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.Inventory,
                                        null,
                                        tint = CreateTextSecondary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = discountDisplayText.ifBlank {
                                            "${presentation.equivalenceText} $unitLabel"
                                        },
                                        color = CreateTextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    } else {
                        AppTextField(
                            label = "DESCUENTA",
                            value = presentation.equivalenceText,
                            keyboardType = KeyboardType.Decimal,
                            onValueChange = onEquivalenceChange
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    AppTextField(
                        label = "PRECIO VENTA",
                        value = presentation.salePriceText,
                        placeholder = "Ej.50.00",
                        keyboardType = KeyboardType.Decimal,
                        leadingIcon = Icons.Outlined.AttachMoney,
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
                verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedId == presentation.id,
                    onClick = { onSelected(presentation.id) })
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = presentation.name, color = CreateTextPrimary, fontSize = 15.sp
                )
            }
        }
        if (!error.isNullOrBlank()) {
            Text(
                text = error, color = Color(0xFFB42318), fontSize = 13.sp
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isLast = step == CreateProductStep.RESUMEN
            val isFirst = step == CreateProductStep.PRODUCTO

            if (!isFirst) {
                OutlinedButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CreateBorder),
                    enabled = !loading
                ) {
                    Text(
                        text = "Atras", color = CreateTextSecondary, fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(if (isFirst) 1f else 2f)
                    .heightIn(min = if (isLast) 62.dp else 54.dp),
                enabled = nextEnabled && !loading,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CreateGreen,
                    contentColor = Color.White,
                    disabledContainerColor = CreateBorder.copy(alpha = 0.9f),
                    disabledContentColor = CreateTextSecondary.copy(alpha = 0.55f)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Procesando...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    } else {
                        Text(
                            text = if (isLast) "Guardar producto" else nextLabel,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
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
            text = label, color = CreateTextSecondary, fontSize = 15.sp
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
    val accent = if (requiresPrescription) CreateOrange else CreateGreen
    val background = CreateInfoGraySoft

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = background, shape = CircleShape, modifier = Modifier.size(40.dp)
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
        }

    }
}

@Composable
private fun CompactSwitchRow(
    title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = CreateGreenSoft, shape = CircleShape, modifier = Modifier.size(40.dp)
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

        }
        Switch(
            checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(
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
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            color = Color.White,
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(
                width = if (hasFocus || isSuccess) 2.dp else 1.dp, color = when {
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
                    .heightIn(min = 56.dp) // Altura mínima adaptable
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
                    }),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = CreateTextPrimary
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
                                    tint = if (hasFocus) CreateGreen else CreateTextSecondary.copy(
                                        alpha = 0.5f
                                    ),
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
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        container = {})
                })
        }

        // Lógica de Error y Helper
        if (!error.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = error,
                color = CreateRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 12.dp)
            )
        } else if (!helper.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .clickable(enabled = onHelperAction != null) { onHelperAction?.invoke() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (helperHighlighted) {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint = CreateAiFocus,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = helper,
                    color = if (helperHighlighted) CreateAiFocus else CreateTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (helperHighlighted) FontWeight.Bold else FontWeight.Normal,
                    textDecoration = if (onHelperAction != null) TextDecoration.Underline else null
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
    val statusLoading =
        com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.Status.LOADING
    val statusReady =
        com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.Status.READY
    val statusError =
        com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.Status.ERROR

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
                            text = if (state.status == statusLoading) "Leyendo etiqueta…"
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
                            modifier = Modifier.size(18.dp), color = CreateGreen, strokeWidth = 2.dp
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
                val borderColor = if (estaVencido) CreateRed.copy(alpha = 0.45f)
                else CreateGreen.copy(alpha = 0.4f)
                val headerColor = if (estaVencido) CreateRed else CreateGreen
                val headerText = if (estaVencido) "⚠️ Fecha vencida – no se aplicó"
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
                            onClick = onRequestScan, modifier = Modifier.fillMaxWidth()
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
    isSuccess: Boolean,
    error: String?,
    helper: String?,
    helperColor: Color,
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
                text = value, selection = TextRange(value.length)
            )
        )
    }

    LaunchedEffect(value) {
        if (fieldValue.text != value) {
            fieldValue = TextFieldValue(
                text = value, selection = TextRange(value.length)
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
                width = if (enabled && (hasFocus || isSuccess)) 2.dp else 1.dp, color = when {
                    !enabled -> CreateBorder.copy(alpha = 0.5f)
                    error != null -> CreateRed
                    isSuccess -> CreateGreen
                    hasFocus -> CreateGreen
                    else -> CreateBorder
                }
            ),
            shadowElevation = if (enabled && hasFocus) 3.dp else 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable(enabled = enabled) {
                    showDatePicker = true
                }) {
            val interactionSource = remember { MutableInteractionSource() }

            BasicTextField(
                value = fieldValue,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
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
                                color = if (enabled) CreateTextSecondary else CreateTextSecondary.copy(
                                    alpha = 0.3f
                                )
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
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        container = {})
                })
        }

        if (enabled && !error.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = error, color = CreateRed, fontSize = 12.sp, fontWeight = FontWeight.Medium
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
        ExpirationMonthYearDialog(value = value, onDismiss = {
            showDatePicker = false
            focusManager.clearFocus()
        }, onConfirm = { formatted ->
            fieldValue = TextFieldValue(
                text = formatted, selection = TextRange(formatted.length)
            )
            onValueChange(formatted)
            showDatePicker = false
            focusManager.clearFocus()
        })
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
                text = value, selection = TextRange(value.length)
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
                width = if (enabled && (hasFocus || isSuccess)) 2.dp else 1.dp, color = when {
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
            }) {
            val interactionSource = remember { MutableInteractionSource() }

            BasicTextField(
                value = fieldValue,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .onFocusChanged { focusState ->
                        hasFocus = focusState.isFocused
                        // V30.8: Forzar apertura del selector al ganar foco para evitar bloqueos
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
                                color = if (enabled) CreateTextSecondary else CreateTextSecondary.copy(
                                    alpha = 0.3f
                                )
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
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        container = {})
                })
        }

        if (enabled && !error.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = error, color = CreateRed, fontSize = 12.sp, fontWeight = FontWeight.Medium
            )
        } else if (enabled && !helper.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = helper, color = helperColor, fontSize = 10.sp, fontWeight = FontWeight.Medium
            )
        }
    }

    if (showDatePicker) {
        FullDatePickerDialog(value = value, onDismiss = {
            showDatePicker = false
            focusManager.clearFocus()
        }, onConfirm = { formatted ->
            fieldValue = TextFieldValue(text = formatted, selection = TextRange(formatted.length))
            onValueChange(formatted)
            showDatePicker = false
            focusManager.clearFocus()
        })
    }
}

@Composable
private fun FullDatePickerDialog(
    value: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PremiumGradient)
            ) {
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
                        Text(
                            "Año:",
                            color = CreateTextSecondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(40.dp)
                        )
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
                        Text(
                            "Mes:",
                            color = CreateTextSecondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(40.dp)
                        )
                        val monthName = YearMonth.of(
                            selectedYear, selectedMonth
                        ).month.getDisplayName(java.time.format.TextStyle.FULL, Locale("es", "ES"))
                            .replaceFirstChar { it.uppercase() }

                        IconButton(onClick = {
                            if (selectedMonth > 1) selectedMonth-- else selectedMonth = 12
                        }) {
                            Icon(
                                Icons.Default.ChevronRight,
                                null,
                                modifier = Modifier.graphicsLayer { rotationZ = 180f })
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

                        IconButton(onClick = {
                            if (selectedMonth < 12) selectedMonth++ else selectedMonth = 1
                        }) {
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
                                border = if (isSelected) null else BorderStroke(
                                    1.dp, Color.White.copy(alpha = 0.5f)
                                )
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
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
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
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
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



/**
 * Convierte un texto "MM/AA" a millis UTC para inicializar el DatePicker.
 * Si el texto estÃ¡ vacÃ­o o es invÃ¡lido, retorna la fecha actual.
 */
private fun parseExpirationToMillis(
    value: String, today: java.time.LocalDate
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


    var manualCategorySession by remember(productName) {
        mutableStateOf(false)
    }

    // 1. NUEVO: Creamos un requester especÃ­fico para la tarjeta de carga
    val loadingRequester = remember { BringIntoViewRequester() }

    val suggestedControlType = remember(
        suggestionState.sugerenciaTipoManual,
        value,
        suggestionState.suggestion,
        categorySelectedFromAi
    ) {
        if (categorySelectedFromAi) {
            // Revisamos primero la sugerencia principal de Gemini
            val tipoPrincipal =
                suggestionState.suggestion?.tipoControl?.toCreateProductControlTypeOrNull()
            if (tipoPrincipal != null) {
                tipoPrincipal
            } else {
                // Si no, caemos en la sugerencia secundaria
                suggestionState.sugerenciaTipoManual?.takeIf { it.confianza == ConfianzaIA.ALTA }?.tipo?.toCreateProductControlTypeOrNull()
            }
        } else null
    }



    Column(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val isAnalyzing =
                suggestionState.status == CategorySuggestionStatus.LOADING || suggestionState.estaCargandoAsistencia
            val isReady = suggestionState.status == CategorySuggestionStatus.READY
            val isManual =
                suggestionState.status == CategorySuggestionStatus.MANUAL || suggestionState.status == CategorySuggestionStatus.FALLBACK_MANUAL
            val isInitial = suggestionState.status == CategorySuggestionStatus.INITIAL

            // 1. ESTADO DE CARGA / ANÁLISIS
            AnimatedVisibility(
                visible = isAnalyzing, enter = fadeIn(), exit = fadeOut()
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

            // 2. SUGERENCIA IA (TARJETA A ANCHO COMPLETO CON SEPARADOR "O")
            AnimatedVisibility(
                visible = isReady && suggestionState.suggestion != null && !isManual && !manualCategorySession && value.isBlank(),
                enter = fadeIn() + slideInVertically { it / 4 },
                exit = fadeOut() + shrinkVertically()
            ) {
                suggestionState.suggestion?.let { suggestion ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CATEGORÍA SUGERIDA",
                            color = CreateTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(start = 4.dp)
                        )

                        // TARJETA DE SELECCIÓN PROFESIONAL
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    manualCategorySession = true
                                    onCategorySelectedFromSuggestion(suggestion.categoria)
                                },
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 2.dp,
                            border = BorderStroke(1.5.dp, CreateGreen.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = suggestion.categoria,
                                    color = CreateGreen,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            CreateGreen.copy(alpha = 0.1f), CircleShape
                                        )
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "ESCOGER",
                                        color = CreateGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        // SECCIÓN DE OPCIÓN MANUAL (CON LA "O" EN EL MEDIO)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "O",
                                color = Color.Black,
                                fontWeight = FontWeight.Black, // Color negra bold
                                fontSize = 14.sp
                            )

                            TextButton(
                                onClick = {
                                    manualCategorySession = true
                                    onSwitchToManual()
                                }, contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = "Escribir manualmente",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CreateTextSecondary,
                                    textDecoration = TextDecoration.Underline // Un toque extra de estilo para que parezca un link
                                )
                            }
                        }
                    }
                }
            }

            // 3. ENTRADA MANUAL (SÓLO SI SE SOLICITA O SI YA HAY UN VALOR SELECCIONADO/ESCRITO)
            // Se muestra si el usuario lo pide (isManual) o si ya tenemos un valor (value no vacío)
            val showManualEntry = isManual || value.isNotBlank() || manualCategorySession
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
                    showBackToAi = false,
                    onValueChange = onValueChange,
                    onCategorySelectedFromSuggestion = { selected ->
                        manualCategorySession = true

                        onCategorySelectedFromSuggestion(selected)
                        focusManager.clearFocus(force = true)
                        keyboardController?.hide()
                    },
                    onManualInteraction = {
                        manualCategorySession = true
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
                exit = fadeOut()
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
    onManualInteraction: () -> Unit = {},
    onBackToAi: () -> Unit,
    suggestionState: com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState = com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState(),
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

    val suggestions = remember(
        value,
        hasCategoryFocus,
        options,
        suggestionState.asistenciaManualCategorias,
        suggestionState.suggestion,
        suppressSuggestions
    ) {
        val normalizedValue = value.trim().stripAccents().lowercase()

        val mainAiResult = suggestionState.suggestion?.categoria
        val aiOptions =
            (listOfNotNull(mainAiResult) + suggestionState.asistenciaManualCategorias).filter { it.isNotBlank() }
                .distinctBy { it.trim().stripAccents().lowercase() }

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
            delay(400)
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
            // CORRECCIÓN: El icono solo aparece si el campo está vacío
            leadingIcon = if (value.isEmpty()) leadingIcon else null,
            trailingIcon = trailingIcon ?: if (value.isNotBlank() && error == null) {
                {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        null,
                        tint = CreateGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else null,
            onValueChange = { newValue ->
                onManualInteraction()
                onValueChange(newValue)
            })

        // Sugerencias integradas
        AnimatedVisibility(
            visible = suggestions.isNotEmpty(),
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = fadeOut()
        ) {
            Column(modifier = Modifier.bringIntoViewRequester(suggestionsRequester)) {
                if (!hasCategoryFocus && value.isBlank() && mainAiResult != null && suggestions.contains(
                        mainAiResult
                    )
                ) {
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
                            val isAiSuggestion = mainAiResult != null && option.equals(
                                mainAiResult, ignoreCase = true
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onManualInteraction()
                                        onCategorySelectedFromSuggestion(option)
                                        hasCategoryFocus = false
                                        focusManager.clearFocus(force = true)
                                        keyboardController?.hide()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isAiSuggestion) Icons.Outlined.Lightbulb else Icons.AutoMirrored.Outlined.Label,
                                    contentDescription = null,
                                    tint = if (isAiSuggestion) CreateGreen else CreateTextSecondary.copy(
                                        alpha = 0.5f
                                    ),
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
private fun Modifier.graphicsScale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
    })

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
                    interactionSource = remember { MutableInteractionSource() }, indication = null
                ) {
                    if (options.isNotEmpty()) {
                        focusManager.clearFocus(force = true)
                        keyboardController?.hide()
                        expanded = true
                    }
                }, color = Color.White, shape = RoundedCornerShape(18.dp), border = BorderStroke(
                1.dp, if (!error.isNullOrBlank()) Color(0xFFB42318) else CreateBorder
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
                text = error, color = Color(0xFFB42318), fontSize = 13.sp
            )
        } else if (!helper.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = helper, color = CreateTextSecondary, fontSize = 13.sp
            )
        }

        if (expanded) {
            AlertDialog(onDismissRequest = { expanded = false }, confirmButton = {}, text = {
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
                            fontSize = 16.sp)
                    }
                }
            })
        }

    }
}

private fun resolveReceivedPresentationFactor(
    state: CreateProductState, presentationName: String
): Double {
    val fromState = state.presentations.firstOrNull {
        it.name.equals(presentationName, ignoreCase = true)
    }?.equivalenceText?.replace(",", ".")?.toDoubleOrNull()
    if (fromState != null && fromState > 0.0) return fromState
    return defaultEquivalenceForPresentation(state.controlType, presentationName).toDouble()
}

private fun defaultEquivalenceForPresentation(
    controlType: CreateProductControlType?, presentationName: String
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
// un único punto decimal o coma (normalizada a punto).
private fun sanitizeDecimalInput(raw: String): String {
    val sb = StringBuilder(raw.length)
    var decimalSeen = false
    for (ch in raw) {
        when {
            ch.isDigit() -> sb.append(ch)
            (ch == '.' || ch == ',') && !decimalSeen -> {
                sb.append('.')
                decimalSeen = true
            }
        }
    }
    return sb.toString()
}

private fun parseCreateProductNumber(value: String): Double {
    val clean = value.trim().replace(" ", "").replace("\u00A0", "")

    if (clean.isBlank()) return 0.0

    val normalized = when {
        clean.contains(".") && clean.contains(",") -> {
            // Formato latino: 1.250,50 -> 1250.50
            if (clean.lastIndexOf(",") > clean.lastIndexOf(".")) {
                clean.replace(".", "").replace(",", ".")
            } else {
                // Formato inglés: 1,250.50 -> 1250.50
                clean.replace(",", "")
            }
        }

        clean.contains(",") -> {
            // 1250,50 -> 1250.50
            clean.replace(",", ".")
        }

        else -> clean
    }

    return normalized.toDoubleOrNull() ?: 0.0
}

fun calculateTotalBaseStock(state: CreateProductState): Double {
    val rawAmount = when (state.stockEntryMode) {
        null -> 0.0

        CreateProductStockEntryMode.UNIDAD -> {
            val quantity = parseCreateProductNumber(state.receivedUnitsText)
            val content = parseCreateProductNumber(state.unitsPerItemText)

            when (state.controlType) {
                CreateProductControlType.UNIDAD -> {
                    if (state.stockControlMode == StockControlMode.DIVISIBLE && content > 0.0) {
                        quantity * content
                    } else {
                        quantity
                    }
                }

                CreateProductControlType.LIQUIDO, CreateProductControlType.PESO -> {
                    if (quantity <= 0.0 || content <= 0.0) {
                        0.0
                    } else {
                        quantity * content
                    }
                }

                null -> 0.0
            }
        }

        CreateProductStockEntryMode.CAJA -> {
            val boxes = parseCreateProductNumber(state.boxesReceivedText)
            val itemsPerBox = parseCreateProductNumber(state.unitsPerBoxText)
            val unitsPerItem = parseCreateProductNumber(state.unitsPerItemText)

            when (state.controlType) {
                CreateProductControlType.UNIDAD -> {
                    if (boxes <= 0.0 || itemsPerBox <= 0.0) {
                        0.0
                    } else if (state.stockControlMode == StockControlMode.DIVISIBLE && unitsPerItem > 0.0) {
                        boxes * itemsPerBox * unitsPerItem
                    } else {
                        boxes * itemsPerBox
                    }
                }

                CreateProductControlType.LIQUIDO, CreateProductControlType.PESO -> {
                    if (boxes <= 0.0 || itemsPerBox <= 0.0 || unitsPerItem <= 0.0) {
                        0.0
                    } else {
                        boxes * itemsPerBox * unitsPerItem
                    }
                }

                null -> 0.0
            }
        }

        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
            val boxes = parseCreateProductNumber(state.boxesReceivedText)
            val packagesPerBox = parseCreateProductNumber(state.packagesPerBoxText)
            val unitsPerPackage = parseCreateProductNumber(state.unitsPerPackageText)

            if (boxes <= 0.0 || packagesPerBox <= 0.0 || unitsPerPackage <= 0.0) {
                0.0
            } else {
                boxes * packagesPerBox * unitsPerPackage
            }
        }
    }

    return when {
        state.controlType == CreateProductControlType.PESO && state.stockEntryUnit == "kg" -> rawAmount * 1000.0

        state.controlType == CreateProductControlType.LIQUIDO && state.stockEntryUnit == "L" -> rawAmount * 1000.0

        else -> rawAmount
    }
}

fun calculateBaseMinimumStock(state: CreateProductState): Double {
    val selectedUnits = state.minimumStockUnits.takeIf { it > 0 }?.toDouble()
        ?: parseCreateProductNumber(state.minimumStockText).takeIf { it > 0.0 } ?: 0.0
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
            CreateProductControlType.LIQUIDO, CreateProductControlType.PESO -> {
                val contenidoVisual =
                    parseCreateProductNumber(state.unitsPerItemText).takeIf { it > 0.0 } ?: 1.0

                val contenidoBase = when {
                    state.controlType == CreateProductControlType.PESO && state.stockEntryUnit == "kg" -> contenidoVisual * 1000.0

                    state.controlType == CreateProductControlType.LIQUIDO && state.stockEntryUnit == "L" -> contenidoVisual * 1000.0

                    else -> contenidoVisual
                }

                selectedUnits * contenidoBase
            }

            CreateProductControlType.UNIDAD -> when (state.stockEntryMode) {
                CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
                    val unidadesPorPaquete =
                        parseCreateProductNumber(state.unitsPerPackageText).takeIf { it > 0.0 }
                            ?: 1.0

                    selectedUnits * unidadesPorPaquete
                }

                CreateProductStockEntryMode.CAJA -> {
                    val unitsPerItem =
                        parseCreateProductNumber(state.unitsPerItemText).takeIf { it > 0.0 } ?: 1.0

                    if (state.stockControlMode == StockControlMode.DIVISIBLE) {
                        selectedUnits * unitsPerItem
                    } else {
                        selectedUnits
                    }
                }

                CreateProductStockEntryMode.UNIDAD -> {
                    val unitsPerItem =
                        parseCreateProductNumber(state.unitsPerItemText).takeIf { it > 0.0 } ?: 1.0

                    if (state.stockControlMode == StockControlMode.DIVISIBLE) {
                        selectedUnits * unitsPerItem
                    } else {
                        selectedUnits
                    }
                }

                else -> selectedUnits
            }

            null -> selectedUnits
        }
    }.coerceAtLeast(0.0)
}


private fun parseCreateProductDouble(value: String): Double {
    return value
        .trim()
        .replace(",", ".")
        .toDoubleOrNull() ?: 0.0
}


fun getPurchasePresentationName(
    controlType: CreateProductControlType?, mode: CreateProductStockEntryMode?
): String {
    return when (mode) {
        CreateProductStockEntryMode.UNIDAD -> when (controlType) {
            CreateProductControlType.LIQUIDO -> "Frasco"
            CreateProductControlType.PESO -> "Empaque"
            else -> "Unidad"
        }

        CreateProductStockEntryMode.CAJA -> "Caja"

        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> "Caja"

        null -> "Unidad"
    }
}


fun isStockEntryModeValidForControlType(
    mode: CreateProductStockEntryMode?, controlType: CreateProductControlType?
): Boolean {
    if (mode == null || controlType == null) return false
    return when (controlType) {
        CreateProductControlType.LIQUIDO, CreateProductControlType.PESO -> mode != CreateProductStockEntryMode.CAJA_CON_PAQUETES

        CreateProductControlType.UNIDAD -> true
    }
}

fun isStockEntryModeValidForControlType(
    controlType: CreateProductControlType?, mode: CreateProductStockEntryMode?
): Boolean = isStockEntryModeValidForControlType(mode, controlType)

fun CreateProductState.resetStockEntryConfiguration(): CreateProductState {
    val isAnchored = barcodeAiApplied && !unitsPerItemText.isBlank()

    return copy(
        stockEntryMode = null,
        receivedUnitsText = "",
        boxesReceivedText = "",
        unitsPerBoxText = "",
        packagesPerBoxText = "",
        unitsPerPackageText = "",
        unitsPerItemText = if (isAnchored) unitsPerItemText else "",
        stockEntryUnit = if (isAnchored) stockEntryUnit else "",
        minimumStockText = "",
        minimumStockUnit = "",
        minimumStockUnits = 0,
        purchaseCost = "",
        stockEntryConfigured = false,
        mismatchJustified = false,
        mismatchReason = null,
        errors = errors - "stockEntryMode" - "purchaseCost" - "minimumStock"
    )
}

/**
 * V31.9: Limpia el estado de conciliación de forma segura.
 * Se usa cada vez que el usuario cambia una cantidad o unidad que altera el cálculo.
 */
private fun CreateProductState.clearMismatchJustification(): CreateProductState {
    return copy(
        mismatchJustified = false, mismatchReason = null
    )
}

/**
 * V31.10: Limpia el estado completo para iniciar un nuevo escaneo desde cero.
 * Elimina toda la "basura" del producto anterior: inventario, lote, proveedor, auditoría, etc.
 */
private fun CreateProductState.resetForNewBarcodeScan(): CreateProductState {
    return copy(
        currentStep = CreateProductStep.PRODUCTO,

        name = "",
        barcode = "",
        category = "",
        location = "",
        controlType = null,
        typeSelectedManually = false,
        categorySelectedFromAi = false,
        categoryStatus = null,
        requiresPrescription = false,
        duplicateProductFound = null,

        forceManualProductEntry = false,
        barcodeAiResult = null,
        barcodeAiError = null,
        barcodeAiApplied = false,
        scannedImageBase64 = null,
        isBarcodeManualMode = false,
        barcodeMismatchDetected = false,
        barcodeMismatchOriginalName = null,
        productoSinCodigoBarra = false,

        defaultSalePresentationName = "",
        defaultSalePresentationFromAi = false,
        saleByFractionSuggested = null,
        defaultSalePresentationConfidence = 0,
        defaultSalePresentationReason = "",

        aiUsageInfo = null,
        aiUsageInfoFetchedFor = "",
        isFetchingUsageInfo = false,

        lotNumber = "",
        expirationDate = "",
        lotScanned = false,
        invoiceNumber = "",
        invoiceImageBase64 = null,
        isCapturingInvoice = false,
        paymentCondition = "CONTADO",
        paymentDueDate = "",
        supplierId = "",
        supplierName = "",
        purchaseCost = "",

        receivedPresentation = "",
        receivedQuantity = "",
        stockEntryMode = null,
        receivedUnitsText = "",
        boxesReceivedText = "",
        unitsPerBoxText = "",
        packagesPerBoxText = "",
        unitsPerPackageText = "",
        unitsPerItemText = "",
        stockEntryUnit = "",
        stockEntryConfigured = false,
        showStockEntryDialog = false,

        minimumStockText = "",
        minimumStockUnit = "",
        minimumStockPercent = 20,
        minimumStockUnits = 0,
        stockControlMode = null,

        presentations = emptyList(),
        mainPresentationId = "",
        addPresentationExpanded = false,
        draftPresentationName = "",
        draftPresentationCustomMode = false,
        draftPresentationCustomAmount = "",
        draftPresentationCustomUnit = "",
        draftPresentationSalePriceText = "",
        mismatchJustified = false,
        mismatchReason = null,
        showStep3ResetDialog = false,
        pendingStockEntryMode = null,
        pendingControlType = null,
        showLossConfirmDialog = false,
        showLowMarginConfirmDialog = false,
        lotConflictProduct = null,
        showLotConflictDialog = false,
        showAddSupplierDialog = false,
        supplierSaveSuccess = false,
        isSupplierHighlighting = false,
        isInvoiceHighlighting = false,

        errors = emptyMap()
    )
}

fun formatCreateProductNumber(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
    }
}

/**
 * V31.9: Funciones unificadas de parseo IA para evitar fallos por formatos sucios.
 */
private fun parseAiNumberOrNull(raw: String?): Double? {
    return raw?.trim()?.replace(",", ".")?.let { Regex("""\d+(?:\.\d+)?""").find(it)?.value }
        ?.toDoubleOrNull()
}

fun parseAiMultiplier(raw: String?): Double {
    return parseAiNumberOrNull(raw) ?: 1.0
}

internal fun normalizeAiUnit(raw: String?): String {
    return raw
        ?.trim()
        ?.lowercase(Locale.getDefault())
        ?.replace(".", "")
        .orEmpty()
}

internal fun isPharmaPotencyUnit(unit: String?): Boolean {
    val clean = normalizeAiUnit(unit)
        .replace(" ", "")

    return clean in setOf(
        "mg",
        "mcg",
        "ug",
        "ui",
        "iu",
        "%"
    ) ||
            clean.contains("mg/") ||
            clean.contains("mcg/") ||
            clean.contains("ug/") ||
            clean.contains("ui/") ||
            clean.contains("iu/") ||
            clean.contains("%")
}

internal fun hasPharmaDosageContext(name: String): Boolean {
    val clean = name
        .lowercase(Locale.getDefault())
        .replace("á", "a")
        .replace("é", "e")
        .replace("í", "i")
        .replace("ó", "o")
        .replace("ú", "u")

    return listOf(
        "tableta",
        "tabletas",
        "tab",
        "capsula",
        "capsulas",
        "cap",
        "comprimido",
        "comprimidos",
        "jarabe",
        "suspension",
        "gotas",
        "ampolla",
        "inyectable",
        "antibiotico",
        "analgesico",
        "antiinflamatorio",
        "paracetamol",
        "ibuprofeno",
        "amoxicilina",
        "azitromicina",
        "diclofenaco",
        "loratadina"
    ).any { it in clean }
}

internal fun shouldRejectAiContentAsInventory(
    productName: String,
    controlType: CreateProductControlType?,
    contentUnit: String?
): Boolean {
    val unitClean = normalizeAiUnit(contentUnit)

    // mg, mcg, UI, IU y % suelen representar dosis/concentración,
    // no contenido físico inventariable.
    if (isPharmaPotencyUnit(unitClean)) return true

    // Si el producto es de control UNIDAD y tiene contexto farmacéutico,
    // no aceptar unidades de potencia como contenido del envase.
    if (
        controlType == CreateProductControlType.UNIDAD &&
        hasPharmaDosageContext(productName)
    ) {
        return unitClean in setOf("mg", "mcg", "ug", "ui", "iu", "%")
    }

    return false
}

internal fun safeInventoryContentValue(
    productName: String,
    controlType: CreateProductControlType?,
    value: String?,
    unit: String?
): String {
    return if (shouldRejectAiContentAsInventory(productName, controlType, unit)) {
        ""
    } else {
        value.orEmpty()
    }
}

internal fun safeInventoryContentUnit(
    productName: String,
    controlType: CreateProductControlType?,
    value: String?,
    unit: String?
): String {
    return if (shouldRejectAiContentAsInventory(productName, controlType, unit)) {
        ""
    } else {
        unit.orEmpty()
    }
}

fun parseAiContent(raw: String?): Double {
    val clean = raw?.trim()?.lowercase(Locale.getDefault()) ?: return 0.0

    // V31.95: Refuerzo para contenido. Si el texto trae unidades métricas,
    // buscamos específicamente el número que está pegado a ellas.
    val metricMatch = Regex("""(\d+(?:[.,]\d+)?)\s*(?:g|gr|ml|l|kg)""").find(clean)
    if (metricMatch != null) {
        return metricMatch.groupValues[1].replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    return parseAiNumberOrNull(raw) ?: 0.0
}

private fun formatCreateProductMoney(value: Double): String {
    return MonedaHelper.formatear(value)
}

private fun formatCreateProductPercent(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}

private fun parseDecimalSafe(value: String): Double {
    return value.trim().replace(
        com.app.administradorfarmadon.ClasesDatabase.SessionManager.monedaSimbolo,
        "",
        ignoreCase = true
    ).replace(",", ".").replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
}

private data class ProfitGuardResult(
    val costoPresentacion: Double,
    val ganancia: Double,
    val margenPercent: Double,
    val color: Color,
    val mensaje: String
)

private fun calculateProfitGuard(
    costoTotal: Double, stockTotalBase: Double, precioVenta: Double, equivalenciaBase: Double
): ProfitGuardResult? {
    if (costoTotal <= 0.0 || stockTotalBase <= 0.0 || precioVenta <= 0.0 || equivalenciaBase <= 0.0) {
        return null
    }

    val costoUnitarioBase = costoTotal / stockTotalBase
    val costoPresentacion = costoUnitarioBase * equivalenciaBase
    val ganancia = precioVenta - costoPresentacion
    val margenPercent = (ganancia / precioVenta) * 100.0

    val color = when {
        ganancia < 0.0 -> CreateRed
        margenPercent < 10.0 -> CreateOrange
        margenPercent < 20.0 -> CreateOrange
        else -> CreateGreen
    }

    val mensaje = when {
        ganancia < 0.0 -> "Estás vendiendo con pérdida"
        margenPercent < 10.0 -> "Margen muy bajo"
        margenPercent < 20.0 -> "Margen ajustado"
        else -> "Margen saludable"
    }

    return ProfitGuardResult(
        costoPresentacion = costoPresentacion,
        ganancia = ganancia,
        margenPercent = margenPercent,
        color = color,
        mensaje = mensaje
    )
}


private fun hasAnyPresentationLoss(
    state: CreateProductState
): Boolean {
    val costoTotal = parseDecimalSafe(state.purchaseCost)
    val stockTotalBase = calculateTotalBaseStock(state)

    if (costoTotal <= 0.0 || stockTotalBase <= 0.0) return false

    return state.presentations.any { presentation ->
        val precioVenta = parseDecimalSafe(presentation.salePriceText)
        val equivalenciaBase = parseDecimalSafe(presentation.equivalenceText)

        val profitGuard = calculateProfitGuard(
            costoTotal = costoTotal,
            stockTotalBase = stockTotalBase,
            precioVenta = precioVenta,
            equivalenciaBase = equivalenciaBase
        )

        profitGuard?.ganancia?.let { it < 0.0 } == true
    }
}

private fun hasAnyPresentationLowMargin(
    state: CreateProductState, minMarginPercent: Double = 10.0
): Boolean {
    val costoTotal = parseDecimalSafe(state.purchaseCost)
    val stockTotalBase = calculateTotalBaseStock(state)

    if (costoTotal <= 0.0 || stockTotalBase <= 0.0) return false

    return state.presentations.any { presentation ->
        val precioVenta = parseDecimalSafe(presentation.salePriceText)
        val equivalenciaBase = parseDecimalSafe(presentation.equivalenceText)

        val profitGuard = calculateProfitGuard(
            costoTotal = costoTotal,
            stockTotalBase = stockTotalBase,
            precioVenta = precioVenta,
            equivalenciaBase = equivalenciaBase
        )

        profitGuard != null && profitGuard.ganancia >= 0.0 && profitGuard.margenPercent < minMarginPercent
    }
}

private fun normalizePresentationNameForDuplicate(name: String): String {
    val clean = java.text.Normalizer.normalize(
        name.trim().lowercase(Locale.getDefault()), java.text.Normalizer.Form.NFD
    ).replace(Regex("\\p{M}+"), "").replace(Regex("[^a-z0-9]+"), " ").trim()

    return if (clean.endsWith("s") && clean.length > 3) {
        clean.dropLast(1)
    } else {
        clean
    }
}

private fun parsePresentationDouble(value: String): Double {
    return value.trim().replace(",", ".").toDoubleOrNull() ?: 0.0
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
        "tableta", "tabletas", "comprimido", "comprimidos", "pastilla", "pastillas", "pildora", "pildoras", "capsula", "capsulas", "perla", "perlas", "unidad", "unidades", "pieza", "piezas" -> "unidad-pieza"

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
    existentes: List<CreateProductPresentation>, nueva: CreateProductPresentation
): Pair<List<CreateProductPresentation>, String> {
    val grupoNueva = grupoSinonimoPresentacion(nueva.name)
    val equivNueva = nueva.equivalenceText.trim().replace(",", ".").toDoubleOrNull()

    val match = existentes.firstOrNull { existing ->
        val mismoNombre = existing.name.equals(nueva.name, ignoreCase = true)
        val mismoGrupo =
            grupoNueva != null && grupoSinonimoPresentacion(existing.name) == grupoNueva
        val mismaEquivalencia =
            equivNueva != null && existing.equivalenceText.trim().replace(",", ".")
                .toDoubleOrNull() == equivNueva
        mismoNombre || mismoGrupo || mismaEquivalencia
    }

    return if (match != null) {
        val precioPrevio = match.salePriceText.ifBlank { nueva.salePriceText }
        val reemplazada = nueva.copy(
            id = match.id, salePriceText = precioPrevio
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
                CreateProductControlType.LIQUIDO, CreateProductControlType.PESO -> when (state.stockEntryMode) {
                    CreateProductStockEntryMode.UNIDAD -> parseCreateProductNumber(state.receivedUnitsText).toInt()
                    CreateProductStockEntryMode.CAJA -> (parseCreateProductNumber(state.boxesReceivedText) * parseCreateProductNumber(
                        state.unitsPerBoxText
                    )).toInt()

                    CreateProductStockEntryMode.CAJA_CON_PAQUETES -> (parseCreateProductNumber(state.boxesReceivedText) * (parseCreateProductNumber(
                        state.packagesPerBoxText
                    ).takeIf { it > 0 } ?: 1.0)).toInt()

                    null -> 0
                }

                CreateProductControlType.UNIDAD -> when (state.stockEntryMode) {
                    CreateProductStockEntryMode.UNIDAD -> parseCreateProductNumber(state.receivedUnitsText).toInt()
                    CreateProductStockEntryMode.CAJA -> (parseCreateProductNumber(state.boxesReceivedText) * parseCreateProductNumber(
                        state.unitsPerBoxText
                    )).toInt()

                    CreateProductStockEntryMode.CAJA_CON_PAQUETES -> (parseCreateProductNumber(state.boxesReceivedText) * (parseCreateProductNumber(
                        state.packagesPerBoxText
                    ).takeIf { it > 0 } ?: 1.0)).toInt()

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
fun calculateValidStockOptions(
    totalPhysicalUnits: Int, mode: StockControlMode
): List<Int> {
    if (totalPhysicalUnits <= 0) return emptyList()

    // Si solo hay 1 unidad, no hay otra alerta posible.
    if (totalPhysicalUnits == 1) return listOf(1)

    val minimumAllowed = kotlin.math.ceil(totalPhysicalUnits * 0.10).toInt().coerceAtLeast(1)

    val maximumAllowed =
        kotlin.math.floor(totalPhysicalUnits * 0.50).toInt().coerceAtLeast(minimumAllowed)
            .coerceAtMost(totalPhysicalUnits - 1)

    val options = mutableListOf<Int>()

    when (mode) {
        StockControlMode.INDIVISIBLE -> {
            for (i in minimumAllowed..maximumAllowed) {
                options.add(i)
            }
        }

        StockControlMode.DIVISIBLE -> {
            val step = minimumAllowed
            var current = minimumAllowed

            while (current <= maximumAllowed) {
                options.add(current)
                current += step
            }

            if (options.isEmpty()) {
                options.add(minimumAllowed)
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
                        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> if (isPlural) "paquetes" else "paquete"

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
        CreateProductControlType.LIQUIDO, CreateProductControlType.PESO -> StockControlMode.INDIVISIBLE

        CreateProductControlType.UNIDAD -> when (state.stockEntryMode) {
            CreateProductStockEntryMode.CAJA_CON_PAQUETES -> StockControlMode.DIVISIBLE
            CreateProductStockEntryMode.UNIDAD, CreateProductStockEntryMode.CAJA, null -> StockControlMode.INDIVISIBLE
        }

        null -> StockControlMode.INDIVISIBLE
    }
}

private fun isStockEntryModeCompleted(
    state: CreateProductState, mode: CreateProductStockEntryMode
): Boolean {
    if (!state.stockEntryConfigured) return false

    return when (mode) {
        CreateProductStockEntryMode.UNIDAD -> {
            if (state.stockEntryMode != CreateProductStockEntryMode.UNIDAD) return false

            val quantityOk =
                state.receivedUnitsText.isNotBlank() && (state.receivedUnitsText.toDoubleOrNull()
                    ?: 0.0) > 0.0

            val needsContent =
                state.controlType == CreateProductControlType.LIQUIDO || state.controlType == CreateProductControlType.PESO || (state.controlType == CreateProductControlType.UNIDAD && state.stockControlMode == StockControlMode.DIVISIBLE)

            val contentOk =
                state.unitsPerItemText.isNotBlank() && (parseCreateProductNumber(state.unitsPerItemText)) > 0.0

            quantityOk && (!needsContent || contentOk)
        }

        CreateProductStockEntryMode.CAJA -> {
            val isCajaMode =
                state.stockEntryMode == CreateProductStockEntryMode.CAJA || state.stockEntryMode == CreateProductStockEntryMode.CAJA_CON_PAQUETES

            if (!isCajaMode) return false

            val boxesOk =
                state.boxesReceivedText.isNotBlank() && (state.boxesReceivedText.toDoubleOrNull()
                    ?: 0.0) > 0.0

            if (state.stockEntryMode == CreateProductStockEntryMode.CAJA_CON_PAQUETES) {
                val packagesOk =
                    state.packagesPerBoxText.isNotBlank() && (state.packagesPerBoxText.toDoubleOrNull()
                        ?: 0.0) > 0.0

                val unitsPerPackageOk =
                    state.unitsPerPackageText.isNotBlank() && (state.unitsPerPackageText.toDoubleOrNull()
                        ?: 0.0) > 0.0

                boxesOk && packagesOk && unitsPerPackageOk
            } else {
                val unitsPerBoxOk =
                    state.unitsPerBoxText.isNotBlank() && (state.unitsPerBoxText.toDoubleOrNull()
                        ?: 0.0) > 0.0

                val needsContent =
                    state.controlType == CreateProductControlType.LIQUIDO || state.controlType == CreateProductControlType.PESO || (state.controlType == CreateProductControlType.UNIDAD && state.stockControlMode == StockControlMode.DIVISIBLE)

                val contentOk =
                    state.unitsPerItemText.isNotBlank() && parseCreateProductNumber(state.unitsPerItemText) > 0.0

                boxesOk && unitsPerBoxOk && (!needsContent || contentOk)
            }
        }

        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> false
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
                    if (singular) "paquete" else "paquetes"
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
    return calculateInitialStockValue(state).toInt().coerceAtLeast(0)
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
            val visualTotal =
                if (state.stockEntryUnit == "kg" || state.stockEntryUnit == "L") total / 1000.0 else total

            if (state.controlType == CreateProductControlType.UNIDAD) {
                if (content > 1.0) {
                    "Recibiste ${formatCreateProductNumber(quantity)} ${itemLabel(quantity)} (${
                        formatCreateProductNumber(
                            content
                        )
                    } u. c/u). Total: ${formatCreateProductNumber(visualTotal)} $unit."
                } else {
                    "Recibiste ${formatCreateProductNumber(quantity)} $unit."
                }
            } else {
                "Recibiste ${formatCreateProductNumber(quantity)} ${itemLabel(quantity)} de ${
                    formatCreateProductNumber(
                        content
                    )
                } ${state.stockEntryUnit.ifBlank { unit }}."
            }
        }

        CreateProductStockEntryMode.CAJA -> {
            val boxes = parseCreateProductNumber(state.boxesReceivedText)
            val unitsPerBox = parseCreateProductNumber(state.unitsPerBoxText)
            val total = calculateTotalBaseStock(state)
            val visualTotal =
                if (state.stockEntryUnit == "kg" || state.stockEntryUnit == "L") total / 1000.0 else total

            if (boxes <= 0.0 || unitsPerBox <= 0.0) return ""

            "Recibiste ${formatCreateProductNumber(boxes)} ${
                plural(
                    boxes, "caja", "cajas"
                )
            } con ${formatCreateProductNumber(unitsPerBox)} ${itemLabel(unitsPerBox)} cada una. Total: ${
                formatCreateProductNumber(
                    visualTotal
                )
            } $unit."
        }

        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
            val boxes = parseCreateProductNumber(state.boxesReceivedText)
            val packagesPerBox = parseCreateProductNumber(state.packagesPerBoxText)
            val unitsPerPackage = parseCreateProductNumber(state.unitsPerPackageText)
            val total = calculateTotalBaseStock(state)

            if (boxes <= 0.0 || packagesPerBox <= 0.0 || unitsPerPackage <= 0.0) {
                ""
            } else {
                "Recibiste ${formatCreateProductNumber(boxes)} ${
                    plural(
                        boxes,
                        "caja",
                        "cajas"
                    )
                } " + "con ${formatCreateProductNumber(packagesPerBox)} ${
                    plural(
                        packagesPerBox, "paquete", "paquetes"
                    )
                } cada una. " + "Cada paquete trae ${formatCreateProductNumber(unitsPerPackage)} $unit. " + "Total: ${
                    formatCreateProductNumber(
                        total
                    )
                } $unit."
            }
        }
    }
}


fun buildStockAvailableSummary(state: CreateProductState): String {
    val total = calculateInitialStockValue(state)
    val totalText = formatCreateProductNumber(total)
    val unit = resolveStockUnitLabel(state)
    return "$totalText $unit"
}

fun buildAiDefaultSalePresentationIfNeeded(
    state: CreateProductState
): CreateProductPresentation? {
    if (state.presentations.isNotEmpty()) return null
    if (!state.stockEntryConfigured) return null

    // Candado principal: solo si el producto fue aplicado desde IA.
    if (!state.barcodeAiApplied) return null
    if (!state.defaultSalePresentationFromAi) return null
    if (state.defaultSalePresentationName.isBlank()) return null
    if (state.defaultSalePresentationConfidence < 80) return null

    val totalBase = calculateTotalBaseStock(state)
    if (totalBase <= 0.0) return null

    val physicalUnits = calculateTotalPhysicalUnits(state).toDouble()
    if (physicalUnits <= 0.0) return null

    val equivalenceBase = when (state.controlType) {
        CreateProductControlType.UNIDAD -> 1.0
        CreateProductControlType.PESO, CreateProductControlType.LIQUIDO -> totalBase / physicalUnits

        null -> return null
    }

    if (equivalenceBase <= 0.0) return null

    return CreateProductPresentation(
        id = "ai_default_${java.util.UUID.randomUUID().toString().take(8)}",
        name = state.defaultSalePresentationName.trim(),
        equivalenceText = formatCreateProductNumber(equivalenceBase),
        salePriceText = "",
        isAiSuggested = true,
        aiDescription = state.defaultSalePresentationReason.ifBlank {
            "Presentación comercial sugerida por IA."
        })
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
        return raw.filter { it.isDigit() || it == '/' }.take(5)
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
    val expira =
        runCatching { YearMonth.of(2000 + year, month).atEndOfMonth() }.getOrNull() ?: return false
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
    val expira =
        runCatching { YearMonth.of(2000 + year, month).atEndOfMonth() }.getOrNull() ?: return null
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
            kind = ExpirationStatusKind.INVALIDO, message = "Mes inválido (1-12)", color = CreateRed
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
            kind = ExpirationStatusKind.INVALIDO, message = "Fecha inválida", color = CreateRed
        )
    }

    val currentYearMonth = YearMonth.now()
    if (expiryYearMonth.isBefore(currentYearMonth)) {
        return ExpirationStatus(
            kind = ExpirationStatusKind.VENCIDO, message = "Este lote ya venció", color = CreateRed
        )
    }

    // Unificamos el helper visual con los mismos rangos que usa el escáner.
    // Evitamos números puntuales ("18 meses") y mostramos rangos breves
    // que dan una idea accionable de proximidad.
    val monthsLeft = ChronoUnit.MONTHS.between(
        currentYearMonth, expiryYearMonth
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
    productName: String, currentLot: String, existingLots: Set<String>
): String {
    val prefix =
        productName.trim().firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString()
            ?: currentLot.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "L"

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
    producto: MoldeProductos, onAddStock: () -> Unit
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
        color = Color.White, shape = RoundedCornerShape(18.dp), border = BorderStroke(
            width = if (isBarcodeValid) 2.dp else 1.dp,
            color = if (isBarcodeValid) CreateGreen else CreateBorder
        ), modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (!isConfirmed) {
                // CABECERA: Título dinámico y botón para salir del modo manual
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isManualMode) "Escribir código" else "Código de barras opcional",
                            style = MaterialTheme.typography.titleSmall,
                            color = CreateTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = CreateGreen
                        )
                    } else if (isManualMode) {
                        // NUEVO: Botón para regresar al selector original (Escanear/Manual)
                        IconButton(
                            onClick = onClear, modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Regresar",
                                tint = CreateRed.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isManualMode) {
                    AppTextField(
                        label = "",
                        value = barcode,
                        error = error,
                        placeholder = "Ej. 193968381356",
                        keyboardType = KeyboardType.Number,
                        leadingIcon = null, // Ya se muestra en la cabecera
                        trailingIcon = {
                            if (barcode.isNotBlank()) {
                                IconButton(onClick = onConfirmManual) {
                                    Icon(
                                        Icons.Outlined.CheckCircle,
                                        null,
                                        tint = CreateGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
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
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.35f))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.QrCodeScanner,
                                contentDescription = null,
                                tint = CreateGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Escanear", color = CreateGreen, fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = onManualMode,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
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
                // ESTADO CONFIRMADO: Vista resumen del código registrado
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
                            onClick = onStartScan, contentPadding = PaddingValues(horizontal = 8.dp)
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
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
fun FluidBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "fluid")

    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "p1"
    )
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart),
        label = "p2"
    )

    val colors = listOf(
        Color(0xFFD32F2F), // Rojo
        Color(0xFF1976D2), // Azul
        Color(0xFF388E3C), // Verde
        Color(0xFF7B1FA2), // Morado
        Color(0xFFD32F2F)  // Ciclo
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            drawRect(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = colors, start = Offset(
                        canvasWidth * kotlin.math.cos(Math.toRadians(phase1.toDouble())).toFloat(),
                        0f
                    ), end = Offset(
                        canvasWidth * kotlin.math.sin(Math.toRadians(phase1.toDouble())).toFloat(),
                        canvasHeight
                    )
                ), alpha = 0.6f
            )

            drawRect(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = colors.reversed(), center = Offset(
                        canvasWidth / 2 + (canvasWidth / 3 * kotlin.math.cos(Math.toRadians(phase2.toDouble()))).toFloat(),
                        canvasHeight / 2 + (canvasHeight / 3 * kotlin.math.sin(Math.toRadians(phase2.toDouble()))).toFloat()
                    ), radius = canvasWidth * 1.5f
                ), alpha = 0.4f
            )
        }
    }
}
