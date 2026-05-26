package com.app.administradorfarmadon.ActivityInventario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OtherDetailsUiModel(
    val productName: String,
    val managesLots: Boolean,
    val summaryLabel: String,
    val summaryValue: String,
    val summaryStatus: String,
    val summaryHelper: String,
    val lotsCount: Int,
    val expiryInput: String,
    val expiryHelper: String
)

data class LotListUiItem(
    val key: String,
    val number: String,
    val status: String,
    val expiry: String,
    val availableText: String,
    val ingresoDate: String,
    val secondaryText: String
)

data class LotDetailUiModel(
    val key: String,
    val number: String,
    val status: String,
    val expiry: String,
    val ingresoDate: String,
    val cantidadIngresada: String,
    val cantidadDisponible: String,
    val unidadBase: String,
    val costoUnitario: String,
    val observaciones: String
)

data class LotFormUiModel(
    val title: String,
    val number: String,
    val expiry: String,
    val cantidadIngresada: String,
    val cantidadDisponible: String,
    val costoUnitario: String,
    val observaciones: String,
    val existingLotNumbers: Set<String> = emptySet()
)

data class LotFormSubmit(
    val number: String,
    val expiry: String,
    val cantidadIngresada: String,
    val cantidadDisponible: String,
    val costoUnitario: String,
    val observaciones: String
)

data class ConsumeLotUiModel(
    val number: String,
    val expiry: String,
    val availableText: String,
    val unidadBase: String,
    val motivo: String = ""
)

data class ConsumeLotSubmit(
    val cantidad: String,
    val motivo: String
)

data class OtherDetailsSubmit(
    val expiryInput: String
)

@Composable
fun OtherDetailsSheet(
    model: OtherDetailsUiModel,
    backIcon: Painter,
    onBack: () -> Unit,
    onManageLots: () -> Unit,
    onClose: () -> Unit,
    onSave: (OtherDetailsSubmit) -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
    stateKey: Int = 0
) {
    var expiryInput by rememberSaveable(stateKey, model.expiryInput) { mutableStateOf(model.expiryInput) }
    var isSaving by rememberSaveable(stateKey) { mutableStateOf(false) }
    val hasChanges = !model.managesLots && expiryInput != model.expiryInput

    LaunchedEffect(hasChanges) {
        if (!hasChanges) isSaving = false
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        SheetHeader(
            title = "Otros detalles",
            subtitle = "Gestiona vencimiento y lotes del producto",
            backIcon = backIcon,
            onBack = onBack,
            showBackButton = showBackButton
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (model.managesLots) {
            SummaryCard(
                label = model.summaryLabel,
                value = model.summaryValue,
                status = model.summaryStatus,
                helper = model.summaryHelper
            )

            Spacer(modifier = Modifier.height(12.dp))

            ActionRowCard(
                title = "Lotes del producto",
                subtitle = "${model.lotsCount} lote${if (model.lotsCount == 1) "" else "s"} registrado${if (model.lotsCount == 1) "" else "s"}",
                helper = "Ver y gestionar lotes",
                onClick = onManageLots
            )
        } else {
            ExpiryInputCard(
                value = expiryInput,
                helper = model.expiryHelper,
                onValueChange = { newValue ->
                    expiryInput = filtrarVencimiento(newValue)
                }
            )
        }

        Spacer(modifier = Modifier.height(22.dp))
        FooterButtons(
            secondaryText = "Cerrar",
            primaryText = if (isSaving) "Guardando cambios..." else "Guardar",
            primaryEnabled = hasChanges && !isSaving,
            onSecondary = onClose,
            onPrimary = {
                isSaving = true
                onSave(OtherDetailsSubmit(expiryInput = expiryInput))
            }
        )
    }
}

@Composable
fun LotesListSheet(
    productName: String,
    unitBase: String,
    totalAvailable: String,
    searchQuery: String,
    items: List<LotListUiItem>,
    backIcon: Painter,
    onBack: () -> Unit,
    onSearchChange: (String) -> Unit,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(painter = backIcon, contentDescription = "Volver", tint = Color(0xFF111827))
            }
            Text(
                text = "Lotes del producto",
                modifier = Modifier.weight(1f),
                color = Color(0xFF111827),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        HorizontalDivider(color = Color(0xFFE9EDF2))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            ProductMiniHeader(
                name = productName,
                subtitle = "Unidad base: $unitBase",
                helper = "Total disponible: $totalAvailable"
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar lote", color = Color(0xFF98A2B3)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color(0xFFD7DDE5),
                    unfocusedIndicatorColor = Color(0xFFD7DDE5),
                    cursorColor = Color(0xFF16A34A)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            items.forEach { item ->
                LotRowCard(item = item, onClick = { onItemClick(item.key) })
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (items.isEmpty()) {
                EmptyState(
                    title = "Sin lotes registrados",
                    subtitle = "Crea el lote desde Ingresar stock para registrar cantidad y vencimiento."
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            InfoNote(text = "Los lotes se consumen automáticamente por FEFO (primero en vencer).")
        }
    }
}

@Composable
fun LoteDetailSheet(
    model: LotDetailUiModel,
    backIcon: Painter,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onManualConsume: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(painter = backIcon, contentDescription = "Volver", tint = Color(0xFF111827))
            }
            Text(
                text = "Detalle del lote",
                modifier = Modifier.weight(1f),
                color = Color(0xFF111827),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Editar",
                modifier = Modifier.clickable(onClick = onEdit),
                color = Color(0xFF16A34A),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatusBadge(status = model.status)
            Text(
                text = model.number,
                color = Color(0xFF111827),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        DetailRow("Fecha de vencimiento", model.expiry)
        DetailRow("Fecha de ingreso", model.ingresoDate)
        DetailRow("Cantidad ingresada", model.cantidadIngresada)
        DetailRow("Cantidad disponible", model.cantidadDisponible)
        DetailRow("Unidad base", model.unidadBase)
        DetailRow("Costo unitario", model.costoUnitario)
        DetailRow("Observaciones", model.observaciones)

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF6FAF7), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Información",
                    color = Color(0xFF147A4A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Este lote mantiene el flujo FEFO y el stock vendible del producto.",
                    color = Color(0xFF667085),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = onManualConsume,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB7DFC7)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF16A34A))
        ) {
            Text("Registrar consumo manual", fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun LoteFormSheet(
    model: LotFormUiModel,
    backIcon: Painter,
    onBack: () -> Unit,
    onSave: (LotFormSubmit) -> Boolean,
    modifier: Modifier = Modifier
) {
    var number by rememberSaveable(model.number) { mutableStateOf(model.number) }
    var expiry by rememberSaveable(model.expiry) { mutableStateOf(model.expiry) }
    var cantidadIngresada by rememberSaveable(model.cantidadIngresada) { mutableStateOf(model.cantidadIngresada) }
    var cantidadDisponible by rememberSaveable(model.cantidadDisponible) { mutableStateOf(model.cantidadDisponible) }
    var costoUnitario by rememberSaveable(model.costoUnitario) { mutableStateOf(model.costoUnitario) }
    var observaciones by rememberSaveable(model.observaciones) { mutableStateOf(model.observaciones) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    val normalizedNumber = number.trim().uppercase()
    val lotAlreadyExists = normalizedNumber.isNotBlank() && normalizedNumber in model.existingLotNumbers
    val hasChanges =
        number != model.number ||
            expiry != model.expiry ||
            cantidadIngresada != model.cantidadIngresada ||
            cantidadDisponible != model.cantidadDisponible ||
            costoUnitario != model.costoUnitario ||
            observaciones != model.observaciones

    LaunchedEffect(hasChanges) {
        if (!hasChanges) isSaving = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        SimpleTopBar(title = model.title, backIcon = backIcon, onBack = onBack)
        Spacer(modifier = Modifier.height(18.dp))

        FieldLabel("Número de lote")
        SimpleInput(
            value = number,
            placeholder = "Ej. L005",
            isError = lotAlreadyExists,
            supportingText = if (lotAlreadyExists) "Ese lote ya existe. Crea uno diferente." else null
        ) { value ->
            number = value.uppercase()
        }
        Spacer(modifier = Modifier.height(14.dp))

        FieldLabel("Fecha de vencimiento")
        SimpleInput(expiry, "MM/AA", keyboardType = KeyboardType.Number) { value ->
            expiry = filtrarVencimiento(value)
        }
        Spacer(modifier = Modifier.height(14.dp))

        FieldLabel("Cantidad ingresada")
        SimpleInput(cantidadIngresada, "Ej. 100", keyboardType = KeyboardType.Number) { value ->
            cantidadIngresada = value.filter(Char::isDigit)
        }
        Spacer(modifier = Modifier.height(14.dp))

        FieldLabel("Cantidad disponible")
        SimpleInput(cantidadDisponible, "Ej. 100", keyboardType = KeyboardType.Number) { value ->
            cantidadDisponible = value.filter(Char::isDigit)
        }
        Spacer(modifier = Modifier.height(14.dp))

        FieldLabel("Costo unitario (opcional)")
        SimpleInput(costoUnitario, "Ej. 0.00", keyboardType = KeyboardType.Decimal) { value ->
            costoUnitario = value.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' }
        }
        Spacer(modifier = Modifier.height(14.dp))

        FieldLabel("Observaciones (opcional)")
        OutlinedTextField(
            value = observaciones,
            onValueChange = { observaciones = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color(0xFFD7DDE5),
                unfocusedIndicatorColor = Color(0xFFD7DDE5),
                cursorColor = Color(0xFF16A34A)
            )
        )

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = {
                val started = onSave(
                    LotFormSubmit(
                        number = number,
                        expiry = expiry,
                        cantidadIngresada = cantidadIngresada,
                        cantidadDisponible = cantidadDisponible,
                        costoUnitario = costoUnitario,
                        observaciones = observaciones
                    )
                )
                isSaving = started
            },
            enabled = hasChanges && !isSaving && !lotAlreadyExists,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF16A34A),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFD0D5DD),
                disabledContentColor = Color.White
            )
        ) {
            Text(
                if (isSaving) "Guardando..." else "Guardar lote",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ConsumeLotSheet(
    model: ConsumeLotUiModel,
    backIcon: Painter,
    onBack: () -> Unit,
    onSave: (ConsumeLotSubmit) -> Boolean,
    modifier: Modifier = Modifier
) {
    var cantidad by rememberSaveable { mutableStateOf("") }
    var motivo by rememberSaveable(model.motivo) { mutableStateOf(model.motivo) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    val hasChanges = cantidad.isNotBlank() || motivo != model.motivo

    LaunchedEffect(hasChanges) {
        if (!hasChanges) isSaving = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        SimpleTopBar(title = "Registrar consumo", backIcon = backIcon, onBack = onBack)
        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE5EAF0), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Lote seleccionado: ${model.number}",
                    color = Color(0xFF111827),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Vence: ${model.expiry} · Disponibles: ${model.availableText}",
                    color = Color(0xFF667085),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FieldLabel("Cantidad a consumir")
        SimpleInput(cantidad, "Ej. 10", keyboardType = KeyboardType.Number) { value ->
            cantidad = value.filter(Char::isDigit)
        }
        Spacer(modifier = Modifier.height(14.dp))

        FieldLabel("Motivo (opcional)")
        OutlinedTextField(
            value = motivo,
            onValueChange = { motivo = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color(0xFFD7DDE5),
                unfocusedIndicatorColor = Color(0xFFD7DDE5),
                cursorColor = Color(0xFF16A34A)
            )
        )

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = {
                val started = onSave(ConsumeLotSubmit(cantidad = cantidad, motivo = motivo))
                isSaving = started
            },
            enabled = hasChanges && !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF16A34A),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFD0D5DD),
                disabledContentColor = Color.White
            )
        ) {
            Text(
                if (isSaving) "Guardando..." else "Registrar consumo",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SheetHeader(
    title: String,
    subtitle: String,
    backIcon: Painter,
    onBack: () -> Unit,
    showBackButton: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showBackButton) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.width(40.dp).height(40.dp)
                ) {
                    Icon(painter = backIcon, contentDescription = "Volver", tint = Color(0xFF111827))
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = Color(0xFF111827),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = subtitle,
            color = Color(0xFF667085),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SimpleTopBar(
    title: String,
    backIcon: Painter,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.width(40.dp).height(40.dp)
        ) {
            Icon(painter = backIcon, contentDescription = "Volver", tint = Color(0xFF111827))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = Color(0xFF111827),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    status: String,
    helper: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFFE6EBF1), RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(label, color = Color(0xFF475467), fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value.ifBlank { "Sin fecha" },
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF111827),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
                StatusBadge(status = status)
            }
            Text(helper, color = Color(0xFF98A2B3), fontSize = 13.sp)
        }
    }
}

@Composable
private fun ExpiryInputCard(
    value: String,
    helper: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Fecha de vencimiento",
            color = Color(0xFF111827),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        SimpleInput(value, "MM/AA", keyboardType = KeyboardType.Number, onValueChange = onValueChange)
        Text(helper, color = Color(0xFF98A2B3), fontSize = 13.sp)
    }
}

@Composable
private fun ActionRowCard(
    title: String,
    subtitle: String,
    helper: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFFD6E8DC), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = Color(0xFF111827), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color(0xFF667085), fontSize = 13.sp)
            Text(helper, color = Color(0xFF16A34A), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        Text(">", color = Color(0xFF98A2B3), fontSize = 22.sp)
    }
}

@Composable
private fun ProductMiniHeader(
    name: String,
    subtitle: String,
    helper: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFEFF8F2), CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(name, color = Color(0xFF111827), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color(0xFF667085), fontSize = 13.sp)
            Text(helper, color = Color(0xFF667085), fontSize = 13.sp)
        }
    }
}

@Composable
private fun LotRowCard(
    item: LotListUiItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE6EBF1), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = item.number,
                    color = Color(0xFF111827),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                StatusBadge(status = item.status)
            }
            Text(item.secondaryText, color = Color(0xFF667085), fontSize = 13.sp)
            Text("Ingreso: ${item.ingresoDate}", color = Color(0xFF98A2B3), fontSize = 12.sp)
        }
        Text(">", color = Color(0xFF98A2B3), fontSize = 22.sp)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color(0xFF667085), fontSize = 14.sp)
            Text(
                text = value.ifBlank { "Sin registro" },
                color = Color(0xFF111827),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        HorizontalDivider(color = Color(0xFFE9EDF2))
    }
}

@Composable
private fun EmptyState(
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = Color(0xFF111827), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color(0xFF667085), fontSize = 13.sp)
        }
    }
}

@Composable
private fun InfoNote(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text("i", color = Color(0xFF98A2B3), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color(0xFF98A2B3), fontSize = 12.sp)
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text = text, color = Color(0xFF344054), fontSize = 13.sp, fontWeight = FontWeight.Medium)
}

@Composable
private fun SimpleInput(
    value: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    supportingText: String? = null,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color(0xFF98A2B3)) },
        supportingText = supportingText?.let { text -> { Text(text, color = Color(0xFFDC2626), fontSize = 12.sp) } },
        singleLine = true,
        isError = isError,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = Color(0xFF111827),
            fontSize = 15.sp
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = if (isError) Color(0xFFDC2626) else Color(0xFFD7DDE5),
            unfocusedIndicatorColor = if (isError) Color(0xFFDC2626) else Color(0xFFD7DDE5),
            cursorColor = Color(0xFF16A34A)
        )
    )
}

@Composable
private fun FooterButtons(
    secondaryText: String,
    primaryText: String,
    primaryEnabled: Boolean,
    onSecondary: () -> Unit,
    onPrimary: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onSecondary,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCAE8D8)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF16A34A))
        ) {
            Text(secondaryText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }

        Button(
            onClick = onPrimary,
            enabled = primaryEnabled,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF16A34A),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFD0D5DD),
                disabledContentColor = Color.White
            )
        ) {
            Text(primaryText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (background, foreground) = when (status.uppercase()) {
        "VENCIDO" -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
        "POR VENCER" -> Color(0xFFFFF3E0) to Color(0xFFEA580C)
        "SIN STOCK" -> Color(0xFFF2F4F7) to Color(0xFF667085)
        else -> Color(0xFFEAF7EE) to Color(0xFF15803D)
    }
    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = status,
            color = foreground,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun filtrarVencimiento(input: String): String {
    val digits = input.filter(Char::isDigit).take(4)
    return when {
        digits.length <= 2 -> digits
        else -> digits.take(2) + "/" + digits.drop(2)
    }
}
