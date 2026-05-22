package com.app.administradorfarmadon.ActivityFragmentos.Fragmentos.logicainventario

import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ClasesDatabase.DbPaths
import java.text.Normalizer
import java.util.Locale

/**
 * Encargado de la lógica de indexación inversa para el buscador.
 * Convierte un producto en una serie de "punteros" en Firebase para búsqueda instantánea.
 */
object BuscadorIndicesManager {

    /**
     * Prepara el mapa de actualizaciones para indexar un producto.
     * Útil para usar dentro de una operación atómica updateChildren().
     */
    fun prepararIndicesParaFirebase(producto: MoldeProductos): Map<String, Any?> {
        val updates = mutableMapOf<String, Any?>()
        val id = producto.indice
        if (id.isBlank()) return emptyMap()

        // 1. Indexar por Nombre (Tokenización)
        val nombreTokens = normalizarYTokenizar(producto.nombre)
        nombreTokens.forEach { token ->
            updates["${DbPaths.INDICE_NOMBRE}/$token/$id"] = true
        }

        // 2. Indexar por Categoría
        val categoriaToken = normalizarTexto(producto.categoria)
        if (categoriaToken.isNotBlank()) {
            updates["${DbPaths.INDICE_CATEGORIA}/$categoriaToken/$id"] = true
        }

        // 3. Indexar por Síntomas (Keywords de la IA)
        val sintomasTokens = producto.referenceUseCases + producto.referenceKeywords + listOf(producto.referenceCommonUse)
        sintomasTokens.flatMap { normalizarYTokenizar(it) }.distinct().forEach { token ->
            if (token.length >= 3) {
                updates["${DbPaths.INDICE_SINTOMAS}/$token/$id"] = true
            }
        }

        // 4. Indexar por Ubicación
        if (producto.ubicacion.isNotBlank()) {
            val ubicacionTokens = normalizarYTokenizar(producto.ubicacion)
            ubicacionTokens.forEach { token ->
                updates["${DbPaths.INDICE_NOMBRE}/$token/$id"] = true // Reusamos índice de nombres para ubicación
            }
        }

        // 5. Atributos especiales (Sin código)
        if (!producto.tieneCodigoBarra && producto.codigo.isBlank()) {
            updates["${DbPaths.INDICE_ATRIBUTOS}/sin_codigo/$id"] = true
        }

        return updates
    }

    fun normalizarTexto(texto: String): String {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase(Locale.getDefault())
            .trim()
    }

    fun normalizarYTokenizar(texto: String): List<String> {
        return normalizarTexto(texto)
            .split(Regex("[^a-z0-9]+"))
            .filter { it.length >= 2 }
    }
}
