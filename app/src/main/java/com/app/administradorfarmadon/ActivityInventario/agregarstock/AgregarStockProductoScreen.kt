package com.app.administradorfarmadon.ActivityInventario.agregarstock

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val FarmadonGreen = Color(0xFF0E8F63)
val FarmadonLightGreen = Color(0xFFE5F4EB)
val FarmadonLightRed = Color(0xFFFFEBEE)
val FarmadonDarkRed = Color(0xFFC62828)
val FarmadonBackgroundGray = Color(0xFFF9F9FB)
val FarmadonTextGray = Color(0xFF4A4A4A)
val FarmadonIconGray = Color(0xFF757575)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarStockProductoScreen(
    state: AgregarStockProductoState,
    onEvent: (AgregarStockEvent) -> Unit,
    onBack: () -> Unit,
    onSupplierSelectClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar stock", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color.White, CircleShape)
                            .size(40.dp)
                            .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = FarmadonGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FarmadonBackgroundGray,
                    titleContentColor = FarmadonTextGray
                )
            )
        },
        bottomBar = {
            if (!state.isLoading && state.productoOriginal != null) {
                Surface(
                    color = Color.White,
                    shadowElevation = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val haptic = LocalHapticFeedback.current
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                onEvent(AgregarStockEvent.GuardarIngreso)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = state.isFormValid && !state.isSaving,
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FarmadonGreen,
                                disabledContainerColor = Color(0xFFE0E0E0)
                            )
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Lock, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Guardar ingreso de stock",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FarmadonBackgroundGray)
                .padding(padding)
        ) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = FarmadonGreen)
                }
            } else if (state.error != null && state.productoOriginal == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error, color = Color.Red, modifier = Modifier.padding(16.dp))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (state.error != null) {
                        Surface(
                            color = FarmadonLightRed,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.error,
                                color = FarmadonDarkRed,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    HeaderProductoCard(state)
                    SectionTitle(icon = Icons.AutoMirrored.Outlined.Label, title = "Identidad del producto")
                    if (state.necesitaConfigurarIdentidadFisica) {
                        ConfigurarIdentidadCard(state, onEvent)
                    } else {
                        IdentidadCard(state)
                    }

                    SectionTitle(icon = Icons.AutoMirrored.Outlined.HelpOutline, title = "¿Cómo recibiste la mercancía?")
                    if (!state.necesitaConfigurarIdentidadFisica) {
                        ModoIngresoCard(state, onEvent)
                        SectionTitle(icon = Icons.AutoMirrored.Outlined.Assignment, title = "Lote de ingreso")
                        LoteProveedorCard(state, onEvent, onSupplierSelectClick)
                        if (state.reconciliationHasMismatch) {
                            DescuadreCard(state, onEvent)
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    ProveedorDropdown(state, onEvent)
}

@Composable
fun SectionTitle(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFFF0F0F5), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = FarmadonIconGray,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = FarmadonTextGray
            )
        )
    }
}

@Composable
fun HeaderProductoCard(state: AgregarStockProductoState) {
    val prod = state.productoOriginal ?: return
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(FarmadonLightGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Inventory2, null, tint = FarmadonGreen, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "NUEVO INGRESO DE MERCADERÍA",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = FarmadonGreen
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                prod.nombre,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val stockActual = if (prod.lotes.isEmpty()) {
                    prod.cantidadinicial.toDoubleOrNull() ?: 0.0
                } else {
                    prod.lotes.values.sumOf { it.cantidad + it.cantidadBloqueada }
                }
                val stockActualStr = if (stockActual % 1.0 == 0.0) stockActual.toInt().toString() else stockActual.toString()
                InfoBadge(
                    icon = Icons.Outlined.Layers,
                    label = "Stock actual:",
                    value = "$stockActualStr ${state.unidadBase}",
                    modifier = Modifier.weight(1f)
                )
                InfoBadge(
                    icon = Icons.Outlined.Category,
                    label = "Contenido base:",
                    value = "${state.contenidoPorEnvase} ${state.unidadBase}",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun InfoBadge(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFFF9F9FB), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Text(value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = FarmadonGreen)
        }
    }
}

@Composable
fun ConfigurarIdentidadCard(
    state: AgregarStockProductoState,
    onEvent: (AgregarStockEvent) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Configura la identidad del producto",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                "Indica el contenido por envase (Ej. 355 mL)",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = state.pendingContenidoPorEnvaseTexto,
                onValueChange = {
                    onEvent(
                        AgregarStockEvent.OnIdentidadFisicaChanged(
                            state.pendingTipoControl,
                            state.pendingUnidadBase,
                            it
                        )
                    )
                },
                label = { Text("Contenido por envase") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FarmadonGreen,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onEvent(AgregarStockEvent.ConfirmarIdentidadFisica)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FarmadonGreen)
            ) {
                Text("Confirmar", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun IdentidadCard(state: AgregarStockProductoState) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Tipo", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(state.tipoControl, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            }
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color(0xFFEEEEEE)))
            Column(modifier = Modifier.weight(1f).padding(start = 20.dp), horizontalAlignment = Alignment.End) {
                Text("Contenido base", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                val baseTexto = if (state.tipoControl == "UNIDAD") "1 unidad" else "${state.contenidoPorEnvase} ${state.unidadBase}"
                Text(baseTexto, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun ModoIngresoCard(state: AgregarStockProductoState, onEvent: (AgregarStockEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ModoChip(
                text = "Unidades",
                icon = Icons.Outlined.LocalPharmacy,
                selected = state.modoIngreso == ModoIngresoStock.UNIDADES_SUELTAS,
                onClick = { onEvent(AgregarStockEvent.OnModoIngresoChanged(ModoIngresoStock.UNIDADES_SUELTAS)) },
                modifier = Modifier.weight(1f)
            )
            ModoChip(
                text = "Caja",
                icon = Icons.Outlined.AllInbox,
                selected = state.modoIngreso == ModoIngresoStock.CAJA_ENVASES,
                onClick = { onEvent(AgregarStockEvent.OnModoIngresoChanged(ModoIngresoStock.CAJA_ENVASES)) },
                modifier = Modifier.weight(1f)
            )
            if (!state.unidadesPorCajaAncladas) {
                ModoChip(
                    text = "Caja + Packs",
                    icon = Icons.Outlined.Layers,
                    selected = state.modoIngreso == ModoIngresoStock.CAJA_PAQUETES,
                    onClick = {
                        onEvent(
                            AgregarStockEvent.OnModoIngresoChanged(
                                ModoIngresoStock.CAJA_PAQUETES
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        when (state.modoIngreso) {
            ModoIngresoStock.UNIDADES_SUELTAS -> {
                CustomTextField(
                    value = state.unidadesSueltasText,
                    onValueChange = { onEvent(AgregarStockEvent.OnUnidadesSueltasChanged(it)) },
                    label = "Cantidad de envases/unidades recibidas"
                )
            }
            ModoIngresoStock.CAJA_ENVASES -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomTextField(
                        value = state.cajasRecibidasText,
                        onValueChange = { onEvent(AgregarStockEvent.OnCajasRecibidasChanged(it)) },
                        label = "Cajas",
                        modifier = Modifier.weight(1f)
                    )
                    if (state.unidadesPorCajaAncladas) {
                        Surface(
                            color = FarmadonLightGreen,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, FarmadonGreen.copy(alpha = 0.25f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Unidades por Caja",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${state.envasesPorCajaText} envases",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = FarmadonGreen
                                )
                            }
                        }
                    } else {
                        CustomTextField(
                            value = state.envasesPorCajaText,
                            onValueChange = { onEvent(AgregarStockEvent.OnEnvasesPorCajaChanged(it)) },
                            label = "Unidades x caja",
                            modifier = Modifier.weight(1f),
                            enabled = !state.unidadesPorCajaAncladas
                        )
                    }
                }
            }
            ModoIngresoStock.CAJA_PAQUETES -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomTextField(
                        value = state.cajasRecibidasText,
                        onValueChange = { onEvent(AgregarStockEvent.OnCajasRecibidasChanged(it)) },
                        label = "Cajas recibidas"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CustomTextField(
                            value = state.paquetesPorCajaText,
                            onValueChange = { onEvent(AgregarStockEvent.OnPaquetesPorCajaChanged(it)) },
                            label = "Packs x caja",
                            modifier = Modifier.weight(1f),
                            enabled = !state.unidadesPorCajaAncladas
                        )
                        CustomTextField(
                            value = state.envasesPorPaqueteText,
                            onValueChange = { onEvent(AgregarStockEvent.OnEnvasesPorPaqueteChanged(it)) },
                            label = "Unid. x pack",
                            modifier = Modifier.weight(1f),
                            enabled = !state.unidadesPorCajaAncladas
                        )
                    }
                }
            }
        }

        Surface(
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color(0xFFF0F0F0))
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(44.dp).background(Color(0xFFF9F9FB), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Calculate, null, tint = FarmadonGreen)
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("TOTAL A INGRESAR", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = FarmadonGreen)
                    val formatNum = if (state.totalBaseCalculado % 1.0 == 0.0) state.totalBaseCalculado.toInt().toString() else state.totalBaseCalculado.toString()
                    Text(
                        text = "$formatNum ${state.unidadBase}",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color(0xFFF0F0F0)))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Stock después:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    val stockActual = state.productoOriginal?.let { prod ->
                        if (prod.lotes.isEmpty()) {
                            prod.cantidadinicial.toDoubleOrNull() ?: 0.0
                        } else {
                            prod.lotes.values.sumOf { it.cantidad + it.cantidadBloqueada }
                        }
                    } ?: 0.0
                    val stockFinal = stockActual + state.totalBaseCalculado
                    val finalStr = if (stockFinal % 1.0 == 0.0) stockFinal.toInt().toString() else stockFinal.toString()
                    Text("$finalStr ${state.unidadBase}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = FarmadonGreen)
                }
            }
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FarmadonGreen,
            unfocusedBorderColor = Color(0xFFF0F0F0),
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White
        ),
        enabled = enabled,
        trailingIcon = trailingIcon
    )
}

@Composable
fun ModoChip(text: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    Surface(
        color = if (selected) Color.White else Color.White,
        border = BorderStroke(1.dp, if (selected) FarmadonGreen else Color(0xFFF0F0F0)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.clickable(onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
            onClick()
        }),
        tonalElevation = if (selected) 4.dp else 0.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Icon(icon, null, tint = if (selected) FarmadonGreen else Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                text,
                color = if (selected) FarmadonGreen else Color.Gray,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp)
            )
        }
    }
}

@Composable
fun LoteProveedorCard(
    state: AgregarStockProductoState,
    onEvent: (AgregarStockEvent) -> Unit,
    onSupplierClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                CustomTextField(
                    value = state.loteNumero,
                    onValueChange = { onEvent(AgregarStockEvent.OnLoteChanged(it)) },
                    label = "Número de lote",
                    trailingIcon = {
                        if (state.isCheckingLot) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = FarmadonGreen
                            )
                        }
                    }
                )

                state.lotConflictMessage?.let { message ->
                    val color = when (state.lotConflictSeverity) {
                        0 -> FarmadonGreen
                        1 -> Color(0xFF2563EB)
                        else -> FarmadonDarkRed
                    }

                    Text(
                        text = message,
                        color = color,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            CustomTextField(
                value = state.loteVencimiento,
                onValueChange = { onEvent(AgregarStockEvent.OnVencimientoChanged(it)) },
                label = "Vencimiento (MM/AA)",
                modifier = Modifier.weight(1f)
            )
        }

        CustomTextField(
            value = state.numeroFactura,
            onValueChange = { onEvent(AgregarStockEvent.OnFacturaChanged(it)) },
            label = "Factura / Referencia"
        )

        CustomTextField(
            value = state.costoCompra,
            onValueChange = { onEvent(AgregarStockEvent.OnCostoChanged(it)) },
            label = "Costo total de compra"
        )

        Surface(
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onSupplierClick()
                },
            border = BorderStroke(1.dp, Color(0xFFF0F0F0))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFFF0F0F5),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.LocalShipping,
                        contentDescription = null,
                        tint = FarmadonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Proveedor",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )

                    Text(
                        text = state.proveedorNombre.ifBlank {
                            "Seleccionar proveedor"
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
        }
    }
}

@Composable
fun DescuadreCard(state: AgregarStockProductoState, onEvent: (AgregarStockEvent) -> Unit) {
    Surface(
        color = FarmadonLightRed,
        border = BorderStroke(1.dp, FarmadonDarkRed.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            val haptic = LocalHapticFeedback.current
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Warning, null, tint = FarmadonDarkRed, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "¿Por qué hay un descuadre?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = FarmadonDarkRed
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            val reasons = listOf(
                "Solo ingreso una parte del pack",
                "El pack vino incompleto",
                "El dato registrado del producto es incorrecto",
                "Otro motivo"
            )

            reasons.forEach { reason ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            onEvent(AgregarStockEvent.OnReconciliationReasonSelected(reason))
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.reconciliationReason == reason,
                        onClick = { onEvent(AgregarStockEvent.OnReconciliationReasonSelected(reason)) },
                        colors = RadioButtonDefaults.colors(selectedColor = FarmadonDarkRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(reason, style = MaterialTheme.typography.bodyMedium, color = FarmadonDarkRed)
                }
            }
        }
    }
}

@Composable
fun ProveedorDropdown(state: AgregarStockProductoState, onEvent: (AgregarStockEvent) -> Unit) {
    if (state.showSupplierDialog) {
        val haptic = LocalHapticFeedback.current
        AlertDialog(
            onDismissRequest = { onEvent(AgregarStockEvent.OnSupplierDialogDismiss) },
            title = { Text("Seleccionar proveedor", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Inventario inicial / Sin proveedor",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                onEvent(
                                    AgregarStockEvent.OnProveedorSelected(
                                        "",
                                        "Inventario inicial / Sin proveedor"
                                    )
                                )
                            }
                            .padding(vertical = 16.dp),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = FarmadonGreen)
                    )
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    state.suppliers.forEach { proveedor ->
                        Text(
                            text = proveedor.nombre,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                    onEvent(
                                        AgregarStockEvent.OnProveedorSelected(
                                            proveedor.id,
                                            proveedor.nombre
                                        )
                                    )
                                }
                                .padding(vertical = 16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onEvent(AgregarStockEvent.OnSupplierDialogDismiss)
                }) {
                    Text("Cancelar", style = MaterialTheme.typography.labelLarge.copy(color = Color.Gray))
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}
