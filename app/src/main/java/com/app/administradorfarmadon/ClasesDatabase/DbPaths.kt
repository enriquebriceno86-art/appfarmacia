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
}

