package com.app.administradorfarmadon.ActivityInventario.ui

import android.net.Uri
import android.widget.ImageView
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.app.administradorfarmadon.ActivityInventario.ProductAvatarHelper
import com.app.administradorfarmadon.R

@Composable
fun MobileEditProductScreen(
    summary: TabletProductSummaryModel,
    sectionItems: List<TabletSectionListItemModel>,
    showQuickActions: Boolean,
    generalInfoModel: GeneralInfoEditorUiModel,
    inventoryModel: InventoryEditorUiModel,
    presentationsModel: PresentacionesEditorUiModel,
    otherDetailsModel: OtherDetailsUiModel,
    lotsPanelState: TabletLotsPanelState,
    refreshKey: Int,
    onPickImage: () -> Unit,
    onAdjustStock: () -> Unit,
    onIngresoStock: () -> Unit,
    onEgresoStock: () -> Unit,
    onSectionVisibilityChanged: (Boolean) -> Unit,
    onSectionSelected: (TabletEditSection) -> Unit,
    onCloseSection: (TabletEditSection) -> Unit,
    onSaveGeneral: (GeneralInfoSubmit) -> Unit,
    onSaveInventory: (InventoryEditSubmit) -> Unit,
    onAddPresentation: () -> Unit,
    onEditPresentation: (String) -> Unit,
    onDeletePresentation: (String) -> Unit,
    onSavePresentations: (PresentacionesSubmit) -> Unit,
    onManageLots: () -> Unit,
    onSaveOtherDetails: (OtherDetailsSubmit) -> Unit,
    onLotBack: () -> Unit,
    onLotSearchChange: (String) -> Unit,
    onLotItemClick: (String) -> Unit,
    onLotEdit: () -> Unit,
    onLotManualConsume: () -> Unit,
    onLotFormSave: (LotFormSubmit) -> Boolean,
    onLotConsumeSave: (ConsumeLotSubmit) -> Boolean,
    modifier: Modifier = Modifier
) {
    var currentSection by rememberSaveable {
        mutableStateOf<TabletEditSection?>(null)
    }
    val seccionesDisponibles = sectionItems.map { it.section }.toSet()

    LaunchedEffect(seccionesDisponibles) {
        currentSection?.let { sec ->
            if (sec !in seccionesDisponibles) currentSection = null
        }
    }

    LaunchedEffect(currentSection) {
        onSectionVisibilityChanged(currentSection != null)
        currentSection?.let { onSectionSelected(it) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7F6))
    ) {
        if (currentSection == null) {
            MobileHomeContent(
                summary = summary,
                sectionItems = sectionItems,
                showQuickActions = showQuickActions,
                onPickImage = onPickImage,
                onAdjustStock = onAdjustStock,
                onIngresoStock = onIngresoStock,
                onEgresoStock = onEgresoStock,
                onSectionClick = { currentSection = it }
            )
        } else {
            val section = currentSection!!

            BackHandler(enabled = true) {
                if (section == TabletEditSection.EXPIRATION_LOTS &&
                    lotsPanelState !is TabletLotsPanelState.Hidden
                ) {
                    onLotBack()
                } else {
                    onCloseSection(section)
                    currentSection = null
                }
            }

            val closeAndGoHome = {
                onCloseSection(section)
                currentSection = null
            }

            when (section) {
                TabletEditSection.GENERAL_INFO -> GeneralInfoEditSheet(
                    model = generalInfoModel,
                    backIcon = painterResource(id = R.drawable.baseline_arrow_back_24),
                    onBack = closeAndGoHome,
                    onClose = closeAndGoHome,
                    onSave = onSaveGeneral,
                    modifier = Modifier.fillMaxSize(),
                    showBackButton = false,
                    stateKey = refreshKey
                )

                TabletEditSection.INVENTORY -> InventoryEditSheet(
                    model = inventoryModel,
                    inventoryIcon = painterResource(id = R.drawable.ic_section_inventory_management),
                    backIcon = painterResource(id = R.drawable.baseline_arrow_back_24),
                    onBack = closeAndGoHome,
                    onClose = closeAndGoHome,
                    onSave = onSaveInventory,
                    modifier = Modifier.fillMaxSize(),
                    showBackButton = false,
                    stateKey = refreshKey
                )

                TabletEditSection.PRESENTATIONS -> PresentacionesEditSheet(
                    model = presentationsModel,
                    backIcon = painterResource(id = R.drawable.baseline_arrow_back_24),
                    onBack = closeAndGoHome,
                    onAdd = onAddPresentation,
                    onEdit = onEditPresentation,
                    onDelete = onDeletePresentation,
                    onClose = closeAndGoHome,
                    onSave = onSavePresentations,
                    modifier = Modifier.fillMaxSize(),
                    showBackButton = false,
                    stateKey = refreshKey
                )

                TabletEditSection.EXPIRATION_LOTS -> when (lotsPanelState) {
                    is TabletLotsPanelState.Hidden -> OtherDetailsSheet(
                        model = otherDetailsModel,
                        backIcon = painterResource(id = R.drawable.baseline_arrow_back_24),
                        onBack = closeAndGoHome,
                        onManageLots = onManageLots,
                        onClose = closeAndGoHome,
                        onSave = onSaveOtherDetails,
                        modifier = Modifier.fillMaxSize(),
                        showBackButton = false,
                        stateKey = refreshKey
                    )

                    is TabletLotsPanelState.List -> LotesListSheet(
                        productName = lotsPanelState.productName,
                        unitBase = lotsPanelState.unitBase,
                        totalAvailable = lotsPanelState.totalAvailable,
                        searchQuery = lotsPanelState.searchQuery,
                        items = lotsPanelState.items,
                        backIcon = painterResource(id = R.drawable.baseline_arrow_back_24),
                        onBack = onLotBack,
                        onSearchChange = onLotSearchChange,
                        onItemClick = onLotItemClick,
                        modifier = Modifier.fillMaxSize()
                    )

                    is TabletLotsPanelState.Detail -> LoteDetailSheet(
                        model = lotsPanelState.model,
                        backIcon = painterResource(id = R.drawable.baseline_arrow_back_24),
                        onBack = onLotBack,
                        onEdit = onLotEdit,
                        onManualConsume = onLotManualConsume,
                        modifier = Modifier.fillMaxSize()
                    )

                    is TabletLotsPanelState.Form -> LoteFormSheet(
                        model = lotsPanelState.model,
                        backIcon = painterResource(id = R.drawable.baseline_arrow_back_24),
                        onBack = onLotBack,
                        onSave = onLotFormSave,
                        modifier = Modifier.fillMaxSize()
                    )

                    is TabletLotsPanelState.Consume -> ConsumeLotSheet(
                        model = lotsPanelState.model,
                        backIcon = painterResource(id = R.drawable.baseline_arrow_back_24),
                        onBack = onLotBack,
                        onSave = onLotConsumeSave,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun MobileHomeContent(
    summary: TabletProductSummaryModel,
    sectionItems: List<TabletSectionListItemModel>,
    showQuickActions: Boolean,
    onPickImage: () -> Unit,
    onAdjustStock: () -> Unit,
    onIngresoStock: () -> Unit,
    onEgresoStock: () -> Unit,
    onSectionClick: (TabletEditSection) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MobileSummaryCard(summary = summary, onPickImage = onPickImage)

        if (showQuickActions) {
            MobileQuickActionsCard(
                onAdjustStock = onAdjustStock,
                onIngresoStock = onIngresoStock,
                onEgresoStock = onEgresoStock
            )
        }

        MobileSectionList(
            items = sectionItems,
            onSectionClick = onSectionClick
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun MobileSummaryCard(
    summary: TabletProductSummaryModel,
    onPickImage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(22.dp))
            .border(1.dp, Color(0xFFE3ECE8), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            MobileProductImage(
                imageUrl = summary.imageUrl,
                imageUri = summary.imageUri,
                productName = summary.name,
                category = summary.category,
                onClick = onPickImage,
                modifier = Modifier.size(84.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = summary.name,
                    color = Color(0xFF0F172A),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${summary.category}  ·  ${summary.status}",
                    color = Color(0xFF667085),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MobileSummaryChip(summary.vendibleChip)
                    MobileSummaryChip(summary.fisicoChip)
                }
            }
        }

        HorizontalDivider(color = Color(0xFFEAECEF))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Vencimiento",
                    color = Color(0xFF98A2B3),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = summary.vence,
                    color = Color(0xFF475467),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Stock mínimo",
                    color = Color(0xFF98A2B3),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = summary.stockMinimo,
                    color = Color(0xFF475467),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MobileProductImage(
    imageUrl: String,
    imageUri: String?,
    productName: String,
    category: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF4F7F6))
            .clickable(onClick = onClick)
    ) {
        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { imageView ->
                ProductAvatarHelper.loadInto(
                    imageView = imageView,
                    imageUrl = imageUrl,
                    productName = productName,
                    category = category,
                    imageUri = imageUri?.takeIf { it.isNotBlank() }?.let(Uri::parse)
                )
            }
        )
    }
}

@Composable
private fun MobileSummaryChip(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFEFF8F2), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF14935C),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun MobileQuickActionsCard(
    onAdjustStock: () -> Unit,
    onIngresoStock: () -> Unit,
    onEgresoStock: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(22.dp))
            .border(1.dp, Color(0xFFE3ECE8), RoundedCornerShape(22.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Acciones rápidas",
            color = Color(0xFF0F172A),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton(
                label = "Ajustar",
                modifier = Modifier.weight(1f),
                onClick = onAdjustStock
            )
            QuickActionButton(
                label = "Ingresar",
                modifier = Modifier.weight(1f),
                onClick = onIngresoStock
            )
            QuickActionButton(
                label = "Egreso",
                modifier = Modifier.weight(1f),
                onClick = onEgresoStock
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEFF8F2))
            .border(1.dp, Color(0xFFA8DABF), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color(0xFF14935C),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun MobileSectionList(
    items: List<TabletSectionListItemModel>,
    onSectionClick: (TabletEditSection) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Secciones",
            color = Color(0xFF0F172A),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
        )

        items.forEach { item ->
            MobileSectionItem(
                item = item,
                onClick = { onSectionClick(item.section) }
            )
        }
    }
}

@Composable
private fun MobileSectionItem(
    item: TabletSectionListItemModel,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFE3ECE8), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFFEFF8F2), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = item.iconRes),
                contentDescription = null,
                tint = Color(0xFF14935C),
                modifier = Modifier.size(22.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.title,
                color = Color(0xFF0F172A),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.subtitle,
                color = Color(0xFF667085),
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = "›",
            color = Color(0xFF98A2B3),
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
