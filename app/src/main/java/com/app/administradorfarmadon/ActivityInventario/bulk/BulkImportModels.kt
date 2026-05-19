package com.app.administradorfarmadon.ActivityInventario.bulk

import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductPresentation
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductControlType
import java.util.UUID

/**
 * Representa el estado de una fila durante el proceso de importación masiva.
 */
enum class ImportValidationState {
    READY,      // Listo para guardar
    WARNING,    // Falta algún dato no crítico (ej. categoría)
    ERROR       // Error crítico (ej. falta nombre o costo inválido)
}

/**
 * Borrador de un producto extraído del CSV.
 * Mantiene los datos crudos y los datos ya parseados/validados.
 */
data class ImportDraftProduct(
    val id: String = UUID.randomUUID().toString(),
    
    // --- DATOS CRUDOS (Lo que se leyó tal cual del archivo) ---
    val rawName: String = "",
    val rawStock: String = "",
    val rawPurchaseCost: String = "",
    val rawSalePrice: String = "",
    val rawLotNumber: String = "",
    val rawExpirationDate: String = "",
    val rawCategory: String = "",

    // --- DATOS PROCESADOS (Listos para MoldeProductos) ---
    val name: String = "",
    val category: String = "Sin categoría",
    val controlType: CreateProductControlType = CreateProductControlType.UNIDAD,
    val purchaseCost: Double = 0.0,
    val initialStock: Double = 0.0,
    val lotNumber: String = "",
    val expirationDate: String = "", // Formato MM/AA
    val presentations: List<CreateProductPresentation> = emptyList(),
    val requiresPrescription: Boolean = false,
    val keywords: List<String> = emptyList(),

    // --- ESTADO DE IMPORTACIÓN ---
    val validationState: ImportValidationState = ImportValidationState.READY,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val isSelected: Boolean = true,
    val isAlreadyInDatabase: Boolean = false
)

/**
 * Resumen final de la operación de importación.
 */
data class BulkImportSummary(
    val totalRows: Int = 0,
    val readyToImport: Int = 0,
    val withWarnings: Int = 0,
    val withErrors: Int = 0,
    val duplicates: Int = 0,
    val importedCount: Int = 0
)

/**
 * Representa el estado global de la UI de Importación Masiva.
 */
data class BulkImportUiState(
    val isLoading: Boolean = false,
    val drafts: List<ImportDraftProduct> = emptyList(),
    val summary: BulkImportSummary = BulkImportSummary(),
    val fileName: String? = null,
    val filterState: ImportValidationState? = null, // null = Mostrar todos
    val currentStep: BulkImportStep = BulkImportStep.SELECT_FILE,
    val errorMessage: String? = null
)

enum class BulkImportStep {
    SELECT_FILE,
    VALIDATION_ROOM,
    PROCESSING,
    SUCCESS_SUMMARY
}
