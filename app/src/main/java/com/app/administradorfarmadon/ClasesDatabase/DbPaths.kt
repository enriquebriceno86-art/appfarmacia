package com.app.administradorfarmadon.ClasesDatabase

/**
 * Rutas canónicas de Firebase Realtime Database.
 *
 * Importante:
 * - Centralizamos aquí los nombres de nodos para evitar "strings" repetidos por toda la app.
 * - Si más adelante reorganizamos la estructura, el cambio se hace en un solo lugar.
 */
object DbPaths {
    /**
     * Nodo único para registrar auditoría/movimientos de toda la app.
     *
     * Estructura:
     * - Movimientos/movimientos/{yyyy-MM-dd}/{idMovimiento}
     * - Movimientos/movimientosInventario/{yyyy-MM-dd}/{idMovimiento}
     */
    const val ROOT_MOVIMIENTOS = "Movimientos"

    const val MOVIMIENTOS_GENERAL = "$ROOT_MOVIMIENTOS/movimientos"
    const val MOVIMIENTOS_INVENTARIO = "$ROOT_MOVIMIENTOS/movimientosInventario"

    /**
     * Nodo raíz para el inventario.
     */
    const val ROOT_INVENTARIO = "Inventario"
    const val INVENTARIO_PRODUCTOS = "$ROOT_INVENTARIO/Productos"
    const val INVENTARIO_CATEGORIAS = "$ROOT_INVENTARIO/CategoriasInventario"
    const val INVENTARIO_NOMBRES = "$ROOT_INVENTARIO/NombresProductos"
    const val INVENTARIO_NOMBRES_NORMALIZADOS = "$ROOT_INVENTARIO/NombresProductosNormalizados"
    const val INVENTARIO_BUSQUEDA = "$ROOT_INVENTARIO/BusquedaProductos"
    const val INVENTARIO_PRESENTACIONES = "$ROOT_INVENTARIO/Presentaciones"
    const val INVENTARIO_PRODUCTO_PRESENTACIONES = "$ROOT_INVENTARIO/ProductoPresentaciones"
    const val INVENTARIO_PRODUCTO_LOTES = "$ROOT_INVENTARIO/ProductoLotes"
    const val INVENTARIO_FICHA_TECNICA = "$ROOT_INVENTARIO/ProductoFichaTecnica"
    const val INVENTARIO_CODIGOS = "$ROOT_INVENTARIO/CodigosProductos"
    const val INVENTARIO_MARGENES = "$ROOT_INVENTARIO/ConfiguracionMargenes"
    const val INVENTARIO_DECISIONES = "$ROOT_INVENTARIO/DecisionesCategoria"
    
    /**
     * V26.0: Kardex de Movimientos (Historial de Auditoría)
     * Estructura: Inventario_Movimientos/{productoId}/{movimientoId}
     */
    const val INVENTARIO_MOVIMIENTOS = "Inventario_Movimientos"
    
    /**
     * V27.0: Catálogo de Proveedores
     */
    const val INVENTARIO_PROVEEDORES = "Proveedores"
    const val INVENTARIO_CUENTAS_POR_PAGAR = "CuentasPorPagar"
    const val INVENTARIO_PAGOS_PROVEEDORES = "Historial_Pagos_Proveedores"
    
    /**
     * V22.0: Nodo de Indexación Inversa para Búsqueda Ultra-Rápida (Estilo ElasticSearch)
     * Estructura: Buscador_Indices/{Tipo}/{Token}/{id_producto}: true
     */
    const val ROOT_BUSCADOR_INDICES = "Buscador_Indices"
    const val INDICE_SINTOMAS = "$ROOT_BUSCADOR_INDICES/Sintomas"
    const val INDICE_NOMBRE = "$ROOT_BUSCADOR_INDICES/Nombre"
    const val INDICE_CATEGORIA = "$ROOT_BUSCADOR_INDICES/Categoria"
    const val INDICE_ATRIBUTOS = "$ROOT_BUSCADOR_INDICES/Atributos"
    
    /**
     * V17.46: Índice plano de lotes para búsqueda quirúrgica sin barridos.
     * Estructura: Inventario/LotesPorNumero/{numeroLote}
     */
    const val INVENTARIO_LOTES_POR_NUMERO = "$ROOT_INVENTARIO/LotesPorNumero"
}
