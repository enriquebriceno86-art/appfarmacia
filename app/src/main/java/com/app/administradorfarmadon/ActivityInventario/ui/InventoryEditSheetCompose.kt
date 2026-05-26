package com.app.administradorfarmadon.ActivityInventario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class InventoryEditorUiModel(
    val unitBase: String,
    val stockActual: String,
    val stockMinActual: String,
    val stockMinInput: String,
    val stockMinSuffix: String,
    val helperText: String
)

data class InventoryEditSubmit(
    val stockMinInput: String
)

@Composable
fun InventoryEditSheet(
    model: InventoryEditorUiModel,
    inventoryIcon: Painter,
    backIcon: Painter,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onSave: (InventoryEditSubmit) -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
    stateKey: Int = 0
) {
    var stockMinInput by rememberSaveable(stateKey, model.stockMinInput) { mutableStateOf(model.stockMinInput) }
    var isSaving by rememberSaveable(stateKey) { mutableStateOf(false) }
    val hasChanges = stockMinInput != model.stockMinInput

    LaunchedEffect(hasChanges) {
        if (!hasChanges) isSaving = false
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Header(
            title = "Editar inventario",
            backIcon = backIcon,
            onBack = onBack,
            showBackButton = showBackButton
        )

        Spacer(modifier = Modifier.height(18.dp))

        InfoSection(
            icon = inventoryIcon,
            unitBase = model.unitBase,
            stockActual = model.stockActual,
            stockMinActual = model.stockMinActual
        )

        Spacer(modifier = Modifier.height(20.dp))
        Divider(color = Color(0xFFEAECEF), thickness = 1.dp)
        Spacer(modifier = Modifier.height(20.dp))

        StockInput(
            value = stockMinInput,
            suffix = model.stockMinSuffix,
            helperText = model.helperText,
            onValueChange = { value ->
                stockMinInput = value.filter { it.isDigit() }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))
        Divider(color = Color(0xFFEAECEF), thickness = 1.dp)
        Spacer(modifier = Modifier.height(22.dp))

        FooterButtons(
            saveEnabled = hasChanges && !isSaving,
            saveText = if (isSaving) "Guardando cambios..." else "Guardar",
            onClose = onClose,
            onSave = {
                isSaving = true
                onSave(InventoryEditSubmit(stockMinInput = stockMinInput))
            }
        )
    }
}

@Composable
fun Header(
    title: String,
    backIcon: Painter,
    onBack: () -> Unit,
    showBackButton: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBackButton) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.width(40.dp).height(40.dp)
            ) {
                Icon(
                    painter = backIcon,
                    contentDescription = "Volver",
                    tint = Color(0xFF111827)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = title,
            color = Color(0xFF0F172A),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun InfoSection(
    icon: Painter,
    unitBase: String,
    stockActual: String,
    stockMinActual: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFEFF8F2), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = Color(0xFF14935C),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Unidad base",
                color = Color(0xFF475467),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = unitBase,
                color = Color(0xFF0F172A),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Stock: $stockActual · Mín: $stockMinActual",
                color = Color(0xFF667085),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun StockInput(
    value: String,
    suffix: String,
    helperText: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Stock mínimo",
            color = Color(0xFF0F172A),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFF111827),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color(0xFFD7DDE5),
                    unfocusedIndicatorColor = Color(0xFFD7DDE5),
                    cursorColor = Color(0xFF14935C)
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = suffix,
                color = Color(0xFF667085),
                fontSize = 18.sp
            )
        }

        Text(
            text = helperText,
            color = Color(0xFF98A2B3),
            fontSize = 13.sp
        )
    }
}

@Composable
fun FooterButtons(
    saveEnabled: Boolean,
    saveText: String,
    onClose: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onClose,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 54.dp),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCAE8D8)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF14935C)
            )
        ) {
            Text(text = "Cerrar", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Button(
            onClick = onSave,
            enabled = saveEnabled,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF14935C),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFD0D5DD),
                disabledContentColor = Color.White
            )
        ) {
            Text(text = saveText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}
