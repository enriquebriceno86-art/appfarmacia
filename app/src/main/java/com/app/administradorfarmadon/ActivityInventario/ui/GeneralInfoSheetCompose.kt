package com.app.administradorfarmadon.ActivityInventario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class GeneralInfoEditorUiModel(
    val productName: String,
    val category: String,
    val categoryOptions: List<String>,
    val requiresPrescription: Boolean,
    val isActive: Boolean,
    val nameHelper: String? = null,
    val categoryHelper: String? = null
)

data class GeneralInfoSubmit(
    val productName: String,
    val category: String,
    val requiresPrescription: Boolean,
    val isActive: Boolean
)

@Composable
fun GeneralInfoEditSheet(
    model: GeneralInfoEditorUiModel,
    backIcon: Painter,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onSave: (GeneralInfoSubmit) -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
    stateKey: Int = 0
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var productName by rememberSaveable(stateKey, model.productName) { mutableStateOf(model.productName) }
    var category by rememberSaveable(stateKey, model.category) { mutableStateOf(model.category) }
    var requiresPrescription by rememberSaveable(stateKey, model.requiresPrescription) { mutableStateOf(model.requiresPrescription) }
    var isActive by rememberSaveable(stateKey, model.isActive) { mutableStateOf(model.isActive) }
    var isSaving by rememberSaveable(stateKey) { mutableStateOf(false) }
    var categoryExpanded by rememberSaveable(stateKey) { mutableStateOf(false) }

    val hasChanges = productName != model.productName ||
        category != model.category ||
        requiresPrescription != model.requiresPrescription ||
        isActive != model.isActive

    val filteredCategories = model.categoryOptions
        .filter { option ->
            category.isBlank() || option.contains(category, ignoreCase = true)
        }
        .distinct()
        .take(8)

    LaunchedEffect(hasChanges) {
        if (!hasChanges) isSaving = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                    categoryExpanded = false
                }
            }
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = true)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            GeneralSheetHeader(
                title = "Información general",
                subtitle = "Edita nombre, categoría y estado del producto",
                backIcon = backIcon,
                onBack = onBack,
                showBackButton = showBackButton
            )

            MinimalTextField(
                label = "Nombre del producto",
                value = productName,
                helperText = model.nameHelper,
                onValueChange = { value ->
                    productName = value
                }
            )

            HorizontalDivider(color = Color(0xFFEAECEF))

            CategoryField(
                label = "Categoría",
                value = category,
                options = filteredCategories,
                helperText = model.categoryHelper ?: "Puedes escribir una nueva o elegir una existente",
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it },
                onValueChange = { value ->
                    category = value
                    if (categoryExpanded && value.isBlank()) {
                        categoryExpanded = false
                    }
                },
                onOptionSelected = { value ->
                    category = value
                    categoryExpanded = false
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                }
            )

            HorizontalDivider(color = Color(0xFFEAECEF))

            ToggleLine(
                title = "Requiere receta médica",
                helper = "Al vender, se solicitará la receta",
                checked = requiresPrescription,
                onCheckedChange = {
                    requiresPrescription = it
                }
            )

            HorizontalDivider(color = Color(0xFFEAECEF))

            ToggleLine(
                title = "Producto activo",
                helper = "Visible y disponible en caja",
                checked = isActive,
                onCheckedChange = {
                    isActive = it
                }
            )

            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        GeneralFooterButtons(
            secondaryText = "Cerrar",
            primaryText = if (isSaving) "Guardando cambios..." else "Listo",
            primaryEnabled = hasChanges && !isSaving,
            onSecondary = onClose,
            onPrimary = {
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
                isSaving = true
                onSave(
                    GeneralInfoSubmit(
                        productName = productName,
                        category = category,
                        requiresPrescription = requiresPrescription,
                        isActive = isActive
                    )
                )
            }
        )
    }
}

@Composable
private fun GeneralSheetHeader(
    title: String,
    subtitle: String,
    backIcon: Painter,
    onBack: () -> Unit,
    showBackButton: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

        Text(
            text = subtitle,
            color = Color(0xFF667085),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun MinimalTextField(
    label: String,
    value: String,
    helperText: String?,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = label,
            color = Color(0xFF344054),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = Color(0xFF101828),
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            ),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color(0xFFD7DDE5),
                unfocusedIndicatorColor = Color(0xFFD7DDE5),
                cursorColor = Color(0xFF14935C)
            )
        )
        helperText?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                color = Color(0xFF667085),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun CategoryField(
    label: String,
    value: String,
    options: List<String>,
    helperText: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onValueChange: (String) -> Unit,
    onOptionSelected: (String) -> Unit
) {
    val focusRequester = FocusRequester()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = label,
            color = Color(0xFF344054),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFF101828),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                trailingIcon = {
                    IconButton(onClick = { onExpandedChange(!expanded) }) {
                        Icon(
                            painter = backIconPlaceholder(),
                            contentDescription = "Mostrar categorías",
                            tint = Color(0xFF667085)
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color(0xFFD7DDE5),
                    unfocusedIndicatorColor = Color(0xFFD7DDE5),
                    cursorColor = Color(0xFF14935C)
                )
            )

            DropdownMenu(
                expanded = expanded && options.isNotEmpty(),
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .background(Color.White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                color = Color(0xFF111827),
                                fontSize = 15.sp
                            )
                        },
                        onClick = { onOptionSelected(option) }
                    )
                }
            }
        }

        Text(
            text = helperText,
            color = Color(0xFF667085),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun GeneralFooterButtons(
    secondaryText: String,
    primaryText: String,
    primaryEnabled: Boolean,
    onSecondary: () -> Unit,
    onPrimary: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        androidx.compose.material3.OutlinedButton(
            onClick = onSecondary,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(20.dp),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF14935C)
            )
        ) {
            Text(secondaryText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }

        androidx.compose.material3.Button(
            onClick = onPrimary,
            enabled = primaryEnabled,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(20.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = Color(0xFF14935C),
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
private fun ToggleLine(
    title: String,
    helper: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF101828),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = helper,
                color = Color(0xFF667085),
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF14935C),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD0D5DD),
                uncheckedBorderColor = Color(0xFFD0D5DD)
            )
        )
    }
}

@Composable
private fun backIconPlaceholder(): Painter {
    return androidx.compose.ui.res.painterResource(
        id = com.app.administradorfarmadon.R.drawable.ic_arrow_drop_down
    )
}
