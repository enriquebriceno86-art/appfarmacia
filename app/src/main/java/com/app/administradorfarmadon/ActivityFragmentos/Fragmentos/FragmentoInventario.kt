package com.app.administradorfarmadon.ActivityFragmentos.Fragmentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.administradorfarmadon.ActivityFragmentos.Fragmentos.logicainventario.BuscadorEscanerViewModel
import com.app.administradorfarmadon.ActivityFragmentos.Fragmentos.logicainventario.FiltroAlerta
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.stockFisicoBase
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.stockMinimoBase
import com.app.administradorfarmadon.ActivityInventario.ui.BarcodeScannerOverlay
import com.app.administradorfarmadon.ClasesDatabase.FeedbackCajaController
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import android.content.Intent
import com.app.administradorfarmadon.ActivityInventario.CrearProducto
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip

class FragmentoInventario : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    val viewModel: BuscadorEscanerViewModel = viewModel()
                    InventarioScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun InventarioScreen(viewModel: BuscadorEscanerViewModel) {
    val state by viewModel.state.collectAsState()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val feedbackController = remember { FeedbackCajaController(context) }
    
    // V28.5: Sincronización proactiva al regresar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.refrescar()
    }

    val listState = rememberLazyListState()
    val isAtTop by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 100 } }
    val showTitle by remember { derivedStateOf { isAtTop && state.busquedaQuery.isEmpty() && !state.busquedaInteligenteActiva } }

    // Manejo de sonido de error al no encontrar producto
    LaunchedEffect(state.productoNoEncontrado) {
        if (state.productoNoEncontrado) {
            delay(1000) // Esperar 1 segundo para no chocar con el sonido del escáner
            feedbackController.error(null)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FC))) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showTitle && state.productosFiltrados.isNotEmpty(),
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Text(
                            text = "Inventario",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF111827),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = state.busquedaQuery,
                            onValueChange = { viewModel.actualizarBusqueda(it) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Buscar producto...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (state.busquedaQuery.isNotEmpty()) {
                                    IconButton(onClick = { 
                                        viewModel.actualizarBusqueda("")
                                        focusManager.clearFocus()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Limpiar búsqueda"
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (state.busquedaInteligenteActiva && state.busquedaQuery.isNotBlank()) {
                                        viewModel.agregarTermino(state.busquedaQuery)
                                    } else {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    }
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF3F4F6),
                                unfocusedContainerColor = Color(0xFFF3F4F6),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        
                        IconButton(
                            onClick = { 
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                viewModel.setEstaEscaneando(true) 
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Escanear código",
                                tint = Color(0xFF15A05C)
                            )
                        }
                    }

                    // Fila de Filtros y Categorías (Se oculta si no hay productos)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = state.productosFiltrados.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Toggle Búsqueda Inteligente (Premium Magic Chip)
                                MagicSymptomChip(
                                    selected = state.busquedaInteligenteActiva,
                                    onClick = { viewModel.toggleBusquedaInteligente() }
                                )

                                androidx.compose.animation.AnimatedVisibility(
                                    visible = !state.busquedaInteligenteActiva,
                                    enter = fadeIn() + expandHorizontally(),
                                    exit = fadeOut() + shrinkHorizontally()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Spacer(modifier = Modifier.width(12.dp))
                                        
                                        // Divisor Visual Sutil
                                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFE5EAF0)))
                                        
                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Chips de Categorías (Scroll Horizontal)
                                        LazyRow(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            contentPadding = PaddingValues(end = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            items(state.categorias) { cat ->
                                                val isSelected = state.categoriaSeleccionada == cat
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = { viewModel.seleccionarCategoria(cat) },
                                                    label = { 
                                                        Text(
                                                            cat,
                                                            fontSize = 12.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                        ) 
                                                    },
                                                    shape = RoundedCornerShape(20.dp),
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = Color(0xFFE8F5EE),
                                                        selectedLabelColor = Color(0xFF15A05C),
                                                        containerColor = Color.Transparent,
                                                        labelColor = Color(0xFF6B7280)
                                                    ),
                                                    border = FilterChipDefaults.filterChipBorder(
                                                        borderColor = Color(0xFFE5EAF0),
                                                        selectedBorderColor = Color(0xFF15A05C).copy(alpha = 0.5f),
                                                        borderWidth = 1.dp,
                                                        selectedBorderWidth = 1.5.dp,
                                                        enabled = true,
                                                        selected = isSelected
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (state.busquedaInteligenteActiva && state.busquedaQuery.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { viewModel.agregarTermino(state.busquedaQuery) },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = Color(0xFF15A05C))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Añadir síntoma: '${state.busquedaQuery}'", color = Color(0xFF15A05C), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    if (state.busquedaInteligenteActiva && state.terminosSeleccionados.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.terminosSeleccionados.forEach { termino ->
                                InputChip(
                                    selected = true,
                                    onClick = { viewModel.eliminarTermino(termino) },
                                    label = { Text(termino) },
                                    trailingIcon = { 
                                        Icon(
                                            Icons.Default.Close, 
                                            null, 
                                            modifier = Modifier.size(16.dp)
                                        ) 
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    // V22.1: El carrusel de alertas se separó de nuevo para mayor claridad visual
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !state.busquedaInteligenteActiva && state.productosFiltrados.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            val alerts = listOfNotNull(
                                if (state.conteoVencidos > 0) Triple(FiltroAlerta.VENCIDOS, "Vencidos", Color(0xFFD32F2F)) else null,
                                if (state.conteoStockBajo > 0) Triple(FiltroAlerta.STOCK_BAJO, "Stock Bajo", Color(0xFFEF6C00)) else null,
                                if (state.conteoPorVencer > 0) Triple(FiltroAlerta.POR_VENCER, "Por Vencer", Color(0xFFFBC02D)) else null,
                                if (state.conteoSinCodigo > 0) Triple(FiltroAlerta.SIN_CODIGO, "Sin Código", Color(0xFF616161)) else null
                            )

                            if (alerts.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(horizontal = 0.dp)
                                ) {
                                    items(alerts) { (tipo, titulo, color) ->
                                        val isSelected = state.filtroAlertaActivo == tipo
                                        val count = when(tipo) {
                                            FiltroAlerta.VENCIDOS -> state.conteoVencidos
                                            FiltroAlerta.STOCK_BAJO -> state.conteoStockBajo
                                            FiltroAlerta.POR_VENCER -> state.conteoPorVencer
                                            FiltroAlerta.SIN_CODIGO -> state.conteoSinCodigo
                                            else -> 0
                                        }

                                        Surface(
                                            onClick = { viewModel.toggleFiltroAlerta(tipo) },
                                            color = if (isSelected) color.copy(alpha = 0.1f) else Color.White,
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(
                                                1.dp, 
                                                if (isSelected) color else color.copy(alpha = 0.2f)
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "$count $titulo",
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                                    color = if (isSelected) color else Color(0xFF374151)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }

            if (state.estaCargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF15A05C))
                }
            } else {
                val isExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
                
                // Ocultar teclado al arrastrar la lista
                LaunchedEffect(listState.isScrollInProgress) {
                    if (listState.isScrollInProgress) {
                        focusManager.clearFocus()
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (state.productosFiltrados.isEmpty()) {
                        EmptyInventoryState(
                            query = state.busquedaQuery,
                            isIntelligent = state.busquedaInteligenteActiva,
                            onClear = { viewModel.actualizarBusqueda("") },
                            onCreate = {
                                val intent = Intent(context, CrearProducto::class.java)
                                context.startActivity(intent)
                            }
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.productosFiltrados) { producto ->
                                ProductCard(
                                    producto = producto,
                                    terminosResaltados = state.terminosSeleccionados,
                                    resaltado = state.indiceResaltado == producto.indice,
                                    onEdit = {
                                        val intent = Intent(context, com.app.administradorfarmadon.ActivityInventario.EditarProductodelInventario::class.java).apply {
                                            putExtra("indice", producto.indice)
                                        }
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }
                    }

                    // BOTÓN FLOTANTE PREMIUM (Efecto Agua/3D)
                    // Se oculta automáticamente durante el scroll o cualquier tipo de búsqueda
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !listState.isScrollInProgress && 
                                 !state.busquedaInteligenteActiva && 
                                 state.busquedaQuery.isEmpty() && 
                                 state.terminosSeleccionados.isEmpty(),
                        enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)) + fadeIn(),
                        exit = scaleOut() + fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp)
                    ) {
                        FluidFloatingActionButton(
                            expanded = isExpanded,
                            onClick = {
                                val intent = Intent(context, CrearProducto::class.java)
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }

        if (state.estaEscaneando) {
            @OptIn(androidx.camera.core.ExperimentalGetImage::class)
            BarcodeScannerOverlay(
                onBarcodeDetected = { result ->
                    viewModel.procesarCodigoEscaneado(result.code)
                },
                onDismiss = {
                    viewModel.setEstaEscaneando(false)
                }
            )
        }

        if (state.productoNoEncontrado) {
            ProductNotFoundDialog(
                codigo = state.ultimoCodigoEscaneado,
                onDismiss = { viewModel.limpiarErrorEscaneo() },
                onRegistrar = {
                    viewModel.limpiarErrorEscaneo()
                    val intent = Intent(context, CrearProducto::class.java).apply {
                        putExtra("codigo_escaneado", state.ultimoCodigoEscaneado)
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun ProductNotFoundDialog(
    codigo: String,
    onDismiss: () -> Unit,
    onRegistrar: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Icono animado
                val infiniteTransition = rememberInfiniteTransition(label = "warning")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale"
                )

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .background(Color(0xFFFFF7ED), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFEA580C),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "No encontrado",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "El código $codigo no existe en tu inventario.",
                        fontSize = 15.sp,
                        color = Color(0xFF6B7280),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                // BOTÓN REGISTRAR PREMIUM (Estilo 3D, Agua/Skeleton)
                val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
                val shimmerPos by shimmerTransition.animateFloat(
                    initialValue = -1000f, targetValue = 1000f,
                    animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)), label = "pos"
                )

                val gradientBrush = Brush.linearGradient(
                    colors = listOf(Color(0xFFEA580C), Color(0xFFDC2626), Color(0xFFEA580C)),
                    start = androidx.compose.ui.geometry.Offset(shimmerPos, 0f),
                    end = androidx.compose.ui.geometry.Offset(shimmerPos + 500f, 500f)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onRegistrar() },
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(gradientBrush)
                            .padding(1.dp), // Efecto borde
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "REGISTRAR PRODUCTO",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Omitir por ahora", color = Color(0xFF9CA3AF), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FluidFloatingActionButton(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onClick: () -> Unit
) {
    val shimmerTransition = rememberInfiniteTransition(label = "fab_shimmer")
    val shimmerPos by shimmerTransition.animateFloat(
        initialValue = -1000f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing)), label = "pos"
    )

    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF15A05C), Color(0xFF0F7A3A), Color(0xFF15A05C)),
        start = androidx.compose.ui.geometry.Offset(shimmerPos, 0f),
        end = androidx.compose.ui.geometry.Offset(shimmerPos + 500f, 500f)
    )

    Surface(
        modifier = modifier
            .height(56.dp)
            .widthIn(min = 56.dp)
            .graphicsLayer {
                shadowElevation = 12f
                shape = RoundedCornerShape(28.dp)
                clip = true
            }
            .clickable { onClick() },
        color = Color.Transparent,
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(
            modifier = Modifier
                .background(gradientBrush)
                .padding(horizontal = if (expanded) 20.dp else 0.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                
                androidx.compose.animation.AnimatedVisibility(
                    visible = expanded,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandHorizontally(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkHorizontally()
                ) {
                    Row {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "NUEVO PRODUCTO",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MagicSymptomChip(
    selected: Boolean,
    onClick: () -> Unit
) {
    val shimmerTransition = rememberInfiniteTransition(label = "magic_shimmer")
    val shimmerPos by shimmerTransition.animateFloat(
        initialValue = -500f,
        targetValue = 500f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_pos"
    )

    val activeGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF2563EB), // Azul
            Color(0xFF8B5CF6), // Morado
            Color(0xFFD946EF), // Rosa/Magenta
            Color(0xFF2563EB)  // Cierre para ciclo
        ),
        start = Offset(shimmerPos, 0f),
        end = Offset(shimmerPos + 300f, 300f)
    )

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        color = if (selected) Color.Transparent else Color(0xFFF3F4F6),
        border = if (!selected) BorderStroke(1.dp, Color(0xFFE5EAF0)) else null,
        shadowElevation = if (selected) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier
                .then(if (selected) Modifier.background(activeGradient) else Modifier)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = if (selected) Color.White else Color(0xFF6B7280),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SÍNTOMAS",
                    color = if (selected) Color.White else Color(0xFF4B5563),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun EmptyInventoryState(
    query: String,
    isIntelligent: Boolean,
    onClear: () -> Unit,
    onCreate: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty_state")
    
    // Animación de flotación para el icono (Efecto 3D)
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icono con profundidad y sombra
        Box(
            modifier = Modifier
                .offset(y = floatAnim.dp)
                .size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            // Sombra del icono
            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .offset(y = 30.dp)
                    .graphicsLayer(alpha = 0.1f, scaleX = 1.2f, scaleY = 0.4f),
                color = Color.Black,
                shape = CircleShape
            ) {}
            
            // Contenedor del icono con gradiente
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier.background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFF3F4F6), Color(0xFFE5EAF0))
                        )
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIntelligent) Icons.Default.AutoAwesome else Icons.Outlined.SearchOff,
                        contentDescription = null,
                        tint = if (isIntelligent) Color(0xFF8B5CF6) else Color(0xFF9CA3AF),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        androidx.compose.animation.AnimatedVisibility(
            visible = query.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Tipografía tipo Uber (Clean, Heavy, Modern)
                Text(
                    text = "Sin resultados",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF111827),
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "No encontramos nada que coincida con \"$query\". Prueba con otros términos.",
                    fontSize = 16.sp,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Botón de acción Limpiar
                OutlinedButton(
                    onClick = onClear,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5EAF0))
                ) {
                    Text("Limpiar", color = Color(0xFF4B5563), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    producto: MoldeProductos,
    terminosResaltados: List<String> = emptyList(),
    resaltado: Boolean = false,
    onEdit: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "highlight")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "alpha"
    )
    
    val estadoVencimiento = remember(producto) { com.app.administradorfarmadon.ActivityInventario.ProductUtils.obtenerEstadoVencimiento(producto) }
    val currentStock = remember(producto) { producto.stockFisicoBase() }
    val minStock = remember(producto) { producto.stockMinimoBase() }
    
    val esStockBajo = remember(currentStock, minStock) { 
        currentStock < minStock && minStock > 0
    }

    // Lógica del Semáforo de Vencimiento
    val colorSemaforo = remember(producto) {
        val loteProximo = com.app.administradorfarmadon.ActivityInventario.ProductUtils.obtenerLoteConVencimientoMasProximo(producto)
        val dias = loteProximo?.vencimiento?.let { 
            com.app.administradorfarmadon.ActivityInventario.ProductUtils.diasHastaVencerLote(it) 
        }
        
        when {
            dias == null -> Color.Transparent
            dias < 30 -> Color(0xFFD32F2F) // Rojo: < 1 mes
            dias < 90 -> Color(0xFFEA580C) // Naranja: < 3 meses
            dias > 180 -> Color(0xFF15A05C) // Verde: > 6 meses
            else -> Color(0xFF9CA3AF) // Gris: entre 3 y 6 meses (Vigente normal)
        }
    }

    var showInfoSheet by remember { mutableStateOf(false) }
    var lotesCargados by remember { mutableStateOf<List<com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteProducto>?>(null) }
    var cargandoLotes by remember { mutableStateOf(false) }

    LaunchedEffect(showInfoSheet) {
        if (showInfoSheet && lotesCargados == null) {
            cargandoLotes = true
            val db = com.google.firebase.database.FirebaseDatabase.getInstance()
            db.getReference(com.app.administradorfarmadon.ClasesDatabase.DbPaths.INVENTARIO_PRODUCTO_LOTES)
                .child(producto.indice)
                .get()
                .addOnSuccessListener { snapshot ->
                    val list = mutableListOf<com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteProducto>()
                    snapshot.children.forEach { child ->
                        child.getValue(com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteProducto::class.java)?.let {
                            list.add(it)
                        }
                    }
                    lotesCargados = list
                    cargandoLotes = false
                }
                .addOnFailureListener { cargandoLotes = false }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (resaltado) 3.dp else 1.dp,
            color = if (resaltado) Color(0xFF15A05C).copy(alpha = borderAlpha) else Color(0xFFE5EAF0)
        ),
        shadowElevation = if (resaltado) 4.dp else 0.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // INDICADOR DE SEMÁFORO (BARRA VERTICAL IZQUIERDA)
            if (colorSemaforo != Color.Transparent) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                        .background(colorSemaforo)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // INFO IZQUIERDA: NOMBRE Y UBICACIÓN
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = producto.nombre,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827),
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            
                            if (estadoVencimiento == "VENCIDO") {
                                Spacer(modifier = Modifier.width(8.dp))
                                Badge(containerColor = Color(0xFFFEE2E2)) {
                                    Text("VENCIDO", color = Color(0xFFD92D20), fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            } else if (esStockBajo) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Badge(containerColor = Color(0xFFFFF7ED)) {
                                    Text("STOCK BAJO", color = Color(0xFFEA580C), fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                        
                        if (producto.ubicacion.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = Color(0xFF6B7280),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = producto.ubicacion,
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // INFO DERECHA: STOCK Y STOCK MÍNIMO
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = "${if (currentStock % 1.0 == 0.0) currentStock.toInt() else currentStock} ${producto.unidadVisualInventario}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = if (esStockBajo) Color(0xFFEA580C) else Color(0xFF15A05C)
                        )
                        if (producto.stockminimo.isNotBlank()) {
                            Text(
                                text = "Mín: ${producto.stockminimo} ${producto.unidadVisualInventario}",
                                fontSize = 11.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }

                    // DIVISOR E ICONO DE INFO
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(modifier = Modifier.width(1.dp).height(28.dp).background(Color(0xFFF3F4F6)))
                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { showInfoSheet = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Detalles",
                            tint = if (showInfoSheet) Color(0xFF15A05C) else Color(0xFF9CA3AF),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // MANTENEMOS LOS MATCHES DE SÍNTOMAS (SUB-TARJETAS) PARA EL BUSCADOR
                if (terminosResaltados.isNotEmpty()) {
                    val matches = (producto.referenceUseCases + producto.referenceKeywords + listOf(producto.referenceCommonUse))
                        .filter { text -> terminosResaltados.any { it in text.lowercase() } }
                        .distinct()

                    if (matches.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            matches.take(3).forEach { match ->
                                Surface(
                                    color = Color(0xFFFFF7ED),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFED7AA))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFFEA580C),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = match,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFEA580C)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showInfoSheet) {
        ProductDetailsBottomSheet(
            producto = producto,
            lotes = lotesCargados,
            isLoading = cargandoLotes,
            onDismiss = { showInfoSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsBottomSheet(
    producto: MoldeProductos,
    lotes: List<com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteProducto>?,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // V26.5: Elevación de estado del Kardex para evitar recargas al cambiar de pestaña
    var movimientos by remember { mutableStateOf<List<com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MovimientoInventario>?>(null) }
    var isLoadingKardex by remember { mutableStateOf(false) }

    // Cargar Kardex solo una vez cuando se necesite
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1 && movimientos == null) {
            isLoadingKardex = true
            val db = com.google.firebase.database.FirebaseDatabase.getInstance()
            db.getReference(com.app.administradorfarmadon.ClasesDatabase.DbPaths.INVENTARIO_MOVIMIENTOS)
                .child(producto.indice)
                .limitToLast(50)
                .get()
                .addOnSuccessListener { snapshot ->
                    val list = mutableListOf<com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MovimientoInventario>()
                    snapshot.children.forEach { child ->
                        child.getValue(com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MovimientoInventario::class.java)?.let {
                            list.add(it)
                        }
                    }
                    movimientos = list.reversed()
                    isLoadingKardex = false
                }
                .addOnFailureListener { isLoadingKardex = false }
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFE5E7EB)) },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f) // V26.7: Altura fija al 85% para evitar que el panel "baile" al cambiar pestañas
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // CABECERA
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFF3F4F6), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory,
                        contentDescription = null,
                        tint = Color(0xFF15A05C),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = producto.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "${producto.categoria}${if (producto.proveedorNombre.isNotBlank()) " • Provedor: ${producto.proveedorNombre}" else ""}",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SELECTOR DE PESTAÑAS (TABS) PREMIUM
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF15A05C),
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFF15A05C)
                        )
                    }
                },
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("LOTES ACTUALES", fontSize = 11.sp, fontWeight = FontWeight.Black) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("KARDEX (HISTORIAL)", fontSize = 11.sp, fontWeight = FontWeight.Black) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // V26.6: Uso de Crossfade para un cambio de pestañas sin "golpes" ni re-animaciones
            Box(modifier = Modifier.weight(1f)) { // Contenedor que absorbe el scroll
                Crossfade(targetState = selectedTab, label = "tab_fade") { tab ->
                    when (tab) {
                        0 -> LotesView(producto, lotes, isLoading)
                        1 -> KardexView(movimientos, isLoadingKardex)
                    }
                }
            }
        }
    }
}

@Composable
fun LotesView(
    producto: MoldeProductos,
    lotes: List<com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteProducto>?,
    isLoading: Boolean
) {
    if (isLoading) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) { LotCardSkeleton() }
        }
    } else if (lotes.isNullOrEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No se encontraron lotes registrados", color = Color(0xFF6B7280))
        }
    } else {
        val lotesOrdenados = lotes.sortedBy { it.vencimiento }
        val loteConsumo = lotesOrdenados.filter { it.cantidad > 0 }.minByOrNull { 
            com.app.administradorfarmadon.ActivityInventario.ProductUtils.diasHastaVencerLote(it.vencimiento) ?: Int.MAX_VALUE 
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(lotesOrdenados) { lote ->
                val esConsumoActual = lote == loteConsumo
                val diasParaVencer = com.app.administradorfarmadon.ActivityInventario.ProductUtils.diasHastaVencerLote(lote.vencimiento)
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (esConsumoActual) Color(0xFF15A05C).copy(alpha = 0.5f) else Color(0xFFE5E7EB)
                    ),
                    color = if (esConsumoActual) Color(0xFFF0FDF4) else Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Lote: ${lote.numero}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF111827)
                                )
                                if (esConsumoActual) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Badge(containerColor = Color(0xFF15A05C)) {
                                        Text("EN CONSUMO", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                            
                            val colorVenc = when {
                                diasParaVencer == null -> Color(0xFF6B7280)
                                diasParaVencer < 0 -> Color(0xFFD32F2F)
                                diasParaVencer <= 90 -> Color(0xFFEA580C)
                                else -> Color(0xFF15A05C)
                            }

                            Surface(
                                color = colorVenc.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Vence: ${lote.vencimiento}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorVenc,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            InfoBlock(label = "FECHA INGRESO", value = lote.fecha.ifBlank { "N/A" })
                            InfoBlock(
                                label = "COSTO COMPRA", 
                                value = com.app.administradorfarmadon.ClasesDatabase.MonedaHelper.formatear(lote.costoUltimoIngreso),
                                valueColor = Color(0xFF111827)
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text("DISPONIBLE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF9CA3AF))
                                Text(
                                    text = "${if (lote.cantidad % 1.0 == 0.0) lote.cantidad.toInt() else lote.cantidad} ${producto.unidadVisualInventario}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (lote.cantidad <= 0) Color(0xFFD32F2F) else Color(0xFF111827)
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
fun KardexView(
    movimientos: List<com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MovimientoInventario>?,
    isLoading: Boolean
) {
    var filtroLote by remember { mutableStateOf("Todos") }

    Column(modifier = Modifier.fillMaxWidth()) {
        // FILTRO POR LOTE DENTRO DEL KARDEX
        val lotesDisponibles = remember(movimientos) {
            listOf("Todos") + (movimientos?.map { it.numeroLote }?.filter { it.isNotBlank() }?.distinct()?.sorted() ?: emptyList())
        }

        if (lotesDisponibles.size > 1) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(lotesDisponibles) { lote ->
                    FilterChip(
                        selected = filtroLote == lote,
                        onClick = { filtroLote = lote },
                        label = { Text(lote, fontSize = 10.sp) },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }

        if (isLoading) {
            repeat(3) { LotCardSkeleton() }
        } else if (movimientos.isNullOrEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("No hay movimientos registrados", color = Color(0xFF6B7280))
            }
        } else {
            val movimientosFiltrados = if (filtroLote == "Todos") movimientos!! else movimientos!!.filter { it.numeroLote == filtroLote }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(movimientosFiltrados) { mov ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val colorIcon = when(mov.tipo) {
                                "COMPRA" -> Color(0xFF15A05C)
                                "VENTA" -> Color(0xFF2563EB)
                                else -> Color(0xFFD32F2F)
                            }

                            Box(
                                modifier = Modifier.size(40.dp).background(colorIcon.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (mov.cantidad > 0) Icons.Default.Add else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = colorIcon,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${if (mov.cantidad > 0) "+" else ""}${if (mov.cantidad % 1.0 == 0.0) mov.cantidad.toInt() else mov.cantidad} ${mov.unidadVisual}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = colorIcon
                                )
                                Text(
                                    text = "${mov.tipo} - ${mov.referencia}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF374151)
                                )
                                if (mov.numeroLote.isNotBlank()) {
                                    Text(text = "Lote: ${mov.numeroLote}", fontSize = 11.sp, color = Color(0xFF6B7280))
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = mov.fecha.take(10), fontSize = 11.sp, color = Color(0xFF9CA3AF))
                                Text(
                                    text = "Final: ${if (mov.stockResultante % 1.0 == 0.0) mov.stockResultante.toInt() else mov.stockResultante}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
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
fun LotCardSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "alpha"
    )
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF3F4F6).copy(alpha = alpha),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {}
}

@Composable
fun InfoBlock(label: String, value: String, valueColor: Color = Color(0xFF6B7280)) {
    Column {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF9CA3AF))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
