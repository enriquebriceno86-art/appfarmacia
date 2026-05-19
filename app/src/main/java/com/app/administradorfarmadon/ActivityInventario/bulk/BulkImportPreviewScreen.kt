package com.app.administradorfarmadon.ActivityInventario.bulk

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkImportPreviewScreen(
    viewModel: BulkImportViewModel,
    onSelectFile: () -> Unit,
    onBackPressed: () -> Unit
) {
    val uiState = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importación Masiva", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            if (uiState.currentStep == BulkImportStep.VALIDATION_ROOM) {
                ImportBottomBar(
                    summary = uiState.summary,
                    onStartImport = { viewModel.executeBulkImport() },
                    isLoading = uiState.isLoading
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF8F9FA))) {
            when (uiState.currentStep) {
                BulkImportStep.SELECT_FILE -> SelectFileStep(onSelectFile)
                BulkImportStep.VALIDATION_ROOM -> ValidationRoomStep(uiState, viewModel)
                BulkImportStep.PROCESSING -> ProcessingStep()
                BulkImportStep.SUCCESS_SUMMARY -> SuccessSummaryStep(uiState.summary, onBackPressed)
            }
            
            if (uiState.isLoading && uiState.currentStep != BulkImportStep.PROCESSING) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun SelectFileStep(onSelectFile: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.UploadFile,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFF2E5BFF)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Sube tu factura o inventario", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            "Formatos aceptados: .csv (separado por comas o ;)",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onSelectFile,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5BFF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(50.dp).width(200.dp)
        ) {
            Text("Seleccionar archivo", color = Color.White)
        }
    }
}

@Composable
fun ValidationRoomStep(uiState: BulkImportUiState, viewModel: BulkImportViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Filtros
        FilterBar(uiState.filterState) { viewModel.filterByState(it) }
        
        // Lista de productos
        val filteredDrafts = when (uiState.filterState) {
            null -> uiState.drafts
            else -> uiState.drafts.filter { it.validationState == uiState.filterState }
        }

        if (filteredDrafts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay productos que coincidan con el filtro", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredDrafts, key = { it.id }) { draft ->
                    DraftProductCard(draft) { viewModel.toggleDraftSelection(draft.id) }
                }
            }
        }
    }
}

@Composable
fun DraftProductCard(draft: ImportDraftProduct, onToggle: () -> Unit) {
    val borderColor = when (draft.validationState) {
        ImportValidationState.READY -> Color(0xFF4CAF50)
        ImportValidationState.WARNING -> Color(0xFFFF9800)
        ImportValidationState.ERROR -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (draft.isSelected) Color.White else Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = draft.isSelected, onCheckedChange = { onToggle() })
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = draft.name.ifBlank { "Sin nombre" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Stock: ${draft.initialStock} | Costo: $${draft.purchaseCost}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (draft.errors.isNotEmpty() || draft.warnings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val message = if (draft.errors.isNotEmpty()) draft.errors.first() else draft.warnings.first()
                    Text(text = "⚠ $message", color = borderColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
            
            // Indicador de estado
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(borderColor, CircleShape)
            )
        }
    }
}

@Composable
fun FilterBar(selected: ImportValidationState?, onFilter: (ImportValidationState?) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp).horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onFilter(null) },
            label = { Text("Todos") }
        )
        FilterChip(
            selected = selected == ImportValidationState.READY,
            onClick = { onFilter(ImportValidationState.READY) },
            label = { Text("Listos") }
        )
        FilterChip(
            selected = selected == ImportValidationState.WARNING,
            onClick = { onFilter(ImportValidationState.WARNING) },
            label = { Text("Advertencias") }
        )
        FilterChip(
            selected = selected == ImportValidationState.ERROR,
            onClick = { onFilter(ImportValidationState.ERROR) },
            label = { Text("Errores") }
        )
    }
}

@Composable
fun ImportBottomBar(summary: BulkImportSummary, onStartImport: () -> Unit, isLoading: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Listos: ${summary.readyToImport}", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                Text("Total: ${summary.totalRows}", fontSize = 12.sp, color = Color.Gray)
            }
            
            Button(
                onClick = onStartImport,
                enabled = summary.readyToImport > 0 && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5BFF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Guardar Productos", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ProcessingStep() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp), color = Color(0xFF2E5BFF))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Guardando en la base de datos...", fontWeight = FontWeight.Bold)
        Text("Esto puede tardar unos segundos", color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
fun SuccessSummaryStep(summary: BulkImportSummary, onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFF4CAF50)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("¡Importación Exitosa!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Productos importados:")
            Text("${summary.importedCount}", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5BFF))
        ) {
            Text("Finalizar", color = Color.White)
        }
    }
}

