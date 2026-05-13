package com.app.administradorfarmadon.ActivityInventario.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.graphicsLayer
import com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestion
import com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionStatus
import com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.app.administradorfarmadon.ActivityInventario.reference.ProductReference
import com.app.administradorfarmadon.ActivityInventario.reference.ProductReferenceStatus
import com.app.administradorfarmadon.ActivityInventario.reference.ProductReferenceUiState
import com.app.administradorfarmadon.ActivityInventario.reference.toSummary
import com.app.administradorfarmadon.R
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

private val CreateBackground = Color(0xFFF7F9FC)
private val CreateBorder = Color(0xFFE5EAF0)
private val CreateGreen = Color(0xFF15A05C)
private val CreateGreenSoft = Color(0xFFF1FBF5)
private val CreateOrangeSoft = Color(0xFFFFF5EB)
private val CreateTextPrimary = Color(0xFF111827)
private val CreateTextSecondary = Color(0xFF667085)
private val CreateRed = Color(0xFFD92D20)
private val CreateOrange = Color(0xFFE17B00)
private val CreateBlue = Color(0xFF2E90FA)

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
    PRODUCTO(1, "Producto", "Define la informacion base del producto"),
    LOTE_INICIAL(2, "Lote inicial", "Registra el lote con el que inicia el inventario"),
    PRESENTACIONES(3, "Presentaciones y precios", "Configura equivalencias y precios de venta"),
    RESUMEN(4, "Resumen del producto", "Confirma los datos antes de guardar")
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
        label = "Caja con frascos / blisters",
        helper = "Cada caja contiene varios frascos o blisters, y cada uno tiene su cantidad (ej. 10 cajas x 5 frascos x 120 mL)"
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
    val isAiSuggested: Boolean = false
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
    val name: String,
    val equivalenceBase: String, // en unidades base: g, mL o cantidad
    val controlType: String      // "UNIDAD" | "PESO" | "LIQUIDO"
)

data class CreateProductState(
    val currentStep: CreateProductStep = CreateProductStep.PRODUCTO,
    val name: String = "",
    val category: String = "",
    val controlType: CreateProductControlType? = null,
    val requiresPrescription: Boolean = false,
    val active: Boolean = true,

    val lotNumber: String = "",
    val expirationDate: String = "",

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

    // Aviso de bajo inventario.
    val minimumStockText: String = "",
    // Unidad seleccionada para mostrar el stock minimo (ej. "g","kg","mL","L","unidades").
    val minimumStockUnit: String = "",
    // Porcentaje del stock recibido que dispara el aviso de stock bajo.
    // Default 20%. El usuario lo ajusta con un stepper visual y la cantidad
    // en unidades (`minimumStockText`) se recalcula en vivo.
    val minimumStockPercent: Int = 20,

    // --- NUEVA LÓGICA DE STOCK MÍNIMO POR UNIDADES FÍSICAS DISCRETAS ---
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
    // Presentaciones guardadas previamente por el usuario (cargadas desde DB)
    val savedPresentationOptions: List<SavedPresentation> = emptyList(),
    val errors: Map<String, String> = emptyMap(),
    val stockEntryConfigured: Boolean = false,
    val showStockEntryDialog: Boolean = false,
    val keywords: List<String> = emptyList(), // Palabras sugeridas por IA
    val selectedKeywords: Set<String> = emptySet() // Palabras elegidas por el usuario
)

@Composable
fun CreateProductScreen(
    state: CreateProductState,
    categoryOptions: List<String>,
    smartHint: SmartProductHint?,
    existingLotNumbers: Set<String>,
    nextEnabled: Boolean,
    referenceState: ProductReferenceUiState,
    // Sugerencia de categoría con IA (Gemini).
    categorySuggestionState: com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState =
        com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState(),
    onAcceptCategorySuggestion: (com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestion) -> Unit = {},
    onSwitchToManualCategory: () -> Unit = {},
    onBackToAiCategory: () -> Unit = {},
    // Escáner OCR de etiqueta (cámara + Gemini Vision).
    labelScannerState: com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.UiState =
        com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.UiState(),
    onRequestLabelScan: () -> Unit = {},
    onConsumeLabelScan: () -> Unit = {},
    loading: Boolean,
    successSummary: ProductCreatedSummary?,
    showReferenceScreen: Boolean,
    showReferenceSection: Boolean,
    onBack: () -> Unit,
    onStateChange: (CreateProductState) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSave: () -> Unit,
    onCreateAnother: () -> Unit,
    onViewProduct: () -> Unit,
    onRetryReference: () -> Unit,
    onOpenReference: () -> Unit,
    onDismissReference: () -> Unit,
    onConfirmReference: (ProductReference) -> Unit,
    onSkipReference: () -> Unit
) {
    // Fix: se eliminaron lecturas y cálculos que se descartaban sin asignarse.
    // Solo se conserva la detección real de teclado, que sí se usa abajo para
    // ocultar el BottomStepActions y ajustar el padding inferior del contenido.
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val navigationBottom = WindowInsets.navigationBars.getBottom(density)
    val keyboardVisible = imeBottom > navigationBottom

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreateBackground)
    ) {
        when {
            showReferenceScreen && referenceState.reference != null -> {
                SmartReferenceSuggestionScreen(
                    referenceState = referenceState,
                    onBack = onDismissReference,
                    onDiscard = onDismissReference,
                    onSaveReference = onConfirmReference
                )
            }

            successSummary != null -> {
                ProductCreatedSuccessScreen(
                    summary = successSummary,
                    referenceState = referenceState,
                    showReferenceSection = showReferenceSection,
                    onViewProduct = onViewProduct,
                    onCreateAnother = onCreateAnother,
                    onRetryReference = onRetryReference,
                    onOpenReference = onOpenReference,
                    onSkipReference = onSkipReference,
                    onSaveReference = onConfirmReference
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
                        categorySuggestionState = categorySuggestionState,
                        onAcceptCategorySuggestion = onAcceptCategorySuggestion,
                        onSwitchToManualCategory = onSwitchToManualCategory,
                        onBackToAiCategory = onBackToAiCategory
                    )
                } else {
                    Scaffold(
                        containerColor = CreateBackground,
                        bottomBar = {
                            if (!keyboardVisible) {
                                BottomStepActions(
                                    step = state.currentStep,
                                    nextLabel = nextLabel,
                                    nextEnabled = nextEnabled,
                                    onNext = if (state.currentStep == CreateProductStep.RESUMEN) onSave else onNext,
                                    onPrevious = onPrevious
                                )
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
                                        onAcceptCategorySuggestion = onAcceptCategorySuggestion,
                                        onSwitchToManualCategory = onSwitchToManualCategory,
                                        onBackToAiCategory = onBackToAiCategory
                                    )

                                    CreateProductStep.LOTE_INICIAL -> InitialLotStep(
                                        state = state,
                                        existingLotNumbers = existingLotNumbers,
                                        onStateChange = onStateChange,
                                        labelScannerState = labelScannerState,
                                        onRequestLabelScan = onRequestLabelScan,
                                        onConsumeLabelScan = onConsumeLabelScan
                                    )

                                    CreateProductStep.PRESENTACIONES -> PresentationsPricesStep(
                                        state = state,
                                        onStateChange = onStateChange
                                    )

                                    CreateProductStep.RESUMEN -> ProductSummaryStep(
                                        state = state
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        if (loading) {
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
                        color = CreateGreen,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Guardando producto...",
                        color = CreateTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
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
    // Sugerencia IA propagada también en tablet para consistencia móvil/tablet.
    categorySuggestionState: com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState =
        com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState(),
    onAcceptCategorySuggestion: (com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestion) -> Unit = {},
    onSwitchToManualCategory: () -> Unit = {},
    onBackToAiCategory: () -> Unit = {}
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
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isFirst) "Cancelar" else "Atras")
                    }
                    Button(
                        onClick = if (isLast) onSave else onNext,
                        modifier = Modifier.weight(2f),
                        enabled = saveEnabled
                    ) {
                        Text(if (isLast) "Guardar producto" else "Siguiente")
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
            // ── Sidebar izquierdo: lista de pasos ────────────────────────────
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
                    // Fix: el sidebar permitía saltar a cualquier paso, ignorando
                    // las validaciones del flujo (`nextEnabled`). Ahora solo se
                    // puede retroceder a pasos previos o quedarse en el actual;
                    // para avanzar es obligatorio usar el botón "Siguiente",
                    // que sí respeta la validación.
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

            // ── Contenido derecho: formulario del paso actual ────────────────
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
                            onAcceptCategorySuggestion = onAcceptCategorySuggestion,
                            onSwitchToManualCategory = onSwitchToManualCategory,
                            onBackToAiCategory = onBackToAiCategory
                        )
                        CreateProductStep.LOTE_INICIAL -> InitialLotStep(
                            state = state,
                            existingLotNumbers = existingLotNumbers,
                            onStateChange = onStateChange
                        )
                        CreateProductStep.PRESENTACIONES -> PresentationsPricesStep(
                            state = state,
                            onStateChange = onStateChange
                        )
                        CreateProductStep.RESUMEN -> ProductSummaryStep(state = state)
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
            .padding(horizontal = 24.dp, vertical = 12.dp)
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
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                    contentDescription = "Atras",
                    tint = CreateTextPrimary
                )
            }
            // Fix: antes había dos `AnimatedContent` separados (uno para "Paso N
            // de 4" y otro para el título/subtítulo) con la misma transición.
            // Animaban de forma independiente y podían quedar fuera de fase.
            // Ahora un solo `AnimatedContent` envuelve los tres elementos para
            // que entren y salgan exactamente al mismo tiempo.
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Paso ${step.number} de 4",
                color = CreateGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        AnimatedContent(
            targetState = step,
            label = "create_product_header_block",
            transitionSpec = {
                if (targetState.number > initialState.number) {
                    (
                        slideInHorizontally { fullWidth -> fullWidth / 3 } +
                            slideInVertically { it / 10 } +
                            fadeIn()
                        ).togetherWith(
                        slideOutHorizontally { fullWidth -> -fullWidth / 5 } +
                            fadeOut()
                    )
                } else {
                    (
                        slideInHorizontally { fullWidth -> -fullWidth / 3 } +
                            slideInVertically { it / 10 } +
                            fadeIn()
                        ).togetherWith(
                        slideOutHorizontally { fullWidth -> fullWidth / 5 } +
                            fadeOut()
                    )
                }
            }
        ) { currentStep ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = currentStep.title,
                    color = CreateTextPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentStep.subtitle,
                    color = CreateTextSecondary,
                    fontSize = 16.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        LinearProgressIndicator(
            progress = { step.number / 4f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = CreateGreen,
            trackColor = CreateBorder
        )
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

    LaunchedEffect(step) {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        listState.scrollToItem(0)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                }
            },
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
fun ProductBasicStep(
    state: CreateProductState,
    categoryOptions: List<String>,
    onStateChange: (CreateProductState) -> Unit,
    // Sugerencia IA opcional. Defaults seguros para callers que no la usan.
    categorySuggestionState: com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState =
        com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionUiState(),
    onAcceptCategorySuggestion: (com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestion) -> Unit = {},
    onSwitchToManualCategory: () -> Unit = {},
    onBackToAiCategory: () -> Unit = {}
) {
    val nameRequester = remember { BringIntoViewRequester() }
    val categoryRequester = remember { BringIntoViewRequester() }

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

    // ELIMINA CUALQUIER LaunchedEffect(Unit) QUE FUERCE UNIDAD AQUÍ

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        AppTextField(
            modifier = Modifier.bringIntoViewRequester(nameRequester),
            label = "Nombre del producto *",
            value = state.name,
            error = state.errors["name"],
            placeholder = "Ej. Ibuprofeno 500mg",
            onValueChange = {
                onStateChange(state.copy(name = it, errors = state.errors - "name"))
            }
        )

        // La tarjeta IA (categoría + tipo de control) reemplaza visualmente
        // ambas secciones cuando el flujo IA está activo. Cuando el usuario
        // cambia a manual, esta misma función pinta el text field + selector.
        CategoryAutocompleteField(
            modifier = Modifier.bringIntoViewRequester(categoryRequester),
            value = state.category,
            options = categoryOptions,
            error = state.errors["category"],
            controlType = state.controlType,
            controlTypeError = state.errors["controlType"],
            suggestionState = categorySuggestionState,
            onAcceptSuggestion = onAcceptCategorySuggestion,
            onSwitchToManual = onSwitchToManualCategory,
            onBackToAi = onBackToAiCategory,
            onValueChange = {
                onStateChange(state.copy(category = it, errors = state.errors - "category"))
            },
            onControlTypeChange = { controlType ->
                onStateChange(
                    state.copy(
                        controlType = controlType,
                        presentations = sugerirPresentacionesIniciales(controlType, ""),
                        mainPresentationId = "",
                        errors = state.errors - "controlType"
                    )
                )
            }
        )

        // Los switches "Requiere receta" y "Producto activo" SOLO aparecen
        // cuando el usuario está en modo manual. Si la IA está activa,
        // ambos datos ya quedan resueltos: la receta se mostró dentro de
        // la tarjeta IA y "activo" se asume true por defecto. Mostrarlos
        // también en modo IA generaría ruido visual sin acción real.
        val mostrarCamposSecundarios =
            categorySuggestionState.status == CategorySuggestionStatus.MANUAL ||
            categorySuggestionState.status == CategorySuggestionStatus.FALLBACK_MANUAL

        AnimatedVisibility(
            visible = mostrarCamposSecundarios,
            enter = fadeIn() + slideInVertically { it / 6 },
            exit = fadeOut() + slideOutVertically { -it / 6 }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                CompactSwitchRow(
                    title = "Requiere receta médica",
                    subtitle = "Solicita receta al momento de vender",
                    checked = state.requiresPrescription,
                    onCheckedChange = {
                        onStateChange(state.copy(requiresPrescription = it))
                    }
                )

                CompactSwitchRow(
                    title = "Producto activo",
                    subtitle = "Queda habilitado para inventario y venta",
                    checked = state.active,
                    onCheckedChange = {
                        onStateChange(state.copy(active = it))
                    }
                )
            }
        }
    }
}

@Composable
fun InitialLotStep(
    state: CreateProductState,
    existingLotNumbers: Set<String>,
    onStateChange: (CreateProductState) -> Unit,
    // Estado y disparador del escáner OCR (cámara + Gemini Vision).
    labelScannerState: com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.UiState =
        com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.UiState(),
    onRequestLabelScan: () -> Unit = {},
    onConsumeLabelScan: () -> Unit = {}
) {
    val lotRequester = remember { BringIntoViewRequester() }
    val expirationRequester = remember { BringIntoViewRequester() }
    val entryModeRequester = remember { BringIntoViewRequester() }
    val minimumStockRequester = remember { BringIntoViewRequester() }
    val purchaseCostRequester = remember { BringIntoViewRequester() }

    val expirationStatus = remember(state.expirationDate) {
        evaluateExpirationStatus(state.expirationDate)
    }

    // Fix: se removieron un `when` y un `remember` cuyos valores no se asignaban
    // ni se consumían. El resumen ya se calcula dentro de StockConfiguredCard y
    // StockEntryDialog, y la etiqueta de unidad la usa cada subcomponente que
    // la necesita. Aquí solo se mantenían como código muerto que disparaba
    // recomposiciones innecesarias.

    val normalizedLot = remember(state.lotNumber) {
        state.lotNumber.trim().uppercase()
    }

    val lotExists = remember(normalizedLot, existingLotNumbers) {
        normalizedLot.isNotBlank() && existingLotNumbers.contains(normalizedLot)
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        CreateSectionCard(
            title = "Datos del lote",
            subtitle = "Identifica este ingreso de inventario."
        ) {
            // Botón para escanear la etiqueta con cámara + Gemini Vision.
            // Llena automáticamente "Número de lote" y "Vencimiento" si la
            // foto los muestra legibles. El usuario siempre puede editar.
            LabelScannerRow(
                state = labelScannerState,
                onRequestScan = onRequestLabelScan,
                onApplyResult = { etiqueta ->
                    // Auto-aplicar resultados al formulario. NO reseteamos
                    // el ViewModel después: queremos que la tarjeta READY
                    // se quede visible como confirmación + opción "Tomar
                    // otra foto". El usuario podrá tomar otra foto sin
                    // necesidad de pulsar "Aplicar" cada vez.
                    var nuevoEstado = state
                    etiqueta.loteNumero?.let { lote ->
                        nuevoEstado = nuevoEstado.copy(
                            lotNumber = lote,
                            errors = nuevoEstado.errors - "lotNumber"
                        )
                    }
                    // Solo aplicamos el vencimiento si está vigente. Vencidos
                    // o inválidos se ignoran para no contaminar el formulario;
                    // la tarjeta de escaneo ya advierte al usuario.
                    etiqueta.vencimientoMmAa?.let { venc ->
                        if (esVencimientoVigenteSimple(venc)) {
                            nuevoEstado = nuevoEstado.copy(
                                expirationDate = venc,
                                errors = nuevoEstado.errors - "expirationDate"
                            )
                        }
                    }
                    onStateChange(nuevoEstado)
                },
                onDismiss = onConsumeLabelScan
            )

            // Solo ocultamos los inputs durante LOADING (procesamiento de la
            // foto). En READY los datos ya se aplicaron automáticamente y los
            // inputs se muestran rellenados para que el usuario pueda
            // ajustarlos si la IA detectó algo distinto a lo que ve.
            val escaneoActivo = labelScannerState.status ==
                com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.Status.LOADING

            AnimatedVisibility(
                visible = !escaneoActivo,
                enter = fadeIn() + slideInVertically { it / 8 },
                exit = fadeOut() + slideOutVertically { -it / 8 }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val showLotSuggestion = state.lotNumber.isBlank() && suggestedLot.isNotBlank()

            AppTextField(
                modifier = Modifier.bringIntoViewRequester(lotRequester),
                label = "Numero de lote *",
                value = state.lotNumber,
                error = when {
                    lotExists -> "Ese lote ya existe"
                    else -> state.errors["lotNumber"]
                },
                helper = when {
                    showLotSuggestion ->
                        "Sugerencia automatica: $suggestedLot"
                    state.lotNumber.equals(suggestedLot, ignoreCase = true) ->
                        null
                    else ->
                        "Usa un codigo breve y facil de rastrear."
                },
                helperActionLabel = if (showLotSuggestion) "Usar" else null,
                helperHighlighted = lotExists,
                onHelperAction = if (showLotSuggestion) {
                    {
                        onStateChange(
                            state.copy(
                                lotNumber = suggestedLot,
                                errors = state.errors - "lotNumber"
                            )
                        )
                    }
                } else null,
                placeholder = "Ej. $suggestedLot",
                keyboardType = KeyboardType.Ascii,
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

            ExpirationDateField(
                modifier = Modifier.bringIntoViewRequester(expirationRequester),
                label = "Fecha de vencimiento *",
                value = state.expirationDate,
                error = when {
                    expirationStatus?.kind == ExpirationStatusKind.VENCIDO ->
                        "La fecha ingresada ya vencio."
                    expirationStatus?.kind == ExpirationStatusKind.INVALIDO ->
                        expirationStatus.message   // ✅ usar el mensaje específico
                    else -> state.errors["expirationDate"]
                },
                helper = expirationStatus?.takeIf {
                    it.kind != ExpirationStatusKind.INVALIDO &&
                            it.kind != ExpirationStatusKind.VENCIDO
                }?.message,
                helperColor = expirationStatus?.color ?: CreateTextSecondary,
                placeholder = "Ej. 07/28",
                onValueChange = {
                    onStateChange(
                        state.copy(
                            expirationDate = formatExpirationDateInput(it),
                            errors = state.errors - "expirationDate"
                        )
                    )
                }
            )
                }  // fin Column dentro de AnimatedVisibility
            }      // fin AnimatedVisibility (escaneo activo)
        }

        CreateSectionCard(
            title = "Como recibiste el producto",
            subtitle = "Elige la forma en que llego este lote."
        ) {
            Box(modifier = Modifier.bringIntoViewRequester(entryModeRequester)) {
                if (!state.stockEntryConfigured) {
                    StockEntryModeSelector(
                        selected = state.stockEntryMode,
                        error = state.errors["stockEntryMode"],
                        onSelected = { mode ->
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
                    )
                } else {
                    StockConfiguredCard(
                        state = state,
                        onEditQuantity = {
                            onStateChange(state.copy(showStockEntryDialog = true))
                        },
                        onChangeType = {
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
                    )
                }
            }
        }



        // Stepper de porcentaje para el stock mínimo. Solo aparece cuando
        // el modo de ingreso está configurado (sin recibido no hay base de
        // cálculo). El campo `minimumStockText` se rellena automáticamente
        // según el porcentaje elegido — el usuario nunca escribe unidades.
        AnimatedVisibility(
            visible = state.stockEntryConfigured,
            enter = fadeIn() + slideInVertically { it / 6 },
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.bringIntoViewRequester(minimumStockRequester)) {
                MinimumStockStepperCard(
                    state = state,
                    onStateChange = onStateChange
                )
            }
        }

        CreateSectionCard(
            title = "Costo de compra",
            subtitle = "Costo total pagado por este lote."
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(
                    modifier = Modifier.bringIntoViewRequester(purchaseCostRequester),
                    label = "Costo total *",
                    value = state.purchaseCost,
                    error = state.errors["purchaseCost"],
                    placeholder = "Ej. Bs 300.00",
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

                // Validación visual: muestra el costo unitario derivado para
                // que el usuario pueda detectar errores de tipeo evidentes
                // (ej. olvidó un cero). No es editable, solo informativo.
                val costoTotal = state.purchaseCost
                    .replace("Bs", "", ignoreCase = true)
                    .trim()
                    .replace(",", ".")
                    .toDoubleOrNull()
                val recibidoBase = calculateRecibidoBase(state)
                if (costoTotal != null && costoTotal > 0.0 && recibidoBase > 0.0) {
                    val unitario = costoTotal / recibidoBase
                    val unitLabel = state.controlType?.baseUnitLabel ?: "unidad"
                    Surface(
                        color = Color(0xFFF5F6FF),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E5F7))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🧮", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Costo por $unitLabel:",
                                color = CreateTextSecondary,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Bs " + String.format(Locale.US, "%.4f", unitario)
                                    .trimEnd('0').trimEnd('.'),
                                color = CreateTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.showStockEntryDialog) {
        StockEntryDialog(
            state = state,
            onStateChange = onStateChange,
            onApply = {
                // El stock mínimo se calcula automáticamente en la tarjeta
                // `MinimumStockStepperCard` a partir del porcentaje (default 20%).
                // Ya no auto-rellenamos aquí; el LaunchedEffect del stepper
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
 * Tarjeta animada para el aviso de bajo inventario.
 *
 * El usuario no escribe un número de unidades. En su lugar:
 *  - Arriba ve cuánto recibió, con desglose (5 cajas × 100 u = 500).
 *  - En el centro ve "Avisar cuando queden N unidades" (calculado).
 *  - Abajo manipula el porcentaje con un stepper [-] [N%] [+] de pasos
 *    de 5%, rango 5%–50%.
 *
 * Cuando el porcentaje cambia, el número del centro se anima a su nuevo
 * valor y el `minimumStockText` se persiste en el estado en unidad base.
 * El cálculo redondea hacia arriba para no avisar tarde.
 */
@Composable
fun MinimumStockStepperCard(
    state: CreateProductState,
    onStateChange: (CreateProductState) -> Unit
) {
    val totalPhysicalUnits = remember(state) { calculateTotalPhysicalUnits(state) }
    val mode = remember(state) { getEffectiveStockControlMode(state) }
    val validOptions = remember(totalPhysicalUnits, mode) { 
        calculateValidStockOptions(totalPhysicalUnits, mode) 
    }
    
    val currentSelection = state.minimumStockUnits
    val unitLabel = getPhysicalUnitLabel(state, currentSelection)
    
    // Sincronización inicial y ajuste si el total cambia.
    LaunchedEffect(validOptions) {
        if (validOptions.isNotEmpty()) {
            if (currentSelection !in validOptions) {
                // Si la selección actual no es válida (o es 0 inicial), 
                // tomamos la primera opción (mínimo 10% o 1 frasco).
                onStateChange(state.copy(minimumStockUnits = validOptions.first()))
            }
        }
    }

    val percentage = remember(currentSelection, totalPhysicalUnits) {
        if (totalPhysicalUnits > 0) {
            (currentSelection.toDouble() / totalPhysicalUnits.toDouble()) * 100.0
        } else 0.0
    }

    val formattedPercent = remember(percentage) {
        if (percentage % 1.0 == 0.0) "${percentage.toInt()}%"
        else "aprox. ${String.format(Locale.US, "%.1f", percentage)}%"
    }

    val desglose = remember(state) {
        val unit = state.controlType?.baseUnitLabel ?: "unidades"
        descripcionRecibidoCorto(state, unit)
    }

    // Animación: el número se interpola al cambiar.
    val numeroAnimado by animateIntAsState(
        targetValue = currentSelection,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
        label = "minimum_stock_number"
    )
    
    val bellPulse = remember { Animatable(1f) }
    LaunchedEffect(currentSelection) {
        bellPulse.snapTo(0.85f)
        bellPulse.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium)
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, CreateBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Encabezado
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🔔",
                    fontSize = 22.sp,
                    modifier = Modifier.graphicsScale(bellPulse.value)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Stock mínimo inteligente",
                        color = CreateTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = CreateGreenSoft,
                            shape = CircleShape
                        ) {
                            Text(
                                text = if (mode == StockControlMode.INDIVISIBLE) "Control por envase" else "Control por unidad",
                                color = CreateGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        if (desglose.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Total: $desglose",
                                color = CreateTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Display central
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AVISAR CUANDO QUEDEN",
                    color = CreateTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = numeroAnimado.toString(),
                        color = CreateGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = unitLabel,
                        color = CreateTextSecondary,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
                
                Surface(
                    color = CreateBackground,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Equivale al $formattedPercent del stock actual",
                        color = CreateTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stepper discreto
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
                            onStateChange(state.copy(minimumStockUnits = validOptions[currentIndex - 1]))
                        }
                    },
                    enabled = canDecrease,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (canDecrease) CreateGreenSoft else CreateBackground,
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
                            onStateChange(state.copy(minimumStockUnits = validOptions[currentIndex + 1]))
                        }
                    },
                    enabled = canIncrease,
                    modifier = Modifier
                        .size(48.dp)
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = if (mode == StockControlMode.INDIVISIBLE) 
                    "Este producto se controla por envases completos. No se permiten fracciones."
                    else "El mínimo se calcula sobre unidades reales para evitar alertas imposibles.",
                color = CreateTextSecondary.copy(alpha = 0.7f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
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
 * Texto corto y entendible del desglose: "5 cajas × 100 u = 500 u",
 * "10 cajas × 5 frascos × 120 mL = 6000 mL", etc.
 */
private fun descripcionRecibidoCorto(
    state: CreateProductState,
    unitLabel: String
): String {
    return when (state.stockEntryMode) {
        CreateProductStockEntryMode.UNIDAD -> {
            val q = parseCreateProductNumber(state.receivedUnitsText)
            if (q <= 0.0) "" else "${formatCreateProductNumber(q)} $unitLabel"
        }
        CreateProductStockEntryMode.CAJA -> {
            val cajas = parseCreateProductNumber(state.boxesReceivedText)
            val porCaja = parseCreateProductNumber(state.unitsPerBoxText)
            val total = cajas * porCaja
            if (cajas <= 0 || porCaja <= 0) "" else
                "${formatCreateProductNumber(cajas)} cajas × " +
                    "${formatCreateProductNumber(porCaja)} $unitLabel = " +
                    "${formatCreateProductNumber(total)} $unitLabel"
        }
        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
            val cajas = parseCreateProductNumber(state.boxesReceivedText)
            val paquetes = parseCreateProductNumber(state.packagesPerBoxText)
            val porPaquete = parseCreateProductNumber(state.unitsPerPackageText)
            val total = cajas * paquetes * porPaquete
            if (cajas <= 0 || paquetes <= 0 || porPaquete <= 0) "" else
                "${formatCreateProductNumber(cajas)} × " +
                    "${formatCreateProductNumber(paquetes)} × " +
                    "${formatCreateProductNumber(porPaquete)} = " +
                    "${formatCreateProductNumber(total)} $unitLabel"
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

    Surface(
        color = CreateGreenSoft,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, CreateBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = when (state.stockEntryMode) {
                    CreateProductStockEntryMode.UNIDAD -> "Por unidad configurado"
                    CreateProductStockEntryMode.CAJA -> "Por caja configurado"
                    CreateProductStockEntryMode.CAJA_CON_PAQUETES -> "Caja con paquetes configurado"
                    null -> "Sin configurar"
                },
                color = CreateGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            if (summary.isNotBlank()) {
                Text(
                    text = summary,
                    color = CreateTextSecondary,
                    fontSize = 14.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEditQuantity) {
                    Text("Cambiar cantidad")
                }

                TextButton(onClick = onChangeType) {
                    Text("Cambiar tipo")
                }
            }
        }
    }
}

@Composable
fun StockEntryDialog(
    state: CreateProductState,
    onStateChange: (CreateProductState) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    val unitLabel = state.controlType?.baseUnitLabel ?: "unidades"
    val summary = buildInitialStockEntrySummary(state)

    // Textos dinámicos según el tipo de control
    val (labelUnidad, trailingUnidad, placeholderUnidad) = when (state.controlType) {
        CreateProductControlType.LIQUIDO -> Triple("Mililitros totales", "mL", "Ej. 5000")
        CreateProductControlType.PESO -> Triple("Gramos totales", "g", "Ej. 2500")
        else -> Triple("Unidades totales", "unidades", "Ej. 100")
    }

    val (labelCajaUnidadBase, placeholderCajaUnidadBase) = when (state.controlType) {
        CreateProductControlType.LIQUIDO -> Pair("mL por caja", "Ej. 120")
        CreateProductControlType.PESO -> Pair("gramos por caja", "Ej. 500")
        else -> Pair("unidades por caja", "Ej. 60")
    }

    val (labelPaquete, trailingPaquete, labelUnidadPaquete, placeholderUnidadPaquete) = when (state.controlType) {
        CreateProductControlType.LIQUIDO -> Tuple4("Frascos / blisters por caja", "frascos", "mL por frasco", "Ej. 120")
        CreateProductControlType.PESO -> Tuple4("Sobres / envases por caja", "sobres", "gramos por sobre", "Ej. 50")
        else -> Tuple4("Paquetes / blisters por caja", "paquetes", "unidades por paquete", "Ej. 10")
    }

    // Selector de unidad: PESO usa g/kg, LIQUIDO usa mL/L. UNIDAD no aplica.
    val (unidadBase, unidadGrande) = when (state.controlType) {
        CreateProductControlType.LIQUIDO -> "mL" to "L"
        CreateProductControlType.PESO -> "g" to "kg"
        else -> "" to ""
    }
    val mostrarSelectorUnidad = state.controlType == CreateProductControlType.PESO ||
        state.controlType == CreateProductControlType.LIQUIDO

    var unitForBox by remember(state.controlType) { mutableStateOf(unidadBase) }
    var unitForPackage by remember(state.controlType) { mutableStateOf(unidadBase) }

    // TextFieldValue para controlar cursor
    var boxFieldValue by remember { mutableStateOf(TextFieldValue()) }
    var packageFieldValue by remember { mutableStateOf(TextFieldValue()) }

    // Fix: se reutiliza `formatCreateProductNumber` para evitar el sufijo
    // ".0" cuando el valor es entero (antes "500" se mostraba como "500.0"
    // al alternar la unidad).
    // Sincronizar con el estado externo (almacenado en unidad base: g o mL).
    LaunchedEffect(state.unitsPerBoxText, unitForBox) {
        val base = state.unitsPerBoxText.toDoubleOrNull() ?: 0.0
        val display = if (unitForBox == unidadGrande && unidadGrande.isNotEmpty()) base / 1000 else base
        val displayStr = if (display == 0.0) "" else formatCreateProductNumber(display)
        if (boxFieldValue.text != displayStr) {
            boxFieldValue = TextFieldValue(displayStr, selection = TextRange(displayStr.length))
        }
    }
    LaunchedEffect(state.unitsPerPackageText, unitForPackage) {
        val base = state.unitsPerPackageText.toDoubleOrNull() ?: 0.0
        val display = if (unitForPackage == unidadGrande && unidadGrande.isNotEmpty()) base / 1000 else base
        val displayStr = if (display == 0.0) "" else formatCreateProductNumber(display)
        if (packageFieldValue.text != displayStr) {
            packageFieldValue = TextFieldValue(displayStr, selection = TextRange(displayStr.length))
        }
    }

    // Fix: ahora se acepta como máximo un punto decimal. Antes se podía
    // escribir "12.3.4" y la función abortaba silenciosamente dejando un
    // texto inválido visible en el campo.
    fun onBoxValueChange(newValue: TextFieldValue) {
        val raw = newValue.text
        if (raw.isBlank()) {
            boxFieldValue = newValue
            onStateChange(state.copy(unitsPerBoxText = ""))
            return
        }
        val sanitized = sanitizeDecimalInput(raw)
        val selection = TextRange(sanitized.length)
        boxFieldValue = if (sanitized == raw) newValue else TextFieldValue(sanitized, selection)
        val number = sanitized.toDoubleOrNull() ?: run {
            onStateChange(state.copy(unitsPerBoxText = ""))
            return
        }
        val baseValue = if (unitForBox == unidadGrande && unidadGrande.isNotEmpty()) number * 1000 else number
        onStateChange(state.copy(unitsPerBoxText = formatCreateProductNumber(baseValue)))
    }

    fun onPackageValueChange(newValue: TextFieldValue) {
        val raw = newValue.text
        if (raw.isBlank()) {
            packageFieldValue = newValue
            onStateChange(state.copy(unitsPerPackageText = ""))
            return
        }
        val sanitized = sanitizeDecimalInput(raw)
        val selection = TextRange(sanitized.length)
        packageFieldValue = if (sanitized == raw) newValue else TextFieldValue(sanitized, selection)
        val number = sanitized.toDoubleOrNull() ?: run {
            onStateChange(state.copy(unitsPerPackageText = ""))
            return
        }
        val baseValue = if (unitForPackage == unidadGrande && unidadGrande.isNotEmpty()) number * 1000 else number
        onStateChange(state.copy(unitsPerPackageText = formatCreateProductNumber(baseValue)))
    }

    // Fix: al cambiar la unidad ahora se preserva la magnitud física en
    // lugar de reinterpretar el número. Antes "500 g" pasaba a "500 kg"
    // (x1000 oculto). Ahora "500 g" muestra "0.5" al pasar a kg, y el
    // valor base en gramos se mantiene intacto. También se usa
    // `formatCreateProductNumber` para evitar el ".0" en enteros.
    fun changeBoxUnit(newUnit: String) {
        if (unitForBox == newUnit) return
        val baseValue = state.unitsPerBoxText.toDoubleOrNull() ?: 0.0
        unitForBox = newUnit
        val displayValue = if (newUnit == unidadGrande && unidadGrande.isNotEmpty()) baseValue / 1000 else baseValue
        val newDisplayStr = if (displayValue == 0.0) "" else formatCreateProductNumber(displayValue)
        boxFieldValue = TextFieldValue(newDisplayStr, selection = TextRange(newDisplayStr.length))
    }

    fun changePackageUnit(newUnit: String) {
        if (unitForPackage == newUnit) return
        val baseValue = state.unitsPerPackageText.toDoubleOrNull() ?: 0.0
        unitForPackage = newUnit
        val displayValue = if (newUnit == unidadGrande && unidadGrande.isNotEmpty()) baseValue / 1000 else baseValue
        val newDisplayStr = if (displayValue == 0.0) "" else formatCreateProductNumber(displayValue)
        packageFieldValue = TextFieldValue(newDisplayStr, selection = TextRange(newDisplayStr.length))
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Configurar cantidad", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = CreateTextPrimary)

                Text(
                    text = when (state.stockEntryMode) {
                        CreateProductStockEntryMode.UNIDAD -> "Unidades sueltas"
                        CreateProductStockEntryMode.CAJA -> "Caja con unidades directas"
                        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> "Caja con frascos / sobres / blisters"
                        null -> ""
                    },
                    color = CreateTextSecondary, fontSize = 14.sp
                )

                when (state.stockEntryMode) {
                    null -> Unit
                    CreateProductStockEntryMode.UNIDAD -> {
                        // Fix: para productos por PESO o LIQUIDO se permiten
                        // decimales (ej. "5000.5 mL"). Para UNIDAD se mantiene
                        // entrada entera. Antes el filtro `isDigit()` descartaba
                        // el punto silenciosamente sin avisar al usuario.
                        val allowDecimalsUnits = state.controlType == CreateProductControlType.PESO ||
                            state.controlType == CreateProductControlType.LIQUIDO
                        OutlinedTextField(
                            value = state.receivedUnitsText,
                            onValueChange = { input ->
                                val sanitized = if (allowDecimalsUnits) sanitizeDecimalInput(input)
                                else input.filter { it.isDigit() }
                                onStateChange(state.copy(receivedUnitsText = sanitized))
                            },
                            label = { Text(labelUnidad) },
                            placeholder = { Text(placeholderUnidad) },
                            isError = state.errors["receivedUnits"] != null,
                            trailingIcon = { Text(trailingUnidad) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (allowDecimalsUnits) KeyboardType.Decimal else KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        )
                        if (state.errors["receivedUnits"] != null)
                            Text(state.errors["receivedUnits"]!!, color = Color.Red, fontSize = 12.sp)
                    }
                    CreateProductStockEntryMode.CAJA -> {
                        // Cajas recibidas
                        OutlinedTextField(
                            value = state.boxesReceivedText,
                            onValueChange = { onStateChange(state.copy(boxesReceivedText = it.filter { it.isDigit() })) },
                            label = { Text("Cajas recibidas *") },
                            placeholder = { Text("Ej. 10") },
                            isError = state.errors["boxesReceived"] != null,
                            trailingIcon = { Text("cajas") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        )
                        if (state.errors["boxesReceived"] != null)
                            Text(state.errors["boxesReceived"]!!, color = Color.Red, fontSize = 12.sp)

                        // Cantidad por caja con selector de unidad (PESO: g/kg, LIQUIDO: mL/L)
                        if (mostrarSelectorUnidad) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = boxFieldValue,
                                        onValueChange = { onBoxValueChange(it) },
                                        label = { Text(labelCajaUnidadBase) },
                                        placeholder = { Text(placeholderCajaUnidadBase) },
                                        isError = state.errors["unitsPerBox"] != null,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    if (state.errors["unitsPerBox"] != null)
                                        Text(state.errors["unitsPerBox"]!!, color = Color.Red, fontSize = 12.sp)
                                }
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF2F4F7))
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    FilterChip(
                                        selected = unitForBox == unidadBase,
                                        onClick = { changeBoxUnit(unidadBase) },
                                        label = { Text(unidadBase) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CreateGreen,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                    FilterChip(
                                        selected = unitForBox == unidadGrande,
                                        onClick = { changeBoxUnit(unidadGrande) },
                                        label = { Text(unidadGrande) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CreateGreen,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = state.unitsPerBoxText,
                                onValueChange = { onStateChange(state.copy(unitsPerBoxText = it.filter { it.isDigit() })) },
                                label = { Text(labelCajaUnidadBase) },
                                placeholder = { Text(placeholderCajaUnidadBase) },
                                isError = state.errors["unitsPerBox"] != null,
                                trailingIcon = { Text(unitLabel) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp)
                            )
                            if (state.errors["unitsPerBox"] != null)
                                Text(state.errors["unitsPerBox"]!!, color = Color.Red, fontSize = 12.sp)
                        }
                    }
                    CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
                        // Cajas recibidas
                        OutlinedTextField(
                            value = state.boxesReceivedText,
                            onValueChange = { onStateChange(state.copy(boxesReceivedText = it.filter { it.isDigit() })) },
                            label = { Text("Cajas recibidas *") },
                            placeholder = { Text("Ej. 10") },
                            isError = state.errors["boxesReceived"] != null,
                            trailingIcon = { Text("cajas") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        )
                        if (state.errors["boxesReceived"] != null)
                            Text(state.errors["boxesReceived"]!!, color = Color.Red, fontSize = 12.sp)

                        // Paquetes por caja
                        OutlinedTextField(
                            value = state.packagesPerBoxText,
                            onValueChange = { onStateChange(state.copy(packagesPerBoxText = it.filter { it.isDigit() })) },
                            label = { Text(labelPaquete) },
                            placeholder = { Text("Ej. 5") },
                            isError = state.errors["packagesPerBox"] != null,
                            trailingIcon = { Text(trailingPaquete) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        )
                        if (state.errors["packagesPerBox"] != null)
                            Text(state.errors["packagesPerBox"]!!, color = Color.Red, fontSize = 12.sp)

                        // Cantidad por paquete con selector (PESO: g/kg, LIQUIDO: mL/L)
                        if (mostrarSelectorUnidad) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = packageFieldValue,
                                        onValueChange = { onPackageValueChange(it) },
                                        label = { Text(labelUnidadPaquete) },
                                        placeholder = { Text(placeholderUnidadPaquete) },
                                        isError = state.errors["unitsPerPackage"] != null,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    if (state.errors["unitsPerPackage"] != null)
                                        Text(state.errors["unitsPerPackage"]!!, color = Color.Red, fontSize = 12.sp)
                                }
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF2F4F7))
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    FilterChip(
                                        selected = unitForPackage == unidadBase,
                                        onClick = { changePackageUnit(unidadBase) },
                                        label = { Text(unidadBase) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CreateGreen,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                    FilterChip(
                                        selected = unitForPackage == unidadGrande,
                                        onClick = { changePackageUnit(unidadGrande) },
                                        label = { Text(unidadGrande) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CreateGreen,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = state.unitsPerPackageText,
                                onValueChange = { onStateChange(state.copy(unitsPerPackageText = it.filter { it.isDigit() })) },
                                label = { Text(labelUnidadPaquete) },
                                placeholder = { Text(placeholderUnidadPaquete) },
                                isError = state.errors["unitsPerPackage"] != null,
                                trailingIcon = { Text(unitLabel) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp)
                            )
                            if (state.errors["unitsPerPackage"] != null)
                                Text(state.errors["unitsPerPackage"]!!, color = Color.Red, fontSize = 12.sp)
                        }
                    }
                }

                if (summary.isNotBlank()) {
                    Surface(
                        color = CreateGreenSoft,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, CreateBorder)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Resultado", color = CreateGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(summary, color = CreateTextSecondary, fontSize = 14.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                    Button(onClick = onApply, modifier = Modifier.weight(1f), enabled = summary.isNotBlank()) { Text("Aplicar") }
                }
            }
        }
    }
}

// Data class auxiliar
private data class Tuple4<T1, T2, T3, T4>(val a: T1, val b: T2, val c: T3, val d: T4)

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun PresentationsPricesStep(
    state: CreateProductState,
    onStateChange: (CreateProductState) -> Unit
) {
    val presentationsRequester = remember { BringIntoViewRequester() }
    val mainPresentationRequester = remember { BringIntoViewRequester() }

    val controlTypeKey = when (state.controlType) {
        CreateProductControlType.UNIDAD -> "UNIDAD"
        CreateProductControlType.PESO -> "PESO"
        CreateProductControlType.LIQUIDO -> "LIQUIDO"
        null -> ""
    }
    // Solo presentaciones guardadas previamente por el usuario (sin sugerencias hardcoded).
    val availableSavedToAdd = remember(state.savedPresentationOptions, controlTypeKey, state.presentations) {
        state.savedPresentationOptions
            .filter { it.controlType == controlTypeKey }
            .filterNot { saved ->
                state.presentations.any { it.name.equals(saved.name, ignoreCase = true) }
            }
    }

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
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, CreateBorder)
        ) {
            Text(
                text = "Agrega las presentaciones de venta y escribe manualmente su equivalencia y precio.",
                modifier = Modifier.padding(16.dp),
                color = CreateTextSecondary,
                fontSize = 14.sp
            )
        }

        Text(
            text = "Presentaciones de venta",
            color = CreateTextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
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
                                    // presentación deja de "pertenecer" a la IA.
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
        }

        if (state.errors["presentations"] != null) {
            Text(
                text = state.errors["presentations"].orEmpty(),
                color = Color(0xFFB42318),
                fontSize = 13.sp
            )
        }

        if (state.addPresentationExpanded) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, CreateBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Chips de presentaciones guardadas previamente (DB)
                    if (availableSavedToAdd.isNotEmpty()) {
                        Text(
                            text = "Guardadas anteriormente",
                            color = CreateTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableSavedToAdd.forEach { saved ->
                                AddPresentationModeChip(
                                    label = saved.name,
                                    selected = false,
                                    onClick = {
                                        val newPresentation = CreateProductPresentation(
                                            id = saved.name.lowercase().replace(" ", "_") + "_" +
                                                java.util.UUID.randomUUID().toString().take(4),
                                            name = saved.name,
                                            equivalenceText = saved.equivalenceBase,
                                            salePriceText = "",
                                            isAiSuggested = false
                                        )
                                        // Mismo helper de fusión que el formulario
                                        // custom: evita duplicados por nombre,
                                        // sinónimos o equivalencia exacta.
                                        val (nextList, idFinal) = mergePresentacionEvitandoDuplicado(
                                            existentes = state.presentations,
                                            nueva = newPresentation
                                        )
                                        onStateChange(
                                            state.copy(
                                                presentations = nextList,
                                                mainPresentationId = state.mainPresentationId
                                                    .takeIf { id -> nextList.any { it.id == id } }
                                                    ?: idFinal,
                                                addPresentationExpanded = false,
                                                draftPresentationName = "",
                                                draftPresentationCustomMode = false,
                                                draftPresentationCustomAmount = "",
                                                draftPresentationCustomUnit = "",
                                                errors = state.errors - "presentations"
                                            )
                                        )
                                    }
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = CreateBorder,
                            shape = RoundedCornerShape(1.dp)
                        ) { Spacer(modifier = Modifier.height(1.dp)) }

                        Text(
                            text = "O crea una nueva",
                            color = CreateTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Formulario personalizado (siempre visible)
                    CustomPresentationFields(
                        controlType = state.controlType,
                        amount = state.draftPresentationCustomAmount,
                        unit = state.draftPresentationCustomUnit.ifBlank {
                            when (state.controlType) {
                                CreateProductControlType.PESO -> "g"
                                CreateProductControlType.LIQUIDO -> "mL"
                                else -> ""
                            }
                        },
                        customName = state.draftPresentationName,
                        onAmountChange = {
                            onStateChange(state.copy(draftPresentationCustomAmount = it))
                        },
                        onUnitChange = {
                            onStateChange(state.copy(draftPresentationCustomUnit = it))
                        },
                        onCustomNameChange = {
                            onStateChange(state.copy(draftPresentationName = it))
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                onStateChange(
                                    state.copy(
                                        addPresentationExpanded = false,
                                        draftPresentationName = "",
                                        draftPresentationCustomMode = false,
                                        draftPresentationCustomAmount = "",
                                        draftPresentationCustomUnit = ""
                                    )
                                )
                            }
                        ) {
                            Text("Cancelar")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        val unitToUse = state.draftPresentationCustomUnit.ifBlank {
                            when (state.controlType) {
                                CreateProductControlType.PESO -> "g"
                                CreateProductControlType.LIQUIDO -> "mL"
                                else -> ""
                            }
                        }
                        val canAdd = when (state.controlType) {
                            CreateProductControlType.UNIDAD ->
                                state.draftPresentationName.isNotBlank() &&
                                    state.draftPresentationCustomAmount.toIntOrNull()?.let { it > 0 } == true
                            CreateProductControlType.PESO,
                            CreateProductControlType.LIQUIDO ->
                                state.draftPresentationCustomAmount.toDoubleOrNull()?.let { it > 0 } == true &&
                                    unitToUse.isNotBlank()
                            null -> false
                        }

                        Button(
                            onClick = {
                                val amount = state.draftPresentationCustomAmount.trim()
                                val (draftName, equivalence) = when (state.controlType) {
                                    CreateProductControlType.UNIDAD -> {
                                        val nombre = state.draftPresentationName.trim()
                                            .ifBlank { "Pack $amount" }
                                        nombre to amount
                                    }
                                    CreateProductControlType.PESO -> {
                                        val grams = (amount.toDoubleOrNull() ?: 0.0).let {
                                            if (unitToUse == "kg") it * 1000 else it
                                        }
                                        val gStr = if (grams % 1 == 0.0) grams.toInt().toString() else grams.toString()
                                        "$amount $unitToUse" to gStr
                                    }
                                    CreateProductControlType.LIQUIDO -> {
                                        val ml = (amount.toDoubleOrNull() ?: 0.0).let {
                                            if (unitToUse == "L") it * 1000 else it
                                        }
                                        val mlStr = if (ml % 1 == 0.0) ml.toInt().toString() else ml.toString()
                                        "$amount $unitToUse" to mlStr
                                    }
                                    null -> return@Button
                                }

                                val newPresentation = CreateProductPresentation(
                                    id = draftName.lowercase().replace(" ", "_") + "_" +
                                        java.util.UUID.randomUUID().toString().take(4),
                                    name = draftName,
                                    equivalenceText = equivalence,
                                    salePriceText = "",
                                    isAiSuggested = false
                                )

                                // Agregar a la lista local + guardar como sugerencia para próxima vez
                                val newSaved = SavedPresentation(
                                    name = draftName,
                                    equivalenceBase = equivalence,
                                    controlType = controlTypeKey
                                )
                                val nextSaved = if (state.savedPresentationOptions.any {
                                        it.name.equals(draftName, ignoreCase = true) &&
                                            it.controlType == controlTypeKey
                                    }) {
                                    state.savedPresentationOptions
                                } else {
                                    state.savedPresentationOptions + newSaved
                                }

                                // Fusión inteligente: si la nueva tiene el
                                // mismo nombre / mismo grupo de sinónimos /
                                // misma equivalencia que una existente,
                                // reemplaza la anterior (sin duplicar).
                                val (nextList, idFinal) = mergePresentacionEvitandoDuplicado(
                                    existentes = state.presentations,
                                    nueva = newPresentation
                                )

                                onStateChange(
                                    state.copy(
                                        presentations = nextList,
                                        mainPresentationId = state.mainPresentationId
                                            .takeIf { id -> nextList.any { it.id == id } }
                                            ?: idFinal,
                                        addPresentationExpanded = false,
                                        draftPresentationName = "",
                                        draftPresentationCustomMode = false,
                                        draftPresentationCustomAmount = "",
                                        draftPresentationCustomUnit = "",
                                        savedPresentationOptions = nextSaved,
                                        errors = state.errors - "presentations"
                                    )
                                )
                            },
                            enabled = canAdd
                        ) {
                            Text("Agregar")
                        }
                    }
                }
            }
        } else {
            OutlinedButton(
                onClick = {
                    onStateChange(state.copy(addPresentationExpanded = true))
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.controlType != null
            ) {
                Text("+ Agregar presentacion")
            }
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
        // Bloque informativo inicial
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, CreateBorder)
        ) {
            Text(
                text = "Revisa cuidadosamente los datos antes de registrar el producto en el inventario.",
                modifier = Modifier.padding(16.dp),
                color = CreateTextSecondary,
                fontSize = 14.sp
            )
        }

        // --- CARD PASO 1: IDENTIDAD DEL PRODUCTO ---
        SummaryCard(
            title = "Paso 1: Identidad",
            icon = "🆔",
            color = CreateBlue
        ) {
            SummaryItem("Nombre", state.name.ifBlank { "(Sin nombre)" })
            SummaryItem("Categoría", state.category.ifBlank { "(Sin categoría)" })
            SummaryItem(
                "Tipo de control",
                state.controlType?.label ?: "(No definido)"
            )
            SummaryItem(
                "Receta médica",
                if (state.requiresPrescription) "Requiere receta 📋" else "Venta libre 🟢"
            )
            SummaryItem(
                "Estado inicial",
                if (state.active) "Activo (Visible en ventas)" else "Inactivo (Oculto)"
            )
        }

        // --- CARD PASO 2: INVENTARIO Y LOTE ---
        SummaryCard(
            title = "Paso 2: Inventario",
            icon = "📦",
            color = CreateOrange
        ) {
            val expirationStatus = evaluateExpirationStatus(state.expirationDate)
            
            SummaryItem("Número de Lote", state.lotNumber.ifBlank { "(Sin lote)" })
            SummaryItem(
                "Vencimiento",
                "${state.expirationDate.ifBlank { "(Sin fecha)" }} " +
                        (expirationStatus?.message?.let { "· $it" } ?: "")
            )
            SummaryItem(
                "Stock Recibido",
                buildInitialStockEntrySummary(state).ifBlank { "(No configurado)" }
            )
            SummaryItem(
                "Costo de compra",
                formatBs(state.purchaseCost)
            )
        }

        // --- CARD PASO 3: VENTA Y ALERTAS ---
        SummaryCard(
            title = "Paso 3: Venta y Alertas",
            icon = "💰",
            color = CreateGreen
        ) {
            // Desglose de presentaciones de venta
            Text(
                text = "Precios de venta:",
                color = CreateTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
            
            if (state.presentations.isEmpty()) {
                Text(text = "(Sin presentaciones)", color = CreateTextSecondary, fontSize = 13.sp)
            } else {
                state.presentations.forEach { pres ->
                    val isMain = pres.id == state.mainPresentationId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${pres.name} (x${pres.equivalenceText}) ${if (isMain) "⭐" else ""}",
                            color = CreateTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = if (isMain) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = formatBs(pres.salePriceText),
                            color = CreateGreen,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CreateBorder))
            Spacer(modifier = Modifier.height(8.dp))

            // Alerta de stock mínimo
            val mode = getEffectiveStockControlMode(state)
            val unitLabel = getPhysicalUnitLabel(state, state.minimumStockUnits)
            
            SummaryItem(
                "Alerta stock bajo",
                "Avisar al quedar ${state.minimumStockUnits} $unitLabel",
                highlight = true
            )
            Text(
                text = if (mode == StockControlMode.INDIVISIBLE) 
                    "Controlado por envase completo." 
                    else "Controlado por unidad suelta.",
                color = CreateTextSecondary,
                fontSize = 11.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }

        // --- CARD PASO 4: USOS Y SÍNTOMAS (INTELIGENCIA IA) ---
        SummaryCard(
            title = "¿Para qué sirve?",
            icon = "🩺",
            color = Color(0xFF8E44AD) // Morado IA
        ) {
            Text(
                text = "Selecciona los usos que deseas activar para el buscador inteligente:",
                color = CreateTextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            if (state.keywords.isEmpty()) {
                // Estado de carga o espera de IA
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF8E44AD)
                    )
                    Text(
                        text = "Analizando usos terapéuticos...",
                        color = CreateTextSecondary,
                        fontSize = 13.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.keywords.forEach { keyword ->
                        val isSelected = state.selectedKeywords.contains(keyword)
                        
                        // Diseño solicitado: Azul Medicina si seleccionado, Blanco/Gris si no
                        Surface(
                            modifier = Modifier.clickable {
                                val currentSelected = state.selectedKeywords.toMutableSet()
                                if (isSelected) {
                                    // Regla: mínimo 1 seleccionado
                                    if (currentSelected.size > 1) {
                                        currentSelected.remove(keyword)
                                    }
                                } else {
                                    currentSelected.add(keyword)
                                }
                                onStateChange(state.copy(selectedKeywords = currentSelected))
                            },
                            color = if (isSelected) Color(0xFF2E5BFF) else Color.White,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = 1.dp, 
                                color = if (isSelected) Color(0xFF2E5BFF) else Color(0xFFE5E7EB)
                            ),
                            elevation = if (isSelected) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isSelected) "✅" else "🛡️", 
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = keyword,
                                    color = if (isSelected) Color.White else Color(0xFF4B5563),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CreateBorder))
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "💡 Solo los usos seleccionados se guardarán para el buscador inteligente.",
                color = Color(0xFF8E44AD),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SummaryCard(
    title: String,
    icon: String,
    color: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, CreateBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = color.copy(alpha = 0.12f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = icon, fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    color = CreateTextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = 0.5.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
    highlight: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            color = CreateTextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = if (highlight) CreateGreen else CreateTextPrimary,
            fontSize = 15.sp,
            fontWeight = if (highlight) FontWeight.ExtraBold else FontWeight.SemiBold
        )
    }
}

@Composable
fun ProductCreatedSuccessScreen(
    summary: ProductCreatedSummary,
    referenceState: ProductReferenceUiState,
    showReferenceSection: Boolean,
    onViewProduct: () -> Unit,
    onCreateAnother: () -> Unit,
    onRetryReference: () -> Unit,
    onOpenReference: () -> Unit,
    onSkipReference: () -> Unit,
    onSaveReference: (ProductReference) -> Unit
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
private fun CleanSmartReferenceCard(
    referenceState: ProductReferenceUiState,
    onOpenReference: () -> Unit,
    onSkipReference: () -> Unit,
    onSaveReference: (ProductReference) -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, CreateBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Sugerencia inteligente",
                color = CreateTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            AnimatedContent(
                targetState = referenceState.status,
                label = "smart_reference_status_animation",
                transitionSpec = {
                    (fadeIn() + slideInVertically { it / 6 })
                        .togetherWith(fadeOut() + slideOutVertically { -it / 6 })
                }
            ) { status ->
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    when (status) {
                        ProductReferenceStatus.LOADING,
                        ProductReferenceStatus.IDLE -> {
                            Text(
                                text = "Buscando información confiable...",
                                color = CreateTextSecondary,
                                fontSize = 15.sp
                            )

                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = CreateGreen,
                                trackColor = CreateBorder
                            )
                        }

                        ProductReferenceStatus.READY -> {
                            val reference = referenceState.reference
                            val summary = reference?.toSummary()
                            val confidenceValue =
                                reference?.confidence?.toString()?.toFloatOrNull() ?: 0f
                            val confidencePercent = (confidenceValue * 100f).toInt()
                            val matchedName = reference?.matchedName.orEmpty()
                            val keywords = reference?.searchKeywords.orEmpty()

                            Text(
                                text = "Información encontrada",
                                color = CreateGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            if (matchedName.isNotBlank()) {
                                Text(
                                    text = "Coincide con: $matchedName",
                                    color = CreateTextSecondary,
                                    fontSize = 14.sp
                                )
                            }

                            Text(
                                text = "Fuente: ${reference?.sourceName.orEmpty().ifBlank { "MedlinePlus en español" }}",
                                color = CreateTextSecondary,
                                fontSize = 14.sp
                            )

                            Text(
                                text = "Confianza: $confidencePercent%",
                                color = CreateTextSecondary,
                                fontSize = 14.sp
                            )

                            ReferenceSimpleBlock(
                                title = "¿Para qué sirve?",
                                value = summary?.shortUse ?: "Sin resumen disponible."
                            )

                            ReferenceSimpleBlock(
                                title = "¿Cómo se toma?",
                                value = reference?.howToUse
                                    ?: "Seguir la información del empaque o receta."
                            )

                            ReferenceSimpleBlock(
                                title = "No recomendado para",
                                value = reference?.notRecommendedFor?.joinToString(", ")
                                    ?.ifBlank { "Verificar empaque, etiqueta oficial o receta." }
                                    ?: "Verificar empaque, etiqueta oficial o receta."
                            )

                            if (keywords.isNotEmpty()) {
                                ReferenceSimpleBlock(
                                    title = "Palabras para buscar al vender",
                                    value = keywords.joinToString(", ")
                                )

                                Text(
                                    text = "Sirven para encontrar el producto por síntomas o uso, por ejemplo: fiebre, dolor de cabeza.",
                                    color = CreateTextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        ProductReferenceStatus.NOT_FOUND -> {
                            Text(
                                text = "No se encontró información confiable.",
                                color = CreateTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )

                            Text(
                                text = "Puedes agregar la información manualmente o hacerlo luego.",
                                color = CreateTextSecondary,
                                fontSize = 15.sp
                            )
                        }

                        ProductReferenceStatus.ERROR -> {
                            Text(
                                text = "No se pudo buscar la información.",
                                color = CreateTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )

                            Text(
                                text = "Revisa la conexión o agrega la información manualmente.",
                                color = CreateTextSecondary,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ReferenceSimpleBlock(
    title: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            color = CreateTextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )

        Text(
            text = value,
            color = CreateTextSecondary,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SmartReferenceSuggestionScreen(
    referenceState: ProductReferenceUiState,
    onBack: () -> Unit,
    onDiscard: () -> Unit,
    onSaveReference: (ProductReference) -> Unit
) {
    val reference = referenceState.reference ?: return

    var commonUse by remember(reference.normalizedName) {
        mutableStateOf(reference.commonUse.orEmpty())
    }

    var useCases by remember(reference.normalizedName) {
        mutableStateOf(reference.useCases.joinToString(", "))
    }

    var howToUse by remember(reference.normalizedName) {
        mutableStateOf(reference.howToUse.orEmpty())
    }

    var notRecommended by remember(reference.normalizedName) {
        mutableStateOf(reference.notRecommendedFor.joinToString(", "))
    }

    var keywords by remember(reference.normalizedName) {
        mutableStateOf(reference.searchKeywords.joinToString(", "))
    }

    Scaffold(
        containerColor = CreateBackground,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDiscard,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        onSaveReference(
                            reference.copy(
                                commonUse = commonUse.trim().ifBlank { null },
                                useCases = parseCommaList(useCases),
                                howToUse = howToUse.trim().ifBlank { null },
                                notRecommendedFor = parseCommaList(notRecommended),
                                searchKeywords = parseCommaList(keywords)
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = "Atras",
                        tint = CreateTextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Agregar información",
                    color = CreateTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                )
            }

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, CreateBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = reference.originalName,
                        color = CreateTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    Text(
                        text = "Completa la información útil para vender o recomendar mejor este producto.",
                        color = CreateTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            CleanReferenceInputCard(
                title = "¿Para qué sirve?",
                value = commonUse,
                placeholder = "Ej. Ayuda a aliviar dolor, fiebre o malestar...",
                onValueChange = { commonUse = it }
            )

            CleanReferenceInputCard(
                title = "Casos comunes",
                value = useCases,
                placeholder = "Ej. fiebre, dolor de cabeza, dolor muscular",
                helper = "Separa con comas.",
                onValueChange = { useCases = it }
            )

            CleanReferenceInputCard(
                title = "¿Cómo se toma?",
                value = howToUse,
                placeholder = "Ej. Seguir la dosis indicada en el empaque o receta.",
                onValueChange = { howToUse = it }
            )

            CleanReferenceInputCard(
                title = "No recomendado para",
                value = notRecommended,
                placeholder = "Ej. Personas alérgicas al componente activo.",
                onValueChange = { notRecommended = it }
            )

            CleanReferenceInputCard(
                title = "Palabras para buscar al vender",
                value = keywords,
                placeholder = "Ej. fiebre, dolor, alergia, tos",
                helper = "Sirven para buscar por síntomas o uso.",
                onValueChange = { keywords = it }
            )

            Surface(
                color = CreateOrangeSoft,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFFF9D7AC))
            ) {
                Text(
                    text = "Información de apoyo. Verifica siempre empaque, etiqueta oficial o receta.",
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFF9A6700),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun CleanReferenceInputCard(
    title: String,
    value: String,
    placeholder: String,
    helper: String? = null,
    onValueChange: (String) -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, CreateBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                color = CreateTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                placeholder = {
                    Text(
                        text = placeholder,
                        color = CreateTextSecondary
                    )
                },
                shape = RoundedCornerShape(16.dp)
            )

            if (!helper.isNullOrBlank()) {
                Text(
                    text = helper,
                    color = CreateTextSecondary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun ProductTypeSelector(
    selected: CreateProductControlType?,
    error: String?,
    onSelected: (CreateProductControlType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CreateProductControlType.values().forEach { controlType ->
            val selectedCurrent = selected == controlType
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelected(controlType) },
                color = if (selectedCurrent) CreateGreenSoft else Color.White,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    width = if (selectedCurrent) 1.5.dp else 1.dp,
                    color = if (selectedCurrent) CreateGreen else CreateBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (selectedCurrent) CreateGreen else Color(0xFFF2F4F7)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedCurrent) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check_circle),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = controlType.label,
                            color = CreateTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = controlType.helper,
                            color = CreateTextSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = controlType.example,
                            color = CreateTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
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


private fun sugerirPresentacionesIniciales(
    controlType: CreateProductControlType?,
    receivedPresentation: String
): List<CreateProductPresentation> {
    val recibida = receivedPresentation.trim()

    val options = if (recibida.isNotBlank()) {
        mutableListOf(recibida)
    } else {
        when (controlType) {
            CreateProductControlType.UNIDAD -> mutableListOf("Unidad", "Blister", "Caja", "Sobre", "Pack")
            CreateProductControlType.PESO -> mutableListOf("g", "100 g", "500 g", "1 kg")
            CreateProductControlType.LIQUIDO -> mutableListOf("mL", "100 mL", "250 mL", "500 mL", "1 litro")
            null -> mutableListOf()
        }
    }

    return options.distinctBy { it.lowercase(Locale.getDefault()) }.map { option ->
        val isBaseSimple = option.equals("Unidad", ignoreCase = true) ||
                option.equals("g", ignoreCase = true) ||
                option.equals("mL", ignoreCase = true)

        // Extraer número automáticamente (ej. "100 mL" -> "100")
        val autoEquivalence = if (!isBaseSimple && recibida.isBlank()) {
            Regex("\\d+").find(option)?.value?.toIntOrNull()?.toString() ?: ""
        } else {
            if (isBaseSimple) "1" else ""
        }

        CreateProductPresentation(
            id = option.lowercase(Locale.getDefault()).replace(" ", "_"),
            name = option,
            equivalenceText = autoEquivalence,
            salePriceText = ""
        )
    }
}

@Composable
fun StockEntryModeSelector(
    selected: CreateProductStockEntryMode?,
    error: String?,
    onSelected: (CreateProductStockEntryMode) -> Unit
) {
    // Nota: anteriormente la IA sugería un modo de ingreso, pero la decisión
    // depende del modelo de compra/venta del usuario (compra 1 frasco suelto
    // vs caja de 10 frascos), no del producto. La sugerencia se removió a
    // favor de un selector manual puro.
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CreateProductStockEntryMode.values().forEach { mode ->
            val selectedCurrent = selected == mode

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelected(mode) },
                color = if (selectedCurrent) CreateGreenSoft else Color.White,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(
                    width = if (selectedCurrent) 1.5.dp else 1.dp,
                    color = if (selectedCurrent) CreateGreen else CreateBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (selectedCurrent) CreateGreen else Color(0xFFF2F4F7)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedCurrent) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check_circle),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mode.label,
                            color = CreateTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = mode.helper,
                            color = CreateTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
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
fun CreateSectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, CreateBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    color = CreateTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        color = CreateTextSecondary,
                        fontSize = 13.sp
                    )
                }
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

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CreateBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = presentation.name,
                            color = CreateTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        // Marcador visual de presentación sugerida por IA.
                        // Se mantiene hasta que el usuario edite la
                        // presentación (en cuyo momento el flag pasa a false).
                        if (presentation.isAiSuggested) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "✨",
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Equivale a ${presentation.equivalenceText.ifBlank { "0" }} $unitLabel",
                        color = CreateTextSecondary,
                        fontSize = 14.sp
                    )
                }

                if (isMain) {
                    Surface(
                        color = CreateGreenSoft,
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = "Principal",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = CreateGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))
                }

                IconButton(onClick = onRemove) {
                    Icon(
                        painter = painterResource(id = R.drawable.deleteiconitem),
                        contentDescription = "Eliminar",
                        tint = Color(0xFFD92D20)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (bloquearEquivalencia) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Equivalencia",
                            color = CreateTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF6F7F9),
                            border = BorderStroke(1.dp, CreateBorder)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "${presentation.equivalenceText.ifBlank { "0" }} $unitLabel",
                                    color = CreateTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                } else {
                    AppTextField(
                        modifier = Modifier.weight(1f),
                        label = "Equivalencia",
                        value = presentation.equivalenceText,
                        placeholder = "Ej. 1",
                        keyboardType = KeyboardType.Decimal,
                        onValueChange = onEquivalenceChange
                    )
                }

                AppTextField(
                    modifier = Modifier.weight(1f),
                    label = "Precio venta",
                    value = presentation.salePriceText,
                    placeholder = "Ej. Bs 5.00",
                    keyboardType = KeyboardType.Decimal,
                    onValueChange = onPriceChange
                )
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
            text = "Presentacion principal para vender",
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
    onPrevious: () -> Unit
) {
    Surface(shadowElevation = 8.dp, color = Color.White) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (step != CreateProductStep.PRODUCTO) {
                OutlinedButton(
                    onClick = onPrevious,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Atras")
                }
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = nextEnabled
            ) {
                Text(nextLabel)
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
private fun CompactSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, CreateBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = CreateTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = CreateTextSecondary,
                    fontSize = 13.sp
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun ReferenceCard(title: String, value: String) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, CreateBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                color = CreateTextSecondary,
                fontSize = 13.sp
            )
            Text(
                text = value,
                color = CreateTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProductReferenceStatusCard(
    referenceState: ProductReferenceUiState,
    onRetryReference: () -> Unit,
    onOpenReference: () -> Unit,
    onSkipReference: () -> Unit,
    onSaveReference: (ProductReference) -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, CreateBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Referencia inteligente",
                color = CreateTextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
            when (referenceState.status) {
                ProductReferenceStatus.LOADING -> {
                    Text(
                        text = "Buscando informacion confiable en español...",
                        color = CreateTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Estamos preparando palabras clave y referencia del producto.",
                        color = CreateTextSecondary,
                        fontSize = 14.sp
                    )
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = CreateGreen,
                        trackColor = CreateBorder
                    )
                    OutlinedButton(
                        onClick = onSkipReference,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Continuar sin esperar")
                    }
                }
                ProductReferenceStatus.READY -> {
                    val reference = referenceState.reference
                    val summary = reference?.toSummary()
                    Text(
                        text = "Referencia inteligente lista",
                        color = CreateGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Fuente: ${reference?.sourceName.orEmpty().ifBlank { "MedlinePlus en español" }}",
                        color = CreateTextSecondary,
                        fontSize = 14.sp
                    )
                    summary?.shortUse?.takeIf { it.isNotBlank() }?.let {
                        ReferenceCard("Para que sirve", it)
                    }
                    summary?.keywords?.take(6)?.takeIf { it.isNotEmpty() }?.let {
                        ReferenceChipCard("Palabras clave", it)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenReference,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ver informacion")
                        }
                        Button(
                            onClick = { reference?.let(onSaveReference) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Guardar como referencia")
                        }
                    }
                    TextButton(onClick = onSkipReference) {
                        Text("Omitir por ahora")
                    }
                }
                ProductReferenceStatus.NOT_FOUND -> {
                    Text(
                        text = "No se encontró referencia confiable en español.",
                        color = CreateTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Puedes completarla manualmente.",
                        color = CreateTextSecondary,
                        fontSize = 14.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onOpenReference,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Completar manualmente")
                        }
                        OutlinedButton(
                            onClick = onSkipReference,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Omitir por ahora")
                        }
                    }
                }
                ProductReferenceStatus.ERROR -> {
                    Text(
                        text = "No se pudo consultar la fuente.",
                        color = CreateTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Revisa la conexión o completa la referencia manualmente.",
                        color = CreateTextSecondary,
                        fontSize = 14.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRetryReference,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reintentar")
                        }
                        Button(
                            onClick = onOpenReference,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Completar manualmente")
                        }
                    }
                    TextButton(onClick = onSkipReference) {
                        Text("Omitir por ahora")
                    }
                }
                ProductReferenceStatus.IDLE -> {
                    Text(
                        text = "La referencia se preparará en segundo plano cuando avances al paso 2.",
                        color = CreateTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ReferenceHeaderMeta(reference: ProductReference) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, CreateBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryRow("Nombre escrito", reference.originalName)
            SummaryRow("Coincidencia encontrada", reference.matchedName ?: "Sin coincidencia clara")
            SummaryRow("RXCUI", reference.rxcui ?: "Sin RXCUI")
            SummaryRow("Fuente", buildReferenceSourceText(reference))
            SummaryRow("Confianza", "${(reference.confidence * 100).toInt()}%")
        }
    }
}

@Composable
private fun ReferenceChipCard(title: String, items: List<String>) {
    if (items.isEmpty()) return
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, CreateBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                color = CreateTextSecondary,
                fontSize = 13.sp
            )
            FlowRowCompact(items)
        }
    }
}

@Composable
private fun FlowRowCompact(items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(3).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { item ->
                    Surface(
                        color = CreateGreenSoft,
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = item,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = CreateGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditableReferenceCard(
    title: String,
    value: String,
    helper: String? = null,
    onValueChange: (String) -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, CreateBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                color = CreateTextSecondary,
                fontSize = 13.sp
            )
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            helper?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    color = CreateTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

private fun buildReferenceSourceText(reference: ProductReference): String {
    return buildString {
        append(reference.sourceName.ifBlank { "MedlinePlus en español" })
        reference.sourceUrl?.takeIf { it.isNotBlank() }?.let {
            append(" · ")
            append(it)
        }
    }
}

private fun parseCommaList(raw: String): List<String> {
    return raw.split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
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
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val bringIntoViewScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val navigationBottom = WindowInsets.navigationBars.getBottom(density)
    val keyboardVisible = imeBottom > navigationBottom
    var hasFocus by remember { mutableStateOf(false) }

    LaunchedEffect(hasFocus, keyboardVisible, imeBottom) {
        if (hasFocus) {
            delay(if (keyboardVisible) 60 else 180)
            bringIntoViewRequester.bringIntoView()
        }
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            color = CreateTextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusChanged { focusState ->
                    hasFocus = focusState.isFocused
                },
            singleLine = true,
            placeholder = {
                if (placeholder.isNotBlank()) {
                    Text(
                        text = placeholder,
                        color = CreateTextSecondary
                    )
                }
            },
            trailingIcon = trailingLabel?.let {
                {
                    Text(
                        text = it,
                        color = CreateTextSecondary,
                        fontSize = 13.sp
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            keyboardActions = KeyboardActions(
                onDone = {
                    bringIntoViewScope.launch {
                        delay(60)
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            ),
            isError = !error.isNullOrBlank(),
            shape = RoundedCornerShape(18.dp)
        )
        if (!error.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = error,
                color = Color(0xFFB42318),
                fontSize = 13.sp
            )
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
                        fontSize = 13.sp,
                        fontWeight = if (helperHighlighted) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onHelperAction) {
                        Text(helperActionLabel)
                    }
                }
            } else {
                Text(
                    text = helper,
                    color = if (helperHighlighted) CreateGreen else CreateTextSecondary,
                    fontWeight = if (helperHighlighted) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * Botón "📷 Escanear etiqueta" + estados (idle/loading/result/error).
 *
 * Cuando hay un resultado nuevo, muestra una tarjeta con lo detectado y
 * dos acciones: [Aplicar] (rellena los campos) y [Descartar].
 */
@Composable
private fun LabelScannerRow(
    state: com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.UiState,
    onRequestScan: () -> Unit,
    onApplyResult: (com.app.administradorfarmadon.ActivityInventario.reference.EtiquetaDetectada) -> Unit,
    onDismiss: () -> Unit
) {
    val statusLoading = com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.Status.LOADING
    val statusReady = com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.Status.READY
    val statusError = com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel.Status.ERROR

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

                // Auto-aplicar al aparecer la tarjeta READY: no hay botón
                // "Aplicar" — los datos se inyectan al formulario apenas
                // se detectan. Si el vencimiento está vencido NO se aplica
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
    placeholder: String = "",
    onValueChange: (String) -> Unit
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val bringIntoViewScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val navigationBottom = WindowInsets.navigationBars.getBottom(density)
    val keyboardVisible = imeBottom > navigationBottom

    // Estado del picker visual de fecha. Se abre al tocar el icono 📅.
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

    Column {
        Text(
            text = label,
            color = CreateTextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = fieldValue,
            onValueChange = { newValue ->
                val deleting = newValue.text.length < fieldValue.text.length
                val formatted = formatExpirationDateInput(newValue.text, deleting)

                fieldValue = TextFieldValue(
                    text = formatted,
                    selection = TextRange(formatted.length)
                )

                onValueChange(formatted)
            },
            modifier = modifier
                .fillMaxWidth()
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusChanged { focusState ->
                    hasFocus = focusState.isFocused
                },
            singleLine = true,
            placeholder = {
                Text(
                    text = placeholder.ifBlank { "Mes/Año: 07/28" },
                    color = CreateTextSecondary
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "MM/AA",
                        color = CreateTextSecondary,
                        fontSize = 12.sp
                    )
                    IconButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Text(text = "📅", fontSize = 18.sp)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            keyboardActions = KeyboardActions(
                onDone = {
                    bringIntoViewScope.launch {
                        delay(60)
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            ),
            isError = !error.isNullOrBlank(),
            shape = RoundedCornerShape(18.dp)
        )

        if (!error.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = error,
                color = Color(0xFFB42318),
                fontSize = 13.sp
            )
        } else if (!helper.isNullOrBlank()) {
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
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // Date picker visual: alternativa al tipeo manual. Al confirmar, mapeamos
    // la fecha seleccionada a MM/AA y la inyectamos al campo. Solo permitimos
    // fechas desde hoy hasta el año máximo permitido (year + 10).
    if (showDatePicker) {
        val today = java.time.LocalDate.now()
        val initialMillis = parseExpirationToMillis(value, today)
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            yearRange = today.year..(today.year + 10),
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // Vencer en el pasado no tiene sentido al crear un lote.
                    val startOfTodayUtc = today.atStartOfDay(
                        java.time.ZoneOffset.UTC
                    ).toInstant().toEpochMilli()
                    return utcTimeMillis >= startOfTodayUtc
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val ld = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneOffset.UTC)
                            .toLocalDate()
                        val mm = ld.monthValue.toString().padStart(2, '0')
                        val aa = (ld.year % 100).toString().padStart(2, '0')
                        val formatted = "$mm/$aa"
                        fieldValue = TextFieldValue(
                            text = formatted,
                            selection = TextRange(formatted.length)
                        )
                        onValueChange(formatted)
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            },
            colors = DatePickerDefaults.colors()
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * Convierte un texto "MM/AA" a millis UTC para inicializar el DatePicker.
 * Si el texto está vacío o es inválido, retorna la fecha actual.
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
 * Picker de categoría dirigido por IA (Gemini).
 *
 * Renderiza distintas vistas según el `status`:
 *  - INITIAL: tarjeta vacía invitando a escribir el nombre del producto.
 *  - LOADING: tarjeta con shimmer animado mientras Gemini consulta.
 *  - READY: tarjeta hermosa con el emoji + categoría + confianza + razón.
 *           El campo `state.category` ya fue auto-rellenado por el caller,
 *           así que aquí solo hay un botón discreto para "escribir manualmente".
 *  - MANUAL / FALLBACK_MANUAL: aparece el text field tradicional con
 *           autocomplete sobre las categorías existentes.
 *
 * El text field NO se muestra fuera de los estados MANUAL/FALLBACK_MANUAL;
 * eso evita la confusión visual de "dos lugares para escribir".
 */
@Composable
private fun CategoryAutocompleteField(
    modifier: Modifier = Modifier,
    value: String,
    options: List<String>,
    error: String?,
    controlType: CreateProductControlType?,
    controlTypeError: String?,
    onValueChange: (String) -> Unit,
    onControlTypeChange: (CreateProductControlType) -> Unit,
    suggestionState: CategorySuggestionUiState = CategorySuggestionUiState(),
    onAcceptSuggestion: (CategorySuggestion) -> Unit = {},
    onSwitchToManual: () -> Unit = {},
    onBackToAi: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val status = suggestionState.status

    Column {
        // El header "Categoría" solo se muestra cuando estamos en modo manual.
        // En los estados de IA la tarjeta lleva su propio título y se siente
        // como una sola unidad visual con el tipo de control.
        val esManual = status == CategorySuggestionStatus.MANUAL ||
            status == CategorySuggestionStatus.FALLBACK_MANUAL
        if (esManual) {
            Text(
                text = "Categoría",
                color = CreateTextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        AnimatedContent(
            targetState = status,
            label = "category_picker_state",
            transitionSpec = {
                (fadeIn() + slideInVertically { it / 8 })
                    .togetherWith(fadeOut() + slideOutVertically { -it / 8 })
            }
        ) { currentStatus ->
            when (currentStatus) {
                // INITIAL: no se renderiza nada. La pantalla queda con
                // solo el campo "Nombre del producto" hasta que el usuario
                // empiece a escribir y dispare la detección por IA.
                CategorySuggestionStatus.INITIAL -> Box(modifier = Modifier)

                CategorySuggestionStatus.LOADING ->
                    AiLoadingCard(productName = suggestionState.queryName)

                CategorySuggestionStatus.READY,
                CategorySuggestionStatus.ACCEPTED -> {
                    val suggestion = suggestionState.suggestion
                    if (suggestion != null) {
                        AiResultCard(
                            suggestion = suggestion,
                            onSwitchToManual = onSwitchToManual,
                            onAccept = onAcceptSuggestion
                        )
                    } else {
                        // Estado defensivo: si por alguna razón llegamos a
                        // READY/ACCEPTED sin sugerencia, no pintamos nada
                        // para no inyectar una tarjeta vacía o confusa.
                        Box(modifier = Modifier)
                    }
                }

                CategorySuggestionStatus.MANUAL,
                CategorySuggestionStatus.FALLBACK_MANUAL -> Column(
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    CategoryManualEntry(
                        modifier = modifier,
                        value = value,
                        options = options,
                        error = error,
                        showBackToAi = suggestionState.suggestion != null &&
                            currentStatus == CategorySuggestionStatus.MANUAL,
                        onValueChange = onValueChange,
                        onBackToAi = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            onBackToAi()
                        }
                    )
                    // En modo manual reaparece el selector tradicional de tipo
                    // de producto. La tarjeta IA se hace cargo cuando el flujo
                    // IA está activo, por eso aquí solo se pinta en manual.
                    CreateSectionCard(
                        title = "Tipo de producto",
                        subtitle = "¿Cómo se controla el inventario?"
                    ) {
                        ProductTypeSelector(
                            selected = controlType,
                            error = controlTypeError,
                            onSelected = onControlTypeChange
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiLoadingCard(productName: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_loading_shimmer")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF1FBF5),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "✨", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Detectando categoría…",
                    color = CreateGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            if (productName.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Analizando \"$productName\"",
                    color = CreateTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(shimmer)
                        .clip(RoundedCornerShape(999.dp))
                        .background(CreateGreen.copy(alpha = 0.85f))
                )
            }
        }
    }
}

@Composable
private fun AiResultCard(
    suggestion: CategorySuggestion,
    onSwitchToManual: () -> Unit,
    onAccept: (CategorySuggestion) -> Unit
) {
    // Animación de aparición: escala + fade.
    val appearance = remember { Animatable(0.85f) }
    LaunchedEffect(suggestion.categoria) {
        appearance.snapTo(0.85f)
        appearance.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.55f,
                stiffness = Spring.StiffnessMediumLow
            )
        )
        // Auto-aceptar: registramos la decisión apenas se muestra el resultado.
        // El usuario no ve botón "Usar"; si no le gusta presiona el botón
        // pequeño "Escribir otra manualmente" más abajo.
        onAccept(suggestion)
    }

    val tipoLabel = when (suggestion.tipoControl) {
        com.app.administradorfarmadon.ActivityInventario.reference.TipoControlDetectado.UNIDAD ->
            "Se cuenta por unidad"
        com.app.administradorfarmadon.ActivityInventario.reference.TipoControlDetectado.PESO ->
            "Se mide por peso (g / kg)"
        com.app.administradorfarmadon.ActivityInventario.reference.TipoControlDetectado.LIQUIDO ->
            "Se mide por volumen (mL / L)"
        com.app.administradorfarmadon.ActivityInventario.reference.TipoControlDetectado.DESCONOCIDO ->
            null
    }
    val tipoEmoji = when (suggestion.tipoControl) {
        com.app.administradorfarmadon.ActivityInventario.reference.TipoControlDetectado.UNIDAD -> "🔢"
        com.app.administradorfarmadon.ActivityInventario.reference.TipoControlDetectado.PESO -> "⚖️"
        com.app.administradorfarmadon.ActivityInventario.reference.TipoControlDetectado.LIQUIDO -> "🧪"
        com.app.administradorfarmadon.ActivityInventario.reference.TipoControlDetectado.DESCONOCIDO -> "❓"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF1FBF5),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "✨", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Detectado por IA",
                    color = CreateGreen,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 0.4.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Bloque interior blanco: categoría arriba, divisor sutil, tipo abajo.
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsScale(appearance.value),
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = suggestion.emoji,
                        fontSize = 34.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "CATEGORÍA",
                        color = CreateTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = suggestion.categoria,
                        color = CreateTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )
                    if (!suggestion.existeEnLista) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = CreateOrangeSoft,
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = "Categoría nueva",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                color = CreateOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (tipoLabel != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(CreateBorder)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "TIPO DE CONTROL",
                            color = CreateTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = tipoEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tipoLabel,
                                color = CreateTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        if (suggestion.razonTipo.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = suggestion.razonTipo,
                                color = CreateTextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // --- NUEVA SECCIÓN: PRESENTACIONES SUGERIDAS POR IA ---
                    if (suggestion.presentacionesSugeridas.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(CreateBorder)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "PRESENTACIONES SUGERIDAS",
                            color = CreateTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            suggestion.presentacionesSugeridas.forEach { sug ->
                                Surface(
                                    color = Color(0xFFF8F9FB),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, CreateBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = "✨", fontSize = 10.sp)
                                        Text(
                                            text = sug.nombre,
                                            color = CreateTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "x${sug.equivalenciaUnidadBase}",
                                            color = CreateGreen,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bloque receta médica
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(CreateBorder)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "RECETA MÉDICA",
                        color = CreateTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val recetaEmoji = if (suggestion.requiereReceta) "📋" else "🟢"
                    val recetaLabel = if (suggestion.requiereReceta)
                        "Requiere receta médica"
                    else "No requiere receta"
                    val recetaColor = if (suggestion.requiereReceta) CreateOrange else CreateGreen
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = recetaEmoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = recetaLabel,
                            color = recetaColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    if (suggestion.razonReceta.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = suggestion.razonReceta,
                            color = CreateTextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Barra de confianza visual.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(suggestion.confianza / 100f)
                            .clip(RoundedCornerShape(999.dp))
                            .background(CreateGreen)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${suggestion.confianza}%",
                    color = CreateGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            if (suggestion.razon.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "“${suggestion.razon}”",
                    color = CreateTextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onSwitchToManual) {
                    Text("Cambiar manualmente")
                }
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
    showBackToAi: Boolean,
    onValueChange: (String) -> Unit,
    onBackToAi: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val suggestions = remember(value, options) {
        if (value.isBlank()) options.take(4)
        else options.filter { it.contains(value, ignoreCase = true) }.take(4)
    }

    Column {
        AppTextField(
            modifier = modifier,
            label = "",
            value = value,
            error = error,
            placeholder = "Ej. Analgesicos",
            onValueChange = onValueChange
        )

        if (suggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestions.forEach { option ->
                    FilterChip(
                        selected = option.equals(value, ignoreCase = true),
                        onClick = {
                            onValueChange(option)
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                        },
                        label = { Text(option) }
                    )
                }
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

private fun formatCreateProductNumber(value: Double): String {
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
 * lógica del cálculo principal.
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
private fun mergePresentacionEvitandoDuplicado(
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
    return calculateInitialStockValue(state)
}

private fun calculateInitialStockValue(state: CreateProductState): Double {
    return when (state.stockEntryMode) {
        null -> 0.0
        CreateProductStockEntryMode.UNIDAD -> {
            parseCreateProductNumber(state.receivedUnitsText)
        }

        CreateProductStockEntryMode.CAJA -> {
            parseCreateProductNumber(state.boxesReceivedText) *
                    parseCreateProductNumber(state.unitsPerBoxText)
        }

        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
            parseCreateProductNumber(state.boxesReceivedText) *
                    parseCreateProductNumber(state.packagesPerBoxText) *
                    parseCreateProductNumber(state.unitsPerPackageText)
        }
    }
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
    return when (getEffectiveStockControlMode(state)) {
        StockControlMode.INDIVISIBLE -> {
            // En modo envase, la unidad física es el recipiente mayor recibido.
            when (state.stockEntryMode) {
                CreateProductStockEntryMode.UNIDAD -> parseCreateProductNumber(state.receivedUnitsText).toInt()
                CreateProductStockEntryMode.CAJA -> parseCreateProductNumber(state.boxesReceivedText).toInt()
                CreateProductStockEntryMode.CAJA_CON_PAQUETES -> parseCreateProductNumber(state.boxesReceivedText).toInt()
                null -> 0
            }
        }
        StockControlMode.DIVISIBLE -> {
            // En modo divisible, la unidad física es la unidad base contable.
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
    val mode = getEffectiveStockControlMode(state)
    val isPlural = quantity != 1

    return when (mode) {
        StockControlMode.INDIVISIBLE -> {
            val baseLabel = when (state.controlType) {
                CreateProductControlType.LIQUIDO -> if (isPlural) "frascos" else "frasco"
                CreateProductControlType.PESO -> if (isPlural) "envases" else "envase"
                else -> if (isPlural) "cajas" else "caja"
            }
            baseLabel
        }
        StockControlMode.DIVISIBLE -> {
            if (isPlural) "unidades" else "unidad"
        }
    }
}

private fun calculateInitialStockInt(state: CreateProductState): Int {
    return calculateInitialStockValue(state)
        .toInt()
        .coerceAtLeast(0)
}

private fun buildInitialStockEntrySummary(state: CreateProductState): String {
    val unit = state.controlType?.baseUnitLabel ?: "unidades"

    return when (state.stockEntryMode) {
        null -> ""

        CreateProductStockEntryMode.UNIDAD -> {
            val quantity = parseCreateProductNumber(state.receivedUnitsText)
            if (quantity <= 0.0) return ""

            "${formatCreateProductNumber(quantity)} $unit disponibles en inventario."
        }

        CreateProductStockEntryMode.CAJA -> {
            val boxes = parseCreateProductNumber(state.boxesReceivedText)
            val unitsPerBox = parseCreateProductNumber(state.unitsPerBoxText)
            val total = boxes * unitsPerBox

            if (boxes <= 0.0 || unitsPerBox <= 0.0) return ""

            "${formatCreateProductNumber(boxes)} cajas x " +
                    "${formatCreateProductNumber(unitsPerBox)} $unit = " +
                    "${formatCreateProductNumber(total)} $unit disponibles en inventario."
        }

        CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
            val boxes = parseCreateProductNumber(state.boxesReceivedText)
            val packagesPerBox = parseCreateProductNumber(state.packagesPerBoxText)
            val unitsPerPackage = parseCreateProductNumber(state.unitsPerPackageText)
            val total = boxes * packagesPerBox * unitsPerPackage

            if (boxes <= 0.0 || packagesPerBox <= 0.0 || unitsPerPackage <= 0.0) return ""

            "${formatCreateProductNumber(boxes)} cajas x " +
                    "${formatCreateProductNumber(packagesPerBox)} paquetes x " +
                    "${formatCreateProductNumber(unitsPerPackage)} $unit = " +
                    "${formatCreateProductNumber(total)} $unit disponibles en inventario."
        }
    }
}

private fun buildStockAvailableSummary(state: CreateProductState): String {
    val total = calculateInitialStockValue(state)
    val totalText = formatCreateProductNumber(total)
    val unit = state.controlType?.baseUnitLabel ?: "unidades"
    return "$totalText $unit"
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

    // Crear fecha de vencimiento (último día del mes)
    val expirationDate = runCatching {
        YearMonth.of(fullYear, month).atEndOfMonth()
    }.getOrNull()

    if (expirationDate == null) {
        return ExpirationStatus(
            kind = ExpirationStatusKind.INVALIDO,
            message = "Fecha inválida",
            color = CreateRed
        )
    }

    val today = LocalDate.now()
    if (expirationDate.isBefore(today)) {
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
        YearMonth.from(today),
        YearMonth.from(expirationDate)
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
