package com.app.administradorfarmadon.ActivityInventario.agregarstock

import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos

enum class ModoIngresoStock {
    UNIDADES_SUELTAS,
    CAJA_ENVASES,
    CAJA_PAQUETES
}

data class AgregarStockProductoState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    
    // Producto
    val productoOriginal: MoldeProductos? = null,
    val tipoControl: String = "", // UNIDAD, PESO, LIQUIDO
    val unidadBase: String = "unidades",
    val contenidoPorEnvase: Double = 1.0,
    
    // Configuración inicial física (si el producto es ambiguo)
    val necesitaConfigurarIdentidadFisica: Boolean = false,
    val pendingContenidoPorEnvaseTexto: String = "",
    val pendingUnidadBase: String = "",
    val pendingTipoControl: String = "",
    
    // Modo de ingreso
    val modoIngreso: ModoIngresoStock = ModoIngresoStock.UNIDADES_SUELTAS,
    
    // Inputs (cantidades)
    val unidadesSueltasText: String = "",
    val cajasRecibidasText: String = "",
    val envasesPorCajaText: String = "",
    val paquetesPorCajaText: String = "",
    val envasesPorPaqueteText: String = "",
    val unidadesPorCajaAncladas: Boolean = false,
    
    // Inputs (Lote)
    val loteNumero: String = "",
    val loteVencimiento: String = "",
    val proveedorId: String = "",
    val proveedorNombre: String = "",
    val suppliers: List<com.app.administradorfarmadon.ActivityInventario.ClasesProductos.Proveedor> = emptyList(),
    val showSupplierDialog: Boolean = false,
    val numeroFactura: String = "",
    val costoCompra: String = "",
    
    // Descuadre (Auditoría)
    val reconciliationHasMismatch: Boolean = false,
    val reconciliationReason: String = "",
    val mismatchJustified: Boolean = false,
    
    // Cálculo en vivo
    val totalBaseCalculado: Double = 0.0,
    val isFormValid: Boolean = false,

    // Validación de lote
    val isCheckingLot: Boolean = false,
    val lotValidatedFor: String = "",
    val lotConflictMessage: String? = null,
    val lotConflictSeverity: Int = 0, // 0=libre, 1=existe-mismo-producto, 2=existe-otro-producto
    val lotWillMerge: Boolean = false
)
