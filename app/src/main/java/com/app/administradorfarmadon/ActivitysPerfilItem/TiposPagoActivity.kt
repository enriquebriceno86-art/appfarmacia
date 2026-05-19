package com.app.administradorfarmadon.ActivitysPerfilItem

import android.widget.ImageView
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.Glide

class TiposPagoActivity : ComponentActivity() {
    private val viewModel: TiposPagoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TiposPagoScreen(viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TiposPagoScreen(
    viewModel: TiposPagoViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current as? TiposPagoActivity
    var methodPendingDelete by remember { mutableStateOf<MetodoPagoConfig?>(null) }
    var qrUploadTarget by remember { mutableStateOf<QrUploadTarget?>(null) }
    val qrPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val target = qrUploadTarget
        qrUploadTarget = null
        if (uri != null && target != null) {
            when (target) {
                is QrUploadTarget.EditorForm -> {
                    viewModel.updateForm { current -> current.copy(qrUrl = uri.toString(), usaQr = true) }
                }
                is QrUploadTarget.ExistingMethod -> {
                    viewModel.saveQrImageForMethod(target.methodId, uri.toString())
                }
            }
        }
    }
    val handleAiInsightClick: (PaymentAiInsight) -> Unit = { insight ->
        val targetMethod = state.methods.firstOrNull {
            it.titulo.equals(insight.targetMethodTitle.orEmpty(), ignoreCase = true)
        }
        val shouldOpenDirectQrUpload = insight.actionType == "EDIT_EXISTING" &&
            targetMethod != null &&
            targetMethod.usaQR &&
            targetMethod.qrUrl.isBlank()

        if (shouldOpenDirectQrUpload) {
            qrUploadTarget = QrUploadTarget.ExistingMethod(targetMethod.id)
            qrPickerLauncher.launch("image/*")
        } else {
            viewModel.handleAiInsight(insight)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tipos de pago") },
                navigationIcon = {
                    IconButton(onClick = { context?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, tonalElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = { viewModel.seedMissingSuggestions() },
                        enabled = !state.isSaving && state.suggestions.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (state.isSaving) "Guardando..." else "Agregar sugeridos del pais")
                    }
                    Button(
                        onClick = { viewModel.showCreateEditor() },
                        enabled = !state.isSaving,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Nuevo metodo")
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                val isTablet = maxWidth >= 920.dp
                if (isTablet) {
                    TabletTiposPagoLayout(
                        state = state,
                        onOpenSuggestion = { viewModel.showCreateEditor(it) },
                        onApplyAiInsight = handleAiInsightClick,
                        onRefreshAiReview = viewModel::refreshAiReview,
                        onEdit = { viewModel.showEditEditor(it) },
                        onDelete = { methodPendingDelete = it }
                    )
                } else {
                    MobileTiposPagoLayout(
                        state = state,
                        onOpenSuggestion = { viewModel.showCreateEditor(it) },
                        onApplyAiInsight = handleAiInsightClick,
                        onRefreshAiReview = viewModel::refreshAiReview,
                        onEdit = { viewModel.showEditEditor(it) },
                        onDelete = { methodPendingDelete = it }
                    )
                }
            }
        }
    }

    if (state.showEditor) {
        EditorMetodoPagoSheet(
            form = state.form,
            isSaving = state.isSaving,
            onDismiss = viewModel::dismissEditor,
            onFormChange = viewModel::updateForm,
            onPickQrImage = {
                qrUploadTarget = QrUploadTarget.EditorForm
                qrPickerLauncher.launch("image/*")
            },
            onSave = viewModel::saveMethod
        )
    }

    if (state.errorMessage != null) {
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("Revisa la configuracion") },
            text = { Text(state.errorMessage ?: "") },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) {
                    Text("Entendido")
                }
            }
        )
    }

    if (methodPendingDelete != null) {
        val method = methodPendingDelete!!
        AlertDialog(
            onDismissRequest = { methodPendingDelete = null },
            title = { Text("Eliminar tipo de pago") },
            text = { Text("Se quitara '${method.titulo}' de la lista disponible para cobros.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        methodPendingDelete = null
                        viewModel.deleteMethod(method)
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { methodPendingDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun MobileTiposPagoLayout(
    state: TiposPagoUiState,
    onOpenSuggestion: (PaymentMethodSuggestion) -> Unit,
    onApplyAiInsight: (PaymentAiInsight) -> Unit,
    onRefreshAiReview: () -> Unit,
    onEdit: (MetodoPagoConfig) -> Unit,
    onDelete: (MetodoPagoConfig) -> Unit
) {
    val auditItems = remember(state.methods, state.country, state.suggestions) {
        buildPaymentAudit(state.country, state.methods, state.suggestions)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { HeroCard(state) }
        item {
            AiInsightPanel(
                isLoading = state.isAiReviewLoading,
                insights = state.aiInsights,
                methods = state.methods,
                onInsightClick = onApplyAiInsight,
                onRefresh = onRefreshAiReview
            )
        }
        item { AuditPanel(auditItems) }
        item { SuggestionsPanel(state, onOpenSuggestion) }
        val activeCount = state.methods.count { it.activo }
        item { SectionHeader("Configurados", "$activeCount activos de ${state.methods.size} totales") }
        if (state.methods.isEmpty()) {
            item { EmptyConfiguredCard(state.country) }
        } else {
            items(state.methods, key = { it.id }) { method ->
                MetodoPagoCard(method = method, onEdit = { onEdit(method) }, onDelete = { onDelete(method) })
            }
        }
    }
}

@Composable
private fun TabletTiposPagoLayout(
    state: TiposPagoUiState,
    onOpenSuggestion: (PaymentMethodSuggestion) -> Unit,
    onApplyAiInsight: (PaymentAiInsight) -> Unit,
    onRefreshAiReview: () -> Unit,
    onEdit: (MetodoPagoConfig) -> Unit,
    onDelete: (MetodoPagoConfig) -> Unit
) {
    val auditItems = remember(state.methods, state.country, state.suggestions) {
        buildPaymentAudit(state.country, state.methods, state.suggestions)
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .width(360.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeroCard(state)
            AiInsightPanel(
                isLoading = state.isAiReviewLoading,
                insights = state.aiInsights,
                methods = state.methods,
                onInsightClick = onApplyAiInsight,
                onRefresh = onRefreshAiReview
            )
            AuditPanel(auditItems)
            SuggestionsPanel(state, onOpenSuggestion)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            val activeCount = state.methods.count { it.activo }
            SectionHeader("Configurados", "$activeCount activos de ${state.methods.size} totales")
            Spacer(Modifier.height(12.dp))
            if (state.methods.isEmpty()) {
                EmptyConfiguredCard(state.country)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(state.methods, key = { it.id }) { method ->
                        MetodoPagoCard(method = method, onEdit = { onEdit(method) }, onDelete = { onDelete(method) })
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(state: TiposPagoUiState) {
    val readyCount = state.methods.count { evaluateMethodHealth(it).level == PaymentHealthLevel.READY }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                RoundedCornerShape(18.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Column {
                        Text(
                            "Cobros adaptados a ${state.country}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Sugerimos metodos usados en tu mercado y formularios acordes al tipo de pago.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeroMetric("${state.methods.count { it.activo }}", "Activos")
                    HeroMetric("$readyCount", "Listos")
                    HeroMetric("${state.suggestions.size}", "Sugeridos")
                }
            }
        }
    }
}

@Composable
private fun HeroMetric(value: String, label: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SuggestionsPanel(
    state: TiposPagoUiState,
    onOpenSuggestion: (PaymentMethodSuggestion) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Sugeridos para ${state.country}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Plantillas listas para farmacia segun el pais donde opera la tienda.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (state.suggestions.isEmpty()) {
                Text(
                    "Ya tienes todos los sugeridos cargados. Puedes crear uno manual si tu negocio necesita un caso especial.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.suggestions.forEach { suggestion ->
                        AssistChip(
                            onClick = { onOpenSuggestion(suggestion) },
                            label = { Text(suggestion.title) },
                            leadingIcon = {
                                if (suggestion.usaQr) {
                                    Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiInsightPanel(
    isLoading: Boolean,
    insights: List<PaymentAiInsight>,
    methods: List<MetodoPagoConfig>,
    onInsightClick: (PaymentAiInsight) -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Recomendado por IA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Analiza tu mix actual y te sugiere el siguiente mejor paso segun tu pais.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onRefresh, enabled = !isLoading) {
                    Text(if (isLoading) "Analizando..." else "Actualizar")
                }
            }

            if (isLoading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text(
                        "La IA esta revisando tus metodos de pago...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else if (insights.isEmpty()) {
                Text(
                    "Todavia no hay recomendaciones IA disponibles para esta configuracion.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    insights.forEach { insight ->
                        val actionLabel = when {
                            insight.actionType == "ADD_SUGGESTED" -> "Crear"
                            insight.actionType == "EDIT_EXISTING" &&
                                methods.any {
                                    it.titulo.equals(insight.targetMethodTitle.orEmpty(), ignoreCase = true) &&
                                        it.usaQR && it.qrUrl.isBlank()
                                } -> "Subir QR"
                            insight.actionType == "EDIT_EXISTING" -> "Completar"
                            else -> null
                        }
                        AiInsightRow(
                            insight = insight,
                            actionLabel = actionLabel,
                            onClick = { onInsightClick(insight) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiInsightRow(
    insight: PaymentAiInsight,
    actionLabel: String?,
    onClick: () -> Unit
) {
    val accent = when (insight.priority.uppercase()) {
        "ALTA" -> MaterialTheme.colorScheme.error
        "MEDIA" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .background(accent.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                .padding(8.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(insight.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                insight.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (actionLabel != null) {
                TextButton(onClick = onClick) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
private fun AuditPanel(items: List<PaymentAuditItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(16.dp))
                        .padding(10.dp)
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                Column {
                    Text(
                        "Auditoria inteligente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Te muestra lo que ya esta listo y lo que conviene completar para cobrar mejor.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items.forEach { item ->
                AuditRow(item)
            }
        }
    }
}

@Composable
private fun AuditRow(item: PaymentAuditItem) {
    val bg = when (item.level) {
        PaymentHealthLevel.READY -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        PaymentHealthLevel.WARNING -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
        PaymentHealthLevel.CRITICAL -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f)
    }
    val fg = when (item.level) {
        PaymentHealthLevel.READY -> MaterialTheme.colorScheme.primary
        PaymentHealthLevel.WARNING -> MaterialTheme.colorScheme.secondary
        PaymentHealthLevel.CRITICAL -> MaterialTheme.colorScheme.error
    }
    val icon = when (item.level) {
        PaymentHealthLevel.READY -> Icons.Default.CheckCircle
        PaymentHealthLevel.WARNING -> Icons.Default.Info
        PaymentHealthLevel.CRITICAL -> Icons.Default.WarningAmber
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(18.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = fg)
        Column {
            Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = fg)
            Text(
                item.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyConfiguredCard(country: String) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Aun no tienes metodos configurados.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Empieza con los sugeridos para $country o crea un metodo manual para tu flujo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetodoPagoCard(
    method: MetodoPagoConfig,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val health = remember(method) { evaluateMethodHealth(method) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(methodAccent(method).copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                            .padding(12.dp)
                    ) {
                        Icon(methodIcon(method), contentDescription = null, tint = methodAccent(method))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(method.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            categoryLabel(method.categoria),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusBadge(text = if (method.activo) "Activo" else "Inactivo")
                    ReadinessBadge(health)
                }
            }

            Text(
                text = buildMethodSummary(method),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistPill(text = "Orden ${method.orden}")
                if (method.disponibleMixto) AssistPill(text = "Mixto")
                if (method.usaQR) AssistPill(text = "QR")
                if (method.usaQR && method.qrUrl.isBlank()) AssistPill(text = "QR pendiente")
            }

            CashierPreviewCard(method = method)

            HorizontalDivider()

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Editar")
                }
                TextButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Eliminar")
                }
            }
        }
    }
}

@Composable
private fun AssistPill(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

@Composable
private fun ReadinessBadge(health: PaymentMethodHealth) {
    val bg = when (health.level) {
        PaymentHealthLevel.READY -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        PaymentHealthLevel.WARNING -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
        PaymentHealthLevel.CRITICAL -> MaterialTheme.colorScheme.error.copy(alpha = 0.14f)
    }
    val fg = when (health.level) {
        PaymentHealthLevel.READY -> MaterialTheme.colorScheme.primary
        PaymentHealthLevel.WARNING -> MaterialTheme.colorScheme.secondary
        PaymentHealthLevel.CRITICAL -> MaterialTheme.colorScheme.error
    }
    Text(
        text = health.label,
        color = fg,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}

@Composable
private fun CashierPreviewCard(method: MetodoPagoConfig) {
    val steps = remember(method) { buildCashierPreview(method) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = methodAccent(method).copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Asi se usara en caja",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = methodAccent(method)
            )
            steps.forEach { step ->
                Text(
                    "• $step",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorMetodoPagoSheet(
    form: PaymentFormState,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onFormChange: ((PaymentFormState) -> PaymentFormState) -> Unit,
    onPickQrImage: () -> Unit,
    onSave: () -> Unit
) {
    BoxWithConstraints {
        val isTablet = maxWidth >= 720.dp
        if (isTablet) {
            Dialog(
                onDismissRequest = onDismiss,
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    shape = RoundedCornerShape(30.dp),
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .fillMaxHeight(0.9f)
                ) {
                    EditorMetodoPagoContent(
                        form = form,
                        isSaving = isSaving,
                        onDismiss = onDismiss,
                        onFormChange = onFormChange,
                        onPickQrImage = onPickQrImage,
                        onSave = onSave,
                        isTablet = true
                    )
                }
            }
        } else {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = onDismiss,
                sheetState = sheetState,
                dragHandle = null,
                tonalElevation = 6.dp
            ) {
                EditorMetodoPagoContent(
                    form = form,
                    isSaving = isSaving,
                    onDismiss = onDismiss,
                    onFormChange = onFormChange,
                    onPickQrImage = onPickQrImage,
                    onSave = onSave,
                    isTablet = false
                )
            }
        }
    }
}

@Composable
private fun EditorMetodoPagoContent(
    form: PaymentFormState,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onFormChange: ((PaymentFormState) -> PaymentFormState) -> Unit,
    onPickQrImage: () -> Unit,
    onSave: () -> Unit,
    isTablet: Boolean
) {
    var showManualQrField by remember(form.id, form.qrUrl, form.usaQr) {
        mutableStateOf(form.qrUrl.isNotBlank() && !form.qrUrl.startsWith("content://"))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = if (isTablet) 24.dp else 20.dp,
                vertical = if (isTablet) 22.dp else 18.dp
            ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (!isTablet) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(999.dp))
                    .width(44.dp)
                    .height(5.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (form.id == null) "Nuevo metodo de pago" else "Editar metodo de pago",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (form.id != null) {
                    Text(
                        categoryLabel(form.category),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            StatusBadge(if (form.activo) "Activo" else "Inactivo")
        }

        if (form.id == null) {
            val categories = listOf("efectivo", "transferencia_bancaria", "billetera_digital", "tarjeta", "otro")
            AppDropdown(
                label = "Categoría del pago",
                options = categories,
                selectedOption = form.category,
                onOptionSelected = { cat ->
                    onFormChange { current ->
                        current.copy(
                            category = cat,
                            // Limpiar campos de comportamiento y datos específicos al cambiar categoría
                            permiteVuelto = false,
                            solicitaMontoRecibido = false,
                            calculaVuelto = false,
                            permiteReferencia = false,
                            usaQr = false,
                            banco = "",
                            tipoCuenta = "",
                            numeroCuenta = "",
                            titularBanco = "",
                            documentoBanco = "",
                            telefonoBilletera = "",
                            titularBilletera = "",
                            aliasBilletera = "",
                            qrUrl = "",
                            instrucciones = ""
                        )
                    }
                }
            )
        }

        if (form.warnings.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Sugerencias de configuracion",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                form.warnings.forEach { warning ->
                    Text(
                        "• $warning",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        OutlinedTextField(
            value = form.title,
            onValueChange = { onFormChange { current -> current.copy(title = it, fieldErrors = current.fieldErrors - "title") } },
            label = { Text("Nombre del metodo") },
            modifier = Modifier.fillMaxWidth(),
            isError = form.fieldErrors.containsKey("title"),
            supportingText = form.fieldErrors["title"]?.let { { Text(it) } }
        )
        OutlinedTextField(
            value = form.descripcion,
            onValueChange = { onFormChange { current -> current.copy(descripcion = it) } },
            label = { Text("Descripcion comercial") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        OutlinedTextField(
            value = form.orden,
            onValueChange = { onFormChange { current -> current.copy(orden = it) } },
            label = { Text("Orden visual") },
            modifier = Modifier.fillMaxWidth()
        )

        DetailSectionTitle("Comportamiento base")
        SwitchRow(form.activo, "Metodo activo") { onFormChange { current -> current.copy(activo = it) } }
        SwitchRow(form.disponibleMixto, "Disponible para cobro mixto") { onFormChange { current -> current.copy(disponibleMixto = it) } }

        when (form.category) {
            "efectivo" -> {
                DetailSectionTitle("Cobro en efectivo")
                SwitchRow(form.permiteVuelto, "Permite vuelto") { onFormChange { current -> current.copy(permiteVuelto = it) } }
                SwitchRow(form.solicitaMontoRecibido, "Solicita monto recibido") { onFormChange { current -> current.copy(solicitaMontoRecibido = it) } }
                SwitchRow(form.calculaVuelto, "Calcula vuelto") { onFormChange { current -> current.copy(calculaVuelto = it) } }
            }

            "transferencia_bancaria" -> {
                DetailSectionTitle("Datos bancarios")
                OutlinedTextField(
                    value = form.banco,
                    onValueChange = { onFormChange { current -> current.copy(banco = it, fieldErrors = current.fieldErrors - "banco") } },
                    label = { Text("Banco") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = form.fieldErrors.containsKey("banco"),
                    supportingText = form.fieldErrors["banco"]?.let { { Text(it) } }
                )
                OutlinedTextField(
                    value = form.tipoCuenta,
                    onValueChange = { onFormChange { current -> current.copy(tipoCuenta = it) } },
                    label = { Text("Tipo de cuenta") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = form.numeroCuenta,
                    onValueChange = { onFormChange { current -> current.copy(numeroCuenta = it, fieldErrors = current.fieldErrors - "numeroCuenta") } },
                    label = { Text("Numero de cuenta") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = form.fieldErrors.containsKey("numeroCuenta"),
                    supportingText = form.fieldErrors["numeroCuenta"]?.let { { Text(it) } }
                )
                OutlinedTextField(
                    value = form.titularBanco,
                    onValueChange = { onFormChange { current -> current.copy(titularBanco = it, fieldErrors = current.fieldErrors - "titularBanco") } },
                    label = { Text("Titular") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = form.fieldErrors.containsKey("titularBanco"),
                    supportingText = form.fieldErrors["titularBanco"]?.let { { Text(it) } }
                )
                OutlinedTextField(
                    value = form.documentoBanco,
                    onValueChange = { onFormChange { current -> current.copy(documentoBanco = it) } },
                    label = { Text("Documento del titular") },
                    modifier = Modifier.fillMaxWidth()
                )
                SwitchRow(form.permiteReferencia, "Pedir referencia") { onFormChange { current -> current.copy(permiteReferencia = it) } }
            }

            "billetera_digital" -> {
                DetailSectionTitle("Billetera digital")
                OutlinedTextField(
                    value = form.telefonoBilletera,
                    onValueChange = { onFormChange { current -> current.copy(telefonoBilletera = it, fieldErrors = current.fieldErrors - "telefonoBilletera" - "aliasBilletera") } },
                    label = { Text("Telefono") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = form.fieldErrors.containsKey("telefonoBilletera"),
                    supportingText = form.fieldErrors["telefonoBilletera"]?.let { { Text(it) } }
                )
                OutlinedTextField(
                    value = form.titularBilletera,
                    onValueChange = { onFormChange { current -> current.copy(titularBilletera = it) } },
                    label = { Text("Titular") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = form.aliasBilletera,
                    onValueChange = { onFormChange { current -> current.copy(aliasBilletera = it, fieldErrors = current.fieldErrors - "telefonoBilletera" - "aliasBilletera") } },
                    label = { Text("Alias o identificador") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = form.fieldErrors.containsKey("aliasBilletera"),
                    supportingText = form.fieldErrors["aliasBilletera"]?.let { { Text(it) } }
                )
                SwitchRow(form.usaQr, "Usar QR") { onFormChange { current -> current.copy(usaQr = it, fieldErrors = if (it) current.fieldErrors else current.fieldErrors - "qrUrl") } }
                if (form.usaQr) {
                    DetailSectionTitle("QR del metodo")
                    if (form.fieldErrors.containsKey("qrUrl")) {
                        Text(
                            form.fieldErrors["qrUrl"].orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                        )
                    }
                    if (form.qrUrl.isNotBlank() && form.qrUrl.startsWith("content://")) {
                        QrImagePreview(
                            qrValue = form.qrUrl,
                            onReplace = onPickQrImage,
                            onRemove = {
                                onFormChange { current -> current.copy(qrUrl = "") }
                            }
                        )
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    if (form.qrUrl.isBlank()) "Aun no agregaste la imagen del QR."
                                    else "Tienes un enlace manual configurado para este QR.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilledTonalButton(onClick = onPickQrImage) {
                                        Text(if (form.qrUrl.isBlank()) "Subir imagen QR" else "Cambiar imagen")
                                    }
                                    TextButton(onClick = { showManualQrField = !showManualQrField }) {
                                        Text(if (showManualQrField) "Ocultar enlace" else "Ingresar enlace manual")
                                    }
                                }
                            }
                        }
                    }
                    if (showManualQrField) {
                        OutlinedTextField(
                            value = if (form.qrUrl.startsWith("content://")) "" else form.qrUrl,
                            onValueChange = { onFormChange { current -> current.copy(qrUrl = it) } },
                            label = { Text("Enlace del QR (opcional)") },
                            supportingText = {
                                Text("Usa esta opcion solo si tu QR vive en un enlace y no como imagen.")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }
                SwitchRow(form.permiteReferencia, "Pedir referencia") { onFormChange { current -> current.copy(permiteReferencia = it) } }
            }

            "tarjeta" -> {
                DetailSectionTitle("Cobro con tarjeta")
                SwitchRow(form.permiteReferencia, "Pedir referencia") { onFormChange { current -> current.copy(permiteReferencia = it) } }
            }
        }

        DetailSectionTitle("Apoyo al equipo")
        OutlinedTextField(
            value = form.instrucciones,
            onValueChange = { onFormChange { current -> current.copy(instrucciones = it) } },
            label = { Text("Instrucciones internas") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TextButton(onClick = onDismiss, enabled = !isSaving, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
            Button(onClick = onSave, enabled = !isSaving, modifier = Modifier.weight(1f)) {
                Text(if (isSaving) "Guardando..." else "Guardar")
            }
        }

        if (!isTablet) Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun QrImagePreview(
    qrValue: String,
    onReplace: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                factory = { ctx ->
                    ImageView(ctx).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                },
                update = { imageView ->
                    Glide.with(imageView)
                        .load(qrValue)
                        .into(imageView)
                }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = onReplace) {
                    Text("Cambiar imagen")
                }
                TextButton(onClick = onRemove) {
                    Text("Quitar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = categoryLabel(selectedOption),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(categoryLabel(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SwitchRow(
    checked: Boolean,
    label: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
private fun StatusBadge(text: String) {
    val bg = if (text == "Activo") MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (text == "Activo") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Text(
        text = text,
        color = fg,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}

private fun methodAccent(method: MetodoPagoConfig): Color {
    return when (method.categoria) {
        "efectivo" -> Color(0xFF2E7D32)
        "transferencia_bancaria" -> Color(0xFF1565C0)
        "billetera_digital" -> Color(0xFF7B1FA2)
        "tarjeta" -> Color(0xFFEF6C00)
        else -> Color(0xFF455A64)
    }
}

private fun methodIcon(method: MetodoPagoConfig) = when (method.categoria) {
    "billetera_digital" -> Icons.Default.QrCode2
    else -> Icons.Default.Payments
}

private fun categoryLabel(category: String): String {
    return when (category) {
        "efectivo" -> "Efectivo"
        "transferencia_bancaria" -> "Transferencia bancaria"
        "billetera_digital" -> "Billetera digital"
        "tarjeta" -> "Tarjeta"
        "mixto" -> "Cobro mixto"
        else -> "Otro"
    }
}

private fun buildMethodSummary(method: MetodoPagoConfig): String {
    val parts = mutableListOf<String>()
    if (method.categoria == "efectivo") {
        if (method.permiteVuelto) parts += "permite vuelto"
        if (method.solicitaMontoRecibido) parts += "solicita monto"
        if (method.calculaVuelto) parts += "calcula vuelto"
    }
    val cat = method.categoria
    if (cat == "transferencia_bancaria") {
        if (method.banco.isNotBlank()) parts += method.banco
        if (method.tipoCuenta.isNotBlank()) parts += method.tipoCuenta
        if (method.permiteReferencia) parts += "pide referencia"
    }
    if (cat == "billetera_digital") {
        if (method.telefonoBilletera.isNotBlank()) parts += method.telefonoBilletera
        if (method.titularBilletera.isNotBlank()) parts += method.titularBilletera
        if (method.usaQR) parts += "con QR"
    }
    if (method.categoria == "tarjeta" && method.permiteReferencia) {
        parts += "pide referencia"
    }
    if (parts.isEmpty()) {
        return method.descripcion.ifBlank { "Metodo configurado para cobros." }
    }
    return parts.joinToString(" · ")
}

private enum class PaymentHealthLevel {
    READY,
    WARNING,
    CRITICAL
}

private data class PaymentMethodHealth(
    val level: PaymentHealthLevel,
    val label: String,
    val detail: String
)

private data class PaymentAuditItem(
    val title: String,
    val detail: String,
    val level: PaymentHealthLevel
)

private sealed interface QrUploadTarget {
    data object EditorForm : QrUploadTarget
    data class ExistingMethod(val methodId: String) : QrUploadTarget
}

private fun evaluateMethodHealth(method: MetodoPagoConfig): PaymentMethodHealth {
    if (!method.activo) {
        return PaymentMethodHealth(
            level = PaymentHealthLevel.WARNING,
            label = "En pausa",
            detail = "El metodo esta guardado, pero no aparece como opcion activa en caja."
        )
    }

    val cat = method.categoria
    return when (cat) {
        "efectivo" -> {
            if (!method.solicitaMontoRecibido && method.permiteVuelto) {
                PaymentMethodHealth(
                    level = PaymentHealthLevel.WARNING,
                    label = "Revisar flujo",
                    detail = "Permite vuelto, pero caja no pedira el monto recibido. Conviene completar ese paso."
                )
            } else {
                PaymentMethodHealth(
                    level = PaymentHealthLevel.READY,
                    label = "Listo",
                    detail = "Caja podra usar este metodo de forma directa."
                )
            }
        }

        "transferencia_bancaria" -> {
            val missing = mutableListOf<String>()
            if (method.banco.isBlank()) missing += "banco"
            if (method.numeroCuenta.isBlank()) missing += "cuenta"
            if (method.titularBanco.isBlank()) missing += "titular"
            when {
                missing.isEmpty() -> PaymentMethodHealth(
                    level = PaymentHealthLevel.READY,
                    label = "Listo",
                    detail = "Los datos bancarios principales ya estan completos."
                )
                missing.size == 1 -> PaymentMethodHealth(
                    level = PaymentHealthLevel.WARNING,
                    label = "Falta ${missing.first()}",
                    detail = "Completa ${missing.first()} para que el equipo cobre sin dudas."
                )
                else -> PaymentMethodHealth(
                    level = PaymentHealthLevel.CRITICAL,
                    label = "Incompleto",
                    detail = "Faltan ${missing.joinToString(", ")} para usar bien esta transferencia."
                )
            }
        }

        "billetera_digital" -> {
            val missing = mutableListOf<String>()
            if (method.telefonoBilletera.isBlank() && method.aliasBilletera.isBlank()) {
                missing += "identificador"
            }
            if (method.titularBilletera.isBlank()) missing += "titular"
            if (method.usaQR && method.qrUrl.isBlank()) {
                return PaymentMethodHealth(
                    level = PaymentHealthLevel.WARNING,
                    label = "QR pendiente",
                    detail = "El metodo usara QR, pero todavia no tiene un enlace configurado."
                )
            }
            when {
                missing.isEmpty() -> PaymentMethodHealth(
                    level = PaymentHealthLevel.READY,
                    label = "Listo",
                    detail = "La billetera ya tiene lo necesario para cobrar."
                )
                missing.size == 1 -> PaymentMethodHealth(
                    level = PaymentHealthLevel.WARNING,
                    label = "Falta ${missing.first()}",
                    detail = "Completa ${missing.first()} para que el equipo identifique mejor este cobro."
                )
                else -> PaymentMethodHealth(
                    level = PaymentHealthLevel.CRITICAL,
                    label = "Incompleto",
                    detail = "Faltan datos base para usar esta billetera con confianza."
                )
            }
        }

        "tarjeta" -> PaymentMethodHealth(
            level = PaymentHealthLevel.READY,
            label = "Listo",
            detail = if (method.permiteReferencia) {
                "Caja pedira referencia para conciliar el cobro."
            } else {
                "El metodo esta listo; puedes pedir referencia si tu operacion la necesita."
            }
        )

        else -> PaymentMethodHealth(
            level = PaymentHealthLevel.WARNING,
            label = "Revisar",
            detail = "Conviene revisar este metodo manualmente antes de usarlo masivamente."
        )
    }
}

private fun buildCashierPreview(method: MetodoPagoConfig): List<String> {
    val cat = method.categoria
    return when (cat) {
        "efectivo" -> {
            val items = mutableListOf("Se mostrara como cobro en efectivo.")
            if (method.solicitaMontoRecibido) items += "Caja pedira el monto recibido del cliente."
            if (method.calculaVuelto) items += "El sistema calculara el vuelto automaticamente."
            else if (method.permiteVuelto) items += "El equipo podra registrar vuelto manualmente."
            items
        }

        "transferencia_bancaria" -> {
            val items = mutableListOf("Caja mostrara los datos bancarios para compartirlos con el cliente.")
            if (method.permiteReferencia) items += "Despues del pago, pedira numero de referencia."
            if (method.banco.isNotBlank()) items += "Usara ${method.banco} como banco visible principal."
            items
        }

        "billetera_digital" -> {
            val items = mutableListOf("Caja mostrara el metodo como billetera o QR.")
            if (method.usaQR) {
                items += if (method.qrUrl.isNotBlank()) {
                    "Tendra QR listo para escanear."
                } else {
                    "Quedara marcado para agregar QR despues."
                }
            }
            if (method.permiteReferencia) items += "Tambien podra pedir referencia del pago."
            if (method.telefonoBilletera.isNotBlank()) items += "Usara ${method.telefonoBilletera} como dato visible principal."
            items
        }

        "tarjeta" -> {
            val items = mutableListOf("Caja lo ofrecera como cobro con tarjeta.")
            if (method.permiteReferencia) items += "Pedira referencia o voucher para cerrar la venta."
            if (method.disponibleMixto) items += "Tambien se podra combinar con otros metodos."
            items
        }

        else -> listOf("Caja usara este metodo como una opcion manual configurada por la tienda.")
    }
}

private fun buildPaymentAudit(
    country: String,
    methods: List<MetodoPagoConfig>,
    suggestions: List<PaymentMethodSuggestion>
): List<PaymentAuditItem> {
    val activeMethods = methods.filter { it.activo }
    val incompleteMethods = activeMethods
        .map { it to evaluateMethodHealth(it) }
        .filter { (_, health) -> health.level != PaymentHealthLevel.READY }

    val items = mutableListOf<PaymentAuditItem>()

    if (activeMethods.none { it.categoria == "efectivo" }) {
        items += PaymentAuditItem(
            title = "Te falta un respaldo en efectivo",
            detail = "Conviene mantener al menos un metodo en efectivo activo por si falla un canal digital.",
            level = PaymentHealthLevel.CRITICAL
        )
    }

    incompleteMethods.take(2).forEach { (method, health) ->
        items += PaymentAuditItem(
            title = "${method.titulo}: ${health.label}",
            detail = health.detail,
            level = health.level
        )
    }

    if (suggestions.isNotEmpty()) {
        val suggestionTitles = suggestions.take(2).joinToString(", ") { it.title }
        items += PaymentAuditItem(
            title = "Todavia puedes completar tu mix para $country",
            detail = "Si quieres ampliar opciones al cliente, empieza por $suggestionTitles.",
            level = PaymentHealthLevel.WARNING
        )
    }

    if (items.isEmpty()) {
        items += PaymentAuditItem(
            title = "Tus cobros se ven consistentes",
            detail = "Los metodos activos ya tienen una configuracion sana para el trabajo diario de caja.",
            level = PaymentHealthLevel.READY
        )
    }

    return items.take(3)
}
