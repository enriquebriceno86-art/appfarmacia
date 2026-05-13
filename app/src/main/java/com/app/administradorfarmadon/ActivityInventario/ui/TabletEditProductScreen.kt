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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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

enum class TabletEditSection {
    GENERAL_INFO,
    INVENTORY,
    PRESENTATIONS,
    EXPIRATION_LOTS
}

data class TabletProductSummaryModel(
    val imageUrl: String,
    val imageUri: String?,
    val name: String,
    val category: String,
    val status: String,
    val vendibleChip: String,
    val fisicoChip: String,
    val vence: String,
    val stockMinimo: String
)

data class TabletSectionListItemModel(
    val section: TabletEditSection,
    val title: String,
    val subtitle: String,
    val iconRes: Int
)

sealed interface TabletLotsPanelState {
    data object Hidden : TabletLotsPanelState

    data class List(
        val productName: String,
        val unitBase: String,
        val totalAvailable: String,
        val searchQuery: String,
        val items: kotlin.collections.List<LotListUiItem>
    ) : TabletLotsPanelState

    data class Detail(
        val model: LotDetailUiModel
    ) : TabletLotsPanelState

    data class Form(
        val model: LotFormUiModel
    ) : TabletLotsPanelState

    data class Consume(
        val model: ConsumeLotUiModel
    ) : TabletLotsPanelState
}

@Composable
fun TabletEditProductScreen(
    summary: TabletProductSummaryModel,
    sectionItems: List<TabletSectionListItemModel>,
    generalInfoModel: GeneralInfoEditorUiModel,
    inventoryModel: InventoryEditorUiModel,
    presentationsModel: PresentacionesEditorUiModel,
    otherDetailsModel: OtherDetailsUiModel,
    lotsPanelState: TabletLotsPanelState,
    refreshKey: Int,
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
    var selectedSection by rememberSaveable { mutableStateOf(TabletEditSection.GENERAL_INFO) }
    val seccionesDisponibles = sectionItems.map { it.section }.toSet()

    LaunchedEffect(seccionesDisponibles) {
        if (selectedSection !in seccionesDisponibles) {
            selectedSection = TabletEditSection.GENERAL_INFO
        }
    }

    LaunchedEffect(selectedSection) {
        onSectionSelected(selectedSection)
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7F6))
            .padding(horizontal = 28.dp, vertical = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.35f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ProductSummaryPanel(summary = summary)
            EditSectionList(
                items = sectionItems,
                selectedSection = selectedSection,
                onSectionClick = { selectedSection = it }
            )
        }

        EditFormPanel(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.65f)
        ) {
            when (selectedSection) {
                TabletEditSection.GENERAL_INFO -> GeneralInfoForm(
                    model = generalInfoModel,
                    refreshKey = refreshKey,
                    onClose = { onCloseSection(TabletEditSection.GENERAL_INFO) },
                    onSave = onSaveGeneral
                )

                TabletEditSection.INVENTORY -> InventoryForm(
                    model = inventoryModel,
                    refreshKey = refreshKey,
                    onClose = { onCloseSection(TabletEditSection.INVENTORY) },
                    onSave = onSaveInventory
                )

                TabletEditSection.PRESENTATIONS -> PresentationsForm(
                    model = presentationsModel,
                    refreshKey = refreshKey,
                    onClose = { onCloseSection(TabletEditSection.PRESENTATIONS) },
                    onAdd = onAddPresentation,
                    onEdit = onEditPresentation,
                    onDelete = onDeletePresentation,
                    onSave = onSavePresentations
                )

                TabletEditSection.EXPIRATION_LOTS -> ExpirationLotsForm(
                    model = otherDetailsModel,
                    lotsPanelState = lotsPanelState,
                    refreshKey = refreshKey,
                    onClose = { onCloseSection(TabletEditSection.EXPIRATION_LOTS) },
                    onManageLots = onManageLots,
                    onSave = onSaveOtherDetails,
                    onLotBack = onLotBack,
                    onLotSearchChange = onLotSearchChange,
                    onLotItemClick = onLotItemClick,
                    onLotEdit = onLotEdit,
                    onLotManualConsume = onLotManualConsume,
                    onLotFormSave = onLotFormSave,
                    onLotConsumeSave = onLotConsumeSave
                )
            }
        }
    }
}

@Composable
fun ProductSummaryPanel(
    summary: TabletProductSummaryModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(26.dp))
            .border(1.dp, Color(0xFFE3ECE8), RoundedCornerShape(26.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            ProductImage(
                imageUrl = summary.imageUrl,
                imageUri = summary.imageUri,
                productName = summary.name,
                category = summary.category,
                modifier = Modifier.size(96.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = summary.name,
                    color = Color(0xFF0F172A),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${summary.category}  ·  ${summary.status}",
                    color = Color(0xFF667085),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryChip(summary.vendibleChip)
                    SummaryChip(summary.fisicoChip)
                }
            }
        }

        HorizontalDivider(color = Color(0xFFEAECEF))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Vencimiento",
                    color = Color(0xFF98A2B3),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = summary.vence,
                    color = Color(0xFF475467),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Stock mínimo",
                    color = Color(0xFF98A2B3),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = summary.stockMinimo,
                    color = Color(0xFF475467),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ProductImage(
    imageUrl: String,
    imageUri: String?,
    productName: String,
    category: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
        },
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF4F7F6)),
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

@Composable
private fun SummaryChip(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFEFF8F2), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF14935C),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EditSectionList(
    items: List<TabletSectionListItemModel>,
    selectedSection: TabletEditSection,
    onSectionClick: (TabletEditSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Secciones",
            color = Color(0xFF0F172A),
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold
        )

        items.forEach { item ->
            SectionListItem(
                item = item,
                selected = item.section == selectedSection,
                onClick = { onSectionClick(item.section) }
            )
        }
    }
}

@Composable
fun SectionListItem(
    item: TabletSectionListItemModel,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (selected) Color(0xFFF5FBF8) else Color.White,
                shape = RoundedCornerShape(22.dp)
            )
            .border(
                width = 1.dp,
                color = if (selected) Color(0xFFA8DABF) else Color(0xFFE3ECE8),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(Color(0xFFEFF8F2), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                painter = painterResource(id = item.iconRes),
                contentDescription = null,
                tint = Color(0xFF14935C),
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.title,
                color = Color(0xFF0F172A),
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = item.subtitle,
                color = Color(0xFF667085),
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = "›",
            color = if (selected) Color(0xFF14935C) else Color(0xFF98A2B3),
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EditFormPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(26.dp))
            .border(1.dp, Color(0xFFE3ECE8), RoundedCornerShape(26.dp))
    ) {
        content()
    }
}

@Composable
fun GeneralInfoForm(
    model: GeneralInfoEditorUiModel,
    refreshKey: Int,
    onClose: () -> Unit,
    onSave: (GeneralInfoSubmit) -> Unit
) {
    GeneralInfoEditSheet(
        model = model,
        backIcon = painterResource(id = R.drawable.baseline_arrow_back_24),
        onBack = {},
        onClose = onClose,
        onSave = onSave,
        modifier = Modifier.fillMaxSize(),
        showBackButton = false,
        stateKey = refreshKey
    )
}

@Composable
fun InventoryForm(
    model: InventoryEditorUiModel,
    refreshKey: Int,
    onClose: () -> Unit,
    onSave: (InventoryEditSubmit) -> Unit
) {
    InventoryEditSheet(
        model = model,
        inventoryIcon = painterResource(id = R.drawable.ic_section_inventory_management),
        backIcon = painterResource(id = R.drawable.baseline_arrow_back_24),
        onBack = {},
        onClose = onClose,
        onSave = onSave,
        modifier = Modifier.fillMaxSize(),
        showBackButton = false,
        stateKey = refreshKey
    )
}

@Composable
fun PresentationsForm(
    model: PresentacionesEditorUiModel,
    refreshKey: Int,
    onClose: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    onSave: (PresentacionesSubmit) -> Unit
) {
    PresentacionesEditSheet(
        model = model,
        backIcon = painterResource(id = R.drawable.baseline_arrow_back_24),
        onBack = {},
        onAdd = onAdd,
        onEdit = onEdit,
        onDelete = onDelete,
        onClose = onClose,
        onSave = onSave,
        modifier = Modifier.fillMaxSize(),
        showBackButton = false,
        stateKey = refreshKey
    )
}

@Composable
fun ExpirationLotsForm(
    model: OtherDetailsUiModel,
    lotsPanelState: TabletLotsPanelState,
    refreshKey: Int,
    onClose: () -> Unit,
    onManageLots: () -> Unit,
    onSave: (OtherDetailsSubmit) -> Unit,
    onLotBack: () -> Unit,
    onLotSearchChange: (String) -> Unit,
    onLotItemClick: (String) -> Unit,
    onLotEdit: () -> Unit,
    onLotManualConsume: () -> Unit,
    onLotFormSave: (LotFormSubmit) -> Boolean,
    onLotConsumeSave: (ConsumeLotSubmit) -> Boolean
) {
    when (lotsPanelState) {
        is TabletLotsPanelState.Hidden -> OtherDetailsSheet(
            model = model,
            backIcon = painterResource(id = R.drawable.baseline_arrow_back_24),
            onBack = {},
            onManageLots = onManageLots,
            onClose = onClose,
            onSave = onSave,
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
