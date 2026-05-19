package com.app.administradorfarmadon.ActivityInventario.bulk

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteProducto
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.PresentacionProducto
import com.app.administradorfarmadon.ActivityInventario.NombreProductos
import com.app.administradorfarmadon.ActivityInventario.ProductUtils
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductControlType
import com.app.administradorfarmadon.ClasesDatabase.DbPaths
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BulkImportViewModel : ViewModel() {

    var uiState by mutableStateOf(BulkImportUiState())
        private set

    /**
     * Procesa el archivo seleccionado por el usuario.
     */
    fun processSelectedFile(context: Context, uri: Uri, fileName: String) {
        uiState = uiState.copy(isLoading = true, fileName = fileName)
        
        viewModelScope.launch {
            val initialDrafts = withContext(Dispatchers.IO) {
                BulkImportCsvParser.parse(context, uri)
            }
            
            // Verificación de duplicados antes de la IA
            val checkedDrafts = checkDuplicates(initialDrafts)
            
            uiState = uiState.copy(drafts = checkedDrafts, summary = calculateSummary(checkedDrafts))

            // Enriquecimiento con IA por lotes de 15
            val enrichedDrafts = mutableListOf<ImportDraftProduct>()
            checkedDrafts.chunked(15).forEach { batch ->
                val enrichedBatch = BulkImportAiRepository.enrichDrafts(batch)
                enrichedDrafts.addAll(enrichedBatch)
                
                // Actualizamos la UI progresivamente
                val currentDrafts = enrichedDrafts + checkedDrafts.drop(enrichedDrafts.size)
                uiState = uiState.copy(
                    drafts = currentDrafts,
                    summary = calculateSummary(currentDrafts)
                )
            }
            
            uiState = uiState.copy(
                isLoading = false,
                currentStep = BulkImportStep.VALIDATION_ROOM
            )
        }
    }

    private suspend fun checkDuplicates(drafts: List<ImportDraftProduct>): List<ImportDraftProduct> {
        return try {
            val db = FirebaseDatabase.getInstance().reference.child(DbPaths.INVENTARIO_NOMBRES)
            val snapshot = db.get().await()
            val existingNormalizedKeys = snapshot.children.mapNotNull { it.key }.toSet()
            
            drafts.map { draft ->
                val normalized = ProductUtils.normalizarNombreContrato(draft.name)
                val key = ProductUtils.generarKeyNombreContrato(normalized)
                
                if (existingNormalizedKeys.contains(key)) {
                    draft.copy(
                        isAlreadyInDatabase = true,
                        validationState = ImportValidationState.WARNING,
                        warnings = draft.warnings + "Este producto ya existe en el inventario"
                    )
                } else draft
            }
        } catch (e: Exception) {
            drafts
        }
    }

    /**
     * Alterna la selección de un borrador para incluirlo o no en la importación.
     */
    fun toggleDraftSelection(draftId: String) {
        val updatedDrafts = uiState.drafts.map {
            if (it.id == draftId) it.copy(isSelected = !it.isSelected) else it
        }
        uiState = uiState.copy(
            drafts = updatedDrafts,
            summary = calculateSummary(updatedDrafts)
        )
    }

    /**
     * Filtra la lista por estado de validación.
     */
    fun filterByState(state: ImportValidationState?) {
        uiState = uiState.copy(filterState = state)
    }

    /**
     * Ejecuta el guardado masivo en Firebase Realtime Database.
     */
    fun executeBulkImport() {
        val toImport = uiState.drafts.filter { it.isSelected && it.validationState != ImportValidationState.ERROR }
        if (toImport.isEmpty()) return

        uiState = uiState.copy(isLoading = true, currentStep = BulkImportStep.PROCESSING)

        viewModelScope.launch {
            try {
                val db = FirebaseDatabase.getInstance().reference
                
                // Agrupamos todas las actualizaciones en un solo mapa
                val updates = mutableMapOf<String, Any?>()
                
                toImport.forEach { draft ->
                    val productKey = db.child(DbPaths.INVENTARIO_PRODUCTOS).push().key ?: return@forEach
                    val product = mapDraftToMolde(draft, productKey)
                    
                    // 1. Ruta principal del producto
                    updates["${DbPaths.INVENTARIO_PRODUCTOS}/$productKey"] = product
                    
                    // 2. Registro de nombre (Paridad total con contrato de producción)
                    val normalized = ProductUtils.normalizarNombreContrato(draft.name)
                    val key = ProductUtils.generarKeyNombreContrato(normalized)
                    
                    if (key.isNotBlank()) {
                        val registroNombre = NombreProductos(
                            id = key,
                            nombre = draft.name,
                            indice = productKey,
                            normalizedName = normalized
                        )
                        updates["${DbPaths.INVENTARIO_NOMBRES}/$key"] = registroNombre
                        updates["${DbPaths.INVENTARIO_NOMBRES_NORMALIZADOS}/$key"] = productKey
                    }

                    // 3. Índice plano de lotes (V17.46)
                    val loteEntry = product.lotes.entries.firstOrNull()
                    val loteId = loteEntry?.key.orEmpty()
                    val numLote = loteEntry?.value?.numero?.trim()?.uppercase() ?: ""
                    if (numLote.isNotBlank() && loteId.isNotBlank()) {
                        val keyLote = ProductUtils.encodeLotKey(numLote)
                        val registroLote = com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteIndexado(
                            numero = numLote,
                            productoId = productKey,
                            productoNombre = product.nombre,
                            loteId = loteId,
                            lotePath = "${DbPaths.INVENTARIO_PRODUCTO_LOTES}/$productKey/$loteId",
                            vencimiento = product.vencimiento
                        )
                        updates["${DbPaths.INVENTARIO_LOTES_POR_NUMERO}/$keyLote"] = registroLote
                    }
                }

                if (updates.isNotEmpty()) {
                    db.updateChildren(updates).await()
                }

                uiState = uiState.copy(
                    isLoading = false,
                    currentStep = BulkImportStep.SUCCESS_SUMMARY,
                    summary = uiState.summary.copy(importedCount = toImport.size)
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Error al guardar: ${e.message}"
                )
            }
        }
    }

    private fun calculateSummary(drafts: List<ImportDraftProduct>): BulkImportSummary {
        return BulkImportSummary(
            totalRows = drafts.size,
            readyToImport = drafts.count { it.validationState == ImportValidationState.READY && it.isSelected },
            withWarnings = drafts.count { it.validationState == ImportValidationState.WARNING },
            withErrors = drafts.count { it.validationState == ImportValidationState.ERROR },
            duplicates = drafts.count { it.isAlreadyInDatabase }
        )
    }

    /**
     * Mapea el borrador al modelo real de la base de datos.
     * Aquí es donde se aplican las reglas de negocio finales.
     */
    private fun mapDraftToMolde(draft: ImportDraftProduct, key: String): MoldeProductos {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        // Creamos el lote inicial
        val primerLote = LoteProducto(
            numero = draft.lotNumber.ifBlank { "LOTE-INICIAL" },
            vencimiento = draft.expirationDate,
            cantidad = draft.initialStock,
            fecha = today,
            costoCompraUnitario = draft.purchaseCost
        )

        return MoldeProductos(
            nombre = draft.name,
            indice = key,
            categoria = draft.category,
            preciodecompra = draft.purchaseCost.toString(),
            cantidadinicial = draft.initialStock.toInt().toString(),
            stockminimo = "5",
            vencimiento = draft.expirationDate,
            estadodelproducto = true,
            unidadbase = when(draft.controlType) {
                CreateProductControlType.PESO -> "g"
                CreateProductControlType.LIQUIDO -> "mL"
                else -> "unidad"
            },
            tipoBaseInventario = when(draft.controlType) {
                CreateProductControlType.PESO -> "PESO"
                CreateProductControlType.LIQUIDO -> "VOLUMEN"
                else -> "UNIDADES"
            },
            requierereceta = draft.requiresPrescription,
            referenceKeywords = draft.keywords,
            lotes = mapOf(primerLote.numero to primerLote),
            loteConsumoSeleccionado = primerLote.numero
        )
    }
}
