package com.app.administradorfarmadon.ActivitysPerfilItem

import android.annotation.SuppressLint
import android.app.Activity
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import android.view.SoundEffectConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.animation.animateColorAsState
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreConfigScreen(
    viewModel: StoreConfigViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    // AGREGA ESTAS DOS LÍNEAS AQUÍ:
    val estados by viewModel.states.collectAsState(initial = emptyList())
    val ciudades by viewModel.cities.collectAsState(initial = emptyList())
    val scrollState = rememberScrollState()
    val context = LocalContext.current as? Activity
    val countryInfo = remember(uiState.config.pais) { CountryCatalog.getCountry(uiState.config.pais) }
    val view = LocalView.current
    val pendingSection = remember(uiState.activeSection, uiState.dirtySections) {
        uiState.activeSection?.takeIf { it in uiState.dirtySections } ?: uiState.dirtySections.firstOrNull()
    }

    var showExitDialog by remember { mutableStateOf(false) }

    // Manejo de salida con cambios pendientes
    BackHandler(enabled = uiState.dirtySections.isNotEmpty()) {
        showExitDialog = true
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            view.playSoundEffect(SoundEffectConstants.CLICK)
            playFeedbackTone(success = true)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            view.playSoundEffect(SoundEffectConstants.NAVIGATION_UP) // Sonido de error/alerta
            playFeedbackTone(success = false)
        }
    }

    LaunchedEffect(uiState.lastSavedSection) {
        if (uiState.lastSavedSection != null) {
            delay(2200)
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuracion del negocio") },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (uiState.dirtySections.isNotEmpty()) showExitDialog = true
                        else context?.finish() 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        bottomBar = {
            // Usamos AnimatedVisibility para que el botón suba y baje con suavidad
            AnimatedVisibility(
                visible = pendingSection != null || uiState.saveSuccess,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                // Si guardamos con éxito, mostramos feedback positivo momentáneo
                if (uiState.saveSuccess && uiState.lastSavedSection != null) {
                    SuccessSaveBar(section = uiState.lastSavedSection!!)
                } else {
                    // Solo intentamos renderizar el StickySaveBar si hay una sección pendiente
                    pendingSection?.let { section ->
                        StickySaveBar(
                            section = section,
                            dirtyCount = uiState.dirtySections.size,
                            isSaving = uiState.isSaving,
                            onSave = viewModel::saveDirtySections
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(scrollState)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryCard(uiState)
                SmartBusinessHintCard(uiState.config, countryInfo)
                SectionNegocio(
                    status = rememberSectionStatus(StoreConfigSection.NEGOCIO, uiState),
                    config = uiState.config,
                    errors = uiState.fieldErrors,
                    onConfigChanged = viewModel::onConfigChanged,
                    onDiscreteConfigChanged = viewModel::onDiscreteConfigChanged
                )
                SectionMercado(
                    status = rememberSectionStatus(StoreConfigSection.MERCADO, uiState),
                    config = uiState.config,
                    errors = uiState.fieldErrors,
                    estados = estados,
                    ciudades = ciudades,
                    onCountrySelected = viewModel::onCountrySelected,
                    onStateSelected = viewModel::onStateSelected,
                    onConfigChanged = viewModel::onConfigChanged
                )
                SectionFiscal(
                    status = rememberSectionStatus(StoreConfigSection.FISCAL, uiState),
                    config = uiState.config,
                    errors = uiState.fieldErrors,
                    countryInfo = countryInfo,
                    onConfigChanged = viewModel::onConfigChanged,
                    onDiscreteConfigChanged = viewModel::onDiscreteConfigChanged
                )
                SectionOperacion(
                    status = rememberSectionStatus(StoreConfigSection.OPERACION, uiState),
                    config = uiState.config,
                    errors = uiState.fieldErrors,
                    onConfigChanged = viewModel::onConfigChanged,
                    onDiscreteConfigChanged = viewModel::onDiscreteConfigChanged
                )
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Cambios sin guardar") },
            text = { Text("Tienes cambios pendientes en ${uiState.dirtySections.joinToString { it.displayName }}. ¿Deseas salir sin guardar?") },
            confirmButton = {
                TextButton(onClick = { 
                    showExitDialog = false
                    context?.finish() 
                }) {
                    Text("Salir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                Button(onClick = { showExitDialog = false }) {
                    Text("Continuar editando")
                }
            }
        )
    }

    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("Revisa la configuracion") },
            text = { Text(uiState.errorMessage ?: "") },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
private fun SuccessSaveBar(
    section: StoreConfigSection
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 16.dp,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "${section.displayName} guardado con éxito",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StickySaveBar(
    section: StoreConfigSection,
    dirtyCount: Int,
    isSaving: Boolean,
    onSave: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 16.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = buildSaveBarHint(section, dirtyCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onSave,
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isSaving) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Text(
                            "Guardando cambios...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        "Guardar Cambios",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(uiState: StoreConfigUIState) {
    val config = uiState.config
    
    // Cálculo de progreso de configuración
    val completionData = remember(config) {
        val totalPoints = 9
        var filledPoints = 0
        if (config.nombreComercial.isNotBlank()) filledPoints++
        if (config.razonSocial.isNotBlank()) filledPoints++
        if (config.pais.isNotBlank()) filledPoints++
        if (config.estado.isNotBlank()) filledPoints++
        if (config.ciudad.isNotBlank()) filledPoints++
        if (config.direccion.isNotBlank()) filledPoints++
        if (config.nroDocumentoFiscal.isNotBlank()) filledPoints++
        if (config.cobraImpuestos) filledPoints++
        if (config.controlarLotes || config.controlarVencimientos) filledPoints++
        
        val percent = (filledPoints.toFloat() / totalPoints.toFloat() * 100f).toInt()
        percent to (filledPoints == totalPoints)
    }
    
    val (progress, isComplete) = completionData

    val gradient = if (isComplete) {
        Brush.linearGradient(listOf(Color(0xFF1B5E20), Color(0xFF43A047), Color(0xFF66BB6A)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF0D47A1), Color(0xFF1976D2), Color(0xFF42A5F5)))
    }

    val contentColor = Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(contentColor.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Icon(
                            imageVector = if (isComplete) Icons.Default.Verified else Icons.Default.Store,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = config.nombreComercial.ifBlank { "Configuración de Tienda" },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Public,
                                null,
                                tint = contentColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (config.pais.isNotBlank()) {
                                    "${config.pais}${if (config.ciudad.isNotBlank()) " · ${config.ciudad}" else ""}"
                                } else "Región no definida",
                                style = MaterialTheme.typography.bodyMedium,
                                color = contentColor.copy(alpha = 0.85f)
                            )
                        }
                    }
                    
                    // Badge de progreso circular o porcentual
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "$progress%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = contentColor
                        )
                        Text(
                            "Progreso",
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.7f)
                        )
                    }
                }

                HorizontalDivider(color = contentColor.copy(alpha = 0.15f), thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HeroMetric(
                        value = if (config.cobraImpuestos) "${config.porcentajeImpuesto.toInt()}% ${config.nombreImpuesto}" else "Exento",
                        label = "Régimen Fiscal",
                        icon = Icons.Default.Receipt
                    )
                    HeroMetric(
                        value = when {
                            config.controlarLotes && config.controlarVencimientos -> "Control Total"
                            config.controlarLotes || config.controlarVencimientos -> "Trazabilidad"
                            else -> "Básico"
                        },
                        label = "Operación",
                        icon = Icons.Default.Inventory
                    )
                }

                uiState.saveSuccessMessage?.let { SaveFeedbackChip(it) }
            }
        }
    }
}

@Composable
private fun HeroMetric(
    value: String,
    label: String,
    icon: ImageVector
) {
    Column(
        modifier = Modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun buildSaveBarHint(section: StoreConfigSection, dirtyCount: Int): String {
    return if (dirtyCount <= 1) {
        "Hay cambios sin guardar en ${section.displayName}"
    } else {
        "Hay cambios sin guardar en ${section.displayName} y ${dirtyCount - 1} seccion(es) mas"
    }
}

private fun playFeedbackTone(success: Boolean) {
    try {
        val tone = android.media.ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, 90)
        val toneType = if (success) android.media.ToneGenerator.TONE_PROP_ACK else android.media.ToneGenerator.TONE_PROP_NACK
        tone.startTone(toneType, 180)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ tone.release() }, 250)
    } catch (e: Exception) {}
}

@Composable
private fun SaveFeedbackChip(text: String) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SmartBusinessHintCard(config: StoreConfig, countryInfo: CountryInfo?) {
    val isAllOrder = config.pais.isNotBlank() &&
            config.estado.isNotBlank() &&
            config.ciudad.isNotBlank() &&
            config.direccion.isNotBlank() &&
            config.cobraImpuestos &&
            config.controlarLotes &&
            config.controlarVencimientos &&
            config.mensajeTicket.isNotBlank()

    val hint = remember(config, countryInfo) {
        when {
            config.pais.isBlank() -> "Define el pais de operacion para ajustar moneda e impuestos de forma automatica."
            config.estado.isBlank() -> "Completa el estado o departamento para terminar de ubicar tu negocio regionalmente."
            config.ciudad.isBlank() -> "Completa la ciudad para cerrar bien la ubicacion operativa de la tienda."
            config.direccion.isBlank() -> "Agrega una direccion exacta para que aparezca correctamente en tus comprobantes."
            !config.cobraImpuestos -> "Tu tienda no esta cobrando impuesto. Si vendes formalmente en ${config.pais}, revisa si debes activar ${countryInfo?.defaultTaxName ?: "el impuesto"}."
            !config.controlarLotes || !config.controlarVencimientos -> "Para farmacia o botica conviene activar lotes y vencimientos para una mejor trazabilidad y control de caducidad."
            config.mensajeTicket.isBlank() -> "Como toque final, puedes agregar un mensaje de agradecimiento personalizado para tus tickets de venta."
            else -> "¡Todo en orden! Tu configuracion base es excelente y saludable."
        }
    }

    val gradient = if (isAllOrder) {
        Brush.horizontalGradient(listOf(Color(0xFF1B5E20), Color(0xFF43A047)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFFB71C1C), Color(0xFFE53935)))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    if (isAllOrder) Icons.Default.CheckCircle else Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        if (isAllOrder) "Tienda lista" else "Sugerencia inteligente",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}



@Composable
private fun rememberSectionStatus(section: StoreConfigSection, uiState: StoreConfigUIState): SectionPanelStatus {
    return remember(uiState.savingSection, uiState.dirtySections, uiState.lastSavedSection) {
        when {
            uiState.savingSection == section -> SectionPanelStatus.SAVING
            section in uiState.dirtySections -> SectionPanelStatus.PENDING
            uiState.lastSavedSection == section -> SectionPanelStatus.SAVED
            else -> SectionPanelStatus.CLEAN
        }
    }
}

private enum class SectionPanelStatus {
    CLEAN,
    PENDING,
    SAVING,
    SAVED
}

@Composable
private fun PanelSectionCard(
    title: String,
    subtitle: String,
    status: SectionPanelStatus,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
                SectionStatusChip(status)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Contenedor interno para los inputs con un fondo sutil
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SectionStatusChip(status: SectionPanelStatus) {
    val (text, fg, bg) = when (status) {
        SectionPanelStatus.CLEAN -> Triple("Listo", MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
        SectionPanelStatus.PENDING -> Triple("Pendiente", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
        SectionPanelStatus.SAVING -> Triple("Guardando", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
        SectionPanelStatus.SAVED -> Triple("Guardado", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = fg,
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}

@Composable
private fun SectionNegocio(
    status: SectionPanelStatus,
    config: StoreConfig,
    errors: Map<String, String>,
    onConfigChanged: (StoreConfig) -> Unit,
    onDiscreteConfigChanged: (StoreConfigSection, StoreConfig) -> Unit
) {
    PanelSectionCard(
        title = "1. Identidad del negocio",
        subtitle = "Define como se presenta la farmacia y bajo que tipo opera.",
        status = status
    ) {
            AppOutlinedField(
                value = config.nombreComercial,
                onValueChange = { onConfigChanged(config.copy(nombreComercial = it)) },
                label = "Nombre comercial",
                error = errors["nombreComercial"]
            )
            AppOutlinedField(
                value = config.razonSocial,
                onValueChange = { onConfigChanged(config.copy(razonSocial = it)) },
                label = "Razon social",
                error = errors["razonSocial"]
            )

            Text("Tipo de negocio", style = MaterialTheme.typography.bodySmall)
            BusinessType.entries.forEach { type ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = config.tipoNegocio == type.name,
                        onClick = { onDiscreteConfigChanged(StoreConfigSection.NEGOCIO, config.copy(tipoNegocio = type.name)) }
                    )
                    Text(type.displayName)
                }
            }
    }
}

@Composable
private fun SectionMercado(
    status: SectionPanelStatus,
    config: StoreConfig,
    errors: Map<String, String>,
    estados: List<String>, // Lista de estados que viene del ViewModel
    ciudades: List<String>, // Lista de ciudades que viene del ViewModel
    onCountrySelected: (String) -> Unit,
    onStateSelected: (String) -> Unit,
    onConfigChanged: (StoreConfig) -> Unit
) {
    PanelSectionCard(
        title = "2. Ubicación y Moneda",
        subtitle = "Configura la región de operación. La moneda y los impuestos se ajustarán solos.",
        status = status
    ) {
        // 1. SELECTOR DE PAÍS
        AppDropdown(
            label = "País de operación",
            options = CountryCatalog.countryNames(),
            selectedOption = config.pais,
            error = errors["pais"],
            onOptionSelected = { nuevoPais ->
                onCountrySelected(nuevoPais)
            }
        )

        // 2. SELECTOR DE ESTADO / DEPARTAMENTO
        AppDropdown(
            label = "Estado / Departamento",
            options = estados,
            selectedOption = config.estado,
            enabled = estados.isNotEmpty(),
            error = errors["estado"],
            onOptionSelected = { nuevoEstado ->
                onStateSelected(nuevoEstado)
            }
        )

        // 3. SELECTOR DE CIUDAD
        AppDropdown(
            label = "Ciudad",
            options = ciudades,
            selectedOption = config.ciudad,
            enabled = ciudades.isNotEmpty(),
            error = errors["ciudad"],
            onOptionSelected = { nuevaCiudad ->
                onConfigChanged(config.copy(ciudad = nuevaCiudad))
            }
        )

        // 4. DIRECCIÓN (Sigue siendo manual porque es texto libre)
        AppOutlinedField(
            value = config.direccion,
            onValueChange = { onConfigChanged(config.copy(direccion = it)) },
            label = "Dirección exacta",
            error = errors["direccion"],
            icon = Icons.Default.Map
        )

        // VISUALIZACIÓN DE MONEDA (Informativa, no se toca)
        Text(
            text = "Datos de moneda (automáticos)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(16.dp)
                )
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoDisplayItem(
                label = "Código",
                value = config.monedaCodigo,
                icon = Icons.Default.CurrencyExchange,
                modifier = Modifier.weight(1f)
            )
            InfoDisplayItem(
                label = "Símbolo",
                value = config.monedaSimbolo,
                icon = Icons.Default.Payments,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InfoDisplayItem(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value.ifBlank { "---" },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Función auxiliar para un toque divertido y visual
fun getFlagEmoji(countryName: String): String {
    return when (countryName) {
        "Perú" -> "🇵🇪"
        "Bolivia" -> "🇧🇴"
        "Venezuela" -> "🇻🇪"
        "Colombia" -> "🇨🇴"
        "Ecuador" -> "🇪🇨"
        else -> "🌐"
    }
}

@Composable
private fun SectionFiscal(
    status: SectionPanelStatus,
    config: StoreConfig,
    errors: Map<String, String>,
    countryInfo: CountryInfo?,
    onConfigChanged: (StoreConfig) -> Unit,
    onDiscreteConfigChanged: (StoreConfigSection, StoreConfig) -> Unit
) {
    // ¡Adiós a la variable 'expanded'! Ya no la necesitamos.
    val documentOptions = countryInfo?.fiscalDocOptions.orEmpty()

    PanelSectionCard(
        title = "3. Fiscal",
        subtitle = "Documento principal e impuestos que afectan las ventas.",
        status = status
    ) {
        // Selector de Documento Fiscal usando tu nuevo componente limpio
        if (documentOptions.isNotEmpty()) {
            AppDropdown(
                label = "Tipo de documento fiscal",
                options = documentOptions,
                selectedOption = config.tipoDocumentoFiscal,
                error = errors["tipoDocumentoFiscal"],
                onOptionSelected = { opcion ->
                    onDiscreteConfigChanged(StoreConfigSection.FISCAL, config.copy(tipoDocumentoFiscal = opcion))
                }
            )
        }

        AppOutlinedField(
            value = config.nroDocumentoFiscal,
            onValueChange = { onConfigChanged(config.copy(nroDocumentoFiscal = it)) },
            label = "Número de ${config.tipoDocumentoFiscal.ifBlank { "documento" }}",
            error = errors["nroDocumentoFiscal"],
            icon = Icons.Default.Numbers
        )

        // Switch de Impuestos con diseño limpio
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(
                    if (config.cobraImpuestos) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                    else Color.Transparent,
                    RoundedCornerShape(12.dp)
                )
                .padding(8.dp)
        ) {
            Switch(
                checked = config.cobraImpuestos,
                onCheckedChange = {
                    val countryTax = countryInfo?.defaultTaxRate ?: config.porcentajeImpuesto
                    onDiscreteConfigChanged(
                        StoreConfigSection.FISCAL,
                        config.copy(
                            cobraImpuestos = it,
                            porcentajeImpuesto = if (it && config.porcentajeImpuesto == 0.0) countryTax else config.porcentajeImpuesto
                        )
                    )
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Cobrar impuestos",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (config.cobraImpuestos) "Activo para ventas" else "Desactivado",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(
            visible = config.cobraImpuestos,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppOutlinedField(
                    value = config.nombreImpuesto,
                    onValueChange = { onConfigChanged(config.copy(nombreImpuesto = it)) },
                    label = "Nombre del impuesto",
                    error = errors["nombreImpuesto"],
                    icon = Icons.Default.Description
                )

                var textValue by remember(config.porcentajeImpuesto) {
                    mutableStateOf(if(config.porcentajeImpuesto == 0.0) "" else config.porcentajeImpuesto.toString())
                }

                AppOutlinedField(
                    value = textValue,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() || it == '.' }) {
                            textValue = newValue
                            val parsed = newValue.toDoubleOrNull() ?: 0.0
                            onConfigChanged(config.copy(porcentajeImpuesto = parsed))
                        }
                    },
                    label = "Porcentaje (%)",
                    error = errors["porcentajeImpuesto"],
                    icon = Icons.Default.Percent
                )
            }
        }
    }
}

@Composable
private fun SectionOperacion(
    status: SectionPanelStatus,
    config: StoreConfig,
    errors: Map<String, String>,
    onConfigChanged: (StoreConfig) -> Unit,
    onDiscreteConfigChanged: (StoreConfigSection, StoreConfig) -> Unit
) {
    PanelSectionCard(
        title = "4. Reglas de Operación",
        subtitle = "Configura la inteligencia y controles de inventario de tu negocio.",
        status = status
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            OperationToggle(
                title = "Control de Rentabilidad",
                description = "Evita ventas por debajo del margen mínimo permitido.",
                icon = Icons.Default.TrendingUp,
                checked = config.usarMargenMinimo,
                onCheckedChange = { onDiscreteConfigChanged(StoreConfigSection.OPERACION, config.copy(usarMargenMinimo = it)) }
            )

            AnimatedVisibility(visible = config.usarMargenMinimo) {
                AppOutlinedField(
                    value = config.margenMinimoDefault.takeUnless { it == 0.0 }?.toString().orEmpty(),
                    onValueChange = { onConfigChanged(config.copy(margenMinimoDefault = it.toDoubleOrNull() ?: 0.0)) },
                    label = "Margen mínimo sugerido (%)",
                    error = errors["margenMinimoDefault"],
                    icon = Icons.Default.Percent
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            OperationToggle(
                title = "Gestión de Lotes y Vencimientos",
                description = "Activa la trazabilidad y alertas de caducidad de medicamentos.",
                icon = Icons.Default.Inventory,
                checked = config.controlarLotes || config.controlarVencimientos, // Agrupado para simplificar
                onCheckedChange = {
                    onDiscreteConfigChanged(StoreConfigSection.OPERACION, config.copy(controlarLotes = it, controlarVencimientos = it))
                }
            )

            AppOutlinedField(
                value = config.mensajeTicket,
                onValueChange = { onConfigChanged(config.copy(mensajeTicket = it)) },
                label = "Mensaje de agradecimiento en Ticket",
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                minLines = 2,
                icon = Icons.Default.Receipt
            )
        }
    }
}




@Composable
private fun OperationToggle(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun AppOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String? = null,
    icon: ImageVector? = null, // Añadimos icono
    modifier: Modifier = Modifier.fillMaxWidth(),
    minLines: Int = 1
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = icon?.let { { Icon(it, contentDescription = null, modifier = Modifier.size(20.dp)) } },
            isError = error != null,
            shape = RoundedCornerShape(16.dp), // Bordes más suaves
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
            minLines = minLines,
            supportingText = {
                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}

@Composable
private fun SwitchRow(
    checked: Boolean,
    label: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    enabled: Boolean = true,
    error: String? = null,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            enabled = enabled,
            isError = error != null,
            shape = RoundedCornerShape(12.dp),
            supportingText = {
                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
