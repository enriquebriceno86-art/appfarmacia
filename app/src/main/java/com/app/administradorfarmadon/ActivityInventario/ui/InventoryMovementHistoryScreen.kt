package com.app.administradorfarmadon.ActivityInventario.ui

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import androidx.compose.foundation.lazy.rememberLazyListState
import com.app.administradorfarmadon.R

enum class MovementQuickFilter(val label: String) {
    ALL("Todos"),
    ENTRY("Entradas"),
    EXIT("Salidas"),
    ADJUSTMENT("Ajustes")
}

enum class MovementTimelineType(
    val title: String,
    val accent: Color,
    val softAccent: Color
) {
    ENTRY(
        title = "Entrada de stock",
        accent = Color(0xFF12A150),
        softAccent = Color(0xFFE9F8EF)
    ),
    EXIT(
        title = "Salida de stock",
        accent = Color(0xFFE24C4B),
        softAccent = Color(0xFFFFF1F0)
    ),
    ADJUSTMENT(
        title = "Ajuste de stock",
        accent = Color(0xFF2785E2),
        softAccent = Color(0xFFEDF5FF)
    ),
    WASTE(
        title = "Merma",
        accent = Color(0xFFFF8A1F),
        softAccent = Color(0xFFFFF5EB)
    )
}

@Immutable
data class InventoryMovementTimelineUi(
    val id: String,
    val productIndex: String,
    val type: MovementTimelineType,
    val typeTitle: String,
    val productName: String,
    val quantityLine: String,
    val lotLine: String?,
    val noteLine: String?,
    val timeLabel: String,
    val actorLabel: String,
    val presentationLabel: String,
    val quantityDetail: String,
    val lotNumber: String?,
    val expiration: String?,
    val reason: String?,
    val dateTimeLabel: String,
    val originLabel: String,
    val userLabel: String
)

@Immutable
data class MovementTimelineGroup(
    val title: String,
    val items: List<InventoryMovementTimelineUi>
)

@Composable
fun InventoryMovementHistoryRoute(
    items: List<InventoryMovementTimelineUi>,
    isLoading: Boolean,
    searchQuery: String,
    selectedFilter: MovementQuickFilter,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (MovementQuickFilter) -> Unit,
    onBack: () -> Unit,
    onMovementClick: (String) -> Unit,
    pageSize: Int = 30,
    historyBackIcon: Painter = painterResource(id = R.drawable.baseline_arrow_back_24)
) {
    val filteredItems = remember(items, searchQuery, selectedFilter) {
        items.filter { item ->
            val matchesFilter = when (selectedFilter) {
                MovementQuickFilter.ALL -> true
                MovementQuickFilter.ENTRY -> item.type == MovementTimelineType.ENTRY
                MovementQuickFilter.EXIT -> item.type == MovementTimelineType.EXIT
                MovementQuickFilter.ADJUSTMENT -> item.type == MovementTimelineType.ADJUSTMENT
            }
            val normalizedQuery = searchQuery.trim()
            val matchesQuery = normalizedQuery.isBlank() || listOf(
                item.productName,
                item.lotNumber,
                item.actorLabel,
                item.userLabel,
                item.originLabel,
                item.reason,
                item.presentationLabel
            ).any { field ->
                field.orEmpty().contains(normalizedQuery, ignoreCase = true)
            }
            matchesFilter && matchesQuery
        }
    }

    var visibleCount by rememberSaveable(filteredItems) {
        mutableIntStateOf(minOf(pageSize, filteredItems.size))
    }
    val visibleItems = remember(filteredItems, visibleCount) {
        filteredItems.take(visibleCount.coerceAtMost(filteredItems.size))
    }
    val groupedItems = remember(visibleItems) { groupMovementsForTimeline(visibleItems) }
    val hasMoreItems = visibleCount < filteredItems.size

    InventoryMovementHistoryScreen(
        groups = groupedItems,
        isLoading = isLoading,
        hasMoreItems = hasMoreItems,
        searchQuery = searchQuery,
        selectedFilter = selectedFilter,
        onSearchQueryChange = onSearchQueryChange,
        onFilterChange = onFilterChange,
        onRequestMore = {
            visibleCount = (visibleCount + pageSize).coerceAtMost(filteredItems.size)
        },
        onItemClick = onMovementClick,
        onBack = onBack,
        historyBackIcon = historyBackIcon
    )
}

@Composable
fun InventoryMovementHistoryScreen(
    groups: List<MovementTimelineGroup>,
    isLoading: Boolean,
    hasMoreItems: Boolean,
    searchQuery: String,
    selectedFilter: MovementQuickFilter,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (MovementQuickFilter) -> Unit,
    onRequestMore: () -> Unit,
    onItemClick: (String) -> Unit,
    onBack: () -> Unit,
    historyBackIcon: Painter
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = historyBackIcon,
                        contentDescription = "Volver",
                        tint = Color(0xFF101828)
                    )
                }
                Text(
                    text = "Historial de movimientos",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = Color(0xFF101828)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Buscar producto, lote o usuario...",
                        color = Color(0xFF98A2B3)
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_history_search),
                        contentDescription = null,
                        tint = Color(0xFF98A2B3)
                    )
                },
                shape = RoundedCornerShape(18.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color(0xFFE4E7EC),
                    unfocusedIndicatorColor = Color(0xFFE4E7EC),
                    disabledIndicatorColor = Color(0xFFE4E7EC)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            MovementFilters(
                selectedFilter = selectedFilter,
                onFilterChange = onFilterChange
            )
            Spacer(modifier = Modifier.height(18.dp))
            if (isLoading) {
                TimelineLoadingState(modifier = Modifier.fillMaxSize())
            } else {
                MovementTimeline(
                    groups = groups,
                    hasMoreItems = hasMoreItems,
                    onRequestMore = onRequestMore,
                    onItemClick = onItemClick
                )
            }
        }
    }
}

@Composable
fun MovementFilters(
    selectedFilter: MovementQuickFilter,
    onFilterChange: (MovementQuickFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(end = 4.dp)
    ) {
        items(
            count = MovementQuickFilter.entries.size,
            key = { index -> MovementQuickFilter.entries[index].name }
        ) { index ->
            val filter = MovementQuickFilter.entries[index]
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterChange(filter) },
                label = {
                    Text(
                        text = filter.label,
                        fontWeight = if (selectedFilter == filter) FontWeight.SemiBold else FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFEAF8F0),
                    selectedLabelColor = Color(0xFF12A150),
                    containerColor = Color.White,
                    labelColor = Color(0xFF344054)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == filter,
                    borderColor = if (selectedFilter == filter) Color(0xFFB7E4C8) else Color(0xFFE4E7EC),
                    selectedBorderColor = Color(0xFFB7E4C8)
                )
            )
        }
    }
}

@Composable
fun MovementTimeline(
    groups: List<MovementTimelineGroup>,
    hasMoreItems: Boolean,
    onRequestMore: () -> Unit,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val totalTimelineItems = remember(groups) {
        groups.sumOf { it.items.size } + groups.size
    }

    LaunchedEffect(listState, totalTimelineItems, hasMoreItems) {
        observeLoadMore(
            listState = listState,
            totalItems = totalTimelineItems,
            hasMoreItems = hasMoreItems,
            onRequestMore = onRequestMore
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        groups.forEach { group ->
            item(key = "header_${group.title}") {
                Text(
                    text = group.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF101828)
                    )
                )
            }
            itemsIndexed(
                items = group.items,
                key = { _, item -> item.id },
                contentType = { _, _ -> "movement_item" }
            ) { index, item ->
                MovementTimelineItem(
                    item = item,
                    showConnector = index != group.items.lastIndex,
                    onClick = { onItemClick(item.id) }
                )
            }
        }
    }
}

private suspend fun observeLoadMore(
    listState: LazyListState,
    totalItems: Int,
    hasMoreItems: Boolean,
    onRequestMore: () -> Unit
) {
    snapshotFlow {
        listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
    }
        .map { lastVisibleIndex ->
            hasMoreItems && totalItems > 0 && lastVisibleIndex >= totalItems - 5
        }
        .distinctUntilChanged()
        .filter { it }
        .collect { onRequestMore() }
}

@Composable
fun MovementTimelineItem(
    item: InventoryMovementTimelineUi,
    showConnector: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.width(46.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(item.type.accent.copy(alpha = 0.14f))
                    .border(1.dp, item.type.accent.copy(alpha = 0.22f), CircleShape)
            )
            if (showConnector) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .width(2.dp)
                        .height(88.dp)
                        .background(Color(0xFFE4E7EC))
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp)
                .padding(top = 2.dp, bottom = if (showConnector) 8.dp else 0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.typeTitle,
                        color = item.type.accent,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.productName,
                        color = Color(0xFF101828),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = item.timeLabel,
                            color = Color(0xFF667085),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.actorLabel,
                            color = Color(0xFF667085),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.ic_history_chevron_right),
                        contentDescription = null,
                        tint = Color(0xFF98A2B3),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.quantityLine,
                color = if (item.type == MovementTimelineType.EXIT || item.type == MovementTimelineType.WASTE) item.type.accent else Color(0xFF101828),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            item.lotLine?.takeIf { it.isNotBlank() }?.let { line ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = line,
                    color = Color(0xFF667085),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            item.noteLine?.takeIf { it.isNotBlank() }?.let { line ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = line,
                    color = Color(0xFF667085),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0F2F5))
        }
    }
}

@Composable
fun MovementDetailScreen(
    movement: InventoryMovementTimelineUi,
    onBack: () -> Unit,
    onOpenProduct: () -> Unit,
    modifier: Modifier = Modifier,
    historyBackIcon: Painter = painterResource(id = R.drawable.baseline_arrow_back_24)
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = historyBackIcon,
                        contentDescription = "Volver",
                        tint = Color(0xFF101828)
                    )
                }
                Text(
                    text = "Detalle del movimiento",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = Color(0xFF101828)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFEAECF0), RoundedCornerShape(24.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(movement.type.accent.copy(alpha = 0.14f))
                                .border(1.dp, movement.type.accent.copy(alpha = 0.22f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = movement.typeTitle,
                                color = movement.type.accent,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = movement.productName,
                                color = Color(0xFF101828),
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = movement.quantityLine,
                                color = Color(0xFF344054),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }

            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFEAECF0), RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                        MovementDetailRow("Cantidad", movement.quantityDetail, movement.type.accent)
                        MovementDetailRow("Presentación", movement.presentationLabel)
                        movement.lotNumber?.let { MovementDetailRow("Lote", it) }
                        movement.expiration?.let { MovementDetailRow("Vencimiento", it) }
                        movement.reason?.takeIf { it.isNotBlank() }?.let { MovementDetailRow("Motivo", it) }
                    }
                }
            }

            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFEAECF0), RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                        MovementDetailRow("Usuario", movement.userLabel)
                        MovementDetailRow("Fecha y hora", movement.dateTimeLabel)
                        MovementDetailRow("Origen", movement.originLabel)
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = onOpenProduct,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Ver producto", fontWeight = FontWeight.SemiBold)
                }
            }

            item {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0E8F63))
                ) {
                    Text(
                        text = "Cerrar",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun MovementDetailRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF101828)
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color(0xFF344054),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = value,
                color = valueColor,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        HorizontalDivider(color = Color(0xFFEAECF0))
    }
}

@Composable
private fun TimelineLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        repeat(4) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .width(42.dp)
                        .fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF2F4F7))
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFF2F4F7), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.42f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF2F4F7))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.56f)
                            .height(18.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF2F4F7))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.74f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF2F4F7))
                    )
                }
            }
        }
    }
}

private fun groupMovementsForTimeline(items: List<InventoryMovementTimelineUi>): List<MovementTimelineGroup> {
    val today = java.time.LocalDate.now()
    val yesterday = today.minusDays(1)
    val weekStart = today.minusDays(6)

    val groups = linkedMapOf(
        "Hoy" to mutableListOf<InventoryMovementTimelineUi>(),
        "Ayer" to mutableListOf<InventoryMovementTimelineUi>(),
        "Esta semana" to mutableListOf<InventoryMovementTimelineUi>(),
        "Anteriores" to mutableListOf<InventoryMovementTimelineUi>()
    )

    items.forEach { item ->
        val date = extractMovementDate(item.id)
        when {
            date == today -> groups.getValue("Hoy").add(item)
            date == yesterday -> groups.getValue("Ayer").add(item)
            date != null && date.isAfter(weekStart.minusDays(1)) -> groups.getValue("Esta semana").add(item)
            else -> groups.getValue("Anteriores").add(item)
        }
    }

    return groups.mapNotNull { (title, groupItems) ->
        groupItems.takeIf { it.isNotEmpty() }?.let { MovementTimelineGroup(title, it) }
    }
}

private fun extractMovementDate(id: String): java.time.LocalDate? {
    val raw = id.substringBefore('|')
    return runCatching { java.time.LocalDate.parse(raw) }.getOrNull()
}
