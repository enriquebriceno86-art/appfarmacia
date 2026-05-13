package com.app.administradorfarmadon.ActivityInventario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PresentacionesEditorUiModel(
    val productName: String,
    val productSubtitle: String,
    val principal: PresentacionUiItem?,
    val items: List<PresentacionUiItem>,
    val showPrincipalSelector: Boolean,
    val note: String,
    val saveEnabled: Boolean
)

data class PresentacionUiItem(
    val id: String,
    val name: String,
    val detail: String,
    val price: String,
    val iconRes: Int,
    val isPrincipal: Boolean,
    val principalDetail: String? = null
)

data class PresentacionesSubmit(
    val principalId: String?
)

@Composable
fun PresentacionesEditSheet(
    model: PresentacionesEditorUiModel,
    backIcon: Painter,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    onClose: () -> Unit,
    onSave: (PresentacionesSubmit) -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
    stateKey: Int = 0
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSaving by rememberSaveable(stateKey) { mutableStateOf(false) }
    var selectedPrincipalId by rememberSaveable(stateKey, model.items) {
        mutableStateOf(model.items.firstOrNull { it.isPrincipal }?.id)
    }
    val hasChanges = selectedPrincipalId != model.items.firstOrNull { it.isPrincipal }?.id

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
                }
            }
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PresentacionesHeader(
                title = "Presentaciones",
                backIcon = backIcon,
                onBack = onBack,
                showBackButton = showBackButton
            )

            ProductSummary(
                name = model.productName,
                subtitle = model.productSubtitle
            )

            HorizontalDivider(color = Color(0xFFE9EDF2))

            if (model.showPrincipalSelector) {
                SectionTitle("Principal de venta")
                model.principal?.let {
                    PrincipalRow(item = it)
                }
                HorizontalDivider(color = Color(0xFFE9EDF2))
            }

            SectionTitle("Presentaciones activas")

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                model.items.forEachIndexed { index, item ->
                    PresentacionRow(
                        item = item.copy(isPrincipal = item.id == selectedPrincipalId),
                        showSelector = model.showPrincipalSelector,
                        onEdit = { onEdit(item.id) },
                        onDelete = { onDelete(item.id) },
                        onSelectPrincipal = { selectedPrincipalId = item.id }
                    )
                    if (index != model.items.lastIndex) {
                        HorizontalDivider(color = Color(0xFFE9EDF2))
                    }
                }
            }

            AddButton(onClick = onAdd)
            NoteRow(text = model.note)
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color(0xFFE9EDF2))
        Spacer(modifier = Modifier.height(12.dp))
        PresentacionesFooterButtons(
            saveEnabled = hasChanges && !isSaving,
            saveText = if (isSaving) "Guardando cambios..." else "Guardar",
            onClose = onClose,
            onSave = {
                isSaving = true
                onSave(PresentacionesSubmit(principalId = selectedPrincipalId))
            }
        )
    }
}

@Composable
private fun PresentacionesHeader(
    title: String,
    backIcon: Painter,
    onBack: () -> Unit,
    showBackButton: Boolean
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        if (showBackButton) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(40.dp)
                    .height(40.dp)
            ) {
                Icon(
                    painter = backIcon,
                    contentDescription = "Volver",
                    tint = Color(0xFF111827)
                )
            }
        }

        Text(
            text = title,
            modifier = Modifier.align(if (showBackButton) Alignment.Center else Alignment.CenterStart),
            color = Color(0xFF111827),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ProductSummary(
    name: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = name,
            color = Color(0xFF111827),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            color = Color(0xFF667085),
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color(0xFF111827),
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun PrincipalRow(item: PresentacionUiItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7FBF8), RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.name,
                    color = Color(0xFF111827),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.principalDetail ?: item.detail,
                    color = Color(0xFF667085),
                    fontSize = 13.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PresentacionRow(
    item: PresentacionUiItem,
    showSelector: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSelectPrincipal: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (showSelector) {
                    Modifier.selectable(
                        selected = item.isPrincipal,
                        onClick = onSelectPrincipal
                    )
                } else {
                    Modifier
                }
            )
            .padding(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (showSelector) {
                RadioButton(
                    selected = item.isPrincipal,
                    onClick = onSelectPrincipal,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(0xFF98A2B3),
                        unselectedColor = Color(0xFFB8C2CC)
                    )
                )
            } else {
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = item.name,
                modifier = Modifier.weight(1f),
                color = Color(0xFF111827),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = item.price,
                color = Color(0xFF111827),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (showSelector) {
                Spacer(modifier = Modifier.width(42.dp))
            } else {
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = item.detail,
                modifier = Modifier.weight(1f),
                color = Color(0xFF667085),
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.isPrincipal) {
                    SoftBadge(text = "Principal")
                }
                if (item.isPrincipal) {
                    VerticalSeparator()
                }
                ActionText(text = "Editar", color = Color(0xFF16A34A), onClick = onEdit)
                VerticalSeparator()
                ActionText(text = "Eliminar", color = Color(0xFFE25555), onClick = onDelete)
            }
        }
    }
}

@Composable
private fun SoftBadge(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFF2F7F3), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF6B8A76),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun VerticalSeparator() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(18.dp)
            .background(Color(0xFFE3E8EE))
    )
}

@Composable
private fun AddButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3FBF6), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+ Agregar presentación",
            color = Color(0xFF16A34A),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun NoteRow(text: String) {
    Text(
        text = text,
        color = Color(0xFF98A2B3),
        fontSize = 13.sp,
        lineHeight = 18.sp
    )
}

@Composable
private fun PresentacionesFooterButtons(
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
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF16A34A)
            )
        ) {
            Text(text = "Cancelar", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Button(
            onClick = onSave,
            enabled = saveEnabled,
            modifier = Modifier
                .weight(1f)
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF16A34A),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFD0D5DD),
                disabledContentColor = Color.White
            )
        ) {
            Text(text = saveText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ActionText(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = color,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.clickable(onClick = onClick)
    )
}
