package com.app.administradorfarmadon.ActivityFragmentos.Fragmentos

data class InventarioResumenState(
    val total: Int,
    val bajoStock: Int,
    val porVencer: Int,
    val mostrarStats: Boolean
)

data class InventarioBannerGeneralState(
    val mostrar: Boolean,
    val mensaje: String
)

data class InventarioStockBajoTabletState(
    val mostrar: Boolean,
    val mensaje: String
)

data class InventarioEstadoVacioState(
    val mostrarVacio: Boolean,
    val mostrarLista: Boolean,
    val titulo: String,
    val descripcion: String,
    val mostrarBotonLimpiar: Boolean
)

data class InventarioLoteAlertaItem(
    val nombreProducto: String,
    val numeroLote: String,
    val vencimiento: String,
    val diasRestantes: Int
)

data class InventarioAlertasLotesState(
    val alertas: List<InventarioLoteAlertaItem>,
    val vencidos: Int,
    val proximos: Int,
    val mostrarBanner: Boolean,
    val textoBanner: String
)

data class InventarioFilterCriteria(
    val textoBusqueda: String,
    val filtroActual: String,
    val categoriaSeleccionada: String
)

data class InventarioFilterResult(
    val filtrados: List<com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos>,
    val hayFiltrosActivos: Boolean
)
