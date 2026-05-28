package com.app.administradorfarmadon.ActivityFragmentos.Fragmentos.logicainventario

import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.toMoldeProductos
import com.app.administradorfarmadon.ClasesDatabase.DbPaths
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para búsquedas proactivas de alto rendimiento.
 * Delega el filtrado pesado a Firebase mediante índices inversos.
 */
object BuscadorSearchRepository {

    private val db = FirebaseDatabase.getInstance()

    /**
     * Realiza una búsqueda multisíntoma (AND logic).
     * Devuelve solo productos que coincidan con TODOS los términos.
     */
    suspend fun buscarPorTerminos(terminos: List<String>): List<MoldeProductos> = coroutineScope {
        if (terminos.isEmpty()) return@coroutineScope emptyList()

        // 1. Descargar en paralelo las listas de IDs para cada término
        val deferidos = terminos.map { termino ->
            async {
                val token = BuscadorIndicesManager.normalizarTexto(termino)
                val idsResult = mutableSetOf<String>()
                
                // Buscar en síntomas, nombres, categorías y lotes
                val refs = listOf(
                    db.getReference("${DbPaths.INDICE_SINTOMAS}/$token"),
                    db.getReference("${DbPaths.INDICE_NOMBRE}/$token"),
                    db.getReference("${DbPaths.INDICE_CATEGORIA}/$token"),
                    db.getReference("${DbPaths.INVENTARIO_LOTES_POR_NUMERO}/$token")
                )
                
                refs.forEach { ref ->
                    val snapshot = ref.get().await()
                    snapshot.children.forEach { child ->
                        child.key?.let { idsResult.add(it) }
                    }
                }
                idsResult
            }
        }

        val resultadosPorTermino = deferidos.awaitAll()

        // 2. Intersección (AND Logic): El ID debe estar presente en TODOS los conjuntos
        val idsFinales = resultadosPorTermino.reduceOrNull { acc, set ->
            acc.intersect(set).toMutableSet()
        } ?: emptySet()

        if (idsFinales.isEmpty()) return@coroutineScope emptyList()

        // 3. Descarga en ráfaga (Batch Fetch) de los productos resultantes (Top 20 por performance)
        val limitados = idsFinales.take(20)
        val productosDeferidos = limitados.map { id ->
            async {
                val prod = db.getReference("${DbPaths.INVENTARIO_PRODUCTOS}/$id").get().await().toMoldeProductos()
                if (prod != null) {
                    val lotesSnap = db.getReference(DbPaths.INVENTARIO_PRODUCTO_LOTES).child(prod.indice).get().await()
                    val lotesMap = mutableMapOf<String, com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteProducto>()
                    lotesSnap.children.forEach { lChild ->
                        lChild.getValue(com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteProducto::class.java)?.let {
                            lotesMap[lChild.key ?: ""] = it
                        }
                    }
                    prod.lotes = lotesMap
                }
                prod
            }
        }

        productosDeferidos.awaitAll().filterNotNull()
    }
}
